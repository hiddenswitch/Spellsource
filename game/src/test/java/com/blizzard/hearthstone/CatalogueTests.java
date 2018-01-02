package com.blizzard.hearthstone;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hiddenswitch.spellsource.util.Serialization;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.utils.Attribute;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CatalogueTests {
	static Gson gson = new Gson();

	private static String getCurrentCards() {
		String testedUrl = "https://api.hearthstonejson.com/v1/22611/enUS/cards.json";
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

		JsonArray json = gson.fromJson(cards, JsonArray.class);
		List<JsonObject> cardObjects = new ArrayList<>();
		for (JsonElement e : json) {
			JsonObject jo = e.getAsJsonObject();
			if (jo.has("type")
					&& jo.has("collectible")
					&& jo.get("collectible").getAsBoolean()
					&& !jo.get("type").getAsString().equals("ENCHANTMENT")
					&& !jo.get("type").getAsString().equals("HERO")) {
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
		final Card card = CardCatalogue.getCardByName(cardObject.get("name").getAsString());
		Assert.assertNotNull(card);
	}

	@Test(dataProvider = "HearthstoneCards")
	public void testAttributes(JsonObject cardObject) {
		Type listType = new TypeToken<List<String>>() {
		}.getType();
		final Card card = CardCatalogue.getCardByName(cardObject.get("name").getAsString());
		String name = cardObject.get("name").getAsString();
//		String text = cardObject.has("text") ? cardObject.get("text").getAsString() : "";
//		text = Jsoup.parse(text).text();
//		text = text.replaceAll("[\\$\\#]", "").replaceAll("\\[x\\]", "");
//		String description = card.getDescription();
//		// Only compare non-whitespace and non-punctuation
//		text = text.replaceAll("[\\s.,:;]", "");
//		description = description.replaceAll("[\\s.,:;]", "");
//		Assert.assertEquals(description, text, "Wrong description for " + name);
		Assert.assertEquals(card.getBaseManaCost(), cardObject.get("cost").getAsInt(), "Wrong cost for " + name);
		if (card.getCardType() == CardType.MINION
				&& !(card instanceof HasChooseOneActions)) {
			MinionCard minionCard = (MinionCard) card;
			Assert.assertEquals(minionCard.getBaseAttack(), cardObject.get("attack").getAsInt(), "Wrong attack for " + name);
			Assert.assertEquals(minionCard.getBaseHp(), cardObject.get("health").getAsInt(), "Wrong health for " + name);

			if (cardObject.has("mechanics")) {
				List<String> mechanics = Serialization.getGson().fromJson(cardObject.get("mechanics"), listType);
				final boolean battlecry = mechanics.stream().anyMatch(m -> m.equals("BATTLECRY"));
				final boolean combos = mechanics.stream().anyMatch(m -> m.equals("COMBO"));
				Assert.assertEquals(minionCard.hasBattlecry(), battlecry || combos, name + " is missing a combos or battlecry attribute.");
				Assert.assertEquals(minionCard.hasAttribute(Attribute.BATTLECRY), battlecry && !combos, name + " is missing battlecry attribute.");
				final boolean deathrattles = mechanics.stream().anyMatch(m -> m.equals("DEATHRATTLE"));
				Assert.assertEquals(minionCard.hasAttribute(Attribute.DEATHRATTLES), deathrattles, name + " is missing deathrattle attribute.");
				final boolean taunt = mechanics.stream().anyMatch(m -> m.equals("TAUNT"));
				Assert.assertEquals(minionCard.hasAttribute(Attribute.TAUNT), taunt, name + " is missing taunt attribute.");
				final boolean stealth = mechanics.stream().anyMatch(m -> m.equals("STEALTH"));
				Assert.assertEquals(minionCard.hasAttribute(Attribute.STEALTH), stealth, name + " is missing stealth attribute.");
				final boolean charge = mechanics.stream().anyMatch(m -> m.equals("CHARGE"));
				Assert.assertEquals(minionCard.hasAttribute(Attribute.CHARGE), charge, name + " is missing charge attribute.");
				final boolean divineShield = mechanics.stream().anyMatch(m -> m.equals("DIVINE_SHIELD"));
				Assert.assertEquals(minionCard.hasAttribute(Attribute.DIVINE_SHIELD), divineShield, name + " is missing divine shield attribute.");
			}

			if (cardObject.has("race")) {
				String race = cardObject.get("race").getAsString();
				if (race.equals("MECHANICAL")) {
					race = "MECH";
				}
				final String actual = minionCard.getRace().toString();
				Assert.assertEquals(actual, race, name + " should have race " + race + " but has " + actual);
			} else {
				Assert.assertEquals(minionCard.getRace(), Race.NONE, name + " should not have race but has " + minionCard.getRace().toString());
			}

			if (cardObject.has("rarity")) {
				String rarity = cardObject.get("rarity").getAsString();
				final String actual = minionCard.getRarity().toString();
				Assert.assertEquals(actual, rarity, name + " should have rarity " + rarity + " but has " + actual);
			}
		}
	}
}
