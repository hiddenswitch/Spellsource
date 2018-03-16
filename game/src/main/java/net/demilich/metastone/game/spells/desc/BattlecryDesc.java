package net.demilich.metastone.game.spells.desc;

import java.io.Serializable;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * The object describing a battlecry.
 * <p>
 * Like a spell, battlecries take {@link #targetSelection}, assumed to be {@link TargetSelection#NONE} if not specified
 * in the JSON.
 * <p>
 * For <b>example,</b> this battlecry summons a 2/1 minion:
 * <pre>
 *      {
 *          "spell": {
 *              "class": "SummonSpell",
 *              "boardPositionRelative": "RIGHT",
 *              "card": "token_ooze",
 *              "targetPlayer": "SELF"
 *          }
 *      }
 * </pre>
 * This battlecry deals 2 damage to the chosen minion:
 * <pre>
 *     {
 *         "targetSelection": "MINIONS",
 *         "spell": {
 *             "class": "DamageSpell",
 *             "value": 2
 *         }
 *     }
 * </pre>
 * This battlecry implements, "If you're holding a spell, deal 1 damage."
 * <pre>
 *     {
 *         "targetSelection": "ANY",
 *         "spell": {
 *             "class": "DamageSpell",
 *             "value": 1
 *         },
 *         "condition": {
 *             "class": "HoldsCardCondition",
 *             "cardFilter": {
 *                 "class": "CardFilter",
 *                 "cardType": "SPELL"
 *             }
 *         }
 *     }
 * </pre>
 * This battlecry is one of two for "Choose One - Deal 2 damage, or Draw a Card". Notice the flavorful name.
 * <pre>
 *     {
 *         "spell": {
 *             "class": "DrawCardSpell"
 *         },
 *         "name": "Study in the Library",
 *         "description": "Draw a Card"
 *     }
 * </pre>
 *
 * @see net.demilich.metastone.game.logic.GameLogic#performBattlecryAction(int, Actor, Player, BattlecryAction) to see
 * how the battlecry action is processed.
 */
public class BattlecryDesc implements Serializable {
	/**
	 * The spell to cast when this battlecry's {@link #condition} is true (or always cast if no condition is specified
	 * and a valid target is available).
	 */
	public SpellDesc spell;
	/**
	 * The targets the battlecry can choose from.
	 * <p>
	 * Battlecries only go into target selection if their {@link #condition} is met.
	 * <p>
	 * If target selection is specified and no valid targets are available, the battlecry is not cast.
	 */
	public TargetSelection targetSelection;
	/**
	 * The condition to evaluate if the player will be prompted to make a battlecry action.
	 * <p>
	 * The condition is also used to determine if the {@link net.demilich.metastone.game.cards.Card} in the
	 * player's hand should receive a "yellow glow" indicating its condition is met.
	 * <p>
	 * In order to implement this glow, it is preferred to specify a condition here rather than using a {@link
	 * net.demilich.metastone.game.spells.ConditionalSpell} in the {@link #spell} field.
	 */
	public ConditionDesc condition;
	/**
	 * A name used to render a card representing the battlecry. When not specified, the description is used instead.
	 * Used for choose-one battlecries.
	 * <p>
	 * If the {@link #spell} is a {@link net.demilich.metastone.game.spells.TransformMinionSpell}, the {@link
	 * SpellArg#CARD} of that spell (the minion the choose-one minion will be transformed into) will be used to render
	 * the choice instead, regardless of your specification of name.
	 */
	public String name;
	/**
	 * A description used to render a card representing the battlecry. Used for choose-one battlecries.
	 * <p>
	 * If the {@link #spell} is a {@link net.demilich.metastone.game.spells.TransformMinionSpell}, the {@link
	 * SpellArg#CARD} of that spell (the minion the choose-one minion will be transformed into) will be used to render
	 * the choice instead, regardless of your specification of description.
	 */
	public String description;

	public TargetSelection getTargetSelection() {
		return targetSelection != null ? targetSelection : TargetSelection.NONE;
	}

	public BattlecryAction toBattlecryAction() {
		return BattlecryAction.createBattlecry(spell, getTargetSelection());
	}
}
