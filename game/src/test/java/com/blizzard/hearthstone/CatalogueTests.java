package com.blizzard.hearthstone;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.demilich.metastone.game.cards.*;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
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
	static Gson gson = new Gson();

	@DataProvider(name = "HearthstoneCards")
	public static Object[][] getHearthstoneCards() throws IOException, URISyntaxException, CardParseException {
		CardCatalogue.loadCardsFromPackage();
		URL url = new URL("https://api.hearthstonejson.com/v1/21517/enUS/cards.json");
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
		final Card card = CardCatalogue.getCardByName(cardObject.get("name").getAsString());
		String name = cardObject.get("name").getAsString();
		String text = cardObject.has("text") ? cardObject.get("text").getAsString() : "";
		text = Jsoup.parse(text).text();
		text = text.replaceAll("[\\$\\#]", "").replaceAll("\\[x\\]", "");
		String description = card.getDescription();
		// Only compare non-whitespace and non-punctuation
		text = text.replaceAll("[\\s.,:;]", "");
		description = description.replaceAll("[\\s.,:;]", "");
		Assert.assertEquals(description, text, "Wrong description for " + name);
		Assert.assertEquals(card.getBaseManaCost(), cardObject.get("cost").getAsInt(), "Wrong cost for " + name);
		if (card.getCardType() == CardType.MINION) {
			MinionCard minionCard = (MinionCard) card;
			Assert.assertEquals(minionCard.getBaseAttack(), cardObject.get("attack").getAsInt(), "Wrong attack for " + name);
			Assert.assertEquals(minionCard.getBaseHp(), cardObject.get("health").getAsInt(), "Wrong health for " + name);
		}
	}
}
