package net.demilich.metastone.game.spells.desc;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.DescDeserializer;
import net.demilich.metastone.game.entities.Actor;

/**
 * This enum describes the keys to the {@link SpellDesc} / the keys of the dictionaries in the {@link
 * net.demilich.metastone.game.cards.desc.CardDesc} card JSON files for spells.
 * <p>
 * To see how a particular argument is interpreted, refer to the Java {@link net.demilich.metastone.game.spells.Spell}
 * class written in the {@link SpellArg#CLASS} field of the {@link SpellDesc}. For example, if you see the spell:
 * <pre>
 *   {
 *     "class": "DrawCardSpell",
 *     "value": 1
 *   }
 * </pre>
 * And you want to see what {@code "value"} means in this context, visit {@link net.demilich.metastone.game.spells.DrawCardSpell}.
 * <p>
 * Observe that every {@code SpellArg} enum value is a key in the JSON for spell effects, except {@code camelCased} and
 * surrounded by quotation marks. When you read documentation for {@link net.demilich.metastone.game.spells.Spell}
 * subclasses and you see a {@code SpellArg} referred to there, assume the documentation is telling you to write a
 * {@code camelCased} version of the {@code SpellArg} into the JSON to get the effect you want. For example, if the
 * documentation says that {@link #EXCLUSIVE}, when set to {@code true}, does something, you'll write something like
 * this in the JSON:
 * <pre>
 *   {
 *     ...
 *     "exclusive": true
 *   }
 * </pre>
 * Whenever a new entry is added here, a corresponding deserialization instruction must be authoered in {@link
 * net.demilich.metastone.game.cards.desc.SpellDescDeserializer#init(DescDeserializer.SerializationContext)}. If a new
 * type is added (the right hand side of the JSON key/value pair), a new {@link net.demilich.metastone.game.cards.desc.ParseValueType}
 * needs to be added.
 *
 * @see net.demilich.metastone.game.cards.desc.SpellDescDeserializer#init(DescDeserializer.SerializationContext) for the
 * 		formal type of the values of these enum keys.
 */
public enum SpellArg {
	/**
	 * The Java subclass of {@link net.demilich.metastone.game.spells.Spell}, located in the {@code
	 * net.demilich.metastone.game.spells} or {@code net.demilich.metastone.game.spells.custom} (when prefixed with {@code
	 * "custom."}) packages in this module.
	 */
	CLASS,
	/**
	 * The amount of armor to buff to an entity, which is almost always only a hero.
	 *
	 * @see net.demilich.metastone.game.spells.BuffSpell for an example of a spell that uses this arg.
	 */
	ARMOR_BONUS,
	/**
	 * The amount of attack to buff to an entity. Used by a few spells and auras.
	 *
	 * @see net.demilich.metastone.game.spells.BuffSpell for an example of a spell that uses this arg.
	 */
	ATTACK_BONUS,
	/**
	 * Indicates an attribute for the spell, typically the one being added, removed, targeted or filtered.
	 *
	 * @see net.demilich.metastone.game.spells.AddAttributeSpell for an example of a spell that uses this arg.
	 */
	ATTRIBUTE,
	/**
	 * Indicates an aura, typically one that will be put into play on a target.
	 *
	 * @see net.demilich.metastone.game.spells.AddEnchantmentSpell for an example of a spell that uses this arg.
	 */
	AURA,
	/**
	 * This argument is added by the resolution of {@link net.demilich.metastone.game.logic.GameLogic#resolveDeathrattles(Player,
	 * Actor)}. It stores the position of the minion before it died. This then gets used by spells like {@link
	 * net.demilich.metastone.game.spells.SummonSpell} to summon a minion where the minion died.
	 * <p>
	 * It may someday be interpreted by other spells to put an entity in a particular position.
	 *
	 * @see net.demilich.metastone.game.spells.SummonSpell for an example of a spell that uses this arg.
	 */
	BOARD_POSITION_ABSOLUTE,
	/**
	 * Typically interpreted as a position relative to the {@code source} of a spell.
	 *
	 * @see net.demilich.metastone.game.spells.SummonSpell for an example of a spell that uses this arg.
	 */
	BOARD_POSITION_RELATIVE,
	/**
	 * When {@code true}, indicates {@link net.demilich.metastone.game.spells.DiscoverSpell} should not show a card the
	 * player already has in his hand.
	 */
	CANNOT_RECEIVE_OWNED,
	/**
	 * Interpreted as the card the spell is acting on.
	 */
	CARD,
	CARD_COST_MODIFIER,
	CARD_DESC_TYPE,
	CARD_FILTER,
	CARD_FILTERS,
	CARD_LOCATION,
	CARD_SOURCE,
	CARD_TYPE,
	CARDS,
	CONDITION,
	CONDITIONS,
	DESCRIPTION,
	EXCLUSIVE,
	FILTER,
	FULL_MANA_CRYSTALS,
	HERO_POWER,
	HOW_MANY,
	HP_BONUS,
	IGNORE_SPELL_DAMAGE,
	MANA,
	NAME,
	OPERATION,
	OPTIONS,
	RACE,
	QUEST,
	RANDOM_TARGET,
	REVERT_TRIGGER,
	SECRET,
	SECOND_REVERT_TRIGGER,
	SECONDARY_NAME,
	SECONDARY_TARGET,
	SECONDARY_VALUE,
	SPELL,
	SPELL1,
	SPELL2,
	SPELLS,
	SUMMON_BASE_HP,
	SUMMON_BASE_ATTACK,
	SUMMON_WINDFURY,
	SUMMON_TAUNT,
	SUMMON_CHARGE,
	SUMMON_DIVINE_SHIELD,
	SUMMON_STEALTH,
	SUMMON_TRIGGERS,
	SUMMON_BATTLECRY,
	SUMMON_DEATHRATTLE,
	SUMMON_AURA,
	TARGET,
	TARGET_PLAYER,
	TARGET_SELECTION,
	TRIGGER,
	GROUP,
	TRIGGERS,
	VALUE
}
