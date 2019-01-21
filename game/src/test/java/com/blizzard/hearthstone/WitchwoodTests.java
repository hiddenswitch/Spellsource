package com.blizzard.hearthstone;

import com.google.common.collect.Sets;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

public class WitchwoodTests extends TestBase {

	@Test
	public void testTheGlassKnight() {
		runGym((context, player, opponent) -> {
			Minion glassKnight = playMinionCard(context, player, "minion_the_glass_knight");
			playCard(context, player, "spell_fireball", glassKnight);
			player.getHero().setHp(29);
			playCard(context, player, "spell_healing_touch", player.getHero());
			assertTrue(glassKnight.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testBewitchPermanentsInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_hagatha_the_witch");
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_bewitch");
			playCard(context, player, "permanent_test");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testDariusCrowley() {
		runGym((context, player, opponent) -> {
			Minion darius = playMinionCard(context, player, "minion_darius_crowley");
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_wisp");
			context.endTurn();
			attack(context, player, darius, target);
			assertFalse(darius.isDestroyed());
			assertTrue(target.isDestroyed());
			assertEquals(darius.getAttack(), darius.getBaseAttack() + 2);
			assertEquals(darius.getMaxHp(), darius.getBaseHp() + 2);
		});

		runGym((context, player, opponent) -> {
			Minion darius = playMinionCard(context, player, "minion_darius_crowley");
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_wisp");
			target.setAttack(4);
			context.endTurn();
			attack(context, player, darius, target);
			assertTrue(darius.isDestroyed(), "Darius Crowley should not be able to survive lethal damage with its effect of gaining +2/+2");
		});
	}

	@Test
	public void testArcaneKeysmith() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("secret_duplicate", "secret_counterspell"));
			playCard(context, player, "secret_duplicate");
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1, "The discover should not show Duplicate, because it's already in play");
				assertEquals(discoverActions.get(0).getCard().getCardId(), "secret_counterspell");
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_arcane_keysmith");
			assertEquals(player.getSecrets().size(), 2, "Both Counterspell and Duplicate should be in play now.");
		}, HeroClass.BLUE, HeroClass.BLUE);
	}

	@Test
	public void testCoffinCrasher() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_loot_hoarder");
			receiveCard(context, player, "minion_runic_egg");
			Minion coffinCrasher = playMinionCard(context, player, "minion_coffin_crasher");
			destroy(context, coffinCrasher);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_runic_egg");
		});
	}

	@Test
	public void testVoodooDoll() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");
			Minion doll = playMinionCardWithBattlecry(context, player, "minion_voodoo_doll", target);
			destroy(context, doll);
			assertTrue(target.isDestroyed());
		});
	}

	@Test
	public void testGentlemansTopHat() {
		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_gentleman_s_top_hat", target1);
			assertEquals(target1.getAttack(), target1.getBaseAttack() + 2);
			Minion target2 = playMinionCard(context, player, "minion_wisp");
			assertEquals(target2.getAttack(), target2.getBaseAttack());
			destroy(context, target1);
			assertEquals(target2.getAttack(), target2.getBaseAttack() + 2);
			Minion target3 = playMinionCard(context, player, "minion_wisp");
			assertEquals(target3.getAttack(), target3.getBaseAttack());
			destroy(context, target2);
			assertEquals(target3.getAttack(), target3.getBaseAttack() + 2);
		});
	}

	@Test
	public void testGlindaCrowskin() {
		runGym((context, player, opponent) -> {
			Card bloodfen = receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_glinda_crowskin");
			assertTrue(bloodfen.hasAttribute(Attribute.AURA_ECHO));
			playCard(context, player, bloodfen);
			assertEquals(player.getHand().size(), 1);
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN));
			playCard(context, player, player.getHand().get(0));
			assertEquals(player.getHand().size(), 1);
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN));
		});
	}

	@Test
	public void testParagonOfLight() {
		runGym((context, player, opponent) -> {
			Minion paragon = playMinionCard(context, player, "minion_paragon_of_light");
			context.endTurn();
			context.endTurn();
			player.getHero().setHp(10);
			attack(context, player, paragon, opponent.getHero());
			assertEquals(player.getHero().getHp(), 10);
		});

		runGym((context, player, opponent) -> {
			Minion paragon = playMinionCard(context, player, "minion_paragon_of_light");
			playCard(context, player, "spell_dragon_s_strength", paragon);
			context.endTurn();
			context.endTurn();
			player.getHero().setHp(10);
			assertTrue(paragon.hasAttribute(Attribute.AURA_LIFESTEAL));
			assertTrue(paragon.hasAttribute(Attribute.AURA_TAUNT));
			attack(context, player, paragon, opponent.getHero());
			assertEquals(player.getHero().getHp(), 10 + paragon.getAttack());
		});
	}

	@Test
	public void testWitchwoodGrizzlySilenceInteraction() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "minion_bloodfen_raptor");
			Minion grizzly = playMinionCard(context, player, "minion_witchwood_grizzly");
			assertEquals(grizzly.getHp(), grizzly.getBaseHp() - 1);
			playCard(context, player, "spell_silence", grizzly);
			assertEquals(grizzly.getHp(), grizzly.getBaseHp());
		});
	}

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
	public void testShudderwockEvolutionInteraction() {
		runGym((context, player, opponent) -> {
			// Make sure there are minions for novice and shudderwock to transform into
			context.setDeckFormat(new FixedCardsDeckFormat("minion_cost_4_test", "minion_cost_11_test"));
			playCard(context, player, "hero_thrall_deathseer");
			playMinionCard(context, player, "minion_novice_engineer");
			Card shouldNotBeDrawn = shuffleToDeck(context, player, "spell_the_coin");
			context.getLogic().setRandom(new XORShiftRandom(0L) {
				@Override
				protected int next(int bits) {
					return 1;
				}
			});
			playCard(context, player, "minion_shudderwock");
			assertEquals(player.getHand().size(), 0);
			assertEquals(shouldNotBeDrawn.getZone(), Zones.DECK);
		});
	}

	@Test
	public void testShudderwockZolaInteraction() {
		runGym((context, player, opponent) -> {
			Minion remove = playMinionCard(context, player, "minion_zola_the_gorgon");
			destroy(context, remove);
			playMinionCard(context, player, "minion_shudderwock");
			assertEquals(player.getHand().size(), 0, "Should NOT copy Shudderwock");
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
	public void testShudderwockHagathaTheWitchInteraction() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_boulderfist_ogre");
			playCard(context, player, "hero_hagatha_the_witch");
			assertEquals(minion.getHp(), minion.getMaxHp() - 3);
			playCard(context, player, "minion_shudderwock");
			assertEquals(minion.getHp(), minion.getMaxHp() - 6);
		});
	}

	@Test
	public void testShudderwockConditionalBattlecriesInteraction() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_hooked_reaver");
			Minion shudderwock = playMinionCard(context, player, "minion_shudderwock");
			assertEquals(shudderwock.getAttack(), 6);
		});
	}

	@Test
	public void testTessGreymane() {
		runGym((context, player, opponent) -> {
			Set<String> willReplay = Sets.newHashSet("spell_never_valid_targets_black_test", "minion_black_test", "minion_play_randomly_battlecry");
			// Fireball should not be revealed
			playCard(context, player, "spell_any_blue_test", opponent.getHero());
			// No valid targets should not be replayed, even if there were valid targets at the time
			playCard(context, player, "spell_never_valid_targets_black_test", opponent.getHero());
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
	public void testTessGreymaneDoesntTriggerFlamewaker() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_any_black_test", opponent.getHero());
			playMinionCard(context, player, "minion_flamewaker");
			playMinionCard(context, player, "minion_tess_greymane");
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp(), "Should not have triggered Flamewaker");
			playCard(context, player, "spell_any_black_test", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2, "Should have triggered Flamewaker");
		}, HeroClass.BLUE, HeroClass.BLUE);
	}

	@Test
	public void testGennGreymane() {
		{
			DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false, DeckFormat.CUSTOM);
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
			DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false, DeckFormat.CUSTOM);
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

	@Test
	public void testBookOfSpecters() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "spell_pyroblast");
			receiveCard(context, player, "token_shadow_of_nothing");
			receiveCard(context, player, "token_shadow_of_nothing");
			playCard(context, player, "spell_book_of_specters");
			assertEquals(player.getHand().get(0).getCardId(), "token_shadow_of_nothing");
			assertEquals(player.getHand().get(1).getCardId(), "token_shadow_of_nothing");
			assertEquals(player.getHand().get(2).getCardId(), "minion_wisp");
			assertEquals(player.getHand().get(3).getCardId(), "minion_wisp");
		});
	}

	@Test
	public void testMalchezaarBaku() {
		int success = 0;
		for (int i = 0; i < 100; i++) {
			DebugContext debug = createContext(HeroClass.BLUE, HeroClass.BLUE, false, DeckFormat.ALL);
			debug.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
			debug.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
				for (int j = 0; j < 10; j++) {
					deck.addCard("minion_pumpkin_peasant");
				}
			});
			debug.getPlayer1().getDeck().addCard(debug.getCardById("minion_prince_malchezaar"));
			debug.getPlayer1().getDeck().addCard(debug.getCardById("minion_baku_the_mooneater"));
			debug.init();
			if (debug.getPlayer1().getHeroPowerZone().get(0).getCardId().equals("hero_power_fireblast_rank_2")) {
				success++;
			}

		}
		assertEquals(success, 100);


	}
}
