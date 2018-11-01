package com.redislabs;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

public class Block { 
	
	private final static Gson GSON = new Gson();
	
	final private int index;
	final private long time;
	final private List<Transaction>  transactions;
	final private int proof;
	final private String previsousHash;

	public Block(int index, long time, List<Transaction> transactions, int proof, String previousHash) {
		this.index = index;
		this.time = time;
		this.transactions = transactions;
		this.proof = proof;
		this.previsousHash = previousHash;
	}
	
	public String getPrevisousHash() {
		return this.previsousHash;
	}

	public int getProof() {
		return this.proof;
	}

	public String hash(){
		return DigestUtils.sha256Hex(this.toString());
	}
	
	public String toString() {
		return Block.GSON.toJson(this);
	}
	
	public static Block fromJSON(String json) {
		return Block.GSON.fromJson(json, Block.class);
	}
}