package com.hiddenswitch.minionate;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.models.MigrateToRequest;
import com.hiddenswitch.proto3.net.models.MigrationToResponse;
import io.vertx.core.*;
import io.vertx.ext.sync.SuspendableRunnable;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hiddenswitch.proto3.net.util.Sync.suspendableHandler;

/**
 * The Minionate Server API. Access it with {@link Minionate#minionate()}.
 * <p>
 * This class provides an easy way to provide a new persist attribute with {@link #persistAttribute(LegacyPersistenceHandler)}.
 * <p>
 * It will provide more APIs for features in the future.
 */
public class Minionate {
	private static Minionate instance;
	Map<String, LegacyPersistenceHandler> legacyPersistenceHandlers = new HashMap<>();
	Map<String, PersistenceHandler> persistAttributeHandlers = new HashMap<>();

	private Minionate() {
	}

	/**
	 * Gets a reference to the Minionate Server API.
	 *
	 * @return An API instance.
	 */
	public static Minionate minionate() {
		if (instance == null) {
			instance = new Minionate();
		}

		return instance;
	}

	/**
	 * Persist an attribute when the given game event occurs, using the provided handler to compute the new value and
	 * to persist it with a {@link PersistenceContext#update(EntityReference, Object)} call inside the handler.
	 * <p>
	 * For example, let's say we want to persist the total amount of damage a minion has dealt:
	 * <pre>
	 *     {@code
	 *     		Minionate.minionate().persistAttribute(
	 *              "total-damage-dealt-1",
	 *              GameEventType.AFTER_PHYSICAL_ATTACK,
	 *              Attribute.TOTAL_DAMAGE_DEALT,
	 *              (PersistenceContext<AfterPhysicalAttackEvent> context) -> {
	 *                  int attackerDamage = context.event().getDamageDealt();
	 *                  context.update(context.event().getAttacker().getReference(), attackerDamage);
	 *              }
	 *          );
	 *     }
	 * </pre>
	 *
	 * @param id        A name of your choosing to uniquely identify this persistence handler.
	 * @param event     The type of event that this handler should be triggered for.
	 * @param attribute The attribute this handler will be persisting.
	 * @param handler   A handler that is passed a {@link PersistenceContext}, whose methods provide the event and a
	 *                  mechanism to update the entity with a new attribute value (both in the {@link GameContext} where
	 *                  this event is currently taking place and in the entity's corresponding {@link
	 *                  com.hiddenswitch.proto3.net.impl.util.InventoryRecord} where the value will be persisted in a
	 *                  database.
	 * @param <T>       The type of the event that corresponds to the provided {@link GameEventType}.
	 */
	public <T extends GameEvent> void persistAttribute(String id, GameEventType event, Attribute attribute, Handler<PersistenceContext<T>> handler) {
		persistAttributeHandlers.put(id, new PersistenceHandler<>(Sync.fiberHandler(handler), id, event, attribute));
	}

	/**
	 * @param legacyHandler A handler for game events and logic requests. See {@link LegacyPersistenceHandler#create(String,
	 *                      GameEventType, Function, Function)} for an easy way to create this handler.
	 * @param <T>           The event type.
	 */
	public <T extends GameEvent> void persistAttribute(LegacyPersistenceHandler<T> legacyHandler) {
		legacyPersistenceHandlers.put(legacyHandler.getId(), legacyHandler);
	}

	/**
	 * Deploys all the services needed to run an embedded server.
	 *
	 * @param vertx       A vertx instance.
	 * @param deployments A handler for the successful deployments. If any deployment fails, the entire handler fails.
	 */
	public void deployAll(Vertx vertx, Handler<AsyncResult<CompositeFuture>> deployments) {
		final List<SyncVerticle> verticles = Arrays.asList(new SyncVerticle[]{
				new CardsImpl(),
				new AccountsImpl(),
				new GamesImpl(),
				new MatchmakingImpl(),
				new BotsImpl(),
				new LogicImpl(),
				new DecksImpl(),
				new InventoryImpl(),
				new DraftImpl(),
				new GatewayImpl()});

		CompositeFuture.all(verticles.stream().map(verticle -> {
			final Future<String> future = Future.future();
			vertx.deployVerticle(verticle, future);
			return future;
		}).collect(Collectors.toList())).setHandler(deployments);
	}

	/**
	 * A sync version of {@link #deployAll(Vertx, Handler)}.
	 *
	 * @param vertx A vertx instance.
	 * @return The result. Failed if any deployment failed.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	public CompositeFuture deployAll(Vertx vertx) throws SuspendExecution, InterruptedException {
		return Sync.awaitResult(h -> deployAll(vertx, h));
	}

	/**
	 * Access non-client features required to implement the persistence features.
	 *
	 * @return A {@link Persistence} utility.
	 */
	public Persistence persistence() {
		return new Persistence(this);
	}

}
