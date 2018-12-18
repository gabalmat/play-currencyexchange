package controllers;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import play.libs.Json;
import play.mvc.*;
import models.Log;
import models.SellOffer;
import services.LogService;
import services.SellOfferService;
import utils.Util;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
	
	@Inject SellOfferService sellOfferService;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }
    
    public Result getSellOffer(String offerid) {
    	SellOffer offer = sellOfferService.getSellOffer(offerid);
    	if (offer == null) {
    		return notFound(Util.getOfferidResponse(
    				"Offer with id: " + offerid + " not found", false));
    	}
    	
    	JsonNode jsonObjects = Json.toJson(offer);
    	return ok(Util.getOfferidResponse(jsonObjects, true));
    }
    
    public Result getSellOfferIds() {
    	Set<String> offers = sellOfferService.getSellOfferIds();
    	
    	if (offers.isEmpty()) {
    		return notFound(Util.getOfferidsResponse("No offer ids were found", false));
    	}
    	
    	return ok(Util.getOfferidsResponse(offers.toString(), true));
    }
    
    public Result getTransactionIds() {
    	List<String> list = LogService.getTransactionIds();
    	return ok(Util.getTransactionIdsResponse(list.toString()));
    }
    
    public Result getTransaction(String transactionId) {
    	Log log = LogService.getTransaction(transactionId);
    	if (log != null) {
    		return ok(Util.getTransactionResponse(log));
    	} 
    	
    	String msg = "Could not find transaction with given ID";
    	return ok(Util.getErrorResponse(msg));
    }

}
