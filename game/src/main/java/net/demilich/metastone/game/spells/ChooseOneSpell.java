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

	public Card getTempCard(GameContext context, SpellDesc spellDesc, Card sourceCard) {
		return ChooseOneOptionSpell.getTempCard(context, spellDesc, sourceCard, "option_");
	}
}
