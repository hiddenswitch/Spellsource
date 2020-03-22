package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OutlawTests extends TestBase {

	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.COPPER;
	}

	@Test
	public void testSpookyTurret() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				putOnTopOfDeck(context, player, "spell_lunstone");
			}
			Minion spookyTurret = playMinionCard(context, player, "minion_spooky_turret");
			context.endTurn();
			context.endTurn();
			assertTrue(spookyTurret.isDestroyed());
			assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				putOnTopOfDeck(context, player, "spell_lunstone");
			}
			Minion spookyTurret1 = playMinionCard(context, player, "minion_spooky_turret");
			Minion spookyTurret2 = playMinionCard(context, player, "minion_spooky_turret");
			context.endTurn();
			context.endTurn();
			assertTrue(spookyTurret1.isDestroyed());
			assertTrue(spookyTurret2.isDestroyed());
			assertEquals(player.getHand().size(), 5);
		});
	}

	// Shootout - 3 Mana Spell Free "Deal $2 damage to three random enemy minions."
	@Test
	public void testShootout() {
		runGym((context, player, opponent) -> {
			Card shootOut = receiveCard(context, player, "spell_shootout");

			context.endTurn();
			playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();

			player.setMana(3);
			assertFalse(shootOut.canBeCast(context, player));

			context.endTurn();
			playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();

			player.setMana(3);
			assertTrue(shootOut.canBeCast(context, player));

			playCard(context, player, shootOut);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	// Trigger Happy - 1 Mana Spell Common "Choose a minion. Whenever it attacks, draw a card."
	@Test
	public void testTriggerHappy() {
		runGym((context, player, opponent) -> {
			Minion guy = playMinionCard(context, player, "minion_beauregard_bouncer");

			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_trigger_happy", guy);
			assertEquals(player.getHand().size(), 0);

			attack(context, player, guy, opponent.getHero());

			assertEquals(player.getHand().size(), 1);

			attack(context, player, guy, opponent.getHero());

			assertEquals(player.getHand().size(), 2);
		});
	}

	// Cheating Wrangler 3 Mana 4/3 Common "Quick Draw: Deal 1 damage to the enemy champion."
	@Test
	public void testCheatingWrangler() {
		runGym((context, player, opponent) -> {
			Minion cheatingWrangler = playMinionCard(context, player, "minion_cheating_wrangler");
			assertTrue(cheatingWrangler.hasAttribute(Attribute.QUICK_DRAW));

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(opponent.getHero().getHp(), 30);

			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(opponent.getHero().getHp(), 29);


			context.getLogic().drawCard(player.getId(), null);
			assertEquals(opponent.getHero().getHp(), 28);

		});
	}

	// Bullet with your Name - 3 Mana Spell Common "Give all friendly minions 'Quick Draw: Deal 1 damage to the enemy champion.'"
	@Test
	public void testBulletWithYourName() {
		runGym((context, player, opponent) -> {
			Minion testMinion = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_bullet_with_your_name");
			assertTrue(testMinion.hasAttribute(Attribute.QUICK_DRAW));

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(opponent.getHero().getHp(), 30);

			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(opponent.getHero().getHp(), 29);

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(opponent.getHero().getHp(), 28);
		});
	}

	// Crate of Dynamite - 2 Mana Spell Common "Give a minion 'Quick Draw: Deal 1 damage to all enemy minions.'"
	@Test
	public void testCrateOfDynamite() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (var i = 0; i < 5; i++) {
				playCard(context, opponent, "minion_neutral_test_1");
			}
			context.endTurn();
			Minion testMinion = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_crate_of_dynamite", testMinion);

			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}
			assertEquals(opponent.getMinions().size(), 5);

			context.getLogic().drawCard(player.getId(), null);
			context.getLogic().endOfSequence();

			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	// Death Blow - 2 Mana Spell Rare "Deal $2 damage to a random enemy minion. If it dies, draw 2 cards."
	@Test
	public void testDeathBlow() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion testMinion = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			playCard(context, player, "spell_death_blow");
			assertTrue(testMinion.isDestroyed());
			assertEquals(player.getHand().size(), 2);
		});
	}

	// Six-Shooter - 5 Mana 1/6 Weapon Rare "Quick Draw: Deal 3 damage to a random enemy minion and lose 1 Durability."
	@Test
	public void testSixShooter() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (var i = 0; i < 7; i++) {
				playCard(context, opponent, "minion_neutral_test_1");
			}

			context.endTurn();
			playCard(context, player, "weapon_six_shooter");

			for (int i = 0; i < 7; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			for (int i = 1; i < 6; i++) {
				context.getLogic().drawCard(player.getId(), null);
				context.getLogic().endOfSequence();
				assertEquals(opponent.getMinions().size(), 7 - i);
				assertEquals(player.getWeaponZone().get(0).getHp(), 6 - i);
			}

		});
	}

	// McGrief - 4 Mana 5/4 Legendary "After you play a Quick Draw card, draw a card."
	@Test
	public void testMcGrief() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 7; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}
			playMinionCard(context, player, "minion_mcgrief");
			playMinionCard(context, player, "minion_cheating_wrangler");
			context.getLogic().endOfSequence();
			assertEquals(opponent.getHero().getHp(), 29);
		});
	}

	// Hired Gunsmith - 1 Mana 2/1 Common "Opener: The next weapon you play this turn costs (2) less."
	@Test
	public void testHiredGunsmith() {
		runGym((context, player, opponent) -> {
			Card weapon1 = receiveCard(context, player, "weapon_test_3_2");
			Card nonWeapon = receiveCard(context, player, "minion_neutral_test");

			playCard(context, player, "minion_hired_gunsmith");

			assertEquals(costOf(context, player, weapon1), weapon1.getBaseManaCost() - 2);
			assertEquals(costOf(context, player, nonWeapon), nonWeapon.getBaseManaCost());


			Card weapon2 = receiveCard(context, player, "weapon_test_3_2");
			assertEquals(costOf(context, player, weapon2), weapon2.getBaseManaCost() - 2);
		});
	}

	// Plan Ahead 1 Mana Spell Rare "Look at the top three cards of your deck. Draw one and put the others at the bottom of your deck."
	@Test
	public void testPlanAhead() {
		runGym((context, player, opponent) -> {
			Stream.of("minion_neutral_test_1", "minion_neutral_test", "minion_neutral_test_14", "minion_test_3_2",
					"minion_test_3_2_beast", "minion_test_3_2_fae", "minion_test_4_5", "minion_test_deflect",
					"minion_test_deathrattle", "minion_test_mech").forEach(s -> shuffleToDeck(context, player, s));

			assertEquals(player.getDeck().size(), 10);

			String[] cardIds = new String[3];

			overrideDiscover(context, player, discoverActions -> {
				for (int i = 0; i < 3; i++) {
					cardIds[i] = discoverActions.get(i).getCard().getCardId();
				}
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_plan_ahead");

			assertEquals(player.getDeck().size(), 9);

			assertEquals(player.getHand().get(0).getCardId(), cardIds[0]);

			if (player.getDeck().get(0).getCardId().equals(cardIds[1])) {
				assertEquals(player.getDeck().get(1).getCardId(), cardIds[2]);
			} else {
				assertEquals(player.getDeck().get(1).getCardId(), cardIds[1]);
				assertEquals(player.getDeck().get(0).getCardId(), cardIds[2]);
			}

		});
	}

	// Mechanical Golem - 5 Mana 3/6 Rare Mech "Dash. Quick Draw: Attack a random enemy minion."
	@Test
	public void testMechanicalGolem() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (var i = 0; i < 5; i++) {
				playCard(context, opponent, "minion_neutral_test_1");
			}
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}
			Minion mechGolem = playMinionCard(context, player, "minion_mechanical_golem");
			for (int i = 1; i < 6; i++) {
				context.getLogic().drawCard(player.getId(), null);
				context.getLogic().endOfSequence();
				assertEquals(opponent.getMinions().size(), 5 - i);
				assertEquals(mechGolem.getHp(), 6 - i);
			}
		});
	}

	// Mass Betrayal - 6 Mana Spell Epic "Force all enemy minions to attack another random enemy minion."
	@Test
	public void testMassBetrayal() {
		runGym((context, player, opponent) -> {

			context.endTurn();
			for (int i = 0; i < 6; i++) {
				shuffleToDeck(context, opponent, "minion_neutral_test_1");
				Minion testMinion = playMinionCard(context, opponent, "minion_neutral_test_1");
				playCard(context, opponent, "spell_trigger_happy", testMinion);
			}
			context.endTurn();
			playCard(context, player, "spell_mass_betrayal");
			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(opponent.getHand().size(), 3);
		});
	}

	// Captain Karver - 8 Mana 7/7 Legendary "Opener: Give your weapon 'Aftermath: Resummon Captain Karver'."
	@Test
	public void testCaptainKarver() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_test_3_2");
			playCard(context, player, "minion_captain_karver");
			assertEquals(player.getMinions().size(), 1);

			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getMinions().size(), 1);

			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getMinions().size(), 2);
		});
	}
}
