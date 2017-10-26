package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

public class TypeTests {
	@Test
	public void testGameEvents() {
		Stream.of(GameEventType.values())
				.forEach(ge -> {
					try {
						GameEvent.EventTypeEnum.valueOf(ge.toString());
					} catch (Exception e) {
						Assert.fail();
					}
				});
	}
}
