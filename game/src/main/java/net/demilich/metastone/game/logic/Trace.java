package net.demilich.metastone.game.logic;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.util.Serialization;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.targeting.IdFactoryImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

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


	public GameContext replayContext(boolean skipLastAction) {
		AtomicInteger nextAction = new AtomicInteger();
		int originalCatalogueVersion = CardCatalogue.getVersion();
		CardCatalogue.setVersion(1);
		GameContext stateRestored = GameContext.fromState(gameState);
		List<Integer> behaviourActions = actions;
		if (skipLastAction) {
			behaviourActions = behaviourActions.subList(0, behaviourActions.size() - 1);
		}
		stateRestored.getPlayer1().setBehaviour(new TraceBehaviour(0, mulligans, nextAction, behaviourActions));
		stateRestored.getPlayer2().setBehaviour(new TraceBehaviour(1, mulligans, nextAction, behaviourActions));
		stateRestored.setLogic(new GameLogic((IdFactoryImpl) stateRestored.getLogic().getIdFactory(), getSeed()));
		stateRestored.init();
		try {
			stateRestored.resume();
		} catch (CancellationException ex) {
		}
		CardCatalogue.setVersion(originalCatalogueVersion);
		return stateRestored;
	}

	public String dump() {
		try {
			return Serialization.serializeBase64(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
}
