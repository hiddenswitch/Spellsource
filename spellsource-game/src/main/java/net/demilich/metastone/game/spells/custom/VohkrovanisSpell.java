package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DiscardSpell;
import net.demilich.metastone.game.spells.DrawCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.UnweightedCatalogueSource;


/**
 * Replaces the {@link SpellArg#TARGET_PLAYER}'s deck with 30 random cards. Then, discards that player's hand, and draws
 * that many cards.
 */
public final class VohkrovanisSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		while (!player.getDeck().isEmpty()) {
			context.getLogic().removeCard(player.getDeck().peek());
		}

		int numberOfCards = desc.getValue(SpellArg.VALUE, context, player, target, source, 30);
		CardList cards = UnweightedCatalogueSource.create().getCards(context, source, player);
		for (int i = 0; i < numberOfCards; i++) {
			context.getLogic().insertIntoDeck(player, context.getLogic().getRandom(cards).getCopy(), player.getDeck().size());
		}

		int handCount = player.getHand().size();

		SpellUtils.castChildSpell(context, player, DiscardSpell.create(DiscardSpell.ALL_CARDS), source, target);
		SpellUtils.castChildSpell(context, player, DrawCardSpell.create(handCount), source, target);
	}
}
