package actors;

import java.util.Map;
import javax.inject.Inject;

import scala.compat.java8.FutureConverters;
import java.util.concurrent.CompletionStage;

import actors.UserActor.*;
import akka.actor.*;
import akka.japi.*;
import models.SellOffer;
import play.libs.Json;
import services.SellOfferService;

import com.fasterxml.jackson.databind.node.ObjectNode;
import static akka.pattern.Patterns.ask;

public class MarketActor extends AbstractActor {
	
	public static boolean confirm_fail = false;
	public static boolean confirm_no_response = false;
	
	// Message class - process Hold Requests from UserActor
	public static class Hold {
		public String offerid;
		public int amount;
		public String transactionId;
		public boolean isLast;
		
		public Hold(Map.Entry<String, Integer> entry, String transactionId, boolean isLast) {
			this.offerid = entry.getKey();
			this.amount = entry.getValue();
			this.transactionId = transactionId;
			this.isLast = isLast;
		}
	}
	
	// Message class - handle debug confirm_fail
	public static class SetDebugFail {
		
		public SetDebugFail() {
			confirm_fail = true;
		}
	}
	
	// Message class - handle debug confirm_no_response
	public static class SetDebugNoResponse {
		
		public SetDebugNoResponse() {
			confirm_no_response = true;
		}
	}
	
	// Message class - handle debug reset
	public static class DebugReset {
		
		public DebugReset() {
			confirm_fail = false;
			confirm_no_response = false;
		}
	}
	
	private final SellOfferService sellOfferService;
	private ActorRef curUserActor;
	
	@Inject
	public MarketActor(SellOfferService sellOfferService) {
		this.sellOfferService = sellOfferService;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(Hold.class,  request -> {
				String transactionId = request.transactionId;
				boolean isSuccessful;
				curUserActor = getSender();
				
				try {
					SellOffer offer = sellOfferService.getSellOffer(request.offerid);
					if (offer.amount >= request.amount) {
						offer.amount -= request.amount;
						
						isSuccessful = true;
						CompletionStage<Object> future = FutureConverters.toJava(ask(getSender(), new HoldSuccess(isSuccessful, transactionId), 3000));
						future.thenApply(response -> {
							if (confirm_no_response) {
								// do nothing
							} else {
								// Confirm Request
								if (!confirm_fail && response.toString().equals("Confirm")) {
									curUserActor.tell(new ConfirmSuccess(isSuccessful, offer.rate, request.amount, transactionId, request.isLast), getSelf());
								} else {
									// Confirm Request FAIL
									curUserActor.tell(new ConfirmSuccess(false, 0, 0, transactionId, request.isLast), getSelf());
									offer.amount += request.amount;
								}
							}
							return response;
						});
					} else {
						// Hold FAIL
						isSuccessful = false;
						getSender().tell(new HoldSuccess(isSuccessful, transactionId), getSelf());
					}
				} catch (Exception ex) {
					// Hold timeout exception
					curUserActor.tell(new HandleException(ex, transactionId), getSelf());
				}
				
			})
			.match(SetDebugFail.class, request -> {
				ObjectNode node = Json.newObject();
				node.put("status", "success");
				getSender().tell(node, getSelf());
			})
			.match(SetDebugNoResponse.class, request -> {
				ObjectNode node = Json.newObject();
				node.put("status", "success");
				getSender().tell(node, getSelf());
			})
			.match(DebugReset.class, request -> {
				ObjectNode node = Json.newObject();
				node.put("status", "success");
				getSender().tell(node, getSelf());
			})
			.build();
	}
}
