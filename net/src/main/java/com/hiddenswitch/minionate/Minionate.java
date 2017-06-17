package com.hiddenswitch.minionate;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.models.PersistAttributeRequest;
import com.hiddenswitch.proto3.net.models.PersistAttributeResponse;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.vertx.core.*;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Minionate Server API. Access it with {@link Minionate#minionate()}.
 * <p>
 * This class provides an easy way to provide a new persist attribute with {@link #persistAttribute(LegacyPersistenceHandler)}.
 * <p>
 * It will provide more APIs for features in the future.
 */
public class Minionate {
	private static Minionate instance;
	private Map<String, LegacyPersistenceHandler> legacyPersistenceHandlers = new HashMap<>();
	private Map<String, PersistenceHandler> persistAttributeHandlers = new HashMap<>();

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
		return new Persistence();
	}

	/**
	 * An internal utility class for implementing persistence features.
	 */
	public class Persistence {
		@SuppressWarnings("unchecked")
		@Suspendable
		public void persistenceTrigger(RpcClient<Logic> logic, GameEvent event) {
			// First, execute the regular handlers. They will persist normally.
			for (PersistenceHandler handler1 : persistAttributeHandlers.values()) {
				if (handler1.getType() != event.getEventType()) {
					continue;
				}

				handler1.getHandler().handle(new PersistenceContextImpl(event, logic, handler1.getId(), handler1.getAttribute()));
			}

			// Now, execute the legacy handlers.
			List<LogicResponse> responses = new ArrayList<>();
			for (LegacyPersistenceHandler handler2 : legacyPersistenceHandlers.values()) {
				if (!handler2.getGameEvent().equals(event.getEventType())) {
					continue;
				}

				EventLogicRequest request = handler2.onGameEvent(event);

				if (request == null) {
					continue;
				}

				PersistAttributeResponse response = logic.uncheckedSync().persistAttribute(new
						PersistAttributeRequest()
						.withId(handler2.getId()).withRequest(request));

				if (response.getLogicResponse() != null) {
					responses.add(response.getLogicResponse());
				}
			}

			for (LogicResponse response : responses) {
				GameContext context = event.getGameContext();
				for (Map.Entry<EntityReference, Map<Attribute, Object>> entry : response.getModifiedAttributes()
						.entrySet()) {

					Entity entity = context.tryFind(entry.getKey());

					if (entity == null) {
						continue;
					}

					for (Map.Entry<Attribute, Object> kv : entry.getValue().entrySet()) {
						SpellDesc spell = SetAttributeSpell.create(entry.getKey(), kv.getKey(), kv.getValue());
						// By setting childSpell to true, additional spell casting triggers don't get called
						// But target overriding effects apply, as they should.
						context.getLogic().castSpell(entity.getOwner(), spell, entity.getReference(), null, true);
					}
				}
			}
		}

		public LegacyPersistenceHandler getLogicHandler(String id) {
			return legacyPersistenceHandlers.get(id);
		}
	}
}
