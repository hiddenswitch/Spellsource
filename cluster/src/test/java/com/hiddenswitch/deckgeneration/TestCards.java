package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.Assert;
import org.testng.annotations.Test;

public class TestCards extends TestBase {
	@Test
	public void testWinWhenStartsInHand() {
		CardCatalogue.loadCardsFromPackage();

		DebugContext context = createContext(HeroClass.BLUE, HeroClass.BLUE, false, DeckFormat.CUSTOM);
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayer1().getHand().add(CardCatalogue.getCardById("minion_win_when_starts_in_hand"));
		Assert.assertTrue(!context.getPlayer2().getHero().isDestroyed());
		context.init();
		Assert.assertTrue(context.getPlayer2().getHero().isDestroyed());

		DebugContext badContext = createContext(HeroClass.BLUE, HeroClass.BLUE, false, DeckFormat.CUSTOM);
		badContext.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		for (int i = 0; i < 100; i++) {
			badContext.getPlayer1().getDeck().add(CardCatalogue.getCardById("minion_novice_engineer"));
		}

		badContext.getPlayer1().getDeck().set(10, CardCatalogue.getCardById("minion_win_when_starts_in_hand"));

		for (int i = 0; i < 100; i++) {
			badContext.getPlayer1().getDeck().add(CardCatalogue.getCardById("minion_novice_engineer"));
		}

		Assert.assertTrue(!badContext.getPlayer2().getHero().isDestroyed());
		badContext.init();
		Assert.assertTrue(!badContext.getPlayer2().getHero().isDestroyed());
	}

	@Test
	public void testDealDamageWithoutAttackMinion() {
		CardCatalogue.loadCardsFromPackage();

		runGym((context, player, opponent) -> {
			playCard(context, player, CardCatalogue.getCardById("minion_deal_damage_when_attacking_minion"));
			playCard(context, opponent, CardCatalogue.getCardById("minion_antique_healbot"));
			attack(context, player, player.getMinions().get(0), opponent.getHero());
			Assert.assertTrue(opponent.getHero().getHp() == 29);
			attack(context, player, player.getMinions().get(0), opponent.getMinions().get(0));
			Assert.assertTrue(opponent.getHero().isDestroyed());
		});
	}
}
