package com.blizzard.hearthstone;

import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OneNightInKarazhanTests extends TestBase {
	@Test
	public void testMalchezaarsImp() {
		runGym((context, player, opponent) -> {
			clearHand(context, player);
			clearZone(context, player.getDeck());
			final Card acidicSwampOoze = CardCatalogue.getCardById("minion_acidic_swamp_ooze");
			acidicSwampOoze.setOwner(player.getId());
			acidicSwampOoze.setId(context.getLogic().getIdFactory().generateId());
			player.getDeck().addCard(acidicSwampOoze);
			final Card minionNoviceEngineer = CardCatalogue.getCardById("minion_novice_engineer");
			context.getLogic().receiveCard(player.getId(), minionNoviceEngineer);
			final Card malchezaarsImpl = CardCatalogue.getCardById("minion_malchezaars_imp");
			final Card soulfire = CardCatalogue.getCardById("spell_soulfire");
			playCard(context, player, malchezaarsImpl);
			context.getLogic().receiveCard(player.getId(), soulfire);
			PlayCardAction action = soulfire.play();
			action.setTarget(context.getPlayer2().getHero());
			context.getLogic().performGameAction(player.getId(), action);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_acidic_swamp_ooze", "The player should have Acidic Swamp Ooze in their hand after playing soulfire.");
			Assert.assertEquals(minionNoviceEngineer.getZone(), Zones.GRAVEYARD, "Novice engineer should be in the graveyard.");
		});
	}
}
