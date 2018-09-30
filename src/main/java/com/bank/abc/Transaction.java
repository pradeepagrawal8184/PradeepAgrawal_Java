package com.bank.abc;

public class Transaction {
	String TransactionId;
	String Instrument;
	String TransactionType;
	int TransactionQuantity;
	public Transaction(String trxID,String inst,String trxType,int trxQty) {
		this.TransactionId = trxID;
		this.Instrument = inst;
		this.TransactionType = trxType;
		this.TransactionQuantity = trxQty;
	}
	public String getTransactionId() {
		return TransactionId;
	}
	public void setTransactionId(String transactionId) {
		TransactionId = transactionId;
	}
	public String getInstrument() {
		return Instrument;
	}
	public void setInstrument(String instrument) {
		Instrument = instrument;
	}
	public String getTransactionType() {
		return TransactionType;
	}
	public void setTransactionType(String transactionType) {
		TransactionType = transactionType;
	}
	public int getTransactionQuantity() {
		return TransactionQuantity;
	}
	public void setTransactionQuantity(int transactionQuantity) {
		TransactionQuantity = transactionQuantity;
	}
	
}
