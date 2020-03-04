package com.hiddenswitch.spellsource.tests.hearthstone;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.minions.Race;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogueTests {

	private static Logger LOGGER = LoggerFactory.getLogger(CatalogueTests.class);

	private static String getCurrentCards() {
		String testedUrl = "https://api.hearthstonejson.com/v1/29933/enUS/cards.json";
		String overrideUrl = System.getProperty("spellsource.cards.url", System.getenv("SPELLSOURCE_CARDS_URL"));
		if (overrideUrl != null && !overrideUrl.equals("")) {
			testedUrl = overrideUrl;
		}
		return testedUrl;
	}
	
	public static Object[][] getHearthstoneCards() throws IOException, URISyntaxException, CardParseException {
		CardCatalogue.loadCardsFromPackage();
		String currentCards = getCurrentCards();
		if (currentCards == null || currentCards.equals("") || (!currentCards.startsWith("http") && !currentCards.startsWith("file"))) {
			LOGGER.warn("getHearthstoneCards: Url {} was invalid, skipping", currentCards);
			return new Object[0][0];
		}
		URL url = new URL(currentCards);
		URLConnection connection = url.openConnection();
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Safari/605.1.15");
		InputStream inputStream = connection.getInputStream();
		String cards = new BufferedReader(new InputStreamReader(inputStream)).lines()
				.parallel().collect(Collectors.joining("\n"));
		inputStream.close();


		JsonArray json = new JsonArray(cards);
		List<JsonObject> cardObjects = new ArrayList<>();
		for (Object e : json) {
			JsonObject jo = (JsonObject) e;
			if (jo.containsKey("type")
					&& jo.containsKey("collectible")
					&& jo.getBoolean("collectible")
					&& !jo.getString("type").equals("ENCHANTMENT")
					&& !jo.getString("type").equals("HERO")) {
				cardObjects.add(jo);
			}
		}

		Object[][] data = new Object[cardObjects.size()][1];
		for (int i = 0; i < cardObjects.size(); i++) {
			data[i][0] = cardObjects.get(i);
		}
		return data;
	}

	@ParameterizedTest
	@MethodSource("getHearthstoneCards")
	public void testHasCard(JsonObject cardObject) {
		Card card = CardCatalogue.getCardByName(cardObject.getString("name").replace("Ã±", "\\u00f1"));
		assertNotNull(card);
	}

	@ParameterizedTest
	@MethodSource("getHearthstoneCards")
	public void testAttributes(JsonObject cardObject) {
		Card card = null;
		try {
			for (CardCatalogueRecord record : CardCatalogue.getRecords().values()) {
				if (CardSet.isHearthstoneSet(record.getDesc().getSet())
						&& record.getDesc().isCollectible()
						&& record.getDesc().getName().equals(cardObject.getString("name").replace("Ã±", "\\u00f1"))) {
					card = record.getDesc().create();
				}
			}
			if (card == null) {
				throw new NullPointerException("not found");
			}
		} catch (NullPointerException ex) {
			fail(String.format("Could not find card with name %s", cardObject.getString("name")));
			return;
		}
		String name = cardObject.getString("name");
		assertEquals(card.getBaseManaCost(), (int) cardObject.getInteger("cost", -1), "Wrong cost for " + name);
		assertTrue(card.isCollectible(), String.format("%s should be collectible", card.getName()));
		if (card.getCardType() == CardType.MINION) {
			assertEquals(card.getBaseAttack(), (int) cardObject.getInteger("attack", -1), "Wrong attack for " + name);
			assertEquals(card.getBaseHp(), (int) cardObject.getInteger("health", -1), "Wrong health for " + name);

			if (cardObject.containsKey("mechanics")) {
				List<String> mechanics = new ArrayList<String>();
				for (Object o : cardObject.getJsonArray("mechanics")) {
					mechanics.add((String) o);
				}

				//for now, manually fix errors in the records
				if (name.equals("Sparring Partner")) {
					mechanics.add("TAUNT");
				} else if (name.equals("Lotus Assassin")) {
					mechanics.add("STEALTH");
				}

				final boolean battlecry = mechanics.stream().anyMatch(m -> m.equals("BATTLECRY"));
				final boolean combos = mechanics.stream().anyMatch(m -> m.equals("COMBO"));
				assertEquals(card.hasBattlecry(), battlecry || combos, name + " is missing a combos or battlecry attribute.");
				assertEquals(card.hasAttribute(Attribute.BATTLECRY), battlecry && !combos, name + " is missing battlecry attribute.");
				final boolean deathrattles = mechanics.stream().anyMatch(m -> m.equals("DEATHRATTLE"));
				assertEquals(card.hasAttribute(Attribute.DEATHRATTLES), deathrattles, name + " is missing deathrattle attribute.");
				final boolean taunt = mechanics.stream().anyMatch(m -> m.equals("TAUNT"));
				assertEquals(card.hasAttribute(Attribute.TAUNT), taunt, name + " is missing taunt attribute.");
				final boolean stealth = mechanics.stream().anyMatch(m -> m.equals("STEALTH"));
				assertEquals(card.hasAttribute(Attribute.STEALTH), stealth, name + " is missing stealth attribute.");
				final boolean charge = mechanics.stream().anyMatch(m -> m.equals("CHARGE"));
				assertEquals(card.hasAttribute(Attribute.CHARGE), charge, name + " is missing charge attribute.");
				final boolean divineShield = mechanics.stream().anyMatch(m -> m.equals("DIVINE_SHIELD"));
				assertEquals(card.hasAttribute(Attribute.DIVINE_SHIELD), divineShield, name + " is missing divine shield attribute.");
			}

			if (cardObject.containsKey("race")) {
				String race = cardObject.getString("race");
				if (race.equals("MECHANICAL")) {
					race = "MECH";
				}
				final String actual = card.getRace().toString();
				assertEquals(actual, race, name + " should have race " + race + " but has " + actual);
			} else {
				assertEquals(card.getRace(), Race.NONE, name + " should not have race but has " + card.getRace().toString());
			}

			if (cardObject.containsKey("rarity")) {
				String rarity = cardObject.getString("rarity");
				final String actual = card.getRarity().toString();
				assertEquals(actual, rarity, name + " should have rarity " + rarity + " but has " + actual);
			}
		}
	}
}
