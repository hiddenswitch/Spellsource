package net.demilich.metastone.game.spells.trigger;

import java.io.Serializable;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.targeting.EntityReference;

public interface IGameEventListener extends Serializable {

	IGameEventListener clone();

	boolean canFire(GameEvent event);

	EntityReference getHostReference();

	int getOwner();

	boolean interestedIn(GameEventType eventType);

	boolean isExpired();

	void onAdd(GameContext context);

	@Suspendable
	void onGameEvent(GameEvent event);

	@Suspendable
	void onRemove(GameContext context);

	void setHost(Entity host);

	void setOwner(int playerIndex);

	boolean hasPersistentOwner();

	boolean oneTurnOnly();

	boolean isDelayed();

	void delayTimeDown();

	void expire();

	boolean canFireCondition(GameEvent event);

}