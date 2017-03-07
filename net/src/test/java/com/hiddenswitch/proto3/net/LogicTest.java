package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.impl.util.ServerGameContext;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import com.hiddenswitch.proto3.net.util.TwoClients;
import io.vertx.core.*;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bberman on 1/31/17.
 */
public class LogicTest extends ServiceTest<LogicImpl> {
	private AccountsImpl accounts;
	private CardsImpl cards;
	private InventoryImpl inventory;
	private DecksImpl decks;
	private GamesImpl games;

	@Test
	@Suspendable
	public void testStartsGame(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::startsGameSync);
	}

	public static InitializeUserResponse initializeUserId(Logic service, String userId) throws SuspendExecution, InterruptedException {
		return service.initializeUser(new InitializeUserRequest().withUserId(userId));
	}

	private void startsGameSync() throws SuspendExecution, InterruptedException {
		// Create two players

		CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
		CreateAccountResponse player2 = accounts.createAccount("b@b.com", "b", "2");

		final String userId1 = player1.userId;
		final String userId2 = player2.userId;

		List<String> deckIds = new ArrayList<>();

		// Build two random decks
		for (String userId : new String[]{userId1, userId2}) {
			initializeUserId(service, userId);
			DeckCreateResponse deckCreateResponse = DeckTest.createDeckForUserId(inventory, decks, userId);
			deckIds.add(deckCreateResponse.getDeckId());
		}

		StartGameResponse response = service.startGame(new StartGameRequest().withPlayers(
				new StartGameRequest.Player()
						.withUserId(userId1)
						.withId(0)
						.withDeckId(deckIds.get(0)),
				new StartGameRequest.Player()
						.withUserId(userId2)
						.withId(1)
						.withDeckId(deckIds.get(1))
		));

		for (StartGameResponse.Player player : response.getPlayers()) {
			player.getDeck().getCards().toList().forEach(c -> {
				final String cardInstanceId = (String) c.getAttribute(Attribute.CARD_INSTANCE_ID);
				getContext().assertNotNull(cardInstanceId);
			});
		}
	}

	@Test
	@Suspendable
	public void testCreatesInventory(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createInventorySync);
	}

	@Test
	@Suspendable
	public void testHandleGameEvent(TestContext context) {
		context.async().complete();
	}

	@Suspendable
	private void createInventorySync() throws SuspendExecution, InterruptedException {
		final Method awaitFiber;
		try {
			awaitFiber = Sync.class.getMethod("awaitFiber", Consumer.class);
		} catch (NoSuchMethodException e) {
			getContext().fail(e);
			return;
		}
		getContext().assertTrue(Arrays.stream(awaitFiber.getAnnotations()).anyMatch(a -> a.annotationType().equals(Suspendable.class)));
		getContext().assertTrue(Fiber.isCurrentFiber());

		CreateAccountResponse createAccountResponse = accounts.createAccount("benjamin.s.berman@gmail.com", "testpass", "doctorpangloss");
		final String userId = createAccountResponse.userId;

		service.initializeUser(new InitializeUserRequest().withUserId(userId));

		GetCollectionResponse response = inventory.getCollection(GetCollectionRequest.user(userId));

		Set<String> cardIds = response.getInventoryRecords().stream().map(r -> r.getCardDesc().id).collect(Collectors.toSet());

		// Should contain the classic cards
		final DeckFormat deckFormat = new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC);
		final List<String> basicClassicCardIds = CardCatalogue.query(deckFormat).toList().stream().map(Card::getCardId).collect(Collectors.toList());
		getContext().assertTrue(cardIds.containsAll(basicClassicCardIds));
	}

	@Test
	public void testAllianceCardExtensionsDontBreak(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::allianceCardExtensionsDontBreak);
	}

	private void allianceCardExtensionsDontBreak() throws SuspendExecution, InterruptedException {
		// Create the users
		String userId1 = accounts.createAccount("a@b.com", "123567", "abfdsc").userId;
		String userId2 = accounts.createAccount("a@c.com", "1235688", "abde").userId;

		// Initialize them
		InitializeUserResponse userResponse1 = service.initializeUser(new InitializeUserRequest().withUserId(userId1));
		InitializeUserResponse userResponse2 = service.initializeUser(new InitializeUserRequest().withUserId(userId2));

		// Give a player the forever post doc card
		AddToCollectionResponse minionResponse = inventory.addToCollection(new AddToCollectionRequest()
				.withUserId(userId1)
				.withCardIds(Collections.singletonList("minion_the_forever_postdoc")));

		String foreverPostdocInventoryId = minionResponse.getInventoryIds().get(0);

		String gameId = "g";

		// Start a new alliance
		// TODO: This should be its own method

		final String allianceId = "a1";
		// Create the alliance
		inventory.createCollection(CreateCollectionRequest.alliance(allianceId, userId1, Collections.emptyList(), Collections.emptyList()));

		// Donate the card to the alliance
		inventory.donateToCollection(new DonateToCollectionRequest(allianceId, Collections.singletonList(foreverPostdocInventoryId)));

		// Borrow it from the collection
		inventory.borrowFromCollection(new BorrowFromCollectionRequest()
				.withUserId(userId1)
				.withCollectionId(allianceId)
				.withInventoryIds(Collections.singletonList(foreverPostdocInventoryId)));

		// Create a 29 card deck then add Forever Post Doc
		List<String> inventoryIds = Stream.concat(
				userResponse1.getCreateCollectionResponse().getCreatedInventoryIds().stream().limit(29),
				Stream.of(foreverPostdocInventoryId)).collect(Collectors.toList());

		DeckCreateResponse dcr1 = decks.createDeck(new DeckCreateRequest()
				.withName("d1")
				.withHeroClass(HeroClass.WARRIOR)
				.withUserId(userId1)
				.withInventoryIds(inventoryIds));

		// Create a 30 card deck
		DeckCreateResponse dcr2 = decks.createDeck(new DeckCreateRequest()
				.withName("d2")
				.withHeroClass(HeroClass.WARLOCK)
				.withUserId(userId2)
				.withInventoryIds(userResponse2.getCreateCollectionResponse().getCreatedInventoryIds().stream().limit(30).collect(Collectors.toList())));

		StartGameResponse sgr = service.startGame(new StartGameRequest().withGameId(gameId)
				.withPlayers(new StartGameRequest.Player().withId(0)
						.withUserId(userId1)
						.withDeckId(dcr1.getDeckId()), new StartGameRequest.Player()
						.withId(1)
						.withUserId(userId2)
						.withDeckId(dcr2.getDeckId())));

		CreateGameSessionResponse cgsr = games.createGameSession(new CreateGameSessionRequest()
				.withGameId(gameId)
				.withPregame1(sgr.getPregamePlayerConfiguration1())
				.withPregame2(sgr.getPregamePlayerConfiguration2()));

		getContext().assertNotNull(cgsr.getGameId());
		ServerGameContext context = games.getGameContext(cgsr.getGameId());

		// Connect two players
		TwoClients twoClients = new TwoClients().invoke(games, cgsr, sgr, userId1, userId2);

		// Play them
		twoClients.play();
		float time = 0f;
		while (time < 60f && !twoClients.gameDecided()) {
			Strand.sleep(1000);
			time += 1.0f;
		}
		twoClients.assertGameOver();
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<LogicImpl>> done) {
		accounts = new AccountsImpl().withEmbeddedConfiguration();
		cards = new CardsImpl().withEmbeddedConfiguration();
		inventory = new InventoryImpl().withEmbeddedConfiguration();
		decks = new DecksImpl().withEmbeddedConfiguration();
		games = new GamesImpl();
		LogicImpl instance = new LogicImpl();

		vertx.deployVerticle(games, then -> {
			vertx.deployVerticle(accounts, then2 -> {
				vertx.deployVerticle(cards, then3 -> {
					vertx.deployVerticle(inventory, then4 -> {
						vertx.deployVerticle(decks, then5 -> {
							vertx.deployVerticle(instance, then6 -> done.handle(Future.succeededFuture(instance)));
						});
					});
				});
			});
		});

	}
}
