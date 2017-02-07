package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.CardsImpl;
import com.hiddenswitch.proto3.net.impl.InventoryImpl;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Result;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.cards.CardSet;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by bberman on 1/22/17.
 */
@RunWith(VertxUnitRunner.class)
public class InventoryTest extends ServiceTest<InventoryImpl> {
	@Test
	public void testCreateCollection(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createCollectionSync);
	}

	@Suspendable
	private void createCollectionSync() throws InterruptedException, SuspendExecution {
		String userId = "user";
		
		CreateCollectionResponse createCollectionResponse = service
				.createCollection(CreateCollectionRequest.startingCollection(userId));
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<InventoryImpl>> done) {
		final InventoryImpl inventory = new InventoryImpl().withEmbeddedConfiguration();
		final CardsImpl cards = new CardsImpl();
		vertx.deployVerticle(cards, then -> {
			vertx.deployVerticle(inventory, then2 -> {
				done.handle(new Result<>(inventory));
			});
		});

	}
}
