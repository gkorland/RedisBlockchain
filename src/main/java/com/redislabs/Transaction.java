package com.redislabs;

//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.ObjectStreamException;
//import java.io.Serializable;

//public class Transaction implements Serializable{
	
public class Transaction {
	
//	private static final long serialVersionUID = 1L;
	
	final private String sender;
	final private String recipient;
	final private int amount;

	public Transaction(String sender, String recipient, int amount) {
		this.sender = sender;
		this.recipient = recipient;
		this.amount = amount;
	}
	
//	public String toString() {
//		return 
//				"{" +
//					"sender:\"" + this.sender + "\"," +
//					"recipient:\"" + this.recipient + "\"," +
//					"amount:" + this.amount +
//				"}";
//	}
//	
//	public void toString(StringBuilder builder) {
//		builder
//		.append("{")
//		.append("sender:\"").append(this.sender).append("\",")
//		.append("recipient:\"").append(this.recipient).append("\",")
//		.append("amount:\"").append(this.amount)
//		.append("}");
//	}
	
//	 private void writeObject(ObjectOutputStream out) throws IOException{
//		 out.writeUTF(this.sender);
//		 out.writeUTF(this.recipient);
//		 out.writeInt(this.amount);
//	 }
//	 
//	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
//		 this.sender = in.readUTF();
//		 this.recipient = in.readUTF();
//		 this.amount = in.readInt();
//	 }
}