package net.demilich.metastone.game.spells.desc.manamodifier;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;

public enum CardCostModifierArg {
	/**
	 * The {@link Class<CardCostModifier>} of this description.
	 */
	CLASS,
	/**
	 * The card type to filter against.
	 *
	 * @see CardCostModifier#appliesTo(GameContext, Card, Player) for more on how this attribute is interpreted.
	 */
	CARD_TYPE,
	/**
	 * An attribute that is required for this modifier to apply.
	 *
	 * @see CardCostModifier#appliesTo(GameContext, Card, Player) for more on how this attribute is interpreted.
	 */
	REQUIRED_ATTRIBUTE,
	/**
	 * When this trigger fires, the {@link CardCostModifier} stops affecting the {@link Card} it applies to.
	 */
	EXPIRATION_TRIGGER,
	/**
	 * Specifies the minimum value that the {@link CardCostModifier} can reduce the cost to.
	 * <p>
	 * Implements Summoning Portal.
	 */
	MIN_VALUE,
	/**
	 * The value that will be interpreted by the given {@link #OPERATION} to determine the final cost effect.
	 */
	VALUE,
	/**
	 * The card's race that is required for this modifier to apply.
	 *
	 * @see CardCostModifier#appliesTo(GameContext, Card, Player) for more on how this attribute is interpreted.
	 */
	RACE,
	/**
	 * The player against which we should evaluate this {@link CardCostModifier}, considering the {@link
	 * CardCostModifier#getHostReference()} and {@link CardCostModifier#getOwner()}.
	 *
	 * @see CardCostModifier#appliesTo(GameContext, Card, Player) for more on how this attribute is interpreted.
	 */
	TARGET_PLAYER,
	/**
	 * A trigger that, when fired, turns on this {@link CardCostModifier} effect.
	 */
	TOGGLE_ON_TRIGGER,
	/**
	 * A trigger that, when fired, turns off this {@link CardCostModifier} effect.
	 */
	TOGGLE_OFF_TRIGGER,
	/**
	 * The entities that this {@link CardCostModifier} should apply to.
	 *
	 * @see CardCostModifier#appliesTo(GameContext, Card, Player) for more on how this attribute is interpreted.
	 */
	TARGET,
	/**
	 * A filter to apply to the {@link #TARGET}.
	 *
	 * @see CardCostModifier#appliesTo(GameContext, Card, Player) for more on how this attribute is interpreted.
	 */
	FILTER,
	/**
	 * An {@link AlgebraicOperation} that corresponds to how the cost modification should apply.
	 * <p>
	 * The best way to interpret this operation is, for a given card entity:
	 * <p>
	 * {@link Card#getBaseManaCost()} {@link AlgebraicOperation} {@link #VALUE}.
	 * <p>
	 * If there are multiple card cost modifiers applying on the given {@link Card}, then the game evaluates the
	 * modifiers that came into play earliest, and uses their resulting values on the left-hand side.
	 * <p>
	 * For <b>example</b>, to reduce the cost of the card by {@code 1}, this attribute should be set to {@link
	 * AlgebraicOperation#SUBTRACT} and the {@link #VALUE} should be {@code 1}.
	 *
	 * @see net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicValueProvider#evaluateOperation(int, int,
	 * AlgebraicOperation) to see more on how operations are evaluated.
	 */
	OPERATION
}
