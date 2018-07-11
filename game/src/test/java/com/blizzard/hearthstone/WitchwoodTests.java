package com.blizzard.hearthstone;

import com.google.common.collect.Sets;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.CardZone;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

public class WitchwoodTests extends TestBase {

	@Test
	public void testChamelosDoesntKeepCost() {
		runGym((context, player, opponent) -> {
			Card corridorCreeper = receiveCard(context, opponent, "minion_corridor_creeper");
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			destroy(context, wisp);
			assertEquals(costOf(context, opponent, corridorCreeper), corridorCreeper.getBaseManaCost() - 1);
			Card chameleos = receiveCard(context, player, "minion_chameleos");
			context.endTurn();
			context.endTurn();
			chameleos = (Card) chameleos.transformResolved(context);
			assertEquals(chameleos.getCardId(), "minion_corridor_creeper");
			assertEquals(costOf(context, player, chameleos), chameleos.getBaseManaCost() - 1, "Chameleos should be Corridor Creeper cost minus 1");
			assertEquals(costOf(context, opponent, corridorCreeper), corridorCreeper.getBaseManaCost() - 1, "Corridor Creeper should still have minus 1 cost");
			context.getLogic().removeCard(corridorCreeper);
			Card bloodfenRaptor = receiveCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			context.endTurn();
			chameleos = (Card) chameleos.transformResolved(context);
			assertEquals(chameleos.getCardId(), "minion_bloodfen_raptor");
			assertEquals(costOf(context, player, chameleos), bloodfenRaptor.getBaseManaCost(), "Chameleos should have Bloodfen Raptor base cost.");
			assertEquals(costOf(context, opponent, corridorCreeper), corridorCreeper.getBaseManaCost() - 1, "Corridor Creeper should still have minus 1 cost");
		});
	}

	@Test
	public void testShudderwockZolaInteraction() {
		runGym((context, player, opponent) -> {
			Minion remove = playMinionCard(context, player, "minion_zola_the_gorgon");
			destroy(context, remove);
			playMinionCard(context, player, "minion_shudderwock");
			assertEquals(player.getHand().get(0).getCardId(), "minion_shudderwock", "Should copy Shudderwock");
		});
	}

	@Test
	public void testShudderwockBloodCultistInteraction() {
		runGym((context, player, opponent) -> {
			Minion remove = playMinionCard(context, player, "minion_blood_cultist");
			context.getLogic().removePeacefully(remove);
			context.getLogic().endOfSequence();
			overrideDiscover(context, player, discoverActions -> {
				fail("Shouldn't prompt to discover");
				return null;
			});
			playMinionCard(context, player, "minion_shudderwock");
		});
	}

	@Test
	public void testShudderwockYoggInteraction() {
		runGym((context, player, opponent) -> {
			// 2 spells
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "spell_the_coin");
			// This only casts Coins
			destroy(context, playMinionCard(context, player, "minion_yogg_one_kind"));
			Card shudderwock = receiveCard(context, player, "minion_shudderwock");
			player.setMana(shudderwock.getBaseManaCost());
			// Mana now at base mana cost
			playCard(context, player, shudderwock);
			// Mana goes from zero to two coins cast worth of mana
			assertEquals(player.getMana(), 2);
		});
	}

	@Test
	public void testTessGreymane() {
		runGym((context, player, opponent) -> {
			Set<String> willReplay = Sets.newHashSet("spell_never_valid_targets_black_test", "minion_black_test", "minion_play_randomly_battlecry");
			// Fireball should not be revealed
			playCardWithTarget(context, player, "spell_any_blue_test", opponent.getHero());
			// No valid targets should not be replayed, even if there were valid targets at the time
			playCardWithTarget(context, player, "spell_never_valid_targets_black_test", opponent.getHero());
			Assert.assertTrue(opponent.getHero().hasAttribute(Attribute.RESERVED_BOOLEAN_3));
			opponent.getHero().getAttributes().remove(Attribute.RESERVED_BOOLEAN_3);
			// Don't replay same color
			destroy(context, playMinionCard(context, player, "minion_blue_test"));
			// Play different color
			Minion notBattlecryTarget = playMinionCard(context, player, "minion_black_test");
			// Replay battlecry randomly
			playMinionCardWithBattlecry(context, player, "minion_play_randomly_battlecry", notBattlecryTarget);
			// Will not replay because this is a BLUE card
			Minion battlecryTarget = playMinionCard(context, player, "minion_battlecry_target");
			// Spy on reveal cards
			GameLogic spyLogic = spy(context.getLogic());
			context.setLogic(spyLogic);
			Mockito.doAnswer(invocation -> {
				Assert.assertTrue(willReplay.remove(((Card) invocation.getArgument(1)).getCardId()));
				return invocation.callRealMethod();
			}).when(spyLogic).revealCard(any(), any());
			playMinionCard(context, player, "minion_tess_greymane");
			assertEquals(willReplay.size(), 0);
			Assert.assertFalse(battlecryTarget.hasAttribute(Attribute.RESERVED_BOOLEAN_2), "The battlecries should not have been resolved.");
			Assert.assertFalse(context.getEntities().anyMatch(e -> e.hasAttribute(Attribute.RESERVED_BOOLEAN_3)), "There should have never been a valid target for the spell that adds this attribute.");
		}, HeroClass.BLUE, HeroClass.BLUE);
	}

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
			assertEquals(context.getEntities().filter(c -> c.getEntityType() == EntityType.CARD)
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
			assertEquals(context.getEntities().filter(c -> c.getEntityType() == EntityType.CARD)
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
			assertEquals(costOf(context, player, nightmare), nightmare.getBaseManaCost() - 1);
		});

		// Test Murlocs cost health
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_seadevil_stinger");
			int hp = player.getHero().getHp();
			playCard(context, player, "minion_nightmare_amalgam");
			assertEquals(player.getHero().getHp(), hp - CardCatalogue.getCardById("minion_nightmare_amalgam").getBaseManaCost());
		});

		// Test RaceCondition
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_patches_the_pirate");
			playCard(context, player, "minion_nightmare_amalgam");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_patches_the_pirate");
		});

		// Test CardFilter
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Card thing = receiveCard(context, player, "minion_thing_from_below");
			assertEquals(costOf(context, player, thing), thing.getBaseManaCost() - 1);
		});

		// Test RaceFilter
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nightmare_amalgam");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			Minion murkEye = playMinionCard(context, player, "minion_old_murk-eye");
			assertEquals(murkEye.getAttack(), murkEye.getBaseAttack() + 1);
		});
	}

	@Test
	public void testBlackCat() {
		runGym((context, player, opponent) -> {
			Card stillInDeck = putOnTopOfDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_black_cat");
			assertEquals(stillInDeck.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card stillInDeck = putOnTopOfDeck(context, player, "minion_argent_squire");
			playCard(context, player, "minion_black_cat");
			assertEquals(stillInDeck.getZone(), Zones.HAND);
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
			assertEquals(summonedPumpkin.getAttack(), pumpkin.getBaseHp() + 1);
		});
	}
}
