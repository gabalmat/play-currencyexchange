
import com.google.inject.AbstractModule;

import actors.MarketActor;
import actors.UserActor;
import play.libs.akka.AkkaGuiceSupport;

public class Module extends AbstractModule implements AkkaGuiceSupport {

	@Override
	protected void configure() {
		bindActor(UserActor.class, "user-actor");
		bindActor(MarketActor.class, "market-actor");
	}

}