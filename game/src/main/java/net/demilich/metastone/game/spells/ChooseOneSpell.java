package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Provides a choice between {@link SpellArg#SPELL1} and {@link SpellArg#SPELL2}, using the {@link SpellArg#NAME} and
 * {@link SpellArg#DESCRIPTION} in those spells to generate the choice cards.
 * <p>
 * The sub-spells should be {@link ChooseOneOptionSpell} spells.
 * <p>
 * The {@link SpellArg#CONDITION}, if specified, casts {@link SpellArg#SPELL} (if specified) instead of giving choices
 * (or does nothing).
 * <p>
 * For <b>example,</b> this text gives the player two choices if their player has more than 1 imbue charge:
 * <pre>
 *   {@code
 *     {
 *       "class": "ChooseOneSpell",
 *       "condition": {
 *         "class": "AttributeCondition",
 *         "target": "FRIENDLY_PLAYER",
 *         "attribute": "IMBUE",
 *         "value": 1,
 *         "operation": "GREATER_OR_EQUAL"
 *       },
 *       "spell1": {
 *         "class": "ChooseOneOptionSpell",
 *         "name": "Normal",
 *         "description": "Don't Imbue."
 *       },
 *       "spell2": {
 *         "class": "ChooseOneOptionSpell",
 *         "name": "Imbue",
 *         "description": "Give this unit Spellpower +1.",
 *         "spells": [
 *           {
 *             "class": "ModifyAttributeSpell",
 *             "value": 1,
 *             "attribute": "SPELL_DAMAGE",
 *             "target": "SELF"
 *           },
 *           {
 *             "class": "ModifyAttributeSpell",
 *             "value": -1,
 *             "attribute": "IMBUE",
 *             "target": "FRIENDLY_PLAYER"
 *           }
 *         ]
 *       }
 *     }
 *   }
 * </pre>
 */
public class ChooseOneSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (desc.containsKey(SpellArg.CONDITION)) {
			Condition condition = (Condition) desc.get(SpellArg.CONDITION);

			if (!condition.isFulfilled(context, player, source, target)) {
				if (desc.containsKey(SpellArg.SPELL)) {
					SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target);
				}
				return;
			}
		}

		var spell1 = (SpellDesc) desc.get(SpellArg.SPELL1);
		var spell2 = (SpellDesc) desc.get(SpellArg.SPELL2);

		var card1 = getTempCard(context, spell1, source.getSourceCard());
		var card2 = getTempCard(context, spell2, source.getSourceCard());

		CardList cards = new CardArrayList();
		if (spell1.containsKey(SpellArg.CONDITION)) {
			var condition = (Condition) spell1.get(SpellArg.CONDITION);
			if (condition.isFulfilled(context, player, source, target)) {
				cards.add(card1);
			}
		} else {
			cards.add(card1);
		}
		if (spell2.containsKey(SpellArg.CONDITION)) {
			var condition = (Condition) spell2.get(SpellArg.CONDITION);
			if (condition.isFulfilled(context, player, source, target)) {
				cards.add(card2);
			}
		} else {
			cards.add(card2);
		}

		cards.removeIf(card -> shouldRemoveCard(card, player, context));

		if (cards.isEmpty()) {
			if (desc.containsKey(SpellArg.SPELL)) {
				SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target);
			}
			return;
		}

		var clone = desc.clone();
		clone.put(SpellArg.SPELL, NullSpell.create());
		var discoverAction = SpellUtils.discoverCard(context, player, source, clone, cards);

		SpellUtils.castChildSpell(context, player, discoverAction.getCard().getSpell(), source, target);
	}

	public boolean shouldRemoveCard(Card card, Player player, GameContext context) {
		return false;
	}

	/**
	 * Generates a temporary card. Used by {@link ChooseOneOptionSpell} to actually generate the card definitions for the
	 * choice cards.
	 *
	 * @param context
	 * @param spellDesc
	 * @param sourceCard
	 * @return
	 */
	public Card getTempCard(GameContext context, SpellDesc spellDesc, Card sourceCard) {
		return ChooseOneOptionSpell.getTempCard(context, spellDesc, sourceCard, "option_");
	}
}
