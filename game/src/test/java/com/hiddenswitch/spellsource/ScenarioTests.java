package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ScenarioTests extends TestBase {

	@Test
	void testNecromancer() {
		runGym((context, player, opponent) -> {
			context.getLogic().setLoggingEnabled(true);
			context.getLogic().changeHero(player, ((HeroCard) CardCatalogue.getCardById("hero_necromancer")).createHero(), true);
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor") /*Cost 2*/;
			Minion bearshark = playMinionCard(context, player, "minion_bearshark") /*Cost 3*/;
			Assert.assertEquals(bloodfen.getDeathrattles().size(), 1);
			Assert.assertNull(bearshark.getDeathrattles());
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_assassinate", bloodfen);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getBaseManaCost(), 1);
		});
	}
}
