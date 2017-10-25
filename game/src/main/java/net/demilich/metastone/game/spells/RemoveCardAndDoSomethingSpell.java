package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class RemoveCardAndDoSomethingSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int amount = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < amount; i++) {
			Card card = player.getDeck().getRandom();

			if (card != null) {
				card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
				SpellDesc cardEffectSpell = (SpellDesc) desc.get(SpellArg.SPELL);
				context.setEventCard(card);
				SpellUtils.castChildSpell(context, player, cardEffectSpell, source, card);
				context.setEventCard(null);

				// If nothing else was done with the card, send it to the graveyard at this point.
				if (card.getZone() == Zones.SET_ASIDE_ZONE) {
					context.getLogic().removeCard(card);
				}
			}
		}
	}
}
