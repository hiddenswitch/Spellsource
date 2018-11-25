package net.demilich.metastone.game.behaviour;

import ch.qos.logback.classic.Level;
import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.Heuristic;
import net.demilich.metastone.game.behaviour.heuristic.ThreatBasedHeuristic;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.GameLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * GameStateValueBehaviour is an implementation of a decent AI with the best-in-class performance among bots in the
 * community.
 * <p>
 * The objective of the bot is to determine the best possible action from the list of actions it can choose from, given
 * the current state of the game. In the AI community, this is called a "policy," and it is typically represented in the
 * literature using the Greek letter Pi.
 * <p>
 * Policies take as inputs a list of actions, like "Friendly hero attacks opposing hero," and the current state of the
 * game, in this case all the data available in {@link GameContext}, and returns an action as an output. {@link
 * #requestAction(GameContext, Player, List)} corresponds to this AI's policy; when implementing an idea, especially
 * from the literature, start with {@link #requestAction(GameContext, Player, List)}.
 * <p>
 * In this AI, {@link #requestAction(GameContext, Player, List)} tries each action available to take and scoring the
 * outcome. The score, in this case, depends on the game state. The action that leads to the highest score is the action
 * returned by {@link #requestAction(GameContext, Player, List)}. Since card games typically involve combos (sequences
 * of actions), this AI will actually play out all available actions until the end of its turn, seeking which starting
 * action will maximize its score at the **end** of its turn.
 * <p>
 * Therefore, the best way to describe this AI is: It is a "single turn horizon" AI. That is, it tries to pick actions
 * to maximize a score by the end of the turn.
 * <p>
 * This means the AI doesn't see some effects that start at the beginning of an opponent's turn, like Doomsayer, or of
 * its turn, like the Cursed card. This class special cases cards like these to maintain its basic architecture.
 * Additionally, playing around secrets is difficult without a long-term vision of the game, so enemy secrets are
 * omitted from the simulation entirely.
 * <p>
 * How does the AI do scoring? Clearly, it can't be as simple as, "The highest score is whatever reduces the opponent's
 * health the most." Indeed, this class uses a complex model for a score, called a {@link Heuristic}, which is capable
 * of looking at any factor in the game state to contribute to the score. The specific heuristic used by this class is
 * the {@link ThreatBasedHeuristic}. Visiting that class, it's clear that things like holding onto hard-removal cards,
 * or fully destroying minions, contribute greatly to the score, along with the heroes' and minions' attack and
 * hitpoints.
 * <p>
 * In order to understand the various tradeoffs between actions, the way the {@link Heuristic} is calculated should
 * somehow reflect the actual ability of these actions to lead to victory. Clearly, the score for an action should, in
 * an ideal world, be as simple as, "Whatever maximizes my chance of winning." But for the time being, answering that
 * question is computationally impossible in this card game. Instead, GameStateValueBehaviour makes the assumption that
 * across many games, maximizing some score at the end of my turn maximizes my chance of winning on average. Hearthstone
 * generally rewards great tactical play, so this is a surprisingly robust assumption. But it isn't necessarily true for
 * many games or for all cards in this game. However, this assumption makes this AI fast, so it is preferred in this
 * context.
 * <p>
 * The {@link ThreatBasedHeuristic} tries to maximize the chance of winning by somehow relating its scoring mechanism to
 * the actual outcome of a match. The <b>Cuckoo</b> application in the cluster package is the system that tweaks the
 * scoring function in order to choose tweaks that corresponded to greater wins in the game. This approach makes
 * GameStateValueBehaviour the best delivered AI in the Hearthstone community.
 *
 * @see #requestAction(GameContext, Player, List) to see how each action of the possible actions is tested for the one
 * 		with the highest score.
 */
public class GameStateValueBehaviour extends IntelligentBehaviour {
	private final Logger logger = LoggerFactory.getLogger(GameStateValueBehaviour.class);

	protected Heuristic heuristic;
	protected FeatureVector featureVector;
	protected String nameSuffix = "";
	protected long timeout = 400;
	protected Deque<GameAction> strictPlan;
	protected Deque<Integer> indexPlan;
	protected int maxDepth = 2;
	protected long minFreeMemory = Long.MAX_VALUE;
	protected boolean disposeNodes = true;
	protected boolean parallel = true;
	protected boolean forceGarbageCollection = false;

	public GameStateValueBehaviour() {
		this(FeatureVector.getFittest(), "Botty McBotface");
	}

	public GameStateValueBehaviour(FeatureVector featureVector, String nameSuffix) {
		this.featureVector = featureVector;
		this.nameSuffix = nameSuffix;
		this.heuristic = new ThreatBasedHeuristic(featureVector);
		if (System.getenv().containsKey("SPELLSOURCE_GSVB_DEPTH")) {
			this.maxDepth = Integer.parseInt(System.getenv("SPELLSOURCE_GSVB_DEPTH"));
		}
		if (System.getenv().containsKey("SPELLSOURCE_GSVB_TIMEOUT_MILLIS")) {
			this.timeout = Long.parseLong(System.getenv("SPELLSOURCE_GSVB_TIMEOUT_MILLIS"));
		}
	}

	/**
	 * Returns a clone of the game context, assuming the opponent is a {@link GameStateValueBehaviour} too.
	 *
	 * @param original The original game context to use.
	 * @return The clone.
	 */
	protected GameContext getClone(GameContext original) {
		GameContext context = original.clone();
		context.setLoggingLevel(Level.ERROR);
		return context;
	}

	@Override
	public Behaviour clone() {
		if (featureVector != null) {
			return new GameStateValueBehaviour(featureVector.clone(), nameSuffix);
		}
		return new GameStateValueBehaviour();
	}

	/**
	 * Indicates the maximum depth of breadth-first-searched nodes that should be expanded in order to find the highest
	 * scoring game state.
	 * <p>
	 * Setting this depth higher exponentially increases the number of nodes that could get visited for evaluating
	 * potential game state scores.
	 * <p>
	 * Setting this depth too low will make the bot miss lethal, especially if it has to use more than {@code maxDepth}
	 * cards or attack with more than {@code maxDepth} minions in order to kill the bot's opponent.
	 * <p>
	 * The default value on the hosted version of Spellsource is {@code 2}. For a good compromise between performance and
	 * finding the most commmon lethals, choose {@code 5}.
	 *
	 * @return The currently configured maximium depth.
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public String getName() {
		return "Game state value " + nameSuffix;
	}

	/**
	 * A strict plan is a cache of a computed path (sequence of actions) to a gamestate stored as the actions themselves.
	 * <p>
	 * Whenever you call {@link #requestAction(GameContext, Player, List)}, the instance of {@link
	 * GameStateValueBehaviour} evaluates sequences of actions of length maximum {@link #getMaxDepth()}, and scores the
	 * value of the <b>last</b> game state (i.e. the game state you arrive at after performing that sequence of actions).
	 * But the {@link #requestAction(GameContext, Player, List)} method returns the <b>first</b> action in that sequence.
	 * <p>
	 * Clearly, the sequence of best actions isn't going to change before and after you take the {@link GameAction} that
	 * was returned by the first call to {@link #requestAction(GameContext, Player, List)}. This {@link Deque} stores the
	 * sequence that was computed as a side effect of {@link #requestAction(GameContext, Player, List)}. With it, the next
	 * call to {@link #requestAction(GameContext, Player, List)} doesn't have to recompute a whole sequence of actions
	 * every time; it can use whatever is left of the sequence of actions that led to the best scoring state.
	 * <p>
	 * Since game states are reproducible, and this behaviour "cheats" (it knows what the random seed is), there should be
	 * an exact match between the {@link Deque#peekFirst()}'d {@link GameAction} in this plan and a game action returned
	 * by {@link GameContext#getValidActions()}  until the plan has been exhausted (i.e. the plan is {@link
	 * Deque#isEmpty()} {@code == true}).
	 * <p>
	 * Because the API of a {@link GameContext} does not guarantee that a {@link GameAction} has no references to the
	 * {@link GameContext} or its objects, this class also implements a {@link #getIndexPlan()}, which uses integers to
	 * represent an index into {@link GameContext#getValidActions()}.
	 * <p>
	 * For example, this code will "follow the plan" that was computed as a side effect of running {@link
	 * #requestAction(GameContext, Player, List)}.
	 * <pre>
	 * {@code
	 * while (!getStrictPlan().isEmpty()) {
	 *   context.performGameAction(playerId, getStrictPlan().pollFirst());
	 * }
	 * }
	 * </pre>
	 *
	 * @return A path of actions (state transitions) towards the highest scoring game state.
	 * @see #getIndexPlan() for an equivalent representation of the path that does not use {@link GameAction} objects.
	 */
	public Deque<GameAction> getStrictPlan() {
		return strictPlan;
	}

	public void setStrictPlan(Deque<GameAction> strictPlan) {
		this.strictPlan = strictPlan;
	}

	/**
	 * Mulligans for cards, preferring to create an on-curve starting hand.
	 *
	 * @param context The game context.
	 * @param player  The player who's mulliganing.
	 * @param cards   The cards in the player's first hand.
	 * @return A list of cards to discard.
	 */
	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() > 3) {
				discardedCards.add(card);
			}
		}

		/*
		ListValuedMap<Integer, Card> cardsAtCost = new ArrayListValuedHashMap<>();
		cards.forEach(card -> cardsAtCost.put(card.getBaseManaCost(), card));

		List<Card> discardedCards = new ArrayList<Card>();
		for (int i = 0; i <= cards.size(); i++) {
			if (cardsAtCost.get(i).size() > 1) {
				discardedCards.add(cardsAtCost.get(i).get(0));
			}
		}
		*/

		return discardedCards;
	}

	/**
	 * Requests an action from the GameStateValueBehaviour using a scoring function. This method uses a cache of what it
	 * has computed before if it is provided with {@link #setIndexPlan(Deque)} or {@link #setStrictPlan(Deque)}.
	 * <p>
	 * Suppose the board looked like this:
	 *
	 * <pre>
	 *     Opponent: Warrior, 7 HP, no cards in hand, no minions on the board.
	 *     This Player: Mage, 30 HP, a Fireball in the hand, 6 mana.
	 * </pre>
	 * <p>
	 * Clearly, this player has lethal: Fireballing the opponent, followed by Fireblasting the opponent, will win the
	 * game. There are two sequences of actions in this case that win the game. How does this function wind up returning
	 * the correct actions twice in order to win the game?
	 * <p>
	 * First, at the beginning of the turn, the request action function receives the above game state, followed by the
	 * possible actions:
	 *
	 * <pre>
	 * 1. Fireball opponent.
	 * 2. Fireball yourself.
	 * 3. Fireblast opponent.
	 * 4. Fireblast yourself.
	 * 5. End the turn.
	 * </pre>
	 * <p>
	 * Suppose we scored each action with a simple function: "1 point if the enemy hero is destroyed, otherwise 0."
	 *
	 * <pre>
	 * 1. Fireball opponent = 0 points.
	 * 2. Fireball yourself = 0 points.
	 * 3. Fireblast opponent = 0 points.
	 * 4. Fireblast yourself = 0 points.
	 * 5. End the turn = 0 points.
	 * </pre>
	 * <p>
	 * By just looking at the current actions, it's impossible to see that fireballing or fireblasting your opponent will
	 * lead to victory, even though the scoring function ought to work fine in this particular case.
	 * <p>
	 * What if instead we chose an action based on the score of the state at the end of the SEQUENCE of actions that
	 * particular action can enable? If we expand all the possible actions given our choices, we get:
	 *
	 * <pre>
	 * 1. Fireball opponent = 0 points.
	 *  1. Fireblast opponent = 1 point.
	 *    **1. End turn = 1 point.**
	 *  2. Fireblast yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 2. Fireball yourself = 0 points.
	 *  1. Fireblast opponent = 0 points.
	 *    1. End turn = 0 points.
	 *  2. Fireblast yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 3. Fireblast opponent = 0 points.
	 *  1. Fireball opponent = 1 point.
	 *    **1. End turn = 1 point.**
	 *  2. Fireball yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 4. Fireblast yourself = 0 points.
	 *  1. Fireball opponent = 0 points.
	 *    1. End turn = 0 points.
	 *  2. Fireball yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 5. End the turn = 0 points.
	 * </pre>
	 * <p>
	 * When expanding all the possible actions, there are now two sequences of actions that end with 1 point.
	 * <p>
	 * This function will return the FIRST action in the sequence that terminates with the highest score at the end of the
	 * turn. In this example, it will return either action 1 (Fireball opponent) or action 3 (Fireblast opponent).
	 * <p>
	 * The scoring function is much more complicated, but in broad strokes it works the way as described above.
	 *
	 * @param context      The game context where the choice is being made.
	 * @param player       The player who is making the choice.
	 * @param validActions The valid actions the player has to choose from.
	 * @return The action that maximizes the score of the state of the game at the end of this player's turn.
	 * @see ThreatBasedHeuristic for an overview of the scoring function.
	 */
	@Override
	@Suspendable
	public @Nullable
	GameAction requestAction(@NotNull GameContext context, @NotNull Player player, @NotNull List<GameAction> validActions) {
		// Isolate this context
		context = context.clone();
		player = context.getPlayer(player.getId());

		// Consistency checks
		String gameId = context.getGameId();
		if (validActions.size() == 0) {
			logger.error("requestAction {} {}: Empty valid actions given", gameId, player);
			return null;
		}

		// First, check if a plan is already cached and ready to be executed
		// The actual process of persisting the plan beyond the lifetime of this GameStateValueBehaviour instance is the
		// responsibility of the caller, and should typically use #getIndexPlan()
		// A strict plan refers to a collection of GameAction objects. A plan is "strictly" followed if the next action
		// proposed in the strict plan is exactly present in the list of valid actions. Otherwise, the index plan is used,
		// where the valid actions are assumed to be in the correct order.
		if (strictPlan != null) {
			if (strictPlan.size() == 0) {
				strictPlan = null;
			} else {
				// Check that the plan action is valid considering these valid actions. If it is, choose it
				GameAction planAction = strictPlan.peekFirst();
				if (validActions.contains(planAction)) {
					logger.debug("requestAction {} {}: Used action from plan with {} actions remaining", gameId, player, strictPlan.size() - 1);
					// Reduce the size of the corresponding index plan too
					if (!indexPlan.isEmpty()) {
						indexPlan.pollFirst();
					}

					final GameAction gameAction = strictPlan.pollFirst();
					if (gameAction instanceof IntermediateAction) {
						// Just choose directly from the valid actions
						return validActions.get(gameAction.getId());
					}
					return gameAction;
				} else {
					// The plan is invalid, set it to null and continue.
					logger.warn("requestAction {} {}: Plan was invalidated, validActions={}, planAction={}", gameId, player, validActions, planAction);
					strictPlan = null;
					indexPlan = null;
				}
			}
		} else if (indexPlan != null) {
			if (indexPlan.size() == 0) {
				indexPlan = null;
			} else {
				// Check that the plan action is valid considering these valid actions. If it is, choose it
				int planAction = indexPlan.peekFirst();
				if (validActions.size() > planAction) {
					logger.debug("requestAction {} {}: Used action from plan with {} actions remaining", gameId, player, indexPlan.size() - 1);
					return validActions.get(indexPlan.pollFirst());
				} else {
					// The plan is invalid, set it to null and continue.
					logger.warn("requestAction {} {}: Plan was invalidated, validActions={}, planAction={}", gameId, player, validActions, planAction);
					indexPlan = null;
				}
			}
		}

		if (validActions.size() == 1) {
			logger.debug("requestAction {} {}: Selecting only action {}", gameId, player, validActions.get(0));
			return validActions.get(0);
		}

		// Depth-first search for the branch which terminates with the highest score, where the DAG has game states as
		// nodes and game actions as edges

		// Max depth indicates that we will expand at most 5 non-intermediate (non-Battlecry and non-Discover) actions
		// away from the game context given to this function.
		int playerId = player.getId();
		Deque<Node> contextStack = new ConcurrentLinkedDeque<>();
		// We're only going to compute scores on the terminal nodes, so we're going to save them separately. Then, we walk
		// the list of predecessors to build a plan.
		List<Node> terminalNodes = new ArrayList<>();

		contextStack.push(new Node(context, null, 0));
		long start = System.currentTimeMillis();
		while (contextStack.size() > 0) {
			traceMemory("node start");
			Node v = contextStack.pop();

			// Is this node terminal?
			if (isTerminal(v, start, playerId)) {
				terminalNodes.add(v);
				continue;
			}

			final int depth = v.depth;

			List<GameAction> edges;
			if (v.predecessor == null) {
				// Initial node
				edges = validActions;
			} else {
				// Expand and compute scores
				edges = v.context.getValidActions();
			}

			if (edges == null || edges.isEmpty()) {
				logger.error("requestAction {} {}: Unexpectedly, an expansion of a game state produced no actions.", gameId, playerId);
				continue;
			}

			// Parallelize the expansion of nodes.
			if (isParallel()) {
				edges
						.parallelStream()
						.unordered()
						.forEach(edge -> rollout(contextStack, playerId, v, edge, depth));
			} else {
				// Non-parallel expansion of nodes
				for (GameAction edge : edges) {
					rollout(contextStack, playerId, v, edge, depth);
				}
			}


			// We've expanded all of this node's edges, we can clear the reference to its game context
			traceMemory("before node dispose");
			if (disposeNodes) {
				v.dispose();
				if (forceGarbageCollection) {
					System.gc();
				}
			}
			traceMemory("after node dispose");
		}

		// Score the terminal nodes, find the highest score
		Optional<Node> maxScore = Optional.empty();

		if (isParallel()) {
			maxScore = terminalNodes
					.parallelStream()
					.peek(bc -> postProcess(playerId, bc.context))
					.peek(bc -> bc.setScore(heuristic.getScore(bc.context, playerId)))
					.peek(Node::dispose)
					.max(Comparator.comparingDouble(Node::getScore));
		} else {
			double score = Double.NEGATIVE_INFINITY;
			for (Node node : terminalNodes) {
				postProcess(playerId, node.context);
				double newScore = heuristic.getScore(node.context, playerId);
				node.setScore(newScore);
				if (disposeNodes) {
					node.dispose();
					if (forceGarbageCollection) {
						System.gc();
					}
				}
				if (newScore > score) {
					maxScore = Optional.of(node);
					score = newScore;
				}
			}
		}
		traceMemory("after scoring terminal nodes");

		if (!maxScore.isPresent()) {
			logger.error("requestAction {} {}: A problem occurred while trying to find the max score in the terminal nodes {}", gameId, player, terminalNodes);
			return null;
		}

		// Save the action plan, iterating backwards from the highest scoring node.
		Deque<GameAction> strictPlan = new ArrayDeque<>();
		Deque<Integer> indexPlan = new ArrayDeque<>();
		Node node = maxScore.get();
		traceMemory("before predecessors");
		while (node != null && node.getPredecessor() != null) {
			for (int i = node.getActions().length - 1; i >= 0; i--) {
				strictPlan.addFirst(node.getActions()[i]);
				indexPlan.addFirst(node.getActionIndices()[i]);
			}
			node = node.getPredecessor();
		}
		traceMemory("after predecessors");

		this.strictPlan = strictPlan;
		this.indexPlan = indexPlan;
		// Pop off the last element of the plan
		this.indexPlan.pollFirst();
		return strictPlan.pollFirst();
	}

	private boolean isTerminal(Node node, long startTime, int playerId) {
		return node.predecessor != null && (
				node.depth >= maxDepth
						|| node.context.updateAndGetGameOver()
						|| (System.currentTimeMillis() - startTime > getTimeout())
						// Technically allows the bot to play through its extra turns
						|| node.context.getActivePlayerId() != playerId);
	}

	/**
	 * Expands the provided game state with the provided action, then appends a new game state with potential actions to
	 * the {@code contextStack}. This expands the game tree by one unit of depth.
	 * <p>
	 * If rolling out the specified action leads to calls to {@link GameLogic#requestAction(Player, List)}, like a
	 * discover or a battlecry request, this method will breadth-first-search those intermediate actions until it gets to
	 * a non-intermediate game state (i.e., one with all of the intermediate action requests answered).
	 * <p>
	 * For example, consider the card: "Choose between two: 'Do nothing', and: 'Choose between do nothing and win the
	 * game.'" The action is to play this card. The following additional combinations of intermediate actions are
	 * created:
	 *
	 * <pre>
	 * 1. Discover and cast do nothing.
	 * 2. Discover and cast 'Choose between do nothing and win the game.'
	 *   1. Discover and cast 'Do nothing.'
	 *   2. Discover and cast 'Win the game.'
	 * </pre>
	 * Clearly, we want the bot to perform the following sequence of actions: Play this card, then make choices #2, #2,
	 * because that will win the game.
	 * <p>
	 * In order to choose that path without emitting intermediate nodes onto the {@code contextStack}, this function
	 * queues these intermediate actions and restarts from the beginning, evaluating a particular sequence it queued.
	 * Eventually, there is a sequence of actions queued that includes "play this card, make choice #2, then make choice
	 * #2," and since that sequence terminates into a non-intermediate game state, that sequence and the resulting game
	 * state are queued as a node onto the {@code contextStack}.
	 * <p>
	 * This optimization only applies to the particular architecture of Spellsource.
	 *
	 * @param contextStack The stack of contexts onto which this function should append rolled-out game states.
	 * @param playerId     The player ID of the player whose point of view we're computing this rollout.
	 * @param node         The node (i.e., game state) from which the specified action should be rolled out.
	 * @param action       The action to roll out.
	 * @param depth        The current depth of this rollout. This is the count of non-intermediate actions from the game
	 *                     state that {@link #requestAction(GameContext, Player, List)} was called with.
	 */
	@Suspendable
	protected void rollout(Deque<Node> contextStack, int playerId, Node node, GameAction action, int depth) {
		// Clone out the context because we're not going to mutate the node's context.
		GameContext mutateContext = getClone(node.context);

		preProcess(playerId, mutateContext);

		// Start: Infrastructure to support intermediate called to requestAction that come as a consequence of calling
		// action.
		Deque<IntermediateNode> intermediateNodes = new ArrayDeque<>();
		AtomicBoolean guard = new AtomicBoolean();

		mutateContext.setBehaviour(playerId, new RequestActionFunction((context1, player1, validActions1) -> {
			// This is a guard function that detects if intermediate game actions, like discovers or battlecries, are created
			// while processing the edge we got from the parameters of the expandAndAppend call. If we reach this code, we
			// have to process intermediate nodes separately. We'll queue the first batch here, and then throw away the result
			// of the actual action we choose. We must use the guard, because a single performGameAction could call this
			// RequestActionFunction multiple times, but we only want to queue up the first intermediate actions.
			if (guard.compareAndSet(false, true)) {
				for (int i = 0; i < validActions1.size(); i++) {
					intermediateNodes.add(new IntermediateNode(i));
				}
			}

			// Will now mutate the context in an unneeded branch.
			return validActions1.get(0);
		}));

		// Perform action
		try {
			mutateContext.getLogic().performGameAction(playerId, action);
		} catch (Throwable simulationError) {
			logger.error("requestAction (unknown) {}: There was a simulation error for action {}: {}", playerId, action, simulationError);
			// Do not queue a busted node onto the contextStack
			return;
		}

		// Check if there are intermediates pending
		if (intermediateNodes.isEmpty()) {
			// Push the new node
			contextStack.add(new Node(mutateContext, node, depth + 1, action));
			return;
		}

		// The intermediate node processing branch
		while (intermediateNodes.size() > 0) {
			IntermediateNode intermediateNode = intermediateNodes.pollFirst();
			if (intermediateNode == null) {
				throw new UnsupportedOperationException("Should not queue null nodes.");
			}

			// Process each intermediate, which may queue more of them. Create a request action function that returns the
			// specified intermediate game action and also queues more intermediates if they are made.
			GameContext intermediateMutateContext = getClone(node.context);
			preProcess(playerId, intermediateMutateContext);

			int queueSize = intermediateNodes.size();
			int[] choices = intermediateNode.choices;
			AtomicInteger counter = new AtomicInteger(0);
			AtomicBoolean intermediateGuard = new AtomicBoolean();
			intermediateMutateContext.setBehaviour(playerId, new RequestActionFunction((context, player, validActions) -> {
				// Make choices until we've exhausted the actions that were specified by this intermediate node.
				int choiceIndex = counter.getAndIncrement();
				if (choiceIndex >= choices.length) {
					// We are queuing more intermediate nodes, mark this intermediate node as having queued more intermediates and
					// this evaluation as having expanded.
					if (intermediateGuard.compareAndSet(false, true)) {
						for (int i = 0; i < validActions.size(); i++) {
							int[] newChoices = Arrays.copyOf(choices, choices.length + 1);
							newChoices[newChoices.length - 1] = i;
							intermediateNodes.add(new IntermediateNode(newChoices));
						}
					}
					// We can throw this route away.
					return validActions.get(0);
				} else {
					return validActions.get(choices[choiceIndex]);
				}
			}));

			try {
				intermediateMutateContext.getLogic().performGameAction(playerId, action);
			} catch (Throwable simulationError) {
				logger.error("requestAction (unknown) {}: There was a simulation error for action {} when considering intermediates {}: {}", playerId, action, choices, simulationError);
				continue;
			}
			// Check if processing this intermediate queued more intermediates.
			if (intermediateNodes.size() > queueSize) {
				// We can toss this result away, we'll have to process again.
				continue;
			}
			// If it didn't, then the intermediate is the last intermediate on a path from real node to real node. Queue a
			// real node onto the context stack. Reconstruct the path by following the predecessors of the intermediates until
			// we reach a real node.
			GameAction[] actions = new GameAction[1 + choices.length];
			actions[0] = action;
			for (int i = 0; i < choices.length; i++) {
				actions[i + 1] = new IntermediateAction(choices[i]);
			}
			contextStack.add(new Node(intermediateMutateContext, node, depth + 1, actions));
		}
	}

	/**
	 * Pre-processes a game state before running a simulation.
	 *
	 * @param playerId
	 * @param thisContext
	 */
	private static void preProcess(int playerId, GameContext thisContext) {
		// Preprocess: Don't simulate the opposing player's secrets
		Player opponent = thisContext.getOpponent(thisContext.getPlayer(playerId));
		thisContext.getLogic().removeSecrets(opponent);
	}

	/**
	 * Post-processes a game state for scoring.
	 *
	 * @param playerId
	 * @param thisContext
	 */
	protected void postProcess(int playerId, GameContext thisContext) {
		// If a Doomsayer is on the board (i.e., not destroyed), don't count friendly minions
		if (thisContext.getPlayers().stream().flatMap(p -> p.getMinions().stream()).anyMatch(c -> c.getSourceCard().getCardId().equals("minion_doomsayer"))) {
			thisContext.getPlayer(playerId).getMinions().forEach(m -> thisContext.getLogic().markAsDestroyed(m));
			thisContext.getLogic().endOfSequence();
		}
	}

	public void setIndexPlan(Deque<Integer> indexPlan) {
		this.indexPlan = indexPlan;
	}

	/**
	 * The index plan is a sequence of indices into {@link GameContext#getValidActions()} that the bot can perform to go
	 * towards a previously-computed highest-scoring game state. It is essentially a cache of a prior computation of the
	 * best possible {@link #getMaxDepth()} number of actions.
	 * <p>
	 * For example, this code will "follow the plan" that was computed as a side effect of running {@link
	 * #requestAction(GameContext, Player, List)}.
	 * <pre>
	 * {@code
	 * while (!getIndexPlan().isEmpty()) {
	 *   context.performGameAction(playerId, context.getValidActions().get(getIndexPlan().pollFirst());
	 * }
	 * }
	 * </pre>
	 *
	 * @return Indices into {@link GameContext#getValidActions()}
	 * @see #getStrictPlan() for an explanation of how this cache works.
	 */
	public Deque<Integer> getIndexPlan() {
		return indexPlan;
	}

	public long getTimeout() {
		return timeout;
	}

	public boolean isDisposeNodes() {
		return disposeNodes;
	}

	public GameStateValueBehaviour setDisposeNodes(boolean disposeNodes) {
		this.disposeNodes = disposeNodes;
		return this;
	}

	public boolean isForceGarbageCollection() {
		return forceGarbageCollection;
	}

	public GameStateValueBehaviour setForceGarbageCollection(boolean forceGarbageCollection) {
		this.forceGarbageCollection = forceGarbageCollection;
		return this;
	}

	public GameStateValueBehaviour setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	public boolean isParallel() {
		return parallel;
	}

	public GameStateValueBehaviour setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}

	/**
	 * Traces memory usage by logging whenever the maximum amount of memory has been reached and where
	 */
	private void traceMemory(String location) {
		long currentMemoryUsage = Runtime.getRuntime().freeMemory();
		if (currentMemoryUsage < minFreeMemory) {
			if (logger.isTraceEnabled()) {
				logger.trace("traceMemory {}: Free memory decreased from {} MB to {} MB", location, minFreeMemory / (1024 * 1024), currentMemoryUsage / (1024 * 1024));
			}
			minFreeMemory = currentMemoryUsage;
		}
	}

	public long getMinFreeMemory() {
		return minFreeMemory;
	}

	/**
	 * This helper class represents an action with an index. Useful for referencing discover and battlecry actions without
	 * holding onto references to state like cards and spells.
	 */
	static class IntermediateAction extends GameAction implements Serializable {

		public IntermediateAction(int index) {
			this.setId(index);
		}

		@Override
		public void execute(GameContext context, int playerId) {
			throw new UnsupportedOperationException("This is an internal game action used by the game state value behaviour.");
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof GameAction)) {
				return false;
			}

			GameAction rhs = (GameAction) obj;
			return rhs.getId() == this.getId();
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(getId());
		}
	}

	/**
	 * This helper class stores a list of choices from an intermediate node expansion.
	 */
	static class IntermediateNode {
		final int choices[];

		IntermediateNode(int... choices) {
			this.choices = choices;
		}
	}

	/**
	 * This helper class represents a node in a rollout. The actions field stores which sequence of actions, relative to
	 * the predecessor, led to this node.
	 */
	static class Node {
		private GameContext context;
		private final int depth;
		private final Node predecessor;
		private final GameAction[] actions;
		private final int[] actionIndices;
		private double score;

		Node(GameContext context, Node predecessor, int depth, GameAction... actions) {
			this.context = context;
			this.predecessor = predecessor;
			this.actions = actions;
			this.depth = depth;
			if (actions != null && actions.length > 0) {
				actionIndices = Stream.of(actions).mapToInt(GameAction::getId).toArray();
			} else {
				actionIndices = new int[0];
			}
		}

		public Node getPredecessor() {
			return predecessor;
		}

		public GameAction[] getActions() {
			return actions;
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		public int[] getActionIndices() {
			return actionIndices;
		}

		public void dispose() {
			if (context == null) {
				throw new UnsupportedOperationException("Already cleared");
			}
			context = null;
		}
	}
}
