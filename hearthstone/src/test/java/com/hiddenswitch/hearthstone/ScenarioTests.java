package com.hiddenswitch.hearthstone;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class ScenarioTests extends TestBase {

	@Test
	void testNecromancer() {
		runGym((context, player, opponent) -> {
			context.getLogic().changeHero(player, ((Card) CardCatalogue.getCardById("hero_necromancer")).createHero(), true);
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor") /*Cost 2*/;
			Minion bearshark = playMinionCard(context, player, "minion_bearshark") /*Cost 3*/;
			Assert.assertEquals(bloodfen.getDeathrattles().size(), 1);
			Assert.assertEquals(bearshark.getDeathrattles().size(), 0);
			context.endTurn();
			playCard(context, opponent, "spell_assassinate", bloodfen);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getBaseManaCost(), 1);
		});
	}

	@Test
	@Ignore
	void testCurvestone() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new DeckFormat().withCardSets("BASIC", "CLASSIC"));
			playCard(context, player, "spell_discover_minion_on_curve");
			Assert.assertEquals(player.getHand().get(0).getBaseManaCost(), 1);
			Assert.assertEquals(player.getHand().get(0).getCardType(), CardType.MINION);
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_discover_minion_on_curve");
			Assert.assertEquals(player.getHand().get(1).getBaseManaCost(), 2);
			Assert.assertEquals(player.getHand().get(1).getCardType(), CardType.MINION);
			playCard(context, player, "spell_discover_spell_on_curve");
			Assert.assertEquals(player.getHand().get(2).getBaseManaCost(), 2);
			Assert.assertEquals(player.getHand().get(2).getCardType(), CardType.SPELL);
		});

		for (int i = 1; i < 11; i++) {
			int finalI = i;
			runGym((context, player, opponent) -> {
				context.setDeckFormat(new DeckFormat().withCardSets("BASIC", "CLASSIC"));
				for (int x = 0; x < finalI - 1; x++) {
					context.endTurn();
					context.endTurn();
				}
				playCard(context, player, "spell_discover_two_cards");
				Assert.assertEquals(player.getHand().stream().mapToInt(Card::getBaseManaCost).sum(), finalI);
				Assert.assertEquals(player.getHand().getCount(), 2);
			});
		}

	}
}
