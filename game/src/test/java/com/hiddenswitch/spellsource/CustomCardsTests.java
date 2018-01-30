package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.Stream;

public class CustomCardsTests extends TestBase {

	@Test
	public void testVereesaWindrunner() {
		GymFactory vareesaFactory = getGymFactory((context, player, opponent) -> {
			playCard(context, player, "minion_vereesa_windrunner");
		}, (context, player, opponent) -> {
			Assert.assertEquals(player.getSecrets().size(), 0);
		});

		GymFactory eaglehornBowFactory = getGymFactory((context, player, opponent) -> {
			playCard(context, player, "minion_vereesa_windrunner");
			playCard(context, player, "weapon_eaglehorn_bow");
		}, (context, player, opponent) -> {
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(player.getWeaponZone().get(0).getDurability(), player.getWeaponZone().get(0).getBaseDurability() + 1);
		});

		Stream.of(vareesaFactory, eaglehornBowFactory).forEach(factory -> {
			Stream.of(
					"secret_freezing_trap",
					"secret_snipe",
					"secret_misdirection",
					"secret_corpse_explosion"
			).forEach(noEffectCardId -> {
				factory.run((context, player, opponent) -> {
					playCard(context, player, noEffectCardId);
				});
			});

			factory.run((context, player, opponent) -> {
				int opponentHp = opponent.getHero().getHp();
				playCard(context, player, "secret_explosive_trap");
				Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 2);
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_cat_trick");
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_cat_in_a_hat");
			});

			factory.run((context, player, opponent) -> {
				MinionCard raptor = (MinionCard) receiveCard(context, player, "minion_bloodfen_raptor");
				playCard(context, player, "secret_hidden_cache");
				Minion raptorOnBoard = playMinionCard(context, player, raptor);
				Assert.assertEquals(raptorOnBoard.getAttack(), raptor.getBaseAttack() + 2);
				Assert.assertEquals(raptorOnBoard.getHp(), raptor.getBaseHp() + 2);
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_venomstrike_trap");
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_emperor_cobra");
			});

			factory.run((context, player, opponent) -> {
				playCard(context, player, "secret_wandering_monster");
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getBaseManaCost(), 3);
			});
		});
	}

	@Test
	public void testFleetfootedScout() {
		runGym((context, player, opponent) -> {
			Card card1 = receiveCard(context, player, "spell_barrage");
			Minion fleetfooted = playMinionCard(context, player, "minion_fleetfooted_scout");
			Card card2 = receiveCard(context, player, "spell_load_and_lock");
			Card card3 = receiveCard(context, player, "spell_mirror_image");
			Stream.of(card1, card2).forEach(c -> Assert.assertEquals(costOf(context, player, c), c.getBaseManaCost() - 1));
			Assert.assertEquals(costOf(context, player, card3), card3.getBaseManaCost());
			playCardWithTarget(context, player, "spell_fireball", fleetfooted);
			Stream.of(card1, card2).forEach(c -> Assert.assertEquals(costOf(context, player, c), c.getBaseManaCost()));
			Assert.assertEquals(costOf(context, player, card3), card3.getBaseManaCost());
		});
	}

	@Test
	public void testSecretGarden() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_bloodfen_raptor");
			}
			playCard(context, player, "secret_secret_garden");
			context.endTurn();
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 1);
			Assert.assertEquals(player.getHand().size(), 0);
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			context.endTurn();
			Assert.assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			}
			context.endTurn();
			Assert.assertEquals(player.getSecrets().size(), 1);
			Assert.assertEquals(player.getHand().size(), 0);
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			Assert.assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testMasterSorcerer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", target);
			Assert.assertEquals(player.getHand().size(), 1);
			Assert.assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", target);
			Assert.assertEquals(player.getHand().size(), 0);
			Assert.assertEquals(player.getDeck().size(), 1);
		});
	}

	@Test
	public void testMetamagicTemporalFluxInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "spell_the_coin");
			}
			overrideDiscover(context, "spell_enhanced", player);
			playCard(context, player, "spell_metamagic");
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, "spell_temporal_flux", opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			Assert.assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testMetamagic() {
		// Costs (2) less.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, "spell_quickened", player);
			playCard(context, player, "spell_metamagic");
			player.setMaxMana(10);
			player.setMana(10);
			Card explosion = receiveCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost() - 2);
			playCard(context, player, explosion);
			explosion = receiveCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost());
		});

		// Deals 1 damage to all enemy minions.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion villager = playMinionCard(context, opponent, "minion_possessed_villager");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			overrideDiscover(context, "spell_unbounded", player);
			playCard(context, player, "spell_metamagic");
			Assert.assertFalse(villager.isDestroyed());
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertTrue(villager.isDestroyed());
			Assert.assertTrue(bloodfen.isDestroyed(), "Two damage should have been dealt in this sequence.");
			Assert.assertEquals(opponent.getMinions().size(), 1, "There should just be a shadowbeast, because the additional spell effect does not happen in its own sequence.");
			context.endTurn();
			bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			Assert.assertEquals(opponent.getMinions().size(), 2, "There should be a shadowbeast and a bloodfen.");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(bloodfen.isDestroyed(), "The next arcane explosion should not have destroyed the bloodfen since it only dealt 1 damage");
			Assert.assertEquals(opponent.getMinions().size(), 1, "But the Shadowbeast should have been destroyed.");
		});

		// Returns to your deck after you cast it.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, "spell_memorized", player);
			playCard(context, player, "spell_metamagic");
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getDeck().size(), 0, "We should not have shuffled a minion card into the deck.");
			context.endTurn();
			// We should still apply the effect to the next spell the player cast
			playCard(context, opponent, "spell_the_coin");
			Assert.assertEquals(player.getDeck().size(), 0, "The opponent's spell should not have been shuffled.");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(player.getDeck().get(0).getCardId(), "spell_arcane_explosion");
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertEquals(player.getDeck().size(), 1, "Only one copy of the card should have been shuffled.");
		});

		// Freezes two random enemies.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, "spell_chilled", player);
			playCard(context, player, "spell_metamagic");
			context.endTurn();
			Minion minion1 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion minion2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertTrue(minion1.hasAttribute(Attribute.FROZEN));
			Assert.assertTrue(minion2.hasAttribute(Attribute.FROZEN));
			Assert.assertEquals(minion1.getHp(), minion1.getBaseHp() - 1);
			Assert.assertEquals(minion2.getHp(), minion1.getBaseHp() - 1);
		});

		// The next spell you cast costs (2) more and has Spell Damage +2.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, "spell_enhanced", player);
			playCard(context, player, "spell_metamagic");
			Card fireball = receiveCard(context, player, "spell_fireball");
			Assert.assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost() + 2);
			Assert.assertEquals(player.getAttributeValue(Attribute.SPELL_DAMAGE), 2);
			int opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 8);
			fireball = receiveCard(context, player, "spell_fireball");
			Assert.assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost(), "The 2nd spell should not be more expensive");
			opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 2nd spell should not have gotten spell damage +2.");
			opponentHp = opponent.getHero().getHp();
			playCardWithTarget(context, player, fireball, opponent.getHero());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 3nd spell should not have gotten spell damage -2.");
		});

		// Deals 3 damage to a random enemy minion.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion chillwind = playMinionCard(context, opponent, "minion_chillwind_yeti");
			context.endTurn();
			overrideDiscover(context, "spell_empowered", player);
			playCard(context, player, "spell_metamagic");
			Assert.assertEquals(chillwind.getHp(), chillwind.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			Assert.assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			Assert.assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3, "The empowered effect should have expired");
		});
	}

	@Test
	public void testNexusKingSalhadaar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_nexus_king_salhadaar");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
		});
	}

	@Test
	public void testSageOfFoursight() {
		runGym((context, player, opponent) -> {
			Minion sage = playMinionCard(context, player, "minion_sage_of_foursight");
			Assert.assertEquals(sage.getAttack(), sage.getBaseAttack(), "Sage should not buff itself.");
			Assert.assertEquals(sage.getHp(), sage.getBaseHp(), "Sage should not buff itself.");
			Card bloodfenCard = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard);
			Assert.assertEquals(costOf(context, player, bloodfenCard), bloodfenCard.getBaseManaCost() + 4, "Bloodfen should cost more because it's the next card the player will play.");

			// It should work with a one turn gap in the middle
			context.endTurn();
			context.endTurn();

			Minion bloodfen = playMinionCard(context, player, (MinionCard) bloodfenCard);
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 4, "Bloodfen should be buffed.");
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 4, "Bloodfen should be buffed.");
			Card bloodfenCard2 = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard2);
			Assert.assertEquals(costOf(context, player, bloodfenCard), bloodfenCard.getBaseManaCost(), "Bloodfen 2 should not cost more.");
			Minion bloodfen2 = playMinionCard(context, player, (MinionCard) bloodfenCard2);
			Assert.assertEquals(bloodfen2.getAttack(), bloodfen2.getBaseAttack(), "The second bloodfen should not be buffed");
			Assert.assertEquals(bloodfen2.getHp(), bloodfen2.getBaseHp(), "The second bloodfen should not be buffed");
		});
	}

	public void testScorpidStinger() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion flipper = playMinionCard(context, opponent, "minion_snowflipper_penguin");
			context.endTurn();
			attack(context, player, player.getHero(), flipper);
			Assert.assertTrue(player.getHand().containsCard("spell_inner_rage"));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			attack(context, player, player.getHero(), bloodfen);
			Assert.assertFalse(player.getHand().containsCard("spell_inner_rage"));
		});
	}

	@Test
	public void testPulseBomb() {
		// Test excess on adjacents
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			playCardWithTarget(context, player, "spell_pulse_bomb", bloodfen);
			Assert.assertTrue(bloodfen.isDestroyed());
			// Up to 18 damage rule
			Assert.assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + bloodfen.getBaseHp());
			Assert.assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + bloodfen.getBaseHp());
		});

		// Test excess in event of divine shield using Explosive Runes rules
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			bloodfen.setAttribute(Attribute.DIVINE_SHIELD);
			context.endTurn();
			playCardWithTarget(context, player, "spell_pulse_bomb", bloodfen);
			Assert.assertFalse(bloodfen.isDestroyed());
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp());
			// Up to 18 damage rule
			Assert.assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + bloodfen.getBaseHp());
			Assert.assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + bloodfen.getBaseHp());
		});
	}

	@Test
	public void testArmaggedonVanguardBolfRamshieldInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_armageddon_vanguard");
			playCard(context, player, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, opponent, "minion_armageddon_vanguard");
			playCard(context, opponent, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, player, "spell_arcane_missiles");
		});
	}

	@Test
	public void testTerrorscaleStalkerBlinkDogInteraction() {
		runGym((context, player, opponent) -> {
			// Deathrattle: Give a random friendly Beast \"Deathrattle: Summon a Blink Dog\"
			Minion blinkDog = playMinionCard(context, player, "minion_blink_dog");
			playCard(context, player, "minion_terrorscale_stalker");
			// Now Blink Dog summons a blink dog and gives a randomly friendly beast an extra deathrattle
			playCardWithTarget(context, player, "spell_fireball", blinkDog);
			Assert.assertEquals(player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals("minion_blink_dog")).count(), 1L);
		});
	}

	@Test
	public void testThinkFast() {
		runGym((context, player, opponent) -> {
			// TODO: This should still work if it's a different class
			playCard(context, player, "spell_mirror_image");
			int[] cost = new int[1];
			overrideDiscover(player, actions -> {
				cost[0] = actions.get(0).getCard().getBaseManaCost();
				return actions.get(0);
			});
			playCard(context, player, "spell_think_fast");
			Assert.assertEquals(costOf(context, player, player.getHand().get(0)), cost[0] - 1);
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(costOf(context, player, player.getHand().get(0)), cost[0]);
		}, HeroClass.BLACK, HeroClass.BLACK);
	}

	@Test
	public void testDejaVu() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_deja_vu");
			Assert.assertEquals(player.getMinions().size(), 2);
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
			playCard(context, player, player.getHand().get(1));
			playCard(context, player, player.getHand().get(0));
			for (int i = 2; i < 4; i++) {
				Assert.assertEquals(player.getMinions().get(i).getAttack(), 1);
				Assert.assertEquals(player.getMinions().get(i).getHp(), 1);
			}
		});
	}

	@Test
	public void testForeverAStudent() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_forever_a_student", bloodfen);
			Minion bloodfen2 = playMinionCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1);
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
			Assert.assertEquals(bloodfen2.getAttack(), bloodfen2.getBaseAttack(), "The newly summoned minion should not be the benefit of the buff.");
			Assert.assertEquals(bloodfen2.getHp(), bloodfen2.getBaseHp());
			context.endTurn();
			playCard(context, opponent, "minion_bloodfen_raptor");
			Assert.assertEquals(bloodfen.getAttack(), bloodfen.getBaseAttack() + 1, "Opponent summoning a minion should not affect the stats of the enchanted minion.");
			Assert.assertEquals(bloodfen.getHp(), bloodfen.getBaseHp() + 1);
		});
	}

	@Test
	public void testNickOfTime() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, player, "minion_nick_of_time");
			context.endTurn();
			Assert.assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).filter(cid -> cid.equals("token_silver_hand_recruit")).count(), 2L);
		});
	}

	@Test
	public void testAwakenTheAncients() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_awaken_the_ancients");
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMana(), 10);
			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMana(), 8);
		});
	}

	@Test
	public void testAcceleratedGrowth() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			shuffleToDeck(context, opponent, "minion_bloodfen_raptor");
			playCard(context, player, "spell_accelerated_growth");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			Assert.assertEquals(opponent.getHand().get(0).getCardId(), "minion_bloodfen_raptor", "Testing the TargetPlayer.BOTH attribute on DrawCardSpell");
		});
	}

	@Test
	public void testMysticSkull() {
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_mystic_skull", bloodfenRaptor);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			Minion newBloodfenRaptor = playMinionCard(context, player, (MinionCard) player.getHand().get(0));
			Assert.assertEquals(newBloodfenRaptor.getAttack(), 5);
		});
	}

	@Test
	public void testGiantDisappointment() {
		runGym((context, player, opponent) -> {
			Card card = CardCatalogue.getCardById("minion_giant_disappointment");
			context.getLogic().receiveCard(player.getId(), card);
			Assert.assertEquals(costOf(context, player, card), 8);
		});
	}

	@Test
	public void testQuestGiver() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bloodfen_raptor");
			playMinionCard(context, player, "minion_quest_giver");
			Assert.assertEquals(player.getDeck().get(0).getCardId(), "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_quest_giver");
			context.endTurn();
			context.endTurn();
			Minion newBloodfen = playMinionCard(context, player, (MinionCard) player.getHand().get(0));
			Assert.assertEquals(newBloodfen.getAttack(), 6);
			Assert.assertEquals(newBloodfen.getHp(), 5);
		});
	}

	@Test
	public void testPowerTrip() {
		// We reach turn 10 so we have 10 mana, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 10; i++) {
				context.endTurn();
				context.endTurn();
			}
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Our opponent gives us 10 mana somehow, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 2; i++) {
				context.endTurn();
				context.endTurn();
			}
			context.endTurn();
			Assert.assertEquals(player.getMaxMana(), 3);
			for (int i = 0; i < 7; i++) {
				playCard(context, opponent, "minion_arcane_golem");
				Assert.assertEquals(player.getMaxMana(), 3 + i + 1);
			}
			Assert.assertEquals(player.getMaxMana(), 10);
			Assert.assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Check that minions have +1/+1
		runGym((context, player, opponent) -> {
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_power_trip");
			Assert.assertEquals(bloodfenRaptor.getAttack(), bloodfenRaptor.getBaseAttack() + 1);
			Assert.assertEquals(bloodfenRaptor.getHp(), bloodfenRaptor.getBaseHp() + 1);
			context.endTurn();
			Minion opponentMinion = playMinionCard(context, player, "minion_chillwind_yeti");
			context.endTurn();
			playCardWithTarget(context, player, "spell_mind_control", opponentMinion);
			Assert.assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack() + 1);
			Assert.assertEquals(opponentMinion.getHp(), opponentMinion.getBaseHp() + 1);
		});

		// Check that Saronite Chain Gang has correct stats
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			playCard(context, player, "minion_saronite_chain_gang");
			player.getMinions().forEach(m -> Assert.assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> Assert.assertEquals(m.getHp(), 4));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_saronite_chain_gang");
			playCard(context, player, "spell_power_trip");
			player.getMinions().forEach(m -> Assert.assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> Assert.assertEquals(m.getHp(), 4));
		});
	}

	@Test
	public void testDancemistress() {
		// When this minion is healed, check if Crazed Dancer is summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", dancemistress);
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_crazed_dancer");
			// Check if the Crazed Dancer has attack and hp of 2
			Assert.assertEquals(player.getMinions().get(1).getBaseAttack(), 2);
			Assert.assertEquals(player.getMinions().get(1).getBaseHp(), 2);
		});

		// When a different minion is healed, Crazed Dancer is NOT summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCardWithTarget(context, player, "spell_ancestral_healing", bloodfenRaptor);
			Assert.assertFalse(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_crazed_dancer")));
		});
	}

	@Test
	public void testSpikeToedBooterang() {
		// Attacks a opponent's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, opponent, "minion_river_crocolisk");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			Assert.assertEquals(opponent.getMinions().get(0).getHp(), 1);
		});

		// Attacks player's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, player, "minion_river_crocolisk");
			playCardWithTarget(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			Assert.assertEquals(player.getMinions().get(0).getHp(), 1);
		});

		// Defeats a Divine Shield
		runGym((context, player, opponent) -> {
			Minion silvermoonGuardian = playMinionCard(context, opponent, "minion_silvermoon_guardian");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", silvermoonGuardian);
			Assert.assertEquals(opponent.getMinions().get(0).getHp(), 2);
		});

		// If attacking Imp Gang Boss, summons two 1/1 Imps for opponent
		runGym((context, player, opponent) -> {
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			context.endTurn();
			playCardWithTarget(context, player, "spell_spike_toed_booterang", impGangBoss);
			Assert.assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_imp");
			Assert.assertEquals(opponent.getMinions().get(2).getSourceCard().getCardId(), "token_imp");
		});
	}

	@Test
	public void testStablePortal() {
		// Correctly adds a Beast to player's hand with a mana cost 2 less
		runGym((context, player, opponent) -> {
			GameLogic spiedLogic = Mockito.spy(context.getLogic());
			context.setLogic(spiedLogic);

			Mockito.doAnswer(invocation ->
					CardCatalogue.getCardById("minion_malorne"))
					.when(spiedLogic)
					.removeRandom(Mockito.anyList());

			playCard(context, player, "spell_stable_portal");
			Card card = player.getHand().get(0);
			Assert.assertEquals(card.getCardId(), "minion_malorne");
			int baseMana = card.getBaseManaCost();
			Assert.assertEquals(baseMana, 7);
			Assert.assertEquals(card.getRace(), Race.BEAST);
			Assert.assertEquals(costOf(context, player, card), baseMana - 2);
		});
	}

	@Test
	public void testWyrmrestSniper() {
		// Friendly Dragon survives damage so 3 damage is dealt to the opponent hero
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_sleepy_dragon");
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			Assert.assertFalse(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Friendly Dragon does not survive damage, no damage is dealt
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_sleepy_dragon");
			// Set hp to 1 so it dies
			minion.setHp(1);
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			Assert.assertTrue(minion.isDestroyed());
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Enemy Dragon survives damage, no damage is dealt to the opponent's hero
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			Minion minion = playMinionCard(context, opponent, "minion_sleepy_dragon");

			// Damages minions by 1
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			Assert.assertFalse(minion.isDestroyed());
			Assert.assertEquals(minion.getHp(), minion.getBaseHp() - 1);
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			Assert.assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});
	}
}
