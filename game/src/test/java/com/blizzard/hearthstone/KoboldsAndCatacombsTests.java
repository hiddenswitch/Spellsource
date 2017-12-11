package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KoboldsAndCatacombsTests extends TestBase {
	@Test
	public void testJasperSpellstone() {
		runGym((context, player, opponent) -> {
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_lesser_jasper_spellstone"));
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
		});
	}
}
