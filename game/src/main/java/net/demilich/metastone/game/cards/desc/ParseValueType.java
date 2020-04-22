package net.demilich.metastone.game.cards.desc;

import com.hiddenswitch.spellsource.client.models.ActionType;
import com.hiddenswitch.spellsource.client.models.CardType;
import com.hiddenswitch.spellsource.client.models.EntityType;
import com.hiddenswitch.spellsource.client.models.Rarity;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.entities.minions.BoardPositionRelative;

/**
 * Indicates a common set of types for arguments to various {@link Desc} objects that appear in the card JSON.
 */
public enum ParseValueType {
	/**
	 * A {@code true} or {@code false} value.
	 */
	BOOLEAN,
	/**
	 * An integer value.
	 */
	INTEGER,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.targeting.TargetSelection} enum.
	 */
	TARGET_SELECTION,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.targeting.EntityReference} static field name.
	 */
	TARGET_REFERENCE,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.spells.TargetPlayer} enum.
	 */
	TARGET_PLAYER,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.SpellDesc}.
	 */
	SPELL,
	/**
	 * An array of {@link net.demilich.metastone.game.spells.desc.SpellDesc} objects.
	 */
	SPELL_ARRAY,
	/**
	 * A string matching the name of a {@link Attribute} enum.
	 */
	ATTRIBUTE,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.spells.PlayerAttribute} enum.
	 */
	PLAYER_ATTRIBUTE,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc}.
	 */
	VALUE_PROVIDER,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc}.
	 */
	ENTITY_FILTER,
	/**
	 * An array of {@link net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc} objects.
	 */
	ENTITY_FILTER_ARRAY,
	/**
	 * A string, typically a card ID, description field or name field.
	 */
	STRING,
	/**
	 * An array of strings, typically an array of card IDs.
	 */
	STRING_ARRAY,
	/**
	 * A string matching the name of a {@link BoardPositionRelative} enum.
	 */
	BOARD_POSITION_RELATIVE,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.targeting.Zones} enum.
	 */
	CARD_LOCATION,
	/**
	 * A string matching the name of a {@link ComparisonOperation} enum.
	 */
	OPERATION,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation}
	 * enum.
	 */
	ALGEBRAIC_OPERATION,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.condition.Condition}.
	 */
	CONDITION,
	/**
	 * An array of {@link net.demilich.metastone.game.spells.desc.condition.Condition} objects.
	 */
	CONDITION_ARRAY,
	/**
	 * A string matching the name of a {@link CardType} enum.
	 */
	CARD_TYPE,
	/**
	 * A string matching the name of a {@link EntityType} enum.
	 */
	ENTITY_TYPE,
	/**
	 * A string matching the name of an {@link ActionType} enum.
	 */
	ACTION_TYPE,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.targeting.TargetType} enum.
	 */
	TARGET_TYPE,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc} object.
	 */
	TRIGGER,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc}.
	 */
	EVENT_TRIGGER,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc}.
	 */
	CARD_COST_MODIFIER,
	/**
	 * A string matching the name of a {@link Rarity} enum.
	 */
	RARITY,
	/**
	 * An integer, or a {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc}.
	 */
	VALUE,
	/**
	 * A string matching the name of a {@link CardType} enum.
	 */
	CARD_DESC_TYPE,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.source.CardSourceDesc}.
	 */
	CARD_SOURCE,
	/**
	 * An array of {@link net.demilich.metastone.game.spells.desc.source.CardSourceDesc}.
	 */
	CARD_SOURCE_ARRAY,
	/**
	 * An array of integers.
	 */
	INTEGER_ARRAY,
	/**
	 * A string matching the name of a {@link net.demilich.metastone.game.spells.GameValue} enum.
	 */
	GAME_VALUE,
	/**
	 * An array of {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc} objects.
	 */
	TRIGGERS,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc} that should be interpreted as a {@link
	 * net.demilich.metastone.game.spells.trigger.secrets.Quest}.
	 */
	QUEST,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.aura.AuraDesc}.
	 */
	AURA,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc} that should be interpreted as a {@link
	 * net.demilich.metastone.game.spells.trigger.secrets.Secret}.
	 */
	SECRET,
	/**
	 * A {@link net.demilich.metastone.game.cards.ChooseOneOverride}.
	 */
	CHOOSE_ONE_OVERRIDE,
	BATTLECRY,
	DYNAMIC_DESCRIPTION,
	/**
	 * The array version of {@link #EVENT_TRIGGER}.
	 */
	EVENT_TRIGGER_ARRAY,
	/**
	 * An array of {@link net.demilich.metastone.game.targeting.Zones} enum values.
	 */
	ZONES,
	/**
	 * An array of {@link net.demilich.metastone.game.cards.dynamicdescription.DynamicDescriptionDesc} objects.
	 */
	DYNAMIC_DESCRIPTION_ARRAY
}
