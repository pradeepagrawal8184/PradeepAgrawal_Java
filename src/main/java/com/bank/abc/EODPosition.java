package com.bank.abc;
public class EODPosition {
	String instrument;
	int account;
	String accountType;
	long quantity;
	long delta;
	public EODPosition(String instrument1,int account1,String accountType1,long quantity1,long delta1) {
		this.instrument = instrument1;
		this.account = account1;
		this.accountType = accountType1;
		this.quantity = quantity1;
		this.delta=delta1;
	}
	public String getInstrument() {
		return instrument;
	}
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	public int getAccount() {
		return account;
	}
	public void setAccount(int account) {
		this.account = account;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public long getDelta() {
		return delta;
	}
	public void setDelta(long delta) {
		this.delta = delta;
	}
	
}
