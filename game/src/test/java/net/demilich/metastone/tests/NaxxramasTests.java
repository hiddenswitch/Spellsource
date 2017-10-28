package net.demilich.metastone.tests;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NaxxramasTests extends TestBase {
	/**
	 * Your opponent plays a Mad Scientist then you play Stampeding Kodo. First, the Kodo enters the board, then during
	 * its Battlecry Phase the Mad Scientist is marked pending destroy. After the Battlecry Phase ends, a Death Phase
	 * begins where the Mad Scientist's Deathrattle puts Mirror Entity into play. We now proceed to the After Play
	 * Phase, where Mirror Entity is Queued and resolved, creating a copy of the Kodo.
	 */
	@Test
	public void testMadScientist() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			context.getLogic().shuffleToDeck(opponent, CardCatalogue.getCardById("secret_mirror_entity"));
			playMinionCard(context, opponent, "minion_mad_scientist");
			context.endTurn();
			playMinionCard(context, player, "minion_stampeding_kodo");
			Assert.assertEquals(opponent.getMinions().size(), 1);
			Assert.assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), "minion_stampeding_kodo");
		});

		/*
		  Alternatively, if you play a minion and a Knife Juggler triggers, killing the enemy Mad Scientist and putting
		  Mirror Entity into play, the Secret does NOT trigger. This is because the After Play Phase has already
		  passed.
		 */

		runGym((context, player, opponent) -> {
			context.endTurn();
			context.getLogic().shuffleToDeck(opponent, CardCatalogue.getCardById("secret_mirror_entity"));
			Minion madScientist = playMinionCard(context, opponent, "minion_mad_scientist");
			madScientist.setHp(1);
			context.endTurn();
			Minion knifeJuggler = playMinionCard(context, player, "minion_knife_juggler");
			overrideMissilesTrigger(context, knifeJuggler, madScientist);
			playMinionCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}
}
