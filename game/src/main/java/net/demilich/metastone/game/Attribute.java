package net.demilich.metastone.game;

import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.Zones;

import java.util.List;

public enum Attribute {
	/**
	 * The base mana cost of the {@link Card}.
	 *
	 * @see Card#getManaCost(GameContext, Player) to get the mana cost of a card
	 * considering all possible effects.
	 */
	BASE_MANA_COST,
	/**
	 * The number of hit points the {@link Actor} currently has.
	 */
	HP,
	/**
	 * The attack value written on the {@link MinionCard}. This is distinct from {@link #BASE_ATTACK},
	 * which is the base attack value of the {@link Minion} this card would summon.
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
	 * The current armor belonging to the {@link Actor}.
	 */
	ARMOR,
	/**
	 * A one-turn long attack bonus given to the {@link Actor}.
	 */
	TEMPORARY_ATTACK_BONUS,
	/**
	 * The amount of hitpoints added by all the {@link net.demilich.metastone.game.spells.BuffSpell} effects on the entity.
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
	 * A conditional attack bonus for the {@link Actor} that corresponds to bonuses
	 * from an {@link net.demilich.metastone.game.spells.EnrageSpell}, {@link net.demilich.metastone.game.spells.ConditionalAttackBonusSpell}
	 * or {@link net.demilich.metastone.game.spells.SetAttributeSpell}. This bonus is typically controlled by a {@link net.demilich.metastone.game.spells.desc.condition.Condition}.
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
	 * {@link Actor#getHp()} is below zero or if they are in the {@link Zones#GRAVEYARD}
	 * or {@link Zones#REMOVED_FROM_PLAY} zones.
	 * <p>
	 * At the end of {@link net.demilich.metastone.game.logic.GameLogic#performGameAction(int, GameAction)} in {@link GameLogic#checkForDeadEntities()},
	 * all entities with {@link #DESTROYED} will be sent to the {@link Zones#GRAVEYARD}.
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
	 * A frozen {@link Actor} cannot attack. Freezing is cleared by a
	 * {@link net.demilich.metastone.game.spells.SilenceSpell} (when the minion is {@link #SILENCED}) or
	 * the owning player ends his turn on a different turn than when the minion was {@link #FROZEN}.
	 *
	 * @see GameLogic#silence(int, Minion) for a complete description of the silence effect.
	 * @see GameLogic#handleFrozen(Actor) to see where freezing is handled.
	 */
	FROZEN,
	/**
	 * This {@link Minion} will typically gain an attack bonus after it is
	 * dealt damage the first time.
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
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
	 */
	MEGA_WINDFURY,
	/**
	 * An {@link Actor} with {@link #UNLIMITED_ATTACKS} has unlimited attacks per turn.
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
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
	 * The total amount of spell damage that an {@link Entity} contributes.
	 */
	SPELL_DAMAGE,
	/**
	 * Some cards give the opponent spell damage. This attribute stores how much.
	 */
	OPPONENT_SPELL_DAMAGE,
	/**
	 * A {@link Minion} with {@link #CHARGE} can attack the same turn it enters play.
	 * @see #SUMMONING_SICKNESS for the attribute that a {@link Minion} otherwise has which prevents it from attacking
	 * the same turn it is summoned.
	 */
	CHARGE,
	/**
	 * An attribute that tracks the number of attacks the {@link Actor} has this turn. Typically, actors start with 1
	 * attack every turn.
	 * @see #WINDFURY for the attribute that sets the number of attacks an actor has to 2 at the start of the owner's
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
	 * turn.
	 */
	NUMBER_OF_ATTACKS,
	/**
	 * An attribute used by Giant Sand Worm that refreshes the number of attacks it has.
	 * @see Actor#canAttackThisTurn() for the complete rules of attacking.
	 */
	EXTRA_ATTACKS,
	/**
	 * When an {@link Actor} is {@link #ENRAGED}, its {@link #CONDITIONAL_ATTACK_BONUS} is set to the amount of damage
	 * gained by an {@link net.demilich.metastone.game.spells.EnrageSpell}.
	 * @see net.demilich.metastone.game.spells.EnrageSpell for the complete Enrage rules.
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
	 * @see GameLogic#performBattlecryAction(int, Actor, Player, BattlecryAction) for the complete rules on
	 * double battlecries.
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
	 * @see GameLogic#resolveDeathrattles(Player, Actor, int) to see the complete rules for deathrattles.
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
	 * @see GameLogic#damageMinion(Actor, int) for the complete rules of damage.
	 */
	DIVINE_SHIELD,
	/**
	 * A {@link Minion} with stealth cannot be targeted by spells, hero powers or physical attacks until it attacks.
	 * <p>
	 * If a Stealthed minion attacks or deals any kind of damage, it will lose Stealth. This includes passive effects
	 * such as that of Knife Juggler, and dealing combat damage in exchange, such as when being struck by a clumsy minion
	 * such as Ogre Brute, or by a Misdirection-redirected minion.
	 * @see GameLogic#fight(Player, Actor, Actor) for the situation where physical attacks cause a minion to lose stealth.
	 * @see GameLogic#damage(Player, Actor, int, Entity, boolean) for the situation where any kind of damage originating
	 * from a minion causes it to lose stealth.
	 * @see net.demilich.metastone.game.logic.TargetLogic#filterTargets(GameContext, Player, GameAction, List) for the
	 * logic behind selecting valid targets.
	 */
	STEALTH,
	/**
	 * A {@link net.demilich.metastone.game.cards.SecretCard} has this attribute to help spells find secrets in the deck.
	 * <p>
	 * Cards marked secret should not be revealed to the opponent.
	 */
	SECRET,
	/**
	 * When a combo {@link Card} is played after another card, an effect is triggered.
	 * @see net.demilich.metastone.game.spells.ComboSpell for the actual implementation of combo effects.
	 * @see GameLogic#afterCardPlayed(int, CardReference) for the control of the combo attribute.
	 */
	COMBO,
	/**
	 * Overload is an {@link Integer} amount of mana that will be locked (unavailable for use) the next turn by playing
	 * this {@link Card}.
	 */
	OVERLOAD,
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
	 * @see GameLogic#summon(int, Minion, Card, int, boolean) for the complete summoning rules.
	 * @see net.demilich.metastone.game.spells.PutMinionOnBoardFromDeckSpell for an unusual situation where minions
	 * enter the battlefield.
	 * @see GameLogic#transformMinion(Minion, Minion) for an unusual situation where minions enter the battlefield.
	 */
	SUMMONING_SICKNESS,
	/**
	 * Marks an {@link Actor} to be untargetable by spells or hero powers. This includes the owner's spells and hero
	 * powers.
	 * @see net.demilich.metastone.game.logic.TargetLogic#filterTargets(GameContext, Player, GameAction, List) for the
	 * complete target selection logic.
	 */
	UNTARGETABLE_BY_SPELLS,
	/**
	 * An {@link Actor} with this attribute is untargetable by spells or hero powers due to an aura.
	 * @see #UNTARGETABLE_BY_SPELLS for more information.
	 */
	AURA_UNTARGETABLE_BY_SPELLS,
	SPELL_DAMAGE_MULTIPLIER,
	SPELL_AMPLIFY_MULTIPLIER,
	HEAL_AMPLIFY_MULTIPLIER,
	MANA_COST_MODIFIER,
	ATTACK_EQUALS_HP,
	CANNOT_ATTACK,
	CANNOT_ATTACK_HEROES,
	INVERT_HEALING,
	CANNOT_REDUCE_HP_BELOW_1,
	COUNTERED,
	DIED_ON_TURN,
	HERO_POWER_CAN_TARGET_MINIONS,
	LAST_HIT,
	PASSIVE_TRIGGER,
	DECK_TRIGGER,
	HERO_POWER_USAGES,
	HERO_POWER_DAMAGE,
	SHADOWFORM,
	CTHUN_ATTACK_BUFF,
	CTHUN_HEALTH_BUFF,
	CTHUN_TAUNT,
	SPELLS_COST_HEALTH,
	MURLOCS_COST_HEALTH,
	IMMUNE_HERO,
	TAKE_DOUBLE_DAMAGE,
	CANNOT_ATTACK_HERO_ON_SUMMON,
	JADE_BUFF,
	ALL_RANDOM_FINAL_DESTINATION,
	ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION,

	// AI flags
	MARKED_FOR_DEATH,

	/**
	 * Describes the owner of this card.
	 */
	USER_ID,
	/**
	 * Describes the instance of this specific entity
	 */
	ENTITY_INSTANCE_ID,
	/**
	 * Describes the instance of this card
	 */
	CARD_INVENTORY_ID,
	/**
	 * Describes the ID of the deck this card is currently put into.
	 */
	DECK_ID,
	/**
	 * Describes the donor of this card.
	 */
	DONOR_ID,
	/**
	 * Describes the current champion (usually this player)
	 */
	CHAMPION_ID,
	/**
	 * Describes the collections this card belongs to
	 */
	COLLECTION_IDS,
	/**
	 * Describes the alliance this card belongs to, if any
	 */
	ALLIANCE_ID,

	// Networked attributes
	FIRST_TIME_PLAYS,

	/**
	 * Describes the last minion (not actor like hero) this actor (hero or minion) destroyed
	 */
	LAST_MINION_DESTROYED_CARD_ID,
	LAST_MINION_DESTROYED_INVENTORY_ID
}
