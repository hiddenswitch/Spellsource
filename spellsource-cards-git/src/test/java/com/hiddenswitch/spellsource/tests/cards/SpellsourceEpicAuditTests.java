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
public class SpellsourceEpicAuditTests extends TestBase {

	private Minion playMinionCardWithBattlecry(GameContext context, Player player, String cardId, Actor target) {
		playCard(context, player, cardId, target);
		return player.getMinions().isEmpty() ? null : player.getMinions().get(player.getMinions().size() - 1);
	}

	// ==================== AMBER ====================

	@Test
	public void testMinionGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_giant");
			assertEquals(15, costOf(context, player, giant));
			Minion beast1 = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion beast2 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			destroy(context, beast1);
			destroy(context, beast2);
			assertEquals(13, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionNurseryOverseer() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_spiderling");
			receiveCard(context, player, "token_spiderling");
			receiveCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_nursery_overseer");
			long guardians = player.getHand().stream().filter(c -> "token_bug_giant_spider".equals(c.getCardId())).count();
			assertEquals(2L, guardians);
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionSpyder() {
		runGym((context, player, opponent) -> {
			Card drawn = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion spyder = playMinionCard(context, player, "minion_spyder");
			destroy(context, spyder);
			assertEquals(Zones.HAND, drawn.getZone());
			assertTrue(opponent.getHand().stream().anyMatch(c -> "minion_spyder".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionTigerAnt() {
		runGym((context, player, opponent) -> {
			Card ant = receiveCard(context, player, "minion_tiger_ant");
			assertEquals(11, costOf(context, player, ant));
			receiveCard(context, player, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_test_3_2_beast");
			assertEquals(9, costOf(context, player, ant));
		});
	}

	@Test
	public void testMinionVisionary() {
		runGym((context, player, opponent) -> {
			Minion visionary = playMinionCard(context, player, "minion_visionary");
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			player.getHero().setHp(20);
			ally.setHp(1);
			playCard(context, player, "spell_test_heal_8", player.getHero());
			assertEquals(28, player.getHero().getHp());
			assertEquals(5, ally.getHp());
		});
	}

	@Test
	public void testMinionWorkerQueen() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_spiderling");
			playMinionCard(context, player, "token_spiderling");
			playMinionCard(context, player, "minion_worker_queen");
			assertTrue(player.getHand().stream().anyMatch(c -> "token_bug_worker".equals(c.getCardId())));
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_bug_worker".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testSpellBossFight() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_boss_fight");
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellCallTheCavalry() {
		runGym((context, player, opponent) -> {
			Minion fodder = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_call_the_cavalry", fodder);
			assertTrue(fodder.isDestroyed());
			assertEquals(1, player.getMinions().size());
			assertEquals("token_shiny_worm", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellEncasedInAmber() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_encased_in_amber", yeti);
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
			assertEquals(Zones.HAND, top.getZone());
			context.endTurn();
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
			context.endTurn();
			assertFalse(yeti.hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testSpellIntoTheParlor() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_into_the_parlor", enemy);
			assertEquals(1, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
			context.endTurn();
			context.endTurn();
			assertEquals(0, player.getMinions().size());
		});
	}

	@Test
	public void testSpellIntoxicate() {
		runGym((context, player, opponent) -> {
			Minion boardMinion = playMinionCard(context, player, "minion_test_4_5");
			Card handMinion = receiveCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_intoxicate", boardMinion);
			assertTrue(boardMinion.hasAttribute(Attribute.POISONOUS));
			assertTrue(handMinion.hasAttribute(Attribute.POISONOUS));
		});
	}

	@Test
	public void testSpellMassHatching() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_mass_hatching");
			assertEquals(10, player.getHand().size());
			for (Card c : player.getHand()) {
				assertTrue(Race.hasRace(context, c, "BEAST"));
			}
		});
	}

	@Test
	public void testSpellParasites() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_parasites", yeti);
			assertEquals(4, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_spiderling", m.getSourceCard().getCardId());
			}
		});
	}

	@Test
	public void testSpellPoisonDart() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_poison_dart", ogre);
			assertTrue(ogre.isDestroyed());
		});
	}

	@Test
	public void testSpellReinforcedShell() {
		runGym((context, player, opponent) -> {
			Minion raptor1 = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_reinforced_shell");
			assertEquals(4, raptor1.getHp());
			Minion raptor2 = playMinionCard(context, player, "minion_test_3_2_beast");
			assertEquals(4, raptor2.getHp());
		});
	}

	@Test
	public void testSpellScarabShield() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_scarab_shield", ogre);
			assertEquals(4, player.getHero().getArmor());
			assertEquals(3, ogre.getHp());
		});
	}

	@Test
	public void testSpellShellReclamation() {
		runGym((context, player, opponent) -> {
			Minion b1 = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion b2 = playMinionCard(context, player, "token_river_crowdcolisk");
			destroy(context, b1);
			destroy(context, b2);
			playCard(context, player, "spell_shell_reclamation");
			assertEquals(2, player.getHero().getArmor());
		});
	}

	@Test
	public void testSpellSpawningPool() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_lunstone");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_spawning_pool");
			assertEquals(2, player.getHand().size());
			assertEquals(player.getHand().get(0).getCardId(), player.getHand().get(1).getCardId());
			assertNotEquals("spell_lunstone", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellTwoByTwo() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_two_by_two");
			assertEquals(4, player.getMinions().size());
		});
	}

	@Test
	public void testSpellUnearth() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_unearth", raptor);
			assertEquals(3, player.getDeck().size());
			for (Card c : player.getDeck()) {
				assertEquals("minion_test_3_2_beast", c.getCardId());
			}
		});
	}

	@Test
	public void testSpellVenomDrench() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_venom_drench");
			assertEquals(2, yeti.getHp());
			assertTrue(yeti.hasAttribute(Attribute.POISONOUS));
			assertEquals(4, ogre.getHp());
			assertTrue(ogre.hasAttribute(Attribute.POISONOUS));
		});
	}

	@Test
	public void testWeaponScepterOfBees() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_test_4_5");
			playCard(context, player, "weapon_scepter_of_bees");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(2, player.getMinions().size());
			assertEquals("token_spiderling", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellDreamOfAFeast() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_dream_of_a_feast");
			assertEquals(6, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals(2, m.getSourceCard().getBaseAttack());
			}
		});
	}

	@Test
	public void testSpellDreamsOfStrength() {
		runGym((context, player, opponent) -> {
			Minion larva = playMinionCard(context, player, "token_spiderling");
			playCard(context, player, "spell_dreams_of_strength", larva);
			assertEquals(8, larva.getAttack());
			assertEquals(8, larva.getHp());
			assertTrue(larva.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testSpellUrgentRecycling() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_urgent_recycling");
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertTrue(Race.hasRace(context, c, "BEAST"));
				assertTrue(c.getBaseManaCost() >= 6);
			}
		});
	}

	// ==================== AZURE ====================

	@Test
	public void testMinionElectricHellkite() {
		runGym((context, player, opponent) -> {
			Minion hellkite = playMinionCard(context, player, "minion_electric_hellkite");
			playCard(context, player, "spell_high_note", hellkite);
			assertEquals(6, hellkite.getHp());
			assertEquals(27, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionWindbreaker() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			playMinionCard(context, player, "minion_windbreaker");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellBlurredRains() {
		runGym((context, player, opponent) -> {
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			Card frostbolt = receiveCard(context, player, "spell_frozen_pulse");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_blurred_rains");
			assertEquals(1, player.getHand().size());
			Card remaining = player.getHand().get(0);
			assertEquals(remaining.getBaseManaCost() - 1, costOf(context, player, remaining));
		});
	}

	@Test
	public void testMinionSeeingSelkie() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_seeing_selkie");
			Card yeti = receiveCard(context, player, "minion_test_4_5");
			assertEquals(4, costOf(context, player, yeti));
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playMinionCard(context, player, "minion_sea_stowaway");
			assertEquals(3, costOf(context, player, yeti));
		});
	}

	@Test
	public void testSpellUnfurlWings() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_unfurl_wings");
			assertEquals(1, player.getHand().size());
			assertEquals("spell_soar", player.getHand().get(0).getCardId());
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_unfurl_wings");
			assertFalse(player.getHand().stream().anyMatch(c -> "spell_soar".equals(c.getCardId())));
		});
	}

	// ==================== BLUEGREY ====================

	@Test
	public void testMinionBogoveyJester() {
		runGym((context, player, opponent) -> {
			Card dragon = putOnTopOfDeck(context, player, "token_44dragon");
			Minion jester = playMinionCard(context, player, "minion_bogovey_jester");
			playCard(context, player, "spell_practice", jester);
			assertEquals(4, jester.getAttack());
			destroy(context, jester);
			assertEquals(Zones.HAND, dragon.getZone());
		});
	}

	@Test
	public void testMinionNoisyUwan() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_noisy_uwan");
			playMinionCardWithBattlecry(context, player, "minion_test_deal_1", opponent.getHero());
			assertEquals(28, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionShortrunStockpiler() {
		runGym((context, player, opponent) -> {
			Card yeti = receiveCard(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_shortrun_stockpiler");
			assertEquals(3, costOf(context, player, yeti));
		});
	}

	@Test
	public void testSpellDraconicDiplomacy() {
		runGym((context, player, opponent) -> {
			Minion drake = playMinionCard(context, player, "token_44dragon");
			assertEquals(4, drake.getAttack());
			playCard(context, player, "spell_draconic_diplomacy");
			assertEquals("hero_power_overpower", player.getHeroPowerZone().get(0).getCardId());
			assertEquals(7, drake.getAttack());
		});
	}

	@Test
	public void testSpellRampantAggression() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(5);
			Card raptor = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_rampant_aggression");
			assertEquals(3, player.getMaxMana());
			assertEquals(2, raptor.getAttributeValue(Attribute.ATTACK_BONUS));
			assertEquals(2, raptor.getAttributeValue(Attribute.HP_BONUS));
		});
	}

	@Test
	public void testMinionShinobiInfiltrator() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion shinobi = playMinionCard(context, player, "minion_shinobi_infiltrator");
			assertTrue(shinobi.hasAttribute(Attribute.STEALTH));
			context.endTurn();
			context.endTurn();
			attack(context, player, shinobi, opponent.getHero());
			assertEquals(Zones.HAND, top.getZone());
		});
	}

	@Test
	public void testMinionTriHornOgre() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(7);
			Minion left = playMinionCard(context, opponent, "minion_test_4_5");
			Minion center = playMinionCard(context, opponent, "minion_test_4_5");
			Minion right = playMinionCard(context, opponent, "minion_test_4_5");
			Minion ogre = playMinionCard(context, player, "minion_tri_horn_ogre");
			assertEquals(5, player.getMaxMana());
			context.endTurn();
			context.endTurn();
			attack(context, player, ogre, center);
			assertTrue(left.isDestroyed());
			assertTrue(center.isDestroyed());
			assertTrue(right.isDestroyed());
		});
	}

	// ==================== CAMO ====================

	@Test
	public void testSpellNoQuarter() {
		runGym((context, player, opponent) -> {
			Card spell = receiveCard(context, player, "spell_no_quarter");
			assertEquals(6, costOf(context, player, spell));
			Minion m1 = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion target = playMinionCard(context, opponent, "minion_test_4_5");
			destroy(context, m1);
			assertEquals(5, costOf(context, player, spell));
			playCard(context, player, spell, target);
			assertTrue(target.isDestroyed());
		});
	}

	@Test
	public void testSpellPowerOfThePeople() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_draw");
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_power_of_the_people");
			assertEquals(Zones.HAND, top.getZone());
		});
	}

	@Test
	public void testMinionAmalgamOfWar() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_demon_test"); // DEMON
			playMinionCard(context, player, "token_44dragon"); // DRAGON
			Minion amalgam = playMinionCard(context, player, "minion_amalgam_of_war");
			assertEquals(6, amalgam.getAttack());
			assertEquals(6, amalgam.getHp());
		});
	}

	@Test
	public void testWeaponDreamscapeScythe() {
		runGym((context, player, opponent) -> {
			Minion demon = playMinionCard(context, player, "minion_bellydrummer");
			Minion dragon = playMinionCard(context, player, "token_44dragon");
			playCard(context, player, "weapon_dreamscape_scythe");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(6, demon.getAttack());
			assertEquals(7, dragon.getAttack());
		});
	}

	// ==================== CANDY ====================

	@Test
	public void testMinionSpotlightHog() {
		runGym((context, player, opponent) -> {
			Minion hog = playMinionCard(context, player, "minion_spotlight_hog");
			assertFalse(hog.hasAttribute(Attribute.AURA_TAUNT));
			assertFalse(hog.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
			playCard(context, player, "spell_practice", hog);
			assertEquals(5, hog.getAttack());
			assertTrue(hog.hasAttribute(Attribute.AURA_TAUNT));
			assertTrue(hog.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
		}, HeroClass.CANDY, HeroClass.CANDY);
	}

	@Test
	public void testSpellLivingFirework() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_frozen_pulse"); // cost 2
			receiveCard(context, player, "token_river_crowdcolisk"); // cost 2
			playCard(context, player, "spell_living_firework");
			assertEquals(1, player.getMinions().size());
			assertEquals("token_river_crowdcolisk", player.getMinions().get(0).getSourceCard().getCardId());
		}, HeroClass.CANDY, HeroClass.CANDY);
	}

	@Test
	public void testSpellStageDive() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_stage_dive", yeti);
			assertEquals(7, yeti.getAttack());
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
			context.endTurn();
			assertEquals(4, yeti.getAttack());
			assertFalse(yeti.hasAttribute(Attribute.IMMUNE));
		}, HeroClass.CANDY, HeroClass.CANDY);
	}

	@Test
	public void testWeaponDancersTorch() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_lunstone");
			playCard(context, player, "weapon_dancer's_torch");
			playCard(context, player, "spell_lunstone");
			assertEquals(1, player.getWeaponZone().get(0).getAttributeValue(Attribute.SPELL_DAMAGE));
		}, HeroClass.CANDY, HeroClass.CANDY);
	}

	// ==================== COPPER ====================

	@Test
	public void testMinionInspiringSentry() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_inspiring_sentry");
			player.setMana(10);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(Zones.HAND, top.getZone());
		});
	}

	@Test
	public void testSpellPiercingShot() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // cost 6
			playCard(context, player, "spell_piercing_shot");
			assertTrue(ogre.isDestroyed());
			assertEquals(24, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellReloading() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			playCard(context, player, "spell_reloading");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellSheriffsBadge() {
		runGym((context, player, opponent) -> {
			Minion chosen = playMinionCard(context, player, "minion_test_4_5");
			Minion other1 = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion other2 = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_sheriffs_badge", chosen);
			assertFalse(chosen.isDestroyed());
			assertTrue(other1.isDestroyed());
			assertTrue(other2.isDestroyed());
		});
	}

	@Test
	public void testWeaponDualDaggers() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "weapon_dual_daggers");
			attack(context, player, player.getHero(), raptor);
			assertTrue(raptor.isDestroyed());
			// Base 2/4, attacks once -> 2/3, then swaps Attack and Durability -> 3/2
			assertEquals(3, player.getWeaponZone().get(0).getAttack());
			assertEquals(2, player.getWeaponZone().get(0).getDurability());
		});
	}

	// ==================== CORAL ====================

	@Test
	public void testMinionSingingArchivist() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_test_draw_cards");
			putOnTopOfDeck(context, player, "spell_frozen_pulse");
			putOnTopOfDeck(context, player, "spell_test_deal_6");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playMinionCard(context, player, "minion_singing_archivist");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testMinionWizenedInstructor() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_wizened_instructor");
			Card raptor = receiveCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "weapon_test_3_2");
			assertEquals(1, raptor.getAttributeValue(Attribute.ATTACK_BONUS));
			assertEquals(1, raptor.getAttributeValue(Attribute.HP_BONUS));
		});
	}

	@Test
	public void testSpellEncoreV3() {
		runGym((context, player, opponent) -> {
			playCard(context, player, receiveCard(context, player, "spell_lunstone"));
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_encore_v3");
			assertTrue(player.getHand().stream().anyMatch(c -> "spell_lunstone".equals(c.getCardId())));
		});
	}

	@Test
	public void testSpellTempoUp() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			playCard(context, player, "spell_tempo_up");
			assertEquals(2, costOf(context, player, fireball));
			assertEquals(2, player.getHand().size());
		});
	}

	// ==================== CRIMSON ====================

	@Test
	public void testMinionThornKnight() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			Minion knight = playMinionCard(context, player, "minion_thorn_knight");
			destroy(context, knight);
			assertEquals(1, player.getMinions().size());
			assertEquals("token_thorn_captain", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testPactBuildingForces() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "pact_building_forces");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			assertEquals(0, player.getHero().getArmor());
			playCard(context, player, "spell_lunstone");
			assertEquals(7, player.getHero().getArmor());
			assertEquals(1, player.getMinions().size());
			assertEquals("token_bone_boy", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellDesperateAct() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			player.getHero().setHp(15);
			playCard(context, player, "spell_desperate_act");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellRecipeForPower() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_recipe_for_power");
			assertEquals(0, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(3, player.getHero().getArmor());
		});
	}

	@Test
	public void testMinionBlightedEnt() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			playMinionCard(context, player, "minion_blighted_ent");
			assertEquals(3, player.getMinions().size());
		});
	}

	// ==================== DARKBLUE ====================

	@Test
	public void testMinionCovetousSoul() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			Minion soul = playMinionCard(context, player, "minion_covetous_soul");
			destroy(context, soul);
			assertEquals(1, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(enemy.getId(), player.getMinions().get(0).getId());
		});
	}

	@Test
	public void testMinionFateIncarnate() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			Minion fate = playMinionCard(context, player, "minion_fate_incarnate");
			assertEquals(0, opponent.getMinions().size());
			playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			destroy(context, fate);
			assertEquals(0, opponent.getMinions().size());
		});
	}

	@Test
	public void testMinionKnowledgeFeeder() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "secret_secret_of_winter");
			putOnTopOfDeck(context, player, "secret_secret_of_spring");
			putOnTopOfDeck(context, player, "secret_secret_of_summer");
			Minion fodder = playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCardWithBattlecry(context, player, "minion_knowledge_feeder", fodder);
			assertTrue(fodder.isDestroyed());
			assertEquals(3, player.getSecrets().size());
		});
	}

	@Test
	public void testSpellDominantWill() {
		runGym((context, player, opponent) -> {
			Minion fodder = playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_dominant_will", fodder);
			assertTrue(fodder.isDestroyed());
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellRiteOfPassage() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_deathrattle");
			playCard(context, player, "spell_rite_of_passage");
			assertTrue(player.getMinions().size() >= 2);
			assertEquals(Zones.HAND, top.getZone());
		});
	}

	@Test
	public void testSpellRiteOfPromise() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_rite_of_promise");
			assertEquals(2, player.getMinions().size());
			assertEquals(24, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellSoultide() {
		runGym((context, player, opponent) -> {
			Minion spirit = playMinionCard(context, player, "minion_covetous_soul"); // SPIRIT
			destroy(context, spirit);
			assertEquals(0, player.getMinions().size());
			playCard(context, player, "spell_soultide");
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_covetous_soul", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellSplitSoul() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_split_soul", ogre);
			assertTrue(ogre.isDestroyed());
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertEquals("minion_neutral_test_6_7", c.getCardId());
				assertEquals(1, costOf(context, player, c));
			}
			Minion copy = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(1, copy.getAttack());
			assertEquals(1, copy.getHp());
		});
	}

	@Test
	public void testSpellExtractNightmare() {
		runGym((context, player, opponent) -> {
			Minion fodder = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_extract_nightmare", fodder);
			assertTrue(fodder.isDestroyed());
			assertEquals(1, enemy.getHp());
		});
	}

	@Test
	public void testSpellRiteOfPain() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_rite_of_pain");
			assertEquals(2, player.getMinions().size());
			assertEquals(5, yeti.getAttack());
		});
	}

	// ==================== DARKGREEN ====================

	@Test
	public void testMinionAstralAnomaly() {
		runGym((context, player, opponent) -> {
			Minion anomaly = playMinionCard(context, player, "minion_astral_anomaly");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_practice", anomaly);
			assertEquals(9, anomaly.getAttack());
			assertEquals(7, yeti.getAttack());
			assertEquals(7, enemy.getAttack());
		});
	}

	@Test
	public void testMinionFacelessShifter() {
		runGym((context, player, opponent) -> {
			Minion shifter = playMinionCard(context, player, "minion_faceless_shifter");
			playMinionCard(context, player, "minion_neutral_test_6_7");
			assertEquals(6, shifter.getAttack());
			assertEquals(7, shifter.getHp());
		});
	}

	@Test
	public void testMinionMercilessCorruptor() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			Minion corruptor = playMinionCard(context, player, "minion_merciless_corruptor");
			destroy(context, raptor);
			assertEquals(4, yeti.getHp());
			assertEquals(4, corruptor.getHp());
		});
	}

	@Test
	public void testMinionRootlessBrute() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_rootless_brute");
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(6, player.getDeck().size());
			for (Card c : player.getDeck()) {
				assertEquals("minion_rootless_brute", c.getCardId());
			}
		});
	}

	@Test
	public void testMinionSlumberingNurikabe() {
		runGym((context, player, opponent) -> {
			Minion nurikabe = playMinionCard(context, player, "minion_slumbering_nurikabe");
			assertTrue(nurikabe.hasAttribute(Attribute.PERMANENT));
			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			assertFalse(nurikabe.hasAttribute(Attribute.PERMANENT));
		});
	}

	@Test
	public void testMinionTentacolossus() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_tentacle");
			playMinionCard(context, player, "token_tauntentacle");
			Minion colossus = playMinionCard(context, player, "minion_tentacolossus");
			assertEquals(1, player.getMinions().size());
			assertEquals(8, colossus.getAttack());
			assertEquals(8, colossus.getHp());
		});
	}

	@Test
	public void testSpellAbduct() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_abduct", yeti);
			assertTrue(yeti.isDestroyed());
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellAwakeningTheHorror() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_awakening_the_horror");
			playCard(context, player, "minion_test_3_2_beast");
			assertEquals(0, player.getMinions().size());
			Minion second = playMinionCard(context, player, "minion_test_4_5");
			assertFalse(second.isDestroyed());
			assertEquals(10, second.getAttack());
			assertEquals(11, second.getHp());
		});
	}

	@Test
	public void testSpellCosmosRift() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_deal_1"); // 1
			putOnTopOfDeck(context, player, "minion_king_in_yellow"); // 3
			putOnTopOfDeck(context, player, "minion_giant_spider"); // 5
			playCard(context, player, "spell_cosmos_rift");
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellDeepMadness() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_deep_madness", enemy);
			assertEquals(14, enemy.getAttack());
			assertEquals(15, enemy.getHp());
			assertEquals(10, player.getHand().size());
		});
	}

	@Test
	public void testSpellGodsize() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_godsize", yeti);
			assertEquals(14, yeti.getAttack());
			assertEquals(15, yeti.getHp());
		});
	}

	@Test
	public void testSpellIsolate() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion center = playMinionCard(context, opponent, "minion_test_4_5");
			Minion right = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_isolate", center);
			assertTrue(left.isDestroyed());
			assertFalse(center.isDestroyed());
			assertTrue(right.isDestroyed());
		});
	}

	@Test
	public void testSpellMindTear() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, opponent, "minion_test_4_5");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_mind_tear");
			playCard(context, player, "spell_lunstone");
			assertTrue(player.getHand().size() >= 1);
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellPitOfKnowledge() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			// Cards added via putOnTopOfDeck do not have STARTED_IN_DECK unless set
			// Add 7 more cards so RecastWhileSpell hits hand size 10
			for (int i = 0; i < 7; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			playCard(context, player, "spell_pit_of_knowledge");
			assertEquals(10, player.getHand().size());
		});
	}

	// ==================== DARKMAGENTA ====================

	@Test
	public void testSpellAwakeningRite() {
		runGym((context, player, opponent) -> {
			player.setMana(5);
			player.setMaxMana(10);
			overrideDiscover(context, player, discoverActions -> discoverActions.get(1)); // Invoke (5) option
			playCard(context, player, "spell_awakening_rite");
			assertEquals(7, player.getMana());
		});
	}

	@Test
	public void testSpellChainSummon() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			player.setMana(8);
			player.setMaxMana(8);
			overrideDiscover(context, player, discoverActions -> discoverActions.get(1)); // Invoke (3): Two copies
			playCard(context, player, "spell_chain_summon", yeti);
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellRuneOfBanishing() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			player.setMana(10);
			player.setMaxMana(10);
			overrideDiscover(context, player, discoverActions -> discoverActions.get(1)); // Invoke (4): Destroy ALL units
			playCard(context, player, "spell_rune_of_banishing", yeti);
			assertTrue(yeti.isDestroyed());
			assertTrue(raptor.isDestroyed());
		});
	}

	// ==================== LIGHTBROWN ====================

	@Test
	public void testMinionDesolateSandstorm() {
		runGym((context, player, opponent) -> {
			Minion sandstorm = playMinionCard(context, player, "minion_desolate_sandstorm");
			receiveCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_charted_territory");
			assertEquals(5, sandstorm.getAttack());
			assertEquals(5, sandstorm.getHp());
		});
	}

	@Test
	public void testMinionTunnelWrecker() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_tunnel_wrecker");
			Card startedInDeck = putOnTopOfDeck(context, player, "minion_test_4_5");
			startedInDeck.setAttribute(Attribute.STARTED_IN_DECK);
			Card extra = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(Zones.HAND, extra.getZone());
			assertEquals(Zones.HAND, startedInDeck.getZone());
		});
	}

	@Test
	public void testSpellBlastApart() {
		runGym((context, player, opponent) -> {
			player.getHero().setAttribute(Attribute.ARMOR, 12);
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_blast_apart", yeti);
			assertEquals(2, player.getHero().getArmor());
			assertEquals(15, yeti.getHp());
		});
	}

	@Test
	public void testSpellBlastApart2() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			Minion center = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			Minion right = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playCard(context, player, "spell_blast_apart_2", center);
			assertTrue(center.isDestroyed());
			assertEquals(2, left.getHp());
			assertEquals(2, right.getHp());
		});
	}

	@Test
	public void testSpellChartedTerritory() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_charted_territory");
			assertEquals(2, player.getHand().size());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testSpellEternalTomb() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_eternal_tomb");
			assertEquals(0, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testSpellSteelSmash() {
		runGym((context, player, opponent) -> {
			player.getHero().setAttribute(Attribute.ARMOR, 5);
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_steel_smash", yeti);
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellDreamOfArmaments() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "weapon_test_3_2");
			putOnTopOfDeck(context, player, "weapon_verdant_scimitar");
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_dream_of_armaments");
			assertEquals(2, player.getHand().size());
			assertEquals(1, player.getDeck().size());
		});
	}

	// ==================== MAGENTA ====================

	@Test
	public void testMinionDraconicMagnate() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, opponent, "minion_test_4_5");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playMinionCard(context, player, "minion_draconic_magnate");
			assertTrue(opponent.getHand().stream().anyMatch(c -> "spell_gold_coin".equals(c.getCardId())));
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_test_4_5".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionGleamwingPatron() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playMinionCard(context, player, "minion_gleamwing_patron");
			assertEquals(1, player.getHand().size());
			assertTrue(player.getHand().get(0).hasAttribute(Attribute.RUSH));
		}, HeroClass.MAGENTA, HeroClass.MAGENTA);
	}

	@Test
	public void testMinionPedanticPeddler() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_king_in_yellow"); // odd cost (3)
			Card peddler = receiveCard(context, player, "minion_pedantic_peddler");
			playCard(context, player, peddler, opponent.getHero());
			assertEquals(25, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionRabbleRouser() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "spell_lunstone");
			receiveCard(context, opponent, "spell_lunstone");
			playMinionCard(context, player, "minion_rabble_rouser");
			assertEquals(3, player.getMinions().size());
			assertEquals(2L, player.getMinions().stream().filter(m -> "token_peasant".equals(m.getSourceCard().getCardId())).count());
		});
	}

	@Test
	public void testMinionStoresmaster() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			putOnTopOfDeck(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_storesmaster");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_bang", opponent.getHero());
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testSpellMarketResearch() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2_beast");
			putOnTopOfDeck(context, opponent, "minion_test_4_5");
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_market_research");
			assertEquals(4, player.getHand().size());
			assertEquals(1, player.getDeck().size());
		});
	}

	@Test
	public void testSpellPowerToThePeople() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2_beast"); // BEAST 3/2
			Minion fae = playMinionCard(context, player, "minion_glimmer_dancer"); // FAE 3/3
			Minion dragon = playMinionCard(context, player, "token_44dragon"); // DRAGON 4/4
			playCard(context, player, "spell_power_to_the_people");
			assertEquals(6, beast.getAttack());
			assertEquals(5, beast.getHp());
			assertEquals(6, fae.getAttack());
			assertEquals(6, fae.getHp());
			assertEquals(7, dragon.getAttack());
			assertEquals(7, dragon.getHp());
		});
	}

	@Test
	public void testSpellScavengedSteel() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_scavenged_steel");
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertEquals(-1, c.getAttributeValue(Attribute.HP_BONUS));
			}
		}, HeroClass.MAGENTA, HeroClass.MAGENTA);
	}

	@Test
	public void testSpellScrollSale() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_scroll_sale");
			assertEquals(1, player.getHand().size());
			assertEquals(1, opponent.getHand().size());
			assertEquals(player.getHand().get(0).getCardId(), opponent.getHand().get(0).getCardId());
		}, HeroClass.MAGENTA, HeroClass.MAGENTA);
	}

	@Test
	public void testSpellSupplyHeist() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_supply_heist");
			assertEquals(3, player.getHand().size());
			for (Card c : player.getHand()) {
				assertEquals(Math.max(0, c.getBaseManaCost() - 1), costOf(context, player, c));
			}
		}, HeroClass.MAGENTA, HeroClass.COPPER);
	}

	@Test
	public void testSpellToughCustomer() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_tough_customer", yeti);
			assertEquals(7, yeti.getAttack());
			assertEquals(8, yeti.getHp());
			assertTrue(yeti.hasAttribute(Attribute.TAUNT));
			playCard(context, player, "spell_high_note", yeti);
			assertTrue(player.getHand().stream().anyMatch(c -> "spell_gold_coin".equals(c.getCardId())));
		});
	}

	// ==================== NAVY ====================

	@Test
	public void testMinionBackstreetBlowup() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_backstreet_blowup");
			assertEquals(1, opponent.getMinions().size());
			assertEquals("token_flakefuse_bomb", opponent.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionConverseReverser() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_converse_reverser");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			context.endTurn();
			context.endTurn();
			attack(context, player, raptor, opponent.getHero());
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionCyclotron() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			player.setMana(10);
			player.setMaxMana(10);
			Card cyclotron = receiveCard(context, player, "minion_cyclotron");
			playCard(context, player, cyclotron);
			// Costs 7 out of 10 -> 3 unspent mana -> draws 3
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testMinionDeadeyedRavager() {
		runGym((context, player, opponent) -> {
			Minion ravager = playMinionCard(context, player, "minion_deadeyed_ravager");
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			context.endTurn();
			context.endTurn();
			attack(context, player, ravager, raptor);
			assertTrue(raptor.isDestroyed());
			assertEquals(22, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionFieldSupplier() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_field_supplier");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5");
			context.endTurn();
			context.endTurn();
			attack(context, player, yeti, enemy);
			assertEquals(5, yeti.getHp());
			assertEquals(1, enemy.getHp());
		});
	}

	@Test
	public void testMinionFleetingFirebug() {
		runGym((context, player, opponent) -> {
			Minion bug = playMinionCard(context, player, "minion_fleeting_firebug");
			destroy(context, bug);
			assertEquals(0, opponent.getMinions().size());
			context.endTurn();
			assertEquals(1, opponent.getMinions().size());
			assertEquals("minion_fleeting_firebug", opponent.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionGildedSwarmer() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_gilded_swarmer");
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_neutral_test_6_7".equals(m.getSourceCard().getCardId())));
			assertTrue(player.getDeck().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionLuteflayer() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_luteflayer");
			assertEquals(6, raptor.getAttack());
			assertEquals(5, raptor.getHp());
		});
	}

	@Test
	public void testMinionNagaIllusionist() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_knight");
			receiveCard(context, player, "token_knight");
			playMinionCard(context, player, "minion_naga_illusionist");
			assertEquals(3, player.getMinions().size());
			assertEquals(2L, player.getMinions().stream().filter(m -> "token_knight".equals(m.getSourceCard().getCardId())).count());
		});
	}

	@Test
	public void testMinionNightKnight() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_night_knight");
			receiveCard(context, player, "minion_test_deal_1"); // 1-cost
			player.setMana(10);
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(2, player.getMinions().size());
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_test_deal_1".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testMinionPintSizedDestroyer() {
		runGym((context, player, opponent) -> {
			Minion destroyer = playMinionCard(context, player, "minion_pint_sized_destroyer");
			playCard(context, player, "minion_test_4_5");
			assertFalse(destroyer.isDestroyed());
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testMinionSi7Traitor() {
		runGym((context, player, opponent) -> {
			Minion pirate = playMinionCard(context, player, "minion_stowaway_scoundrel"); // PIRATE
			playMinionCard(context, player, "minion_si7_traitor");
			assertTrue(pirate.hasAttribute(Attribute.AURA_IMMUNE));
		});
	}

	@Test
	public void testMinionSparkgunner() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_knight");
			receiveCard(context, player, "token_baron_protector");
			playMinionCard(context, player, "minion_sparkgunner");
			assertEquals(3, player.getMinions().size());
			assertEquals(2L, player.getMinions().stream().filter(m -> "token_baron_spark".equals(m.getSourceCard().getCardId())).count());
		});
	}

	@Test
	public void testMinionSparklefuser() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_sparklefuser");
			assertTrue(raptor.hasAttribute(Attribute.AURA_LIFESTEAL));
			assertTrue(raptor.hasAttribute(Attribute.AURA_POISONOUS));
		});
	}

	@Test
	public void testMinionWarTrebuchet() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_war_trebuchet");
			playMinionCard(context, player, "minion_test_4_5");
			assertEquals(26, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellArmyRecruits() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_army_recruits");
			assertEquals(1, player.getHand().size());
			assertEquals("token_knight", player.getHand().get(0).getCardId());
			context.endTurn();
			context.endTurn();
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellConquer() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			playCard(context, player, "spell_conquer");
			assertEquals(1, opponent.getMinions().size());
			Minion dragon = opponent.getMinions().get(0);
			destroy(context, dragon);
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellFinalDefenses() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_final_defenses");
			assertEquals(2, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_doomsday_guardian", m.getSourceCard().getCardId());
				assertEquals(0, m.getAttack());
				assertEquals(6, m.getHp());
			}
		});
	}

	@Test
	public void testSpellSelfAppoint() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_self_appoint", raptor);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellVictoryRoyale() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 7; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_victory_royale");
			assertEquals(10, player.getHand().size());
		});
	}

	@Test
	public void testWeaponBootyBayConscription() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_booty_bay_conscription");
			Minion spirit = playMinionCard(context, player, "minion_covetous_soul"); // 4/3 SPIRIT
			assertEquals(4, spirit.getHp());
		});
	}

	@Test
	public void testMinionLostLegionnaire() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_knight");
			receiveCard(context, player, "token_knight");
			playMinionCard(context, player, "minion_lost_legionnaire");
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertTrue(c.getCardId().startsWith("secret_secret_of_"));
			}
		});
	}

	// ==================== NEONGREEN ====================

	@Test
	public void testMinionFerronivore() {
		runGym((context, player, opponent) -> {
			Minion mech = playMinionCard(context, player, "minion_mech_test"); // 2/2 MECH -> 4 stats
			playMinionCardWithBattlecry(context, player, "minion_ferronivore", mech);
			assertTrue(mech.isDestroyed());
			assertEquals(26, opponent.getHero().getHp());
		});
	}

	@Test
	public void testPermanentFormlessCelerity() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			putOnTopOfDeck(context, opponent, "minion_test_3_2_beast");
			Minion perm = playMinionCard(context, player, "permanent_formless_celerity");
			assertTrue(perm.hasAttribute(Attribute.PERMANENT));
			context.endTurn();
			assertEquals(1, player.getHand().size());
			assertEquals(1, opponent.getHand().size());
			context.endTurn();
			for (int i = 0; i < 8; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			context.getLogic().endOfSequence();
			assertEquals(0, player.getMinions().size());
		});
	}

	@Test
	public void testSpellChemicalWarfare() {
		runGym((context, player, opponent) -> {
			Card raptor = receiveCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_chemical_warfare");
			assertEquals(2, raptor.getAttributeValue(Attribute.WITHER));
		});
	}

	@Test
	public void testSpellDreamOfAdaptation() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_high_note"); // 1-cost spell
			playCard(context, player, "spell_dream_of_adaptation");
			assertEquals(0, player.getHand().size());
			assertEquals("High Note", player.getHeroPowerZone().get(0).getName());
		});
	}

	@Test
	public void testSpellMAD() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_m_a_d");
			assertEquals(3, player.getAttributeValue(Attribute.SPELL_DAMAGE));
			receiveCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_high_note", opponent.getHero());
			assertEquals(25, opponent.getHero().getHp()); // 2 + 3 spell damage = 5
			assertEquals(0, player.getHand().size()); // discarded raptor
		});
	}

	// ==================== NEUTRAL ====================

	@Test
	public void testMinionBittertideCrab() {
		runGym((context, player, opponent) -> {
			opponent.getHero().setAttribute(Attribute.ARMOR, 6);
			Minion crab = playMinionCardWithBattlecry(context, player, "minion_bittertide_crab", opponent.getHero());
			assertEquals(1, opponent.getHero().getArmor());
			assertEquals(2, crab.getAttack());
			assertEquals(3, crab.getHp());
		});
	}

	@Test
	public void testMinionCostumeSeller() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_neutral_test_6_7");
			playMinionCardWithBattlecry(context, player, "minion_costume_seller", raptor);
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_neutral_test_6_7".equals(m.getSourceCard().getCardId())));
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionExpertExplorer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_expert_explorer");
			receiveCard(context, player, "minion_test_3_2_beast");
			context.endTurn();
			context.endTurn();
			assertTrue(player.getDeck().size() >= 1 || player.getHand().size() >= 2);
		});
	}

	@Test
	public void testMinionFiendishGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_fiendish_giant");
			assertEquals(10, costOf(context, player, giant));
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(4, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionGemstoneGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_gemstone_giant");
			playMinionCard(context, player, "minion_magma_hound"); // ELEMENTAL
			context.endTurn();
			context.endTurn();
			assertEquals(8, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionGentlemansGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_gentleman_s_giant");
			int before = costOf(context, player, giant);
			context.endTurn();
			context.endTurn();
			assertEquals(before - 1, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionGiantReaper() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_giant_reaper");
			assertEquals(10, costOf(context, player, giant));
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			destroy(context, raptor);
			assertEquals(9, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionHarbingerOfHope() {
		runGym((context, player, opponent) -> {
			Card leg = putOnTopOfDeck(context, player, "minion_morris");
			Minion harbinger = playMinionCard(context, player, "minion_harbinger_of_hope");
			destroy(context, harbinger);
			assertEquals(Zones.HAND, leg.getZone());
		});
	}

	@Test
	public void testMinionHolyGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_holy_giant");
			assertEquals(12, costOf(context, player, giant));
			player.getHero().setHp(20);
			playCard(context, player, "spell_test_heal_8", player.getHero());
			assertEquals(11, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionJackInTheBox() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			Minion jack = playMinionCard(context, player, "minion_jack_in_the_box");
			assertEquals(1, player.getMinions().size());
			assertEquals(1, player.getMinions().get(0).getSourceCard().getBaseManaCost());
		});
	}

	@Test
	public void testMinionMiniatureGunner() {
		runGym((context, player, opponent) -> {
			Minion gunner = playMinionCard(context, player, "minion_miniature_gunner");
			context.endTurn();
			context.endTurn();
			attack(context, player, gunner, opponent.getHero());
			context.endTurn();
			assertTrue(gunner.hasAttribute(Attribute.WINDFURY));
			context.endTurn();
			attack(context, player, gunner, opponent.getHero());
			attack(context, player, gunner, opponent.getHero());
			context.endTurn();
			assertTrue(gunner.hasAttribute(Attribute.MEGA_WINDFURY));
		});
	}

	@Test
	public void testMinionMossyGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_mossy_giant");
			assertEquals(15, costOf(context, player, giant));
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			destroy(context, raptor);
			assertEquals(14, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionRecklessGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_reckless_giant");
			assertEquals(8, costOf(context, player, giant));
			playCard(context, player, "weapon_test_3_2");
			assertEquals(7, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionSandGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_sand_giant");
			assertEquals(14, costOf(context, player, giant));
			playMinionCard(context, player, "minion_magma_hound");
			assertEquals(13, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionSouthseaGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_southsea_giant");
			assertEquals(10, costOf(context, player, giant));
			playCard(context, player, "weapon_test_3_2");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(9, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionTreeGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_tree_giant");
			assertEquals(12, costOf(context, player, giant));
			playMinionCard(context, player, "minion_beauregard_bouncer"); // TAUNT
			assertEquals(11, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionAntiqueCollector() {
		runGym((context, player, opponent) -> {
			Card yeti = receiveCard(context, player, "minion_test_4_5");
			Card ogre = receiveCard(context, player, "minion_neutral_test_6_7");
			playMinionCard(context, player, "minion_antique_collector");
			assertEquals(2, costOf(context, player, yeti));
			assertEquals(6, costOf(context, player, ogre));
		});
	}

	@Test
	public void testMinionGraciousTimekeeper() {
		runGym((context, player, opponent) -> {
			Card archer = receiveCard(context, player, "minion_test_deal_1"); // 1-cost
			assertEquals(1, costOf(context, player, archer));
			playMinionCard(context, player, "minion_gracious_timekeeper");
			assertEquals(0, costOf(context, player, archer));
		});
	}

	@Test
	public void testMinionCrewCommander() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_crew_commander", yeti);
			assertEquals(2, yeti.getHp());
		});
	}

	@Test
	public void testMinionDarkbentDrake() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			Minion drake = playMinionCard(context, player, "minion_darkbent_drake");
			assertEquals(6, drake.getAttack());
			assertEquals(6, drake.getHp());
		});
	}

	@Test
	public void testMinionDarkshardDuelist() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "minion_test_3_2_beast"); // 3/2
			Minion duelist = playMinionCard(context, player, "minion_darkshard_duelist"); // 7/7
			assertEquals(0, opponent.getHand().size());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(4, duelist.getHp());
		});
	}

	@Test
	public void testMinionGlimmerDancer() {
		runGym((context, player, opponent) -> {
			Minion selkie = playMinionCard(context, player, "minion_seeing_selkie"); // FAE 3/5
			playCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_glimmer_dancer");
			assertEquals(7, selkie.getHp());
		});
	}

	@Test
	public void testMinionKawarimiHeadbasher() {
		runGym((context, player, opponent) -> {
			Minion headbasher = playMinionCard(context, player, "minion_kawarimi_headbasher");
			destroy(context, headbasher);
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_kawarimi_headbasher".equals(c.getCardId())));
			assertEquals(1, player.getMinions().size());
			assertEquals("token_kawarimi_underling", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionKrentiaxxTyrant() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_krentiaxx_tyrant");
			assertEquals(1, player.getMinions().size());
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionOnisEye() {
		runGym((context, player, opponent) -> {
			Minion eye = playMinionCard(context, player, "minion_onis_eye");
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_practice", eye);
			context.endTurn();
			context.endTurn();
			attack(context, player, eye, opponent.getHero());
			assertEquals(3, yeti.getHp());
			assertTrue(eye.isDestroyed());
		});
	}

	@Test
	public void testMinionSlitherbow() {
		runGym((context, player, opponent) -> {
			Minion slitherbow = playMinionCard(context, player, "minion_slitherbow"); // 1/5
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3/2
			context.endTurn();
			attack(context, opponent, raptor, player.getHero());
			assertEquals(2, slitherbow.getHp());
			assertEquals(5, raptor.getHp());
		});
	}

	@Test
	public void testMinionSonorousSpectre() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, opponent, "spell_practice", yeti);
			assertEquals(7, yeti.getAttack());
			Minion spectre = playMinionCard(context, player, "minion_sonorous_spectre");
			assertEquals(4, yeti.getAttack());
			playCard(context, opponent, "spell_practice", yeti);
			assertEquals(7, yeti.getAttack());
			destroy(context, spectre);
			assertEquals(4, yeti.getAttack());
		});
	}

	@Test
	public void testMinionDawnrisenHarpy() {
		runGym((context, player, opponent) -> {
			Minion harpy = playMinionCard(context, player, "minion_dawnrisen_harpy"); // 3/4 Rush Windfury
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3/2
			attack(context, player, harpy, raptor);
			assertTrue(raptor.isDestroyed());
			assertEquals(4, harpy.getHp());
		});
	}

	@Test
	public void testMinionDreamwayWhale() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_dreamway_whale");
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertTrue(c.getCardId().startsWith("secret_secret_of_"));
			}
		});
	}

	@Test
	public void testMinionEmeraldDreamer() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_emerald_dreamer");
			context.endTurn();
			context.endTurn();
			assertEquals(1, player.getHand().size());
			assertEquals(8, player.getHand().get(0).getBaseManaCost());
		});
	}

	@Test
	public void testMinionFeralChild() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_feral_child");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_lunstone");
			assertEquals(4, raptor.getAttack());
			assertEquals(3, raptor.getHp());
		});
	}

	@Test
	public void testMinionMagelightDragon() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_magelight_dragon");
			assertEquals(3, player.getHand().size());
			for (Card c : player.getHand()) {
				assertEquals(1, c.getBaseManaCost());
			}
		});
	}

	@Test
	public void testMinionNightmareDragon() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_nightmare_dragon", yeti);
			assertEquals(7, yeti.getAttack());
			assertEquals(8, yeti.getHp());
			context.endTurn();
			context.endTurn();
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testMinionShadownestArachne() {
		runGym((context, player, opponent) -> {
			Minion arachne = playMinionCard(context, player, "minion_shadownest_arachne");
			destroy(context, arachne);
			assertEquals(3, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_spidey_egg", m.getSourceCard().getCardId());
			}
		});
	}

	// ==================== OFFWHITE ====================

	@Test
	public void testMinionEtherealChanneler() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_ethereal_channeler");
			Card yeti = receiveCard(context, player, "minion_test_4_5");
			receiveCard(context, player, "spell_lunstone");
			assertEquals(2, costOf(context, player, yeti));
			playCard(context, player, yeti);
			// Consumed the other card in hand
			assertEquals(0, player.getHand().size());
		});
	}

	@Test
	public void testMinionLunfly() {
		runGym((context, player, opponent) -> {
			Card coin = receiveCard(context, player, "spell_lunstone");
			Card yeti = receiveCard(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_lunfly");
			assertEquals(3, costOf(context, player, coin));
			assertEquals(4, costOf(context, player, yeti));
		});
	}

	@Test
	public void testMinionMistyRavager() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_misty_ravager");
			Minion enemyMinion = playMinionCard(context, opponent, "minion_test_3_2_beast");
			context.endTurn();
			playCard(context, opponent, "spell_lunstone");
			assertTrue(enemyMinion.isDestroyed());
		});
	}

	@Test
	public void testSpellTimeShred() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_time_shred");
			assertEquals(0, player.getMinions().size());
			assertEquals(1, player.getAttributeValue(Attribute.EXTRA_TURN));
		});
	}

	@Test
	public void testSpellViolentRewinding() {
		runGym((context, player, opponent) -> {
			Minion oldMinion = playMinionCard(context, opponent, "minion_test_4_5");
			context.endTurn();
			Minion lastTurnMinion = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			context.endTurn();
			playCard(context, player, "spell_violent_rewinding");
			assertFalse(oldMinion.isDestroyed());
			assertTrue(lastTurnMinion.isDestroyed());
		});
	}

	// ==================== OLIVE ====================

	@Test
	public void testSpellTallTales() {
		runGym((context, player, opponent) -> {
			playCard(context, player, receiveCard(context, player, "spell_lunstone"));
			playCard(context, player, receiveCard(context, player, "spell_lunstone"));
			playCard(context, player, receiveCard(context, player, "spell_tall_tales"));
			assertEquals(2, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_skeptic", m.getSourceCard().getCardId());
			}
		});
	}

	// ==================== PEACH ====================

	@Test
	public void testMinionKitsuneTrickster() {
		runGym((context, player, opponent) -> {
			Minion trickster = playMinionCard(context, player, "minion_kitsune_trickster");
			assertEquals(0, trickster.getAttributeValue(Attribute.AURA_SPELL_DAMAGE));
			for (int i = 0; i < 6; i++) {
				receiveCard(context, opponent, "spell_lunstone");
			}
			assertEquals(2, trickster.getAttributeValue(Attribute.AURA_SPELL_DAMAGE));
		});
	}

	@Test
	public void testMinionManaFormer() {
		runGym((context, player, opponent) -> {
			Minion manaformer = playMinionCard(context, player, "minion_mana_former");
			playCard(context, player, "spell_practice", manaformer);
			assertEquals(1, player.getHand().size());
			assertTrue(player.getHand().get(0).hasHeroClass(HeroClass.PEACH));
		});
	}

	@Test
	public void testSpellDualityDance() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_practice");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_duality_dance", yeti);
			assertEquals(7, yeti.getAttack());
			assertEquals(0, player.getDeck().size());
		});
	}

	@Test
	public void testSpellKumimarch() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_kumimarch");
			assertEquals(2, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_kumiho_spellpower", m.getSourceCard().getCardId());
				assertEquals(2, m.getAttributeValue(Attribute.SPELL_DAMAGE));
			}
		});
	}

	@Test
	public void testSpellMarkOfTwilight() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_mark_of_twilight", raptor);
			destroy(context, raptor);
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellPlayfulFires() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_playful_fires");
			player.setMana(10);
			player.setMaxMana(10);
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(1, player.getMinions().size());
			assertEquals(4, player.getMinions().get(0).getAttack());
			assertEquals(4, player.getMinions().get(0).getHp());
		});
	}

	// ==================== RUST ====================

	@Test
	public void testMinionArmoredChallenger() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_armored_challenger");
			assertEquals(1, opponent.getMinions().size());
			assertEquals("token_44dragon", opponent.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionBlazingDreadnought() {
		runGym((context, player, opponent) -> {
			Minion dread = playMinionCard(context, player, "minion_blazing_dreadnought"); // 7/7
			Minion enemy = playMinionCard(context, opponent, "minion_test_3_2_beast"); // 3/2
			context.endTurn();
			context.endTurn();
			attack(context, player, dread, enemy);
			assertTrue(enemy.isDestroyed());
			assertEquals(10, dread.getAttack());
		});
	}

	@Test
	public void testMinionDevouringDevilsaur() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playMinionCardWithBattlecry(context, player, "minion_devouring_devilsaur", ogre);
			assertTrue(ogre.isDestroyed());
			assertEquals(26, player.getHero().getHp());
		});
	}

	@Test
	public void testMinionDracomancer() {
		runGym((context, player, opponent) -> {
			Minion draco = playMinionCard(context, player, "minion_dracomancer");
			context.endTurn();
			context.endTurn();
			attack(context, player, draco, opponent.getHero());
			assertEquals(1, player.getMinions().size());
			assertEquals("token_44dragon", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionDraconicGiant() {
		runGym((context, player, opponent) -> {
			Card giant = receiveCard(context, player, "minion_draconic_giant");
			assertEquals(12, costOf(context, player, giant));
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_forewarn_blast", raptor);
			assertEquals(11, costOf(context, player, giant));
		});
	}

	@Test
	public void testMinionEvocatorLancer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_evocator_lancer");
			assertEquals(2, player.getMinions().size());
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_ebon_drake".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testMinionHeadhunter() {
		runGym((context, player, opponent) -> {
			Minion hh = playMinionCard(context, player, "minion_headhunter");
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			context.endTurn();
			context.endTurn();
			attack(context, player, hh, raptor);
			assertTrue(raptor.isDestroyed());
			assertTrue(player.getHand().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
			context.endTurn();
			assertFalse(player.getHand().stream().anyMatch(c -> "minion_test_3_2_beast".equals(c.getCardId())));
		});
	}

	@Test
	public void testMinionMagnataurBrutalist() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			Minion brutalist = playMinionCardWithBattlecry(context, player, "minion_magnataur_brutalist", ogre); // 5/5 -> takes 3 excess -> 5/2
			assertTrue(ogre.isDestroyed());
			assertEquals(2, brutalist.getHp());
		});
	}

	@Test
	public void testMinionOmegaDragon() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(10);
			playMinionCardWithBattlecry(context, player, "minion_omega_dragon", opponent.getHero());
			assertEquals(26, opponent.getHero().getHp());
		});
	}

	@Test
	public void testMinionTolVirVitalizer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_tol_vir_vitalizer");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			context.endTurn();
			context.endTurn();
			attack(context, player, yeti, raptor);
			assertEquals(5, yeti.getHp());
		});
	}

	@Test
	public void testMinionWitchwoodHarpy() {
		runGym((context, player, opponent) -> {
			Minion harpy = playMinionCard(context, player, "minion_witchwood_harpy");
			context.endTurn();
			context.endTurn();
			attack(context, player, harpy, opponent.getHero());
			assertFalse(harpy.canAttackThisTurn(context));
			playCard(context, player, "spell_lunstone");
			assertTrue(harpy.canAttackThisTurn(context));
		});
	}

	@Test
	public void testMinionWraithOfYShaarj() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test_6_7");
			Minion wraith = playMinionCard(context, player, "minion_wraith_of_y_shaarj");
			destroy(context, wraith);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellAspectAwakening() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_aspect_awakening");
			assertEquals(1, player.getMinions().size());
		});
	}

	@Test
	public void testSpellBigBadAssault() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast"); // 3/2
			playMinionCard(context, player, "minion_test_4_5"); // 4/5
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7
			playCard(context, player, "spell_big_bad_assault", ogre);
			assertTrue(ogre.isDestroyed());
		});
	}

	@Test
	public void testSpellCallOfTheAspects() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_call_of_the_aspects");
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testSpellCasinoRoyale() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			putOnTopOfDeck(context, player, "minion_test_4_5");
			Minion e1 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion e2 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			destroy(context, e1);
			destroy(context, e2);
			playCard(context, player, "spell_casino_royale");
			context.endTurn();
			assertEquals(2, player.getHand().size());
		});
	}

	@Test
	public void testSpellComeHither() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast"); // cost 2
			putOnTopOfDeck(context, player, "token_bulette"); // cost 7
			overrideDiscover(context, player, discoverActions -> discoverActions.get(0));
			playCard(context, player, "spell_come_hither");
			assertEquals(1, player.getMinions().size());
			assertEquals("token_bulette", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellDraconicInsight() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_test_deal_6");
			playCard(context, player, "spell_draconic_insight");
			playMinionCard(context, player, "minion_test_3_2_beast");
			assertEquals(1, player.getHand().size());
			assertEquals("spell_test_deal_6", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellForbiddenRoar() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			player.setMana(6);
			playCard(context, player, "spell_forbidden_roar", yeti);
			assertEquals(0, player.getMana());
			assertEquals(10, yeti.getAttack());
		});
	}

	@Test
	public void testSpellForewarnBlast() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			playCard(context, player, "spell_forewarn_blast", raptor);
			assertTrue(raptor.isDestroyed());
			assertEquals(2, costOf(context, player, fireball));
		});
	}

	@Test
	public void testSpellIgneousHatchery() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_igneous_hatchery");
			assertEquals("hero_power_raise_hatchling", player.getHeroPowerZone().get(0).getCardId());
		});
	}

	@Test
	public void testSpellManufacturing() {
		runGym((context, player, opponent) -> {
			Minion elem = playMinionCard(context, player, "minion_magma_hound"); // ELEMENTAL
			playCard(context, player, "spell_manufacturing", elem);
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testSpellPrimalRage() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, player, "minion_neutral_test_6_7"); // 6/7
			Minion enemy = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			playCard(context, player, "spell_primal_rage", ogre);
			assertTrue(enemy.isDestroyed());
		});
	}

	@Test
	public void testSpellRideTheNuke() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			Minion center = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5 -> 4 excess
			Minion right = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			playCard(context, player, "spell_ride_the_nuke", center);
			assertTrue(center.isDestroyed());
			assertEquals(1, left.getHp());
			assertEquals(1, right.getHp());
		});
	}

	@Test
	public void testSpellUnitedRetaliation() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			context.endTurn();
			context.endTurn();
			receiveCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_united_retaliation");
			attack(context, player, raptor, opponent.getHero());
			assertEquals(2, player.getMinions().size());
			Minion ogre = player.getMinions().get(1);
			assertEquals("minion_neutral_test_6_7", ogre.getSourceCard().getCardId());
			assertTrue(ogre.hasAttribute(Attribute.RUSH));
		});
	}

	@Test
	public void testWeaponAnemicCannon() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_anemic_cannon");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(1, yeti.getAttack());
		});
	}

	// ==================== TOAST ====================

	@Test
	public void testMinionExpeditionCook() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_magma_hound"); // ELEMENTAL
			playMinionCard(context, player, "minion_expedition_cook");
			assertEquals(1, player.getDeck().size());
			assertEquals("spell_elemental_soup", player.getDeck().get(0).getCardId());
		});
	}

	@Test
	public void testMinionHeadOfTheCaravan() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_head_of_the_caravan");
			assertEquals(2, player.getMinions().size());
			Minion head = player.getMinions().stream().filter(m -> "minion_head_of_the_caravan".equals(m.getSourceCard().getCardId())).findFirst().orElseThrow();
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_supply_caravan".equals(m.getSourceCard().getCardId())));
			destroy(context, head);
			assertEquals(2L, player.getMinions().stream().filter(m -> "minion_supply_caravan".equals(m.getSourceCard().getCardId())).count());
		});
	}

	@Test
	public void testMinionIronforgeBaker() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test_6_7");
			playMinionCard(context, player, "minion_ironforge_baker");
			assertEquals(0, player.getHand().size());
			assertEquals(1, player.getDeck().size());
			assertEquals(0, costOf(context, player, player.getDeck().get(0)));
		});
	}

	@Test
	public void testMinionMeatConstruct() {
		runGym((context, player, opponent) -> {
			Minion construct = playMinionCard(context, player, "minion_meat_construct");
			destroy(context, construct);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_meat_construct", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testMinionOmeletteChef() {
		runGym((context, player, opponent) -> {
			Minion egg = playMinionCard(context, opponent, "token_spidey_egg"); // 0/2 with Deathrattle
			playMinionCardWithBattlecry(context, player, "minion_omelette_chef", egg);
			assertTrue(egg.isDestroyed());
			assertEquals(0, opponent.getMinions().size()); // silenced before destroy
		});
	}

	@Test
	public void testMinionStarvingMyrmidon() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playMinionCardWithBattlecry(context, player, "minion_starving_myrmidon", yeti);
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellHauteCuisine() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast"); // 2
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7"); // 6
			playCard(context, player, "spell_haute_cuisine");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_neutral_test_6_7", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellPartyNight() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			playCard(context, player, "spell_party_night");
			assertEquals(10, player.getHand().size());
		});
	}

	@Test
	public void testSpellTopChef() {
		runGym((context, player, opponent) -> {
			Card ogre = putOnTopOfDeck(context, player, "minion_neutral_test_6_7"); // cost 6 -> drawn
			playCard(context, player, "spell_top_chef");
			assertEquals(Zones.HAND, ogre.getZone());
			Card raptor = putOnTopOfDeck(context, player, "minion_test_3_2_beast"); // cost 2 -> roasted
			playCard(context, player, "spell_top_chef");
			assertTrue(raptor.hasAttribute(Attribute.ROASTED));
		});
	}

	@Test
	public void testWeaponMeatHook() {
		runGym((context, player, opponent) -> {
			receiveCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "weapon_meat_hook");
			assertEquals(1, opponent.getMinions().size());
			assertTrue(opponent.getMinions().get(0).hasAttribute(Attribute.TAUNT));
		});
	}

	// ==================== TWILIGHT ====================

	@Test
	public void testMinionLividLearner() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion learner = playMinionCard(context, player, "minion_livid_learner");
			Minion hoarder = playMinionCard(context, player, "minion_test_deathrattle");
			destroy(context, hoarder);
			assertEquals(Zones.HAND, top.getZone());
			Card top2 = putOnTopOfDeck(context, player, "minion_test_4_5");
			destroy(context, learner);
			assertEquals(Zones.HAND, top2.getZone());
		});
	}

	@Test
	public void testMinionManicRitualist() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			destroy(context, yeti);
			playMinionCard(context, player, "minion_manic_ritualist");
			assertEquals(2, player.getMinions().size());
			Minion revived = player.getMinions().stream().filter(m -> "minion_test_4_5".equals(m.getSourceCard().getCardId())).findFirst().orElseThrow();
			assertEquals(1, revived.getHp());
		});
	}

	@Test
	public void testMinionMindMelter() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5 -> 4/4 -> swapped 4/4
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // 6/7 -> 6/6 -> swapped 6/6
			Minion shieldmasta = playMinionCard(context, opponent, "minion_corona_fae_defender"); // 3/5 -> 3/4 -> swapped 4/3
			playMinionCard(context, player, "minion_mind_melter");
			assertEquals(4, shieldmasta.getAttack());
			assertEquals(3, shieldmasta.getHp());
		});
	}

	@Test
	public void testMinionShacklebreaker() {
		runGym((context, player, opponent) -> {
			Minion shackle = playMinionCard(context, player, "minion_shacklebreaker");
			assertFalse(shackle.hasAttribute(Attribute.AURA_CHARGE));
			player.getHero().setHp(20);
			playCard(context, player, "spell_lunstone");
			assertTrue(shackle.hasAttribute(Attribute.AURA_CHARGE));
		});
	}

	@Test
	public void testMinionStormshade() {
		runGym((context, player, opponent) -> {
			assertEquals("hero_power_hemophage", player.getHeroPowerZone().get(0).getCardId());
			Minion stormshade = playMinionCard(context, player, "minion_stormshade");
			assertEquals("token_hero_power_stormshade", player.getHeroPowerZone().get(0).getCardId());
			destroy(context, stormshade);
			assertEquals("hero_power_hemophage", player.getHeroPowerZone().get(0).getCardId());
		}, HeroClass.TWILIGHT, HeroClass.TWILIGHT);
	}

	@Test
	public void testMinionVeinWyrm() {
		runGym((context, player, opponent) -> {
			Card wyrm = receiveCard(context, player, "minion_vein_wyrm");
			assertEquals(12, costOf(context, player, wyrm));
			playCard(context, player, "spell_test_heal_8", player.getHero()); // 8 excess healing
			assertEquals(4, costOf(context, player, wyrm));
		});
	}

	@Test
	public void testSpellCrimsonGift() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_crimson_gift", yeti);
			assertEquals(10, yeti.getHp());
		});
	}

	@Test
	public void testSpellDarkSkies() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_test_deal_6"); // 4-cost
			playCard(context, player, "spell_dark_skies");
			assertEquals(1, player.getHand().size());
			Card fb = player.getHand().get(0);
			assertEquals(1, costOf(context, player, fb));
			playCard(context, player, fb, opponent.getHero());
			assertEquals(27, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellGrimAwakeningRework() {
		runGym((context, player, opponent) -> {
			Minion dead = playMinionCard(context, player, "minion_test_3_2_beast");
			destroy(context, dead);
			Minion alive = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_grim_awakening_rework");
			assertTrue(alive.hasAttribute(Attribute.LIFESTEAL));
			assertTrue(dead.hasAttribute(Attribute.LIFESTEAL));
		});
	}

	// ==================== EGGPLANT ====================

	@Test
	public void testMinionCorruptForerunner() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "token_44dragon");
			receiveCard(context, player, "token_44dragon");
			player.setMana(10);
			player.setMaxMana(10);
			Card forerunner = receiveCard(context, player, "minion_corrupt_forerunner");
			playCard(context, player, forerunner);
			assertEquals(3, player.getMinions().size());
		});
	}

	@Test
	public void testMinionCrowdcodile() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_4_5");
			player.setMana(10);
			player.setMaxMana(10);
			Card croc = receiveCard(context, player, "minion_crowdcodile");
			playCard(context, player, croc);
			assertEquals(5, player.getMinions().size());
		});
	}

	@Test
	public void testMinionCurvehornGuardian() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_curvehorn_guardian");
			playMinionCard(context, player, "minion_test_3_2_beast");
			assertEquals(3, player.getMinions().size());
			assertEquals(3, player.getMinions().get(2).getAttack());
			assertEquals(3, player.getMinions().get(2).getHp());
		});
	}

	@Test
	public void testSecretIsolate() {
		runGym((context, player, opponent) -> {
			Minion neighbor = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "secret_isolate");
			context.endTurn();
			playMinionCard(context, opponent, "minion_neutral_test_6_7");
			assertEquals(0, player.getSecrets().size());
			assertTrue(neighbor.isDestroyed());
		});
	}

	@Test
	public void testSpellMonologue() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_monologue");
			assertFalse(yeti.isDestroyed());
			context.endTurn();
			context.endTurn();
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellPlagueOfRats() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_plague_of_rats");
			assertEquals(2, player.getMinions().size());
			for (Minion m : player.getMinions()) {
				assertEquals("token_plague_rat", m.getSourceCard().getCardId());
			}
		});
	}

	@Test
	public void testSpellPotionOfCreation() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			player.setMaxMana(10);
			Card potion = receiveCard(context, player, "spell_potion_of_creation");
			playCard(context, player, potion);
			assertEquals(1, player.getMinions().size());
			assertEquals(8, player.getMinions().get(0).getSourceCard().getBaseManaCost());
		});
	}

	@Test
	public void testSpellVolatileInfusion() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_volatile_infusion");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_high_note", yeti);
			assertEquals(2, player.getMinions().size());
			assertTrue(player.getMinions().stream().anyMatch(m -> "minion_neutral_test_6_7".equals(m.getSourceCard().getCardId())));
		});
	}

	// ==================== ICE ====================

	@Test
	public void testSecretAntiMagicZone() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "secret_anti_magic_zone");
			context.endTurn();
			playCard(context, opponent, "spell_lunstone");
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testSpellDeathStrike() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "weapon_test_3_2"); // 3 Attack
			playCard(context, player, "spell_death_strike", ogre); // 6 damage with Lifesteal
			assertEquals(1, ogre.getHp());
			assertEquals(26, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellLingeringApparition() {
		runGym((context, player, opponent) -> {
			Minion ally = playMinionCard(context, player, "minion_test_4_5");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_lingering_apparition", enemy);
			assertFalse(ally.hasAttribute(Attribute.AURA_IMMUNE));
			context.endTurn();
			assertTrue(ally.hasAttribute(Attribute.AURA_IMMUNE));
		});
	}

	@Test
	public void testSpellRime() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion center = playMinionCard(context, opponent, "minion_test_4_5");
			Minion right = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_rime", center);
			context.endTurn();
			assertEquals(3, center.getHp());
			assertTrue(left.hasAttribute(Attribute.FROZEN));
			assertTrue(right.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testSpellWinterIsComing() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5
			playCard(context, player, "spell_frozen_pulse", yeti); // 4/5 Frozen
			playCard(context, player, "spell_winter_is_coming", yeti);
			assertTrue(yeti.isDestroyed());
			assertEquals(25, opponent.getHero().getHp());
		});
	}

	@Test
	public void testWeaponBloodcakedAxe() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_bloodcaked_axe");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(2, player.getHero().getArmor());
		});
	}

	@Test
	public void testWeaponEbonBladeGreatsword() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "weapon_ebon_blade_greatsword");
			assertEquals(2, player.getWeaponZone().get(0).getAttack());
			playCard(context, player, "spell_rune", raptor);
			assertEquals(3, player.getWeaponZone().get(0).getAttack());
		});
	}

	// ==================== LEATHER ====================

	@Test
	public void testMinionRotorbladeChopper() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_4_5");
			Minion center = playMinionCard(context, opponent, "minion_test_4_5");
			Minion right = playMinionCard(context, opponent, "minion_test_4_5");
			Minion chopper = playMinionCard(context, player, "minion_rotorblade_chopper");
			attack(context, player, chopper, center);
			assertTrue(left.isDestroyed());
			assertTrue(center.isDestroyed());
			assertTrue(right.isDestroyed());
		});
	}

	@Test
	public void testSecretHiddenTrap() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "secret_hidden_trap");
			context.endTurn();
			playCard(context, opponent, "spell_lunstone");
			playCard(context, opponent, "spell_lunstone");
			assertEquals(0, player.getSecrets().size());
			context.endTurn();
			playCard(context, player, "spell_extermination_team");
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellDefensivePosition() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_defensive_position", yeti);
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
			assertEquals(Zones.HAND, top.getZone());
			context.endTurn();
			assertFalse(yeti.hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testSpellExterminationTeam() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_revelating_gas", yeti);
			playCard(context, player, "spell_extermination_team");
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellNewArrival() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "secret_hidden_trap");
			putOnTopOfDeck(context, player, "secret_anti_magic_zone");
			playCard(context, player, "spell_new_arrival");
			assertEquals(2, player.getSecrets().size());
		});
	}

	// ==================== OBSIDIAN ====================

	@Test
	public void testMinionCryognome() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_frozen_pulse", yeti);
			playMinionCard(context, player, "minion_cryognome");
			assertEquals(1, player.getDeck().size());
			assertEquals("minion_test_4_5", player.getDeck().get(0).getCardId());
		});
	}

	@Test
	public void testMinionFrostyMiner() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			Minion miner = playMinionCard(context, player, "minion_frosty_miner");
			miner.setAttribute(Attribute.FROZEN);
			destroy(context, miner);
			assertEquals(Zones.HAND, top.getZone());
		});
	}

	@Test
	public void testSpellCollectibleDoomPact() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			putOnTopOfDeck(context, player, "minion_test_4_5");
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_collectible_doom_pact");
			assertEquals(0, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, player.getDeck().size());
		});
	}

	@Test
	public void testSpellPermafrost() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_permafrost", yeti);
			assertTrue(yeti.hasAttribute(Attribute.FROZEN));
			assertTrue(yeti.hasAttribute(Attribute.FREEZES_PERMANENTLY));
		});
	}

	@Test
	public void testSpellPowerForAPrice() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_power_for_a_price", yeti);
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
			context.endTurn();
			context.endTurn();
			assertFalse(yeti.hasAttribute(Attribute.IMMUNE));
			assertTrue(yeti.hasAttribute(Attribute.SILENCED));
		});
	}

	@Test
	public void testSpellSixFeetUnderDk() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			playCard(context, player, "spell_six_feet_under_dk");
			assertEquals(0, player.getDeck().size());
			assertEquals(6, player.getHand().size());
		});
	}

	// ==================== ROSE ====================

	@Test
	public void testMinionEvilExcavatedEntity() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_evil_excavated_entity");
			assertEquals(1, player.getDeck().size());
			assertEquals("spell_excavated_curse", player.getDeck().get(0).getCardId());
		});
	}

	@Test
	public void testMinionPackmother() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "token_river_crowdcolisk"); // 2-cost Beast
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast"); // 2-cost Beast
			playMinionCardWithBattlecry(context, player, "minion_packmother", raptor);
			assertEquals(3, player.getMinions().size());
			assertTrue(player.getMinions().stream().anyMatch(m -> "token_river_crowdcolisk".equals(m.getSourceCard().getCardId())));
		});
	}

	@Test
	public void testMinionResourcefulHoarder() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_test_4_5");
			playMinionCard(context, player, "minion_resourceful_hoarder");
			assertEquals(4, player.getHand().size());
		});
	}

	@Test
	public void testMinionShimmerscale() {
		runGym((context, player, opponent) -> {
			Minion shimmerscale = playMinionCard(context, player, "minion_shimmerscale");
			assertTrue(shimmerscale.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(shimmerscale.hasAttribute(Attribute.STEALTH));
			assertTrue(shimmerscale.hasAttribute(Attribute.UNTARGETABLE_BY_OPPONENT_SPELLS));
		});
	}

	@Test
	public void testMinionStoryCollector() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_story_collector");
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertTrue(c.hasHeroClass("ROSE"));
			}
		});
	}

	@Test
	public void testMinionTantalizingPeacock() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			Minion peacock = playMinionCard(context, player, "minion_tantalizing_peacock");
			destroy(context, peacock);
			assertEquals(0, opponent.getMinions().size());
			assertEquals(1, opponent.getDeck().size());
			assertEquals("minion_test_4_5", opponent.getDeck().get(0).getCardId());
		});
	}

	@Test
	public void testMinionVitalityThrower() {
		runGym((context, player, opponent) -> {
			Minion ogre = playMinionCard(context, player, "minion_neutral_test_6_7"); // 6/7
			ogre.setHp(1);
			playMinionCardWithBattlecry(context, player, "minion_vitality_thrower", ogre); // heals 5 -> 6/7 (still damaged) -> Attack 10
			assertEquals(6, ogre.getHp());
			assertEquals(10, ogre.getAttack());
		});
	}

	@Test
	public void testSecretDevilsDeal() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_devils_deal");
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			context.endTurn();
			attack(context, opponent, raptor, player.getHero());
			assertEquals(32, player.getHero().getHp()); // 30 + 5 - 3
		});
	}

	@Test
	public void testSpellAlterEgo() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5"); // 4/5 -> 4/3 -> 3/4
			playCard(context, player, "spell_alter_ego");
			assertEquals(3, yeti.getAttack());
			assertEquals(4, yeti.getHp());
		});
	}

	@Test
	public void testSpellBerserk() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5"); // 4/5 -> +3/+3 = 7/8
			Minion e1 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion e2 = playMinionCard(context, opponent, "minion_test_3_2_beast");
			playCard(context, player, "spell_berserk", yeti);
			assertTrue(e1.isDestroyed());
			assertTrue(e2.isDestroyed());
			assertEquals(2, yeti.getHp());
		});
	}

	@Test
	public void testSpellCouncilMeeting() {
		runGym((context, player, opponent) -> {
			Card meeting = receiveCard(context, player, "spell_council_meeting");
			receiveCard(context, player, "spell_lunstone");
			receiveCard(context, player, "spell_lunstone");
			assertEquals(6, costOf(context, player, meeting));
			player.getHero().setHp(20);
			playCard(context, player, meeting, player.getHero());
			assertEquals(28, player.getHero().getHp());
		});
	}

	@Test
	public void testSpellCursedMask() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_cursed_mask", raptor);
			destroy(context, raptor);
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testSpellRageAscension() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_rage_ascension");
			assertEquals(2, player.getHand().size());
			for (Card c : player.getHand()) {
				assertEquals("token_rage_behemoth", c.getCardId());
			}
		});
	}

	@Test
	public void testSpellRottenCurse() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_rotten_curse", yeti);
			assertEquals(1, yeti.getHp());
			assertTrue(yeti.hasAttribute(Attribute.IMMUNE));
		});
	}

	@Test
	public void testSpellUnearthlyHaunting() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test_6_7");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_unearthly_haunting", raptor);
			destroy(context, raptor);
			assertEquals(1, player.getMinions().size());
			assertEquals("minion_neutral_test_6_7", player.getMinions().get(0).getSourceCard().getCardId());
		});
	}

	@Test
	public void testSpellWickedInsight() {
		runGym((context, player, opponent) -> {
			Card raptor = putOnTopOfDeck(context, player, "minion_test_3_2_beast"); // 2-cost
			playCard(context, player, "spell_wicked_insight");
			assertEquals(28, player.getHero().getHp());
			assertEquals(Zones.HAND, raptor.getZone());
		});
	}

	@Test
	public void testWeaponHexcarver() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_hexcarver"); // 2/8
			player.setMana(0);
			Card fb = receiveCard(context, player, "spell_test_deal_6"); // cost 4
			playCard(context, player, fb, opponent.getHero());
			assertEquals(24, opponent.getHero().getHp());
			assertEquals(4, player.getWeaponZone().get(0).getDurability());
		});
	}

	@Test
	public void testWeaponSpiritbinder() {
		runGym((context, player, opponent) -> {
			Card fb = receiveCard(context, player, "spell_test_deal_6");
			playCard(context, player, "weapon_spiritbinder");
			assertEquals(2, costOf(context, player, fb));
		});
	}

	@Test
	public void testMinionTormentaxxHulk() {
		runGym((context, player, opponent) -> {
			Minion hulk = playMinionCard(context, player, "minion_tormentaxx_hulk"); // 7/12
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast"); // cost 2
			context.endTurn();
			context.endTurn();
			attack(context, player, hulk, raptor);
			assertTrue(raptor.isDestroyed());
			assertEquals(5, hulk.getAttack());
		});
	}

	@Test
	public void testSpellRecurringNightmare() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Card yeti = receiveCard(context, opponent, "minion_test_4_5");
			opponent.setMana(10);
			opponent.setMaxMana(10);
			context.getLogic().performGameAction(opponent.getId(), yeti.play());
			context.endTurn();
			receiveCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_recurring_nightmare");
			assertEquals(1, player.getHand().size());
			assertEquals("minion_test_4_5", player.getHand().get(0).getCardId());
		});
	}

	// ==================== TEAL ====================

	@Test
	public void testMinionSteamvaultBruiser() {
		runGym((context, player, opponent) -> {
			// Deck has 0 cards -> Reservoir (0-15) -> +4 Health and Guard (5/8 Taunt)
			Minion bruiser = playMinionCard(context, player, "minion_steamvault_bruiser");
			assertEquals(8, bruiser.getHp());
			assertTrue(bruiser.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testSpellTheyCameFromBelow() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			}
			Minion ogre = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_they_came_from_below", ogre);
			assertTrue(ogre.isDestroyed());
		});
	}

	@Test
	public void testWeaponBrineCrossbolter() {
		runGym((context, player, opponent) -> {
			// Deck has 0 cards -> Reservoir (0-5) -> doubles Attack from 5 to 10
			playCard(context, player, "weapon_brine_crossbolter");
			assertEquals(10, player.getWeaponZone().get(0).getAttack());
		});
	}

	// ==================== TIME ====================

	@Test
	public void testMinionMetalicManadrake() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			player.setMaxMana(10);
			Card drake = receiveCard(context, player, "minion_metalic_manadrake"); // cost 7 -> 3 unspent mana
			playCard(context, player, drake);
			context.endTurn();
			assertEquals(27, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellAstralShockwave() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_4_5"); // takes 1 (adj) + 1 (enemy minion) + 1 (enemy char) = 3 -> 2 HP
			Minion center = playMinionCard(context, opponent, "minion_neutral_test_6_7"); // takes 1 + 1 + 1 + 1 = 4 -> 3 HP
			playCard(context, player, "spell_astral_shockwave", center);
			assertEquals(3, center.getHp());
			assertEquals(2, left.getHp());
			assertEquals(29, opponent.getHero().getHp());
		});
	}

	@Test
	public void testSpellBandOfBrothers() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_band_of_brothers");
			assertEquals(4, player.getHand().size());
		});
	}

	@Test
	public void testSpellLoneSurvivor() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, opponent, "minion_test_3_2_beast");
			Minion center = playMinionCard(context, opponent, "minion_test_4_5");
			Minion right = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			playCard(context, player, "spell_lone_survivor", center);
			assertTrue(left.isDestroyed());
			assertFalse(center.isDestroyed());
			assertTrue(right.isDestroyed());
		});
	}

	@Test
	public void testSpellFillTheStands() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_fill_the_stands");
			assertEquals(4, player.getHand().size());
		});
	}

	@Test
	public void testSpellSkyfall() {
		runGym((context, player, opponent) -> {
			Card skyfall = receiveCard(context, player, "spell_skyfall");
			assertEquals(9, costOf(context, player, skyfall));
			playCard(context, player, "spell_lunstone");
			assertEquals(8, costOf(context, player, skyfall));
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, skyfall, yeti);
			assertTrue(yeti.isDestroyed());
		});
	}

	@Test
	public void testMinionThunderclapPhoenix() {
		runGym((context, player, opponent) -> {
			Card phoenix = receiveCard(context, player, "minion_thunderclap_phoenix");
			assertEquals(8, costOf(context, player, phoenix));
			playCard(context, player, "spell_lunstone");
			assertEquals(7, costOf(context, player, phoenix));
			playMinionCard(context, player, phoenix);
			assertTrue(player.getHand().stream().anyMatch(c -> "spell_lightning_strike".equals(c.getCardId())));
		});
	}

	@Test
	public void testSpellChaoticSpellstorm() {
		runGym((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2_beast");
			receiveCard(context, player, "minion_test_4_5");
			receiveCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_chaotic_spellstorm");
			assertTrue(raptor.isDestroyed());
			assertNotEquals("minion_test_4_5", player.getHand().get(0).getCardId());
			assertNotEquals("minion_test_4_5", opponent.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testSpellRequiem() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "token_river_crowdcolisk");
			playCard(context, player, "spell_requiem");
			assertEquals(0, opponent.getMinions().size());
			assertEquals(2, player.getMinions().size());
		});
	}

	@Test
	public void testSpellBloodmoonRitual() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, player, "minion_test_3_2_beast");
			playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_bloodmoon_ritual");
			assertEquals(0, player.getMinions().size());
			assertEquals(0, opponent.getMinions().size());
		});
	}

	@Test
	public void testSpellNewRecruits() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_gold_coin");
			playCard(context, player, "spell_new_recruits");
			assertEquals(0, player.getHand().size());
			assertEquals(5, player.getMinions().size());
		});
	}

	@Test
	public void testSpellRatPlague() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_rat_plague");
			assertEquals(1, player.getHand().size());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testMinionOrganDonor() {
		runGym((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5"); // 4/5
			Minion donor = playMinionCard(context, player, "minion_organ_donor"); // 4/3
			destroy(context, donor);
			assertEquals(8, yeti.getAttack());
			assertEquals(8, yeti.getHp());
		});
	}

	@Test
	public void testMinionMonumancer() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_monumancer");
			player.setMana(10);
			player.setMaxMana(10);
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(1, player.getDeck().size());
		});
	}

	@Test
	public void testMinionUmbralTempest() {
		runGym((context, player, opponent) -> {
			Minion tempest = playMinionCard(context, player, "minion_umbral tempest"); // 6/5
			context.endTurn();
			assertEquals(5, tempest.getAttack());
			assertEquals(6, tempest.getHp());
		});
	}

	@Test
	public void testSpellSupplySabotage() {
		runGym((context, player, opponent) -> {
			Card top = putOnTopOfDeck(context, player, "minion_test_3_2_beast");
			playCard(context, player, "spell_supply_sabotage");
			assertEquals(Zones.HAND, top.getZone());
			assertEquals(2, opponent.getDeck().size());
		});
	}
}
