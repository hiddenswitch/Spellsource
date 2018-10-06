package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.PlayChooseOneCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class BoomsdayProjectTests extends TestBase {

	@Test
	public void testMissileLauncher() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion shouldBeDestroyed = playMinionCard(context, opponent, "minion_wisp");
			context.endTurn();
			Minion wargear = playMinionCard(context, player, "minion_wargear");
			playCardWithTarget(context, player, "minion_missile_launcher", wargear);
			assertEquals(player.getMinions().size(), 1);
			context.endTurn();
			assertTrue(shouldBeDestroyed.isDestroyed());
		});
	}

	@Test
	public void testWeaponsProject() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_weapons_project");
			assertEquals(player.getHero().getArmor(), 6);
			assertEquals(opponent.getHero().getArmor(), 6);
			assertEquals(player.getHero().getWeapon().getSourceCard().getCardId(), "weapon_gearblade");
			assertEquals(opponent.getHero().getWeapon().getSourceCard().getCardId(), "weapon_gearblade");
		});
	}

	@Test
	public void testDrMorrigan() {
		runGym((context, player, opponent) -> {
			Minion morrigan = playMinionCard(context, player, "minion_dr__morrigan");
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			destroy(context, morrigan);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_dr__morrigan");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});

		runGym((context, player, opponent) -> {
			Minion morrigan = playMinionCard(context, player, "minion_dr__morrigan");
			destroy(context, morrigan);
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion morrigan = playMinionCard(context, player, "minion_dr__morrigan");
			shuffleToDeck(context, player, "minion_deathwing");
			destroy(context, morrigan);
			assertEquals(player.getMinions().size(), 1, "minions");
			assertEquals(player.getDeck().size(), 1, "deck");
			assertEquals(player.getMinions().get(0).getName(), "Deathwing", "Deathwing");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_dr__morrigan");
			context.endTurn();
			context.endTurn();
			playCard(context, player, player.getHand().get(0));
			context.endTurn();
			context.endTurn();
			assertEquals(player.getMinions().size(), 2, "minions");
		});


		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_dr__morrigan");
			playCard(context, player, "minion_spiritsinger_umbra");
			playCard(context, player, "minion_knife_juggler");
			playCard(context, player, "minion_dr__morrigan");
			assertTrue(opponent.getHero().isDestroyed());
		});
	}

	@Test
	public void testTreantSynergyCards() { //dendrologist, mulchmuncher, landscaping
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_dendrologist");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "token_treant");
			playCard(context, player, "minion_dendrologist");
			assertEquals(player.getHand().size(), 1);
		});

		/*
		runGym(((context, player, opponent) -> {
			receiveCard(context, player, "minion_mulchmuncher");
			context.getLogic().modifyMaxMana(player, 4);
			playCard(context, player, "spell_living_mana"); //these bad boys should count as treants too
			assertEquals(player.getMinions().size(), 5);
			playCard(context, player, "spell_landscaping");
			playCard(context, player, "spell_twisting_nether");
			receiveCard(context, player, "minion_mulchmuncher");
			assertEquals(player.getHand().get(0).getManaCost(context, player), 3);
			assertEquals(player.getHand().get(1).getManaCost(context, player), 3);
		}));
    */
	}

	@Test
	public void testFlobbidinousFloop() {
		runGym(((context, player, opponent) -> {
			receiveCard(context, player, "minion_flobbidinous_floop");
			playCard(context, player, "minion_stonetusk_boar");
			assertEquals(player.getHand().get(0).getAttack(), 3);
			assertEquals(player.getHand().get(0).getHp(), 4);
			assertEquals(context.getLogic().getModifiedManaCost(player, player.getHand().get(0)), 4);
			playCard(context, player, player.getHand().get(0));
			assertEquals(player.getMinions().get(1).getAttack(), 3);
			assertEquals(player.getMinions().get(1).getHp(), 4);
		}));
	}

	@Test
	public void testMagnets() {
		runGym((context, player, opponent) -> {
			Minion mech = playMinionCard(context, player, "minion_upgradeable_framebot");
			playCardWithTarget(context, player, "minion_spider_bomb", mech);
			Minion anotherMech = playMinionCard(context, player, "minion_spider_bomb");
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(0).getName(), "Upgradeable Framebot");
			assertEquals(player.getMinions().get(0).getAttack(), 3);
			assertEquals(player.getMinions().get(0).getHp(), 7);
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.DEATHRATTLES));
			assertEquals(player.getMinions().get(1).getName(), "Spider Bomb");
			playCardWithTarget(context, player, "minion_zilliax", anotherMech);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getAttack(), 5, "attack");
			assertEquals(player.getMinions().get(1).getHp(), 4, "hp");
			for (Attribute attribute : Arrays.asList(Attribute.DEATHRATTLES, Attribute.LIFESTEAL, Attribute.RUSH, Attribute.DIVINE_SHIELD, Attribute.TAUNT)) {
				assertTrue(player.getMinions().get(1).hasAttribute(attribute), attribute.toString());
			}

			playCard(context, player, "spell_twisting_nether");
			playCard(context, player, "spell_kangors_endless_army");
			assertEquals(player.getMinions().size(), 2);
			Minion one = (Minion) find(context, "minion_upgradeable_framebot");
			Minion two = (Minion) find(context, "minion_spider_bomb");
			assertEquals(one.getAttack(), 3);
			assertEquals(one.getHp(), 7);
			assertTrue(one.hasAttribute(Attribute.DEATHRATTLES));
			assertEquals(two.getAttack(), 5, "attack");
			assertEquals(two.getHp(), 4, "hp");
			for (Attribute attribute : Arrays.asList(Attribute.DEATHRATTLES, Attribute.LIFESTEAL, Attribute.RUSH, Attribute.DIVINE_SHIELD, Attribute.TAUNT)) {
				assertTrue(two.hasAttribute(attribute), attribute.toString());
			}
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_venomizer");
			playCardWithTarget(context, player, "minion_missile_launcher", player.getMinions().get(0));
			playCard(context, opponent, "minion_ultrasaur");
			playCard(context, opponent, "minion_ultrasaur");
			playCard(context, opponent, "minion_ultrasaur");
			playCard(context, opponent, "minion_ultrasaur");
			context.endTurn();
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_mecharoo");
			playCardWithTarget(context, player, "minion_replicating_menace", player.getMinions().get(0));
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getMinions().size(), 4);
		});


	}

	@Test
	public void testCelestialEmissary() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, opponent, "token_shadow_of_nothing"); //no fatigue
			playCard(context, player, "minion_celestial_emissary");
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 22);
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 16);
			playCard(context, player, "minion_celestial_emissary");
			context.endTurn();
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 10);
		});
	}

	@Test
	public void testUnexpectedResults() { //well hopefully we can get some expected results here
		runGym((context, player, opponent) -> {
			// Just summon basic cards so that there aren't so many weird interactions
			context.setDeckFormat(new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC));
			playCard(context, player, "spell_unexpected_results");
			assertEquals(player.getMinions().get(0).getSourceCard().getBaseManaCost(), 2);
			assertEquals(player.getMinions().get(1).getSourceCard().getBaseManaCost(), 2);
		});

		runGym((context, player, opponent) -> {
			context.setDeckFormat(new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC));
			playCard(context, player, "minion_bloodmage_thalnos");
			playCard(context, player, "spell_unexpected_results");
			assertEquals(player.getMinions().get(1).getSourceCard().getBaseManaCost(), 3);
			assertEquals(player.getMinions().get(2).getSourceCard().getBaseManaCost(), 3);
		});

	}

	@Test
	public void testStargazerLuna() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "token_shadow_of_nothing"); //it just feels like a good card to use, OK?
			}
			Card fireball1 = receiveCard(context, player, "spell_fireball");
			Card fireball2 = receiveCard(context, player, "spell_fireball");
			playCard(context, player, "minion_stargazer_luna");
			playCardWithTarget(context, player, fireball2, opponent.getHero());
			assertEquals(player.getHand().getCount(), 2);
			playCardWithTarget(context, player, fireball1, opponent.getHero());
			assertEquals(player.getHand().getCount(), 1);
		});
	}

	@Test
	public void testStarAligner() { //The stars align! Fear their portents!
		runGym((context, player, opponent) -> {
			Minion bot = playMinionCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, player, "minion_star_aligner");
			assertEquals(opponent.getHero().getHp(), 30);
			assertEquals(bot.getHp(), 9);
			playCard(context, player, "minion_star_aligner");
			assertEquals(opponent.getHero().getHp(), 30);
			assertEquals(bot.getHp(), 9);
			playCard(context, player, "minion_star_aligner");
			assertEquals(opponent.getHero().getHp(), 23);
			assertEquals(bot.getHp(), 2);
		});
	}

	@Test
	public void testCrystalsmithKangor() {
		runGym((context, player, opponent) -> {
			playCardWithTarget(context, player, "spell_pyroblast", player.getHero());
			assertEquals(player.getHero().getHp(), 20);
			Minion kangor = playMinionCard(context, player, "minion_crystalsmith_kangor");
			attack(context, player, kangor, opponent.getHero());
			assertEquals(player.getHero().getHp(), 22);
			useHeroPower(context, player, player.getHero().getReference());
			assertEquals(player.getHero().getHp(), 26);
			playCardWithTarget(context, player, "spell_drain_life", opponent.getHero());
			assertEquals(player.getHero().getHp(), 30);
		}, HeroClass.WHITE, HeroClass.RED);
	}

	@Test
	public void testAutoDefenseMatric() {
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, opponent, "minion_upgradeable_framebot");
			Minion defender = playMinionCard(context, player, "token_defender"); //he lives!
			Minion bolvar = playMinionCard(context, player, "minion_bolvar_fireblood");
			playCard(context, player, "secret_autodefense_matrix");
			if (context.getActivePlayerId() == player.getId()) {
				context.endTurn();
			}
			attack(context, opponent, attacker, defender);
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(attacker.getHp(), 3, "hp");
			assertEquals(defender.getHp(), 1, "hp");
			assertTrue(!defender.isDestroyed());
			assertEquals(bolvar.getAttack(), 3, "bolvar");
		});
	}


	@Test
	public void testOmegaCards() {
		runGym((context, player, opponent) -> {
			Minion defender1 = playMinionCard(context, player, "minion_omega_defender");
			assertEquals(defender1.getAttack(), 2);
			context.getLogic().modifyMaxMana(player, 10);
			Minion defender2 = playMinionCard(context, player, "minion_omega_defender");
			assertEquals(defender2.getAttack(), 12);
		});

		runGym((context, player, opponent) -> {
			playCardWithTarget(context, player, "spell_pyroblast", player.getHero());
			Minion defender1 = playMinionCard(context, player, "minion_omega_medic");
			assertEquals(player.getHero().getHp(), 20);
			context.getLogic().modifyMaxMana(player, 10);
			Minion defender2 = playMinionCard(context, player, "minion_omega_medic");
			assertEquals(player.getHero().getHp(), 30);
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_omega_agent");
			assertEquals(player.getMinions().size(), 1);
			context.getLogic().modifyMaxMana(player, 10);
			playMinionCard(context, player, "minion_omega_agent");
			assertEquals(player.getMinions().size(), 4);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_omega_assembly");
			assertEquals(player.getHand().size(), 1);
			context.getLogic().modifyMaxMana(player, 10);
			playCard(context, player, "spell_omega_assembly");
			assertEquals(player.getHand().size(), 4);
		});

		runGym((context, player, opponent) -> {
			playCardWithTarget(context, player, "spell_pyroblast", player.getHero());
			playCardWithTarget(context, player, "spell_pyroblast", player.getHero());
			assertEquals(player.getMaxMana(), 1);
			assertEquals(player.getHero().getHp(), 10);
			playCard(context, player, "minion_omega_mind");
			playCardWithTarget(context, player, "spell_pyroblast", opponent.getHero());
			assertEquals(player.getHero().getHp(), 10);
			context.getLogic().modifyMaxMana(player, 10);
			playCard(context, player, "minion_omega_mind");
			playCardWithTarget(context, player, "spell_pyroblast", opponent.getHero());
			assertEquals(player.getHero().getHp(), 20);
		});

	}

	@Test
	public void testRecklessExperimenter() {
		runGym((context, player, opponent) -> {
			context.getLogic().modifyCurrentMana(player.getId(), 10, false);
			playCard(context, player, "minion_reckless_experimenter");
			assertEquals(player.getMana(), 5);
			playCard(context, player, "minion_devilsaur_egg");
			assertEquals(player.getMana(), 5);
			context.endTurn();
			assertEquals(player.getMinions().get(1).getName(), "Devilsaur");

		});
	}

	@Test
	public void testZerekMasterCloner() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_zerek_master_cloner");
			playCardWithTarget(context, player, "spell_fireball", opponent.getHero());
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getMinions().size(), 0);

			Minion zerek = playMinionCard(context, player, "minion_zerek_master_cloner");
			playCardWithTarget(context, player, "spell_power_word_shield", zerek);
			assertTrue(zerek.hasAttribute(Attribute.RESERVED_BOOLEAN_4));
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getMinions().size(), 1);
		});

		runGym((context, player, opponent) -> { //I'm assuming that this is the way Zerek is supposed to work?
			Minion zerek = playMinionCard(context, player, "minion_zerek_master_cloner");
			playCardWithTarget(context, player, "spell_power_word_replicate", zerek);
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testZerekCloningGallery() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_cairne_bloodhoof");
			shuffleToDeck(context, player, "minion_devilsaur_egg");
			shuffleToDeck(context, player, "minion_devilsaur_egg");
			shuffleToDeck(context, player, "minion_devilsaur_egg");
			playCard(context, player, "spell_zereks_cloning_gallery");
			int cairne = 0;
			int egg = 0;
			for (Minion minion : player.getMinions()) {
				assertEquals(minion.getAttack(), 1);
				assertEquals(minion.getHp(), 1);
				if (minion.getName().equals("Cairne Bloodhoof")) {
					cairne++;
				} else if (minion.getName().equals("Devilsaur Egg")) {
					egg++;
				}
			}

			assertEquals(cairne, 1);
			assertEquals(egg, 3);
		});
	}

	@Test
	public void testMyraRotspring() {
		runGym((context, player, opponent) -> {
			Minion myra = playMinionCard(context, player, "minion_myra_rotspring");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			assertEquals(player.getHand().size(), 1);
			assertTrue(myra.hasAttribute(Attribute.DEATHRATTLES), "deathrattle");
		});
	}

	@Test
	public void testAcademicEspionage() {
		runGym((context, player, opponent) -> {
			assertEquals(player.getDeck().size(), 0);
			assertEquals(opponent.getHero().getHeroClass(), HeroClass.RED, "class");
			playCard(context, player, "spell_academic_espionage");
			for (Card card : player.getDeck()) {
				assertEquals(costOf(context, player, card), 1, card.getName() + " mana");
				assertTrue(card.hasHeroClass(HeroClass.RED), card.getName() + " class");
			}
		}, HeroClass.RED, HeroClass.RED);

		/* TODO: Card cost modifiers should be copied with Academic Espionage
		runGym((context, player, opponent) -> {
			assertEquals(player.getDeck().size(), 0);
			assertEquals(opponent.getHero().getHeroClass(), HeroClass.RED, "class");
			playCard(context, player, "minion_augmented_elekk");
			playCard(context, player, "spell_academic_espionage");

			assertEquals(player.getDeck().size(), 20);
			for (Card card : player.getDeck()) {
				assertEquals(costOf(context, player, card), 1, card.getName() + " mana");
				assertTrue(card.hasHeroClass(HeroClass.RED), card.getName() + " class");
			}
		}, HeroClass.RED, HeroClass.BLACK);
		*/
	}

	@Test
	public void testAugmentedElekk() {
		runGym((context, player, opponent) -> {
			Minion elekk = playMinionCard(context, player, "minion_augmented_elekk");
			playCardWithTarget(context, player, "minion_lab_recruiter", elekk);
			assertEquals(player.getDeck().size(), 6);
			playCardWithTarget(context, opponent, "spell_gang_up", elekk);
			assertEquals(opponent.getDeck().size(), 3);
			assertEquals(player.getDeck().size(), 6);
		});
	}

	@Test
	public void testAugmentedElekkWeasel() { //currently only shuffles 1, we'll see how it works in real life
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_augmented_elekk");
			Minion weasel = playMinionCard(context, player, "minion_weasel_tunneler");
			destroy(context, weasel);
			for (Card card : opponent.getDeck()) {
				//System.out.println(card.getName());
			}
		});
	}

	@Test
	public void testAugmentedElekkScream() { //currently only shuffles 1 each, we'll see how it works in real life
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_snowflipper_penguin");
			playMinionCard(context, player, "minion_augmented_elekk");
			playMinionCard(context, player, "minion_wisp");
			playCard(context, player, "spell_psychic_scream");
			for (Card card : opponent.getDeck()) {
				//System.out.println(card.getName());
			}
		});
	}

	@Test
	public void testPogoHopper() {
		runGym((context, player, opponent) -> {
			Minion pogo11 = playMinionCard(context, player, "minion_pogo_hopper");
			Minion pogo33 = playMinionCard(context, player, "minion_pogo_hopper");
			Minion pogo55 = playMinionCard(context, player, "minion_pogo_hopper");
			Minion pogo77 = playMinionCard(context, player, "minion_pogo_hopper");
			assertEquals(pogo11.getAttack(), 1);
			assertEquals(pogo11.getHp(), 1);
			assertEquals(pogo33.getAttack(), 3);
			assertEquals(pogo33.getHp(), 3);
			assertEquals(pogo55.getAttack(), 5);
			assertEquals(pogo55.getHp(), 5);
			assertEquals(pogo77.getAttack(), 7);
			assertEquals(pogo77.getHp(), 7);
		});

	}

	@Test
	public void testMyrasUnstableElement() { //make sure faldorei strider procs + element doesn't fatigue you
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "token_shadow_of_nothing");
			}
			playCard(context, player, "minion_faldorei_strider");
			playCard(context, player, "spell_myras_unstable_element");
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHero().getHp(), 30);
		});

	}

	@Test
	public void testElectraStormsurge() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_electra_stormsurge");
			playCardWithTarget(context, player, "spell_lava_burst", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 20);
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 4);
			playCardWithTarget(context, player, "spell_lava_burst", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 15);
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 6);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_electra_stormsurge");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_feral_spirit");
			assertEquals(player.getMinions().size(), 3);
		});

	}

	@Test
	public void testDemonicProject() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_wisp");
			receiveCard(context, player, "spell_fireball");
			receiveCard(context, opponent, "minion_wisp");
			receiveCard(context, opponent, "spell_fireball");
			playCard(context, player, "spell_demonic_project");

			assertTrue(player.getHand().get(0).getRace().hasRace(Race.DEMON), player.getHand().get(0).getName());
			assertTrue(opponent.getHand().get(0).getRace().hasRace(Race.DEMON), opponent.getHand().get(0).getName());
		});
	}


	@Test
	public void testDrBoomMadGenius() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_violet_illusionist");
			playCard(context, opponent, "minion_violet_illusionist");
			playCard(context, player, "hero_dr_boom_mad_genius");
			assertNotEquals(player.getHero().getHeroPower().getCardId(), "hero_power_big_red_button");
			List<String> boomPowers = Arrays.asList("hero_power_micro_squad",
					"hero_power_zap_cannon",
					"hero_power_blast_shield",
					"hero_power_kaboom",
					"hero_power_delivery_drone");
			for (int i = 0; i < 10; i++) {
				String heroPower = player.getHero().getHeroPower().getCardId();
				assertTrue(boomPowers.contains(heroPower), heroPower);
				context.endTurn();
				context.endTurn();
				assertNotEquals(heroPower, player.getHero().getHeroPower().getCardId());
			}
		});

	}

	@Test
	public void testTheBoomShip() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_deathwing");
			receiveCard(context, player, "minion_deathwing");
			receiveCard(context, player, "minion_deathwing");
			receiveCard(context, player, "minion_deathwing");
			receiveCard(context, player, "minion_deathwing");
			playCard(context, player, "spell_the_boomship");
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getMinions().size(), 3);
			for (Minion minion : player.getMinions()) {
				assertTrue(minion.hasAttribute(Attribute.RUSH));
			}

		});
	}

	@Test
	public void testSupercollider() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_supercollider");
			Minion giant1 = playMinionCard(context, opponent, "minion_molten_giant");
			Minion giant2 = playMinionCard(context, opponent, "minion_molten_giant");
			Minion giant3 = playMinionCard(context, opponent, "minion_molten_giant");
			Minion giant4 = playMinionCard(context, opponent, "minion_molten_giant");
			Minion giant5 = playMinionCard(context, opponent, "minion_molten_giant");
			attack(context, player, player.getHero(), giant1);
			assertTrue(giant1.isDestroyed(), "giant1");
			assertTrue(giant2.isDestroyed(), "giant2");
			attack(context, player, player.getHero(), giant4);
			assertTrue(giant4.isDestroyed(), "giant4");
			assertTrue(giant3.isDestroyed() || giant5.isDestroyed());
		});
	}

	@Test
	public void testMechaThun() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_mecha_thun");
			destroy(context, player.getMinions().get(0));
			assertTrue(opponent.getHero().isDestroyed());
			//yup
		});
	}


	@Test
	public void testTheSoularium() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "token_shadow_of_nothing");
			shuffleToDeck(context, player, "token_shadow_of_nothing");
			shuffleToDeck(context, player, "token_shadow_of_nothing");

			Minion knight = playMinionCard(context, player, "minion_tiny_knight_of_evil");
			playCard(context, player, "spell_the_soularium");
			playCard(context, player, player.getHand().get(0));
			// Check that the effect doesn't remove all the cards from the player's hand
			Card other = receiveCard(context, player, "spell_the_coin");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(other.getZone(), Zones.HAND);
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 2);
		});

	}

	@Test
	public void testArcaneDynamo() {
		for (int i = 0; i < 25; i++) {
			runGym((context, player, opponent) -> {
				playCard(context, player, "minion_arcane_dynamo");
				assertTrue(player.getHand().get(0).getBaseManaCost() >= 5);

			});
		}
	}

	@Test
	public void testPrismaticLens() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_molten_giant");
			shuffleToDeck(context, player, "spell_frostbolt");
			playCard(context, player, "spell_prismatic_lens");
			Card giant = (Card) findCard(context, "minion_molten_giant");
			Card frostbolt = (Card) findCard(context, "spell_frostbolt");
			assertEquals(context.getLogic().getModifiedManaCost(player, giant), 2, "giant");
			assertEquals(context.getLogic().getModifiedManaCost(player, frostbolt), 20, "frostbolt");
		});
	}

	@Test
	public void testSubject9() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "secret_rat_trap");
			shuffleToDeck(context, player, "secret_snake_trap");
			shuffleToDeck(context, player, "secret_bear_trap");
			shuffleToDeck(context, player, "secret_bear_trap");
			shuffleToDeck(context, player, "secret_bear_trap");
			shuffleToDeck(context, player, "secret_bear_trap");
			playCard(context, player, "minion_subject_9");
			assertEquals(player.getHand().size(), 3);
		});

	}

	@Test
	public void testHarbingerCelestia() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_harbinger_celestia");
			playMinionCard(context, opponent, "minion_wisp");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_wisp");
			playMinionCard(context, opponent, "minion_ultrasaur");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_wisp");
		});
	}

	@Test
	public void testFlarksBoomZooka() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_raid_leader");
			shuffleToDeck(context, player, "minion_raid_leader");
			shuffleToDeck(context, player, "minion_raid_leader");
			Minion ancientOne = playMinionCard(context, opponent, "token_the_ancient_one");
			playCard(context, player, "spell_flarks_boom_zooka");
			assertEquals(ancientOne.getHp(), 21);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_flametongue_totem");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			Minion ancientOne = playMinionCard(context, opponent, "token_the_ancient_one");
			playCard(context, player, "spell_flarks_boom_zooka");
			assertEquals(ancientOne.getHp(), 21);
		});

	}

	@Test
	public void testHolomancer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_holomancer");
			playCard(context, opponent, "minion_ultrasaur");
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_ultrasaur");
			assertEquals(player.getMinions().get(1).getAttack(), 1);
			assertEquals(player.getMinions().get(1).getHp(), 1);
		});

	}

	@Test
	public void testChooseYourPathNeverGivesQuest() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 100; i++) {
				playCard(context, player, "spell_choose_your_path");
				assertEquals(player.getHand().size(), 1);
				assertFalse(player.getHand().get(0).isQuest());
				context.getLogic().removeCard(player.getHand().get(0));
			}
		});
	}

	@Test
	public void testTestSubjectCombo() {
		runGym((context, player, opponent) -> {
			Minion subject = playMinionCard(context, player, "minion_boomsday_test_subject");
			playCardWithTarget(context, player, "spell_divine_spirit", subject);
			playCardWithTarget(context, player, "spell_vivid_nightmare", subject);
			playCardWithTarget(context, player, "spell_topsy_turvy", player.getMinions().get(1));
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testTestSubjectChooseOne() {
		runGym((context, player, opponent) -> {
			Minion subject = playMinionCard(context, player, "minion_boomsday_test_subject");
			Card starfall = receiveCard(context, player, "spell_starfall");
			player.setMana(starfall.getBaseManaCost());
			PlayChooseOneCardAction choice = context.getValidActions().stream()
					.filter(c -> c instanceof PlayChooseOneCardAction)
					.map(PlayChooseOneCardAction.class::cast)
					.filter(f -> f.getChoiceCardId().equals("spell_starfall_1") && f.getTargetReference().equals(subject.getReference()))
					.findFirst()
					.orElseThrow(NullPointerException::new);

			context.getLogic().performGameAction(player.getId(), choice);
			assertTrue(subject.isDestroyed());
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_starfall");
		});
	}
}
