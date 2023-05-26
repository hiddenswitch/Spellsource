package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.Attribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class AllianceSpellTests extends TestBase {
	@Test
	public void testLastMinionDestroyedBattlecrySummon() {
		GameContext context = createContext("BLUE", "RED");
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		Card card = context.getCardCatalogue().getCardById("minion_sourcing_specialist");
		card.setAttribute(Attribute.LAST_MINION_DESTROYED_CARD_ID, "minion_neutral_test");

		playCard(context, mage, card);

		assertTrue(mage.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_neutral_test")));
	}
}
