package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
}
