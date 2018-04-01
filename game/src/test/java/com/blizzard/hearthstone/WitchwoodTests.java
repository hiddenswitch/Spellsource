package com.blizzard.hearthstone;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.Stream;

public class WitchwoodTests extends TestBase {

	@Test
	public void testGennGreymane() {
		{
			DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false);
			context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
			context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
				Stream.generate(() -> "minion_bloodfen_raptor")
						.map(CardCatalogue::getCardById)
						.limit(29)
						.forEach(deck::addCard);
				deck.addCard(CardCatalogue.getCardById("minion_genn_greymane"));
			});

			context.init();
			Assert.assertTrue(context.getEntities().anyMatch(c -> c.getSourceCard().getCardId().equals("spell_the_coin")));
			// Both player's hero powers should cost one
			Assert.assertEquals(context.getEntities().filter(c -> c.getEntityType() == EntityType.CARD)
					.map(c -> (Card) c)
					.filter(c -> c.getCardType() == CardType.HERO_POWER)
					.filter(c -> costOf(context, context.getPlayer(c.getOwner()), c) == 1)
					.count(), 2L);
		}

		{
			DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false);
			context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
			context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
				Stream.generate(() -> "minion_argent_squire")
						.map(CardCatalogue::getCardById)
						.limit(29)
						.forEach(deck::addCard);
				deck.addCard(CardCatalogue.getCardById("minion_genn_greymane"));
			});

			context.init();
			// Someone should have the coin
			Assert.assertTrue(context.getEntities().anyMatch(c -> c.getSourceCard().getCardId().equals("spell_the_coin")));
			// Both player's hero powers should cost one
			Assert.assertEquals(context.getEntities().filter(c -> c.getEntityType() == EntityType.CARD)
					.map(c -> (Card) c)
					.filter(c -> c.getCardType() == CardType.HERO_POWER)
					.filter(c -> costOf(context, context.getPlayer(c.getOwner()), c) == 2)
					.count(), 2L);
		}
	}

	@Test
	public void testNightmareAmalgam() {
		// Test card cost modifier
		runGym((context, player, opponent) -> {
			Card nightmare = receiveCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_mechwarper");
			Assert.assertEquals(costOf(context, player, nightmare), nightmare.getBaseManaCost() - 1);
		});

		// Test Murlocs cost health
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_seadevil_stinger");
			int hp = player.getHero().getHp();
			playCard(context, player, "minion_nightmare_amalgam");
			Assert.assertEquals(player.getHero().getHp(), hp - CardCatalogue.getCardById("minion_nightmare_amalgam").getBaseManaCost());
		});

		// Test RaceCondition
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_patches_the_pirate");
			playCard(context, player, "minion_nightmare_amalgam");
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_patches_the_pirate");
		});

		// Test CardFilter
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Card thing = receiveCard(context, player, "minion_thing_from_below");
			Assert.assertEquals(costOf(context, player, thing), thing.getBaseManaCost() - 1);
		});

		// Test RaceFilter
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Minion murkEye = playMinionCard(context, player, "minion_old_murk-eye");
			Assert.assertEquals(murkEye.getAttack(), murkEye.getBaseAttack() + 1);
		});
	}

	@Test
	public void testBlackCat() {
		runGym((context, player, opponent) -> {
			Card stillInDeck = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_black_cat");
			Assert.assertEquals(stillInDeck.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card stillInDeck = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_black_cat");
			Assert.assertEquals(stillInDeck.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testPumpkinPeasant() {
		runGym((context, player, opponent) -> {
			Card pumpkin = receiveCard(context, player, "minion_pumpkin_peasant");
			playCard(context, player, "minion_grimestreet_outfitter");
			context.endTurn();
			context.endTurn();
			Minion summonedPumpkin = playMinionCard(context, player, pumpkin);
			Assert.assertEquals(summonedPumpkin.getAttack(), pumpkin.getBaseHp() + 1);
		});
	}
}
