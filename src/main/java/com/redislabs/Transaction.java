package com.redislabs;

public class Transaction {
	final private String sender;
	final private String recipient;
	final private int amount;

	public Transaction(String sender, String recipient, int amount) {
		this.sender = sender;
		this.recipient = recipient;
		this.amount = amount;
	}
	
	public String toString() {
		return 
				"{" +
					"sender:\"" + this.sender + "\"," +
					"recipient:\"" + this.recipient + "\"," +
					"amount:" + this.amount +
				"}";
	}
	
	public void toString(StringBuilder builder) {
		builder
		.append("{")
		.append("sender:\"").append(this.sender).append("\",")
		.append("recipient:\"").append(this.recipient).append("\",")
		.append("amount:\"").append(this.amount)
		.append("}");
	}
}