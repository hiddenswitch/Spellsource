package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by bberman on 3/17/17.
 */
public class AllianceSpellTests extends TestBase {
	@Test
	public void testLastMinionDestroyedBattlecrySummon() {
		GameContext context = createContext(HeroClass.BLUE, HeroClass.RED);
		Player mage = context.getPlayer1();
		mage.setMana(10);
		Player warrior = context.getPlayer2();
		warrior.setMana(10);

		MinionCard minionCard = (MinionCard) CardCatalogue.getCardById("minion_sourcing_specialist");
		minionCard.setAttribute(Attribute.LAST_MINION_DESTROYED_CARD_ID, "minion_wisp");

		playCard(context, mage, minionCard);

		Assert.assertTrue(mage.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_wisp")));
	}
}
