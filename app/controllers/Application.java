package controllers;

import java.util.concurrent.CompletionStage;
import scala.compat.java8.FutureConverters;
import utils.Util;

import javax.inject.*;

import com.fasterxml.jackson.databind.node.ObjectNode;

import actors.UserActor.*;
import actors.MarketActor.*;
import akka.actor.*;
import akka.japi.Function;
import play.mvc.*;

import static akka.pattern.Patterns.ask;

@Singleton
public class Application extends Controller {

	final ActorRef userActor;
	final ActorRef marketActor;
	
	@Inject 
	public Application(@Named("user-actor") ActorRef userActor, @Named("market-actor") ActorRef marketActor) {
		this.userActor = userActor;
		this.marketActor = marketActor;
	}
	
	public CompletionStage<Result> handleRequest(int maxrate, int amount) {
		return FutureConverters.toJava(ask(userActor, new BuyRequest(maxrate, amount), 5000))
				.thenApply(response -> ok(((ObjectNode) response).toString()));
	}
	
	public CompletionStage<Result> addUserBalance(int amount) {
		return FutureConverters.toJava(ask(userActor, new AddBalance(amount), 3000))
				.thenApply(response -> ok(Util.addBalanceResponse((String) response)));
	}
	
	public CompletionStage<Result> getUserBalance() {
		return FutureConverters.toJava(ask(userActor, new GetBalance(), 3000))
				.thenApply(response -> ok(((ObjectNode) response).toString()));
	}
	
	public CompletionStage<Result> debugFail() {
		return FutureConverters.toJava(ask(marketActor, new SetDebugFail(), 3000))
				.thenApply(response -> ok(((ObjectNode) response).toString()));
	}
	
	public CompletionStage<Result> debugNoResponse() {
		return FutureConverters.toJava(ask(marketActor, new SetDebugNoResponse(), 3000))
				.thenApply(response -> ok(((ObjectNode) response).toString()));
	}
	
	public CompletionStage<Result> debugReset() {
		return FutureConverters.toJava(ask(marketActor, new DebugReset(), 3000))
				.thenApply(response -> ok(((ObjectNode) response).toString()));
	}

}
