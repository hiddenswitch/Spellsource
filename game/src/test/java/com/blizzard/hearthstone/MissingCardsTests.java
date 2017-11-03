package com.blizzard.hearthstone;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MissingCardsTests {

	@DataProvider(name = "CardNames")
	public static Object[][] getHearthstoneCards() throws IOException, URISyntaxException, CardParseException {
		CardCatalogue.loadCardsFromPackage();
		URL url = new URL("https://api.hearthstonejson.com/v1/21517/enUS/cards.json");
		URLConnection connection = url.openConnection();
		connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		String cards = IOUtils.toString(connection.getInputStream());
		Gson gson = new Gson();
		JsonArray json = gson.fromJson(cards, JsonArray.class);
		Object[] names = StreamSupport.stream(Spliterators.spliterator(json.iterator(), json.size(), Spliterator.SIZED), false)
				.map(JsonElement::getAsJsonObject)
				.filter(jo -> jo.has("type")
						&& jo.has("collectibe")
						&& jo.get("collectibe").getAsBoolean()
						&& !jo.get("type").getAsString().equals("ENCHANTMENT")
						&& !jo.get("type").getAsString().equals("HERO"))
				.map(jo -> jo.get("name").getAsString())
				.collect(Collectors.toList()).toArray();
		Object[][] data = new Object[names.length][1];
		for (int i = 0; i < names.length; i++) {
			data[i][0] = names[i];
		}
		return data;
	}

	@Test(dataProvider = "CardNames")
	public void testHasCard(String cardName) {
		Assert.assertNotNull(CardCatalogue.getCardByName(cardName));
	}
}
