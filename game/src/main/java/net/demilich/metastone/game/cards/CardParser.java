package net.demilich.metastone.game.cards;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.utils.ResourceInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.AttributeDeserializer;
import net.demilich.metastone.game.cards.desc.AuraDeserializer;
import net.demilich.metastone.game.cards.desc.CardCostModifierDeserializer;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.ChooseBattlecryCardDesc;
import net.demilich.metastone.game.cards.desc.ChooseOneCardDesc;
import net.demilich.metastone.game.cards.desc.ConditionDeserializer;
import net.demilich.metastone.game.cards.desc.HeroCardDesc;
import net.demilich.metastone.game.cards.desc.HeroPowerCardDesc;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.cards.desc.SecretCardDesc;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.cards.desc.SpellDeserializer;
import net.demilich.metastone.game.cards.desc.ValueProviderDeserializer;
import net.demilich.metastone.game.cards.desc.WeaponCardDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDeserializer;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;

public class CardParser {

	private static Logger logger = LoggerFactory.getLogger(CardParser.class);

	private static final Gson gson;

	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(SpellDesc.class, new SpellDeserializer());
		Type mapType = new TypeToken<Map<Attribute, Object>>() {
		}.getType();
		gsonBuilder.registerTypeAdapter(mapType, new AttributeDeserializer());
		gsonBuilder.registerTypeAdapter(ConditionDesc.class, new ConditionDeserializer());
		gsonBuilder.registerTypeAdapter(EventTriggerDesc.class, new EventTriggerDeserializer());
		gsonBuilder.registerTypeAdapter(AuraDesc.class, new AuraDeserializer());
		gsonBuilder.registerTypeAdapter(ValueProviderDesc.class, new ValueProviderDeserializer());
		gsonBuilder.registerTypeAdapter(CardCostModifierDesc.class, new CardCostModifierDeserializer());
		gson = gsonBuilder.create();
	}

	public static CardCatalogueRecord parseCard(JsonObject card) throws IOException {
		// Do something horrible: Serialize to json, then read it in with GSON. :(
		String text = Json.encode(card);
		final String id = card.getString("id");
		CardDesc desc = getCardDesc(id, gson.fromJson(text, JsonElement.class));
		return new CardCatalogueRecord(id, card, desc);
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		String input = IOUtils.toString(resourceInputStream.inputStream);
		JsonObject json = new JsonObject(Json.mapper.readValue(input, Map.class));
		JsonElement jsonData = gson.fromJson(input, JsonElement.class);

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
					return gson.fromJson(jsonData, SecretCardDesc.class);
				} else {
					if (!gsonObject.has("targetSelection")) {
						throw new RuntimeException(id + " is missing 'targetSelection' attribute!");
					}
					return gson.fromJson(jsonData, SpellCardDesc.class);
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
				return gson.fromJson(jsonData, ChooseOneCardDesc.class);
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
					return gson.fromJson(jsonData, ChooseBattlecryCardDesc.class);
				} else {
					return gson.fromJson(jsonData, MinionCardDesc.class);
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
				return gson.fromJson(jsonData, WeaponCardDesc.class);
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
				return gson.fromJson(jsonData, HeroPowerCardDesc.class);
			case HERO:
				return gson.fromJson(jsonData, HeroCardDesc.class);
			default:
				logger.error("Unknown cardType: " + type);
				break;
		}
		return null;
	}
}
