package net.demilich.metastone.game;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.NetworkBehaviour;
import io.vertx.core.Handler;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.RandomDeck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.EntityZoneTable;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.environment.EnvironmentDeque;
import net.demilich.metastone.game.environment.EnvironmentMap;
import net.demilich.metastone.game.environment.EnvironmentValue;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.logic.TargetLogic;
import net.demilich.metastone.game.logic.Trace;
import net.demilich.metastone.game.services.Inventory;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.TriggerManager;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.IdFactoryImpl;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.NetworkDelegate;
import net.demilich.metastone.game.utils.TurnState;
import org.apache.commons.math3.util.Combinations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
 * GameContext#getBehaviours()} to get the behaviour, and then call {@link Behaviour#requestAction(GameContext, Player,
 * List)} to request which action of a list of actions the player takes. Note that this is just called as a plain
 * function, so the end user of the {@link GameContext} is responsible for the blocking that would occur if e.g. the
 * {@link Behaviour} waits on user input to answer the action request. Ordinarily, as long as the thread running {@link
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
public class GameContext implements Cloneable, Serializable, NetworkDelegate, Inventory, EntityZoneTable {
	public static final int PLAYER_1 = 0;
	public static final int PLAYER_2 = 1;
	protected static Logger logger = LoggerFactory.getLogger(GameContext.class);
	private Player[] players = new Player[2];
	private Behaviour[] behaviours = new Behaviour[2];
	private GameLogic logic;
	private DeckFormat deckFormat;
	private TargetLogic targetLogic = new TargetLogic();
	private TriggerManager triggerManager = new TriggerManager();
	private Map<Environment, Object> environment = new HashMap<>();
	private int activePlayerId = -1;
	private Player winner;
	private GameStatus result;
	private TurnState turnState = TurnState.TURN_ENDED;
	private boolean disposed = false;
	// Note: startTurn is called at least once, so the reported turn will always start counting at 1
	private int turn;
	private int actionsThisTurn;
	private boolean ignoreEvents;
	private CardList tempCards = new CardArrayList();
	private transient Trace trace = new Trace();
	private boolean gameEnded;

	/**
	 * Creates a game context with two empty players.
	 */
	public GameContext() {
		setLogic(new GameLogic());
		behaviours = new Behaviour[]{new PlayRandomBehaviour(), new PlayRandomBehaviour()};
		setDeckFormat(DeckFormat.STANDARD);
		setPlayer1(Player.empty());
		setPlayer2(Player.empty());
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
		player1.setHero(HeroClass.getHeroCard(playerHero1).createHero());
		player2.setHero(HeroClass.getHeroCard(playerHero2).createHero());
		return new GameContext(player1, player2, new GameLogic(), new DeckFormat().withCardSets(CardSet.values()));
	}

	/**
	 * Returns an uninitialized game context supporting all formats. Entity IDs have not been assigned to the cards;
	 * mulligans haven't completed; the hero classes are neutral.
	 *
	 * @return An uninitialized game context that is nonetheless valid for performing many operations.
	 */
	public static GameContext uninitialized() {
		return uninitialized(HeroClass.ANY, HeroClass.ANY);
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
		this();
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

	/**
	 * Construct a game with the specified hero class and cards for both players.
	 *
	 * @param heroClass1
	 * @param cardIds1
	 * @param heroClass2
	 * @param cardIds2
	 */
	public GameContext(HeroClass heroClass1, List<String> cardIds1, HeroClass heroClass2, List<String> cardIds2) {
		this();
		HeroClass[] heroClasses = new HeroClass[]{heroClass1, heroClass2};
		List[] cardIds = new List[]{cardIds1, cardIds2};
		List<Deck> decks = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			Deck deck = new Deck(heroClasses[i]);
			for (Object s : cardIds[i]) {
				Card c = CardCatalogue.getCardById(s.toString());
				deck.getCards().add(c);
			}
			Player player = new Player(deck, "Player " + Integer.toString(i));
			player.setId(i);
			setPlayer(i, player);
			behaviours[i] = new PlayRandomBehaviour();
			decks.add(deck);
		}
		DeckFormat deckFormat = DeckFormat.getSmallestSupersetFormat(decks);
		setLogic(new GameLogic());
		setDeckFormat(deckFormat);
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
	 * This method is not thread safe. Two threads can't clone and mutate a context at the same time.
	 *
	 * @return A cloned instance of the game context.
	 */
	@Override
	public GameContext clone() {
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

		clone.behaviours = new Behaviour[]{behaviours[0] == null ? null : behaviours[0].clone(), behaviours[1] == null ? null : behaviours[1].clone()};
		return clone;
	}

	/**
	 * Clears state to ensure this context isn't referencing it anymore.
	 */
	public void dispose() {
		this.disposed = true;
		this.players = null;
		getTriggerManager().dispose();
		getEnvironment().clear();
	}

	/**
	 * Ends the game immediately.
	 */
	@Suspendable
	protected void endGame() {
		// Don't do processing of end game effects more than once.
		if (gameEnded) {
			notifyPlayersGameOver();
			return;
		}

		gameEnded = true;
		getTriggerManager().expireAll();
		logger.debug("endGame {}: Game is now ending", getGameId());
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
		for (int i = 0; i < behaviours.length; i++) {
			behaviours[i].onGameOver(this, i, getWinner() != null ? getWinner().getId() : -1);
		}
	}

	protected void calculateStatistics() {
		if (getWinner() != null) {
			logger.debug("calculateStatistics {}: Game finished after {}, turns, the winner is {}", getGameId(), getTurn(), getWinner().getName());
			getWinner().getStatistics().gameWon();
			Player loser = getOpponent(getWinner());
			loser.getStatistics().gameLost();
		} else {
			logger.debug("calculateStatistics {}: Game finished after {} turns in a draw", getGameId(), getTurn());
			getPlayer1().getStatistics().gameLost();
			getPlayer2().getStatistics().gameLost();
		}
	}

	/**
	 * Ends the current player's turn immediately, setting the active player to their opponent.
	 */
	@Suspendable
	public void endTurn() {
		logger.debug("{} endTurn: Ending turn {}", getGameId(), getActivePlayer().getId());
		getLogic().endTurn(getActivePlayerId());
		setActivePlayerId(getLogic().getNextActivePlayerId());
		setTurnState(TurnState.TURN_ENDED);
		onGameStateChanged();
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
	 *
	 * <ol><li>They implement trigger-based gameplay like card text that reads, "Whenever a minion is healed, draw a
	 * card." </li><li>They are changes in game state that are notable for a player to see. They can be interpreted as
	 * checkpoints that need to be rendered to the client</li><li></ol>
	 * <p>
	 * Typically a {@link GameEvent} is instantiated inside a function in {@link GameLogic}, like {@link
	 * net.demilich.metastone.game.events.SummonEvent} inside {@link GameLogic#summon(int, Minion, Card, int, boolean)},
	 * and then fired by the {@link GameLogic} using this function.
	 *
	 * @param gameEvent     The {@link GameEvent} to fire.
	 * @param otherTriggers Other triggers to consider besides the ones inside this game context's {@link TriggerManager}.
	 *                      This may be synthetic triggers that implement analytics, networked game logic, newsfeed
	 *                      reports, spectating features, etc.
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
	@Suspendable
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
	 * @param targetReference The minion whose adjacent minions we should get.
	 * @return The adjacent minions.
	 */
	public List<Actor> getAdjacentMinions(EntityReference targetReference) {
		List<Actor> adjacentMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(targetReference);
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
		for (Card tempCard : getTempCards()) {
			if (tempCard.getCardId().equalsIgnoreCase(cardId)) {
				return tempCard.clone();
			}
		}
		return CardCatalogue.getCardById(cardId);
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
	 * Gets the current output card.
	 *
	 * @return The event card.
	 * @see Environment#OUTPUTS for more.
	 */
	public Card getOutputCard() {
		return (Card) resolveSingleTarget(getOutputStack().peek()).getSourceCard();
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
	 * @return The list of {@link Actor} (typically one or two) that are geometrically opposite from the minion referenced
	 * by {@code minionReference}.
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
	 * Gets the player at the given index.
	 *
	 * @param index {@link GameContext#PLAYER_1} or {@link GameContext#PLAYER_2}
	 * @return A reference to the player with that ID / at that {@code index}.
	 */
	public Player getPlayer(int index) {
		return getPlayers().get(index);
	}

	/**
	 * @param id {@link GameContext#PLAYER_1} or {@link GameContext#PLAYER_2}
	 * @return {@code true} if the game context has a valid {@link Player} object at that index.
	 */
	public boolean hasPlayer(int id) {
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
	public List<Player> getPlayers() {
		if (players == null) {
			return Collections.unmodifiableList(new ArrayList<>());
		}
		return Collections.unmodifiableList(Arrays.asList(players));
	}

	public List<Behaviour> getBehaviours() {
		return Collections.unmodifiableList(Arrays.asList(behaviours));
	}

	/**
	 * Gets minions geometrically right of the given {@code minionReference} on the {@link Zones#BATTLEFIELD} that belongs
	 * to the specified player.
	 *
	 * @param player          The player to query.
	 * @param minionReference The minion reference.
	 * @return A list of {@link Actor} (sometimes empty) of minions to the geometric right of the {@code minionReference}.
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
	public void init() {
		getLogic().contextReady();
		startTrace();
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		setActivePlayerId(getPlayer(startingPlayerId).getId());
		logger.debug("{} init: Initializing game with starting player {}", getGameId(), getActivePlayer().getUserId());
		getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));
		getLogic().initializePlayer(PLAYER_1);
		getLogic().initializePlayer(PLAYER_2);
		List<Card> mulligans1 = getLogic().init(getActivePlayerId(), true);
		List<Card> mulligans2 = getLogic().init(getOpponent(getActivePlayer()).getId(), false);
		int[][] tracedMulligans = new int[2][];
		tracedMulligans[getActivePlayerId()] = mulligans1.stream().mapToInt(Card::getId).toArray();
		tracedMulligans[getOpponent(getActivePlayer()).getId()] = mulligans2.stream().mapToInt(Card::getId).toArray();
		trace.setMulligans(tracedMulligans);
	}

	private void startTrace() {
		trace.setStartState(getGameStateCopy());
		trace.setSeed(getLogic().getSeed());
		trace.setCatalogueVersion(CardCatalogue.getVersion());
	}

	@Suspendable
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
	 * action and feeds it to the {@link GameLogic}, which executes the effects of that action until the next action needs
	 * to be requested.
	 *
	 * @see #takeActionInTurn() for a breakdown of a specific turn.
	 */
	@Suspendable
	public void play() {
		logger.debug("play {}: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
		if (Fiber.isCurrentFiber()
				&& Arrays.stream(behaviours).anyMatch(FiberBehaviour.class::isInstance)) {
			new Fiber<Void>(() -> {
				init();
				resume();
			}).start();
		} else {
			init();
			resume();
		}
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
			logger.warn("{} takeActionInTurn: Turn has been forcefully ended after {} actions", getGameId(), getActionsThisTurn());
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

		GameAction nextAction = behaviours[getActivePlayerId()].requestAction(this, getActivePlayer(), getValidActions());

		if (nextAction == null) {
			throw new NullPointerException("nextAction");
		}

		trace.addAction(nextAction.getId(), nextAction);
		performAction(getActivePlayerId(), nextAction);

		return nextAction.getActionType() != ActionType.END_TURN;
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
	 * @param removeAuras     {@code true} if the entity has {@link net.demilich.metastone.game.spells.aura.Aura} triggers
	 *                        that should be removed.
	 */
	public void removeTriggersAssociatedWith(EntityReference entityReference, boolean removeAuras) {
		triggerManager.removeTriggersAssociatedWith(entityReference, removeAuras);
	}

	/**
	 * Tries to find the entity references by the {@link EntityReference}.
	 *
	 * @param targetKey The reference to find.
	 * @return The {@link Entity} pointed to by the {@link EntityReference}, or {@code null} if the provided entity
	 * reference was {@code null} or {@link EntityReference#NONE}
	 * @throws NullPointerException if the reference could not be found. Game rules shouldn't be looking for references
	 *                              that cannot be found.
	 */
	public Entity resolveSingleTarget(EntityReference targetKey) throws NullPointerException {
		if (targetKey == null) {
			return null;
		}

		final Entity entity = targetLogic.findEntity(this, targetKey).transformResolved(this);

		// TODO: Better inspect and test what causes these issues (Auras being removed from transformed entities?)
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
		return entities.stream().map(e -> e.transformResolved(this)).collect(toList());
	}

	public void setIgnoreEvents(boolean ignoreEvents) {
		this.ignoreEvents = ignoreEvents;
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
	 * Retrieves the current target override specified in the environment.
	 * <p>
	 * A target override can be a specific {@link EntityReference} or a "group reference" (logical entity reference) that
	 * returns exactly zero or one targets. The override should almost always succeed, and it would be surprising if there
	 * were overrides that resulted in no targets being found.
	 *
	 * @param player The player for whom the override should be evaluated.
	 * @param source The source entity of this override.
	 * @return An {@link Entity} or {@code null} if no override is specified.
	 */
	public @Nullable
	Entity getTargetOverride(@NotNull Player player, @Nullable Entity source) {
		if (!getEnvironment().containsKey(Environment.TARGET_OVERRIDE)) {
			return null;
		}

		EntityReference reference = (EntityReference) getEnvironment().get(Environment.TARGET_OVERRIDE);

		if (reference != null
				&& !reference.equals(EntityReference.NONE)) {
			// Might be a single-entity reference
			if (source == null && reference.isTargetGroup()) {
				throw new NullPointerException("Should not be retrieving a target override for a logical reference that has no valid source.");
			}
			List<Entity> entities = resolveTarget(player, source, reference);
			if (entities == null) {
				logger.warn("getTargetOverride {} {}: Key {} resolved to no target entities", getGameId(), source, reference);
				return null;
			}

			if (entities.size() == 0) {
				logger.warn("getTargetOverride {} {}: Key {} resolved to empty entities list", getGameId(), source, reference);
				return null;
			}

			if (entities.size() > 1) {
				throw new RuntimeException("Not permitted to override a multi-target reference.");
			}

			return entities.get(0);
		}

		return null;
	}

	/**
	 * Starts the turn for a player.
	 *
	 * @param playerId The player whose turn should be started.
	 */
	@Suspendable
	public void startTurn(int playerId) {
		logger.debug("{} startTurn: Starting turn {} for playerId={}", getGameId(), getTurn() + 1, playerId);
		setTurn(getTurn() + 1);
		getLogic().startTurn(playerId);
		setActionsThisTurn(0);
		setTurnState(TurnState.TURN_IN_PROGRESS);
		onGameStateChanged();
	}

	@Override
	public String toString() {
		return String.format("[GameContext gameId=%s turn=%d turnState=%s]", getGameId(), getTurn(), getTurnState().toString());
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
		} catch (Throwable e) {
		}
		return null;
	}

	public void setLogic(GameLogic logic) {
		this.logic = logic;
		logic.setContext(this);
	}

	public void setDeckFormat(DeckFormat deckFormat) {
		this.deckFormat = deckFormat;
	}

	public void setEnvironment(Map<Environment, Object> environment) {
		this.environment = environment;
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
		this.setEnvironment(state.environment);
		this.setTriggerManager(state.triggerManager);
		if (getLogic() == null) {
			setLogic(new GameLogic());
		}
		this.getLogic().setIdFactory(new IdFactoryImpl(state.currentId));
		this.getLogic().setContext(this);
		this.setTurnState(state.turnState);
		this.setTurn(state.turnNumber);
		this.setActivePlayerId(state.activePlayerId);
		this.setDeckFormat(state.deckFormat);
	}

	public void setPlayer(int index, Player player) {
		this.players[index] = player;
		if (player.getId() != index) {
			player.setId(index);
		}
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
	@Suspendable
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

	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getOutputStack() {
		if (!getEnvironment().containsKey(Environment.OUTPUTS)) {
			getEnvironment().put(Environment.OUTPUTS, new EnvironmentDeque<>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.OUTPUTS);
	}

	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getAttackerReferenceStack() {
		if (!getEnvironment().containsKey(Environment.ATTACKER_REFERENCE_STACK)) {
			getEnvironment().put(Environment.ATTACKER_REFERENCE_STACK, new EnvironmentDeque<>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.ATTACKER_REFERENCE_STACK);
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, EntityReference> getLastCardPlayedMap() {
		if (!getEnvironment().containsKey(Environment.LAST_CARD_PLAYED)) {
			getEnvironment().put(Environment.LAST_CARD_PLAYED, new EnvironmentMap<>());
		}
		return (Map<Integer, EntityReference>) getEnvironment().get(Environment.LAST_CARD_PLAYED);
	}

	public void setLastCardPlayed(int playerId, EntityReference cardReference) {
		getLastCardPlayedMap().put(IdFactory.UNASSIGNED, cardReference);
		getLastCardPlayedMap().put(playerId, cardReference);
	}

	public EntityReference getLastCardPlayed(int playerId) {
		return getLastCardPlayedMap().get(playerId);
	}

	public EntityReference getLastCardPlayed() {
		return getLastCardPlayedMap().get(IdFactory.UNASSIGNED);
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

	/**
	 * Retrieves the stack of event sources.
	 *
	 * @return A stack of event source {@link EntityReference} objects.
	 */
	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getEventSourceStack() {
		if (!getEnvironment().containsKey(Environment.EVENT_SOURCE_REFERENCE_STACK)) {
			getEnvironment().put(Environment.EVENT_SOURCE_REFERENCE_STACK, new EnvironmentDeque<EntityReference>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.EVENT_SOURCE_REFERENCE_STACK);
	}

	/**
	 * Runs a simulation of the decks with the specified AIs.
	 * <p>
	 * This call will be blocking regardless of using it in a parallel fashion.
	 *
	 * @param deckPair        A pair of decks to run a match with.
	 * @param player1         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 *                        <p>
	 *                        For example, use the argument {@code GameStateValueBehaviour::new} to specify that the first
	 *                        player's AI should be a game state value behaviour.
	 * @param player2         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 * @param useJavaParallel When {@code true}, uses the Java Streams Parallel interface to parallelize this computation
	 *                        on this JVM instance.
	 * @param matchCounter    When not {@code null}, the simulator will increment this counter each time a match is
	 *                        completed. This can be used to implement progress on a different thread.
	 */
	public static SimulationResult simulate(List<Deck> deckPair, Supplier<Behaviour> player1, Supplier<Behaviour> player2, int numberOfGamesInBatch, boolean useJavaParallel, AtomicInteger matchCounter) {
		return simulate(deckPair, player1, player2, numberOfGamesInBatch, useJavaParallel, matchCounter, null);
	}

	/**
	 * Runs a simulation of the decks with the specified AIs.
	 * <p>
	 * This call will be blocking regardless of using it in a parallel fashion.
	 * <p>
	 * When more than two decks are specified, the players will have their statistics merged with multiple decks.
	 *
	 * @param decks           Decks to run the match with. At least two are required.
	 * @param player1         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 *                        <p>
	 *                        For example, use the argument {@code GameStateValueBehaviour::new} to specify that the first
	 *                        player's AI should be a game state value behaviour.
	 * @param player2         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 * @param useJavaParallel When {@code true}, uses the Java Streams Parallel interface to parallelize this computation
	 *                        on this JVM instance.
	 * @param matchCounter    When not {@code null}, the simulator will increment this counter each time a match is
	 * @param contextHandler  A handler that can modify the game context for customization after it was initialized with
	 *                        the specified decks but before mulligans. For example, the {@link GameLogic#seed} can be
	 *                        changed here.
	 */
	public static SimulationResult simulate(List<Deck> decks, Supplier<Behaviour> player1, Supplier<Behaviour> player2, int numberOfGamesInBatch, boolean useJavaParallel, AtomicInteger matchCounter, Consumer<GameContext> contextHandler) {
		// Actually run the computation
		List<Deck[]> combinations = getDeckCombinations(decks, false);
		Stream<Deck[]> deckStream = IntStream.range(0, numberOfGamesInBatch).boxed().flatMap(i -> combinations.stream());
		if (useJavaParallel) {
			deckStream = deckStream.parallel();
		}

		return deckStream.map(decksPair -> {
			GameContext newGame = GameContext.fromDecks(decks);
			newGame.behaviours[0] = player1.get();
			newGame.behaviours[1] = player2.get();
			if (contextHandler != null) {
				contextHandler.accept(newGame);
			}
			SimulationResult innerResult = new SimulationResult(1);

			try {
				newGame.play();

				innerResult.getPlayer1Stats().merge(newGame.getPlayer1().getStatistics());
				innerResult.getPlayer2Stats().merge(newGame.getPlayer2().getStatistics());
				innerResult.calculateMetaStatistics();
			} finally {
				newGame.dispose();
			}

			if (matchCounter != null) {
				matchCounter.incrementAndGet();
			}

			return innerResult;
		}).reduce(SimulationResult::merge).orElseThrow(NullPointerException::new);
	}

	/**
	 * A generator of simulation results. Blocks until all simulations are complete.
	 *
	 * @param deckPair
	 * @param behaviours
	 * @param numberOfGamesInBatch
	 * @param reduce               When {@code true}, merges matches that have the same behaviour and decks.
	 * @param computed             The callback that will be fed a simulation result whenever it is computed.
	 * @throws InterruptedException
	 */
	public static void simulate(List<Deck> deckPair, List<Supplier<Behaviour>> behaviours, int numberOfGamesInBatch, boolean reduce, Consumer<SimulationResult> computed) throws InterruptedException {
		// Actually run the computation
		Stream<Integer> stream = IntStream.range(0, numberOfGamesInBatch).boxed().parallel().unordered();

		Stream<SimulationResult> result = stream.map(i -> {
			GameContext newGame;
			if (behaviours.size() == 0) {
				newGame = fromDecks(deckPair);
			} else if (behaviours.size() == 1) {
				newGame = fromDecks(deckPair, behaviours.get(0).get(), new PlayRandomBehaviour());
			} else {
				newGame = fromDecks(deckPair, behaviours.get(0).get(), behaviours.get(1).get());
			}

			SimulationResult innerResult = new SimulationResult(1);

			try {
				newGame.play();
			} catch (Throwable t) {
				return null;
			}

			innerResult.getPlayer1Stats().merge(newGame.getPlayer1().getStatistics());
			innerResult.getPlayer2Stats().merge(newGame.getPlayer2().getStatistics());
			innerResult.calculateMetaStatistics();

			newGame.dispose();

			return innerResult;
		}).filter(Objects::nonNull);

		if (reduce) {
			computed.accept(result.reduce(SimulationResult::merge).orElseThrow(NullPointerException::new));
		} else {
			result.forEach(computed);
		}
	}

	/**
	 * Gets a game context that's ready to play from two {@link Deck} objects.
	 *
	 * @param decks The {@link Deck}s to use for the players.
	 * @return A {@link GameContext} for which {@link #play()} will immediately work.
	 * @see #getTrace() to get the log of actions that were taken in the game.
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromDecks(List<Deck> decks, Behaviour behaviour1, Behaviour behaviour2) {
		GameContext context = new GameContext();
		Behaviour[] behaviours = new Behaviour[]{behaviour1, behaviour2};
		for (int i = 0; i < 2; i++) {
			context.setPlayer(i, new Player(decks.get(i), "Player " + Integer.toString(i)));
			context.behaviours[i] = behaviours[i];
		}
		context.setDeckFormat(DeckFormat.getSmallestSupersetFormat(decks));

		return context;

	}

	/**
	 * Gets a game context that's ready to play from two {@link Deck} objects. Uses the {@link PlayRandomBehaviour} for
	 * both players.
	 *
	 * @param decks The {@link Deck}s to use for the players.
	 * @return A {@link GameContext} for which {@link #play()} will immediately work.
	 * @see #getTrace() to get the log of actions that were taken in the game.
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromDecks(List<Deck> decks) {
		return fromDecks(decks, new PlayRandomBehaviour(), new PlayRandomBehaviour());
	}


	/**
	 * Creates a new game context with two random decks and random play behaviour.
	 *
	 * @return A game context
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromTwoRandomDecks() {
		return fromDecks(Arrays.asList(new RandomDeck(), new RandomDeck()));
	}

	/**
	 * Creates a game context from the given state.
	 *
	 * @param state A {@link GameState} object.
	 */
	public static GameContext fromState(GameState state) {
		GameContext context = new GameContext();
		context.setGameState(state);
		return context;
	}


	/**
	 * Creates all the possible combinations of decks given a list of decks
	 *
	 * @param decks An input list of deck names
	 * @return A list of 2-tuples of deck names.
	 */
	public static List<String[]> getDeckCombinations(List<String> decks) {
		// Create deck combinations
		Combinations combinations = new Combinations(decks.size(), 2);
		List<String[]> deckPairs = new ArrayList<>();
		for (int[] combination : combinations) {
			deckPairs.add(new String[]{decks.get(combination[0]), decks.get(combination[1])});
		}
		// Include same deck matchups
		for (String deck : decks) {
			deckPairs.add(new String[]{deck, deck});
		}
		return deckPairs;
	}

	public static List<Deck[]> getDeckCombinations(List<Deck> decks, boolean includeMirrors) {
		// Create deck combinations
		Combinations combinations = new Combinations(decks.size(), 2);
		List<Deck[]> deckPairs = new ArrayList<>();
		for (int[] combination : combinations) {
			deckPairs.add(new Deck[]{decks.get(combination[0]), decks.get(combination[1])});
		}
		// Include same deck matchups
		if (includeMirrors) {
			for (Deck deck : decks) {
				deckPairs.add(new Deck[]{deck, deck});
			}
		}
		return deckPairs;
	}

	/**
	 * The number of milliseconds remaining until the active player is automatically changed.
	 *
	 * @return {@code null} if there are no turn/mulligan timers, otherwise the amount of time remaining in milliseconds.
	 */
	public Long getMillisRemaining() {
		return null;
	}

	/**
	 * Retrieves a trace of this game's actions.
	 * <p>
	 * Serialization is not guaranteed to work on later versions of the codebase.
	 *
	 * @return A {@link Trace} containing all the actions that were performed in this game and its initial state.
	 */
	public Trace getTrace() {
		return trace;
	}

	/**
	 * Returns {@code null}, because by default {@link GameContext} are not networked and have no sense of inventory.
	 *
	 * @param player The player whose deck collections should be queried.
	 * @param name   The name of the deck to retrieve
	 * @return
	 */
	@Override
	@Suspendable
	public Deck getDeck(Player player, String name) {
		return null;
	}

	/**
	 * Sets the logging level on this instance.
	 *
	 * @param loggingLevel A preferred logging level.
	 */
	public void setLoggingLevel(Level loggingLevel) {
		((ch.qos.logback.classic.Logger) logger).setLevel(loggingLevel);
	}

	/**
	 * Retrieves the stack of hosts of the currently firing trigger.
	 *
	 * @return A host reference, or null if the trigger didn't have a host.
	 */
	@SuppressWarnings("unchecked")
	public Deque<EntityReference> getTriggerHostStack() {
		if (!getEnvironment().containsKey(Environment.TRIGGER_HOST_STACK)) {
			getEnvironment().put(Environment.TRIGGER_HOST_STACK, new EnvironmentDeque<>());
		}
		return (Deque<EntityReference>) getEnvironment().get(Environment.TRIGGER_HOST_STACK);
	}

	public void setTargetOverride(EntityReference reference) {
		getEnvironment().put(Environment.TARGET_OVERRIDE, reference);
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Resolves a single target that could be a {@link EntityReference#isTargetGroup()} that points to exactly one entity,
	 * like {@link EntityReference#FRIENDLY_HERO}.
	 *
	 * @param player The source player.
	 * @param source The entity from whose point of view this target should be evaluated.
	 * @param target A target key to a specific entity or a named reference ("target group") that returns exactly one
	 *               entity.
	 * @return The entity.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entity> T resolveSingleTarget(Player player, Entity source, EntityReference target) {
		if (target == null || target.equals(EntityReference.NONE)) {
			return null;
		}

		if (target.isTargetGroup()) {
			List<Entity> entities = resolveTarget(player, source, target);
			if (entities == null
					|| entities.size() == 0) {
				return null;
			} else if (entities.size() > 1) {
				throw new ArrayStoreException(String.format("Cannot resolve target %s %s %s", player.toString(), source.toString(), target.toString()));
			}
			return (T) entities.get(0);
		} else {
			return (T) resolveSingleTarget(target);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Entity> EntityZone<E> getZone(int owner, Zones zone) {
		return (EntityZone<E>) getPlayer(owner).getZone(zone);
	}

	public void setBehaviours(Behaviour[] behaviours) {
		this.behaviours = behaviours;
	}

	public void setBehaviour(int i, Behaviour behaviour) {
		behaviours[i] = behaviour;
	}
}
