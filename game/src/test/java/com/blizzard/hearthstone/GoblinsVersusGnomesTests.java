package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestMinionCard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GoblinsVersusGnomesTests extends TestBase {
	@Test
	public void testBetrayalOnBurlyRockjawTroggDeals5Damage() {
		GameContext context = createContext(HeroClass.GOLD, HeroClass.BLACK);
		Player paladin = context.getPlayer1();

		MinionCard adjacentMinionCard1 = new TestMinionCard(1, 5, 0);
		playMinionCard(context, paladin, adjacentMinionCard1);

		MinionCard targetMinionCard = (MinionCard) CardCatalogue.getCardById("minion_burly_rockjaw_trogg");
		Minion targetMinion = playMinionCard(context, paladin, targetMinionCard);

		MinionCard adjacentMinionCard2 = new TestMinionCard(1, 5, 0);
		playMinionCard(context, paladin, adjacentMinionCard2);

		context.getLogic().endTurn(paladin.getId());

		Assert.assertEquals(paladin.getMinions().size(), 3);

		Player rogue = context.getPlayer2();

		Card betrayal = CardCatalogue.getCardById("spell_betrayal");

		context.getLogic().receiveCard(rogue.getId(), betrayal);
		GameAction action = betrayal.play();
		action.setTarget(targetMinion);
		context.getLogic().performGameAction(rogue.getId(), action);

		Assert.assertEquals(paladin.getMinions().size(), 1);
	}
}
