package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.CardsImpl;
import com.hiddenswitch.spellsource.impl.InventoryImpl;
import com.hiddenswitch.spellsource.impl.ServiceTest;
import com.hiddenswitch.spellsource.models.CreateCollectionRequest;
import com.hiddenswitch.spellsource.models.CreateCollectionResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
	private void createCollectionSync() throws SuspendExecution, InterruptedException {
		String userId = "user";
		
		CreateCollectionResponse createCollectionResponse = service
				.createCollection(CreateCollectionRequest.startingCollection(userId));
	}

	@Test
	public void testCreateCollection2(TestContext context) {

	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<InventoryImpl>> done) {
		final InventoryImpl inventory = new InventoryImpl();
		final CardsImpl cards = new CardsImpl();
		vertx.deployVerticle(cards, then -> {
			vertx.deployVerticle(inventory, then2 -> {
				done.handle(Future.succeededFuture(inventory));
			});
		});

	}
}
