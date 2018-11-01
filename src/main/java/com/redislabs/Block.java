package com.redislabs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

//public class Block implements Serializable{
	
public class Block { 
	
//	private static final long serialVersionUID = 1L;
	
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
	
//	public byte[] toBytes() {
//		try {
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			ObjectOutputStream out = new ObjectOutputStream(os);
//			this.writeObject(out);
//			return os.toByteArray();
//		} catch (IOException e) { // can't really happen
//			throw new RuntimeException(e);
//		}
//	}
//	
//	public void fromBytes(byte[] buffer) {
//		try {
//			ByteArrayInputStream is = new ByteArrayInputStream(buffer);
//			ObjectInputStream in = new ObjectInputStream(is);
//			this.readObject(in);
//		} catch (Exception e) { // can't really happen
//			throw new RuntimeException(e);
//		} 		
//	}
//
//	private void writeObject(ObjectOutputStream out) throws IOException{
//		
//		out.writeInt(this.index);
//		out.writeLong(this.time);
//		
//		int size = this.transactions.size();
//        out.writeInt(size);
//        for(Transaction trans : this.transactions) {
//        	out.writeObject(trans);
//        }
//		
//		out.writeInt(this.proof);
//		out.writeUTF(this.previsousHash);
//	}
//
//	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
//		this.index = in.readInt();
//		this.time = in.readLong();
//		
//		int size = in.readInt();
//		this.transactions = new ArrayList<Transaction>(size);
//        for(int i=0; i<size; ++i) {
//        	Transaction trans = (Transaction) in.readObject();
//        	this.transactions.add(trans);
//        }
//		
//        this.proof = in.readInt();
//        this.previsousHash = in.readUTF();
//	}
}