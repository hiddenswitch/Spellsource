package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.CaseFormat;
import com.hiddenswitch.spellsource.client.models.CardType;
import com.hiddenswitch.spellsource.client.models.Rarity;
import io.vertx.core.json.jackson.DatabindCodec;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.dynamicdescription.*;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.minions.BoardPositionRelative;
import net.demilich.metastone.game.spells.GameValue;
import net.demilich.metastone.game.spells.PlayerAttribute;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.TargetType;
import net.demilich.metastone.game.targeting.Zones;

import java.io.IOException;

public class ParseUtils {
	private static DescDeserializer<SpellDesc, ?, ?> spellParser = new SpellDescDeserializer();
	private static DescDeserializer<ValueProviderDesc, ?, ?> valueProviderParser = new ValueProviderDescDeserializer();
	private static DescDeserializer<EntityFilterDesc, ?, ?> filterParser = new EntityFilterDescDeserializer();
	private static DescDeserializer<AuraDesc, ?, ?> auraParser = new AuraDescDeserializer();
	private static DescDeserializer<CardSourceDesc, ?, ?> sourceParser = new CardSourceDescDeserializer();
	private static DescDeserializer<ConditionDesc, ?, ?> conditionParser = new ConditionDescDeserializer();
	private static DescDeserializer<EventTriggerDesc, ?, ?> eventTriggerParser = new EventTriggerDescDeserializer();
	private static DescDeserializer<DynamicDescriptionDesc, ?, ?> dynamicDescriptionParser = new DynamicDescriptionDeserializer();
	private static DescDeserializer<CardCostModifierDesc, ?, ?> manaModifierParser = new CardCostModifierDescDeserializer();

	@SuppressWarnings("deprecation")
	private static EntityReference parseEntityReference(String str) {
		String lowerCaseName = str.toLowerCase();
		if (lowerCaseName.length() > 0 && Character.isDigit(lowerCaseName.charAt(0))) {
			return new EntityReference(Integer.parseInt(lowerCaseName));
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
			case "other_enemy_minions":
				return EntityReference.OTHER_ENEMY_MINIONS;
			case "leftmost_friendly_card_hand":
				return EntityReference.LEFTMOST_FRIENDLY_CARD_HAND;
			case "leftmost_enemy_card_hand":
				return EntityReference.LEFTMOST_ENEMY_CARD_HAND;
			case "friendly_last_spell_played_this_turn":
				return EntityReference.FRIENDLY_LAST_SPELL_PLAYED_THIS_TURN;
			case "rightmost_friendly_card_hand":
				return EntityReference.RIGHTMOST_FRIENDLY_CARD_HAND;
			case "last_card_played":
				return EntityReference.LAST_CARD_PLAYED;
			case "friendly_last_card_played":
				return EntityReference.FRIENDLY_LAST_CARD_PLAYED;
			case "enemy_last_card_played":
				return EntityReference.ENEMY_LAST_CARD_PLAYED;
			case "last_card_played_before_current_sequence":
				return EntityReference.LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE;
			case "friendly_last_card_played_before_current_sequence":
				return EntityReference.FRIENDLY_LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE;
			case "enemy_last_card_played_before_current_sequence":
				return EntityReference.ENEMY_LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE;
			case "trigger_host":
				return EntityReference.TRIGGER_HOST;
			case "adjacent_minions":
				return EntityReference.ADJACENT_MINIONS;
			case "adjacent_to_target":
				return EntityReference.ADJACENT_TO_TARGET;
			case "attacker_adjacent_minions":
				return EntityReference.ATTACKER_ADJACENT_MINIONS;
			case "friendly_set_aside":
				return EntityReference.FRIENDLY_SET_ASIDE;
			case "enemy_set_aside":
				return EntityReference.ENEMY_SET_ASIDE;
			case "friendly_graveyard":
				return EntityReference.FRIENDLY_GRAVEYARD;
			case "enemy_graveyard":
				return EntityReference.ENEMY_GRAVEYARD;
			case "friendly_last_died_minion":
				return EntityReference.FRIENDLY_LAST_DIED_MINION;
			case "all_entities":
				return EntityReference.ALL_ENTITIES;
			case "opposite_minions":
				return EntityReference.OPPOSITE_MINIONS;
			case "opposite_characters":
				return EntityReference.OPPOSITE_CHARACTERS;
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
			case "event_source":
				return EntityReference.EVENT_SOURCE;
			case "target":
				return EntityReference.TARGET;
			case "spell_target":
				return EntityReference.SPELL_TARGET;
			case "output":
				return EntityReference.OUTPUT;
			case "self":
				return EntityReference.SELF;
			case "attacker":
				return EntityReference.ATTACKER;
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
			case "friendly_deck_from_top":
				return EntityReference.FRIENDLY_DECK_FROM_TOP;
			case "friendly_top_card":
				return EntityReference.FRIENDLY_TOP_CARD;
			case "enemy_deck":
				return EntityReference.ENEMY_DECK;
			case "enemy_top_card":
				return EntityReference.ENEMY_TOP_CARD;
			case "both_decks":
				return EntityReference.BOTH_DECKS;
			case "both_hands":
				return EntityReference.BOTH_HANDS;
			case "transform_reference":
				return EntityReference.TRANSFORM_REFERENCE;
			case "friendly_hero_power":
				return EntityReference.FRIENDLY_HERO_POWER;
			case "enemy_hero_power":
				return EntityReference.ENEMY_HERO_POWER;
			case "enemy_minions_left_to_right":
				return EntityReference.ENEMY_MINIONS_LEFT_TO_RIGHT;
			case "friendly_minions_left_to_right":
				return EntityReference.FRIENDLY_MINIONS_LEFT_TO_RIGHT;
			case "physical_attack_targets":
				return EntityReference.PHYSICAL_ATTACK_TARGETS;
			case "left_adjacent_minion":
				return EntityReference.LEFT_ADJACENT_MINION;
			case "right_adjacent_minion":
				return EntityReference.RIGHT_ADJACENT_MINION;
			case "friendly_cards":
				return EntityReference.FRIENDLY_CARDS;
			case "enemy_cards":
				return EntityReference.ENEMY_CARDS;
			case "current_summoning_minion":
				return EntityReference.CURRENT_SUMMONING_MINION;
			case "enemy_middle_minions":
				return EntityReference.ENEMY_MIDDLE_MINIONS;
			case "friendly_last_minion_played":
				return EntityReference.FRIENDLY_LAST_MINION_PLAYED;
			case "other_friendly_characters":
				return EntityReference.OTHER_FRIENDLY_CHARACTERS;
			case "friendly_signature":
				return EntityReference.FRIENDLY_SIGNATURE;
			case "enemy_signature":
				return EntityReference.ENEMY_SIGNATURE;
			case "friendly_secrets":
				return EntityReference.FRIENDLY_SECRETS;
			case "PLAYER_1":
				return EntityReference.PLAYER_1;
			case "PLAYER_2":
				return EntityReference.PLAYER_2;
			default:
				throw new NullPointerException(str);
		}
	}

	public static String toCamelCase(String input) {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, input);
	}

	public static String toUpperCase(String input) {
		return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, input);
	}

	@SuppressWarnings("unchecked")
	public static Object parse(JsonNode jsonData, ParseValueType valueType, DeserializationContext ctxt) throws JsonMappingException {
		switch (valueType) {
			case INTEGER:
				return jsonData.asInt();
			case INTEGER_ARRAY: {
				ArrayNode jsonArray = (ArrayNode) jsonData;
				int[] array = new int[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = jsonArray.get(i).asInt();
				}
				return array;
			}
			case BOOLEAN:
				return jsonData.asBoolean();
			case STRING:
				return jsonData.asText();
			case STRING_ARRAY: {
				ArrayNode jsonArray = (ArrayNode) jsonData;
				String[] array = new String[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = jsonArray.get(i).asText();
				}
				return array;
			}
			case TARGET_SELECTION:
				return Enum.valueOf(TargetSelection.class, jsonData.asText());
			case TARGET_REFERENCE:
				return parseEntityReference(jsonData.asText());
			case TARGET_PLAYER:
				return Enum.valueOf(TargetPlayer.class, jsonData.asText());
			case SPELL:
				return spellParser.innerDeserialize(ctxt, jsonData);
			case SPELL_ARRAY: {
				ArrayNode jsonArray = (ArrayNode) jsonData;
				SpellDesc[] array = new SpellDesc[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					JsonNode entry1 = jsonArray.get(i);
					array[i] = spellParser.innerDeserialize(ctxt, entry1);
				}
				return array;
			}
			case ATTRIBUTE:
				return Enum.valueOf(Attribute.class, jsonData.asText());
			case PLAYER_ATTRIBUTE:
				return Enum.valueOf(PlayerAttribute.class, jsonData.asText());
			case RARITY:
				return Enum.valueOf(Rarity.class, jsonData.asText());
			case GAME_VALUE:
				return Enum.valueOf(GameValue.class, jsonData.asText());
			case BOARD_POSITION_RELATIVE:
				return Enum.valueOf(BoardPositionRelative.class, jsonData.asText());
			case CARD_LOCATION:
				return Enum.valueOf(Zones.class, jsonData.asText());
			case OPERATION:
				return Enum.valueOf(ComparisonOperation.class, jsonData.asText());
			case CARD_TYPE:
				return Enum.valueOf(CardType.class, jsonData.asText());
			case ENTITY_TYPE:
				return Enum.valueOf(EntityType.class, jsonData.asText());
			case ACTION_TYPE:
				return Enum.valueOf(ActionType.class, jsonData.asText());
			case TARGET_TYPE:
				return Enum.valueOf(TargetType.class, jsonData.asText());
			case CARD_DESC_TYPE:
				return Enum.valueOf(CardDescType.class, jsonData.asText());
			case ALGEBRAIC_OPERATION:
				return Enum.valueOf(AlgebraicOperation.class, jsonData.asText());
			case VALUE:
				// value is either an int or a ValueProvider
				// if it is not an object, parse it as int, else fall-through to VALUE_PROVIDER case
				if (!(jsonData instanceof ObjectNode)) {
					return jsonData.asInt();
				}
			case VALUE_PROVIDER:
				return valueProviderParser.innerDeserialize(ctxt, jsonData).create();
			case ENTITY_FILTER: {
				return filterParser.innerDeserialize(ctxt, jsonData).create();
			}
			case CARD_SOURCE: {
				return sourceParser.innerDeserialize(ctxt, jsonData).create();
			}
			case CARD_SOURCE_ARRAY: {
				ArrayNode jsonArray = (ArrayNode) jsonData;
				CardSource[] array = new CardSource[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = sourceParser.innerDeserialize(ctxt, jsonArray.get(i)).create();
				}
				return array;
			}
			case AURA: {
				return auraParser.innerDeserialize(ctxt, jsonData);
			}
			case ENTITY_FILTER_ARRAY: {
				ArrayNode jsonArray = (ArrayNode) jsonData;
				EntityFilter[] array = new EntityFilter[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = filterParser.innerDeserialize(ctxt, jsonArray.get(i)).create();
				}
				return array;
			}
			case CONDITION: {
				return conditionParser.innerDeserialize(ctxt, jsonData).create();
			}
			case CONDITION_ARRAY: {
				ArrayNode jsonArray = (ArrayNode) jsonData;
				Condition[] array = new Condition[jsonArray.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = conditionParser.innerDeserialize(ctxt, jsonArray.get(i)).create();
				}
				return array;
			}
			case BATTLECRY:
				try {
					return DatabindCodec.mapper().readerFor(OpenerDesc.class).readValue(jsonData);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			case TRIGGER:
				return getTriggerDesc(jsonData);
			case TRIGGERS:
				ArrayNode array = (ArrayNode) jsonData;
				EnchantmentDesc[] enchantmentDescs = new EnchantmentDesc[array.size()];
				for (int i = 0; i < array.size(); i++) {
					enchantmentDescs[i] = getTriggerDesc(array.get(i));
				}
				return enchantmentDescs;
			case EVENT_TRIGGER:
				// Does not expect a concrete instance!
				return eventTriggerParser.innerDeserialize(ctxt, jsonData);
			case EVENT_TRIGGER_ARRAY:
				ArrayNode eventTriggerArray = (ArrayNode) jsonData;
				EventTriggerDesc[] eventTriggerDescs = new EventTriggerDesc[eventTriggerArray.size()];
				// Does not expect a concrete instance!
				for (int i = 0; i < eventTriggerArray.size(); i++) {
					eventTriggerDescs[i] = eventTriggerParser.innerDeserialize(ctxt, eventTriggerArray.get(i));
				}
				return eventTriggerDescs;
			case QUEST:
				EnchantmentDesc questEnchantmentDesc = getTriggerDesc(jsonData);
				return new Quest(questEnchantmentDesc, null);
			case SECRET:
				EnchantmentDesc secretEnchantmentDesc = getTriggerDesc(jsonData);
				return new Secret(secretEnchantmentDesc, null);
			case CARD_COST_MODIFIER:
				return manaModifierParser.innerDeserialize(ctxt, jsonData);
			case CHOOSE_ONE_OVERRIDE:
				return Enum.valueOf(ChooseOneOverride.class, jsonData.asText());
			case DYNAMIC_DESCRIPTION:
				if (jsonData.isTextual()) {
					DynamicDescriptionDesc descriptionDesc = new DynamicDescriptionDesc(StringDescription.class);
					descriptionDesc.put(DynamicDescriptionArg.STRING, jsonData.asText());
					return descriptionDesc.create();
				}
				return dynamicDescriptionParser.innerDeserialize(ctxt, jsonData).create();
			case DYNAMIC_DESCRIPTION_ARRAY:
				ArrayNode jsonArray = (ArrayNode) jsonData;
				DynamicDescription[] dynamicDescriptions = new DynamicDescription[jsonArray.size()];
				for (int i = 0; i < dynamicDescriptions.length; i++) {
					if (jsonArray.get(i).isTextual()) {
						DynamicDescriptionDesc descriptionDesc = new DynamicDescriptionDesc(StringDescription.class);
						descriptionDesc.put(DynamicDescriptionArg.STRING, jsonArray.get(i).asText());
						dynamicDescriptions[i] = descriptionDesc.create();
					} else {
						dynamicDescriptions[i] = dynamicDescriptionParser.innerDeserialize(ctxt, jsonArray.get(i)).create();
					}
				}
				return dynamicDescriptions;
			case ZONES:
				ArrayNode zoneArray = (ArrayNode) jsonData;
				Zones[] zones = new Zones[zoneArray.size()];
				for (int i = 0; i < zoneArray.size(); i++) {
					zones[i] = Enum.valueOf(Zones.class, jsonData.get(i).asText());
				}
				return zones;
			default:
				break;
		}
		return null;
	}

	private static EnchantmentDesc getTriggerDesc(JsonNode jsonData) {
		try {
			return DatabindCodec.mapper().readerFor(EnchantmentDesc.class).readValue(jsonData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
