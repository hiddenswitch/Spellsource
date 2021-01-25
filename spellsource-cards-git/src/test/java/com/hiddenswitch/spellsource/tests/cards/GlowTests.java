package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.testutils.CardValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class GlowTests extends TestBase {

	@Test
	public void testOpenerCondition() {
		runGym((context, player, opponent) -> {
			var doesNotMeetCondition = receiveCard(context, player, "minion_test_opener_condition");
			assertFalse(doesNotMeetCondition.getDesc().getGlowConditions().anyMatch(c -> c.isFulfilled(context, player, doesNotMeetCondition, null)));
		});

		runGym((context, player, opponent) -> {
			var doesMeetCondition = receiveCard(context, player, "minion_test_opener_condition");
			var onBattlefield = playMinionCard(context, player, 1, 1);
			assertEquals(1L, doesMeetCondition.getDesc().getGlowConditions().count(), "should match at least one");
			assertTrue(doesMeetCondition.getDesc().getGlowConditions().anyMatch(c -> c.isFulfilled(context, player, doesMeetCondition, null)));
		});
	}
}
