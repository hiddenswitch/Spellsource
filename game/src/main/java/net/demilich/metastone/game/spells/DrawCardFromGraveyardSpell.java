package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceArg;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.GraveyardCardAndActorSourceCardSource;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Draws a card from the graveyard. Alters history!
 */
public class DrawCardFromGraveyardSpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(DrawCardFromGraveyardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.VALUE);
		int cardCount = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		CardSourceDesc desc1 = new CardSourceDesc();
		desc1.put(CardSourceArg.CLASS, GraveyardCardAndActorSourceCardSource.class);
		desc1.put(CardSourceArg.DISTINCT, true);
		CardSource cardSource = desc1.create();
		CardList cards = cardSource.getCards(context, source, player);
		for (int i = 0; i < cardCount; i++) {
			if (cards.isEmpty()) {
				return;
			}
			Card card = context.getLogic().removeRandom(cards);
			Zones originalZone = card.getZone();
			boolean drawn = context.getLogic().drawCard(player.getId(), card, source) != null;
			if (drawn) {
				// Successfully drawn. This minion has now no longer died on the battlefield
				if (originalZone == Zones.GRAVEYARD) {
					player.getGraveyard().stream().filter(e -> Objects.equals(e.getSourceCard(), card))
							.findFirst()
							.ifPresent(entity -> entity.getAttributes().remove(Attribute.DIED_ON_TURN));
				}
			}
		}
	}
}

