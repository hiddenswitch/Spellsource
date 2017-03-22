package com.hiddenswitch.proto3.net.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.AsyncResult;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by bberman on 2/21/17.
 */
public class AllianceGameEventListener implements IGameEventListener {
	private final ServiceProxy<Logic> logic;
	private final String gameId;
	private transient final GameContext context;

	public AllianceGameEventListener(ServiceProxy<Logic> logicProxy, GameContext context, String gameId) {
		this.logic = logicProxy;
		this.gameId = gameId;
		this.context = context;
	}

	/**
	 * These are the current events we are listening to.
	 */
	private Set<GameEventType> types = EnumSet.of(GameEventType.BEFORE_SUMMON);

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

				if (event2.getAttacker().hasAllianceEffects()) {
					response = logic.uncheckedSync().afterPhysicalAttack(request1);
				} else {
					logic.async((AsyncResult<LogicResponse> ignored) -> {
						// TODO: Do nothing really
					}).afterPhysicalAttack(request1);
				}
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
				if (event1.getMinion().hasAllianceEffects()) {
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
	public IGameEventListener clone() {
		return new AllianceGameEventListener(logic, context, gameId);
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
}
