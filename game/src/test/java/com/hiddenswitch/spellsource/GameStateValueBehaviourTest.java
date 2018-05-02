package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.shared.threat.GameStateValueBehaviour;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class GameStateValueBehaviourTest extends TestBase {
	@Test
	public void testDestroyAt2HP() {
		runGym((context, player, opponent) -> {
			Card fireball = receiveCard(context, player, "spell_fireball");
			player.setMana(4);
			player.setMaxMana(4);
			opponent.getHero().setHp(2);
			List<GameAction> actions = context.getValidActions();
			Assert.assertTrue(actions.stream().anyMatch(ga -> fireball.getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to cast the Fireball on the opponent's hero");
			Assert.assertTrue(actions.stream().anyMatch(ga -> player.getHero().getHeroPower().getReference().equals(ga.getSourceReference())
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
	public void testAIWillNeverPlayCursed() {
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
	public void testAIWillPlayIntoDoomsayer() {
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
