package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class ReplaceCardsSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card replacement = context.getLogic().getRandom(desc.getFilteredCards(context, player, source));

		if (target.getZone() == Zones.HAND) {
			context.getLogic().replaceCardInHand(player.getId(), (Card) target, replacement);
		} else if (target.getZone() == Zones.DECK) {
			context.getLogic().replaceCardInDeck(player.getId(), (Card) target, replacement);
		}

		desc.subSpells(0).forEach(subSpell -> {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, replacement);
		});
	}
}
