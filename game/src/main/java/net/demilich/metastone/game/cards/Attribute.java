package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.RoastSpell;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A list of attributes on entities.
 * <p>
 * This represents the "columns" of the proverbial data that is stored in an entity. Many keywords, like <b>stealth</b>,
 * have a corresponding attribute. But it really depends on how the implementation works.
 * <p>
 * The attributes here interact with many mechanics. They are also the keys to an {@link AttributeMap} and {@link
 * CardAttributeMap}.
 */
public enum Attribute {
	/**
	 * The base mana cost of the {@link Card}.
	 *
	 * @see Card#getManaCost(GameContext, Player) to get the mana cost of a card considering all possible effects.
	 */
	BASE_MANA_COST,
	/**
	 * Indicates that this {@link Card} costs health instead of mana when played from the hand.
	 */
	COSTS_HEALTH_INSTEAD_OF_MANA,
	/**
	 * An Aura version of {@link #COSTS_HEALTH_INSTEAD_OF_MANA}.
	 */
	AURA_COSTS_HEALTH_INSTEAD_OF_MANA,
	/**
	 * The number of hit points the {@link Actor} currently has.
	 */
	HP,
	/**
	 * Returns the index of the entity in its current zone.
	 */
	INDEX,
	/**
	 * Returns the index of the entity from the end of its current zone.
	 * <p>
	 * For example, if it's the last element (i.e., index {@code -1}), its index from the end will be {@code 1}.
	 */
	INDEX_FROM_END,
	/**
	 * Returns the index of the entity in its current zone at the start of the game.
	 */
	STARTING_INDEX,
	/**
	 * The attack value written on the {@link Card}. This is distinct from {@link #BASE_ATTACK}, which is the base attack
	 * value of the {@link Minion} this card would summon.
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
	 * net.demilich.metastone.game.cards.Card}.
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
	 * The amount of attack added by all the {@link net.demilich.metastone.game.spells.aura.Aura} effects that target the
	 * entity.
	 */
	AURA_ATTACK_BONUS,
	/**
	 * The amount of hitpoints added by all the {@link net.demilich.metastone.game.spells.aura.Aura} effects that target
	 * the entity.
	 */
	AURA_HP_BONUS,
	/**
	 * Grants an immunity aura when present on an entity.
	 *
	 * @see #IMMUNE
	 */
	AURA_IMMUNE,
	/**
	 * When set, the card behaves as though it were actually a copy of the card of the specified ID.
	 * <p>
	 * This is distinct from transformation effects.
	 */
	AURA_CARD_ID,
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
	 * GameLogic#endOfSequence()}, all entities with {@link #DESTROYED} will be sent to the {@link Zones#GRAVEYARD}.
	 *
	 * @see Actor#isDestroyed() for a complete list of situations where an {@link Actor} is destroyed.
	 */
	DESTROYED,
	/**
	 * Fatigue is a game mechanic that deals increasing damage to players who have already drawn all of the cards in their
	 * deck, whenever they attempt to draw another card.
	 * <p>
	 * This attribute tracks how much damage a {@link net.demilich.metastone.game.entities.heroes.Hero} should take when
	 * the player draws a card.
	 *
	 * @see GameLogic#drawCard(int, Entity) for the complete usage of Fatigue.
	 */
	FATIGUE,
	/**
	 * A frozen {@link Actor} cannot attack. Freezing is cleared by a {@link net.demilich.metastone.game.spells.SilenceSpell}
	 * (when the minion is {@link #SILENCED}) or the owning player ends his turn on a different turn than when the minion
	 * was {@link #FROZEN}.
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
	 * @see GameLogic#silence(int, Actor) for a complete description of the silence effect.
	 */
	SILENCED,
	/**
	 * An {@link Actor} with {@link #WINDFURY} has two attacks per turn.
	 */
	WINDFURY,
	/**
	 * An aura version of {@link #WINDFURY}
	 */
	AURA_WINDFURY,
	/**
	 * An {@link Actor} with {@link #MEGA_WINDFURY} has four attacks per turn.
	 *
	 * @see Actor#canAttackThisTurn(GameContext) for the complete rules of attacking.
	 */
	MEGA_WINDFURY,
	/**
	 * An {@link Actor} with {@link #UNLIMITED_ATTACKS} has unlimited attacks per turn.
	 *
	 * @see Actor#canAttackThisTurn(GameContext) for the complete rules of attacking.
	 */
	UNLIMITED_ATTACKS,
	/**
	 * An {@link Actor} with {@link #TAUNT} must be targeted by opposing {@link net.demilich.metastone.game.actions.PhysicalAttackAction}
	 * actions first. This means the {@link Minion} with {@link #TAUNT} acts like a shield for its other non-taunt minions
	 * and its owning player's hero, because the opposing minions and hero must attack the taunt minion first.
	 *
	 * @see net.demilich.metastone.game.logic.TargetLogic#getValidTargets(GameContext, Player, GameAction) for the
	 * complete targeting logic.
	 */
	TAUNT,
	/**
	 * Like taunt, but created as a result of an {@link net.demilich.metastone.game.spells.aura.Aura}.
	 *
	 * @see #TAUNT for more about taunt.
	 */
	AURA_TAUNT,
	/**
	 * Like taunt, but only applies while in hand or in the deck. Affects the summoned minion and goes away when this card
	 * is moved to the graveyard.
	 */
	CARD_TAUNT,
	/**
	 * The total amount of spell damage that an {@link Entity} contributes.
	 */
	SPELL_DAMAGE,
	/**
	 * The aura version of spell damage.
	 */
	AURA_SPELL_DAMAGE,
	/**
	 * The additional amount of healing an {@link Entity} contributes.
	 */
	HEALING_BONUS,
	/**
	 * The additional amount of healing an {@link Entity} contributes to this owner's enemy.
	 */
	ENEMY_HEALING_BONUS,
	/**
	 * The aura version of {@link #HEALING_BONUS}.
	 */
	AURA_HEALING_BONUS,
	/**
	 * The aura version of {@link #ENEMY_HEALING_BONUS}.
	 */
	AURA_ENEMY_HEALING_BONUS,
	/**
	 * Some cards give the opponent spell damage. This attribute stores how much.
	 */
	OPPONENT_SPELL_DAMAGE,
	/**
	 * A {@link Minion} with {@link #CHARGE} can attack the same turn it enters play.
	 *
	 * @see #SUMMONING_SICKNESS for the attribute that a {@link Minion} otherwise has which prevents it from attacking the
	 * same turn it is summoned.
	 */
	CHARGE,
	/**
	 * An Aura version of {@link #CHARGE}.
	 */
	AURA_CHARGE,
	/**
	 * An attribute that tracks the number of attacks the {@link Actor} has this turn. Typically, actors start with 1
	 * attack every turn.
	 *
	 * @see #WINDFURY for the attribute that sets the number of attacks an actor has to 2 at the start of the owner's
	 * @see Actor#canAttackThisTurn(GameContext) for the complete rules of attacking. turn.
	 */
	NUMBER_OF_ATTACKS,
	/**
	 * An attribute used by Giant Sand Worm that refreshes the number of attacks it has.
	 *
	 * @see Actor#canAttackThisTurn(GameContext) for the complete rules of attacking.
	 */
	EXTRA_ATTACKS,
	/**
	 * A virtual attribute that will call {@link Actor#getMaxNumberOfAttacks()} and return it.
	 */
	MAX_ATTACKS,
	/**
	 * When an {@link Actor} is {@link #ENRAGED}, its {@link #CONDITIONAL_ATTACK_BONUS} is set to the amount of damage
	 * gained by an {@link net.demilich.metastone.game.spells.EnrageSpell}.
	 *
	 * @see net.demilich.metastone.game.spells.EnrageSpell for the spell that implements Enrage.
	 */
	ENRAGED,
	/**
	 * An {@link Entity} with {@link #BATTLECRY} performs an action when it goes from the {@link Zones#HAND} to the {@link
	 * Zones#BATTLEFIELD}. This attribute is used to look up / keep track of entities that have battlecries. It does not
	 * define the battlecry itself.
	 */
	BATTLECRY,
	/**
	 * An {@link Entity} with {@link #DOUBLE_BATTLECRIES} causes other friendly battlecries to occur twice.
	 * <p>
	 * This implements Brann Bronzebeard's text.
	 *
	 * @deprecated since the introduction of {@link net.demilich.metastone.game.spells.aura.DoubleBattlecriesAura}.
	 */
	@Deprecated
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
	 * @see GameLogic#resolveAftermaths(Player, Actor, net.demilich.metastone.game.entities.EntityLocation) to see the
	 * complete rules for deathrattles.
	 */
	@Deprecated
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
	 * An Aura version of {@link #IMMUNE_WHILE_ATTACKING}.
	 */
	AURA_IMMUNE_WHILE_ATTACKING,
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
	 * If a Stealthed minion attacks or deals any kind of damage, it will lose Stealth. This includes passive effects such
	 * as that of Knife Juggler, and dealing combat damage in exchange, such as when being struck by a clumsy minion such
	 * as Ogre Brute, or by a Misdirection-redirected minion.
	 *
	 * @see GameLogic#fight(Player, Actor, Actor, net.demilich.metastone.game.actions.PhysicalAttackAction) for the
	 * situation where physical attacks cause a minion to lose stealth.
	 * @see GameLogic#damage(Player, Actor, int, Entity, boolean) for the situation where any kind of damage originating
	 * from a minion causes it to lose stealth.
	 * @see net.demilich.metastone.game.logic.TargetLogic#filterTargets(GameContext, Player, GameAction, List) for the
	 * logic behind selecting valid targets.
	 */
	STEALTH,
	/**
	 * The aura version of {@link #STEALTH}
	 */
	AURA_STEALTH,
	/**
	 * A {@link net.demilich.metastone.game.cards.Card} has this attribute to help spells find secrets in the deck.
	 * <p>
	 * Cards marked secret should not be revealed to the opponent.
	 */
	SECRET,
	/**
	 * When a combo {@link Card} is played after another card, an effect is triggered.
	 *
	 * @see net.demilich.metastone.game.spells.ComboSpell for the actual implementation of combo effects.
	 * @see GameLogic#playCard(int, EntityReference, EntityReference) for the control of the combo attribute.
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
	 * An integer attribute indicating which choice was taken on a choose one card.
	 * <p>
	 * {@code -1} indicates that the choose-both option occurred.
	 */
	CHOICE,
	/**
	 * An array of integer choices, for {@link net.demilich.metastone.game.spells.CastFromGroupSpell}, to allow adaptation
	 * choices to replay correctly.
	 */
	CHOICES,
	/**
	 * A {@link Minion} with this attribute causes both choose one options of a {@link Card} with {@link #CHOOSE_ONE} to
	 * be played.
	 * <p>
	 * This implements the Fandral Staghelm card text.
	 */
	@Deprecated
	BOTH_CHOOSE_ONE_OPTIONS,
	/**
	 * Summoning sickness prevents a {@link Minion} from attacking the same turn it is played or summoned. Minions with
	 * {@link #CHARGE} do not have summoning sickness.
	 * <p>
	 * Summoning sickness occurs however the minion entered the battlefield, whether through a play from the hand, a
	 * Summon effect, a put into battlefield effect, or a transform effect.
	 *
	 * @see GameLogic#summon(int, Minion, Entity, int, boolean) for the complete summoning rules.
	 * @see net.demilich.metastone.game.spells.PutMinionOnBoardFromDeckSpell for an unusual situation where minions enter
	 * the battlefield.
	 * @see GameLogic#transformMinion(net.demilich.metastone.game.spells.desc.SpellDesc, Entity, Minion, Minion, boolean)
	 * for an unusual situation where minions enter the battlefield.
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
	 * Marks an {@link Actor} to be untargetable by an <b>opponent's</b> spells or hero powers.
	 *
	 * @see net.demilich.metastone.game.logic.TargetLogic#filterTargets(GameContext, Player, GameAction, List) for the
	 * complete target selection logic.
	 */
	UNTARGETABLE_BY_OPPONENT_SPELLS,
	/**
	 * An {@link Actor} with this attribute is untargetable by spells or hero powers due to an aura.
	 *
	 * @see #UNTARGETABLE_BY_SPELLS for more information.
	 */
	AURA_UNTARGETABLE_BY_SPELLS,
	/**
	 * When a {@link net.demilich.metastone.game.cards.Card} that casts a {@link net.demilich.metastone.game.spells.DamageSpell}
	 * has this attribute, its bonus from spell damage is doubled.
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
	SPELL_DAMAGE_AMPLIFY_MULTIPLIER,
	/**
	 * Applies a multiplier to the amount of hero damage the owning player's skill deals.
	 */
	HERO_POWER_DAMAGE_AMPLIFY_MULTIPLIER,
	/**
	 * When any friendly {@link Entity} has this attribute, all friendly healing effects that use {@link
	 * GameLogic#heal(Player, Actor, int, Entity)} are multiplied by this attribute's value.
	 * <p>
	 * This implements Prophet Velen.
	 */
	HEAL_AMPLIFY_MULTIPLIER,
	SPELL_HEAL_AMPLIFY_MULTIPLIER,
	HERO_POWER_HEAL_AMPLIFY_MULTIPLIER,
	/**
	 * An attribute that specifies that the attack of this {@link Minion} is equal to its hitpoints.
	 */
	ATTACK_EQUALS_HP,
	/**
	 * The aura version of {@link #ATTACK_EQUALS_HP}.
	 */
	AURA_ATTACK_EQUALS_HP,
	/**
	 * When set, this {@link Minion} cannot attack.
	 */
	CANNOT_ATTACK,
	/**
	 * An Aura version of {@link #CANNOT_ATTACK}.
	 */
	AURA_CANNOT_ATTACK,
	/**
	 * When set, this {@link Minion} cannot target heroes with physical attacks.
	 * <p>
	 * Unusually, this attribute affects the {@link net.demilich.metastone.game.actions
	 * .PhysicalAttackAction#canBeExecutedOn(GameContext, Player, Entity)} method instead of {@link
	 * net.demilich.metastone.game.logic.TargetLogic#filterTargets (GameContext, Player, GameAction, List)}.
	 */
	CANNOT_ATTACK_HEROES,
	/**
	 * An Aura version of {@link #CANNOT_ATTACK_HEROES}
	 */
	AURA_CANNOT_ATTACK_HEROES,
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
	 * @see GameLogic#playCard(int, EntityReference, EntityReference) for the complete card playing implementation.
	 */
	COUNTERED,
	/**
	 * This attribute records which turn a {@link Minion} was marked as {@link #DESTROYED}.
	 */
	DIED_ON_TURN,
	/**
	 * When any {@link Entity} alive has this attribute, the owning player's hero power freezes its target.
	 * <p>
	 * Implements Ice Walker
	 */
	@Deprecated
	HERO_POWER_FREEZES_TARGET,
	/**
	 * When any {@link Entity} alive has this attribute, BOTH player's hero powers are disabled.
	 * <p>
	 * Implements Mindbreaker.
	 */
	HERO_POWERS_DISABLED,
	/**
	 * Records the amount of damage last sustained by an {@link Actor}. Typically used by an {@link
	 * net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider} to feed a value into a spell (e.g., a
	 * healing spell may heal the owning player by the amount of damage last dealt to an entity).
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
	 * @see GameLogic#fireGameEvent(GameEvent)  for the complete rules on event triggering.
	 * @see Enchantment for the entity that corresponds to a passive trigger.
	 */
	PASSIVE_TRIGGERS,
	/**
	 * Marks that this {@link Card} has a trigger that should be active while it is in the deck.
	 *
	 * @see #PASSIVE_TRIGGERS for an attribute that marks the entity has a trigger that is only active in the player's
	 * battlefield or hand.
	 */
	DECK_TRIGGERS,
	/**
	 * Marks that this {@link Card} has a trigger (this attribute) that should be active throughout the game.
	 */
	GAME_TRIGGERS,
	/**
	 * This attribute indicates the maximum number of times a hero power can be used in a turn. It is an aura effect. The
	 * number of times a hero power can be used will be the max value found among in-play entities owned by the player.
	 *
	 * @see GameLogic#canPlayCard(int, EntityReference) for the implementation that determines whether or not a card, like
	 * a hero power card, can be played.
	 */
	HERO_POWER_USAGES,
	/**
	 * An {@link Entity} with hero power damage contributes to the total hero power damage the player gets as a bonus to
	 * their base hero power damage. Applies to {@link net.demilich.metastone.game.spells.DamageSpell} based hero powers.
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
	 * @see net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider for the value provider that reads
	 * attributes like these and provides values to various spells.
	 */
	CTHUN_ATTACK_BUFF,
	/**
	 * This attribute keeps track of how many hitpoints should be added to C'Thun.
	 * <p>
	 * Implements the C'Thun mechanic.
	 *
	 * @see net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider for the value provider that reads
	 * attributes like these and provides values to various spells.
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
	 * When any {@link Entity} has this attribute in play, minions cost health instead of mana.
	 */
	MINIONS_COST_HEALTH,
	/**
	 * When any {@link Entity} has this attribute in play, a {@link Card} costs health instead of mana.
	 * <p>
	 * This attribute implements Seadevil Stinger.
	 */
	MURLOCS_COST_HEALTH,
	/**
	 * An {@link Entity} with this attribute takes twice the damage whenever it is dealt damage.
	 *
	 * @see GameLogic#damage(Player, Actor, int, Entity, boolean) for the complete damage implementation.
	 */
	TAKE_DOUBLE_DAMAGE,
	/**
	 * The Aura version of {@link #TAKE_DOUBLE_DAMAGE}
	 */
	AURA_TAKE_DOUBLE_DAMAGE,
	/**
	 * A {@link Minion} with this attribute cannot target a {@link Hero} the same turn it is summoned. This is typically
	 * given to a {@link #CHARGE} minion that would be too powerful it it could target a hero.
	 *
	 * @see net.demilich.metastone.game.actions.PhysicalAttackAction for a complete implementation of what a minion can
	 * attack.
	 */
	RUSH,
	/**
	 * Aura version of {@link #RUSH}
	 */
	AURA_RUSH,
	/**
	 * An attribute that keeps track of how much attack and hitpoints to add to the next Jade Golem that gets summoned.
	 */
	JADE_BUFF,
	/**
	 * When any {@link Entity} has a non-zero value for this attribute, spells are cast with random targets, random
	 * discover choices are made, physical attacks target randomly, and battlecries target randomly.
	 * <p>
	 * Implements Yogg-Saron, Hope's End; Servant of Yogg-Saron; Mayor Noggenfogger
	 */
	RANDOM_CHOICES,
	/**
	 * A {@link #QUEST} {@link Entity} is an untargetable permanent that lives in the {@link Zones#QUEST} zone but is
	 * visible to the opponent.
	 * <p>
	 * Implements quest cards.
	 */
	QUEST,
	/**
	 * A {@link #PACT} {@link Entity} is an untargetable permanent that lives in the {@link Zones#QUEST} zone, is visible
	 * to the opponent, and can be triggered by actions that either the opponent or player performs (activated during both
	 * turns).
	 */
	PACT,
	/**
	 * An attribute given to {@link Card} entities that started in the player's deck, as opposed to being generated by
	 * other cards.
	 * <p>
	 * Implements Open the Waygate quest.
	 */
	STARTED_IN_DECK,
	/**
	 * An attribute given to {@link Card} entities that started in the player's opening hand
	 * <p>
	 * Implements Hex Lord Malacrass.
	 */
	STARTED_IN_HAND,
	/**
	 * This attribute describes a {@link Minion} that can never be targeted by spells, abilities, auras or physical
	 * attacks but does occupy a position on the {@link Zones#BATTLEFIELD}.
	 * <p>
	 * Implements permanents.
	 */
	PERMANENT,
	/**
	 * This attribute is a {@link String} that describes the inventory owner (as opposed to in-match owner) of the card.
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
	DONOR_ID,
	/**
	 * This {@link String} is the user ID of the player who is currently using a card that belongs to someone else.
	 */
	CHAMPION_ID,
	/**
	 * This {@link String} array is the collections this card belongs to, like the deck, user and alliances.
	 */
	COLLECTION_IDS,
	/**
	 * This {@link String} is the ID of the alliance this card belongs to, if any.
	 */
	ALLIANCE_ID,
	/**
	 * Every time a player summons this {@link Minion} for the first time in their lifetime of the game, the {@link Card}
	 * is incremented. This attribute persists between matches.
	 */
	UNIQUE_CHAMPION_IDS_SIZE,
	/**
	 * Every unique user ID that has summoned this minion is stored in this attribute's array of {@link String}.
	 */
	UNIQUE_CHAMPION_IDS,
	/**
	 * Every time an {@link Actor} destroys a {@link Minion}, the {@link String} card ID is stored in this attribute. This
	 * attribute persists between matches.
	 */
	LAST_MINION_DESTROYED_CARD_ID,
	/**
	 * Every time an {@link Actor} destroys a {@link Minion}, the {@link String} card inventory ID is stored in this
	 * attribute. This attribute persists between matches.
	 */
	LAST_MINION_DESTROYED_INVENTORY_ID,
	/**
	 * Every time an {@link Actor} damages a target, increment this attribute with the total amount of damage dealt
	 * across
	 * <b>all games.</b>
	 * <p>
	 * This is a legacy mechanic. In a networked multiplayer environment, this value will persist between matches.
	 */
	TOTAL_DAMAGE_DEALT,
	/**
	 * Every time an {@link Actor} kills a target, increment this attribute.
	 */
	TOTAL_KILLS,
	/**
	 * Every time an {@link Actor} receives damage, increment this attribute with the total amount of damage dealt.
	 */
	TOTAL_DAMAGE_RECEIVED,
	/**
	 * Every time an {@link Actor} is healed, increment this attribute with the amount of healing and set to zero at the
	 * end of the turn.
	 */
	HEALING_THIS_TURN,
	/**
	 * Every time an {@link Actor} is healed, increment this attribute with the amount of healing and set to zero at the
	 * end of the turn.
	 */
	EXCESS_HEALING_THIS_TURN,
	/**
	 * Every time an {@link Actor} has its max HP increased, this value increases.
	 */
	TOTAL_HP_INCREASES,
	/**
	 * Every time an {@link Actor} is damaged, increment this attribute with the amount of damage and set it to zero at
	 * the end of the turn.
	 */
	DAMAGE_THIS_TURN,
	/**
	 * Every time a {@link Player} successfully summons a minion, this counter is incremented on the summoning player.
	 */
	MINIONS_SUMMONED_THIS_TURN,
	/**
	 * Every time a {@link Player} successfully summons a minion, this counter is incremented on both players
	 */
	TOTAL_MINIONS_SUMMONED_THIS_TURN,
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
	 * An aura version of {@link #POISONOUS}
	 */
	AURA_POISONOUS,
	/**
	 * A shorthand implementation of the Lifesteal keyword. Indicates that the {@link Hero} of the owner of the {@link
	 * Minion}, or {@link net.demilich.metastone.game.cards.Card} should be healed by the amount of damage dealt by that
	 * minion.
	 */
	LIFESTEAL,
	/**
	 * An aura version of {@link #LIFESTEAL}
	 */
	AURA_LIFESTEAL,
	/**
	 * Indicates that the {@link Player}'s end turn triggers should trigger twice.
	 * <p>
	 * Implements Drakkari Enchanter.
	 */
	DOUBLE_END_TURN_TRIGGERS,
	/**
	 * Indicates the {@link Integer} turn that the specified card was played from the hand or the deck.
	 */
	PLAYED_FROM_HAND_OR_DECK,
	/**
	 * Indicates how much Mana the player spent to play the card
	 */
	MANA_SPENT,
	/**
	 * An {@link EntityReference} that, when set, indicates which entity this copied, if the entity copied with {@link
	 * Entity#getCopy}.
	 */
	COPIED_FROM,
	/**
	 * Overrides the name of the {@link Entity}
	 */
	NAME,
	/**
	 * Overrides the description of the {@link Entity}
	 */
	DESCRIPTION,
	/**
	 * Indicates the name in Spellsource for the specified card.
	 */
	SPELLSOURCE_NAME,
	/**
	 * Returns the {@link Card}'s {@link net.demilich.metastone.game.cards.desc.CardDesc#manaCostModifier} field.
	 */
	MANA_COST_MODIFIER,
	/**
	 * Indicates the number of uses of a hero power.
	 */
	USED_THIS_TURN,
	/**
	 * On the player entity, specifies how much mana has been spent this turn.
	 */
	MANA_SPENT_THIS_TURN,
	/**
	 * Returns the {@link Card}'s {@link net.demilich.metastone.game.cards.desc.CardDesc#heroClass} field.
	 */
	HERO_CLASS,
	/**
	 * Returns the {@link Card}'s {@link net.demilich.metastone.game.cards.desc.CardDesc#targetSelection} field.
	 */
	TARGET_SELECTION,
	/**
	 * When set, overrides the card's identity and makes it behave like a different card in all its base effects.
	 */
	CARD_ID,
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
	 * For entities that are in the {@link Zones#REMOVED_FROM_PLAY} zone due to a transform effect, this attribute points
	 * to the entity that replaced this one.
	 */
	TRANSFORM_REFERENCE,
	/**
	 * Whenever a {@link Card} is received into the hand, this attribute indicates on which turn (typically {@link
	 * GameContext#getTurn()} the card was received.
	 */
	RECEIVED_ON_TURN,
	/**
	 * Remembers the {@link #ATTACK_BONUS}, {@link #HP_BONUS}, {@link #LIFESTEAL}, {@link #WINDFURY}, {@link #POISONOUS}
	 * that was applied to the {@link Actor} that gets subjected to a {@link net.demilich.metastone.game.spells.ShuffleToDeckSpell}.
	 * <p>
	 * Implements Kingsbane.
	 */
	KEEPS_ENCHANTMENTS,
	/**
	 * When {@code true}, indicates this card never appears in a mulligan. Typically used to implement passives.
	 */
	NEVER_MULLIGANS,
	/**
	 * Indicates the card will put a copy of itself into the player's hand after it is played.
	 */
	ECHO,
	/**
	 * The aura version of {@link #ECHO}.
	 */
	AURA_ECHO,
	/**
	 * Whenever a {@link Minion} with this attribute is first attacked, it loses this attribute and the damage dealt to it
	 * is dealt to its owner's hero instead.
	 */
	DEFLECT,
	/**
	 * Whenever a {@link Card} with this attribute is in your hand, and you have at least the invoke amount of mana, you
	 * may gain an extra card with a bonus effect on it to cast with that extra mana.
	 */
	INVOKE,
	/**
	 * The aura version of {@link #INVOKE}
	 */
	AURA_INVOKE,
	/**
	 * Indicates a card's invoke for the specified amount of mana.
	 * <p>
	 * On a {@link Player} entity, indicates the number of cards that were invoked.
	 */
	INVOKED,
	/**
	 * Indicates that this {@link Entity} should remove itself from play peacefully at the end of the current turn.
	 */
	REMOVES_SELF_AT_END_OF_TURN,
	/**
	 * This value indicates the turn number of the {@link Player}'s last turn.
	 */
	LAST_TURN,
	/**
	 * When {@code true}, indicates this {@link Player} entity is an AI opponent.
	 * <p>
	 * Implements AI-specific game logic like eliminating turn timers.
	 */
	AI_OPPONENT,
	/**
	 * When set on a card in the {@link Zones#DISCOVER}, indicates the card should be turned over and visible to the
	 * discovering player's opponent.
	 */
	UNCENSORED,
	/**
	 * Indicates the minion should magnetize when being played left of a mech instead of summoning normally
	 */
	MAGNETIC,
	/**
	 * An array of card ID strings that correspond to which cards this card was magnetized from.
	 */
	MAGNETS,
	/*
	 * The position in a player's hand that a card was played from
	 */
	HAND_INDEX,
	/**
	 * Indicates this card was roasted (removed from the top of the deck) by a {@link RoastSpell} on the specified {@link
	 * Integer} turn.
	 * <p>
	 * Implements Warchef Gordo / Chef's Roast effect.
	 */
	ROASTED,
	/**
	 * Indicates an {@link Actor} will do something when it attacks and kills a minion.
	 * <p>
	 * Implements Alder Ravenwald / Dragoon's Supremacy effect.
	 */
	SUPREMACY,
	/**
	 * Implements Electra Stormsurge.
	 */
	SPELLS_CAST_TWICE,
	/**
	 * Self explanatory.
	 */
	SPELLS_CAST_THRICE,
	/**
	 * Applies a multiplier to the base attack plus bonus attack on an {@link Actor}.
	 */
	ATTACK_MULTIPLIER,
	/**
	 * The aura version of {@link #ATTACK_MULTIPLIER}.
	 */
	AURA_ATTACK_MULTIPLIER,
	/**
	 * When non-zero, multiplies the {@link #ATTACK_BONUS}, {@link #TEMPORARY_ATTACK_BONUS}, {@link #AURA_ATTACK_BONUS}
	 * and {@link #CONDITIONAL_ATTACK_BONUS} by this amount. In other words, a multiplier that only affects bonuses.
	 */
	ATTACK_BONUS_MULTIPLIER,
	/**
	 * The aura version of {@link #ATTACK_BONUS_MULTIPLIER}.
	 */
	AURA_ATTACK_BONUS_MULTIPLIER,
	/**
	 * Will block an entity from receiving game event triggers
	 */
	CANT_GAIN_ENCHANTMENTS,
	/**
	 * Indicates a character shouldn't naturally lose the FROZEN attribute
	 */
	FREEZES_PERMANENTLY,
	/**
	 * Indicates how many turns an actor should have {@link Attribute#STEALTH} for
	 */
	STEALTH_FOR_TURNS,
	/**
	 * Indicates that the spell was played targeting a friendly minion Implements Lynessa Sunsorrow
	 */
	CASTED_ON_FRIENDLY_MINION,
	/**
	 * Tracks the amount of times an actor has attacked over the course of a game
	 */
	ATTACKS_THIS_GAME,
	/**
	 * Tracks a card being in the progress of playing
	 */
	BEING_PLAYED,
	/**
	 * Marks a card as using a "Quick Draw" effect for the Outlaw class
	 */
	QUICK_DRAW,
	/**
	 * Allows spell effects to count and keep track of values without a dedicate enchantment.
	 */
	RESERVED_INTEGER_1,
	/**
	 * Allows spell effects to count and keep track of values without a dedicated enchantment.
	 */
	RESERVED_INTEGER_2,
	/**
	 * Allows spell effects to count and keep track of values without a dedicated enchantment.
	 */
	RESERVED_INTEGER_3,
	/**
	 * Allows spell effects to count and keep track of values without a dedicated enchantment.
	 */
	RESERVED_INTEGER_4,
	/**
	 * Allows spell effects to count and keep track of values without a dedicated enchantment.
	 */
	RESERVED_INTEGER_5,
	/**
	 * Allows spell effects to mark things without a dedicated enchantment.
	 */
	RESERVED_BOOLEAN_1,
	/**
	 * Allows spell effects to mark things without a dedicated enchantment.
	 */
	RESERVED_BOOLEAN_2,
	/**
	 * Allows spell effects to mark things without a dedicated enchantment.
	 */
	RESERVED_BOOLEAN_3,
	/**
	 * Allows spell effects to mark things without a dedicated enchantment.
	 */
	RESERVED_BOOLEAN_4,
	/**
	 * Allows spell effects to mark things without a dedicated enchantment.
	 */
	RESERVED_BOOLEAN_5,
	/**
	 * Counts the number of supremacies (kills, but not overkills) that the {@link Actor} has achieved.
	 */
	SUPREMACIES_THIS_GAME,
	/**
	 * Records the {@link EntityReference} of this choose one spell card's source card.
	 */
	CHOICE_SOURCE,
	/**
	 * When set on the {@link Player} entity, that player no longer takes fatigue damage.
	 */
	DISABLE_FATIGUE,
	/**
	 * Indicates how many times the target {@link Actor} has been healed.
	 * <p>
	 * When set on the {@link Player} entity, indicates how many friendly characters have been healed total.
	 */
	TIMES_HEALED,
	/**
	 * Indicates which turn a minion was summoned.
	 */
	SUMMONED_ON_TURN,
	/**
	 * The player ID of the owner of the source entity that summoned this minion.
	 */
	SUMMONED_BY_PLAYER,
	/**
	 * Indicates the number of attacks this {@link Actor} has made this turn.
	 */
	ATTACKS_THIS_TURN,
	/**
	 * The number of turns a player has for Demonic Form
	 */
	DEMONIC_FORM,
	/**
	 * Indicates this actor has a wither effect active on it. Does not actually implement the wither.
	 */
	WITHER,
	/**
	 * Indicates this actor has been withered.
	 */
	WITHERED,
	/**
	 * Counter for each time a "XXXXX's Scheme" card has upgraded
	 */
	SCHEME,
	/**
	 * Indicates a minion is part of the "___ Lackey" subset of cards for the Year of the Dragon
	 */
	LACKEY,
	/**
	 * Indicates the decay keyword, which causes the entity to lose one health/armor/durability at the end of every
	 * owner's turn
	 * <p>
	 * Requires an appropriate trigger to actually implement the effect.
	 */
	DECAY,
	/**
	 * The {@link net.demilich.metastone.game.spells.aura.Aura} version of the {@link #DECAY} keyword.
	 * <p>
	 * Requires an appropriate trigger to actually implement the effect.
	 */
	AURA_DECAY,
	/**
	 * Indicates a minion is an official Treant, considered for Treant-related synergies
	 */
	TREANT,
	/**
	 * Indicates how much an entity has {@link net.demilich.metastone.game.spells.DrainSpell} drained this turn.
	 */
	DRAINED_THIS_TURN,
	/**
	 * Indicates how much an entity has {@link net.demilich.metastone.game.spells.DrainSpell} drained over its lifetime.
	 */
	TOTAL_DRAINED,
	/**
	 * Indicates how much an entity has {@link net.demilich.metastone.game.spells.DrainSpell} drained last turn.
	 */
	DRAINED_LAST_TURN,
	/**
	 * The keyword for cards with Surge (a bonus gained when the card is drawn that turn).
	 */
	SURGE,
	/**
	 * An override for the entity's description that indicates it has an {@link net.demilich.metastone.game.cards.dynamicdescription.DynamicDescription}.
	 * <p>
	 * Contains an array of {@link net.demilich.metastone.game.cards.dynamicdescription.DynamicDescriptionDesc}
	 */
	DYNAMIC_DESCRIPTION,
	/**
	 * Stores passive auras, i.e., auras that are active while the entity is in the hand.
	 */
	PASSIVE_AURAS,
	CURSE,
	/**
	 * Drain indicates the card will deal damage to the specified target and buffs the source's HP by that amount.
	 *
	 * @see net.demilich.metastone.game.spells.DrainSpell for more on drains
	 */
	DRAIN,
	/**
	 * Records how much damage was dealt to minions by this player or entity this game.
	 */
	TOTAL_MINION_DAMAGE_DEALT_THIS_GAME,
	/**
	 * Records how many attacks last turn an actor made.
	 */
	ATTACKS_LAST_TURN,
	/**
	 * An attribute that indicates a card can be used to Discover something, offering a choice of 3 options to the player
	 * with a 4x increase in frequency of class cards.
	 * <p>
	 * This is only really used for one Trader card right now, but it seemed like a useful one to add for future cards and
	 * their effects.
	 */
	DISCOVER,
	/**
	 * Signifies that this card is an Artifact, a type of token card added by What Lies Beneath. Used to easily identify
	 * them for synergy purposes.
	 */
	ARTIFACT,
	/**
	 * Keeps track of damage dealt by this {@code source} minion <b>this game</b>.
	 */
	TOTAL_DAMAGE_DEALT_THIS_GAME,
	/**
	 * Attribute determining which card is the "Signature" for Ringmaster cards.
	 */
	SIGNATURE,
	/**
	 * When set on a player entity, indicates the player is currently in the starting turn phase.
	 */
	STARTING_TURN,
	/**
	 * Attribute to track if a spell should cast itself automatically when drawn, also drawing another card in the
	 * process.
	 */
	CASTS_WHEN_DRAWN,
	/**
	 * Stores the Eidolon effect tribe.
	 */
	EIDOLON_RACE,
	/**
	 * Represents the minimum attack that auras can reduce an actor's attack to
	 */
	AURA_MIN_ATTACK,
	/**
	 * The number of aftermaths active on this entity.
	 */
	AFTERMATH_COUNT,
	/**
	 * Indicates that a card makes use of the Imbue keyword, specifically using up a charge
	 * <p>
	 * Also used to store the number of Imbue charges on the player
	 */
	IMBUE,
	/**
	 * Indicates the player has drawn their starting hand. Essentially the end of the start-of-game phase.
	 */
	STARTING_HAND_DRAWN,
	/**
	 * The {@link EntityReference} of the entity that destroyed this one.
	 */
	DESTROYED_BY;

	public String toKeyCase() {
		return ParseUtils.toCamelCase(this.toString());
	}

	private static final List<Attribute> cardEnchantmentAttributes = List.of(CARD_TAUNT);
	private static final Set<Attribute> auraAttributes = Arrays.stream(Attribute.values()).filter(attr -> attr.name().startsWith("AURA_")).collect(Collectors.toSet());

	private static final Set<Attribute> storesTurnNumberAttributes = EnumSet.of(
			Attribute.ROASTED,
			Attribute.DISCARDED,
			Attribute.PLAYED_FROM_HAND_OR_DECK,
			Attribute.LAST_TURN,
			Attribute.RECEIVED_ON_TURN,
			Attribute.SUMMONED_ON_TURN,
			Attribute.DIED_ON_TURN
	);

	private static final Set<Attribute> enchantmentLikeAttributes = EnumSet.of(Attribute.POISONOUS,
			Attribute.DIVINE_SHIELD,
			COSTS_HEALTH_INSTEAD_OF_MANA,
			TEMPORARY_ATTACK_BONUS,
			HEALING_BONUS,
			SPELL_DAMAGE,
			Attribute.STEALTH,
			Attribute.TAUNT,
			Attribute.CANNOT_ATTACK,
			Attribute.ATTACK_EQUALS_HP,
			Attribute.CANNOT_ATTACK_HEROES,
			Attribute.CHARGE,
			Attribute.DEFLECT,
			Attribute.IMMUNE,
			Attribute.ENRAGABLE,
			Attribute.IMMUNE_WHILE_ATTACKING,
			Attribute.FROZEN,
			Attribute.KEEPS_ENCHANTMENTS,
			Attribute.MAGNETIC,
			Attribute.PERMANENT,
			Attribute.RUSH,
			Attribute.WITHER,
			Attribute.INVOKE,
			Attribute.LIFESTEAL,
			Attribute.WINDFURY,
			Attribute.ATTACK_BONUS,
			Attribute.HP_BONUS);

	/**
	 * Contains the list of attributes that enchant cards as opposed to actors.
	 *
	 * @return A list of attributes.
	 */
	public static List<Attribute> getCardEnchantmentAttributes() {
		return cardEnchantmentAttributes;
	}

	/**
	 * Contains attributes that are the {@link net.demilich.metastone.game.spells.aura.Aura} version of a corresponding
	 * attribute.
	 *
	 * @return A list of attributes.
	 */
	public static Set<Attribute> getAuraAttributes() {
		return auraAttributes;
	}

	/**
	 * Contains the set of attributes that store turn numbers.
	 * <p>
	 * This affects whether or not the entity is considered having an integer attribute in the {@link
	 * Entity#hasAttribute(Attribute)} call.
	 *
	 * @return A set of attributes.
	 */
	public static Set<Attribute> getStoresTurnNumberAttributes() {
		return storesTurnNumberAttributes;
	}

	public static Set<Attribute> getEnchantmentLikeAttributes() {
		return enchantmentLikeAttributes;
	}
}
