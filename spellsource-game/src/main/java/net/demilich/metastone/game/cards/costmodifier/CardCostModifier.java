package net.demilich.metastone.game.cards.costmodifier;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.cards.desc.HasDescSerializer;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.trigger.AfterCardPlayedTrigger;
import net.demilich.metastone.game.spells.trigger.CardReceivedTrigger;
import net.demilich.metastone.game.spells.trigger.DidEndSequenceTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A card cost modifier.
 * <p>
 * In this <b>example</b>, the specified modifier reduces the hosting entity's cost by one.
 * <pre>
 *     {
 *         "class": "CardCostModifier",
 *         "target": "SELF",
 *         "operation": "SUBTRACT",
 *         "value": 1
 *     }
 * </pre>
 * <p>
 * When a target isn't specified, the card cost modification applies to the hand of the owner of this card cost modifier
 * (i.e. {@link EntityReference#FRIENDLY_HAND}.
 * <p>
 * To make a card cost modifier apply to both player's hands during both player's turns, {@link
 * CardCostModifierArg#TARGET_PLAYER} must be {@link TargetPlayer#BOTH} and the {@code target} should be {@link
 * EntityReference#BOTH_HANDS}.
 *
 * @see net.demilich.metastone.game.spells.CardCostModifierSpell for a spell that can put {@link CardCostModifier}
 * effects into play.
 * @see CardCostModifierArg for a list of arguments for card cost modification.
 */
@JsonSerialize(using = HasDescSerializer.class)
public class CardCostModifier extends Enchantment implements HasDesc<CardCostModifierDesc> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CardCostModifier.class);
	private static final EventTriggerDesc[] DEFAULT_TRIGGERS = new EventTriggerDesc[]{new EventTriggerDesc(CardReceivedTrigger.class), new EventTriggerDesc(AfterCardPlayedTrigger.class), new EventTriggerDesc(DidEndSequenceTrigger.class)};

	/**
	 * The default target reference is the {@link EntityReference#FRIENDLY_HAND}.
	 */
	private final EntityReference targetReference;
	private final Condition condition;
	private CardCostModifierDesc desc;

	public CardCostModifier(CardCostModifierDesc desc) {
		super(desc);
		// Updates at end of sequence and when cards are added to the hand
		setDesc(desc);

		var triggerDesc = (EventTriggerDesc) desc.get(CardCostModifierArg.EXPIRATION_TRIGGER);
		if (triggerDesc != null) {
			this.getExpirationTriggers().add(triggerDesc.create());
		}
		var triggerDescs = (EventTriggerDesc[]) desc.get(CardCostModifierArg.EXPIRATION_TRIGGERS);
		if (triggerDescs != null) {
			this.getExpirationTriggers().addAll(Arrays.stream(triggerDescs).map(EventTriggerDesc::create).collect(Collectors.toList()));
		}

		targetReference = (EntityReference) desc.getOrDefault(CardCostModifierArg.TARGET, EntityReference.FRIENDLY_HAND);
		condition = (Condition) desc.get(CardCostModifierArg.CONDITION);
		setZones(Enchantment.getDefaultBattlefieldZones());
	}

	/**
	 * Determines whether this modifier applies to the specified card.
	 * <p>
	 * First, the method checks if the modifier has been {@link #expired} due to its {@link
	 * CardCostModifierArg#EXPIRATION_TRIGGER}. For a {@link OneTurnCostModifier}, the expiration trigger is assumed to be
	 * a {@link net.demilich.metastone.game.spells.trigger.TurnEndTrigger}.
	 * <p>
	 * Then, the modifier evaluates its {@link CardCostModifierArg#TARGET} and {@link CardCostModifierArg#FILTER} and sees
	 * if the card is equal to or is contained within the resulting set of {@link Entity} objects. When not specified, the
	 * {@link CardCostModifierArg#TARGET} is assumed to be the {@link EntityReference#FRIENDLY_HAND}.
	 * <p>
	 * The {@link CardCostModifierArg#RACE} argument, if specified, is compared to the {@code card} instance's {@link
	 * Card#getRace()}.
	 * <p>
	 * The {@link CardCostModifierArg#CARD_TYPE} argument, if specified, is compared to the {@code card} instance's {@link
	 * Card#getCardType()}.
	 * <p>
	 * Finally, the {@link CardCostModifierArg#TARGET_PLAYER} argument, if specified, is compared to the {@code card}
	 * instance's {@link #getOwner()} with respect to the {@link #getHostReference()} of this modifier. For example, if
	 * {@link CardCostModifierArg#TARGET_PLAYER} is {@link TargetPlayer#SELF}, then the card's {@link Card#getOwner()} is
	 * compared to the {@link #getHostReference()} {@link Entity#getOwner()}.
	 *
	 * @param context The game context.
	 * @param card    The card to evaluate.
	 * @param player  The player from whose point of view this should be evaluated.
	 * @return {@code true} if the modifier applies to this card.
	 */
	public boolean appliesTo(GameContext context, Card card, Player player) {
		boolean applies;

		// Is it expired?
		applies = !expired;

		// If it's expired, don't continue evaluating
		if (!applies) {
			return false;
		}

		if (condition != null && !condition.isFulfilled(context, player, context.resolveSingleTarget(this.getHostReference()), card)) {
			return false;
		}

		if (Objects.equals(hostReference, EntityReference.NONE)) {
			LOGGER.error(String.format("appliesTo: The card cost modified from %s had no host reference.", getSourceCard().getCardId()));
			expire(context);
			return false;
		}

		// If a target reference is specified, does the target match?
		applies = !(targetReference != null
				&& !targetReference.isTargetGroup()
				&& !targetReference.equals(card.transformResolved(context).getReference()));

		if (!applies) {
			return false;
		}

		// If a target reference is a group reference, is the target in the valid list?
		Entity host;
		try {
			host = context.resolveSingleTarget(hostReference);
		} catch (NullPointerException notFound) {
			LOGGER.error(String.format("appliesTo: The card cost modifier from %s with desc %s has a host reference %s which could not be found", getSourceCard().getCardId(), getDesc().toString(), hostReference == null ? "(null)" : hostReference.toString()));
			expire(context);
			return false;
		}

		applies = !(targetReference != null
				&& targetReference.isTargetGroup()
				&& context.resolveTarget(player, host, targetReference)
				.stream().map(Entity::getId).noneMatch(eid -> eid == card.getId()));


		// If a required attribute is specified, does it match?
		applies &= !(getRequiredAttribute() != null
				&& !card.hasAttribute(getRequiredAttribute()));

		// If a target race is specified, does it match?
		applies &= !(getRequiredRace() != null
				&& !Race.hasRace(context, card, getRequiredRace()));

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
			case PLAYER_1:
				applies &= card.getOwner() == GameContext.PLAYER_1;
				break;
			case PLAYER_2:
				applies &= card.getOwner() == GameContext.PLAYER_2;
				break;
			case BOTH:
			case EITHER:
			default:
				break;
		}

		// Is this the correct card type
		applies &= !(getCardType() != null
				&& !GameLogic.isCardType(card.getCardType(), getCardType()));

		// If a filter is specified, does it satisfy the filter?
		applies &= !(getFilter() != null
				&& !getFilter().matches(context, player, card, host));

		return applies;
	}

	/**
	 * Returns the value of the specified {@link CardCostModifierArg}
	 *
	 * @param arg The argument
	 * @return The value
	 */
	protected Object get(CardCostModifierArg arg) {
		return desc.get(arg);
	}

	/**
	 * Gets the filter to apply to the {@link CardCostModifierArg#TARGET}.
	 *
	 * @return An {@link EntityFilter}
	 * @see #appliesTo(GameContext, Card, Player) to read more on how application is determined.
	 */
	protected EntityFilter getFilter() {
		return (EntityFilter) desc.get(CardCostModifierArg.FILTER);
	}

	protected CardType getCardType() {
		return (CardType) desc.get(CardCostModifierArg.CARD_TYPE);
	}

	public int getMinValue() {
		return desc.getInt(CardCostModifierArg.MIN_VALUE);
	}

	protected Attribute getRequiredAttribute() {
		return (Attribute) desc.get(CardCostModifierArg.REQUIRED_ATTRIBUTE);
	}

	protected String getRequiredRace() {
		return (String) get(CardCostModifierArg.RACE);
	}

	/**
	 * Gets the target player of the given card cost modifier.
	 * <p>
	 * This is relative to the owner of the modifier, which is typically the caster, not the owner of the {@link
	 * #getHostReference()}.
	 *
	 * @return {@link TargetPlayer#SELF} if not specified, otherwise the {@link  TargetPlayer} specified by the modifier.
	 */
	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) desc.getOrDefault(CardCostModifierArg.TARGET_PLAYER, TargetPlayer.SELF);
	}

	@Override
	public void onAdd(GameContext context, Player player, Entity source, Entity host) {
	}

	public int process(GameContext context, Entity host, Card card, int currentManaCost, Player player) {
		AlgebraicOperation operation = (AlgebraicOperation) desc.getOrDefault(CardCostModifierArg.OPERATION, AlgebraicOperation.ADD);
		int value = desc.getValue(CardCostModifierArg.VALUE, context, player, card, host, 0);
		return operation.performOperation(currentManaCost, value);
	}

	@Override
	public boolean isPersistentOwner() {
		return false;
	}

	public boolean targetsSelf() {
		return targetReference != null
				&& (targetReference.equals(EntityReference.SELF)
				|| targetReference.equals(hostReference));
	}

	@Override
	public CardCostModifierDesc getDesc() {
		return desc;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (CardCostModifierDesc) desc;
	}

	@Override
	protected EventTriggerDesc[] getDefaultTriggers() {
		return DEFAULT_TRIGGERS;
	}

	@Override
	public CardCostModifier clone() {
		// Conditions are stateless so they are safe to clone
		return (CardCostModifier) super.clone();
	}

	@Override
	protected void cast(int ownerId, SpellDesc spell, GameEvent event) {
		// Card cost modifiers have no effects
	}

	@Override
	protected Zones[] getDefaultZones() {
		return Enchantment.getDefaultBattlefieldZones();
	}

	@Override
	protected boolean shouldNotifyEnchantmentFired(GameEvent event) {
		return false;
	}

}
