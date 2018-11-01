package com.redislabs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import redis.clients.jedis.EntryID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntry;

public class Blockchain {
  
  final private static String BLOCKCHAIN_TOPIC = "blockchain";
  final private static String TX_GROUP = "tx_group";
  final private static String TX_TOPIC = "transactions";
  final private static String BLOCKCHAIN_GROUP = "blockchain_group";
  final private static String TEST_KAFKA_BROKER = "kafka:9092";
  
  final private List<Block> blockchain = new ArrayList<>();
  final private List<Transaction> currentTransactions = new ArrayList<>();
  final private String nodeIdentifier = UUID.randomUUID().toString().replace("-", "");
  final private Jedis jedis = new Jedis();
  
  private EntryID lastOffset;

  public Blockchain() {
  }

  public void start() {
    this.initializeChain();
    this.readAndValidateChain(null);
    this.readTransactions();
  }

  private void initializeChain() {
    if ( this.jedis.xlen(BLOCKCHAIN_TOPIC) == 0) {
    	this.publishBlock(this.genesisBlock());
    }
  }

  private void readAndValidateChain( EntryID offset) {

	  boolean first = true;
	  while(true){
		  List<Entry<String, List<StreamEntry>>> result = this.jedis.xread(1, 0, new AbstractMap.SimpleEntry<String, EntryID>(BLOCKCHAIN_TOPIC, offset));

		  if (result.size() == 0) {
			  break;
		  }
		  StreamEntry streamEntry = result.get(0).getValue().get(0);
		  if(streamEntry != null) {
			  offset = streamEntry.getID();
			  String message = streamEntry.getFields().get("block");
			  Block block = Block.fromJSON(message);
			  if (first){ // Skip validating the genesis block
				  if(this.blockchain.size() == 0) { //genesis is not already there
					  this.blockchain.add(block);
				  }
				  first = false;
			  } else if (this.validBlock(this.blockchain.get(this.blockchain.size() - 1), block)) {
				  this.blockchain.add(block);
			  }
			  this.lastOffset = offset;
			  continue;
		  }
	  }
  }

  private void readTransactions() {
    System.out.println("Waiting for transactions...");
//    int tx_count = 0;

    //    topic = this.getTopic(TX_TOPIC);
//    partition = topic.partitions[int(partition)]
//        consumer = topic.get_simple_consumer(
//            consumer_group=TX_GROUP,
//            auto_commit_enable=True,
//            auto_offset_reset=OffsetType.LATEST,
//            partitions=[partition])

     	Map.Entry<String, EntryID> stream = new AbstractMap.SimpleEntry<String, EntryID>(TX_TOPIC, null);
    
     	while(true) {
     		List<Entry<String, List<StreamEntry>>> results = jedis.xread(1, 10000000, stream);
     		Entry<String, List<StreamEntry>> streamResult = results.get(0);
     		StreamEntry streamEntry = streamResult.getValue().get(0);
     		
     		System.out.println(streamEntry);
     		
     		Map<String, String> transaction = streamEntry.getFields();
     		
//            tx_count += 1;
//            transaction = message.value.decode("utf-8");
            
//            new_tx = json.loads(transaction);
            this.newTransaction(transaction.get("from"),transaction.get("to"), Integer.parseInt(transaction.get("amount")));
            // Create a new block every 3 transactions
            if (this.currentTransactions.size() >= 3) {
              this.mine();
              this.currentTransactions.clear();
            }
     		
     		
     		stream = new AbstractMap.SimpleEntry<String, EntryID>(TX_TOPIC, streamEntry.getID());
     	}
  }

  private void mine() {
    // First check if there's a new block available with a higher offset
    // than our internal copy. If so, rewind our offset and consume from
    // that offset to get latest changes checking that the newest additions
    // are valid blocks, and adding to our internal representation if so
	  EntryID latestOffset = this.findHighestOffset();
        if (latestOffset.compareTo(this.lastOffset) > 0) {
          System.out.println("New blocks found, appending to our chain");
          this.readAndValidateChain(latestOffset);
        }

          // Now we've achieved consensus, continue with adding our transactions
          // and making a new block.
          // First, run the proof of work algorithm to get the next proof
          int last_proof = this.blockchain.get(this.blockchain.size() - 1).getProof();
          int proof = this.proofOfWork(last_proof);

          // Reward ourselves for finding the proof with a new transaction
          this.newTransaction("0", this.nodeIdentifier,1);

          // Publish the new block to add it to the chain
          this.publishBlock(this.newBlock(proof));
        
  }

  private void publishBlock( Block block) {
    // Add the block to our internal representation, and publish it
    this.blockchain.add(block);

    // Publish Block
    Map<String, String> entry = new HashMap<>();
    entry.put("block", block.toString());  
    
    this.lastOffset = jedis.xadd(BLOCKCHAIN_TOPIC, null, entry);
    System.out.println("Published block with proof " + block.getProof());
  }

  private EntryID findHighestOffset() {
	  List<StreamEntry> result = this.jedis.xrevrange(BLOCKCHAIN_TOPIC, null, null, 1);
	  return result.get(0).getID();
  }

  private Block genesisBlock() {
    return new Block(1, System.currentTimeMillis(), new ArrayList<>(), 100, "");
  }

  /**
  * Create a new Block in the Blockchain
  * 
  * :param proof: <int> The proof given by the Proof of Work algorithm
  * :param previousHash: (Optional) <str> Hash of previous Block
  * :return: <dict> New Block
  */
  private Block newBlock( int proof) {
    String previousHash = this.hash(this.blockchain.get(this.blockchain.size() - 1));
    return new Block(this.blockchain.size() + 1, System.currentTimeMillis(), this.currentTransactions, proof, previousHash);
  }

  private void newTransaction( String sender, String recipient, int amount) {
    this.currentTransactions.add(new Transaction( sender, recipient, amount));
  }

  /**
    Simple Proof of Work Algorithm:
      - Find a number p' such that hash(pp') contains leading 4 zeroes, where p is the previous p'
- p is the previous proof, and p' is the new proof
:param last_proof: <int>
      :return: <int>
   */
  private int proofOfWork( int last_proof) {


      int proof = 0;
      while (!this.validProof(last_proof, proof)) {
        proof += 1;
      }

        return proof;
  }
/**
    Validates the Proof: Does hash(last_proof, proof) contain 4 leading zeroes?
        :param last_proof: <int> Previous Proof
        :param proof: <int> Current Proof
        :return: <bool> True if correct, False if not.
 * @return
 */
  private boolean validProof( int last_proof, int proof) {

//            guess = f'{last_proof}{proof}'.encode();
//            guess_hash = hashlib.sha256(guess).hexdigest();
//
//            return guess_hash[:4] == "0000";
	  //TODO 
	  return true;
  }
/**
    Creates a SHA-256 hash of a Block
    :param block: <dict> Block
    :return: <str>
 */
  private String hash( Block block) {

    // We must make sure that the Dictionary is Ordered, or we'll have
    // inconsistent hashes
//    String block_string = json.dumps(block, sort_keys=True).encode();
//    hashlib.sha256(block_string).hexdigest();
  
	  return block.hash();
    
  }

  private boolean validBlock( Block lastBlock, Block block) {
    // Check each block, add to our local copy if it's valid
    if (block.getPrevisousHash().equals(lastBlock.hash()))
      return false;

    // Check that the Proof of Work is correct
    if (!this.validProof(lastBlock.getProof(), block.getProof())) {
      return false;
    }
    return true;
  }
  


}









