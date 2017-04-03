package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.ApiKeyAuthHandler;
import com.hiddenswitch.proto3.net.Server;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.amazon.UserRecord;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.client.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.client.models.Entity;
import com.hiddenswitch.proto3.net.client.models.GameState;
import com.hiddenswitch.proto3.net.common.*;
import com.hiddenswitch.proto3.net.impl.auth.TokenAuthProvider;
import com.hiddenswitch.proto3.net.impl.util.HandlerFactory;
import com.hiddenswitch.proto3.net.impl.util.Zones;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.models.MatchCancelResponse;
import com.hiddenswitch.proto3.net.util.Serialization;
import com.hiddenswitch.proto3.net.util.WebResult;
import io.vertx.core.Verticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.utils.Tuple;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * Created by bberman on 11/27/16.
 */
public class ServerImpl extends Service<ServerImpl> implements Server {
	static Logger logger = LoggerFactory.getLogger(ServerImpl.class);
	CardsImpl cards = new CardsImpl();
	AccountsImpl accounts = new AccountsImpl();
	GamesImpl games = new GamesImpl();
	MatchmakingImpl matchmaking = new MatchmakingImpl();
	BotsImpl bots = new BotsImpl();
	LogicImpl logic = new LogicImpl();
	DecksImpl decks = new DecksImpl();
	InventoryImpl inventory = new InventoryImpl();

	@Override
	@Suspendable
	public void start() throws RuntimeException {
		HttpServer server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(8080));
		Router router = Router.router(vertx);

		configureMongo();

		try {
			for (Verticle verticle : Arrays.asList(cards, accounts, games, matchmaking, bots, logic, decks, inventory)) {
				final String name = verticle.getClass().getName();
				logger.info("Deploying " + name + "...");
				String deploymentId = Sync.awaitResult(done -> vertx.deployVerticle(verticle, done));
				logger.info("Deployed " + name + " with ID " + deploymentId);
			}

			logger.info("Configuring router...");

			final TokenAuthProvider authProvider = new TokenAuthProvider(vertx);
			final ApiKeyAuthHandler authHandler = ApiKeyAuthHandler.create(authProvider, "X-Auth-Token");
			final BodyHandler bodyHandler = BodyHandler.create();

			// All routes need logging.
			router.route().handler(LoggerHandler.create());

			router.route("/v1/accounts/:targetUserId")
					.handler(authHandler);
			router.route("/v1/accounts/:targetUserId")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler("targetUserId", this::getAccount));

			router.route("/v1/accounts")
					.handler(bodyHandler);
			router.route("/v1/accounts")
					.method(HttpMethod.POST)
					.handler(HandlerFactory.handler(LoginRequest.class, this::login));
			router.route("/v1/accounts")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(CreateAccountRequest.class, this::createAccount));
			router.route("/v1/accounts")
					.method(HttpMethod.GET)
					.handler(authHandler);
			router.route("/v1/accounts")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(GetAccountsRequest.class, this::getAccounts));


			router.route("/v1/decks")
					.handler(bodyHandler);
			router.route("/v1/decks")
					.handler(authHandler);
			router.route("/v1/decks")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(DecksPutRequest.class, this::decksPut));
			router.route("/v1/decks")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(this::decksGetAll));

			router.route("/v1/decks/:deckId")
					.handler(bodyHandler);
			router.route("/v1/decks/:deckId")
					.handler(authHandler);

			router.route("/v1/decks/:deckId")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler("deckId", this::decksGet));

			router.route("/v1/decks/:deckId")
					.method(HttpMethod.POST)
					.handler(HandlerFactory.handler(DecksUpdateCommand.class, "deckId", this::decksUpdate));

			router.route("/v1/decks/:deckId")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler("deckId", this::decksDelete));

			router.route("/v1/matchmaking/constructed")
					.handler(bodyHandler);
			router.route("/v1/matchmaking/constructed")
					.handler(authHandler);
			router.route("/v1/matchmaking/constructed")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(this::matchmakingConstructedGet));
			router.route("/v1/matchmaking/constructed")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler(this::matchmakingConstructedDelete));

			router.route("/v1/matchmaking/constructed/queue")
					.handler(bodyHandler);
			router.route("/v1/matchmaking/constructed/queue")
					.handler(authHandler);
			router.route("/v1/matchmaking/constructed/queue")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(MatchmakingQueuePutRequest.class, this::matchmakingConstructedQueuePut));

			router.route("/v1/matchmaking/constructed/queue")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler(this::matchmakingConstructedQueueDelete));

			logger.info("Router configured.");
			HttpServer listening = awaitResult(done -> server.requestHandler(router::accept).listen(done));
			logger.info("Listening on port " + Integer.toString(server.actualPort()));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException {
		// TODO: If it's an ally, send all the information
		if (userId.equals(targetUserId)) {
			final Account account = getAccount(userId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(account)));
		} else {
			UserRecord record = accounts.get(userId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(new Account()
					.name(record.getProfile().getDisplayName())
					.id(targetUserId))));
		}
	}


	@Override
	public WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException {
		return null;
	}

	@Override
	public WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.proto3.net.models.CreateAccountResponse internalResponse = accounts.createAccount(request.getEmail(), request.getPassword(), request.getName());

		if (internalResponse.invalidEmailAddress) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		} else if (internalResponse.invalidPassword) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		}

		// Initialize the collection
		final String userId = internalResponse.userId;
		logic.initializeUser(new InitializeUserRequest().withUserId(userId));

		final Account account = getAccount(userId);
		return WebResult.succeeded(new CreateAccountResponse()
				.loginToken(internalResponse.loginToken.token)
				.account(account));
	}

	@Override
	public WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.proto3.net.amazon.LoginResponse internalResponse = accounts.login(request.getEmail(), request.getPassword());

		if (internalResponse.isBadPassword()) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		} else if (internalResponse.isBadEmail()) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		}

		return WebResult.succeeded(new LoginResponse()
				.account(getAccount(internalResponse.getToken().getAccessKey()))
				.loginToken(internalResponse.getToken().token));
	}

	@Override
	public WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException {
		final HeroClass heroClass = HeroClass.valueOf(request.getHeroClass());

		DeckCreateResponse internalResponse = decks.createDeck(new DeckCreateRequest()
				.withName(request.getName())
				.withInventoryIds(request.getInventoryIds())
				.withHeroClass(heroClass)
				.withUserId(userId));

		return WebResult.succeeded(new DecksPutResponse().deckId(internalResponse.getDeckId()));
	}

	private WebResult<DecksGetResponse> getDeck(String userId, String deckId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse updatedCollection = inventory.getCollection(new GetCollectionRequest()
				.withUserId(userId)
				.withDeckId(deckId));

		return WebResult.succeeded(new DecksGetResponse()
				.inventoryIdsSize(updatedCollection.getInventoryRecords().size())
				.collection(updatedCollection.asInventoryCollection()));
	}

	@Override
	public WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException {
		decks.updateDeck(new DeckUpdateRequest(userId, deckId, updateCommand));

		// Get the updated collection
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		List<String> decks = accounts.get(userId).getDecks();

		List<DecksGetResponse> responses = new ArrayList<>();
		for (String deck : decks) {
			responses.add(getDeck(userId, deck).result());
		}

		return WebResult.succeeded(new DecksGetAllResponse().decks(responses));
	}

	@Override
	public WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(decks.deleteDeck(new DeckDeleteRequest(deckId)));
	}

	@Override
	public WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext routingContext, String userId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException {
		MatchmakingRequest internalRequest = new MatchmakingRequest(request, userId).withBotMatch(request.getCasual());
		MatchmakingResponse internalResponse = matchmaking.matchmakeAndJoin(internalRequest);

		// Compute the appropriate response
		MatchmakingQueuePutResponse userResponse = new MatchmakingQueuePutResponse();
		if (internalResponse.getConnection() != null) {
			final JavaSerializationObject connection;
			try {
				connection = new JavaSerializationObject()
						.javaSerialized(Serialization.serializeBase64(internalResponse.getConnection()));
			} catch (IOException e) {
				routingContext.fail(e);

				return WebResult.failed(e);
			}
			userResponse.connection(connection);
		}

		// Determine status code
		int statusCode = 200;
		if (internalResponse.getRetry() != null) {
			userResponse.retry(new MatchmakingQueuePutRequest()
					.deckId(request.getDeckId()));
			statusCode = 202;
		}

		return WebResult.succeeded(statusCode, userResponse);
	}

	@Override
	public WebResult<com.hiddenswitch.proto3.net.client.models.MatchCancelResponse> matchmakingConstructedQueueDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		MatchCancelResponse internalResponse = matchmaking.cancel(new MatchCancelRequest(userId));

		com.hiddenswitch.proto3.net.client.models.MatchCancelResponse response =
				new com.hiddenswitch.proto3.net.client.models.MatchCancelResponse()
						.isCanceled(internalResponse.getCanceled());

		return WebResult.succeeded(response);
	}

	@Override
	public WebResult<MatchConcedeResponse> matchmakingConstructedDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		MatchCancelResponse response = matchmaking.cancel(new MatchCancelRequest(userId));
		if (response == null) {
			return WebResult.failed(new RuntimeException());
		}
		games.concedeGameSession(new ConcedeGameSessionRequest(response.getGameId(), response.getPlayerId()));
		return WebResult.succeeded(new MatchConcedeResponse().isConceded(true));
	}

	@Override
	public WebResult<GameState> matchmakingConstructedGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		CurrentMatchResponse response = matchmaking.getCurrentMatch(new CurrentMatchRequest(userId));
		if (response.getGameId() == null) {
			return WebResult.failed(404, new NullPointerException("Game not found."));
		}

		DescribeGameSessionResponse gameSession = games.describeGameSession(new DescribeGameSessionRequest(response.getGameId()));
		final com.hiddenswitch.proto3.net.common.GameState state = gameSession.getState();

		final Player local;
		final Player opponent;
		if (state.player1.getUserId().equals(userId)) {
			local = state.player1;
			opponent = state.player2;
		} else {
			local = state.player2;
			opponent = state.player1;
		}

		List<Zone> zones = new ArrayList<>();
		List<Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible

		GameContext workingContext = new GameContext();
		workingContext.loadState(state);

		List<Entity> localHand = new ArrayList<>();
		for (Card card : local.getHand()) {
			final Entity entity = new Entity()
					.description(card.getDescription())
					.entityType(Entity.EntityTypeEnum.CARD)
					.name(card.getName())
					.description(card.getDescription())
					.id(card.getId())
					.cardId(card.getCardId());
			final EntityState entityState = new EntityState();
			entityState.playable(workingContext.getLogic().canPlayCard(card.getOwner(), card.getCardReference()));
			final Player localPlayer = workingContext.getPlayer(card.getOwner());
			entityState.manaCost(workingContext.getLogic().getModifiedManaCost(localPlayer, card));
			entityState.baseManaCost(card.getBaseManaCost());
			entityState.battlecry(card.hasAttribute(Attribute.BATTLECRY));
			entityState.deathrattles(card.hasAttribute(Attribute.DEATHRATTLES));
			final boolean hostsTrigger = workingContext.getTriggerManager().getTriggersAssociatedWith(card.getReference()).size() > 0;
			// TODO: Run the game context to see if the card has any triggering side effects. If it does, then color its border yellow.
			switch (card.getCardType()) {
				case MINION:
					MinionCard minionCard = (MinionCard) card;
					entityState.attack(minionCard.getAttack() + minionCard.getBonusAttack());
					entityState.baseAttack(minionCard.getBaseAttack());
					entityState.baseManaCost(minionCard.getBaseManaCost());
					entityState.hp(minionCard.getHp());
					entityState.maxHp(minionCard.getBaseHp() + minionCard.getBonusHp());
					entityState.underAura(minionCard.getBonusAttack() > 0
							|| minionCard.getBonusAttack() > 0
							|| hostsTrigger);
					break;
				case WEAPON:
					WeaponCard weaponCard = (WeaponCard) card;
					entityState.durability(weaponCard.getDurability());
					entityState.hp(weaponCard.getDurability());
					entityState.maxHp(weaponCard.getBaseDurability() + weaponCard.getBonusDurability());
					entityState.attack(weaponCard.getDamage() + weaponCard.getBonusDamage());
					entityState.underAura(weaponCard.getBonusDamage() > 0
							|| weaponCard.getBonusDurability() > 0
							|| hostsTrigger);
					break;
				case SPELL:
				case CHOOSE_ONE:
					SpellCard spellCard = (SpellCard) card;
					int damage = 0;
					int spellpowerDamage = 0;
					if (DamageSpell.class.isAssignableFrom(spellCard.getSpell().getSpellClass())) {
						damage = DamageSpell.getDamage(workingContext, localPlayer, spellCard.getSpell(), card, null);
						spellpowerDamage = workingContext.getLogic().applySpellpower(localPlayer, spellCard, damage);
					}
					entityState.underAura(spellpowerDamage > damage
							|| hostsTrigger);
					entityState.spellDamage(spellpowerDamage);
					break;
			}
			entity.state(entityState);
			localHand.add(entity);
		}

		// Add complete information for the local hand
		entities.addAll(localHand);
		zones.add(new Zone()
				.id(Zones.LOCAL_HAND)
				.entities(localHand.stream().map(Entity::getId).collect(toList())));

		for (Tuple<Integer, List<Minion>> minionTuple : Arrays.asList(new Tuple<>(Zones.LOCAL_BATTLEFIELD, local.getMinions()), new Tuple<>(Zones.OPPONENT_BATTLEFIELD, opponent.getMinions()))) {
			int zone = minionTuple.getFirst();
			List<Minion> battlefield = minionTuple.getSecond();
			List<Entity> minions = new ArrayList<>();
			for (Minion minion : battlefield) {
				final Card card = minion.getSourceCard();
				final Entity entity = new Entity()
						.description(card.getDescription())
						.entityType(Entity.EntityTypeEnum.MINION)
						.name(card.getName())
						.description(card.getDescription())
						.id(card.getId())
						.cardId(card.getCardId());
				final EntityState entityState = new EntityState();
				entityState.playable(workingContext.getLogic().canPlayCard(card.getOwner(), card.getCardReference()));
				final Player localPlayer = workingContext.getPlayer(card.getOwner());
				entityState.manaCost(workingContext.getLogic().getModifiedManaCost(localPlayer, card));
				entityState.baseManaCost(card.getBaseManaCost());
				entityState.silenced(minion.hasAttribute(Attribute.SILENCED));
				entityState.deathrattles(minion.getDeathrattles() != null);
				entityState.playable(minion.canAttackThisTurn());
				entityState.attack(minion.getAttack());
				entityState.hp(minion.getHp());
				entityState.maxHp(minion.getMaxHp());
				entityState.underAura(minion.hasAttribute(Attribute.AURA_ATTACK_BONUS)
						|| minion.hasAttribute(Attribute.AURA_HP_BONUS)
						|| minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
						|| minion.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
						|| minion.hasAttribute(Attribute.HP_BONUS)
						|| minion.hasAttribute(Attribute.ATTACK_BONUS)
						|| minion.hasAttribute(Attribute.CONDITIONAL_ATTACK_BONUS)
						|| minion.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS));
				entityState.frozen(minion.hasAttribute(Attribute.FROZEN));
				entityState.stealth(minion.hasAttribute(Attribute.STEALTH));
				entityState.taunt(minion.hasAttribute(Attribute.TAUNT));
				entityState.divineShield(minion.hasAttribute(Attribute.DIVINE_SHIELD));
				entityState.enraged(minion.hasAttribute(Attribute.ENRAGED));
				entityState.destroyed(minion.hasAttribute(Attribute.DESTROYED));
				entityState.cannotAttack(minion.hasAttribute(Attribute.CANNOT_ATTACK));
				entityState.spellDamage(minion.getAttributeValue(Attribute.SPELL_DAMAGE));
				entityState.windfury(minion.hasAttribute(Attribute.WINDFURY));
				entityState.untargetableBySpells(minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
				entity.state(entityState);
				minions.add(entity);
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
			zones.add(new Zone()
					.id(zone)
					.entities(minions.stream().map(Entity::getId).collect(toList())));
		}

		// Add IDs for the decks and the opposing hand.
		List<Entity> localDeck = local.getDeck().toList().stream().map(c -> new Entity().id(c.getId())).collect(toList());
		List<Entity> opposingDeck = opponent.getDeck().toList().stream().map(c -> new Entity().id(c.getId())).collect(toList());
		// TODO: The opposing hand should show auras
		List<Entity> opposingHand = opponent.getHand().toList().stream().map(c -> new Entity().id(c.getId())).collect(toList());

		zones.add(new Zone()
				.id(Zones.LOCAL_DECK)
				.entities(localDeck.stream().map(Entity::getId).collect(toList())));
		zones.add(new Zone()
				.id(Zones.OPPONENT_DECK)
				.entities(opposingDeck.stream().map(Entity::getId).collect(toList())));
		zones.add(new Zone()
				.id(Zones.OPPONENT_HAND)
				.entities(opposingHand.stream().map(Entity::getId).collect(toList())));


		return WebResult.succeeded(new GameState()
				.entities(entities)
				.zones(zones)
				.timestamp(System.nanoTime())
				.turnState(workingContext.getTurnState().toString()));
	}

	private Account getAccount(String userId) throws SuspendExecution, InterruptedException {
		// Get the personal collection
		UserRecord record = accounts.get(userId);
		GetCollectionResponse personalCollection = inventory.getCollection(GetCollectionRequest.user(record.getId()));

		// Get the decks
		GetCollectionResponse deckCollections = inventory.getCollection(GetCollectionRequest.decks(record.getDecks()));

		final String displayName = record.getProfile().getDisplayName();
		return new Account()
				.id(record.getId())
				.decks(deckCollections.getResponses().stream().map(GetCollectionResponse::asInventoryCollection).collect(toList()))
				.personalCollection(personalCollection.asInventoryCollection())
				.email(record.getProfile().getEmailAddress())
				.name(displayName);
	}


	private void configureMongo() {
		// Get the Mongo URL
		if (System.getProperties().containsKey("mongo.url")
				|| System.getenv().containsKey("MONGO_URL")) {

			String mongoUrl = System.getProperties().getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", "mongodb://localhost:27017/local"));
			URI url;
			try {
				url = new URI(mongoUrl);
			} catch (URISyntaxException e) {
				logger.error("The Mongo URL is malformed. We got " + mongoUrl);
				throw new RuntimeException(e);
			}

			final JsonObject config = new JsonObject().put("host", url.getHost())
					.put("port", url.getPort());

			if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
				String username = url.getUserInfo().split(":")[0];
				String password = url.getUserInfo().split(":")[1];

				config.put("username", username)
						.put("password", password);
			}

			String db_name = MongoClient.DEFAULT_DB_NAME;
			if (url.getPath() != null && !url.getPath().isEmpty()) {
				db_name = url.getPath().startsWith("/") ? url.getPath().substring(1) : url.getPath();
			}
			config.put("db_name", db_name);

			String query = url.getQuery();
			if (query != null
					&& !query.isEmpty()
					&& query.contains("authSource")) {

				List<NameValuePair> params = URLEncodedUtils.parse(url, "UTF-8");
				Optional<NameValuePair> authSource = params.stream().filter(p -> Objects.equals(p.getName(), "authSource")).findFirst();
				if (authSource.isPresent()) {
					config.put("authSource", authSource.get().getValue());
				}
			}

			MongoClient client = MongoClient.createShared(vertx, config);

			logic.withMongo(client);
			accounts.withMongo(client);
			inventory.withMongo(client);
			decks.withMongo(client);
			bots.withMongo(client);
		} else {
			logic.withEmbeddedConfiguration();
			accounts.withEmbeddedConfiguration();
			inventory.withEmbeddedConfiguration();
			decks.withEmbeddedConfiguration();
			bots.withEmbeddedConfiguration();
		}
	}
}
