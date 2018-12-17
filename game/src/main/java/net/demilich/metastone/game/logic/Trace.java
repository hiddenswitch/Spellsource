package net.demilich.metastone.game.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.util.Serialization;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.targeting.IdFactoryImpl;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * @see #replayContext(boolean, Consumer) to replay a context after loading it from a string. Provide {@code
 * 		skipLastAction: true} as the argument if the last action throws an exception (useful for debugging). Provide {@code
 * 		recorder} is useful if you'd like to process each {@link GameContext} (useful for recording replays).
 */
public class Trace implements Serializable, Cloneable {
	private static final long serialVersionUID = 2L;
	private long seed;
	private int catalogueVersion;
	private HeroClass[] heroClasses;
	private String[][] deckCardIds;
	private String deckFormatName;
	private CardSet[] deckFormatSets;
	private int[][] mulligans;
	private List<Integer> actions = new ArrayList<>();
	private transient List<String> log = new ArrayList<>();

	public Trace() {
	}

	@JsonIgnore
	public void setStartState(GameState gameState) {
		Player[] players = new Player[]{gameState.player1, gameState.player2};
		deckFormatSets = gameState.deckFormat.getCardSets().toArray(CardSet[]::new);
		deckFormatName = gameState.deckFormat.getName();
		heroClasses = new HeroClass[2];
		deckCardIds = new String[2][];
		for (int i = 0; i < 2; i++) {
			heroClasses[i] = players[i].getHero().getHeroClass();
			deckCardIds[i] = players[i].getDeck().stream().map(Card::getCardId).toArray(String[]::new);
		}
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

	@JsonIgnore
	public void addAction(int actionId, GameAction action, GameContext context) {
		actions.add(actionId);
		if (context == null) {
			log.add(action.toString());
		} else {
			log.add(action.getDescription(context, context.getActivePlayerId()));
		}
	}

	@JsonIgnore
	public GameContext replayContext() {
		return replayContext(false, null);
	}

	@JsonIgnore
	public GameContext replayContext(boolean skipLastAction) {
		return replayContext(skipLastAction, null);
	}

	/**
	 * Creates a game context and replays it using data from this trace. A {@link Consumer} can be optionally specified
	 * that receives the game context before every action taken by either player.
	 *
	 * @param skipLastAction
	 * @param beforeRequestActionHandler
	 * @return
	 */
	@JsonIgnore
	public GameContext replayContext(boolean skipLastAction, @Nullable Consumer<GameContext> beforeRequestActionHandler) {
		AtomicInteger nextAction = new AtomicInteger();
		int originalCatalogueVersion = CardCatalogue.getVersion();
		CardCatalogue.setVersion(1);
		GameContext stateRestored = new GameContext(
				new Player(DeckCreateRequest.fromCardIds(heroClasses[0], deckCardIds[0]).withFormat(deckFormatName).toGameDeck(), "Player 0"),
				new Player(DeckCreateRequest.fromCardIds(heroClasses[1], deckCardIds[1]).withFormat(deckFormatName).toGameDeck(), "Player 1"),
				new GameLogic(),
				new DeckFormat().withName(deckFormatName).withCardSets(deckFormatSets)
		);
		List<Integer> behaviourActions = actions;
		if (skipLastAction) {
			behaviourActions = behaviourActions.subList(0, behaviourActions.size() - 1);
		}
		stateRestored.setBehaviour(
				0, new TraceBehaviour(0, mulligans, nextAction, behaviourActions, beforeRequestActionHandler));
		stateRestored.setBehaviour(
				1, new TraceBehaviour(1, mulligans, nextAction, behaviourActions, beforeRequestActionHandler));
		GameLogic logic = new GameLogic((IdFactoryImpl) stateRestored.getLogic().getIdFactory(), getSeed());
		logic.setContext(stateRestored);
		stateRestored.setLogic(logic);
		try {
			stateRestored.init();
			stateRestored.resume();
		} catch (CancellationException ex) {
			// DO NOT REMOVE, resume throws cancellation on purpose.
		}
		CardCatalogue.setVersion(originalCatalogueVersion);
		return stateRestored;
	}

	public String dump() {
		return Json.encodePrettily(this);
	}

	public static Trace load(String trace) {
		return Json.decodeValue(trace, Trace.class);
	}

	public void setMulligans(int[][] mulligans) {
		this.mulligans = mulligans;
	}

	@Override
	public Trace clone() {
		try {
			Trace clone = (Trace) super.clone();
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

	@JsonIgnore
	public List<String> getLog() {
		return log;
	}

	public HeroClass[] getHeroClasses() {
		return heroClasses;
	}

	public Trace setHeroClasses(HeroClass[] heroClasses) {
		this.heroClasses = heroClasses;
		return this;
	}

	public String[][] getDeckCardIds() {
		return deckCardIds;
	}

	public Trace setDeckCardIds(String[][] deckCardIds) {
		this.deckCardIds = deckCardIds;
		return this;
	}

	public String getDeckFormatName() {
		return deckFormatName;
	}

	public Trace setDeckFormatName(String deckFormatName) {
		this.deckFormatName = deckFormatName;
		return this;
	}

	public CardSet[] getDeckFormatSets() {
		return deckFormatSets;
	}

	public Trace setDeckFormatSets(CardSet[] deckFormatSets) {
		this.deckFormatSets = deckFormatSets;
		return this;
	}

	public int[][] getMulligans() {
		return mulligans;
	}

	public Trace setActions(List<Integer> actions) {
		this.actions = actions;
		return this;
	}
}
