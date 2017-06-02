package net.demilich.metastone.game;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.common.GameState;
import io.vertx.core.Handler;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.MatchResult;
import net.demilich.metastone.game.logic.TargetLogic;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.utils.IDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * A game context helps execute a match of Minionate, providing a place to store state, deliver requests for actions to
 * players, apply those player actions through a {@link GameLogic}, and then save the updated state as a result of those
 * actions.
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
 * // Note "PlayRandomBehaviour"—
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
 * {@link net.demilich.metastone.game.behaviour.IBehaviour} delegate for player actions and mulligans. </li>. <li>A
 * {@link GameLogic} instance. It handles everything in between receiving a player action to the request for the next
 * player action..</li> <li>A {@link DeckFormat}, which is a collection of {@link CardSet} values that correspond to the
 * (1) legal cards that may be played and put into decks and (2) legal cards that may appear in randomly drawn or
 * created card effects.</li></ul><p>
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
 * @see net.demilich.metastone.game.behaviour.IBehaviour for the interface that the {@link GameContext} delegates player
 * actions and notifications to. This is both the "event handler" specification for which events a player may be
 * interested in; and also a "delegate" in the sense that the object implementing this interface makes decisions about
 * what actions in the game to take (with e.g. {@link net.demilich.metastone.game.behaviour.IBehaviour#requestAction(GameContext,
 * Player, List)}.
 * @see net.demilich.metastone.game.behaviour.PlayRandomBehaviour for an example behaviour that just makes random
 * decisions when requested.
 * @see com.hiddenswitch.proto3.net.common.NetworkBehaviour for the class that turns requests to the {@link
 * net.demilich.metastone.game.behaviour.IBehaviour} into calls over the network.
 * @see GameLogic for the class that actually implements the Minionate game rules. This class requires a {@link
 * GameContext} because it manipulates the state stored in it.
 * @see GameState for a class that encapsulates all of the state of a game of Minionate.
 * @see #getGameState() to access and modify the game state.
 * @see #getGameStateCopy() to get a copy of the state that can be stored and diffed.
 * @see #getEntities() for a way to enumerate through all of the entities in the game.
 */
public class GameContext implements Cloneable, IDisposable, Serializable {
	public static final int PLAYER_1 = 0;
	public static final int PLAYER_2 = 1;

	protected static final Logger logger = LoggerFactory.getLogger(GameContext.class);

	private Player[] players = new Player[2];
	private GameLogic logic;
	private DeckFormat deckFormat;
	private TargetLogic targetLogic = new TargetLogic();
	private TriggerManager triggerManager = new TriggerManager();
	private HashMap<Environment, Object> environment = new HashMap<>();
	private List<CardCostModifier> cardCostModifiers = new ArrayList<>();
	private int activePlayerId = -1;
	private Player winner;
	private MatchResult result;
	private TurnState turnState = TurnState.TURN_ENDED;
	private boolean disposed = false;
	private int turn;
	private int actionsThisTurn;
	private boolean ignoreEvents;
	private CardList tempCards = new CardArrayList();
	private Stack<GameAction> actionStack = new Stack<>();
	private Stack<GameEvent> eventStack = new Stack<>();

	/**
	 * Creates a game context with no valid start state.
	 */
	public GameContext() {
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
		getTempCards().removeAll();
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
	 * @param trigger An {@link IGameEventListener} that is used as a delegate whenever an event is fired in the game.
	 * @see #fireGameEvent(GameEvent, List) for more about firing game events.
	 */
	public void addTrigger(IGameEventListener trigger) {
		getTriggerManager().addTrigger(trigger);
	}

	/**
	 * Clones the game context, recursively cloning the game state and logic.
	 * <p>
	 * Internally, this is used by AI functions to evaluate a game state until a win condition (or just the end of the
	 * turn) is reached.
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
		clone.setResult(getResult());
		clone.setTurnState(getTurnState());
		clone.setWinner(logicClone.getWinner(player1Clone, player2Clone));
		clone.getCardCostModifiers().clear();
		for (CardCostModifier cardCostModifier : getCardCostModifiers()) {
			clone.getCardCostModifiers().add(cardCostModifier.clone());
		}
		Stack<Integer> damageStack = new Stack<Integer>();
		damageStack.addAll(getDamageStack());
		clone.getEnvironment().put(Environment.DAMAGE_STACK, damageStack);
		Stack<EntityReference> summonReferenceStack = new Stack<EntityReference>();
		summonReferenceStack.addAll(getSummonReferenceStack());
		clone.getEnvironment().put(Environment.SUMMON_REFERENCE_STACK, summonReferenceStack);
		Stack<EntityReference> eventTargetReferenceStack = new Stack<EntityReference>();
		eventTargetReferenceStack.addAll(getEventTargetStack());
		clone.getEnvironment().put(Environment.EVENT_TARGET_REFERENCE_STACK, eventTargetReferenceStack);

		for (Environment key : getEnvironment().keySet()) {
			if (!key.customClone()) {
				clone.getEnvironment().put(key, getEnvironment().get(key));
			}
		}
		clone.actionStack.addAll(actionStack);
		clone.getEventStack().addAll(getEventStack());
		return clone;
	}

	/**
	 * Clears state to ensure this context isn't referencing it anymore.
	 */
	@Override
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

	/**
	 * Makes a request over the network for a game action. Unsupported in this game context.
	 *
	 * @param state    The game state to send.
	 * @param playerId The player ID to request from.
	 * @param actions  The valid actions to choose from.
	 * @param callback A handler for the response.
	 */
	@Suspendable
	public void networkRequestAction(GameState state, int playerId, List<GameAction> actions, Handler<GameAction> callback) {
		throw new UnsupportedOperationException();
	}

	/**
	 * If possible, makes a request over the network for which cards to mulligan. Unsupported in this game context.
	 *
	 * @param player       The player to request from.
	 * @param starterCards The cards the player started with.
	 * @param callback     A handler for the response.
	 */
	public void networkRequestMulligan(Player player, List<Card> starterCards, Handler<List<Card>> callback) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Notifies the recipient player that the {@code winner} player won the game.
	 *
	 * @param recipient The player to notify.
	 * @param winner    The winner.
	 */
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
		setActivePlayerId(getActivePlayerId() == PLAYER_1 ? PLAYER_2 : PLAYER_1);
		onGameStateChanged();
		setTurnState(TurnState.TURN_ENDED);
	}

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
	 * @see #addTrigger(IGameEventListener) for the place to add triggers that react to game events.
	 */
	@Suspendable
	public void fireGameEvent(GameEvent gameEvent, List<IGameEventListener> otherTriggers) {
		if (ignoreEvents()) {
			return;
		}
		try {
			getTriggerManager().fireGameEvent(gameEvent, otherTriggers);
		} catch (Exception e) {
			logger.error("Error while processing gameEvent {}", gameEvent);
			getLogic().logDebugHistory();
			throw e;
		}
	}

	/**
	 * Determines whether the game is over (decided).
	 *
	 * @return {@code true} if the game has been decided by concession or because one of the two heroes have been
	 * destroyed.
	 */
	public boolean gameDecided() {
		setResult(getLogic().getMatchResult(getActivePlayer(), getOpponent(getActivePlayer())));
		setWinner(getLogic().getWinner(getActivePlayer(), getOpponent(getActivePlayer())));
		return getResult() != MatchResult.RUNNING;
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
	 * Gets the minions adjacent to the given minion.
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
			return adjacentMinions;
		}
		int left = index - 1;
		int right = index + 1;
		if (left > -1 && left < minions.size()) {
			adjacentMinions.add(minions.get(left));
		}
		if (right > -1 && right < minions.size()) {
			adjacentMinions.add(minions.get(right));
		}
		return adjacentMinions;
	}

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

	@SuppressWarnings("unchecked")
	public Stack<Integer> getDamageStack() {
		if (!getEnvironment().containsKey(Environment.DAMAGE_STACK)) {
			getEnvironment().put(Environment.DAMAGE_STACK, new Stack<Integer>());
		}
		return (Stack<Integer>) getEnvironment().get(Environment.DAMAGE_STACK);
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
	 * Gets a reference to the game context's environment, a piece of game state that keeps tracks of which minions
	 * are currently being summoned, which targets are being targeted, how much damage is set to be dealt, etc.
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
	 * @return The event card.
	 * @see Environment#EVENT_CARD for more.
	 */
	public Card getEventCard() {
		return (Card) resolveSingleTarget((EntityReference) getEnvironment().get(Environment.EVENT_CARD));
	}

	/**
	 * Gets the current event target stack.
	 * @return A stack of targets.
	 * @see Environment#EVENT_TARGET_REFERENCE_STACK for more.
	 */
	@SuppressWarnings("unchecked")
	public Stack<EntityReference> getEventTargetStack() {
		if (!getEnvironment().containsKey(Environment.EVENT_TARGET_REFERENCE_STACK)) {
			getEnvironment().put(Environment.EVENT_TARGET_REFERENCE_STACK, new Stack<EntityReference>());
		}
		return (Stack<EntityReference>) getEnvironment().get(Environment.EVENT_TARGET_REFERENCE_STACK);
	}

	/**
	 * Gets the minions to the left on the battlefield of the given minion.
	 * @param minionReference An {@link EntityReference} pointing to the minion.
	 * @return A list of entities to the left of the provided minion.
	 */
	public List<Actor> getLeftMinions(EntityReference minionReference) {
		List<Actor> leftMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		List<Minion> minions = getPlayer(minion.getOwner()).getMinions();
		int index = minions.indexOf(minion);
		if (index == -1) {
			return leftMinions;
		}
		for (int i = 0; i < index; i++) {
			leftMinions.add(minions.get(i));
		}
		return leftMinions;
	}

	public GameLogic getLogic() {
		return logic;
	}

	public int getMinionCount(Player player) {
		return player.getMinions().size();
	}

	public Player getOpponent(Player player) {
		return player.getId() == PLAYER_1 ? getPlayer2() : getPlayer1();
	}

	public List<Actor> getOppositeMinions(Player player, EntityReference minionReference) {
		List<Actor> oppositeMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		Player owner = getPlayer(minion.getOwner());
		Player opposingPlayer = getOpponent(owner);
		int index = minion.getEntityLocation().getIndex();
		if (opposingPlayer.getMinions().size() == 0 || owner.getMinions().size() == 0 || index == -1) {
			return oppositeMinions;
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
		return oppositeMinions;
	}

	public Card getPendingCard() {
		return (Card) resolveSingleTarget((EntityReference) getEnvironment().get(Environment.PENDING_CARD));
	}

	public synchronized Player getPlayer(int index) {
		return getPlayers().get(index);
	}

	public synchronized boolean hasPlayer(int id) {
		return id >= 0 && players != null && players.length > id && players[id] != null;
	}

	public Player getPlayer1() {
		return getPlayer(PLAYER_1);
	}

	public Player getPlayer2() {
		return getPlayer(PLAYER_2);
	}

	/**
	 * Each player holds the player's {@link Behaviour} and all of the {@link
	 * Entity} objects in the game.
	 *
	 * @return An {@link java.util.Collections.UnmodifiableList} of {@link Player} objects.
	 */
	public synchronized List<Player> getPlayers() {
		if (players == null) {
			return Collections.unmodifiableList(new ArrayList<>());
		}
		return Collections.unmodifiableList(Arrays.asList(players));
	}

	public List<Actor> getRightMinions(Player player, EntityReference minionReference) {
		List<Actor> rightMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		List<Minion> minions = getPlayer(minion.getOwner()).getMinions();
		int index = minion.getEntityLocation().getIndex();
		if (index == -1) {
			return rightMinions;
		}
		for (int i = index + 1; i < player.getMinions().size(); i++) {
			rightMinions.add(minions.get(i));
		}
		return rightMinions;
	}

	@SuppressWarnings("unchecked")
	public Stack<EntityReference> getSummonReferenceStack() {
		if (!getEnvironment().containsKey(Environment.SUMMON_REFERENCE_STACK)) {
			getEnvironment().put(Environment.SUMMON_REFERENCE_STACK, new Stack<EntityReference>());
		}
		return (Stack<EntityReference>) getEnvironment().get(Environment.SUMMON_REFERENCE_STACK);
	}

	public int getTotalMinionCount() {
		int totalMinionCount = 0;
		for (Player player : getPlayers()) {
			totalMinionCount += getMinionCount(player);
		}
		return totalMinionCount;
	}

	public List<IGameEventListener> getTriggersAssociatedWith(EntityReference entityReference) {
		return getTriggerManager().getTriggersAssociatedWith(entityReference);
	}

	public int getTurn() {
		return turn;
	}

	public TurnState getTurnState() {
		return turnState;
	}

	@Suspendable
	public List<GameAction> getValidActions() {
		if (gameDecided()) {
			return new ArrayList<>();
		}
		return getLogic().getValidActions(getActivePlayerId());
	}

	public int getWinningPlayerId() {
		return getWinner() == null ? -1 : getWinner().getId();
	}

	public boolean ignoreEvents() {
		return ignoreEvents;
	}

	@Suspendable
	public void init() {
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		setActivePlayerId(getPlayer(startingPlayerId).getId());
		logger.debug(getActivePlayer().getName() + " begins");
		getLogic().initializePlayer(PLAYER_1);
		getLogic().initializePlayer(PLAYER_2);
		getLogic().init(getActivePlayerId(), true);
		getLogic().init(getOpponent(getActivePlayer()).getId(), false);
	}

	protected void onGameStateChanged() {
	}

	@Suspendable
	protected void performAction(int playerId, GameAction gameAction) {
		getLogic().performGameAction(playerId, gameAction);
		onGameStateChanged();
	}

	@Suspendable
	public void play() {
		logger.debug("Game starts: " + getPlayer1().getName() + " VS. " + getPlayer2().getName());
		init();
		while (!gameDecided()) {
			startTurn(getActivePlayerId());
			while (playTurn()) {
			}
			if (getTurn() > GameLogic.TURN_LIMIT) {
				break;
			}
		}
		endGame();
	}

	@Suspendable
	public boolean playTurn() {
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

	public void printCurrentTriggers() {
		logger.info("Active spelltriggers:");
		getTriggerManager().printCurrentTriggers();
	}

	public void removeTrigger(IGameEventListener trigger) {
		getTriggerManager().removeTrigger(trigger);
	}

	public void removeTriggersAssociatedWith(EntityReference entityReference, boolean removeAuras) {
		triggerManager.removeTriggersAssociatedWith(entityReference, removeAuras);
	}

	@SuppressWarnings("unchecked")
	public Card resolveCardReference(CardReference cardReference) {
		Player player = getPlayer(cardReference.getPlayerId());
		Card card = null;
		if (getPendingCard() != null && getPendingCard().getCardReference().equals(cardReference)) {
			card = getPendingCard();
		} else {
			switch (cardReference.getZone()) {
				case SET_ASIDE_ZONE:
				case DISCOVER:
					final Optional<Entity> first = ((EntityZone<Entity>) player.getZone(cardReference.getZone())).stream().filter(e -> e.getId() == cardReference.getCardId()).findFirst();
					if (first.isPresent()
							&& Card.class.isAssignableFrom(first.get().getClass())) {
						card = (Card) first.get();
					}
					break;
				case DECK:
					card = findCardinCollection(player.getDeck(), cardReference.getCardId());
					break;
				case HAND:
					card = findCardinCollection(player.getHand(), cardReference.getCardId());
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

	public Entity resolveSingleTarget(EntityReference targetKey) {
		if (targetKey == null) {
			return null;
		}
		return targetLogic.findEntity(this, targetKey);
	}

	public List<Entity> resolveTarget(Player player, Entity source, EntityReference targetKey) {
		return targetLogic.resolveTargetKey(this, player, source, targetKey);
	}

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

	@Suspendable
	protected void startTurn(int playerId) {
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

	public void setEnvironment(HashMap<Environment, Object> environment) {
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

	public MatchResult getResult() {
		return result;
	}

	public void setResult(MatchResult result) {
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
		if (getCurrentEvent() != null) {
			builder.append("\nCurrent event:\n");
			builder.append('\t');
			builder.append(getCurrentEvent());
			builder.append('\n');
		}
		if (getCurrentAction() != null) {
			builder.append("\nCurrent action:\n");
			builder.append('\t');
			builder.append(getCurrentAction());
			builder.append('\n');
		}
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
		builder.append("Result: " + getResult() + "\n");
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
		this.getActionStack().addAll(state.actionStack);
		this.getEventStack().addAll(state.eventStack);
		this.setTurnState(state.turnState);
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

	@SuppressWarnings("unchecked")
	public Stream<Entity> getEntities() {
		return getPlayers().stream().flatMap(p -> Stream.of(new Zones[]{
				Zones.PLAYER,
				Zones.BATTLEFIELD,
				Zones.DECK,
				Zones.GRAVEYARD,
				Zones.HAND,
				Zones.HERO,
				Zones.HERO_POWER,
				Zones.SET_ASIDE_ZONE,
				Zones.WEAPON,
				Zones.SECRET,
				Zones.REMOVED_FROM_PLAY
		}).flatMap(z -> ((EntityZone<Entity>) p.getZone(z)).stream()));
	}

	public void onWillPerformGameAction(int playerId, GameAction action) {
	}

	public void onDidPerformGameAction(int playerId, GameAction action) {
	}

	public Stack<GameAction> getActionStack() {
		return actionStack;
	}

	public Stack<GameEvent> getEventStack() {
		return eventStack;
	}

	public GameEvent getCurrentEvent() {
		if (eventStack.isEmpty()) {
			return null;
		}

		return eventStack.get(eventStack.size());
	}

	public GameAction getCurrentAction() {
		if (actionStack.isEmpty()) {
			return null;
		}

		return actionStack.peek();
	}

	public GameState getGameState() {
		return new GameState(this, this.getTurnState(), true);
	}

	public GameState getGameStateCopy() {
		return new GameState(this);
	}
}
