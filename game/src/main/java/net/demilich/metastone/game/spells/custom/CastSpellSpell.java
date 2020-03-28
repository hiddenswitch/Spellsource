package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class CastSpellSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, Integer.MAX_VALUE);
		cards.shuffle(context.getLogic().getRandom());
		for (Card card : cards) {
			if (target.getZone() != Zones.BATTLEFIELD || target.isDestroyed() || !card.isSpell()) {
				return;
			}

			context.getLogic().revealCard(player, card);
			player.modifyAttribute(Attribute.RANDOM_CHOICES, 1);
			SpellUtils.castChildSpell(context, player, card.getSpell(), card, target);
			player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
		}
	}
}
