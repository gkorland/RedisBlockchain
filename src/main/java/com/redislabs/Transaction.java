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
}