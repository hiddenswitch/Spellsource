package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.behaviour.FlatMonteCarlo;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class FlatMonteCarloTests extends TestBase {
	@Test
	public void testFlatMonteCarlo() {
		runGym((context, player, opponent) -> {
			Card winTheGame = receiveCard(context, player, "spell_win_the_game");
			FlatMonteCarlo behaviour = new FlatMonteCarlo(10);
			List<GameAction> validActions = context.getValidActions();
			GameAction playCardAction = behaviour.requestAction(context, player, validActions);
			Assert.assertEquals(playCardAction.getActionType(), ActionType.SPELL);
			Assert.assertEquals(((PlayCardAction) playCardAction).getEntityReference(), winTheGame.getReference());
		});
	}
}
