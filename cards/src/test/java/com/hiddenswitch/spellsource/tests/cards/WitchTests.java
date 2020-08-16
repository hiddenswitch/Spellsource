package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class WitchTests extends TestBase {

	@Test
	public void testMaxCharges() {
		runGym((context, player, opponent) -> {
			var target = playMinionCard(context, player, 1, 1);
			for (var i = 0; i < 4; i++) {
				playCard(context, player, "spell_fox_dance", target);
			}
			assertEquals(3, player.getAttributeValue(Attribute.IMBUE), "3 is the max charges from imbue");
		}, HeroClass.PEACH, HeroClass.PEACH);
	}

	@Test
	public void testChooseOneOptionsShowSpellpower() {
		runGym((context, player, opponent) -> {
			var spellpower = playMinionCard(context, player, 1, 1);
			spellpower.setAttribute(Attribute.SPELL_DAMAGE, 1);
			var target = playMinionCard(context, player, 1, 1);
			overrideDiscover(context, player, discoverActions -> {
				assertTrue(discoverActions.get(0).getDescription(context, player.getId()).contains("7"));
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_nine_tail_fury", target);
		});
	}
}
