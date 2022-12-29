package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.DiscoverSpell;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;

/**
 * Source 3 cards from other classes (i.e., Source 3 times, each time, select from cards from other classes.
 * <p>
 * Implements Exotic Goods.
 */
public final class ExoticGoodsSpell extends DiscoverSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var allCards = CardCatalogue.query(context.getDeckFormat(), null, null, null, null, false);
		for (var i = 0; i < 3; i++) {
			var count = 3;
			var cards = new ArrayList<String>(count);
			for (var limit = 100; limit > 0; limit--) {
				var card = context.getLogic().getRandom(allCards);
				if (!HeroClass.hasHeroClass(context, player, card, HeroClass.SELF)
						&& !HeroClass.hasHeroClass(context, player, card, HeroClass.ANY)) {
					cards.add(card.getCardId());
					count--;
				}
				if (count == 0) {
					break;
				}
			}
			var discover = DiscoverSpell.create(ReceiveCardSpell.create());
			discover.put(SpellArg.CARDS, cards.toArray(new String[0]));
			super.onCast(context, player, discover, source, target);
		}
	}
}
