package com.hiddenswitch.proto3.net.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.AsyncResult;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by bberman on 2/21/17.
 */
public class AllianceGameEventListener implements IGameEventListener {
	private final ServiceProxy<Logic> logic;
	private final String gameId;

	public AllianceGameEventListener(ServiceProxy<Logic> logicProxy, String gameId) {
		logic = logicProxy;
		this.gameId = gameId;
	}

	/**
	 * These are the current events we are listening to.
	 */
	private Set<GameEventType> types = EnumSet.of(GameEventType.BEFORE_SUMMON);

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		switch (event.getEventType()) {
			case BEFORE_SUMMON:
				final BeforeSummonEvent event1 = (BeforeSummonEvent) event;
				final String cardInstanceId = (String) event1.getSource().getAttributes().getOrDefault(Attribute.CARD_INVENTORY_ID, null);
				if (cardInstanceId == null) {
					// Can't process a non-alliance card.
					return;
				}

				final EventLogicRequest<BeforeSummonEvent> request = new EventLogicRequest<>();
				request.setEvent(event1);
				request.setCardInventoryId(cardInstanceId);
				// Check if the entity has network sideeffects it needs to be notified about. Otherwise, do
				// not wait.
				if (event1.getMinion().hasAllianceEffects()) {
					LogicResponse response = logic.uncheckedSync().beforeSummon(request);
				} else {
					// If we don't have effects we don't need to wait.
					logic.async((AsyncResult<LogicResponse> response) -> {
						// TODO: Do nothing really
					}).beforeSummon(request);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public IGameEventListener clone() {
		return new AllianceGameEventListener(logic, gameId);
	}

	@Override
	public boolean canFire(GameEvent event) {
		return types.contains(event);
	}

	@Override
	public EntityReference getHostReference() {
		return EntityReference.NONE;
	}

	@Override
	public int getOwner() {
		// This listener has no owner
		return -1;
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
