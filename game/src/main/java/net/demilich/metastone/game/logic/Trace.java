package net.demilich.metastone.game.logic;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.util.Serialization;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.targeting.IdFactoryImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Stores information about a game context that allows you to reproduce exactly what happened in the match.
 * <p>
 * Given the seed, starting conditions and index of each action in the available actions that a player chose, the game
 * will reproduce.
 *
 * @see #dump() to create a string you can save and later load.
 * @see #load(String) to recreate this object from a dumped string.
 * @see #replayContext(boolean, Optional<Consumer<GameContext>>) to replay a context after loading it from a string.
 * 	    Provide {@code skipLastAction: true} as the argument if the last action throws an exception (useful for
 * 	    debugging). Provide {@code recorder} is useful if you'd like to process each {@link GameContext} (useful for
 * 	    recording replays).
 * @see #getRawActions() to iterate through the actions that were taken in the game. This is <b>not</b> restored by the
 * 	    trace, while the integer actions themselves in {@link #getActions()} are.
 */
public class Trace implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private GameState gameState;
	private long seed;
	private int catalogueVersion;
	private int[][] mulligans;
	private List<Integer> actions = new ArrayList<>();
	private transient List<GameAction> rawActions = new ArrayList<>();

	public void setStartState(GameState gameState) {
		this.gameState = gameState;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public long getSeed() {
		return seed;
	}

	public void setCatalogueVersion(int catalogueVersion) {
		this.catalogueVersion = catalogueVersion;
	}

	public int getCatalogueVersion() {
		return catalogueVersion;
	}

	public List<Integer> getActions() {
		return actions;
	}

	public void addAction(int actionId, GameAction action) {
		actions.add(actionId);
		rawActions.add(action);
	}

	public GameContext replayContext(boolean skipLastAction, Optional<Consumer<GameContext>> recorder) {
		AtomicInteger nextAction = new AtomicInteger();
		int originalCatalogueVersion = CardCatalogue.getVersion();
		CardCatalogue.setVersion(1);
		GameContext stateRestored = GameContext.fromState(gameState);
		List<Integer> behaviourActions = actions;
		if (skipLastAction) {
			behaviourActions = behaviourActions.subList(0, behaviourActions.size() - 1);
		}
		stateRestored.setBehaviour(
				0, new TraceBehaviour(0, mulligans, nextAction, behaviourActions, recorder));
		stateRestored.setBehaviour(
				1, new TraceBehaviour(1, mulligans, nextAction, behaviourActions, recorder));
		GameLogic logic = new GameLogic((IdFactoryImpl) stateRestored.getLogic().getIdFactory(), getSeed());
		logic.setContext(stateRestored);
		stateRestored.setLogic(logic);
		stateRestored.init();
		try {
			stateRestored.resume();
		} catch (CancellationException ex) {
		}
		CardCatalogue.setVersion(originalCatalogueVersion);
		return stateRestored;
	}

	public String dump() {
		return Serialization.serializeBase64(this);

	}

	public static Trace load(String trace) {
		return (Trace) Serialization.deserializeBase64(trace);
	}

	public void setMulligans(int[][] mulligans) {
		this.mulligans = mulligans;
	}

	@Override
	public Trace clone() {
		try {
			Trace clone = (Trace) super.clone();
			if (gameState != null) {
				clone.gameState = gameState.clone();
			}
			if (mulligans != null) {
				int[][] mulliganCopy = new int[mulligans.length][];
				for (int i = 0; i < mulligans.length; i++) {
					mulliganCopy[i] = Arrays.copyOf(mulligans[i], mulligans[i].length);
				}
				clone.mulligans = mulliganCopy;
			}
			clone.actions = new ArrayList<>(actions);
			return clone;
		} catch (Exception ex) {
			return null;
		}
	}

	public List<GameAction> getRawActions() {
		return rawActions;
	}
}
