package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.TargetType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
	private EventTriggerDesc desc;

	public EventTrigger(EventTriggerDesc desc) {
		this.desc = desc;
	}

	@Override
	public EventTrigger clone() {
		return (EventTrigger) super.clone();
	}

	private boolean determineTargetPlayer(GameEvent event, TargetPlayer targetPlayer, Entity host, int targetPlayerId, int enchantmentOwnerId) {
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
				return enchantmentOwnerId != targetPlayerId;
			case OWNER:
				return host.getOwner() == targetPlayerId;
			case SELF:
				return enchantmentOwnerId == targetPlayerId;
			case PLAYER_1:
				return targetPlayerId == GameContext.PLAYER_1;
			case PLAYER_2:
				return targetPlayerId == GameContext.PLAYER_2;
			default:
				break;
		}
		return false;
	}

	/**
	 * When an event this trigger is {@link #interestedIn()} occurs, this test indicates whether or not the trigger should
	 * enter the queue of effects that should be evaluated. This is distinct from whether or not
	 *
	 * @param event
	 * @param enchantment
	 * @param host
	 * @return
	 */
	protected abstract boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host);

	public final boolean queues(GameEvent event, Enchantment enchantment, Entity host, int playerId) {
		var targetPlayer = getDesc().getTargetPlayer();
		if (targetPlayer != null && !determineTargetPlayer(event, targetPlayer, host, event.getTargetPlayerId(), playerId)) {
			return false;
		}

		var sourcePlayer = getDesc().getSourcePlayer();
		if (sourcePlayer != null && !determineTargetPlayer(event, sourcePlayer, host, event.getSourcePlayerId(), playerId)) {
			return false;
		}

		if (!hostConditionMet(event, host)) {
			return false;
		}

		var sourceEntityType = (EntityType) getDesc().get(EventTriggerArg.SOURCE_ENTITY_TYPE);
		var source = event.getSource();
		if (source != null && sourceEntityType != null && sourceEntityType != source.getEntityType()
				|| (source == null && sourceEntityType != null)) {
			return false;
		}

		var targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if ((event.getTarget() != null && targetEntityType != null && !Entity.hasEntityType(event.getTarget().getEntityType(), targetEntityType))
				|| (event.getTarget() == null && targetEntityType != null)) {
			return false;
		}

		var condition = (Condition) getDesc().get(EventTriggerArg.QUEUE_CONDITION);

		// For compatibility purposes, the host's owner is used instead of the enchantment's owner when checking the
		// queue condition, and the source is the host for condition checking when the source is null
		var ownerId = enchantment.getOwner();
		if (enchantment.getSourceCard().getDesc().getFileFormatVersion() <= 1) {
			// playerId is the enchantment's owner
			ownerId = playerId;
			if (source == null) {
				source = host;
			}
		}
		var owner = event.getGameContext().getPlayer(ownerId);
		if (condition != null && !condition.isFulfilled(event.getGameContext(), owner, source, event.getTarget())) {
			return false;
		}
		// Hero power triggers are disabled if a {@link Attribute#HERO_POWERS_DISABLED} is in play.
		// Implements Death's Shadow interaction with Mindbreaker.
		if (host.getSourceCard() != null
				&& host.getSourceCard().getCardType() == CardType.HERO_POWER
				&& event.getGameContext().getLogic().heroPowersDisabled()) {
			return false;
		}
		return innerQueues(event, enchantment, host);
	}

	protected boolean hostConditionMet(GameEvent event, Entity host) {
		var hostTargetType = (TargetType) getDesc().get(EventTriggerArg.HOST_TARGET_TYPE);
		if (hostTargetType == TargetType.IGNORE_AS_TARGET && event.getTarget() == host) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_AS_SOURCE && event.getSource() == host) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_AS_SOURCE_CARD && event.getSource() == host.getSourceCard()) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_AS_TARGET_CARD && event.getTarget() == host.getSourceCard()) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_OTHER_TARGETS && event.getTarget() != host) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_OTHER_TARGET_CARDS && event.getTarget() != host.getSourceCard()) {
			return false;
		} else if (hostTargetType == TargetType.IGNORE_OTHER_SOURCES && event.getSource() != host) {
			return false;
		}
		return true;
	}

	public abstract EventTypeEnum interestedIn();

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("targetPlayer", getDesc().getTargetPlayer())
				.append("sourcePlayer", getDesc().getSourcePlayer())
				.append("hostTargetType", getDesc().get(EventTriggerArg.HOST_TARGET_TYPE))
				.append("queueCondition", getDesc().get(EventTriggerArg.QUEUE_CONDITION))
				.append("fireCondition", getDesc().get(EventTriggerArg.FIRE_CONDITION))
				.toString();
	}

	public boolean fires(GameEvent event, Entity host, int playerId) {
		var condition = (Condition) getDesc().get(EventTriggerArg.FIRE_CONDITION);
		var player = event.getGameContext().getPlayer(playerId);
		var source = event.getSource();
		if (source == null) {
			source = host;
		}
		if (condition != null && !condition.isFulfilled(event.getGameContext(), player, source, event.getTarget())) {
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
