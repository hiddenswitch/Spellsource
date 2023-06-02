package net.demilich.metastone.game;

import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import com.hiddenswitch.spellsource.common.GameState;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import net.demilich.metastone.game.actions.EndTurnAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.PlayGameLogicRandomBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.EntityZoneTable;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.environment.*;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.logic.*;
import net.demilich.metastone.game.services.Inventory;
import net.demilich.metastone.game.spells.DrawCardSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.targeting.*;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
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
import java.util.stream.Collectors;
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
 * GameContext context = GameContext.fromTwoRandomDecks();
 * context.setBehaviour(GameContext.PLAYER_1, new PlayRandomBehaviour());
 * context.setBehaviour(GameContext.PLAYER_2, new PlayRandomBehaviour());
 * context.play();
 * // The game is over here.
 * }
 * </pre>
 * <p>
 * This will start a game between two opponents that try to play a little smarter, using the Spellsource agent {@link
 * net.demilich.metastone.game.behaviour.GameStateValueBehaviour}. It will also use a pair of decks to do it.
 * <pre>
 * {@code
 * GameContext context = new GameContext();
 * for (int playerId : new int[] {GameContext.PLAYER_1, GameContext.PLAYER_2}) {
 *   context.setBehaviour(playerId, new GameStateValueProvider());
 *   context.setDeck(playerId, Deck.randomDeck("RED", DeckFormat.getFormat("Standard")));
 * }
 * context.play();
 * }
 * </pre>
 * <p>
 * The most important part of the game state is encoded inside the fields of the {@link Player} object in {@link
 * #getPlayers()}, like {@link Player#getMinions()}. The actions taken by the players are delegated to the {@link
 * Behaviour} objects in {@link #getBehaviours()}.
 * <p>
 * Game state is composed of a variety of fields that live inside the context. These fields are:
 * <ul>
 * <li>The {@link #getPlayers()} player objects. This includes zones, attributes on the player object itself, and some
 * statistics fields.</li>
 * <li>The {@link #getEnvironment()} "environment variables," referring to state or memory in the game that does not
 * live on entities.</li>
 * <li>The {@link #getTriggers()} value from the {@link #getLogic()} trigger manager.</li>
 * <li>The {@link DeckFormat} living in {@link #getDeckFormat()}.</li>
 * <li>The next entity ID that will be returned by {@link GameLogic#generateId()}, which is stored in {@link
 * GameLogic#getIdFactory()}'s values.</li>
 * <li>The seed of {@link GameLogic#getSeed()} and the internal serialized state of the {@link GameLogic#getRandom()}
 * random object indicating the next value.</li>
 * <li>The {@link #getTempCards()} temporary cards in a catalogue. These include cards that are generated by effects
 * like Build-A-Beast.</li>
 * <li>The {@link #getTurn()} current turn.</li>
 * <li>The {@link #getTurnState()} current turn state.</li>
 * <li>The {@link #getActivePlayerId()} currently active player ID.</li>
 * <li>The {@link #getActionsThisTurn()} number of actions taken this turn.</li>
 * <li>The state implicit in the call stack of the currently executing action. While this cannot be inspected or easily
 * serialized, consider that while awaiting a mulligan, battlecry choice or discover action this instance is in the
 * middle of executing a function but still has a valid game state.</li>
 * </ul>
 * <p>
 * To get a copy of the state, use {@link #getGameStateCopy()}; while you can access a modifiable copy of the {@link
 * GameState} with {@link #getGameState()}, you're encouraged only use the {@link GameLogic} methods (which mutate the
 * state stored inside this game context) in order to always have valid data.
 * <p>
 * Game actions are chosen by {@link Behaviour} objects living inside {@link #getBehaviours()}. Typically, the {@link
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
 * expected to provide a {@link Player} instance with a {@link Behaviour} that suits the end user's needs. Override the
 * methods of the {@link Behaviour} to model how you'd like to behave given the information incoming to those methods.
 * <p>
 * Executing the card code is complicated, and follows the adage: Every sufficiently complex program has a poorly
 * implemented version of common Lisp. At a high level, players take turns generating {@link GameAction} objects, whose
 * {@link GameAction#execute(GameContext, int)} implementation does things like {@link GameLogic#castSpell(int,
 * SpellDesc, EntityReference, EntityReference, TargetSelection, boolean, GameAction)} or {@link GameLogic#summon(int,
 * Minion, Entity, int, boolean)}. {@code "spell"} fields inside the {@link CardDesc} of the card currently being played
 * get executed by looking at their {@link SpellArg#CLASS} and creating an instance of the corresponding subclass of
 * {@link Spell}. Subsequent "sub-spells" are called by {@link SpellUtils#castChildSpell(GameContext, Player, SpellDesc,
 * Entity, Entity)}. Various components like {@link ValueProvider} or {@link Condition} are defined in the card JSON and
 * used to provide program-like functionality.
 * <p>
 * To illustrate, the key parts of the call stack of a player playing the card Novice Engineer looks like this:
 * <ol>
 * <li>At the top of the stack: {@link GameContext#play()}. This is the main entry point
 * for a started game and we exit this when the game is over.</li>
 * <li>{@link GameContext#takeActionInTurn()}, which is the core step in the game loop.
 * To get possible play actions, we called {@link Card#play()} on each card in the hand, which gives us {@link
 * GameAction} objects. Then, we called {@link Behaviour#requestAction(GameContext, Player, List)} to get which action
 * we wanted the player to take of the possible choices from {@link GameContext#getValidActions()}.</li>
 * <li>{@link GameAction#execute(GameContext, int)},
 * which actually starts the chain of effects for playing a card.</li>
 * <li>{@link GameLogic#summon(int, Minion, Entity, int, boolean)}, which summons minions.</li>
 * <li>{@link GameLogic#resolveOpeners(int, Actor)}},
 * which resolves the battlecry written on Novice Engineer.</li>
 * <li>{@link GameLogic#castSpell(int, SpellDesc,
 * EntityReference, EntityReference, TargetSelection, boolean, GameAction)}, which actually evaluates <b>all
 * effects</b>, not just spells. This method will create an instance of a {@link DrawCardSpell} and eventually
 * calls...</li>
 * <li>{@link Spell#cast(GameContext,
 * Player, SpellDesc, Entity, List)}, which provides common targeting code for spell effects. The meat and bones of
 * effects like drawing a card is done by:</li>
 * <li>{@link Spell#cast(GameContext, Player, SpellDesc, Entity, List)}, the method that all 200+ spells
 * implement to actually cause effects in game. {@link DrawCardSpell} therefore gets its...</li>
 * <li>{@code DrawCardSpell#onCast(GameContext, Player, SpellDesc, Entity, Entity)} method called. In order to cause a
 * card to be drawn, this method calls...</li>
 * <li>{@link GameLogic#drawCard(int, Entity)},
 * which is where the actual work of moving a card from the {@link Player#getDeck()} {@link Zones#DECK} to the hand
 * occurs.</li>
 * </ol>
 * The {@code runGym} methods in the {@code test} code for this module provides an great template for testing rules and
 * card behaviour using Spellsource.
 *
 * @see #play() for more about how a game is "played."
 * @see Behaviour for the interface that the {@link GameContext} delegates player actions and notifications to. This is
 * both the "event handler" specification for which events a player may be interested in; and also a "delegate" in the
 * sense that the object implementing this interface makes decisions about what actions in the game to take (with e.g.
 * {@link Behaviour#requestAction(GameContext, Player, List)}.
 * @see PlayRandomBehaviour for an example behaviour that just makes random decisions when requested.
 * @see GameLogic for the class that actually implements the Spellsource game rules. This class requires a {@link
 * GameContext} because it manipulates the state stored in it.
 * @see GameState for a class that encapsulates all of the state of a game of Spellsource.
 * @see #getGameState() to access and modify the game state.
 * @see #getGameStateCopy() to get a copy of the state that can be stored and diffed.
 * @see Player#getStatistics() to see a summary of a player's activity during a game.
 * @see #getEntities() for a way to enumerate through all of the entities in the game.
 */
public class GameContext implements Cloneable, Serializable, Inventory, EntityZoneTable, Comparable<GameContext> {
	private static Logger LOGGER = LoggerFactory.getLogger(GameContext.class);
    protected transient static ThreadLocal<GameContext> currentContext = new ThreadLocal<>();
	public static final int PLAYER_1 = 0;
	public static final int PLAYER_2 = 1;
	protected transient SpanContext spanContext;
	private Player[] players = new Player[2];
	private Behaviour[] behaviours;
	private GameLogic logic;
	private DeckFormat deckFormat;
	private TargetLogic targetLogic = new TargetLogic();
	private List<Trigger> triggers = new ArrayList<Trigger>();
	private Deque<GameLogic.QueuedTrigger> deferredTriggersQueue = new ArrayDeque<>();
	private Deque<GameAction> actionStack = new ArrayDeque<>();
	private Set<Trigger> processingTriggers = new HashSet<>();
	private Map<Environment, Object> environment = new HashMap<>();
	private Map<String, AtomicInteger> variables = new HashMap<>();
	private int activePlayerId = -1;
	private Player winner;
	private GameStatus result;
	private TurnState turnState = TurnState.TURN_ENDED;
	private boolean disposed = false;
	// Note: startTurn is called at least once, so the reported turn will always start counting at 1
	private int turn;
	private int actionsThisTurn;
	private boolean ignoreEvents;
	private CardCatalogue cardCatalogue = ClasspathCardCatalogue.INSTANCE;
	private CardList tempCards = new CardArrayList();
	private boolean didCallEndGame;

	private transient Trace trace = new Trace();
	private transient Thread thread;

	/**
	 * Creates a game context with two empty players and two {@link PlayRandomBehaviour} behaviours.
	 * <p>
	 * Hero cards are not given to the players. Thus, this is not enough typically to mutate and run a game. Use {@link
	 * #GameContext(String...)} to create a game initialized with the specified hero classes.
	 */
	public GameContext() {
		behaviours = new Behaviour[]{new PlayRandomBehaviour(), new PlayRandomBehaviour()};
		setLogic(new GameLogic());
		setDeckFormat(ClasspathCardCatalogue.INSTANCE.all());
		setPlayer1(new Player());
		setPlayer2(new Player());
	}

	/**
	 * Creates a game context from another context by copying it.
	 *
	 * @param fromContext The other context to copy.
	 */
	public GameContext(GameContext fromContext) {
		GameLogic logicClone = fromContext.getLogic().clone();
		Player player1Clone = fromContext.getPlayer1().clone();
		Player player2Clone = fromContext.getPlayer2().clone();
		setLogic(logicClone);
		behaviours = new Behaviour[]{fromContext.behaviours[0] == null ? null : fromContext.behaviours[0].clone(), fromContext.behaviours[1] == null ? null : fromContext.behaviours[1].clone()};
		setDeckFormat(fromContext.getDeckFormat());
		setPlayer1(player1Clone);
		setPlayer2(player2Clone);

		setTempCards(fromContext.getTempCards().clone());
		var triggers = fromContext.getTriggers().stream().map(Trigger::clone).collect(toList());
		setTriggers(triggers);
		setActivePlayerId(fromContext.getActivePlayerId());
		setTurn(fromContext.getTurn());
		setActionsThisTurn(fromContext.getActionsThisTurn());
		setStatus(fromContext.getStatus());
		setTurnState(fromContext.getTurnState());
		setWinner(fromContext.getWinner());
		setTrace(getTrace().clone());
		if (fromContext.getTrace() != null) {
			setTrace(fromContext.getTrace().clone());
		}

		for (Map.Entry<Environment, Object> entry : fromContext.getEnvironment().entrySet()) {
			Object value1 = entry.getValue();
			if (value1 == null
					|| !EnvironmentValue.class.isAssignableFrom(value1.getClass())) {
				getEnvironment().put(entry.getKey(), value1);
			} else {
				EnvironmentValue value = (EnvironmentValue) value1;
				getEnvironment().put(entry.getKey(), value.getCopy());
			}
		}

		variables = new HashMap<>();
		for (var kv : fromContext.variables.entrySet()) {
			variables.put(kv.getKey(), new AtomicInteger(kv.getValue().intValue()));
		}
	}

	/**
	 * Gets the current game context.
	 * @return a game context instance
	 */
	public static GameContext current() {
		return currentContext.get();
	}

    private void setTriggers(List<Trigger> triggers) {
		this.triggers = triggers;
	}

	/**
	 * Creates an uninitialized game context (i.e., no cards in the decks of the players or behaviours specified). A hero
	 * card is retrieved and given to each player.
	 * <p>
	 * This is typically the absolute minimum needed to mutate and run a game.
	 *
	 * @param heroClasses The player's hero classes.
	 */
	public GameContext(String... heroClasses) {
		this();
		for (int i = 0; i < heroClasses.length; i++) {
			getPlayer(i).setHero(getCardCatalogue().getHeroCard(heroClasses[i]).hero());
		}
	}

	/**
	 * Creates a game context from a trace.
	 *
	 * @param trace
	 * @return
	 */
	public static GameContext fromTrace(Trace trace) {
		return trace.replayContext();
	}

	/**
	 * Adds a temporary card. A temporary card is a card that exists only in this instance and not in the {@link
	 * CardCatalogue}.
	 *
	 * @param card The card to add, typically made with code.
	 */
	public void addTempCard(Card card) {
		getTempCards().addCard(card.clone());
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
		return new GameContext(this);
	}

	/**
	 * Clears state to ensure this context isn't referencing it anymore.
	 */
	protected void close() {
		getLogic().dispose();
	}

	/**
	 * Ends the game immediately.
	 */
	protected void endGame() {
        currentContext.set(this);
		updateAndGetGameOver();

		// Don't do processing of end game effects more than once.
		if (didCallEndGame()) {
			notifyPlayersGameOver();
			return;
		}

		didCallEndGame = true;

		// Expire the game just once here
		getLogic().expireAll();
		LOGGER.debug("endGame {}: Game is now ending", getGameId());
		setWinner(getLogic().getWinner(getActivePlayer(), getOpponent(getActivePlayer())));
		notifyPlayersGameOver();
		calculateStatistics();
	}

	protected void notifyPlayersGameOver() {
		for (int i = 0; i < behaviours.length; i++) {
			if (behaviours[i] == null) {
				continue;
			}
			behaviours[i].onGameOver(this, i, getWinner() != null ? getWinner().getId() : -1);
		}
	}

	protected void calculateStatistics() {
		if (getWinner() != null) {
			LOGGER.trace("calculateStatistics {}: Game finished after {}, turns, the winner is {}", getGameId(), getTurn(), getWinner().getName());
			getWinner().getStatistics().gameWon();
			Player loser = getOpponent(getWinner());
			loser.getStatistics().gameLost();
		} else {
			LOGGER.trace("calculateStatistics {}: Game finished after {} turns in a draw", getGameId(), getTurn());
			getPlayer1().getStatistics().gameLost();
			getPlayer2().getStatistics().gameLost();
		}
	}

	/**
	 * Ends the current player's turn immediately, setting the active player to their opponent.
	 */
	public void endTurn() {
		currentContext.set(this);
		LOGGER.trace("{} endTurn: Ending turn {}", getGameId(), getActivePlayer().getId());
		getLogic().endTurn(getActivePlayerId());
		setActivePlayerId(getLogic().getNextActivePlayerId());
		setTurnState(TurnState.TURN_ENDED);
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
			setStatus(GameStatus.RUNNING);
			return false;
		}
		setStatus(getLogic().getMatchResult(getActivePlayer(), getOpponent(getActivePlayer())));
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
		Entity entity = resolveSingleTarget(targetReference);
		if (entity.getZone() != Zones.BATTLEFIELD) {
			return new ArrayList<>();
		}
		Actor minion = (Actor) entity;
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
		return cardCatalogue.getCardById(cardId);
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
		Entity entity = resolveSingleTarget(minionReference);
		if (entity.getZone() != Zones.BATTLEFIELD) {
			return new ArrayList<>();
		}
		Actor minion = (Actor) entity;
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
	 * @return A read only list of {@link Player} objects.
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
	 * @param minionReference The minion reference.
	 * @return A list of {@link Actor} (sometimes empty) of minions to the geometric right of the {@code minionReference}.
	 */
	public List<Actor> getRightMinions(EntityReference minionReference) {
		List<Actor> rightMinions = new ArrayList<>();
		Actor minion = (Actor) resolveSingleTarget(minionReference);
		Player player = getPlayer(minion.getOwner());
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
	 * middle of evaluating a {@link GameLogic#summon(int, Minion, Entity, int, boolean)}.
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
	 * When true, the game logic doesn't handle an events being raised.
	 *
	 * @return {@code true} if the game context should ignore incoming events.
	 */
	public boolean getIgnoreEvents() {
		return ignoreEvents;
	}

	/**
	 * Initializes a game.
	 * <p>
	 * This function will choose a starting player, then move cards into the mulligan (set aside) zone, ask for mulligans,
	 * and start the game. {@link #resume()} will start the first turn.
	 */
	public void init() {
        currentContext.set(this);
		getLogic().contextReady();
		startTrace();
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		init(startingPlayerId);
	}

	/**
	 * Initialized the game with the specified starting player. When called by itself, does not initialize the trace.
	 *
	 * @param startingPlayerId
	 */
	public void init(int startingPlayerId) {
        currentContext.set(this);
		setActivePlayerId(startingPlayerId);
		getEnvironment().put(Environment.STARTING_PLAYER, startingPlayerId);
		LOGGER.trace("{} init: Initializing game with starting player {}", getGameId(), getActivePlayer().getUserId());
		getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));
		getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_1, startingPlayerId == PLAYER_1);
		getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_2, startingPlayerId == PLAYER_2);
		List<Card> firstHandActive = getActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
		List<Card> discardedCardsActive = getActivePlayer().getSetAsideZone().isEmpty()
				? Collections.emptyList() :
				getBehaviours().get(getActivePlayerId()).mulligan(this, getActivePlayer(), firstHandActive);
		List<Card> firstHandNonActive = getNonActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
		List<Card> discardedCardsNonActive = getNonActivePlayer().getSetAsideZone().isEmpty()
				? Collections.emptyList()
				: getBehaviours().get(getNonActivePlayerId()).mulligan(this, getNonActivePlayer(), firstHandNonActive);
		getLogic().handleMulligan(getActivePlayer(), true, discardedCardsActive);
		getLogic().handleMulligan(getNonActivePlayer(), false, discardedCardsNonActive);
		traceMulligans(discardedCardsActive, discardedCardsNonActive);
		startGame();
	}

	protected void traceMulligans(List<Card> mulligansActive, List<Card> mulligansNonActive) {
		trace.setMulligans(Arrays.asList(
				new MulliganTrace()
						.setPlayerId(getActivePlayerId())
						.setEntityIds(mulligansActive.stream().mapToInt(Card::getId).boxed().collect(Collectors.toUnmodifiableList())),
				new MulliganTrace()
						.setPlayerId(getNonActivePlayerId())
						.setEntityIds(mulligansNonActive.stream().mapToInt(Card::getId).boxed().collect(Collectors.toUnmodifiableList()))
		));
	}

	/**
	 * Ensures that the game state is traced / recorded
	 */
	protected void startTrace() {
		trace.setStartState(getGameState());
		trace.setSeed(getLogic().getSeed());
	}

	/**
	 * Executes the specified game action, typically by calling {@link GameLogic#performGameAction(int, GameAction)}.
	 *
	 * @param playerId   The player who's performing the action.
	 * @param gameAction The action to perform.
	 * @see GameLogic#performGameAction(int, GameAction) for more about game actions.
	 */
	public void performAction(int playerId, GameAction gameAction) {
        currentContext.set(this);
		getLogic().performGameAction(playerId, gameAction);
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
	public void play() {
		LOGGER.debug("play {}: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
		play(false);
	}

	public void play(boolean fork) {
		if (fork) {
			if (getThread() != null) {
				throw new IllegalStateException("fiber");
			}

			setThread(Thread.startVirtualThread(() -> {
				init();
				resume();
			}));

		} else {
			try {
				init();
				resume();
			} catch (Throwable throwable) {
				getTrace().setTraceErrors(true);
				throw throwable;
			}
		}
	}

	/**
	 * Requests an action from a player and takes it in the turn.
	 * <p>
	 * This method will call {@link Behaviour#requestAction(GameContext, Player, List)} to get an action from the
	 * currently active player. It then calls {@link #performAction(int, GameAction)} with the returned {@link
	 * GameAction}.
	 *
	 * @return {@code false} if the player selected an {@link EndTurnAction}, indicating the player would like to end
	 * their turn.
	 */
	public boolean takeActionInTurn() {
        currentContext.set(this);
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("GameContext/takeActionInTurn")
				.withTag("gameId", getGameId())
				.withTag("turn", getTurn())
				.withTag("actionsThisTurn", getActionsThisTurn())
				.asChildOf(getSpanContext())
				.start();
		setActionsThisTurn(getActionsThisTurn() + 1);
		try (Scope s1 = tracer.activateSpan(span)) {
			if (getActionsThisTurn() > 99) {
				LOGGER.warn("{} takeActionInTurn: Turn has been forcefully ended after {} actions", getGameId(), getActionsThisTurn());
				endTurn();
				return false;
			}

			boolean gameOver = updateAndGetGameOver();
			if (gameOver) {
				return false;
			}

			List<GameAction> validActions = getLogic().getValidActions(getActivePlayerId());

			if (validActions.size() == 0) {
				return false;
			}

			// TODO: Does this need to respect random actions?
			// todo: do we need to lock here, and unlock again later?
			GameAction nextAction = behaviours[getActivePlayerId()].requestAction(this, getActivePlayer(), validActions);

			if (nextAction == null) {
				throw new NullPointerException("nextAction");
			}

			trace.addAction(nextAction);
			getLogic().performGameAction(getActivePlayerId(), nextAction);
			return nextAction.getActionType() != ActionType.END_TURN;
		} finally {
			span.finish();
		}
	}

	/**
	 * Tries to find the entity references by the {@link EntityReference}.
	 *
	 * @param targetKey The reference to find.
	 * @return The {@link Entity} pointed to by the {@link EntityReference}, or {@code null} if the provided entity
	 * reference was {@code null} or {@link EntityReference#NONE}
	 * @throws TargetNotFoundException if the reference could not be found. Game rules shouldn't be looking for references
	 *                                 that cannot be found.
	 */
	public Entity resolveSingleTarget(EntityReference targetKey) throws TargetNotFoundException {
        currentContext.set(this);
		return resolveSingleTarget(targetKey, true);
	}

	/**
	 * Resolves a single target given the specification, even if the specification is a {@link
	 * EntityReference#isTargetGroup()}
	 *
	 * @param targetKey
	 * @param rejectRemovedFromPlay
	 * @return
	 * @throws TargetNotFoundException
	 */
	public Entity resolveSingleTarget(EntityReference targetKey, boolean rejectRemovedFromPlay) throws TargetNotFoundException {
        currentContext.set(this);
		if (targetKey == null || Objects.equals(targetKey, EntityReference.NONE)) {
			return null;
		}

		Entity entity = targetLogic.findEntity(this, targetKey).transformResolved(this);

		// TODO: Better inspect and test what causes these issues (Auras being removed from transformed entities?)
		if (rejectRemovedFromPlay && entity.getZone() == Zones.REMOVED_FROM_PLAY) {
			throw new TargetNotFoundException("Although this reference was found, it was located in the REMOVED_FROM_PLAY zone", targetKey);
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
        currentContext.set(this);
		List<Entity> entities = targetLogic.resolveTargetKey(this, player, source, targetKey);
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
				LOGGER.warn("getTargetOverride {} {}: Key {} resolved to no target entities", getGameId(), source, reference);
				return null;
			}

			if (entities.size() == 0) {
				LOGGER.warn("getTargetOverride {} {}: Key {} resolved to empty entities list", getGameId(), source, reference);
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
	 * Fire the start game events here instead
	 */
	protected void startGame() {
		getLogic().startGameForPlayer(getPlayer(PLAYER_1));
		getLogic().startGameForPlayer(getPlayer(PLAYER_2));
	}

	public int getNonActivePlayerId() {
		return getNonActivePlayer().getId();
	}

	/**
	 * Starts the turn for a player.
	 *
	 * @param playerId The player whose turn should be started.
	 */
	public void startTurn(int playerId) {
        currentContext.set(this);
		LOGGER.trace("{} startTurn: Starting turn {} for playerId={}", getGameId(), getTurn() + 1, playerId);
		setTurn(getTurn() + 1);
		getLogic().startTurn(playerId);
		setActionsThisTurn(0);
		setTurnState(TurnState.TURN_IN_PROGRESS);
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
	public @Nullable Entity tryFind(EntityReference targetKey) {
		return tryFind(targetKey, true);
	}

	/**
	 * Tries to find an entity given the reference.
	 *
	 * @param targetKey             The reference to the entity.
	 * @param rejectRemovedFromPlay
	 * @return The found {@link Entity}, or {@code null} if no entity was found.
	 */
	public @Nullable Entity tryFind(EntityReference targetKey, boolean rejectRemovedFromPlay) {
		if (targetKey == null) {
			return null;
		}

		Entity entity = targetLogic.findEntity(this, targetKey);
		if (entity == null) {
			return null;
		}

		entity = entity.transformResolved(this);

		// TODO: Better inspect and test what causes these issues (Auras being removed from transformed entities?)
		if (rejectRemovedFromPlay && entity.getZone() == Zones.REMOVED_FROM_PLAY) {
			return null;
		}

		return entity;
	}

	public void setLogic(GameLogic logic) {
		if (this.logic != null && logic.getInternalId() != this.logic.getInternalId()) {
			throw new IllegalArgumentException("logic.getInternalId() != this.logic.getInternalId()");
		}
		this.logic = logic;
		logic.setContext(this);
	}

	public GameContext setDeckFormat(DeckFormat deckFormat) {
		this.deckFormat = deckFormat;
		return this;
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

	public void setStatus(GameStatus result) {
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
		this.setPlayer(GameContext.PLAYER_1, state.getPlayer1());
		this.setPlayer(GameContext.PLAYER_2, state.getPlayer2());
		this.setTempCards(state.getTempCards());
		this.setEnvironment(state.getEnvironment());
		this.setTriggers(state.getTriggers());
		if (getLogic() == null) {
			setLogic(new GameLogic());
		}
		this.getLogic().setIdFactory(new IdFactoryImpl(state.getCurrentId()));
		this.getLogic().setContext(this);
		this.setTurnState(state.getTurnState());
		this.setTurn(state.getTurnNumber());
		this.setActivePlayerId(state.getActivePlayerId());
		this.setDeckFormat(state.getDeckFormat());
	}

	public GameContext setPlayer(int index, Player player) {
		this.players[index] = player;
		if (player.getId() != index) {
			player.setId(index);
		}
		return this;
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
		return Stream.concat(
				getPlayer1().getLookup().values().stream(),
				getPlayer2().getLookup().values().stream());
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
        currentContext.set(this);
		// Make sure IDs are assigned before we try to repeatedly destroy hero
		if (getEntities().anyMatch(e -> e.getId() == IdFactory.UNASSIGNED)) {
			getLogic().initializePlayerAndMoveMulliganToSetAside(0, true);
			getLogic().initializePlayerAndMoveMulliganToSetAside(1, false);
		}

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
	 * Returns the spell values calculated so far by {@link MetaSpell} spells.
	 * <p>
	 * Implements Living Mana. Using a stack fixes issues where a later {@link MetaSpell} busts an earlier one.
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

	@SuppressWarnings("unchecked")
	public Map<Integer, EntityReference> getLastCardPlayedBeforeCurrentSequenceMap() {
		if (!getEnvironment().containsKey(Environment.LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE)) {
			getEnvironment().put(Environment.LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE, new EnvironmentMap<>());
		}
		return (Map<Integer, EntityReference>) getEnvironment().get(Environment.LAST_CARD_PLAYED_BEFORE_CURRENT_SEQUENCE);
	}

	public void setLastCardPlayedBeforeCurrentSequence(int playerId, EntityReference cardReference) {
		getLastCardPlayedBeforeCurrentSequenceMap().put(IdFactory.UNASSIGNED, cardReference);
		getLastCardPlayedBeforeCurrentSequenceMap().put(playerId, cardReference);
	}

	public EntityReference getLastCardPlayed(int playerId) {
		return getLastCardPlayedMap().get(playerId);
	}

	public EntityReference getLastCardPlayed() {
		return getLastCardPlayedMap().get(IdFactory.UNASSIGNED);
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, EntityReference> getLastSpellPlayedThisTurnMap() {
		if (!getEnvironment().containsKey(Environment.LAST_SPELL_PLAYED_THIS_TURN)) {
			getEnvironment().put(Environment.LAST_SPELL_PLAYED_THIS_TURN, new EnvironmentMap<>());
		}
		return (Map<Integer, EntityReference>) getEnvironment().get(Environment.LAST_SPELL_PLAYED_THIS_TURN);
	}

	public void setLastSpellPlayedThisTurn(int playerId, EntityReference cardReference) {
		getLastSpellPlayedThisTurnMap().put(IdFactory.UNASSIGNED, cardReference);
		getLastSpellPlayedThisTurnMap().put(playerId, cardReference);
	}

	public EntityReference getLastCardPlayedBeforeCurrentSequence() {
		return getLastCardPlayedBeforeCurrentSequenceMap().get(IdFactory.UNASSIGNED);
	}

	public EntityReference getLastCardPlayedBeforeCurrentSequence(int playerId) {
		return getLastCardPlayedBeforeCurrentSequenceMap().get(playerId);
	}

	public EnvironmentAftermathTriggeredList getAftermaths() {
		return (EnvironmentAftermathTriggeredList) getEnvironment().computeIfAbsent(Environment.DEATHRATTLES_TRIGGERED, environment1 -> new EnvironmentAftermathTriggeredList());
	}

	protected Player getNonActivePlayer() {
		return getOpponent(getActivePlayer());
	}

	/**
	 * Resumes a game, playing it to completion.
	 * <p>
	 * Useful for implementing Monte Carlo Tree Search AI algorithms.
	 */
	public void resume() {
        currentContext.set(this);
		while (!updateAndGetGameOver()) {
			startTurn(getActivePlayerId());
			while (takeActionInTurn()) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
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
	 * @param decks           Decks to run the match with. At least two are required.
	 * @param player1         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 *                        <p>
	 *                        For example, use the argument {@code GameStateValueBehaviour::new} to specify that the first
	 *                        player's AI should be a game state value behaviour.
	 * @param player2         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 * @param gamesPerMatchup The number of games per matchup to play. The number of matchups total can be calculated with
	 *                        {@link #simulationCount(int, int, boolean)}.
	 * @param useJavaParallel When {@code true}, uses the Java Streams Parallel interface to parallelize this computation
	 *                        on this JVM instance.
	 * @param matchCounter    When not {@code null}, the simulator will increment this counter each time a match is
	 *                        completed. This can be used to implement progress on a different thread.
	 */
	public static SimulationResult simulate(List<GameDeck> decks, Supplier<Behaviour> player1, Supplier<Behaviour> player2, int gamesPerMatchup, boolean useJavaParallel, AtomicInteger matchCounter) {
		return simulate(decks, player1, player2, gamesPerMatchup, useJavaParallel, false, matchCounter, null, null);
	}

	/**
	 * Runs a simulation of the decks with the specified AIs.
	 * <p>
	 * This call will be blocking regardless of using it in a parallel fashion.
	 *
	 * @param decks           Decks to run the match with. At least one is required if {@code includeMirrors} is {@code
	 *                        true}, otherwise at least two.
	 * @param player1         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 *                        <p>
	 *                        For example, use the argument {@code GameStateValueBehaviour::new} to specify that the first
	 *                        player's AI should be a game state value behaviour.
	 * @param player2         A {@link Supplier} (function which returns a new instance) of a {@link Behaviour} that
	 *                        corresponds to an AI to use for this player.
	 * @param gamesPerMatchup The number of games per matchup to play. The number of matchups total can be calculated with
	 *                        {@link #simulationCount(int, int, boolean)}.
	 * @param useJavaParallel When {@code true}, uses the Java Streams Parallel interface to parallelize this computation
	 *                        on this JVM instance.
	 * @param includeMirrors  When {@code true}, includes mirror matchups for each deck.
	 */
	public static SimulationResult simulate(List<GameDeck> decks, Supplier<Behaviour> player1, Supplier<Behaviour> player2, int gamesPerMatchup, boolean useJavaParallel, boolean includeMirrors) {
		return simulate(decks, player1, player2, gamesPerMatchup, useJavaParallel, includeMirrors, null, null, null);
	}

	/**
	 * Runs a simulation of the decks with the specified AIs.
	 * <p>
	 * This call will be blocking regardless of using it in a parallel fashion.
	 * <p>
	 * When more than two decks are specified, the players will have their statistics merged with multiple decks.
	 *
	 * @param decks                        Decks to run the match with. At least two are required.
	 * @param player1                      A {@link Supplier} (function which returns a new instance) of a {@link
	 *                                     Behaviour} that corresponds to an AI to use for this player.
	 *                                     <p>
	 *                                     For example, use the argument {@code GameStateValueBehaviour::new} to specify
	 *                                     that the first player's AI should be a game state value behaviour.
	 * @param player2                      A {@link Supplier} (function which returns a new instance) of a {@link
	 *                                     Behaviour} that corresponds to an AI to use for this player.
	 * @param gamesPerMatchup              The number of games per matchup to play. The number of matchups total can be
	 *                                     calculated with {@link #simulationCount(int, int, boolean)}.
	 * @param useJavaParallel              When {@code true}, uses the Java Streams Parallel interface to parallelize this
	 *                                     computation on this JVM instance.
	 * @param includeMirrors               When {@code true}, includes mirror matchups
	 * @param matchCounter                 When not {@code null}, the simulator will increment this counter each time a
	 *                                     match is
	 * @param mutateConstructedGameContext A handler that can modify the game context for customization after it was
	 *                                     initialized with the specified decks but before mulligans. For example, the
	 *                                     {@link GameLogic#getSeed()} can
	 * @param afterGameContextInit         A handler that can add/remove things to the game context after the players have
	 *                                     mulliganned.
	 */
	public static SimulationResult simulate(List<GameDeck> decks, Supplier<Behaviour> player1, Supplier<Behaviour> player2, int gamesPerMatchup, boolean useJavaParallel, boolean includeMirrors, AtomicInteger matchCounter, Consumer<GameContext> mutateConstructedGameContext, Consumer<GameContext> afterGameContextInit) {
		// Actually run the computation
		List<GameDeck[]> combinations = getDeckCombinations(decks, includeMirrors);
		Stream<GameDeck[]> deckStream = IntStream.range(0, gamesPerMatchup).boxed().flatMap(i -> combinations.stream());
		if (useJavaParallel) {
			deckStream = deckStream.parallel();
		}

		return deckStream.map(decksPair -> {
			GameContext newGame = GameContext.fromDecks(Arrays.asList(decksPair));
			newGame.behaviours[0] = player1.get();
			newGame.behaviours[1] = player2.get();
			if (mutateConstructedGameContext != null) {
				mutateConstructedGameContext.accept(newGame);
			}
			SimulationResult innerResult = new SimulationResult(1);

			try {
				if (matchCounter != null) {
					matchCounter.incrementAndGet();
				}

				newGame.init();
				if (afterGameContextInit != null) {
					afterGameContextInit.accept(newGame);
				}
				newGame.resume();

				innerResult.getPlayer1Stats().merge(newGame.getPlayer1().getStatistics());
				innerResult.getPlayer2Stats().merge(newGame.getPlayer2().getStatistics());
				innerResult.calculateMetaStatistics();
			} catch (Throwable any) {
				innerResult.setExceptionCount(innerResult.getExceptionCount() + 1);
				return innerResult;
			} finally {
				newGame.close();
			}

			return innerResult;
		})
				.filter(Objects::nonNull)
				.reduce(SimulationResult::merge).orElseThrow(NullPointerException::new);
	}

	/**
	 * Calculates the expected number of simulations that will be run given the parameters of the simulation function.
	 *
	 * @param numberOfDecks   The number of decks (i.e., {@code decks.size()})
	 * @param gamesPerMatchup The number of games to play per unique deck pair.
	 * @param includeMirrors  When true, include mirror matches.
	 * @return
	 */
	public static int simulationCount(int numberOfDecks, int gamesPerMatchup, boolean includeMirrors) {
		return ((includeMirrors ? numberOfDecks : 0) + (numberOfDecks * (numberOfDecks - 1)) / 2) * gamesPerMatchup;
	}

	/**
	 * A generator of simulation results. Blocks until all simulations are complete.
	 *
	 * @param deckPair        Two decks to test. Specify the same deck twice to perform a mirror match.
	 * @param behaviours      The behaviours to use. When an empty list is specified, uses {@link PlayRandomBehaviour}.
	 * @param gamesPerMatchup The number of games to play per matchup.
	 * @param reduce          When {@code true}, merges matches that have the same behaviour and decks.
	 * @param computed        The callback that will be fed a simulation result whenever it is computed.
	 * @throws InterruptedException
	 */
	public static void simulate(List<GameDeck> deckPair, List<Supplier<Behaviour>> behaviours, int gamesPerMatchup, boolean reduce, Consumer<SimulationResult> computed) throws InterruptedException {
		// Actually run the computation
		Stream<Integer> stream = IntStream.range(0, gamesPerMatchup).boxed().parallel().unordered();

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
				LOGGER.error("simulation: {}", t);
				return null;
			}

			innerResult.getPlayer1Stats().merge(newGame.getPlayer1().getStatistics());
			innerResult.getPlayer2Stats().merge(newGame.getPlayer2().getStatistics());
			innerResult.calculateMetaStatistics();

			return innerResult;
		}).filter(Objects::nonNull);

		if (reduce) {
			computed.accept(result.reduce(SimulationResult::merge).orElseThrow(NullPointerException::new));
		} else {
			result.forEach(computed);
		}
	}

	/**
	 * Gets a game context that's ready to play from two {@link GameDeck} objects.
	 *
	 * @param decks The {@link GameDeck}s to use for the players.
	 * @return A {@link GameContext} for which {@link #play()} will immediately work.
	 * @see #getTrace() to get the log of actions that were taken in the game.
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromDecks(List<GameDeck> decks, Behaviour behaviour1, Behaviour behaviour2) {
		GameContext context = new GameContext();
		Behaviour[] behaviours = new Behaviour[]{behaviour1, behaviour2};
		for (int i = 0; i < 2; i++) {
			context.setPlayer(i, new Player(decks.get(i), "Player " + Integer.toString(i), ClasspathCardCatalogue.INSTANCE));
			context.behaviours[i] = behaviours[i];
		}
		context.setDeckFormat(context.getCardCatalogue().getSmallestSupersetFormat(decks));

		return context;

	}

	/**
	 * Gets a game context that's ready to play from two {@link GameDeck} objects. Uses the {@link PlayRandomBehaviour}
	 * for both players.
	 *
	 * @param decks The {@link GameDeck}s to use for the players.
	 * @return A {@link GameContext} for which {@link #play()} will immediately work.
	 * @see #getTrace() to get the log of actions that were taken in the game.
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromDecks(List<GameDeck> decks) {
		return fromDecks(decks, new PlayRandomBehaviour(), new PlayRandomBehaviour());
	}

	/**
	 * Gets a game context that's ready to play from two deck lists encoded in the standard community format. Uses the
	 * {@link PlayRandomBehaviour} for both players.
	 *
	 * @param deckLists A Hearthstone deck string or a deck list of the format, with newlines:
	 *                  <p>
	 *                  Name: Deck Name
	 *                  <p>
	 *                  Class: Color Hero Class (e.g., PRIEST) specified in {@link HeroClass}.
	 *                  <p>
	 *                  Format: Standard, Wild, Custom or others specified in {@link CardCatalogue#formats()}.
	 *                  <p>
	 *                  1x Card Name
	 *                  <p>
	 *                  2x Card Name
	 * @return A game context.
	 * @see #getTrace() to get the log of actions that were taken in the game.
	 * @see #play() to actually execute the game.
	 */
	public static GameContext fromDeckLists(List<String> deckLists) {
		return fromDecks(deckLists.stream().map(DeckCreateRequest::fromDeckList).map(DeckCreateRequest::toGameDeck).collect(toList()));
	}


	public static GameContext fromDecks(long seed, List<GameDeck> decks) {
		var context = fromDecks(decks, new PlayGameLogicRandomBehaviour(), new PlayGameLogicRandomBehaviour());
		context.setLogic(new GameLogic(seed));
		return context;
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

	public static List<GameDeck[]> getDeckCombinations(List<GameDeck> decks, boolean includeMirrors) {
		List<GameDeck[]> deckPairs = new ArrayList<>();

		// Create deck combinations
		// Include same deck matchups
		if (includeMirrors) {
			for (GameDeck deck : decks) {
				deckPairs.add(new GameDeck[]{deck, deck});
			}
		}

		if (decks.size() >= 2) {
			Combinations combinations = new Combinations(decks.size(), 2);
			for (int[] combination : combinations) {
				deckPairs.add(new GameDeck[]{decks.get(combination[0]), decks.get(combination[1])});
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
	public GameDeck getDeck(Player player, String name) {
		return null;
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
		return LOGGER;
	}

	public void setLogger(Logger logger) {
		this.LOGGER = logger;
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

	protected boolean didCallEndGame() {
		return didCallEndGame;
	}

	public Thread getThread() {
		return thread;
	}

	protected GameContext setThread(Thread fiber) {
		this.thread = fiber;
		return this;
	}

	/**
	 * Sets the specified player's deck and hero (by implication)
	 *
	 * @param playerId
	 * @param deck
	 */
	public void setDeck(int playerId, GameDeck deck) {
		getPlayer(playerId).getDeck().clear();
		getPlayer(playerId).getDeck().addAll(deck.getCardsCopy());
		getPlayer(playerId).setHero(deck.getHeroCard(getCardCatalogue()).hero());
	}

	/**
	 * Returns {@code 0} if the two game contexts have the same meaningful game state.
	 * <p>
	 * Otherwise, returns {@code 1} if {@code other} is "further along"
	 *
	 * @param other
	 * @return
	 */
	public int compareTo(@NotNull GameContext other) {
		return 0;
	}

	protected void setTrace(Trace trace) {
		this.trace = trace;
	}

	/**
	 * Provides context for tracing in this context. This is the OpenTracing span context, typically assigned by the
	 * matchmaker or whatever created this instance.
	 *
	 * @return
	 */
	public SpanContext getSpanContext() {
		return spanContext;
	}

	public GameContext setSpanContext(SpanContext spanContext) {
		this.spanContext = spanContext;
		return this;
	}

	/**
	 * Returns a reference to the variables stored in the game context, used by spells to maintain correct space when a
	 * strand currently being executed is cloned.
	 *
	 * @return
	 */
	public Map<String, AtomicInteger> getVariables() {
		return variables;
	}

	/**
	 * Creates an integer value in the context's cloneable storage.
	 *
	 * @param name
	 * @param initialValue
	 */
	public void createInt(String name, int initialValue) {
		variables.put(name, new AtomicInteger(initialValue));
	}

	/**
	 * Adds the delta value and returns the new value of the named integer from the context's cloneable storage.
	 *
	 * @param name
	 * @param delta
	 * @return
	 */
	public int addAndGetInt(String name, int delta) {
		return variables.get(name).addAndGet(delta);
	}

	/**
	 * Gets the value of the named integer in the context's cloneable storage.
	 *
	 * @param name
	 * @return
	 */
	public int getInt(String name) {
		return variables.get(name).get();
	}

	/**
	 * Removes the specified integer from the context's cloneable storage.
	 *
	 * @param name
	 * @return
	 */
	public int removeInt(String name) {
		return variables.remove(name).get();
	}

	public GameContext setOtherTriggers(List<Trigger> otherTriggers) {
		return this;
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	public Deque<GameLogic.QueuedTrigger> getDeferredTriggersQueue() {
		return deferredTriggersQueue;
	}

	public Set<Trigger> getProcessingTriggers() {
		return processingTriggers;
	}

	public Deque<GameAction> getActionStack() {
		return actionStack;
	}

	public GameContext setActionStack(Deque<GameAction> actionStack) {
		this.actionStack = actionStack;
		return this;
	}

	@Nullable
	public GameAction getCurrentAction() {
		return actionStack.peekLast();
	}

	public void onNotificationWillFire(Notification event) {
	}

	public void onNotificationDidFire(Notification event) {
	}

	public CardCatalogue getCardCatalogue() {
		return cardCatalogue;
	}

    /**
     * Returns a card for the purposes of overriding a card. The {@code CardDesc} on the card has been cloned, in order
     * to allow the overriding card to potentially modify / mutate the {@code CardDesc} without modifying the
     * underlying card catalogue.
     *
     * @param overrideCardId the card ID to override
     * @return a copy of a card where the {@code CardDesc} is copied, cached in temp cards
     */
    public Card getOverriddenCardById(String overrideCardId) {
        Card target = null;
        for (var card : getTempCards()) {
            if (card.getCardId().equals(overrideCardId)) {
                target = card;
                break;
            }
        }

        if (target == null) {
            // creates a clone
            target = getCardById(overrideCardId);
            // deeply clone the card desc
            target.setDesc(target.getDesc().clone());
        }
        return target;
    }
}
