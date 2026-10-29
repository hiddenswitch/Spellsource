package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class SpellsourceRareAuditTests extends TestBase {

	private Minion playMinionCardWithBattlecry(GameContext context, Player player, String cardId, Actor target) {
		playCard(context, player, cardId, target);
		return player.getMinions().isEmpty() ? null : player.getMinions().get(player.getMinions().size() - 1);
	}

	// ==================== AMBER ====================

	@Test
	public void testMinionBackwaterWise() {
		runGym((context, player, opponent) -> {
			Card faeInDeck = putOnTopOfDeck(context, player, "minion_glimmer_dancer"); // FAE
			playMinionCard(context, player, "minion_glimmer_dancer");
			playMinionCard(context, player, "minion_backwater_wise");
			assertEquals(Zones.HAND, faeInDeck.getZone());
		});
	}

	@Test
	public void testMinionBait() {
		runGym((context, player, opponent) -> {
			Minion bait = playMinionCard(context, player, "minion_bait");
			destroy(context, bait);
			assertEquals(4, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_spiderling", m.getSourceCard().getCardId());
			}
		});
	}

	@Test
	public void testMinionBigBro() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_big_bro");
			assertEquals(1, player.getHand().size());
			assertEquals("token_lil_bro", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionBroodguard() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion broodguard = playMinionCard(context, player, "minion_broodguard");
			assertTrue(broodguard.hasAttribute(Attribute.TAUNT));
			destroy(context, broodguard);
			assertEquals(4, raptor.getAttack());
			assertEquals(3, raptor.getHp());
		});
	}

	@Test
	public void testMinionBugBoss() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast"); // 3/2
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playMinionCardWithBattlecry(context, player, "minion_bug_boss", ogre); // 4/4 + 3/2 = 7 damage
			assertTrue(ogre.isDestroyed());
		});
	}

	@Test
	public void testMinionColossalSpider() {
		runGym((context, player, opponent) -> {
			Minion spider = playMinionCard(context, player, "minion_colossal_spider");
			assertEquals(10, spider.getAttack());
			assertEquals(10, spider.getHp());
			assertTrue(spider.hasAttribute(Attribute.TAUNT));
			assertTrue(spider.hasAttribute(Attribute.POISONOUS));
		});
	}

	@Test
	public void testMinionDieInsect() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			Minion insect = playMinionCard(context, player, "minion_die_insect");
			destroy(context, insect);
			assertTrue(ogre.isDestroyed());
		});
	}

	@Test
	public void testMinionLadyBug() {
		runGym((context, player, opponent) -> {
			Minion ladybug = playMinionCard(context, player, "minion_lady_bug");
			Minion other = playMinionCard(context, player, "minion_test_4_5");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			destroy(context, raptor);
			assertEquals(6, other.getHp());
			assertEquals(2, ladybug.getHp());
		});
	}

	@Test
	public void testMinionMenagerieCaretaker() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_menagerie_caretaker");
			assertEquals(3, player.getHand().size());
			assertTrue(player.getHand().stream().anyMatch(c -> "token_spiderling".equals(c.getCardId())));
			assertTrue(player.getHand().stream().anyMatch(c -> "token_whelpling".equals(c.getCardId())));
			assertTrue(player.getHand().stream().anyMatch(c -> "token_murkling".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionMotherSpider() {
		runGym((context, player, opponent) -> {
			Minion spider = playMinionCard(context, player, "minion_mother_spider");
			destroy(context, spider);
			assertEquals(2, player.getDeck().size());
			for (Card c : player.getDeck()) {
				assertEquals("minion_mother_spider", c.getCardId());
			}
		});
	}

	@Test
	public void testMinionNurseryGuard() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion guard = playMinionCard(context, player, "minion_nursery_guard");
			assertTrue(guard.hasAttribute(Attribute.TAUNT));
			destroy(context, guard);
			assertEquals("token_spidey_egg", player.getDeck().peek().getCardId());
		});
	}

	@Test
	public void testMinionRiverRider() {
		runGym((context, player, opponent) -> {
			Minion rider = playMinionCard(context, player, "minion_river_rider");
			assertEquals(2, rider.getAttack());
			playMinionCard(context, player, "minion_test_3_2_beast");
			assertEquals(4, rider.getAttack());
		});
	}

	@Test
	public void testMinionSamureetle() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_samureetle");
			player.setMana(10);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(4, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionSpringer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_springer");
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getHand().size());
		});
	}

	@Test
	public void testMinionSwarmlord() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_spiderling");
			receiveCard(context, player, "token_spiderling");
			playMinionCard(context, player, "minion_swarmlord");
			assertEquals(3, player.getMinions().size());
			assertEquals(0, player.getHand().size());
		});
	}

	@Test
	public void testMinionWeaponweaver() {
		runGym((context, player, opponent) -> {
			Card raptor = receiveCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_weaponweaver");
			assertTrue(raptor.hasAttribute(Attribute.POISONOUS));
		});
	}

	@Test
	public void testSpellAChallengerAppears() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_a_challenger_appears", yeti);
			assertEquals(4, yeti.getHp());
			assertEquals(0, player.getMinions().size());
		});
	}

	@Test
	public void testSpellBeeSquad() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_bee_squad");
			assertEquals(3, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_bee", m.getSourceCard().getCardId());
				assertTrue(m.hasAttribute(Attribute.CHARGE));
				assertTrue(m.hasAttribute(Attribute.POISONOUS));
			}
		});
	}

	@Test
	public void testSpellBeetleBash() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_beetle_bash");
			assertEquals(4, player.getHero().getArmor());
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_blue_beetle", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellForestCalling() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast"); // BEAST
			putOnTopOfDeck(context, player, "token_44dragon"); // DRAGON
			putOnTopOfDeck(context, player, "minion_glimmer_dancer"); // FAE
			playCard(context, player, "spell_forest_calling");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellHarden() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5"); // 4/5
			playCard(context, player, "spell_harden", yeti);
			assertEquals(9, yeti.getHp());
			assertEquals(9, player.getHero().getArmor());
		});
	}

	@Test
	public void testSpellHiveMind() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast"); // 3/2
			playMinionCard(context, player, "minion_test_4_5"); // 4/5
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playCard(context, player, "spell_hive_mind", ogre);
			assertTrue(ogre.isDestroyed());
		});
	}

	@Test
	public void testSpellImpale() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5"); // 4/5
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playCard(context, player, "spell_impale", yeti);
			assertEquals(3, ogre.getHp());
		});
	}

	@Test
	public void testSpellIncubation() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_incubation", raptor);
			assertEquals(3, player.getHand().size());
			for (Card c : player.getHand()) {
				assertEquals("minion_test_3_2_beast", c.getCardId());
			}
		});
	}

	@Test
	public void testSpellLesserAmberSpellstone() {
		runGym((context, player, opponent) -> {
			Card spellstone = receiveCard(context, player, "spell_lesser_amber_spellstone");
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_3_2_beast");
			assertEquals("spell_amber_spellstone", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellPheromones() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_deal_1");
			putOnTopOfDeck(context, player, "spell_high_note");
			playCard(context, player, "spell_pheromones");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellPincerAttack() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion center = playMinionCard(context, opponent, "minion_test_4_5");
			Minion right = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_pincer_attack");
			assertTrue(left.isDestroyed());
			assertFalse(center.isDestroyed());
			assertTrue(right.isDestroyed());
		});
	}

	@Test
	public void testSpellPollinate() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_pollinate", ogre);
			assertEquals(2, player.getMinions().size());
			Minion copy = player.getMinions().get(1);
			assertEquals(1, copy.getAttack());
			assertEquals(1, copy.getHp());
		});
	}

	@Test
	public void testSpellProtectTheHive() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_protect_the_hive");
			assertEquals(2, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_wasp", m.getSourceCard().getCardId());
				assertTrue(m.hasAttribute(Attribute.POISONOUS));
			}
		});
	}

	@Test
	public void testSpellSecondWave() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_second_wave");
			destroy(context, raptor);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellSwarmShield() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_swarm_shield");
			assertEquals(6, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionNanny() {
		runGym((context, player, opponent) -> {
			Card raptor = receiveCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_nanny");
			context.endTurn();
			assertEquals(1, raptor.getAttributeValue(Attribute.ATTACK_BONUS));
			assertEquals(1, raptor.getAttributeValue(Attribute.HP_BONUS));
		});
	}

	@Test
	public void testSpellMoonlightDancers() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_moonlight_dancers");
			destroy(context, raptor);
			assertEquals(1, player.getHand().size());
			assertTrue(player.getHand().get(0).getCardId().startsWith("secret_secret_of_"));
		});
	}

	@Test
	public void testSpellPrimordialOoze() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, player, "minion_neutral_test_6_7");
			destroy(context, ogre);
			playMinionCard(context, player, "token_spiderling");
			playCard(context, player, "spell_primordial_ooze");
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellRoyalDecree() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			overrideDiscover(context, player, discoverActions -> discoverActions.stream()
					.filter(da -> "spell_pheromone_attack".equals(da.getCard().getCardId()))
					.findFirst().orElse(discoverActions.get(0)));
			playCard(context, player, "spell_royal_decree");
			assertTrue(raptor.hasAttribute(Attribute.AURA_RUSH));
		});
	}

	@Test
	public void testWeaponPoisonStingSpear() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "weapon_poison_sting-spear");
			attack(context, player, player.getHero(), ogre);
			assertTrue(ogre.isDestroyed());
		});
	}

	// ==================== AZURE ====================

	@Test
	public void testMinionFaeTricksterSkyward() {
		runGym((context, player, opponent) -> {
			Card yeti = putOnTopOfDeck(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_fae_trickster_skyward");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(3, costOf(context, player, yeti));
		});
	}

	@Test
	public void testMinionMoonlitDragon() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_lunstone");
			Minion dragon = playMinionCard(context, player, "minion_moonlit_dragon");
			assertEquals(4, dragon.getHp());
		});
	}

	@Test
	public void testMinionRumblingStormfront() {
		runGym((context, player, opponent) -> {
			Minion storm = playMinionCard(context, player, "minion_rumbling_stormfront");
			assertTrue(storm.hasAttribute(Attribute.RUSH));
			assertTrue(storm.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(storm.hasAttribute(Attribute.TAUNT));
			assertTrue(storm.hasAttribute(Attribute.DECAY));
			context.endTurn();
			assertFalse(storm.hasAttribute(Attribute.DIVINE_SHIELD));
			context.endTurn();
			context.endTurn();
			assertEquals(8, storm.getHp());
		});
	}

	@Test
	public void testMinionSolisCaster() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playMinionCard(context, player, "minion_solis_caster");
			assertEquals(7, player.getHand().size());
		});
	}

	@Test
	public void testSpellFaerieSquadron() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_faerie_squadron");
			assertEquals(3, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertTrue(Race.hasRace(context, m, "FAE"));
			}
		});
	}

	@Test
	public void testMinionBallLightning() {
		runGym((context, player, opponent) -> {
			Card ball = receiveCard(context, player, "minion_ball_lightning");
			assertEquals(7, costOf(context, player, ball));
			receiveCard(context, player, "spell_lunstone");
			assertEquals(6, costOf(context, player, ball));
		});
	}

	@Test
	public void testMinionStormchaser() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_stormchaser");
			context.endTurn();
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionFaeWarcaller() {
		runGym((context, player, opponent) -> {
			Minion fae = playMinionCard(context, player, "minion_glimmer_dancer"); // 3/3 FAE
			playMinionCard(context, player, "minion_fae_warcaller");
			assertEquals(4, fae.getAttack());
			assertEquals(4, fae.getHp());
		});
	}

	@Test
	public void testSpellAerialManeuver() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_aerial_maneuver", ogre);
			assertEquals(0, player.getMinions().size());
			assertEquals(1, player.getHand().size());
			assertEquals(0, costOf(context, player, player.getHand().get(0)));
		});
	}

	@Test
	public void testSpellUpheaval() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playCard(context, player, "spell_upheaval"); // 3 + 2 + 1 = 6 damage
			assertEquals(1, ogre.getHp());
		});
	}

	// ==================== BLUEGREY ====================

	@Test
	public void testMinionBogoveyTraitor() {
		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, opponent, "token_44dragon");
			playMinionCardWithBattlecry(context, player, "minion_bogovey_traitor", dragon);
			assertTrue(dragon.isDestroyed());
		});
	}

	@Test
	public void testMinionDarkfireBlaster() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_darkfire_blaster", target);
			assertEquals(2, target.getHp());
		});
	}

	@Test
	public void testMinionShadyNamahage() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			ally.setAttribute(Attribute.STEALTH);
			playMinionCardWithBattlecry(context, player, "minion_shady_namahage", ally);
			assertTrue(ally.hasAttribute(Attribute.WINDFURY));
		});
	}

	@Test
	public void testSpellChaoticSun() {
		runGym((context, player, opponent) -> {
			opponent.getAttributes().remove(Attribute.DISABLE_FATIGUE);
			int hpBefore = opponent.getHero().getHp();
			playCard(context, player, "spell_chaotic_sun", opponent.getHero());
			assertEquals(hpBefore - 5, opponent.getHero().getHp()); // 4 + 1 fatigue
		});
	}

	@Test
	public void testSpellMigraine() {
		runGym((context, player, opponent) -> {
			int hpBefore = player.getHero().getHp();
			playCard(context, player, "spell_migraine");
			assertEquals(hpBefore - 2, player.getHero().getHp());
			assertTrue(player.getHero().hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testSpellRally() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(5);
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_rally", ally);
			assertEquals(2, player.getMinions().size());
			assertEquals(1, player.getHand().size());
			assertEquals(3, player.getMaxMana());
		});
	}

	@Test
	public void testSpellThinSmoke() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "minion_test_4_5");
			playCard(context, player, "spell_thin_smoke");
			assertEquals(2, player.getHand().size());
			assertEquals("spell_lunstone", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionBluefistTrainer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bluefist_trainer");
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_high_note", ally);
			assertEquals(6, ally.getAttack());
			assertEquals(3, ally.getHp());
		});
	}

	@Test
	public void testMinionNoviceRecruiter() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_novice_recruiter");
			assertEquals(0, costOf(context, player, player.getHeroPowerZone().get(0)));
		});
	}

	@Test
	public void testMinionRedhideSaboteur() {
		runGym((context, player, opponent) -> {
			opponent.getAttributes().remove(Attribute.DISABLE_FATIGUE);
			int hpBefore = opponent.getHero().getHp();
			Minion saboteur = playMinionCard(context, player, "minion_redhide_saboteur");
			assertTrue(saboteur.hasAttribute(Attribute.STEALTH));
			assertEquals(hpBefore - 1, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionStormcloudAssailant() {
		runGym((context, player, opponent) -> {
			Minion stealthAlly = playMinionCard(context, player, "minion_neutral_test");
			stealthAlly.setAttribute(Attribute.STEALTH);
			playMinionCard(context, player, "minion_stormcloud_assailant");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testMinionWaygateCommander() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_waygate_commander");
			Minion demon = playMinionCard(context, player, "minion_demon_test");
			assertTrue(demon.hasAttribute(Attribute.AURA_RUSH));
		});
	}

	@Test
	public void testSpellNurseWounds() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_high_note", ally);
			playCard(context, player, "spell_nurse_wounds");
			assertTrue(ally.hasAttribute(Attribute.STEALTH));
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellWarmongering() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_warmongering", ally);
			attack(context, player, ally, opponent.getHero());
			assertEquals(6, ally.getAttack());
			assertEquals(7, ally.getHp());
		});
	}

	@Test
	public void testMinionBullhorn() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_bullhorn");
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionFeatheredFantasy() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCardWithBattlecry(context, player, "minion_feathered_fantasy", ally);
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_3_2_beast", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellLeadByExample() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_test_3_2");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_lead_by_example", ally);
			assertEquals(5, ally.getAttack());
		});
	}

	@Test
	public void testSpellQueensCommand() {
		runGym((context, player, opponent) -> {
			int hpBefore = opponent.getHero().getHp();
			playCard(context, player, "spell_queens_command");
			playMinionCardWithBattlecry(context, player, "minion_test_deal_1", opponent.getHero());
			assertEquals(hpBefore - 2, opponent.getHero().getHp());
		});
	}

	// ==================== CAMO ====================

	@Test
	public void testMinionFrontlineCutthroat() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion cutthroat = playMinionCard(context, player, "minion_frontline_cutthroat");
			attack(context, player, cutthroat, enemy);
			assertEquals(3, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionJeeringBodyguard() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3/2
			Minion bodyguard = playMinionCardWithBattlecry(context, player, "minion_jeering_bodyguard", enemy); // 5/8
			assertTrue(enemy.isDestroyed());
			assertEquals(5, bodyguard.getHp());
		});
	}

	@Test
	public void testMinionWatchfulBruiser() {
		runGym((context, player, opponent) -> {
			Minion bruiser = playMinionCard(context, player, "minion_watchful_bruiser");
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			context.endTurn();
			assertTrue(bruiser.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
			attack(context, player, ally, opponent.getHero());
			assertFalse(bruiser.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
		});
	}

	@Test
	public void testSpellCoerce() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion e2 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_coerce", e1);
			assertTrue(e1.isDestroyed());
			assertTrue(e2.isDestroyed());
		});
	}

	@Test
	public void testSpellRoleCall() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_role_call");
			assertEquals(2, player.getHand().size());
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.BATTLECRY));
		});
	}

	@Test
	public void testWeaponCapturedFlag() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_test_4_5");
			shuffleToDeck(context, player, "spell_lunstone");
			playCard(context, player, "weapon_captured_flag");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testMinionOniCourier() {
		runGym((context, player, opponent) -> {
			Minion courier = playMinionCard(context, player, "minion_oni_courier");
			attack(context, player, courier, opponent.getHero());
			assertEquals(3, player.getHero().getArmor());
		});
	}

	@Test
	public void testSpellMarkedForDeath() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_marked_for_death", enemy);
			playCard(context, player, "spell_high_note", enemy); // 2 * 2 = 4 damage
			assertEquals(1, enemy.getHp());
		});
	}

	// ==================== CANDY ====================

	@Test
	public void testSpellDarlingAudience() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_darling_audience");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(3, ally.getAttack());
			assertEquals(3, ally.getHp());
		});
	}

	@Test
	public void testSpellFantasticalPlay() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playCard(context, player, "spell_fantastical_play");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellStarOfTheNight() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_star_of_the_night", ally);
			assertEquals(6, ally.getAttack());
			assertEquals(6, ally.getHp());
			assertEquals(2, player.getHand().size());
		});
	}

	// ==================== COPPER ====================

	@Test
	public void testMinionAdamantGunslinger() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_adamant_gunslinger");
			assertEquals(1, enemy.getHp());
		});
	}

	@Test
	public void testMinionDustbowlVigilante() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_dustbowl_vigilante");
			shuffleToDeck(context, player, "minion_neutral_test");
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testMinionTriggerHappyRebel() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_trigger_happy_rebel");
			shuffleToDeck(context, player, "minion_neutral_test");
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testSpellArmedAndDangerous() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			int hpBefore = opponent.getHero().getHp();
			playCard(context, player, "spell_armed_and_dangerous");
			assertEquals(1, player.getHand().size());
			useHeroPower(context, player, opponent.getHero().getReference());
			assertEquals(hpBefore - 2, opponent.getHero().getHp());
		}, HeroClass.COPPER, HeroClass.COPPER);
	}

	@Test
	public void testWeaponRustyPitchfork() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_trigger_happy_rebel");
			playCard(context, player, "weapon_rusty_pitchfork");
			assertEquals(3, player.getWeaponZone().get(0).getDurability());
		});
	}

	@Test
	public void testWeaponSilverScattershot() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "weapon_silver_scattershot");
			assertEquals(1, enemy.getHp());
		});
	}

	// ==================== CORAL ====================

	@Test
	public void testMinionHauntingHarpist() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Card harpist = receiveCard(context, player, "minion_haunting_harpist");
			playCard(context, player, "spell_heroic_ballad"); // +5/+5 to leftmost minion in hand
			playMinionCard(context, player, harpist);
			assertEquals(1, enemy.getAttack());
		});
	}

	@Test
	public void testMinionJauntyPianist() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_jaunty_pianist");
			Card inHand = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, inHand.getAttributeValue(Attribute.ATTACK_BONUS));
			assertEquals(1, inHand.getAttributeValue(Attribute.HP_BONUS));
		});
	}

	@Test
	public void testMinionMarchingBand() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_marching_band", enemy);
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testMinionRhythmThief() {
		runGym((context, player, opponent) -> {
			int hpBefore = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_rhythm_thief");
			playCard(context, player, "spell_lunstone");
			assertEquals(hpBefore - 2, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellBrassBlast() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_brass_blast", enemy); // 2 spells played this turn
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testSpellCollaboration() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_collaboration");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellHealingWord() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			player.getHero().setHp(20);
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_healing_word", player.getHero());
			assertEquals(23, player.getHero().getHp());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellHeroicBallad() {
		runGym((context, player, opponent) -> {
			Card inHand = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_heroic_ballad");
			Minion summoned = playMinionCard(context, player, inHand);
			assertEquals(7, summoned.getAttack());
			assertEquals(7, summoned.getHp());
		});
	}

	@Test
	public void testSpellMagicalSecrets() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_magical_secrets");
			playCard(context, player, "spell_high_note", enemy); // 2 + 2 = 4
			assertEquals(1, enemy.getHp());
		});
	}

	@Test
	public void testSpellSeductiveCharm() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			enemy.setAttribute(Attribute.TAUNT);
			playCard(context, player, "spell_seductive_charm", enemy);
			assertFalse(enemy.hasAttribute(Attribute.TAUNT));
			assertEquals(2, enemy.getAttack());
		});
	}

	@Test
	public void testWeaponAcclaimedAccordion() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_acclaimed_accordion");
			Card spell = receiveCard(context, player, "spell_test_deal_6"); // 4-cost
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(2, costOf(context, player, spell));
		});
	}

	@Test
	public void testWeaponDeafeningDrum() {
		runGym((context, player, opponent) -> {
			int hpBefore = opponent.getHero().getHp();
			playCard(context, player, "weapon_deafening_drum");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(hpBefore - 2, opponent.getHero().getHp()); // 1 weapon + 1 trigger
		});
	}

	@Test
	public void testWeaponGrittyGuitar() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "weapon_gritty_guitar");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(1, enemy.getHp());
		});
	}

	// ==================== CRIMSON ====================

	@Test
	public void testMinionLordsGuard() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_captive_overlord");
			playMinionCard(context, player, "token_underling_overlord");
			playMinionCard(context, player, "minion_lords_guard");
			assertEquals(6, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionBloodshardElemental() {
		runGym((context, player, opponent) -> {
			int hpBefore = opponent.getHero().getHp();
			Minion gargoyle = playMinionCard(context, player, "minion_bloodshard_elemental");
			destroy(context, gargoyle);
			assertEquals(hpBefore - 2, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionBoneMan() {
		runGym((context, player, opponent) -> {
			Minion berserker = playMinionCard(context, player, "minion_bone_man");
			assertTrue(berserker.hasAttribute(Attribute.CHARGE));
			destroy(context, berserker);
			assertEquals(1, player.getMinions().size());
			assertEquals("token_hired_guard", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionFrontlineCoward() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			receiveCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_frontline_coward");
			assertEquals(0, player.getHand().size());
			assertEquals(3, ally.getAttack());
			assertEquals(3, ally.getHp());
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionMagicGlutton() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			enemy.setAttribute(Attribute.TAUNT);
			Minion glutton = playMinionCard(context, player, "minion_magic_glutton");
			assertEquals(1, enemy.getAttack());
			destroy(context, glutton);
			assertFalse(enemy.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionPatheticSquire() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_pathetic_squire");
			assertEquals(0, costOf(context, player, player.getHeroPowerZone().get(0)));
		});
	}

	@Test
	public void testMinionRecruitBarracks() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_recruit_barracks");
			context.endTurn();
			assertEquals(2, player.getMinions().size());
			assertEquals("token_captive_overlord", player.getMinions().get(1).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionUnderworldGuard() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(18);
			Minion guard = playMinionCard(context, player, "minion_underworld_guard");
			assertEquals(5, guard.getAttack());
			assertEquals(8, guard.getHp());
		});
	}

	@Test
	public void testPactReinforcements() {
		runGym((context, player, opponent) -> {
			Minion u1 = playMinionCard(context, player, "token_underling_overlord");
			Minion u2 = playMinionCard(context, player, "token_underling_overlord");
			playCard(context, player, "pact_reinforcements");
			destroy(context, u1);
			destroy(context, u2);
			assertEquals(2, player.getMinions().size());
			assertEquals("token_captiveguard_overlord", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testPactVengefulCurse() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "pact_vengeful_curse");
			destroy(context, ally);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellCurseOfAim() {
		runGym((context, player, opponent) -> {
			int hpBefore = opponent.getHero().getHp();
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_curse_of_aim", enemy);
			context.endTurn();
			attack(context, opponent, enemy, player.getHero());
			assertEquals(hpBefore - 3, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellDevastation() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(10);
			Minion target = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3 attack
			Minion otherEnemy = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			playCard(context, player, "spell_devastation", target);
			assertTrue(target.isDestroyed());
			assertEquals(2, otherEnemy.getHp());
		});
	}

	@Test
	public void testSpellTransferLife() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_transfer_life", ally);
			assertTrue(ally.isDestroyed());
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testMinionBlightHarvester() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "token_captive_overlord");
			playMinionCard(context, player, "token_underling_overlord");
			playMinionCard(context, player, "minion_blight_harvester");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellBlightHosts() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_blight_hosts");
			assertEquals(3, player.getMinions().size());
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testSpellWraithAncientCurse() {
		runGym((context, player, opponent) -> {
			player.getHero().setAttribute(Attribute.ARMOR, 3);
			int pHp = player.getHero().getHp();
			int oHp = opponent.getHero().getHp();
			playCard(context, player, "spell_wraith_ancient_curse");
			assertEquals(oHp - 6, opponent.getHero().getHp());
			assertEquals(pHp - 6, player.getHero().getHp());
			assertEquals(9, player.getHero().getArmor());
		});
	}

	// ==================== DARKBLUE ====================

	@Test
	public void testMinionJekkTrinity() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_jekk_trinity");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testMinionSpiritMother() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_jekk_trinity"); // summons 3 spirits
			Card mother = receiveCard(context, player, "minion_spirit_mother");
			assertEquals(3, costOf(context, player, mother));
			playMinionCardWithBattlecry(context, player, "minion_spirit_mother", ally);
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
		});
	}

	@Test
	public void testSecretTransmuteLife() {
		runGym((context, player, opponent) -> {
			Minion a1 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion a2 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "secret_transmute_life");
			context.endTurn();
			playCard(context, opponent, "spell_high_note", a1);
			assertEquals(3, a2.getAttack());
			assertEquals(3, a2.getHp());
		});
	}

	@Test
	public void testSecretTraverseFate() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "secret_traverse_fate");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", ally);
			assertEquals(1, player.getMinions().size());
			assertEquals(1, player.getMinions().get(0).getHp());
		});
	}

	@Test
	public void testSpellGrimVortex() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Card vortex = receiveCard(context, player, "spell_grim_vortex");
			assertEquals(3, costOf(context, player, vortex));
			playCard(context, player, vortex);
			assertTrue(enemy.isDestroyed());
			assertEquals(3, ally.getHp());
		});
	}

	@Test
	public void testMinionLucidSeer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_neutral_test");
			Minion seer = playMinionCard(context, player, "minion_lucid_seer");
			assertEquals(1, player.getMinions().size());
			assertEquals(1, player.getHand().size());
			assertEquals(3, seer.getAttack());
			assertEquals(4, seer.getHp());
		});
	}

	@Test
	public void testSpellReflect() {
		runGym((context, player, opponent) -> {
			int hpBefore = opponent.getHero().getHp();
			playCard(context, player, "spell_reflect");
			assertEquals(1, player.getMinions().size()); // Soulbind token
			playMinionCardWithBattlecry(context, player, "minion_test_deal_1", opponent.getHero());
			assertEquals(hpBefore - 2, opponent.getHero().getHp());
		});
	}

	// ==================== DARKGREEN ====================

	@Test
	public void testMinionAncientAbomination() {
		runGym((context, player, opponent) -> {
			Minion abom = playMinionCard(context, player, "minion_ancient_abomination");
			playCard(context, player, "spell_high_note", abom);
			assertEquals(2, player.getMinions().size());
			assertEquals("token_tauntentacle", player.getMinions().get(1).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionCorruptedCroaker() {
		runGym((context, player, opponent) -> {
			int pHp = player.getHero().getHp();
			int oHp = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_corrupted_croaker");
			context.endTurn();
			assertEquals(pHp - 2, player.getHero().getHp());
			assertEquals(oHp - 2, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionCultPromoter() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_cult_promoter");
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast");
			destroy(context, ally);
			assertEquals(1, player.getDeck().size());
			assertEquals("minion_test_3_2_beast", player.getDeck().get(0).getCardId());
		});
	}

	@Test
	public void testMinionDecayingColossus() {
		runGym((context, player, opponent) -> {
			Minion colossus = playMinionCard(context, player, "minion_decaying_colossus");
			context.endTurn();
			assertEquals(6, colossus.getHp());
		});
	}

	@Test
	public void testMinionEaterOfSacrifices() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, ally);
			Minion eater = playMinionCard(context, player, "minion_eater_of_sacrifices");
			assertEquals(5, eater.getAttack());
		});
	}

	@Test
	public void testMinionEldritchExperimenter() {
		runGym((context, player, opponent) -> {
			Minion exp = playMinionCard(context, player, "minion_eldritch_experimenter");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_high_note", exp);
			assertEquals(3, ally.getAttack());
			assertEquals(3, ally.getHp());
		});
	}

	@Test
	public void testMinionMutatingMonstrosity() {
		runGym((context, player, opponent) -> {
			Minion monstro = playMinionCard(context, player, "minion_mutating_monstrosity");
			playCard(context, player, "spell_high_note", monstro);
			assertEquals(2, player.getMinions().size());
			assertEquals(1, player.getMinions().get(1).getHp());
		});
	}

	@Test
	public void testMinionPelagicParasite() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_neutral_test_6_7");
			Minion parasite = playMinionCard(context, player, "minion_pelagic_parasite");
			destroy(context, parasite);
			assertEquals("token_tauntentacle", opponent.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionSkytearer() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Minion sky = playMinionCard(context, player, "minion_skytearer");
			destroy(context, sky);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_test_4_5", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionSpawnOfGobogeg() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			Minion spawn = playMinionCard(context, player, "minion_spawn_of_gobogeg");
			playCard(context, player, "spell_high_note", spawn);
			assertEquals(oHp - 3, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionToweringHorror() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, ally);
			Minion horror = playMinionCard(context, player, "minion_towering_horror");
			assertEquals(4, horror.getHp());
		});
	}

	@Test
	public void testMinionTreevil() {
		runGym((context, player, opponent) -> {
			Minion treevil = playMinionCard(context, player, "minion_treevil");
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			destroy(context, ally); // Treevil counter = 1
			destroy(context, treevil); // deals 1 damage to all minions
			assertEquals(4, enemy.getHp());
		});
	}

	@Test
	public void testMinionTwistedFlesh() {
		runGym((context, player, opponent) -> {
			Minion flesh = playMinionCard(context, player, "minion_twisted_flesh"); // 7/4
			playCard(context, player, "spell_high_note", flesh); // deals 2, then heals to full
			assertEquals(4, flesh.getHp());
		});
	}

	@Test
	public void testSpellAccumulatedRage() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Card rage = receiveCard(context, player, "spell_accumulated_rage");
			context.endTurn();
			context.endTurn();
			playCard(context, player, rage, enemy); // 1 + 1 = 2 damage
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testSpellAncientReadings() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_1");
			shuffleToDeck(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_ancient_readings");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_neutral_test_6_7", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellArduousInvocation() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_arduous_invocation");
			assertEquals(1, player.getMinions().size());
			assertEquals(5, player.getMinions().get(0).getSourceCard().getBaseManaCost());
		});
	}

	@Test
	public void testSpellCelestialImprisonment() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_celestial_imprisonment");
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getDeck().size());
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testSpellCurseOfDesolation() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test");
			playMinionCard(context, opponent, "minion_neutral_test");
			playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_curse_of_desolation", e1);
			assertTrue(e1.isDestroyed());
		});
	}

	@Test
	public void testSpellDeepExploration() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_deep_exploration", ally);
			assertEquals(0, player.getMinions().size());
			assertEquals(1, player.getDeck().size());
			shuffleToDeck(context, player, "minion_test_4_5");
			shuffleToDeck(context, player, "minion_test_4_5");
			shuffleToDeck(context, player, "minion_test_4_5");
			while (player.getHand().isEmpty() || !player.getHand().stream().anyMatch(c -> c.getCardId().equals("minion_neutral_test"))) {
				context.getLogic().drawCard(player.getId(), null);
			}
			assertTrue(player.getHand().size() >= 4);
		});
	}

	@Test
	public void testSpellForbiddenKnowledge() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			Card card = receiveCard(context, player, "spell_forbidden_knowledge");
			player.setMana(5);
			context.getLogic().performGameAction(player.getId(), card.play());
			assertEquals(0, player.getMana());
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellGoreball() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			destroy(context, ally);
			playCard(context, player, "spell_goreball", enemy);
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testSpellHorrendousExperiment() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_horrendous_experiment", ally);
			assertEquals(2, player.getHand().size());
			Minion copy = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(5, copy.getAttack());
			assertEquals(5, copy.getHp());
		});
	}

	@Test
	public void testSpellHorribleHallucination() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_horrible_hallucination");
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_test_4_5", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellRaiseDead() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				Minion m = playMinionCard(context, player, "minion_neutral_test");
				destroy(context, m);
			}
			Card raise = receiveCard(context, player, "spell_raise_dead");
			assertEquals(3, costOf(context, player, raise));
			playCard(context, player, raise);
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellSacrifire() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_sacrifire");
			assertTrue(ally.isDestroyed());
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellSixFeetUnder() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_six_feet_under", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(3, player.getDeck().size());
		});
	}

	@Test
	public void testSpellSpawnOfEvil() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_spawn_of_evil", enemy);
			assertEquals(1, player.getMinions().size());
			assertEquals(2, opponent.getMinions().size());
		});
	}

	@Test
	public void testSpellSunkenRitual() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_sunken_ritual", ally);
			assertEquals(6, ally.getAttack());
			assertEquals(6, ally.getHp());
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
			assertTrue(ally.hasAttribute(Attribute.CANNOT_ATTACK_HEROES));
		});
	}

	@Test
	public void testSpellVileIntent() {
		runGym((context, player, opponent) -> {
			Minion dead = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, dead);
			Card inHand = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_vile_intent");
			Minion played = playMinionCard(context, player, inHand);
			assertEquals(4, played.getAttack());
			assertEquals(4, played.getHp());
		});
	}

	@Test
	public void testSpellVoidCurse() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_void_curse", enemy);
			context.endTurn();
			attack(context, opponent, enemy, player.getHero());
			assertTrue(enemy.isDestroyed());
		});
	}

	// ==================== DARKMAGENTA ====================

	@Test
	public void testMinionBogoveySupplicant() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bogovey_supplicant");
			assertTrue(player.getHand().size() == 1 || player.getMinions().size() == 2);
		});
	}

	@Test
	public void testSpellCollectiveCalling() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2_beast"); // 2-cost
			receiveCard(context, player, "minion_neutral_test"); // 1 card in hand (+1 offset = 2)
			playCard(context, player, "spell_collective_calling");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellConjureColossus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_conjure_colossus");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			assertEquals(7, ally.getAttack());
			assertEquals(7, ally.getHp());
		});
	}

	@Test
	public void testSpellRiteOfMoromah() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_rite_of_moromah");
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellShakeTheGround() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_shake_the_ground");
			assertTrue(enemy.isDestroyed());
		});
	}

	// ==================== LIGHTBROWN ====================

	@Test
	public void testMinionAtumjunTrapfinder() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_atumjun_trapfinder");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionEvergreenGrower() {
		runGym((context, player, opponent) -> {
			Minion elem = playMinionCard(context, player, "minion_magma_hound"); // 7/6 Elemental
			playMinionCardWithBattlecry(context, player, "minion_evergreen_grower", elem);
			assertEquals(8, elem.getAttack());
			assertEquals(8, elem.getHp());
		});
	}

	@Test
	public void testMinionFossilExcavator() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_fossil_excavator");
			context.endTurn();
			assertEquals(1, player.getDeck().size());
		});
	}

	@Test
	public void testMinionNativeGuide() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_test_3_2");
			Minion guide = playMinionCard(context, player, "minion_native_guide");
			assertEquals(6, guide.getAttack());
			assertEquals(7, guide.getHp());
		});
	}

	@Test
	public void testMinionOnsiteEnforcer() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_onsite_enforcer");
			assertEquals(0, player.getHand().size());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testMinionPaleontologist() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_paleontologist");
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testSpellCarbonCopy() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_carbon_copy", ally);
			assertEquals(5, ally.getHp());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testWeaponMystick() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_mystick");
			assertEquals(2, player.getHero().getAttack()); // +2 aura on hero
			assertEquals(2, player.getWeaponZone().get(0).getAttack()); // +2 weapon attack
		});
	}

	@Test
	public void testWeaponSentryOrb() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			playCard(context, player, "weapon_sentry_orb");
			context.endTurn();
			assertEquals(oHp - 5, opponent.getHero().getHp());
		});
	}

	@Test
	public void testWeaponTimesnipper() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_timesnipper", enemy);
			assertTrue(enemy.isDestroyed());
			destroy(context, player.getWeaponZone().get(0));
			assertEquals(1, opponent.getMinions().size());
		});
	}

	@Test
	public void testWeaponTranslifeMirror() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_translife_mirror");
			context.endTurn();
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testMinionCobbleElemental() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_magma_hound");
			shuffleToDeck(context, player, "minion_magma_hound");
			Minion cobble = playMinionCard(context, player, "minion_cobble_elemental");
			destroy(context, cobble);
			assertEquals(2, player.getHand().size());
		});
	}

	// ==================== MAGENTA ====================

	@Test
	public void testMinionGlitzchaser() {
		runGym((context, player, opponent) -> {
			Minion glitz = playMinionCard(context, player, "minion_glitzchaser");
			playCard(context, player, "spell_gold_coin");
			assertEquals(2, glitz.getAttack());
		});
	}

	@Test
	public void testMinionGuardSprite() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_glimmer_dancer"); // Fae
			playMinionCard(context, player, "minion_test_3_2_beast"); // Beast
			playMinionCard(context, player, "token_44dragon"); // Dragon
			playMinionCard(context, player, "minion_guard_sprite");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testMinionGuardWhelp() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playMinionCardWithBattlecry(context, player, "minion_guard_whelp", ally);
			assertTrue(ally.hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testMinionMinorityMarketer() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2_beast");
			shuffleToDeck(context, player, "minion_glimmer_dancer");
			shuffleToDeck(context, player, "token_44dragon");
			playMinionCard(context, player, "minion_minority_marketer");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testMinionPackMule() {
		runGym((context, player, opponent) -> {
			Minion mule = playMinionCard(context, player, "minion_pack_mule");
			attack(context, player, mule, opponent.getHero());
			assertEquals(1, player.getHand().size());
			assertEquals("spell_gold_coin", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionSkittishPixie() {
		runGym((context, player, opponent) -> {
			Minion pixie = playMinionCard(context, player, "minion_skittish_pixie");
			assertEquals(1, player.getHand().size());
			destroy(context, pixie);
			assertEquals(1, opponent.getHand().size());
		});
	}

	@Test
	public void testSpellCoercion() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_coercion", enemy);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getHand().size());
		});
	}

	@Test
	public void testSpellFranticSearch() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_1"); // 1-cost
			shuffleToDeck(context, player, "minion_neutral_test"); // 2-cost (even, discarded)
			shuffleToDeck(context, player, "minion_trigger_happy_rebel"); // 3-cost
			playCard(context, player, "spell_frantic_search");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellNewMuscle() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_new_muscle");
			assertEquals(1, player.getMinions().size());
			assertTrue(player.getMinions().get(0).getSourceCard().getBaseManaCost() >= 7);
		}, HeroClass.MAGENTA, HeroClass.MAGENTA);
	}

	@Test
	public void testSpellNicheInTheMarket() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test_1");
			receiveCard(context, player, "minion_trigger_happy_rebel");
			playCard(context, player, "spell_niche_in_the_market");
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSpellNoDeal() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5"); // 4-cost
			playCard(context, player, "spell_no_deal", enemy);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getDeck().size());
			assertEquals(6, costOf(context, opponent, opponent.getDeck().get(0)));
		});
	}

	@Test
	public void testSpellPyramidScheme() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_pyramid_scheme");
			assertEquals(1, player.getMinions().size());
			assertEquals(1, player.getHand().size());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testSpellSwitcheroo() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_switcheroo", ally);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
			assertEquals("minion_neutral_test_1", opponent.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	// ==================== NAVY ====================

	@Test
	public void testMinionAutomatrainer() {
		runGym((context, player, opponent) -> {
			Minion trainer = playMinionCard(context, player, "minion_automatrainer");
			assertEquals(0, opponent.getMinions().size());
			assertEquals(4, trainer.getHp());
		});
	}

	@Test
	public void testMinionBogMutant() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Minion mutant = playMinionCard(context, player, "minion_bog_mutant");
			destroy(context, mutant);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_test_4_5", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionBootyBayPorter() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_booty_bay_porter");
			Minion spirit = playMinionCard(context, player, "token_spirit");
			destroy(context, spirit);
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testMinionCorruptedDefender() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			playMinionCard(context, player, "minion_corrupted_defender");
			assertTrue(enemy.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionDeliveryDealer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_delivery_dealer");
			context.endTurn();
			assertEquals(2, player.getMinions().size());
			assertEquals(1, opponent.getMinions().size());
		});
	}

	@Test
	public void testMinionDoomwalker() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion walker = playMinionCard(context, player, "minion_doomwalker");
			attack(context, player, walker, enemy);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_doomwalker", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionHauntedBerserker() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion berserker = playMinionCard(context, player, "minion_haunted_berserker");
			attack(context, player, berserker, e1);
			assertEquals(3, e1.getHp()); // 1 attack + 1 splash
			assertEquals(4, e2.getHp());
		});
	}

	@Test
	public void testMinionHuntingTerradon() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_hunting_terradon");
			assertEquals(2, player.getMinions().size());
			assertEquals(1, opponent.getMinions().size());
		});
	}

	@Test
	public void testMinionOldRatmother() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_old_ratmother");
			assertEquals(3, player.getMinions().size());
			assertEquals(2, opponent.getMinions().size());
		});
	}

	@Test
	public void testMinionSavageWorgen() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion e2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_savage_worgen");
			context.endTurn();
			assertTrue(e1.isDestroyed());
			assertTrue(e2.isDestroyed());
		});
	}

	@Test
	public void testMinionScurvyAssassin() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_stowaway_scoundrel"); // Pirate
			playMinionCardWithBattlecry(context, player, "minion_scurvy_assassin", enemy); // also a Pirate = 2 Pirates
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testMinionSpiritOfTheBear() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_spirit_of_the_bear");
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			context.endTurn();
			attack(context, player, ally, opponent.getHero());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionStonecoldSergeant() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_stonecold_sergeant");
			playCard(context, player, "spell_warfare_project");
			Minion knight = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(4, knight.getAttack());
			assertEquals(4, knight.getHp());
		});
	}

	@Test
	public void testMinionStormwindCommander() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_stormwind_commander");
			Minion knight = playMinionCard(context, player, "token_knight");
			assertTrue(knight.hasAttribute(Attribute.AURA_RUSH));
		});
	}

	@Test
	public void testMinionTitanicTerror() {
		runGym((context, player, opponent) -> {
			Minion terror = playMinionCard(context, player, "minion_titanic_terror");
			playCard(context, player, "spell_lunstone");
			assertEquals(12, terror.getAttack());
		});
	}

	@Test
	public void testMinionTriswordSwinger() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion e2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion e3 = playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCardWithBattlecry(context, player, "minion_trisword_swinger", e2);
			assertTrue(e1.isDestroyed());
			assertTrue(e2.isDestroyed());
			assertTrue(e3.isDestroyed());
		});
	}

	@Test
	public void testMinionUnstableArtifact() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion artifact = playMinionCard(context, player, "minion_unstable_artifact");
			destroy(context, artifact);
			assertEquals(1, enemy.getHp());
		});
	}

	@Test
	public void testMinionZandalariZapper() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_zandalari_zapper");
			Card deal6 = receiveCard(context, player, "spell_test_deal_6"); // 4-cost -> 2
			Card note = receiveCard(context, player, "spell_high_note"); // 1-cost -> min 1
			assertEquals(2, costOf(context, player, deal6));
			assertEquals(1, costOf(context, player, note));
		});
	}

	@Test
	public void testMinonLotusKnight() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			Minion lotus = playMinionCard(context, player, "minon_lotus_knight");
			assertTrue(lotus.hasAttribute(Attribute.CHARGE));
			assertTrue(lotus.hasAttribute(Attribute.POISONOUS));
			attack(context, player, lotus, enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellAvengingForces() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_avenging_forces");
			assertEquals(5, player.getMinions().size());
		});
	}

	@Test
	public void testSpellBattlefield() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_battlefield");
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
			assertTrue(enemy.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testSpellCursedGrips() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_cursed_grips", ally);
			context.endTurn();
			context.endTurn();
			attack(context, player, ally, e1);
			assertEquals(2, e2.getHp());
		});
	}

	@Test
	public void testSpellDeepseaDuel() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_6_7");
			shuffleToDeck(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_deepsea_duel");
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSpellDuplimancy() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_duplimancy", ally);
			assertEquals(2, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellEnchantedMirror() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_enchanted_mirror", ally);
			assertEquals(2, player.getMinions().size());
			assertEquals(4, player.getMinions().get(1).getAttack());
			assertEquals(4, player.getMinions().get(1).getHp());
		});
	}

	@Test
	public void testSpellExtract() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test"); // 2 attack
			Minion target = playMinionCard(context, opponent, "minion_test_4_5"); // 4 attack
			playCard(context, player, "spell_extract", target);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellForcesUnite() {
		runGym((context, player, opponent) -> {
			Card inHand = receiveCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_forces_unite");
			Minion summoned = playMinionCard(context, player, inHand);
			assertEquals(5, summoned.getAttack());
			assertEquals(5, summoned.getHp());
		});
	}

	@Test
	public void testSpellSpellforce() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_spellforce", ally);
			assertTrue(ally.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		});
	}

	@Test
	public void testSpellWarfareProject() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_warfare_project");
			assertEquals(2, player.getHand().size());
			assertEquals(2, opponent.getHand().size());
		});
	}

	@Test
	public void testSpellWarfareTraining() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			receiveCard(context, player, "token_knight");
			receiveCard(context, player, "token_knight");
			playCard(context, player, "spell_warfare_training");
			assertEquals(4, player.getHand().size());
		});
	}

	@Test
	public void testSpellWeakPoint() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_weak_point", enemy);
			playCard(context, player, "spell_high_note", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellWhelpEncounter() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playCard(context, player, "spell_whelp_encounter");
			assertEquals(2, opponent.getMinions().size());
			assertEquals(4, player.getHand().size());
		});
	}

	@Test
	public void testMinionLonelyWhelpling() {
		runGym((context, player, opponent) -> {
			Card c1 = receiveCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_lonely_whelpling");
			assertEquals(0, costOf(context, player, c1));
		});
	}

	@Test
	public void testMinionSandman() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_sandman");
			assertEquals(3, player.getMinions().size());
		});
	}

	// ==================== NEONGREEN ====================

	@Test
	public void testMinionNanomiteCloud() {
		runGym((context, player, opponent) -> {
			Minion cloud = playMinionCard(context, player, "minion_nanomite_cloud"); // 1/2
			Minion ping = playMinionCardWithBattlecry(context, player, "minion_test_deal_1", cloud);
			assertEquals(4, player.getMinions().size()); // cloud + 2 copies + ping
		});
	}

	@Test
	public void testMinionNebulousEntity() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_nebulous_entity", enemy);
			assertEquals(2, enemy.getAttack());
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testMinionRancorousGrunt() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			Minion grunt = playMinionCard(context, player, "minion_rancorous_grunt"); // 1/4 Dash, Wither(2), pre-attack 1 dmg
			attack(context, player, grunt, enemy);
			assertEquals(0, enemy.getAttack()); // Wither (2) reduced attack twice (pre-attack + physical attack)
			assertEquals(3, enemy.getHp()); // 1 trigger damage + 1 physical attack
		});
	}

	@Test
	public void testPermanentFormlessRancour() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playMinionCard(context, player, "permanent_formless_rancour");
			assertEquals(3, ally.getAttack());
			playMinionCard(context, player, "permanent_formless_rancour");
			assertEquals(1, player.getMinions().size()); // both permanents destroyed when adjacent
		});
	}

	@Test
	public void testPermanentFormlessWomb() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playMinionCard(context, player, "minion_test_4_5");
				playMinionCard(context, opponent, "minion_test_4_5");
			}
			playMinionCard(context, player, "permanent_formless_womb");
			context.endTurn(); // deals 1 damage to 10 minions -> summons 8/8 Titan
			assertTrue(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("token_titan_8_8")));
		});
	}

	@Test
	public void testPermanentFormlessWormhole() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "permanent_formless_wormhole");
			context.endTurn();
			assertEquals(2, player.getMinions().size());
			assertEquals("token_xenodrone_02", player.getMinions().get(1).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellAntiAnomalyScales() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_anti-anomaly_scales", ally);
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
			assertTrue(ally.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		});
	}

	@Test
	public void testSpellAsteroidImpact() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5"); // 5 HP -> 5 excess
			playCard(context, player, "spell_asteroid_impact", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(oHp - 5, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellFeedingFrenzy() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion sac = playMinionCard(context, player, "minion_test_3_2_beast"); // 3/2
			Minion survivor = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_feeding_frenzy", sac);
			assertTrue(sac.isDestroyed());
			assertEquals(5, survivor.getAttack());
			assertEquals(4, survivor.getHp());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellLifeformAssimilation() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			playCard(context, player, "spell_lifeform_assimilation");
			assertEquals(1, player.getHand().size());
			assertEquals(1, costOf(context, player, player.getHeroPowerZone().get(0)));
		});
	}

	@Test
	public void testSpellTemporalAcceleration() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				receiveCard(context, player, "minion_neutral_test_1");
				shuffleToDeck(context, player, "minion_test_4_5");
			}
			playCard(context, player, "spell_temporal_acceleration");
			assertEquals(3, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellAbductOtherworlder() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_abduct_otherworlder", enemy);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellScourTheLand() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "token_xenodrone_02");
			playCard(context, player, "spell_scour_the_land");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellTranshumanism() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			receiveCard(context, player, "spell_high_note"); // 1-cost spell
			receiveCard(context, player, "spell_practice"); // 1-cost spell
			playCard(context, player, "spell_transhumanism", ally);
			assertEquals(0, player.getHand().size());
			assertEquals(6, ally.getAttack());
			assertEquals(6, ally.getHp());
		});
	}

	// ==================== NEUTRAL ====================

	@Test
	public void testMinionAbandonedGeneticist() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_abandoned_geneticist");
			assertEquals(1, player.getHand().size());
			Card c = player.getHand().get(0);
			assertEquals(1, costOf(context, player, c));
			assertEquals(1, c.getAttributeValue(Attribute.ATTACK));
			assertEquals(1, c.getAttributeValue(Attribute.HP));
		});
	}

	@Test
	public void testMinionAeonChanneler() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_vitium_surger");
			playCard(context, player, "spell_high_note", player.getMinions().get(0)); // receives Artifact
			playMinionCardWithBattlecry(context, player, "minion_aeon_channeler", opponent.getHero());
			assertEquals(oHp - 3, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionDerangedGreengill() {
		runGym((context, player, opponent) -> {
			Minion fae = playMinionCard(context, opponent, "minion_glimmer_dancer");
			playMinionCardWithBattlecry(context, player, "minion_deranged_greengill", fae);
			assertEquals(2, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
		});
	}

	@Test
	public void testMinionFlashbulbSpider() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_flashbulb_spider");
			assertTrue(enemy.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
		});
	}

	@Test
	public void testMinionIneptInvestigator() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_inept_investigator");
			assertEquals(1, player.getHand().size());
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.ARTIFACT));
		});
	}

	@Test
	public void testMinionLittlePurrsuer() {
		runGym((context, player, opponent) -> {
			Minion purr = playMinionCard(context, player, "minion_little_purrsuer");
			assertTrue(purr.hasAttribute(Attribute.RUSH));
			assertEquals(2, purr.getHp());
		});
	}

	@Test
	public void testMinionPurifyingOoze() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "weapon_mystick"); // gives +2 Attack aura
			context.endTurn();
			assertEquals(2, opponent.getHero().getAttack());
			playMinionCard(context, player, "minion_purifying_ooze");
			assertEquals(0, opponent.getHero().getAttack());
		});
	}

	@Test
	public void testMinionPurityKeeper() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_warclub_ruffian");
			assertTrue(enemy.hasAttribute(Attribute.TAUNT));
			assertTrue(enemy.hasAttribute(Attribute.DIVINE_SHIELD));
			playMinionCard(context, player, "minion_purity_keeper");
			assertFalse(enemy.hasAttribute(Attribute.TAUNT));
			assertFalse(enemy.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testMinionSmugSmuggler() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "token_river_crowdcolisk"); // Beast
			Minion beast = playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCardWithBattlecry(context, player, "minion_smug_smuggler", beast);
			assertEquals(1, player.getHand().size());
			assertEquals("token_river_crowdcolisk", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionStreetwiseSpecialist() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			shuffleToDeck(context, opponent, "minion_neutral_test");
			playMinionCard(context, player, "minion_streetwise_specialist");
			context.endTurn(); // opponent draws at start of turn
			assertEquals(oHp - 2, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionTriggerHappySurvivor() {
		runGym((context, player, opponent) -> {
			int totalHpBefore = player.getHero().getHp() + opponent.getHero().getHp();
			playMinionCard(context, player, "minion_trigger_happy_survivor");
			context.endTurn();
			assertEquals(totalHpBefore - 3, player.getHero().getHp() + opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionUndeadTrickster() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_undead_trickster");
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testMinionVitiumSurger() {
		runGym((context, player, opponent) -> {
			Minion surger = playMinionCard(context, player, "minion_vitium_surger");
			playCard(context, player, "spell_high_note", surger);
			assertEquals(1, player.getHand().size());
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.ARTIFACT));
		});
	}

	@Test
	public void testMinionBeholderOfTheBeginning() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			receiveCard(context, player, "minion_trigger_happy_rebel"); // 3-cost
			playMinionCardWithBattlecry(context, player, "minion_beholder_of_the_beginning", enemy);
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testMinionChronocaller() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_chronocaller");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testMinionDefenderOfTomorrow() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				receiveCard(context, player, "minion_neutral_test_6_7"); // 5 * 6 = 30
			}
			Minion def = playMinionCard(context, player, "minion_defender_of_tomorrow");
			assertEquals(8, def.getHp());
		});
	}

	@Test
	public void testMinionEmissaryOfTheEnd() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test_6_7"); // 6-cost
			Minion emissary = playMinionCard(context, player, "minion_emissary_of_the_end");
			assertEquals(6, emissary.getAttack());
		});
	}

	@Test
	public void testMinionParallelProtector() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_high_note");
			Minion prot = playMinionCard(context, player, "minion_parallel_protector");
			assertEquals(4, prot.getHp());
		});
	}

	@Test
	public void testMinionTetradimensionalist() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_holy_bulwark"); // 1-cost Taunt
			assertTrue(enemy.hasAttribute(Attribute.TAUNT));
			playMinionCard(context, player, "minion_tetradimensionalist");
			assertFalse(enemy.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionTwilightAnnihilator() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_twilight_annihilator");
			assertTrue(e1.isDestroyed());
			assertFalse(e2.isDestroyed());
		});
	}

	@Test
	public void testMinionBlueNestwatcher() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_blue_nestwatcher");
			assertEquals(4, player.getHand().size());
		});
	}

	@Test
	public void testMinionBugbiter() {
		runGym((context, player, opponent) -> {
			Minion weak = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion strong = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_bugbiter");
			assertTrue(weak.isDestroyed());
			assertFalse(strong.isDestroyed());
		});
	}

	@Test
	public void testMinionCaptainOfTheGuard() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_captain_of_the_guard");
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionChainwreckerCorsair() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_chainwrecker_corsair");
			playCard(context, player, "weapon_test_3_2");
			attack(context, player, player.getHero(), e1);
			assertEquals(2, e1.getHp());
			assertEquals(2, e2.getHp());
		});
	}

	@Test
	public void testMinionCorruptSentinel() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			Minion sentinel = playMinionCard(context, player, "minion_corrupt_sentinel");
			context.endTurn();
			context.endTurn();
			attack(context, player, sentinel, opponent.getHero());
			assertEquals(oHp - 4, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionFestivityGolem() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_test_3_2_beast"); // Beast
			playMinionCard(context, player, "minion_festivity_golem"); // Elemental -> 2 unique tribes
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testMinionGargoyleWarden() {
		runGym((context, player, opponent) -> {
			Minion warden = playMinionCard(context, player, "minion_gargoyle_warden");
			assertTrue(warden.hasAttribute(Attribute.CANNOT_ATTACK));
			assertEquals(5, warden.getAttack());
			assertEquals(10, warden.getHp());
		});
	}

	@Test
	public void testMinionGoldPicaroon() {
		runGym((context, player, opponent) -> {
			Minion picaroon = playMinionCard(context, player, "minion_gold_picaroon");
			attack(context, player, picaroon, opponent.getHero());
			assertEquals(1, player.getHand().size());
			assertEquals("spell_lunstone", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionHoldHunter() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion hunter = playMinionCard(context, player, "minion_hold_hunter");
			attack(context, player, hunter, enemy);
			assertTrue(hunter.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testMinionHolyBulwark() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			Minion bulwark = playMinionCard(context, player, "minion_holy_bulwark");
			destroy(context, bulwark);
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionHoodedRitualist() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_hooded_ritualist");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testMinionHostileSnapdragon() {
		runGym((context, player, opponent) -> {
			int pHp = player.getHero().getHp();
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_hostile_snapdragon");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(pHp - 4, player.getHero().getHp());
		});
	}

	@Test
	public void testMinionLunstoneRumbler() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(3);
			playCard(context, player, "weapon_test_3_2");
			playMinionCard(context, player, "minion_lunstone_rumbler");
			assertTrue(player.getWeaponZone().isEmpty());
			assertEquals(4, player.getMaxMana());
		});
	}

	@Test
	public void testMinionMagedrainer() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion drainer = playMinionCardWithBattlecry(context, player, "minion_magedrainer", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(2, drainer.getAttributeValue(Attribute.SPELL_DAMAGE));
		});
	}

	@Test
	public void testMinionMightyMufunga() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playMinionCard(context, player, "minion_mighty_mufunga");
			assertEquals(4, ally.getAttack());
		});
	}

	@Test
	public void testMinionMoltenCauterizer() {
		runGym((context, player, opponent) -> {
			int pHp = player.getHero().getHp();
			playMinionCard(context, player, "minion_molten_cauterizer");
			assertEquals(pHp - 8, player.getHero().getHp());
			assertEquals(8, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionNezumiMaster() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_terrified_tanooki"); // 1/3 Hidden
			playMinionCardWithBattlecry(context, player, "minion_nezumi_master", ally);
			assertEquals(3, ally.getAttack());
			assertTrue(ally.hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testMinionSavageSprite() {
		runGym((context, player, opponent) -> {
			Minion sprite = playMinionCard(context, player, "minion_savage_sprite");
			playMinionCard(context, player, "minion_glimmer_dancer"); // Fae
			assertEquals(3, sprite.getAttack());
		});
	}

	@Test
	public void testMinionSpellquaker() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_spellquaker");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionSwarmedCaptive() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_swarmed_captive");
			playMinionCard(context, player, "minion_neutral_test_1");
			assertEquals(2, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionTerrifiedTanooki() {
		runGym((context, player, opponent) -> {
			Minion tanooki = playMinionCard(context, player, "minion_terrified_tanooki");
			attack(context, player, tanooki, opponent.getHero());
			assertEquals(2, tanooki.getAttack());
		});
	}

	@Test
	public void testMinionVolcanicElemental() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion volc = playMinionCard(context, player, "minion_volcanic_elemental");
			destroy(context, volc);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionWarclubRuffian() {
		runGym((context, player, opponent) -> {
			Minion ruffian = playMinionCard(context, player, "minion_warclub_ruffian");
			assertTrue(ruffian.hasAttribute(Attribute.RUSH));
			assertTrue(ruffian.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(ruffian.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionWhiptailGatherer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_whiptail_gatherer");
			context.endTurn();
			assertEquals(1, player.getHand().size());
			assertEquals("token_berry", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionAcolyteOfPerotharn() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_beloved_benji"); // Legendary
			playMinionCard(context, player, "minion_acolyte_of_peroth'arn");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_beloved_benji", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionAmbushingRose() {
		runGym((context, player, opponent) -> {
			Minion rose = playMinionCard(context, player, "minion_ambushing_rose");
			destroy(context, rose);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionBookkeeper() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(6);
			player.setMana(0);
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_high_note");
			Card bk = receiveCard(context, player, "minion_bookkeeper");
			player.setMana(6);
			context.getLogic().performGameAction(player.getId(), bk.play());
			assertEquals(2, player.getMana());
		});
	}

	@Test
	public void testMinionBrambleboundWarrior() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bramblebound_warrior");
			playCard(context, player, "weapon_test_3_2");
			playCard(context, player, "spell_practice", player.getHero());
			attack(context, player, player.getHero(), opponent.getHero());
			assertFalse(player.getHero().canAttackThisTurn(context));
			destroy(context, player.getWeaponZone().get(0));
			assertTrue(player.getHero().canAttackThisTurn(context));
		});
	}

	@Test
	public void testMinionDeepwoodBarghest() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_deepwood_barghest");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testMinionDragonPod() {
		runGym((context, player, opponent) -> {
			Minion pod = playMinionCard(context, player, "minion_dragon_pod");
			destroy(context, pod);
			assertEquals(1, player.getMinions().size());
			assertEquals("token_flytrap_drake", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionPetalKnight() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion knight = playMinionCard(context, player, "minion_petal_knight");
			attack(context, player, knight, enemy);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionWaveringDiabloist() {
		runGym((context, player, opponent) -> {
			Minion weak = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3/2 dies
			Minion strong = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5 takes 2, heals 2 -> 5
			playMinionCard(context, player, "minion_wavering_diabloist");
			assertTrue(weak.isDestroyed());
			assertEquals(5, strong.getHp());
		});
	}

	@Test
	public void testMinionWildPyrodancer() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_wild_pyrodancer");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, enemy.getHp());
		});
	}

	// ==================== OFFWHITE ====================

	@Test
	public void testMinionElderDreamguide() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_elder_dreamguide"); // Beast!
			Card spirit = receiveCard(context, player, "minion_haunting_harpist"); // 3-cost Spirit
			assertEquals(1, costOf(context, player, spirit));
		});
	}

	@Test
	public void testMinionMindMelder() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCardWithBattlecry(context, player, "minion_mind_melder", enemy);
			assertEquals(2, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
		});
	}

	@Test
	public void testMinionPortalKeeper() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test"); // consumed from hand
			Minion keeper = playMinionCard(context, player, "minion_portal_keeper");
			destroy(context, keeper);
			assertEquals(0, player.getHand().size());
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testMinionShadowheart() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_shadowheart");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion sac = playMinionCard(context, player, "minion_neutral_test_1");
			destroy(context, sac);
			assertEquals(4, enemy.getHp());
		});
	}

	@Test
	public void testSpellBanishToTheDream() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_deathrattle");
			playCard(context, player, "spell_banish_to_the_dream");
			assertTrue(enemy.isDestroyed());
			assertEquals(0, opponent.getHand().size()); // silenced before destroy
		});
	}

	@Test
	public void testSpellFadingMemories() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_fading_memories");
			assertEquals(7, player.getMinions().size());
		});
	}

	@Test
	public void testSpellPartTheVeil() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_part_the_veil");
			assertEquals(0, player.getHand().size());
			assertTrue(player.getMinions().size() >= 1);
		});
	}

	@Test
	public void testSpellPullFromBeyond() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Card pull = receiveCard(context, player, "spell_pull_from_beyond");
			player.setMana(4);
			context.getLogic().performGameAction(player.getId(), pull.play());
			assertEquals(0, player.getMana());
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellSoulshackle() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_soulshackle", enemy);
			assertEquals(1, player.getMinions().size());
			assertTrue(enemy.hasAttribute(Attribute.TAUNT));
			context.endTurn();
			context.endTurn();
			assertEquals(0, player.getMinions().size());
			assertEquals(1, opponent.getMinions().size());
		});
	}

	@Test
	public void testSpellTemporalFlicker() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_temporal_flicker");
			assertTrue(ally.hasAttribute(Attribute.CHARGE));
			assertTrue(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	// ==================== OLIVE ====================

	@Test
	public void testMinionPigletKnight() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_glimmer_dancer"); // Fae
			playMinionCard(context, player, "minion_piglet_knight");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellForestGetTogether() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			player.getHero().setHp(20);
			playCard(context, player, "spell_forest_get-together");
			assertEquals(1, player.getHand().size());
			assertEquals(24, player.getHero().getHp());
		});
	}

	// ==================== PEACH ====================

	@Test
	public void testMinionDormantObelisk() {
		runGym((context, player, opponent) -> {
			Minion obelisk = playMinionCard(context, player, "minion_dormant_obelisk");
			playCard(context, player, "spell_practice", obelisk); // +3 attack
			assertEquals(6, obelisk.getAttack()); // 3 from spell + 3 from trigger
			assertTrue(obelisk.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		});
	}

	@Test
	public void testMinionLunarOracle() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.IMBUE, 1);
			for (int i = 0; i < 3; i++) {
				Card s = receiveCard(context, player, "spell_lunstone");
				playCard(context, player, s);
			}
			Card deal6 = receiveCard(context, player, "spell_test_deal_6");
			playMinionCard(context, player, "minion_lunar_oracle");
			assertTrue(costOf(context, player, deal6) == 1 || costOf(context, player, deal6) == 4);
		});
	}

	@Test
	public void testMinionMoonFox() {
		runGym((context, player, opponent) -> {
			Minion fox = playMinionCard(context, player, "minion_moon_fox");
			attack(context, player, fox, opponent.getHero());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellFireRing() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_fire_ring", ally);
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
		});
	}

	@Test
	public void testSpellFutureSight() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			playCard(context, player, "spell_future_sight");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellKindredSpirits() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_moon_fox"); // Spellpower +1
			playCard(context, player, "spell_kindred_spirits");
			assertEquals(3, player.getMinions().size()); // Moon Fox + 2 Kumiho
		});
	}

	@Test
	public void testSpellLostPossessions() {
		runGym((context, player, opponent) -> {
			Card s = receiveCard(context, player, "spell_lunstone");
			playCard(context, player, s);
			playCard(context, player, "spell_lost_possessions");
			assertEquals(1, player.getHand().size());
			assertEquals("spell_lunstone", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellMorningFlame() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(4);
			int oHp = opponent.getHero().getHp();
			playCard(context, player, "spell_morning_flame", opponent.getHero());
			assertEquals(oHp - 4, opponent.getHero().getHp());
			assertEquals(6, player.getMaxMana());
		});
	}

	@Test
	public void testSpellNineTailInfusion() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 5/5 -> 10/10
			playCard(context, player, "spell_nine_tail_infusion", ally);
			assertEquals(10, ally.getAttack());
			assertEquals(10, ally.getHp());
		});
	}

	@Test
	public void testSpellStarlightCeremony() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			player.getHero().setHp(20);
			playCard(context, player, "spell_starlight_ceremony", player.getHero());
			assertEquals(3, player.getHand().size());
			assertEquals(26, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellWhisperOfBetrayal() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e3 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_whisper_of_betrayal", e2);
			assertTrue(e1.isDestroyed());
			assertTrue(e3.isDestroyed());
		});
	}

	// ==================== RUST ====================

	@Test
	public void testMinionBomberWhelp() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion whelp = playMinionCard(context, player, "minion_bomber_whelp");
			destroy(context, whelp);
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testMinionBoneWhelp() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_44dragon");
			Minion whelp = playMinionCard(context, player, "minion_bone_whelp");
			destroy(context, whelp);
			assertEquals(1, player.getHand().size());
			assertEquals("minion_bone_whelp", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionChainedGuardian() {
		runGym((context, player, opponent) -> {
			Minion g = playMinionCard(context, player, "minion_chained_guardian");
			assertTrue(g.hasAttribute(Attribute.PERMANENT));
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			assertFalse(player.getMinions().get(0).hasAttribute(Attribute.PERMANENT));
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testMinionDarkmireDrake() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_44dragon");
			Card drake = receiveCard(context, player, "minion_darkmire_drake");
			assertEquals(5, costOf(context, player, drake));
		});
	}

	@Test
	public void testMinionEbonMarshmage() {
		runGym((context, player, opponent) -> {
			Card s = receiveCard(context, player, "spell_lunstone");
			playCard(context, player, s);
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion mage = playMinionCard(context, player, "minion_ebon_marshmage");
			context.endTurn();
			context.endTurn();
			attack(context, player, mage, enemy);
			assertEquals(1, player.getHand().size());
			assertEquals("spell_lunstone", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionEruditeDrakonid() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion drak = playMinionCard(context, player, "minion_erudite_drakonid");
			context.endTurn();
			context.endTurn();
			shuffleToDeck(context, player, "spell_lunstone");
			player.getHand().clear();
			attack(context, player, drak, enemy);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionEtherealMaker() {
		runGym((context, player, opponent) -> {
			Minion big = playMinionCard(context, player, "minion_ancient_abomination"); // 7-cost
			playMinionCardWithBattlecry(context, player, "minion_ethereal_maker", big);
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testMinionFireguardBulwark() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_44dragon");
			Card bulwark = receiveCard(context, player, "minion_fireguard_bulwark");
			assertEquals(6, costOf(context, player, bulwark));
		});
	}

	@Test
	public void testMinionGearscrapper() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_magma_hound"); // Elemental
			playMinionCardWithBattlecry(context, player, "minion_gearscrapper", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionGrimestreetCorporal() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2/2
			playMinionCardWithBattlecry(context, player, "minion_grimestreet_corporal", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionLittlestForerunner() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion runner = playMinionCard(context, player, "minion_littlest_forerunner");
			context.endTurn();
			context.endTurn();
			Card dragon = receiveCard(context, player, "token_44dragon"); // 4-cost
			attack(context, player, runner, enemy);
			assertEquals(3, costOf(context, player, dragon));
		});
	}

	@Test
	public void testMinionRousingCommander() {
		runGym((context, player, opponent) -> {
			Minion cmd = playMinionCard(context, player, "minion_rousing_commander"); // 2/4
			useHeroPower(context, player);
			assertEquals(3, cmd.getAttack());
		}, HeroClass.GOLD, HeroClass.GOLD);
	}

	@Test
	public void testMinionSavageOgreseer() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_savage_ogreseer");
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testMinionScarletMagiserpent() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_lunstone");
			Minion serp = playMinionCard(context, player, "minion_scarlet_magiserpent");
			assertEquals(5, serp.getAttack());
			assertEquals(5, serp.getHp());
		});
	}

	@Test
	public void testMinionShackledRedbane() {
		runGym((context, player, opponent) -> {
			Minion redbane = playMinionCard(context, player, "minion_shackled_redbane");
			assertTrue(redbane.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
			playMinionCard(context, player, "token_44dragon");
			assertFalse(redbane.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
		});
	}

	@Test
	public void testMinionSharkscaleEnvoy() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_sharkscale_envoy");
			Card d1 = receiveCard(context, player, "token_44dragon"); // 4-cost
			assertEquals(2, costOf(context, player, d1));
		});
	}

	@Test
	public void testMinionSpiritOfTheSerpent() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_spirit_of_the_serpent");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			destroy(context, enemy);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionSupremeFinlord() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_supreme_finlord");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_lacerate", enemy);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionTrailblazer() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion blazer = playMinionCard(context, player, "minion_trailblazer");
			context.endTurn();
			context.endTurn();
			shuffleToDeck(context, player, "minion_neutral_test"); // 2-cost
			attack(context, player, blazer, enemy);
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSpellAscendancy() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_ascendancy");
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSpellDraconicBolt() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_draconic_bolt", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellDragonPendant() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 2 HP
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 6/6
			playCard(context, player, "spell_dragon_pendant", ally);
			context.endTurn();
			context.endTurn();
			attack(context, player, ally, enemy);
			assertEquals(oHp - 4, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellDragonforce() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_dragonforce");
			playCard(context, player, "spell_lacerate", enemy);
			assertEquals(1, player.getMinions().size());
			assertEquals("token_44dragon", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellInciendaryBreak() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3 attack
			playCard(context, player, "spell_inciendary_break", e2);
			assertEquals(2, e1.getHp());
		});
	}

	@Test
	public void testSpellLacerate() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_lacerate", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(3, player.getHero().getAttack());
		});
	}

	@Test
	public void testSpellLesserGarnetSpellstone() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_lesser_garnet_spellstone");
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
		});
	}

	@Test
	public void testSpellOverwhelmingStrength() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 4/4
			playCard(context, player, "spell_overwhelming_strength", ally);
			context.endTurn();
			context.endTurn();
			attack(context, player, ally, enemy);
			assertEquals(6, ally.getAttack());
		});
	}

	@Test
	public void testSpellPlundererAbound() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			playCard(context, player, "spell_plunderer_abound");
			assertEquals(3, opponent.getMinions().size());
			destroy(context, opponent.getMinions().get(0));
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellRoarOfDominance() {
		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, player, "token_44dragon");
			playCard(context, player, "spell_roar_of_dominance");
			assertTrue(dragon.hasAttribute(Attribute.IMMUNE));
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellSmashNRun() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5"); // 4 attack
			Card inHand = receiveCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_smash_n_run", enemy);
			assertTrue(enemy.isDestroyed());
			Minion played = playMinionCard(context, player, inHand);
			assertEquals(6, played.getAttack());
		});
	}

	@Test
	public void testSpellSupersonicRoar() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_supersonic_roar");
			assertEquals(1, enemy.getAttack());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellVerdantVitality() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_high_note", ally); // 4/3
			playCard(context, player, "spell_verdant_vitality", ally);
			assertEquals(6, ally.getAttack());
			assertEquals(9, ally.getHp());
		});
	}

	@Test
	public void testSpellWildResonance() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_wild_resonance");
			assertEquals(4, ally.getAttack());
			assertEquals(2, player.getHero().getAttack());
		});
	}

	@Test
	public void testTokenChainedGuardian() {
		runGym((context, player, opponent) -> {
			Minion g = playMinionCard(context, player, "token_chained_guardian");
			assertTrue(g.hasAttribute(Attribute.TAUNT));
			assertEquals(8, g.getAttack());
		});
	}

	@Test
	public void testWeaponBladeOfEventide() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5"); // even
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "weapon_blade_of_eventide");
			destroy(context, player.getWeaponZone().get(0));
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testWeaponBreakerJavelin() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "weapon_breaker_javelin");
			destroy(context, player.getWeaponZone().get(0));
			assertEquals(5, ally.getAttack());
			assertEquals(4, ally.getHp());
		});
	}

	@Test
	public void testWeaponMarrowCleaver() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "weapon_marrow_cleaver");
			attack(context, player, player.getHero(), enemy);
			assertEquals(1, player.getHand().size());
			assertEquals("weapon_marrow_cleaver", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testWeaponSeethingAxe() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "weapon_seething_axe");
			attack(context, player, player.getHero(), enemy);
			assertEquals(3, ally.getAttack());
		});
	}

	@Test
	public void testWeaponVerdantScimitar() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_44dragon");
			Card scim = receiveCard(context, player, "weapon_verdant_scimitar");
			assertEquals(4, costOf(context, player, scim));
		});
	}

	// ==================== TOAST ====================

	@Test
	public void testMinionBananaTree() {
		runGym((context, player, opponent) -> {
			Minion tree = playMinionCard(context, player, "minion_banana_tree");
			playCard(context, player, "spell_high_note", tree);
			assertEquals(1, player.getHand().size());
			assertEquals("spell_apple", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionDifficultChild() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test"); // 2-cost
			Card child = receiveCard(context, player, "minion_difficult_child");
			player.setMana(child.getBaseManaCost());
			context.getLogic().performGameAction(player.getId(), child.play());
			assertEquals(0, player.getMinions().size());
			assertEquals(1, player.getHand().size());
			assertEquals("minion_difficult_child", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionInedibleGhoul() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			shuffleToDeck(context, player, "minion_inedible_ghoul");
			playCard(context, player, "spell_massive_burp", enemy); // roasts ghoul (3 cost) + ghoul deals 3
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionLimbTentacle() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			shuffleToDeck(context, player, "minion_limb_tentacle");
			playCard(context, player, "spell_massive_burp", enemy);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_limb_tentacle", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionPastryGolem() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_6_7");
			playMinionCard(context, player, "minion_pastry_golem");
			playMinionCard(context, player, "minion_difficult_child"); // roasts 6-cost card
			assertEquals(3, player.getMinions().size()); // pastry golem + difficult child + 1/1 pieling
		});
	}

	@Test
	public void testMinionPickyLookout() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playMinionCard(context, player, "minion_picky_lookout");
			assertEquals(0, player.getDeck().size());
		});
	}

	@Test
	public void testMinionVoraciousToadosaur() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_magma_hound"); // Elemental
			context.endTurn();
			context.endTurn();
			playMinionCard(context, player, "minion_voracious_toadosaur");
			assertEquals(3, player.getDeck().size());
		});
	}

	@Test
	public void testSpellAlDente() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_glimmer_dancer"); // 3/3
			playCard(context, player, "spell_al_dente", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(1, player.getHand().size());
			assertEquals("spell_al_dente", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellBloodThirst() {
		runGym((context, player, opponent) -> {
			Minion a1 = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 4/4
			Minion a2 = playMinionCard(context, player, "minion_test_4_5"); // 4/5
			playCard(context, player, "spell_blood_thirst", a1);
			context.endTurn();
			assertEquals(1, a2.getHp());
		});
	}

	@Test
	public void testSpellConjuredTable() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playMinionCard(context, player, "minion_moon_fox"); // Spellpower +1
			playCard(context, player, "spell_conjured_table");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellDeviateDelight() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_deviate_delight", ally);
			assertEquals(3, player.getMinions().get(0).getAttack());
			assertEquals(2, player.getMinions().get(0).getHp());
		});
	}

	@Test
	public void testSpellHalitosis() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast"); // 3 attack
			playCard(context, player, "spell_halitosis", ally);
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testSpellJulienneCut() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_julienne_cut", ally);
			assertEquals(2, player.getMinions().size());
			assertEquals(1, player.getMinions().get(0).getAttack());
			assertEquals(1, player.getMinions().get(0).getHp());
		});
	}

	@Test
	public void testSpellLesserOpalSpellstone() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_lesser_opal_spellstone", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellMassiveBurp() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5"); // 4-cost
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_massive_burp", enemy);
			assertEquals(3, enemy.getHp());
		});
	}

	@Test
	public void testSpellPrimalSeasoning() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_primal_seasoning");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellStormchops() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_stormchops", ally);
			destroy(context, ally);
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testSpellSummonedTable() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_massive_burp", enemy);
			playCard(context, player, "spell_summoned_table");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellTuskarrProvisions() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_test_4_5");
			playCard(context, player, "spell_tuskarr_provisions");
			assertEquals(2, player.getHand().size()); // 1 sourced + 1 drawn (1 roasted)
		});
	}

	@Test
	public void testWeaponBananaJuiceBottle() {
		runGym((context, player, opponent) -> {
			Card s = receiveCard(context, player, "spell_high_note"); // 1-cost spell
			playCard(context, player, s, opponent.getHero());
			playCard(context, player, "weapon_banana_juice_bottle");
			assertEquals(3, player.getWeaponZone().get(0).getAttack());
			assertEquals(4, player.getWeaponZone().get(0).getDurability());
		});
	}

	@Test
	public void testWeaponButchersCleaver() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_butcher_s_cleaver");
			attack(context, player, player.getHero(), e1);
			assertEquals(2, e1.getHp());
			assertEquals(2, e2.getHp());
		});
	}

	// ==================== TWILIGHT ====================

	@Test
	public void testMinionBloodburner() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			int oHp = opponent.getHero().getHp();
			playMinionCardWithBattlecry(context, player, "minion_bloodburner", opponent.getHero());
			assertEquals(oHp - 2, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionGlyphCarver() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			player.getHero().setHp(15);
			Minion carver = playMinionCard(context, player, "minion_glyph_carver");
			destroy(context, carver);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionShadowsurger() {
		runGym((context, player, opponent) -> {
			Minion surger = playMinionCard(context, player, "minion_shadowsurger");
			destroy(context, surger);
			assertEquals(6, player.getHero().getAttack());
		});
	}

	@Test
	public void testMinionSoulstitcher() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_high_note", ally); // 4/3
			playMinionCardWithBattlecry(context, player, "minion_soulstitcher", ally);
			assertEquals(5, ally.getHp());
		});
	}

	@Test
	public void testMinionTaintedSkulker() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_tainted_skulker");
			context.endTurn();
			context.endTurn(); // end of opponent's turn triggers decay
			assertEquals(4, enemy.getHp());
		});
	}

	@Test
	public void testSpellDeathThroes() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			destroy(context, ally);
			playCard(context, player, "spell_death_throes");
			assertEquals(1, player.getMinions().size());
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.DECAY));
		});
	}

	@Test
	public void testSpellDireLiturgy() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20); // missing 10 HP
			playCard(context, player, "spell_dire_liturgy");
			assertEquals(1, player.getMinions().size());
			assertEquals(10, player.getMinions().get(0).getAttack());
			assertEquals(10, player.getMinions().get(0).getHp());
		});
	}

	@Test
	public void testSpellGorgeRework() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 6/6
			playCard(context, player, "spell_gorge_rework", ally);
			assertEquals(6, ally.getAttack());
			destroy(context, ally);
			assertEquals(1, enemy.getHp());
		});
	}

	@Test
	public void testSpellReap() {
		runGym((context, player, opponent) -> {
			Minion weak = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion strong = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_reap");
			assertTrue(weak.isDestroyed());
			assertFalse(strong.isDestroyed());
		});
	}

	@Test
	public void testSpellScarletThirstRework() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			playCard(context, player, "spell_scarlet_thirst_rework");
			Card note = receiveCard(context, player, "spell_high_note");
			playCard(context, player, note, opponent.getHero());
			assertEquals(22, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellTaintedGrasp() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_1");
			shuffleToDeck(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_tainted_grasp");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_neutral_test_6_7", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellWarpwalk() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_warpwalk", ally);
			assertTrue(ally.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
			destroy(context, ally);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellYoullDo() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			Minion ally = playMinionCard(context, player, "minion_test_4_5"); // 5 HP
			playCard(context, player, "spell_youll_do", ally);
			assertTrue(ally.isDestroyed());
			assertEquals(25, player.getHero().getHp());
			assertEquals(5, player.getHero().getAttack());
		});
	}

	@Test
	public void testWeaponHookedCleaver() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_hooked_cleaver");
			attack(context, player, player.getHero(), e1);
			assertEquals(4, e1.getHp());
			assertEquals(4, e2.getHp());
		});
	}

	@Test
	public void testWeaponWarpScythe() {
		runGym((context, player, opponent) -> {
			int pHp = player.getHero().getHp();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playCard(context, player, "weapon_warp_scythe"); // 3/2
			attack(context, player, player.getHero(), enemy);
			assertEquals(1, enemy.getHp()); // 3 * 2 = 6 damage
			assertEquals(pHp - 12, player.getHero().getHp()); // 6 * 2 = 12 damage
		});
	}

	// ==================== EGGPLANT ====================

	@Test
	public void testEarthquake() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "earthquake");
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionBogtoeChallenger() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bogtoe_challenger");
			playMinionCard(context, player, "minion_neutral_test_1");
			useHeroPower(context, player);
			assertEquals("minion_bogtoe_challenger", player.getMinions().get(1).getSourceCard().getCardId());
		}, HeroClass.GOLD, HeroClass.GOLD);
	}

	@Test
	public void testMinionClamouringCrusher() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_neutral_test");
			Minion crusher = playMinionCard(context, player, "minion_clamouring_crusher");
			assertEquals(4, crusher.getHp());
		});
	}

	@Test
	public void testMinionFemmeFatale() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_femme_fatale");
			context.endTurn();
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionLibrarySupervisor() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Minion sup = playMinionCard(context, player, "minion_library_supervisor");
			destroy(context, sup);
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionMagicat() {
		runGym((context, player, opponent) -> {
			Card c = receiveCard(context, player, "minion_little_purrsuer"); // 0-cost 0/2
			playMinionCard(context, player, "minion_magicat");
			Minion played = playMinionCard(context, player, c);
			assertEquals(1, played.getAttack());
			assertEquals(4, played.getHp());
		});
	}

	@Test
	public void testMinionProudjawLackey() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_proudjaw_lackey");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2/2 -> 4/4
			assertEquals(4, enemy.getAttack());
			assertEquals(4, enemy.getHp());
		});
	}

	@Test
	public void testMinionPurrfectTracker() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_lunstone");
			shuffleToDeck(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_purrfect_tracker");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testMinionRavenousLasher() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2-cost
			playMinionCard(context, player, "minion_ravenous_lasher");
			context.endTurn();
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionSlipperyMook() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.INVOKED, 1);
			Card mook = receiveCard(context, player, "minion_slippery_mook");
			player.setMana(6);
			context.getLogic().performGameAction(player.getId(), mook.play());
			Minion summoned = player.getMinions().get(0);
			assertEquals(3, summoned.getAttack());
			assertEquals(4, summoned.getHp());
		});
	}

	@Test
	public void testMinionSqueakFreak() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_lunstone");
			Minion freak = playMinionCard(context, player, "minion_squeak_freak");
			destroy(context, freak);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionSteelwingRider() {
		runGym((context, player, opponent) -> {
			Card rider = receiveCard(context, player, "minion_steelwing_rider");
			player.setMana(11);
			player.setMaxMana(11);
			context.getLogic().performGameAction(player.getId(), rider.play());
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testMinionTuskarrCamper() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_test_deal_6"); // 4-cost
			player.getHero().setHp(20);
			playMinionCard(context, player, "minion_tuskarr_camper");
			assertEquals(1, player.getHand().size());
			assertEquals(24, player.getHero().getHp());
		});
	}

	@Test
	public void testMinionVengefulGhoul() {
		runGym((context, player, opponent) -> {
			Minion ghoul = playMinionCard(context, player, "minion_vengeful_ghoul");
			context.endTurn();
			playCard(context, opponent, "spell_high_note", ghoul);
			assertEquals(1, player.getHand().size());
			assertEquals("minion_vengeful_ghoul", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSecretRebirth() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "secret_rebirth");
			context.endTurn();
			playCard(context, opponent, "spell_high_note", ally);
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testSpellBlacksteelShell() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_mech_test");
			playMinionCard(context, player, "minion_mech_test");
			playCard(context, player, "spell_blacksteel_shell");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2 -> 4/4
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
		});
	}

	@Test
	public void testSpellDastardlyDuo() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_dastardly_duo", ally);
			assertEquals(2, player.getMinions().size());
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.TAUNT));
			assertTrue(player.getMinions().get(1).hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testSpellEvilLaughter() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_evil_laughter");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellLesserCitrineSpellstone() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2-cost
			playCard(context, player, "spell_lesser_citrine_spellstone");
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellWildMutation() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_wild_mutation");
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			assertEquals(7, ally.getAttack());
			assertEquals(7, ally.getHp());
		});
	}

	// ==================== ICE ====================

	@Test
	public void testMinionBolsterousGesit() {
		runGym((context, player, opponent) -> {
			Minion geist = playMinionCard(context, player, "minion_bolsterous_gesit");
			assertTrue(geist.hasAttribute(Attribute.TAUNT));
			assertTrue(geist.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		});
	}

	@Test
	public void testMinionGilneanWitchhuntress() {
		runGym((context, player, opponent) -> {
			Card c = receiveCard(context, player, "minion_gilnean_witchhuntress"); // 5/7
			context.endTurn();
			context.endTurn();
			Minion m = playMinionCard(context, player, c);
			assertEquals(7, m.getAttack());
			assertEquals(5, m.getHp());
		});
	}

	@Test
	public void testMinionIceFrostWyrm() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_ice_frost_wyrm");
			assertEquals(3, enemy.getHp());
			assertTrue(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellAbominationsMight() {
		runGym((context, player, opponent) -> {
			Minion a1 = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			a1.setAttribute(Attribute.FROZEN);
			Minion a2 = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_abominations_might", a1);
			assertEquals(6, a1.getAttack());
			assertEquals(6, a1.getHp());
			assertEquals(3, a2.getHp());
		});
	}

	@Test
	public void testSpellAcherusDeathgate() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_ice_frost_wyrm"); // 8-cost
			playCard(context, player, "spell_acherus_deathgate");
			assertEquals(1, player.getHand().size());
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testSpellAllWillServe() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			destroy(context, enemy);
			playCard(context, player, "spell_all_will_serve");
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellBloodPlague() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_blood_plague");
			destroy(context, e1);
			assertEquals(4, e2.getHp());
		});
	}

	@Test
	public void testSpellBoneShield() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_test_3_2");
			playCard(context, player, "spell_bone_shield");
			assertEquals(3, player.getHero().getArmor());
			assertEquals(5, player.getWeaponZone().get(0).getDurability());
		});
	}

	@Test
	public void testSpellHeartstrike() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			int oHp = opponent.getHero().getHp();
			playCard(context, player, "weapon_test_3_2"); // 3 attack
			playCard(context, player, "spell_heartstrike", opponent.getHero());
			assertEquals(oHp - 3, opponent.getHero().getHp());
			assertEquals(26, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellIcyTalon() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			e1.setAttribute(Attribute.FROZEN);
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_icy_talon", e1);
			assertTrue(e1.isDestroyed());
			assertEquals(3, e2.getHp());
		});
	}

	@Test
	public void testSpellRunicCorruption() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			receiveCard(context, player, "spell_rune");
			playCard(context, player, "spell_runic_corruption");
			assertEquals(0, player.getHand().size());
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
		});
	}

	@Test
	public void testSpellRunicEmpowerment() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_runic_empowerment");
			assertEquals(3, player.getHand().size());
			assertEquals("spell_rune", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testWeaponFrostscythe() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			e1.setAttribute(Attribute.FROZEN);
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_frostscythe"); // 3 attack
			attack(context, player, player.getHero(), e1);
			assertEquals(1, e1.getHp()); // 3 + 3 = 6 damage
			assertEquals(2, e2.getHp()); // 3 adjacent damage
		});
	}

	// ==================== LEATHER ====================

	@Test
	public void testMinionHighwayman() {
		runGym((context, player, opponent) -> {
			Minion h = playMinionCard(context, player, "minion_highwayman");
			assertTrue(h.hasAttribute(Attribute.CHARGE));
			assertTrue(h.hasAttribute(Attribute.CANNOT_ATTACK_HEROES));
		});
	}

	@Test
	public void testMinionSkilledHunter() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			context.getLogic().addEnchantments(player, player, context.getCardById("enchantment_marked"), enemy);
			Minion hunter = playMinionCard(context, player, "minion_skilled_hunter");
			assertEquals(4, hunter.getAttack());
			assertEquals(4, hunter.getHp());
		});
	}

	@Test
	public void testSpellAmberDrop() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_amber_drop");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellEscapePosition() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_escape_position", enemy);
			destroy(context, enemy);
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testWeaponSharpAPractice() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			playCard(context, player, "weapon_sharp_a_practice");
			assertEquals("hero_power_double_scatter_shot", player.getHeroPowerZone().get(0).getCardId());
			useHeroPower(context, player);
			assertEquals(oHp - 4, opponent.getHero().getHp());
			assertEquals(4, player.getWeaponZone().get(0).getDurability());
		});
	}

	// ==================== OBSIDIAN ====================

	@Test
	public void testMinionAvalancheAcolyte() {
		runGym((context, player, opponent) -> {
			Minion e1 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion e2 = playMinionCard(context, opponent, "minion_test_4_5");
			Minion acolyte = playMinionCard(context, player, "minion_avalanche_acolyte");
			context.endTurn();
			context.endTurn();
			attack(context, player, acolyte, e1);
			assertTrue(e2.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testMinionEchowebLurker() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_echoweb_lurker", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionFrostWyrm() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_frost_wyrm");
			assertTrue(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testMinionHypothermicOutcast() {
		runGym((context, player, opponent) -> {
			Minion outcast = playMinionCard(context, player, "minion_hypothermic_outcast");
			assertTrue(outcast.hasAttribute(Attribute.FROZEN));
			playCard(context, player, "spell_high_note", outcast);
			assertFalse(outcast.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testMinionIceboxSnowbuddy() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_icebox_snowbuddy");
			assertEquals(oHp - 3, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionIcyDebris() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_icy_debris");
			shuffleToDeck(context, player, "minion_icy_debris");
			Minion debris = playMinionCard(context, player, "minion_icy_debris");
			destroy(context, debris);
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testMinionMadIcicler() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			enemy.setAttribute(Attribute.FROZEN);
			playMinionCard(context, player, "minion_mad_icicler");
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testMinionMarrowling() {
		runGym((context, player, opponent) -> {
			int oHp = opponent.getHero().getHp();
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "spell_bone_storm");
			// Ensure spell_bone_storm is on top
			player.getDeck().clear();
			shuffleToDeck(context, player, "spell_bone_storm");
			playMinionCard(context, player, "minion_marrowling");
			assertEquals(oHp - 1, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionMournfulGravetender() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			destroy(context, ally);
			playMinionCard(context, player, "minion_mournful_gravetender");
			assertEquals(1, player.getHand().size());
			Minion replayed = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(3, replayed.getAttack());
			assertEquals(3, replayed.getHp());
		});
	}

	@Test
	public void testMinionObsidianKnight() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_obsidian_knight");
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionSacredTombstone() {
		runGym((context, player, opponent) -> {
			Minion tomb = playMinionCard(context, player, "minion_sacred_tombstone");
			destroy(context, tomb);
			context.endTurn();
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_sacred_tombstone", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionSaroniteDrake() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			Minion drake = playMinionCard(context, player, "minion_saronite_drake");
			assertEquals(2, player.getDeck().size());
			destroy(context, drake);
			assertEquals(3, player.getDeck().size());
		});
	}

	@Test
	public void testMinionTombstoneTerror() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_test_deathrattle");
			destroy(context, enemy);
			Minion terror = playMinionCard(context, player, "minion_tombstone_terror");
			destroy(context, terror);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionTrappedSoul() {
		runGym((context, player, opponent) -> {
			Card c = receiveCard(context, player, "minion_neutral_test"); // 2-cost
			playMinionCard(context, player, "minion_trapped_soul");
			assertTrue(player.getHero().hasAttribute(Attribute.AURA_IMMUNE));
			assertEquals(12, costOf(context, player, c));
		});
	}

	@Test
	public void testSpellChainHook() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_chain_hook", enemy);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellCollectibleDeathGrip() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_collectible_death_grip");
			assertEquals(0, opponent.getDeck().size());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellCollectibleObliterate() {
		runGym((context, player, opponent) -> {
			int pHp = player.getHero().getHp();
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5"); // 5 HP
			playCard(context, player, "spell_collectible_obliterate", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(pHp - 5, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellDeathsEmbrace() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			Minion enemy = playMinionCard(context, opponent, "minion_test_deathrattle");
			playCard(context, player, "spell_deaths_embrace", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(1, player.getHand().size()); // friendly deathrattle triggered!
		});
	}

	@Test
	public void testSpellGlacialAdvance() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_glacial_advance");
			assertTrue(enemy.isDestroyed());
			context.endTurn();
			Minion next = playMinionCard(context, opponent, "minion_neutral_test");
			assertTrue(next.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellHornOfWinter() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_4_5");
			playCard(context, player, "spell_horn_of_winter");
			assertEquals(1, player.getMinions().size());
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellShadowInfusion() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_shadow_infusion", ally);
			destroy(context, ally);
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSpellWraithWalk() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_wraith_walk");
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testWeaponDualChillblades() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_dual_chillblades");
			assertTrue(player.getHero().hasAttribute(Attribute.WINDFURY));
			attack(context, player, player.getHero(), enemy);
			assertTrue(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	// ==================== RED ====================

	@Test
	public void testMinionMischeviousWisp() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_mischevious_wisp");
			assertEquals(1, opponent.getDeck().size());
			assertEquals("spell_rude_stare", opponent.getDeck().get(0).getCardId());
		});
	}

	// ==================== ROSE ====================

	@Test
	public void testMinionAlphaRaptor() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2-cost
			playMinionCard(context, player, "minion_test_3_2_beast"); // Beast
			playMinionCardWithBattlecry(context, player, "minion_alpha_raptor", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testMinionBirdbrain() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2_beast"); // 3/2
			playMinionCardWithBattlecry(context, player, "minion_birdbrain", beast);
			assertEquals(8, beast.getAttack() + beast.getHp());
		});
	}

	@Test
	public void testMinionCeremonialAlter() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, opponent, "minion_neutral_test");
			shuffleToDeck(context, opponent, "minion_neutral_test");
			Minion alter = playMinionCard(context, player, "minion_ceremonial_alter");
			destroy(context, alter);
			assertEquals(2, opponent.getHand().size());
		});
	}

	@Test
	public void testMinionChantLeader() {
		runGym((context, player, opponent) -> {
			Card leader = receiveCard(context, player, "minion_chant_leader"); // 5-cost
			playCard(context, player, "spell_lunstone");
			assertEquals(4, costOf(context, player, leader));
		});
	}

	@Test
	public void testMinionDreadweaver() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test"); // 2-cost
			shuffleToDeck(context, player, "minion_test_3_2_beast"); // 2-cost
			Minion target = playMinionCard(context, player, "minion_neutral_test"); // 2-cost
			playMinionCardWithBattlecry(context, player, "minion_dreadweaver", target);
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testMinionEntrancedDancer() {
		runGym((context, player, opponent) -> {
			Minion dancer = playMinionCard(context, player, "minion_entranced_dancer"); // 4/3
			playCard(context, player, "spell_practice", dancer);
			assertEquals(4, dancer.getHp());
		});
	}

	@Test
	public void testMinionForgottenAncestor() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			Minion anc = playMinionCard(context, player, "minion_forgotten_ancestor");
			destroy(context, anc);
			assertTrue(ally.hasAttribute(Attribute.TAUNT));
			assertTrue(ally.hasAttribute(Attribute.LIFESTEAL));
		});
	}

	@Test
	public void testMinionHexcrazedVessel() {
		runGym((context, player, opponent) -> {
			Minion vessel = playMinionCard(context, player, "minion_hexcrazed_vessel");
			playCard(context, player, "spell_practice", vessel);
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionKrentiaxxProwler() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			Card prowler = receiveCard(context, player, "minion_krentiaxx_prowler");
			assertEquals(5, costOf(context, player, prowler));
		});
	}

	@Test
	public void testMinionProlificTamer() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_test_3_2_beast");
			}
			playMinionCard(context, player, "minion_prolific_tamer");
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testMinionRaptorRiderRaider() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_6_7");
			shuffleToDeck(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_raptor_rider_raider");
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testMinionTempleMilitia() {
		runGym((context, player, opponent) -> {
			Minion militia = playMinionCard(context, player, "minion_temple_militia");
			playCard(context, player, "spell_practice", militia);
			assertEquals(1, player.getHand().size());
			assertEquals("minion_temple_militia", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testMinionTempleWatcher() {
		runGym((context, player, opponent) -> {
			Card s = receiveCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_temple_watcher");
			assertEquals(5, costOf(context, player, s));
		});
	}

	@Test
	public void testMinionVexspewer() {
		runGym((context, player, opponent) -> {
			Minion vex = playMinionCard(context, player, "minion_vexspewer");
			assertEquals(1, player.getHand().size());
			destroy(context, vex);
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSecretShallowGrave() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "secret_shallow_grave");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", ally);
			assertFalse(ally.isDestroyed());
			assertEquals(2, ally.getHp());
		});
	}

	@Test
	public void testSecretSoulSplit() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "secret_soul_split");
			context.endTurn();
			attack(context, opponent, enemy, player.getHero());
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSecretWardEvilEye() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "secret_ward_evil_eye");
			context.endTurn();
			context.endTurn(); // opponent ends turn -> triggers secret
			assertTrue(ally.hasAttribute(Attribute.UNTARGETABLE_BY_OPPONENT_SPELLS));
		});
	}

	@Test
	public void testSpellAshesToAshesVoodoo() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_ashes_to_ashes_voodoo", enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(1, player.getHand().size());
			assertEquals("minion_neutral_test_1", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellBatTalion() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_moon_fox"); // Spellpower +1
			playCard(context, player, "spell_bat-talion");
			assertEquals(3, player.getMinions().size()); // Moon Fox + 2 Bats
		});
	}

	@Test
	public void testSpellCemetaryParty() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_deathrattle");
			destroy(context, ally);
			player.getHand().clear();
			playCard(context, player, "spell_cemetary_party");
			assertEquals(1, player.getMinions().size());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellDinosoul() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2_beast");
			destroy(context, beast);
			playCard(context, player, "spell_dinosoul");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_3_2_beast", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellFalsePromise() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 7 HP
			playCard(context, player, "spell_test_deal_6", enemy); // 1 HP
			playCard(context, player, "spell_false_promise", enemy); // heals 3 -> 4 HP (still damaged) -> destroyed!
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellGhostlyEssence() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			playMinionCard(context, player, "minion_moon_fox"); // Spellpower +1
			playCard(context, player, "spell_ghostly_essence", player.getHero());
			assertEquals(23, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellLawOfTheJungle() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test"); // 2-cost
			playCard(context, player, "spell_law_of_the_jungle", enemy);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellParanoia() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_paranoia", ally);
			context.endTurn();
			Card enemyCard = receiveCard(context, opponent, "minion_test_3_2_beast");
			opponent.setMana(enemyCard.getBaseManaCost());
			context.getLogic().performGameAction(opponent.getId(), enemyCard.play());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testSpellSplitPersonality() {
		runGym((context, player, opponent) -> {
			int pHp = player.getHero().getHp();
			int oHp = opponent.getHero().getHp();
			Minion ally = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_split_personality", ally);
			assertEquals(pHp - 3, player.getHero().getHp());
			assertEquals(oHp - 3, opponent.getHero().getHp());
		});
	}

	@Test
	public void testWeaponEnchantingWhip() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2_beast"); // 3/2
			receiveCard(context, player, "spell_divination");
			playCard(context, player, "weapon_enchanting_whip", beast);
			assertEquals(0, player.getHand().size());
			assertEquals(5, beast.getAttack());
			assertEquals(4, beast.getHp());
		});
	}

	@Test
	public void testWeaponSliverOfSilver() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_sliver_of_silver");
			attack(context, player, player.getHero(), opponent.getHero());
			playCard(context, player, "spell_high_note", enemy); // 2 + 1 = 3
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testSpellPandorasBox() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_pandoras_box", enemy);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getHand().size());
			assertEquals("spell_pandoras_box", opponent.getHand().get(0).getCardId());
		});
	}

	// ==================== TEAL ====================

	@Test
	public void testMinionCoilfangBattlemaiden() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_coilfang_battlemaiden");
			playCard(context, player, "spell_condensation"); // Reservoir (25+) active even with 0 deck!
			assertEquals(3, player.getMinions().size());
			assertEquals(1, player.getDeck().size());
		});
	}

	@Test
	public void testMinionNZothsBattlemaiden() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_n_zoth_s_battlemaiden");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_salt_the_wound"); // Reservoir (0-15) inactive even with 0 deck!
			assertEquals(3, enemy.getHp());
			assertFalse(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellCondensation() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 25; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playCard(context, player, "spell_condensation");
			assertEquals(2, player.getMinions().size());
			assertEquals(26, player.getDeck().size());
		});
	}

	@Test
	public void testSpellFindingMobus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_finding_mobus");
			assertEquals(1, player.getHand().size());
			assertEquals("token_whale_shark", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellRegalGaze() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			enemy.setAttribute(Attribute.TAUNT);
			playCard(context, player, "spell_regal_gaze", enemy);
			assertFalse(enemy.hasAttribute(Attribute.TAUNT));
			assertTrue(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellSaltTheWound() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_salt_the_wound"); // 0 cards in deck -> Reservoir (0-15) active
			assertEquals(3, enemy.getHp());
			assertTrue(enemy.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellSquidWard() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_neutral_test"); // 2/2
			playCard(context, player, "spell_squid_ward", ally);
			assertEquals(4, ally.getAttack());
			assertEquals(4, ally.getHp());
			destroy(context, ally);
			assertEquals(1, player.getDeck().size());
		});
	}

	// ==================== TIME ====================

	@Test
	public void testMinionArcaneAccelerator() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, player, "minion_arcane_accelerator");
			playCard(context, player, "spell_high_note", enemy); //SpellCastedTrigger fires before damage: 2 + 1 = 3
			assertEquals(2, enemy.getHp());
		});
	}

	@Test
	public void testMinionMysticWeaver() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test");
			Minion weaver = playMinionCard(context, player, "minion_mystic_weaver");
			assertEquals(0, player.getHand().size());
			assertEquals(1, player.getDeck().size());
			assertTrue(weaver.getAttributeValue(Attribute.SPELL_DAMAGE) >= 1);
		});
	}

	@Test
	public void testSecretMomentsHesitation() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "secret_moments_hesitation");
			context.endTurn();
			attack(context, opponent, enemy, player.getHero());
			assertEquals(1, enemy.getHp());
		});
	}
}




