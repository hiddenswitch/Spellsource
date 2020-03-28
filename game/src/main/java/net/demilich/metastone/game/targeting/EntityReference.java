package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.io.Serializable;
import java.util.List;

/**
 * An entity reference identifies a specific game entity, like a card or minion; or, it is interpreted as a group of
 * entities, possibly always of length 1 (like {@link #FRIENDLY_HERO}) or 0 or more ({@link #FRIENDLY_MINIONS}). These
 * references are called group references, and their {@link #isTargetGroup()} will return {@code true}.
 * <p>
 * Group references are appropriate to put into {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET}. If
 * {@link net.demilich.metastone.game.spells.desc.SpellArg#RANDOM_TARGET} is specified along with a group reference, a
 * random entity from the group will be chosen.
 * <p>
 * When {@link Entity#getReference()} is called, a specific (non-group) reference is always returned. Thus, calling
 * {@link Hero#getReference()} will never return {@link #FRIENDLY_HERO}.
 * <p>
 * When the target has been transformed, group reference resolution will always return the transformed targets using
 * {@link Entity#transformResolved(GameContext)}. Resolving a target using {@link GameContext#resolveSingleTarget(EntityReference)}
 * will also always return the transformed target, regardless if it is a group reference or not.
 * <p>
 * All references omit {@link Attribute#PERMANENT} actors except {@link #SELF}, {@link #ALL_ENTITIES}, {@link
 * #TRIGGER_HOST}, {@link #OUTPUT}, {@link #EVENT_SOURCE}, {@link #EVENT_TARGET} and {@link #TRANSFORM_REFERENCE}.
 * <p>
 * Friendly versus enemy references are evaluated with respect to the calling/casting player. This includes triggers
 * that are put on the opponent by effects cast by the player. In other words, regardless of "where" the {@code source}
 * is, the {@code player} from whose point of view the reference is being resolved is always the caster.
 * <p>
 * This can be confusing in a situation like Loatheb, who puts a {@link net.demilich.metastone.game.cards.costmodifier.CardCostModifier}
 * on the caster's {@link Player} entity whose {@link net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg#TARGET}
 * is {@link #FRIENDLY_HAND} but whose {@link net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg#TARGET_PLAYER}
 * is {@link net.demilich.metastone.game.spells.TargetPlayer#OPPONENT}. In card cost modifiers, target player is used to
 * determine if a modifier affects a card by comparing the trigger's owner to the card's owner; opponent indicates that
 * the card's owner must not equal the trigger's owner. However, {@link #FRIENDLY_HAND} is evaluated from each player's
 * point of view separately, because card cost modifiers ({@link net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(Player,
 * Card)}) are evaluated by both players, always (this allows opponents to see card cost changes).
 * <p>
 * Or, for example, Temporus causes a spell to be cast during the turn of the opponent of the owner of Temporus. The
 * {@code source} of the spell that modifies the {@link Attribute#EXTRA_TURN} of both players is cast during the
 * opponent's turn and in an event whose {@link net.demilich.metastone.game.spells.TargetPlayer} is the opponent. But,
 * {@link net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc#spell} is always cast by the enchantment's
 * owner, which was the caster who originally put the enchantment into play. Therefore, the {@code source} will be the
 * same as the player who put Temporus onto the battlefield, and that's from whose point of view the {@link
 * #FRIENDLY_PLAYER} and {@link #ENEMY_PLAYER} will be evaluated.
 *
 * @see net.demilich.metastone.game.GameContext#resolveTarget(Player, Entity, EntityReference) to see how references are
 * 		interpreted.
 * @see net.demilich.metastone.game.spells.Spell#cast(GameContext, Player, SpellDesc, Entity, List) to see more about
 * 		how group references are used in the casting of spells.
 */
public final class EntityReference implements Serializable {
	/**
	 * Indicates a spell should take no target. It will receive {@code null} as its {@code target} argument.
	 */
	public static final EntityReference NONE = new EntityReference(-1);
	/**
	 * References all the enemy characters (the opponent's hero and minions).
	 */
	public static final EntityReference ENEMY_CHARACTERS = new EntityReference(-2);
	/**
	 * References all the enemy's minions.
	 */
	public static final EntityReference ENEMY_MINIONS = new EntityReference(-3);
	/**
	 * References a list with a single element, the enemy's hero. This list may extremely rarely be empty if the target
	 * evaluation is being performed after the hero is destroyed.
	 */
	public static final EntityReference ENEMY_HERO = new EntityReference(-4);
	/**
	 * References all the friendly characters (the player's hero and minions).
	 */
	public static final EntityReference FRIENDLY_CHARACTERS = new EntityReference(-5);
	/**
	 * References all the player's minions.
	 */
	public static final EntityReference FRIENDLY_MINIONS = new EntityReference(-6);
	/**
	 * References all the player's minions except the {@code source} of this targeting resolution. For example,
	 * battlecries often specify other friendly minions to give a bonus to any friendly minion besides itself.
	 */
	public static final EntityReference OTHER_FRIENDLY_MINIONS = new EntityReference(-7);
	/**
	 * References a possibly zero-length list of minions adjacent to the {@code source} of this targeting resolution.
	 */
	public static final EntityReference ADJACENT_MINIONS = new EntityReference(-8);
	/**
	 * References the friendly hero.
	 */
	public static final EntityReference FRIENDLY_HERO = new EntityReference(-9);
	/**
	 * References all the minions on the board.
	 */
	public static final EntityReference ALL_MINIONS = new EntityReference(-10);
	/**
	 * References both heroes and all the minions on the board.
	 */
	public static final EntityReference ALL_CHARACTERS = new EntityReference(-11);
	/**
	 * References all the heroes and minions excluding the {@code source} of this targeting resolution.
	 */
	public static final EntityReference ALL_OTHER_CHARACTERS = new EntityReference(-12);
	/**
	 * References all the minions excluding the {@code source} of this targeting resolution.
	 */
	public static final EntityReference ALL_OTHER_MINIONS = new EntityReference(-13);
	/**
	 * References the {@link net.demilich.metastone.game.entities.weapons.Weapon} of the friendly hero, or a zero-length
	 * list if one isn't equipped.
	 */
	public static final EntityReference FRIENDLY_WEAPON = new EntityReference(-14);
	/**
	 * References the weapon of the enemy hero, or a zero-length list if one isn't equipped.
	 */
	public static final EntityReference ENEMY_WEAPON = new EntityReference(-15);
	/**
	 * References all the cards in the player's hand.
	 */
	public static final EntityReference FRIENDLY_HAND = new EntityReference(-16);
	/**
	 * References all the cards in the enemy player's hand./
	 */
	public static final EntityReference ENEMY_HAND = new EntityReference(-17);
	/**
	 * References zero, one or two minions opposite of this {@code source} minion.
	 */
	public static final EntityReference OPPOSITE_MINIONS = new EntityReference(-18);
	/**
	 * References the friendly minion whose index in the minions zone is 0, which can possibly be the {@code source}
	 * minion. Or, a zero length list if no friendly minions are on the board.
	 */
	public static final EntityReference LEFTMOST_FRIENDLY_MINION = new EntityReference(-19);
	/**
	 * References the enemy minion whose index in the minions zone is 0. Or, a zero length list if no enemy minions are on
	 * the board.
	 */
	public static final EntityReference LEFTMOST_ENEMY_MINION = new EntityReference(-20);
	/**
	 * References the friendly {@link Player} entity. This entity is a useful host for enchantments and attributes like
	 * {@link Attribute#EXTRA_TURN}.
	 */
	public static final EntityReference FRIENDLY_PLAYER = new EntityReference(-21);
	/**
	 * References the enemy {@link Player} entity. This entity is a useful host for enchantments and attributes like
	 * {@link Attribute#EXTRA_TURN}.
	 */
	public static final EntityReference ENEMY_PLAYER = new EntityReference(-22);
	/**
	 * References the minions with an index less than the {@code source} minion.
	 * <p>
	 * The client will appear to be targeting to the minions on-screen to the right of the {@code source}.
	 */
	public static final EntityReference MINIONS_TO_LEFT = new EntityReference(-23);
	/**
	 * References the minions with an index greater than the {@code source} minion.
	 * <p>
	 * The client will appear to be targeting to the minions on-screen to the left of the {@code source}.
	 */
	public static final EntityReference MINIONS_TO_RIGHT = new EntityReference(-24);
	/**
	 * When the player performs a {@link net.demilich.metastone.game.actions.GameAction} that has a target choice, like a
	 * physical attack, a targeted battlecry, casting a targeted spell or summoning a minion (interpreted as the minion
	 * the player is summoning next to), this reference will return the target that the player originally chose regardless
	 * of target overriding effects or effects in the sequence that may later destroy or transform the target.
	 */
	public static final EntityReference TARGET = new EntityReference(-25);
	/**
	 * This reference allows you to distinguish the target of an effect (the spell target) versus the target selected by a
	 * player (the {@link #TARGET}).
	 * <p>
	 * When an effect like a spell card, a battlecry or an enchantment casts a {@link
	 * net.demilich.metastone.game.spells.Spell} with a group reference, the spell is cast separately for each entity in
	 * the list of entities after that group reference is resolved. Use this reference to retrieve the specific entity
	 * (rather than the group reference) that the spell is currently being evaluated against.
	 * <p>
	 * For example, suppose you had a spell, "Choose a minion. For each other minion, you have a 50% of dealing damage
	 * equal to the minion's attack or the chosen minion's hitpoints". You need some way of referencing the minion being
	 * damaged versus the minion that was chosen by the player. Spell target is the minion currently being damaged, while
	 * {@link #TARGET} (or inherited from the topmost spell) is the minion that was chosen:
	 * <pre>
	 *     "targetSelection": "MINIONS",
	 *     "spell": {
	 *         "class": "EitherOrSpell",
	 *         "target": "ALL_OTHER_MINIONS",
	 *         "condition": {"class": "RandomCondition"},
	 *         "spell1": {
	 *             "class": "DamageSpell",
	 *             "value": {
	 *                 "class": "AttributeValueProvider",
	 *                 "target": "SPELL_TARGET",
	 *                 "attribute": "ATTACK"
	 *             }
	 *         },
	 *         "spell2": {
	 *             "class": "DamageSpell",
	 *             "value": {
	 *                 "class": "AttributeValueProvider",
	 *                 "target": "TARGET",
	 *                 "attribute": "HP"
	 *             }
	 *         }
	 *     }
	 * </pre>
	 */
	public static final EntityReference SPELL_TARGET = new EntityReference(-26);
	/**
	 * This reference retrieves the (possibly {@code null}) entity pointed to by {@link GameEvent#getEventTarget()}. An
	 * event target is never itself a group reference; it always retrieves a specific entity.
	 * <p>
	 * To see which game events generate targets, which can be counter-intuitive, look at the constructors of the various
	 * {@link GameEvent} classes and see how they're called.
	 * <p>
	 * Some important event targets include: <ul> <li>{@link net.demilich.metastone.game.events.PhysicalAttackEvent}: The
	 * attacker.</li> <li>{@link net.demilich.metastone.game.events.SpellCastedEvent}: The <b>source card</b></li>
	 * <li>{@link net.demilich.metastone.game.events.SummonEvent}: The minion being summoned.</li> <li>{@link
	 * net.demilich.metastone.game.events.AfterSpellCastedEvent}: For a targeted spell, the chosen target or, if it was
	 * overridden by an effect, the override.</li> </ul>
	 */
	public static final EntityReference EVENT_TARGET = new EntityReference(-27);
	/**
	 * Returns the {@code source} of the target resolution.
	 * <p>
	 * Inside an enchantment's {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#FIRE_CONDITION} and
	 * {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#QUEUE_CONDITION}, which are the conditions
	 * that evaluate whether or not the trigger for the enchantment should fire, the source entity is the {@link
	 * GameEvent#getEventSource()}; it is <b>not</b> the entity hosting the trigger. To get the entity hosting the
	 * trigger, use {@link #TRIGGER_HOST} instead.
	 */
	public static final EntityReference SELF = new EntityReference(-28);
	/**
	 * Retrieves a reference to the minion destroyed by a {@link net.demilich.metastone.game.spells.DestroySpell}.
	 */
	public static final EntityReference KILLED_MINION = new EntityReference(-29);
	/**
	 * This references the attacker during a {@link net.demilich.metastone.game.actions.PhysicalAttackAction} or {@link
	 * net.demilich.metastone.game.logic.GameLogic#fight(Player, Actor, Actor, PhysicalAttackAction)} invocation (i.e., a
	 * {@link net.demilich.metastone.game.spells.FightSpell}).
	 */
	public static final EntityReference ATTACKER = new EntityReference(-30);
	/**
	 * Use this reference to apply effects to new entities created by spells.
	 * <p>
	 * For example, to apply a buff to a newly summoned minion:
	 * <pre>
	 *     "spell": {
	 *         "class": "SummonSpell",
	 *         "card": "minion_bloodfen_raptor",
	 *         "spell": {
	 *             "class": "BuffSpell",
	 *             "target": "OUTPUT",
	 *             "attackBonus": 2
	 *         }
	 *     }
	 * </pre>
	 * Like {@link #SPELL_TARGET}, this reference helps you distinguish between the player's chosen {@link #TARGET}, which
	 * will be propagated downwards to child spells, and the entity created by something like a {@link
	 * net.demilich.metastone.game.spells.SummonSpell}.
	 *
	 * @see net.demilich.metastone.game.spells.SpellUtils#castChildSpell(GameContext, Player, SpellDesc, Entity, Entity,
	 *    Entity) for the method that sets the {@link net.demilich.metastone.game.environment.Environment#OUTPUTS}. All the
	 * 		usages of this method set outputs.
	 */
	public static final EntityReference OUTPUT = new EntityReference(-32);
	/**
	 * References all the cards in the player's deck.
	 */
	public static final EntityReference FRIENDLY_DECK = new EntityReference(-33);
	/**
	 * References all the card's in the enemy player's deck.
	 */
	public static final EntityReference ENEMY_DECK = new EntityReference(-34);
	/**
	 * References all the cards in both decks.
	 */
	public static final EntityReference BOTH_DECKS = new EntityReference(-35);
	/**
	 * References all the cards in both hands.
	 */
	public static final EntityReference BOTH_HANDS = new EntityReference(-36);
	/**
	 * When a minion gets transformed while it is being summoned, the result of the transformation of the minion is found
	 * with this reference.
	 * <p>
	 * Deprecated by {@link Entity#transformResolved(GameContext)}.
	 */
	@Deprecated
	public static final EntityReference TRANSFORM_REFERENCE = new EntityReference(-37);
	/**
	 * References the friendly minion with the largest index in the minions zone, or an empty list if the minion zone is
	 * empty.
	 * <p>
	 * The rightmost minion in the client will appear to be the leftmost one.
	 */
	public static final EntityReference RIGHTMOST_FRIENDLY_MINION = new EntityReference(-38);
	/**
	 * References the enemy minion with the largest index in the minions zone, or an empty list if the minion zone is
	 * empty.
	 * <p>
	 * The rightmost minion in the client will appear to be the leftmost one.
	 */
	public static final EntityReference RIGHTMOST_ENEMY_MINION = new EntityReference(-39);
	/**
	 * References the minions adjancent to the attacker during a {@link net.demilich.metastone.game.logic.GameLogic#fight(Player,
	 * Actor, Actor, PhysicalAttackAction)} sequence.
	 */
	public static final EntityReference ATTACKER_ADJACENT_MINIONS = new EntityReference(-40);
	/**
	 * References the cards and actors located in the friendly set aside zone. Rarely used.
	 */
	public static final EntityReference FRIENDLY_SET_ASIDE = new EntityReference(-41);
	/**
	 * References the cards and actors located in the enemy set aside zone. Rarely used.
	 */
	public static final EntityReference ENEMY_SET_ASIDE = new EntityReference(-42);
	/**
	 * References the cards and actors in the friendly graveyard.
	 * <p>
	 * TODO: It is not easy to distinguish between cards and actors in the graveyard.
	 * <p>
	 * To retrieve cards, use the {@link net.demilich.metastone.game.spells.desc.source.GraveyardCardsSource}. To retrieve
	 * actors (weapons, minions and heroes that have been destroyed), use {@link net.demilich.metastone.game.spells.desc.source.GraveyardActorsSource}.
	 */
	public static final EntityReference FRIENDLY_GRAVEYARD = new EntityReference(-43);
	/**
	 * References the cards and actors in the enemy graveyard.
	 * <p>
	 * TODO: It is not easy to distinguish between cards and actors in the graveyard.
	 * <p>
	 * To retrieve cards, use the {@link net.demilich.metastone.game.spells.desc.source.GraveyardCardsSource}. To retrieve
	 * actors (weapons, minions and heroes that have been destroyed), use {@link net.demilich.metastone.game.spells.desc.source.GraveyardActorsSource}.
	 */
	public static final EntityReference ENEMY_GRAVEYARD = new EntityReference(-44);
	/**
	 * References all entities in the game, including transformed ones and permanents. Typically used to find attributes
	 * about specific cards played or used in the past; or to find specific permanents in play.
	 *
	 * @see net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter for a way to filter for specific card IDs
	 * 		using this reference.
	 */
	public static final EntityReference ALL_ENTITIES = new EntityReference(-45);
	/**
	 * References the {@link GameEvent#getEventSource()} entity when a trigger/enchantment is being evaluated.
	 *
	 * @see #EVENT_TARGET for more on how to discover what the source is set to for common events.
	 */
	public static final EntityReference EVENT_SOURCE = new EntityReference(-46);
	/**
	 * References the next card the friendly player will draw, or an empty list if no cards remain in the player's deck.
	 */
	public static final EntityReference FRIENDLY_TOP_CARD = new EntityReference(-47);
	/**
	 * References the next card the opponent will draw, or an empty list if no cards remain in the player's deck.
	 */
	public static final EntityReference ENEMY_TOP_CARD = new EntityReference(-48);
	/**
	 * References the card that is currently the friendly player's hero power.
	 */
	public static final EntityReference FRIENDLY_HERO_POWER = new EntityReference(-49);
	/**
	 * References the card that is currently the enemey player's hero power.
	 */
	public static final EntityReference ENEMY_HERO_POWER = new EntityReference(-50);
	/**
	 * References all the enemy minions excluding the source.
	 * <p>
	 * Implements Frost Bomb.
	 */
	public static final EntityReference OTHER_ENEMY_MINIONS = new EntityReference(-51);
	/**
	 * References the card in the friendly player's hand with the lowest index in the friendly player's hand.
	 * <p>
	 * Implements Antique Collector and Ironforge Baker.
	 */
	public static final EntityReference LEFTMOST_FRIENDLY_CARD_HAND = new EntityReference(-52);
	/**
	 * References the last card played by either player. It will typically be in the player's hand (before it is casted)
	 * or in the graveyard (at most other times).
	 * <p>
	 * Implements Shadow of the Past.
	 */
	public static final EntityReference LAST_CARD_PLAYED = new EntityReference(-53);
	/**
	 * References the last card played by the friendly player.
	 */
	public static final EntityReference FRIENDLY_LAST_CARD_PLAYED = new EntityReference(-54);
	/**
	 * References the last card played by the opponent.
	 */
	public static final EntityReference ENEMY_LAST_CARD_PLAYED = new EntityReference(-55);
	/**
	 * References the {@link Entity} that hosts the {@link net.demilich.metastone.game.spells.trigger.Enchantment} whose
	 * fire condition or spell is currently being evaluated.
	 * <p>
	 * During a condition evaluation on an {@link net.demilich.metastone.game.spells.trigger.EventTrigger}, {@link #SELF}
	 * refers to {@link GameEvent#getEventSource()}, not the host of the trigger whose condition is being evaluated. Use
	 * this reference to get the host of the trigger currently being evaluated.
	 *
	 * @see #SELF for an important comparison about how this reference is used.
	 */
	public static final EntityReference TRIGGER_HOST = new EntityReference(-56);

	/**
	 * References all the enemy's minions, except ordered by their location on the board instead of their order of play.
	 */
	public static final EntityReference ENEMY_MINIONS_LEFT_TO_RIGHT = new EntityReference(-57);
	/**
	 * References the enemy minions and hero which would ordinarily be targetable by a physical attack from the friendly
	 * player's point of view.
	 */
	public static final EntityReference PHYSICAL_ATTACK_TARGETS = new EntityReference(-58);
	/**
	 * References the minion to the left of the {@code source} of this targeting resolution, or a zero-length list if
	 * there isn't one
	 */
	public static final EntityReference LEFT_ADJACENT_MINION = new EntityReference(-59);
	/**
	 * References the minion to the right of the {@code source} of this targeting resolution, or a zero-length list if
	 * there isn't one
	 */
	public static final EntityReference RIGHT_ADJACENT_MINION = new EntityReference(-60);
	/**
	 * References the last card played by either player before the end of this current sequence.
	 * <p>
	 * Implements Study.
	 */
	public static final EntityReference LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE = new EntityReference(-61);
	/**
	 * References the last card played by the friendly player before this current sequence.
	 */
	public static final EntityReference FRIENDLY_LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE = new EntityReference(-62);
	/**
	 * References the last card played by the opponent before this current sequence.
	 */
	public static final EntityReference ENEMY_LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE = new EntityReference(-63);
	/**
	 * References all the friendly minions, except ordered by their location on the board instead of their order of play.
	 */
	public static final EntityReference FRIENDLY_MINIONS_LEFT_TO_RIGHT = new EntityReference(-64);
	/**
	 * References the minions adjacent to the current {@link EntityReference#TARGET}, i.e., the currently selected target
	 * in the pending action.
	 */
	public static final EntityReference ADJACENT_TO_TARGET = new EntityReference(-65);
	/**
	 * References the last minion belonging to the friendly player that died.
	 */
	public static final EntityReference FRIENDLY_LAST_DIED_MINION = new EntityReference(-66);
	/**
	 * References all the cards in the player's deck, ordered from the top of the deck towards the bottom.
	 */
	public static final EntityReference FRIENDLY_DECK_FROM_TOP = new EntityReference(-67);
	/**
	 * References the minion that is currently being summoned.
	 * <p>
	 * This entry is valid from just before {@link net.demilich.metastone.game.events.BeforeSummonEvent} is fired and just
	 * after {@link net.demilich.metastone.game.events.AfterSummonEvent} is fired. Since battlecries are resolved between
	 * {@link BeforeSummonEvent} and {@link net.demilich.metastone.game.events.SummonEvent}, this reference may change to
	 * the minion being summoned by a battlecry rather than the battlecrying minion itself, depending on when this
	 * reference resolved.
	 */
	public static final EntityReference CURRENT_SUMMONING_MINION = new EntityReference(-68);
	/**
	 * References all entities that are "in play" for the friendly player i.e. cards in hand, deck, set aside + minions +
	 * hero.
	 */
	public static final EntityReference FRIENDLY_CARDS = new EntityReference(-69);
	/**
	 * References all entities that are "in play" for the enemy player i.e. cards in hand, deck, set aside + minions +
	 * hero.
	 */
	public static final EntityReference ENEMY_CARDS = new EntityReference(-70);
	/**
	 * References the minions that are in the middle of the enemy's battlefield. This will return two minions when the
	 * number of minions on the opponent's battlefield is even.
	 */
	public static final EntityReference ENEMY_MIDDLE_MINIONS = new EntityReference(-71);
	/**
	 * References the last minion card played before this current sequence.
	 */
	public static final EntityReference FRIENDLY_LAST_MINION_PLAYED = new EntityReference(-72);
	/**
	 * References a combination of {@link #OTHER_FRIENDLY_MINIONS} and the {@link #FRIENDLY_HERO}.
	 */
	public static final EntityReference OTHER_FRIENDLY_CHARACTERS = new EntityReference(-73);
	/**
	 * References the card in the friendly player's hand with the highest index.
	 * <p>
	 * Implements Mutamite Terror.
	 */
	public static final EntityReference RIGHTMOST_FRIENDLY_CARD_HAND = new EntityReference(-74);
	/**
	 * References the card in the enemy player's hand with the lowest index in the enemy player's hand.
	 * <p>
	 * Implements Mindswapper.
	 */
	public static final EntityReference LEFTMOST_ENEMY_CARD_HAND = new EntityReference(-75);
	/**
	 * References the last spell played by this player this turn only.
	 * <p>
	 * Implements Recurring Torrent.
	 */
	public static final EntityReference FRIENDLY_LAST_SPELL_PLAYED_THIS_TURN = new EntityReference(-76);
	/**
	 * Returns the opposing characters from the {@code source}. If there is no minion opposite of the {@code source},
	 * returns the opposing champion instead.
	 */
	public static final EntityReference OPPOSITE_CHARACTERS = new EntityReference(-77);

	public static final EntityReference FRIENDLY_SIGNATURE = new EntityReference(-78);

	public static final EntityReference ENEMY_SIGNATURE = new EntityReference(-78);

	public static EntityReference pointTo(Entity entity) {
		if (entity == null) {
			return null;
		}
		return new EntityReference(entity.getId());
	}

	private int id;

	private EntityReference() {
	}

	public EntityReference(int key) {
		this.id = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EntityReference)) {
			return false;
		}
		EntityReference entityReference = (EntityReference) obj;
		return entityReference.getId() == getId();
	}

	public int getId() {
		return id;
	}

	public void setId(int key) {
		this.id = key;
	}

	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * When {@code true}, indicates that this entity reference references <b>not</b> to a specific {@link Entity} but to a
	 * pointer to an entity, like {@link EntityReference#SELF}.
	 *
	 * @return {@code true} if the {@link #id} is negative, which all the special {@link EntityReference} static elements
	 * 		are.
	 */
	public boolean isTargetGroup() {
		return id < 0;
	}

	@Override
	public String toString() {
		return String.format("[EntityReference id:%d]", id);
	}
}
