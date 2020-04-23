package net.demilich.metastone.game.behaviour;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.common.Tracing;
import io.opentracing.util.GlobalTracer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.Heuristic;
import net.demilich.metastone.game.behaviour.heuristic.ThreatBasedHeuristic;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.TurnState;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.*;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
 * Playing around secrets is difficult without a long-term vision of the game, so enemy secrets are omitted from the
 * simulation entirely. The bot's and opponennt's start turn effects are heuristically triggered at the end of the
 * turn.
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
 * with the highest score.
 */
public class GameStateValueBehaviour extends IntelligentBehaviour {
	public static final int DEFAULT_TARGET_CONTEXT_STACK_SIZE = 2 * 7 * 6 - 1;
	public static final int DEFAULT_MAXIMUM_DEPTH = 3;
	public static final int DEFAULT_TIMEOUT = 11800;
	public static final int DEFAULT_LETHAL_TIMEOUT = 15000;
	private final static Logger LOGGER = LoggerFactory.getLogger(GameStateValueBehaviour.class);

	protected Heuristic heuristic;
	protected FeatureVector featureVector;
	protected String nameSuffix = "";
	protected long timeout = DEFAULT_TIMEOUT;
	protected Deque<GameAction> strictPlan;
	protected Deque<Integer> indexPlan;
	protected int maxDepth = DEFAULT_MAXIMUM_DEPTH;
	protected long minFreeMemory = Long.MAX_VALUE;
	protected boolean disposeNodes = true;
	protected boolean parallel = false;
	protected boolean forceGarbageCollection = false;
	protected boolean throwOnInvalidPlan = false;
	protected boolean pruneContextStack = true;
	protected boolean throwsExceptions = true;
	protected boolean expandDepthForLethal = true;
	protected boolean triggerStartTurns = true;
	protected boolean pruneEarlyEndTurn = false;
	protected long lethalTimeout = DEFAULT_LETHAL_TIMEOUT;
	protected int targetContextStackSize = DEFAULT_TARGET_CONTEXT_STACK_SIZE;
	protected long requestActionStartTime = Long.MAX_VALUE;

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
		var context = original.clone();
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

	public GameStateValueBehaviour setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
		return this;
	}

	/**
	 * Indicates this game state value behaviour should throw exceptions when its underlying assumptions about the
	 * mechanics of the game are violated. For example, this will cause the GSVB to throw an exception if it is requested
	 * to evaluate discover actions directly.
	 *
	 * @return {@code true} if operating in debug mode.
	 */
	public boolean throwsExceptions() {
		return throwsExceptions;
	}

	public GameStateValueBehaviour setThrowsExceptions(boolean throwsExceptions) {
		this.throwsExceptions = throwsExceptions;
		return this;
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
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return super.mulligan(context, player, cards);
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
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("GameStateValueBehaviour/requestAction")
				.withTag("gameId", context.getGameId())
				.withTag("deckId", (String) player.getAttributes().get(Attribute.DECK_ID))
				.start();
		// Isolate this context
		context = context.clone();
		player = context.getPlayer(player.getId());

		var oldMaxDepth = getMaxDepth();
		var oldTimeout = getTimeout();
		Optional<Node> maxScore = Optional.empty();

		Deque<Node> contextStack;
		if (isParallel()) {
			contextStack = new ConcurrentLinkedDeque<>();
		} else {
			contextStack = new ArrayDeque<>(getTargetContextStackSize() * getMaxDepth());
		}

		try (var s1 = tracer.activateSpan(span)) {
			// Consistency checks
			var gameId = context.getGameId();
			if (validActions.size() == 0) {
				LOGGER.error("requestAction {} {}: Empty valid actions given", gameId, player);
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
					var planAction = strictPlan.peekFirst();
					if (validActions.contains(planAction)) {
						LOGGER.debug("requestAction {} {}: Used action from plan with {} actions remaining", gameId, player, strictPlan.size() - 1);
						// Reduce the size of the corresponding index plan too
						if (!indexPlan.isEmpty()) {
							indexPlan.pollFirst();
						}

						final var gameAction = strictPlan.pollFirst();
						if (gameAction instanceof IntermediateAction) {
							// Just choose directly from the valid actions
							return validActions.get(gameAction.getId());
						}
						return gameAction;
					} else {
						// The plan is invalid, set it to null and continue.
						if (throwOnInvalidPlan) {
							throw new IllegalStateException("invalid plan");
						} else {
							LOGGER.warn("requestAction {} {}: Plan was invalidated, validActions={}, planAction={}", gameId, player, validActions, planAction);
						}
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
						LOGGER.debug("requestAction {} {}: Used action from plan with {} actions remaining", gameId, player, indexPlan.size() - 1);
						return validActions.get(indexPlan.pollFirst());
					} else {
						// The plan is invalid, set it to null and continue.
						LOGGER.warn("requestAction {} {}: Plan was invalidated, validActions={}, planAction={}", gameId, player, validActions, planAction);
						indexPlan = null;
					}
				}
			}

			if (validActions.size() == 1) {
				LOGGER.debug("requestAction {} {}: Selecting only action {}", gameId, player, validActions.get(0));
				return validActions.get(0);
			}

			// If the game state value behaviour got this far and has all discover actions, that means it is receiving a
			// discover action it could not have evaluated in the context of intermediate nodes. This typically happens when
			// gameplay causes a discover on a trigger, like a "Start of Game: Choose a new starting hero power."
			if (validActions.stream().allMatch(a -> a.getActionType() == ActionType.DISCOVER || a.getActionType() == ActionType.BATTLECRY)) {
				// We're going to choose an action at random at this point.
				final var finalContext = context;
				LOGGER.error("requestAction {} {}: Plan did not have answer to actions that were all discovers or battlecries. " +
								"Sources were: {}", gameId, player,
						validActions.stream().
								map(ga -> ga.getSource(finalContext))
								.filter(Objects::nonNull)
								.map(Entity::getSourceCard)
								.map(Card::getCardId)
								.collect(toList()));
				throw new UnsupportedOperationException();
			}

			// Depth-first search for the branch which terminates with the highest score, where the DAG has game states as
			// nodes and game actions as edges

			// Max depth indicates that we will expand at most MAX_DEPTH non-intermediate (non-Battlecry and non-Discover)
			// actions away from the game context given to this function. If we have lethal on the board and we're configured
			// to do so, we should temporarily expand the max depth to accommodate the number of cards we can play and actors
			// that can attack we have.

			if (isExpandDepthForLethal()
					&& observesLethal(context, player.getId(), context.getOpponent(player).getHero())) {
				var newMaxDepth = 0;
				Actor actors[] = new Actor[player.getMinions().size() + 1];
				player.getMinions().toArray(actors);
				actors[actors.length - 1] = player.getHero();
				for (var i = 0; i < actors.length; i++) {
					var actor = actors[i];
					var attacks = actor.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + actor.getAttributeValue(Attribute.EXTRA_ATTACKS);
					if (attacks > 0 && actor.canAttackThisTurn(context)) {
						newMaxDepth += attacks;
					}
				}
				var cards = new Card[player.getHand().size() + 1];
				player.getHand().toArray(cards);
				cards[cards.length - 1] = player.getHeroPowerZone().get(0);
				for (var i = 0; i < cards.length; i++) {
					var card = cards[i];
					if (context.getLogic().canPlayCard(player, card)) {
						newMaxDepth += 1;
					}
				}
				setMaxDepth(newMaxDepth);
				setTimeout(lethalTimeout);
			}

			// Now we will actually start expanding game states
			var playerId = player.getId();

			// Immediately score terminal nodes to save memory.
			var score = Double.NEGATIVE_INFINITY;

			contextStack.push(new Node(context, null, 0));
			setRequestActionStartTime(System.currentTimeMillis());

			// Depth-first search loop with a twist.
			// We will expand the longest nodes first. However, nodes that are terminal go to the end of the context stack,
			// instead of the beginning, where they are popped first. Our heuristic is to prune all but the longest terminal
			// nodes in order to save memory.

			while (contextStack.size() > 0) {
				traceMemory("node start");
				var v = contextStack.pop();

				// Is this node terminal?
				if (isTerminal(v, playerId)) {
					postProcess(playerId, v.context);
					var newScore = heuristic.getScore(v.context, playerId);
					v.setScore(newScore);
					if (disposeNodes) {
						v.dispose();
						if (forceGarbageCollection) {
							System.gc();
						}
					}
					if (newScore > score) {
						maxScore = Optional.of(v);
						score = newScore;
					}
					// If we found lethal, we can terminate immediately
					if (score == Double.POSITIVE_INFINITY) {
						break;
					} else {
						continue;
					}
				}

				// If we've been interrupted, peacefully exit this intense part of the code
				if (isInterrupted()) {
					break;
				}

				// Prune after we've scored, so that we don't accidentally prune a lethal node
				pruneContextStack(contextStack, playerId);

				final var depth = v.depth;

				List<GameAction> edges;
				if (v.predecessor == null) {
					// Initial node
					edges = validActions;
				} else {
					// Expand and compute scores
					edges = v.context.getValidActions();
				}

				if (edges.isEmpty()) {
					continue;
				}

				// Don't prune end turns if there are start or end turn triggers in play, because it may be significant to keep
				// them around to get their effects.
				if (isPruneEarlyEndTurn()
						&& edges.size() > 1
						&& context.getTriggers()
						.stream().flatMap(t -> t instanceof Enchantment ? ((Enchantment) t).getTriggers().stream() : Stream.empty()).noneMatch(t -> t instanceof TurnTrigger)) {
					edges.removeIf(ga -> ga.getActionType() == ActionType.END_TURN);
				}

				// Parallelize the expansion of nodes.
				if (isParallel()) {
					edges
							.parallelStream()
							.unordered()
							.forEach(edge -> evaluate(contextStack, playerId, v, edge, depth));
				} else {
					// Non-parallel expansion of nodes
					for (var edge : edges) {
						evaluate(contextStack, playerId, v, edge, depth);
					}
				}


				// We've expanded all of this node's edges, we can clear the reference to its game context
				traceMemory("before node dispose");
				if (isDisposeNodes()) {
					v.dispose();
					if (isForceGarbageCollection()) {
						System.gc();
					}
				}
				traceMemory("after node dispose");
			}

			if (maxScore.isEmpty()) {
				throw new NullPointerException("maxScore");
			}

			// Save the action plan, iterating backwards from the highest scoring node.
			GameAction gameAction = savePlan(maxScore);
			if (gameAction == null) {
				LOGGER.error("requestAction {} {}: A problem occurred while polling the strict plan, returning the first action.", gameId, player);
				throw new NullPointerException("strict plan was empty");
			}

			return gameAction;
		} catch (Throwable throwable) {
			if (context.getTrace() != null) {
				span.setTag("trace", context.getTrace().dump());
			}
			span.setTag("contextSizeSize", contextStack.size());
			Tracing.error(throwable, span, true);
			if (throwsExceptions()) {
				throw throwable;
			}
			return exceptionActionChoice(maxScore, validActions);
		} finally {
			setTimeout(oldTimeout);
			setMaxDepth(oldMaxDepth);
			for (var node : contextStack) {
				node.dispose();
			}
			span.finish();
		}
	}

	/**
	 * Saves the plan and retrieves the game action from the max score
	 *
	 * @param maxScore
	 * @return
	 */
	@Nullable
	protected GameAction savePlan(Optional<Node> maxScore) {
		Deque<GameAction> strictPlan = new ArrayDeque<>();
		Deque<Integer> indexPlan = new ArrayDeque<>();
		var node = maxScore.get();
		traceMemory("before predecessors");
		while (node != null && node.getPredecessor() != null) {
			for (var i = node.getActions().length - 1; i >= 0; i--) {
				strictPlan.addFirst(node.getActions()[i]);
				indexPlan.addFirst(node.getActionIndices()[i]);
			}
			node = node.getPredecessor();
		}
		traceMemory("after predecessors");

		this.strictPlan = strictPlan;
		this.indexPlan = indexPlan;
		// Pop off the first element of the plan
		this.indexPlan.pollFirst();
		return strictPlan.pollFirst();
	}

	/**
	 * Checks if the bot has timed out or if the thread it is executing on is interrupted.
	 *
	 * @return
	 */
	protected boolean isInterrupted() {
		return Strand.currentStrand().isInterrupted() || (System.currentTimeMillis() - getRequestActionStartTime() > getTimeout());
	}

	/**
	 * Gets the best scoring action, the end turn action or the first action in the list.
	 *
	 * @param maxScoreSoFar
	 * @param gameActions
	 * @return
	 */
	protected GameAction exceptionActionChoice(Optional<Node> maxScoreSoFar, @NotNull List<GameAction> gameActions) {
		Node first = null;
		if (maxScoreSoFar.isPresent()) {
			first = maxScoreSoFar.get();
			while (first.getPredecessor() != null) {
				first = first.getPredecessor();
			}

		}
		if (first == null || first.getActions() == null || first.getActions().length == 0) {
			return gameActions.stream().filter(ga -> ga != null && ga.getActionType() == ActionType.END_TURN)
					.findFirst().orElse(gameActions.get(0));
		}
		return first.getActions()[0];
	}

	/**
	 * Prunes the context stack to save memory. Removes terminal nodes that are not worth exploring heuristically.
	 *
	 * @param contextStack
	 * @param playerId
	 */
	protected void pruneContextStack(Deque<Node> contextStack, int playerId) {
		if (!isPruneContextStack()) {
			return;
		}

		if (contextStack.size() > getTargetContextStackSize()) {
			// Remove all terminating actions except the longest one. Ensures that if we've encountered a terminal node, it
			// won't get pruned off by accident.
			var iterator = contextStack.descendingIterator();
			Node longestNode = null;
			var longestNodeLength = Integer.MIN_VALUE;
			while (iterator.hasNext()) {
				var node = iterator.next();
				if (isTerminal(node, playerId)) {
					if (node.depth > longestNodeLength) {
						longestNode = node;
						longestNodeLength = node.getActions().length;
					}
				} else {
					// We always queue end turn actions at the end
					break;
				}
			}

			iterator = contextStack.descendingIterator();
			if (longestNode != null) {
				while (iterator.hasNext()) {
					var node = iterator.next();
					if (node == longestNode) {
						break;
					} else {
						iterator.remove();
						if (isDisposeNodes()) {
							node.dispose();
						}
					}
				}
			}

			// Continues to remove all but the longest nodes
			while (iterator.hasNext()) {
				if (contextStack.size() <= getTargetContextStackSize()) {
					break;
				}
				var node = iterator.next();
				iterator.remove();
				if (isDisposeNodes()) {
					node.dispose();
				}
			}
		}
	}

	private boolean isTerminal(Node node, int playerId) {
		return node.predecessor != null && (
				node.depth >= getMaxDepth()
						|| node.context.updateAndGetGameOver()
						|| isInterrupted()
						// Technically allows the bot to play through its extra turns
						|| node.context.getActivePlayerId() != playerId);
	}

	/**
	 * Evaluates the provided game state with the provided action, then appends a new game state with potential actions to
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
	protected void evaluate(Deque<Node> contextStack, int playerId, Node node, GameAction action, int depth) {
		// Clone out the context because we're not going to mutate the node's context.
		var mutateContext = getClone(node.context);

		preProcess(playerId, mutateContext);

		// Start: Infrastructure to support intermediate called to requestAction that come as a consequence of calling
		// action.
		Deque<IntermediateNode> intermediateNodes = new ArrayDeque<>();
		var guard = new AtomicBoolean();

		mutateContext.setBehaviour(playerId, new RequestActionFunction((context1, player1, validActions1) -> {
			if (isInterrupted() || intermediateNodes.size() > getMaxDepth()) {
				intermediateNodes.clear();
				throw new RuntimeException("interrupted");
			}

			// This is a guard function that detects if intermediate game actions, like discovers or battlecries, are created
			// while processing the edge we got from the parameters of the expandAndAppend call. If we reach this code, we
			// have to process intermediate nodes separately. We'll queue the first batch here, and then throw away the result
			// of the actual action we choose. We must use the guard, because a single performGameAction could call this
			// RequestActionFunction multiple times, but we only want to queue up the first intermediate actions.
			if (guard.compareAndSet(false, true)) {
				for (var i = 0; i < validActions1.size(); i++) {
					intermediateNodes.add(new IntermediateNode(i));
				}
			}

			// Will now mutate the context in an unneeded branch.
			return validActions1.get(0);
		}));

		if (isInterrupted()) {
			// Bail out here if possible, does not queue new nodes.
			return;
		}

		mutateContext.performAction(playerId, action);

		// Check if there are intermediates pending
		if (intermediateNodes.isEmpty()) {
			var computeAction = new Node(mutateContext, node, depth + 1, action);
			// Push the new node
			if (action.getActionType() == ActionType.END_TURN) {
				contextStack.addLast(computeAction);
			} else {
				// Depth first!
				contextStack.addFirst(computeAction);
			}
			return;
		}

		// The intermediate node processing branch
		while (intermediateNodes.size() > 0) {
			if (isInterrupted()) {
				return;
			}

			var intermediateNode = intermediateNodes.pollFirst();
			if (intermediateNode == null) {
				throw new UnsupportedOperationException("should not queue null nodes.");
			}

			// Process each intermediate, which may queue more of them. Create a request action function that returns the
			// specified intermediate game action and also queues more intermediates if they are made.
			var intermediateMutateContext = getClone(node.context);
			preProcess(playerId, intermediateMutateContext);

			var queueSize = intermediateNodes.size();
			var choices = intermediateNode.choices;
			var counter = new AtomicInteger(0);
			var intermediateGuard = new AtomicBoolean();
			intermediateMutateContext.setBehaviour(playerId, new RequestActionFunction((context, player, validActions) -> {
				if (isInterrupted()) {
					intermediateNodes.clear();
					throw new RuntimeException("interrupted");
				}

				// Make choices until we've exhausted the actions that were specified by this intermediate node.
				var choiceIndex = counter.getAndIncrement();
				if (choiceIndex >= choices.length) {
					// We are queuing more intermediate nodes, mark this intermediate node as having queued more intermediates and
					// this evaluation as having expanded.
					if (intermediateGuard.compareAndSet(false, true)) {
						for (var i = 0; i < validActions.size(); i++) {
							var newChoices = Arrays.copyOf(choices, choices.length + 1);
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

			if (isInterrupted()) {
				return;
			}

			intermediateMutateContext.performAction(playerId, action);

			if (isInterrupted()) {
				return;
			}

			// Check if processing this intermediate queued more intermediates.
			if (intermediateNodes.size() > queueSize) {
				// We can toss this result away, we'll have to process again.
				continue;
			}
			// If it didn't, then the intermediate is the last intermediate on a path from real node to real node. Queue a
			// real node onto the context stack. Reconstruct the path by following the predecessors of the intermediates until
			// we reach a real node.

			var actions = new GameAction[1 + choices.length];
			actions[0] = action;
			for (var i = 0; i < choices.length; i++) {
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
		var opponent = thisContext.getOpponent(thisContext.getPlayer(playerId));
		thisContext.getLogic().removeSecrets(opponent);
	}

	/**
	 * Post-processes a game state for scoring.
	 * <p>
	 * Currently, this method triggers turn start effects on both sides of the battlefield.
	 *
	 * @param playerId
	 * @param context
	 */
	protected void postProcess(int playerId, GameContext context) {
		if (isTriggerStartTurns()
				&& !context.updateAndGetGameOver()
				&& context.getTurnState() == TurnState.TURN_ENDED) {
			var player = context.getPlayer(playerId);
			var opponent = context.getOpponent(player);

			// Make sure that friendly start turns don't accidentally wind up killing the opponent
			var opponentHp = opponent.getHero().getHp();
			for (var trigger : new ArrayList<>(context.getTriggers())) {
				if (trigger instanceof Enchantment && !(trigger instanceof Aura) && !trigger.isExpired()) {
					var enchantment = (Enchantment) trigger;
					if (enchantment.getTriggers().stream().anyMatch(e -> e.getClass().equals(TurnStartTrigger.class)
							|| (e.getClass().equals(TurnEndTrigger.class) && enchantment.getOwner() == opponent.getId()))) {
						// Correctly set the trigger stacks
						context.getTriggerHostStack().push(trigger.getHostReference());
						context.getLogic().castSpell(trigger.getOwner(), enchantment.getSpell(), trigger.getHostReference(), EntityReference.NONE, TargetSelection.NONE, true, null);
						context.getTriggerHostStack().pop();
					}
				}
			}

			// If a turn start trigger killed the opponent, it probably should not have had, and should not count as a
			// game-ending effect.
			if (opponent.getHero().getHp() <= 0) {
				opponent.getHero().setHp(opponentHp);
			}

			if (opponent.getHero().isDestroyed()) {
				opponent.getHero().getAttributes().remove(Attribute.DESTROYED);
			}
			context.getLogic().endOfSequence();
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

	/**
	 * Just below the maximum amount of time in milliseconds the bot will spend <b>per call</b> to {@link
	 * #requestAction(GameContext, Player, List)} to determine its sequence of actions. This time tends to be amortized
	 * over {@link #getMaxDepth()}-length action sequences, because this behaviour caches the calculation of a complete
	 * sequence.
	 *
	 * @return The time to spend per request, in milliseconds
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * Indicates whether or not nodes should be "disposed" (their game context references set to {@code null}.
	 * <p>
	 * On some JVMs, this may help the garbage collector find finished game states faster.
	 *
	 * @return
	 */
	public boolean isDisposeNodes() {
		return disposeNodes;
	}

	public GameStateValueBehaviour setDisposeNodes(boolean disposeNodes) {
		this.disposeNodes = disposeNodes;
		return this;
	}

	/**
	 * Indicates whether or not a garbage collection call via {@link System#gc()} should be made whenever a game context
	 * is done being processed.
	 * <p>
	 * This will slow the bot down but may reduce overall heap usage.
	 *
	 * @return
	 */
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

	/**
	 * Indicates whether or not the bot should process / expand nodes in its game state tree expansion using multiple
	 * threads.
	 * <p>
	 * When using {@link GameContext#simulate(List, Supplier, Supplier, int, boolean, AtomicInteger)}, typically this
	 * should be set to {@code false} whenever {@code useJavaParallel} is {@code true}.
	 *
	 * @return
	 */
	public boolean isParallel() {
		return parallel;
	}

	public GameStateValueBehaviour setParallel(boolean parallel) {
		this.parallel = parallel;
		return this;
	}

	/**
	 * Throws an exception if an invalid plan is encountered.
	 * <p>
	 * When errors occur during replaying existing plans, there might be a game reproducibility issue where the same exact
	 * seed and sequence of actions did not produce the same exact results; or, there is an issue cloning game states.
	 *
	 * @return
	 */
	public boolean isThrowOnInvalidPlan() {
		return throwOnInvalidPlan;
	}

	public GameStateValueBehaviour setThrowOnInvalidPlan(boolean throwOnInvalidPlan) {
		this.throwOnInvalidPlan = throwOnInvalidPlan;
		return this;
	}

	/**
	 * Indicates the amount of time available to this instance to find lethal, when lethal is probably on the board.
	 *
	 * @return
	 */
	public long getLethalTimeout() {
		return lethalTimeout;
	}

	public GameStateValueBehaviour setLethalTimeout(long lethalTimeout) {
		this.lethalTimeout = lethalTimeout;
		return this;
	}

	/**
	 * Indicates whether or not this class should make attempts to prune the "context stack," or game states left to
	 * expand, in order to save memory.
	 * <p>
	 * The pruning strategies may change.
	 *
	 * @return
	 */
	public boolean isPruneContextStack() {
		return pruneContextStack;
	}

	public GameStateValueBehaviour setPruneContextStack(boolean pruneContextStack) {
		this.pruneContextStack = pruneContextStack;
		return this;
	}

	/**
	 * When pruning with {@link #isPruneContextStack()}, sets the maximum size of the context stack (the number of game
	 * states left to expand).
	 *
	 * @return
	 */
	public int getTargetContextStackSize() {
		return targetContextStackSize;
	}

	public GameStateValueBehaviour setTargetContextStackSize(int targetContextStackSize) {
		this.targetContextStackSize = targetContextStackSize;
		return this;
	}

	/**
	 * Traces memory usage by logging whenever the maximum amount of memory has been reached and where
	 */
	private void traceMemory(String location) {
		var currentMemoryUsage = Runtime.getRuntime().freeMemory();
		if (currentMemoryUsage < minFreeMemory) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("traceMemory {}: Free memory decreased from {} MB to {} MB", location, minFreeMemory / (1024 * 1024), currentMemoryUsage / (1024 * 1024));
			}
			minFreeMemory = currentMemoryUsage;
		}
	}

	/**
	 * Gets the minimum observed free memory recorded during the execution of this instance.
	 *
	 * @return
	 */
	public long getMinFreeMemory() {
		return minFreeMemory;
	}

	/**
	 * Records a call to {@link System#currentTimeMillis()} at the start of a call to {@link
	 * GameStateValueBehaviour#requestAction(GameContext, Player, List)}.
	 *
	 * @return
	 */
	protected long getRequestActionStartTime() {
		return requestActionStartTime;
	}

	protected GameStateValueBehaviour setRequestActionStartTime(long requestActionStartTime) {
		this.requestActionStartTime = requestActionStartTime;
		return this;
	}

	/**
	 * Indicates whether the {@link #getMaxDepth()} setting should be temporarily expanded to the number of actors and
	 * playable cards + 1 (the end turn action) in order to help the system find a way to lethally destroy a target.
	 *
	 * @return
	 */
	public boolean isExpandDepthForLethal() {
		return expandDepthForLethal;
	}

	public GameStateValueBehaviour setExpandDepthForLethal(boolean expandDepthForLethal) {
		this.expandDepthForLethal = expandDepthForLethal;
		return this;
	}

	/**
	 * Indicates if start turn effects should be evaluated at the end of the bot's turn.
	 *
	 * @return
	 */
	public boolean isTriggerStartTurns() {
		return triggerStartTurns;
	}

	public GameStateValueBehaviour setTriggerStartTurns(boolean triggerStartTurns) {
		this.triggerStartTurns = triggerStartTurns;
		return this;
	}

	/**
	 * Indicates if end turns should only be evaluated if they are the only action available
	 *
	 * @return
	 */
	public boolean isPruneEarlyEndTurn() {
		return pruneEarlyEndTurn;
	}

	public GameStateValueBehaviour setPruneEarlyEndTurn(boolean pruneEarlyEndTurn) {
		this.pruneEarlyEndTurn = pruneEarlyEndTurn;
		return this;
	}

	/*
	 * Gets the number of lowest-scoring nodes that should be trimmed from the evaluation stack.
	 *
	 * @return
	public int getNumberOfLowestScoringNodesToPrune() {
		return numberOfLowestScoringNodesToPrune;
	}

	public GameStateValueBehaviour setNumberOfLowestScoringNodesToPrune(int numberOfLowestScoringNodesToPrune) {
		this.numberOfLowestScoringNodesToPrune = numberOfLowestScoringNodesToPrune;
		return this;
	}*/

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

			var rhs = (GameAction) obj;
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
	public static class IntermediateNode {
		public final int choices[];

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

		@NotNull
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
			context = null;
		}
	}

	/**
	 * Determines whether a combination of physical attacks, weapons and direct damage spells can give the player lethal
	 * against its opponent.
	 * <p>
	 * When this is {@code true}, there is a way to combine physical attacks and direct damage spells that results in a
	 * lethal, but not necessarily accounting for all side effects from attacking taunt minions that may heal their owners
	 * or otherwise.
	 * <p>
	 * Should be used as a heuristic to temporarily increase the max depth and prune early end turn actions.
	 * <p>
	 * Does not yet account for taunts or lifesteals.
	 *
	 * @param context
	 * @param playerId
	 * @param target
	 * @return
	 */
	public static boolean observesLethal(GameContext context, int playerId, Actor target) {
		try {
			var player = context.getPlayer(playerId);
			// If the opponent has a taunt minion, always return false.
			var opponent = context.getOpponent(player);
			if (target == null) {
				target = opponent.getHero();
			}
			// There may be situations where the opposing hero is null, so we'll just return false here.
			if (target == null) {
				return false;
			}
			var targetIsMinion = target.getEntityType() == EntityType.MINION;
			var targetIsHero = target.getEntityType() == EntityType.HERO;
			for (var minion : opponent.getMinions()) {
				if ((targetIsHero
						|| (targetIsMinion && !target.hasAttribute(Attribute.TAUNT)))
						&& (minion.hasAttribute(Attribute.TAUNT) || minion.hasAttribute(Attribute.AURA_TAUNT))) {
					return false;
				}
			}

			var damage = 0;
			for (var minion : player.getMinions()) {
				damage += minion.canAttackThisTurn(context) ? (minion.getAttack() * (minion.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + minion.getAttributeValue(Attribute.EXTRA_ATTACKS))) : 0;
			}
			var hero = player.getHero();
			damage += hero.canAttackThisTurn(context) ? (hero.getAttack() * (hero.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + hero.getAttributeValue(Attribute.EXTRA_ATTACKS))) : 0;

			var cards = new Card[player.getHand().size() + 1];
			player.getHand().toArray(cards);
			cards[cards.length - 1] = player.getHeroPowerZone().get(0);

			var maxWeaponDamage = 0;
			var cardDamage = 0;
			var totalManaCost = 0;
			for (var card : cards) {
				if (!context.getLogic().canPlayCard(player, card)) {
					continue;
				}

				var desc = card.getDesc();

				// If this is a weapon, account for it, but only if our hero still has attacks
				if (card.getCardType() == CardType.WEAPON && (hero.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + hero.getAttributeValue(Attribute.EXTRA_ATTACKS) > 0)) {
					// We can only really deal damage with a weapon once.
					maxWeaponDamage = Math.max(maxWeaponDamage, card.getDamage());
					totalManaCost += context.getLogic().getModifiedManaCost(player, card);
					continue;
				}

				// Determine if the card deals damage or gives the hero attack, up to a certain point.
				if ((card.getCardType() == CardType.HERO_POWER || card.getCardType() == CardType.SPELL) && desc.getSpell() != null) {
					// Check the first level of meta spells and nothing more
					var spells = new SpellDesc[]{desc.getSpell()};
					if (spells[0].getDescClass() == MetaSpell.class) {
						spells = (SpellDesc[]) spells[0].get(SpellArg.SPELLS);
					}
					if (spells == null || spells.length == 0 || spells[0] == null) {
						LOGGER.warn("observeLethal: {} had null spells", card);
						continue;
					}
					for (var spell : spells) {
						// First check hero attack buff
						if (spell.getDescClass() == BuffSpell.class
								&& Objects.equals(spell.getTarget(), EntityReference.FRIENDLY_HERO)
								&& spell.containsKey(SpellArg.ATTACK_BONUS)) {
							var buff = spell.getValue(SpellArg.ATTACK_BONUS, context, player, player.getHero(), player.getHero(), 0);
							buff *= hero.canAttackThisTurn(context) ? (hero.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + hero.getAttributeValue(Attribute.EXTRA_ATTACKS)) : 0;
							cardDamage += buff;
							totalManaCost += context.getLogic().getModifiedManaCost(player, card);
						}
						// Then check DamageSpell
						if (spell.getDescClass() == DamageSpell.class) {
							var targetSelection = desc.getTargetSelection();
							var thisSpellTarget = spell.getTarget();
							if (Objects.equals(thisSpellTarget, EntityReference.ALL_CHARACTERS)
									|| Objects.equals(thisSpellTarget, EntityReference.ENEMY_CHARACTERS)
									|| (Objects.equals(thisSpellTarget, EntityReference.ENEMY_HERO) && targetIsHero)
									|| (Objects.equals(thisSpellTarget, EntityReference.ENEMY_MINIONS) && targetIsMinion)
									|| (Objects.equals(thisSpellTarget, EntityReference.ALL_MINIONS) && targetIsMinion)
									|| Objects.equals(thisSpellTarget, EntityReference.ALL_OTHER_CHARACTERS)
									|| targetSelection == TargetSelection.ANY
									|| targetSelection == TargetSelection.ENEMY_CHARACTERS
									|| (targetSelection == TargetSelection.ENEMY_HERO && targetIsHero)
									|| (targetSelection == TargetSelection.ENEMY_MINIONS && targetIsMinion)
									|| (targetSelection == TargetSelection.MINIONS && targetIsMinion)
									|| (targetSelection == TargetSelection.HEROES && targetIsHero)) {
								var spellDamage = spell.getValue(SpellArg.VALUE, context, player, player.getHero(), player.getHero(), 0);
								cardDamage += context.getLogic().applySpellpower(player, card, spellDamage);
								totalManaCost += context.getLogic().getModifiedManaCost(player, card);
							}
						}
						continue;
					}
				}
				// TODO: Should probably include missiles spell in the narrow case that the opponent has no minions.
			}

			// Add the average damage our cards can deal, so that we're not evaluating as though we could play all the cards
			damage += (int) ((float) (cardDamage + maxWeaponDamage) / (float) totalManaCost * player.getMana());

			return damage >= target.getHp();
		} catch (RuntimeException runtimeException) {
			LOGGER.error("observeLethal:\n", runtimeException);
			return false;
		}
	}
}
