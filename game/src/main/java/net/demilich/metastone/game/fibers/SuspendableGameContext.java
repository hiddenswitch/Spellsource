package net.demilich.metastone.game.fibers;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.strands.Condition;
import co.paralleluniverse.strands.SimpleConditionSynchronizer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.FiberBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.MulliganTrace;
import net.demilich.metastone.game.logic.Trace;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * A game context that uses only {@link FiberBehaviour} and suspends itself whenever an action or mulligan is
 * requested.
 * <p>
 * Its {@link #clone()} implementation does not copy data. Instead, it replays the {@link Trace} located on the game
 * context.
 */
public class SuspendableGameContext extends GameContext {

	private Condition ready;

	public FiberBehaviour getBehaviour(int playerIndex) {
		return (FiberBehaviour) getBehaviours().get(playerIndex);
	}

	public FiberBehaviour getActiveBehaviour() {
		return (FiberBehaviour) getBehaviours().get(getActivePlayerId());
	}

	public SuspendableGameContext(GameContext fromContext) {
		super(fromContext);
		ready = new SimpleConditionSynchronizer(this);
		setBehaviour(0, new FiberBehaviour(getReady()));
		setBehaviour(1, new FiberBehaviour(getReady()));
	}

	@Override
	@Suspendable
	public SuspendableGameContext clone() {
		// Replay up to the current point, doesn't actually perform a clone unfortunately
		return fromTrace(this);
	}

	/**
	 * Creates a suspendable game context using the trace in the {@code sourceContext}
	 *
	 * @param sourceContext
	 * @return a context
	 */
	@Suspendable
	public static SuspendableGameContext fromTrace(@NotNull GameContext sourceContext) {
		try {
			Trace trace = Objects.requireNonNull(sourceContext.getTrace());
			SuspendableGameContext destinationContext = new SuspendableGameContext();
			trace.restoreStartingStateTo(destinationContext);
			if (sourceContext.getActivePlayerId() == -1) {
				return destinationContext;
			}
			destinationContext.play(true);
			List<MulliganTrace> mulligans = trace.getMulligans();
			if (mulligans == null) {
				return destinationContext;
			}
			for (int i : new int[]{destinationContext.getActivePlayerId(), destinationContext.getNonActivePlayerId()}) {
				List<Integer> mulligan = mulligans.get(i).getEntityIds();
				if (mulligan == null) {
					continue;
				}

				FiberBehaviour behaviour = (FiberBehaviour) destinationContext.getBehaviours().get(i);
				destinationContext.getReady().await(1);
				List<Card> cards = behaviour.getMulliganCards();

				behaviour.setMulligan(mulligan.stream()
						.map(j -> {
							Optional<Card> card = cards
									.stream()
									.filter(c -> c.getId() == j)
									.findFirst();
							if (card.isPresent()) {
								return card.get();
							} else {
								throw new NullPointerException();
							}
						}).collect(Collectors.toList()));
			}

			List<Integer> actions = trace.getActions();
			if (actions == null) {
				return destinationContext;
			}
			for (int i = 0; i < actions.size(); i++) {
				int actionIndex = actions.get(i);
				FiberBehaviour behaviour = (FiberBehaviour) destinationContext.getBehaviours().get(destinationContext.getActivePlayerId());
				destinationContext.getReady().await(1);
				behaviour.setAction(behaviour.getValidActions().get(actionIndex));
			}

			return destinationContext;
		} catch (SuspendExecution | InterruptedException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public SuspendableGameContext() {
		super();
		setBehaviour(0, new FiberBehaviour());
		setBehaviour(1, new FiberBehaviour());
		ready = new SimpleConditionSynchronizer(this);
	}

	public SuspendableGameContext(String heroClass1, String heroClass2) {
		this();
		getPlayer1().setHero(HeroClass.getHeroCard(heroClass1).createHero(getPlayer1()));
		getPlayer2().setHero(HeroClass.getHeroCard(heroClass2).createHero(getPlayer2()));
		getTrace().setHeroClasses(Arrays.asList(heroClass1, heroClass2));
	}

	public SuspendableGameContext(GameDeck... decks) {
		this();
		for (int i = 0; i < decks.length; i++) {
			setDeck(i, decks[i]);
		}
	}

	@Override
	@Suspendable
	public void play() {
		play(true);
	}

	@Override
	public boolean updateAndGetGameOver() {
		boolean gameOver = super.updateAndGetGameOver();
		if (gameOver) {
			getReady().signalAll();
		}
		return gameOver;
	}

	@Override
	@Suspendable
	public void play(boolean fork) {
		setFiber(new GameContextFiber(() -> {
			init();
			resume();
		}));

		if (fork) {
			getFiber().start();
		} else {
			try {
				getFiber().join();
			} catch (ExecutionException e) {
				throw (RuntimeException) e.getCause();
			} catch (InterruptedException e) {
			}
		}
	}

	@Suspendable
	public void setMulligan(int playerId, List<Card> cardsToDiscard) {
		FiberBehaviour behaviour = (FiberBehaviour) getBehaviours().get(playerId);
		try {
			behaviour.setMulligan(cardsToDiscard);
			getReady().await(1);
		} catch (InterruptedException | SuspendExecution e) {
			throw new IllegalStateException(e);
		}
	}

	public List<Card> getMulliganChoices(int playerId) {
		return ((FiberBehaviour) getBehaviours().get(playerId)).getMulliganCards();
	}

	@Override
	@Suspendable
	public void performAction(int playerId, GameAction gameAction) {
		performAction(playerId, gameAction, true);
	}

	@Suspendable
	public void performAction(int playerId, GameAction gameAction, boolean wait) {
		FiberBehaviour fiberBehaviour = (FiberBehaviour) getBehaviours().get(playerId);
		try {
			fiberBehaviour.setAction(gameAction);
			if (wait) {
				getReady().await(1);
			}
		} catch (SuspendExecution suspendExecution) {
			throw new IllegalStateException(suspendExecution);
		} catch (InterruptedException interrupted) {
			try {
				endGame();
			} catch (Throwable any) {
			}
		}
	}

	@Override
	public GameContextFiber getFiber() {
		return (GameContextFiber) super.getFiber();
	}

	public Condition getReady() {
		return ready;
	}

	@Override
	public List<GameAction> getValidActions() {
		if (getActivePlayerId() == -1) {
			return Collections.emptyList();
		}
		if (updateAndGetGameOver()) {
			return Collections.emptyList();
		}

		return getActiveBehaviour().getValidActions();
	}
}
