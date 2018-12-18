package services;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.*;

import models.SellOffer;

@Singleton
public class SellOfferService {
	
	// Map containing entries for each sell offer. Keys are the sell offer ID and values are the SellOffer object
	public Map<String, SellOffer> sellOffers = new LinkedHashMap<>();
	
	// Constructor - creates 3 default instances of the SellOffer class and adds them to the Map
	public SellOfferService() {
		String offer1Id = "431671cb";
		String offer2Id = "16b961ed";
		String offer3Id = "1e06381d";
		
		SellOffer offer1 = new SellOffer(100, 5, offer1Id);
		SellOffer offer2 = new SellOffer(80, 2, offer2Id);
		SellOffer offer3 = new SellOffer(50, 12, offer3Id);
		
		sellOffers.put(offer1Id, offer1);
		sellOffers.put(offer2Id, offer2);
		sellOffers.put(offer3Id, offer3);
	}
	
	// Returns a list of sell offer IDs
	public Set<String> getSellOfferIds() {
		Set<String> offerids = sellOffers.keySet();
		
		return offerids;
	}
	
	// Returns the SellOffer instance belonging to the id parameter
	public SellOffer getSellOffer(String id) {
		SellOffer ret = null;
		
		if (sellOffers.containsKey(id)) {
			ret = sellOffers.get(id);
		}
		
		return ret;
	}
}
