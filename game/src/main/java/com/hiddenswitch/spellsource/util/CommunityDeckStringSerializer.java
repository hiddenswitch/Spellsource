package com.hiddenswitch.spellsource.util;

import com.google.common.io.Resources;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommunityDeckStringSerializer {
	private static final Map<Integer, String> dbf;

	static {
		Map<Integer, String> loaded;
		try {
			loaded = loadDbfFromResources();
		} catch (IOException ignored) {
			loaded = null;
		}
		dbf = loaded;
	}

	public CommunityDeckStringSerializer() {
	}

	private static Map<Integer, String> loadDbfFromResources() throws IOException {
		return (new JsonObject(Resources.toString(Resources.getResource("dbf.json"), Charset.defaultCharset())))
				.stream()
				.collect(Collectors.toMap(jo -> Integer.parseInt(jo.getKey()), jo -> (String) jo.getValue()));
	}

	public DeckCreateRequest toDeckCreateRequest(String userId, String name, String communityDeckString) {
		byte[] bytes;
		try {
			bytes = Base64.getDecoder().decode(communityDeckString);
		} catch (Throwable ex) {
			throw new IllegalArgumentException(String.format("Invalid deck string %s", communityDeckString), ex);
		}
		DataInput buffer = new DataInputStream(new ByteArrayInputStream(bytes));
		// Zero byte;
		try {
			buffer.readByte();
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid first byte.");
		}

		int version = read(buffer);
		if (version != 1) {
			throw new IllegalArgumentException("Invalid version. Expected 1.");
		}

		int formatId = read(buffer);
		String format = formatId == 1 ? "Wild" : "Standard";

		int numberHeroes = read(buffer);
		if (numberHeroes != 1) {
			throw new IllegalArgumentException("Invalid number of heroes. Expected 1.");
		}

		int heroCardId = read(buffer);
		int numberSingleCards = read(buffer);
		List<Integer> cardIds = new ArrayList<>();
		for (int i = 0; i < numberSingleCards; i++) {
			cardIds.add(read(buffer));
		}
		int numberDoubleCards = read(buffer);
		for (int i = 0; i < numberDoubleCards; i++) {
			int id = read(buffer);
			cardIds.add(id);
			cardIds.add(id);
		}
		int numberMultiCards = read(buffer);
		for (int i = 0; i < numberMultiCards; i++) {
			int id = read(buffer);
			int count = read(buffer);
			for (int j = 0; j < count; j++) {
				cardIds.add(id);
			}
		}

		if (name == null
				|| name.equals("")) {
			name = "Netdeck";
		}

		HeroClass heroClass = getRecord(heroCardId).getHeroClass();

		return DeckCreateRequest.empty(userId, name, heroClass)
				.withFormat(format)
				.withCardIds(cardIds.stream().map(CommunityDeckStringSerializer::getRecord).map(Card::getCardId).collect(Collectors.toList()));
	}

	private static int read(DataInput buffer) {
		try {
			return Varint.readUnsignedVarInt(buffer);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read.", e);
		}
	}

	private static Card getRecord(int dbfId) {
		return CardCatalogue.getCardByName(dbf.get(dbfId));
	}
}
