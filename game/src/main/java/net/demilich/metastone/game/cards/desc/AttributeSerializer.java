package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;

public class AttributeSerializer implements JsonDeserializer<AttributeMap>, JsonSerializer<AttributeMap> {

	@Override
	public AttributeMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		AttributeMap map = new AttributeMap();
		JsonObject jsonData = json.getAsJsonObject();
		parseAttribute(Attribute.ARMOR, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HP, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HP_BONUS, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.MAX_HP, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ATTACK, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ATTACK_BONUS, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.ATTACK_EQUALS_HP, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.BATTLECRY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.BASE_ATTACK, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.BASE_HP, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.BASE_MANA_COST, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.MAX_HP, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.RACE, jsonData, map, ParseValueType.RACE);
		parseAttribute(Attribute.LAST_HIT, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.BOTH_CHOOSE_ONE_OPTIONS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CANNOT_ATTACK, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CHOOSE_ONE, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CANNOT_ATTACK_HERO_ON_SUMMON, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CANNOT_ATTACK_HEROES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.CHARGE, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.COMBO, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DIED_ON_TURN, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.DESCRIPTION, jsonData, map, ParseValueType.STRING);
		parseAttribute(Attribute.DOUBLE_END_TURN_TRIGGERS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.NUMBER_OF_ATTACKS, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.SUMMONING_SICKNESS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DESTROYED, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DEATHRATTLES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DIVINE_SHIELD, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DOUBLE_BATTLECRIES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.DOUBLE_DEATHRATTLES, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.ENRAGABLE, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.HEAL_AMPLIFY_MULTIPLIER, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HEALING_THIS_TURN, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HERO_POWER_CAN_TARGET_MINIONS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.HERO_POWER_FREEZES_TARGET, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.HERO_POWER_DAMAGE, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HERO_POWER_USAGES, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.HERO_POWERS_DISABLED, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.IMMUNE_HERO, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.INVERT_HEALING, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.LIFESTEAL, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.MEGA_WINDFURY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.OPPONENT_SPELL_DAMAGE, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.OVERLOAD, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.OVERLOADED_THIS_GAME, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.POISONOUS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.SPELL_AMPLIFY_MULTIPLIER, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.SPELL_DAMAGE, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.SPELL_DAMAGE_MULTIPLIER, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.STEALTH, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.TURN_TIME, jsonData, map, ParseValueType.INTEGER);
		parseAttribute(Attribute.UNTARGETABLE_BY_SPELLS, jsonData, map, ParseValueType.BOOLEAN);
		// TODO: Remove from Spellstopper
		parseAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.TAUNT, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.WINDFURY, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.PERMANENT, jsonData, map, ParseValueType.BOOLEAN);
		parseAttribute(Attribute.QUEST, jsonData, map, ParseValueType.BOOLEAN);

		return map;
	}

	private void parseAttribute(Attribute attribute, JsonObject jsonData, Map<Attribute, Object> map, ParseValueType valueType) {
		String argName = attribute.toString();
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		Boolean bool = value instanceof Boolean ? (Boolean) value : null;
		if (bool != null && bool) {
			value = 1;
		}
		map.put(attribute, value);
	}

	@Override
	public JsonElement serialize(AttributeMap src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		for (Attribute attribute : Attribute.values()) {
			if (!src.containsKey(attribute)) {
				continue;
			}
			String argName = attribute.toString();
			Object value = src.get(attribute);
			switch (attribute) {
				case BATTLECRY:
				case DEATHRATTLES:
					value = true;
				default:
					break;
			}
			result.add(argName, context.serialize(value));
		}
		return result;
	}
}
