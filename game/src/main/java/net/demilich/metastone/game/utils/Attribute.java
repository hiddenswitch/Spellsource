package net.demilich.metastone.game.utils;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.Zones;

import java.util.List;

public enum Attribute {
	/**
	 * The base mana cost of the {@link Card}.
	 *
	 * @see Card#getManaCost(GameContext, Player) to get the mana cost of a card considering all possible effects.
	 */
	BASE_MANA_COST,
	/**
	 * The number of hit points the {@link Actor} currently has.
	 */
	HP,
	/**
	 * The attack value written on the {@link MinionCard}. This is distinct from {@link #BASE_ATTACK}, which is the base
	 * attack value of the {@link Minion} this card would summon.
	 */
	ATTACK,
	/**
	 * An attack bonus that should be applied to the {@link Minion} attack.
	 */
	ATTACK_BONUS,
	/**
	 * The maximum number of hitpoints the {@link Actor} can have.
	 */
	MAX_HP,
	/**
	 * The current armor belonging to the {@link Actor}, or the additional armor gained by playing the specified {@link
	 * net.demilich.metastone.game.cards.HeroCard}.
	 */
	ARMOR,
	/**
	 * A one-turn long attack bonus given to the {@link Actor}.
	 */
	TEMPORARY_ATTACK_BONUS,
	/**
	 * The amount of hitpoints added by all the {@link net.demilich.metastone.game.spells.BuffSpell} effects on the
	 * entity.
	 */
	HP_BONUS,
	/**
	 * The amount of attack added by all the {@link net.demilich.metastone.game.spells.aura.Aura} effects that target
	 * the entity.
	 */
	AURA_ATTACK_BONUS,
	/**
	 * The amount of hitpoints added by all the {@link net.demilich.metastone.game.spells.aura.Aura} effects that target
	 * the entity.
	 */
	AURA_HP_BONUS,
	/**
	 * The base number of hitpoints for the {@link Actor}.
	 */
	BASE_HP,
	/**
	 * The base amount of attack for the {@link Actor}.
	 */
	BASE_ATTACK,
	/**
	 * A conditional attack bonus for the {@link Actor} that corresponds to bonuses from an {@link
	 * net.demilich.metastone.game.spells.EnrageSpell}, {@link net.demilich.metastone.game.spells.ConditionalAttackBonusSpell}
	 * or {@link net.demilich.metastone.game.spells.SetAttributeSpell}. This bonus is typically controlled by a {@link
	 * net.demilich.metastone.game.spells.desc.condition.Condition}.
	 */
	CONDITIONAL_ATTACK_BONUS,
	/**
	 * The race of the entity.
	 *
	 * @see net.demilich.metastone.game.entities.minions.Race
	 */
	RACE,
	/**
	 * When the entity has this attribute, it is destroyed. However, entities are also considered destroyed if their
	 * {@link Actor#getHp()} is below zero or if they are in the {@link Zones#GRAVEYARD} or {@link
	 * Zones#REMOVED_FROM_PLAY} zones.
	 * <p>
	 * At the end of {@link net.demilich.metastone.game.logic.GameLogic#performGameAction(int, GameAction)} in {@link
	 * GameLogic#checkForDeadEntities()}, all entities with {@link #DESTROYED} will be sent to the {@link
	 * Zones#GRAVEYARD}.
	 *
	 * @see Actor#isDestroyed() for a complete list of situations where an {@link Actor} is destroyed.
	 */
	DESTROYED,
	/**
	 * Fatigue is a game mechanic that deals increasing damage to players who have already drawn all of the cards in
	 * their deck, whenever they attempt to draw another card.
	 * <p>
	 * This attribute tracks how much damage a {@link net.demilich.metastone.game.entities.heroes.Hero} should take when
	 * the player draws a card.
	 *
	 * @see GameLogic#drawCard(int, Entity) for the complete usage of Fatigue.
	 */
	FATIGUE,
	/**
	 * A frozen {@link Actor} cannot attack. Freezing is cleared by a {@link net.demilich.metastone.game.spells.SilenceSpell}
	 * (when the minion is {@link #SILENCED}) or the owning player ends his turn on a different turn than when the
	 * minion was {@link #FROZEN}.
	 *
	 * @see GameLogic#silence(int, Minion) for a complete description of the silence effect.
	 * @see GameLogic#handleFrozen(Actor) to see where freezing is handled.
	 */
	FROZEN,
	/**
	 * This {@link Minion} will typically gain an attack bonus after it is dealt damage the first time.
	 *
	 * @see net.demilich.metastone.game.spells.EnrageSpell for the spell that encapsulates this effect.
	 * @see #CONDITIONAL_ATTACK_BONUS for the attribute that typically stores the amount of attack gained by an enrage.
	 */
	ENRAGABLE,
	/**
	 * Records that an {@link Entity} was silenced. Silencing clears all attributes and effects.
	 *
	 * @see GameLogic#silence(int, Minion) for a complete description of the silence effect.
	 */
	SILENCED,
	/**
	 * An {@link Actor} with {@link #WINDFURY} has two attacks per turn.
	 */
	WINDFURY,
	/**
	 * An {@link Actor} with {@link #MEGA_WINDFURY} has four attacks per turn.
	 *
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
	 */
	MEGA_WINDFURY,
	/**
	 * An {@link Actor} with {@link #UNLIMITED_ATTACKS} has unlimited attacks per turn.
	 *
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
	 */
	UNLIMITED_ATTACKS,
	/**
	 * An {@link Actor} with {@link #TAUNT} must be targeted by opposing {@link net.demilich.metastone.game.actions.PhysicalAttackAction}
	 * actions first. This means the {@link Minion} with {@link #TAUNT} acts like a shield for its other non-taunt
	 * minions and its owning player's hero, because the opposing minions and hero must attack the taunt minion first.
	 *
	 * @see net.demilich.metastone.game.logic.TargetLogic#getValidTargets(GameContext, Player, GameAction) for the
	 * complete targeting logic.
	 */
	TAUNT,
	/**
	 * The total amount of spell damage that an {@link Entity} contributes.
	 */
	SPELL_DAMAGE,
	/**
	 * Some cards give the opponent spell damage. This attribute stores how much.
	 */
	OPPONENT_SPELL_DAMAGE,
	/**
	 * A {@link Minion} with {@link #CHARGE} can attack the same turn it enters play.
	 *
	 * @see #SUMMONING_SICKNESS for the attribute that a {@link Minion} otherwise has which prevents it from attacking
	 * the same turn it is summoned.
	 */
	CHARGE,
	/**
	 * An attribute that tracks the number of attacks the {@link Actor} has this turn. Typically, actors start with 1
	 * attack every turn.
	 *
	 * @see #WINDFURY for the attribute that sets the number of attacks an actor has to 2 at the start of the owner's
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking. turn.
	 */
	NUMBER_OF_ATTACKS,
	/**
	 * An attribute used by Giant Sand Worm that refreshes the number of attacks it has.
	 *
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
	 */
	EXTRA_ATTACKS,
	/**
	 * When an {@link Actor} is {@link #ENRAGED}, its {@link #CONDITIONAL_ATTACK_BONUS} is set to the amount of damage
	 * gained by an {@link net.demilich.metastone.game.spells.EnrageSpell}.
	 *
	 * @see net.demilich.metastone.game.spells.EnrageSpell for the spell that implements Enrage.
	 * @see GameLogic#handleEnrage(Actor) for the logic that controls this attribute.
	 */
	ENRAGED,
	/**
	 * An {@link Entity} with {@link #BATTLECRY} performs an action when it goes from the {@link Zones#HAND} to the
	 * {@link Zones#BATTLEFIELD}. This attribute is used to look up / keep track of entities that have battlecries. It
	 * does not define the battlecry itself.
	 */
	BATTLECRY,
	/**
	 * An {@link Entity} with {@link #DOUBLE_BATTLECRIES} causes other friendly battlecries to occur twice.
	 * <p>
	 * This implements Brann Bronzebeard's text.
	 *
	 * @see GameLogic#performBattlecryAction(int, Actor, Player, BattlecryAction) for the complete rules on double
	 * battlecries.
	 */
	DOUBLE_BATTLECRIES,
	/**
	 * An {@link Entity} with {@link #DEATHRATTLES} casts a spell when it is destroyed.
	 * <p>
	 * This attribute does not store the spell itself. It marks an entity that has a deathrattle.
	 */
	DEATHRATTLES,
	/**
	 * An {@link Entity} with {@link #DOUBLE_DEATHRATTLES} causes other friendly deathrattles to occur twice.
	 * <p>
	 * This implements Baron Rivendare's text.
	 *
	 * @see GameLogic#resolveDeathrattles(Player, Actor, net.demilich.metastone.game.entities.EntityLocation) to see the
	 * complete rules for deathrattles.
	 */
	DOUBLE_DEATHRATTLES,
	/**
	 * An immune {@link Actor} does not take any damage.
	 */
	IMMUNE,
	/**
	 * An {@link Actor} with this attribute does not take damage from the targets of its physical attacks.
	 */
	IMMUNE_WHILE_ATTACKING,
	/**
	 * Marks that the {@link Actor} has a divine shield.
	 * <p>
	 * Divine shield causes the actor to take zero damage instead of the full damage it should receive the first time it
	 * receives damage.
	 *
	 * @see GameLogic#damage(Player, Actor, int, Entity) for the complete rules of damage.
	 */
	DIVINE_SHIELD,
	/**
	 * A {@link Minion} with stealth cannot be targeted by spells, hero powers or physical attacks until it attacks.
	 * <p>
	 * If a Stealthed minion attacks or deals any kind of damage, it will lose Stealth. This includes passive effects
	 * such as that of Knife Juggler, and dealing combat damage in exchange, such as when being struck by a clumsy
	 * minion such as Ogre Brute, or by a Misdirection-redirected minion.
	 *
	 * @see GameLogic#fight(Player, Actor, Actor) for the situation where physical attacks cause a minion to lose
	 * stealth.
	 * @see GameLogic#damage(Player, Actor, int, Entity, boolean) for the situation where any kind of damage originating
	 * from a minion causes it to lose stealth.
	 * @see net.demilich.metastone.game.logic.TargetLogic#filterTargets(GameContext, Player, GameAction, List) for the
	 * logic behind selecting valid targets.
	 */
	STEALTH,
	/**
	 * A {@link net.demilich.metastone.game.cards.SecretCard} has this attribute to help spells find secrets in the
	 * deck.
	 * <p>
	 * Cards marked secret should not be revealed to the opponent.
	 */
	SECRET,
	/**
	 * When a combo {@link Card} is played after another card, an effect is triggered.
	 *
	 * @see net.demilich.metastone.game.spells.ComboSpell for the actual implementation of combo effects.
	 * @see GameLogic#playCard(int, CardReference) for the control of the combo attribute.
	 */
	COMBO,
	/**
	 * Overload is an {@link Integer} amount of mana that will be locked (unavailable for use) the next turn by playing
	 * this {@link Card}.
	 */
	OVERLOAD,
	/**
	 * The {@link Integer} amount of mana overloaded overloaded by the player over the course of the game.
	 */
	OVERLOADED_THIS_GAME,
	/**
	 * A {@link Card} with this attribute signals that it has two options that a player chooses from when the card is
	 * played.
	 */
	CHOOSE_ONE,
	/**
	 * A {@link Minion} with this attribute causes both choose one options of a {@link Card} with {@link #CHOOSE_ONE} to
	 * be played.
	 * <p>
	 * This implements the Fandral Staghelm card text.
	 */
	BOTH_CHOOSE_ONE_OPTIONS,
	/**
	 * Summoning sickness prevents a {@link Minion} from attacking the same turn it is played or summoned. Minions with
	 * {@link #CHARGE} do not have summoning sickness.
	 * <p>
	 * Summoning sickness occurs however the minion entered the battlefield, whether through a play from the hand, a
	 * Summon effect, a put into battlefield effect, or a transform effect.
	 *
	 * @see GameLogic#summon(int, Minion, Card, int, boolean) for the complete summoning rules.
	 * @see net.demilich.metastone.game.spells.PutMinionOnBoardFromDeckSpell for an unusual situation where minions
	 * enter the battlefield.
	 * @see GameLogic#transformMinion(Minion, Minion) for an unusual situation where minions enter the battlefield.
	 */
	SUMMONING_SICKNESS,
	/**
	 * Marks an {@link Actor} to be untargetable by spells or hero powers. This includes the owner's spells and hero
	 * powers.
	 *
	 * @see net.demilich.metastone.game.logic.TargetLogic#filterTargets(GameContext, Player, GameAction, List) for the
	 * complete target selection logic.
	 */
	UNTARGETABLE_BY_SPELLS,
	/**
	 * An {@link Actor} with this attribute is untargetable by spells or hero powers due to an aura.
	 *
	 * @see #UNTARGETABLE_BY_SPELLS for more information.
	 */
	AURA_UNTARGETABLE_BY_SPELLS,
	/**
	 * When a {@link net.demilich.metastone.game.cards.SpellCard} that casts a {@link
	 * net.demilich.metastone.game.spells.DamageSpell} has this attribute, its bonus from spell damage is doubled.
	 *
	 * @see GameLogic#applySpellpower(Player, Entity, int) for an entity's spellpower contribution.
	 */
	SPELL_DAMAGE_MULTIPLIER,
	/**
	 * When any friendly {@link Entity} has this attribute, all friendly spell damage effects (typically cast by {@link
	 * net.demilich.metastone.game.spells.DamageSpell} deal damage multiplied by this attribute's value.
	 * <p>
	 * This implements Prophet Velen.
	 *
	 * @see GameLogic#damage(Player, Actor, int, Entity, boolean) for the full spell damage calculation.
	 */
	SPELL_AMPLIFY_MULTIPLIER,
	/**
	 * When any friendly {@link Entity} has this attribute, all friendly healing effects that use {@link
	 * GameLogic#heal(Player, Actor, int, Entity)} are multiplied by this attribute's value.
	 * <p>
	 * This implements Prophet Velen.
	 */
	HEAL_AMPLIFY_MULTIPLIER,
	/**
	 * Stores an adjustment to the cost of this {@link Card}, typically due to changes to deck or hand cards from
	 * various spells.
	 * <p>
	 * The value accumulated in this attribute is permanent, in the sense that the effect that changed it did not intend
	 * the changes to be reversible.
	 *
	 * @see GameLogic#getModifiedManaCost(Player, Card) for a complete calculation of spell damage.
	 */
	MANA_COST_MODIFIER,
	/**
	 * An attribute that specifies that the attack of this {@link Minion} is equal to its hitpoints.
	 */
	ATTACK_EQUALS_HP,
	/**
	 * When set, this {@link Minion} cannot attack.
	 */
	CANNOT_ATTACK,
	/**
	 * When set, this {@link Minion} cannot target heroes with physical attacks.
	 * <p>
	 * Unusually, this attribute affects the {@link net.demilich.metastone.game.actions
	 * .PhysicalAttackAction#canBeExecutedOn(GameContext, Player, Entity)} method instead of {@link
	 * net.demilich.metastone.game.logic.TargetLogic#filterTargets (GameContext, Player, GameAction, List)}.
	 */
	CANNOT_ATTACK_HEROES,
	/**
	 * Whens set on any entity, friendly healing effects deal damage instead.
	 */
	INVERT_HEALING,
	/**
	 * When set, the hitpoints of the {@link Actor} cannot be reduced below 1, typically just for the current turn.
	 */
	CANNOT_REDUCE_HP_BELOW_1,
	/**
	 * When a {@link Card} is played and countered (by e.g. Counterspell), it is marked with this attribute and its text
	 * is not executed.
	 *
	 * @see GameLogic#playCard(int, CardReference) for the complete card playing implementation.
	 */
	COUNTERED,
	/**
	 * This attribute records which turn a {@link Minion} was marked as {@link #DESTROYED}.
	 */
	DIED_ON_TURN,
	/**
	 * When any {@link Entity} has this attribute, the owning player's hero power can target a {@link Minion}.
	 * <p>
	 * This is useful for the Hunter's hero power, which ordinarily can only target the opponent's hero.
	 * <p>
	 * Implements Steamwheedle Sniper
	 */
	HERO_POWER_CAN_TARGET_MINIONS,
	/**
	 * When any {@link Entity} alive has this attribute, the owning player's hero power freezes its target.
	 * <p>
	 * Implements Ice Walker
	 */
	HERO_POWER_FREEZES_TARGET,
	/**
	 * When any {@link Entity} alive has this attribute, BOTH player's hero powers are disabled.
	 * <p>
	 * Implements Mindbreaker.
	 */
	HERO_POWERS_DISABLED,
	/**
	 * Records the amount of damage last sustained by an {@link Actor}. Typically used by an {@link
	 * net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider} to feed a value into a spell (e
	 * .g., a healing spell may heal the owning player by the amount of damage last dealt to an entity).
	 */
	LAST_HIT,
	/**
	 * Records the amount of healing last applied to this {@link Actor}.
	 */
	LAST_HEAL,
	/**
	 * Marks that this {@link Entity} has a passive trigger that activates to a {@link
	 * net.demilich.metastone.game.events.GameEvent}.
	 *
	 * @see net.demilich.metastone.game.spells.trigger.TriggerManager for the complete rules on event triggering.
	 * @see Enchantment for the entity that corresponds to a passive trigger.
	 */
	PASSIVE_TRIGGERS,
	/**
	 * Marks that this {@link Card} has a trigger that should be active while it is in the deck.
	 *
	 * @see #PASSIVE_TRIGGERS for an attribute that marks the entity has a trigger that is only active in the player's
	 * battlefield or hand.
	 */
	DECK_TRIGGER,
	/**
	 * This attribute keeps track of how many times the hero power was used this turn.
	 *
	 * @see GameLogic#canPlayCard(int, CardReference) for the implementation that determines whether or not a card, like
	 * a hero power card, can be played.
	 */
	HERO_POWER_USAGES,
	/**
	 * An {@link Entity} with hero power damage contributes to the total hero power damage the player gets as a bonus to
	 * their base hero power damage. Applies to {@link net.demilich.metastone.game.spells.DamageSpell} based hero
	 * powers.
	 */
	HERO_POWER_DAMAGE,
	/**
	 * Shadowform implements a hero power upgrading mechanic.
	 * <p>
	 * Implements the Shadowform card.
	 *
	 * @see net.demilich.metastone.game.spells.ChangeHeroPowerSpell for hero power changing spells.
	 */
	SHADOWFORM,
	/**
	 * This attribute keeps track of how much attack should be added to C'Thun.
	 * <p>
	 * Implements the C'Thun mechanic.
	 *
	 * @see net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider for the value provider that
	 * reads attributes like these and provides values to various spells.
	 */
	CTHUN_ATTACK_BUFF,
	/**
	 * This attribute keeps track of how many hitpoints should be added to C'Thun.
	 * <p>
	 * Implements the C'Thun mechanic.
	 *
	 * @see net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider for the value provider that
	 * reads attributes like these and provides values to various spells.
	 */
	CTHUN_HEALTH_BUFF,
	/**
	 * This attribute marks that C'Thun will have Taunt when it is summoned.
	 * <p>
	 * Implements the C'Thun mechanic.
	 */
	CTHUN_TAUNT,
	/**
	 * When any {@link Entity} has this attribute in play, spells cost health instead of mana.
	 * <p>
	 * This attribute implements Chogall.
	 */
	SPELLS_COST_HEALTH,
	/**
	 * When any {@link Entity} has this attribute in play, a {@link MinionCard} with the {@link
	 * net.demilich.metastone.game.entities.minions.Race#MURLOC} costs health instead of mana.
	 * <p>
	 * This attribute implements Seadevil Stinger.
	 */
	MURLOCS_COST_HEALTH,
	/**
	 * A {@link net.demilich.metastone.game.entities.heroes.Hero} with this attribute does not take damage.
	 *
	 * @see GameLogic#damageHero(Hero, int) for the complete damage implementation for heroes.
	 */
	IMMUNE_HERO,
	/**
	 * An {@link Entity} with this attribute takes twice the damage whenever it is dealt damage.
	 *
	 * @see GameLogic#damage(Player, Actor, int, Entity, boolean) for the complete damage implementation.
	 */
	TAKE_DOUBLE_DAMAGE,
	/**
	 * A {@link Minion} with this attribute cannot target a {@link Hero} the same turn it is summoned. This is typically
	 * given to a {@link #CHARGE} minion that would be too powerful it it could target a hero.
	 *
	 * @see net.demilich.metastone.game.actions.PhysicalAttackAction for a complete implementation of what a minion can
	 * attack.
	 */
	CANNOT_ATTACK_HERO_ON_SUMMON,
	/**
	 * An attribute that keeps track of how much attack and hitpoints to add to the next Jade Golem that gets summoned.
	 */
	JADE_BUFF,
	/**
	 * When any {@link Entity} has this attribute, spells are cast with random targets, random discover choices are
	 * made, physical attacks target randomly, and battlecries target randomly.
	 * <p>
	 * Implements Yogg-Saron, Hope's End; Servant of Yogg-Saron; Mayor Noggenfogger
	 */
	RANDOM_CHOICES,
	/**
	 * A {@link #QUEST} {@link Entity} is an untargetable permanent that lives in the {@link Zones#SECRET} zone but is
	 * visible to the opponent.
	 * <p>
	 * Implements quest cards.
	 */
	QUEST,
	/**
	 * An attribute given to {@link Card} entities that started in the player's deck, as opposed to being generated by
	 * other cards.
	 * <p>
	 * Implements Open the Waygate quest.
	 */
	STARTED_IN_DECK,
	/**
	 * This attribute describes a {@link Minion} that can never be targeted by spells, abilities, auras or physical
	 * attacks but does occupy a position on the {@link Zones#BATTLEFIELD}.
	 * <p>
	 * Implements permanents.
	 */
	PERMANENT,
	/**
	 * This attribute is a {@link String} that describes the inventory owner (as opposed to in-match owner) of the
	 * card.
	 */
	USER_ID,
	/**
	 * This {@link String} describes the instance of this specific entity inside the particular match.
	 */
	ENTITY_INSTANCE_ID,
	/**
	 * This {@link String} is the inventory record ID of this card.
	 */
	CARD_INVENTORY_ID,
	/**
	 * This {@link String} is the ID of the deck this card is currently put into.
	 */
	DECK_ID,
	/**
	 * This {@link String} is the user ID of the player who opened the card pack that contained this card.
	 */
	@Deprecated
	DONOR_ID,
	/**
	 * This {@link String} is the user ID of the player who is currently using a card that belongs to someone else.
	 */
	@Deprecated
	CHAMPION_ID,
	/**
	 * This {@link String[]} are the collections this card belongs to, like the deck, user and alliances.
	 */
	COLLECTION_IDS,
	/**
	 * This {@link String} is the ID of the alliance this card belongs to, if any.
	 */
	ALLIANCE_ID,
	/**
	 * Every time a player summons this {@link Minion} for the first time in their lifetime of the game, the {@link
	 * MinionCard} is incremented. This attribute persists between matches.
	 */
	UNIQUE_CHAMPION_IDS_SIZE,
	/**
	 * Every unique user ID that has summoned this minion is stored in this attribute's array of {@link String}.
	 */
	UNIQUE_CHAMPION_IDS,
	/**
	 * Every time an {@link Actor} destroys a {@link Minion}, the {@link String} card ID is stored in this attribute.
	 * This attribute persists between matches.
	 */
	LAST_MINION_DESTROYED_CARD_ID,
	/**
	 * Every time an {@link Actor} destroys a {@link Minion}, the {@link String} card inventory ID is stored in this
	 * attribute. This attribute persists between matches.
	 */
	LAST_MINION_DESTROYED_INVENTORY_ID,
	/**
	 * Every time an {@link Actor} damages a target, increment this attribute with the total amount of damage dealt.
	 */
	TOTAL_DAMAGE_DEALT,
	/**
	 * Every time an {@link Actor} is healed, increment this attribute with the amount of healing and set to zero at the
	 * end of the turn.
	 */
	HEALING_THIS_TURN,
	/**
	 * Whenever an {@link Actor} dies, increment this attribute by 1 if the actor had the lowest attack on the
	 * battlefield.
	 */
	WEAKEST_ON_BATTLEFIELD_WHEN_DESTROYED_COUNT,
	/**
	 * A shorthand implementation of the Poisonous keyword. Indicates that whenever the source minion deals more than 0
	 * damage to the target minion, the target minion  is destroyed.
	 */
	POISONOUS,
	/**
	 * A shorthand implementation of the Lifesteal keyword. Indicates that the {@link Hero} of the owner of the {@link
	 * Minion} should be healed by the amount of damage dealt by that minion.
	 */
	LIFESTEAL,
	/**
	 * Indicates that the {@link Player}'s end turn triggers should trigger twice.
	 * <p>
	 * Implements Drakkari Enchanter.
	 */
	DOUBLE_END_TURN_TRIGGERS,
	/**
	 * Indicates that the specified spell card was casted from the hand or the deck.
	 */
	CAST_FROM_HAND_OR_DECK,
	/**
	 * Overrides the name of the {@link Entity}
	 */
	NAME,
	/**
	 * Overrides the description of the {@link Entity}
	 */
	DESCRIPTION,
	/**
	 * Indicates how many extra turns the player has.
	 * <p>
	 * Implements Open the Waygate.
	 */
	EXTRA_TURN,
	/**
	 * Indicates the amount of time, in seconds, a player has to perform their turn.
	 */
	TURN_TIME,
	/**
	 * Indicates the time, in millis since the beginning of the match, that a player's turn was started.
	 */
	TURN_START_TIME_MILLIS,
	/**
	 * Indicates the time, in millis, that a game was started
	 */
	GAME_START_TIME_MILLIS,
	/**
	 * Indicates how many cards the player has discarded during the game.
	 */
	DISCARDED,
	/**
	 * Indicates that the game has started for the specified player.
	 */
	GAME_STARTED,
	/**
	 * For entities that are in the {@link Zones#REMOVED_FROM_PLAY} zone due to a transform effect, this attribute
	 * points to the entity that replaced this one.
	 */
	TRANSFORM_REFERENCE,
	/**
	 * Remembers the {@link #ATTACK_BONUS}, {@link #HP_BONUS}, {@link #LIFESTEAL}, {@link #WINDFURY}, {@link #POISONOUS}
	 * that was applied to the {@link Actor} that gets subjected to a {@link net.demilich.metastone.game.spells.ShuffleToDeckSpell}.
	 * <p>
	 * Implements Kingsbane.
	 */
	KEEPS_ENCHANTMENTS,
	/**
	 * Allows spell effects to count and keep track of things, interpreted however they'd like.
	 * <p>
	 */
	RESERVED_INTEGER_1,
	RESERVED_INTEGER_2,
	RESERVED_INTEGER_3,
	RESERVED_INTEGER_4,
	/**
	 * Allows spell effects to mark  things, interpreted however they'd like.
	 */
	RESERVED_BOOLEAN_1,
	RESERVED_BOOLEAN_2,
	RESERVED_BOOLEAN_3,
	RESERVED_BOOLEAN_4;

	public String toKeyCase() {
		return ParseUtils.toCamelCase(this.toString());
	}
}
