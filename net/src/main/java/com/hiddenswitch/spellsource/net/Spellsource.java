package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hiddenswitch.spellsource.net.impl.Trigger;
import com.hiddenswitch.spellsource.net.impl.util.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.vertx.core.*;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * The Spellsource Server API. Access it with {@link Spellsource#spellsource()}.
 * <p>
 * It will provide more APIs for features in the future.
 * <p>
 * When adding new collections, this class stores the migrations where index creation is appropriate.
 *
 * @see com.hiddenswitch.spellsource.net.applications.Clustered for the entry point of the executable.
 */
public class Spellsource {
	private static Spellsource instance;
	private final int gatewayPort;
	private List<DeckCreateRequest> cachedStandardDecks;
	private Map<String, PersistenceHandler> persistAttributeHandlers = new ConcurrentHashMap<>();
	private Map<String, Trigger> gameTriggers = new ConcurrentHashMap<>();
	private Map<String, Spell> spells = new ConcurrentHashMap<>();

	static {
		DatabindCodec.mapper().setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
	}

	protected Spellsource(int gatewayPort) {
		this.gatewayPort = gatewayPort;
	}

	/**
	 * Gets a reference to the Spellsource Server API.
	 *
	 * @return An API instance.
	 */
	public synchronized static Spellsource spellsource() {
		if (instance == null) {
			instance = new Spellsource(Configuration.apiGatewayPort());
		}

		return instance;
	}

	public synchronized static Spellsource spellsource(int gatewayPort) {
		return new Spellsource(gatewayPort);
	}

	/**
	 * Gets the location in the resources directory containing the decklists.
	 *
	 * @return
	 */
	private String getStandardDecksDirectoryPrefix() {
		return "decklists/current";
	}

	/**
	 * Gets the current deck lists specified in the decklists.current resources directory.
	 *
	 * @return A list of deck create requests without a {@link DeckCreateRequest#getUserId()} specified.
	 */
	public synchronized List<DeckCreateRequest> getStandardDecks() {
		if (cachedStandardDecks == null) {
			cachedStandardDecks = Collections.synchronizedList(new ArrayList<>());
			CardCatalogue.loadCardsFromPackage();
			List<String> deckLists;
			try (ScanResult scanResult = new ClassGraph()
					.disableRuntimeInvisibleAnnotations()
					.whitelistPaths(getStandardDecksDirectoryPrefix()).scan()) {
				deckLists = scanResult
						.getResourcesWithExtension(".txt")
						.stream()
						.map(resource -> {
							try {
								return resource.getContentAsString();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						})
						.collect(toList());

				if (deckLists.size() == 0) {
					throw new IllegalStateException("no bot decks were loaded");
				}
				cachedStandardDecks.addAll(deckLists.stream()
						.map((deckList) -> DeckCreateRequest.fromDeckList(deckList).setStandardDeck(true))
						.filter(Objects::nonNull)
						.collect(toList()));
			}
		}

		return cachedStandardDecks;
	}

	/**
	 * Persist an attribute when the given game event occurs, using the provided handler to compute the new value and to
	 * persist it with a {@link PersistenceContext#update(EntityReference, Object)} call inside the handler.
	 * <p>
	 * For example, let's say we want to persist the total amount of damage a minion has dealt:
	 * <pre>
	 *     {@code
	 *     		Spellsource.Spellsource().persistAttribute(
	 *              "total-damage-dealt-1",
	 *              com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PHYSICAL_ATTACK,
	 *              Attribute.LIFETIME_DAMAGE_DEALT,
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
	 *                  this event is currently taking place and in the entity's corresponding {@link InventoryRecord}
	 *                  where the value will be persisted in a database.
	 * @param <T>       The type of the event that corresponds to the provided {@link EventTypeEnum}.
	 */
	public <T extends GameEvent> Spellsource persistAttribute(String id, EventTypeEnum event, Attribute attribute, SuspendableAction1<PersistenceContext<T>> handler) {
		if (getPersistAttributeHandlers().containsKey(id)) {
			return this;
		}
		getPersistAttributeHandlers().put(id, new PersistenceHandler<>(handler, id, event, attribute));
		return this;
	}

	/**
	 * Configures a trigger to be added to the start of every game.
	 *
	 * @param id               An ID for this trigger.
	 * @param eventTriggerDesc The event this trigger should listen for.
	 * @param spell            The spell that should be casted by this event trigger desc.
	 * @return This Spellsource instance.
	 */
	public Spellsource trigger(String id, EventTriggerDesc eventTriggerDesc, Spell spell) {
		getSpells().put(id, spell);
		getGameTriggers().put(id, new Trigger(eventTriggerDesc, id));
		return this;
	}

	/**
	 * Deploys all the services needed to run an embedded server.
	 *
	 * @param vertx       A vertx instance.
	 * @param deployments A handler for the successful deployments. If any deployment fails, the entire handler fails.
	 */
	@Suspendable
	public void deployAll(Vertx vertx, Handler<AsyncResult<CompositeFuture>> deployments) {
		deployAll(vertx, Runtime.getRuntime().availableProcessors(), deployments);
	}

	@Suspendable
	public void deployAll(Vertx vertx, int concurrency, Handler<AsyncResult<CompositeFuture>> deployments) {
		List<Future> futures = new ArrayList<>();

		// Correctly use event loops
		for (Supplier<Verticle> verticle : services()) {
			Promise<String> future = Promise.promise();
			vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(concurrency), future);
			futures.add(future.future());
		}

		CompositeFuture.all(futures).setHandler(deployments);
	}

	protected List<Supplier<Verticle>> services() {
		return Arrays.asList(
				() -> Gateway.create(gatewayPort),
				Games::create
		);
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

	public Map<String, PersistenceHandler> getPersistAttributeHandlers() {
		return persistAttributeHandlers;
	}

	public void close() {
		instance = null;
	}

	public Map<String, Trigger> getGameTriggers() {
		return gameTriggers;
	}

	/**
	 * A map of spells that can be cast by {@link net.demilich.metastone.game.spells.desc.SpellArg#NAME} using a {@link
	 * DelegateSpell}.
	 *
	 * @return
	 */
	public Map<String, Spell> getSpells() {
		return spells;
	}

}
