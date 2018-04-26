package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.utils.Attribute;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by bberman on 1/31/17.
 */
public class LogicTest extends SpellsourceTestBase {

	@Test
	@Suspendable
	public void testStartsGame(TestContext context) {
		Logging.setLoggingLevel(Level.ERROR);
		sync(() -> {
			// Create two players

			CreateAccountResponse player1 = createRandomAccount();
			CreateAccountResponse player2 = createRandomAccount();

			final String userId1 = player1.getUserId();
			final String userId2 = player2.getUserId();

			List<String> deckIds = new ArrayList<>();

			// Build two random decks
			for (String userId : new String[]{userId1, userId2}) {
				Logic.initializeUser(InitializeUserRequest.create(userId).withUserId(userId));
				DeckCreateResponse deckCreateResponse = SpellsourceTestBase.createDeckForUserId(userId);
				deckIds.add(deckCreateResponse.getDeckId());
			}

			StartGameResponse response = Logic.startGame(new StartGameRequest().withPlayers(new StartGameRequest.Player
					().withUserId(userId1).withId(0).withDeckId(deckIds.get(0)), new StartGameRequest.Player().withUserId
					(userId2).withId(1).withDeckId(deckIds.get(1))));

			for (StartGameResponse.Player player : response.getPlayers()) {
				player.getDeck().getCards().toList().forEach(c -> {
					final String cardInstanceId = (String) c.getAttribute(Attribute.CARD_INVENTORY_ID);
					assertNotNull(cardInstanceId);
				});
			}
		});
	}

	@Test
	@Suspendable
	public void testCreatesInventory(TestContext context) {
		Logging.setLoggingLevel(Level.ERROR);
		sync(() -> {
			CreateAccountResponse createAccountResponse = createRandomAccount();
			final String userId = createAccountResponse.getUserId();

			Logic.initializeUser(InitializeUserRequest.create(userId).withUserId(userId));

			GetCollectionResponse response = Inventory.getCollection(GetCollectionRequest.user(userId));

			Set<String> cardIds = response.getInventoryRecords().stream().map(r -> r.getCardDesc().id).collect(
					toSet());

			// Get the starting decks distinct card IDs
			Set<String> actualCardIds = Spellsource.spellsource().getStandardDecks().stream().flatMap(d -> d.getCardIds().stream()).collect(toSet());

			assertTrue("The user's initial collection should contain all the cards they need in the starter decks.", cardIds.equals(actualCardIds));
		});
	}

	@Test
	@Suspendable
	public void testHandleGameEvent(TestContext context) {
		context.async().complete();
	}

	@Test
	public void testAllianceCardExtensionsDontBreak(TestContext context) {
		sync(() -> {
			Logic.triggers();
			// Create the users
			final CreateAccountResponse car1 = createRandomAccount();
			String userId1 = car1.getUserId();
			final CreateAccountResponse car2 = createRandomAccount();
			String userId2 = car2.getUserId();

			// Initialize them
			InitializeUserResponse userResponse1 = Logic.initializeUser(InitializeUserRequest.create(userId1));
			InitializeUserResponse userResponse2 = Logic.initializeUser(InitializeUserRequest.create(userId2));

			// Give a player the forever post doc card
			AddToCollectionResponse minionResponse = Inventory.addToCollection(new AddToCollectionRequest().withUserId
					(userId1).withCardIds(Collections.singletonList("minion_the_forever_postdoc")));

			String foreverPostdocInventoryId = minionResponse.getInventoryIds().get(0);

			String gameId = RandomStringUtils.randomAlphanumeric(32);

			// Start a new alliance
			// TODO: This should be its own method

			final String allianceId = RandomStringUtils.randomAlphanumeric(32);
			// Create the alliance
			Inventory.createCollection(CreateCollectionRequest.alliance(allianceId, userId1, Collections.emptyList(),
					Collections.emptyList()));

			// Donate the card to the alliance
			Inventory.donateToCollection(DonateToCollectionRequest.create(allianceId, Collections.singletonList
					(foreverPostdocInventoryId)));

			// Borrow it from the collection
			Inventory.borrowFromCollection(new BorrowFromCollectionRequest().withUserId(userId1).withCollectionId
					(allianceId).withInventoryIds(Collections.singletonList(foreverPostdocInventoryId)));

			List<String> cardIds = new ArrayList<>(Collections.nCopies(15, "minion_the_forever_postdoc"));
			cardIds.addAll(Collections.nCopies(15, "minion_sourcing_specialist"));
			DeckCreateResponse dcr1 = Decks.createDeck(new DeckCreateRequest().withName("d1").withHeroClass(HeroClass
					.RED).withUserId(userId1).withCardIds(cardIds));

			// Create a 30 card deck
			DeckCreateResponse dcr2 = Decks.createDeck(new DeckCreateRequest().withName("d2").withHeroClass(HeroClass
					.VIOLET).withUserId(userId2).withCardIds(cardIds));

			StartGameResponse sgr = Logic.startGame(new StartGameRequest().withGameId(gameId).withPlayers(new
					StartGameRequest.Player().withId(0).withUserId(userId1).withDeckId(dcr1.getDeckId()), new
					StartGameRequest.Player().withId(1).withUserId(userId2).withDeckId(dcr2.getDeckId())));

			CreateGameSessionResponse cgsr = Rpc.connect(Games.class).sync().createGameSession(new CreateGameSessionRequest().withGameId(gameId)
					.withPregame1(sgr.getConfig1()).withPregame2(sgr.getConfig2()));

			assertNotNull(cgsr.gameId);

			UnityClient client1 = new UnityClient(context, car1.getLoginToken().getToken());
			UnityClient client2 = new UnityClient(context, car2.getLoginToken().getToken());

			client1.play();
			client2.play();
			client2.waitUntilDone();

			// Assert that there are minions who recorded some stats
			final boolean inventory1 = Inventory.getCollection(GetCollectionRequest.user(userId1)).getInventoryRecords()
					.stream().anyMatch(ir -> ir.getPersistentAttribute(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID) !=
							null);
			final boolean inventory2 = Inventory.getCollection(GetCollectionRequest.user(userId2)).getInventoryRecords()
					.stream().anyMatch(ir -> ir.getPersistentAttribute(Attribute.LAST_MINION_DESTROYED_CARD_ID) != null);
			assertTrue(inventory1 || inventory2);
		});
	}

	private Minion createMinionFromId(String inventoryId, int entityId, String userId, String deckId) throws
			InterruptedException, SuspendExecution {
		GetCollectionResponse gcr = Inventory.getCollection(new GetCollectionRequest().withUserId(userId));
		InventoryRecord record = gcr.getInventoryRecords().stream().filter(p -> Objects.equals(p.getId(), inventoryId)
		).findFirst().get();
		Card card = Logic.getDescriptionFromRecord(record, userId, deckId).create();
		Minion minion = card.summon();
		minion.setId(entityId);
		return minion;
	}

	private String addCardForUser(String cardId, String userId) throws InterruptedException, SuspendExecution {
		AddToCollectionResponse atcr = Inventory.addToCollection(new AddToCollectionRequest().withUserId(userId)
				.withCardIds(Collections.singletonList(cardId)));
		return atcr.getInventoryIds().get(0);
	}

	private String createCardAndUser(String cardId, String userId) throws InterruptedException, SuspendExecution {
		CreateCollectionResponse car = Inventory.createCollection(CreateCollectionRequest.startingCollection(userId));
		AddToCollectionResponse atcr = Inventory.addToCollection(new AddToCollectionRequest().withUserId(userId)
				.withCardIds(Collections.singletonList(cardId)));
		return atcr.getInventoryIds().get(0);
	}
}
