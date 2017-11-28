package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.impl.server.PregamePlayerConfiguration;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckWithId;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by bberman on 6/7/17.
 */
public class IntegrationTestContext {
	private final io.vertx.ext.unit.TestContext vertxTestContext;
	private final Vertx vertx;
	private List<UnityClient> clients = new ArrayList<>();
	private String gameId;

	public IntegrationTestContext(io.vertx.ext.unit.TestContext vertxTestContext, Vertx vertx) {
		this.vertxTestContext = vertxTestContext;
		this.vertx = vertx;
	}

	public IntegrationTestContext startGame() throws SuspendExecution, InterruptedException {
		RpcClient<Accounts> accounts = Rpc.connect(Accounts.class, vertx.eventBus());
		CreateAccountResponse car1 = accounts.sync().createAccount(new CreateAccountRequest().withName("player1").withEmailAddress("test1@test.com").withPassword("testpassword"));
		CreateAccountResponse car2 = accounts.sync().createAccount(new CreateAccountRequest().withName("player2").withEmailAddress("test2@test.com").withPassword("testpassword"));

		RpcClient<Logic> logic = Rpc.connect(Logic.class, vertx.eventBus());
		logic.uncheckedSync().initializeUser(new InitializeUserRequest(car1.getUserId()));
		logic.uncheckedSync().initializeUser(new InitializeUserRequest(car2.getUserId()));

		logic.sync().startGame(new StartGameRequest()
				.withGameId(gameId)
				.withPlayers(new StartGameRequest.Player()
						.withId(0)
						.withUserId(car1.getUserId())));
		RpcClient<Games> games = Rpc.connect(Games.class, vertx.eventBus());
		games.sync().createGameSession(new CreateGameSessionRequest()
				.withPregame1(new PregamePlayerConfiguration(new DeckWithId("deckId1"), "Player 1"))
				.withPregame2(new PregamePlayerConfiguration(new DeckWithId("deckId2"), "Player 2")));
		clients.add(new UnityClient(vertxTestContext).contextPlay(this));
		clients.add(new UnityClient(vertxTestContext).contextPlay(this));
		return this;
	}

	public Minion testMinion(int attack, int hp, int cost) {
		return null;
	}

	public IntegrationTestContext playFromHand(int player1, String cardId) {
		return this;
	}

	public IntegrationTestContext summonToBattlefield(int player2, Minion minion) {
		return this;
	}

	public IntegrationTestContext destroy(int player1, String cardId) {
		return this;
	}

	public IntegrationTestContext endGame() {
		return this;
	}

	public IntegrationTestContext assertThat(Consumer<IntegrationTestContext> handler) {
		return this;
	}

	public GameContext gameContext() {
		return null;
	}

	public IntegrationTestContext destroy(String cardId) {
		return this;
	}

	public Actor getMinion(String cardId) {
		return gameContext().getEntities()
				.filter(m -> m.getEntityType() == EntityType.MINION)
				.map(m -> (Actor) m)
				.filter(m -> m.getSourceCard().getCardId().equals(cardId))
				.findFirst().get();
	}

	public class Player {
		public int id;

		public Player startingHand() {
			return this;
		}

		public Player playCard(Card card) {
			return this;
		}
	}


}
