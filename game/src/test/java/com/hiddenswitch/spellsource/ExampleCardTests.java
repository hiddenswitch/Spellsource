package com.hiddenswitch.spellsource;

import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExampleCardTests extends TestBase {
	@Test
	public void testExampler() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_exampler");
			Assert.assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(),
					"token_skeletal_enforcer",
					"The opponent should have a Skeletal Enforcer after Exampler is summoned");
		});
	}
}
