package com.blizzard.hearthstone;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.utils.Attribute;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CatalogueTests {

	private static String getCurrentCards() {
		String testedUrl = "https://api.hearthstonejson.com/v1/27358/enUS/cards.json";
		String overrideUrl = System.getProperty("spellsource.cards.url", System.getenv("SPELLSOURCE_CARDS_URL"));
		if (overrideUrl != null && !overrideUrl.equals("")) {
			testedUrl = overrideUrl;
		}
		return testedUrl;
	}

	@DataProvider(name = "HearthstoneCards")
	public static Object[][] getHearthstoneCards() throws IOException, URISyntaxException, CardParseException {
		CardCatalogue.loadCardsFromPackage();
		URL url = new URL(getCurrentCards());
		URLConnection connection = url.openConnection();
		connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		String cards = IOUtils.toString(connection.getInputStream());


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

	@Test(dataProvider = "HearthstoneCards")
	public void testHasCard(JsonObject cardObject) {
		final Card card = CardCatalogue.getCardByName(cardObject.getString("name").replace("Ã±", "\\u00f1"));
		Assert.assertNotNull(card);
	}

	@Test(dataProvider = "HearthstoneCards")
	public void testAttributes(JsonObject cardObject) {
		Card card;
		try {
			card = CardCatalogue.getCardByName(cardObject.getString("name").replace("Ã±", "\\u00f1"));
		} catch (NullPointerException ex) {
			Assert.fail(String.format("Could not find card with name %s", cardObject.getString("name")));
			return;
		}
		String name = cardObject.getString("name");
		Assert.assertEquals(card.getBaseManaCost(), (int) cardObject.getInteger("cost", -1), "Wrong cost for " + name);
		Assert.assertTrue(card.isCollectible(), String.format("%s should be collectible", card.getName()));
		if (card.getCardType() == CardType.MINION) {
			Assert.assertEquals(card.getBaseAttack(), (int) cardObject.getInteger("attack", -1), "Wrong attack for " + name);
			Assert.assertEquals(card.getBaseHp(), (int) cardObject.getInteger("health", -1), "Wrong health for " + name);

			if (cardObject.containsKey("mechanics")) {
				List<String> mechanics = new ArrayList<String>();
				for (Object o : cardObject.getJsonArray("mechanics")) {
					mechanics.add((String) o);
				}

				final boolean battlecry = mechanics.stream().anyMatch(m -> m.equals("BATTLECRY"));
				final boolean combos = mechanics.stream().anyMatch(m -> m.equals("COMBO"));
				Assert.assertEquals(card.hasBattlecry(), battlecry || combos, name + " is missing a combos or battlecry attribute.");
				Assert.assertEquals(card.hasAttribute(Attribute.BATTLECRY), battlecry && !combos, name + " is missing battlecry attribute.");
				final boolean deathrattles = mechanics.stream().anyMatch(m -> m.equals("DEATHRATTLE"));
				Assert.assertEquals(card.hasAttribute(Attribute.DEATHRATTLES), deathrattles, name + " is missing deathrattle attribute.");
				final boolean taunt = mechanics.stream().anyMatch(m -> m.equals("TAUNT"));
				Assert.assertEquals(card.hasAttribute(Attribute.TAUNT), taunt, name + " is missing taunt attribute.");
				final boolean stealth = mechanics.stream().anyMatch(m -> m.equals("STEALTH"));
				Assert.assertEquals(card.hasAttribute(Attribute.STEALTH), stealth, name + " is missing stealth attribute.");
				final boolean charge = mechanics.stream().anyMatch(m -> m.equals("CHARGE"));
				Assert.assertEquals(card.hasAttribute(Attribute.CHARGE), charge, name + " is missing charge attribute.");
				final boolean divineShield = mechanics.stream().anyMatch(m -> m.equals("DIVINE_SHIELD"));
				Assert.assertEquals(card.hasAttribute(Attribute.DIVINE_SHIELD), divineShield, name + " is missing divine shield attribute.");
			}

			if (cardObject.containsKey("race")) {
				String race = cardObject.getString("race");
				if (race.equals("MECHANICAL")) {
					race = "MECH";
				}
				final String actual = card.getRace().toString();
				Assert.assertEquals(actual, race, name + " should have race " + race + " but has " + actual);
			} else {
				Assert.assertEquals(card.getRace(), Race.NONE, name + " should not have race but has " + card.getRace().toString());
			}

			if (cardObject.containsKey("rarity")) {
				String rarity = cardObject.getString("rarity");
				final String actual = card.getRarity().toString();
				Assert.assertEquals(actual, rarity, name + " should have rarity " + rarity + " but has " + actual);
			}
		}
	}
}
