package net.demilich.metastone.game.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import com.hiddenswitch.spellsource.common.GameState;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.targeting.IdFactoryImpl;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Stores information about a game context that allows you to reproduce exactly what happened in the match.
 * <p>
 * Given the seed, starting conditions and index of each action in the available actions that a player chose, the game
 * will reproduce.
 *
 * @see #dump() to create a string you can save and later load.
 * @see #load(String) to recreate this object from a dumped string.
 * @see #replayContext(boolean, Consumer) to replay a context after loading it from a string. Provide
 * {@code skipLastAction: true} as the argument if the last action throws an exception (useful for debugging). Provide
 * {@code recorder} is useful if you'd like to process each {@link GameContext} (useful for recording replays).
 */
public class Trace implements Serializable, Cloneable {
	private static final long serialVersionUID = 4L;
	private long seed;
	private int catalogueVersion;
	private List<String> heroClasses;
	private List<DeckTrace> deckCardIds;
	private String deckFormatName;
	private List<String> deckFormatSets;
	private List<String> secondPlayerBonusCards;
	private List<MulliganTrace> mulligans;
	private List<Integer> actions = new ArrayList<>();
	private String id;
	private boolean traceErrors;
	private int version = 5;
	@JsonIgnore
	private transient List<GameAction> rawActions = new ArrayList<>();
	@JsonIgnore
	private transient CardCatalogue cardCatalogue;

	public Trace() {
	}

	@JsonIgnore
	public void setStartState(GameState gameState) {
		Player[] players = new Player[]{gameState.getPlayer1(), gameState.getPlayer2()};
		DeckFormat deckFormat = gameState.getDeckFormat();
		deckFormatSets = new ArrayList<>(deckFormat.getSets());
		deckFormatName = gameState.getDeckFormat().getName();
		secondPlayerBonusCards = Arrays.asList(gameState.getDeckFormat().getSecondPlayerBonusCards());
		setHeroClasses(Arrays.asList(null, null));
		setDeckCardIds(Arrays.asList(new DeckTrace().setPlayerId(0), new DeckTrace().setPlayerId(1)));
		for (int i = 0; i < 2; i++) {
			getHeroClasses().set(i, players[i].getHero().getHeroClass());
			getDeckCardIds().get(i).setCardIds(players[i].getDeck().stream().map(Card::getCardId).collect(Collectors.toList()));
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
	public void addAction(GameAction action) {
		actions.add(action.getId());
		rawActions.add(action);
	}

	@JsonIgnore
	public GameContext replayContext() {
		return replayContext(isTraceErrors(), null);
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
		return replayContext(skipLastAction, beforeRequestActionHandler, this.cardCatalogue == null ? ClasspathCardCatalogue.INSTANCE : this.cardCatalogue);
	}

	/**
	 * Creates a game context and replays it using data from this trace. A {@link Consumer} can be optionally specified
	 * that receives the game context before every action taken by either player.
	 *
	 * @param skipLastAction
	 * @param beforeRequestActionHandler
	 * @param cardCatalogue
	 * @return
	 */
	@JsonIgnore
	public GameContext replayContext(boolean skipLastAction, @Nullable Consumer<GameContext> beforeRequestActionHandler, CardCatalogue cardCatalogue) {
		this.cardCatalogue = cardCatalogue;
		AtomicInteger nextAction = new AtomicInteger();
		GameContext gameContext = new GameContext();
		restoreStartingStateTo(gameContext);

		List<Integer> behaviourActions = actions;
		if (skipLastAction) {
			behaviourActions = behaviourActions.subList(0, behaviourActions.size() - 1);
		}

		gameContext.setBehaviour(
				0, new TraceBehaviour(0, mulligans, nextAction, behaviourActions, beforeRequestActionHandler));
		gameContext.setBehaviour(
				1, new TraceBehaviour(1, mulligans, nextAction, behaviourActions, beforeRequestActionHandler));

		try {
			gameContext.init();
			gameContext.resume();
		} catch (CancellationException ex) {
			// DO NOT REMOVE, resume throws cancellation on purpose.
		}
		return gameContext;
	}

	public void restoreStartingStateTo(GameContext context) {
		if (version >= 5 && this.cardCatalogue != null) {
			context.setCardCatalogue(this.cardCatalogue);
		}

		// Compatibility with previous deck formats
		DeckFormat deckFormat = new DeckFormat();
		if (secondPlayerBonusCards != null) {
			deckFormat.setSecondPlayerBonusCards(secondPlayerBonusCards.toArray(new String[0]));
		}
		if (deckFormatSets != null) {
			deckFormat.withCardSets(deckFormatSets);
		}
		if (deckFormatName != null) {
			if (deckFormatSets == null || deckFormatSets.size() == 0) {
				deckFormat = context.getCardCatalogue().getFormat(deckFormatName);
			} else {
				deckFormat.setName(deckFormatName);
			}
		}

		// Compatibility with previous traces
		if (getCatalogueVersion() == 1 && secondPlayerBonusCards == null) {
			deckFormat.setSecondPlayerBonusCards(new String[]{"spell_the_coin"});
		}
		context.setDeckFormat(deckFormat);

		if (heroClasses != null && deckCardIds != null) {
			context.setPlayer(0, new Player(DeckCreateRequest.fromCardIds(heroClasses.get(0), deckCardIds.get(0).getCardIds()).withFormat(deckFormatName).toGameDeck(), "Player 0", context.getCardCatalogue()));
			context.setPlayer(1, new Player(DeckCreateRequest.fromCardIds(heroClasses.get(1), deckCardIds.get(1).getCardIds()).withFormat(deckFormatName).toGameDeck(), "Player 1", context.getCardCatalogue()));
		} else if (heroClasses != null) {
			context.setPlayer(0, new Player(heroClasses.get(0), context.getCardCatalogue()));
			context.setPlayer(1, new Player(heroClasses.get(1), context.getCardCatalogue()));
		} else {
			context.setPlayer(0, new Player());
			context.setPlayer(1, new Player());
		}

		GameLogic logic = new GameLogic((IdFactoryImpl) context.getLogic().getIdFactory(), getSeed());
		logic.setContext(context);
		context.setLogic(logic);
	}

	public String dump() {
		return Json.encodePrettily(this);
	}

	public static Trace load(String trace) {
		return Json.decodeValue(trace, Trace.class);
	}

	public List<String> getHeroClasses() {
		return heroClasses;
	}

	public Trace setHeroClasses(List<String> heroClasses) {
		this.heroClasses = heroClasses;
		return this;
	}

	public List<DeckTrace> getDeckCardIds() {
		return deckCardIds;
	}

	public Trace setDeckCardIds(List<DeckTrace> deckCardIds) {
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

	public List<String> getDeckFormatSets() {
		return deckFormatSets;
	}

	public Trace setDeckFormatSets(List<String> deckFormatSets) {
		this.deckFormatSets = deckFormatSets;
		return this;
	}

	public List<String> getSecondPlayerBonusCards() {
		return secondPlayerBonusCards;
	}

	public Trace setSecondPlayerBonusCards(List<String> secondPlayerBonusCards) {
		this.secondPlayerBonusCards = secondPlayerBonusCards;
		return this;
	}

	public List<MulliganTrace> getMulligans() {
		return mulligans;
	}

	public Trace setMulligans(List<MulliganTrace> mulligans) {
		this.mulligans = mulligans;
		return this;
	}

	public Trace setActions(List<Integer> actions) {
		this.actions = actions;
		return this;
	}

	public String getId() {
		return id;
	}

	public Trace setId(String id) {
		this.id = id;
		return this;
	}

	public boolean isTraceErrors() {
		return traceErrors;
	}

	public Trace setTraceErrors(boolean traceErrors) {
		this.traceErrors = traceErrors;
		return this;
	}

	@JsonIgnore
	public List<GameAction> getRawActions() {
		return rawActions;
	}

	@JsonIgnore
	public Trace setRawActions(List<GameAction> rawActions) {
		this.rawActions = rawActions;
		return this;
	}

	public int getVersion() {
		return version;
	}

	public Trace setVersion(int version) {
		this.version = version;
		return this;
	}

	@Override
	public Trace clone() {
		try {
			Trace clone = (Trace) super.clone();
			List<MulliganTrace> mulliganTraces = new ArrayList<>();
			if (getMulligans() != null) {
				for (MulliganTrace mulliganTrace : getMulligans()) {
					mulliganTraces.add(mulliganTrace.clone());
				}
				clone.setMulligans(mulliganTraces);
			}

			if (actions != null) {
				clone.actions = new ArrayList<>(actions);
			}
			return clone;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", getId())
				.toString();
	}

	public JsonObject toJson() {
		return JsonObject.mapFrom(this);
	}

	public void setCardCatalogue(CardCatalogue cardCatalogue) {
		this.cardCatalogue = cardCatalogue;
	}

	public CardCatalogue getCardCatalogue() {
		return cardCatalogue;
	}
}
