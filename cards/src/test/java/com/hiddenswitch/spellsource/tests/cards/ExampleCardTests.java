package com.hiddenswitch.spellsource.tests.cards;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class ExampleCardTests extends TestBase {
	@Test
	public void testExampler() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_exampler");
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(),
					"token_skeletal_enforcer",
					"The opponent should have a Skeletal Enforcer after Exampler is summoned");
		});
	}
}

