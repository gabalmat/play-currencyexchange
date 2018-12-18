package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Log;
import play.libs.Json;

public class Util {
	
	public static ObjectNode getOfferidResponse(Object response, boolean ok) {
		
		ObjectNode result = Json.newObject();
		
		if (ok) {
			result.put("status", "success");
		} else {
			result.put("status", "error");
		}
		
		if (response instanceof String) {
			result.put("message",  (String) response);
		} else if (response instanceof JsonNode) {
			result.set("rate", ((JsonNode) response).get("rate"));
			result.set("amount", ((JsonNode) response).get("amount"));
		}
		
		return result;
	}
	
	public static ObjectNode getOfferidsResponse(String response, boolean ok) {
		
		ObjectNode result = Json.newObject();
		
		if (ok) {
			result.put("status", "success");
		} else {
			result.put("status", "error");
		}
		
		result.put("offers", response);
		return result;
	}
	
	public static ObjectNode addBalanceResponse(String response) {
		ObjectNode result = Json.newObject();
		result.put("status",  response);
		
		return result;
	}
	
	public static ObjectNode getTransactionIdsResponse(String response) {
		ObjectNode result = Json.newObject();
		result.put("status", "success");
		result.put("transactions", response);
		
		return result;
	}
	
	public static ObjectNode getTransactionResponse(Log log) {
		ObjectNode result = Json.newObject();
		result.put("status", "success");
		result.put("amount", log.amountBtc);
		
		return result;
	}
	
	public static ObjectNode getErrorResponse(String msg) {
		ObjectNode result = Json.newObject();
		result.put("status", "error");
		result.put("message", msg);
		
		return result;
	}

}
