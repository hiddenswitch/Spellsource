package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.impl.JsonMessageCodec;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonMessageCodecTests {
	public record Payload(String name, List<Integer> choices, Map<String, String> metadata) {}

	@Test
	public void roundTripsConsecutiveSmileFrames() {
		var codec = new JsonMessageCodec<>(Payload.class);
		var first = new Payload("player 日本語", List.of(1, 3, 7), Map.of("status", "ready"));
		var second = new Payload(null, List.of(), Map.of());
		var buffer = Buffer.buffer();
		codec.encodeToWire(buffer, first);
		var secondFrameOffset = buffer.length();
		codec.encodeToWire(buffer, second);
		assertEquals(first, codec.decodeFromWire(0, buffer));
		assertEquals(second, codec.decodeFromWire(secondFrameOffset, buffer));
	}
}
