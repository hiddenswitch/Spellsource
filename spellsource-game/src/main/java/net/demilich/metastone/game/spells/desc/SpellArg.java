package net.demilich.metastone.game.spells.desc;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.HasDeathrattleEnchantments;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.DescDeserializer;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.AddDeathrattleSecondaryAsTargetSpell;
import net.demilich.metastone.game.spells.AddQuestSpell;
import net.demilich.metastone.game.spells.AddSecretSpell;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.CastFromGroupSpell;
import net.demilich.metastone.game.spells.ConditionalSpell;
import net.demilich.metastone.game.spells.CreateCardSpell;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.DiscoverFilteredCardSpell;
import net.demilich.metastone.game.spells.DiscoverSpell;
import net.demilich.metastone.game.spells.ExcessDamageSpell;
import net.demilich.metastone.game.spells.FightSpell;
import net.demilich.metastone.game.spells.MissilesSpell;
import net.demilich.metastone.game.spells.ModifyDamageSpell;
import net.demilich.metastone.game.spells.ModifyMaxManaSpell;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.RecruitSpell;
import net.demilich.metastone.game.spells.RevertableSpell;
import net.demilich.metastone.game.spells.SetDescriptionSpell;
import net.demilich.metastone.game.spells.SetRaceSpell;
import net.demilich.metastone.game.spells.ShuffleToDeckSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.StealCardSpell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.spells.desc.source.UnweightedCatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

import java.util.List;

/**
 * This enum describes the keys to the {@link SpellDesc} / the keys of the dictionaries in the {@link
 * net.demilich.metastone.game.cards.desc.CardDesc} card JSON files for spells.}
 * <p>
 * To see how a particular argument is interpreted, refer to the Java {@link net.demilich.metastone.game.spells.Spell}
 * class written in the {@link SpellArg#CLASS} field of the {@link SpellDesc}. For example, if you see the spell:
 * <pre>
 *   {
 *     "class": "DrawCardSpell",
 *     "value": 1
 *   }
 * </pre>
 * Observe that {@link SpellArg#VALUE} corresponds to the key {@code "value"} in this JSON. All the fields in the JSON
 * in {@code camelCase} have a corresponding field name in {@code UPPER_CASE}. To see what {@code "value"} / {@link
 * SpellArg#VALUE} means in this context, visit {@link net.demilich.metastone.game.spells.DrawCardSpell}.
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
 * DescDeserializer#init(DescDeserializer.SerializationContext)}. If a new type is added (the right hand side of the
 * JSON key/value pair), a new {@link net.demilich.metastone.game.cards.desc.ParseValueType} needs to be added.
 *
 * @see DescDeserializer#init(DescDeserializer.SerializationContext) for the formal type of the values of these enum
 * keys.
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
	 * Indicates an {@link net.demilich.metastone.game.spells.desc.aura.AuraDesc}, typically one that will be put into
	 * play on a target.
	 *
	 * @see net.demilich.metastone.game.spells.AddEnchantmentSpell for an example of a spell that uses this arg.
	 */
	AURA,
	/**
	 * This argument is added by the resolution of {@link net.demilich.metastone.game.logic.GameLogic#resolveAftermaths(Player,
	 * Actor)}. It stores the position of the minion before it died. This then gets used by spells like {@link
	 * SummonSpell} to summon a minion where the minion died.
	 * <p>
	 * It may someday be interpreted by other spells to put an entity in a particular position.
	 *
	 * @see SummonSpell for an example of a spell that uses this arg.
	 */
	BOARD_POSITION_ABSOLUTE,
	/**
	 * Typically interpreted as a position relative to the {@code source} of a spell.
	 *
	 * @see SummonSpell for an example of a spell that uses this arg.
	 */
	BOARD_POSITION_RELATIVE,
	/**
	 * When {@code true}, indicates {@link net.demilich.metastone.game.spells.DiscoverSpell} should not show a card the
	 * player already has in his hand.
	 */
	CANNOT_RECEIVE_OWNED,
	/**
	 * Interpreted as the card the spell is acting on. It is typically the ID (file name of a card without the {@code
	 * ".json"} extension) of a card.
	 * <p>
	 * This argument is fairly common. For a {@link SummonSpell}, it indicates which card to summon. For a {@link
	 * ReceiveCardSpell}, it indicates which card should be put in the player's hand.
	 * <p>
	 * Many spells use a {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)} system to
	 * interpret a set of arguments ({@link SpellArg#CARD} being one of them) as a way to specify "a bunch of cards."
	 *
	 * @see ReceiveCardSpell for an example of a spell that uses this arg.
	 */
	CARD,
	/**
	 * Describes a {@link net.demilich.metastone.game.cards.costmodifier.CardCostModifier} for the {@link
	 * net.demilich.metastone.game.spells.CardCostModifierSpell}.
	 */
	CARD_COST_MODIFIER,
	/**
	 * Describes the kind of card created in a {@link net.demilich.metastone.game.spells.CreateCardSpell}. Considered
	 * obsolete.
	 */
	CARD_DESC_TYPE,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.filter.EntityFilter} that typically operates on cards. These
	 * cards are retrieved from a {@link CardSource}, which when not specified is the {@link UnweightedCatalogueSource} by
	 * default for random effects and {@link CatalogueSource} for discovers. Like {@link SpellArg#CARD}, it is used by
	 * pipelines like {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)} and {@link
	 * SpellDesc#getFilteredCards(GameContext, Player, Entity)} to generate a list of cards for some effect.
	 * <p>
	 * This argument shouldn't be confused with {@link net.demilich.metastone.game.spells.desc.filter.CardFilter}. Any
	 * {@link net.demilich.metastone.game.spells.desc.filter.EntityFilter} can be on the right hand side of a {@code
	 * "cardFilter"}.
	 *
	 * @see DiscoverSpell for an example of a spell that uses this arg.
	 * @see SpellArg#CARD_SOURCE for an arg that is commonly included with this arg.
	 * @see net.demilich.metastone.game.spells.desc.filter.EntityFilter for more about entity filters.
	 */
	CARD_FILTER,
	/**
	 * Multiple card filters that are used by effects like {@link DiscoverFilteredCardSpell}. Considered obsolete.
	 */
	CARD_FILTERS,
	/**
	 * A value from {@link Zones} that represents a place to or from a card will be affected.
	 *
	 * @see RecruitSpell for an example of a spell that uses this arg.
	 * @see StealCardSpell for an example of a spell that uses this arg.
	 */
	CARD_LOCATION,
	/**
	 * A {@link CardSource} that specifies an original list of cards that should be filtered, typically with a {@link
	 * SpellArg#CARD_FILTER}. Common choices for a card source include {@link CatalogueSource} and {@link
	 * UnweightedCatalogueSource}.
	 *
	 * @see DiscoverSpell for an example of a spell that uses this arg.
	 * @see SpellArg#CARD_FILTER for an arg that is commonly included with this arg.
	 * @see CardSource for more about card sources.
	 */
	CARD_SOURCE,
	CARD_SOURCES,
	/**
	 * Used by the {@link CreateCardSpell} to determine what kind of card to make. Considered obsolete.
	 */
	CARD_TYPE,
	/**
	 * An array version of {@link #CARD}. Typically interpreted as a group of cards, rather than as a random selection of
	 * one of these cards.
	 *
	 * @see ReceiveCardSpell for an example of a spell that uses this arg.
	 */
	CARDS,
	/**
	 * A {@link net.demilich.metastone.game.spells.desc.condition.Condition} that is evaluated against a {@code target}.
	 * It is typically interpreted as whether or not a spell should execute its subspell {@link SpellArg#SPELL} or do
	 * {@link SpellArg#SPELL1} versus {@link SpellArg#SPELL2} given the result of evaluating the condition.
	 *
	 * @see ConditionalSpell for an example of a spell that uses this arg.
	 */
	CONDITION,
	/**
	 * The array version of {@link #CONDITION}.
	 *
	 * @see ConditionalSpell for an example of a spell that uses this arg.
	 */
	CONDITIONS,
	/**
	 * A piece of text that gets written on the card using the {@link SetDescriptionSpell}, or a description to appear on
	 * a generated card in a {@link CastFromGroupSpell}.
	 * <p>
	 * Eventually, the value of this argument will support dynamic text generation and more advanced text manipulation.
	 */
	DESCRIPTION,
	/**
	 * A typically catch-all boolean argument.
	 * <p>
	 * In many cases, this arg is interpreted to mean the difference between affecting the {@code target} (when {@code
	 * true}) versus the base card (when {@code false}). Consult the spell's documentation for its specific
	 * interpretation.
	 * <p>
	 * In other cases, setting this arg to {@code true} suppresses the firing of an event. This is used by e.g. {@link
	 * ShuffleToDeckSpell} to prevent an infinite loop of shuffles for cards like Augmented Elekk.
	 * <p>
	 * In other cases, setting this arg to {@code true} only causes the "secondary" effect to occur in the spell. This is
	 * used by e.g. {@link ExcessDamageSpell} to only deal excess damage instead of both the regular and excess damage.
	 * <p>
	 * Finally, this arg is used by {@link SummonSpell} to only summon minions whose card ID is not already present on the
	 * casting player's battlefield.
	 * <p>
	 * Use "Find Usages" to see the various ways this arg is used.
	 */
	EXCLUSIVE,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.filter.EntityFilter} that is applied to the targets returned by
	 * the {@link #TARGET} or {@link net.demilich.metastone.game.targeting.TargetSelection} specified on the {@link
	 * CardDesc#getTargetSelection()}. After filtering, if no targets remain, the spell is not cast; or, if the spell has
	 * a target selection specified and no targets are available, the spell typically cannot be cast.
	 * <p>
	 * This arg is interpreted by the spell casting system and never by any {@link Spell} class in particular.
	 *
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) for how this arg is interpreted.
	 */
	FILTER,
	/**
	 * When {@code true}, indicates to {@link ModifyMaxManaSpell} whether it should give a player full or empty mana
	 * crystals.
	 */
	FULL_MANA_CRYSTALS,
	/**
	 * For spells where {@link #VALUE} has a separate interpretation from "copies" or "duplicates," this arg is typically
	 * used to indicate copies or duplicates.
	 * <p>
	 * Because {@link #VALUE} is used by {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)}
	 * to indicate how many random cards should be selected, this arg is typically used to indicate the number of copies
	 * of those cards. Therefore, a good rule of thumb is that the number of cards that will be operated on by a {@link
	 * SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)} call is {@code "value" x "howMany"}
	 * cards.
	 *
	 * @see MissilesSpell where this arg indicates the number of missiles.
	 * @see DiscoverSpell where this arg indicates the number of cards that should be shown in a discover.
	 */
	HOW_MANY,
	/**
	 * Used by {@link BuffSpell} to indicate how much additional full-healed health should be given to the {@code target}
	 * as a buff.
	 */
	HP_BONUS,
	/**
	 * Used by the {@link DamageSpell} to indicate that even though this effect is written on a spell card, the amount of
	 * damage dealt (the {@link SpellArg#VALUE}) should not be affected by spell damage.
	 */
	IGNORE_SPELL_DAMAGE,
	/**
	 * Used by the {@link CreateCardSpell} to indicate how much the card it creates should cost. Considered obsolete.
	 */
	MANA,
	/**
	 * Used by various spells to put a name on a card or to change the name of a target card.
	 *
	 * @see net.demilich.metastone.game.spells.custom.TextifySpell for an example of a spell that uses this arg.
	 */
	NAME,
	/**
	 * An {@link net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation} that is typically used to do
	 * math.
	 * <p>
	 * Currently only used by {@link ModifyDamageSpell}.
	 */
	OPERATION,
	/**
	 * Like a {@link SpellArg#QUEST} but a player can have multiple of these active at once.
	 */
	PACT,
	/**
	 * Used by the {@link SetRaceSpell} to change a target minion's race.
	 */
	RACE,
	/**
	 * Used by {@link AddQuestSpell} to define a {@link net.demilich.metastone.game.spells.trigger.secrets.Quest} to put
	 * into play.
	 */
	QUEST,
	/**
	 * When {@code true}, indicates that a random single target from the list of filtered targets should be cast on by
	 * this spell.
	 * <p>
	 * {@link SummonSpell}, when given a {@link SpellArg#CARDS} argument, will interpret this arg to mean that a single
	 * card should be chosen from the list of cards to summon.
	 *
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) for the complete targeting rules and how this arg is
	 * interpreted.
	 */
	RANDOM_TARGET,
	/**
	 * Used by the {@link RevertableSpell} series of spells to indicate which {@link
	 * net.demilich.metastone.game.spells.trigger.EventTrigger} will cause the effects to be "reverted."
	 *
	 * @see AddAttributeSpell for an example of a spell that uses this arg.
	 */
	REVERT_TRIGGER,
	/**
	 * Used by the {@link AddSecretSpell} to define a {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} to
	 * put into play.
	 */
	SECRET,
	/**
	 * Used by the {@link RevertableSpell} series of spells to indicate another {@link
	 * net.demilich.metastone.game.spells.trigger.EventTrigger} that will cause the effects to be "reverted."
	 *
	 * @see AddAttributeSpell for an example of a spell that uses this arg.
	 */
	SECOND_REVERT_TRIGGER,
	/**
	 * Used by the {@link CreateCardSpell} to name the card is creates. Considered obsolete.
	 */
	SECONDARY_NAME,
	/**
	 * Typically interpreted as a second target for effects that have a {@code source}, a {@code target}, and another
	 * entity that does something to the {@code target}.
	 * <p>
	 * The {@link net.demilich.metastone.game.targeting.EntityReference} here is typically interpreted using {@link
	 * GameContext#resolveSingleTarget(Player, Entity, EntityReference)}, a method which returns one entity from "group
	 * references" (i.e. {@link EntityReference#isTargetGroup()} {@code = true}).
	 *
	 * @see FightSpell for an example of a spell that uses this arg.
	 * @see AddDeathrattleSecondaryAsTargetSpell for an example of a spell that uses this arg.
	 */
	SECONDARY_TARGET,
	/**
	 * Indicates a second value. Implements some Reservoir effects and otherwise only used by Swipe spell.
	 */
	SECONDARY_VALUE,
	/**
	 * Typically the {@link SpellDesc} that is cast with {@link EntityReference#OUTPUT} set to the result of the parent
	 * spell's effect; or, the spell that is cast when a {@link net.demilich.metastone.game.spells.desc.condition.Condition}
	 * is met.
	 *
	 * @see ConditionalSpell for an example of a spell that uses this arg.
	 */
	SPELL,
	/**
	 * Like {@link #SPELL}, a {@link SpellDesc} that's typically interpreted as the "first" or primary spell.
	 *
	 * @see net.demilich.metastone.game.spells.AdjacentEffectSpell for an example of a spell that uses this arg.
	 */
	SPELL1,
	/**
	 * Like {@link #SPELL}, a {@link SpellDesc} that's typically interpreted as the "second" or alternative spell.
	 *
	 * @see DiscoverSpell for an exaple of a spell that uses this arg. There, it is cast on the cards the player did
	 * <b>not</b> choose.
	 */
	SPELL2,
	/**
	 * An array of spells, used almost exclusively by {@link net.demilich.metastone.game.spells.MetaSpell} as the spells
	 * to cast, one after another, or by {@link ConditionalSpell}, cast when the corresponding conditions in {@link
	 * #CONDITIONS} are met.
	 *
	 * @see net.demilich.metastone.game.spells.MetaSpell for an example of a spell that uses this arg.
	 * @see ConditionalSpell for an example of a spell that uses this arg.
	 */
	SPELLS,
	/**
	 * For internal use only.
	 */
	SUMMON_BASE_HP,
	/**
	 * For internal use only.
	 */
	SUMMON_BASE_ATTACK,
	/**
	 * For internal use only.
	 */
	SUMMON_WINDFURY,
	/**
	 * For internal use only.
	 */
	SUMMON_TAUNT,
	/**
	 * For internal use only.
	 */
	SUMMON_CHARGE,
	/**
	 * For internal use only.
	 */
	SUMMON_DIVINE_SHIELD,
	/**
	 * For internal use only.
	 */
	SUMMON_STEALTH,
	/**
	 * For internal use only.
	 */
	SUMMON_TRIGGERS,
	/**
	 * For internal use only.
	 */
	SUMMON_BATTLECRY,
	/**
	 * For internal use only.
	 */
	SUMMON_DEATHRATTLE,
	/**
	 * For internal use only.
	 */
	SUMMON_AURA,
	/**
	 * Specifies the {@link EntityReference} on which this spell should be cast, overriding the {@code target} received
	 * from the parent or selected by the user.
	 * <p>
	 * This entity reference, when a {@link EntityReference#isTargetGroup()} group reference, is filtered by {@link
	 * SpellArg#FILTER}. If the target turns out to be empty or the filtering removes all targets, the spell is <b>not</b>
	 * cast.
	 *
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) for more about how this argument is used.
	 */
	TARGET,
	/**
	 * Overrides the source of an effect with the specified {@link EntityReference}, which must resolve to zero to one
	 * targets.
	 * <p>
	 * Some references are still "group references" that typically refer to exactly one entity if it exists, or zero if it
	 * does not. For example, {@link EntityReference#FRIENDLY_WEAPON}. This is an appropriate specifier for source.
	 *
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) for more about how this argument is used.
	 */
	SOURCE,
	/**
	 * Indicates whose point of view this spell should be cast from. Typically becomes the {@code player} object in the
	 * spell's Spell#cast(GameContext, Player, SpellDesc, Entity, List) implementation {@code onCast}.
	 */
	TARGET_PLAYER,
	/**
	 * Used by the {@link CreateCardSpell} to indicate the card's target selection. Considered obsolete.
	 */
	TARGET_SELECTION,
	/**
	 * Specifies the {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc} enchantment that should be
	 * created, typically by the {@link net.demilich.metastone.game.spells.AddEnchantmentSpell}.
	 *
	 * @see net.demilich.metastone.game.spells.AddEnchantmentSpell for an example of a spell that uses this arg.
	 */
	TRIGGER,
	/**
	 * For internal use only.
	 */
	GROUP,
	/**
	 * The plural version of {@link #TRIGGER}.
	 */
	TRIGGERS,
	/**
	 * The value is either an integer or a {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider}
	 * which is typically interpreted as the amount or copies of an action.
	 * <p>
	 * For most spells that generate cards using {@link SpellUtils#getCards(GameContext, Player, Entity, Entity,
	 * SpellDesc, int)}, the value is interpreted as the number of cards to select from the randomly generated list. In
	 * that case, {@link #HOW_MANY} is used to indicate copies.
	 *
	 * @see DamageSpell for an example of a spell that uses this arg to mean amount.
	 * @see ReceiveCardSpell for an example of a spell that uses this arg to mean number of copies.
	 */
	VALUE,
	/**
	 * Specifies a battlecry as a {@link OpenerDesc} that will soon be added by a {@code AddBattlecrySpell}.
	 */
	BATTLECRY,
	/**
	 * Specifies a unique integer ID for a {@link SpellDesc} that is also a deathrattle, to allow deathrattles to identify
	 * themselves inside iterators for {@link HasDeathrattleEnchantments#getDeathrattleEnchantments()}.
	 */
	AFTERMATH_ID,
	/**
	 * Specifies which zones the spell's effects apply to. The interpretation may depend on the spell.
	 */
	ZONES
}
