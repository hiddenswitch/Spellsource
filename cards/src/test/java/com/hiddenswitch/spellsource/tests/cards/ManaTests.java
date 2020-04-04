package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class ManaTests extends TestBase {

	@Test
	public void testDarnassusAspirant() {
		GameContext context = createContext("BROWN", "RED");
		Player player = context.getPlayer1();
		int playerId = player.getId();

		context.getLogic().startTurn(playerId);
		player.setMana(4);
		player.setMaxMana(4);
		assertEquals(player.getMana(), 4);
		assertEquals(player.getMaxMana(), 4);
		playCard(context, player, "minion_mana_chap");
		assertEquals(player.getMana(), 2);
		assertEquals(player.getMaxMana(), 5);
		playCard(context, player, "minion_end_reveler");
		assertEquals(player.getMana(), 1);
		assertEquals(player.getMaxMana(), 5);
		context.getLogic().endTurn(playerId);

		// start turn - Doomsayer triggers and kills Darnassus Aspirant
		context.getLogic().startTurn(playerId);
		// player should loose a full mana crystal in this case
		assertEquals(player.getMana(), 5);
		assertEquals(player.getMaxMana(), 5);
	}

}
