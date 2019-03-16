package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RiseOfShadowsTests extends TestBase {

	@Test
	public void testTwinSpell() {
		runGym((context, player, opponent) -> {
			Card aid = receiveCard(context, player, "spell_the_forests_aid");
			playCard(context, player, aid);
			assertEquals(player.getHand().size(), 1);
			aid = player.getHand().get(0);
			assertFalse(aid.getDescription().contains("Twinspell"));
			assertFalse(aid.hasAttribute(Attribute.TWINSPELL));
			playCard(context, player, aid);

			assertEquals(player.getHand().size(), 0);
		});
	}
}
