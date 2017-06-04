package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.*;

import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SpellDescSerializer implements JsonDeserializer<SpellDesc>, JsonSerializer<SpellDesc> {
	@SuppressWarnings("unchecked")
	@Override
	public SpellDesc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("SpellDesc parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		final String suppliedClassName = jsonData.get("class").getAsString();
		String[] spellClassNames = new String[]{Spell.class.getPackage().getName() + "." + suppliedClassName, Spell.class.getPackage().getName() + ".custom." + suppliedClassName};
		Class<? extends Spell> spellClass = null;
		for (String spellClassName : spellClassNames) {
			try {
				spellClass = (Class<? extends Spell>) Class.forName(spellClassName);
			} catch (ClassNotFoundException e) {
				continue;
			}
		}
		if (spellClass == null) {
			throw new JsonParseException("SpellDesc parser encountered an invalid spell class: " + suppliedClassName);
		}

		Map<SpellArg, Object> spellArgs = SpellDesc.build(spellClass);
		parseArgument(SpellArg.ATTACK_BONUS, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.ATTRIBUTE, jsonData, spellArgs, ParseValueType.ATTRIBUTE);
		parseArgument(SpellArg.ARMOR_BONUS, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.BOARD_POSITION_RELATIVE, jsonData, spellArgs, ParseValueType.BOARD_POSITION_RELATIVE);
		parseArgument(SpellArg.CANNOT_RECEIVE_OWNED, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.CARD, jsonData, spellArgs, ParseValueType.STRING);
		parseArgument(SpellArg.CARD_COST_MODIFIER, jsonData, spellArgs, ParseValueType.CARD_COST_MODIFIER);
		parseArgument(SpellArg.CARD_DESC_TYPE, jsonData, spellArgs, ParseValueType.CARD_DESC_TYPE);
		parseArgument(SpellArg.CARD_FILTER, jsonData, spellArgs, ParseValueType.ENTITY_FILTER);
		parseArgument(SpellArg.CARD_FILTERS, jsonData, spellArgs, ParseValueType.ENTITY_FILTER_ARRAY);
		parseArgument(SpellArg.CARD_LOCATION, jsonData, spellArgs, ParseValueType.CARD_LOCATION);
		parseArgument(SpellArg.CARD_SOURCE, jsonData, spellArgs, ParseValueType.CARD_SOURCE);
		parseArgument(SpellArg.CARD_TYPE, jsonData, spellArgs, ParseValueType.CARD_TYPE);
		parseArgument(SpellArg.CARDS, jsonData, spellArgs, ParseValueType.STRING_ARRAY);
		parseArgument(SpellArg.CONDITION, jsonData, spellArgs, ParseValueType.CONDITION);
		parseArgument(SpellArg.CONDITIONS, jsonData, spellArgs, ParseValueType.CONDITION_ARRAY);
		parseArgument(SpellArg.DESCRIPTION, jsonData, spellArgs, ParseValueType.STRING);
		parseArgument(SpellArg.EXCLUSIVE, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.FILTER, jsonData, spellArgs, ParseValueType.ENTITY_FILTER);
		parseArgument(SpellArg.FULL_MANA_CRYSTALS, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.HOW_MANY, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.HP_BONUS, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.IGNORE_SPELL_DAMAGE, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.INCLUDE_UNCOLLECTIBLE, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.MANA, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.MANA_MODIFIER, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.MAX_DAMAGE, jsonData, spellArgs, ParseValueType.INTEGER);
		parseArgument(SpellArg.MIN_DAMAGE, jsonData, spellArgs, ParseValueType.INTEGER);
		parseArgument(SpellArg.NAME, jsonData, spellArgs, ParseValueType.STRING);
		parseArgument(SpellArg.OPERATION, jsonData, spellArgs, ParseValueType.ALGEBRAIC_OPERATION);
		parseArgument(SpellArg.OPTIONS, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.RANDOM_TARGET, jsonData, spellArgs, ParseValueType.BOOLEAN);
		parseArgument(SpellArg.REVERT_TRIGGER, jsonData, spellArgs, ParseValueType.EVENT_TRIGGER);
		parseArgument(SpellArg.SECOND_REVERT_TRIGGER, jsonData, spellArgs, ParseValueType.EVENT_TRIGGER);
		parseArgument(SpellArg.SECONDARY_NAME, jsonData, spellArgs, ParseValueType.STRING);
		parseArgument(SpellArg.SECONDARY_TARGET, jsonData, spellArgs, ParseValueType.TARGET_REFERENCE);
		parseArgument(SpellArg.SECONDARY_VALUE, jsonData, spellArgs, ParseValueType.VALUE);
		parseArgument(SpellArg.SPELL, jsonData, spellArgs, ParseValueType.SPELL);
		parseArgument(SpellArg.SPELL_1, jsonData, spellArgs, ParseValueType.SPELL);
		parseArgument(SpellArg.SPELL_2, jsonData, spellArgs, ParseValueType.SPELL);
		parseArgument(SpellArg.SPELLS, jsonData, spellArgs, ParseValueType.SPELL_ARRAY);
		parseArgument(SpellArg.TARGET, jsonData, spellArgs, ParseValueType.TARGET_REFERENCE);
		parseArgument(SpellArg.TARGET_PLAYER, jsonData, spellArgs, ParseValueType.TARGET_PLAYER);
		parseArgument(SpellArg.TARGET_SELECTION, jsonData, spellArgs, ParseValueType.TARGET_SELECTION);
		parseArgument(SpellArg.TRIGGER, jsonData, spellArgs, ParseValueType.TRIGGER);
		parseArgument(SpellArg.VALUE, jsonData, spellArgs, ParseValueType.VALUE);
		return new SpellDesc(spellArgs);
	}

	private void parseArgument(SpellArg spellArg, JsonObject jsonData, Map<SpellArg, Object> spellArgs, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(spellArg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		spellArgs.put(spellArg, value);
	}

	private ThreadLocal<Set<SpellDesc>> sources =  ThreadLocal.withInitial(LinkedHashSet::new);

	@Override
	public JsonElement serialize(SpellDesc src, Type type, JsonSerializationContext context) {
		if (sources.get().contains(src)) {
			return JsonNull.INSTANCE;
//			throw new RuntimeException("Recursing!");
		}
		sources.get().add(src);
		JsonObject result = new JsonObject();
		result.add("class", new JsonPrimitive(src.getSpellClass().getSimpleName()));
		for (SpellArg attribute : SpellArg.values()) {
			if (attribute == SpellArg.CLASS) {
				continue;
			}
			if (!src.containsKey(attribute)) {
				continue;
			}
			String argName = ParseUtils.toCamelCase(attribute.toString());
			result.add(argName, context.serialize(src.get(attribute)));
		}
		sources.get().remove(src);
		return result;
	}
}
