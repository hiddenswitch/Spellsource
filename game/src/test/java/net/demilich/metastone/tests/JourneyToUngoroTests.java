package net.demilich.metastone.tests;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JourneyToUngoroTests extends TestBase {
	@Test
	public void testEarthenScales() {
		GameContext context = createContext(HeroClass.DRUID, HeroClass.DRUID);
		Player player = context.getPlayer1();

		playCard(context, player, CardCatalogue.getCardById("token_sapling"));
		Minion sapling = player.getMinions().get(0);
		Assert.assertEquals(sapling.getAttack(), 1);
		playCardWithTarget(context, player, CardCatalogue.getCardById("spell_earthen_scales"), sapling);
		Assert.assertEquals(player.getHero().getArmor(), 2);
	}

	@Test
	public void testBarnabusTheStomper() {
		GameContext context = createContext(HeroClass.DRUID, HeroClass.DRUID);
		Player player = context.getPlayer1();
		clearHand(context, player);
		clearZone(context, player.getDeck());
		context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("token_sapling"));
		playCard(context, player, CardCatalogue.getCardById("token_barnabus_the_stomper"));
		context.getLogic().drawCard(player.getId(), null);
		Card sapling = player.getHand().get(0);
		Assert.assertEquals(sapling.getCardId(), "token_sapling");
		Assert.assertEquals(context.getLogic().getModifiedManaCost(player, sapling), 0);
	}
}
