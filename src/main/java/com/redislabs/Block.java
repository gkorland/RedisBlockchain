package com.redislabs;

import org.apache.commons.codec.digest.DigestUtils;

public class Block {
	final private int index;
	final private long time;
	final private Transaction[] transactions;
	final private int proof;
	final private String previsousHash;

	public Block(int index, long time, Transaction[] transactions, int proof, String previousHash) {
		this.index = index;
		this.time = time;
		this.transactions = transactions;
		this.proof = proof;
		this.previsousHash = previousHash;
	}
	
	
	public String getPrevisousHash() {
		return this.previsousHash;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{")
		.append("index:").append(this.index).append(",")
		.append("time:").append(this.time).append(",")
		.append("transactions:[");

		for(int i=0 ; i< transactions.length ;) {
			transactions[i].toString(builder);
			if(++i<transactions.length) { // don't append "," after the last transaction
				builder.append(",");
			}
		}
		
		builder
		.append("],")
		.append("proof:").append(this.proof).append(",")
		.append("previsousHash:").append(this.previsousHash)
		.append("}");
		
		return builder.toString();
	}

	public int getProof() {
		return this.proof;
	}
	
	public String hash() {
		return DigestUtils.sha256Hex(toString());
	}
	
//	public String hash() throws NoSuchAlgorithmException {
//		MessageDigest digest = MessageDigest.getInstance("SHA-256");
//		byte[] encodedhash = digest.digest( toString().getBytes(StandardCharsets.UTF_8));
//					//		      
//	}
}