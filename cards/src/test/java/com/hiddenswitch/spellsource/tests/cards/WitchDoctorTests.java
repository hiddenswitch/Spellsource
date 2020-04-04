package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Actor;

import net.demilich.metastone.game.entities.minions.Minion;

import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class WitchDoctorTests extends TestBase {

	@Test
	public void testUshibashiVersusEldritchampion() {
		runGym((context, player, opponent) -> {
			// Extremely sensitive to order of play here
			playCard(context, player, "minion_elven_woundsealer");
			playCard(context, player, "minion_eldritchampion");
			context.endTurn();
			playCard(context, opponent, "minion_ushibasu_the_vigilant");
			Minion target = playMinionCard(context, opponent, 1, 7);
			context.endTurn();
			assertEquals(context.getActivePlayerId(), player.getId());
			playCard(context, player, "spell_blood_cleave", target);
		});
	}

	@Test
	public void testHotheadedVillager() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_hotheaded_villager");
			context.endTurn();
			playCard(context, player, "spell_2_missiles");
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "token_rage_behemoth");
			assertEquals(target.transformResolved(context), opponent.getMinions().get(0));
		});
	}

	@Test
	public void testMariAnetteAuraInteraction() {
		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, player, "minion_dragon_test");
			playMinionCard(context, player, "minion_irena_dragon_knight");
			assertEquals(dragon.getAttack(), dragon.getBaseAttack() * 2);
			playCard(context, player, "minion_mari_anette");
			context.endTurn();
			context.endTurn();
			context.endTurn();
		});
	}

	@Test
	public void testHexlordZixxis() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "spell_test_summon_tokens");
			context.endTurn();
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			context.endTurn();
			player.getHero().setAttribute(Attribute.IMMUNE);
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "minion_hexlord_zixxis");
			assertEquals(opponent.getHero().getHp(), opponentHp - 5);
			assertEquals(player.getMinions().size(), 1, "No Mirror Images");
		});
	}

	@Test
	public void testTheliaSilentdreamer() {
		runGym((context, player, opponent) -> {
			Minion thelia = playMinionCard(context, player, "minion_thelia_silentdreamer");
			Minion shouldNotDouble = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Minion shouldNotDouble2 = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			playCard(context, player, "spell_test_cost_3_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 1, "thelia but less than 5, so should not double");
			playCard(context, player, "spell_test_cost_6_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 3, "thelia and greater than 5, so should double");
			playCard(context, player, "spell_test_cost_6_buff", shouldNotDouble);
			assertEquals(shouldNotDouble.getBaseAttack() + 1, shouldNotDouble.getAttack(), "not Thelia so should not double");
			context.endTurn();
			playCard(context, opponent, "spell_test_cost_6_buff", shouldNotDouble2);
			assertEquals(shouldNotDouble2.getBaseAttack() + 1, shouldNotDouble2.getAttack(), "not friendly and not Thelia so should not double");
			playCard(context, opponent, "spell_test_cost_6_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 4, "thelia and greater than 5 but not friendly, so should not double");
		});
	}

	@Test
	public void testSpiritualDiffusion() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target3 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target4 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion shouldNotBeDestroyed = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_spiritual_diffusion");

			assertEquals(opponent.getMinions().size(), 4);
			assertEquals(opponent.getMinions().stream().filter(c -> c.getSourceCard().getCardId().equals("token_voodoo_spirit")).count(), 3L);
			assertFalse(shouldNotBeDestroyed.isDestroyed());
			assertEquals(shouldNotBeDestroyed.getSourceCard().getCardId(), "minion_neutral_test");
			assertEquals(Stream.of(target1, target2, target3, target4).filter(Actor::isDestroyed).count(), 3L);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion shouldNotBeDestroyed = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_spiritual_diffusion");
			assertEquals(opponent.getMinions().size(), 2);
			assertEquals(opponent.getMinions().stream()
					.filter(c -> c.getSourceCard().getCardId().equals("token_voodoo_spirit")).count(), 2L);
			assertFalse(shouldNotBeDestroyed.isDestroyed());
			assertEquals(shouldNotBeDestroyed.getSourceCard().getCardId(), "minion_neutral_test");
			assertEquals(Stream.of(target1, target2).filter(Actor::isDestroyed).count(), 2L);
		});
	}

	@Test
	public void testDreamshaker() {
		runGym((context, player, opponent) -> {
			Card played1 = receiveCard(context, player, "spell_test_gain_mana");
			Card notPlayed1 = receiveCard(context, player, "minion_test_mech");
			Card played2 = receiveCard(context, opponent, "minion_neutral_test");
			Card notPlayed2 = receiveCard(context, opponent, "minion_test_mech");
			playCard(context, player, "minion_neutral_test");
			context.endTurn();
			playCard(context, opponent, "spell_test_gain_mana");
			context.endTurn();
			playCard(context, player, "minion_dreamshaker");
			assertEquals(played1.getZone(), Zones.GRAVEYARD);
			assertTrue(played1.hasAttribute(Attribute.DISCARDED));
			assertEquals(played2.getZone(), Zones.GRAVEYARD);
			assertTrue(played2.hasAttribute(Attribute.DISCARDED));
			assertEquals(notPlayed1.getZone(), Zones.HAND);
			assertEquals(notPlayed2.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testTaintedBloodPupeteerSenzaku() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "hero_puppeteer_senzaku");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, charger, target);
			assertEquals(player.getMinions().size(), 0, "Should not summon charger because not Puppeteer's turn.");
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "hero_puppeteer_senzaku");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			context.endTurn();
			attack(context, player, target, charger);
			assertEquals(player.getMinions().size(), 1, "Should summon charger because Puppeteer's turn.");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_charge_test", "Should summon charger because Puppeteer's turn.");
		});
	}

	@Test
	public void testGaithaTheProtector() {
		runGym((context, player, opponent) -> {
			Minion test1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_gaitha_the_protector");
			playCard(context, player, "spell_test_deal_6", test1);
			assertEquals(test1.getHp(), 1);
			Minion test2 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_deal_6", test2);
			assertEquals(test2.getHp(), 1);
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", test2);
			assertEquals(test2.getHp(), 1);
			Minion shouldBeDestroyed = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, opponent, "spell_test_deal_6", shouldBeDestroyed);
			assertTrue(shouldBeDestroyed.isDestroyed());
			// Summon a minion for the opponent, and assert it cannot be destroyed
			playCard(context, opponent, "spell_summon_for_opponent");
			Minion test3 = player.getMinions().get(3);
			assertNotEquals(test1, test3);
			assertNotEquals(test2, test3);
			playCard(context, opponent, "spell_test_deal_6", test3);
			assertEquals(test3.getHp(), 1);
			context.endTurn();
			for (Minion minion : new Minion[]{test1, test2, test3}) {
				playCard(context, player, "spell_test_deal_6", minion);
				assertTrue(minion.isDestroyed());
			}
		});
	}

	@Test
	public void testDoctosaur() {
		runGym((context, player, opponent) -> {
			Minion doctosaur = playMinionCard(context, player, "minion_doctosaur");
			Minion mech = playMinionCard(context, player, "minion_test_mech");
			Minion notMech = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "weapon_test_3_2");
			int startingHp = opponent.getHero().getHp();
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(opponent.getHero().getHp(), startingHp - 3 /*weapon damange*/ - mech.getAttack());
		});
	}

	@Test
	public void testMindControlledMech() {
		runGym((context, player, opponent) -> {
			Minion mech1 = playMinionCard(context, player, "minion_mind_controlled_mech");
			Minion mech2 = playMinionCard(context, player, "minion_mind_controlled_mech");
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_mental_command");
			int startingHp = opponent.getHero().getHp();
			useHeroPower(context, player, opponent.getHero().getReference());
			assertEquals(opponent.getHero().getHp(), startingHp - mech1.getAttack() - mech2.getAttack());
			destroy(context, mech1);
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_mental_command");
		});
	}

	@Test
	public void testWitchingTraveler() {
		int SET_COST = 0;
		runGym((context, player, opponent) -> {
			Card spell = receiveCard(context, player, "spell_test_heal_8");
			assertEquals(costOf(context, player, spell), spell.getBaseManaCost());
			playCard(context, player, "spell_test_gain_mana");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, spell), spell.getBaseManaCost());
			playCard(context, player, "spell_test_gain_mana");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, spell), spell.getBaseManaCost());
			playCard(context, player, "spell_test_gain_mana");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, spell), SET_COST);
		});
	}

	// Secretive Chanter: "Opener: Transform all Voodoo spells in your hand into Emerald Secrets."
	@Test
	public void testSecretiveChanter() {
		runGym((context, player, opponent) -> {
			Card v1 = receiveCard(context, player, "spell_divination");
			Card v2 = receiveCard(context, player, "spell_frenzy");
			Card v3 = receiveCard(context, player, "spell_hex_bolt");
			Card v4 = receiveCard(context, player, "spell_spirit_bind");
			Card v5 = receiveCard(context, player, "spell_spirit_bind");
			Card nonv = receiveCard(context, player, "spell_test_deal_6");
			playMinionCard(context, player, "minion_secretive_chanter");
			// Check whether all Voodoo spells have been transformed
			assertTrue(player.getHand().get(0).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(1).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(2).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(3).getCardId().contains("secret_secret_of"));
			assertTrue(player.getHand().get(4).getCardId().contains("secret_secret_of"));
			assertEquals(player.getHand().get(5).getCardId(), "spell_test_deal_6");
		});
	}

	// High Shaman Mawliki: "Your spells have Toxic. Your healing is doubled. Your other minions have Elusive and Guard."
	@Test
	public void testHighShamanMawliki() {
		runGym((context, player, opponent) -> {
			Minion friend1 = playMinionCard(context, player, "minion_neutral_test");
			Minion friend2 = playMinionCard(context, player, "minion_neutral_test_big");
			playMinionCard(context, player, "minion_high_shaman_mawliki");
			// Check for guard and elusive
			assertTrue(friend1.hasAttribute(Attribute.AURA_TAUNT));
			assertTrue(friend2.hasAttribute(Attribute.AURA_TAUNT));
			assertTrue(friend1.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
			assertTrue(friend2.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_armageddon_wyvern");
			Minion enemy2 = playMinionCard(context, opponent, "minion_armageddon_wyvern");
			Minion enemy3 = playMinionCard(context, opponent, "minion_armageddon_wyvern");
			context.endTurn();
			// Check for toxic spells
			playCard(context, player, "spell_test_deal_6", enemy1);
			playCard(context, player, "spell_test_deal_6", enemy2);
			assertTrue(enemy1.isDestroyed());
			assertTrue(enemy2.isDestroyed());
			assertEquals(opponent.getMinions().size(), 1);
			context.endTurn();
			attack(context, opponent, enemy3, friend2);
			assertEquals(friend2.getHp(), 10);
			context.endTurn();
			// Check for double healing effect
			playCard(context, player, "spell_test_heal_8", friend2);
			assertEquals(friend2.getHp(), 20);
		});
	}

}