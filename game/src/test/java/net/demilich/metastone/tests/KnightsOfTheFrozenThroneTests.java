package net.demilich.metastone.tests;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.ChooseBattlecryHeroCard;
import net.demilich.metastone.game.cards.ChooseBattlecryMinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KnightsOfTheFrozenThroneTests extends TestBase {

	@Test
	public void testMalfurionThePestilent() {
		GameContext context = createContext(HeroClass.DRUID, HeroClass.WARRIOR);
		Player player = context.getActivePlayer();
		clearHand(context, player);
		clearZone(context, player.getDeck());
		ChooseBattlecryHeroCard malfurion = (ChooseBattlecryHeroCard) CardCatalogue.getCardById("hero_malfurion_the_pestilent");
		context.getLogic().receiveCard(player.getId(), malfurion);
		player.setMaxMana(7);
		player.setMana(7);
		// TODO: Finish testing Malfurion
		context.getLogic().performGameAction(player.getId(), malfurion.playBothOptions());
		Assert.assertEquals(player.getHero().getArmor(), 5);
	}
}
