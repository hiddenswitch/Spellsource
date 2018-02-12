package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class ReplaceCardsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card specificCard = SpellUtils.getCard(context, desc);
		Card replacement;
		if (specificCard == null) {
			replacement = context.getLogic().getRandom(desc.getFilteredCards(context, player, source));
		} else {
			replacement = specificCard;
		}

		replacement = context.getLogic().replaceCard(player.getId(), (Card) target, replacement);
		final Card output = replacement;
		desc.subSpells(0).forEach(subSpell -> {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, output);
		});
	}
}
