package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.annotations.Test;

import static org.junit.Assert.*;

public class OccultistTests extends TestBase {

	@Test
	public void testUnearthedHorrorXitaluInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_xitalu");
			Minion target = playMinionCard(context, player, "minion_unearthed_horror");
			playCard(context, player, "spell_underwater_horrors", target);
			assertEquals(player.getDeck().size(), 3);
			for (Card card : player.getDeck()) {
				assertEquals(card.getBonusAttack(), 7);
				assertEquals(card.getBonusHp(), 7);
			}
		});
	}

	@Test
	public void testGhatanothoa() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "token_ghatanothoa");
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			String firstHp = player.getHeroPowerZone().get(0).getCardId();
			target.setMaxHp(5);
			target.setHp(3);
			useHeroPower(context, player, target.getReference());
			assertTrue(target.getHp() == 1 || target.getHp() == 5);
			String secondHp = player.getHeroPowerZone().get(0).getCardId();
			assertNotEquals(firstHp, secondHp);
		});
	}
}
