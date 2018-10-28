package com.redislabs;

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

//from time import time
//
//import sys
//from pykafka import KafkaClient
//from pykafka.common import OffsetType
//import json
//from uuid import uuid4

public class Blockchain {
  
  final private static String BLOCKCHAIN_TOPIC = "blockchain";
  final private static String TX_GROUP = "tx_group";
  final private static String TX_TOPIC = "transactions";
  final private static String BLOCKCHAIN_GROUP = "blockchain_group";
  final private static String TEST_KAFKA_BROKER = "kafka:9092";
  
  final private String partition;
  final private int last_offset;
  final private List<Object> blockchain;
  final private List<Transaction> current_transactions;
  final private String node_identifier;
  final private Jedis jedis;

  public Blockchain(String partition) {
    this.blockchain = new ArrayList<>();
    this.current_transactions = new ArrayList<>();
    this.node_identifier = UUID.randomUUID().toString().replace("-", "");
    this.last_offset = 0;
    this.partition = partition;
    this.jedis = new Jedis();
  }

  public void start() {
    this.initialize_chain();
    this.read_and_validate_chain();
    this.read_transactions(this.partition);
  }

  private void initialize_chain() {
    if (this.find_highest_offset() == 0){
      this.publish_block(this.genesis_block());
    }
  }

  private void read_and_validate_chain( offset=OffsetType.EARLIEST) {
    topic = this.get_topic(BLOCKCHAIN_TOPIC);
        consumer = topic.get_simple_consumer(
            consumer_group=BLOCKCHAIN_GROUP,
            auto_commit_enable=True,
            auto_offset_reset=offset,
            reset_offset_on_start=True,
            consumer_timeout_ms=5000);

        for (message : consumer){
          if (message) {
            block = json.loads(message.value.decode("utf-8"));
            // Skip validating the genesis block
            if (message.offset == 0){
              this.blockchain.append(block);
            } else if (this.valid_block(this.blockchain[-1], block)) {
              this.blockchain.append(block);
            }
            this.last_offset = message.offset + 1;
          }
        }
  }

  private void read_transactions( String partition) {
    System.out.println("Waiting for transactions on partition " + partition);
//    int tx_count = 0;

    //    topic = this.get_topic(TX_TOPIC);
//    partition = topic.partitions[int(partition)]
//        consumer = topic.get_simple_consumer(
//            consumer_group=TX_GROUP,
//            auto_commit_enable=True,
//            auto_offset_reset=OffsetType.LATEST,
//            partitions=[partition])

     	Map.Entry<String, EntryID> stream = new AbstractMap.SimpleEntry<String, EntryID>(TX_TOPIC, null);
    
     	while(true) {
     		List<Entry<String, List<StreamEntry>>> results = jedis.xread(1, Long.MAX_VALUE, stream);
     		Entry<String, List<StreamEntry>> streamResult = results.get(0);
     		StreamEntry streamEntry = streamResult.getValue().get(0);
     		
     		System.out.println(streamEntry);
     		
     		Map<String, String> transaction = streamEntry.getFields();
     		
//            tx_count += 1;
//            transaction = message.value.decode("utf-8");
            
//            new_tx = json.loads(transaction);
            this.new_transaction(transaction.get("from"),transaction.get("to"), Integer.parseInt(transaction.get("amount")));
            // Create a new block every 3 transactions
            if (this.current_transactions.size() >= 3) {
              this.mine();
              this.current_transactions = new ArrayList<>();
            }
     		
     		
     		stream = new AbstractMap.SimpleEntry<String, EntryID>(TX_TOPIC, streamEntry.getID());
     	}
  }

  private void mine() {
    // First check if there's a new block available with a higher offset
    // than our internal copy. If so, rewind our offset and consume from
    // that offset to get latest changes checking that the newest additions
    // are valid blocks, and adding to our internal representation if so
    latest_offset = this.find_highest_offset();
        if (this.find_highest_offset() > this.last_offset) {
          System.out.println("New blocks found, appending to our chain");
          this.read_and_validate_chain(latest_offset);

          // Now we've achieved consensus, continue with adding our transactions
          // and making a new block.
          // First, run the proof of work algorithm to get the next proof
          last_proof = this.blockchain[-1]['proof']
              proof = this.proof_of_work(last_proof)

              // Reward ourselves for finding the proof with a new transaction
              this.new_transaction("0", this.node_identifier,1);

              // Publish the new block to add it to the chain
              this.publish_block(this.new_block(proof));
        }
  }

  private void get_topic( String topicName) {
	
    client = KafkaClient(TEST_KAFKA_BROKER);
    return client.topics[topicName];
  }

  private void publish_block( Block block) {
    topic = this.get_topic(BLOCKCHAIN_TOPIC);
    producer = topic.get_producer();
    // Add the block to our internal representation, and publish it
    this.blockchain.append(block);
    producer.produce(json.dumps(block).encode('utf-8'));
    this.last_offset += 1;
    System.out.println("Published block with proof {block['proof']}");
  }

  private int find_highest_offset() {
    latest = this.get_topic(BLOCKCHAIN_TOPIC).latest_available_offsets();
    // TODO: We are only using topic partition 0 at this point
    return latest[0].offset[0];
  }

  private Block genesis_block() {
    return new Block(1, time(), [], 100, 1);
  }

  /**
    Create a new Block in the Blockchain
    :param proof: <int> The proof given by the Proof of Work algorithm
    :param previous_hash: (Optional) <str> Hash of previous Block
    :return: <dict> New Block
   */
  private Block new_block( String proof) {
    String previous_hash = this.hash(this.blockchain[-1]);
    return new Block(len(this.blockchain) + 1, time(), this.current_transactions, proof, previous_hash);
  }

  private void new_transaction( String sender, String recipient, int amount) {
    this.current_transactions.add(new Transaction( sender, recipient, amount));
  }

  /**
    Simple Proof of Work Algorithm:
      - Find a number p' such that hash(pp') contains leading 4 zeroes, where p is the previous p'
- p is the previous proof, and p' is the new proof
:param last_proof: <int>
      :return: <int>
   */
  private int proof_of_work( last_proof) {


      int proof = 0;
      while (!this.valid_proof(last_proof, proof)) {
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
  private boolean valid_proof( int last_proof, int proof) {

            guess = f'{last_proof}{proof}'.encode();
            guess_hash = hashlib.sha256(guess).hexdigest();

            return guess_hash[:4] == "0000";
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

  private boolean valid_block( Block last_block, Block block) {
    // Check each block, add to our local copy if it's valid
    if (block.getPrevisousHash().equals(last_block.hash()))
      return false;

    // Check that the Proof of Work is correct
    if (!this.valid_proof(last_block.getProof(), block.getProof())) {
      return false;
    }
    return true;
  }
}









