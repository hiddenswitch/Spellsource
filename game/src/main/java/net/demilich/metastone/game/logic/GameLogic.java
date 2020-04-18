package net.demilich.metastone.game.logic;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.collect.Multiset;
import com.hiddenswitch.spellsource.client.models.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.PhysicalAttackEvent;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.aura.*;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.OriginalValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.spells.trigger.*;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.*;
import net.demilich.metastone.game.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * The game logic class implements the basic primitives of gameplay.
 * <p>
 * This class processes all the changes to a game state (a variety of fields in a {@link GameContext} between requests
 * for player actions. It does not make player action requests itself (the {@link GameContext} is responsible for
 * that).
 * <p>
 * Most functions will accept a {@link GameContext} as an argument and mutate it. You can use {@link
 * GameContext#clone()} to create an "immutable" equivalent behaviour.
 * <p>
 * Most effects are encoded in {@link Spell} classes, which subsequently call functions in this class. However, a few
 * key functions are called by {@link GameAction#execute(GameContext, int)} calls directly, like {@link #summon(int,
 * Minion, Entity, int, boolean)} and {@link #fight(Player, Actor, Actor, PhysicalAttackAction)}.
 */
public class GameLogic implements Cloneable, Serializable, IdFactory {
	public static final int END_OF_SEQUENCE_MAX_DEPTH = 14;
	protected static Logger logger = LoggerFactory.getLogger(GameLogic.class);
	/**
	 * The maximum number of {@link Minion} entities that can be on a {@link Zones#BATTLEFIELD}.
	 */
	public static final int MAX_MINIONS = 7;
	/**
	 * The maximum number of deathrattle enchantments that can be added to an actor.
	 */
	public static final int MAX_DEATHRATTLES = 16;
	/**
	 * The maximum number of {@link Card} entities that can be in a {@link Zones#HAND}.
	 */
	public static final int MAX_HAND_CARDS = 10;
	/**
	 * The default maximum {@link Attribute#HP} a {@link Hero} can have.
	 */
	public static final int MAX_HERO_HP = 30;
	/**
	 * The number of {@link Card} entities that a {@link Player} should start with at the beginning of a game.
	 */
	public static final int STARTER_CARDS = 3;
	/**
	 * The maximum amount of mana a {@link Player} can have at the start of a turn. Some effects allow a player to spend
	 * more than {@link #MAX_MANA} mana in a turn, but never start with more.
	 */
	public static final int MAX_MANA = 10;
	/**
	 * The maximum number of {@link Secret} entities that can be in a {@link Zones#SECRET}.
	 */
	public static final int MAX_SECRETS = 5;
	/**
	 * The maximum number of {@link Quest} entities that can be in a {@link Zones#QUEST}.
	 */
	public static final int MAX_QUESTS = 1;
	/**
	 * The maximum number of {@link Card} entities that a {@link Player} can build a {@link GameDeck} with. Some effects,
	 * like Prince Malchezaar's text, allow the player to start a game with more than {@link #DECK_SIZE} cards.
	 */
	public static final int DECK_SIZE = 30;
	/**
	 * The maximum number of {@link Card} entities that can be in a {@link Zones#DECK} zone.
	 */
	public static final int MAX_DECK_SIZE = 60;
	/**
	 * The maximum number of turns until a game is forced into a draw.
	 * <p>
	 * If both heroes take measures to survive for this long, the game ends in an unconditional draw at the start of the
	 * 90th turn, even if both players are Immune.
	 */
	public static final int TURN_LIMIT = 89;
	/**
	 * The default amount of time a player has to complete a turn in seconds.
	 */
	public static final int DEFAULT_TURN_TIME = 75;
	/**
	 * The default amount of time a player has to mulligan in seconds.
	 */
	public static final int DEFAULT_MULLIGAN_TIME = 65;
	/**
	 * The number of attacks gained by {@link Attribute#WINDFURY}.
	 *
	 * @see Actor#canAttackThisTurn() for the complete attack count logic.
	 */
	public static final int WINDFURY_ATTACKS = 2;
	/**
	 * The number of attacks gained by {@link Attribute#MEGA_WINDFURY}.
	 *
	 * @see Actor#canAttackThisTurn() for the complete attack count logic.
	 */
	public static final int MEGA_WINDFURY_ATTACKS = 4;
	/**
	 * Represents the maximum number of spells that can be evaluated by the game logic since the start of performing a
	 * game action.
	 * <p>
	 * This will probably be migrated to the entire game context when it becomes possible to programmatically perform a
	 * game action.
	 */
	public static final int MAX_PROGRAM_COUNTER = 1000;
	/**
	 * This {@link Set} stores each {@link Attribute} that is not cleared when an {@link Entity} is silenced.
	 *
	 * @see #silence(int, Actor) for the silence game logic.
	 */
	public static final Set<Attribute> IMMUNE_TO_SILENCE = new HashSet<>();
	/**
	 * A prefix appended to cards that are temporarily generated by game rules.
	 *
	 * @see GameLogic#generateCardId() for the situation where card IDs need to be generated.
	 */
	private static final int MAX_SPELL_DEPTH = 288;
	private static final String TEMP_CARD_LABEL = "temp_card_id_";
	public static final int INFINITE = -1;
	public static final String DEFAULT_SIGNATURE = "token_berry";
	private final TargetLogic targetLogic = new TargetLogic();
	private final ActionLogic actionLogic = new ActionLogic();
	private IdFactoryImpl idFactory;
	private long seed = XORShiftRandom.createSeed();
	private XORShiftRandom random = new XORShiftRandom(seed);
	private transient int spellDepth = 0;
	private transient int programCounter = 0;
	protected transient GameContext context;

	static {
		IMMUNE_TO_SILENCE.addAll(Attribute.getAuraAttributes());
		IMMUNE_TO_SILENCE.add(Attribute.HP);
		IMMUNE_TO_SILENCE.add(Attribute.MAX_HP);
		IMMUNE_TO_SILENCE.add(Attribute.BASE_HP);
		IMMUNE_TO_SILENCE.add(Attribute.BASE_ATTACK);
		IMMUNE_TO_SILENCE.add(Attribute.SUMMONING_SICKNESS);
		IMMUNE_TO_SILENCE.add(Attribute.RACE);
		IMMUNE_TO_SILENCE.add(Attribute.DESTROYED);
		IMMUNE_TO_SILENCE.add(Attribute.NUMBER_OF_ATTACKS);
		IMMUNE_TO_SILENCE.add(Attribute.PERMANENT);
		IMMUNE_TO_SILENCE.add(Attribute.COPIED_FROM);
		IMMUNE_TO_SILENCE.add(Attribute.TRANSFORM_REFERENCE);
		IMMUNE_TO_SILENCE.add(Attribute.DAMAGE_THIS_TURN);
		IMMUNE_TO_SILENCE.add(Attribute.HERO_CLASS);
		IMMUNE_TO_SILENCE.add(Attribute.STARTED_IN_DECK);
		IMMUNE_TO_SILENCE.add(Attribute.USER_ID);
		IMMUNE_TO_SILENCE.add(Attribute.LAST_HIT);
		IMMUNE_TO_SILENCE.add(Attribute.LAST_HEAL);
		IMMUNE_TO_SILENCE.add(Attribute.NAME);
		IMMUNE_TO_SILENCE.add(Attribute.DESCRIPTION);
		IMMUNE_TO_SILENCE.add(Attribute.DRAINED_LAST_TURN);
		IMMUNE_TO_SILENCE.add(Attribute.LAST_MINION_DESTROYED_CARD_ID);
		IMMUNE_TO_SILENCE.add(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_BOOLEAN_1);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_BOOLEAN_2);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_BOOLEAN_3);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_BOOLEAN_4);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_BOOLEAN_5);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_INTEGER_1);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_INTEGER_2);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_INTEGER_3);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_INTEGER_4);
		IMMUNE_TO_SILENCE.add(Attribute.RESERVED_INTEGER_5);
		IMMUNE_TO_SILENCE.add(Attribute.CARD_INVENTORY_ID);
		IMMUNE_TO_SILENCE.add(Attribute.TREANT);
		IMMUNE_TO_SILENCE.add(Attribute.LACKEY);
	}

	/**
	 * Creates a new game logic instance whose next ID generated for an {@link Entity#setId(int)} argument will be zero.
	 */
	public GameLogic() {
		idFactory = new IdFactoryImpl();
	}

	/**
	 * Creates a game logic instance with an ID factory. Typically you can create an ID factory and set its current ID to
	 * whichever number you want to be the next entity ID created by this game logic.
	 *
	 * @param idFactory An existing ID factory.
	 */
	private GameLogic(IdFactoryImpl idFactory) {
		this.idFactory = idFactory;
	}

	/**
	 * Creates a game logic instance with the specified seed.
	 *
	 * @param seed A random seed.
	 */
	public GameLogic(long seed) {
		this();
		this.seed = seed;
		random = new XORShiftRandom(seed);
	}

	/**
	 * Create a game logic instance with the specified seed and ID factory.
	 *
	 * @param idFactory
	 * @param seed
	 */
	public GameLogic(IdFactoryImpl idFactory, long seed) {
		this(idFactory);
		this.seed = seed;
		random = new XORShiftRandom(seed);
	}

	/**
	 * Indicates whether or not the instance is of the specified card type.
	 * <p>
	 * Use this instead of direct comparisons to interpret a choose one card as a spell card.
	 *
	 * @param thisType
	 * @param other    The card type to compare to.
	 * @return {@code true} if this instance is of the {@code cardType}.
	 */
	public static boolean isCardType(CardType thisType, CardType other) {
		if (thisType == CardType.CHOOSE_ONE && other == CardType.SPELL) {
			return true;
		} else if (thisType == other) {
			return true;
		}
		return false;
	}

	/**
	 * Compares two rarities, taking into account that a free and common rarity are the same from a gameplay point of
	 * view.
	 *
	 * @param thisRarity
	 * @param other
	 * @return
	 */
	public static boolean isRarity(Rarity thisRarity, Rarity other) {
		if (thisRarity == Rarity.FREE && other == Rarity.COMMON) {
			return true;
		} else if (thisRarity == other) {
			return true;
		}
		return false;
	}

	/**
	 * Modifies the {@code target} entity's HP, firing the {@link MaxHpIncreasedEvent} and incrementing its
	 *
	 * @param source
	 * @param target
	 * @param hpBonus
	 */
	@Suspendable
	public void modifyHpSpell(Entity source, Entity target, int hpBonus) {
		target.modifyHpBonus(hpBonus);
		if (hpBonus > 0) {
			target.modifyAttribute(Attribute.TOTAL_HP_INCREASES, hpBonus);
			context.fireGameEvent(new MaxHpIncreasedEvent(context, target, hpBonus, source.getOwner()));
		}
	}

	/**
	 * Adds a {@link Trigger} to a specified {@link Entity}. These are typically {@link Enchantment} instances that react
	 * to game events.
	 *
	 * @param player            Usually the current turn player.
	 * @param gameEventListener A game event listener, like a {@link Aura}, {@link Secret} or {@link CardCostModifier}.
	 * @param host              The {@link Entity} that will be pointed to by {@link Trigger#getHostReference()}.
	 * @see TriggerManager#fireGameEvent(GameEvent, List) for the complete implementation of triggers.
	 */
	@Suspendable
	public void addGameEventListener(Player player, Trigger gameEventListener, Entity host) {
		if (context.updateAndGetGameOver() || (host.hasAttribute(Attribute.CANT_GAIN_ENCHANTMENTS) && gameEventListener instanceof Enchantment)) {
			// Don't add game event listeners while the game is over or if the target shouldn't get it
			return;
		}

		if (Objects.equals(host.getReference(), EntityReference.NONE)
				&& Objects.equals(gameEventListener.getHostReference(), EntityReference.NONE)) {
			var message = String.format("addGameEventListener %s %s: References are none!", host, gameEventListener);
			throw new RuntimeException(message);
		} else if (!Objects.equals(host.getReference(), EntityReference.NONE)) {
			gameEventListener.setHost(host);
		}
		if (!gameEventListener.hasPersistentOwner() || gameEventListener.getOwner() == Entity.NO_OWNER) {
			gameEventListener.setOwner(player.getId());
		}

		if (gameEventListener instanceof Enchantment) {
			// Start assigning IDs to these things if they don't have an ID
			Enchantment enchantment = (Enchantment) gameEventListener;

			if (enchantment.getId() == IdFactory.UNASSIGNED) {
				enchantment.setId(generateId());
			}

			if (enchantment.getSourceCard() == null) {
				enchantment.setSourceCard(host.getSourceCard());
			}
		}


		gameEventListener.onAdd(context);
		context.getTriggerManager().addTrigger(gameEventListener);
	}

	public int generateId() {
		var i = idFactory.generateId();
		return i;
	}

	/**
	 * Calculates how much to amplify an attribute by. This is typically either a spell damage or healing effect
	 * multiplier.
	 * <p>
	 * This implements Prophet Velen.
	 *
	 * @param player    The friendly player.
	 * @param baseValue The value to multiply.
	 * @param attribute The attribute to look up in all entities.
	 * @return The newly calculate spell or healing value.
	 * @see Attribute#SPELL_HEAL_AMPLIFY_MULTIPLIER for the healing amplification attribute.
	 * @see Attribute#SPELL_DAMAGE_AMPLIFY_MULTIPLIER for the spell damage amplification attribute.
	 */
	@Suspendable
	public int applyAmplify(Player player, int baseValue, Attribute attribute) {
		int amplify = getTotalAttributeMultiplier(player, attribute);
		return baseValue * amplify;
	}

	/**
	 * Gives an {@link Entity} a boolean {@link Attribute}.
	 * <p>
	 * This addresses bugs with {@link Attribute#WINDFURY} and should be the place for special rules around attributes in
	 * the future.
	 *
	 * @param entity An {@link Entity}
	 * @param attr   An {@link Attribute}
	 */
	@Suspendable
	public void applyAttribute(Entity entity, Attribute attr) {
		applyAttribute(entity, attr, null);
	}

	/**
	 * Gives an {@link Entity} a boolean {@link Attribute}.
	 * <p>
	 * This addresses bugs with {@link Attribute#WINDFURY} and should be the place for special rules around attributes in
	 * the future.
	 *
	 * @param entity An {@link Entity}
	 * @param attr   An {@link Attribute}
	 * @param source A source entity for the attribute application.
	 */
	@Suspendable
	public void applyAttribute(Entity entity, Attribute attr, Entity source) {
		boolean hasWindfury = entity.hasAttribute(Attribute.WINDFURY) || entity.hasAttribute(Attribute.AURA_WINDFURY);
		boolean hasMegaWindfury = entity.hasAttribute(Attribute.MEGA_WINDFURY);
		boolean gainingWindfury = attr == Attribute.WINDFURY || attr == Attribute.AURA_WINDFURY;
		boolean gainingMegaWindfury = attr == Attribute.MEGA_WINDFURY;
		int numberOfAttacks = -1;

		if (!hasWindfury && !hasMegaWindfury && gainingWindfury) {
			numberOfAttacks = WINDFURY_ATTACKS - 1;
		} else if (!hasWindfury && !hasMegaWindfury && gainingMegaWindfury) {
			numberOfAttacks = MEGA_WINDFURY_ATTACKS - 1;
		} else if (hasWindfury && !hasMegaWindfury && gainingMegaWindfury) {
			numberOfAttacks = MEGA_WINDFURY_ATTACKS - WINDFURY_ATTACKS;
		}

		if (numberOfAttacks != -1) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, numberOfAttacks);
		}

		entity.setAttribute(attr);
		context.fireGameEvent(new AttributeAppliedEvent(context, entity.getOwner(), entity, source, attr));
	}

	/**
	 * Applies hero power damage increases
	 *
	 * @param player    The Player to grab additional hero power damage from
	 * @param baseValue The base damage the hero power does
	 * @return Increased hero power damage
	 */
	@Suspendable
	public int applyHeroPowerDamage(Player player, int baseValue) {
		int spellpower = getTotalAttributeValue(player, Attribute.HERO_POWER_DAMAGE);
		return baseValue + spellpower;
	}

	/**
	 * Applies spell damage increases
	 *
	 * @param player    The Player to grab the additional spell damage from
	 * @param source    The source Card
	 * @param baseValue The base damage the spell does
	 * @return Increased spell damage
	 */
	@Suspendable
	public int applySpellpower(Player player, Entity source, int baseValue) {
		int spellpower = getTotalAttributeValue(player, Attribute.SPELL_DAMAGE)
				+ getTotalAttributeValue(player, Attribute.AURA_SPELL_DAMAGE)
				+ getTotalAttributeValue(context.getOpponent(player), Attribute.OPPONENT_SPELL_DAMAGE);
		if (source.hasAttribute(Attribute.SPELL_DAMAGE_MULTIPLIER)) {
			spellpower *= source.getAttributeValue(Attribute.SPELL_DAMAGE_MULTIPLIER);
		}
		return baseValue + spellpower;
	}

	/**
	 * Assigns an {@link Entity#getId()} and {@link Entity#getOwner()} to each {@link Card} in a given {@link GameDeck}.
	 *
	 * @param cardList   The {@link GameDeck} whose cards should have IDs and owners assigned.
	 * @param ownerIndex The owner to assign to this {@link CardList}
	 */
	protected void assignEntityIds(Iterable<? extends Entity> cardList, int ownerIndex) {
		Player player = context.getPlayer(ownerIndex);

		for (Entity entity : cardList) {
			entity.setId(generateId());
			entity.setOwner(ownerIndex);
			player.updateLookup(entity);
		}
	}

	/**
	 * Checks if any {@link Entity} in the game has the given {@link Attribute}.
	 *
	 * @param attr The attribute to look up.
	 * @return {@code true} if any {@link Entity} has the given attribute.
	 */
	@Suspendable
	public boolean attributeExists(Attribute attr) {
		return context.getEntities().anyMatch(e -> e.hasAttribute(attr));
	}

	/**
	 * Determines whether the given player can play the given card. Useful for drawing green borders around cards to
	 * signal to an end user that they can play a particular card. Takes into account whether or not a spell that requires
	 * targets has possible targets in the game.
	 *
	 * @param playerId        The player whose point of view should be considered for this method.
	 * @param entityReference A reference to the card.
	 * @return {@code true} if the card can be played.
	 */
	@Suspendable
	public boolean canPlayCard(int playerId, EntityReference entityReference) {
		Player player = context.getPlayer(playerId);
		Card card = (Card) context.resolveSingleTarget(entityReference);
		return canPlayCard(player, card);
	}

	/**
	 * Determines whether the given player can play the given card. Useful for drawing green borders around cards to
	 * signal to an end user that they can play a particular card. Takes into account whether or not a spell that requires
	 * targets has possible targets in the game.
	 *
	 * @param player The player whose point of view should be considered for this method.
	 * @param card   The card.
	 * @return {@code true} if the card can be played.
	 */
	@Suspendable
	public boolean canPlayCard(Player player, Card card) {
		int playerId = player.getId();
		EntityReference entityReference = card.getReference();
		// A player cannot play a card the player does not own.
		if (card.getOwner() != player.getId()
				&& card.getOwner() != Entity.NO_OWNER) {
			return false;
		}
		int manaCost = getModifiedManaCost(player, card);

		List<CardCostInsteadAura> costAuras = SpellUtils.getAuras(context, playerId, CardCostInsteadAura.class);
		boolean cardCostOverridden = costAuras.size() > 0 && costAuras.stream().anyMatch(aura -> aura.getAffectedEntities().contains(entityReference.getId()));
		// Only play the last card cost override whose condition was met.
		if (cardCostOverridden) {
			// We're reversing the cost auras because the most recent card cost aura to come into play is most intuitively the
			// one we're paying with.
			Collections.reverse(costAuras);
			for (CardCostInsteadAura aura : costAuras) {
				// The card is affected by the cost condition if the card or the player casting it are affected by the aura.
				if (aura.getAffectedEntities().contains(entityReference.getId()) || aura.getAffectedEntities().contains(playerId)) {
					return aura.getCanAffordCondition().isFulfilled(context, player, card, card);
				}
			}
		} else if (doesCardCostHealth(player, card)
				&& player.getHero().getEffectiveHp() < manaCost
				&& manaCost != 0) {
			return false;
		} else if (!doesCardCostHealth(player, card)
				&& player.getMana() < manaCost
				&& manaCost != 0) {
			return false;
		}

		if (isCardType(card.getCardType(), CardType.HERO_POWER)) {
			Card power = card;
			int heroPowerUsages = getGreatestAttributeValue(player, Attribute.HERO_POWER_USAGES);
			if (heroPowerUsages == 0) {
				heroPowerUsages = 1;
			}
			if (heroPowerUsages != INFINITE && power.hasBeenUsed() >= heroPowerUsages) {
				return false;
			}

			// Implements Mindbreaker
			if (heroPowersDisabled()) {
				return false;
			}

			return card.canBeCast(context, player);
		} else if (isCardType(card.getCardType(), CardType.MINION)) {
			return canSummonMoreMinions(player);
		}

		if (isCardType(card.getCardType(), CardType.SPELL)) {
			return card.canBeCast(context, player);
		}
		return true;
	}

	/**
	 * Are hero powers disabled?
	 * <p>
	 * Disables hero powers from being able to be played and disables their passive triggers.
	 * <p>
	 * This implements Mindbreaker
	 *
	 * @return {@code true} if hero powers are disabled.
	 */
	public boolean heroPowersDisabled() {
		return context.getPlayers().stream().anyMatch(p -> hasAttribute(p, Attribute.HERO_POWERS_DISABLED));
	}

	/**
	 * Determines whether a player can play a {@link Secret}.
	 * <p>
	 * Players cannot have more than one copy of the same Secret active at any one time. Players are unable to play Secret
	 * cards which match one of their active Secrets.
	 * <p>
	 * When played directly from the hand, players can have up to 5 different Secrets active at a time. Once this limit is
	 * reached, the player will be unable to play further Secret cards.
	 *
	 * @param player The player whose {@link Zones#SECRET} zone should be inspected.
	 * @param card   The secret card being evaluated.
	 * @return {@code true} if the secret can be played.
	 */
	public boolean canPlaySecret(Player player, @NotNull Card card) {
		return player.getSecrets().size() < MAX_SECRETS && !player.getSecretCardIds().contains(card.getCardId());
	}

	/**
	 * Determines whether a player can play a quest.
	 * <p>
	 * Quests count as secrets
	 *
	 * @param player
	 * @param card
	 * @return
	 */
	public boolean canPlayQuest(Player player, @NotNull Card card) {
		return player.getSecrets().size() < MAX_SECRETS && player.getQuests().stream().filter(quest -> !quest.isPact()).collect(toList()).size() < MAX_QUESTS
				&& player.getQuests().stream().map(Quest::getSourceCard).map(Card::getCardId).noneMatch(cid -> cid.equals(card.getCardId()));
	}

	/**
	 * Determines whether a player can play a pact.
	 * <p>
	 * Pacts count as quests
	 *
	 * @param player
	 * @param card
	 * @return
	 */
	public boolean canPlayPact(Player player, @NotNull Card card) {
		return player.getSecrets().size() < MAX_SECRETS
				&& player.getQuests().stream().map(Quest::getSourceCard).map(Card::getCardId).noneMatch(cid -> cid.equals(card.getCardId()));
	}

	/**
	 * Determines whether or not a player can summon more minions.
	 *
	 * @param player The player whose {@link Zones#BATTLEFIELD} zone should be inspected for minions.
	 * @return {@code true} if the player can summon more minions.
	 */
	public boolean canSummonMoreMinions(Player player) {
		return player.getMinions().size() < MAX_MINIONS;
	}

	/**
	 * Casts one of the two options of a "Choose One" spell and handles all its sophisticated rules.
	 * <p>
	 * Choose One is an ability which allows a player to choose one of multiple possible effects when the card is played
	 * from the hand. Cards with this ability are limited to the druid class.
	 * <p>
	 * Choose One effects are similar to Discover effects, and certain other cards such as Tracking, which also allow you
	 * to choose between multiple options.
	 *
	 * @param playerId        The player casting the choose one spell.
	 * @param spellDesc       The {@link SpellDesc} of the chosen card, not the parent card that contains the choices.
	 * @param sourceReference The source of the spell, typically the original {@link Card}.
	 * @param targetReference The target selected for this choice.
	 * @param cardId          The card that was chosen.
	 * @param sourceAction
	 */
	@Suspendable
	public void castChooseOneSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference, String cardId, GameAction sourceAction) {
		Player player = context.getPlayer(playerId);
		Entity source = null;

		if (sourceReference != null) {
			source = context.resolveSingleTarget(sourceReference);
		}

		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		Card sourceCard = null;
		Card chosenCard = context.getCardById(cardId);
		chosenCard.setOwner(playerId);
		chosenCard.setId(generateId());
		chosenCard.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;

		if (sourceCard != null) {
			if (sourceCard.hasAttribute(Attribute.STARTED_IN_DECK)) {
				chosenCard.setAttribute(Attribute.STARTED_IN_DECK);
			}

			chosenCard.setAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK, context.getTurn());
			chosenCard.getAttributes().put(Attribute.CHOICE_SOURCE, sourceCard.getReference());
			sourceCard.getAttributes().put(Attribute.CHOICE, Arrays.asList(sourceCard.getChooseOneCardIds()).indexOf(cardId));
		}

		if (!spellDesc.hasPredefinedTarget() && targets != null && targets.size() == 1) {
			if (chosenCard.getTargetSelection() != TargetSelection.NONE) {
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				Entity override = targetAcquisition(player, chosenCard, sourceAction);
				if (override != null) {
					targets = Collections.singletonList(override);
					spellDesc = spellDesc.removeArg(SpellArg.FILTER);
				}
			}
		}

		Spell spell = spellDesc.create();
		spell.cast(context, player, spellDesc, source, targets);

		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

		chosenCard.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		endOfSequence();
		handleAfterSpellCasted(playerId, targets, sourceCard);
	}

	/**
	 * Casts a spell.
	 * <p>
	 * This method uses the {@link SpellDesc} (a {@link Map} of {@link SpellArg}, {@link Object}) to figure out what the
	 * spell should do. The {@link SpellDesc#create()} method creates an instance of the {@link Spell} class returned by
	 * {@code spellDesc.getSpellClass()}, then calls its {@link Spell#cast(GameContext, Player, SpellDesc, Entity, List)}
	 * method to actually execute the code of the spell.
	 * <p>
	 * For example, imagine a spell, "Deal 2 damage to all Fae." This would have a {@link SpellDesc} (1) whose {@link
	 * SpellArg#CLASS} would be {@link DamageSpell}, (2) whose {@link SpellArg#FILTER} would be an instance of {@link
	 * EntityFilter} with {@link EntityFilterArg#RACE} as {@link Race#FAE}, (3) whose {@link SpellArg#VALUE} would be
	 * {@code 2} to deal 2 damage, and whose (4) {@link SpellArg#TARGET} would be {@link EntityReference#ALL_MINIONS}.
	 * <p>
	 * Effects can modify spells or create new ones. {@link SpellDesc} allows the code to modify the "code" of a spell.
	 * <p>
	 * This method is responsible for turning the {@link SpellArg#CLASS} argument into a spell instance. The particular
	 * spell class is then responsible for interpreting the rest of its arguments. This code also handles the player's
	 * chosen target whenever a spell had a target selection.
	 *
	 * @param playerId        The players from whose point of view this spell is cast (typically the owning player).
	 * @param spellDesc       A description of the spell.
	 * @param sourceReference The origin of the spell. This is typically the {@link Minion} if the spell is a battlecry or
	 *                        deathrattle; or, the {@link Card} if this spell is coming from a card.
	 * @param targetReference A reference to the target the user selected, if the spell was supposed to have a target.
	 * @param targetSelection If not {@code null}, the spell must have at least one {@link Entity} satisfying this target
	 *                        selection requirement in order for it to be cast.
	 * @param childSpell      When {@code true}, this spell is part an effect, like one of the {@link SpellArg#SPELLS} of
	 *                        a {@link MetaSpell}, and so it shouldn't trigger the firing of events like {@link
	 *                        SpellCastedTrigger}. When {@code false}, this spell is what a player would interpret as a
	 *                        spell coming from a card (a "spell" in the sense of what is written on cards). Battlecries
	 *                        and deathrattles are, unusually, {@code false} (not) child spells.
	 * @param sourceAction    The {@link GameAction}, usually a {@link }
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) for the code that interprets the {@link
	 * SpellArg#FILTER}, and {@link SpellArg#RANDOM_TARGET} arguments.
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) {@code Spell onCast} for the function that typically
	 * has the spell-specific code. {@code onCast} actually implements the logic of a damage spell and interprets the
	 * {@link SpellArg#VALUE} attribute of the {@link SpellDesc} as damage.
	 * @see MetaSpell for the mechanism that multiple spells as children are chained together to create an effect.
	 * @see ActionLogic#rollout(GameAction, GameContext, Player, Collection) for the code that turns a target selection
	 * into actions the player can take.
	 * @see PlayCardAction#innerExecute(GameContext, int) for the call to this function that a player actually does when
	 * they play a {@link Card} (as opposed to a battlecry or deathrattle).
	 * @see BattlecryAction#execute(GameContext, int) for the call to this function that demonstrates a battlecry effect.
	 * Battlecries are spells in the sense that they are effects, though they're not {@link Card} objects.
	 */
	@Suspendable
	public void castSpell(int playerId, @NotNull SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference,
	                      @NotNull TargetSelection targetSelection, boolean childSpell, @Nullable GameAction sourceAction) {
		if (Strand.currentStrand().isInterrupted()) {
			return;
		}

		programCounter++;
		if (programCounter > MAX_PROGRAM_COUNTER) {
			throw new IllegalStateException("program counter");
		}
		spellDepth++;
		if (spellDepth > MAX_SPELL_DEPTH) {
			throw new UnsupportedOperationException("infinite spell depth");
		}
		if (sourceAction != null && sourceAction.isOverrideChild()) {
			childSpell = true;
		}
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null && !sourceReference.equals(EntityReference.NONE)) {
			source = context.resolveSingleTarget(sourceReference);
		}
		// Check if the source is overridden for this effect.
		if (spellDesc.containsKey(SpellArg.SOURCE)) {
			// Try to resolve it as a single target. This will correctly fail if it resolves to multiple targets.
			Entity originalSource = source;
			source = context.resolveSingleTarget(player, source, (EntityReference) spellDesc.get(SpellArg.SOURCE));
			if (source == null) {
				logger.warn("castSpell {} {}: Casting with a SpellArg.SOURCE changed source to null", context.getGameId(), originalSource);
			}
		}

		// Implement SpellOverrideAura
		Class<? extends Spell> spellClass = spellDesc.getDescClass();
		List<Aura> overrideAuras = context.getTriggerManager().getTriggers().stream()
				.filter(t -> t instanceof SpellOverrideAura)
				.map(t -> (Aura) t)
				.filter(aura -> !aura.isExpired()
						&& aura.isActivated()
						&& aura.getDesc().getRemoveEffect().get(SpellArg.CLASS).equals(spellClass)
						&& aura.getAffectedEntities().contains(playerId))
				.collect(Collectors.toList());

		context.getTriggerManager().getTriggers().stream()
				.filter(t -> t instanceof Enchantment)
				.map(t -> (Enchantment) t)
				.filter(e -> e.getSourceCard() != null
						&& isCardType(e.getSourceCard().getCardType(), CardType.ENCHANTMENT)
						&& e.getOwner() == playerId
						&& e.isActivated()
						&& !e.isExpired())
				.forEach(e -> overrideAuras.addAll(e.getSourceCard().createEnchantments().stream()
						.filter(t -> t instanceof SpellOverrideAura)
						.map(t -> (Aura) t)
						.filter(aura -> aura.getDesc().getRemoveEffect().get(SpellArg.CLASS).equals(spellClass))
						.collect(Collectors.toList())));

		if (!overrideAuras.isEmpty()) {
			spellDesc = spellDesc.clone();
			for (Aura aura : overrideAuras) {
				for (Map.Entry<SpellArg, Object> spellArgObjectEntry : aura.getDesc().getApplyEffect().entrySet()) {
					Object value = spellArgObjectEntry.getValue();

					boolean originalValueProvider = value instanceof OriginalValueProvider;
					if (originalValueProvider) {
						if (spellDesc.containsKey(spellArgObjectEntry.getKey())) {
							// Only override key/value pairs in the override using OriginalValueProvider if the original value exists
							Object originalValue = spellDesc.get(spellArgObjectEntry.getKey());
							ValueProvider newValue = (ValueProvider) value;
							ValueProviderDesc newDesc = newValue.getDesc().clone();
							newDesc.put(ValueProviderArg.VALUE, originalValue);
							newValue.setDesc(newDesc);
							value = newValue;
						} else {
							// If the key doesn't exist in the original SpellDesc that we're trying to retrieve with
							// OriginalValueProvider, don't put the new key/value pair into the new SpellDesc
							continue;
						}
					}
					spellDesc.put(spellArgObjectEntry.getKey(), value);
				}
			}
		}

		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		var targetResolution = resolveTarget(player, source, spellTarget, spellDesc, sourceAction);
		if (targetResolution.isOverridden()) {
			spellDesc = spellDesc.removeArg(SpellArg.FILTER);
		}
		var targets = targetResolution.getTargets();

		Spell spell = spellDesc.create();
		spell.cast(context, player, spellDesc, source, targets);

		Card sourceCard = source != null ? source.getSourceCard() : null;

		// This implements Lynessa Sunsorrow
		if (sourceCard != null
				&& sourceCard.getCardType() == CardType.SPELL
				&& targetSelection != TargetSelection.NONE
				&& targets != null
				&& targets.size() == 1
				&& targets.get(0).getOwner() == playerId
				&& targets.get(0).getEntityType().equals(EntityType.MINION)
				&& sourceAction != null
				&& Objects.equals(sourceAction.getSourceReference(), source.getReference())
				&& !childSpell) {
			sourceCard.setAttribute(Attribute.CASTED_ON_FRIENDLY_MINION);
		}

		if (targetSelection != TargetSelection.NONE
				&& targets != null
				&& targets.size() == 1
				&& sourceCard != null
				&& targets.get(0).getEntityType().equals(EntityType.MINION)
				&& sourceAction != null
				&& Objects.equals(sourceAction.getSourceReference(), source.getReference())
				&& !childSpell) {
			boolean willTargetAdjacent = false;
			for (SpellTargetsAdjacentAura aura : SpellUtils.getAuras(context, playerId, SpellTargetsAdjacentAura.class)) {
				aura.onGameEvent(new WillEndSequenceEvent(context));
				if (aura.getAffectedEntities().contains(sourceCard.getId())) {
					willTargetAdjacent = true;
				}
			}
			if (willTargetAdjacent) {
				SpellDesc adjacentSpellDesc = AdjacentEffectSpell.create(targetReference, NullSpell.create(), spellDesc);
				castSpell(playerId, adjacentSpellDesc, sourceReference, targetReference, TargetSelection.NONE, true, null);
			}
		}


		if (sourceAction != null
				&& sourceCard != null
				&& isCardType(sourceCard.getCardType(), CardType.SPELL)
				&& Objects.equals(sourceAction.getSourceReference(), source.getReference())
				&& !childSpell) {
			context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
			endOfSequence();

			handleAfterSpellCasted(playerId, targets, sourceCard);
		}

		// Cast second time if source belongs to aura and not child spell (Implements Lady Uki)
		List<SpellEffectsCastTwiceAura> castTwiceAuras = SpellUtils.getAuras(context, SpellEffectsCastTwiceAura.class, source);
		if (!castTwiceAuras.isEmpty()
				&& !childSpell) {
			castSpell(playerId, spellDesc, sourceReference, targetReference, targetSelection, true, sourceAction);
		}
		spellDepth--;
	}

	@Suspendable
	private void handleAfterSpellCasted(int playerId, List<Entity> targets, Card sourceCard) {
		// Spells should only be marked as having been casted if they were played from the hand or deck
		if (sourceCard.hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK)) {
			if (targets == null || targets.size() != 1) {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
			} else {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
			}
		}
	}

	/**
	 * Stores the result of a targeting resolution.
	 */
	public class TargetResolution {
		private final boolean overridden;
		private final List<Entity> targets;

		public TargetResolution(boolean overridden, List<Entity> targets) {
			this.overridden = overridden;
			this.targets = targets;
		}

		/**
		 * Was the target overriden by an effect?
		 *
		 * @return
		 */
		public boolean isOverridden() {
			return overridden;
		}

		/**
		 * The actual targets.
		 *
		 * @return
		 */
		public List<Entity> getTargets() {
			return targets;
		}
	}

	/**
	 * Resolves a targeting {@link EntityReference}, triggering a target acquisition event on the board.
	 *
	 * @param player
	 * @param source
	 * @param spellTarget
	 * @param spellDesc
	 * @param sourceAction
	 * @return
	 */
	@Suspendable
	public TargetResolution resolveTarget(Player player, Entity source, EntityReference spellTarget, SpellDesc spellDesc, GameAction sourceAction) {
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);

		var overridden = false;
		Entity override = targetAcquisition(player, source, sourceAction);
		if (override != null && !spellDesc.hasPredefinedTarget()) {
			targets = new ArrayList<>();
			targets.add(override);
			overridden = true;
		}

		// Spell effects should never be cast on the old reference to a transform.
		if (targets != null && !targets.isEmpty()) {
			for (int i = 0; i < targets.size(); i++) {
				if (targets.get(i).getAttributes().containsKey(Attribute.TRANSFORM_REFERENCE)) {
					targets.set(i, targets.get(i).transformResolved(context));
				}
			}
		}

		// Implements incorruptibility (removes the target if it is affected by an incorruptibility aura)
		if (targets != null) {
			var incorruptibilityAuras = SpellUtils.getAuras(context, player.getId(), IncorruptibilityAura.class);
			for (var aura : incorruptibilityAuras) {
				targets.removeIf(e -> aura.getAffectedEntities().contains(e.getId()));
			}
		}

		return new TargetResolution(overridden, targets);
	}

	/**
	 * Processes an action for its appropriate target overriding effects, if any, and triggers target acquisition.
	 *
	 * @param player       The player.
	 * @param source       The source entity.
	 * @param sourceAction When {@code null}, no overrides can occur.
	 * @return {@code null} if this is not an overridable form of target acquisition; or, the intended target if the
	 * target was not overridden, or the new target.
	 */
	@Suspendable
	protected @Nullable
	Entity targetAcquisition(@NotNull Player player, @Nullable Entity source, @Nullable GameAction sourceAction) {
		if (sourceAction == null) {
			return null;
		}

		if (sourceAction.getTargetRequirement() == TargetSelection.NONE) {
			return null;
		}

		Entity target = context.resolveSingleTarget(sourceAction.getTargetReference());
		TargetAcquisitionEvent gameEvent = new TargetAcquisitionEvent(context, sourceAction, source, target);
		context.fireGameEvent(gameEvent);
		Entity targetOverride = context.getTargetOverride(player, source);
		if (targetOverride != null) {
			// Consume the target override
			context.setTargetOverride(null);
		}
		return targetOverride == null ? target : targetOverride;
	}

	/**
	 * Changes the player's hero.
	 * <p>
	 * A hero consists of the actual {@link Hero} actor, the hero's hero power {@link Card} specified on its {@link
	 * net.demilich.metastone.game.cards.desc.CardDesc#heroPower} field, and possibly a {@link Weapon} equipped by an
	 * {@link EquipWeaponSpell} specified in its battlecry. Heroes that do not resolve battlecries (i.e., heroes that are
	 * not played from the hand) generally do not equip weapons, while heroes coming into play in any way generally change
	 * the hero powers.
	 * <p>
	 * Many attributes of the current hero are retained, like its {@link Attribute#NUMBER_OF_ATTACKS}. Enchantments are
	 * removed. When the hero card specifies a new {@link Attribute#MAX_HP} and {@link Attribute#HP}, the hitpoints of the
	 * new hero are changed; otherwise, the old hitpoints are retained. An {@link Attribute#ARMOR} amount is added to the
	 * previous hero's armor, not replaced.
	 * <p>
	 * Hero powers have their {@link net.demilich.metastone.game.cards.desc.CardDesc#passiveTrigger} processed, because
	 * the hero power behaves like an extension of the hand, not a zone in play. Otherwise, the {@link
	 * net.demilich.metastone.game.cards.desc.CardDesc#trigger} is activated when the hero comes into play.
	 * <p>
	 * The previous hero is not moved to the graveyard, because it was not destroyed. It is moved to {@link
	 * Zones#REMOVED_FROM_PLAY}.
	 * <p>
	 * Some "boss" heroes, like Ragnaros, should not change the hero class of the player. They use the hero class {@link
	 * HeroClass#INHERIT}, which will set the player and hero's class to the previous hero's class. Note that in
	 * Spellsource, changing your hero to a different class will change the results of your discovers.
	 * <p>
	 * Implements Lord Jaraxxus and hero cards. Resolves battlecries.
	 *
	 * @param player The player whose hero to change.
	 * @param hero   The new hero the player will have.
	 */
	@Suspendable
	public void changeHero(Player player, Hero hero) {
		changeHero(player, hero, true);
	}


	/**
	 * Changes the player's hero.
	 *
	 * @param player           The player whose hero to change.
	 * @param hero             The new hero the player  will  have
	 * @param resolveBattlecry Whether or not the battlecry specified on the hero should be resolved.
	 * @see #changeHero(Player, Hero) for more information.
	 */
	@Suspendable
	public void changeHero(final Player player, final Hero hero, boolean resolveBattlecry) {
		final Hero previousHero = player.getHero();
		hero.setId(generateId());
		hero.setOwner(player.getId());
		if (hero.getHeroClass() == null || hero.getHeroClass().equals(HeroClass.ANY)) {
			hero.setHeroClass(previousHero.getHeroClass());
		}

		// Get the current hitpoints and armor
		int previousHp = previousHero.getHp();
		// Get the additional armor from the incoming hero
		int previousArmor = previousHero.getArmor();

		if (hero.getHeroClass().equals(HeroClass.INHERIT)) {
			hero.setHeroClass(previousHero.getHeroClass());
		}

		Card heroPower = CardCatalogue.getCardById(hero.getSourceCard().getDesc().getHeroPower());
		// Set hero power ID before events trigger to prevent issues
		heroPower.setId(generateId());
		heroPower.setOwner(hero.getOwner());

		// Set the new hero's number of attacks to the old hero's.
		hero.getAttributes().put(Attribute.NUMBER_OF_ATTACKS, previousHero.getAttributes().get(Attribute.NUMBER_OF_ATTACKS));

		// Maintain the old hero's temporary stats
		Stream.of(Attribute.NUMBER_OF_ATTACKS,
				Attribute.TOTAL_DAMAGE_DEALT,
				Attribute.LAST_HEAL,
				Attribute.LAST_HIT,
				Attribute.RESERVED_BOOLEAN_1,
				Attribute.RESERVED_BOOLEAN_2,
				Attribute.RESERVED_BOOLEAN_3,
				Attribute.RESERVED_BOOLEAN_4,
				Attribute.RESERVED_INTEGER_1,
				Attribute.RESERVED_INTEGER_2,
				Attribute.RESERVED_INTEGER_3,
				Attribute.RESERVED_INTEGER_4).forEach(attr -> {
			if (previousHero.hasAttribute(attr)) {
				hero.getAttributes().put(attr, previousHero.getAttributes().get(attr));
			}
		});

		// Remove the old hero from play
		removeEnchantments(previousHero);
		// This removes the hero power enchantments too
		removeCard(previousHero.getHeroPower());
		previousHero.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		hero.moveOrAddTo(context, Zones.HERO);
		player.getHeroPowerZone().add(heroPower);
		if (Objects.equals(hero.getHeroPower().getHeroClass(), HeroClass.INHERIT)) {
			hero.getHeroPower().setHeroClass(previousHero.getHeroClass());
		}
		hero.modifyArmor(previousArmor);
		final int armorChange = hero.getArmor() - previousArmor;
		if (armorChange != 0) {
			context.fireGameEvent(new ArmorChangedEvent(context, player.getHero(), armorChange));
		}

		// Only override the HP if both max and new HP is defined.
		if (!(hero.hasAttribute(Attribute.MAX_HP) && hero.hasAttribute(Attribute.HP))) {
			hero.setHp(previousHp);
		}

		if (resolveBattlecry
				&& !hero.getBattlecries().isEmpty()) {
			resolveBattlecries(player.getId(), hero);
			endOfSequence();
		}

		processBattlefieldEnchantments(player, hero);
		processGameTriggers(player, hero);
		processGameTriggers(player, hero.getHeroPower());
		processPassiveTriggers(player, hero.getHeroPower());
		processPassiveAuras(player, hero.getHeroPower());
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Removes entities for whom {@link Entity#isDestroyed()} is true, moving them to the {@link Zones#GRAVEYARD} and
	 * triggering their deathrattles with {@link #resolveAftermaths(Player, Actor)}.
	 * <p>
	 * Since deathrattles may destroy other entities (e.g., a {@link DamageSpell} deathrattle), this function calls itself
	 * recursively until there are no more dead entities on the board.
	 */
	@Suspendable
	public void endOfSequence() {
		endOfSequence(0, new ArrayList<>());
	}

	/**
	 * Checks all player minions and weapons for destroyed actors and proceeds with the removal in correct order.
	 *
	 * @param sequenceDepth         The number of times this method has been called to avoid infinite death checking.
	 * @param cumulativeDestroyList Keeps track of the entities that have appeared on the destroy list (for debugging
	 *                              purposes).
	 */
	@Suspendable
	private void endOfSequence(int sequenceDepth, List<Actor> cumulativeDestroyList) {
		if (sequenceDepth == 0) {
			context.fireGameEvent(new WillEndSequenceEvent(context));
		}

		// Check if triggers were added that do not have hosts
		for (var trigger : context.getTriggerManager().getTriggers()) {
			if (trigger instanceof HasCard && Objects.isNull(((HasCard) trigger).getSourceCard())) {
				logger.error("endOfSequence: Trigger {} is missing source card", trigger, new RuntimeException());
			}

			if (Objects.equals(trigger.getHostReference(), EntityReference.NONE) || trigger.getHostReference() == null) {
				logger.error("endOfSequence: Trigger has host reference of NONE on an enchantment from card {}", trigger, new RuntimeException());
			}

			if (trigger instanceof Enchantment && ((Enchantment) trigger).getId() == UNASSIGNED) {
				logger.error("endOfSequence: Trigger {} is missing an ID", trigger);
			}
		}

		// Only perform at most END_OF_SEQUENCE_MAX_DEPTH times. This limits the number of deathrattles to evaluate.
		if (sequenceDepth > END_OF_SEQUENCE_MAX_DEPTH) {
			throw new RuntimeException("Infinite death checking loop");
		}

		List<Actor> destroyList = getDestroyedCharacters();

		if (destroyList.isEmpty()) {
			// This is the end of the sequence, call board changed event at most once even if no minions died.
			if (sequenceDepth == 0) {
				context.fireGameEvent(new BoardChangedEvent(context));
			}
			context.getEnvironment().put(Environment.DESTROYED_THIS_SEQUENCE_COUNT, 0);
			// Reset all enchantment sequence counters
			context.getTriggerManager().getTriggers().stream()
					.filter(Enchantment.class::isInstance)
					.map(Enchantment.class::cast)
					.forEach(Enchantment::endOfSequence);
			context.fireGameEvent(new DidEndSequenceEvent(context));
			// Check if any characters have been marked as destroyed by ending the sequence. If one has, we're in big trouble.
			if (!getDestroyedCharacters().isEmpty()) {
				// Gotta end the sequence again!
				cumulativeDestroyList.addAll(destroyList);
				endOfSequence(sequenceDepth + 1, cumulativeDestroyList);
			}
			return;
		}

		// sort the destroyed actors by their id. This implies that actors with a lower id entered the game earlier than those with higher ids!
		destroyList.sort(Comparator.comparingInt(Entity::getId));
		// this method performs the actual removal
		destroy(destroyList.toArray(new Actor[0]));
		if (context.updateAndGetGameOver()) {
			// The game ended. By now, all the triggers that were put into play may have been expired
			return;
		}

		cumulativeDestroyList.addAll(destroyList);
		// deathrattles have been resolved, which may lead to other actors being destroyed now, so we need to check again
		endOfSequence(sequenceDepth + 1, cumulativeDestroyList);
	}

	@NotNull
	private List<Actor> getDestroyedCharacters() {
		List<Actor> destroyList = new ArrayList<>();
		for (Player player : context.getPlayers()) {

			if ((player.getHero().isDestroyed() || player.hasAttribute(Attribute.DESTROYED)) &&
					player.getHero().getZone() != Zones.GRAVEYARD) {
				destroyList.add(player.getHero());
			}

			for (Minion minion : player.getMinions()) {
				if (minion.isDestroyed()) {
					destroyList.add(minion);
				}
			}

			for (Entity entity : player.getSetAsideZone()) {
				if (!(entity instanceof Actor)) {
					continue;
				}
				if (entity.isDestroyed()) {
					destroyList.add((Actor) entity);
				}
			}

			if (player.getHero().getWeapon() != null && player.getHero().getWeapon().isDestroyed()) {
				destroyList.add(player.getHero().getWeapon());
			}
		}
		return destroyList;
	}

	/**
	 * Clones the game logic. The only state in this instance is its debug history and the current ID of the ID Factory.
	 *
	 * @return A clone of this logic.
	 * @see IdFactoryImpl for the internal state of an {@link IdFactoryImpl}.
	 */
	@Override
	public GameLogic clone() {
		GameLogic clone = new GameLogic(idFactory.clone(), getSeed());
		clone.random = random.clone();
		clone.context = context;
		return clone;
	}

	/**
	 * Deals damage to a target.
	 *
	 * @param player     The originating player of the damage.
	 * @param target     The target to damage.
	 * @param baseDamage The base amount of damage to deal.
	 * @param source     The source of the damage.
	 * @return The amount of damage ultimately dealt, considering all on board effects.
	 * @see #damage(Player, Actor, int, Entity, boolean, DamageTypeEnum) for a complete description of the damage effect.
	 */
	@Suspendable
	public int damage(Player player, Actor target, int baseDamage, Entity source) {
		return damage(player, target, baseDamage, source, false);
	}

	/**
	 * Deals damage to a target.
	 *
	 * @param player            The originating player of the damage.
	 * @param target            The target to damage.
	 * @param baseDamage        The base amount of damage to deal.
	 * @param source            The source of the damage.
	 * @param ignoreSpellDamage When {@code true}, spell damage bonuses are not added to the damage dealt.
	 * @return The amount of damage ultimately dealt, considering all on board effects.
	 * @see #damage(Player, Actor, int, Entity, boolean, DamageTypeEnum) for a complete description of the damage effect.
	 */
	@Suspendable
	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage) {
		// sanity check to prevent StackOverFlowError with Mistress of Pain +
		// Auchenai Soulpriest
		return damage(player, target, baseDamage, source, ignoreSpellDamage, DamageTypeEnum.MAGICAL);
	}

	/**
	 * Deals damage to a target.
	 * <p>
	 * Damage is measured by a number which is deducted from the armor first, followed by hitpoints, of an {@link Actor}.
	 * If the {@link Actor#getHp()} is reduced to zero (or below), it will be killed. Note that other types of harm that
	 * can be inflicted to characters (such as a {@link DestroySpell}, freeze effects and the card Equality) are not
	 * considered damage for game purposes and, although most damage is dealt through {@link #fight(Player, Actor, Actor,
	 * PhysicalAttackAction)}, dealing damage is not considered an "fight" for game purposes.
	 * <p>
	 * Damage can activate a number of triggered effects, both from receiving it (such as Acolyte of Pain's {@link
	 * DamageReceivedTrigger}) and from dealing it (such as Lightning Automaton's {@link DamageCausedTrigger}). However,
	 * damage negated by an {@link Actor} with {@link Attribute#DIVINE_SHIELD} or {@link Attribute#IMMUNE} effects is not
	 * considered to have been successfully dealt, and thus will not trigger any on-damage triggered effects.
	 * <p>
	 * A {@link Hero} with nonzero {@link Hero#getArmor()} will have any damage deducted from their armor before their
	 * hitpoints: any damage beyond the {@link Actor}'s current Armor will be deducted from their hitpoints. Armor will
	 * not prevent damage from being dealt: damage dealt only to Armor still counts as damage for the purpose of effects
	 * such as Lightning Automaton and Floating Watcher.
	 *
	 * @param player            The originating player of the damage.
	 * @param target            The target to damage.
	 * @param baseDamage        The base amount of damage to deal.
	 * @param source            The source of the damage.
	 * @param ignoreSpellDamage When {@code true}, spell damage bonuses are not added to the damage dealt.
	 * @param damageType        The type of damage dealt ot the target.
	 * @return The amount of damage that was actually dealt
	 */
	@Suspendable
	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage, DamageTypeEnum damageType) {
		return damage(player, target, baseDamage, source, ignoreSpellDamage, false, damageType);
	}

	/**
	 * Deals damage to a target.
	 * <p>
	 * Damage is measured by a number which is deducted from the armor first, followed by hitpoints, of an {@link Actor}.
	 * If the {@link Actor#getHp()} is reduced to zero (or below), it will be killed. Note that other types of harm that
	 * can be inflicted to characters (such as a {@link DestroySpell}, freeze effects and the card Equality) are not
	 * considered damage for game purposes and, although most damage is dealt through {@link #fight(Player, Actor, Actor,
	 * PhysicalAttackAction)}, dealing damage is not considered an "fight" for game purposes.
	 * <p>
	 * Damage can activate a number of triggered effects, both from receiving it (such as Acolyte of Pain's {@link
	 * DamageReceivedTrigger}) and from dealing it (such as Lightning Automaton's {@link DamageCausedTrigger}). However,
	 * damage negated by an {@link Actor} with {@link Attribute#DIVINE_SHIELD} or {@link Attribute#IMMUNE} effects is not
	 * considered to have been successfully dealt, and thus will not trigger any on-damage triggered effects.
	 * <p>
	 * A {@link Hero} with nonzero {@link Hero#getArmor()} will have any damage deducted from their armor before their
	 * hitpoints: any damage beyond the {@link Actor}'s current Armor will be deducted from their hitpoints. Armor will
	 * not prevent damage from being dealt: damage dealt only to Armor still counts as damage for the purpose of effects
	 * such as Lightning Automaton and Floating Watcher.
	 *
	 * @param player            The originating player of the damage.
	 * @param target            The target to damage.
	 * @param baseDamage        The base amount of damage to deal.
	 * @param source            The source of the damage.
	 * @param ignoreSpellDamage When {@code true}, spell damage bonuses are not added to the damage dealt.
	 * @param ignoreLifesteal
	 * @param damageType        The type of damage dealt ot the target.
	 * @return The amount of damage that was actually dealt
	 */
	@Suspendable
	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage, boolean ignoreLifesteal, DamageTypeEnum damageType) {
		int damageDealt = applyDamageToActor(target, baseDamage, player, source, ignoreSpellDamage, damageType);
		resolveDamageEvent(player, target, source, damageDealt, ignoreLifesteal, damageType);
		if (source.getEntityType() == EntityType.CARD) {
			Card card = (Card) source;
			if (card.isHeroPower()) {
				player.getStatistics().heroPowerDamage(damageDealt);
			}
		}
		return damageDealt;
	}

	@Suspendable
	protected void resolveDamageEvent(Player player, Actor target, Entity source, int damageDealt, DamageTypeEnum damageType) {
		resolveDamageEvent(player, target, source, damageDealt, false, damageType);
	}

	@Suspendable
	protected void resolveDamageEvent(Player player, Actor target, Entity source, int damageDealt, boolean ignoreLifesteal, DamageTypeEnum damageType) {
		// Check if the target is already destroyed. This allows kills to be tracked properly (one source per kill).
		boolean startedDestroyed = target.isDestroyed();
		if (damageDealt > 0) {
			// Keyword effects for lifesteal and poisonous will come BEFORE all other events
			// Poisonous resolves in a queue with higher priority, and it stops Grim Patron spawning regardless of
			// Dominant Player. However, Acidmaw can never stop Grim Patron spawning.
			if (target.getEntityType() == EntityType.MINION
					&& ((source.hasAttribute(Attribute.POISONOUS) || source.hasAttribute(Attribute.AURA_POISONOUS))
					|| (source instanceof Hero
					&& ((Hero) source).getWeapon() != null
					&& (((Hero) source).getWeapon().hasAttribute(Attribute.POISONOUS)
					|| ((Hero) source).getWeapon().hasAttribute(Attribute.AURA_POISONOUS))))) {
				markAsDestroyed(target);
			}

			// Implement lifesteal
			if (!ignoreLifesteal
					&& (source.hasAttribute(Attribute.LIFESTEAL) || source.hasAttribute(Attribute.AURA_LIFESTEAL))
					// Lifesteal now does not apply if the source shares an owner with the target and the target is a hero.
					&& !(source.getOwner() == target.getOwner() && target.getEntityType() == EntityType.HERO && isCardType(source.getSourceCard().getCardType(), CardType.SPELL))
					|| (source instanceof Hero
					&& ((Hero) source).getWeapon() != null
					&& (((Hero) source).getWeapon().hasAttribute(Attribute.LIFESTEAL)
					|| ((Hero) source).getWeapon().hasAttribute(Attribute.AURA_LIFESTEAL)))
					|| (source instanceof Secret
					&& (source.getSourceCard().hasAttribute(Attribute.LIFESTEAL)) || source.getSourceCard().hasAttribute(Attribute.AURA_LIFESTEAL))) {
				Player sourceOwner = context.getPlayer(source.getOwner());
				// Implements Flesshapper
				if (!SpellUtils.getAuras(context, LifedrainGrantsArmorInsteadAura.class, source).isEmpty()) {
					gainArmor(player, damageDealt);
				} else {
					heal(sourceOwner, sourceOwner.getHero(), damageDealt, source);
					// Implements Lan the Forsaken
					var bonusAuras = SpellUtils.getAuras(context, LifedrainHealsAdditionalAura.class, source);
					if (!bonusAuras.isEmpty()) {
						for (var aura : bonusAuras) {
							var host = context.resolveSingleTarget(aura.getHostReference());
							var secondaries = context.resolveTarget(player, host, aura.getSecondaryTarget());
							secondaries.remove(source);
							for (var secondary : secondaries) {
								if (!Entity.hasEntityType(secondary.getEntityType(), EntityType.ACTOR)) {
									continue;
								}
								heal(sourceOwner, (Actor) secondary, damageDealt, source);
							}
						}
					}
				}
			}

			if (damageDealt > 0 &&
					(source.hasAttribute(Attribute.WITHER))
					// Lifesteal now does not apply if the source shares an owner with the target and the target is a hero.
					&& !(source.getOwner() == target.getOwner() && target.getEntityType() == EntityType.HERO && isCardType(source.getSourceCard().getCardType(), CardType.SPELL))
					|| (source instanceof Hero && ((Hero) source).getWeapon() != null
					&& (((Hero) source).getWeapon().hasAttribute(Attribute.WITHER)))
					|| (source instanceof Secret
					&& (source.getSourceCard().hasAttribute(Attribute.WITHER)))) {

				int amount;
				if (source.hasAttribute(Attribute.WITHER)) {
					amount = source.getAttributeValue(Attribute.WITHER);
				} else if ((source instanceof Hero && ((Hero) source).getWeapon() != null
						&& (((Hero) source).getWeapon().hasAttribute(Attribute.WITHER)))) {
					amount = ((Hero) source).getWeapon().getAttributeValue(Attribute.WITHER);
				} else {
					amount = source.getSourceCard().getAttributeValue(Attribute.WITHER);
				}

				target.modifyAttribute(Attribute.WITHERED, amount);
			}

			// Implement Doomlord
			target.modifyAttribute(Attribute.DAMAGE_THIS_TURN, damageDealt);
			if (target.getEntityType() == EntityType.HERO) {
				Player targetOwner = context.getPlayer(target.getOwner());
				targetOwner.modifyAttribute(Attribute.DAMAGE_THIS_TURN, damageDealt);
			}

			player.getStatistics().damageDealt(damageDealt);

			// Implement Yakha Reiri
			var sourceOwner = context.getPlayer(source.getOwner());
			if (target.getEntityType() == EntityType.MINION) {
				if (source.getEntityType() != EntityType.PLAYER) {
					source.modifyAttribute(Attribute.TOTAL_MINION_DAMAGE_DEALT_THIS_GAME, damageDealt);
				}
				sourceOwner.modifyAttribute(Attribute.TOTAL_MINION_DAMAGE_DEALT_THIS_GAME, damageDealt);
			}

			if (source.getEntityType() != EntityType.PLAYER) {
				source.modifyAttribute(Attribute.TOTAL_DAMAGE_DEALT_THIS_GAME, damageDealt);
			}

			if (source.getEntityType() == EntityType.WEAPON) {
				sourceOwner.getHero().modifyAttribute(Attribute.TOTAL_DAMAGE_DEALT_THIS_GAME, damageDealt);
			}
			sourceOwner.modifyAttribute(Attribute.TOTAL_DAMAGE_DEALT_THIS_GAME, damageDealt);

			DamageEvent damageEvent = new DamageEvent(context, target, source, damageDealt, damageType);
			context.fireGameEvent(damageEvent);

			// Check now if a kill is registered so the source can be properly credited
			if (target.isDestroyed() && !startedDestroyed) {
				source.modifyAttribute(Attribute.TOTAL_KILLS, 1);
			}
		}
	}

	@Suspendable
	protected int applyDamageToActor(Actor target, final int baseDamage, Player player, Entity source, boolean ignoreSpellDamage, DamageTypeEnum damageType) {
		if (target.getHp() < -100) {
			return 0;
		}
		int damage = baseDamage;
		Card sourceCard = source.getSourceCard();
		if (!ignoreSpellDamage && sourceCard != null) {
			if (isCardType(sourceCard.getCardType(), CardType.SPELL)) {
				damage = applySpellpower(player, source, baseDamage);
			} else if (isCardType(sourceCard.getCardType(), CardType.HERO_POWER)) {
				damage = applyHeroPowerDamage(player, damage);
			}
			if (sourceCard.isSpell()) {
				damage = applyAmplify(player, damage, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
			}
			if (sourceCard.isHeroPower()) {
				damage = applyAmplify(player, damage, Attribute.HERO_POWER_DAMAGE_AMPLIFY_MULTIPLIER);
			}
		}

		if (target.hasAttribute(Attribute.TAKE_DOUBLE_DAMAGE) || target.hasAttribute(Attribute.AURA_TAKE_DOUBLE_DAMAGE)) {
			damage *= 2;
		}

		// Dealing zero damage at this point correctly sets the last recorded hit as zero, but does not trigger damage
		// effects since no damage is going to be dealt.
		if (damage == 0) {
			target.setAttribute(Attribute.LAST_HIT, 0);
			return 0;
		}

		context.getDamageStack().push(damage);
		context.fireGameEvent(new PreDamageEvent(context, target, source, damage, damageType));
		damage = context.getDamageStack().pop();
		if (damage > 0) {
			removeAttribute(source, null, Attribute.STEALTH);
		}

		int damageDealt = 0;
		switch (target.getEntityType()) {
			case MINION:
				damageDealt = damageMinion(player, damage, source, target);
				break;
			case HERO:
				damageDealt = damageHero((Hero) target, damage, source, damageType);
				break;
			default:
				break;
		}

		// Dealing zero damage at this point still counts as a hit.
		target.setAttribute(Attribute.LAST_HIT, damageDealt);
		target.getAttributes().put(Attribute.TOTAL_DAMAGE_RECEIVED, (int) target.getAttributes().getOrDefault(Attribute.TOTAL_DAMAGE_RECEIVED, 0) + damageDealt);
		return damageDealt;
	}

	@Suspendable
	private int damageHero(Hero hero, final int damage, Entity source, DamageTypeEnum damageType) {
		if (hero.hasAttribute(Attribute.IMMUNE) || hero.hasAttribute(Attribute.AURA_IMMUNE)) {
			return 0;
		}

		// Hero now supports having divine shield
		if (hero.hasAttribute(Attribute.DIVINE_SHIELD)) {
			removeAttribute(hero, source, Attribute.DIVINE_SHIELD);
			return 0;
		}

		int effectiveHp;
		int newHp;
		if (damageType == DamageTypeEnum.IGNORES_ARMOR) {
			effectiveHp = hero.getHp();
			newHp = effectiveHp - damage;
		} else {
			effectiveHp = hero.getHp() + hero.getArmor();
			int armorChange = hero.modifyArmor(-damage);
			if (armorChange != 0) {
				context.fireGameEvent(new ArmorChangedEvent(context, hero, armorChange));
				context.getPlayer(hero.getOwner()).getStatistics().loseArmor(-armorChange);
			}
			newHp = Math.min(hero.getHp(), effectiveHp - damage);
		}

		hero.setHp(newHp);
		return damage;
	}

	@Suspendable
	private int damageMinion(Player player, int damage, Entity source, Actor minion) {
		if (damage == 0) {
			return damage;
		}

		if (hitShields(player, damage, source, minion)) {
			return 0;
		}

		if (minion.hasAttribute(Attribute.IMMUNE) || minion.hasAttribute(Attribute.AURA_IMMUNE)) {
			return 0;
		}
		if (damage >= minion.getHp() && minion.hasAttribute(Attribute.CANNOT_REDUCE_HP_BELOW_1)) {
			damage = minion.getHp() - 1;
		}

		minion.setHp(minion.getHp() - damage);
		handleHpChange(minion);
		return damage;
	}

	/**
	 * Processes a hit against possible shields on the {@code target} {@link Actor}.
	 * <p>
	 * {@link Attribute#DIVINE_SHIELD} and {@link Attribute#DEFLECT} are the two kinds of shields currently supported.
	 * <p>
	 * This will have side effects for {@link Attribute#DEFLECT}.
	 *
	 * @param player the caster of this effect
	 * @param damage the damage that would be otherwise dealt
	 * @param source the source of the damage
	 * @param target the target
	 * @return {@code true} if a shield was hit, otherwise {@code false}.
	 */
	@Suspendable
	public boolean hitShields(Player player, int damage, Entity source, Actor target) {
		if (target.hasAttribute(Attribute.DIVINE_SHIELD)) {
			removeAttribute(target, source, Attribute.DIVINE_SHIELD);
			return true;
		}
		if (target.hasAttribute(Attribute.DEFLECT)
				&& target.getHp() <= damage) {
			removeAttribute(target, source, Attribute.DEFLECT);
			damage(player, context.getPlayer(target.getOwner()).getHero(), damage, source, true, DamageTypeEnum.DEFLECT);
			return true;
		}
		return false;
	}

	/**
	 * Destroys the given targets, triggering their aftermaths if necessary.
	 *
	 * @param targets A list of {@link Actor} targets that should be destroyed.
	 * @see #endOfSequence() for the code that actually finds dead entities as a result of effects and eventually destroys
	 * them.
	 */
	@Suspendable
	public void destroy(Actor... targets) {
		// Reverse the targets
		Map<Actor, EntityLocation> previousLocations = new HashMap<>();

		List<Actor> reversed = new ArrayList<>(Arrays.asList(targets));

		reversed.sort((a, b) -> -Integer.compare(a.getEntityLocation().getIndex(), b.getEntityLocation().getIndex()));

		for (Actor target : reversed) {
			if (!target.hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
				removeEnchantments(target, false, false, false);
			}
			previousLocations.put(target, target.getEntityLocation());
			target.moveOrAddTo(context, Zones.GRAVEYARD);
		}

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			EntityLocation actorPreviousLocation = previousLocations.get(target);
			Player owner = context.getPlayer(target.getOwner());
			switch (target.getEntityType()) {
				case HERO:
					applyAttribute(target, Attribute.DESTROYED);
					applyAttribute(context.getPlayer(target.getOwner()), Attribute.DESTROYED);
					break;
				case MINION:
					corpse(target, actorPreviousLocation, false);
					break;
				case WEAPON:
					destroyWeapon((Weapon) target);
					break;
				case ANY:
				default:
					logger.error("Trying to destroy unknown entity type {}", target.getEntityType());
					break;
			}

			resolveAftermaths(owner, target, previousLocations.get(target));
		}
		for (Actor target : targets) {
			removeEnchantments(target, true, false, true);
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Corpses a target, setting it to be destroyed on the appropriate turn, firing a kill event, and clearing the
	 * environment variables associated with the kill event.
	 *
	 * @param target
	 * @param actorPreviousLocation
	 */
	@Suspendable
	public void corpse(Actor target, EntityLocation actorPreviousLocation, boolean removeEnchantments) {
		if (target.getId() == UNASSIGNED) {
			throw new RuntimeException();
		}
		if (removeEnchantments) {
			removeEnchantments(target, false, false, false);
		}
		if (target.getEntityLocation().equals(EntityLocation.UNASSIGNED) && !actorPreviousLocation.equals(EntityLocation.UNASSIGNED)) {
			context.getPlayer(actorPreviousLocation.getPlayer()).getGraveyard().add(target);
		} else if (target.getZone() != Zones.GRAVEYARD) {
			target.moveOrAddTo(context, Zones.GRAVEYARD);
		}
		// Minions removed peacefully do not trigger kill events
		// Deathrattles delegate correctly and do not trigger if the minion was previously in the set aside
		// zone.
		if (actorPreviousLocation.getZone() != Zones.SET_ASIDE_ZONE) {
			context.getEnvironment().put(Environment.KILLED_MINION, target.getReference());
			// The attributes of being destroyed and which turn something died should be correct when the KillEvent is
			// fired.
			applyAttribute(target, Attribute.DESTROYED);
			target.setAttribute(Attribute.DIED_ON_TURN, context.getTurn());
			KillEvent killEvent = new KillEvent(context, target);
			context.fireGameEvent(killEvent);
			context.getEnvironment().remove(Environment.KILLED_MINION);
			// Remove peacefully
		} else if (target.hasAttribute(Attribute.DESTROYED)) {
			target.getAttributes().remove(Attribute.DESTROYED);
		}
	}

	@Suspendable
	private void destroyWeapon(Weapon weapon) {
		// Already in graveyard by this point
		Player owner = context.getPlayer(weapon.getOwner());
		weapon.onUnequip(context, owner);
		removeEnchantments(weapon);
		weapon.setAttribute(Attribute.DIED_ON_TURN, context.getTurn());
		context.fireGameEvent(new WeaponDestroyedEvent(context, weapon));
	}

	/**
	 * Determines who is the first player of a list of player IDs.
	 *
	 * @param playerIds The possible options for first player.
	 * @return The ID of the player that should go first.
	 */
	public int determineBeginner(int... playerIds) {
		return getRandom().nextBoolean() ? playerIds[0] : playerIds[1];
	}

	/**
	 * Discards a card from your hand, either through discard card effects or "overdraw" (forced destruction of cards due
	 * to too many cards in your hand).
	 * <p>
	 * Discarded cards are removed from the game, without activating Deathrattles. Discard effects are most commonly found
	 * on warlock cards. Discard effects are distinguished from overdraw, and Fel Reaver's remove from deck effect, both
	 * of which remove cards directly from the deck without entering the hand; and from Tracking's "discard" effect, which
	 * in fact removes cards directly from a special display zone without entering the hand. While similar to discard
	 * effects, neither is considered a discard for game purposes, and will not activate related effects.
	 * <p>
	 * This method handles all situations and correctly triggers a {@link DiscardEvent} only when a card is discarded from
	 * the hand.
	 *
	 * @param player The player that owns the card getting discarded.
	 * @param card   The card to discard.
	 * @see #receiveCard(int, Card) for more on receiving and drawing cards.
	 */
	@Suspendable
	public void discardCard(Player player, Card card) {
		// only a 'real' discard should fire a DiscardEvent
		if (card.getZone() == Zones.HAND) {
			logger.debug("discardCard {}: {} discards {}", context.getGameId(), player.getName(), card);
			card.getAttributes().put(Attribute.DISCARDED, context.getTurn());
			context.fireGameEvent(new DiscardEvent(context, player.getId(), card));
			if (!card.hasAttribute(Attribute.DISCARDED)) {
				logger.debug("discardCard {}: Discard of {} has been cancelled by a trigger.", context.getGameId(), card);
				return;
			}
			player.getStatistics().cardDiscarded();
		} else if (card.getZone() == Zones.DECK) {
			logger.debug("discardCard {}: {} roasts {}", context.getGameId(), player.getName(), card);
			card.getAttributes().put(Attribute.ROASTED, context.getTurn());
			context.fireGameEvent(new RoastEvent(context, player.getId(), card));
			if (!card.hasAttribute(Attribute.ROASTED)) {
				logger.debug("discardCard {}: Roast of {} has been cancelled by a trigger", context.getGameId(), card);
				return;
			}
		}

		removeCard(card);
	}

	/**
	 * Draws a card for a player from the deck to the hand.
	 * <p>
	 * When a {@link GameDeck} is empty, the player's {@link Hero} takes "fatigue" damage, which increases by 1 every time
	 * a card should have been drawn but is not.
	 *
	 * @param playerId The player who should draw a card.
	 * @param source   The card that is the origin of the drawing effect, or {@code null} if this is the draw from the
	 *                 beginning of a turn
	 * @return The card that was drawn, or null if the deck was empty.
	 * @see #receiveCard(int, Card) for the full rules on receiving cards into the hand.
	 */
	@Suspendable
	public @Nullable
	Card drawCard(int playerId, Entity source) {
		Player player = context.getPlayer(playerId);
		CardList deck = player.getDeck();
		if (checkAndDealFatigue(player)) {
			return null;
		}

		return drawCard(playerId, deck.peek(), source);
	}

	/**
	 * Checks if the player's deck is empty. If it is, increments the fatigue amount and deals fatigue damange.
	 * <p>
	 * Fatigue is a game mechanic that deals increasing damage to players who have already drawn all of the cards in their
	 * deck, whenever they attempt to draw another card.
	 * <p>
	 * Fatigue deals 1 damage to the hero, plus 1 damage for each time Fatigue has already dealt damage to the player.
	 * Fatigue therefore deals damage cumulatively, steadily increasing in power each time it deals damage.
	 * <p>
	 * If both heroes take measures to survive for this long, the game ends in an unconditional draw at the start of the
	 * 90th turn, even if both players are Immune.
	 * <p>
	 * Fatigue shouldn't count as having originated from anything.
	 *
	 * @param player The {@link Player}  whose fatigue should be checked and dealt to.
	 * @return {@code true} if fatigue damage was dealt.
	 */
	@Suspendable
	public boolean checkAndDealFatigue(Player player) {
		CardList deck = player.getDeck();
		if (deck.isEmpty()) {
			dealFatigueDamage(player);
			return true;
		}
		return false;
	}

	/**
	 * Actually deal and increment fatigue damage to the specified player.
	 *
	 * @param player The player to whom fatigue damage should be dealt.
	 */
	@Suspendable
	public void dealFatigueDamage(Player player) {
		Hero hero = player.getHero();

		if (!player.hasAttribute(Attribute.DISABLE_FATIGUE) && !hero.isDestroyed()) {
			int fatigue = player.hasAttribute(Attribute.FATIGUE) ? player.getAttributeValue(Attribute.FATIGUE) : 0;
			fatigue++;
			player.setAttribute(Attribute.FATIGUE, fatigue);

			damage(player, hero, fatigue, player, true, true, DamageTypeEnum.FATIGUE);
			context.fireGameEvent(new FatigueEvent(context, player.getId(), fatigue));
			player.getStatistics().fatigueDamage(fatigue);
		}
	}

	/**
	 * Draws a specific card into the hand.
	 *
	 * @param playerId The player who should draw the card.
	 * @param card     The specific card to draw.
	 * @param source   The {@link Entity} that is responsible for this effect.
	 * @return
	 * @see #drawCard(int, Entity) for the more general card draw as you would imagine from a deck to the hand.
	 */
	@Suspendable
	public Card drawCard(int playerId, Card card, Entity source) {
		card = receiveCard(playerId, card, source, true);
		return card;
	}

	/**
	 * Ends the player's turn, triggering {@link net.demilich.metastone.game.spells.trigger.TurnEndTrigger} triggers,
	 * clearing one-turn attributes and effects, and removing dead entities.
	 *
	 * @param playerId The player whose turn should be ended.
	 */
	@Suspendable
	public void endTurn(int playerId) {
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);

		// All actors that have attacked last turn get their ATTACKS_LAST_TURN attribute incremented
		Stream.of(player.getMinions(), player.getHeroZone(), opponent.getMinions(), opponent.getHeroZone())
				.flatMap(Collection::stream)
				.forEach((Consumer<Actor>) actor -> {

				});

		Hero hero = player.getHero();
		hero.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
		hero.getAttributes().remove(Attribute.HERO_POWER_USAGES);
		player.getAttributes().remove(Attribute.ATTACKS_THIS_TURN);
		hero.getAttributes().remove(Attribute.ATTACKS_THIS_TURN);
		if (hero.getWeapon() != null) {
			hero.getWeapon().getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
		}
		handleFrozen(hero);
		handleWithered(hero);
		for (Minion minion : player.getMinions()) {
			minion.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
			handleFrozen(minion);
			handleWithered(minion);
		}
		player.getAttributes().remove(Attribute.COMBO);
		hero.activateWeapon(false);

		context.fireGameEvent(new TurnEndEvent(context, playerId));
		if (hasAttribute(player, Attribute.DOUBLE_END_TURN_TRIGGERS)) {
			context.fireGameEvent(new TurnEndEvent(context, playerId));
		}

		// Remove these attributes to occur after the turn has ended
		context.getEntities()
				.filter(entity -> entity.hasAttribute(Attribute.HEALING_THIS_TURN) || entity.hasAttribute(Attribute.EXCESS_HEALING_THIS_TURN) || entity.hasAttribute(Attribute.DAMAGE_THIS_TURN))
				.forEach(actor -> {
					actor.getAttributes().remove(Attribute.HEALING_THIS_TURN);
					actor.getAttributes().remove(Attribute.DAMAGE_THIS_TURN);
					actor.getAttributes().remove(Attribute.EXCESS_HEALING_THIS_TURN);
				});

		for (Player eachPlayer : context.getPlayers()) {
			eachPlayer.setAttribute(Attribute.MINIONS_SUMMONED_THIS_TURN, 0);
			eachPlayer.setAttribute(Attribute.TOTAL_MINIONS_SUMMONED_THIS_TURN, 0);
			eachPlayer.setAttribute(Attribute.DAMAGE_THIS_TURN, 0);
		}

		// Peacefully remove in-play entities with this attribute
		for (Entity entity : context.getEntities()
				.filter(e -> e.isInPlay() || e.getZone() == Zones.HAND)
				.filter(e -> e.hasAttribute(Attribute.REMOVES_SELF_AT_END_OF_TURN))
				.collect(toList())) {
			removePeacefully(entity);
		}

		endOfSequence();

		context.setLastSpellPlayedThisTurn(playerId, null);

		player.setAttribute(Attribute.LAST_TURN, context.getTurn());
	}

	/**
	 * Removes the specified entity peacefully. This will prevent its deathrattles from being triggered or a {@link
	 * KillEvent} from being raised.
	 * <p>
	 * The entity will transition to the right zone after {@link #endOfSequence()} is called.
	 *
	 * @param entity The entity to remove peacefully.
	 */
	@Suspendable
	public void removePeacefully(Entity entity) {
		if (!(entity.isInPlay() || entity.getZone() == Zones.HAND)) {
			return;
		}

		if (entity instanceof Enchantment) {
			((Enchantment) entity).expire();
		}

		entity.setAttribute(Attribute.DESTROYED);
		entity.getAttributes().remove(Attribute.DEATHRATTLES);
		removeEnchantments(entity, true, false, false);
		entity.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
	}

	/**
	 * Equips a {@link Weapon} for a {@link Hero}. Destroys the previous weapon if one was equipped and triggers its
	 * deathrattle effect.
	 *
	 * @param playerId         The player whose hero should equip the weapon.
	 * @param weapon           The weapon to equip.
	 * @param weaponCard
	 * @param resolveBattlecry If {@code true}, the weapon's battlecry {@link Spell} should be cast. This is {@code false}
	 *                         if the weapon was equipped due to some other effect (typically a random weapon
	 */
	@Suspendable
	public void equipWeapon(int playerId, Weapon weapon, Card weaponCard, boolean resolveBattlecry) {
		Player player = context.getPlayer(playerId);

		weapon.setId(generateId());
		weapon.setOwner(playerId);
		Weapon currentWeapon = player.getHero().getWeapon();

		if (currentWeapon != null) {
			currentWeapon.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		}

		player.getWeaponZone().add(weapon);

		if (resolveBattlecry
				&& !weapon.getBattlecries().isEmpty()) {
			resolveBattlecries(playerId, weapon);
			// This will not resolve the deathrattle of the weapon we're replacing
			endOfSequence();
		}

		// We've definitely replaced the existing weapon, whether or not the new weapon is still in play, so its deathrattle
		// will still need to be evaluated (at a later time).
		if (currentWeapon != null) {
			markAsDestroyed(currentWeapon);
		}

		// Resolving the battlecry may have destroyed the weapon we are currently putting into play
		if (weapon.isInPlay()) {
			player.getStatistics().equipWeapon(weapon);
			weapon.onEquip(context, player);
			weapon.setActive(context.getActivePlayerId() == playerId);

			processBattlefieldEnchantments(player, weapon);

			if (weapon.getCardCostModifier() != null) {
				addGameEventListener(player, weapon.getCardCostModifier(), weapon);
			}

			// We're only going to fire this now if the weapon was successfully equipped
			context.fireGameEvent(new WeaponEquippedEvent(context, weapon, weaponCard));
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Causes two actors to fight.
	 * <p>
	 * From Gamepedia:
	 * <p>
	 * A fight, or an "attack," is what occurs when a player commands one character to attack another, causing them to
	 * simultaneously deal damage to each other. Combat is the source of the majority of the damage dealt in many
	 * Hearthstone matches, especially those involving a large number of minions. The core combat mechanics are quite
	 * simple, but the mathematics of multiple minions and heroes attacking each other can require deep strategic
	 * analysis. Attacking can also activate a variety of triggered effects, making even a single attack a potentially
	 * complex process. Some players use "attack" to describe any damage or negative action directed toward the enemy, but
	 * in game terminology only the standard combat action described here counts as an attack and triggers related
	 * effects. Attacking in Hearthstone is usually understood to represent physical combat, particularly melee combat, in
	 * contrast to combat via spells. "Hit" and "swing" are other informal terms for attacking, as in "hit the face" or
	 * "swing into a minion".
	 * <p>
	 * Each character involved in an attack deals {@link #damage(Player, Actor, int, Entity, boolean)} equal to its {@link
	 * Actor#getAttack()} stat to the other. Combat is the primary way for most minions to affect the game, by attacking
	 * either the enemy {@link Hero} or their {@link Minion}s. Minions deal their attack damage both offensively and
	 * defensively, making them potentially dangerous on both sides of combat. Heroes can be involved in combat as either
	 * an attacker or defender too, but all sources of hero attack power only apply on their own turn. Therefore, enemy
	 * minions can hit the hero without harm during the opponent's turn.
	 *
	 * @param player       The player who is initiating the fight.
	 * @param attacker     The attacking {@link Actor}
	 * @param defender     The defending {@link Actor}
	 * @param sourceAction The action corresponding to this fight, if one exists
	 * @see <a href="http://hearthstone.gamepedia.com/Attack">Attack</a> for more on this method and its rules.
	 * @see PhysicalAttackAction#execute(GameContext, int) for the main caller of this function.
	 * @see net.demilich.metastone.game.spells.MisdirectSpell for an example of a spell that causes actors to fight each
	 * other without a player initiatied action.
	 * @see ActionLogic#rollout(GameAction, GameContext, Player, Collection) to see how to enumerate all the possible
	 * {@link PhysicalAttackAction} that determine what can fight what.
	 * @see TargetLogic#getValidTargets(GameContext, Player, GameAction) to see how minions with {@link Attribute#TAUNT}
	 * affect what can and cannot be fought by a player.
	 */
	@Suspendable
	public void fight(Player player, Actor attacker, Actor defender, PhysicalAttackAction sourceAction) {
		// Manages the attacked
		context.getAttackerReferenceStack().addFirst(attacker.getReference());

		Actor target = defender;
		Entity targetOverride = targetAcquisition(player, attacker, sourceAction);
		if (targetOverride != null) {
			target = (Actor) targetOverride;
		}

		if (target != defender) {
			// Override the defender here for the sake of readability
			defender = target;
		}

		// Attacker can change after the target acquisition, or the attack can be cancelled.
		attacker = context.resolveSingleTarget(player, attacker, context.getAttackerReferenceStack().peekFirst());
		if (attacker == null) {
			// Attack was canceled before target was acquired
			return;
		}

		// This BeforePhysicalAttackEvent tracks after targets are resolved and possibly overriden, but before stealth is
		// lost, the number of attacks is modified or immunity is applied.
		context.fireGameEvent(new BeforePhysicalAttackEvent(context, attacker, defender));

		Entity entityGrantedImmunity = null;
		if (attacker.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING) || attacker.hasAttribute(Attribute.AURA_IMMUNE_WHILE_ATTACKING)) {
			applyAttribute(attacker, Attribute.IMMUNE);
			entityGrantedImmunity = attacker;
		}

		removeAttribute(attacker, null, Attribute.STEALTH);
		// Attacker should lose an attack as soon as it loses stealth
		attacker.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, -1);

		// Hearthstone checks for win/loss/draw.
		if (context.updateAndGetGameOver()) {
			clearImmuneWhileAttacking(entityGrantedImmunity);
			return;
		}

		// Damage is computed before the physical attack event, in case it is buffed. To buff before a physical attack, use
		// a BeforePhysicalAttackTrigger
		int attackerDamage = attacker.getAttack();
		int defenderDamage = defender.getAttack();
		// Defender should not be changed in a physical attack event, so target overrides are ignored
		context.fireGameEvent(new PhysicalAttackEvent(context, attacker, defender, attackerDamage));

		// Attacker can change or attack can be cancelled after the PhysicalAttackEvent too
		attacker = context.resolveSingleTarget(player, attacker, context.getAttackerReferenceStack().peekFirst());
		if (attacker == null) {
			// Attack was canceled
			clearImmuneWhileAttacking(entityGrantedImmunity);
			return;
		}

		// secret may have killed attacker ADDENDUM: or defender
		if (attacker.isDestroyed() || defender.isDestroyed()) {
			context.getAttackerReferenceStack().pop();
			clearImmuneWhileAttacking(entityGrantedImmunity);
			return;
		}

		if (defender.getOwner() == Entity.NO_OWNER) {
			logger.error("defender has no owner!! {}", defender);
		}

		// No events are fired by applying damage to an actor so it doesn't actually matter what order this occurs in
		// This could change, theoretically, if the minion has an ability  whose damage depends on the other minion's HP.
		boolean attackerWasDestroyed = attacker.isDestroyed();
		boolean defenderWasDestroyed = defender.isDestroyed();
		int damageDealtToAttacker = applyDamageToActor(attacker, defenderDamage, player, defender, true, DamageTypeEnum.PHYSICAL);
		int damageDealtToDefender = applyDamageToActor(defender, attackerDamage, player, attacker, true, DamageTypeEnum.PHYSICAL);
		// Defender queues first. Damage events should not change the attacker
		resolveDamageEvent(context.getPlayer(defender.getOwner()), defender, attacker, damageDealtToDefender, DamageTypeEnum.PHYSICAL);
		resolveDamageEvent(context.getPlayer(attacker.getOwner()), attacker, defender, damageDealtToAttacker, DamageTypeEnum.PHYSICAL);

		clearImmuneWhileAttacking(entityGrantedImmunity);

		if (attacker.getEntityType() == EntityType.HERO) {
			Hero hero = (Hero) attacker;
			Weapon weapon = hero.getWeapon();
			if (weapon != null && weapon.isActive() && !weapon.hasAttribute(Attribute.IMMUNE) && !weapon.hasAttribute(Attribute.AURA_IMMUNE)) {
				modifyDurability(hero.getWeapon(), -1);
			}
			context.getPlayer(hero.getOwner()).modifyAttribute(Attribute.ATTACKS_THIS_GAME, 1);
			context.getPlayer(hero.getOwner()).modifyAttribute(Attribute.ATTACKS_THIS_TURN, 1);
		}
		attacker.modifyAttribute(Attribute.ATTACKS_THIS_GAME, 1);
		attacker.modifyAttribute(Attribute.ATTACKS_THIS_TURN, 1);

		if (attacker.isDestroyed() && !attackerWasDestroyed) {
			incrementedDestroyedThisSequenceCount();
		}
		if (defender.isDestroyed() && !defenderWasDestroyed) {
			incrementedDestroyedThisSequenceCount();
		}
		context.fireGameEvent(new AfterPhysicalAttackEvent(context, attacker, defender, damageDealtToDefender));
		context.getAttackerReferenceStack().pop();
	}

	private void clearImmuneWhileAttacking(Entity entityGrantedImmunity) {
		if (entityGrantedImmunity != null && (entityGrantedImmunity.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING)
				|| entityGrantedImmunity.hasAttribute(Attribute.AURA_IMMUNE_WHILE_ATTACKING))) {
			entityGrantedImmunity.getAttributes().remove(Attribute.IMMUNE);
		}
	}

	/**
	 * Gains armor and triggers an {@link ArmorChangedEvent}.
	 *
	 * @param player The player whose {@link Hero} should gain armor.
	 * @param armor  The amount of armor to gain.
	 * @see #damage(Player, Actor, int, Entity, boolean) for a description of how armor protects an {@link Actor} like a
	 * {@link Hero}.
	 */
	@Suspendable
	public void gainArmor(Player player, int armor) {
		logger.debug("{} gains {} armor", player.getHero(), armor);
		player.getHero().modifyArmor(armor);
		player.getStatistics().armorGained(armor);
		if (armor != 0) {
			context.fireGameEvent(new ArmorChangedEvent(context, player.getHero(), armor));
		}
	}

	/**
	 * Generates a card ID for creating cards on the fly inside the game.
	 *
	 * @return A new {@link String} that describes a new card ID.
	 */
	public String generateCardId() {
		return TEMP_CARD_LABEL + idFactory.generateId();
	}

	/**
	 * Gets a random target from a list of potential targets.
	 * <p>
	 * Implements Misdirect and cards that have a 50% chance to hit the wrong target, like Ogre Brute.
	 *
	 * @param player           The player whose actor is initiating a target acquisition.
	 * @param attacker         The attacker that may missed its intended target.
	 * @param originalTarget   The intended target.
	 * @param potentialTargets The other targets the attacker could hit.
	 * @return The new target.
	 */
	public Actor getAnotherRandomTarget(Player player, Actor attacker, Actor originalTarget, EntityReference potentialTargets) {
		List<Entity> validTargets = context.resolveTarget(player, attacker, potentialTargets);
		// cannot redirect to attacker
		validTargets.remove(attacker);
		// cannot redirect to original target
		validTargets.remove(originalTarget);
		if (validTargets.isEmpty()) {
			return originalTarget;
		}

		return (Actor) context.getLogic().getRandom(validTargets);
	}

	/**
	 * Returns the first value of the attribute encountered. This method should be used with caution, as the result is
	 * random if there are different values of the same attribute in play.
	 *
	 * @param player       The player whose actors should be queries.
	 * @param attr         Which attribute to find
	 * @param defaultValue The value returned if no occurrence of the attribute is found
	 * @return the first occurrence of the value of attribute or defaultValue
	 */
	public int getAttributeValue(Player player, Attribute attr, int defaultValue) {
		for (Entity minion : player.getMinions()) {
			if (minion.hasAttribute(attr)) {
				return minion.getAttributeValue(attr);
			}
		}

		return defaultValue;
	}

	/**
	 * For heroes that have a {@link Card} that has automatic target selection, returns the hero power. It is not clear if
	 * this is used by any hero power cards in the game.
	 *
	 * @param playerId The player equipped with an auto hero power.
	 * @return The action to play the hero power.
	 */
	@Suspendable
	public GameAction getAutoHeroPowerAction(int playerId) {
		return actionLogic.getAutoHeroPower(context, context.getPlayer(playerId));
	}

	/**
	 * Finds {@link ChooseOneOverrideAura} auras that affect the {@code card} and indicates what choose one override is
	 * specified.
	 *
	 * @param player
	 * @param card
	 * @return The override, or {@link ChooseOneOverride#NONE} if none is specified.
	 */
	public ChooseOneOverride getChooseOneAuraOverrides(Player player, final Card card) {
		List<ChooseOneOverrideAura> auras = SpellUtils.getAuras(context, player.getId(), ChooseOneOverrideAura.class);
		ChooseOneOverride override = ChooseOneOverride.NONE;
		// Since it's in order of play, the last aura will take precedence by overwriting the prior auras.
		for (ChooseOneOverrideAura aura : auras) {
			// The aura affects the card if either it is affected by the aura or its owner is affected by the aura.
			if (aura.getAffectedEntities().contains(card.getId()) ||
					(aura.getAffectedEntities().contains(player.getId()) && card.getOwner() == player.getId())) {
				override = aura.getChooseOneOverride();
			}
		}

		return override;
	}

	/**
	 * Return the greatest value of an attribute from all {@link Actor}s of a player.
	 * <p>
	 * This method will return infinite if an Attribute value is negative, so use this method with caution.
	 *
	 * @param player Which player to check
	 * @param attr   Which attribute to find
	 * @return The highest value from all sources. -1 is considered infinite.
	 */
	public int getGreatestAttributeValue(Player player, Attribute attr) {
		int greatest = 0;
		if (player.getHero().hasAttribute(Attribute.HERO_POWER_USAGES)) {
			int attributeValue = player.getHero().getAttributeValue(attr);
			if (attributeValue == INFINITE) {
				return greatest;
			} else {
				greatest = Math.max(attributeValue, greatest);
			}
		}

		if (player.hasAttribute(Attribute.HERO_POWER_USAGES)) {
			int attributeValue = player.getAttributeValue(attr);
			if (attributeValue == INFINITE) {
				return greatest;
			} else {
				greatest = Math.max(attributeValue, greatest);
			}
		}

		for (Entity minion : player.getMinions()) {
			if (minion.hasAttribute(attr)) {
				if (minion.getAttributeValue(attr) > greatest) {
					greatest = minion.getAttributeValue(attr);
				}
				if (minion.getAttributeValue(attr) == INFINITE) {
					return INFINITE;
				}
			}
		}
		return greatest;
	}

	/**
	 * Gets the current status of a match.
	 *
	 * @param player   The player whose point of view to use for the {@link GameStatus}
	 * @param opponent The player's opponent.
	 * @return A {@link GameStatus} from the point of view of the given player.
	 */
	public GameStatus getMatchResult(Player player, Player opponent) {
		boolean playerLost = hasPlayerLost(player);
		boolean opponentLost = hasPlayerLost(opponent);
		if (playerLost && opponentLost) {
			return GameStatus.DOUBLE_LOSS;
		} else if (playerLost || opponentLost) {
			return GameStatus.WON;
		}
		return GameStatus.RUNNING;
	}

	/**
	 * Gets the mana cost of a card considering any {@link CardCostModifier} objects that may apply to it.
	 * <p>
	 * If the card is not in play, the deck or the hand, returns the base mana cost of the card.
	 *
	 * @param player The player whose point of view to consider for the card cost.
	 * @param card   The card to cost.
	 * @return The modified mana cost of the card.
	 */
	@Suspendable
	public int getModifiedManaCost(Player player, Card card) {
		int manaCost = card.getBaseManaCost();
		if (card.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			return manaCost - card.getManaCostModification(context, player);
		}
		int minValue = 0;
		for (Trigger trigger : context.getTriggerManager().getTriggers()) {
			if (!(trigger instanceof CardCostModifier)) {
				continue;
			}

			CardCostModifier costModifier = (CardCostModifier) trigger;
			if (!costModifier.appliesTo(context, card, player)) {
				continue;
			}
			Entity host = context.resolveSingleTarget(costModifier.getHostReference());
			manaCost = costModifier.process(context, host, card, manaCost, player);
			if (costModifier.getMinValue() > minValue) {
				minValue = costModifier.getMinValue();
			}
		}
		manaCost -= card.getManaCostModification(context, player);
		manaCost = MathUtils.clamp(manaCost, minValue, Integer.MAX_VALUE);
		if (canActivateInvokeKeyword(player, card)) {
			if (card.hasAttribute(Attribute.AURA_INVOKE)) {
				manaCost = card.getAttributeValue(Attribute.AURA_INVOKE);
			} else if (card.hasAttribute(Attribute.INVOKE)) {
				manaCost = card.getAttributeValue(Attribute.INVOKE);
			}
		}

		// A card that was invoked has a different modified mana cost
		if (card.hasAttribute(Attribute.INVOKED)) {
			manaCost = card.getAttributeValue(Attribute.INVOKED);
		}
		return manaCost;
	}

	private boolean canActivateInvokeKeyword(Player player, Card card) {
		// Short circuit this costly computation
		if (!card.hasAttribute(Attribute.INVOKE) && !card.hasAttribute(Attribute.AURA_INVOKE)) {
			return false;
		}

		int mana = player.getMana();
		List<CardCostInsteadAura> auras = SpellUtils.getAuras(context, player.getId(), CardCostInsteadAura.class);
		if (doesCardCostHealth(player, card) && player.getHero() != null) {
			// TODO: Cards that cost health should migrate to the CardCostInsteadAura system so that order of play is respected
			mana = player.getHero().getHp();
		}

		if (auras.size() > 0) {
			// TODO: How should Invoke interact with card costs like this?
			mana = auras.stream().mapToInt(aura -> aura.getAmountOfCurrency(context, player, card, card)).max().orElseThrow(RuntimeException::new);
		}

		return (card.hasAttribute(Attribute.INVOKE) && card.getAttributeValue(Attribute.INVOKE) <= mana)
				|| (card.hasAttribute(Attribute.AURA_INVOKE) && card.getAttributeValue(Attribute.AURA_INVOKE) <= mana);
	}

	private int getTotalAttributeValue(Player player, Attribute attr) {
		int value = 0;
		for (Entity entity : player.getLookup().values()) {
			if (!entity.isInPlay()) {
				continue;
			}
			value += entity.getAttributeValue(attr);
		}
		for (Trigger trigger : context.getTriggerManager().getTriggers()) {
			if (trigger instanceof Enchantment && trigger.getOwner() == player.getId()) {
				value += ((Enchantment) trigger).getAttributeValue(attr);
			}
		}
		return value;
	}

	private int getTotalAttributeMultiplier(Player player, Attribute attribute) {
		int total = 1;
		if (player.getHero().hasAttribute(attribute)) {
			player.getHero().getAttributeValue(attribute);
		}
		for (Entity minion : player.getMinions()) {
			if (minion.hasAttribute(attribute)) {
				total *= minion.getAttributeValue(attribute);
			}
		}
		return total;
	}

	/**
	 * Computes all the valid actions a player can currently take.
	 *
	 * @param playerId The player whose point of view should be considered.
	 * @return A list of valid actions the player can take. If it is not the player's turn, no actions are returned.
	 * @see ActionLogic#getValidActions(GameContext, Player) for the logic behind determining what actions a player can
	 * take.
	 */
	@Suspendable
	public List<GameAction> getValidActions(int playerId) {
		Player player = context.getPlayer(playerId);
		if (context.getActivePlayerId() != playerId) {
			return Collections.emptyList();
		}
		return actionLogic.getValidActions(context, player);
	}

	/**
	 * Gets the list of valid targets for an action.
	 * <p>
	 * This method is primarily used for cards that change regular actions into "random" actions, like {@link
	 * net.demilich.metastone.game.spells.CastRandomSpellSpell}
	 *
	 * @param playerId The player that would take the action.
	 * @param action   The action to get valid targets for.
	 * @return A list of valid targets
	 * @see TargetLogic#getValidTargets(GameContext, Player, GameAction) for the logic behind determining valid targets
	 * given an action.
	 */
	public List<Entity> getValidTargets(int playerId, GameAction action) {
		Player player = context.getPlayer(playerId);
		return targetLogic.getValidTargets(context, player, action);
	}

	/**
	 * Determines which player is the winner from the point of view of the given player.
	 *
	 * @param player   The local player.
	 * @param opponent The player's opponent.
	 * @return The player that is the winner, or {@code null} if there is no winner.
	 */
	public Player getWinner(Player player, Player opponent) {
		boolean playerLost = hasPlayerLost(player);
		boolean opponentLost = hasPlayerLost(opponent);
		if (playerLost && opponentLost) {
			return null;
		} else if (opponentLost) {
			return player;
		} else if (playerLost) {
			return opponent;
		}
		return null;
	}

	/**
	 * Handles changes in a entity's hitpoints.
	 * <p>
	 * Currently implements {@link Attribute#ENRAGED}.
	 *
	 * @param entity The entity whose hitpoints have changed.
	 */
	@Suspendable
	private void handleHpChange(Actor entity) {
		if (!entity.hasAttribute(Attribute.ENRAGABLE)) {
			return;
		}
		boolean enraged = entity.getHp() < entity.getMaxHp();
		// enrage publicState has not changed; do nothing
		if (entity.hasAttribute(Attribute.ENRAGED) == enraged) {
			return;
		}

		if (enraged) {
			entity.setAttribute(Attribute.ENRAGED);
		} else {
			entity.getAttributes().remove(Attribute.ENRAGED);
		}

		context.fireGameEvent(new EnrageChangedEvent(context, entity));
	}

	@Suspendable
	private void handleFrozen(Actor actor) {
		if (!actor.hasAttribute(Attribute.FROZEN)) {
			return;
		}
		if (actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) >= actor.getMaxNumberOfAttacks() && !actor.hasAttribute(Attribute.FREEZES_PERMANENTLY)) {
			removeAttribute(actor, null, Attribute.FROZEN);
		}
	}

	@Suspendable
	private void handleWithered(Actor actor) {
		if (!actor.hasAttribute(Attribute.WITHERED)) {
			return;
		}
		if (actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) >= actor.getMaxNumberOfAttacks()) {
			removeAttribute(actor, null, Attribute.WITHERED);
		}
	}

	/**
	 * Determines whether a {@link Player}, the player's {@link Hero} or a player's {@link Minion} entities have a given
	 * attribute.
	 *
	 * @param player The player whose player entity and minions will be queries for the attribute.
	 * @param attr   The attribute to query.
	 * @return {@code true} if the player entity or its minions have the given attribute.
	 */
	public boolean hasAttribute(Player player, Attribute attr) {
		if (player.hasAttribute(attr)) {
			return true;
		}

		if (player.getHero().hasAttribute(attr)) {
			return true;
		}

		if (player.getHero().getHeroPower() != null
				&& player.getHero().getHeroPower().hasAttribute(attr)) {
			return true;
		}

		for (Entity minion : player.getMinions()) {
			if (minion.hasAttribute(attr)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns {@code true} if the given player has an "auto" (auto-targeting) hero power.
	 *
	 * @param player The player whose point of view should be considered.
	 * @return {@code true} if the hero power is "auto" targeting.
	 */
	@Suspendable
	public boolean hasAutoHeroPower(int player) {
		return actionLogic.hasAutoHeroPower(context, context.getPlayer(player));
	}

	/**
	 * Checks whether a player has a card with the given card ID.
	 *
	 * @param player The player whose hand or hero power should be queries.
	 * @param card   The card whose ID should be used for comparisons.
	 * @return {@code true} if the card is contained in the {@link Zones#HAND} or {@link Zones#HERO_POWER} zones.
	 */
	public boolean hasCard(Player player, Card card) {
		return Stream.concat(player.getHand().stream(), player.getHeroPowerZone().stream()).anyMatch(c -> c.getCardId().equals(card.getCardId()));
	}


	@Suspendable
	public HealingResult heal(Player player, Actor target, int healing, Entity source) {
		return heal(player, target, healing, source, true);
	}


	/**
	 * Heals (restores hitpoints to) a target.
	 * <p>
	 * Healing an {@link Actor} will increase their {@link Actor#getHp()} by the stated amount, up to but not beyond their
	 * current {@link Actor#getMaxHp()}.
	 * <p>
	 * Healing comes from openers, aftermaths, spell triggers, hero powers and spell cards that cast a {@link HealSpell}.
	 * Most healing effects affect a single {@link Actor} (these effects can be targetable or select the target
	 * automatically or at random), while some others have an area of effect.
	 * <p>
	 * Healing is distinct from granting a minion increased hitpoints, which increases both the current and maximum Health
	 * for the target. Increasing a minion's hitpoints is usually achieved through enchantments (or removing them through
	 * {@link SilenceSpell}), while healing is usually achieved through effects.
	 * <p>
	 * Although healing effects (including targetable ones) can target undamaged characters, attempting to restore
	 * hitpoints to an {@link Actor} already at their current maximum Health will have no effect and will not count as
	 * healing for game purposes (for example, on-heal triggers such as {@link HealingTrigger} will not trigger).
	 * <p>
	 * Excess healing triggers {@link ExcessHealingTrigger}.
	 * <p>
	 * Healing a character to full hitpoints will remove its damaged status and thus any {@link Attribute#ENRAGED} effect
	 * currently active.
	 *
	 * @param player            The player who chose the target of the healing.
	 * @param target            The target of the healing.
	 * @param healing           The amount of healing.
	 * @param source            The {@link Entity}, typically a {@link Card} or {@link Minion} with opener, that is the
	 *                          source of the healing.
	 * @param applyHealingBonus Whether or not to compute the effects of {@link Attribute#HEALING_BONUS} and {@link
	 *                          Attribute#SPELL_HEAL_AMPLIFY_MULTIPLIER} on this card.
	 * @return the amount of healing that was actually performed
	 * @see Attribute#ENRAGED for more about enrage.
	 */
	@Suspendable
	public HealingResult heal(Player player, Actor target, int healing, Entity source, boolean applyHealingBonus) {
		if (hasAttribute(player, Attribute.INVERT_HEALING)) {
			damage(player, target, healing, source);
			return new HealingResult(0, 0);
		}

		healing = getModifiedHealing(player, healing, source, applyHealingBonus);
		int newHp = Math.min(target.getMaxHp(), target.getHp() + healing);
		int oldHp = target.getHp();
		int excess = healing - (newHp - oldHp);
		healing = newHp - oldHp;
		target.setHp(newHp);

		if (target.getEntityType() == EntityType.MINION) {
			handleHpChange(target);
		}

		// Only record the amount that is actually healed
		if (healing > 0) {
			HealEvent healEvent = new HealEvent(context, player.getId(), target, healing);
			// Implements Happy Ghoul
			target.modifyAttribute(Attribute.HEALING_THIS_TURN, healing);
			player.modifyAttribute(Attribute.HEALING_THIS_TURN, healing);
			target.setAttribute(Attribute.LAST_HEAL, healing);
			player.setAttribute(Attribute.LAST_HEAL, healing);
			// Implements Crystal Giant
			player.modifyAttribute(Attribute.TIMES_HEALED, 1);
			target.modifyAttribute(Attribute.TIMES_HEALED, 1);
			context.fireGameEvent(healEvent);
			player.getStatistics().heal(healing);
		}

		if (excess > 0) {
			context.fireGameEvent(new ExcessHealingEvent(context, player.getId(), target, excess));
			target.modifyAttribute(Attribute.EXCESS_HEALING_THIS_TURN, excess);
			player.modifyAttribute(Attribute.EXCESS_HEALING_THIS_TURN, excess);
		}

		return new HealingResult(healing, excess);
	}

	/**
	 * Compute the amount of healing given a specified base healing.
	 *
	 * @param player
	 * @param healing
	 * @param source
	 * @param applyHealingBonus
	 * @return
	 */
	public int getModifiedHealing(Player player, int healing, Entity source, boolean applyHealingBonus) {
		if (applyHealingBonus) {
			healing += getAttributeValue(player, Attribute.HEALING_BONUS, 0);
			healing += getAttributeValue(player, Attribute.AURA_HEALING_BONUS, 0);
			healing += getAttributeValue(context.getOpponent(player), Attribute.ENEMY_HEALING_BONUS, 0);
			healing += getAttributeValue(context.getOpponent(player), Attribute.AURA_ENEMY_HEALING_BONUS, 0);
			healing = applyAmplify(player, healing, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		}

		if (source instanceof Card) {
			Card sourceCard = (Card) source;
			if (sourceCard.isSpell()) {
				healing = applyAmplify(player, healing, Attribute.SPELL_HEAL_AMPLIFY_MULTIPLIER);
			}
			if (sourceCard.isHeroPower()) {
				healing = applyAmplify(player, healing, Attribute.HERO_POWER_HEAL_AMPLIFY_MULTIPLIER);
			}
		}
		return healing;
	}

	/**
	 * Activates all the appropriate enchantments for a player who has mulliganned, and gives that player the player's
	 * {@link GameStartEvent}.
	 *
	 * @param player Player who just finished mulligan phase, but before turn starts
	 */
	@Suspendable
	public void startGameForPlayer(Player player) {
		player.setAttribute(Attribute.GAME_STARTED);

		processGameTriggers(player, player.getHero());
		processBattlefieldEnchantments(player, player.getHero());

		processGameTriggers(player, player.getHero().getHeroPower());
		processPassiveTriggers(player, player.getHero().getHeroPower());
		processPassiveAuras(player, player.getHero().getHeroPower());

		for (Card card : player.getDeck()) {
			processGameTriggers(player, card);
			processDeckTriggers(player, card);
		}

		for (Card card : player.getHand()) {
			processGameTriggers(player, card);
			processPassiveTriggers(player, card);
			processPassiveAuras(player, player.getHero().getHeroPower());
		}

		context.fireGameEvent(new PreGameStartEvent(context, player.getId()));
		context.fireGameEvent(new GameStartEvent(context, player.getId()));
	}

	/**
	 * Configures the player {@link Player}, {@link Hero}, and deck &amp; hand {@link Card} entities with the correct IDs,
	 * {@link EntityZone} locations and owners. Shuffles the deck.
	 *
	 * @param playerId The player that should be initialized.
	 * @param begins
	 * @return The initialized {@link Player} object.
	 */
	@Suspendable
	public Player initializePlayerAndMoveMulliganToSetAside(int playerId, boolean begins) {
		Player player = context.getPlayer(playerId);

		if (player.getId() == IdFactory.UNASSIGNED) {
			player.setId(playerId);
		}

		player.setOwner(player.getId());
		Hero hero = player.getHero();
		player.getHero().setId(generateId());
		player.getHero().setOwner(player.getId());
		player.getHero().setMaxHp(player.getHero().getAttributeValue(Attribute.BASE_HP));
		player.getHero().setHp(player.getHero().getAttributeValue(Attribute.BASE_HP));
		Card heroPower = context.getCardById(hero.getSourceCard().getDesc().getHeroPower());
		heroPower.setOwner(playerId);
		heroPower.setId(generateId());
		player.getHeroPowerZone().add(heroPower);
		player.updateLookup(player);
		player.updateLookup(hero);
		player.updateLookup(heroPower);
		assignEntityIds(player.getDeck(), playerId);
		assignEntityIds(player.getHand(), playerId);
		// The player can use a hero power once per turn by default
		player.setAttribute(Attribute.HERO_POWER_USAGES, 1);

		// Implements Open the Waygate
		Stream.concat(player.getDeck().stream(),
				player.getHand().stream()).forEach(c -> c.getAttributes().put(Attribute.STARTED_IN_DECK, true));

		// The deck is shuffled TWICE. Once before the mulligan, here, and once after.
		player.getDeck().shuffle(getRandom());

		// TODO: Should we really be doing this here?
		if (player.getHero().hasEnchantment()) {
			for (Enchantment trigger : player.getHero().getEnchantments()) {
				addGameEventListener(player, trigger, player.getHero());
			}
		}
		processGameTriggers(player, hero.getHeroPower());
		processPassiveTriggers(player, hero.getHeroPower());
		processPassiveAuras(player, hero.getHeroPower());

		// Populate both player's hands here first to prevent consuming random resources
		int numberOfStarterCards = begins ? getStarterCards() : getStarterCards() + getSecondPlayerBonusStarterCards();

		// The player's starting hand should always contain the quest.
		// Since our server could theoretically allow you to have a deck with multiple quests, they will
		// all start here.
		List<Card> starterCards = player.getDeck().stream()
				.filter(card -> card.hasAttribute(Attribute.QUEST))
				.filter(card -> !card.hasAttribute(Attribute.NEVER_MULLIGANS))
				.limit(numberOfStarterCards)
				.collect(toList());

		// Cards are now in the set aside zone
		starterCards.forEach(card -> player.getDeck().move(card, player.getSetAsideZone()));

		// After quests, fill the remainder of the starter cards
		for (int j = starterCards.size(); j < numberOfStarterCards && !player.getDeck().isEmpty(); j++) {
			Card randomCard = getRandom(player.getDeck().filtered(c -> !c.hasAttribute(Attribute.NEVER_MULLIGANS)));
			if (randomCard != null) {
				player.getDeck().move(randomCard, player.getSetAsideZone());
				starterCards.add(randomCard);
			}
		}

		return player;
	}

	protected int getStarterCards() {
		return STARTER_CARDS;
	}

	/**
	 * A joust describes when cards are revealed from each player's deck, and the "winner" of a joust is determined by
	 * whoever draws a card with a higher {@link Card#getBaseManaCost()}.
	 * <p>
	 * From Hearthpedia:
	 * <p>
	 * Joust is an ability that causes a minion to be revealed at random from the deck of each player. If the player who
	 * initiated the Joust has the higher mana cost minion, a special secondary effect will be activated, depending on the
	 * Joust card. Once the Joust is complete, the two Jousting minions are shuffled back into their respective decks.
	 * Jousts are triggered through other abilities, most commonly Battlecry, but at least one card uses Deathrattle to
	 * Joust. Joust does not exist as a keyword, but is the official term for the card text, "Reveal a minion in each
	 * deck. If yours costs more, [secondary effect]."
	 *
	 * @param player     The player who initiated the joust.
	 * @param cardFilter
	 * @param source
	 * @return The joust event that was fired.
	 */
	@Suspendable
	public JoustEvent joust(Player player, EntityFilter cardFilter, Entity source) {
		Card ownCard;
		CardList ownCards = player.getDeck().filtered(c -> cardFilter.matches(context, player, c, source));
		if (ownCards.size() == 0) {
			ownCard = null;
		} else {
			ownCard = getRandom(ownCards);
		}
		Card opponentCard = null;
		boolean won = false;
		// no minions left in deck - automatically loose joust
		if (ownCard == null) {
			won = false;
		} else {
			Player opponent = context.getOpponent(player);
			CardList opponentCards = opponent.getDeck().filtered(c -> cardFilter.matches(context, opponent, c, source));
			if (opponentCards.size() > 0) {
				opponentCard = getRandom(opponentCards);
			}
			// opponent has no minions left in deck - automatically win joust
			if (opponentCard == null) {
				won = true;
			} else {
				// both players have minion cards left, the initiator needs to
				// have the one with
				// higher mana cost to win the joust
				won = ownCard.getBaseManaCost() > opponentCard.getBaseManaCost();
			}
		}
		JoustEvent joustEvent = new JoustEvent(context, player.getId(), won, ownCard, opponentCard);
		context.fireGameEvent(joustEvent);
		return joustEvent;
	}

	/**
	 * Marks an {@link Actor} as destroyed. Used for "Destroy" effects.
	 * <p>
	 * An actor marked this way gets moved to the {@link Zones#GRAVEYARD} by a {@link #endOfSequence()} call.
	 *
	 * @param target The {@link Actor} to mark as destroyed.
	 */
	public void markAsDestroyed(Actor target) {
		if (target != null) {
			if (!target.isDestroyed()) {
				incrementedDestroyedThisSequenceCount();
			}
			target.setAttribute(Attribute.DESTROYED);
		}
	}

	/**
	 * Increments the number of actors that have been destroyed this sequence.
	 * <p>
	 * This gets cleared at the end of the sequence.
	 */
	public void incrementedDestroyedThisSequenceCount() {
		context.getEnvironment().compute(Environment.DESTROYED_THIS_SEQUENCE_COUNT,
				(k, v) -> (v == null) ? 1 : ((int) v + 1));
	}

	/**
	 * Mind control moves a {@link Minion} from the opponent's {@link Zones#BATTLEFIELD} to their own battlefield and puts
	 * it under control of the given {@link Player}.
	 * <p>
	 * Mind control effects or control effects are effects which allow a player to seize control of an enemy minion.
	 * Controlled minions are treated as belonging to the controlling player for all purposes, can be directed to attack
	 * its former allies and owner, and will immediately be transferred to the controlling player's side of the
	 * battlefield, to the far right of the board.
	 * <p>
	 * Control is generally a permanent state change, and as such cannot be changed through Silences, Return effects or
	 * other means. The exceptions to this are Shadow Madness and Potion of Madness, which grant temporary control of a
	 * minion through a one-turn enchantment.
	 * <p>
	 * If a player activates a mind control effect when their side of the battlefield is already full (i.e. they have the
	 * maximum 7 minions), the mind controlled minion will be instantly destroyed. Any Deathrattle that activates as a
	 * result of this will trigger as if their opponent still controlled the minion. It is often a good idea for a player
	 * to choose to intentionally destroy one of their own minions in order to be able to seize control of one of their
	 * opponent's, especially by sacrificing a weak minion in order to gain control of a very powerful one.
	 * <p>
	 * As with summoning effects such as Mirror Image and Feral Spirit, mind controlled minions will always join the board
	 * on the far right. Anticipating this can allow for superior placement of minions, important for positional effects.
	 * When planning to summon other minions that turn, the player can use the timing of the mind control effect to allow
	 * them to determine the final placement of the mind controlled minion. For example, a player with a Shieldbearer
	 * already on the board may take control of a Flametongue Totem, before then summoning a Sludge Belcher to the right
	 * of it, thereby ensuring the Totem's is placed between the two minions, making the most of its buff.
	 * <p>
	 * Minions that have just been mind controlled are normally {@link Attribute#SUMMONING_SICKNESS} for one turn and
	 * cannot attack, just as with minions that were summoned that turn. However, Shadow Madness and Potion of Madness do
	 * not cause its target to be {@link Attribute#SUMMONING_SICKNESS}, allowing it to attack - the effect only lasts
	 * until end of turn, and would otherwise be nearly useless. Charge affects mind control exhaustion just as it affects
	 * {@link Attribute#SUMMONING_SICKNESS} - minions with that ability can attack on the same turn they are mind
	 * controlled.
	 *
	 * @param player The new owner of a minion.
	 * @param minion The minion to mind control.
	 */
	@Suspendable
	public void mindControl(Player player, Minion minion) {
		Player opponent = context.getOpponent(player);
		if (!opponent.getMinions().contains(minion)) {
			// logger.warn("Minion {} cannot be mind-controlled, because
			// opponent does not own it.", minion);
			return;
		}
		if (canSummonMoreMinions(player)) {
			context.getOpponent(player).getMinions().remove(minion);
			player.getMinions().add(minion);
			applyAttribute(minion, Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(minion);
			changeOwner(minion, player.getId());
		} else {
			markAsDestroyed(minion);
		}
	}

	/**
	 * Steals the card, transferring its owner and moving its current zones. Keeps all associated {@link Trigger} objects
	 * and changes all trigger owners whose {@link Trigger#hasPersistentOwner()} property is {@code false}.
	 * <p>
	 * Similar to {@link #mindControl(Player, Minion)} but for {@link Card} entities.
	 * <p>
	 * To implement King Togwaggle, stealing to the {@link Zones#SET_ASIDE_ZONE} first is supported.
	 *
	 * @param newOwner    The new owner's player ID.
	 * @param source      The source of the card theft (typically the card that is casting the stealing spell).
	 *                    Corresponds to the {@link #drawCard(int, Card, Entity)} {@code source} argument.
	 * @param card        The {@link Card} to steal
	 * @param destination The destination {@link Zones}. Only {@link Zones#DECK}, {@link Zones#HAND} and {@link
	 *                    Zones#SET_ASIDE_ZONE} are currently valid.
	 * @throws IllegalArgumentException if the destination is invalid (not {@link Zones#HAND}, {@link Zones#DECK} and
	 *                                  {@link Zones#SET_ASIDE_ZONE}.
	 */
	@Suspendable
	public boolean stealCard(Player newOwner, Entity source, Card card, Zones destination) throws IllegalArgumentException {
		// If the card isn't already in the SET_ASIDE_ZONE, move it
		if (card.getZone() != Zones.SET_ASIDE_ZONE) {
			// Move to set aside zone first.
			context.getPlayer(card.getOwner()).getZone(card.getZone()).move(card, newOwner.getSetAsideZone());
		}

		// Only change the owner if necessary
		if (card.getOwner() != newOwner.getId()) {
			changeOwner(card, newOwner.getId());
		}

		// Move to the destination
		if (destination == Zones.HAND) {
			return receiveCard(newOwner.getId(), card, source, false) != null;
		} else if (destination == Zones.DECK) {
			// Remove again to make shuffling to deck valid.
			context.getPlayer(card.getOwner()).getZone(card.getZone()).remove(card);
			return shuffleToDeck(newOwner, card);
		} else if (destination != Zones.SET_ASIDE_ZONE) {
			throw new IllegalArgumentException(String.format("Invalid destination %s for card %s", destination.name(), card.getName()));
		}
		return true;
	}

	/**
	 * Changes the owner of a target. Does not move its zones.
	 *
	 * @param target     The {@link Entity} whose ownership should change.
	 * @param newOwnerId The player ID of the new owner.
	 * @throws ArrayStoreException if the target is in a zone that does not match the new owner.
	 */
	@Suspendable
	public void changeOwner(Entity target, int newOwnerId) throws ArrayStoreException {
		innerChangeOwner(target, newOwnerId);
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	@Suspendable
	public void innerChangeOwner(Entity target, int newOwnerId) {
		if (target.getEntityLocation().getPlayer() != newOwnerId) {
			throw new ArrayStoreException("Cannot change the owner of an entity that is located in a zone not owned by the new owner.");
		}
		target.setOwner(newOwnerId);
		List<Trigger> triggers = context.getTriggersAssociatedWith(target.getReference())
				.stream().map(Trigger::clone).collect(toList());
		removeEnchantments(target);
		for (Trigger trigger : triggers) {
			if (!trigger.hasPersistentOwner()) {
				trigger.setOwner(newOwnerId);
			}
			addGameEventListener(context.getPlayer(newOwnerId), trigger, target);
		}
	}

	/**
	 * Modifies the current mana that the player has.
	 * <p>
	 * Fires a {@link ModifyCurrentManaEvent} if the {@code mana} does not equal zero <b>and</b> if the {@code mana} is
	 * negative, only if {@code spent} is {@code true}.
	 *
	 * @param playerId The player whose mana should be modified.
	 * @param mana     The amount to increment or decrement the mana by.
	 * @param spent    If {@code true}, indicates the effect modifying this mana should be considered a form of spending.
	 */
	@Suspendable
	public void modifyCurrentMana(int playerId, int mana, boolean spent) {
		Player player = context.getPlayer(playerId);
		int newMana = MathUtils.clamp(player.getMana() + mana, 0, MAX_MANA + Math.abs(mana));
		player.setMana(newMana);
		if ((mana < 0 && spent) || mana > 0) {
			context.getPlayer(playerId).modifyAttribute(Attribute.MANA_SPENT_THIS_TURN, mana < 0 ? -mana : 0);
			context.fireGameEvent(new ModifyCurrentManaEvent(context, playerId, mana));
		}
	}


	/**
	 * Modifies the durability (hitpoints) of a weapon.
	 *
	 * @param weapon     The weapon to modify.
	 * @param durability The amount of durability to increment or decrement to the weapon.
	 * @see Weapon for a complete description of a weapon's fields.
	 * @see Weapon#getDurability() for more about durability.
	 */
	public void modifyDurability(Weapon weapon, int durability) {
		weapon.modifyAttribute(Attribute.HP, durability);
		if (durability > 0) {
			weapon.modifyAttribute(Attribute.MAX_HP, durability);
		}
	}

	/**
	 * Increment or decrement the {@link Actor#getMaxHp()} property of a {@link Actor}
	 *
	 * @param actor The actor
	 * @param value The amount to increment or decrement the amount of hitpoints.
	 */
	@Suspendable
	public void setHpAndMaxHp(Actor actor, int value) {
		// If there is an active aura, we must account for it here
		int auraHp = actor.getAttributeValue(Attribute.AURA_HP_BONUS);
		int newMaxHp = value + auraHp;
		int currentMaxHp = actor.getMaxHp();
		actor.setMaxHp(newMaxHp);
		actor.setHp(newMaxHp);
		handleHpChange(actor);
		if (newMaxHp > currentMaxHp) {
			int amount = newMaxHp - currentMaxHp;
			actor.modifyAttribute(Attribute.TOTAL_HP_INCREASES, amount);
			context.fireGameEvent(new MaxHpIncreasedEvent(context, actor, amount, -1));
		}
	}

	/**
	 * Increment or decrement the {@link Player#getMaxMana()} property of a {@link Player}
	 *
	 * @param player The player
	 * @param delta  The amount to increment or decrement the amount of mana the player has.
	 */
	@Suspendable
	public void modifyMaxMana(Player player, int delta) {
		final int maxMana = MathUtils.clamp(player.getMaxMana() + delta, 0, GameLogic.MAX_MANA);
		final int initialMaxMana = player.getMaxMana();
		final int change = maxMana - initialMaxMana;
		player.setMaxMana(maxMana);
		if (delta < 0 && player.getMana() > player.getMaxMana()) {
			modifyCurrentMana(player.getId(), delta, false);
		}
		if (change != 0) {
			context.fireGameEvent(new MaxManaChangedEvent(context, player.getId(), change));
		}
	}

	/**
	 * Sets the cards that the player discarded during the mulligan phase.
	 *
	 * @param player
	 * @param begins
	 * @param discardedCards
	 */
	@Suspendable
	public void handleMulligan(Player player, boolean begins, List<Card> discardedCards) {
		// Get the entity ids of the discarded cards and then replace the discarded cards with them
		final Map<Integer, Entity> setAsideZone = player.getSetAsideZone().stream().collect(Collectors.toMap(Entity::getId, Function.identity()));
		discardedCards = discardedCards.stream().map(Card::getId).map(setAsideZone::get).map(e -> (Card) e).collect(toList());

		// The starter cards have been put into the setAsideZone
		List<Card> starterCards = player.getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
		int numberOfStarterCards = begins ? getStarterCards() : getStarterCards() + getSecondPlayerBonusStarterCards();

		// remove player selected cards from starter cards
		for (Card discardedCard : discardedCards) {
			starterCards.removeIf(c -> c.getId() == discardedCard.getId());
		}

		// draw random cards from deck until required starter card count is
		// reached
		while (starterCards.size() < numberOfStarterCards) {
			if (player.getDeck().isEmpty()) {
				break;
			}
			Card randomCard = getRandom(player.getDeck().filtered(card -> !card.hasAttribute(Attribute.NEVER_MULLIGANS)));
			player.getDeck().move(randomCard, player.getSetAsideZone());
			starterCards.add(randomCard);
		}

		// put the mulligan cards back in the deck
		for (Card discardedCard : discardedCards) {
			player.getSetAsideZone().move(discardedCard, player.getDeck());
		}

		for (Card starterCard : starterCards) {
			if (starterCard != null) {
				starterCard.setAttribute(Attribute.STARTED_IN_HAND);
				receiveCard(player.getId(), starterCard);
			}
		}

		// This is the SECOND time the deck gets shuffled. It is first shuffled before the mulligan.
		if (!player.getDeck().isEmpty()) {
			player.getDeck().shuffle(getRandom());
		}

		// Assign the attribute indicating the starting index of these cards AFTER the second shuffle
		for (int i = 0; i < player.getDeck().size(); i++) {
			player.getDeck().get(i).setAttribute(Attribute.STARTING_INDEX, i);
		}

		// second player gets specified bonus cards in this format
		if (!begins) {
			for (String cardId : context.getDeckFormat().getSecondPlayerBonusCards()) {
				receiveCard(player.getId(), context.getCardById(cardId));
			}
		}
	}

	protected int getSecondPlayerBonusStarterCards() {
		return 1;
	}

	/**
	 * Performs a game action, or a selection of what to do by a player from a list of {@link #getValidActions(int)}.
	 * <p>
	 * This method is the primary entry point to turn a player's selected {@link GameAction} into modified game state.
	 * Typically this method will call the action's {@link GameAction#execute(GameContext, int)} overrider, and the {@link
	 * GameAction} will then call {@link GameLogic} methods again to do its business. This is a bit of a rigamarole and
	 * should probably be changed.
	 *
	 * @param playerId The player performing the game action.
	 * @param action   The game action to perform.
	 * @see #getValidActions(int) for the way the {@link GameLogic} determines what actions a player can take.
	 * @see Card#play() for an example of how a card generates a {@link PlayCardAction} that will eventually be sent to
	 * this method.
	 * @see SpellUtils#discoverCard(GameContext, Player, Entity, SpellDesc, CardList) for an example of how a discover
	 * mechanic generates a {@link DiscoverAction} that gets sent to this method.
	 * @see Minion#getBattlecries() for the method that creates battlecry actions. (Note: Deathrattles never involve a
	 * player decision, so deathrattles never generate a battlecry).
	 */
	@Suspendable
	public void performGameAction(int playerId, GameAction action) {
		programCounter = 0;
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("GameLogic/performGameAction")
				.withTag("gameId", context.getGameId())
				.withTag("action.id", action.getId())
				.withTag("action.actionType", action.getActionType().toString())
				.withTag("action.description", action.getDescription(context, playerId))
				.asChildOf(context.getSpanContext())
				.start();
		logger.debug("performGameAction {} {}: {}", context.getGameId(), playerId, action.getDescription(context, playerId));
		try (Scope s1 = tracer.activateSpan(span)) {
			context.onWillPerformGameAction(playerId, action);
			if (playerId != context.getActivePlayerId()) {
				logger.info("Player {} tries to perform an action, but it is not his turn!", context.getPlayer(playerId).getName());
			}
			if (action.getTargetRequirement() != TargetSelection.NONE) {
				Entity target = context.resolveSingleTarget(action.getTargetReference());
				if (target != null) {
					context.getEnvironment().put(Environment.TARGET, target.getReference());
				} else {
					context.getEnvironment().put(Environment.TARGET, null);
				}
			}

			if (logger.isTraceEnabled()) {
				logger.trace("performGameAction {} {}: {}", context.getGameId(), playerId, action.getDescription(context, playerId));
			}

			action.execute(context, playerId);

			context.getEnvironment().remove(Environment.TARGET);
			if (action.getActionType() != ActionType.BATTLECRY) {
				endOfSequence();
			}

			// Calculate how all the entities changed.
			context.onDidPerformGameAction(playerId, action);
		} finally {
			span.finish();
		}
	}

	/**
	 * Determines whether the specified card, from this player's point of view, costs health, due to various effects on
	 * the board.
	 *
	 * @param player The {@link Player} who would play the card.
	 * @param card   The {@link Card}
	 * @return {@code true} if this card costs health, otherwise {@code false}.
	 */
	public boolean doesCardCostHealth(Player player, Card card) {
		final boolean cardCostsHealthAttribute = card.hasAttribute(Attribute.COSTS_HEALTH_INSTEAD_OF_MANA)
				|| card.hasAttribute(Attribute.AURA_COSTS_HEALTH_INSTEAD_OF_MANA);
		final boolean spellsCostHealthCondition = isCardType(card.getCardType(), CardType.SPELL)
				&& hasAttribute(player, Attribute.SPELLS_COST_HEALTH);
		final boolean murlocsCostHealthCondition = Race.hasRace(context, card, Race.MURLOC)
				&& hasAttribute(player, Attribute.MURLOCS_COST_HEALTH);
		final boolean minionsCostHealthCondition = isCardType(card.getCardType(), CardType.MINION)
				&& hasAttribute(player, Attribute.MINIONS_COST_HEALTH);
		return spellsCostHealthCondition
				|| murlocsCostHealthCondition
				|| minionsCostHealthCondition
				|| cardCostsHealthAttribute;
	}

	/**
	 * Plays a card.
	 * <p>
	 * Playing a card from the hand moves it to the graveyard before its effects are resolved. This means its enchantments
	 * are removed.
	 * <p>
	 * A card is marked as played (by setting its turn played as {@link Attribute#PLAYED_FROM_HAND_OR_DECK} before its
	 * effects are resolved. The card is given {@link Attribute#BEING_PLAYED}, then its effects are evaluated, then the
	 * attribute is removed.
	 * <p>
	 * {@link #playCard(int, EntityReference, EntityReference)} is always initiated by an action, like a {@link
	 * PlayCardAction}. It represents playing a card from the hand. This method then deducts the appropriate amount of
	 * mana (or health, depending on the card). Then, it will check if the {@link Card} was countered by Counter Spell (a
	 * {@link Secret} which adds a {@link Attribute#COUNTERED} attribute to the card that was raised in the {@link
	 * CardPlayedEvent}). It applies the {@link Attribute#OVERLOAD} amount to the mana the player has locked next turn.
	 * Finally, it removes the card from the player's {@link Zones#HAND} and puts it in the {@link Zones#GRAVEYARD}.
	 * <p>
	 * The actual effects of the card are evaluated in {@link PlayCardAction#innerExecute(GameContext, int)} overloads.
	 *
	 * @param playerId        The player that is playing the card.
	 * @param cardReference   The card that got played.
	 * @param targetReference
	 */
	@Suspendable
	public void playCard(int playerId, EntityReference cardReference, EntityReference targetReference) {
		Player player = context.getPlayer(playerId);
		Card card = (Card) context.resolveSingleTarget(cardReference);
		Entity target = targetReference != null ? context.resolveSingleTarget(targetReference) : null;
		card.setAttribute(Attribute.BEING_PLAYED);
		int modifiedManaCost = getModifiedManaCost(player, card);
		boolean cardCostsHealth = doesCardCostHealth(player, card);
		List<CardCostInsteadAura> costAuras = SpellUtils.getAuras(context, playerId, CardCostInsteadAura.class);
		boolean cardCostOverridden = costAuras.size() > 0 && costAuras.stream().anyMatch(aura -> aura.getAffectedEntities().contains(cardReference.getId()));

		// The modified mana cost already reflects the invoke cost
		if (canActivateInvokeKeyword(player, card)) {
			card.setAttribute(Attribute.INVOKED, modifiedManaCost);
		}

		if (cardCostOverridden) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			// Only play the last card cost override whose condition was met. Reverse order of play seems more intuitive here.
			Collections.reverse(costAuras);
			boolean paid = false;
			for (CardCostInsteadAura aura : costAuras) {
				if (!aura.getAffectedEntities().contains(cardReference.getId()) && !aura.getAffectedEntities().contains(playerId)) {
					continue;
				}

				if (aura.getCanAffordCondition().isFulfilled(context, player, card, card)) {
					castSpell(playerId, aura.getPayEffect(), card.getReference(), card.getReference(), TargetSelection.NONE, true, null);
					paid = true;
					break;
				}
			}
			if (!paid) {
				throw new UnsupportedOperationException("A card cost was overridden, successfully played but could not actually get its cost paid.");
			}
		} else if (cardCostsHealth) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		} else {
			context.getEnvironment().put(Environment.LAST_MANA_COST, modifiedManaCost);
			modifyCurrentMana(playerId, -modifiedManaCost, true);
			player.getStatistics().manaSpent(modifiedManaCost);
		}

		player.getStatistics().cardPlayed(card, context.getTurn());
		card.setAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK, context.getTurn());
		card.setAttribute(Attribute.MANA_SPENT, modifiedManaCost);
		card.setAttribute(Attribute.HAND_INDEX, card.getEntityLocation().getIndex());
		CardPlayedEvent cardPlayedEvent = new CardPlayedEvent(context, playerId, card);
		context.setLastCardPlayed(playerId, card.getReference());
		if (card.getCardType() == CardType.SPELL) {
			context.setLastSpellPlayedThisTurn(playerId, card.getReference());
		}
		context.fireGameEvent(cardPlayedEvent);

		if (card.hasAttribute(Attribute.OVERLOAD)) {
			// Implements Electra Stormsurge w/ Overload spells
			if (spellsCastTwice(player, card, target)) {
				context.fireGameEvent(new OverloadEvent(context, playerId, card, card.getAttributeValue(Attribute.OVERLOAD)));
			}
			context.fireGameEvent(new OverloadEvent(context, playerId, card, card.getAttributeValue(Attribute.OVERLOAD)));
		}

		// Move the played card to the set aside zone. After its effects are evaluated, it is moved to the graveyard.
		card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		// Passive triggers are still active here, but it's not clear if it matters (a card may transform this way like a Spellstone)

		if ((isCardType(card.getCardType(), CardType.SPELL))) {
			GameEvent spellCastedEvent = new SpellCastedEvent(context, playerId, card, target);
			// Silencing a card here means its effects should not be executed
			context.fireGameEvent(spellCastedEvent);
			if (card.hasAttribute(Attribute.COUNTERED)) {
				return;
			}
		}

		if (card.hasAttribute(Attribute.OVERLOAD)) {
			// Implements Electra Stormsurge w/ Overload spells
			if (spellsCastTwice(player, card, target)) {
				player.modifyAttribute(Attribute.OVERLOAD, card.getAttributeValue(Attribute.OVERLOAD));
				player.modifyAttribute(Attribute.OVERLOADED_THIS_GAME, card.getAttributeValue(Attribute.OVERLOAD));
			}
			player.modifyAttribute(Attribute.OVERLOAD, card.getAttributeValue(Attribute.OVERLOAD));
			// Implements Snowfury Giant
			player.modifyAttribute(Attribute.OVERLOADED_THIS_GAME, card.getAttributeValue(Attribute.OVERLOAD));
		}
	}

	/**
	 * Determines if spells should be casting twice. Allows auras to control double spell casting.
	 *
	 * @param player The player casting the spell
	 * @param card   The card that is the spell being cast
	 * @param target The spell's target, if there is one
	 * @return
	 */
	public boolean spellsCastTwice(Player player, Card card, Entity target) {
		boolean playerHasAttribute = context.getLogic().hasAttribute(player, Attribute.SPELLS_CAST_TWICE);
		boolean playerHasAura = false;
		if (card != null) {
			for (SpellsCastTwiceAura aura : SpellUtils.getAuras(context, SpellsCastTwiceAura.class, card)) {
				if (aura.isFulfilled(context, player, card, target)) {
					playerHasAura = true;
					break;
				}
			}
		}

		return playerHasAttribute || playerHasAura;
	}

	/**
	 * Play a secret.
	 *
	 * @param player The player initiating the play.
	 * @param secret The secret the player wants to play.
	 * @see #playSecret(Player, Secret, boolean) for the complete rules.
	 */
	@Suspendable
	public void playSecret(Player player, Secret secret) {
		playSecret(player, secret, true);
	}

	/**
	 * Plays a secret.
	 * <p>
	 * Takes a {@link Secret} entity, assigns it an ID, configures its trigger listening and adds it to the player's
	 * {@link Zones#SECRET} zone.
	 * <p>
	 * The caller is responsible for enforcing that fewer than {@link #MAX_SECRETS} are in play; that only distinct
	 * secrets are active; and, that the {@link Card} is discarded. The {@link SecretPlayedEvent} is not censored here and
	 * has sensitive information that cannot be shown to the opponent.
	 *
	 * @param player   The player whose gaining the secret.
	 * @param secret   The secret being played.
	 * @param fromHand When {@code true}, a {@link SecretPlayedEvent} is fired; otherwise, the event is not fired.
	 * @see net.demilich.metastone.game.spells.AddSecretSpell the place where secret entities are created. A {@link Card}
	 * uses this spell to actually create a {@link Secret}.
	 */
	@Suspendable
	public void playSecret(Player player, Secret secret, boolean fromHand) {
		secret = secret.clone();
		secret.setId(generateId());
		secret.setOwner(player.getId());
		secret.moveOrAddTo(context, Zones.SECRET);
		addGameEventListener(player, secret, secret);
		if (fromHand) {
			context.fireGameEvent(new SecretPlayedEvent(context, player.getId(), secret.getSourceCard()));
		}
	}

	/**
	 * Modifies the target selection of the specified action and returns it. Respects {@link TargetSelectionOverrideAura}
	 * entities that affect the {@link GameAction#getSourceReference()} of the provided action.
	 *
	 * @param action
	 * @return
	 */
	@Suspendable
	public GameAction processTargetModifiers(GameAction action) {
		Entity entity = action.getSource(context);
		List<TargetSelectionOverrideAura> auras = SpellUtils.getAuras(context, entity.getOwner(), TargetSelectionOverrideAura.class);
		if (!auras.isEmpty()) {
			for (TargetSelectionOverrideAura aura : auras) {
				aura.processTargetModification(entity, action);
			}
		}

		return action;
	}

	/**
	 * Gets a random number.
	 *
	 * @param max Upper bound of random number (exclusive)
	 * @return Random number between 0 and max (exclusive)
	 */
	public int random(int max) {
		return getRandom().nextInt(max);
	}

	/**
	 * Choose a random item from a list of options.
	 *
	 * @param options Options items
	 * @param <T>     Typically entities or other
	 * @return {@code null} if the options array was of length zero, otherwise a choice.
	 */
	public <T> T getRandom(List<T> options) {
		if (options.size() == 0) {
			return null;
		}
		return options.get(getRandom().nextInt(options.size()));
	}

	/**
	 * Choose and remove a random item from a list of options
	 *
	 * @param options A list of items / options  to choose from
	 * @param <T>     The item type
	 * @return An item returned from the options, or {@code null} if there were no options.
	 */
	public <T> T removeRandom(List<T> options) {
		if (options.size() == 0) {
			return null;
		}
		return options.remove(getRandom().nextInt(options.size()));
	}

	/**
	 * Choose and remove a random item from a weighted list of options
	 *
	 * @param weightedOptions A map of weights to items to choose from
	 * @param <T>             The item type
	 * @return An item returned from the weighted options, or {@code null} if there were no options.
	 */
	public <T> T removeRandom(Multiset<T> weightedOptions) {
		if (weightedOptions.size() == 0) {
			return null;
		}

		// Still faster than creating and removing copies from a list
		int index = getRandom().nextInt(weightedOptions.size());
		Iterator<T> iterator = weightedOptions.iterator();
		while (index > 0) {
			if (Strand.currentStrand().isInterrupted()) {
				break;
			}
			iterator.next();
			index--;
		}
		T item = iterator.next();
		weightedOptions.remove(item);
		return item;
	}

	/**
	 * Gets a random boolean value.
	 *
	 * @return A random boolean.
	 */
	public boolean randomBool() {
		return getRandom().nextBoolean();
	}

	/**
	 * Receives a card into the player's hand.
	 *
	 * @param playerId The player receiving the card.
	 * @param card     The card to receive.
	 * @see #receiveCard(int, Card, Entity, boolean) for more complete rules.
	 */
	@Suspendable
	public void receiveCard(int playerId, Card card) {
		receiveCard(playerId, card, null);
	}

	/**
	 * Receives a card into the player's hand.
	 *
	 * @param playerId The player receiving the card.
	 * @param card     The card to receive.
	 * @param copies   The number of copies of this card to receive.
	 * @see #receiveCard(int, Card, Entity, boolean) for more complete rules.
	 */
	@Suspendable
	public void receiveCard(int playerId, Card card, int copies) {
		for (int i = 0; i < Math.min(copies, 1); i++) {
			receiveCard(playerId, card, null);
		}

		for (int i = 1; i < copies; i++) {
			receiveCard(playerId, card.getCopy(), null);
		}
	}

	/**
	 * Receives a card into the player's hand.
	 *
	 * @param playerId The player receiving the card.
	 * @param card     The card to receive.
	 * @param source   The {@link Entity} that caused the card to be received, or {@code null} if this is due to drawing a
	 *                 card at the beginning of a turn.
	 * @see #receiveCard(int, Card, Entity, boolean) for more complete rules.
	 */
	@Suspendable
	public void receiveCard(int playerId, Card card, Entity source) {
		receiveCard(playerId, card, source, false);
	}

	/**
	 * Receives a card into the player's hand, as though it was drawn. It moves a card from whatever current {@link Zones}
	 * zone it is in into the {@link Zones#HAND} zone. Implements the "Draw a card" text.
	 * <p>
	 * A card draw effect is an effect which causes the player to draw one or more cards directly from their deck. Cards
	 * with card draw effects are sometimes called "cantrips", after similar effects in other games.
	 * <p>
	 * Card draw effects are distinguished from generate effects, which place new cards into your hand without removing
	 * them from your deck; and from put into hand and put into battlefield effects, which place cards of a specific type
	 * into the hand or the battlefield directly from the player's deck, rather than simply drawing the next card in the
	 * deck. A few cards have special effects which trigger based on the drawing of cards.
	 * <p>
	 * Attempting to draw a card when you already have 10 cards in your hand will result in the drawn card being removed
	 * from play, something referred to as "overdraw". Overdrawn cards are revealed to both players, before the card is
	 * visually destroyed.
	 * <p>
	 * Overdrawing is similar to discarding, but does not count as a discard for game purposes. While discard effects
	 * remove cards from the hand, overdraw removes the card directly from the deck. Overdraw also does not count as card
	 * draw for game purposes, since the game never attempts to draw the card into the hand, but rather destroys it since
	 * there is no room.
	 * <p>
	 * Card draw effects draw from the top of the randomly ordered deck, unlike "put into battlefield" or "put into hand"
	 * effects, resulting in an even chance of getting any card remaining in the deck. However, it is possible to gain
	 * more control over drawing using Tracking, which gives the player a choice among the top three random draws.
	 *
	 * @param playerId
	 * @param card
	 * @param source
	 * @param isDrawnFromDeck {@code true} if the card was drawn by a deck-drawing process, otherwise {@code false}.
	 * @return The card that was received (the card may have been transformed after it was received).
	 */
	@Suspendable
	public Card receiveCard(int playerId, Card card, Entity source, boolean isDrawnFromDeck) {
		Player player = context.getPlayer(playerId);
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(generateId());
		}

		if (card.getOwner() == IdFactory.UNASSIGNED) {
			card.setOwner(playerId);
		}

		CardZone hand = player.getHand();

		if (hand.getCount() < MAX_HAND_CARDS) {
			// Cards that are received this way should never keep an ephemeral state like choices
			card.getAttributes().remove(Attribute.CHOICES);
			// Forget that the card was invoked
			card.getAttributes().remove(Attribute.INVOKED);
			// Should have included if the card was discarded
			card.getAttributes().remove(Attribute.DISCARDED);
			// Clears the played from hand status
			card.getAttributes().remove(Attribute.PLAYED_FROM_HAND_OR_DECK);
			// Clears the roasted status
			card.getAttributes().remove(Attribute.ROASTED);
			// No mana has been spent yet
			card.getAttributes().remove(Attribute.MANA_SPENT);
			// Does not retain hand index history
			card.getAttributes().remove(Attribute.HAND_INDEX);
			// Clears countered
			card.getAttributes().remove(Attribute.COUNTERED);
			// Put triggers/enchantments into play that have not yet been put into play. Passive indicates active inside the
			// hand.
			processGameTriggers(player, card);
			processPassiveTriggers(player, card);
			processPassiveAuras(player, card);
			card.getAttributes().put(Attribute.RECEIVED_ON_TURN, context.getTurn());
			card.moveOrAddTo(context, Zones.HAND);
			if (isDrawnFromDeck) {
				player.getStatistics().cardDrawn();
			}
			context.fireGameEvent(new DrawCardEvent(context, playerId, card, isDrawnFromDeck));
		} else {
			discardCard(player, card);
		}
		return (Card) card.transformResolved(context);
	}

	/**
	 * Process the {@link Trigger} specified as the passive trigger on a card.
	 *
	 * @param player The player who should own the trigger.
	 * @param card   The card with the trigger.
	 */
	@Suspendable
	public void processPassiveTriggers(Player player, Card card) {
		if (card.getPassiveTriggers() != null
				&& card.getPassiveTriggers().length > 0) {
			for (EnchantmentDesc enchantmentDesc : card.getPassiveTriggers()) {
				addEnchantmentOnce(player, card, enchantmentDesc);
			}
		}
	}

	@Suspendable
	public void processGameTriggers(Player player, Entity entity) {
		if (entity.getGameTriggers() != null
				&& entity.getGameTriggers().length > 0) {
			for (EnchantmentDesc enchantmentDesc : entity.getGameTriggers()) {
				addEnchantmentOnce(player, entity, enchantmentDesc);
			}
		}
	}

	/**
	 * Adds an enchantment to the specified host entity only once.
	 * <p>
	 * Checks to see if there are existing triggers on this {@code entity} whose source card is the same as the {@code
	 * entity}'s source card. If there is, that trigger is not added.
	 *
	 * @param player
	 * @param entity
	 * @param enchantmentDesc
	 */
	@Suspendable
	public void addEnchantmentOnce(Player player, Entity entity, EnchantmentDesc enchantmentDesc) {
		Stream<Enchantment> existingTriggers = context.getTriggersAssociatedWith(entity.getReference())
				.stream()
				.filter(t -> Enchantment.class.isAssignableFrom(t.getClass()))
				.map(t -> (Enchantment) t);

		if (existingTriggers.anyMatch(t -> t.getSourceCard().getCardId().equals(entity.getSourceCard() != null ? entity.getSourceCard().getCardId() : null)
				&& ((t.getSpell() == null || enchantmentDesc.spell == null) || Objects.equals(t.getSpell().getDescClass(), enchantmentDesc.spell.getDescClass())))) {
			return;
		}

		Enchantment enchantment = enchantmentDesc.create();
		enchantment.setSourceCard(entity.getSourceCard());
		addGameEventListener(player, enchantment, entity);
	}

	/**
	 * Refreshes the number of attacks an {@link Actor} has, typically to 1 or the number of {@link Attribute#WINDFURY}
	 * attacks if the actor has Windfury.
	 *
	 * @param entity
	 */
	public void refreshAttacksPerRound(Entity entity) {
		int attacks = 1;
		if (entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			attacks = MEGA_WINDFURY_ATTACKS;
		} else if (entity.hasAttribute(Attribute.WINDFURY) || entity.hasAttribute(Attribute.AURA_WINDFURY)) {
			attacks = WINDFURY_ATTACKS;
		}
		entity.setAttribute(Attribute.NUMBER_OF_ATTACKS, attacks);
	}

	/**
	 * Removes an attribute from an entity. Handles removing {@link Attribute#WINDFURY} and its impact on the number of
	 * attacks a minion can make.
	 *
	 * @param entity The entity to remove an attribute from.
	 * @param source The source of this attribute removal effect
	 * @param attr   The attribute to remove.
	 */
	public void removeAttribute(Entity entity, Entity source, Attribute attr) {
		if (!entity.hasAttribute(attr)) {
			return;
		}

		int sourcePlayerId = source == null ? -1 : source.getOwner();
		if (attr == Attribute.STEALTH) {
			context.fireGameEvent(new LoseStealthEvent(context, entity, sourcePlayerId));
		}

		if (attr == Attribute.DEFLECT) {
			context.fireGameEvent(new LoseDeflectEvent(context, entity, entity.getId(), sourcePlayerId));
		}

		if (attr == Attribute.DIVINE_SHIELD) {
			context.fireGameEvent(new LoseDivineShieldEvent(context, entity, entity.getOwner(), sourcePlayerId));
		}

		if (attr == Attribute.MEGA_WINDFURY && entity.hasAttribute(Attribute.WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - MEGA_WINDFURY_ATTACKS);
		}
		if ((attr == Attribute.WINDFURY || attr == Attribute.AURA_WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - WINDFURY_ATTACKS);
		} else if (attr == Attribute.MEGA_WINDFURY) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - MEGA_WINDFURY_ATTACKS);
		}
		entity.getAttributes().remove(attr);
	}

	/**
	 * Moves a card to the {@link Zones#GRAVEYARD}. Removes each {@link Enchantment} associated with the card, if any.
	 * <p>
	 * No events are raised.
	 * <p>
	 * Also removes all attributes added to the card that did not appear on the text.
	 *
	 * @param card The card to move to the graveyard.
	 * @see #discardCard(Player, Card) for when cards are discarded from the hand or should otherwise raise events.
	 */
	@Suspendable
	public void removeCard(Card card) {
		removeEnchantments(card);
		// If it's already in the graveyard, do nothing more
		if (card.getEntityLocation().getZone() == Zones.GRAVEYARD
				|| card.getEntityLocation().getZone() == Zones.REMOVED_FROM_PLAY) {
			return;
		}
		card.moveOrAddTo(context, Zones.GRAVEYARD);
	}

	/**
	 * Removes an actor by moving it to...
	 *
	 * <ul> <li>The {@link Zones#GRAVEYARD} if the actor is being removed {@code peacefully == false}. Also marks it
	 * {@link Attribute#DESTROYED}.</li> <li>The {@link Zones#SET_ASIDE_ZONE} if the actor is being removed {@code
	 * peacefully == true}. The caller is responsible for moving it elsewhere.</li> </ul>
	 * <p>
	 * Deathrattles are not triggered.
	 *
	 * @param actor      The actor to remove.
	 * @param peacefully If {@code true}, remove the card typically due to a {@link ReturnTargetToHandSpell}--that is, not
	 *                   due to a destruction of the minion. Otherwise, move the {@link Minion} to the {@link
	 *                   Zones#SET_ASIDE_ZONE} where it will be found by {@link #endOfSequence()}.
	 * @see ReturnTargetToHandSpell for usage of {@link #removeActor(Actor, boolean)}. Note, this and {@link
	 * net.demilich.metastone.game.spells.ShuffleMinionToDeckSpell} appear to be the only two users of this function.
	 */
	@Suspendable
	public void removeActor(Actor actor, boolean peacefully) {
		if (actor instanceof Weapon
				&& peacefully) {
			// Move the weapon directly to the graveyard and don't trigger its deathrattle.
			// Also remove its enchantments!
			removeEnchantments(actor);
			actor.moveOrAddTo(context, Zones.GRAVEYARD);
		} else {
			actor.setAttribute(Attribute.DESTROYED);
			actor.moveOrAddTo(context, peacefully ? Zones.SET_ASIDE_ZONE : Zones.GRAVEYARD);
		}
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Removes all the secrets for the player.
	 * <p>
	 * This implements Eater of Secrets, Flare and Visibility Machine.
	 *
	 * @param player The players whose secrets must be removed.
	 */
	@Suspendable
	public void removeSecrets(Player player) {
		for (Secret secret : new ArrayList<>(player.getSecrets())) {
			removeEnchantments(secret);
			secret.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		}
	}

	@Suspendable
	public void removeEnchantments(Entity entity) {
		removeEnchantments(entity, true, false, false);
	}

	@Suspendable
	private void removeEnchantments(Entity entity, boolean removeAuras, boolean keepSelfCardCostModifiers, boolean removeAddedDeathrattles) {
		EntityReference entityReference = entity.getReference();
		// Remove all card enchantments
		if (entity.getEntityType() == EntityType.CARD) {
			for (Attribute cardEnchantmentAttribute : Attribute.getCardEnchantmentAttributes()) {
				entity.getAttributes().remove(cardEnchantmentAttribute);
			}
		}

		if (removeAddedDeathrattles) {
			// Remove all the deathrattles that were added as part of other effects.
			if (entity instanceof HasDeathrattleEnchantments) {
				HasDeathrattleEnchantments hasDeathrattleEnchantments = (HasDeathrattleEnchantments) entity;
				hasDeathrattleEnchantments.clearAddedDeathrattles();
			}
		}

		for (Trigger trigger : context.getTriggersAssociatedWith(entityReference)) {
			if (!removeAuras && trigger instanceof Aura) {
				continue;
			}
			if (keepSelfCardCostModifiers && trigger instanceof CardCostModifier && ((CardCostModifier) trigger).targetsSelf()) {
				continue;
			}
			trigger.onRemove(context);
		}
		context.removeTriggersAssociatedWith(entityReference, removeAuras, keepSelfCardCostModifiers);
	}

	protected void transferKeptEnchantments(Card oldCard, Card newCard) {
		transferTriggers(oldCard, newCard, t -> t instanceof Enchantment && ((Enchantment) t).isKeptAfterTransform());
	}

	protected void transferTriggers(Entity oldEntity, Entity newEntity, Predicate<Trigger> predicate) {
		context.getTriggersAssociatedWith(oldEntity.getReference())
				.stream()
				.filter(predicate)
				.forEach(e -> e.setHost(newEntity));
	}

	/**
	 * Replaces the specified old card with the specified new card. Deals with cards that have {@link
	 * Attribute#DECK_TRIGGERS} correctly.
	 *
	 * @param playerId The player whose {@link Zones#DECK} will be manipulated.
	 * @param oldCard  The old {@link Card} to find and replace in this deck.
	 * @param newCard  The replacement card.
	 */
	@Suspendable
	public Card replaceCard(int playerId, Card oldCard, Card newCard) {
		return replaceCard(playerId, oldCard, newCard, true);
	}

	/**
	 * Replaces the specified old card with the specified new card. Deals with cards that have {@link
	 * Attribute#DECK_TRIGGERS} correctly.
	 *
	 * @param playerId              The player whose {@link Zones#DECK} will be manipulated.
	 * @param oldCard               The old {@link Card} to find and replace in this deck.
	 * @param newCard               The replacement card.
	 * @param keepCardCostModifiers If {@code true}, keeps card cost modifiers hosted by the old card, setting the host to
	 *                              the new card.
	 */
	@Suspendable
	public Card replaceCard(int playerId, Card oldCard, Card newCard, boolean keepCardCostModifiers) {
		Player player = context.getPlayer(playerId);
		@SuppressWarnings("unchecked")
		EntityZone<? super Card> zone = (EntityZone<? super Card>) player.getZone(oldCard.getZone());
		if (zone == null) {
			throw new ClassCastException(String.format("replaceCard must be called on entities in a zone that can accept cards, which is not a %s", oldCard.getZone()));
		}

		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(generateId());
		}
		if (newCard.getOwner() == Entity.NO_OWNER) {
			newCard.setOwner(oldCard.getOwner());
		}

		if (zone.stream().noneMatch(c -> c.getId() == oldCard.getId())) {
			throw new IllegalArgumentException("Cannot replace a card that doesn't currently exist.");
		}

		final int oldIndex = oldCard.getEntityLocation().getIndex();
		transferKeptEnchantments(oldCard, newCard);
		if (keepCardCostModifiers) {
			transferTriggers(oldCard, newCard, CardCostModifier.class::isInstance);
		}
		removeEnchantments(oldCard);
		oldCard.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		oldCard.getAttributes().put(Attribute.TRANSFORM_REFERENCE, newCard.getReference());
		zone.add(oldIndex, newCard);

		processGameTriggers(player, newCard);
		if (zone.getZone() == Zones.HAND) {
			processPassiveTriggers(player, newCard);
			processPassiveAuras(player, newCard);
			context.fireGameEvent(new DrawCardEvent(context, playerId, newCard, false));
		} else if (zone.getZone() == Zones.DECK) {
			processDeckTriggers(player, newCard);
		}

		return newCard;
	}

	/**
	 * Resolves an {@link Actor}'s battlecry, requesting an action from the player's {@link
	 * net.demilich.metastone.game.behaviour.Behaviour} if necessary.
	 *
	 * @param playerId
	 * @param actor
	 * @return
	 */
	@Suspendable
	public BattlecryAction[] resolveBattlecries(int playerId, Actor actor) {
		List<BattlecryDesc> battlecries = actor.getBattlecries();
		BattlecryAction[] battlecryActions = new BattlecryAction[battlecries.size()];
		for (int i = 0; i < battlecryActions.length; i++) {
			BattlecryAction battlecry = battlecries.get(i).toBattlecryAction();
			battlecry.setSourceReference(actor.getReference());
			Player player = context.getPlayer(playerId);
			if (battlecry.shouldOverrideTargetSelection(context, player, actor)) {
				battlecry.setTargetRequirement(battlecry.getTargetSelectionOverride());
			}
			processTargetModifiers(battlecry);
			if (!battlecry.canBeExecuted(context, player)) {
				battlecryActions[i] = BattlecryAction.NONE;
				continue;
			}

			battlecry.setSourceReference(actor.getReference());

			if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
				List<GameAction> battlecryActionChoices = getTargetedBattlecryGameActions(battlecry, player);

				if (battlecryActionChoices == null
						|| battlecryActionChoices.size() == 0) {
					battlecryActions[i] = BattlecryAction.NONE;
					continue;
				}

				BattlecryAction targetedBattlecry = (BattlecryAction) requestAction(player, battlecryActionChoices);
				performBattlecryAction(playerId, actor, player, targetedBattlecry);
				battlecryActions[i] = targetedBattlecry;
			} else {
				performBattlecryAction(playerId, actor, player, battlecry);
				battlecryActions[i] = battlecry;
			}
		}

		return battlecryActions;
	}

	/**
	 * Requests an action internally, allowing special request logic to be executed.
	 *
	 * @param player  The player whose {@link net.demilich.metastone.game.behaviour.Behaviour} will be queried for an
	 *                action.
	 * @param actions The possible actions.
	 * @return The chosen action
	 */
	@Suspendable
	public GameAction requestAction(Player player, List<GameAction> actions) {
		if (actions == null
				|| actions.size() == 0) {
			throw new NullPointerException("No actions specified");
		}
		for (int i = 0; i < actions.size(); i++) {
			actions.get(i).setId(i);
		}

		GameAction action;
		if (hasAttribute(player, Attribute.RANDOM_CHOICES)) {
			action = getRandom(actions);
		} else {
			action = context.getBehaviours().get(player.getId()).requestAction(context, player, actions);
			// Only add the action to the trace if it represents a real choice by the user
			context.getTrace().addAction(action);
		}

		if (action == null) {
			throw new NullPointerException("Behaviour did not return action");
		}
		return action;
	}

	protected List<GameAction> getTargetedBattlecryGameActions(BattlecryAction battlecry, Player player) {
		List<Entity> validTargets = targetLogic.getValidTargets(context, player, battlecry);
		if (validTargets.isEmpty()) {
			return null;
		}

		List<GameAction> battlecryActions = new ArrayList<>();
		for (Entity validTarget : validTargets) {
			GameAction targetedBattlecry = battlecry.clone();
			targetedBattlecry.setTarget(validTarget);
			battlecryActions.add(targetedBattlecry);
		}
		return battlecryActions;
	}

	@Suspendable
	protected void performBattlecryAction(int playerId, Actor actor, Player player, BattlecryAction battlecryAction) {
		if (battlecryAction.getChooseOneOptionIndex() != null) {
			// Add an attribute to the actor's source card, if it exists, specifying which action was taken
			int choice = battlecryAction.getChooseOneOptionIndex();
			if (actor.getSourceCard() != null) {
				actor.getSourceCard().getAttributes().put(Attribute.CHOICE, choice);
			}
			actor.getAttributes().put(Attribute.CHOICE, choice);
		}

		boolean willDouble = false;
		List<DoubleBattlecriesAura> doubleBattlecryAuras = SpellUtils.getAuras(context, actor.getOwner(), DoubleBattlecriesAura.class);
		if (!doubleBattlecryAuras.isEmpty() && actor.hasAttribute(Attribute.BATTLECRY)) {
			for (DoubleBattlecriesAura aura : doubleBattlecryAuras) {
				aura.onGameEvent(new WillEndSequenceEvent(context));
				if (aura.getAffectedEntities().contains(actor.getId())) {
					willDouble = true;
				}
			}
		}
		List<DoubleCombosAura> doubleComboAuras = SpellUtils.getAuras(context, actor.getOwner(), DoubleCombosAura.class);
		if (!doubleComboAuras.isEmpty() && actor.hasAttribute(Attribute.COMBO)) {
			for (DoubleCombosAura aura : doubleComboAuras) {
				if (aura.getAffectedEntities().contains(actor.getId())) {
					willDouble = true;
				}
			}
		}

		if (willDouble) {
			EntityReference target = battlecryAction.getPredefinedSpellTargetOrUserTarget();
			performGameAction(playerId, battlecryAction);
			// Make sure the battlecry is still targetable
			// The target may have transformed
			if (target != null
					&& !target.isTargetGroup()) {
				target = context.resolveSingleTarget(target).transformResolved(context).getReference();
				battlecryAction.setTargetReference(target);
			}
			final EntityReference target1 = target;
			final boolean targetable = target == null
					|| target.isTargetGroup()
					|| getValidTargets(playerId, battlecryAction).stream().map(EntityReference::pointTo).anyMatch(er -> er.equals(target1));
			if (!battlecryAction.canBeExecuted(context, player) || !targetable) {
				return;
			}
			performGameAction(playerId, battlecryAction);
		} else {
			performGameAction(playerId, battlecryAction);
		}
	}

	/**
	 * Executes the deathrattle effect written for this {@link Actor}.
	 *
	 * @param player The player that owns the actor.
	 * @param actor  The actor.
	 */
	@Suspendable
	public void resolveAftermaths(Player player, Actor actor) {
		resolveAftermaths(player, actor, actor.getEntityLocation());
	}

	/**
	 * Executes the deathrattle effect written on this {@link Actor}, wherever it is.
	 *
	 * @param player           The player that owns the actor.
	 * @param actor            The actor.
	 * @param previousLocation The position on the board the actor used to have. Important for adjacency deathrattle
	 */
	@Suspendable
	public void resolveAftermaths(Player player, Actor actor, EntityLocation previousLocation) {
		int boardPosition = previousLocation.getIndex();
		if (!actor.hasAttribute(Attribute.DEATHRATTLES)) {
			return;
		}

		boolean isWeapon = actor instanceof Weapon;
		// Don't trigger deathrattles for entities in the set aside zone... unless it's a weapon
		if (previousLocation.getZone() == Zones.SET_ASIDE_ZONE
				&& !isWeapon) {
			return;
		}

		int playerId = player.getId();
		List<SpellDesc> deathrattles = new ArrayList<>(actor.getDeathrattles());
		EntityReference sourceReference = actor.getReference();
		int sourceOwner = actor.getOwner();

		resolveAftermaths(playerId, sourceReference, deathrattles, sourceOwner, boardPosition);
	}

	/**
	 * Casts a list of deathrattle spells given information about the entity that "hosts" those deathrattles
	 *
	 * @param playerId        The casting player
	 * @param sourceReference A reference to the source
	 * @param deathrattles    The actual deathrattles to cast
	 * @param sourceOwner     The owner of the source
	 * @param boardPosition   The former board position of the source
	 */
	@Suspendable
	public void resolveAftermaths(int playerId, EntityReference sourceReference, List<SpellDesc> deathrattles, int sourceOwner, int boardPosition) {
		resolveAftermaths(playerId, sourceReference, deathrattles, sourceOwner, boardPosition, true);
	}

	/**
	 * Casts a list of deathrattle spells given information about the entity that "hosts" those deathrattles
	 *
	 * @param playerId                         The casting player
	 * @param sourceReference                  A reference to the source
	 * @param deathrattles                     The actual deathrattles to cast
	 * @param sourceOwner                      The owner of the source
	 * @param boardPosition                    The former board position of the source
	 * @param shouldAddToDeathrattlesTriggered {@code true} if the deathrattle should be recorded in the list of triggered
	 *                                         deathrattles
	 */
	@Suspendable
	public void resolveAftermaths(int playerId, EntityReference sourceReference, List<SpellDesc> deathrattles, int sourceOwner, int boardPosition, boolean shouldAddToDeathrattlesTriggered) {
		boolean doubleDeathrattles = false;
		List<DoubleDeathrattlesAura> doubleDeathrattleAuras = SpellUtils.getAuras(context, sourceOwner, DoubleDeathrattlesAura.class);
		if (!doubleDeathrattleAuras.isEmpty()) {
			for (DoubleDeathrattlesAura aura : doubleDeathrattleAuras) {
				if (aura.getAffectedEntities().contains(sourceReference.getId())) {
					doubleDeathrattles = true;
				}
			}
		}

		// TODO: What happens if a deathrattle modifies another deathrattle?
		int id = 0;
		for (SpellDesc deathrattleTemplate : deathrattles) {
			SpellDesc deathrattle = deathrattleTemplate.addArg(SpellArg.BOARD_POSITION_ABSOLUTE, boardPosition);
			deathrattle.put(SpellArg.AFTERMATH_ID, id);
			id++;
			castSpell(playerId, deathrattle, sourceReference, EntityReference.NONE, TargetSelection.NONE, false, null);
			if (doubleDeathrattles) {
				// TODO: Likewise, with double deathrattles, make sure that we can still target whatever we're targeting in the spells (possibly metaspells!)
				castSpell(playerId, deathrattle, sourceReference, EntityReference.NONE, TargetSelection.NONE, true, null);
				if (shouldAddToDeathrattlesTriggered) {
					context.getAftermaths().addAftermath(playerId, sourceReference, deathrattle, context.resolveSingleTarget(sourceReference).getSourceCard().getCardId());
				}
			}
			if (shouldAddToDeathrattlesTriggered) {
				context.getAftermaths().addAftermath(playerId, sourceReference, deathrattle, context.resolveSingleTarget(sourceReference).getSourceCard().getCardId());
			}
		}
	}

	/**
	 * This method is where the {@link GameLogic} handles the firing of a {@link Secret}. It removes the secret from play
	 * and raises a {@link SecretRevealedEvent}.
	 *
	 * @param player The player that owns the secret.
	 * @param secret The secret that got triggered.
	 * @see Secret for the code that handles when a secret is fired.
	 */
	@Suspendable
	public void secretTriggered(Player player, Secret secret) {
		// Move the secret to removed from play.
		removeEnchantments(secret);
		if (secret.isInPlay()) {
			secret.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			context.fireGameEvent(new SecretRevealedEvent(context, secret.getSourceCard(), player.getId()));
		}
	}

	// TODO: circular dependency. Very ugly, refactor!
	public void setContext(GameContext context) {
		this.context = context;
	}

	/**
	 * Inserts a card into the specified location in the player's deck. Use {@link CardZone#size()} as the index for the
	 * top of the deck, and {@code 0} for the bottom.
	 *
	 * @return {@code true} if the card was successfully inserted, {@code false} if the deck was full (size was {@link
	 * #MAX_DECK_SIZE}).
	 */
	@Suspendable
	public boolean insertIntoDeck(Player player, Card card, int index) {
		return insertIntoDeck(player, card, index, false);
	}

	/**
	 * Inserts a card into the specified location in the player's deck. Use {@link CardZone#size()} as the index for the
	 * top of the deck, and {@code 0} for the bottom.
	 *
	 * @param player
	 * @param card
	 * @param index
	 * @param quiet  If {@code true}, does not fire the {@link CardAddedToDeckEvent}.
	 * @return {@code true} if the card was successfully inserted, {@code false} if the deck was full (size was {@code
	 * MAX_DECK_SIZE}).
	 */
	@Suspendable
	public boolean insertIntoDeck(Player player, Card card, int index, boolean quiet) {
		int count = player.getDeck().getCount();
		if (count < MAX_DECK_SIZE) {
			if (card.getId() == IdFactory.UNASSIGNED) {
				card.setId(generateId());
			}

			if (card.getOwner() == IdFactory.UNASSIGNED) {
				card.setOwner(player.getId());
			}

			if (card.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
				player.getDeck().add(index, card);
			} else {
				card.moveOrAddTo(context, Zones.DECK);
			}

			processGameTriggers(player, card);
			processDeckTriggers(player, card);

			if (!quiet) {
				context.fireGameEvent(new CardAddedToDeckEvent(context, card.getOwner(), player.getId(), card));
			}
			return true;
		} else if (card.getId() != IdFactory.UNASSIGNED) {
			card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		}
		return false;
	}

	/**
	 * @param player    The player whose deck this card is getting shuffled into.
	 * @param card      The card to shuffle into that player's deck.
	 * @param extraCopy If {@code true}, indicates this is an "extra copy" and should not recursively trigger certain
	 *                  kinds of shuffle-copying effects.
	 * @return
	 * @see ShuffleToDeckSpell for the spell that interacts with this function. When its {@link SpellArg#EXCLUSIVE} flag
	 * is {@code true}, {@code quiet} here is {@code true}, making it possible to shuffle cards into the deck without
	 * triggering another shuffle event (e.g., with Augmented Elekk).
	 */
	@Suspendable
	public boolean shuffleToDeck(Player player, Card card, boolean extraCopy) {
		return shuffleToDeck(player, null, card, extraCopy, false, player.getId());
	}

	/**
	 * Implements a "Shuffle into deck" text. This will select a random location for the card to go without shuffling the
	 * deck (i.e., changing the existing order of the cards).
	 * <p>
	 * Removes the enchantments on the card before shuffling it into the deck. This removes card cost modification
	 * enchantments.
	 *
	 * @param player         The player whose deck this card is getting shuffled into.
	 * @param card           The card to shuffle into that player's deck.
	 * @param extraCopy      If {@code true}, indicates this is an "extra copy" and should not recursively trigger certain
	 *                       kinds of shuffle-copying effects.
	 * @param sourcePlayerId The caster of the spell that is executing the shuffleToDeck method.
	 * @see ShuffleToDeckSpell for the spell that interacts with this function. When its {@link SpellArg#EXCLUSIVE} flag
	 * is {@code true}, {@code quiet} here is {@code true}, making it possible to shuffle cards into the deck without
	 * triggering another shuffle event (e.g., with Augmented Elekk).
	 */
	@Suspendable
	public boolean shuffleToDeck(Player player, Card card, boolean extraCopy, int sourcePlayerId) {
		return shuffleToDeck(player, null, card, extraCopy, false, sourcePlayerId);
	}

	/**
	 * @param player                The player whose deck this card is getting shuffled into.
	 * @param relatedEntity         The entity related to the card that is getting shuffled. For example, when shuffling
	 *                              a
	 * @param card                  The card to shuffle into that player's deck.
	 * @param extraCopy             If {@code true}, indicates this is an "extra copy" and should not recursively trigger
	 *                              certain kinds of shuffle-copying effects.
	 * @param keepCardCostModifiers If {@code true}, keeps card cost modifiers whose {@link Enchantment#getHostReference()}
	 *                              is the targeted card and whose {@link net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg#TARGET}
	 *                              is {@link EntityReference#SELF} or exactly the host entity's ID (i.e., self-targeting
	 *                              card cost modifiers).
	 */
	@Suspendable
	public boolean shuffleToDeck(Player player, @Nullable Entity relatedEntity, @NotNull Card card, boolean extraCopy, boolean keepCardCostModifiers) {
		return shuffleToDeck(player, relatedEntity, card, extraCopy, keepCardCostModifiers, player.getId());
	}

	/**
	 * Implements a "Shuffle into deck" text. This will select a random location for the card to go without shuffling the
	 * deck (i.e., changing the existing order of the cards).
	 * <p>
	 * Removes the enchantments on the card before shuffling it into the deck. If {@code keepCardCostModifiers} is {@code
	 * true}, those enchantments will not be removed.
	 *
	 * @param player                The player whose deck this card is getting shuffled into.
	 * @param relatedEntity         The entity related to the card that is getting shuffled. For example, when shuffling a
	 *                              minion ({@link Actor}) to the deck, that minion should be this argument.
	 * @param card                  The card to shuffle into that player's deck.
	 * @param extraCopy             If {@code true}, indicates this is an "extra copy" and should not recursively trigger
	 *                              certain kinds of shuffle-copying effects.
	 * @param keepCardCostModifiers If {@code true}, keeps card cost modifiers whose {@link Enchantment#getHostReference()}
	 *                              is the targeted card and whose {@link net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg#TARGET}
	 *                              is {@link EntityReference#SELF} or exactly the host entity's ID (i.e., self-targeting
	 *                              card cost modifiers).
	 * @param sourcePlayerId        The caster of the spell that is executing the shuffleToDeck method.
	 * @see ShuffleToDeckSpell for the spell that interacts with this function. When its {@link SpellArg#EXCLUSIVE} flag
	 * is {@code true}, {@code quiet} here is {@code true}, making it possible to shuffle cards into the deck without
	 * triggering another shuffle event (e.g., with Augmented Elekk).
	 */
	@Suspendable
	public boolean shuffleToDeck(Player player, @Nullable Entity relatedEntity, @NotNull Card card, boolean extraCopy, boolean keepCardCostModifiers, int sourcePlayerId) {
		int count = player.getDeck().getCount();
		if (count < MAX_DECK_SIZE) {
			if (card.getId() == IdFactory.UNASSIGNED) {
				card.setId(generateId());
			}
			if (card.getOwner() == IdFactory.UNASSIGNED) {
				card.setOwner(player.getId());
			}

			// Remove passive triggers if the card was in a place they were active
			if (card.getZone() == Zones.HAND || card.getZone() == Zones.HERO_POWER) {
				removeEnchantments(card, true, keepCardCostModifiers, false);
			}

			if (count == 0) {
				card.moveOrAddTo(context, Zones.DECK);
			} else {
				card.moveOrAddTo(context, Zones.DECK, getRandom().nextInt(count));
			}
			processGameTriggers(player, card);
			processDeckTriggers(player, card);


			if (relatedEntity != null) {
				context.fireGameEvent(new ShuffledEvent(context, player.getId(), sourcePlayerId, extraCopy, relatedEntity, card));
			} else {
				context.fireGameEvent(new ShuffledEvent(context, player.getId(), sourcePlayerId, extraCopy, card));

			}

			if (card.getZone() == Zones.DECK) {
				context.fireGameEvent(new CardAddedToDeckEvent(context, card.getOwner(), player.getId(), card));
			}

			if (card.getZone() == Zones.DECK) {
				EnvironmentEntityList.getList(context, Environment.SHUFFLED_CARDS_LIST).add(player, card);
			}
			return true;
		} else if (card.getId() != IdFactory.UNASSIGNED) {
			removeEnchantments(card, true, keepCardCostModifiers, false);
			card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		}
		return false;
	}

	/**
	 * Shuffles the specified card into the player's deck.
	 * <p>
	 * Removes all enchantments written on the card, including card cost modifying enchantments. See the overloaded
	 * methods to control this behaviour.
	 *
	 * @param player
	 * @param card
	 * @return {@code true} if the card was successfully shuffled into the player's deck.
	 */
	@Suspendable
	public boolean shuffleToDeck(Player player, Card card) {
		return shuffleToDeck(player, card, false, player.getId());
	}

	/**
	 * Puts into play any deck triggers ({@link CardDesc#getDeckTriggers()} written on the card.
	 * <p>
	 * Deck triggers are active while the card is located in the player's deck.
	 *
	 * @param player
	 * @param card
	 */
	@Suspendable
	public void processDeckTriggers(Player player, Card card) {
		if (card.getDeckTriggers() != null
				&& card.getDeckTriggers().length > 0) {
			for (EnchantmentDesc enchantmentDesc : card.getDeckTriggers()) {
				addEnchantmentOnce(player, card, enchantmentDesc);
			}
		}
	}

	/**
	 * Silence is an ability which removes all current card text, enchantments, and abilities from the targeted minion. It
	 * does not remove damage or minion type.
	 *
	 * @param playerId The ID of the player (typically the owner of the target). This is used by {@link
	 *                 net.demilich.metastone.game.spells.custom.MindControlOneTurnSpell} to reverse the mind control of a
	 *                 minion that somehow gets silenced during the turn that spell is cast.
	 * @param target   A {@link Minion} to silence.
	 * @see <a href="http://hearthstone.gamepedia.com/Silence">Silence</a> for a complete description of the silencing
	 * game rules.
	 */
	@Suspendable
	public void silence(int playerId, Actor target) {
		removeBonusAttributes(target);
		removeEnchantments(target);
		target.setAttribute(Attribute.SILENCED);
		context.fireGameEvent(new SilenceEvent(context, playerId, target));
	}

	/**
	 * Removes all attributes that count as bonuses (essentially baked-in enchantments)
	 *
	 * @param target The target to remove these attributes from.
	 */
	public void removeBonusAttributes(Entity target) {
		List<Attribute> tags = new ArrayList<>();
		tags.addAll(target.getAttributes().keySet());
		for (Attribute attr : tags) {
			if (IMMUNE_TO_SILENCE.contains(attr)) {
				continue;
			}
			removeAttribute(target, null, attr);
		}

		if (target instanceof Actor) {
			Actor actor = (Actor) target;
			int oldMaxHp = actor.getMaxHp();
			actor.setMaxHp(actor.getAttributeValue(Attribute.BASE_HP));
			actor.setAttack(actor.getAttributeValue(Attribute.BASE_ATTACK));
			if (actor.getHp() > actor.getMaxHp()) {
				actor.setHp(actor.getMaxHp());
			} else if (oldMaxHp < actor.getMaxHp()) {
				actor.setHp(actor.getHp() + actor.getMaxHp() - oldMaxHp);
			}
		}
	}

	/**
	 * Starts a turn.
	 * <p>
	 * At the start of each of their turns, the player gains {@link Player#getMaxMana()} (up to a maximum of {@link
	 * #MAX_MANA}), and attempts to draw a card. The player is then free (but not forced) to take an action by playing
	 * cards, using their Hero Power, and/or attacking with their minions or hero. Once all possible actions have been
	 * taken, the "End Turn" button will light up.
	 * <p>
	 * All minions with {@link Attribute#SUMMONING_SICKNESS} will have that attribute cleared; {@link Attribute#OVERLOAD}
	 * will cause the player's {@link Player#getMana()} to decline by {@link Player#getLockedMana()}; and temporary
	 * bonuses like {@link Attribute#TEMPORARY_ATTACK_BONUS} will be lost.
	 *
	 * @param playerId The player that is starting their turn.
	 */
	@Suspendable
	public void startTurn(int playerId) {
		int now = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
		Player player = context.getPlayer(playerId);
		int gameStartTime;
		if (player.getAttributes().containsKey(Attribute.GAME_START_TIME_MILLIS)) {
			gameStartTime = now;
		} else {
			gameStartTime = (int) player.getAttributes().get(Attribute.GAME_START_TIME_MILLIS);
		}

		int startTime = Math.max(now - gameStartTime, 0);
		player.setAttribute(Attribute.MANA_SPENT_THIS_TURN, 0);
		player.getAttributes().put(Attribute.TURN_START_TIME_MILLIS, startTime);

		if (player.getMaxMana() < MAX_MANA) {
			player.setMaxMana(player.getMaxMana() + 1);
		}
		player.getStatistics().startTurn();

		player.setLockedMana(player.getAttributeValue(Attribute.OVERLOAD));
		int mana = Math.min(player.getMaxMana() - player.getLockedMana(), MAX_MANA);
		player.setMana(mana);
		context.fireGameEvent(new MaxManaChangedEvent(context, player.getId(), 1));

		player.getAttributes().remove(Attribute.OVERLOAD);
		player.setAttribute(Attribute.EXTRA_TURN, Math.max(player.getAttributeValue(Attribute.EXTRA_TURN) - 1, 0));

		if (!player.getHeroPowerZone().isEmpty()) {
			player.getHeroPowerZone().get(0).setUsed(0);
		}
		if (!player.getWeaponZone().isEmpty()) {
			player.getWeaponZone().get(0).setActive(true);
		}

		startTurnForEntity(player);
		startTurnForEntity(player.getHero());
		for (Minion minion : player.getMinions()) {
			startTurnForEntity(minion);
		}

		context.fireGameEvent(new TurnStartEvent(context, player.getId()));
		castSpell(playerId, DrawCardSpell.create(), player.getReference(), null, TargetSelection.NONE, true, null);
		endOfSequence();
	}

	/**
	 * Resets turn temporary fields on the specified entity and prepares it for the next turn.
	 *
	 * @param entity
	 */
	protected void startTurnForEntity(Entity entity) {
		if (Entity.hasEntityType(entity.getEntityType(), EntityType.ACTOR)) {
			refreshAttacksPerRound(entity);
			stealthForTurns(entity);
			entity.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
		}

		if (Entity.hasEntityType(entity.getEntityType(), EntityType.MINION)) {
			entity.getAttributes().remove(Attribute.SUMMONING_SICKNESS);
		}

		entity.setAttribute(Attribute.ATTACKS_LAST_TURN, entity.getAttributeValue(Attribute.ATTACKS_THIS_TURN));
		entity.getAttributes().remove(Attribute.ATTACKS_THIS_TURN);
		entity.setAttribute(Attribute.DRAINED_LAST_TURN, entity.getAttributeValue(Attribute.DRAINED_THIS_TURN));
		entity.getAttributes().remove(Attribute.DRAINED_THIS_TURN);
	}

	@Suspendable
	protected void stealthForTurns(Entity actor) {
		if (actor.hasAttribute(Attribute.STEALTH) && actor.hasAttribute(Attribute.STEALTH_FOR_TURNS)) {
			int stealthForTurns = actor.getAttributeValue(Attribute.STEALTH_FOR_TURNS);
			if (stealthForTurns == 1) {
				removeAttribute(actor, null, Attribute.STEALTH);
				removeAttribute(actor, null, Attribute.STEALTH_FOR_TURNS);
			} else {
				actor.setAttribute(Attribute.STEALTH_FOR_TURNS, stealthForTurns - 1);
			}
		}
	}

	/**
	 * Summons a {@link Minion}.
	 * <p>
	 * Playing a minion card places that minion onto the battlefield. This process is known as 'summoning'. Each minion
	 * has a mana cost indicated by {@link Card#getManaCost(GameContext, Player)}, which shows the amount of mana you must
	 * pay to summon the minion.
	 * <p>
	 * Successfully playing a minion card will transform the card into the minion itself, which will then appear upon the
	 * battleground represented by a portrait. Once summoned the minion will stay on the battlefield until it is destroyed
	 * or returned to the hand of its owner. Minions can be destroyed by reducing their hitpoints to zero, or by using
	 * destroy effects such as Assassinate to remove them directly. Note that if a minion is returned to its owner's hand,
	 * or shuffled back into the owner's deck, its Attack and Health will return to their original values, and any
	 * enchantments will be removed. However, transformations will not be reversed by returning a minion to its owner's
	 * hand or deck; the transformed minion is considered an entirely different card from what it was transformed from,
	 * and that is what is "returned" to the deck.
	 * <p>
	 * This method returns {@code false} if the summon failed, typically due to a rule violation. The caller is
	 * responsible for handling a failed summon. Summons can fail because players can normally have a maximum of {@link
	 * #MAX_MINIONS} minions on the battlefield at any time. Once {@link #MAX_MINIONS} friendly minions are on the field,
	 * the player will not be able to summon further minions. Minion cards and summon effects such as Totemic Call will
	 * not be playable, and any minion Battlecries and Deathrattles that summon other minions will be wasted.
	 * <p>
	 * Minions summoned by a summon effect written on a card other than a {@link Card} are not played directly from the
	 * hand, and therefore will not trigger Battlecries or Overload. However, they will work with triggered effects which
	 * respond to the summoning of minions, like {@link MinionSummonedTrigger}.
	 * <p>
	 * A minion's {@link AfterMinionSummonedTrigger} and {@link AfterMinionPlayedTrigger} enchantments <b>will</b> fire
	 * off this minion's summoning. Its {@link MinionSummonedTrigger}, {@link MinionPlayedTrigger}, {@link
	 * BeforeMinionSummonedTrigger} and {@link BeforeMinionPlayedTrigger} enchantments will <b>not</b> trigger off this
	 * minion's summonining.
	 *
	 * @param playerId         The player who will own the minion (not the initiator of the summon, which may be the
	 *                         opponent).
	 * @param minion           The minion to summon. Typically the result of a {@link Card#summon()} call, but other cards
	 *                         have effects that summon minions on e.g., battlecry with a {@link SummonSpell}.
	 * @param source           The {@link Card} or {@link Entity} responsible for summoning this minion. If this is a
	 *                         {@link EntityType#CARD} of {@link CardType#MINION} and the source has the attribute {@link
	 *                         Attribute#PLAYED_FROM_HAND_OR_DECK}, this summoning is considered to have been a minion
	 *                         "played" as opposed to merely summoned.
	 * @param index            The location on the {@link Zones#BATTLEFIELD} to place this minion.
	 * @param resolveBattlecry If {@code true}, the battlecry should be cast. Battlecries are only cast when a {@link
	 *                         Minion} is summoned by a {@link Card} played from the {@link Zones#HAND}.
	 * @return {@code true} if the summoning was successful.
	 */
	@Suspendable
	public boolean summon(int playerId, @NotNull Minion minion, @NotNull Entity source, int index, boolean resolveBattlecry) {
		Player player = context.getPlayer(playerId);

		if (!canSummonMoreMinions(player)) {
			return false;
		}
		minion.setId(generateId());
		minion.setOwner(player.getId());
		minion.setAttribute(Attribute.SUMMONED_ON_TURN, context.getTurn());
		minion.setAttribute(Attribute.SUMMONED_BY_PLAYER, source.getOwner());
		context.getSummonReferenceStack().push(minion.getReference());

		try {
			if (index < 0 || index >= player.getMinions().size()) {
				minion.moveOrAddTo(context, Zones.BATTLEFIELD);
			} else {
				player.getMinions().add(index, minion);
			}

			// After ever event, something may have transformed the minion, so make sure to get the correct entity.
			if (!minion.hasAttribute(Attribute.PERMANENT)) {
				context.fireGameEvent(new BeforeSummonEvent(context, minion, source, false, null));
				minion = summonTransformResolved(minion);
			}

			context.fireGameEvent(new BoardChangedEvent(context));
			minion = summonTransformResolved(minion);
			BattlecryAction[] battlecryActions = new BattlecryAction[0];
			if (resolveBattlecry
					&& !minion.getBattlecries().isEmpty()) {
				battlecryActions = resolveBattlecries(player.getId(), minion);
				minion = summonTransformResolved(minion);
				// A battlecry may have transformed the minion
				endOfSequence();
				minion = summonTransformResolved(minion);
				// A deathrattle may have transformed the minion
			}

			// Anything that gets this far will get its triggers put into play, even if it gets destroyed, because the end
			// of the sequence now comes after the summon
			context.fireGameEvent(new BoardChangedEvent(context));
			minion = summonTransformResolved(minion);

			player.getStatistics().minionSummoned(minion);
			SummonEvent summonEvent;
			if (context.getEnvironment().get(Environment.TARGET_OVERRIDE) != null) {
				Actor actor = (Actor) context.resolveTarget(player, source, (EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE)).get(0);
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				summonEvent = new SummonEvent(context, actor, source, resolveBattlecry, battlecryActions);
			} else {
				summonEvent = new SummonEvent(context, minion, source, resolveBattlecry, battlecryActions);
			}

			if (!summonEvent.getMinion().hasAttribute(Attribute.PERMANENT)) {
				context.fireGameEvent(summonEvent);
				minion = summonTransformResolved(minion);
			}

			if (minion.isInPlay()) {
				newMinionOnBattlefield(minion, player);
			}

			if (player.getMinions().contains(minion)
					&& !minion.hasAttribute(Attribute.PERMANENT)) {
				player.modifyAttribute(Attribute.MINIONS_SUMMONED_THIS_TURN, 1);
				player.modifyAttribute(Attribute.TOTAL_MINIONS_SUMMONED_THIS_TURN, 1);
				context.getOpponent(player).modifyAttribute(Attribute.TOTAL_MINIONS_SUMMONED_THIS_TURN, 1);
				context.fireGameEvent(new AfterSummonEvent(context, minion, source, resolveBattlecry, battlecryActions));
			}
			context.fireGameEvent(new BoardChangedEvent(context));
			return true;
		} finally {
			context.getSummonReferenceStack().pop();
		}
	}

	@Suspendable
	protected void newMinionOnBattlefield(@NotNull Minion minion, Player player) {
		minion.setAttribute(Attribute.SUMMONING_SICKNESS);
		refreshAttacksPerRound(minion);
		processBattlefieldEnchantments(player, minion);

		if (minion.getCardCostModifier() != null) {
			addGameEventListener(player, minion.getCardCostModifier(), minion);
		}

		handleHpChange(minion);
	}


	/**
	 * Combines two minions together using the rules of magnetization.
	 * <p>
	 * From Gamepedia: Magnetic is an ability exclusive to certain Mech minions which allows multiple minions to be merged
	 * together. Playing a Magnetic minion to the left of an existing Mech will automatically cause the two minions' stats
	 * and card text to be combined into a single minion.
	 *
	 * <ul>
	 * <li>A Magnetic card cannot be played if the player's board is full, even to Magnetize onto another minion.[3]</li>
	 * <li>If you use a Magnetic card to upgrade a mech, that upgrade is treated as an enchantment, not as a
	 * transformation. So, for example, if the mech is silenced or returned to the hand, it loses all the effects it got
	 * from the magnetizing.</li>
	 * <li>In-hand enchantments given to Magnetic minions are considered part of the Magnetic effect when attached to
	 * another Mech. For example, Kangor's Endless Army will resurrect a Mech with both the Magnetic buff and the in-hand
	 * enchantment.</li>
	 * <li>In-hand enchantments which give Deathrattle-effects (i.e. Val'anyr) are attached to the Target Mech-Unit all
	 * the same. The Deathrattle-effect of the Unit which has been enhanced by a magnetic unit with an In-hand Deathrattle
	 * enchantment does not show up in the tooltip of that unit. The Deathrattle-Symbol under the unit does show up
	 * however and the Deathrattle-effect is excecuted upon destruction of the unit as expected.</li>
	 * </ul>
	 *
	 * @param playerId
	 * @param card
	 * @param targetMinion
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Suspendable
	public boolean magnetize(int playerId, Card card, Minion targetMinion) {
		Player player = context.getPlayer(playerId);
		if (!canSummonMoreMinions(player)) {
			return false;
		}
		targetMinion.modifyAttribute(Attribute.ATTACK_BONUS, card.getAttack());
		targetMinion.modifyHpBonus(card.getHp());
		List<Attribute> badAttributes = Arrays.asList(Attribute.HP, Attribute.BASE_HP, Attribute.BASE_ATTACK, Attribute.HP_BONUS,
				Attribute.ATTACK, Attribute.ATTACK_BONUS, Attribute.RACE, Attribute.BASE_MANA_COST, Attribute.DEATHRATTLES,
				Attribute.MAGNETIC, Attribute.MAGNETS, Attribute.ECHO, Attribute.AURA_ECHO, Attribute.PLAYED_FROM_HAND_OR_DECK, Attribute.CARD_ID);
		for (Attribute attribute : card.getDesc().getAttributes().keySet()) {
			if (!badAttributes.contains(attribute)) {
				targetMinion.setAttribute(attribute, card.getAttribute(attribute));
			}
		}


		List<String> magnets = new ArrayList<>();
		if (targetMinion.hasAttribute(Attribute.MAGNETS)) {
			magnets.addAll(Arrays.asList((String[]) targetMinion.getAttribute(Attribute.MAGNETS)));
		}
		if (card.getDesc().getDeathrattle() != null) {
			targetMinion.addDeathrattle(card.getDesc().getDeathrattle());
		}
		if (card.getDeathrattleEnchantments().size() > 0) {
			card.getDeathrattleEnchantments().forEach(targetMinion::addDeathrattle);
		}
		if (card.getStoredEnchantments().size() > 0) {
			card.getStoredEnchantments().forEach(ed -> context.getLogic().addGameEventListener(player, ed.create(), targetMinion));
		}
		if (card.getDesc().getTrigger() != null) {
			addGameEventListener(player, card.getDesc().getTrigger().create(), targetMinion);
		}

		if (card.getDesc().getTriggers() != null) {
			for (EnchantmentDesc trigger : card.getDesc().getTriggers()) {
				addGameEventListener(player, trigger.create(), targetMinion);
			}
		}
		if (card.getDesc().getAura() != null) {
			final Aura enchantment = card.getDesc().getAura().create();
			addGameEventListener(player, enchantment, targetMinion);
		}

		if (card.getDesc().getAuras() != null) {
			for (AuraDesc auraDesc : card.getDesc().getAuras()) {
				addGameEventListener(player, auraDesc.create(), targetMinion);
			}
		}

		magnets.add(card.getCardId());
		targetMinion.setAttribute(Attribute.MAGNETS, magnets.toArray(new String[0]));

		context.fireGameEvent(new BoardChangedEvent(context));
		return true;
	}

	@Suspendable
	private Minion summonTransformResolved(Minion minion) {
		// This should probably be deprecated
		if (context.getEnvironment().get(Environment.TRANSFORM_REFERENCE) != null) {
			minion = (Minion) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TRANSFORM_REFERENCE));
			minion.setBattlecry(null);
			context.getEnvironment().remove(Environment.TRANSFORM_REFERENCE);
		}
		minion = (Minion) minion.transformResolved(context);
		return minion;
	}

	@Suspendable
	public void processBattlefieldEnchantments(Player player, Actor actor) {
		if (actor.hasEnchantment()) {
			for (Enchantment trigger : actor.getEnchantments()) {
				addGameEventListener(player, trigger, actor);
			}
		}
	}

	/**
	 * Transforms a {@link Minion} into a new {@link Minion}.
	 * <p>
	 * The caller is responsible for making sure the new minion is created with {@link Minion#getCopy()} if the minion was
	 * the result of targeting an existing minion on the battlefield.
	 * <p>
	 * Transform is an ability which irreversibly transforms a minion into something else. This removes all card text,
	 * abilities and enchantments, and does not trigger any Deathrattles.
	 * <p>
	 * Transformation is not an {@link Aura} but rather a permanent change, which cannot be undone. {@link #silence(int,
	 * Actor)}ing the transformed minion or returning it to its owner's hand will not revert the transformation.
	 * <p>
	 * While transform effects effectively create a new minion in place of the old one, they do not summon minions and so
	 * will not trigger effects such as Knife Juggler or Starving Buzzard. The process appears to continue the summoning
	 * process precisely, with the new minion in place of the old.
	 * <p>
	 * Minions produced by transformations will have {@link Attribute#SUMMONING_SICKNESS}, as though they had just been
	 * summoned. This is true even if the minion which was transformed was previously ready to attack. However, if the
	 * resulting minion has {@link Attribute#CHARGE} it will not suffer from {@link Attribute#SUMMONING_SICKNESS}.
	 * <p>
	 * Minions removed from play due to being transformed are not considered to have died, and so cannot be resummoned by
	 * effects like Resurrect or Kel'Thuzad.
	 *
	 * @param spellDesc The spell responsible for this transform minion effect
	 * @param minion    The original minion in play
	 * @param newMinion The new minion to transform into
	 * @see net.demilich.metastone.game.spells.TransformMinionSpell for the complete transformation logic.
	 */
	@Suspendable
	public void transformMinion(SpellDesc spellDesc, @NotNull Minion minion, @NotNull Minion newMinion) {
		Objects.requireNonNull(newMinion);
		// Remove any spell triggers associated with the old minion.
		removeEnchantments(minion);

		Player owner = context.getPlayer(minion.getOwner());
		// We may be transforming minions before they hit the board.
		int index;
		// Tracking of old index implements Sherazin, Seed
		Zones oldZone = minion.getZone();

		// It's okay to transform in the battlefield or graveyard (handles Sherazin)
		if (minion.getZone() == Zones.BATTLEFIELD) {
			index = minion.getEntityLocation().getIndex();
			owner.getZone(minion.getEntityLocation().getZone()).remove(index);
		} else if (minion.getZone() == Zones.GRAVEYARD || minion.getZone() == Zones.SET_ASIDE_ZONE) {
			// Are we evaluating a deathrattle?
			if (spellDesc.containsKey(SpellArg.AFTERMATH_ID)) {
				// Resurrect to its original position if there is one, or the rightmost position on the board
				index = spellDesc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE, owner.getMinions().size() - 1);
			} else {
				index = minion.getEntityLocation().getIndex();
			}
			// Make sure that the position specifically does not insert the minion somewhere it can't be, because we might be
			// processing multiple deathrattles and thus multiple minions have been removed, but not in the right order.
			index = MathUtils.clamp(index, 0, Math.max(0, owner.getMinions().size() - 1));
			owner.getZone(minion.getEntityLocation().getZone()).remove(minion.getEntityLocation().getIndex());
		} else {
			// Transforming a minion before it hits the board? Probably a bug
			throw new UnsupportedOperationException("not on board");
		}

		// If we want to straight up remove a minion from existence without
		// killing it, this would be the best way.
		// Give the new minion an ID.
		newMinion.setId(generateId());
		newMinion.setOwner(owner.getId());

		minion.getAttributes().put(Attribute.TRANSFORM_REFERENCE, newMinion.getReference());
		// If minion is currently being summoned, newMinion gets summoned instead. The fact that newMinion is summoned
		// instead is communicated back to the summon function via Environment.TRANSFORM_REFERENCE
		if (!context.getSummonReferenceStack().isEmpty()
				&& Objects.equals(context.getSummonReferenceStack().peek(), minion.getReference())
				&& !context.getEnvironment().containsKey(Environment.TRANSFORM_REFERENCE)) {
			context.getEnvironment().put(Environment.TRANSFORM_REFERENCE, newMinion.getReference());
			owner.getMinions().add(index, newMinion);
			// Otherwise, if the set aside zone does not contain the minion (it is definitely not on the battlefield or in
			// the graveyard and we are not transforming something being returned to hand), we have removed the old minion here
			// and can now drop in the new minion.
		} else if (!owner.getSetAsideZone().contains(minion)) {
			if (index < 0 || index >= owner.getMinions().size()) {
				owner.getMinions().add(newMinion);
			} else {
				owner.getMinions().add(index, newMinion);
			}

			newMinionOnBattlefield(newMinion, owner);
		} else {
			// Not sure if this ever happens, it's related to whether the minion still belongs to its owner?
			owner.getSetAsideZone().add(newMinion);
			removeEnchantments(newMinion);
			return;
		}

		// Special case for Sherazin, Seed
		if (oldZone == Zones.GRAVEYARD) {
			if (minion.getId() == UNASSIGNED) {
				throw new RuntimeException();
			}
			minion.moveOrAddTo(context, Zones.GRAVEYARD);
		} else if (minion.transformResolved(context).equals(newMinion)) {
			// The old minion should always be removed from play, because it has transformed.
			removeEnchantments(minion);
			minion.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		} else {
			// Something else happened and the source minion was not successfully transformed.
			minion.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(XORShiftRandom random) {
		this.random = random;
	}

	public IdFactory getIdFactory() {
		return idFactory;
	}

	public void setIdFactory(IdFactoryImpl idFactory) {
		this.idFactory = idFactory;
	}

	/**
	 * Concedes the game for the specified player.
	 * <p>
	 * Concession is implemented by destroy's the player's {@link Hero}.
	 *
	 * @param playerId The player that should concede.
	 */
	@Suspendable
	public void concede(int playerId) {
		repeatedlyDestroyHero(playerId);
	}

	/**
	 * Plays a quest from the hand.
	 *
	 * @param player
	 * @param quest
	 * @see #playQuest(Player, Quest, boolean) for a complete reference.
	 */
	public void playQuest(Player player, Quest quest) {
		playQuest(player, quest, true);
	}

	/**
	 * Plays the specified quest. The quest goes into the {@link Zones#QUEST} zone and triggers using the enchantment's
	 * {@link Enchantment#getCountUntilCast()} functionality.
	 *
	 * @param player   The player that triggered the quest.
	 * @param quest    The quest to put into play.
	 * @param fromHand If {@code true}, fires a {@link QuestPlayedEvent}.
	 */
	@Suspendable
	public void playQuest(Player player, Quest quest, boolean fromHand) {
		quest = quest.clone();
		quest.setId(generateId());
		quest.setOwner(player.getId());
		quest.moveOrAddTo(context, Zones.QUEST);
		addGameEventListener(player, quest, quest);
		if (fromHand) {
			context.fireGameEvent(new QuestPlayedEvent(context, player.getId(), quest.getSourceCard(), quest));
		}
	}

	/**
	 * Plays a quest from the hand.
	 *
	 * @param player
	 * @param pact
	 * @see #playQuest(Player, Quest, boolean) for a complete reference.
	 */
	@Suspendable
	public void playPact(Player player, Quest pact) {
		playPact(player, pact, true);
	}

	/**
	 * Plays the specified quest. The quest goes into the {@link Zones#QUEST} zone and triggers using the enchantment's
	 * {@link Enchantment#getCountUntilCast()} functionality.
	 *
	 * @param player   The player that triggered the quest.
	 * @param pact     The pact to put into play.
	 * @param fromHand If {@code true}, fires a {@link QuestPlayedEvent}.
	 */
	@Suspendable
	public void playPact(Player player, Quest pact, boolean fromHand) {
		pact = pact.clone();
		pact.setId(generateId());
		pact.setOwner(player.getId());
		pact.moveOrAddTo(context, Zones.QUEST);
		addGameEventListener(player, pact, pact);
	}

	/**
	 * Indicates that a quest was successful (its spell was casted).
	 *
	 * @param player The player that triggered the quest. May be different than its owner.
	 * @param quest  The quest {@link Enchantment} living inside the {@link Zones#QUEST} zone.
	 */
	@Suspendable
	public void questTriggered(Player player, Quest quest) {
		removeEnchantments(quest);
		if (quest.isInPlay()) {
			quest.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			context.fireGameEvent(new QuestSuccessfulEvent(context, quest.getSourceCard(), player.getId()));
		}
	}

	/**
	 * Gets the player ID of the player who is going to take the next turn.
	 * <p>
	 * Takes into account {@link Attribute#EXTRA_TURN}.
	 * <p>
	 * Implements Open the Waygate.
	 *
	 * @return The player ID.
	 */
	public int getNextActivePlayerId() {
		if (context.getActivePlayer().getAttributeValue(Attribute.EXTRA_TURN) > 0) {
			return context.getActivePlayerId();
		} else {
			return context.getActivePlayerId() == GameContext.PLAYER_1 ? GameContext.PLAYER_2 : GameContext.PLAYER_1;
		}
	}

	/**
	 * Get the amount of time the player has to mulligan, in milliseconds.
	 *
	 * @return The default mulligan time for now.
	 */
	public long getMulliganTimeMillis() {
		return GameLogic.DEFAULT_MULLIGAN_TIME * 1000L;
	}

	/**
	 * Reveals a card to both players.
	 * <p>
	 * Implements Dragon's Fury.
	 *
	 * @param player       The player who is revealing the card.
	 * @param cardToReveal The card that should be revealed.
	 */
	@Suspendable
	public void revealCard(Player player, Card cardToReveal) {
		// For now, just trigger a reveal card event.
		context.fireGameEvent(new CardRevealedEvent(context, player.getId(), cardToReveal));
	}

	/**
	 * Indicates that the specified played discovered a card.
	 *
	 * @param playerId
	 */
	@Suspendable
	public void discoverCard(int playerId) {
		// Similar to above, we will just fire an event for now.
		context.fireGameEvent(new DiscoverEvent(context, playerId));
	}

	public int getInternalId() {
		return idFactory.getInternalId();
	}

	/**
	 * Indicates that the {@link GameContext} references in this instance is ready.
	 */
	public void contextReady() {
	}

	public long getSeed() {
		return seed;
	}

	/**
	 * Returns {@code true} if any the card's conditions are met.
	 * <p>
	 * If the card does not contain any conditions, returns {@code false}.
	 * <p>
	 * For cards with spells that contain multiple conditions, it's unlikely they are supposed to be interpreted as all of
	 * them are met.
	 *
	 * @param localPlayerId
	 * @param card
	 * @return {@code true} if there are conditions and any are met, otherwise {@code false}.
	 */
	@Suspendable
	public boolean conditionMet(int localPlayerId, @NotNull Card card) {
		try {
			return card.getDesc()
					.getConditions()
					.allMatch(conditionDesc -> conditionDesc.create().isFulfilled(context, context.getPlayer(localPlayerId), card, null));
		} catch (Throwable ignored) {
			return false;
		}
	}

	/**
	 * Destroys both player's heroes to force a draw
	 */
	@Suspendable
	public void loseBothPlayers() {
		repeatedlyDestroyHero(IdFactory.PLAYER_1);
		repeatedlyDestroyHero(IdFactory.PLAYER_2);
	}

	/**
	 * Repeatedly destroys the hero for the given player ID, to account for heroes that may replace themselves with new
	 * heroes on a deathrattle.
	 *
	 * @param playerId The player whose hero (and subsequent new heroes) should be destroyed
	 */
	@Suspendable
	protected void repeatedlyDestroyHero(int playerId) {
		while (context.getPlayer(playerId).getHero() != null && context.getPlayer(playerId).getHero().getZone() != Zones.GRAVEYARD) {
			if (Strand.currentStrand().isInterrupted()) {
				break;
			}

			Hero hero = context.getPlayer(playerId).getHero();
			if (hero.getOwner() == UNASSIGNED) {
				hero.setOwner(playerId);
			}

			if (hero.getId() == UNASSIGNED) {
				hero.setId(generateId());
			}

			destroy(context.getPlayer(playerId).getHero());
			endOfSequence();
		}
	}

	public void processPassiveAuras(Player player, Card card) {
		if (card.getDesc().getPassiveAuras() != null) {
			for (AuraDesc aura : card.getDesc().getPassiveAuras()) {
				addGameEventListener(player, aura.create(), card);
			}
		}
	}

	private static boolean hasPlayerLost(Player player) {
		return player.getHero() == null
				|| player.getHero().getHp() < 1
				|| player.getHero().hasAttribute(Attribute.DESTROYED)
				|| player.hasAttribute(Attribute.DESTROYED);
	}

	/**
	 * Compute the turn time in milliseconds for the specified player.
	 * <p>
	 * TODO: Consider how many turns the player has skipped.
	 *
	 * @return The turn time in milliseconds.
	 */
	public int getTurnTimeMillis(int playerId) {
		return Integer.parseInt(System.getProperty("games.turnTimeMillis", System.getenv().getOrDefault("SPELLSOURCE_TURN_TIME", Integer.toString(DEFAULT_TURN_TIME * 1000))));
	}
}
