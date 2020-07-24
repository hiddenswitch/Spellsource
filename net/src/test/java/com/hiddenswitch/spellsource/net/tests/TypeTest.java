package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

;

public class TypeTest {
	@Test
	public void testGameEvents() {
		Stream.of(EventTypeEnum.values())
				.forEach(ge -> {
					try {
						EventTypeEnum.valueOf(ge.toString());
					} catch (Exception e) {
						fail(ge.toString());
					}
				});
	}
}
