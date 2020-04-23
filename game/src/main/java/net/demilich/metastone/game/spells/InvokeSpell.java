package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * When the player has at least {@link Attribute#INVOKE} additional unspent mana, source an extra bonus effect for that
 * cost.
 * <p>
 * By default, casts the effect in {@link SpellArg#SPELL1}. If the player can afford the extra cost, {@link
 * SpellArg#SPELL2} is printed on a card, and the player is given a discover choice between the two effects.
 */
public class InvokeSpell extends Spell {

	@Suspendable
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var manaRemaining = player.getMana();
		var invoke = source.getAttributeValue(Attribute.INVOKE);
		if (source.hasAttribute(Attribute.AURA_INVOKE)) {
			invoke = Math.min(invoke, source.getAttributeValue(Attribute.AURA_INVOKE));
		}
		if (manaRemaining < invoke) {
			if (desc.containsKey(SpellArg.SPELL)) {
				SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target);
			}
			return;
		}
		var spell1 = (SpellDesc) desc.get(SpellArg.SPELL1);
		var spell2 = (SpellDesc) desc.get(SpellArg.SPELL2);

		var card1 = InvokeOptionSpell.getTempCard(context, spell1, source.getSourceCard());
		var card2 = InvokeOptionSpell.getTempCard(context, spell2, source.getSourceCard());

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

		// add aura invoke cards
		cards.removeIf(card -> card.getBaseManaCost() > manaRemaining);

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


}
