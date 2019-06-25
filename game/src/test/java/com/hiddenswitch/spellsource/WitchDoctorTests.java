package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.testng.Assert.*;

public class WitchDoctorTests extends TestBase {

	@Test
	public void testGurubashiBloodletter() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_gurubashi_bloodletter");
			context.endTurn();
			playCard(context, player, "spell_2_missiles");
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "token_rage_behemoth");
			assertEquals(target.transformResolved(context), opponent.getMinions().get(0));
		});
	}

	@Test
	public void testHexlordZixxis() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "spell_mirror_image");
			context.endTurn();
			context.endTurn();
			playCard(context, opponent, "spell_mind_blast");
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
			playCard(context, player, "spell_test_cost_3_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 1);
			playCard(context, player, "spell_test_cost_6_buff", thelia);
			assertEquals(thelia.getAttack(), thelia.getBaseAttack() + 3);
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
			Card played1 = receiveCard(context, player, "spell_the_coin");
			Card notPlayed1 = receiveCard(context, player, "minion_test_mech");
			Card played2 = receiveCard(context, opponent, "minion_neutral_test");
			Card notPlayed2 = receiveCard(context, opponent, "minion_test_mech");
			playCard(context, player, "minion_neutral_test");
			context.endTurn();
			playCard(context, opponent, "spell_the_coin");
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
	public void testDobyMick() {
		runGym((context, player, opponent) -> {
			Minion test1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_doby_mick");
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
		runGym((context, player, opponent) -> {
			Card coin = receiveCard(context, player, "spell_the_coin");
			assertEquals(costOf(context, player, coin), 0);
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, coin), 0);
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, coin), 0);
			playCard(context, player, "spell_the_coin");
			playCard(context, player, "minion_witching_traveler");
			assertEquals(costOf(context, player, coin), 1);
		});
	}
}
