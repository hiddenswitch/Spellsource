package net.demilich.metastone.game.cards;

import java.io.IOException;
import java.util.Map;

import com.hiddenswitch.proto3.net.util.Serialization;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.utils.ResourceInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.ChooseBattlecryCardDesc;
import net.demilich.metastone.game.cards.desc.ChooseOneCardDesc;
import net.demilich.metastone.game.cards.desc.HeroCardDesc;
import net.demilich.metastone.game.cards.desc.HeroPowerCardDesc;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.cards.desc.SecretCardDesc;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.cards.desc.WeaponCardDesc;

public class CardParser {
	private static Logger logger = LoggerFactory.getLogger(CardParser.class);

	public static CardCatalogueRecord parseCard(JsonObject card) throws IOException {
		// Do something horrible: Serialize to json, then read it in with GSON. :(
		String text = Json.encode(card);
		final String id = card.getString("id");
		CardDesc desc = getCardDesc(id, Serialization.getGson().fromJson(text, JsonElement.class));
		return new CardCatalogueRecord(id, card, desc);
	}

	private static Gson getGson() {
		return Serialization.getGson();
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		String input = IOUtils.toString(resourceInputStream.inputStream);
		JsonObject json = new JsonObject(Json.mapper.readValue(input, Map.class));
		JsonElement jsonData = Serialization.getGson().fromJson(input, JsonElement.class);

		final String fileName = resourceInputStream.fileName;
		String id = fileName.split("(\\.json)")[0];

		CardDesc desc = getCardDesc(id, jsonData);

		return new CardCatalogueRecord(id, json, desc);
	}

	private static CardDesc getCardDesc(String id, JsonElement jsonData) {
		com.google.gson.JsonObject gsonObject = jsonData.getAsJsonObject();
		gsonObject.addProperty("id", id);
		if (!gsonObject.has("name")) {
			throw new RuntimeException(id + " is missing 'name' attribute!");
		}
		if (!gsonObject.has("baseManaCost")) {
			throw new RuntimeException(id + " is missing 'baseManaCost' attribute!");
		}
		if (!gsonObject.has("type")) {
			throw new RuntimeException(id + " is missing 'type' attribute!");
		}
		if (!gsonObject.has("heroClass")) {
			throw new RuntimeException(id + " is missing 'heroClass' attribute!");
		}
		if (!gsonObject.has("rarity")) {
			throw new RuntimeException(id + " is missing 'rarity' attribute!");
		}
		if (!gsonObject.has("collectible")) {
			throw new RuntimeException(id + " is missing 'collectible' attribute!");
		}
		if (!gsonObject.has("set")) {
			throw new RuntimeException(id + " is missing 'set' attribute!");
		}
		CardType type = CardType.valueOf((String) gsonObject.get("type").getAsString());
		switch (type) {
			case SPELL:
				if (!gsonObject.has("description")) {
					throw new RuntimeException(id + " is missing 'description' attribute!");
				}
				if (!gsonObject.has("spell")) {
					throw new RuntimeException(id + " is missing 'spell' attribute!");
				}
				if (gsonObject.has("trigger")) {
					return Serialization.getGson().fromJson(jsonData, SecretCardDesc.class);
				} else {
					if (!gsonObject.has("targetSelection")) {
						throw new RuntimeException(id + " is missing 'targetSelection' attribute!");
					}
					return Serialization.getGson().fromJson(jsonData, SpellCardDesc.class);
				}
			case CHOOSE_ONE:
				if (!gsonObject.has("description")) {
					throw new RuntimeException(id + " is missing 'description' attribute!");
				}
				if (!gsonObject.has("options")) {
					throw new RuntimeException(id + " is missing 'options' attribute!");
				}
				if (!gsonObject.has("bothOptions")) {
					throw new RuntimeException(id + " is missing 'bothOptions' attribute!");
				}
				return Serialization.getGson().fromJson(jsonData, ChooseOneCardDesc.class);
			case MINION:
				if (!gsonObject.has("baseAttack")) {
					throw new RuntimeException(id + " is missing 'baseAttack' attribute!");
				}
				if (!gsonObject.has("baseHp")) {
					throw new RuntimeException(id + " is missing 'baseHp' attribute!");
				}
				if (!gsonObject.has("description") && (gsonObject.has("battlecry") ||
						gsonObject.has("deathrattle") || gsonObject.has("attributes") ||
						gsonObject.has("trigger") || gsonObject.has("passiveTrigger") ||
						gsonObject.has("deckTrigger") || gsonObject.has("options"))) {
					throw new RuntimeException(id + " is missing 'description' attribute!");
				}
				if (gsonObject.has("options")) {
					return Serialization.getGson().fromJson(jsonData, ChooseBattlecryCardDesc.class);
				} else {
					return Serialization.getGson().fromJson(jsonData, MinionCardDesc.class);
				}
			case WEAPON:
				if (!gsonObject.has("damage")) {
					throw new RuntimeException(id + " is missing 'damage' attribute!");
				}
				if (!gsonObject.has("durability")) {
					throw new RuntimeException(id + " is missing 'durability' attribute!");
				}
				if (!gsonObject.has("description") && (gsonObject.has("battlecry") ||
						gsonObject.has("deathrattle") || gsonObject.has("attributes") ||
						gsonObject.has("trigger") || gsonObject.has("passiveTrigger") ||
						gsonObject.has("deckTrigger") || gsonObject.has("onEquip") ||
						gsonObject.has("onUnequip"))) {
					throw new RuntimeException(id + " is missing 'description' attribute!");
				}
				return Serialization.getGson().fromJson(jsonData, WeaponCardDesc.class);
			case HERO_POWER:
				if (!gsonObject.has("description")) {
					throw new RuntimeException(id + " is missing 'description' attribute!");
				}
				if (!gsonObject.has("spell") && !gsonObject.has("options")) {
					throw new RuntimeException(id + " is missing 'spell' or 'options' attribute!");
				}
				if (!gsonObject.has("targetSelection")) {
					throw new RuntimeException(id + " is missing 'targetSelection' attribute!");
				}
				return Serialization.getGson().fromJson(jsonData, HeroPowerCardDesc.class);
			case HERO:
				return Serialization.getGson().fromJson(jsonData, HeroCardDesc.class);
			default:
				logger.error("Unknown cardType: " + type);
				break;
		}
		return null;
	}
}
