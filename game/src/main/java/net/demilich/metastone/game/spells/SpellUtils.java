package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.minions.RelativeToSource;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.Operation;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * A set of utilities to help write spells.
 */
public class SpellUtils {
	/**
	 * Sets upu the source and target references for casting a child spell, typically an "effect" of a spell defined on
	 * a card.
	 *
	 * @param context The game context.
	 * @param player  The player casting the spell.
	 * @param spell   The child spell to cast.
	 * @param source  The source of the spell, typically the spell card or minion whose battlecry is being called.
	 * @param target  The target reference.
	 * @see net.demilich.metastone.game.logic.GameLogic#castSpell(int, SpellDesc, EntityReference, EntityReference,
	 * TargetSelection, boolean) where its parameter {@code childSpell = true}.
	 */
	@Suspendable
	public static void castChildSpell(GameContext context, Player player, SpellDesc spell, Entity source, Entity target) {
		EntityReference sourceReference = source != null ? source.getReference() : null;
		EntityReference targetReference = spell.getTarget();
		if (targetReference == null && target != null) {
			targetReference = target.getReference();
		}
		context.getLogic().castSpell(player.getId(), spell, sourceReference, targetReference, true);
	}

	/**
	 * Given a filter {@link Operation}, return a boolean representing whether that operation is satisfied.
	 *
	 * @param operation   The algebraic operation.
	 * @param actualValue The left hand side.
	 * @param targetValue The right hand side.
	 * @return {@code true} if the evaluation is truue.
	 */
	public static boolean evaluateOperation(Operation operation, int actualValue, int targetValue) {
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
	 * Consider the {@link net.demilich.metastone.game.Environment#PENDING_CARD} and {@link net.demilich.metastone.game.Environment#EVENT_CARD},
	 * the search the {@link Zones#DISCOVER} zone for the specified card
	 * @param context
	 * @param cardId
	 * @return
	 */
	private static Card getSingleCard(GameContext context, String cardId) {
		if (cardId == null) {
			return null;
		}
		Card card;
		if (cardId.toUpperCase().equals("PENDING_CARD")) {
			card = context.getPendingCard();
		} else if (cardId.toUpperCase().equals("EVENT_CARD")) {
			card = context.getEventCard();
		} else {
			card = getCardFromContextOrDiscover(context, cardId);
		}
		return card;
	}

	public static Card[] getCards(GameContext context, SpellDesc spell) {
		String[] cardIds;
		if (spell.containsKey(SpellArg.CARDS)) {
			cardIds = (String[]) spell.get(SpellArg.CARDS);
		} else {
			cardIds = new String[1];
			cardIds[0] = (String) spell.get(SpellArg.CARD);
		}
		Card[] cards = new Card[cardIds.length];
		for (int i = 0; i < cards.length; i++) {
			// If the discover zone contains the card, reference it instead
			final String cardId = cardIds[i];
			cards[i] = getSingleCard(context, cardId);
		}
		return cards;
	}

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
	 * @param player  {@link Player#getBehaviour()} will be called to get the behaviour that will choose from the
	 *                cards.
	 * @param desc    For every card the player can discover, this method will create a {@link Spell} from this {@link
	 *                SpellDesc} and set its {@link SpellArg#CARD} argument to the discoverable card. Typically, this
	 *                {@link SpellDesc} defines a {@link ReceiveCardSpell}, {@link ReceiveCardAndDoSomethingSpell}, or a
	 *                {@link ChangeHeroPowerSpell}. These spells all receive cards as arguments. This argument allows a
	 *                {@link DiscoverAction} to do more sophisticated things than just put cards into hands.
	 * @param cards   A {@link CardList} of cards that get copied, added to the {@link Zones#DISCOVER} zone of the
	 *                player and shown in the discover card UI to the player.
	 * @return The {@link DiscoverAction} that corresponds to the card the player chose.
	 * @see DiscoverCardSpell for the spell that typically calls this method.
	 * @see ReceiveCardSpell for the spell that is typically the {@link SpellArg#SPELL} property of a {@link
	 * DiscoverCardSpell}.
	 */
	@Suspendable
	public static DiscoverAction discoverCard(GameContext context, Player player, SpellDesc desc, CardList cards) {
		// Discovers always work with a copy of the incoming cards
		cards = cards.getCopy();
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		List<GameAction> discoverActions = new ArrayList<>();
		for (int i = 0; i < cards.getCount(); i++) {
			Card card = cards.get(i);
			card.setId(context.getLogic().getIdFactory().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.DISCOVER);

			SpellDesc spellClone = spell.addArg(SpellArg.CARD, card.getCardId());
			DiscoverAction discover = DiscoverAction.createDiscover(spellClone);
			discover.setCard(card);
			discover.setId(i);
			discoverActions.add(discover);
		}

		return postDiscover(context, player, cards, discoverActions);
	}

	private static DiscoverAction postDiscover(GameContext context, Player player, Iterable<? extends Card> cards, List<GameAction> discoverActions) {
		if (discoverActions.size() == 0) {
			return null;
		}
		final DiscoverAction discoverAction;

		if (context.getLogic().attributeExists(Attribute.ALL_RANDOM_YOGG_ONLY_FINAL_DESTINATION)) {
			discoverAction = (DiscoverAction) discoverActions.get(context.getLogic().random(discoverActions.size()));
		} else {
			discoverAction = (DiscoverAction) player.getBehaviour().requestAction(context, player, discoverActions);
		}

		// Move the cards back
		for (Card card : cards) {
			// Cards that are being discovered are always copies, so they are always removed from play afterwards.
			card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
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
	 * @param source  The source entity, typically the {@link SpellCard} or {@link Minion#getBattlecry()} that initiated
	 *                this call.
	 * @return A {@link DiscoverAction} whose {@link DiscoverAction#getCard()} property corresponds to the selected
	 * card. To retrieve the spell, get the card's spell with {@link SpellCard#getSpell()}.
	 */
	@Suspendable
	public static DiscoverAction getSpellDiscover(GameContext context, Player player, SpellDesc desc, List<SpellDesc> spells, Entity source) {
		List<GameAction> discoverActions = new ArrayList<>();
		List<Card> cards = new ArrayList<>();
		for (int i = 0; i < spells.size(); i++) {
			final SpellDesc spell = spells.get(i);
			final SpellCardDesc spellCardDesc = new SpellCardDesc();
			final String name = spell.getString(SpellArg.NAME);
			final String description = spell.getString(SpellArg.DESCRIPTION);
			// TODO: Parse the parenthesized part of a name in a spell as a description
			spellCardDesc.id = context.getLogic().generateCardId();
			spellCardDesc.name = name;
			List<Entity> entities = context.resolveTarget(player, source, desc.getTarget());
			int baseManaCost = 0;
			if (entities != null
					&& entities.size() > 0) {
				baseManaCost = desc.getValue(SpellArg.MANA, context, player, entities.get(0), source, 0);
			}
			spellCardDesc.baseManaCost = baseManaCost;
			spellCardDesc.description = description;
			spellCardDesc.heroClass = HeroClass.ANY;
			spellCardDesc.type = CardType.SPELL;
			spellCardDesc.rarity = Rarity.FREE;
			spellCardDesc.targetSelection = (TargetSelection) spell.getOrDefault(SpellArg.TARGET_SELECTION, desc.getOrDefault(SpellArg.TARGET_SELECTION, TargetSelection.NONE));
			spellCardDesc.spell = spell;
			spellCardDesc.collectible = false;

			Card card = spellCardDesc.createInstance();
			card.setId(context.getLogic().getIdFactory().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.DISCOVER);
			cards.add(card);

			DiscoverAction discover = DiscoverAction.createDiscover(spell);
			discover.setId(i);
			discover.setCard(card);
			discover.setName(name);
			discover.setDescription(description);
			discoverActions.add(discover);
		}

		return postDiscover(context, player, cards, discoverActions);
	}

	@Suspendable
	public static Card getRandomCard(CardList source, Predicate<Card> filter) {
		CardList result = getCards(source, filter);
		if (result.isEmpty()) {
			return null;
		}
		return result.getRandom();
	}

	@Suspendable
	public static HeroClass getRandomHeroClass() {
		HeroClass[] values = HeroClass.values();
		List<HeroClass> heroClasses = new ArrayList<HeroClass>();
		for (HeroClass heroClass : values) {
			if (heroClass.isBaseClass()) {
				heroClasses.add(heroClass);
			}
		}
		return heroClasses.get(ThreadLocalRandom.current().nextInt(heroClasses.size()));
	}

	public static HeroClass getRandomHeroClassExcept(HeroClass... heroClassesExcluded) {
		HeroClass[] values = HeroClass.values();
		List<HeroClass> heroClasses = new ArrayList<HeroClass>();
		for (HeroClass heroClass : values) {
			if (heroClass.isBaseClass()) {
				heroClasses.add(heroClass);
				for (HeroClass heroClassExcluded : heroClassesExcluded) {
					if (heroClassExcluded == heroClass) {
						heroClasses.remove(heroClass);
					}
				}
			}
		}
		return heroClasses.get(ThreadLocalRandom.current().nextInt(heroClasses.size()));
	}

	public static <T> T getRandomTarget(List<T> targets) {
		int randomIndex = ThreadLocalRandom.current().nextInt(targets.size());
		return targets.get(randomIndex);
	}

	public static List<Actor> getValidRandomTargets(List<Entity> targets) {
		List<Actor> validTargets = new ArrayList<Actor>();
		for (Entity entity : targets) {
			Actor actor = (Actor) entity;
			if (!actor.isDestroyed() || actor.getEntityType() == EntityType.HERO) {
				validTargets.add(actor);
			}

		}
		return validTargets;
	}

	public static List<Entity> getValidTargets(GameContext context, Player player, List<Entity> allTargets, EntityFilter filter) {
		if (filter == null) {
			return allTargets;
		}
		List<Entity> validTargets = new ArrayList<>();
		for (Entity entity : allTargets) {
			if (filter.matches(context, player, entity)) {
				validTargets.add(entity);
			}
		}
		return validTargets;
	}

	public static int hasHowManyOfRace(Player player, Race race) {
		int count = 0;
		for (Minion minion : player.getMinions()) {
			if (minion.getRace() == race) {
				count++;
			}
		}
		return count;
	}

	public static boolean highlanderDeck(Player player) {
		List<String> cards = new ArrayList<String>();
		for (Card card : player.getDeck()) {
			if (cards.contains(card.getCardId())) {
				return false;
			}
			cards.add(card.getCardId());
		}
		return true;
	}

	public static boolean holdsCardOfType(Player player, CardType cardType) {
		for (Card card : player.getHand()) {
			if (card.getCardType().isCardType(cardType)) {
				return true;
			}
		}
		return false;
	}

	public static boolean holdsMinionOfRace(Player player, Race race) {
		for (Card card : player.getHand()) {
			if (card.getAttribute(Attribute.RACE) == race) {
				return true;
			}
		}
		return false;
	}

	@Suspendable
	public static int howManyMinionsDiedThisTurn(GameContext context) {
		int currentTurn = context.getTurn();
		int count = 0;
		for (Player player : context.getPlayers()) {
			for (Entity deadEntity : player.getGraveyard()) {
				if (deadEntity.getEntityType() != EntityType.MINION) {
					continue;
				}

				if (deadEntity.getAttributeValue(Attribute.DIED_ON_TURN) == currentTurn) {
					count++;
				}

			}
		}
		return count;
	}

	public static int getBoardPosition(GameContext context, Player player, SpellDesc desc, Entity source) {
		final int UNDEFINED = -1;
		int boardPosition = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE, UNDEFINED);
		if (boardPosition != UNDEFINED) {
			return boardPosition;
		}
		RelativeToSource relativeBoardPosition = (RelativeToSource) desc.get(SpellArg.BOARD_POSITION_RELATIVE);
		if (relativeBoardPosition == null) {
			return UNDEFINED;
		}

		int sourcePosition = ((Minion) source).getEntityLocation().getIndex();
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

	public static MinionCard getMinionCardFromSummonSpell(GameContext context, Player player, Entity source, SpellDesc desc) {
		// TODO: Actually create a minion card
		return (MinionCard) (new MinionCardDesc().createInstance());
	}

	static SpellDesc[] getGroup(GameContext context, SpellDesc spell) {
		Card card = null;
		String cardName = (String) spell.get(SpellArg.GROUP);
		card = context.getCardById(cardName);
		if (card != null && card instanceof GroupCard) {
			GroupCard groupCard = (GroupCard) card;
			return groupCard.getGroup();
		}
		return null;
	}
}
