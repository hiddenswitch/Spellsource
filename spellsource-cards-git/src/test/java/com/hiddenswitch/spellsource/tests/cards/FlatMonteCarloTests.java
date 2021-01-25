package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.GameContext;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.behaviour.FlatMonteCarloBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.statistics.SimulationResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlatMonteCarloTests extends TestBase {

	@Test
	@Disabled("test too slow for unused functionality")
	public void testDecksFlatMonteCarlo() {
		GameDeck druid = DeckCreateRequest.fromDeckList("### Druid\n" +
				"# Class: White\n" +
				"# Format: Standard\n" +
				"# Year of the Mammoth\n" +
				"#\n" +
				"# 2x (1) Innervate\n" +
				"# 2x (1) Claw\n" +
				"# 2x (2) Acidic Swamp Ooze\n" +
				"# 2x (2) Bloodfen Raptor\n" +
				"# 1x (2) Kobold Geomancer\n" +
				"# 1x (2) Mark of the Wild\n" +
				"# 2x (2) Wild Growth\n" +
				"# 2x (3) Ironfur Grizzly\n" +
				"# 2x (3) Shattered Sun Cleric\n" +
				"# 2x (4) Chillwind Yeti\n" +
				"# 2x (4) Gnomish Inventor\n" +
				"# 2x (4) Sen'jin Shieldmasta\n" +
				"# 2x (4) Swipe\n" +
				"# 2x (6) Starfire\n" +
				"# 2x (6) Boulderfist Ogre\n" +
				"# 2x (8) Ironbark Protector\n" +
				"#").toGameDeck();

		GameDeck warrior = DeckCreateRequest.fromDeckList("### Warrior\n" +
				"# Class: White\n" +
				"# Format: Standard\n" +
				"# Year of the Mammoth\n" +
				"#\n" +
				"# 1x (1) Whirlwind\n" +
				"# 2x (2) Cleave\n" +
				"# 1x (2) Execute\n" +
				"# 2x (2) Heroic Strike\n" +
				"# 2x (2) Acidic Swamp Ooze\n" +
				"# 2x (2) Bloodfen Raptor\n" +
				"# 2x (3) Fiery War Axe\n" +
				"# 2x (3) Shield Block\n" +
				"# 2x (3) Ironfur Grizzly\n" +
				"# 2x (3) Shattered Sun Cleric\n" +
				"# 2x (4) Chillwind Yeti\n" +
				"# 2x (4) Gnomish Inventor\n" +
				"# 2x (4) Sen'jin Shieldmasta\n" +
				"# 2x (4) Kor'kron Elite\n" +
				"# 2x (5) Arcanite Reaper\n" +
				"# 2x (6) Boulderfist Ogre\n" +
				"#").toGameDeck();
		final AtomicInteger matchCounter = new AtomicInteger();
		SimulationResult result = GameContext.simulate(Arrays.asList(druid, warrior), FlatMonteCarloBehaviour::new, PlayRandomBehaviour::new, 10, true, matchCounter);
		assertEquals(result.getNumberOfGames(), 10);
	}

	@Test
	@Disabled("test too slow for unused functionality")
	public void testFlatMonteCarlo() {
		runGym((context, player, opponent) -> {
			Card winTheGame = receiveCard(context, player, "spell_win_the_game");
			FlatMonteCarloBehaviour behaviour = new FlatMonteCarloBehaviour(10);
			List<GameAction> validActions = context.getValidActions();
			GameAction playCardAction = behaviour.requestAction(context, player, validActions);
			assertEquals(playCardAction.getActionType(), ActionType.SPELL);
			assertEquals(((PlayCardAction) playCardAction).getSourceReference(), winTheGame.getReference());
		});
	}
}
