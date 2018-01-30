package net.demilich.metastone.game.cards.costmodifier;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.io.Serializable;

public class CardCostModifier extends CustomCloneable implements Trigger, Serializable {
	private static Logger logger = LoggerFactory.getLogger(CardCostModifier.class);
	private boolean expired;
	private int owner;
	private EntityReference hostReference;
	/**
	 * The default target reference is the {@link EntityReference#FRIENDLY_HAND}.
	 */
	private EntityReference targetReference = EntityReference.FRIENDLY_HAND;
	private EventTrigger expirationTrigger;
	private CardCostModifierDesc desc;

	public CardCostModifier(CardCostModifierDesc desc) {
		this.desc = desc;
		EventTriggerDesc triggerDesc = (EventTriggerDesc) desc.get(CardCostModifierArg.EXPIRATION_TRIGGER);
		if (triggerDesc != null) {
			this.expirationTrigger = triggerDesc.create();
		}
		if (desc.containsKey(CardCostModifierArg.TARGET)) {
			targetReference = (EntityReference) desc.get(CardCostModifierArg.TARGET);
		}
	}

	public boolean appliesTo(GameContext context, Card card, Player player) {
		boolean applies = true;

		// Is it expired?
		applies &= !expired;

		// If it's expired, don't continue evaluating
		if (!applies) {
			return false;
		}

		// If a target reference is specified, does the target match?
		applies &= !(targetReference != null
				&& !targetReference.isTargetGroup()
				&& !targetReference.equals(card.transformResolved(context).getReference()));

		// If a target reference is a group reference, is the target in the valid list?
		final Entity host;
		try {
			host = context.resolveSingleTarget(hostReference);
		} catch (NullPointerException notFound) {
			logger.error("The card cost modifier's reference is not found.", hostReference);
			expire();
			throw notFound;
		}

		applies &= !(targetReference != null
				&& targetReference.isTargetGroup()
				&& context.resolveTarget(player, host, targetReference)
				.stream().map(Entity::getId).noneMatch(eid -> eid == card.getId()));


		// If a required attribute is specified, does it match?
		applies &= !(getRequiredAttribute() != null
				&& !card.hasAttribute(getRequiredAttribute()));

		// If a target race is specified, does it match?
		applies &= !(getRequiredRace() != null
				&& card.getAttribute(Attribute.RACE) != getRequiredRace());

		// Is the enchantment owner / caster the same as the card owner?
		switch (getTargetPlayer()) {
			case OPPONENT:
				applies &= card.getOwner() != getOwner();
				break;
			case SELF:
				applies &= card.getOwner() == getOwner();
				break;
			case ACTIVE:
				applies &= card.getOwner() == context.getActivePlayerId();
				break;
			case INACTIVE:
				applies &= card.getOwner() != context.getActivePlayerId();
				break;
			case OWNER:
				applies &= card.getOwner() == player.getOwner();
				break;
			default:
				break;
		}

		// Is this the correct card type
		applies &= !(getCardType() != null
				&& !card.getCardType().isCardType(getCardType()));

		// If a filter is specified, does it satisfy the filter?
		applies &= !(getFilter() != null
				&& !getFilter().matches(context, player, card, host));

		return applies;
	}

	@Override
	public boolean canFire(GameEvent event) {
		return true;
	}

	@Override
	public CardCostModifier clone() {
		CardCostModifier clone = (CardCostModifier) super.clone();
		clone.expirationTrigger = expirationTrigger != null ? expirationTrigger.clone() : null;
		return clone;
	}

	public void expire() {
		expired = true;
	}

	protected Object get(CardCostModifierArg arg) {
		return desc.get(arg);
	}

	protected EntityFilter getFilter() {
		return (EntityFilter) desc.get(CardCostModifierArg.FILTER);
	}

	protected CardType getCardType() {
		return (CardType) desc.get(CardCostModifierArg.CARD_TYPE);
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	public int getMinValue() {
		return desc.getInt(CardCostModifierArg.MIN_VALUE);
	}

	@Override
	public int getOwner() {
		return owner;
	}

	protected Attribute getRequiredAttribute() {
		return (Attribute) desc.get(CardCostModifierArg.REQUIRED_ATTRIBUTE);
	}

	protected Race getRequiredRace() {
		return (Race) get(CardCostModifierArg.RACE);
	}

	/**
	 * Gets the target player of the given card cost modifier.
	 * <p>
	 * This is relative to the owner of the modifier, which is typically the caster, not the owner of the
	 * {@link #getHostReference()}.
	 *
	 * @return {@link TargetPlayer#SELF} if not specified, otherwise the {@link  TargetPlayer} specified by the modifier.
	 */
	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) desc.getOrDefault(CardCostModifierArg.TARGET_PLAYER, TargetPlayer.SELF);
	}

	@Override
	public boolean interestedIn(GameEventType eventType) {
		if (expirationTrigger == null) {
			return false;
		}
		return eventType == expirationTrigger.interestedIn() || expirationTrigger.interestedIn() == GameEventType.ALL;
	}

	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	public void onAdd(GameContext context) {
	}

	@Override
	public void onGameEvent(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(getHostReference());
		if (expirationTrigger != null && event.getEventType() == expirationTrigger.interestedIn() && expirationTrigger.fires(event, host)) {
			expire();
		}
	}

	@Override
	public void onRemove(GameContext context) {
		expired = true;
	}

	public int process(GameContext context, Entity host, Card card, int currentManaCost, Player player) {
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(CardCostModifierArg.OPERATION);
		int value = desc.getValue(CardCostModifierArg.VALUE, context, player, card, host, 0);
		if (operation != null) {
			return operation.performOperation(currentManaCost, value);
		}
		return currentManaCost + desc.getInt(CardCostModifierArg.VALUE);
	}

	@Override
	public void setHost(Entity host) {
		hostReference = host.getReference();
	}

	@Override
	public void setOwner(int playerIndex) {
		this.owner = playerIndex;
		if (expirationTrigger != null) {
			expirationTrigger.setOwner(playerIndex);
		}
	}

	@Override
	public boolean hasPersistentOwner() {
		return false;
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
	public boolean canFireCondition(GameEvent event) {
		if (expirationTrigger != null) {
			return expirationTrigger.canFireCondition(event);
		}
		return true;
	}

	public boolean targetsSelf() {
		return targetReference != null
				&& (targetReference.equals(EntityReference.SELF)
				|| targetReference.equals(hostReference));
	}
}
