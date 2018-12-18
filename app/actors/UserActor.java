package actors;

import actors.MarketActor.Hold;
import akka.actor.*;
import akka.japi.*;
import models.SellOffer;
import play.libs.Json;
import services.LogService;
import services.SellOfferService;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.*;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UserActor extends AbstractActor {
	
	@Inject @Named("market-actor")
	private ActorRef marketActor;
	
	// Message class - the max rate and amount for the request
	public static class BuyRequest {
		public final int maxrate;
		public final int amount;
		
		public BuyRequest(Integer maxrate, Integer amount) {
			this.maxrate = maxrate;
			this.amount = amount;
		}
	}
	
	// Message class - confirmation of hold requests from MarketActor
	public static class HoldSuccess {
		public boolean success;
		public String transactionId;
		
		public HoldSuccess(boolean success, String transactionId) {
			this.success = success;
			this.transactionId = transactionId;
		}
	}
	
	// Message class - confirmation of confirm requests from MarketActor
	public static class ConfirmSuccess {
		public boolean success;
		public String transactionId;
		public int rate;
		public int amount;
		public boolean isLastConfirm;
		
		public ConfirmSuccess(boolean success, int rate, int amount, String transactionId, boolean isLastConfirm) {
			this.success = success;
			this.rate = rate;
			this.amount = amount;
			this.transactionId = transactionId;
			this.isLastConfirm = isLastConfirm;
		}
	}
	
	// Message class - add to USD balance
	public static class AddBalance {
		public int amount;
		
		public AddBalance(int amount) {
			UserActor.usd += amount;
		}
	}
	
	// Message class - get the USD and BTC balance
	public static class GetBalance {
		public int usdAmount;
		public int btcAmount;
		
		public GetBalance() {
			usdAmount = UserActor.usd;
			btcAmount = UserActor.btc;
		}
	}
	
	// Message class - handle's timeout exception from MarketActor
	public static class HandleException {
		public Exception ex;
		public String transId;
		
		public HandleException(Exception ex, String transId) {
			this.ex = ex;
			this.transId = transId;
		}
	}
	
	
	public static int btc;
	public static int usd;
	private ActorRef controllerActor;
	private SellOfferService sellOfferService;
	private LogService logService;
	
	@Inject
	public UserActor(SellOfferService sellOfferService, LogService logService) {
		btc = 0;
		usd = 0;
		this.sellOfferService = sellOfferService;
		this.logService = logService;
		controllerActor = null;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(BuyRequest.class,  br -> {		// Request to purchase from Controller
				String transactionId = buildId();
				try {
					controllerActor = getSender();
					
					Map<String, Integer> holdMap = getOptimalOrders(br.maxrate, br.amount);
					
					if (holdMap == null || holdMap.isEmpty()) {
						// UserActor FAIL response
						ObjectNode node = Json.newObject();
						ObjectNode dbNode = Json.newObject();
						
						String errorMsg = "Maxrate is too low or user doesn't have enough USD balance. Please try again.";
						node.put("status", "error");
						node.put("message", errorMsg);
						
						dbNode.put("transactionId", transactionId);
						logService.createLog(dbNode);		// log event
						
						// Send error to controller
						getSender().tell(node, getSelf());
					} else {
						boolean isLastHold = false;
						int counter = 1;
						// Send Hold Request
						for (Map.Entry<String,Integer> entry : holdMap.entrySet()) {
							if (counter < holdMap.size()) {
								marketActor.tell(new Hold(entry, transactionId, isLastHold), getSelf());
								counter++;
							} else {
								isLastHold = true;
								marketActor.tell(new Hold(entry, transactionId, isLastHold), getSelf());
							}
						}
					}
				} catch (Exception ex) {
					// Buy Request FAIL
					ObjectNode node = Json.newObject();
					ObjectNode dbNode = Json.newObject();
					
		            String msg = "Buy Request failed. Exception was thrown: " + ex.getMessage();
		            node.put("status", "error");
		            node.put("message", msg);
		            controllerActor.tell(node, getSelf());
		            
		            dbNode.put("transactionId", transactionId);
		            logService.createLog(dbNode);		// log event
				}
			})
			.match(HoldSuccess.class, response -> {		// Response to Hold Request
				try {
					ObjectNode node = Json.newObject();
					ObjectNode dbNode = Json.newObject();
					
					if (response.success) {
						// Hold success
						dbNode.put("transactionId", response.transactionId);
						logService.createLog(dbNode);		// log event
						
						// Send Confirm Request
						getSender().tell("Confirm", getSelf());
					} else {
						// Hold FAIL
						String errorMsg = "Hold failed for transactionID " + response.transactionId;
						node.put("status", "error");
						node.put("message", errorMsg);
						
						dbNode.put("transactionId", response.transactionId);
						logService.createLog(dbNode);		// log event
						
						// Send error to controller
						controllerActor.tell(node, getSelf());
					}
				} catch (Exception ex) {
					// Hold timeout exception response
					ObjectNode node = Json.newObject();
					ObjectNode dbNode = Json.newObject();
					
					String errorMsg = "Hold timeout expired for transactionID " + response.transactionId;
					node.put("status", "error");
					node.put("message", errorMsg);
					
					dbNode.put("transactionId", response.transactionId);
					logService.createLog(dbNode);		// log event
					
					// Send error to controller
					controllerActor.tell(node, getSelf());
				}
			})
			.match(ConfirmSuccess.class,  response -> {		// Response to Confirm Request
				ObjectNode dbNode = Json.newObject();
				ObjectNode retNode = Json.newObject();
				try {
					if (response.success) {
						// Confirm success
						int deduct = response.rate * response.amount;
						UserActor.usd -= deduct;
						UserActor.btc += response.amount;
						
						if (response.isLastConfirm) {
							retNode.put("status", "succes");
							retNode.put("transactionID", response.transactionId);
							
							dbNode.put("transactionId", response.transactionId);
							dbNode.put("amountBtc", UserActor.btc);
							
							logService.createLog(dbNode);	// log event
							
							// Send success message to controller
							controllerActor.tell(retNode, getSelf());
						}
					} else {
						// Confirm FAIL
						throw new Exception("Confirm request failed");
					}
				} catch (Exception ex) {
					// Confirm FAIL
					retNode.put("status", "error");
					retNode.put("message", ex.getMessage());
					
					dbNode.put("transactionId", response.transactionId);
					logService.createLog(dbNode);	// log event
					
					// Send error to controller
					controllerActor.tell(retNode, getSelf());
				}
			})
			.match(AddBalance.class,  request -> {		// Add Balance request from controller
				try {
					getSender().tell("success", getSelf());
				} catch (Exception ex) {
					getSender().tell("error - " + ex.getMessage(), getSelf());
				}
			})
			.match(GetBalance.class, balance -> {		// Get Balance request from controller
				ObjectNode result = Json.newObject();
				try {
					result.put("status", "success");
					result.put("usd", balance.usdAmount);
					result.put("btc", balance.btcAmount);
					getSender().tell(result, getSelf());
				} catch (Exception ex) {
					result.removeAll();
					result.put("status", "error - " + ex.getMessage());
					getSender().tell(result, getSelf());
				}
				
			})
			.match(HandleException.class, response -> {
				ObjectNode node = Json.newObject();
				ObjectNode dbNode = Json.newObject();
				
	            String msg = "Buy Request failed. Exception was thrown: " + response.ex.getMessage();
	            node.put("status", "error");
	            node.put("message", msg);
	            controllerActor.tell(node, getSelf());
	            
	            dbNode.put("transactionId", response.transId) ;
	            logService.createLog(dbNode);		// log event
			})
			.build();
	}
	
	private Map<String, Integer> getOptimalOrders(int maxrate, int amount) {
		Map<String, Integer> orders = new HashMap<>();
		int amountToBuy = amount;
		int amountToSpend = 0;
		
		List<String> reverseOrderedKeys = new ArrayList<String>(sellOfferService.sellOffers.keySet());
		Collections.reverse(reverseOrderedKeys);
		
		for (String key : reverseOrderedKeys) {
			SellOffer offer = sellOfferService.sellOffers.get(key);
			
			// determine order
			if (amountToBuy > 0) {
				if (offer.rate <= maxrate) {
					if (offer.amount >= amountToBuy) {
						orders.put(offer.offerId, amountToBuy);
						amountToSpend += offer.rate * amountToBuy;
						break;
					} else {
						orders.put(offer.offerId, offer.amount);
						amountToSpend += offer.rate * offer.amount;
						amountToBuy -= offer.amount;
					}
				} else {
					return null;
				}
			}
		}
		
		// verify that user has enough money for order
		if (amountToSpend > UserActor.usd) {
			return null;
		}
		
		return orders;
		
	}
	
	static final String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
	static SecureRandom rnd = new SecureRandom();
	private String buildId() {
		StringBuilder sb = new StringBuilder(5);
		for (int i = 0; i < 5; i++) {
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}
}
