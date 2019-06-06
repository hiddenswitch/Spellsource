package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;


public class TestBehaviours extends TestBase {
	@Test
	public static void testValidActionsFilterForDamagingSelf() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_shock_0");
			context.getPlayer2().getHero().setHp(5);
			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			List<GameAction> actions = context.getValidActions();
			int originalSize = actions.size();
			behaviour.filterActions(player, actions);
			assertTrue(originalSize - 1 == actions.size());
		});
	}

	@Test
	public static void testValidBattlecryActionsFilterForDamagingSelf() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			overrideBattlecry(context, player, battlecryActions -> {
				int originalSize = battlecryActions.size();
				List<GameAction> actions = battlecryActions.stream().map(battlecryAction -> (GameAction) battlecryAction).collect(Collectors.toList());
				behaviour.filterActions(player, actions);
				assertTrue(originalSize - 1 == actions.size());
				return battlecryActions.get(0);
			});
			playCard(context, player, "minion_with_damage_3");
		});
	}

	@Test
	public static void testValidBattlecryActionsFilterForDamagingOwnMinions() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_with_damage_3");
			receiveCard(context, player, "minion_stat_3");
			playCard(context, player, "minion_stat_3");

			PlayRandomWithoutSelfDamageBehaviour behaviour = new PlayRandomWithoutSelfDamageBehaviour();
			behaviour.ownMinionTargetingIsEnabled(false);
			overrideBattlecry(context, player, battlecryActions -> {
				int originalSize = battlecryActions.size();
				List<GameAction> actions = battlecryActions.stream().map(battlecryAction -> (GameAction) battlecryAction).collect(Collectors.toList());
				behaviour.filterActions(player, actions);
				assertTrue(originalSize - 2 == actions.size());
				return battlecryActions.get(0);
			});
			playCard(context, player, "minion_with_damage_3");
		});
	}
}
