package net.demilich.metastone.game.cards.desc;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.spells.GameValue;
import net.demilich.metastone.game.spells.PlayerAttribute;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.cards.CardDescType;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.minions.RelativeToSource;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterDesc;
import net.demilich.metastone.game.spells.desc.filter.Operation;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDescSerializer;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.TargetType;

public class ParseUtils {
	private static SpellDescSerializer spellParser = new SpellDescSerializer();
	private static ValueProviderDescSerializer valueProviderParser = new ValueProviderDescSerializer();
	private static FilterDescSerializer filterParser = new FilterDescSerializer();
	private static SourceDescSerializer sourceParser = new SourceDescSerializer();
	private static ConditionDescSerializer conditionParser = new ConditionDescSerializer();
	private static EventTriggerDescSerializer triggerParser = new EventTriggerDescSerializer();
	private static CardCostModifierDescSerializer manaModifierParser = new CardCostModifierDescSerializer();

	public static Object parse(String argName, JsonObject jsonData, ParseValueType valueType) {
		JsonElement entry = jsonData.get(argName);
		switch (valueType) {
			case INTEGER:
				return entry.getAsInt();
			case INTEGER_ARRAY: {
				JsonArray jsonArray = entry.getAsJsonArray();
				int[] array = new int[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = jsonArray.get(i).getAsInt();
				}
				return array;
			}
			case BOOLEAN:
				return entry.getAsBoolean();
			case STRING:
				return entry.getAsString();
			case STRING_ARRAY: {
				JsonArray jsonArray = entry.getAsJsonArray();
				String[] array = new String[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = jsonArray.get(i).getAsString();
				}
				return array;
			}
			case TARGET_SELECTION:
				return Enum.valueOf(TargetSelection.class, entry.getAsString());
			case TARGET_REFERENCE:
				return parseEntityReference(entry.getAsString());
			case TARGET_PLAYER:
				return Enum.valueOf(TargetPlayer.class, entry.getAsString());
			case RACE:
				return Enum.valueOf(Race.class, entry.getAsString());
			case CARD_SET:
				return Enum.valueOf(CardSet.class, entry.getAsString());
			case SPELL:
				return spellParser.deserialize(entry, SpellDesc.class, null);
			case SPELL_ARRAY: {
				JsonArray jsonArray = entry.getAsJsonArray();
				SpellDesc[] array = new SpellDesc[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					JsonElement entry1 = jsonArray.get(i);
					if (entry1.getAsJsonObject().has("desc")) {
						entry1 = entry1.getAsJsonObject().get("desc");
					}
					array[i] = spellParser.deserialize(entry1, SpellDesc.class, null);
				}
				return array;
			}
			case ATTRIBUTE:
				return Enum.valueOf(Attribute.class, entry.getAsString());
			case PLAYER_ATTRIBUTE:
				return Enum.valueOf(PlayerAttribute.class, entry.getAsString());
			case RARITY:
				return Enum.valueOf(Rarity.class, entry.getAsString());
			case HERO_CLASS:
				return Enum.valueOf(HeroClass.class, entry.getAsString());
			case GAME_VALUE:
				return Enum.valueOf(GameValue.class, entry.getAsString());
			case HERO_CLASS_ARRAY: {
				JsonArray jsonArray = entry.getAsJsonArray();
				HeroClass[] array = new HeroClass[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = Enum.valueOf(HeroClass.class, jsonArray.get(i).getAsString());
				}
				return array;
			}
			case BOARD_POSITION_RELATIVE:
				return Enum.valueOf(RelativeToSource.class, entry.getAsString());
			case CARD_LOCATION:
				return Enum.valueOf(Zones.class, entry.getAsString());
			case OPERATION:
				return Enum.valueOf(Operation.class, entry.getAsString());
			case CARD_TYPE:
				return Enum.valueOf(CardType.class, entry.getAsString());
			case ENTITY_TYPE:
				return Enum.valueOf(EntityType.class, entry.getAsString());
			case ACTION_TYPE:
				return Enum.valueOf(ActionType.class, entry.getAsString());
			case TARGET_TYPE:
				return Enum.valueOf(TargetType.class, entry.getAsString());
			case CARD_DESC_TYPE:
				return Enum.valueOf(CardDescType.class, entry.getAsString());
			case ALGEBRAIC_OPERATION:
				return Enum.valueOf(AlgebraicOperation.class, entry.getAsString());
			case VALUE:
				// value is either an int or a ValueProvider
				// if it is not an object, parse it as int, else fall-through to VALUE_PROVIDER case
				if (!entry.isJsonObject()) {
					return entry.getAsInt();
				}
			case VALUE_PROVIDER:
				if (entry.getAsJsonObject().has("desc")) {
					entry = entry.getAsJsonObject().get("desc");
				}
				ValueProviderDesc valueProviderDesc = valueProviderParser.deserialize(entry, ValueProviderDesc.class, null);
				return valueProviderDesc.createInstance();
			case ENTITY_FILTER: {
				if (entry.getAsJsonObject().has("desc")) {
					entry = entry.getAsJsonObject().get("desc");
				}
				FilterDesc filterDesc = filterParser.deserialize(entry, FilterDesc.class, null);
				return filterDesc.create();
			}
			case CARD_SOURCE: {
				if (entry.getAsJsonObject().has("desc")) {
					entry = entry.getAsJsonObject().get("desc");
				}
				SourceDesc sourceDesc = sourceParser.deserialize(entry, SourceDesc.class, null);
				return sourceDesc.create();
			}
			case ENTITY_FILTER_ARRAY: {
				JsonArray jsonArray = entry.getAsJsonArray();
				EntityFilter[] array = new EntityFilter[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					JsonElement entry1 = jsonArray.get(i);
					if (entry1.getAsJsonObject().has("desc")) {
						entry1 = entry1.getAsJsonObject().get("desc");
					}
					FilterDesc filterDesc = filterParser.deserialize(entry1, FilterDesc.class, null);
					array[i] = filterDesc.create();
				}
				return array;
			}
			case CONDITION: {
				if (entry.getAsJsonObject().has("desc")) {
					entry = entry.getAsJsonObject().get("desc");
				}
				ConditionDesc conditionDesc = conditionParser.deserialize(entry, ConditionDesc.class, null);
				return conditionDesc.create();
			}
			case CONDITION_ARRAY: {
				JsonArray jsonArray = entry.getAsJsonArray();
				Condition[] array = new Condition[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					JsonElement entry1 = jsonArray.get(i);
					if (entry1.getAsJsonObject().has("desc")) {
						entry1 = entry1.getAsJsonObject().get("desc");
					}
					ConditionDesc conditionDesc = conditionParser.deserialize(entry1, ConditionDesc.class, null);
					array[i] = conditionDesc.create();
				}
				return array;
			}
			case TRIGGER:
				return deserializeTriggerDesc(entry);
			case TRIGGERS:
				JsonArray array = entry.getAsJsonArray();
				TriggerDesc[] triggerDescs = new TriggerDesc[array.size()];
				for (int i = 0; i < array.size(); i++) {
					triggerDescs[i] = deserializeTriggerDesc(array.get(i));
				}
				return triggerDescs;
			case EVENT_TRIGGER:
				if (entry.getAsJsonObject().has("desc")) {
					entry = entry.getAsJsonObject().get("desc");
				}
				return triggerParser.deserialize(entry, EventTriggerDesc.class, null);
			case CARD_COST_MODIFIER:
				if (entry.getAsJsonObject().has("desc")) {
					entry = entry.getAsJsonObject().get("desc");
				}
				return manaModifierParser.deserialize(entry, CardCostModifierDesc.class, null);
			default:
				break;
		}
		return null;
	}

	protected static TriggerDesc deserializeTriggerDesc(JsonElement entry) {
		JsonObject triggerObject = entry.getAsJsonObject();
		TriggerDesc triggerDesc = new TriggerDesc();
		triggerDesc.eventTrigger = triggerParser.deserialize(triggerObject.get("eventTrigger"), EventTriggerDesc.class, null);
		triggerDesc.spell = spellParser.deserialize(triggerObject.get("spell"), SpellDesc.class, null);
		triggerDesc.oneTurn = triggerObject.has("oneTurn") && triggerObject.get("oneTurn").getAsBoolean();
		triggerDesc.persistentOwner = triggerObject.has("persistentOwner") && triggerObject.get("persistentOwner").getAsBoolean();
		triggerDesc.turnDelay = triggerObject.has("turnDelay") ? triggerObject.get("turnDelay").getAsInt() : 0;
		triggerDesc.maxFires = triggerObject.has("maxFires") ? triggerObject.get("maxFires").getAsInt() : null;
		triggerDesc.countUntilCast = triggerObject.has("countUntilCast") ? triggerObject.get("countUntilCast").getAsInt() : null;
		triggerDesc.keepAfterTransform = triggerObject.has("keepAfterTransform") && triggerObject.get("keepAfterTransform").getAsBoolean();
		return triggerDesc;
	}

	private static EntityReference parseEntityReference(String str) {
		String lowerCaseName = str.toLowerCase();
		try {
			return new EntityReference(Integer.parseInt(lowerCaseName));
		} catch (Exception ignored) {
		}
		switch (lowerCaseName) {
			case "none":
				return EntityReference.NONE;
			case "enemy_characters":
				return EntityReference.ENEMY_CHARACTERS;
			case "enemy_minions":
				return EntityReference.ENEMY_MINIONS;
			case "enemy_hero":
				return EntityReference.ENEMY_HERO;
			case "enemy_weapon":
				return EntityReference.ENEMY_WEAPON;
			case "friendly_characters":
				return EntityReference.FRIENDLY_CHARACTERS;
			case "friendly_minions":
				return EntityReference.FRIENDLY_MINIONS;
			case "other_friendly_minions":
				return EntityReference.OTHER_FRIENDLY_MINIONS;
			case "adjacent_minions":
				return EntityReference.ADJACENT_MINIONS;
			case "attacker_adjacent_minions":
				return EntityReference.ATTACKER_ADJACENT_MINIONS;
			case "friendly_set_aside":
				return EntityReference.FRIENDLY_SET_ASIDE;
			case "enemy_set_aside":
				return EntityReference.ENEMY_SET_ASIDE;
			case "opposite_minions":
				return EntityReference.OPPOSITE_MINIONS;
			case "friendly_hero":
				return EntityReference.FRIENDLY_HERO;
			case "friendly_weapon":
				return EntityReference.FRIENDLY_WEAPON;
			case "all_minions":
				return EntityReference.ALL_MINIONS;
			case "all_characters":
				return EntityReference.ALL_CHARACTERS;
			case "all_other_characters":
				return EntityReference.ALL_OTHER_CHARACTERS;
			case "all_other_minions":
				return EntityReference.ALL_OTHER_MINIONS;
			case "event_target":
				return EntityReference.EVENT_TARGET;
			case "target":
				return EntityReference.TARGET;
			case "spell_target":
				return EntityReference.SPELL_TARGET;
			case "pending_card":
				return EntityReference.PENDING_CARD;
			case "event_card":
				return EntityReference.EVENT_CARD;
			case "self":
				return EntityReference.SELF;
			case "attacker":
				return EntityReference.ATTACKER_REFERENCE;
			case "friendly_hand":
				return EntityReference.FRIENDLY_HAND;
			case "enemy_hand":
				return EntityReference.ENEMY_HAND;
			case "leftmost_friendly_minion":
				return EntityReference.LEFTMOST_FRIENDLY_MINION;
			case "leftmost_enemy_minion":
				return EntityReference.LEFTMOST_ENEMY_MINION;
			case "rightmost_friendly_minion":
				return EntityReference.RIGHTMOST_FRIENDLY_MINION;
			case "rightmost_enemy_minion":
				return EntityReference.RIGHTMOST_ENEMY_MINION;
			case "friendly_player":
				return EntityReference.FRIENDLY_PLAYER;
			case "enemy_player":
				return EntityReference.ENEMY_PLAYER;
			case "minions_to_left":
				return EntityReference.MINIONS_TO_LEFT;
			case "minions_to_right":
				return EntityReference.MINIONS_TO_RIGHT;
			case "friendly_deck":
				return EntityReference.FRIENDLY_DECK;
			case "enemy_deck":
				return EntityReference.ENEMY_DECK;
			case "both_decks":
				return EntityReference.BOTH_DECKS;
			case "both_hands":
				return EntityReference.BOTH_HANDS;
			case "transform_reference":
				return EntityReference.TRANSFORM_REFERENCE;
			default:
				return null;
		}
	}

	public static String toCamelCase(String input) {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, input);
	}

	public static boolean tryParseBool(String value) {
		try {
			Boolean.parseBoolean(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean tryParseInt(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
