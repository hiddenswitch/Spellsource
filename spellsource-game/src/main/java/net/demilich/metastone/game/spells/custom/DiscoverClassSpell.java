package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Discovers a class card.
 */
public class DiscoverClassSpell extends Spell {

	@Suspendable
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		boolean cantReceiveOwned = desc.getBool(SpellArg.CANNOT_RECEIVE_OWNED);

		CardList classCards = new CardArrayList(HeroClass.getClassCards(DeckFormat.all()));
		classCards.removeIf(card -> !context.getDeckFormat().isInFormat(card));
		classCards.removeIf(card -> !card.isCollectible());
		if (cantReceiveOwned) {
			classCards.removeIf(card -> card.getHeroClass().equals(player.getHero().getHeroClass()));
		}

		SpellDesc fakeDesc = NullSpell.create();
		fakeDesc.put(SpellArg.SPELL, NullSpell.create());

		classCards.shuffle(context.getLogic().getRandom());
		classCards = new CardArrayList(classCards.subList(0, Math.min(classCards.size(), desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3))));

		DiscoverAction discoverAction = SpellUtils.discoverCard(context, player, source, fakeDesc, classCards);
		String classCard = discoverAction.getCard().getCardId();
		SpellDesc subSpell = desc.getSpell();

		SpellUtils.castChildSpell(context, player, subSpell, source, target, context.getCardById(classCard));
	}
}
