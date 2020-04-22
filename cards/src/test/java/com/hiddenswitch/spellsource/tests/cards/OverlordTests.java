package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class OverlordTests extends TestBase {

	@Test
	public void testDarkRule() {
		runGym((context, player, opponent) -> {
			useHeroPower(context, player);
			overrideDiscover(context, player, discoverActions -> {
				List<String> cardNames = discoverActions.stream().map(dA -> dA.getCard().getName()).collect(Collectors.toList());
				assertTrue(cardNames.contains("Assassin"));
				assertTrue(cardNames.contains("Defender"));
				assertTrue(cardNames.contains("Duelist"));
				return discoverActions.get(context.getLogic().random(3));
			});
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHero().getHp(), player.getHero().getBaseHp() - 2);
		}, HeroClass.CRIMSON, HeroClass.CRIMSON);
	}

	@Test
	public void testDebtCollector() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_debt_collector");
			Quest pact = (Quest) context.getTriggers().get(0);
			assertNotEquals("minion_debt_collector", pact.getSourceCard().getCardId());
    });
	}
  
	@Test
	public void testDestroyTheStrong() {
		runGym((context, player, opponent) -> {
			Minion captive = playMinionCard(context, player, "token_captiveguard_overlord");
			playCard(context, player, "spell_destroy_the_strong");
			assertFalse(captive.isDestroyed());
			destroy(context, captive);
			assertEquals(1, player.getMinions().size());
			playCard(context, player, "spell_destroy_the_strong");
			assertEquals(1, player.getMinions().size());
		});
	}
  
	@Test
	public void testTheOathbreaker() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_the_oathbreaker");
			player.setMana(4);
			playCard(context, player, "pact_extraction");
			playCard(context, player, "pact_nothing_to_waste");
			assertEquals(2, player.getMana());
		});
	}

	@Test
	public void testCovensDebt() {
		CardList crimsonCards = CardCatalogue.query(DeckFormat.spellsource(), "CRIMSON");
		crimsonCards.stream()
				.filter(card -> card.getCardId().startsWith("pact_"))
				.filter(card -> !card.getCardId().equals("pact_binding_nightmare")).forEach(pact -> {
			runGym((context, player, opponent) -> {
				for (int i = 0; i < 3; i++) {
					shuffleToDeck(context, player, "minion_neutral_test");
				}
				playCard(context, player, "pact_binding_nightmare");
				playCard(context, player, pact);
				assertEquals(player.getDeck().size(), 0);
			});
		});
	}
}
