package net.demilich.metastone.game.cards.desc;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;

/**
 * The class that card JSON files deserialize (get decoded) into.
 * <p>
 * All types of cards are encoded as a {@link CardDesc}; spells, secrets, hero cards, hero powers, minions, etc. This
 * class should only store the data related to the card, not card functionality itself.
 * <p>
 * Each of the fields in this class correspond exactly to the fields in a {@code .json} file located in the {@code
 * cards} directory. A card JSON looks like:
 *
 * <pre>
 *           {
 *             "name": "Angry Primate",
 *             "baseManaCost": 1,
 *             "type": "MINION",
 *             "heroClass": "ANY",
 *             "baseAttack": 1,
 *             "baseHp": 2,
 *             "rarity": "COMMON",
 *             "race": "BEAST",
 *             "description": "Battlecry: Add a Banana to your hand.",
 *  line 56:   "battlecry": {
 *               "targetSelection": "NONE",
 *               "spell": {
 *                 "class": "ReceiveCardSpell",
 *  line 60:       "cards": [
 *                   "spell_bananas"
 *                 ]
 *               }
 *             },
 *  line 65:   "attributes": {
 *               "BATTLECRY": true
 *             },
 *             "collectible": true,
 *             "set": "CUSTOM",
 *             "fileFormatVersion": 1
 *           }
 * </pre>
 * Observe that each of the keys in the JSON, or the text matching the quotation marks, matches a field in this class.
 * So {@code "name"} corresponds to the {@link #name} field, {@code "battlecry"} corresponds to the {@link #battlecry}
 * field, etc.
 * <p>
 * To figure out the format of the value of complex objects like "battlecry" on line 56 in the example above, look at
 * the <b>type</b> of the field in this class. In the case of battlecry, the type is a {@link BattlecryDesc}, and it
 * appears to also have fields that exactly correspond to the keys that appear in the JSON object that is the value of
 * "battlecry."
 * <p>
 * Some objects, like {@code "spell"}, are {@link Desc} classes: these use a corresponding "argument" enumeration to
 * determine the names and types of the fields. In the case of {@link SpellDesc}, the keys are {@link SpellArg} names,
 * except written in camelcase. In the example of Angry Primate above, the {@code "cards"} key on line 60 inside the
 * {@link SpellDesc} corresponds to the {@link SpellArg#CARDS} enum name. Observe that {@code "cards"} is just {@link
 * SpellArg#CARDS} except lowercase.
 * <p>
 * The only exception to this rule is the {@link AttributeMap} object located on line 65 in the example above. The keys
 * (left hand part in quotation marks of the JSON object) should always be capitalized, and correspond exactly to the
 * names in {@link Attribute}.
 *
 * @see Card for the gameplay functionality of a card that consults data stored in a {@link CardDesc}.
 */
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
public final class CardDesc implements Serializable, Cloneable {
	/**
	 * The ID of the card when referred to by other cards and other places in the game engine.
	 * <p>
	 * Typically, the ID is not specified inside the card file. It is assumed to be the file name of the card file, minus
	 * the JSON extension. For example, the card {@code minion_bloodfen_raptor.json} will have its ID field assigned to
	 * {@code minion_bloodfen_raptor}.
	 * <p>
	 * IDs should not be changed after a public server release is made, even when there is a misspelling or other issue.
	 * Player's inventories store references to the underlying cards using the IDs, and changing the IDs will damage those
	 * references. The {@code Spellsource} migrations system provides a mechanism for changing IDs of cards after they
	 * have been given to players; see the {@code net} module's {@code Spellsource} class for details.
	 */
	public String id;
	/**
	 * The name of the card that should be rendered in the client. The name is also used for some card mechanics, like The
	 * Caverns Below. The name can be overridden by {@link Attribute#NAME} on the card entity.
	 *
	 * @see Card#getName() for the complete usage of the name field.
	 */
	public String name;
	/**
	 * A description of the card that should be rendered in the client. This field does <b>not</b> support formatting
	 * specifiers like bolding, italics, etc.
	 *
	 * @see Card#getDescription() for the complete usage of the description field.
	 */
	public String description;
	/**
	 * Indicates whether this card will participate in the determination of legacy mechanics, the storing of data about
	 * cards across all matches.
	 *
	 * @see Attribute#TOTAL_DAMAGE_DEALT for an example of a legacy mechanic.
	 */
	public Boolean legacy;
	/**
	 * The type of card this instance describes.
	 */
	public CardType type;
	/**
	 * The hero class this card belongs to.
	 * <p>
	 * Choose {@link HeroClass#ANY} for a neutral card.
	 */
	public HeroClass heroClass;
	/**
	 * For tri-class cards from the MSOG Hearthstone expansion, this field contains their three classes. Typically
	 * uninteresting to use for custom cards.
	 */
	public HeroClass[] heroClasses;
	/**
	 * The rarity of the card. Use {@link Rarity#FREE} for tokens, and {@link Rarity#ALLIANCE} for {@link #legacy} cards.
	 */
	public Rarity rarity;
	/**
	 * The set this card belongs to. Unless the card's author designed this card in the context of a greater set, use
	 * {@link CardSet#CUSTOM} for community cards.
	 * <p>
	 * Eventually, a set will be immutable and represent a particular release or expansion, while a {@link
	 * net.demilich.metastone.game.decks.DeckFormat} will represent a certain set of rules of play.
	 */
	public CardSet set;
	/**
	 * The base mana cost of the card. All cards should have this field set, even if they are virtual / non-acting cards.
	 * Use {@code 0} as the cost of those cards.
	 * <p>
	 * Choice cards for {@link CardType#CHOOSE_ONE} cards and {@link CardType#MINION} cards with choose-one battlecries
	 * that transform into another {@link Minion} should have the same cost as the parent card. For example, the choice
	 * card of Wrath should still have Wrath's cost, not {@code 0}.
	 */
	public int baseManaCost;
	/**
	 * Indicates whether or not the card should appear in discovers and in the collection browser. Choose one choice
	 * cards, virtual choice cards, tokens, base heroes, and previous versions of other cards ("unnerfed" cards) should
	 * not be collectible.
	 */
	public boolean collectible = true;
	/**
	 * Represents a key-value collection of {@link Attribute}. {@link AttributeMap} is a {@link java.util.Map} type, so in
	 * JSON, it will be represented by a bracketed object.
	 * <p>
	 * For example, a spell with lifesteal will have a field {@code "attributes"} that looks like:
	 * <pre>
	 *     "attributes": {
	 *         "LIFESTEAL": true
	 *     }
	 * </pre>
	 * Not all attributes are {@link Boolean}. For example, an {@link Attribute#OVERLOAD} card that reads, "Overload: 3"
	 * will have a value equal to amount of overload the card will give:
	 * <pre>
	 *     "attributes": {
	 *         "OVERLOAD": 3
	 *     }
	 * </pre>
	 *
	 * @see Attribute for a full description of attributes. Some of them are not appropriate to put on a card, because
	 * they are ephemeral (that is, they are only on a {@link net.demilich.metastone.game.entities.Entity} while it is in
	 * play, not on a card definition like {@link CardDesc}).
	 */
	public AttributeMap attributes;
	/**
	 * Indicates the version of this card description. Defaults to {@code 1}. When new expansions are released, this
	 * number should be incremented for the new cards to help the game record traces and reproduce bugs (only the cards
	 * that were around for a particular release of the code should participate in a game trace's lookup of cards in the
	 * {@link net.demilich.metastone.game.cards.CardCatalogue}, for example).
	 */
	public int fileFormatVersion = 1;
	/**
	 * Indicates an amount the card's cost should be subtracted by while the card is in the player's hand.
	 * <p>
	 * For example, to decrease the card's cost for each other card in the player's hand, the {@code "manaCostModifier"}
	 * field in the {@link CardDesc} would look like:
	 * <pre>
	 * "manaCostModifier": {
	 *      "class": "PlayerAttributeValueProvider",
	 *      "offset": -1,
	 *      "playerAttribute": "HAND_COUNT",
	 *      "targetPlayer": "SELF"
	 * }
	 * </pre>
	 */
	public ValueProviderDesc manaCostModifier;
	/**
	 * Describes an {@link Enchantment} that is active while the card is in the player's {@link Zones#HAND}.
	 * <p>
	 * For example, to reduce the cost of a card each turn the card is in the player's hand, the {@code "passiveTrigger"}
	 * field should look like:
	 * <pre>
	 *     "passiveTrigger": {
	 *          "eventTrigger": {
	 *              "class": "TurnStartTrigger",
	 *              "targetPlayer": "SELF"
	 *          },
	 *          "spell": {
	 *              "class": "CardCostModifierSpell",
	 *              "target": "SELF",
	 *              "cardCostModifier": {
	 *                  "class": "CardCostModifier",
	 *                  "target": "SELF",
	 *                  "value": 1,
	 *                  "operation": "SUBTRACT"
	 *              }
	 *          }
	 *      }
	 * </pre>
	 *
	 * @see Enchantment for more about enchantments.
	 */
	public EnchantmentDesc passiveTrigger;
	/**
	 * Describes an array of {@link Enchantment}s that are active while the card is in the player's {@link Zones#HAND}.
	 * <p>
	 * Arrays in JSON are specified using {@code []}. Place what you would ordinarily put in the value part (to the right
	 * of the colon) for {@link #passiveTrigger} into the brackets here. For example:
	 * <pre>
	 *     "passiveTriggers": [
	 *          {
	 *              ...
	 *          },
	 *          {
	 *              ...
	 *          }
	 *     ]
	 * </pre>
	 */
	public EnchantmentDesc[] passiveTriggers;
	/**
	 * Indiciates an {@link Enchantment} that is active while the card is in the player's {@link Zones#DECK}.
	 */
	public EnchantmentDesc deckTrigger;
	/**
	 * Indicates an {@link Enchantment} that is active as soon as the game begins (just after {@link
	 * net.demilich.metastone.game.logic.GameLogic#handleMulligan(Player, boolean, GameLogic.FirstHand, List)}, in the
	 * {@link GameLogic#startGameForPlayer(Player)} phase.
	 * <p>
	 * Note that the {@link net.demilich.metastone.game.events.GameStartEvent} is raised twice, once for each player, so
	 * your {@link EventTriggerDesc} should specify a {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#TARGET_PLAYER}.
	 * <p>
	 * For example, consider the text, "Passive: You draw an extra card at the start of every turn." A passive is
	 * implemented as a game trigger, and typically it puts another enchantment into play. Observe that the {@link
	 * net.demilich.metastone.game.spells.trigger.GameStartTrigger} is used to do something when the game starts, while
	 * the actual effect is implemented by a different enchantment:
	 * <pre>
	 *     "gameTriggers": [{
	 *         "eventTrigger": {
	 *             "class": "GameStartTrigger",
	 *             "targetPlayer": "SELF"
	 *         },
	 *         "spell": {
	 *             "class": "AddEnchantmentSpell",
	 *             "trigger": {
	 *                 "eventTrigger": {
	 *                     "class": "TurnStartTrigger",
	 *                     "targetPlayer": "SELF"
	 *                 },
	 *                 "spell": {
	 *                     "class": "DrawCardSpell"
	 *                 }
	 *             }
	 *         }
	 *     }]
	 * </pre>
	 */
	public EnchantmentDesc[] gameTriggers;
	/**
	 * Indicates the author of this card.
	 * <p>
	 * This field will be migrated to indicate where this username originated from (e.g., Discord versus Reddit versus
	 * Spellsource).
	 */
	public String author;
	/**
	 * Stores flavor text provided by the author.
	 */
	public String flavor;
	/**
	 * Stores notes about the card's implementation or behaviour. Use this field to explain surprising rules or to do a
	 * Q&A.
	 * <p>
	 * This field will be migrated to support Markdown syntax in the future for better rendering controls in the client.
	 */
	public String wiki;
	/**
	 * Specifies the minion, hero or weapon's battlecry.
	 * <p>
	 * Battlecries are always executed whenever the {@link net.demilich.metastone.game.cards.Card} is played from the
	 * hand.
	 * <p>
	 * In order to be counted as a "Battlecry" minion, the card's {@link CardDesc#attributes} must contain a {@link
	 * Attribute#BATTLECRY} key with {@code true}.
	 *
	 * @see BattlecryDesc for more about battlecries.
	 */
	public BattlecryDesc battlecry;
	/**
	 * Specifies the minion, hero or weapon's deathrattle.
	 *
	 * @see SpellDesc for more about deathrattles.
	 */
	public SpellDesc deathrattle;
	/**
	 * Specifies the minion, hero, or weapon's {@link Enchantment} that become active when the actor goes into an in-play
	 * zone ({@link Zones#BATTLEFIELD}, {@link Zones#WEAPON}, {@link Zones#HERO}).
	 * <p>
	 * {@link CardType#HERO_POWER} should have its {@link #passiveTrigger} or {@link #passiveTriggers} fields set instead
	 * of this one, because hero powers behave like an extension of your hand and not like a place in the battlefield.
	 */
	public EnchantmentDesc trigger;
	/**
	 * Multiple {@link #trigger} objects that should come into play whenever the actor comes into an in-play zone.
	 */
	public EnchantmentDesc[] triggers;
	/**
	 * The aura that is active whenever the actor is in a in-play zone ({@link Zones#BATTLEFIELD}, {@link Zones#WEAPON},
	 * {@link Zones#HERO}). {@link Card} entities do not support auras, since they are not in play.
	 * <p>
	 * Auras describe ongoing effects on other cards. They are updated at the end of a sequence (i.e., when actors are
	 * removed from the battlefield) and whenever the "board" (the three in-play zones) change. Updated means the affected
	 * actors are recalculated.
	 * <p>
	 * Auras are appropriate for effects like Ironwood Golem, which reads "Taunt. Can only attack if you have 3 or more
	 * Armor.":
	 * <pre>
	 *      "aura": {
	 *          "class": "AttributeAura",
	 *          "target": "SELF",
	 *          "condition": {
	 *              "class": "AttributeCondition",
	 *              "target": "FRIENDLY_HERO",
	 *              "value": 3,
	 *              "attribute": "ARMOR",
	 *              "operation": "LESS"
	 *          },
	 *          "attribute": "AURA_CANNOT_ATTACK",
	 *          "secondaryTrigger": {
	 *              "class": "ArmorChangedTrigger",
	 *              "targetPlayer": "SELF"
	 *          }
	 * }
	 * </pre>
	 * Observe that this aura, an {@link net.demilich.metastone.game.spells.aura.AttributeAura}, has a condition to
	 * indicate when the specified attribute should or should not be present on the target ({@link
	 * net.demilich.metastone.game.targeting.EntityReference#SELF}). Also observe that the attribute is prefixed with
	 * {@code AURA_} indicating that, as opposed to an effect that is applied once (e.g {@link Attribute#CANNOT_ATTACK}),
	 * it is an "aura" effect, and it won't be removed by a silence.
	 *
	 * @see net.demilich.metastone.game.spells.aura.Aura for more about auras.
	 */
	public AuraDesc aura;
	/**
	 * The auras that are active whenever the actor is in play.
	 */
	public AuraDesc[] auras;
	/**
	 * The actor's race, or "tribe."
	 */
	public Race race;
	/**
	 * A card cost modifier that is active whenever the actor is in play.
	 */
	public CardCostModifierDesc cardCostModifier;
	/**
	 * The base attack of the minion. This will be the {@link Actor#getBaseAttack()} value.
	 */
	public int baseAttack;
	/**
	 * The base HP of the minion. This will be the {@link Actor#getBaseHp()} value.
	 */
	public int baseHp;
	/**
	 * Whenever a {@link CardType#MINION} has choose one battlecries, the player will be given an option of which
	 * battlecry will be played for the minion.
	 * <p>
	 * Typically, choose one battlecries that put a "different" minion into play will be implemented using a {@link
	 * net.demilich.metastone.game.spells.TransformMinionSpell}. In this common situation, the client will render the
	 * choice card as the {@link SpellArg#CARD} argument of the spell, i.e., the minion the base minion will transform
	 * into.
	 * <p>
	 * The {@link SpellArg#CARD} specified by the transform spell should not be {@link #collectible}.
	 */
	public BattlecryDesc[] chooseOneBattlecries;
	/**
	 * Indicates the battlecry that will be played when Fandral Staghelm is in play, if this {@link CardType#MINION} has
	 * {@link #chooseOneBattlecries} specified.
	 */
	public BattlecryDesc chooseBothBattlecry;
	/**
	 * Whenever the card is a {@link CardType#CHOOSE_ONE} and this field is specified, the player will get to choose
	 * between these two cards for their effects. The choice card is put into the {@link Zones#SET_ASIDE_ZONE}, cast, and
	 * then put into {@link Zones#REMOVED_FROM_PLAY}, while the base card is moved to the {@link Zones#GRAVEYARD}.
	 * <p>
	 * These choice cards should not be {@link #collectible}.
	 */
	public String[] chooseOneCardIds;
	/**
	 * Indicates the spell card that will be cast when Fandral Staghelm is in play, if this is a {@link
	 * CardType#CHOOSE_ONE} card.
	 */
	public String chooseBothCardId;
	/**
	 * Indicates the amount of damage this {@link CardType#WEAPON} will deal (add to the attack of the equipping {@link
	 * net.demilich.metastone.game.entities.heroes.Hero}).
	 */
	public int damage;
	/**
	 * Indicates the durability of this {@link CardType#WEAPON}.
	 */
	public int durability;
	/**
	 * Indicates a spell that should be cast when the weapon enters the battlefield/an in-play zone, regardless of how it
	 * is put into play (i.e., unlike a battlecry, which is only activated by cards played from the hand).
	 * <p>
	 * Contemporaneously, such effects are better implemented by {@link #aura}.
	 */
	public SpellDesc onEquip;
	/**
	 * Indicates a spell taht shoudl be cast when the weapon exits the battlefield/an in-play zone, regardless of how it
	 * is removed.
	 * <p>
	 * Contemporaneously, such effects are better implemented by {@link #aura}.
	 */
	public SpellDesc onUnequip;
	/**
	 * Indicates a {@link CardType#HERO_POWER} that this {@link CardType#HERO} should put into play for the player.
	 *
	 * @see GameLogic#changeHero(Player, Hero) for more about how heroes come into play.
	 */
	public String heroPower;
	/**
	 * Indicates what kind of target selection this {@link CardType#SPELL} or {@link CardType#HERO_POWER} has. Any choice
	 * other than {@link TargetSelection#NONE} will prompt the user to pick a target as filtered by the {@link #spell}
	 * field's {@link SpellArg#FILTER} field.
	 * <p>
	 * For example, to indicate a spell should prompt the user to choose any Murloc on the battlefield:
	 * <pre>
	 *     "targetSelection": "MINIONS",
	 *     "spell": {
	 *         "class": "...",
	 *         "filter": {
	 *             "class": "RaceFilter",
	 *             "race": "MURLOC"
	 *         },
	 *         "..."
	 *     }
	 * </pre>
	 * Observe that a {@code "target"} of {@link net.demilich.metastone.game.targeting.EntityReference#ALL_MINIONS} is not
	 * specified on the {@code "spell"} field; the target is set to the player's choice, as filtered by {@code "filter"}.
	 */
	public TargetSelection targetSelection;
	/**
	 * Indicates the spell that this {@link CardType#SPELL} or {@link CardType#HERO_POWER} should cast when {@link
	 * Card#play()}.
	 * <p>
	 * For {@link CardType#SPELL} that contain a {@link #secret} or {@link #quest} field set, this spell is cast when the
	 * secret or quest is activated.
	 */
	public SpellDesc spell;
	/**
	 * The {@link net.demilich.metastone.game.spells.desc.condition.Condition} that must be met in order for this {@link
	 * CardType#SPELL} to be playable.
	 */
	public ConditionDesc condition;
	/**
	 * Indicates the subspells/subcards of this {@link CardType#GROUP}.
	 * <p>
	 * Used for Adaptation effects.
	 */
	public SpellDesc[] group;
	/**
	 * Indicates the {@link EventTrigger} for this secret.
	 */
	public EventTriggerDesc secret;
	/**
	 * Indicates the {@link EventTrigger} that will increase the number of fires of the quest by one.
	 */
	public EventTriggerDesc quest;
	/**
	 * Indicates the number of times the {@link #quest} trigger needs to fire until this quest's {@link #spell} is cast.
	 */
	public int countUntilCast;

	/**
	 * Creates a {@link Card} entity with no ID or location backed by this {@link CardDesc}.
	 *
	 * @return A card.
	 */
	@JsonIgnore
	public Card create() {
		return new Card(this);
	}

	public boolean getCollectible() {
		return collectible;
	}

	/**
	 * Retrieves a battlecry action specified on this card.
	 *
	 * @return A battlecry action.
	 */
	@JsonIgnore
	public BattlecryAction getBattlecryAction() {
		if (battlecry == null) {
			return null;
		}
		BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecry.spell, battlecry.getTargetSelection());
		if (battlecry.condition != null) {
			battlecryAction.setCondition(battlecry.condition.create());
		}
		return battlecryAction;
	}

	@Override
	public CardDesc clone() {
		try {
			CardDesc clone = (CardDesc) super.clone();
			if (attributes != null) {
				clone.attributes = attributes.clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
