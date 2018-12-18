package models;

public class SellOffer {
	
	public int rate;
	public int amount;
	public String offerId;
	
	public SellOffer(int rate, int amount, String offerId) {
		this.rate = rate;
		this.amount = amount;
		this.offerId = offerId;
	}
	
	// Using the Play Enhancer plugin so setters and getters are automatically handled
}
