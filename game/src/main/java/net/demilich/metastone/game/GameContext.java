package net.demilich.metastone.game;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.NetworkBehaviour;
import io.vertx.core.Handler;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.heroes.MetaHero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.environment.EnvironmentDeque;
import net.demilich.metastone.game.environment.EnvironmentValue;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.logic.TargetLogic;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.NetworkDelegate;
import net.demilich.metastone.game.utils.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A game context helps execute a match of Spellsource, providing a place to store state, deliver requests for actions
 * to players, apply those player actions through a {@link GameLogic}, and then save the updated state as a result of
 * those actions.
 * <p>
 * For example, this code starts a game between two opponents that perform random actions:
 * <pre>
 * {@code
 * // Adds every card set to the card format available in this game.
 * DeckFormat deckFormat = new DeckFormat();
 * for (CardSet set : CardSet.values()) {
 *      deckFormat.addSet(set);
 * }
 * // Chooses a random class and creates a player for it.
 * HeroClass heroClass1 = getRandomClass();
 * // Note "PlayRandomBehaviour"--this is the delegate for player actions.
 * PlayerConfig player1Config = new PlayerConfig(DeckFactory.getRandomDeck(heroClass1, deckFormat), new
 * PlayRandomBehaviour());
 * player1Config.setName("Player 1");
 * player1Config.setHeroCard(getHeroCardForClass(heroClass1));
 * Player player1 = new Player(player1Config);
 * // Chooses another random class and creates another player for it
 * HeroClass heroClass2 = getRandomClass();
 * PlayerConfig player2Config = new PlayerConfig(DeckFactory.getRandomDeck(heroClass2, deckFormat), new
 * PlayRandomBehaviour());
 * player2Config.setName("Player 2");
 * player2Config.setHeroCard(getHeroCardForClass(heroClass2));
 * Player player2 = new Player(player2Config);
 * // Creates a game context with the given players, a new game logic, and the specified deck format.
 * GameContext context = new GameContext(player1, player2, new GameLogic(), deckFormat);
 * // Plays the game to completion.
 * context.play();
 * // Disposes the context.
 * context.dispose();
 * }
 * </pre>
 * <p>
 * Based on the code above, you'll see the minimum requirements to execute a {@link #play()} command: <ul> <li>2 {@link
 * Player} objects, each configured with a {@link Behaviour}. These objects represent (1) the most important part of the
 * game state (encoded inside the fields of the {@link Player} object, like {@link Player#getMinions()}; and (2) the
 * {@link Behaviour} delegate for player actions and mulligans. </li>. <li>A {@link GameLogic} instance. It handles
 * everything in between receiving a player action to the request for the next player action..</li> <li>A {@link
 * DeckFormat}, which is a collection of {@link CardSet} values that correspond to the (1) legal cards that may be
 * played and put into decks and (2) legal cards that may appear in randomly drawn or created card
 * effects.</li></ul><p>
 * <p>
 * Game state is composed of a variety of fields that live inside the context. To get a copy of the state, use {@link
 * #getGameStateCopy()}; while you can access a modifiable copy of the {@link GameState} with {@link #getGameState()},
 * you're encouraged only use the {@link GameLogic} methods (which mutate the state stored inside this game context) in
 * order to always have valid data.
 * <p>
 * Game actions are chosen by {@link Behaviour} objects living inside the {@link Player} object. Typically, the {@link
 * GameLogic} instance will call {@link #getActivePlayer()} for the currently active player, call {@link
 * Player#getBehaviour()} to get the behaviour, and then call {@link Behaviour#requestAction(GameContext, Player, List)}
 * to request which action of a list of actions the player takes. Note that this is just called as a plain function, so
 * the end user of the {@link GameContext} is responsible for the blocking that would occur if e.g. the {@link
 * Behaviour} waits on user input to answer the action request. Ordinarily, as long as the thread running {@link
 * GameContext} doesn't do anything but process the game, blocking on user input isn't an issue; only one player may
 * take an action at a time.
 * <p>
 * State is mutated by the {@link GameLogic} instance. It will process a player's selected {@link GameAction} with
 * {@link #performAction(int, GameAction)}, mutating the state fields in the {@link GameContext} appropriately until it
 * encounters the next request for actions (e.g., once an action has been processed, when a battlecry is resolved, or
 * when the player must choose which card to discover). It is not necessarily dangerous to modify the game state without
 * using {@link GameLogic}, though doing so many break what players would expect to happen based on the text of cards.
 * <p>
 * Generally, this instance does not provide a way to "choose" actions in game. The end user of a {@link GameContext} is
 * expected to provide a {@link Player} instance with a {@link Behaviour} that suits the end user's needs.
 *
 * @see #play() for more about how a game is "played."
 * @see Behaviour for the interface that the {@link GameContext} delegates player actions and notifications to. This is
 * both the "event handler" specification for which events a player may be interested in; and also a "delegate" in the
 * sense that the object implementing this interface makes decisions about what actions in the game to take (with e.g.
 * {@link Behaviour#requestAction(GameContext, Player, List)}.
 * @see net.demilich.metastone.game.behaviour.PlayRandomBehaviour for an example behaviour that just makes random
 * decisions when requested.
 * @see NetworkBehaviour for the class that turns requests to the {@link Behaviour} into calls over the network.
 * @see GameLogic for the class that actually implements the Spellsource game rules. This class requires a {@link
 * GameContext} because it manipulates the state stored in it.
 * @see GameState for a class that encapsulates all of the state of a game of Spellsource.
 * @see #getGameState() to access and modify the game state.
 * @see #getGameStateCopy() to get a copy of the state that can be stored and diffed.
 * @see #getEntities() for a way to enumerate through all of the entities in the game.
 */
public class GameContext implements Cloneable, Serializable, NetworkDelegate {
	public static final int PLAYER_1 = 0;
	public static final int PLAYER_2 = 1;

	protected static final Logger logger = LoggerFactory.getLogger(GameContext.class);

	private Player[] players = new Player[2];
	private GameLogic logic;
	private DeckFormat deckFormat;
	private TargetLogic targetLogic = new TargetLogic();
	private TriggerManager triggerManager = new TriggerManager();
	private Map<Environment, Object> environment = new HashMap<>();
	private List<CardCostModifier> cardCostModifiers = new ArrayList<>();
	private int activePlayerId = -1;
	private Player winner;
	private GameStatus result;
	private TurnState turnState = TurnState.TURN_ENDED;
	private boolean disposed = false;
	private int turn;
	private int actionsThisTurn;
	private boolean ignoreEvents;
	private CardList tempCards = new CardArrayList();

	/**
	 * Creates a game context with no valid start state.
	 */
	public GameContext() {
	}

	/**
	 * Creates an uninitialized game context (i.e., no cards in the decks of the players or behaviours specified).
	 *
	 * @param playerHero1 The first player's {@link HeroClass}
	 * @param playerHero2 The second player's {@link HeroClass}
	 * @return A game context.
	 */
	public static GameContext uninitialized(HeroClass playerHero1, HeroClass playerHero2) {
		final Player player1 = new Player();
		final Player player2 = new Player();
		player1.setHero(MetaHero.getHeroCard(playerHero1).createHero());
		player2.setHero(MetaHero.getHeroCard(playerHero2).createHero());
		return new GameContext(player1, player2, new GameLogic(), new DeckFormat().withCardSets(CardSet.values()));
	}

	/**
	 * Creates a game context from the given state.
	 *
	 * @param state A {@link GameState} object.
	 */
	public GameContext(GameState state) {
		this();
		setGameState(state);
	}

	/**
	 * Creates a game context from the given players, logic and deck format.
	 *
	 * @param player1    The first {@link Player} with a valid {@link Behaviour}. This is not necessarily the player who
	 *                   will go first, which is determined by a coin flip.
	 * @param player2    The second {@link Player} with a valid {@link Behaviour}.
	 * @param logic      The game logic instance to use as the rules of the game.
	 * @param deckFormat The cards that are legal to play in terms of a set of {@link CardSet} values.
	 */
	public GameContext(Player player1, Player player2, GameLogic logic, DeckFormat deckFormat) {
		if (player1.getId() == IdFactory.UNASSIGNED) {
			player1.setId(PLAYER_1);
		}

		setPlayer(player1.getId(), player1);

		if (player2 != null) {
			if (player2.getId() == IdFactory.UNASSIGNED) {
				player2.setId(PLAYER_2);
			}

			this.setPlayer(player2.getId(), player2);
		}

		this.setLogic(logic);
		this.setDeckFormat(deckFormat);
	}

	protected boolean acceptAction(GameAction nextAction) {
		return true;
	}

	/**
	 * Adds a temporary card. A temporary card is a card that exists only in this instance and not in the {@link
	 * CardCatalogue}.
	 *
	 * @param card The card to add, typically made with code.
	 */
	public void addTempCard(Card card) {
		getTempCards().addCard(card);
	}

	/**
	 * Adds a trigger to the game.
	 *
	 * @param trigger An {@link Trigger} that is used as a delegate whenever an event is fired in the game.
	 * @see #fireGameEvent(GameEvent, List) for more about firing game events.
	 */
	public void addTrigger(Trigger trigger) {
		getTriggerManager().addTrigger(trigger);
	}

	/**
	 * Clones the game context, recursively cloning the game state and logic.
	 * <p>
	 * Internally, this is used by AI functions to evaluate a game state until a win condition (or just the end of the
	 * turn) is reached.
	 * <p>
	 * This method is marked {@code synchronized} because the {@link GameContext} object is not thread safe. Two threads
	 * can't clone and mutate a context at the same time.
	 *
	 * @return A cloned instance of the game context.
	 */
	@Override
	public synchronized GameContext clone() {
		GameLogic logicClone = getLogic().clone();
		Player player1Clone = getPlayer1().clone();
		Player player2Clone = getPlayer2().clone();
		GameContext clone = new GameContext(player1Clone, player2Clone, logicClone, getDeckFormat());
		clone.setTempCards(getTempCards().clone());
		clone.setTriggerManager(getTriggerManager().clone());
		clone.setActivePlayerId(activePlayerId);
		clone.setTurn(getTurn());
		clone.setActionsThisTurn(getActionsThisTurn());
		clone.setResult(getStatus());
		clone.setTurnState(getTurnState());
		clone.setWinner(logicClone.getWinner(player1Clone, player2Clone));
		clone.getCardCostModifiers().clear();

		for (CardCostModifier cardCostModifier : getCardCostModifiers()) {
			clone.getCardCostModifiers().add(cardCostModifier.clone());
		}

		for (Map.Entry<Environment, Object> entry : getEnvironment().entrySet()) {
			Object value1 = entry.getValue();
			if (value1 == null
					|| !EnvironmentValue.class.isAssignableFrom(value1.getClass())) {
				clone.getEnvironment().put(entry.getKey(), value1);
			} else {
				EnvironmentValue value = (EnvironmentValue) value1;
				clone.getEnvironment().put(entry.getKey(), value.getCopy());
			}
		}

		return clone;
	}

	/**
	 * Clears state to ensure this context isn't referencing it anymore.
	 */
	@Deprecated
	public synchronized void dispose() {
		this.disposed = true;
		this.players = null;
		getCardCostModifiers().clear();
		getTriggerManager().dispose();
		getEnvironment().clear();
	}

	/**
	 * Ends the game immediately.
	 */
	@Suspendable
	protected void endGame() {
		setWinner(getLogic().getWinner(getActivePlayer(), getOpponent(getActivePlayer())));

		notifyPlayersGameOver();

		calculateStatistics();
	}

	@Override
	@Suspendable
	public void networkRequestAction(GameState state, int playerId, List<GameAction> actions, Handler<GameAction> callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void networkRequestMulligan(Player player, List<Card> starterCards, Handler<List<Card>> callback) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendGameOver(Player recipient, Player winner) {
	}

	@Suspendable
	protected void notifyPlayersGameOver() {
		for (Player player : getPlayers()) {
			player.getBehaviour().onGameOver(this, player.getId(), getWinner() != null ? getWinner().getId() : -1);
		}
	}

	protected void calculateStatistics() {
		if (getWinner() != null) {
			logger.debug("Game finished after " + getTurn() + " turns, the winner is: " + getWinner().getName());
			getWinner().getStatistics().gameWon();
			Player loser = getOpponent(getWinner());
			loser.getStatistics().gameLost();
		} else {
			logger.debug("Game finished after " + getTurn() + " turns, DRAW");
			getPlayer1().getStatistics().gameLost();
			getPlayer2().getStatistics().gameLost();
		}
	}

	/**
	 * Ends the current player's turn immediately, setting the active player to their opponent.
	 */
	@Suspendable
	public void endTurn() {
		getLogic().endTurn(getActivePlayerId());
		setActivePlayerId(getLogic().getNextActivePlayerId());
		onGameStateChanged();
		setTurnState(TurnState.TURN_ENDED);
	}


	@Deprecated
	private Card findCardinCollection(Iterable<Card> cardCollection, int cardId) {
		for (Card card : cardCollection) {
			if (card.getId() == cardId) {
				return card;
			}
		}
		return null;
	}

	/**
	 * Fires a game event.
	 *
	 * @param gameEvent The event to fire.
	 * @see #fireGameEvent(GameEvent, List) for a complete description of this function.
	 */
	@Suspendable
	public void fireGameEvent(GameEvent gameEvent) {
		fireGameEvent(gameEvent, null);
	}

	/**
	 * Fires a {@link GameEvent}.
	 * <p>
	 * Game events two purposes:
	 * <p>
	 * <ol><li>They implement trigger-based gameplay like card text that reads, "Whenever a minion is healed, draw a
	 * card." </li><li>They are changes in game state that are notable for a player to see. They can be interpreted as
	 * checkpoints that need to be rendered to the client</li><li></ol>
	 * <p>
	 * Typically a {@link GameEvent} is instantiated inside a function in {@link GameLogic}, like {@link
	 * net.demilich.metastone.game.events.SummonEvent} inside {@link GameLogic#summon(int, Minion, Card, int, boolean)},
	 * and then fired by the {@link GameLogic} using this function.
	 *
	 * @param gameEvent     The {@link GameEvent} to fire.
	 * @param otherTriggers Other triggers to consider besides the ones inside this game context's {@link
	 *                      TriggerManager}. This may be synthetic triggers that implement analytics, networked game
	 *                      logic, newsfeed reports, spectating features, etc.
	 * @see net.demilich.metastone.game.spells.trigger.HealingTrigger for an example of a trigger that listens to a
	 * specific event.
	 * @see TriggerManager#fireGameEvent(GameEvent, List) for the complete game logic for firing game events.
	 * @see #addTrigger(Trigger) for the place to add triggers that react to game events.
	 */
	@Suspendable
	public void fireGameEvent(GameEvent gameEvent, List<Trigger> otherTriggers) {
		if (ignoreEvents()) {
			return;
		}

		getTriggerManager().fireGameEvent(gameEvent, otherTriggers);
	}

	/**
	 * Determines whether the game is over (decided). As a side effect, records the current result of the game.
	 *
	 * @return {@code true} if the game has been decided by concession or because one of the two heroes have been
	 * destroyed.
	 */
	public boolean updateAndGetGameOver() {
		if (getPlayer1() == null
				|| getPlayer2() == null) {
			setResult(GameStatus.RUNNING);
			return false;
		}
		setResult(getLogic().getMatchResult(getActivePlayer(), getOpponent(getActivePlayer())));
		setWinner(getLogic().getWinner(getActivePlayer(), getOpponent(getActivePlayer())));
		return getStatus() != GameStatus.RUNNING;
	}

	/**
	 * Gets a reference to the currently active player (the player whose turn it is).
	 *
	 * @return The player whose turn it is.
	 */
	public Player getActivePlayer() {
		return getPlayer(getActivePlayerId());
	}

	/**
	 * Gets the integer ID of the player whose current turn it is.
	 *
	 * @return The integer ID.
	 */
	public int getActivePlayerId() {
		return activePlayerId;
	}

	/**
	 * Gets the minions adjacent to the given minion. Omits permanents.
	 *
	 * @param minionReference The minion whose adjacent minions we should get.
	 * @return The adjacent minions.
	 */
	public List<Actor> getAdjacentMinions(EntityReference minionReference) {
		List<Actor> adjacentMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		List<Minion> minions = getPlayer(minion.getOwner()).getMinions();
		int index = minion.getEntityLocation().getIndex();
		if (index == -1) {
			return TargetLogic.withoutPermanents(adjacentMinions);
		}
		int left = index - 1;
		int right = index + 1;
		if (left > -1 && left < minions.size()) {
			adjacentMinions.add(minions.get(left));
		}
		if (right > -1 && right < minions.size()) {
			adjacentMinions.add(minions.get(right));
		}
		return TargetLogic.withoutPermanents(adjacentMinions);
	}

	/**
	 * Retrieves a hero power action that occurs automatically at the start of the turn, if one is specified for the
	 * hero.
	 * <p>
	 * Implements certain scenarios.
	 *
	 * @return The game action that should be performed.
	 */
	@Suspendable
	public GameAction getAutoHeroPowerAction() {
		return getLogic().getAutoHeroPowerAction(getActivePlayerId());
	}

	/**
	 * Gets a card by ID, checking both the catalogue and the cards in {@link #getTempCards()}.
	 *
	 * @param cardId The string card ID.
	 * @return A clone of the {@link Card}.
	 */
	public Card getCardById(String cardId) {
		Card card = CardCatalogue.getCardById(cardId);
		if (card == null) {
			for (Card tempCard : getTempCards()) {
				if (tempCard.getCardId().equalsIgnoreCase(cardId)) {
					return tempCard.clone();
				}
			}
		}
		return card;
	}

	/**
	 * Gets the current card cost modifiers in play.
	 *
	 * @return A list of {@link CardCostModifier} objects.
	 */
	public List<CardCostModifier> getCardCostModifiers() {
		return cardCostModifiers;
	}

	/**
	 * Retrieves all the damage values that are supposed to be applied.
	 * <p>
	 * Implements spells that override the amount of damage something deals.
	 *
	 * @return The stack.
	 */
	@SuppressWarnings("unchecked")
	public Deque<Integer> getDamageStack() {
		if (!getEnvironment().containsKey(Environment.DAMAGE_STACK)) {
			getEnvironment().put(Environment.DAMAGE_STACK, new EnvironmentDeque<Integer>());
		}
		return (Deque<Integer>) getEnvironment().get(Environment.DAMAGE_STACK);
	}

	/**
	 * Gets the {@link DeckFormat} of this context, or the currently legal cards in terms of {@link CardSet} objects.
	 *
	 * @return A {@link DeckFormat} object.
	 */
	public DeckFormat getDeckFormat() {
		return deckFormat;
	}

	/**
	 * Gets a reference to the game context's environment, a piece of game state that keeps tracks of which minions are
	 * currently being summoned, which targets are being targeted, how much damage is set to be dealt, etc.
	 * <p>
	 * This helps implement a variety of complex rules in the game.
	 *
	 * @return A mutable map of environment variables.
	 * @see Environment for a description of the environment variables.
	 */
	public Map<Environment, Object> getEnvironment() {
		return environment;
	}

	/**
	 * Gets the current event card.
	 *
	 * @return The event card.
	 * @see Environment#EVENT_CARD for more.
	 */
	public Card getEventCard() {
		return (Card) resolveSingleTarget((EntityReference) getEnvironment().get(Environment.EVENT_CARD));
	}

	/**
	 * Gets the current event target stack.
	 *
	 * @return A stack of targets.
	 * @see Environment#EVENT_TARGET_REFERENCE_STACK for more.
	 */
	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getEventTargetStack() {
		if (!getEnvironment().containsKey(Environment.EVENT_TARGET_REFERENCE_STACK)) {
			getEnvironment().put(Environment.EVENT_TARGET_REFERENCE_STACK, new EnvironmentDeque<EntityReference>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.EVENT_TARGET_REFERENCE_STACK);
	}

	/**
	 * Gets the minions to the left on the battlefield of the given minion.
	 *
	 * @param minionReference An {@link EntityReference} pointing to the minion.
	 * @return A list of entities to the left of the provided minion.
	 */
	public List<Actor> getLeftMinions(EntityReference minionReference) {
		List<Actor> leftMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		List<Minion> minions = getPlayer(minion.getOwner()).getMinions();
		int index = minions.indexOf(minion);
		if (index == -1) {
			return TargetLogic.withoutPermanents(leftMinions);
		}
		for (int i = 0; i < index; i++) {
			leftMinions.add(minions.get(i));
		}
		return TargetLogic.withoutPermanents(leftMinions);
	}

	/**
	 * Gets a reference to the game logic associated with this context.
	 *
	 * @return A {@link GameLogic} instance.
	 * @see GameLogic for more.
	 */
	public GameLogic getLogic() {
		return logic;
	}

	/**
	 * Gets the number of minions a player has.
	 *
	 * @param player The player to query.
	 * @return The count of minions.
	 */
	public int getMinionCount(Player player) {
		return player.getMinions().size();
	}

	/**
	 * Gets the opponent from the point of view of the given player.
	 *
	 * @param player The friendly player.
	 * @return The opposing player from the point of view of the {@code player} argument.
	 */
	public Player getOpponent(Player player) {
		return player.getId() == PLAYER_1 ? getPlayer2() : getPlayer1();
	}

	/**
	 * Gets the {@link Actor} entities geometrically opposite of the given {@code minionReference} on the {@link
	 * Zones#BATTLEFIELD}.
	 *
	 * @param minionReference The minion from whose perspective we will consider "opposite."
	 * @return The list of {@link Actor} (typically one or two) that are geometrically opposite from the minion
	 * referenced by {@code minionReference}.
	 */
	public List<Actor> getOppositeMinions(EntityReference minionReference) {
		List<Actor> oppositeMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		Player owner = getPlayer(minion.getOwner());
		Player opposingPlayer = getOpponent(owner);
		int index = minion.getEntityLocation().getIndex();
		if (opposingPlayer.getMinions().size() == 0 || owner.getMinions().size() == 0 || index == -1) {
			return TargetLogic.withoutPermanents(oppositeMinions);
		}
		List<Minion> opposingMinions = opposingPlayer.getMinions();
		int delta = opposingPlayer.getMinions().size() - owner.getMinions().size();
		if (delta % 2 == 0) {
			delta /= 2;
			int epsilon = delta + index;
			if (epsilon > -1 && epsilon < opposingMinions.size()) {
				oppositeMinions.add(opposingMinions.get(epsilon));
			}
		} else {
			delta = (delta - 1) / 2;
			int epsilon = delta + index;
			if (epsilon > -1 && epsilon < opposingMinions.size()) {
				oppositeMinions.add(opposingMinions.get(epsilon));
			}
			if (epsilon + 1 > -1 && epsilon + 1 < opposingMinions.size()) {
				oppositeMinions.add(opposingMinions.get(epsilon + 1));
			}
		}
		return TargetLogic.withoutPermanents(oppositeMinions);
	}

	/**
	 * Gets the card that is currently being played, or {@code null} if no card is currently being played.
	 *
	 * @return A {@link Card} that was the result of a {@link net.demilich.metastone.game.actions.PlayCardAction}.
	 */
	public Card getPendingCard() {
		return (Card) resolveSingleTarget((EntityReference) getEnvironment().get(Environment.PENDING_CARD));
	}

	/**
	 * Gets the player at the given index.
	 *
	 * @param index {@link GameContext#PLAYER_1} or {@link GameContext#PLAYER_2}
	 * @return A reference to the player with that ID / at that {@code index}.
	 */
	public synchronized Player getPlayer(int index) {
		return getPlayers().get(index);
	}

	/**
	 * @param id {@link GameContext#PLAYER_1} or {@link GameContext#PLAYER_2}
	 * @return {@code true} if the game context has a valid {@link Player} object at that index.
	 */
	public synchronized boolean hasPlayer(int id) {
		return id >= 0 && players != null && players.length > id && players[id] != null;
	}

	/**
	 * Gets the first player.
	 *
	 * @return A player object.
	 */
	public Player getPlayer1() {
		return getPlayer(PLAYER_1);
	}

	/**
	 * Gets the second player.
	 *
	 * @return A player object.
	 */
	public Player getPlayer2() {
		return getPlayer(PLAYER_2);
	}

	/**
	 * Each player holds the player's {@link AbstractBehaviour} and all of the {@link Entity} objects in the game.
	 *
	 * @return An {@link java.util.Collections.UnmodifiableList} of {@link Player} objects.
	 */
	public synchronized List<Player> getPlayers() {
		if (players == null) {
			return Collections.unmodifiableList(new ArrayList<>());
		}
		return Collections.unmodifiableList(Arrays.asList(players));
	}

	/**
	 * Gets minions geometrically right of the given {@code minionReference} on the {@link Zones#BATTLEFIELD} that
	 * belongs to the specified player.
	 *
	 * @param player          The player to query.
	 * @param minionReference The minion reference.
	 * @return A list of {@link Actor} (sometimes empty) of minions to the geometric right of the {@code
	 * minionReference}.
	 */
	public List<Actor> getRightMinions(Player player, EntityReference minionReference) {
		List<Actor> rightMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		List<Minion> minions = getPlayer(minion.getOwner()).getMinions();
		int index = minion.getEntityLocation().getIndex();
		if (index == -1) {
			return TargetLogic.withoutPermanents(rightMinions);
		}
		for (int i = index + 1; i < player.getMinions().size(); i++) {
			rightMinions.add(minions.get(i));
		}
		return TargetLogic.withoutPermanents(rightMinions);
	}

	/**
	 * Gets the minions whose summoning is currently being processed.
	 * <p>
	 * This stack can have multiple entries because battlecries or secrets can trigger summoning of other minions in the
	 * middle of evaluating a {@link GameLogic#summon(int, Minion, Card, int, boolean)}.
	 *
	 * @return A stack of summons.
	 */
	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getSummonReferenceStack() {
		if (!getEnvironment().containsKey(Environment.SUMMON_REFERENCE_STACK)) {
			getEnvironment().put(Environment.SUMMON_REFERENCE_STACK, new EnvironmentDeque<EntityReference>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.SUMMON_REFERENCE_STACK);
	}

	/**
	 * Gets the total number of minions on both player's {@link Zones#BATTLEFIELD}.
	 *
	 * @return A total.
	 */
	public int getTotalMinionCount() {
		int totalMinionCount = 0;
		for (Player player : getPlayers()) {
			totalMinionCount += getMinionCount(player);
		}
		return totalMinionCount;
	}

	/**
	 * Gets the {@link Trigger} objects for which {@link Trigger#getHostReference()}  matches the specified {@link
	 * EntityReference}.
	 *
	 * @param entityReference An {@link EntityReference}/
	 * @return A list of triggers, possibly empty.
	 * @see Trigger#getHostReference() for an explanation of what an "associated" trigger would mean.
	 */
	public List<Trigger> getTriggersAssociatedWith(EntityReference entityReference) {
		return getTriggerManager().getTriggersAssociatedWith(entityReference);
	}

	/**
	 * Gets the current turn.
	 *
	 * @return The turn. 0 is the first turn.
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * Gets the current {@link TurnState}
	 *
	 * @return The turn state of this context.
	 */
	public TurnState getTurnState() {
		return turnState;
	}

	@Suspendable
	public List<GameAction> getValidActions() {
		if (updateAndGetGameOver()) {
			return new ArrayList<>();
		}
		return getLogic().getValidActions(getActivePlayerId());
	}

	/**
	 * Gets the winning player's ID or {@code -1} if no player is the winner.
	 *
	 * @return The winning player's ID or {@code -1} if no player is the winner.
	 */
	public int getWinningPlayerId() {
		return getWinner() == null ? -1 : getWinner().getId();
	}

	/**
	 * When true, the {@link TriggerManager} doesn't handle an events being raised.
	 *
	 * @return {@code true} if the game context should ignore incoming events.
	 */
	public boolean ignoreEvents() {
		return ignoreEvents;
	}

	/**
	 * Initializes a game.
	 * <p>
	 * Typically, this determines the beginner with {@link GameLogic#determineBeginner(int...)}; then it sets the active
	 * player; then it calls {@link GameLogic#initializePlayer(int)} for both players, and then it asks both players for
	 * their mulligans using {@link GameLogic#init(int, boolean)}.
	 */
	@Suspendable
	protected void init() {
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		setActivePlayerId(getPlayer(startingPlayerId).getId());
		logger.debug(getActivePlayer().getName() + " begins");
		getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));
		getLogic().initializePlayer(PLAYER_1);
		getLogic().initializePlayer(PLAYER_2);
		getLogic().init(getActivePlayerId(), true);
		getLogic().init(getOpponent(getActivePlayer()).getId(), false);
	}

	protected void onGameStateChanged() {
	}


	/**
	 * Executes the specified game action, typically by calling {@link GameLogic#performGameAction(int, GameAction)}.
	 *
	 * @param playerId   The player who's performing the action.
	 * @param gameAction The action to perform.
	 * @see GameLogic#performGameAction(int, GameAction) for more about game actions.
	 */
	@Suspendable
	protected void performAction(int playerId, GameAction gameAction) {
		getLogic().performGameAction(playerId, gameAction);
		onGameStateChanged();
	}

	/**
	 * Plays the game.
	 * <p>
	 * When a game is played, mulligans are requested from both players, and then each player is asked for actions until
	 * the player can't take any.
	 * <p>
	 * Play relies on the {@link Behaviour} delegates to determine what a player's chosen action is. It takes the chosen
	 * action and feeds it to the {@link GameLogic}, which executes the effects of that action until the next action
	 * needs to be requested.
	 *
	 * @see #takeActionInTurn() for a breakdown of a specific turn.
	 */
	@Suspendable
	public void play() {
		logger.debug("Game starts: " + getPlayer1().getName() + " VS. " + getPlayer2().getName());
		init();
		resume();
	}

	/**
	 * Requests an action from a player and takes it in the turn.
	 * <p>
	 * This method will call {@link Behaviour#requestAction(GameContext, Player, List)} to get an action from the
	 * currently active player. It then calls {@link #performAction(int, GameAction)} with the returned {@link
	 * GameAction}.
	 *
	 * @return {@code false} if the player selected an {@link net.demilich.metastone.game.actions.EndTurnAction},
	 * indicating the player would like to end their turn.
	 */
	@Suspendable
	public boolean takeActionInTurn() {
		setActionsThisTurn(getActionsThisTurn() + 1);
		if (getActionsThisTurn() > 99) {
			logger.warn("Turn has been forcefully ended after {} actions", getActionsThisTurn());
			endTurn();
			return false;
		}
		if (getLogic().hasAutoHeroPower(getActivePlayerId())) {
			performAction(getActivePlayerId(), getAutoHeroPowerAction());
			return true;
		}

		List<GameAction> validActions = getValidActions();
		if (validActions.size() == 0) {
			//endTurn();
			return false;
		}

		GameAction nextAction = getActivePlayer().getBehaviour().requestAction(this, getActivePlayer(), getValidActions());
		while (!acceptAction(nextAction)) {
			nextAction = getActivePlayer().getBehaviour().requestAction(this, getActivePlayer(), getValidActions());
		}
		if (nextAction == null) {
			throw new RuntimeException("Behaviour " + getActivePlayer().getBehaviour().getName() + " selected NULL action while "
					+ getValidActions().size() + " actions were available");
		}
		performAction(getActivePlayerId(), nextAction);

		return nextAction.getActionType() != ActionType.END_TURN;
	}

	/**
	 * Prints to the {@link #logger} the currently active triggers.
	 */
	public void printCurrentTriggers() {
		logger.info("Active enchantments:");
		getTriggerManager().printCurrentTriggers();
	}

	/**
	 * Removes a trigger from the game context.
	 *
	 * @param trigger The trigger to remove.
	 */
	public void removeTrigger(Trigger trigger) {
		getTriggerManager().removeTrigger(trigger);
	}

	/**
	 * Removes all the triggers associated with a particular {@link Entity}, typically because the entity has been
	 * destroyed,
	 *
	 * @param entityReference The entity whose triggers should be removed.
	 * @param removeAuras     {@code true} if the entity has {@link net.demilich.metastone.game.spells.aura.Aura}
	 *                        triggers that should be removed.
	 */
	public void removeTriggersAssociatedWith(EntityReference entityReference, boolean removeAuras) {
		triggerManager.removeTriggersAssociatedWith(entityReference, removeAuras);
	}

	/**
	 * Interprets a card reference and returns the appropriate card.
	 * <p>
	 * This method allows lookups by entity ID or card ID.
	 *
	 * @param cardReference The {@link CardReference} with which to search the entities in this game context.
	 * @return A {@link Card} that was references.
	 * @throws NullPointerException when the card reference could not be resolved. Game rules should generally never
	 *                              search for a card that doesn't exist.
	 */
	@SuppressWarnings("unchecked")
	public Card resolveCardReference(CardReference cardReference) throws NullPointerException {
		Player player = getPlayer(cardReference.getPlayerId());
		Card card = null;
		if (getPendingCard() != null && getPendingCard().getCardReference().equals(cardReference)) {
			card = getPendingCard();
		} else {
			switch (cardReference.getZone()) {
				case SET_ASIDE_ZONE:
				case DISCOVER:
					final Optional<Entity> first = ((EntityZone<Entity>) player.getZone(cardReference.getZone())).stream().filter(e -> e.getId() == cardReference.getEntityId()).findFirst();
					if (first.isPresent()
							&& Card.class.isAssignableFrom(first.get().getClass())) {
						card = (Card) first.get();
					}
					break;
				case DECK:
					card = findCardinCollection(player.getDeck(), cardReference.getEntityId());
					break;
				case HAND:
					card = findCardinCollection(player.getHand(), cardReference.getEntityId());
					break;
				case HERO_POWER:
					card = player.getHero().getHeroPower();
				default:
					break;
			}
		}
		if (card == null) {
			throw new NullPointerException("Could not resolve cardReference " + cardReference.toString());
		} else {
			return card;
		}
	}

	/**
	 * Tries to find the entity references by the {@link EntityReference}.
	 *
	 * @param targetKey The reference to find.
	 * @return The {@link Entity} pointed to by the {@link EntityReference}.
	 * @throws NullPointerException if the reference could not be found. Game rules shouldn't be looking for references
	 *                              that cannot be found.
	 */
	public Entity resolveSingleTarget(EntityReference targetKey) throws NullPointerException {
		if (targetKey == null) {
			return null;
		}

		final Entity entity = targetLogic.findEntity(this, targetKey).transformResolved(this);

		if (entity.getZone() == Zones.REMOVED_FROM_PLAY) {
			throw new RuntimeException("Invalid reference.");
		}

		return entity;
	}

	/**
	 * Interprets {@link EntityReference} that specifies a group of {@link Entity} objects, like {@link
	 * EntityReference#ALL_MINIONS}.
	 *
	 * @param player    The player from whose point of view this method interprets the {@link EntityReference}.
	 * @param source    The entity from whose point of view this method interprets the {@link EntityReference}.
	 * @param targetKey The {@link EntityReference}.
	 * @return A potentially empty list of entities.
	 * @see TargetLogic#resolveTargetKey(GameContext, Player, Entity, EntityReference) for more about how target
	 * resolution works.
	 */
	public List<Entity> resolveTarget(Player player, Entity source, EntityReference targetKey) {
		final List<Entity> entities = targetLogic.resolveTargetKey(this, player, source, targetKey);
		if (entities == null) {
			return null;
		}
		return entities.stream().map(e -> e.transformResolved(this)).collect(Collectors.toList());
	}

	/**
	 * Sets the environment variable {@link Environment#EVENT_CARD}.
	 *
	 * @param eventCard The card to set.
	 */
	public void setEventCard(Card eventCard) {
		if (eventCard != null) {
			getEnvironment().put(Environment.EVENT_CARD, eventCard.getReference());
		} else {
			getEnvironment().put(Environment.EVENT_CARD, null);
		}
	}

	public void setIgnoreEvents(boolean ignoreEvents) {
		this.ignoreEvents = ignoreEvents;
	}

	public void setPendingCard(Card pendingCard) {
		if (pendingCard != null) {
			getEnvironment().put(Environment.PENDING_CARD, pendingCard.getReference());
		} else {
			getEnvironment().put(Environment.PENDING_CARD, null);
		}
	}

	@SuppressWarnings("unchecked")
	public int getEventValue() {
		if (getEnvironment().containsKey(Environment.EVENT_VALUE_STACK)) {
			return ((Deque<Integer>) getEnvironment().get(Environment.EVENT_VALUE_STACK)).peek();
		} else {
			return 0;
		}
	}

	/**
	 * Starts the turn for a player.
	 *
	 * @param playerId The player whose turn should be started.
	 */
	@Suspendable
	public void startTurn(int playerId) {
		setTurn(getTurn() + 1);
		getLogic().startTurn(playerId);
		onGameStateChanged();
		setActionsThisTurn(0);
		setTurnState(TurnState.TURN_IN_PROGRESS);
	}

	@Override
	public String toString() {
		return String.format("[GameContext turn=%d turnState=%s]", getTurn(), getTurnState().toString());
	}

	/**
	 * Tries to find an entity given the reference.
	 *
	 * @param targetKey The reference to the entity.
	 * @return The found {@link Entity}, or {@code null} if no entity was found.
	 */
	public Entity tryFind(EntityReference targetKey) {
		try {
			return resolveSingleTarget(targetKey);
		} catch (Exception e) {
		}
		return null;
	}

	public void setLogic(GameLogic logic) {
		this.logic = logic;
		this.getLogic().setContext(this);
	}

	public void setDeckFormat(DeckFormat deckFormat) {
		this.deckFormat = deckFormat;
	}

	public void setEnvironment(Map<Environment, Object> environment) {
		this.environment = environment;
	}

	public void setCardCostModifiers(List<CardCostModifier> cardCostModifiers) {
		this.cardCostModifiers = cardCostModifiers;
	}

	public Player getWinner() {
		return winner;
	}

	public void setWinner(Player winner) {
		this.winner = winner;
	}

	public GameStatus getStatus() {
		return result;
	}

	public void setResult(GameStatus result) {
		this.result = result;
	}

	public void setTurnState(TurnState turnState) {
		this.turnState = turnState;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public int getActionsThisTurn() {
		return actionsThisTurn;
	}

	public void setActionsThisTurn(int actionsThisTurn) {
		this.actionsThisTurn = actionsThisTurn;
	}

	public String toLongString() {
		StringBuilder builder = new StringBuilder("GameContext hashCode: " + hashCode() + "\n");

		for (Player player : getPlayers()) {
			if (player == null) {
				builder.append("(null player)\n");
				continue;
			}
			builder.append("Player " + player.getName() + "\n");
			builder.append(" Mana: ");
			builder.append(player.getMana());
			builder.append('/');
			builder.append(player.getMaxMana());
			builder.append(" HP: ");
			builder.append(player.getHero().getHp() + "(" + player.getHero().getArmor() + ")");
			builder.append('\n');
			builder.append("Behaviour: " + player.getBehaviour().getName() + "\n");
			builder.append("Minions:\n");
			for (Actor minion : player.getMinions()) {
				builder.append('\t');
				builder.append(minion);
				builder.append('\n');
			}
			builder.append("Cards (hand):\n");
			for (Card card : player.getHand()) {
				builder.append('\t');
				builder.append(card);
				builder.append('\n');
			}
			builder.append("Set aside:\n");
			for (Entity entity : player.getSetAsideZone()) {
				builder.append('\t');
				builder.append(entity);
				builder.append('\n');
			}
			builder.append("Graveyard:\n");
			for (Entity entity : player.getGraveyard()) {
				builder.append('\t');
				builder.append(entity);
				builder.append('\n');
			}
			builder.append("Secrets:\n");
			for (String secretId : player.getSecretCardIds()) {
				builder.append('\t');
				builder.append(secretId);
				builder.append('\n');
			}
		}
		builder.append("Turn: " + getTurn() + "\n");
		builder.append("Result: " + getStatus() + "\n");
		builder.append("Winner: " + (getWinner() == null ? "tbd" : getWinner().getName()));

		return builder.toString();
	}

	public void setPlayer1(Player player1) {
		setPlayer(PLAYER_1, player1);
	}

	public void setPlayer2(Player player2) {
		setPlayer(PLAYER_2, player2);
	}

	public void setGameState(GameState state) {
		this.setPlayer(GameContext.PLAYER_1, state.player1);
		this.setPlayer(GameContext.PLAYER_2, state.player2);
		this.setTempCards(state.tempCards);
		this.setCardCostModifiers(state.cardCostModifiers);
		this.setEnvironment(state.environment);
		this.setTriggerManager(state.triggerManager);
		if (getLogic() == null) {
			setLogic(new GameLogic());
		}
		if (getDeckFormat() == null) {
			setDeckFormat(new DeckFormat().withCardSets(CardSet.values()));
		}
		this.getLogic().setIdFactory(new IdFactory(state.currentId));
		this.getLogic().setContext(this);
		this.setTurnState(state.turnState);
		this.setTurn(state.turnNumber);
		this.setActivePlayerId(state.activePlayerId);
	}

	public void setPlayer(int index, Player player) {
		this.players[index] = player;
	}

	public void setActivePlayerId(int id) {
		activePlayerId = id;
	}

	public TargetLogic getTargetLogic() {
		return targetLogic;
	}

	public void setTargetLogic(TargetLogic targetLogic) {
		this.targetLogic = targetLogic;
	}

	public TriggerManager getTriggerManager() {
		return triggerManager;
	}

	public void setTriggerManager(TriggerManager triggerManager) {
		this.triggerManager = triggerManager;
	}

	public CardList getTempCards() {
		return tempCards;
	}

	public void setTempCards(CardList tempCards) {
		this.tempCards = tempCards;
	}

	public boolean isDisposed() {
		return disposed;
	}

	public String getGameId() {
		return "local";
	}

	/**
	 * Gets all the entities in the game, aside from hidden ones, as a {@link Stream}.
	 *
	 * @return The {@link Stream} of game entities.
	 */
	@SuppressWarnings("unchecked")
	public Stream<Entity> getEntities() {
		return getPlayers().stream()
				.flatMap(p -> Stream.of(Zones.values())
						.flatMap(z -> ((EntityZone<Entity>) p.getZone(z)).stream()));
	}

	public void onWillPerformGameAction(int playerId, GameAction action) {
	}

	public void onDidPerformGameAction(int playerId, GameAction action) {
	}

	public GameState getGameState() {
		return new GameState(this, this.getTurnState(), true);
	}

	public GameState getGameStateCopy() {
		return new GameState(this);
	}

	/**
	 * Concedes a game by destroying the specified player's hero and calling end game.
	 *
	 * @param playerId The player that should concede/lose
	 */
	public void concede(int playerId) {
		getLogic().concede(playerId);
		endGame();
	}

	/**
	 * Raised when a {@link Enchantment} is fired (i.e., a secret is about to be played or a special effect hosted by a
	 * minion/weapon is about to happen).
	 *
	 * @param enchantment The spell trigger that fired.
	 */
	public void onEnchantmentFired(Enchantment enchantment) {
	}

	/**
	 * Returns the spell values calculated so far by descending {@link net.demilich.metastone.game.spells.MetaSpell#onCast(GameContext,
	 * Player, SpellDesc, Entity, Entity)} calls.
	 * <p>
	 * Implements Living Mana. Using a stack fixes issues where a later {@link net.demilich.metastone.game.spells.MetaSpell}
	 * busts an earlier one.
	 *
	 * @return A stack of {@link Integer} spell values.
	 */
	@SuppressWarnings("unchecked")
	public Deque<Integer> getSpellValueStack() {
		if (!getEnvironment().containsKey(Environment.SPELL_VALUE_STACK)) {
			getEnvironment().put(Environment.SPELL_VALUE_STACK, new EnvironmentDeque<Integer>());
		}
		return (Deque<Integer>) getEnvironment().get(Environment.SPELL_VALUE_STACK);
	}

	@SuppressWarnings("unchecked")
	public Deque<Integer> getEventValueStack() {
		if (!getEnvironment().containsKey(Environment.EVENT_VALUE_STACK)) {
			getEnvironment().put(Environment.EVENT_VALUE_STACK, new EnvironmentDeque<>());
		}
		return (Deque<Integer>) getEnvironment().get(Environment.EVENT_VALUE_STACK);
	}

	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getSpellTargetStack() {
		if (!getEnvironment().containsKey(Environment.SPELL_TARGET)) {
			getEnvironment().put(Environment.SPELL_TARGET, new EnvironmentDeque<>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.SPELL_TARGET);
	}

	/**
	 * Resumes a game, playing it to completion.
	 * <p>
	 * Useful for implementing Monte Carlo Tree Search AI algorithms.
	 */
	@Suspendable
	public void resume() {
		while (!updateAndGetGameOver()) {
			startTurn(getActivePlayerId());
			while (takeActionInTurn()) {
			}
			if (getTurn() > GameLogic.TURN_LIMIT) {
				break;
			}
		}
		endGame();
	}
}
