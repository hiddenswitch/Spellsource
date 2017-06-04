package com.hiddenswitch.proto3.net.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.annotations.Expose;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * A trigger that records persistent {@link Attribute} to a database. Think of it as analytics for {@link Entity}
 * objects where some analytics events have side effects on gameplay.
 * <p>
 * To implement a new persistence effect: <ul><li>Add an attribute to {@link Attribute} that describes the value you
 * want added to an {@link Entity} for looking up later.</li><li>Add the {@link GameEvent} that changes the {@link
 * Attribute} in {@link #types}.</li><li>Add a corresponding method to {@link Logic} for handling when that event
 * occurs, computing whatever you need to know and save to the database.</li><li>Make sure the attribute is included in
 * the {@link Logic#getDescriptionFromRecord(InventoryRecord, String, String)} method so that an {@link Entity},
 * typically cards in the player's deck, actually starts with the persisted values of the {@link
 * Attribute}.</li><li>Handle the event in {@link #onGameEvent(GameEvent)} and actually make the call to the {@link
 * #logic} to persist the new attribute value.</li></ul>
 * <p>
 * It's recommended to transfer as little data as possible to the
 * Unfortunately, this process is complicated. It could be simplified at a later date, but for this particular pipeline,
 * all aspects are unit and integration tested.
 * <p>
 * In games with persistence effects enabled, the {@link PersistenceTrigger} is added to a list of "other triggers" that
 * are just always running throughout a game. In Minionate, this trigger is added by a {@link
 * ServerGameContext#enablePersistenceEffects()} when it is created. This trigger should not be added to non-networked
 * game contexts, like a regular {@link GameContext}, because other code (like the AI) may use a game context for a
 * purpose other than hosting a two-player multiplayer networked match.
 * <p>
 * There are performance impacts of using a database for saving pieces of game state. In order to improve performance of
 * responding to game events, this implementation relies on an {@link Entity#hasPersistentEffects()} function to decide
 * whether it must wait for the consequences of persistent effects or not. When it does not have to wait for the
 * consequences of a persistence effect, this implementation makes a non-blocking call to the database, much like an
 * ordinary analytics method call would.
 * <p>
 * This class requires a reference to an {@link RpcClient} for {@link Logic}. It relies on the different ways RPC calls
 * can be made, like {@link RpcClient#sync()} versus {@link RpcClient#async(Handler)}.
 */
public class PersistenceTrigger implements Trigger {
	/**
	 * The {@link RpcClient} for the {@link Logic} service.
	 */
	@Expose(serialize = false, deserialize = false)
	private transient final RpcClient<Logic> logic;
	private final String gameId;
	@Expose(serialize = false, deserialize = false)
	private transient final GameContext context;

	public PersistenceTrigger(RpcClient<Logic> logic, GameContext context, String gameId) {
		this.logic = logic;
		this.gameId = gameId;
		this.context = context;
	}

	/**
	 * These are the current events we are listening to.
	 */
	private Set<GameEventType> types = EnumSet.of(GameEventType.BEFORE_SUMMON, GameEventType.AFTER_PHYSICAL_ATTACK);

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (isExpired()) {
			return;
		}

		LogicResponse response = null;

		switch (event.getEventType()) {
			case AFTER_PHYSICAL_ATTACK:
				final AfterPhysicalAttackEvent event2 = (AfterPhysicalAttackEvent) event;
				final String attackerInstanceId = event2.getAttacker().getCardInventoryId();
				final String defenderInstanceId = event2.getDefender().getCardInventoryId();
				if (attackerInstanceId == null
						|| defenderInstanceId == null) {
					// Can't process a non-alliance card.
					return;
				}

				final EventLogicRequest<AfterPhysicalAttackEvent> request1 = new EventLogicRequest<>();
				request1.setEvent(event2);
				request1.setCardInventoryId(attackerInstanceId);
				request1.setGameId(gameId);
				request1.setUserId(event2.getAttacker().getUserId());

				if (event2.getAttacker().hasPersistentEffects()) {
					response = logic.uncheckedSync().afterPhysicalAttack(request1);
				} else {
					logic.async((AsyncResult<LogicResponse> ignored) -> {
						// TODO: Do nothing really
					}).afterPhysicalAttack(request1);
				}
				break;
			case BEFORE_SUMMON:
				final BeforeSummonEvent event1 = (BeforeSummonEvent) event;
				final String cardInstanceId = event1.getMinion().getCardInventoryId();
				if (cardInstanceId == null) {
					// Can't process a non-alliance card.
					return;
				}

				final EventLogicRequest<BeforeSummonEvent> request = new EventLogicRequest<>();
				request.setEvent(event1);
				request.setCardInventoryId(cardInstanceId);
				request.setGameId(gameId);
				request.setUserId(event1.getMinion().getUserId());
				// Check if the entity has network side effects it needs to be notified about. Otherwise, do
				// not wait.
				if (event1.getMinion().hasPersistentEffects()) {
					response = logic.uncheckedSync().beforeSummon(request);
				} else {
					// If we don't have effects we don't need to wait.
					logic.async((AsyncResult<LogicResponse> ignored) -> {
						// TODO: Do nothing really
					}).beforeSummon(request);
				}
				break;
			default:
				break;
		}

		// Process the effects
		if (response != null) {
			if (!response.getGameIdsAffected().contains(event.getGameContext().getGameId())) {
				return;
			}

			for (Map.Entry<EntityReference, Map<Attribute, Object>> entry : response.getModifiedAttributes().entrySet()) {
				Entity entity = event.getGameContext().tryFind(entry.getKey());

				if (entity == null) {
					continue;
				}

				for (Map.Entry<Attribute, Object> kv : entry.getValue().entrySet()) {
					SpellDesc spell = SetAttributeSpell.create(entry.getKey(), kv.getKey(), kv.getValue());
					// By setting childSpell to true, additional spell casting triggers don't get called
					// But target overriding effects apply, as they should.
					event.getGameContext().getLogic().castSpell(entity.getOwner(), spell, entity.getReference(), null, true);
				}
			}
		}
	}

	@Override
	public Trigger clone() {
		return new PersistenceTrigger(logic, context, gameId);
	}

	@Override
	public boolean canFire(GameEvent event) {
		return types.contains(event.getEventType());
	}

	@Override
	public EntityReference getHostReference() {
		return EntityReference.NONE;
	}

	@Override
	public int getOwner() {
		// This listener is owned by the currently playing player
		return context.getActivePlayerId();
	}

	@Override
	public boolean interestedIn(GameEventType eventType) {
		return types.contains(eventType);
	}

	@Override
	public boolean isExpired() {
		return false;
	}

	@Override
	public void onAdd(GameContext context) {
	}

	@Override
	public void onRemove(GameContext context) {
	}

	@Override
	public void setHost(Entity host) {
	}

	@Override
	public void setOwner(int playerIndex) {
	}

	@Override
	public boolean hasPersistentOwner() {
		return true;
	}

	@Override
	public boolean oneTurnOnly() {
		return false;
	}

	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public void delayTimeDown() {
	}

	@Override
	public void expire() {
	}

	@Override
	public boolean canFireCondition(GameEvent event) {
		return true;
	}


	public Set<GameEventType> getTypes() {
		return types;
	}

	public void setTypes(Set<GameEventType> types) {
		this.types = types;
	}
}
