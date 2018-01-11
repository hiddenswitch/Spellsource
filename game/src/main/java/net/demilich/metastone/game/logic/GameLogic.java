package net.demilich.metastone.game.logic;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.demilich.metastone.game.*;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.SpellFactory;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.DamageCausedTrigger;
import net.demilich.metastone.game.spells.trigger.DamageReceivedTrigger;
import net.demilich.metastone.game.spells.trigger.HealingTrigger;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.MinionSummonedTrigger;
import net.demilich.metastone.game.spells.trigger.SpellCastedTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.*;
import net.demilich.metastone.game.shared.utils.MathUtils;
import net.demilich.metastone.game.utils.Attribute;
import org.apache.commons.collections4.Bag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
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
 * Minion, Card, int, boolean)} and {@link #fight(Player, Actor, Actor)}.
 */
public class GameLogic implements Cloneable, Serializable {
	protected static Logger logger = LoggerFactory.getLogger(GameLogic.class);
	/**
	 * The maximum number of {@link Minion} entities that can be on a {@link Zones#BATTLEFIELD}.
	 */
	public static final int MAX_MINIONS = 7;
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
	 * The maximum number of {@link Card} entities that a {@link Player} can build a {@link Deck} with. Some effects,
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
	 * This {@link Set} stores each {@link Attribute} that is not cleared when an {@link Entity} is silenced.
	 *
	 * @see #silence(int, Minion) for the silence game logic.
	 */
	public static final Set<Attribute> immuneToSilence = new HashSet<>();
	/**
	 * A prefix appended to cards that are temporarily generated by game rules.
	 *
	 * @see GameLogic#generateCardId() for the situation where card IDs need to be generated.
	 */
	public static final String TEMP_CARD_LABEL = "temp_card_id_";
	private static final int INFINITE = -1;
	protected final TargetLogic targetLogic = new TargetLogic();
	private final ActionLogic actionLogic = new ActionLogic();
	private final SpellFactory spellFactory = new SpellFactory();
	private IdFactory idFactory;
	private Random random = new Random();
	protected transient GameContext context;
	private boolean loggingEnabled = false;
	private final int MAX_HISTORY_ENTRIES = 100;
	private ArrayDeque<String> debugHistory = new ArrayDeque<>();

	static {
		immuneToSilence.add(Attribute.HP);
		immuneToSilence.add(Attribute.MAX_HP);
		immuneToSilence.add(Attribute.BASE_HP);
		immuneToSilence.add(Attribute.BASE_ATTACK);
		immuneToSilence.add(Attribute.SUMMONING_SICKNESS);
		immuneToSilence.add(Attribute.AURA_ATTACK_BONUS);
		immuneToSilence.add(Attribute.AURA_HP_BONUS);
		immuneToSilence.add(Attribute.AURA_UNTARGETABLE_BY_SPELLS);
		immuneToSilence.add(Attribute.RACE);
		immuneToSilence.add(Attribute.NUMBER_OF_ATTACKS);
	}

	/**
	 * Creates a new game logic instance whose next ID generated for an {@link Entity#setId(int)} argument will be
	 * zero.
	 */
	public GameLogic() {
		idFactory = new IdFactory();
	}

	/**
	 * Creates a game logic instance with an ID factory. Typically you can create an ID factory and set its current ID
	 * to whichever number you want to be the next entity ID created by this game logic.
	 *
	 * @param idFactory An existing ID factory.
	 */
	private GameLogic(IdFactory idFactory) {
		this.idFactory = idFactory;
	}

	/**
	 * Adds a {@link Trigger} to a specified {@link Entity}. These are typically {@link Enchantment} instances that
	 * react to game events.
	 *
	 * @param player            Usually the current turn player.
	 * @param gameEventListener A game event listener, like a {@link Aura}, {@link Secret} or {@link CardCostModifier}.
	 * @param host              The {@link Entity} that will be pointed to by {@link Trigger#getHostReference()}.
	 * @see TriggerManager#fireGameEvent(GameEvent, List) for the complete implementation of triggers.
	 */
	@Suspendable
	public void addGameEventListener(Player player, Trigger gameEventListener, Entity host) {
		if (isLoggingEnabled()) {
			debugHistory.add("Player " + player.getId() + " has set event listener " + gameEventListener.getClass().getName() + " from entity " + host.getName() + "[Reference ID: " + host.getId() + "]");
		}

		gameEventListener.setHost(host);
		if (!gameEventListener.hasPersistentOwner() || gameEventListener.getOwner() == Entity.NO_OWNER) {
			gameEventListener.setOwner(player.getId());
		}

		if (gameEventListener instanceof Enchantment) {
			// Start assigning IDs to these things
			Enchantment enchantment = (Enchantment) gameEventListener;
			enchantment.setId(getIdFactory().generateId());
			if (enchantment.getSourceCard() == null) {
				enchantment.setSourceCard(host.getSourceCard());
			}
		}

		gameEventListener.onAdd(context);
		context.addTrigger(gameEventListener);

		log("New enchantment was added for {} on {}: {}", player.getName(), host, gameEventListener);
	}

	/**
	 * Specifically add a card cost modifier to the game, which is a special kind of {@link Trigger}.
	 *
	 * @param player           Usually the current turn player.
	 * @param cardCostModifier The card cost modifier.
	 * @param target           The {@link Card} whose cost should be modified.
	 */
	@Suspendable
	public void addManaModifier(Player player, CardCostModifier cardCostModifier, Entity target) {
		context.getCardCostModifiers().add(cardCostModifier);
		addGameEventListener(player, cardCostModifier, target);
	}

	/**
	 * Handles combo and mana cost modifier removal for the card played in the {@link
	 * PlayCardAction#execute(GameContext, int)} method. Can probably be inlined.
	 *
	 * @param playerId        The player index
	 * @param EntityReference A reference to the card.
	 */
	@Suspendable
	public void afterCardPlayed(int playerId, EntityReference EntityReference) {
		Player player = context.getPlayer(playerId);

		player.modifyAttribute(Attribute.COMBO, +1);
		Card card = context.resolveEntityReference(EntityReference);

		card.getAttributes().remove(Attribute.MANA_COST_MODIFIER);
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
	 * @see Attribute#HEAL_AMPLIFY_MULTIPLIER for the healing amplification attribute.
	 * @see Attribute#SPELL_AMPLIFY_MULTIPLIER for the spell damage amplification attribute.
	 */
	@Suspendable
	public int applyAmplify(Player player, int baseValue, Attribute attribute) {
		int amplify = getTotalAttributeMultiplier(player, attribute);
		return baseValue * amplify;
	}

	/**
	 * Gives an {@link Entity} a boolean {@link Attribute}.
	 * <p>
	 * This addresses bugs with {@link Attribute#WINDFURY} and should be the place for special rules around attributes
	 * in the future.
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
	 * This addresses bugs with {@link Attribute#WINDFURY} and should be the place for special rules around attributes
	 * in the future.
	 *
	 * @param entity An {@link Entity}
	 * @param attr   An {@link Attribute}
	 * @param source A source entity for the attribute application.
	 */
	@Suspendable
	public void applyAttribute(Entity entity, Attribute attr, Entity source) {
		if (attr == Attribute.MEGA_WINDFURY && entity.hasAttribute(Attribute.WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, MEGA_WINDFURY_ATTACKS - WINDFURY_ATTACKS);
		} else if (attr == Attribute.WINDFURY && !entity.hasAttribute(Attribute.WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - 1);
		} else if (attr == Attribute.MEGA_WINDFURY && !entity.hasAttribute(Attribute.WINDFURY) && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, MEGA_WINDFURY_ATTACKS - 1);
		}
		entity.setAttribute(attr);
		context.fireGameEvent(new AttributeAppliedEvent(context, entity.getId(), entity, source, attr));
		log("Applying attr {} to {}", attr, entity);
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
				+ getTotalAttributeValue(context.getOpponent(player), Attribute.OPPONENT_SPELL_DAMAGE);
		if (source.hasAttribute(Attribute.SPELL_DAMAGE_MULTIPLIER)) {
			spellpower *= source.getAttributeValue(Attribute.SPELL_DAMAGE_MULTIPLIER);
		}
		return baseValue + spellpower;
	}

	/**
	 * Assigns an {@link Entity#id} and {@link Entity#ownerIndex} to each {@link Card} in a given {@link Deck}.
	 *
	 * @param cardList   The {@link Deck} whose cards should have IDs and owners assigned.
	 * @param ownerIndex The owner to assign to this {@link CardList}
	 */
	@Suspendable
	protected void assignCardIds(CardList cardList, int ownerIndex) {
		for (Card card : cardList) {
			card.setId(getIdFactory().generateId());
			card.setOwner(ownerIndex);
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
	 * signal to an end user that they can play a particular card. Takes into account whether or not a spell that
	 * requires targets has possible targets in the game.
	 *
	 * @param playerId        The player whose point of view should be considered for this method.
	 * @param EntityReference A reference to the card.
	 * @return {@code true} if the card can be played.
	 */
	@Suspendable
	public boolean canPlayCard(int playerId, EntityReference EntityReference) {
		Player player = context.getPlayer(playerId);
		Card card = context.resolveEntityReference(EntityReference);
		// A player cannot play a card the player does not own.
		if (card.getOwner() != player.getId()
				&& card.getOwner() != Entity.NO_OWNER) {
			return false;
		}
		int manaCost = getModifiedManaCost(player, card);
		if (card.getCardType().isCardType(CardType.SPELL)
				&& player.hasAttribute(Attribute.SPELLS_COST_HEALTH)
				&& player.getHero().getEffectiveHp() < manaCost) {
			return false;
		} else if (card.getCardType().isCardType(CardType.MINION)
				&& (Race) card.getAttribute(Attribute.RACE) == Race.MURLOC
				&& player.hasAttribute(Attribute.MURLOCS_COST_HEALTH)
				&& player.getHero().getEffectiveHp() < manaCost) {
			return false;
		} else if (player.getMana() < manaCost && manaCost != 0
				&& !((card.getCardType().isCardType(CardType.SPELL)
				&& player.hasAttribute(Attribute.SPELLS_COST_HEALTH))
				|| ((Race) card.getAttribute(Attribute.RACE) == Race.MURLOC
				&& player.hasAttribute(Attribute.MURLOCS_COST_HEALTH)))) {
			return false;
		}
		if (card.getCardType().isCardType(CardType.HERO_POWER)) {
			HeroPowerCard power = (HeroPowerCard) card;
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
		} else if (card.getCardType().isCardType(CardType.MINION)) {
			return canSummonMoreMinions(player);
		}

		if (card instanceof SpellCard) {
			SpellCard spellCard = (SpellCard) card;
			return spellCard.canBeCast(context, player);
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
	 * Players cannot have more than one copy of the same Secret active at any one time. Players are unable to play
	 * Secret cards which match one of their active Secrets.
	 * <p>
	 * When played directly from the hand, players can have up to 5 different Secrets active at a time. Once this limit
	 * is reached, the player will be unable to play further Secret cards.
	 *
	 * @param player The player whose {@link Zones#SECRET} zone should be inspected.
	 * @param card   The secret card being evaluated.
	 * @return {@code true} if the secret can be played.
	 */
	public boolean canPlaySecret(Player player, SecretCard card) {
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
	public boolean canPlayQuest(Player player, QuestCard card) {
		return player.getSecrets().size() < MAX_SECRETS && player.getQuests().size() < MAX_QUESTS && !player.getQuests().contains(card.getCardId());
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
	 * Choose One effects are similar to Discover effects, and certain other cards such as Tracking, which also allow
	 * you to choose between multiple options.
	 *
	 * @param playerId        The player casting the choose one spell.
	 * @param spellDesc       The {@link SpellDesc} of the chosen card, not the parent card that contains the choices.
	 * @param sourceReference The source of the spell, typically the original {@link ChooseOneCard}.
	 * @param targetReference The target selected for this choice.
	 * @param cardId          The card that was chosen.
	 */
	@Suspendable
	public void castChooseOneSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference, String cardId) {
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null) {
			try {
				source = context.resolveSingleTarget(sourceReference);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error resolving source entity while casting spell: " + spellDesc);
			}
		}
		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		Card sourceCard = null;
		SpellCard chosenCard = (SpellCard) context.getCardById(cardId);
		sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;
		if (!spellDesc.hasPredefinedTarget() && targets != null && targets.size() == 1) {
			if (chosenCard.getTargetRequirement() != TargetSelection.NONE) {
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				context.getEnvironment().put(Environment.CHOOSE_ONE_CARD, chosenCard.getCardId());
				GameEvent spellTargetEvent = new TargetAcquisitionEvent(context, playerId, ActionType.SPELL, chosenCard, targets.get(0));
				context.fireGameEvent(spellTargetEvent);
				Entity targetOverride = context
						.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				if (targetOverride != null && targetOverride.getId() != IdFactory.UNASSIGNED) {
					targets.remove(0);
					targets.add(targetOverride);
					spellDesc = spellDesc.addArg(SpellArg.FILTER, null);
					log("Target for spell {} has been changed! New target {}", chosenCard, targets.get(0));
				}
			}
		}
		try {
			Spell spell = spellFactory.getSpell(spellDesc);
			spell.cast(context, player, spellDesc, source, targets);
		} catch (Exception e) {
			if (source != null) {
				logger.error("Error while playing card: " + source.getName());
			}
			logger.error("Error while casting spell: " + spellDesc);
			logDebugHistory();
			e.printStackTrace();
		}

		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
		context.getEnvironment().remove(Environment.CHOOSE_ONE_CARD);

		checkForDeadEntities();
		if (targets == null || targets.size() != 1) {
			context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
		} else {
			context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
		}
	}

	/*
	 * Casts a spell.
	 * @param playerId The casting player.
	 * @param spellDesc The {@link SpellDesc} for the spell.
	 * @param sourceReference The source of the spell (typically the card), or {@code null} if it doesn't have one.
	 * @param targetReference The selected target of the spell, or {@code null} if it doesn't have one.
	 * @param childSpell When true, this spell is implementing an effect rather than what a player would think of as a
	 * "spell"--a single card.
	 * @see #castSpell(int, SpellDesc, EntityReference, EntityReference, TargetSelection, boolean) for complete documentation.
	 */
	@Suspendable
	public void castSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference,
	                      boolean childSpell) {
		castSpell(playerId, spellDesc, sourceReference, targetReference, TargetSelection.NONE, childSpell);
	}

	/**
	 * Casts a spell.
	 * <p>
	 * This method uses the {@link SpellDesc} (a {@link Map} of {@link SpellArg}, {@link Object}) to figure out what the
	 * spell should do. The {@link SpellFactory#getSpell(SpellDesc)} method creates an instance of the {@link Spell}
	 * class returned by {@code spellDesc.getSpellClass()}, then calls its {@link Spell#onCast(GameContext, Player,
	 * SpellDesc, Entity, Entity)} method to actually execute the code of the spell.
	 * <p>
	 * For example, imagine a spell, "Deal 2 damage to all Murlocs." This would have a {@link SpellDesc} (1) whose
	 * {@link SpellArg#CLASS} would be {@link DamageSpell}, (2) whose {@link SpellArg#FILTER} would be an instance of
	 * {@link EntityFilter} with {@link FilterArg#RACE} as {@link Race#MURLOC}, (3) whose {@link SpellArg#VALUE} would
	 * be {@code 2} to deal 2 damage, and whose (4) {@link SpellArg#TARGET} would be {@link
	 * EntityReference#ALL_MINIONS}.
	 * <p>
	 * Effects can modify spells or create new ones. {@link SpellDesc} allows the code to modify the "code" of a spell.
	 * <p>
	 * This method is responsible for turning the {@link SpellArg#CLASS} argument into a spell instance. The particular
	 * spell class is then responsible for interpreting the rest of its arguments. This code also handles the player's
	 * chosen target whenever a spell had a target selection.
	 *
	 * @param playerId        The players from whose point of view this spell is cast (typically the owning player).
	 * @param spellDesc       A description of the spell.
	 * @param sourceReference The origin of the spell. This is typically the {@link Minion} if the spell is a battlecry
	 *                        or deathrattle; or, the {@link SpellCard} if this spell is coming from a card.
	 * @param targetReference A reference to the target the user selected, if the spell was supposed to have a target.
	 * @param targetSelection If not {@code null}, the spell must have at least one {@link Entity} satisfying this
	 *                        target selection requirement in order for it to be cast.
	 * @param childSpell      When {@code true}, this spell is part an effect, like one of the {@link SpellArg#SPELLS}
	 *                        of a {@link MetaSpell}, and so it shouldn't trigger the firing of events like {@link
	 *                        SpellCastedTrigger}. When {@code false}, this spell is what a player would interpret as a
	 *                        spell coming from a card (a "spell" in the sense of what is written on cards). Battlecries
	 *                        and deathrattles are, unusually, {@code false} (not) child spells.
	 * @see Spell#cast(GameContext, Player, SpellDesc, Entity, List) for the code that interprets the {@link
	 * SpellArg#FILTER}, and {@link SpellArg#RANDOM_TARGET} arguments.
	 * @see Spell#onCast(GameContext, Player, SpellDesc, Entity, Entity) for the function that typically has the
	 * spell-specific code (e.g., {@link DamageSpell#onCast(GameContext, Player, SpellDesc, Entity, Entity)} actually
	 * implements the logic of a damage spell and interprets the {@link SpellArg#VALUE} attribute of the {@link
	 * SpellDesc} as damage.
	 * @see MetaSpell for the mechanism that multiple spells as children are chained together to create an effect.
	 * @see ActionLogic#rollout(GameAction, GameContext, Player, Collection) for the code that turns a target selection
	 * into actions the player can take.
	 * @see PlaySpellCardAction#play(GameContext, int) for the call to this function that a player actually does when
	 * they play a {@link SpellCard} (as opposed to a battlecry or deathrattle).
	 * @see BattlecryAction#execute(GameContext, int) for the call to this function that demonstrates a battlecry
	 * effect. Battlecries are spells in the sense that they are effects, though they're not {@link SpellCard} objects.
	 */
	@Suspendable
	public void castSpell(int playerId, SpellDesc spellDesc, EntityReference sourceReference, EntityReference targetReference,
	                      TargetSelection targetSelection, boolean childSpell) {
		Player player = context.getPlayer(playerId);
		Entity source = null;
		if (sourceReference != null) {
			try {
				source = context.resolveSingleTarget(sourceReference);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error resolving source entity while casting spell: " + spellDesc);
			}

		}
		EntityReference spellTarget = spellDesc.hasPredefinedTarget() ? spellDesc.getTarget() : targetReference;
		List<Entity> targets = targetLogic.resolveTargetKey(context, player, source, spellTarget);
		// target can only be changed when there is one target
		// note: this code block is basically exclusively for the SpellBender
		// Secret, but it can easily be expanded if targets of area of effect
		// spell should be changeable as well
		Card sourceCard = null;
		if (source != null) {
			sourceCard = source.getEntityType() == EntityType.CARD ? (Card) source : null;
		}
		if (sourceCard != null && sourceCard.getCardType().isCardType(CardType.SPELL) && !spellDesc.hasPredefinedTarget() && targets != null
				&& targets.size() == 1) {
			if (sourceCard.getCardType().isCardType(CardType.SPELL) && targetSelection != TargetSelection.NONE && !childSpell) {
				GameEvent spellTargetEvent = new TargetAcquisitionEvent(context, playerId, ActionType.SPELL, sourceCard, targets.get(0));
				context.fireGameEvent(spellTargetEvent);
				Entity targetOverride = context
						.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
				if (targetOverride != null && targetOverride.getId() != IdFactory.UNASSIGNED) {
					targets.remove(0);
					targets.add(targetOverride);
					spellDesc = spellDesc.addArg(SpellArg.FILTER, null);
					log("Target for spell {} has been changed! New target {}", sourceCard, targets.get(0));
				}
			}

		}

		// This implements Ice Walker
		if (sourceCard != null
				&& sourceCard.getCardType().isCardType(CardType.HERO_POWER)
				&& targets != null
				&& targets.size() > 0
				&& targetSelection != TargetSelection.NONE
				&& hasAttribute(player, Attribute.HERO_POWER_FREEZES_TARGET)
				&& !childSpell) {
			spellDesc = SpellDesc.join(spellDesc, AddAttributeSpell.create(Attribute.FROZEN));
		}

		// This implements a more durable tracking of spells that were casted
		if (sourceCard != null
				&& sourceCard instanceof SpellCard
				&& !sourceCard.getCardType().isCardType(CardType.HERO_POWER)
				&& !childSpell) {
			sourceCard.setAttribute(Attribute.CAST_FROM_HAND_OR_DECK, context.getTurn());
		}

		Spell spell = spellFactory.getSpell(spellDesc);
		spell.cast(context, player, spellDesc, source, targets);

		// This implements Lynessa Sunsorrow
		if (sourceCard != null
				&& sourceCard.getCardType() == CardType.SPELL
				&& targetSelection != TargetSelection.NONE
				&& targets.size() == 1
				&& targets.get(0).getOwner() == playerId
				&& !childSpell) {
			EnvironmentEntityList.getList(context, Environment.LYNESSA_SUNSORROW_ENTITY_LIST)
					.add(context.getPlayer(playerId), sourceCard);
		}

		if (sourceCard != null && sourceCard.getCardType().isCardType(CardType.SPELL) && !childSpell) {
			context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

			checkForDeadEntities();
			if (targets == null || targets.size() != 1) {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, null));
			} else {
				context.fireGameEvent(new AfterSpellCastedEvent(context, playerId, sourceCard, targets.get(0)));
			}
		}
	}

	/**
	 * Changes the player's hero.
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
		hero.setId(getIdFactory().generateId());
		hero.setOwner(player.getId());
		if (hero.getHeroClass() == null || hero.getHeroClass() == HeroClass.ANY) {
			hero.setHeroClass(previousHero.getHeroClass());
		}

		// Get the current hitpoints and armor
		int previousHp = previousHero.getHp();
		// Get the additional armor from the incoming hero
		int previousArmor = previousHero.getArmor();

		log("{}'s hero has been changed to {}", player.getName(), hero);

		hero.setWeapon(previousHero.getWeapon());
		if (hero.getHeroClass().equals(HeroClass.INHERIT)) {
			hero.setHeroClass(previousHero.getHeroClass());
		}
		if (hero.getHeroPower().getHeroClass() == HeroClass.INHERIT) {
			hero.getHeroPower().setHeroClass(previousHero.getHeroClass());
		}
		// Set hero power ID before events trigger to prevent issues
		hero.getHeroPower().setId(getIdFactory().generateId());
		hero.getHeroPower().setOwner(hero.getOwner());

		// Remove the old hero from play
		removeEnchantments(previousHero);
		// This removes the hero power enchantments too
		removeCard(previousHero.getHeroPower());
		previousHero.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		player.setHero(hero);
		hero.modifyArmor(previousArmor);
		final int armorChange = hero.getArmor() - previousArmor;
		if (armorChange != 0) {
			context.fireGameEvent(new ArmorChangedEvent(context, player.getHero(), armorChange));
		}

		// Only override the HP if both max and new HP is defined.
		if (!(hero.hasAttribute(Attribute.MAX_HP) && hero.hasAttribute(Attribute.HP))) {
			hero.setHp(previousHp);
		}


		refreshAttacksPerRound(hero);

		if (resolveBattlecry && hero.getBattlecry() != null) {
			resolveBattlecry(player.getId(), hero);
			checkForDeadEntities();
		}

		if (hero.hasEnchantment()) {
			for (Enchantment trigger : hero.getEnchantments()) {
				addGameEventListener(player, trigger, hero);
			}
		}

		processPassiveTriggers(player, hero.getHeroPower());
	}

	/**
	 * Removes entities for whom {@link Entity#isDestroyed()} is true, moving them to the {@link Zones#GRAVEYARD} and
	 * triggering their deathrattles with {@link #resolveDeathrattles(Player, Actor)}.
	 * <p>
	 * Since deathrattles may destroy other entities (e.g., a {@link DamageSpell} deathrattle), this function calls
	 * itself recursively until there are no more dead entities on the board.
	 */
	@Suspendable
	public void checkForDeadEntities() {
		checkForDeadEntities(0);
	}

	/**
	 * Checks all player minions and weapons for destroyed actors and proceeds with the removal in correct order.
	 *
	 * @param i The number of times this method has been called to avoid infinite death checking.
	 */
	@Suspendable
	private void checkForDeadEntities(int i) {
		// sanity check, this method should never call itself that often
		if (i > 20) {
			logDebugHistory();
			throw new RuntimeException("Infinite death checking loop");
		}

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

		if (destroyList.isEmpty()) {
			return;
		}

		// sort the destroyed actors by their id. This implies that actors with a lower id entered the game earlier than those with higher ids!
		destroyList.sort(Comparator.comparingInt(Entity::getId));
		// this method performs the actual removal
		destroy(destroyList.toArray(new Actor[0]));
		if (context.updateAndGetGameOver()) {
			return;
		}
		// deathrattles have been resolved, which may lead to other actors being destroyed now, so we need to check again
		checkForDeadEntities(i + 1);
	}

	/**
	 * Clones the game logic. The only state in this instance is its debug history and the current ID of the ID
	 * Factory.
	 *
	 * @return A clone of this logic.
	 * @see IdFactory for the internal state of an {@link IdFactory}.
	 */
	@Override
	public GameLogic clone() {
		GameLogic clone = new GameLogic(getIdFactory().clone());
		clone.setLoggingEnabled(this.isLoggingEnabled());
		if (isLoggingEnabled()) {
			clone.debugHistory = this.debugHistory.clone();
		}
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
	 * @see #damage(Player, Actor, int, Entity, boolean) for a complete description of the damage effect.
	 */
	@Suspendable
	public int damage(Player player, Actor target, int baseDamage, Entity source) {
		return damage(player, target, baseDamage, source, false);
	}

	/**
	 * Deals damage to a target.
	 * <p>
	 * Damage is measured by a number which is deducted from the armor first, followed by hitpoints, of an {@link
	 * Actor}. If the {@link Actor#getHp()} is reduced to zero (or below), it will be killed. Note that other types of
	 * harm that can be inflicted to characters (such as a {@link DestroySpell}, freeze effects and the card Equality)
	 * are not considered damage for game purposes and, although most damage is dealt through {@link #fight(Player,
	 * Actor, Actor)}, dealing damage is not considered an "fight" for game purposes.
	 * <p>
	 * Damage can activate a number of triggered effects, both from receiving it (such as Acolyte of Pain's {@link
	 * DamageReceivedTrigger}) and from dealing it (such as Lightning Automaton's {@link DamageCausedTrigger}). However,
	 * damage negated by an {@link Actor} with {@link Attribute#DIVINE_SHIELD} or {@link Attribute#IMMUNE} effects is
	 * not considered to have been successfully dealt, and thus will not trigger any on-damage triggered effects.
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
	 * @return The amount of damage that was actually dealt
	 */
	@Suspendable
	public int damage(Player player, Actor target, int baseDamage, Entity source, boolean ignoreSpellDamage) {
		// sanity check to prevent StackOverFlowError with Mistress of Pain +
		// Auchenai Soulpriest
		int damageDealt = applyDamageToActor(target, baseDamage, player, source, ignoreSpellDamage);
		resolveDamageEvent(player, target, source, damageDealt);
		return damageDealt;
	}

	@Suspendable
	protected void resolveDamageEvent(Player player, Actor target, Entity source, int damageDealt) {
		if (damageDealt > 0) {
			// Keyword effects for lifesteal and poisonous will come BEFORE all other events
			// Poisonous resolves in a queue with higher priority, and it stops Grim Patron spawning regardless of
			// Dominant Player. However, Acidmaw can never stop Grim Patron spawning.
			if (source.hasAttribute(Attribute.POISONOUS)
					&& target.getEntityType() == EntityType.MINION) {
				markAsDestroyed(target);
			}

			// Implement lifesteal
			if (source.hasAttribute(Attribute.LIFESTEAL)) {
				Player sourceOwner = context.getPlayer(source.getOwner());
				heal(sourceOwner, sourceOwner.getHero(), damageDealt, source);
			}

			player.getStatistics().damageDealt(damageDealt);
			DamageEvent damageEvent = new DamageEvent(context, target, source, damageDealt);
			context.fireGameEvent(damageEvent);
		}
	}

	@Suspendable
	protected int applyDamageToActor(Actor target, int baseDamage, Player player, Entity source, boolean ignoreSpellDamage) {
		if (target.getHp() < -100) {
			return 0;
		}
		int damage = baseDamage;
		Card sourceCard = source.getSourceCard();
		if (!ignoreSpellDamage && sourceCard != null) {
			if (sourceCard.getCardType().isCardType(CardType.SPELL)) {
				damage = applySpellpower(player, source, baseDamage);
			} else if (sourceCard.getCardType().isCardType(CardType.HERO_POWER)) {
				damage = applyHeroPowerDamage(player, damage);
			}
			if (sourceCard.getCardType().isCardType(CardType.SPELL) || sourceCard.getCardType().isCardType(CardType.HERO_POWER)) {
				damage = applyAmplify(player, damage, Attribute.SPELL_AMPLIFY_MULTIPLIER);
			}
		}
		int damageDealt = 0;
		if (target.hasAttribute(Attribute.TAKE_DOUBLE_DAMAGE)) {
			damage *= 2;
		}
		context.getDamageStack().push(damage);
		context.fireGameEvent(new PreDamageEvent(context, target, source, damage));
		damage = context.getDamageStack().pop();
		if (damage > 0) {
			source.getAttributes().remove(Attribute.STEALTH);
		}
		switch (target.getEntityType()) {
			case MINION:
				damageDealt = damageMinion(player, damage, source, (Actor) target);
				break;
			case HERO:
				damageDealt = damageHero((Hero) target, damage);
				break;
			default:
				break;
		}

		target.setAttribute(Attribute.LAST_HIT, damageDealt);
		return damageDealt;
	}

	private int damageHero(Hero hero, int damage) {
		if (hero.hasAttribute(Attribute.IMMUNE) || hasAttribute(context.getPlayer(hero.getOwner()), Attribute.IMMUNE_HERO)) {
			log("{} is IMMUNE and does not take damage", hero);
			return 0;
		}
		int effectiveHp = hero.getHp() + hero.getArmor();
		final int armorChange = hero.modifyArmor(-damage);
		if (armorChange != 0) {
			context.fireGameEvent(new ArmorChangedEvent(context, hero, armorChange));
		}
		int newHp = Math.min(hero.getHp(), effectiveHp - damage);
		hero.setHp(newHp);
		log(hero.getName() + " receives " + damage + " damage, hp now: " + hero.getHp() + "(" + hero.getArmor() + ")");
		return damage;
	}

	@Suspendable
	private int damageMinion(Player player, int damage, Entity source, Actor minion) {
		if (minion.hasAttribute(Attribute.DIVINE_SHIELD)) {
			removeAttribute(minion, Attribute.DIVINE_SHIELD);
			log("{}'s DIVINE SHIELD absorbs the damage", minion);
			context.fireGameEvent(new LoseDivineShieldEvent(context, minion, player.getId(), source.getId()));
			return 0;
		}
		if (minion.hasAttribute(Attribute.IMMUNE)) {
			log("{} is IMMUNE and does not take damage", minion);
			return 0;
		}
		if (damage >= minion.getHp() && minion.hasAttribute(Attribute.CANNOT_REDUCE_HP_BELOW_1)) {
			damage = minion.getHp() - 1;
		}

		log("{} is damaged for {}", minion, damage);
		minion.setHp(minion.getHp() - damage);
		handleEnrage(minion);
		return damage;
	}

	/**
	 * Destroys the given targets, triggering their deathrattles if necessary.
	 *
	 * @param targets A list of {@link Actor} targets that should be destroyed.
	 * @see #checkForDeadEntities() for the code that actually finds dead entities as a result of effects and eventually
	 * destroys them.
	 */
	@Suspendable
	public void destroy(Actor... targets) {
		// Reverse the targets
		Map<Actor, EntityLocation> previousLocation = new HashMap<>();

		List<Actor> reversed = new ArrayList<>(Arrays.asList(targets));
		reversed.sort((a, b) -> -Integer.compare(a.getEntityLocation().getIndex(), b.getEntityLocation().getIndex()));

		for (Actor target : reversed) {
			removeEnchantments(target, false);
			previousLocation.put(target, target.getEntityLocation());
			log("{} is destroyed", target);
			target.moveOrAddTo(context, Zones.GRAVEYARD);
		}

		for (int i = 0; i < targets.length; i++) {
			Actor target = targets[i];
			Player owner = context.getPlayer(target.getOwner());
			switch (target.getEntityType()) {
				case HERO:
					log("Hero {} has been destroyed.", target.getName());
					applyAttribute(target, Attribute.DESTROYED);
					applyAttribute(context.getPlayer(target.getOwner()), Attribute.DESTROYED);
					break;
				case MINION:
					context.getEnvironment().put(Environment.KILLED_MINION, target.getReference());
					KillEvent killEvent = new KillEvent(context, target);
					context.fireGameEvent(killEvent);
					context.getEnvironment().remove(Environment.KILLED_MINION);
					applyAttribute(target, Attribute.DESTROYED);
					target.setAttribute(Attribute.DIED_ON_TURN, context.getTurn());
					break;
				case WEAPON:
					destroyWeapon((Weapon) target);
					break;
				case ANY:
				default:
					logger.error("Trying to destroy unknown entity type {}", target.getEntityType());
					break;
			}

			resolveDeathrattles(owner, target, previousLocation.get(target));
		}
		for (Actor target : targets) {
			removeEnchantments(target, true);
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	@Suspendable
	private void destroyWeapon(Weapon weapon) {
		Player owner = context.getPlayer(weapon.getOwner());
		if (owner.getHero().getWeapon() != null && owner.getHero().getWeapon().getId() == weapon.getId()) {
			owner.getHero().setWeapon(null);
		}
		weapon.onUnequip(context, owner);
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
	 * Discards a card from your hand, either through discard card effects or "overdraw" (forced destruction of cards
	 * due to too many cards in your hand).
	 * <p>
	 * Discarded cards are removed from the game, without activating Deathrattles. Discard effects are most commonly
	 * found on warlock cards. Discard effects are distinguished from overdraw, and Fel Reaver's remove from deck
	 * effect, both of which remove cards directly from the deck without entering the hand; and from Tracking's
	 * "discard" effect, which in fact removes cards directly from a special display zone without entering the hand.
	 * While similar to discard effects, neither is considered a discard for game purposes, and will not activate
	 * related effects.
	 * <p>
	 * This method handles all situations and correctly triggers a {@link DiscardEvent} only when a card is discarded
	 * from the hand.
	 *
	 * @param player The player that owns the card getting discarded.
	 * @param card   The card to discard.
	 * @see #receiveCard(int, Card) for more on receiving and drawing cards.
	 */
	@Suspendable
	public void discardCard(Player player, Card card) {
		logger.debug("{} discards {}", player.getName(), card);
		// only a 'real' discard should fire a DiscardEvent
		if (card.getZone() == Zones.HAND) {
			card.getAttributes().put(Attribute.DISCARDED, true);
			context.fireGameEvent(new DiscardEvent(context, player.getId(), card));
			player.getStatistics().cardDiscarded();
		} else if (card.getZone() == Zones.DECK) {
			context.fireGameEvent(new MillEvent(context, player.getId(), card));
		}

		removeCard(card);
	}

	/**
	 * Draws a card for a player from the deck to the hand.
	 * <p>
	 * When a {@link Deck} is empty, the player's {@link Hero} takes "fatigue" damage, which increases by 1 every time a
	 * card should have been drawn but is not.
	 *
	 * @param playerId The player who should draw a card.
	 * @param source   The card that is the origin of the drawing effect, or {@code null} if this is the draw from the
	 *                 beginning of a turn
	 * @return The card that was drawn.
	 * @see #receiveCard(int, Card) for the full rules on receiving cards into the hand.
	 */
	@Suspendable
	public Card drawCard(int playerId, Entity source) {
		Player player = context.getPlayer(playerId);
		CardList deck = player.getDeck();
		if (checkAndDealFatigue(player)) {
			return null;
		}

		Card card = getRandom(deck);
		return drawCard(playerId, card, source);
	}

	/**
	 * Checks if the player's deck is empty. If it is, increments the fatigue amount and deals fatigue damange.
	 * <p>
	 * Fatigue is a game mechanic that deals increasing damage to players who have already drawn all of the cards in
	 * their deck, whenever they attempt to draw another card.
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
			Hero hero = player.getHero();
			int fatigue = player.hasAttribute(Attribute.FATIGUE) ? player.getAttributeValue(Attribute.FATIGUE) : 0;
			fatigue++;
			player.setAttribute(Attribute.FATIGUE, fatigue);
			damage(player, hero, fatigue, hero);
			log("{}'s deck is empty, taking {} fatigue damage!", player.getName(), fatigue);
			player.getStatistics().fatigueDamage(fatigue);
			return true;
		}
		return false;
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
		Player player = context.getPlayer(playerId);
		player.getStatistics().cardDrawn();
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

		Hero hero = player.getHero();
		hero.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
		hero.getAttributes().remove(Attribute.HERO_POWER_USAGES);
		handleFrozen(hero);
		for (Minion minion : player.getMinions()) {
			minion.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
			handleFrozen(minion);
		}
		player.getAttributes().remove(Attribute.COMBO);
		hero.activateWeapon(false);
		context.getEntities()
				.filter(actor -> actor.hasAttribute(Attribute.HEALING_THIS_TURN))
				.forEach(actor -> actor.getAttributes().remove(Attribute.HEALING_THIS_TURN));
		log("{} ends his turn.", player.getName());
		context.fireGameEvent(new TurnEndEvent(context, playerId));
		if (hasAttribute(player, Attribute.DOUBLE_END_TURN_TRIGGERS)) {
			context.fireGameEvent(new TurnEndEvent(context, playerId));
		}

		context.getCardCostModifiers().removeIf(CardCostModifier::isExpired);
		checkForDeadEntities();
	}

	/**
	 * Equips a {@link Weapon} for a {@link Hero}. Destroys the previous weapon if one was equipped and triggers its
	 * deathrattle effect.
	 *
	 * @param playerId         The player whose hero should equip the weapon.
	 * @param weapon           The weapon to equip.
	 * @param weaponCard
	 * @param resolveBattlecry If {@code true}, the weapon's battlecry {@link Spell} should be cast. This is {@code
	 *                         false} if the weapon was equipped due to some other effect (typically a random weapon
	 */
	@Suspendable
	public void equipWeapon(int playerId, Weapon weapon, WeaponCard weaponCard, boolean resolveBattlecry) {
		PreEquipWeapon preEquipWeapon = new PreEquipWeapon(playerId, weapon).invoke();
		Weapon currentWeapon = preEquipWeapon.getCurrentWeapon();
		Player player = preEquipWeapon.getPlayer();

		if (resolveBattlecry
				&& weapon.getBattlecry() != null) {
			resolveBattlecry(playerId, weapon);
			checkForDeadEntities();
		}

		postEquipWeapon(playerId, weapon, currentWeapon, player, weaponCard);
	}

	@Suspendable
	protected void postEquipWeapon(int playerId, Weapon newWeapon, Weapon currentWeapon, Player player, WeaponCard source) {
		if (currentWeapon != null) {
			log("{} discards currently equipped weapon {}", player.getHero(), currentWeapon);
			markAsDestroyed(currentWeapon);
		}

		player.getStatistics().equipWeapon(newWeapon);
		newWeapon.onEquip(context, player);
		newWeapon.setActive(context.getActivePlayerId() == playerId);
		if (newWeapon.hasEnchantment()) {
			List<Enchantment> enchantments = newWeapon.getEnchantments();
			for (Enchantment enchantment : enchantments) {
				addGameEventListener(player, enchantment, newWeapon);
			}
		}
		if (newWeapon.getCardCostModifier() != null) {
			addManaModifier(player, newWeapon.getCardCostModifier(), newWeapon);
		}
		context.fireGameEvent(new WeaponEquippedEvent(context, newWeapon, source));
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
	 * complex process. Some players use "attack" to describe any damage or negative action directed toward the enemy,
	 * but in game terminology only the standard combat action described here counts as an attack and triggers related
	 * effects. Attacking in Hearthstone is usually understood to represent physical combat, particularly melee combat,
	 * in contrast to combat via spells. "Hit" and "swing" are other informal terms for attacking, as in "hit the face"
	 * or "swing into a minion".
	 * <p>
	 * Each character involved in an attack deals {@link #damage(Player, Actor, int, Entity, boolean)} equal to its
	 * {@link Actor#getAttack()} stat to the other. Combat is the primary way for most minions to affect the game, by
	 * attacking either the enemy {@link Hero} or their {@link Minion}s. Minions deal their attack damage both
	 * offensively and defensively, making them potentially dangerous on both sides of combat. Heroes can be involved in
	 * combat as either an attacker or defender too, but all sources of hero attack power only apply on their own turn.
	 * Therefore, enemy minions can hit the hero without harm during the opponent's turn.
	 *
	 * @param player   The player who is initiating the fight.
	 * @param attacker The attacking {@link Actor}
	 * @param defender The defending {@link Actor}
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
	public void fight(Player player, Actor attacker, Actor defender) {
		log("{} attacks {}", attacker, defender);

		context.getEnvironment().put(Environment.ATTACKER_REFERENCE, attacker.getReference());

		TargetAcquisitionEvent targetAcquisitionEvent = new TargetAcquisitionEvent(context, player.getId(), ActionType.PHYSICAL_ATTACK,
				attacker, defender);
		context.fireGameEvent(targetAcquisitionEvent);
		Actor target = defender;
		if (context.getEnvironment().containsKey(Environment.TARGET_OVERRIDE)) {
			target = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE));
		}
		context.getEnvironment().remove(Environment.TARGET_OVERRIDE);

		if (target != defender) {
			log("Target of attack was changed! New Target: {}", target);
			// Override the defender here for the sake of readability
			defender = target;
		}

		if (attacker.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING)) {
			applyAttribute(attacker, Attribute.IMMUNE);
		}

		removeAttribute(attacker, Attribute.STEALTH);

		// Hearthstone checks for win/loss/draw.
		if (context.updateAndGetGameOver()) {
			return;
		}

		int attackerDamage = attacker.getAttack();
		int defenderDamage = defender.getAttack();
		context.fireGameEvent(new PhysicalAttackEvent(context, attacker, defender, attackerDamage));
		// secret may have killed attacker ADDENDUM: or defender
		if (attacker.isDestroyed() || defender.isDestroyed()) {
			context.getEnvironment().remove(Environment.ATTACKER_REFERENCE);
			return;
		}

		if (defender.getOwner() == Entity.NO_OWNER) {
			logger.error("defender has no owner!! {}", defender);
		}

		// No events are fired by applying damage to an actor so it doesn't actually matter what order this occurs in
		// This could change, theoretically, if the minion has an ability  whose damage depends on the other minion's HP.
		int damageDealtToAttacker = applyDamageToActor(attacker, defenderDamage, player, defender, true);
		int damageDealtToDefender = applyDamageToActor(defender, attackerDamage, player, attacker, true);
		// Defender queues first
		resolveDamageEvent(context.getPlayer(defender.getOwner()), defender, attacker, damageDealtToDefender);
		resolveDamageEvent(context.getPlayer(attacker.getOwner()), attacker, defender, damageDealtToAttacker);

		if (attacker.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING)) {
			attacker.getAttributes().remove(Attribute.IMMUNE);
		}

		if (attacker.getEntityType() == EntityType.HERO) {
			Hero hero = (Hero) attacker;
			Weapon weapon = hero.getWeapon();
			if (weapon != null && weapon.isActive() && !weapon.hasAttribute(Attribute.IMMUNE)) {
				modifyDurability(hero.getWeapon(), -1);
			}
		}
		attacker.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, -1);
		context.fireGameEvent(new AfterPhysicalAttackEvent(context, attacker, defender, damageDealtToDefender));
		context.getEnvironment().remove(Environment.ATTACKER_REFERENCE);
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
	 * For heroes that have a {@link HeroPowerCard} that has automatic target selection, returns the hero power. It is
	 * not clear if this is used by any hero power cards in the game.
	 *
	 * @param playerId The player equipped with an auto hero power.
	 * @return The action to play the hero power.
	 */
	@Suspendable
	public GameAction getAutoHeroPowerAction(int playerId) {
		return actionLogic.getAutoHeroPower(context, context.getPlayer(playerId));
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
	private int getGreatestAttributeValue(Player player, Attribute attr) {
		int greatest = Math.max(INFINITE, player.getHero().getAttributeValue(attr));
		if (greatest == INFINITE) {
			return greatest;
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
	 *
	 * @param player The player whose point of view to consider for the card cost.
	 * @param card   The card to cost.
	 * @return The modified mana cost of the card.
	 */
	public int getModifiedManaCost(Player player, Card card) {
		int manaCost = card.getManaCost(context, player);
		int minValue = 0;
		for (CardCostModifier costModifier : context.getCardCostModifiers()) {
			if (!costModifier.appliesTo(card)) {
				continue;
			}
			manaCost = costModifier.process(card, manaCost);
			if (costModifier.getMinValue() > minValue) {
				minValue = costModifier.getMinValue();
			}
		}
		if (card.hasAttribute(Attribute.MANA_COST_MODIFIER)) {
			manaCost += card.getAttributeValue(Attribute.MANA_COST_MODIFIER);
		}
		manaCost = MathUtils.clamp(manaCost, minValue, Integer.MAX_VALUE);
		return manaCost;
	}

	/**
	 * Gets a list of secrets for a player.
	 *
	 * @param player The player whose point of view to query for secrets.
	 * @return The secrets as {@link Trigger}
	 * @see Player#getSecrets() for a more reliable way to get the {@link Secret} entities that are in play for a
	 * player.
	 */
	private List<Trigger> getSecrets(Player player) {
		List<Trigger> secrets = context.getTriggersAssociatedWith(player.getHero().getReference());
		for (Iterator<Trigger> iterator = secrets.iterator(); iterator.hasNext(); ) {
			Trigger trigger = iterator.next();
			if (!(trigger instanceof Secret)) {
				iterator.remove();
			}
		}
		return secrets;
	}

	private int getTotalAttributeValue(Player player, Attribute attr) {
		int total = player.getHero().getAttributeValue(attr);
		for (Entity minion : player.getMinions()) {
			if (!minion.hasAttribute(attr)) {
				continue;
			}

			total += minion.getAttributeValue(attr);
		}
		return total;
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

	@Suspendable
	private void handleEnrage(Actor entity) {
		if (!entity.hasAttribute(Attribute.ENRAGABLE)) {
			return;
		}
		boolean enraged = entity.getHp() < entity.getMaxHp();
		// enrage publicState has not changed; do nothing
		if (entity.hasAttribute(Attribute.ENRAGED) == enraged) {
			return;
		}

		if (enraged) {
			log("{} is now enraged", entity);
			entity.setAttribute(Attribute.ENRAGED);
		} else {
			log("{} is no longer enraged", entity);
			entity.getAttributes().remove(Attribute.ENRAGED);
		}

		context.fireGameEvent(new EnrageChangedEvent(context, entity));
	}

	@Suspendable
	private void handleFrozen(Actor actor) {
		if (!actor.hasAttribute(Attribute.FROZEN)) {
			return;
		}
		if (actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) >= actor.getMaxNumberOfAttacks()) {
			removeAttribute(actor, Attribute.FROZEN);
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
	public void heal(Player player, Actor target, int healing, Entity source) {
		heal(player, target, healing, source, true);
	}

	/**
	 * Heals (restores hitpoints to) a target.
	 * <p>
	 * Healing an {@link Actor} will increase their {@link Actor#getHp()} by the stated amount, up to but not beyond
	 * their current {@link Actor#getMaxHp()}.
	 * <p>
	 * Healing comes from battlecries, deathrattles, spell triggers, hero powers and spell cards that cast a {@link
	 * HealSpell}. Most healing effects affect a single {@link Actor} (these effects can be targetable or select the
	 * target automatically or at random), while some others have an area of effect.
	 * <p>
	 * Healing is distinct from granting a minion increased hitpoints, which increases both the current and maximum
	 * Health for the target. Increasing a minion's hitpoints is usually achieved through enchantments (or removing them
	 * through {@link SilenceSpell}), while healing is usually achieved through effects.
	 * <p>
	 * Although healing effects (including targetable ones) can target undamaged characters, attempting to restore
	 * hitpoints to an {@link Actor} already at their current maximum Health will have no effect and will not count as
	 * healing for game purposes (for example, on-heal triggers such as Lightwarden's {@link HealingTrigger} will not
	 * trigger).
	 * <p>
	 * Healing a character to full hitpoints will remove its damaged status and thus any {@link Attribute#ENRAGED}
	 * effect currently active, which can be very useful for denying enemy minions' Enrage effects.
	 *
	 * @param player                    The player who chose the target of the healing.
	 * @param target                    The target of the healing.
	 * @param healing                   The amount of healing.
	 * @param source                    The {@link Entity}, typically a {@link SpellCard} or {@link Minion} with
	 *                                  battlecry, that is the source of the healing.
	 * @param applyHealingAmplification Whether or not to compute the effects of {@link Attribute#HEAL_AMPLIFY_MULTIPLIER}
	 *                                  on this card.
	 * @see Attribute#ENRAGED for more about enrage.
	 */
	@Suspendable
	public void heal(Player player, Actor target, int healing, Entity source, boolean applyHealingAmplification) {
		if (hasAttribute(player, Attribute.INVERT_HEALING)) {
			log("All healing inverted, deal damage instead!");
			damage(player, target, healing, source);
			return;
		}
		if (applyHealingAmplification
				&& source != null
				&& source instanceof Card
				&& (((Card) source).getCardType().isCardType(CardType.SPELL)
				|| ((Card) source).getCardType().isCardType(CardType.HERO_POWER))) {
			healing = applyAmplify(player, healing, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		}
		boolean success = false;
		switch (target.getEntityType()) {
			case MINION:
				success = healMinion((Actor) target, healing);
				break;
			case HERO:
				success = healHero((Hero) target, healing);
				break;
			default:
				break;
		}

		if (success) {
			HealEvent healEvent = new HealEvent(context, player.getId(), target, healing);
			// Implements Happy Ghoul
			target.modifyAttribute(Attribute.HEALING_THIS_TURN, healing);
			target.setAttribute(Attribute.LAST_HEAL, healing);
			context.fireGameEvent(healEvent);
			player.getStatistics().heal(healing);
		}
	}

	private boolean healHero(Hero hero, int healing) {
		int newHp = Math.min(hero.getMaxHp(), hero.getHp() + healing);
		int oldHp = hero.getHp();
		if (logger.isDebugEnabled()) {
			log(hero + " is healed for " + healing + ", hp now: " + newHp / hero.getMaxHp());
		}

		hero.setHp(newHp);
		return newHp != oldHp;
	}

	@Suspendable
	private boolean healMinion(Actor minion, int healing) {
		int newHp = Math.min(minion.getMaxHp(), minion.getHp() + healing);
		int oldHp = minion.getHp();
		if (logger.isDebugEnabled()) {
			log(minion + " is healed for " + healing + ", hp now: " + newHp + "/" + minion.getMaxHp());
		}

		minion.setHp(newHp);
		handleEnrage(minion);
		return newHp != oldHp;
	}

	/**
	 * Starts a game for the given player by requesting a mulligan and setting up all the {@link
	 * net.demilich.metastone.game.spells.trigger.GameStartTrigger} and {@link Attribute#DECK_TRIGGER} cards.
	 *
	 * @param playerId The player to start the game for.
	 * @param begins   {@code true} if this player is starting the game and should start with {@link #STARTER_CARDS}
	 *                 cards. {@code false} if this player is not starting the game and should get {@link
	 *                 #STARTER_CARDS} + 1 cards.
	 */
	@Suspendable
	public void init(int playerId, boolean begins) {
		mulligan(context.getPlayer(playerId), begins);

		startGameForPlayer(context.getPlayer(playerId));
	}

	@Suspendable
	protected void startGameForPlayer(Player player) {
		player.setAttribute(Attribute.GAME_STARTED);
		for (Card card : player.getDeck()) {
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
		}
		for (Card card : player.getHand()) {
			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
		}

		GameStartEvent gameStartEvent = new GameStartEvent(context, player.getId());
		context.fireGameEvent(gameStartEvent);
	}

	/**
	 * Configures the player {@link Player}, {@link Hero}, and deck & hand {@link Card} entities with the correct IDs,
	 * {@link EntityZone} locations and owners. Shuffles the deck.
	 *
	 * @param playerId The player that should be initialized.
	 * @return The initialized {@link Player} object.
	 */
	@Suspendable
	public Player initializePlayer(int playerId) {
		Player player = context.getPlayer(playerId);
		player.setOwner(player.getId());
		Hero hero = player.getHero();
		player.getHero().setId(getIdFactory().generateId());
		player.getHero().setOwner(player.getId());
		player.getHero().setMaxHp(player.getHero().getAttributeValue(Attribute.BASE_HP));
		player.getHero().setHp(player.getHero().getAttributeValue(Attribute.BASE_HP));
		hero.getHeroPower().setId(getIdFactory().generateId());
		assignCardIds(player.getDeck(), playerId);
		assignCardIds(player.getHand(), playerId);

		// Implements Open the Waygate
		Stream.concat(player.getDeck().stream(),
				player.getHand().stream()).forEach(c -> c.getAttributes().put(Attribute.STARTED_IN_DECK, true));

		log("Setting hero hp to {} for {}", hero.getHp(), player.getName());
		player.getDeck().shuffle();
		if (player.getHero().hasEnchantment()) {
			for (Enchantment trigger : player.getHero().getEnchantments()) {
				addGameEventListener(player, trigger, player.getHero());
			}
		}
		processPassiveTriggers(player, hero.getHeroPower());
		return player;
	}

	/**
	 * Should event logging be enabled? This logging lets you debug what has happened in the game, but comes with a high
	 * performance cost--lots of memory used on big strings.
	 *
	 * @return {@code true} if logging should be enabled.
	 */
	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	/**
	 * A joust describes when cards are revealed from each player's deck, and the "winner" of a joust is determined by
	 * whoever draws a card with a higher {@link Card#getBaseManaCost()}.
	 *
	 * @param player     The player who initiated the joust.
	 * @param cardFilter
	 * @param source
	 * @return The joust event that was fired.
	 */
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
			log("Jousting LOST - no minion card left");
		} else {
			Player opponent = context.getOpponent(player);
			CardList opponentCards = opponent.getDeck().filtered(c -> cardFilter.matches(context, opponent, c, source));
			if (opponentCards.size() > 0) {
				opponentCard = getRandom(opponentCards);
			}
			// opponent has no minions left in deck - automatically win joust
			if (opponentCard == null) {
				won = true;
				log("Jousting WON - opponent has no minion card left");
			} else {
				// both players have minion cards left, the initiator needs to
				// have the one with
				// higher mana cost to win the joust
				won = ownCard.getBaseManaCost() > opponentCard.getBaseManaCost();

				log("Jousting {} - {} vs. {}", won ? "WON" : "LOST", ownCard, opponentCard);
			}
		}
		JoustEvent joustEvent = new JoustEvent(context, player.getId(), won, ownCard, opponentCard);
		context.fireGameEvent(joustEvent);
		return joustEvent;
	}

	private void log(String message) {
		logToDebugHistory(message);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	private void log(String message, Object param1) {
		logToDebugHistory(message, param1);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1);
		}
	}

	protected void log(String message, Object param1, Object param2) {
		logToDebugHistory(message, param1, param2);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1, param2);
		}
	}

	private void log(String message, Object param1, Object param2, Object param3) {
		logToDebugHistory(message, param1, param2, param3);
		if (isLoggingEnabled() && logger.isDebugEnabled()) {
			logger.debug(message, param1, param2, param3);
		}
	}

	private void logToDebugHistory(String message, Object... params) {
		if (!isLoggingEnabled()) {
			return;
		}
		if (debugHistory.size() == MAX_HISTORY_ENTRIES) {
			debugHistory.poll();
		}
		if (params != null && params.length > 0) {
			message = message.replaceAll("\\{\\}", "%s");
			message = String.format(message, params);
		}

		debugHistory.add(message);
	}

	/**
	 * Marks an {@link Actor} as destroyed. Used for "Destroy" effects.
	 * <p>
	 * An actor marked this way gets moved to the {@link Zones#GRAVEYARD} by a {@link #checkForDeadEntities()} call.
	 *
	 * @param target The {@link Actor} to mark as destroyed.
	 */
	public void markAsDestroyed(Actor target) {
		if (target != null) {
			target.setAttribute(Attribute.DESTROYED);
		}
	}

	/**
	 * Mind control moves a {@link Minion} from the opponent's {@link Zones#BATTLEFIELD} to their own battlefield and
	 * puts it under control of the given {@link Player}.
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
	 * If a player activates a mind control effect when their side of the battlefield is already full (i.e. they have
	 * the maximum 7 minions), the mind controlled minion will be instantly destroyed. Any Deathrattle that activates as
	 * a result of this will trigger as if their opponent still controlled the minion. It is often a good idea for a
	 * player to choose to intentionally destroy one of their own minions in order to be able to seize control of one of
	 * their opponent's, especially by sacrificing a weak minion in order to gain control of a very powerful one.
	 * <p>
	 * As with summoning effects such as Mirror Image and Feral Spirit, mind controlled minions will always join the
	 * board on the far right. Anticipating this can allow for superior placement of minions, important for positional
	 * effects. When planning to summon other minions that turn, the player can use the timing of the mind control
	 * effect to allow them to determine the final placement of the mind controlled minion. For example, a player with a
	 * Shieldbearer already on the board may take control of a Flametongue Totem, before then summoning a Sludge Belcher
	 * to the right of it, thereby ensuring the Totem's is placed between the two minions, making the most of its buff.
	 * <p>
	 * Minions that have just been mind controlled are normally {@link Attribute#SUMMONING_SICKNESS} for one turn and
	 * cannot attack, just as with minions that were summoned that turn. However, Shadow Madness and Potion of Madness
	 * do not cause its target to be {@link Attribute#SUMMONING_SICKNESS}, allowing it to attack - the effect only lasts
	 * until end of turn, and would otherwise be nearly useless. Charge affects mind control exhaustion just as it
	 * affects {@link Attribute#SUMMONING_SICKNESS} - minions with that ability can attack on the same turn they are
	 * mind controlled.
	 *
	 * @param player The new owner of a minion.
	 * @param minion The minion to mind control.
	 */
	@Suspendable
	public void mindControl(Player player, Minion minion) {
		log("{} mind controls {}", player.getName(), minion);
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
	 * Steals the card, transferring its owner and moving its current zones. Keeps all associated {@link Trigger}
	 * objects and changes all trigger owners whose {@link Trigger#hasPersistentOwner()} property is {@code false}.
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
	public void stealCard(Player newOwner, Entity source, Card card, Zones destination) throws IllegalArgumentException {

		// If the card isn't already in the SET_ASIDE_ZONE, move it
		if (card.getZone() != Zones.SET_ASIDE_ZONE
				&& card.getEntityLocation().getPlayer() != newOwner.getId()) {
			// Move to set aside zone first.
			context.getPlayer(card.getOwner()).getZone(card.getZone()).remove(card);
			newOwner.getSetAsideZone().add(card);
		}

		// Only change the owner if necessary
		if (card.getOwner() != newOwner.getId()) {
			changeOwner(card, newOwner.getId());
		}

		// Move to the destination
		if (destination == Zones.HAND) {
			receiveCard(newOwner.getId(), card, source, false);
		} else if (destination == Zones.DECK) {
			// Remove again to make shuffling to deck valid.
			context.getPlayer(card.getOwner()).getZone(card.getZone()).remove(card);
			shuffleToDeck(newOwner, card);
		} else if (destination != Zones.SET_ASIDE_ZONE) {
			throw new IllegalArgumentException(String.format("Invalid destination %s for card %s", destination.name(), card.getName()));
		}
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
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Modifies the current mana that the player has.
	 *
	 * @param playerId The player whose mana should be modified.
	 * @param mana     The amount to increment or decrement the mana by.
	 */
	public void modifyCurrentMana(int playerId, int mana) {
		Player player = context.getPlayer(playerId);
		int newMana = Math.min(player.getMana() + mana, MAX_MANA);
		player.setMana(newMana);
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
		log("Durability of weapon {} is changed by {}", weapon, durability);

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
	public void modifyMaxHp(Actor actor, int value) {
		actor.setMaxHp(value);
		actor.setHp(value);
		handleEnrage(actor);
	}

	/**
	 * Increment or decrement the {@link Player#getMaxMana()} property of a {@link Player}
	 *
	 * @param player The player
	 * @param delta  The amount to increment or decrement the amount of mana the player has.
	 */
	public void modifyMaxMana(Player player, int delta) {
		log("Maximum mana was changed by {} for {}", delta, player.getName());
		final int maxMana = MathUtils.clamp(player.getMaxMana() + delta, 0, GameLogic.MAX_MANA);
		final int initialMaxMana = player.getMaxMana();
		final int change = maxMana - initialMaxMana;
		player.setMaxMana(maxMana);
		if (delta < 0 && player.getMana() > player.getMaxMana()) {
			modifyCurrentMana(player.getId(), delta);
		}
		if (change != 0) {
			context.fireGameEvent(new MaxManaChangedEvent(context, player.getId(), change));
		}
	}

	@Suspendable
	protected void mulligan(Player player, boolean begins) {
		FirstHand firstHand = new FirstHand(player, begins).invoke();

		List<Card> discardedCards = player.getBehaviour().mulligan(context, player, firstHand.getStarterCards());

		handleMulligan(player, begins, firstHand, discardedCards);
	}

	@Suspendable
	protected void handleMulligan(Player player, boolean begins, FirstHand firstHand, List<Card> discardedCards) {
		// Get the entity ids of the discarded cards and then replace the discarded cards with them
		final Map<Integer, Entity> setAsideZone = player.getSetAsideZone().stream().collect(Collectors.toMap(Entity::getId, Function.identity()));
		discardedCards = discardedCards.stream().map(Card::getId).map(setAsideZone::get).map(e -> (Card) e).collect(toList());

		// The starter cards have been put into the setAsideZone
		List<Card> starterCards = firstHand.getStarterCards();
		int numberOfStarterCards = firstHand.getNumberOfStarterCards();

		// remove player selected cards from starter cards
		for (Card discardedCard : discardedCards) {
			log("Player {} mulligans {} ", player.getName(), discardedCard);
			starterCards.removeIf(c -> c.getId() == discardedCard.getId());
		}

		// draw random cards from deck until required starter card count is
		// reached
		while (starterCards.size() < numberOfStarterCards) {
			Card randomCard = getRandom(player.getDeck());
			player.getDeck().move(randomCard, player.getSetAsideZone());
			starterCards.add(randomCard);
		}

		// put the networkRequestMulligan cards back in the deck
		for (Card discardedCard : discardedCards) {
			player.getSetAsideZone().move(discardedCard, player.getDeck());
		}

		for (Card starterCard : starterCards) {
			if (starterCard != null) {
				receiveCard(player.getId(), starterCard);
			}
		}

		player.getDeck().shuffle();

		// second player gets the coin additionally
		if (!begins) {
			Card theCoin = CardCatalogue.getCardById("spell_the_coin");
			receiveCard(player.getId(), theCoin);
		}
	}

	/**
	 * Gets the complete log stored in the debug history.
	 */
	public void logDebugHistory() {
		for (String entry : debugHistory) {
			logger.error(entry);
		}
	}

	/**
	 * Performs a game action, or a selection of what to do by a player from a list of {@link #getValidActions(int)}.
	 * <p>
	 * This method is the primary entry point to turn a player's selected {@link GameAction} into modified game state.
	 * Typically this method will call the action's {@link GameAction#execute(GameContext, int)} overrider, and the
	 * {@link GameAction} will then call {@link GameLogic} methods again to do its business. This is a bit of a
	 * rigamarole and should probably be changed.
	 *
	 * @param playerId The player performing the game action.
	 * @param action   The game action to perform.
	 * @see #getValidActions(int) for the way the {@link GameLogic} determines what actions a player can take.
	 * @see Card#play() for an example of how a card generates a {@link PlayCardAction} that will eventually be sent to
	 * this method.
	 * @see SpellUtils#discoverCard(GameContext, Player, SpellDesc, CardList) for an example of how a discover mechanic
	 * generates a {@link DiscoverAction} that gets sent to this method.
	 * @see Minion#getBattlecry() for the method that creates battlecry actions. (Note: Deathrattles never involve a
	 * player decision, so deathrattles never generate a battlecry).
	 * @see ChooseOneCard#playOptions() for the choose one mechanic's way of creating a {@link PlayCardAction} that
	 * corresponds to the choices on a choose one card.
	 */
	@Suspendable
	public void performGameAction(int playerId, GameAction action) {
		context.onWillPerformGameAction(playerId, action);
		if (isLoggingEnabled()) {
			debugHistory.add(action.toString());
		}

		if (playerId != context.getActivePlayerId()) {
			logger.warn("Player {} tries to perform an action, but it is not his turn!", context.getPlayer(playerId).getName());
		}
		if (action.getTargetRequirement() != TargetSelection.NONE) {
			Entity target = context.resolveSingleTarget(action.getTargetReference());
			if (target != null) {
				context.getEnvironment().put(Environment.TARGET, target.getReference());
			} else {
				context.getEnvironment().put(Environment.TARGET, null);
			}
		}

		action.execute(context, playerId);

		context.getEnvironment().remove(Environment.TARGET);
		if (action.getActionType() != ActionType.BATTLECRY) {
			checkForDeadEntities();
		}

		// Calculate how all the entities changed.

		context.onDidPerformGameAction(playerId, action);
	}

	/**
	 * Plays a card.
	 * <p>
	 * {@link #playCard(int, EntityReference)} is always initiated by an action, like a {@link PlayCardAction}. It
	 * represents playing a card from the hand. This method then deducts the appropriate amount of mana (or health,
	 * depending on the card). Then, it will check if the {@link SpellCard} was countered by Counter Spell (a {@link
	 * Secret} which adds a {@link Attribute#COUNTERED} attribute to the card that was raised in the {@link
	 * CardPlayedEvent}). It applies the {@link Attribute#OVERLOAD} amount to the mana the player has locked next turn.
	 * Finally, it removes the card from the player's {@link Zones#HAND} and puts it in the {@link Zones#GRAVEYARD}.
	 *
	 * @param playerId        The player that is playing the card.
	 * @param EntityReference The card that got played.
	 */
	@Suspendable
	public void playCard(int playerId, EntityReference EntityReference) {
		Player player = context.getPlayer(playerId);
		Card card = context.resolveEntityReference(EntityReference);

		int modifiedManaCost = getModifiedManaCost(player, card);
		if (card.getCardType().isCardType(CardType.SPELL)
				&& player.hasAttribute(Attribute.SPELLS_COST_HEALTH)) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		} else if ((Race) card.getAttribute(Attribute.RACE) == Race.MURLOC
				&& player.getHero().hasAttribute(Attribute.MURLOCS_COST_HEALTH)) {
			context.getEnvironment().put(Environment.LAST_MANA_COST, 0);
			damage(player, player.getHero(), modifiedManaCost, card, true);
		} else {
			context.getEnvironment().put(Environment.LAST_MANA_COST, modifiedManaCost);
			modifyCurrentMana(playerId, -modifiedManaCost);
			player.getStatistics().manaSpent(modifiedManaCost);
		}
		log("{} plays {}", player.getName(), card);

		player.getStatistics().cardPlayed(card, context.getTurn());
		CardPlayedEvent cardPlayedEvent = new CardPlayedEvent(context, playerId, card);
		context.fireGameEvent(cardPlayedEvent);

		if (card.hasAttribute(Attribute.OVERLOAD)) {
			context.fireGameEvent(new OverloadEvent(context, playerId, card, card.getAttributeValue(Attribute.OVERLOAD)));
		}

		removeCard(card);

		if ((card.getCardType().isCardType(CardType.SPELL))) {
			GameEvent spellCastedEvent = new SpellCastedEvent(context, playerId, card);
			context.fireGameEvent(spellCastedEvent);
			if (card.hasAttribute(Attribute.COUNTERED)) {
				log("{} was countered!", card.getName());
				return;
			}
		}

		if (card.hasAttribute(Attribute.OVERLOAD)) {
			player.modifyAttribute(Attribute.OVERLOAD, card.getAttributeValue(Attribute.OVERLOAD));
			// Implements Snowfury Giant
			player.modifyAttribute(Attribute.OVERLOADED_THIS_GAME, card.getAttributeValue(Attribute.OVERLOAD));
		}
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
	 * The caller is responsible for enforcing that fewer than {@link #MAX_SECRETS} are in play; and, that the {@link
	 * SecretCard} is discarded. The {@link SecretPlayedEvent} is not censored here and has sensitive information that
	 * cannot be shown to the opponent.
	 *
	 * @param player   The player whose gaining the secret.
	 * @param secret   The secret being played.
	 * @param fromHand When {@code true}, a {@link SecretPlayedEvent} is fired; otherwise, the event is not fired.
	 * @see net.demilich.metastone.game.spells.AddSecretSpell#onCast(GameContext, Player, SpellDesc, Entity, Entity) for
	 * the place where secret entities are created. A {@link SecretCard} uses this spell to actually create a {@link
	 * Secret}.
	 */
	@Suspendable
	public void playSecret(Player player, Secret secret, boolean fromHand) {
		log("{} has a new secret activated: {}", player.getName(), secret.getSourceCard());
		Secret newSecret = secret.clone();
		newSecret.setId(getIdFactory().generateId());
		newSecret.setOwner(player.getId());
		addGameEventListener(player, newSecret, player.getHero());
		player.getSecrets().add(newSecret);
		if (fromHand) {
			context.fireGameEvent(new SecretPlayedEvent(context, player.getId(), (SecretCard) newSecret.getSourceCard()));
		}
	}

	void processTargetModifiers(Player player, GameAction action) {
		HeroPowerCard heroPower = player.getHero().getHeroPower();
		if (heroPower.getHeroClass() != HeroClass.GREEN) {
			return;
		}
		if (action.getActionType() == ActionType.HERO_POWER && hasAttribute(player, Attribute.HERO_POWER_CAN_TARGET_MINIONS)) {
			PlaySpellCardAction spellCardAction = (PlaySpellCardAction) action;
			SpellDesc targetChangedSpell = spellCardAction.getSpell().removeArg(SpellArg.TARGET);
			spellCardAction.setSpell(targetChangedSpell);
			spellCardAction.setTargetRequirement(TargetSelection.ANY);
		}
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
	public <T> T removeRandom(Bag<T> weightedOptions) {
		if (weightedOptions.size() == 0) {
			return null;
		}

		// Still faster than creating and removing copies from a list
		int index = getRandom().nextInt(weightedOptions.size());
		Iterator<T> iterator = weightedOptions.iterator();
		while (index > 0) {
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
	 * @param source   The {@link Entity} that caused the card to be received, or {@code null} if this is due to drawing
	 *                 a card at the beginning of a turn.
	 * @see #receiveCard(int, Card, Entity, boolean) for more complete rules.
	 */
	@Suspendable
	public void receiveCard(int playerId, Card card, Entity source) {
		receiveCard(playerId, card, source, false);
	}

	/**
	 * Receives a card into the player's hand, as though it was drawn. It moves a card from whatever current {@link
	 * Zones} zone it is in into the {@link Zones#HAND} zone. Implements the "Draw a card" text.
	 * <p>
	 * A card draw effect is an effect which causes the player to draw one or more cards directly from their deck. Cards
	 * with card draw effects are sometimes called "cantrips", after similar effects in other games.
	 * <p>
	 * Card draw effects are distinguished from generate effects, which place new cards into your hand without removing
	 * them from your deck; and from put into hand and put into battlefield effects, which place cards of a specific
	 * type into the hand or the battlefield directly from the player's deck, rather than simply drawing the next card
	 * in the deck. A few cards have special effects which trigger based on the drawing of cards.
	 * <p>
	 * Attempting to draw a card when you already have 10 cards in your hand will result in the drawn card being removed
	 * from play, something referred to as "overdraw". Overdrawn cards are revealed to both players, before the card is
	 * visually destroyed.
	 * <p>
	 * Overdrawing is similar to discarding, but does not count as a discard for game purposes. While discard effects
	 * remove cards from the hand, overdraw removes the card directly from the deck. Overdraw also does not count as
	 * card draw for game purposes, since the game never attempts to draw the card into the hand, but rather destroys it
	 * since there is no room.
	 * <p>
	 * Card draw effects draw from the top of the randomly ordered deck, unlike "put into battlefield" or "put into
	 * hand" effects, resulting in an even chance of getting any card remaining in the deck. However, it is possible to
	 * gain more control over drawing using Tracking, which gives the player a choice among the top three random draws.
	 *
	 * @param playerId
	 * @param card
	 * @param source
	 * @param drawn    {@code true} if the card was drawn by a deck-drawing process, otherwise {@code false}.
	 * @return The card that was received (the card may have been transformed after it was received).
	 */
	@Suspendable
	public Card receiveCard(int playerId, Card card, Entity source, boolean drawn) {
		Player player = context.getPlayer(playerId);
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(getIdFactory().generateId());
		}

		if (card.getOwner() == IdFactory.UNASSIGNED) {
			card.setOwner(playerId);
		}

		CardZone hand = player.getHand();

		if (hand.getCount() < MAX_HAND_CARDS) {
			processPassiveTriggers(player, card);

			log("{} receives card {}", player.getName(), card);
			card.moveOrAddTo(context, Zones.HAND);
			CardType sourceType = null;
			if (source instanceof Card) {
				Card sourceCard = (Card) source;
				sourceType = sourceCard.getCardType();
			}
			context.fireGameEvent(new DrawCardEvent(context, playerId, card, sourceType, drawn));
		} else {
			log("{} has too many cards on his hand, card destroyed: {}", player.getName(), card);
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
			for (TriggerDesc triggerDesc : card.getPassiveTriggers()) {
				Stream<Enchantment> existingTriggers = context.getTriggersAssociatedWith(card.getReference())
						.stream()
						.filter(t -> Enchantment.class.isAssignableFrom(t.getClass()))
						.map(t -> (Enchantment) t);

				if (existingTriggers.anyMatch(t -> t.getSourceCard().getCardId().equals(card.getCardId())
						&& t.getSpell().getSpellClass().equals(triggerDesc.spell.getSpellClass()))) {
					continue;
				}

				Enchantment enchantment = triggerDesc.create();
				enchantment.setSourceCard(card);
				addGameEventListener(player, enchantment, card);
			}
		}
	}

	/**
	 * Refreshes the number of attacks an {@link Actor} has, typically to 1 or the number of {@link Attribute#WINDFURY}
	 * attacks if the actor has Windfury.
	 *
	 * @param entity
	 */
	public void refreshAttacksPerRound(Actor entity) {
		int attacks = 1;
		if (entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			attacks = MEGA_WINDFURY_ATTACKS;
		} else if (entity.hasAttribute(Attribute.WINDFURY)) {
			attacks = WINDFURY_ATTACKS;
		}
		entity.setAttribute(Attribute.NUMBER_OF_ATTACKS, attacks);
	}

	/**
	 * Removes an attribute from an entity. Handles removing {@link Attribute#WINDFURY} and its impact on the number of
	 * attacks a minion can make.
	 *
	 * @param entity The entity to remove an attribute from.
	 * @param attr   The attribute to remove.
	 */
	public void removeAttribute(Entity entity, Attribute attr) {
		if (!entity.hasAttribute(attr)) {
			return;
		}
		if (attr == Attribute.MEGA_WINDFURY && entity.hasAttribute(Attribute.WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, WINDFURY_ATTACKS - MEGA_WINDFURY_ATTACKS);
		}
		if (attr == Attribute.WINDFURY && !entity.hasAttribute(Attribute.MEGA_WINDFURY)) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - WINDFURY_ATTACKS);
		} else if (attr == Attribute.MEGA_WINDFURY) {
			entity.modifyAttribute(Attribute.NUMBER_OF_ATTACKS, 1 - MEGA_WINDFURY_ATTACKS);
		}
		entity.getAttributes().remove(attr);
		log("Removing attribute {} from {}", attr, entity);
	}

	/**
	 * Moves a card to the {@link Zones#GRAVEYARD}. Removes each {@link Enchantment} associated with the card, if any.
	 * <p>
	 * No events are raised.
	 *
	 * @param card The card to move to the graveyard.
	 * @see #discardCard(Player, Card) for when cards are discarded from the hand or should otherwise raise events.
	 */
	@Suspendable
	public void removeCard(Card card) {
		log("Card {} has been moved from the {} to the GRAVEYARD", card, card.getEntityLocation().getZone().toString());
		removeEnchantments(card);
		// If it's already in the graveyard, do nothing more
		if (card.getEntityLocation().getZone() == Zones.GRAVEYARD
				|| card.getEntityLocation().getZone() == Zones.REMOVED_FROM_PLAY) {
			return;
		}
		// TODO: It's not necessarily in the hand when it's removed!
		card.moveOrAddTo(context, Zones.GRAVEYARD);
	}

	/**
	 * Moves the specified card from the player's {@link Zones#DECK} to their {@link Zones#GRAVEYARD}. Removes each
	 * {@link Enchantment} associated with the card, if any. Does not raise any events.
	 *
	 * @param playerId The player whose deck to check for this card
	 * @param card     The card to remove from this player's deck.
	 * @see #discardCard(Player, Card) for when cards are discarded from the hand or should otherwise raise events.
	 */
	@Suspendable
	public void removeCardFromDeck(int playerId, Card card) {
		Player player = context.getPlayer(playerId);
		log("Card {} has been moved from the DECK to the GRAVEYARD", card);
		removeEnchantments(card);
		if (card.getZone() != Zones.GRAVEYARD) {
			player.getDeck().move(card, player.getGraveyard());
		}
	}

	/**
	 * Removes an actor by moving it to...
	 * <p>
	 * <ul> <li>The {@link Zones#GRAVEYARD} if the actor is being removed {@code peacefully == false}. Also marks it
	 * {@link Attribute#DESTROYED}.</li> <li>The {@link Zones#SET_ASIDE_ZONE} if the actor is being removed {@code
	 * peacefully == true}. The caller is responsible for moving it elsewhere.</li> </ul>
	 * <p>
	 * Deathrattles are not triggered.
	 *
	 * @param actor      The actor to remove.
	 * @param peacefully If {@code true}, remove the card typically due to a {@link ReturnTargetToHandSpell}--that is,
	 *                   not due to a destruction of the minion. Otherwise, move the {@link Minion} to the {@link
	 *                   Zones#SET_ASIDE_ZONE} where it will be found by {@link #checkForDeadEntities()}.
	 * @see ReturnTargetToHandSpell for usage of {@link #removeActor(Actor, boolean)}. Note, this and {@link
	 * net.demilich.metastone.game.spells.ShuffleMinionToDeckSpell} appear to be the only two users of this function.
	 */
	@Suspendable
	public void removeActor(Actor actor, boolean peacefully) {
		removeEnchantments(actor);
		log("{} was removed", actor);
		if (actor instanceof Weapon
				&& peacefully) {
			// Move the weapon directly to the graveyard and don't trigger its deathrattle.
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
		log("All secrets for {} have been destroyed", player.getName());
		// this only works while Secrets are the only Enchantment on the heroes
		for (Trigger secret : getSecrets(player)) {
			secret.onRemove(context);
			context.removeTrigger(secret);
		}
		player.getSecrets().clear();
	}

	@Suspendable
	private void removeEnchantments(Entity entity) {
		removeEnchantments(entity, true);
	}

	@Suspendable
	private void removeEnchantments(Entity entity, boolean removeAuras) {
		EntityReference entityReference = entity.getReference();
		for (Trigger trigger : context.getTriggersAssociatedWith(entityReference)) {
			if (!removeAuras && trigger instanceof Aura) {
				continue;
			}
			log("Enchantment {} was removed for {}", trigger, entity);
			trigger.onRemove(context);
		}
		context.removeTriggersAssociatedWith(entityReference, removeAuras);
		context.getCardCostModifiers().removeIf(cardCostModifier -> cardCostModifier.getHostReference().equals(entityReference));
	}

	/**
	 * Replaces the specified old card with the specified new card. Deals with cards that have {@link
	 * Attribute#PASSIVE_TRIGGERS} (an in-hand trigger) correctly.
	 *
	 * @param playerId The player whose {@link Zones#HAND} will be manipulated.
	 * @param oldCard  The old {@link Card} to find and replace in this hand.
	 * @param newCard  The replacement card.
	 * @return A reference to the replacement, or {@code null} if no replacement was made..
	 */
	@Suspendable
	public Card replaceCardInHand(int playerId, Card oldCard, Card newCard) {
		Player player = context.getPlayer(playerId);
		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(getIdFactory().generateId());
		}

		if (!player.getHand().contains(oldCard)) {
			return null;
		}

		newCard.setOwner(playerId);
		CardList hand = player.getHand();
		log("{} replaces card {} with card {}", player.getName(), oldCard, newCard);
		hand.replace(oldCard, newCard);
		transferKeptEnchantments(oldCard, newCard);
		removeEnchantments(oldCard);
		oldCard.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		oldCard.getAttributes().put(Attribute.TRANSFORM_REFERENCE, newCard.getReference());
		processPassiveTriggers(player, newCard);
		context.fireGameEvent(new DrawCardEvent(context, playerId, newCard, null, false));
		return newCard;
	}

	protected void transferKeptEnchantments(Card oldCard, Card newCard) {
		context.getTriggersAssociatedWith(oldCard.getReference())
				.stream()
				.filter(t -> Enchantment.class.isAssignableFrom(t.getClass()))
				.map(t -> (Enchantment) t)
				.filter(Enchantment::isKeptAfterTransform)
				.forEach(e -> e.setHost(newCard));
	}

	/**
	 * Replaces the specified old card with the specified new card. Deals with cards that have {@link
	 * Attribute#DECK_TRIGGER} correctly.
	 *
	 * @param playerId The player whose {@link Zones#DECK} will be manipulated.
	 * @param oldCard  The old {@link Card} to find and replace in this deck.
	 * @param newCard  The replacement card.
	 */
	@Suspendable
	public void replaceCardInDeck(int playerId, Card oldCard, Card newCard) {
		Player player = context.getPlayer(playerId);
		if (newCard.getId() == IdFactory.UNASSIGNED) {
			newCard.setId(getIdFactory().generateId());
		}

		if (!player.getDeck().contains(oldCard)) {
			return;
		}

		newCard.setOwner(playerId);
		CardList deck = player.getDeck();

		if (newCard.getAttribute(Attribute.DECK_TRIGGER) != null) {
			TriggerDesc triggerDesc = (TriggerDesc) newCard.getAttribute(Attribute.DECK_TRIGGER);
			addGameEventListener(player, triggerDesc.create(), newCard);
		}

		log("{} replaces card {} with card {}", player.getName(), oldCard, newCard);
		deck.replace(oldCard, newCard);
		removeCardFromDeck(playerId, oldCard);
	}

	@Suspendable
	protected void resolveBattlecry(int playerId, Actor actor) {
		BattlecryAction battlecry = actor.getBattlecry();

		Player player = context.getPlayer(playerId);
		if (!battlecry.canBeExecuted(context, player)) {
			return;
		}

		battlecry.setSource(actor.getReference());

		if (battlecry.getTargetRequirement() != TargetSelection.NONE) {
			List<GameAction> battlecryActions = getTargetedBattlecryGameActions(battlecry, player);

			if (battlecryActions == null
					|| battlecryActions.size() == 0) {
				return;
			}

			BattlecryAction targetedBattlecry = (BattlecryAction) player.getBehaviour().requestAction(context, player, battlecryActions);
			performBattlecryAction(playerId, actor, player, targetedBattlecry);
		} else {
			performBattlecryAction(playerId, actor, player, battlecry);
		}
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
		if (hasAttribute(player, Attribute.DOUBLE_BATTLECRIES) && actor.getSourceCard().hasAttribute(Attribute.BATTLECRY)) {
			// You need DOUBLE_BATTLECRIES before your battlecry action, not after.
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
	public void resolveDeathrattles(Player player, Actor actor) {
		resolveDeathrattles(player, actor, actor.getEntityLocation());
	}

	/**
	 * Executes the deathrattle effect written on this {@link Actor}.
	 *
	 * @param player           The player that owns the actor.
	 * @param actor            The actor.
	 * @param previousLocation The position on the board the actor used to have. Important for adjacency deathrattle
	 */
	@Suspendable
	public void resolveDeathrattles(Player player, Actor actor, EntityLocation previousLocation) {
		int boardPosition = previousLocation.getIndex();
		if (!actor.hasAttribute(Attribute.DEATHRATTLES)) {
			return;
		}

		// Don't trigger deathrattles for entities in the set aside zone... unless it's a weapon
		if (previousLocation.getZone() == Zones.SET_ASIDE_ZONE
				&& !(actor instanceof Weapon)) {
			return;
		}

		boolean doubleDeathrattles = hasAttribute(player, Attribute.DOUBLE_DEATHRATTLES);
		EntityReference sourceReference = actor.getReference();
		for (SpellDesc deathrattleTemplate : actor.getDeathrattles()) {
			SpellDesc deathrattle = deathrattleTemplate.addArg(SpellArg.BOARD_POSITION_ABSOLUTE, boardPosition);
			castSpell(player.getId(), deathrattle, sourceReference, EntityReference.NONE, false);
			if (doubleDeathrattles) {
				// TODO: Likewise, with double deathrattles, make sure that we can still target whatever we're targeting in the spells (possibly metaspells!)
				castSpell(player.getId(), deathrattle, sourceReference, EntityReference.NONE, false);
			}
		}
	}

	/**
	 * This method is where the {@link GameLogic} handles the firing of a {@link Secret}. It removes the secret from
	 * play and raises a {@link SecretRevealedEvent}.
	 *
	 * @param player The player that owns the secret.
	 * @param secret The secret that got triggered.
	 * @see {@link Secret#onFire(int, SpellDesc, GameEvent)} for the code that handles when a secret is fired.
	 */
	@Suspendable
	public void secretTriggered(Player player, Secret secret) {
		log("Secret was trigged: {}", secret.getSourceCard());
		// Move the secret to removed from play.
		secret.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		context.fireGameEvent(new SecretRevealedEvent(context, (SecretCard) secret.getSourceCard(), player.getId()));
	}

	// TODO: circular dependency. Very ugly, refactor!
	public void setContext(GameContext context) {
		this.context = context;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	/**
	 * Implements a "Shuffle into deck" text. This will select a random location for the card to go without shuffling
	 * the deck (i.e., changing the existing order of the cards).
	 *
	 * @param player The player whose deck this card is getting shuffled into.
	 * @param card   The card to shuffle into that player's deck.
	 */
	@Suspendable
	public void shuffleToDeck(Player player, Card card) {
		if (card.getId() == IdFactory.UNASSIGNED) {
			card.setId(getIdFactory().generateId());
		}

		if (card.getOwner() == IdFactory.UNASSIGNED) {
			card.setOwner(player.getId());
		}

		int count = player.getDeck().getCount();
		if (count < MAX_DECK_SIZE) {
			if (count == 0) {
				player.getDeck().add(card);
			} else {
				player.getDeck().add(getRandom().nextInt(count), card);
			}

			if (card.getAttribute(Attribute.DECK_TRIGGER) != null) {
				TriggerDesc triggerDesc = (TriggerDesc) card.getAttribute(Attribute.DECK_TRIGGER);
				addGameEventListener(player, triggerDesc.create(), card);
			}
			log("Card {} has been shuffled to {}'s deck", card, player.getName());
		}
	}

	/**
	 * Silence is an ability which removes all current card text, enchantments, and abilities from the targeted minion.
	 * It does not remove damage or minion type.
	 *
	 * @param playerId The ID of the player (typically the owner of the target). This is used by {@link
	 *                 net.demilich.metastone.game.spells.custom.MindControlOneTurnSpell} to reverse the mind control of
	 *                 a minion that somehow gets silenced during the turn that spell is cast.
	 * @param target   A {@link Minion} to silence.
	 * @see <a href="http://hearthstone.gamepedia.com/Silence">Silence</a> for a complete description of the silencing
	 * game rules.
	 */
	@Suspendable
	public void silence(int playerId, Minion target) {
		context.fireGameEvent(new SilenceEvent(context, playerId, target));

		List<Attribute> tags = new ArrayList<Attribute>();
		tags.addAll(target.getAttributes().keySet());
		for (Attribute attr : tags) {
			if (immuneToSilence.contains(attr)) {
				continue;
			}
			removeAttribute(target, attr);
		}
		removeEnchantments(target);
		target.setAttribute(Attribute.SILENCED);

		int oldMaxHp = target.getMaxHp();
		target.setMaxHp(target.getAttributeValue(Attribute.BASE_HP));
		target.setAttack(target.getAttributeValue(Attribute.BASE_ATTACK));
		if (target.getHp() > target.getMaxHp()) {
			target.setHp(target.getMaxHp());
		} else if (oldMaxHp < target.getMaxHp()) {
			target.setHp(target.getHp() + target.getMaxHp() - oldMaxHp);
		}

		log("{} was silenced", target);
	}

	/**
	 * Starts a turn.
	 * <p>
	 * At the start of each of their turns, the player gains {@link Player#maxMana} (up to a maximum of {@link
	 * #MAX_MANA}), and attempts to draw a card. The player is then free (but not forced) to take an action by playing
	 * cards, using their Hero Power, and/or attacking with their minions or hero. Once all possible actions have been
	 * taken, the "End Turn" button will light up.
	 * <p>
	 * All minions with {@link Attribute#SUMMONING_SICKNESS} will have that attribute cleared; {@link
	 * Attribute#OVERLOAD} will cause the player's {@link Player#mana} to decline by {@link Player#getLockedMana()}; and
	 * temporary bonuses like {@link Attribute#TEMPORARY_ATTACK_BONUS} will be lost.
	 *
	 * @param playerId The player that is starting their turn.
	 */
	@Suspendable
	public void startTurn(int playerId) {
		final int now = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
		Player player = context.getPlayer(playerId);
		final int gameStartTime;
		if (player.getAttributes().containsKey(Attribute.GAME_START_TIME_MILLIS)) {
			gameStartTime = now;
		} else {
			gameStartTime = (int) player.getAttributes().get(Attribute.GAME_START_TIME_MILLIS);
		}

		final int _startTime = now - gameStartTime;
		final int startTime = _startTime + (_startTime < 0 ? Integer.MAX_VALUE : 0);
		player.getAttributes().put(Attribute.TURN_START_TIME_MILLIS, startTime);

		if (player.getMaxMana() < MAX_MANA) {
			player.setMaxMana(player.getMaxMana() + 1);
		}
		player.getStatistics().startTurn();

		player.setLockedMana(player.getAttributeValue(Attribute.OVERLOAD));
		int mana = Math.min(player.getMaxMana() - player.getLockedMana(), MAX_MANA);
		player.setMana(mana);
		context.fireGameEvent(new MaxManaChangedEvent(context, player.getId(), 1));

		String manaString = player.getMana() + "/" + player.getMaxMana();
		if (player.getLockedMana() > 0) {
			manaString += " (" + player.getLockedMana() + " locked by overload)";
		}
		log("{} starts his turn with {} mana", player.getName(), manaString);

		player.getAttributes().remove(Attribute.OVERLOAD);
		for (Minion minion : player.getMinions()) {
			minion.getAttributes().remove(Attribute.TEMPORARY_ATTACK_BONUS);
		}
		player.setAttribute(Attribute.EXTRA_TURN, Math.max(player.getAttributeValue(Attribute.EXTRA_TURN) - 1, 0));

		player.getHero().getHeroPower().setUsed(0);
		player.getHero().activateWeapon(true);
		refreshAttacksPerRound(player.getHero());
		for (Minion minion : player.getMinions()) {
			minion.getAttributes().remove(Attribute.SUMMONING_SICKNESS);
			refreshAttacksPerRound(minion);
		}
		context.fireGameEvent(new TurnStartEvent(context, player.getId()));
		drawCard(playerId, null);
		checkForDeadEntities();
	}

	/**
	 * Summons a {@link Minion}.
	 * <p>
	 * Playing a minion card places that minion onto the battlefield. This process is known as 'summoning'. Each minion
	 * has a mana cost indicated by {@link MinionCard#getManaCost(GameContext, Player)}, which shows the amount of mana
	 * you must pay to summon the minion.
	 * <p>
	 * Successfully playing a minion card will transform the card into the minion itself, which will then appear upon
	 * the battleground represented by a portrait. Once summoned the minion will stay on the battlefield until it is
	 * destroyed or returned to the hand of its owner. Minions can be destroyed by reducing their hitpoints to zero, or
	 * by using destroy effects such as Assassinate to remove them directly. Note that if a minion is returned to its
	 * owner's hand, or shuffled back into the owner's deck, its Attack and Health will return to their original values,
	 * and any enchantments will be removed. However, transformations will not be reversed by returning a minion to its
	 * owner's hand or deck; the transformed minion is considered an entirely different card from what it was
	 * transformed from, and that is what is "returned" to the deck.
	 * <p>
	 * This method returns {@code false} if the summon failed, typically due to a rule violation. The caller is
	 * responsible for handling a failed summon. Summons can fail because players can normally have a maximum of {@link
	 * #MAX_MINIONS} minions on the battlefield at any time. Once {@link #MAX_MINIONS} friendly minions are on the
	 * field, the player will not be able to summon further minions. Minion cards and summon effects such as Totemic
	 * Call will not be playable, and any minion Battlecries and Deathrattles that summon other minions will be wasted.
	 * <p>
	 * Minions summoned by a summon effect written on a card other than a {@link MinionCard} are not played directly
	 * from the hand, and therefore will not trigger Battlecries or Overload. However, they will work with triggered
	 * effects which respond to the summoning of minions, like {@link MinionSummonedTrigger}.
	 *
	 * @param playerId         The player who will own the minion (not the initiator of the summon, which may be the
	 *                         opponent).
	 * @param minion           The minion to summon. Typically the result of a {@link MinionCard#summon()} call, but
	 *                         other cards have effects that summon minions on e.g., battlecry with a {@link
	 *                         SummonSpell}.
	 * @param source           The {@link MinionCard} or {@link Entity} response for this minion.
	 * @param index            The location on the {@link Zones#BATTLEFIELD} to place this minion.
	 * @param resolveBattlecry If {@code true}, the battlecry should be cast. Battlecries are only cast when a {@link
	 *                         Minion} is summoned by a {@link MinionCard} played from the {@link Zones#HAND}.
	 * @return {@code true} if the summoning was successful.
	 */
	@Suspendable
	public boolean summon(int playerId, Minion minion, Card source, int index, boolean resolveBattlecry) {
		PreSummon preSummon = new PreSummon(playerId, minion, index, source).invoke();
		if (preSummon.failed()) return false;
		Player player = preSummon.getPlayer();

		if (resolveBattlecry && minion.getBattlecry() != null) {
			resolveBattlecry(player.getId(), minion);
			checkForDeadEntities();
		}

		postSummon(minion, source, player, false);
		return true;
	}

	@Suspendable
	protected void postSummon(Minion minion, Card source, Player player, boolean skipAfterSummon) {
		if (context.getEnvironment().get(Environment.TRANSFORM_REFERENCE) != null) {
			minion = (Minion) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.TRANSFORM_REFERENCE));
			minion.setBattlecry(null);
			context.getEnvironment().remove(Environment.TRANSFORM_REFERENCE);
		}

		context.fireGameEvent(new BoardChangedEvent(context));

		player.getStatistics().minionSummoned(minion);
		if (context.getEnvironment().get(Environment.TARGET_OVERRIDE) != null) {
			Actor actor = (Actor) context.resolveTarget(player, source, (EntityReference) context.getEnvironment().get(Environment.TARGET_OVERRIDE)).get(0);
			context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
			SummonEvent summonEvent = new SummonEvent(context, actor, source);
			context.fireGameEvent(summonEvent);
		} else {
			SummonEvent summonEvent = new SummonEvent(context, minion, source);
			context.fireGameEvent(summonEvent);
		}

		applyAttribute(minion, Attribute.SUMMONING_SICKNESS);
		refreshAttacksPerRound(minion);

		if (minion.hasEnchantment()) {
			for (Enchantment trigger : minion.getEnchantments()) {
				addGameEventListener(player, trigger, minion);
			}
		}

		if (minion.getCardCostModifier() != null) {
			addManaModifier(player, minion.getCardCostModifier(), minion);
		}

		if (source != null) {
			source.setAttribute(Attribute.ATTACK, source.getAttributeValue(Attribute.BASE_ATTACK));
			source.setAttribute(Attribute.ATTACK_BONUS, 0);
			source.setAttribute(Attribute.MAX_HP, source.getAttributeValue(Attribute.BASE_HP));
			source.setAttribute(Attribute.HP, source.getAttributeValue(Attribute.BASE_HP));
			source.setAttribute(Attribute.HP_BONUS, 0);
		}
		handleEnrage(minion);

		context.getSummonReferenceStack().pop();
		if (player.getMinions().contains(minion)
				&& !skipAfterSummon) {
			context.fireGameEvent(new AfterSummonEvent(context, minion, source));
		}
		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Transforms a {@link Minion} into a new {@link Minion}.
	 * <p>
	 * The caller is responsible for making sure the new minion is created with {@link Minion#getCopy()} if the minion
	 * was the result of targeting an existing minion on the battlefield.
	 * <p>
	 * Transform is an ability which irreversibly transforms a minion into something else. This removes all card text,
	 * abilities and enchantments, and does not trigger any Deathrattles.
	 * <p>
	 * Transformation is not an {@link Aura} but rather a permanent change, which cannot be undone. {@link #silence(int,
	 * Minion)}ing the transformed minion or returning it to its owner's hand will not revert the transformation.
	 * <p>
	 * While transform effects effectively create a new minion in place of the old one, they do not summon minions and
	 * so will not trigger effects such as Knife Juggler or Starving Buzzard. The process appears to continue the
	 * summoning process precisely, with the new minion in place of the old.
	 * <p>
	 * Minions produced by transformations will have {@link Attribute#SUMMONING_SICKNESS}, as though they had just been
	 * summoned. This is true even if the minion which was transformed was previously ready to attack. However, if the
	 * resulting minion has {@link Attribute#CHARGE} it will not suffer from {@link Attribute#SUMMONING_SICKNESS}.
	 * <p>
	 * Minions removed from play due to being transformed are not considered to have died, and so cannot be resummoned
	 * by effects like Resurrect or Kel'Thuzad.
	 *
	 * @param minion    The original minion in play
	 * @param newMinion The new minion to transform into
	 * @see net.demilich.metastone.game.spells.TransformMinionSpell for the complete transformation logic.
	 */
	@Suspendable
	public void transformMinion(Minion minion, Minion newMinion) {
		// Remove any spell triggers associated with the old minion.
		removeEnchantments(minion);

		Player owner = context.getPlayer(minion.getOwner());
		int index = -1;
		// Tracking of old zone implements Sherazin, Seed
		Zones oldZone = minion.getZone();
		if (!minion.getEntityLocation().equals(EntityLocation.UNASSIGNED)
				&& owner != null) {
			index = minion.getEntityLocation().getIndex();
			owner.getZone(minion.getEntityLocation().getZone()).remove(index);
		}

		// If we want to straight up remove a minion from existence without
		// killing it, this would be the best way.
		if (newMinion != null) {
			log("{} was transformed to {}", minion, newMinion);

			// Give the new minion an ID.
			newMinion.setId(getIdFactory().generateId());
			newMinion.setOwner(owner.getId());

			minion.getAttributes().put(Attribute.TRANSFORM_REFERENCE, newMinion.getReference());
			// If the minion being transforms is being summoned, replace the old
			// minion on the stack.
			// Otherwise, summon the add the new minion.
			// However, do not give a summon event.
			if (!context.getSummonReferenceStack().isEmpty() && context.getSummonReferenceStack().peek().equals(minion.getReference())
					&& !context.getEnvironment().containsKey(Environment.TRANSFORM_REFERENCE)) {
				context.getEnvironment().put(Environment.TRANSFORM_REFERENCE, newMinion.getReference());
				owner.getMinions().add(index, newMinion);

				// It's quite possible that this is actually supposed to add the
				// minion to the zone it was originally in.
				// This means minions in the SetAsideZone or the Graveyard that are
				// targeted (through bizarre mechanics)
				// add the minion to there. This will be tested eventually with
				// Resurrect, Recombobulator, and Illidan.
				// Since this is unknown, this is the patch for it.
			} else if (!owner.getSetAsideZone().contains(minion)) {
				if (index < 0 || index >= owner.getMinions().size()) {
					owner.getMinions().add(newMinion);
				} else {
					owner.getMinions().add(index, newMinion);
				}

				applyAttribute(newMinion, Attribute.SUMMONING_SICKNESS);
				refreshAttacksPerRound(newMinion);

				if (newMinion.hasEnchantment()) {
					for (Enchantment enchantment : newMinion.getEnchantments()) {
						addGameEventListener(owner, enchantment, newMinion);
					}
				}

				if (newMinion.getCardCostModifier() != null) {
					addManaModifier(owner, newMinion.getCardCostModifier(), newMinion);
				}

				handleEnrage(newMinion);
			} else {
				owner.getSetAsideZone().add(newMinion);
				removeEnchantments(newMinion);
				return;
			}

		}

		// Special case for Sherazin, Seed
		if (oldZone == Zones.GRAVEYARD) {
			minion.moveOrAddTo(context, Zones.GRAVEYARD);
		} else if (minion.transformResolved(context).equals(newMinion)) {
			// The old minion should always be removed from play, because it has transformed.
			minion.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		} else {
			// Something else happened and the source minion was not successfully transformed.
			minion.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		}

		context.fireGameEvent(new BoardChangedEvent(context));
	}

	/**
	 * Uses the player's hero power (plays the {@link HeroPowerCard}).
	 *
	 * @param playerId The player whose power should be used.
	 */
	@Suspendable
	public void useHeroPower(int playerId) {
		Player player = context.getPlayer(playerId);
		HeroPowerCard power = player.getHero().getHeroPower();
		int modifiedManaCost = getModifiedManaCost(player, power);
		modifyCurrentMana(playerId, -modifiedManaCost);
		log("{} uses {}", player.getName(), power);
		power.markUsed();
		player.getStatistics().cardPlayed(power, context.getTurn());
		context.fireGameEvent(new HeroPowerUsedEvent(context, playerId, power));
	}

	@Suspendable
	protected void mulliganAsync(Player player, boolean begins, Handler<Object> callback) {
		throw new RuntimeException("Cannot call GameLogic::mulliganAsync from a non-async GameLogic instance.");
	}

	@Suspendable
	public void initAsync(int playerId, boolean begins, Handler<Player> callback) {
		throw new RuntimeException("Cannot call GameLogic::initAsync from a non-async GameLogic instance.");
	}

	@Suspendable
	protected void resolveBattlecryAsync(int playerId, Actor actor, Handler<AsyncResult<Boolean>> result) {
		throw new RuntimeException("Cannot call GameLogic::resolveBattlecryAsync from a non-async GameLogic instance.");

	}

	@Suspendable
	public void equipWeaponAsync(int playerId, Weapon weapon, WeaponCard weaponCard, Handler<AsyncResult<Boolean>> result, boolean resolveBattlecry) {
		throw new RuntimeException("Cannot call GameLogic::equipWeaponAsync from a non-async GameLogic instance.");
	}

	@Suspendable
	protected void summonAsync(int playerId, Minion minion, Card source, int index, boolean resolveBattlecry, Handler<AsyncResult<Boolean>> summoned) {
		throw new RuntimeException("Cannot call GameLogic::summonAsync from a non-async GameLogic instance.");
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public IdFactory getIdFactory() {
		return idFactory;
	}

	public void setIdFactory(IdFactory idFactory) {
		this.idFactory = idFactory;
	}

	/**
	 * Concedes the game for the specified player.
	 * <p>
	 * Concession is implemented by destroy's the player's {@link Hero}.
	 *
	 * @param playerId The player that should concede.
	 */
	public void concede(int playerId) {
		final Hero hero = context.getPlayer(playerId).getHero();
		if (!hero.isDestroyed()
				&& (hero.getZone() != Zones.GRAVEYARD)) {
			destroy(hero);
		}
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
	 * {@link Enchantment#countUntilCast} functionality.
	 *
	 * @param player   The player that triggered the quest.
	 * @param quest    The quest to put into play.
	 * @param fromHand If {@code true}, fires a {@link QuestPlayedEvent}.
	 */
	public void playQuest(Player player, Quest quest, boolean fromHand) {
		log("{} has a new quest activated: {}", player.getName(), quest.getSourceCard());
		quest = quest.clone();
		quest.setId(getIdFactory().generateId());
		quest.setOwner(player.getId());
		addGameEventListener(player, quest, player.getHero());
		player.getQuests().add(quest);
		if (fromHand) {
			context.fireGameEvent(new QuestPlayedEvent(context, player.getId(), quest.getSourceCard()));
		}
	}

	/**
	 * Indicates that a quest was successful (its spell was casted).
	 *
	 * @param player The player that triggered the quest. May be different than its owner.
	 * @param quest  The quest {@link Enchantment} living inside the {@link Zones#QUEST} zone.
	 */
	public void questTriggered(Player player, Quest quest) {
		log("Quest was trigged: {}", quest.getSourceCard());
		quest.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		context.fireGameEvent(new QuestSuccessfulEvent(context, quest.getSourceCard(), player.getId()));
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
	public void revealCard(Player player, Card cardToReveal) {
		// For now, just trigger a reveal card event.
		context.fireGameEvent(new CardRevealedEvent(context, player.getId(), cardToReveal, 1.0));
	}

	protected class FirstHand {
		private Player player;
		private boolean begins;
		private int numberOfStarterCards;
		private List<Card> starterCards;

		public FirstHand(Player player, boolean begins) {
			this.player = player;
			this.begins = begins;
		}

		public int getNumberOfStarterCards() {
			return numberOfStarterCards;
		}

		public List<Card> getStarterCards() {
			return starterCards;
		}

		public FirstHand invoke() {
			numberOfStarterCards = begins ? STARTER_CARDS : STARTER_CARDS + 1;
			starterCards = new ArrayList<>();

			// The player's starting hand should always contain the quest.
			// Since our server could theoretically allow you to have a deck with multiple quests, they will
			// all start here.
			starterCards.addAll(player.getDeck().stream()
					.filter(card -> card.hasAttribute(Attribute.QUEST))
					.limit(numberOfStarterCards)
					.collect(toList()));

			starterCards.forEach(card -> player.getDeck().move(card, player.getSetAsideZone()));

			for (int j = starterCards.size(); j < numberOfStarterCards; j++) {
				Card randomCard = getRandom(player.getDeck());
				if (randomCard != null) {
					player.getDeck().move(randomCard, player.getSetAsideZone());
					log("Player {} been offered card {} for networkRequestMulligan", player.getName(), randomCard);
					starterCards.add(randomCard);
				}
			}
			return this;
		}
	}

	protected class PreEquipWeapon {
		private int playerId;
		private Weapon weapon;
		private Player player;
		private Weapon currentWeapon;

		public PreEquipWeapon(int playerId, Weapon weapon) {
			this.playerId = playerId;
			this.weapon = weapon;
		}

		public Player getPlayer() {
			return player;
		}

		public Weapon getCurrentWeapon() {
			return currentWeapon;
		}

		public PreEquipWeapon invoke() {
			player = context.getPlayer(playerId);

			weapon.setId(getIdFactory().generateId());
			currentWeapon = player.getHero().getWeapon();

			if (currentWeapon != null) {
				currentWeapon.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			}

			log("{} equips weapon {}", player.getHero(), weapon);
			player.getHero().setWeapon(weapon);
			return this;
		}
	}

	protected class PreSummon {
		private Card source;
		private boolean failed;
		private int playerId;
		private Minion minion;
		private int index;
		private Player player;

		public PreSummon(int playerId, Minion minion, int index, Card source) {
			this.playerId = playerId;
			this.minion = minion;
			this.index = index;
			this.source = source;
		}

		public boolean failed() {
			return failed;
		}

		public Player getPlayer() {
			return player;
		}

		@Suspendable
		public PreSummon invoke() {
			player = context.getPlayer(playerId);
			if (!canSummonMoreMinions(player)) {
				log("{} cannot summon any more minions, {} is destroyed", player.getName(), minion);
				failed = true;
				return this;
			}
			minion.setId(getIdFactory().generateId());
			minion.setOwner(player.getId());

			context.getSummonReferenceStack().push(minion.getReference());

			log("{} summons {}", player.getName(), minion);

			if (index < 0 || index >= player.getMinions().size()) {
				minion.moveOrAddTo(context, Zones.BATTLEFIELD);
			} else {
				player.getMinions().add(index, minion);
			}

			context.fireGameEvent(new BeforeSummonEvent(context, minion, source));
			context.fireGameEvent(new BoardChangedEvent(context));
			failed = false;
			return this;
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
		return Integer.parseInt(System.getProperty("games.turnTimeMillis", Integer.toString(DEFAULT_TURN_TIME * 1000)));
	}
}
