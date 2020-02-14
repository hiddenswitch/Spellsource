package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

public class TypeTest {
	@Test
	public void testGameEvents() {
		Stream.of(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.values())
				.forEach(ge -> {
					try {
						GameEvent.EventTypeEnum.valueOf(ge.toString());
					} catch (Exception e) {
						Assert.fail(ge.toString());
					}
				});
	}
}
