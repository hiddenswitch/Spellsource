package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.TargetType;

import java.io.Serializable;

/**
 * This is the base class of all effects that react to events in the game.
 * <p>
 * These subclasses correspond to the {@code "class"} field on the {@code "eventTrigger"} property of the {@link
 * net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc}. For example, {@link TurnEndTrigger} corresponds to
 * the {@code "TurnEndTrigger"} string found in this {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc}
 * written on a card:
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *       "class": "TurnEndTrigger"
 *     },
 *     "spell": ...
 *   }
 * </pre>
 */
public abstract class EventTrigger extends CustomCloneable implements Serializable, HasDesc<EventTriggerDesc> {

	private int owner = -1;
	private EventTriggerDesc desc;

	public EventTrigger(EventTriggerDesc desc) {
		this.desc = desc;
	}

	@Override
	public EventTrigger clone() {
		return (EventTrigger) super.clone();
	}

	protected boolean determineTargetPlayer(GameEvent event, TargetPlayer targetPlayer, Entity host, int targetPlayerId) {
		if (targetPlayerId == -1 || targetPlayer == null) {
			return true;
		}
		switch (targetPlayer) {
			case ACTIVE:
				return event.getGameContext().getActivePlayerId() == targetPlayerId;
			case INACTIVE:
				return event.getGameContext().getActivePlayerId() != targetPlayerId;
			case BOTH:
				return true;
			case OPPONENT:
				return getOwner() != targetPlayerId;
			case OWNER:
				return host.getOwner() == targetPlayerId;
			case SELF:
				return getOwner() == targetPlayerId;
			case PLAYER_1:
				return targetPlayerId == GameContext.PLAYER_1;
			case PLAYER_2:
				return targetPlayerId == GameContext.PLAYER_2;
			default:
				break;
		}
		return false;
	}

	protected abstract boolean fire(GameEvent event, Entity host);

	public final boolean fires(GameEvent event, Entity host) {
		TargetPlayer targetPlayer = getDesc().getTargetPlayer();
		if (targetPlayer != null && !determineTargetPlayer(event, targetPlayer, host, event.getTargetPlayerId())) {
			return false;
		}
		TargetPlayer sourcePlayer = getDesc().getSourcePlayer();
		if (sourcePlayer != null && !determineTargetPlayer(event, sourcePlayer, host, event.getSourcePlayerId())) {
			return false;
		}


		if (!hostConditionMet(event, host)) {
			return false;
		}

		Condition condition = (Condition) getDesc().get(EventTriggerArg.QUEUE_CONDITION);
		Player owner = event.getGameContext().getPlayer(getOwner());
		if (condition != null && !condition.isFulfilled(event.getGameContext(), owner, event.getEventSource(), event.getEventTarget())) {
			return false;
		}
		// Hero power triggers are disabled if a {@link Attribute#HERO_POWERS_DISABLED} is in play.
		// Implements Death's Shadow interaction with Mindbreaker.
		if (host != null
				&& host.getSourceCard() != null
				&& host.getSourceCard().getCardType() == CardType.HERO_POWER
				&& event.getGameContext().getLogic().heroPowersDisabled()) {
			return false;
		}
		return fire(event, host);
	}

	protected boolean hostConditionMet(GameEvent event, Entity host) {
		TargetType hostTargetType = (TargetType) getDesc().get(EventTriggerArg.HOST_TARGET_TYPE);
		if (hostTargetType == TargetType.IGNORE_AS_TARGET && event.getEventTarget() == host) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_AS_SOURCE && event.getEventSource() == host) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_AS_SOURCE_CARD && event.getEventSource() == host.getSourceCard()) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_OTHER_TARGETS && event.getEventTarget() != host) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_OTHER_SOURCES && event.getEventSource() != host) {
			return false;
		}
		return true;
	}

	public int getOwner() {
		return owner;
	}

	public abstract GameEventType interestedIn();

	public void setOwner(int playerIndex) {
		this.owner = playerIndex;
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + " owner:" + owner + "]";
	}

	public boolean canFireCondition(GameEvent event) {
		Condition condition = (Condition) getDesc().get(EventTriggerArg.FIRE_CONDITION);
		Player owner = event.getGameContext().getPlayer(getOwner());
		if (condition != null && !condition.isFulfilled(event.getGameContext(), owner, event.getEventSource(), event.getEventTarget())) {
			return false;
		}
		return true;
	}

	@Override
	public EventTriggerDesc getDesc() {
		return desc;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (EventTriggerDesc) desc;
	}
}
