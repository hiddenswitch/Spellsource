package com.hiddenswitch.spellsource.trainer;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Decodes Hearthstone deckstrings (base64-encoded varint format) into DBF ID → count maps.
 */
public final class DeckStringDecoder {

	/**
	 * Decoded deck data: hero DBF ID and card DBF ID → count mapping.
	 */
	public record DecodedDeck(int heroDbfId, Map<Integer, Integer> cards) {
	}

	public static DecodedDeck decode(String deckstring) {
		byte[] bytes = Base64.getDecoder().decode(deckstring.trim());
		ByteBuffer buf = ByteBuffer.wrap(bytes);

		int reserved = readVarint(buf);
		if (reserved != 0) {
			throw new IllegalArgumentException("Expected reserved byte 0, got " + reserved);
		}
		int version = readVarint(buf);
		if (version != 1) {
			throw new IllegalArgumentException("Unsupported deckstring version: " + version);
		}
		int format = readVarint(buf);

		// Heroes section
		int numHeroes = readVarint(buf);
		int heroDbfId = 0;
		for (int i = 0; i < numHeroes; i++) {
			heroDbfId = readVarint(buf);
		}

		Map<Integer, Integer> cards = new LinkedHashMap<>();

		// Single-copy cards
		int numSingleCopy = readVarint(buf);
		for (int i = 0; i < numSingleCopy; i++) {
			int dbfId = readVarint(buf);
			cards.put(dbfId, 1);
		}

		// Double-copy cards
		int numDoubleCopy = readVarint(buf);
		for (int i = 0; i < numDoubleCopy; i++) {
			int dbfId = readVarint(buf);
			cards.put(dbfId, 2);
		}

		// N-copy cards (dbfId, count pairs)
		if (buf.hasRemaining()) {
			int numNcopy = readVarint(buf);
			for (int i = 0; i < numNcopy; i++) {
				int dbfId = readVarint(buf);
				int count = readVarint(buf);
				cards.put(dbfId, count);
			}
		}

		return new DecodedDeck(heroDbfId, cards);
	}

	private static int readVarint(ByteBuffer buf) {
		int result = 0;
		int shift = 0;
		while (true) {
			byte b = buf.get();
			result |= (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				break;
			}
			shift += 7;
		}
		return result;
	}

	private DeckStringDecoder() {
	}
}
