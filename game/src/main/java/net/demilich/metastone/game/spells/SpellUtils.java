package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.BoardPositionRelative;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.custom.RepeatAllAftermathsSpell;
import net.demilich.metastone.game.spells.custom.RepeatAllOtherBattlecriesSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.HasCardCreationSideEffects;
import net.demilich.metastone.game.spells.desc.source.UnweightedCatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A set of utilities to help write spells.
 */
public class SpellUtils {
	private static Logger logger = LoggerFactory.getLogger(SpellUtils.class);
	private static Set<String> specialCards = Set.of("EVENT_SOURCE", "OUTPUT");

	/**
	 * Sets up the source and target references for casting a child spell, typically an "effect" of a spell defined on a
	 * card.
	 *
	 * @param context The game context.
	 * @param player  The player casting the spell.
	 * @param spell   The child spell to cast.
	 * @param source  The source of the spell, typically the spell card or minion whose battlecry is being called.
	 * @param target  The target reference.
	 */
	@Suspendable
	public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target) {
		EntityReference sourceReference = source != null ? source.getReference() : null;
		EntityReference targetReference = spell.getTarget();

		// Inherit target
		if (targetReference == null && target != null) {
			targetReference = target.getReference();
		}

		if (sourceReference == null) {
			sourceReference = EntityReference.NONE;
		}

		if (targetReference == null) {
			targetReference = EntityReference.NONE;
		}

		context.getLogic().castSpell(player.getId(), spell, sourceReference, targetReference, TargetSelection.NONE, true, null);
	}

	/**
	 * Plays a card "randomly."
	 * <p>
	 * This will cause it to select random targets if, after target selection modification, it accepts targets.
	 *
	 * @param context
	 * @param player
	 * @param card
	 * @param source
	 * @param summonRightmost       When {@code true} and {@link Card#isActor()}, summons the card in the rightmost
	 *                              position. Otherwise, summons it to a random position.
	 * @param resolveBattlecry      When {@code true}, also resolves the battlecry with a random target. Otherwise, the
	 *                              battlecry is ignored.
	 * @param onlyWhileSourceInPlay When {@code true},
	 * @param randomChooseOnes      When {@code true}, randomly chooses the choose-one effects. Otherwise, checks if the
	 *                              card has had a {@link Attribute#CHOICES} made.
	 * @param playFromHand          When {@code true}, the card is counterable, mana is deducted and played card stats are
	 *                              incremented. Otherwise, only the effects of the card, like putting a minion into play
	 *                              or the underlying spell effects, are executed.
	 * @return
	 */
	@Suspendable
	public static boolean playCardRandomly(GameContext context,
	                                       Player player,
	                                       Card card,
	                                       Entity source,
	                                       boolean summonRightmost,
	                                       boolean resolveBattlecry,
	                                       boolean onlyWhileSourceInPlay,
	                                       boolean randomChooseOnes,
	                                       boolean playFromHand) {

		DetermineCastingPlayer determineCastingPlayer = determineCastingPlayer(context, player, source, TargetPlayer.SELF);
		// Stop casting battlecries if Shudderwock is transformed or destroyed
		if (onlyWhileSourceInPlay && !determineCastingPlayer.isSourceInPlay()) {
			return false;
		}

		player = determineCastingPlayer.getCastingPlayer();
		player.modifyAttribute(Attribute.RANDOM_CHOICES, 1);

		PlayCardAction action = null;
		if (card.isChooseOne()) {
			ChooseOneOverride chooseOneOverride = context.getLogic().getChooseOneAuraOverrides(player, card);
			if (chooseOneOverride != ChooseOneOverride.NONE) {
				switch (chooseOneOverride) {
					case ALWAYS_FIRST:
						action = card.playOptions()[0];
						break;
					case ALWAYS_SECOND:
						action = card.playOptions()[1];
						break;
					case BOTH_COMBINED:
						action = card.playBothOptions();
						break;
				}
			} else {
				boolean doesNotContainChoice = !card.getAttributes().containsKey(Attribute.CHOICE)
						&& card.getAttributes().containsKey(Attribute.PLAYED_FROM_HAND_OR_DECK);
				if (randomChooseOnes || doesNotContainChoice) {
					if (doesNotContainChoice) {
						logger.warn("playCardRandomly {} {}: A choose one card {} played from the hand does not contain a choice. Choosing randomly.",
								context.getGameId(), source, card);
					}
					PlayCardAction[] options = card.playOptions();
					action = options[context.getLogic().random(options.length)];
				} else if (card.getAttributes().containsKey(Attribute.CHOICE)) {
					action = card.playOptions()[card.getAttributeValue(Attribute.CHOICE)];
				}
			}
		} else {
			card.processTargetSelectionOverride(context, player);
			action = card.play();
		}

		if (action == null) {
			logger.error("playCardRandom {} {}: No action generated for card {}", context.getGameId(), source, card);
			player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
			return false;
		}

		action = action.clone();

		if (card.isActor()) {
			if (!summonRightmost && card.getCardType() == CardType.MINION) {
				int minionCount = player.getMinions().size();
				int targetIndex = context.getLogic().random(minionCount + 1);
				if (targetIndex == minionCount) {
					// Summon at the rightmost spot (default)
				} else {
					// summon next to the specified target
					action.setTargetReference(player.getMinions().get(targetIndex).getReference());
				}
			}

			// Do we resolve battlecries?
			if (!resolveBattlecry && action instanceof HasBattlecry) {
				HasBattlecry actionWithBattlecry = ((HasBattlecry) action);
				// No matter what the battlecry, clear it. This way, when the action is executed, resolve battlecry can be
				// true but this method's parameter to not resolve battlecries will be respected
				BattlecryDesc nullBattlecry = new BattlecryDesc();
				nullBattlecry.spell = NullSpell.create();
				actionWithBattlecry.setBattlecry(nullBattlecry);
			}
		} else if (card.isSpell() || card.isHeroPower()) {
			// This is some other kind of action that takes a target. Process possible target modification first.
			if (!card.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
				action.setSourceReference(card.getReference());
				context.getLogic().processTargetModifiers(action);
			}
			if (action.getTargetRequirement() != null && action.getTargetRequirement() != TargetSelection.NONE) {
				List<Entity> targets = context.getLogic().getValidTargets(player.getId(), action);
				EntityReference randomTarget = null;
				if (targets != null && !targets.isEmpty()) {
					randomTarget = context.getLogic().getRandom(targets).getReference();
					action.setTargetReference(randomTarget);
				} else {
					// Card should be revealed, but there were no valid targets so the spell isn't cast
					// TODO: It's not obvious if cards with no valid targets should be uncastable if their conditions permit it
					player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
					return true;
				}
			}

			// Target requirement may have been none, but the action is still valid.
		} else {
			logger.error("playCardRandomly {} {}: Unsupported card type {} for card {}", context.getGameId(), source, card.getCardType(), card);
			player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
			return false;
		}

		// Do the deed
		if (playFromHand) {
			action.execute(context, player.getId());
		} else {
			action.setOverrideChild(true);
			int playedFromHandOrDeck = -1;
			// Reference the real card
			if (card.getId() != IdFactory.UNASSIGNED) {
				card = (Card) context.resolveSingleTarget(card.getReference());
			}
			if (card.hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK)) {
				playedFromHandOrDeck = card.getAttributeValue(Attribute.PLAYED_FROM_HAND_OR_DECK);
				card.getAttributes().remove(Attribute.PLAYED_FROM_HAND_OR_DECK);
			}
			action.innerExecute(context, player.getId());
			if (playedFromHandOrDeck != -1) {
				card.getAttributes().put(Attribute.PLAYED_FROM_HAND_OR_DECK, playedFromHandOrDeck);
			}
		}

		player.modifyAttribute(Attribute.RANDOM_CHOICES, -1);
		return true;
	}


	/**
	 * Given a filter {@link ComparisonOperation}, return a boolean representing whether that operation is satisfied.
	 *
	 * @param operation   The algebraic operation.
	 * @param actualValue The left hand side.
	 * @param targetValue The right hand side.
	 * @return {@code true} if the evaluation is truue.
	 */
	public static boolean evaluateOperation(ComparisonOperation operation, int actualValue, int targetValue) {
		switch (operation) {
			case EQUAL:
				return actualValue == targetValue;
			case GREATER:
				return actualValue > targetValue;
			case GREATER_OR_EQUAL:
				return actualValue >= targetValue;
			case HAS:
				return actualValue > 0;
			case LESS:
				return actualValue < targetValue;
			case LESS_OR_EQUAL:
				return actualValue <= targetValue;
		}
		return false;
	}

	/**
	 * Filters a card list. Does not copy source cards.
	 *
	 * @param source A {@link CardList} source.
	 * @param filter A function that returns {@code true} when the card should be kept, or {@code null} to include all
	 *               cards.
	 * @return A {@link CardList} backed by a mutable, non-copy {@link CardArrayList}.
	 */
	public static CardList getCards(CardList source, Predicate<Card> filter) {
		CardList result = new CardArrayList();
		for (Card card : source) {
			if (filter == null || filter.test(card)) {
				result.addCard(card);
			}
		}
		return result;
	}

	/**
	 * Gets a card out of a {@link SpellDesc}. Typically only consults the {@link SpellArg#CARD} property.
	 *
	 * @param context The context.
	 * @param spell   The {@link SpellDesc}.
	 * @return A card.
	 */
	public static Card getCard(GameContext context, SpellDesc spell) {
		String cardId = (String) spell.get(SpellArg.CARD);
		return getSingleCard(context, cardId);
	}

	/**
	 * Consider the  and {@link Environment#OUTPUTS}, and the {@link Zones#DISCOVER} zone for the specified card
	 *
	 * @param context
	 * @param cardId
	 * @return
	 */
	private static Card getSingleCard(GameContext context, String cardId) {
		if (cardId == null) {
			return null;
		}
		Card card;
		if (cardId.toUpperCase().equals("EVENT_SOURCE")) {
			card = (Card) context.resolveSingleTarget(context.getEventSourceStack().peek());
		} else if (cardId.toUpperCase().equals("OUTPUT")) {
			card = context.getOutputCard();
		} else {
			card = getCardFromContextOrDiscover(context, cardId);
		}
		return card;
	}

	/**
	 * Retrieves the cards specified inside the {@link SpellArg#CARD} and {@link SpellArg#CARDS} arguments.
	 *
	 * @param context The game context to use for  or {@link GameContext#getOutputCard()} lookups.
	 * @param spell   The spell description to retrieve the cards from.
	 * @return A new array of {@link Card} entities.
	 * @see #castChildSpell(GameContext, Player, SpellDesc, Entity, Entity, Entity) for a description of what an {@code
	 * "OUTPUT_CARD"} value corresponds to.
	 */
	public static Card[] getCards(GameContext context, SpellDesc spell) {
		String[] cardIds;
		if (spell.containsKey(SpellArg.CARDS)) {
			cardIds = (String[]) spell.get(SpellArg.CARDS);
		} else if (spell.containsKey(SpellArg.CARD)) {
			cardIds = new String[1];
			cardIds[0] = (String) spell.get(SpellArg.CARD);
		} else {
			return new Card[0];
		}
		Card[] cards = new Card[cardIds.length];
		for (int i = 0; i < cards.length; i++) {
			// If the discover zone contains the card, reference it instead
			final String cardId = cardIds[i];
			cards[i] = getSingleCard(context, cardId);
		}
		return cards;
	}

	/**
	 * Retrieves a reference to a newly generated card currently in the {@link Zones#DISCOVER} if the given {@code cardId}
	 * can be found there.
	 * <p>
	 * This allows spells to be cast on cards while they are in the discover zone, even though the spells casting effects
	 * on them are supposed to be executing their effects on base cards.
	 *
	 * @param context
	 * @param cardId
	 * @return
	 */
	public static Card getCardFromContextOrDiscover(GameContext context, String cardId) {
		return context.getPlayers().stream()
				.flatMap(p -> p.getDiscoverZone().stream())
				.filter(c -> c.getCardId().equals(cardId))
				.findFirst()
				.orElseGet(() -> context.getCardById(cardId));
	}

	/**
	 * Requests that the player chooses from a selection of cards and casts a spell (typically {@link ReceiveCardSpell}
	 * with that card.
	 * <p>
	 * This method makes a network request if required.
	 *
	 * @param context The game context that hosts the player and state for this request.
	 * @param player  The player that will choose from the cards.
	 * @param source
	 * @param desc    For every card the player can discover, this method will create a {@link Spell} from this {@link
	 *                SpellDesc} and set its {@link SpellArg#CARD} argument to the discoverable card. Typically, this
	 *                {@link SpellDesc} defines a {@link ReceiveCardSpell}, {@link ReceiveCardAndDoSomethingSpell}, or a
	 *                {@link ChangeHeroPowerSpell}. These spells all receive cards as arguments. This argument allows a
	 *                {@link DiscoverAction} to do more sophisticated things than just put cards into hands.
	 * @param cards   A {@link CardList} of cards that get copied, added to the {@link Zones#DISCOVER} zone of the player
	 *                and shown in the discover card UI to the player.
	 * @return The {@link DiscoverAction} that corresponds to the card the player chose.
	 * @see DiscoverCardSpell for the spell that typically calls this method.
	 * @see ReceiveCardSpell for the spell that is typically the {@link SpellArg#SPELL} property of a {@link
	 * DiscoverCardSpell}.
	 */
	@Suspendable
	public static DiscoverAction discoverCard(GameContext context, Player player, Entity source, SpellDesc desc, CardList cards) {
		// Discovers always work with a copy of the incoming cards
		cards = cards.getCopy();
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		List<GameAction> discoverActions = new ArrayList<>();
		for (int i = 0; i < cards.getCount(); i++) {
			Card card = cards.get(i);
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.DISCOVER);

			SpellDesc spellClone = spell.addArg(SpellArg.CARD, card.getCardId());
			DiscoverAction discover = DiscoverAction.createDiscover(spellClone);
			discover.setCard(card);
			discover.setId(i);
			discover.setSourceReference(source == null ? null : source.getReference());
			discoverActions.add(discover);
		}

		return postDiscover(context, player, cards, discoverActions);
	}

	/**
	 * Moves the card put into the {@link Zones#DISCOVER} by a {@link #discoverCard(GameContext, Player, Entity,
	 * SpellDesc, CardList)} action back to where it came from. If it was a newly generated card it is removed from play.
	 *
	 * @param context
	 * @param player
	 * @param cards
	 * @param discoverActions
	 * @return
	 */
	@Suspendable
	public static DiscoverAction postDiscover(GameContext context, Player player, Iterable<? extends Card> cards, List<GameAction> discoverActions) {
		if (discoverActions.size() == 0) {
			return null;
		}

		DiscoverAction discoverAction = (DiscoverAction) context.getLogic().requestAction(player, discoverActions);
		// We do not perform the game action here

		// Move the cards back
		for (Card card : cards) {
			// Cards that are being discovered are always copies, so they are always removed from play afterwards.
			if (card.getZone() == Zones.DISCOVER) {
				card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			}

			context.getLogic().removeCard(card);
		}

		return discoverAction;
	}

	/**
	 * Requests that the player chooses from a selection of cards, then returns just the spell from the cards.
	 * <p>
	 * Removes all the cards from play unless otherwise specified, since these cards aren't actually used.
	 *
	 * @param context The {@link GameContext}
	 * @param player  The {@link Player}
	 * @param desc    A {@link SpellDesc} to use as the "parent" of the discovered spells. The mana cost and targets are
	 *                inherited from this spell.
	 * @param spells  A list of spells from which to generate virtual cards.
	 * @param source  The source entity, typically the {@link Card} or {@link Minion#getBattlecries()} that initiated this
	 *                call.
	 * @return A {@link DiscoverAction} whose {@link DiscoverAction#getCard()} property corresponds to the selected card.
	 * To retrieve the spell, get the card's spell with {@link Card#getSpell()}.
	 */
	@Suspendable
	public static DiscoverAction getSpellDiscover(GameContext context, Player player, SpellDesc desc, List<SpellDesc> spells, Entity source) {
		List<GameAction> discoverActions = new ArrayList<>();
		List<Card> cards = new ArrayList<>();
		for (int i = 0; i < spells.size(); i++) {
			final SpellDesc spell = spells.get(i);
			final CardDesc spellCardDesc = new CardDesc();
			final String name = spell.getString(SpellArg.NAME);
			final String description = spell.getString(SpellArg.DESCRIPTION);
			// TODO: Parse the parenthesized part of a name in a spell as a description
			spellCardDesc.setId(context.getLogic().generateCardId());
			spellCardDesc.setName(name);
			List<Entity> entities = context.resolveTarget(player, source, desc.getTarget());
			int baseManaCost = 0;
			if (entities != null
					&& entities.size() > 0) {
				baseManaCost = desc.getValue(SpellArg.MANA, context, player, entities.get(0), source, 0);
			}
			spellCardDesc.setBaseManaCost(baseManaCost);
			spellCardDesc.setDescription(description);
			spellCardDesc.setHeroClass(HeroClass.ANY);
			spellCardDesc.setType(CardType.SPELL);
			spellCardDesc.setRarity(Rarity.FREE);
			spellCardDesc.setTargetSelection((TargetSelection) spell.getOrDefault(SpellArg.TARGET_SELECTION, desc.getOrDefault(SpellArg.TARGET_SELECTION, TargetSelection.NONE)));
			spellCardDesc.setSpell(spell);
			spellCardDesc.setCollectible(false);

			Card card = spellCardDesc.create();
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			context.addTempCard(card);
			card.moveOrAddTo(context, Zones.DISCOVER);
			cards.add(card);

			DiscoverAction discover = DiscoverAction.createDiscover(spell);
			discover.setId(i);
			discover.setCard(card);
			discover.setSourceReference(source == null ? null : source.getReference());
			discoverActions.add(discover);
		}

		return postDiscover(context, player, cards, discoverActions);
	}

	/**
	 * Returns a list of valid missile targets given that missiles stop hitting destroyed actors before an {@link
	 * GameLogic#endOfSequence()} is called.
	 *
	 * @param targets
	 * @return
	 */
	static List<Entity> getValidRandomTargets(List<Entity> targets) {
		List<Entity> validTargets = new ArrayList<>();
		for (Entity entity : targets) {
			if (entity instanceof Actor) {
				Actor actor = (Actor) entity;
				if (!actor.isDestroyed() || actor.getEntityType() == EntityType.HERO) {
					validTargets.add(actor);
				}
			} else {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	/**
	 * Filters a list of targets.
	 *
	 * @param context
	 * @param player
	 * @param allTargets
	 * @param filter
	 * @param host
	 * @return
	 */
	public static List<Entity> getValidTargets(GameContext context, Player player, List<Entity> allTargets, EntityFilter filter, Entity host) {
		if (filter == null) {
			return allTargets;
		}
		List<Entity> validTargets = new ArrayList<>();
		for (Entity entity : allTargets) {
			if (filter.matches(context, player, entity, host)) {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	/**
	 * Interprets the {@link SpellArg#BOARD_POSITION_ABSOLUTE} or {@link SpellArg#BOARD_POSITION_RELATIVE} in a {@code
	 * desc} given the {@code source} entity.
	 *
	 * @param context
	 * @param player
	 * @param desc
	 * @param source
	 * @return An index in the {@code source} entity's {@link Zones} zone.
	 */
	public static int getBoardPosition(GameContext context, Player player, SpellDesc desc, Entity source) {
		final int UNDEFINED = -1;
		int boardPosition = desc.getValue(SpellArg.BOARD_POSITION_ABSOLUTE, context, player, null, source, UNDEFINED);
		if (boardPosition != UNDEFINED) {
			return boardPosition;
		}
		BoardPositionRelative relativeBoardPosition = (BoardPositionRelative) desc.get(SpellArg.BOARD_POSITION_RELATIVE);
		if (relativeBoardPosition == null) {
			return UNDEFINED;
		}

		if (!(source instanceof Minion)) {
			return UNDEFINED;
		}

		int sourcePosition = source.getEntityLocation().getIndex();
		if (sourcePosition == UNDEFINED) {
			return UNDEFINED;
		}
		switch (relativeBoardPosition) {
			case LEFT:
				return sourcePosition;
			case RIGHT:
				return sourcePosition + 1;
			default:
				return UNDEFINED;
		}
	}

	/**
	 * Retrieves the group (deprecated) spells printed on the specified {@code spell}
	 *
	 * @param context
	 * @param spell
	 * @return
	 */
	static SpellDesc[] getGroup(GameContext context, SpellDesc spell) {
		Card card = null;
		String cardName = (String) spell.get(SpellArg.GROUP);
		card = context.getCardById(cardName);
		if (card.getCardType() == CardType.GROUP) {
			return card.getGroup();
		}
		return null;
	}

	/**
	 * When the target {@link Attribute#KEEPS_ENCHANTMENTS}, populates the provided {@link AttributeMap} with the
	 * keyword-based enchantments to keep.
	 * <p>
	 * This ought to include taunt, divine shield, stealth etc. but does not, because it is primarily designed for
	 * Kingsbane (weapons aren't stealthed).
	 *
	 * @param target
	 * @param map
	 * @return
	 */
	static AttributeMap processKeptEnchantments(Entity target, AttributeMap map) {
		if (target.hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
			Stream.of(
					Attribute.POISONOUS,
					Attribute.DIVINE_SHIELD,
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
					Attribute.HP_BONUS
			)
					.filter(target::hasAttribute).forEach(k -> map.put(k, target.getAttributes().get(k)));

			if (target instanceof Minion) {
				Minion minion = (Minion) target;
				if (minion.hasAttribute(Attribute.DEATHRATTLES)) {
					map.put(Attribute.DEATHRATTLES, minion.getAttribute(Attribute.DEATHRATTLES));
				}
				map.put(Attribute.BASE_ATTACK, minion.getBaseAttack());
				map.put(Attribute.BASE_HP, minion.getBaseHp());
			}
		}
		return map;
	}

	/**
	 * Process the text "keeps enchantments" on a {@code target} and the card that the enchantments are being moved to,
	 * typically for a shuffle-to-deck or return-to-hand effect.
	 *
	 * @param target
	 * @param card
	 */
	static void processKeptEnchantments(Entity target, Card card) {
		processKeptEnchantments(target, card.getAttributes());
	}

	/**
	 * Casts a subspell on a card that was returned by {@link GameLogic#receiveCard(int,
	 * Card)}. Will not execute if the output is null or in the {@link Zones#GRAVEYARD}.
	 *
	 * @param context The {@link GameContext} to operate on.
	 * @param player  The player from whose point of view we are casting this sub spell. This should be passed down from
	 *                the {@link Spell#onCast(GameContext, Player, SpellDesc, Entity, Entity)} {@code player} argument.
	 * @param spell   The sub spell, typically from the {@code desc} argument's {@link SpellArg#SPELL} key.
	 * @param source  The source entity.
	 * @param target
	 * @param output  The card. When {@code null} or the card is located in the {@link Zones#GRAVEYARD}.
	 */
	@Suspendable
	public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target, Entity output) {
		// card may be null (i.e. try to draw from deck, but already in
		// fatigue)
		if (output == null
				|| output.getZone() == Zones.GRAVEYARD) {
			return;
		}
		if (spell == null) {
			return;
		}

		// We should never try to cast a child on output that is valid but has an unassigned location. Temporarily move it
		// into set aside when that happens
		boolean needsToBeRemoved = false;
		if (output.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			output.setId(context.getLogic().generateId());
			output.setOwner(player.getId());
			output.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			needsToBeRemoved = true;
		}

		context.getOutputStack().push(output.getReference());
		castChildSpell(context, player, spell, source, target);
		context.getOutputStack().pop();

		if (needsToBeRemoved) {
			output.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		}
	}

	/**
	 * Retrieves the cards specified in the {@link SpellDesc}, either in the {@link SpellArg#CARD} or {@link
	 * SpellArg#CARDS} properties or as specified by a {@link CardSource}
	 * and {@link CardFilter}. If neither of those are specified, uses the
	 * target's {@link Entity#getSourceCard()} as the targeted card.
	 * <p>
	 * The number of cards randomly retrieved is equal to the {@link SpellArg#VALUE} specified in the {@code desc}
	 * argument, defaulting to 1.
	 *
	 * @param context The game context
	 * @param player  The player from whose point of view these cards should be retrieved
	 * @param target  The target, which can be {@code null}
	 * @param source  The source or host {@link Entity}, typically the origin of this spell cast.
	 * @param desc    The {@link SpellDesc} typically of the calling spell.
	 * @return A list of cards.
	 * @see #getCards(GameContext, Player, Entity, Entity, SpellDesc, int) for a complete description of the rules of how
	 * cards are generated or retrieved in this method.
	 */
	public static CardList getCards(GameContext context, Player player, Entity target, Entity source, SpellDesc desc) {
		return getCards(context, player, target, source, desc, desc.getValue(SpellArg.VALUE, context, player, target, source, 1));
	}

	/**
	 * Retrieves the cards specified in the {@link SpellDesc}, either in the {@link SpellArg#CARD} or {@link
	 * SpellArg#CARDS} properties or as specified by a {@link CardSource}
	 * and {@link CardFilter}. If neither of those are specified, uses the
	 * target's {@link Entity#getSourceCard()} as the targeted card.
	 * <p>
	 * The {@link SpellDesc} given in {@code desc} is inspected for a variety of arguments. If there is a {@link
	 * SpellArg#CARD_SOURCE} or {@link SpellArg#CARD_FILTER} specified, the card source generates a list of cards using
	 * {@link CardSource#getCards(GameContext, Entity, Player)}, and that list
	 * is filtered using {@link EntityFilter#matches(GameContext, Player, Entity, Entity)}.
	 * <p>
	 * Anytime {@link SpellArg#CARD} or {@link SpellArg#CARDS} is specified, the card IDs in those args are added to the
	 * list of cards returned by this method.
	 * <p>
	 * If <b>no</b> arguments are specified, the {@code target} entity's {@link Entity#getSourceCard()} is added to the
	 * list of cards returned by this method. This means that providing a {@link SpellDesc} that contains neither {@link
	 * SpellArg#CARD}, {@link SpellArg#CARDS}, {@link SpellArg#CARD_SOURCE} nor {@link SpellArg#CARD_FILTER} will be
	 * interpreted as trying to retrieve the target's base card.
	 * <p>
	 * Cards are not generated as copies unless the {@link CardSource} has
	 * the {@link HasCardCreationSideEffects} trait and is used as an arg
	 * in the {@code desc}.
	 * <p>
	 * By default, when a {@link SpellArg#CARD_FILTER} is specified and a {@link SpellArg#CARD_SOURCE} is not, the default
	 * card source used is {@link UnweightedCatalogueSource}.
	 * <p>
	 * The cards are chosen randomly <b>without replacement</b>.
	 *
	 * @param context The game context
	 * @param player  The player from whose point of view these cards should be retrieved
	 * @param target  The target, which can be {@code null}
	 * @param source  The source or host {@link Entity}, typically the origin of this spell cast.
	 * @param desc    The {@link SpellDesc} typically of the calling spell.
	 * @param count   The maximum number of cards to return, exclusively and randomly, from the generated card list. Or,
	 *                returns all the cards if {@code count > cards.size()}, where {@code cards} is all the possible
	 *                cards.
	 * @return A list of cards.
	 */
	public static CardList getCards(GameContext context, Player player, Entity target, Entity source, SpellDesc desc, int count) {
		CardList cards = new CardArrayList(Arrays.asList(getCards(context, desc)));
		boolean hasCardSourceOrFilter = desc.containsKey(SpellArg.CARD_SOURCE) || desc.containsKey(SpellArg.CARD_FILTER);
		if (cards.isEmpty()) {
			if (hasCardSourceOrFilter) {
				cards.addAll(desc.getFilteredCards(context, player, source));
			} else if (target != null) {
				cards.add(target.getSourceCard());
			}
		}

		if (count < cards.size()) {
			CardList result = new CardArrayList();
			int i = count;
			while (cards.size() > 0
					&& i > 0) {
				if (Strand.currentStrand().isInterrupted()) {
					break;
				}
				result.add(context.getLogic().removeRandom(cards));
				i--;
			}
			return result;
		} else {
			return cards;
		}
	}

	/**
	 * Determines whether any of the {@link Entity#isInPlay()} entities belonging to the {@code playerId} host an
	 * unexpired, active instance of the {@code auraClass} aura.
	 *
	 * @param context
	 * @param playerId
	 * @param auraClass
	 * @param <T>
	 * @return {@code true} if such an aura is found.
	 * @see #getAuras(GameContext, int, Class) to retrieve the aura instances themselves.
	 */
	public static <T extends Aura> boolean hasAura(GameContext context, int playerId, Class<T> auraClass) {
		return context.getEntities()
				.filter(e -> e.getOwner() == playerId && e.isInPlay())
				.anyMatch(m -> context.getTriggersAssociatedWith(m.getReference()).stream()
						.filter(auraClass::isInstance)
						.map(t -> (Aura) t)
						.anyMatch(((Predicate<Aura>) Aura::isExpired).negate()));
	}

	/**
	 * Retrieves all of the unexpired, active auras that are instances of the {@code auraClass} hosted by {@link
	 * Entity#isInPlay()} entities belonging to the {@code playerId} or passive auras hosted by hero powers and cards.
	 *
	 * @param context
	 * @param playerId
	 * @param auraClass
	 * @param <T>
	 * @return A list of aura instances.
	 */
	public static <T extends Aura> List<T> getAuras(GameContext context, int playerId, @NotNull Class<T> auraClass) {
		return context.getTriggerManager()
				.getTriggers()
				.stream()
				.filter(e -> e.getOwner() == playerId && !e.isExpired() && auraClass.isInstance(e))
				.map(auraClass::cast)
				// Should respect order of play
				.sorted(Comparator.comparingInt(Entity::getId))
				.collect(toList());
	}

	/**
	 * Get the auras that are affecting the specified target of the given class.
	 *
	 * @param context
	 * @param auraClass
	 * @param target
	 * @param <T>
	 * @return
	 */
	public static <T extends Aura> List<T> getAuras(GameContext context, Class<T> auraClass, Entity target) {
		return context.getTriggerManager().getTriggers().stream()
				.filter(aura -> auraClass.isInstance(aura) && !aura.isExpired())
				.map(auraClass::cast)
				.filter(aura -> aura.getAffectedEntities().contains(target.getId()) || aura.getAffectedEntities().contains(target.getSourceCard().getId()))
				.collect(toList());
	}

	/**
	 * Retrieves an array of spells corresponding to the {@link AuraArg#APPLY_EFFECT}
	 * field on an aura whose condition is null or fulfilled for the given {@code source} and {@code target}.
	 *
	 * @param context
	 * @param playerId
	 * @param auraClass
	 * @param source
	 * @param target
	 * @return A list of spells
	 */
	public static SpellDesc[] getBonusesFromAura(GameContext context, int playerId, Class<? extends Aura> auraClass, Entity source, Entity target) {
		return context.getEntities()
				.filter(e -> e.getOwner() == playerId && e.isInPlay())
				.flatMap(m -> context.getTriggersAssociatedWith(m.getReference()).stream()
						.filter(auraClass::isInstance)
						.map(t -> (Aura) t)
						.filter(((Predicate<Aura>) Aura::isExpired).negate())
						.filter(aura -> aura.getCondition() == null || aura.getCondition().isFulfilled(context, context.getPlayer(playerId), source, target))
						.map(aura -> aura.getDesc().getApplyEffect()))
				.toArray(SpellDesc[]::new);
	}

	/**
	 * Tries to determine the currently casting player from the point of view of a source, considering if the source has
	 * changed owners or if it was destroyed.
	 *
	 * @param context             The game context
	 * @param player              The casting player
	 * @param source              The source from whose point of view the casting player should be determined
	 * @param castingTargetPlayer Whose point of view the determination should be made. For example, if {@link
	 *                            TargetPlayer#OPPONENT} is chosen here, then the opponent of the owner of the {@code
	 *                            source} will be used.
	 * @return An object containing information related to who is the casting player and whether or not the source has
	 * been destroyed.
	 */
	public static DetermineCastingPlayer determineCastingPlayer(GameContext context, Player player, Entity source, TargetPlayer castingTargetPlayer) {
		return new DetermineCastingPlayer(context, player, source, castingTargetPlayer).invoke();
	}

	/**
	 * Returns {@code true} if the caller is in a recursive stack
	 *
	 * @param callingClass
	 */
	public static boolean isRecursive(Class<? extends Spell> callingClass) {
		/*
		return StackWalker.getInstance().walk(s -> s
				.takeWhile(f -> f.getClassName().contains(GameContext.class.getPackageName()))
				.skip(2)
				.limit(16)
				.anyMatch(f -> f.getClassName().contains(callingClass.getName())));*/
		return false;
	}

	/**
	 * Gets a list of special card IDs.
	 *
	 * @return
	 */
	public static Set<String> getSpecialCards() {
		return specialCards;
	}

	/**
	 * An object that contains results of a {@link #determineCastingPlayer(GameContext, Player, Entity, TargetPlayer)}
	 * call.
	 */
	public static class DetermineCastingPlayer {
		private boolean sourceDestroyed;
		private GameContext context;
		private Player player;
		private Entity source;
		private TargetPlayer castingTargetPlayer;
		private Player castingPlayer;

		public DetermineCastingPlayer(GameContext context, Player player, Entity source, TargetPlayer castingTargetPlayer) {
			this.context = context;
			this.player = player;
			this.source = source;
			this.castingTargetPlayer = castingTargetPlayer;
		}

		public boolean isSourceInPlay() {
			return !sourceDestroyed;
		}

		public Player getCastingPlayer() {
			return castingPlayer;
		}

		public DetermineCastingPlayer invoke() {
			// In case the minion changes sides, this should case who the spells are being cast for.
			switch (castingTargetPlayer) {
				case BOTH:
				case OWNER:
				default:
					castingPlayer = context.getPlayer(source.getOwner());
					break;
				case SELF:
					castingPlayer = player;
					break;
				case OPPONENT:
					castingPlayer = context.getOpponent(player);
					break;
				case ACTIVE:
					castingPlayer = context.getActivePlayer();
					break;
				case PLAYER_1:
					castingPlayer = context.getPlayer1();
					break;
				case PLAYER_2:
					castingPlayer = context.getPlayer2();
					break;
				case INACTIVE:
					castingPlayer = context.getOpponent(context.getActivePlayer());
					break;
			}

			// If the minion is removed from the board, stop casting spells.
			if (source != null
					&& !source.isInPlay()) {
				sourceDestroyed = true;
				return this;
			}

			// If the minion is transformed, stop casting spells
			if (source != source.transformResolved(context)) {
				sourceDestroyed = true;
				return this;
			}

			sourceDestroyed = false;
			return this;
		}
	}
}
