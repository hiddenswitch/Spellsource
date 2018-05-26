package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.shared.threat.GameStateValueBehaviour;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class GameStateValueBehaviourTest extends TestBase implements Serializable {

	@Test
	public void testPlaysWildGrowth() {
		runGym((context, player, opponent) -> {
			// Should play wild growth
			receiveCard(context, player, "spell_wild_growth");
			receiveCard(context, player, "minion_pompous_thespian");
			player.setMana(2);
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getMaxMana(), 2);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			playMinionCard(context, opponent, "minion_bloodfen_raptor");
			// should not play wild growth
			context.endTurn();
			// Set hp to yellow threat
			player.getHero().setHp(17);
			receiveCard(context, player, "spell_wild_growth");
			receiveCard(context, player, "minion_pompous_thespian");
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			// Note: this is turn 2 for the player
			assertEquals(player.getMaxMana(), 2);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_pompous_thespian");
		});
	}

	@Test
	public void testBrannSpiteful() {
		runGym((context, player, opponent) -> {
			Card brann = receiveCard(context, player, "minion_brann_bronzebeard");
			Card spiteful = receiveCard(context, player, "minion_spiteful_summoner");
			putOnTopOfDeck(context, player, "spell_ultimate_infestation");
			player.setMana(10);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			List<GameAction> actions = new ArrayList<>();
			for (int i = 0; i < 2; i++) {
				GameAction chosen = behaviour.requestAction(context, player, context.getValidActions());
				actions.add(chosen);
				context.getLogic().performGameAction(player.getId(), chosen);
			}
			Assert.assertEquals(actions.get(0).getSourceReference(), brann.getReference());
			Assert.assertEquals(actions.get(1).getSourceReference(), spiteful.getReference());
		});
	}

	@Test
	public void testAIExploresDiscoversForWinning() {
		runGym((context, player, opponent) -> {

			// There is a branch of discover actions that wins the bot the game.
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());
			receiveCard(context, player, "spell_cascading_discovers");

			while (context.takeActionInTurn()) {
			}

			assertTrue(context.updateAndGetGameOver());
		});
	}

	@Test
	public void testCorrectOrder() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, opponent, "minion_bloodfen_raptor");
			opponent.getHero().setHp(4);
			// Your hero power is, Equip a 1/1 Weapon (costs 2)
			Minion cannotAttack = playMinionCard(context, player, "minion_1_1_hero_power");
			// Whenever this minion is targeted by a battlecry, gain 10,000 HP
			Minion gains = playMinionCard(context, player, "minion_gains_huge_hp");
			player.setMana(6);
			// Cost 3, Give a minion +1/+1
			receiveCard(context, player, "minion_shattered_sun_cleric");
			// Cost 1, Has Charge while you have a weapon equipped
			receiveCard(context, player, "minion_southsea_deckhand");
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			// Should equip weapon, play Southsea, then play Shattered Sun Cleric, battlecry target Southsea, then attack with
			// southsea, then attack with hero
			// Southsea MUST attack before weapon attacks
			// Sun cleric MUST be played after southsea and should NOT buff a minion that gains a huge amount of hp
			// This is a depth 5 puzzle.
			Assert.assertNull(player.getHero().getWeapon());
			assertTrue(context.updateAndGetGameOver());
		}, HeroClass.BLACK, HeroClass.BLACK);
	}

	@Test
	public void testDestroyAt2HP() {
		runGym((context, player, opponent) -> {
			Card fireball = receiveCard(context, player, "spell_fireball");
			player.setMana(4);
			player.setMaxMana(4);
			opponent.getHero().setHp(2);
			List<GameAction> actions = context.getValidActions();
			assertTrue(actions.stream().anyMatch(ga -> fireball.getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to cast the Fireball on the opponent's hero");
			assertTrue(actions.stream().anyMatch(ga -> player.getHero().getHeroPower().getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to Fireblast on the opponent's hero");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction chosen = behaviour.requestAction(context, player, actions);
			Assert.assertEquals(chosen.getSourceReference(), fireball.getReference(), "The AI should have chosen to play the Fireball");
			Assert.assertEquals(chosen.getTargetReference(), opponent.getHero().getReference(), "The AI should have chosen to cast it against the enemy hero.");
		}, /*Set hero class to mage for fireblast hero power*/ HeroClass.BLUE, HeroClass.BLUE);
	}


	@Test
	public void testAIWillPlayQuests() {
		runGym((context, player, opponent) -> {
			Card powerTrip = receiveCard(context, player, "spell_power_trip");
			receiveCard(context, player, "minion_voidwalker");
			// The AI loves Taunts
			receiveCard(context, player, "spell_mirror_image");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, player, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.SPELL);
			Assert.assertEquals(((PlayCardAction) action).getEntityReference(), powerTrip.getReference());
		});
	}

	@Test
	public void testAIWillPlayCursed() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			opponent.setMana(2);
			opponent.setMaxMana(2);
			receiveCard(context, opponent, "spell_cursed");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.SPELL);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			opponent.setMana(4);
			opponent.setMaxMana(4);
			Card cursed = receiveCard(context, opponent, "spell_cursed");
			receiveCard(context, opponent, "spell_fireball");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			context.getLogic().performGameAction(opponent.getId(), action);
			action = behaviour.requestAction(context, opponent, context.getValidActions());
			context.getLogic().performGameAction(opponent.getId(), action);
			Assert.assertFalse(opponent.getHand().contains(cursed));
		});
	}

	@Test
	public void testAIWillNotPlayIntoDoomsayer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doomsayer");
			context.endTurn();
			receiveCard(context, opponent, "minion_snowflipper_penguin");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.END_TURN);
		});

		runGym((context, player, opponent) -> {
			Minion doomsayer = playMinionCard(context, player, "minion_doomsayer");
			context.endTurn();
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, opponent, "minion_wolfrider");
			}
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.PHYSICAL_ATTACK);
			Assert.assertEquals(action.getTargetReference(), doomsayer.getReference());
		});

		runGym((context, player, opponent) -> {
			Minion doomsayer = playMinionCard(context, player, "minion_doomsayer");
			context.endTurn();
			playMinionCard(context, opponent, "minion_kobold_geomancer");
			receiveCard(context, opponent, "spell_fireball");
			opponent.setMaxMana(4);
			opponent.setMana(4);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.SPELL);
			Assert.assertEquals(action.getTargetReference(), doomsayer.getReference());
		});
	}

	@Test
	public void testAIWillPlayIntoSnakeTrap() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_snake_trap");
			Minion targetDummy = playMinionCard(context, player, "minion_target_dummy");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, opponent, "minion_wolfrider");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			Assert.assertEquals(action.getActionType(), ActionType.PHYSICAL_ATTACK);
			Assert.assertEquals(action.getTargetReference(), targetDummy.getReference());
			Assert.assertEquals(action.getSourceReference(), wolfrider.getReference());
		});
	}
}
