package com.hiddenswitch.framework.migrations;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.CaseFormat;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;
import io.vertx.ext.auth.authorization.Authorizations;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.decks.GameDeck;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class V8__Migrate_from_previous_server extends BaseJavaMigration {
	private static final Logger LOGGER = LoggerFactory.getLogger(V8__Migrate_from_previous_server.class);
	public static String INVENTORY = "inventory.cards";
	public static String COLLECTIONS = "inventory.collections";
	public static String USERS = "accounts.users";

	@Override
	public Integer getChecksum() {
		return getMongoUrl().hashCode();
	}

	@Override
	public boolean canExecuteInTransaction() {
		return false;
	}

	@Override
	public void migrate(Context context) throws Exception {
		// we don't use the context in this one
		var vertx = Vertx.vertx();
		// only run the migration if there's a mongo url set
		var mongoUri = getMongoUrl();

		if (mongoUri.isEmpty()) {
			return;
		}

		LOGGER.debug("mongoUri: {}", mongoUri);

		/*
		// try to connect to mongo and see if it actually works
		var mongoClient = MongoClient.create(vertx, new JsonObject().put("connection_string", mongoUri));
		var canRead = mongoClient.findOne("migrations", new JsonObject(), new JsonObject().put("_id", 1)).toCompletionStage().toCompletableFuture().get(8000, TimeUnit.MILLISECONDS);
		if (canRead == null) {
			throw new IllegalStateException("unexpectedly could not read migrations");
		}

		await(fiber(vertx, () -> {
			var executor = Environment.queryExecutor();
			// retrieve all non bot accounts
			var accountJsons = mongoClient.findBatch(USERS, new JsonObject()
					.put(UserRecord.BOT, new JsonObject().put("$ne", true)));

			// later we will migrate all the friend records
			var friends = new ArrayList<FriendRecordWithId>();
			var userIdMapping = new HashMap<String, String>();
			var existingUsernames = HashMultiset.create();
			var existingEmails = HashMultiset.create();

			// populate existing users
			existingUsernames.addAll(await(executor.findManyRow(dsl -> dsl.select(USER_ENTITY.USERNAME).from(USER_ENTITY)))
					.stream().map(row -> row.getString(USER_ENTITY.USERNAME.getName()).toLowerCase()).collect(Collectors.toList()));

			// iterate through all the accounts
			for (var accountJson : await(accountJsons)) {
				var userRecord = accountJson.mapTo(UserRecord.class);
				var emails = userRecord.getEmails();
				if (emails == null || emails.isEmpty()) {
					continue;
				}

				var username = userRecord.getUsername();
				existingUsernames.add(username.toLowerCase());
				var count = existingUsernames.count(username.toLowerCase());
				if (count > 1) {
					while (existingUsernames.contains(username.toLowerCase() + count)) {
						count++;
					}

					username = username + count;
				}

				// create the user in our system
				var email = emails.get(0).getAddress().toLowerCase();
				existingEmails.add(email);
				count = existingEmails.count(email);
				if (count > 1) {

					var parts = email.split("@");
					email = parts[0] + "+dup" + (count - 1) + "@" + parts[1];
				}

				var userEntity = await(Accounts.createUserWithHashed(
						email,
						username,
						userRecord.getServices().getPassword().getScrypt()));

				userIdMapping.put(userRecord.getId(), userEntity.getId());

				var record = USER_ENTITY_ADDONS.newRecord()
						.setId(userEntity.getId())
						.setMigrated(true);

				// migrate the privacy token
				var privacyToken = userRecord.getPrivacyToken();
				if (privacyToken != null && !privacyToken.isEmpty()) {
					record.setPrivacyToken(privacyToken);

				}

				await(executor.execute(dsl -> dsl.insertInto(USER_ENTITY_ADDONS).set(record)));

				// migrate the decks
				if (userRecord.getDecks() != null && !userRecord.getDecks().isEmpty()) {
					for (var deckId : userRecord.getDecks()) {
						var getCollectionResponse = getCollection(mongoClient, GetCollectionRequest.deck(deckId));
						// don't transfer deleted or "standard" decks
						if (getCollectionResponse.getCollectionRecord().isTrashed()
								|| getCollectionResponse.getCollectionRecord().isStandardDeck()) {
							continue;
						}

						var legacyInventoryCollection = getCollectionResponse.asInventoryCollection();
						var format = legacyInventoryCollection.getFormat();
						if (format == null || format.isEmpty()) {
							format = DeckFormat.spellsource().getName();
						}
						await(Legacy.createDeck(userEntity.getId(), new DeckCreateRequest()
								.withName(legacyInventoryCollection.getName())
								.withCardIds(legacyInventoryCollection.getInventory().stream().map(CardRecord::getEntity).map(Entity::getCardId).collect(Collectors.toList()))
								.withFormat(format)
								.withHeroClass(legacyInventoryCollection.getHeroClass())
						));
					}
				}

				// migrate friends later
				if (userRecord.getFriends() != null && !userRecord.getFriends().isEmpty()) {
					for (var friend : userRecord.getFriends()) {
						friends.add(new FriendRecordWithId(userRecord.getId(), friend));
					}
				}

				LOGGER.debug("migrated user {}", userEntity.getUsername());
			}
			if (!friends.isEmpty()) {
				// now that we've created all the accounts, the friend constraints should be okay
				await(executor.execute(dsl -> {
					var insert = dsl.insertInto(FRIENDS);
					InsertValuesStepN<FriendsRecord> values = null;
					for (var friend : friends) {
						var friendRecord = FRIENDS.newRecord()
								.setId(userIdMapping.get(friend.getId()))
								.setFriend(userIdMapping.get(friend.getFriendId()))
								.setCreatedAt(
										OffsetDateTime.ofInstant(Instant.ofEpochMilli(friend.getSince()), ZoneId.systemDefault())
								);
						values = insert.values(friendRecord.intoArray());
					}
					return values.onDuplicateKeyIgnore();
				}));
			}

			return (Void) null;
		}));

		 */
	}

	public static String getMongoUrl() {
		return System.getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", ""));
	}

	static GetCollectionResponse getCollection(/*MongoClient*/ Object mongoClient, GetCollectionRequest request) {
		if (request.isBatchRequest()) {
			List<GetCollectionResponse> responses = new ArrayList<>();
			var requests = request.getRequests();

			// Retrieve deck requests and process them separately
			List<GetCollectionRequest> deckRequests = new ArrayList<>();
			var requestsIterator = requests.iterator();
			while (requestsIterator.hasNext()) {
				var subRequest = requestsIterator.next();
				if (subRequest.getDeckId() != null) {
					requestsIterator.remove();
					deckRequests.add(subRequest);
				} else {
					responses.add(getCollection(mongoClient, subRequest));
				}
			}

			// Bulk retrieve deck inventory records and collection information
			var deckIds = deckRequests.stream().map(GetCollectionRequest::getDeckId).collect(toList());
			/*
			var deckRecords = await(mongoClient.find(COLLECTIONS, new JsonObject().put("_id", new JsonObject().put("$in", new JsonArray(deckIds)))))
					.stream().map(r -> r.mapTo(CollectionRecord.class)).collect(toMap(CollectionRecord::getId, Function.identity()));

			Map<String, List<InventoryRecord>> deckInventories = new HashMap<>();
			for (var deckId : deckIds) {
				deckInventories.put(deckId, new Vector<>());
			}

			await(mongoClient.find(INVENTORY, new JsonObject().put("collectionIds", new JsonObject().put("$in", new JsonArray(deckIds)))))
					.stream()
					.map(r -> r.mapTo(InventoryRecord.class))
					.forEach(ir -> ir.getCollectionIds().forEach(cid -> {
						if (deckInventories.containsKey(cid)) {
							deckInventories.get(cid).add(ir);
						}
					}));

			deckIds.forEach(deckId -> {
				var record = deckRecords.get(deckId);
				responses.add(GetCollectionResponse.collection(record, deckInventories.get(deckId)));
			});

			 */

			return GetCollectionResponse.batch(responses);
		}

		String collectionId;
		CollectionTypes type;
		var userId = request.getUserId();
		if (userId != null
				&& request.getDeckId() == null) {
			collectionId = userId;
			type = CollectionTypes.USER;
		} else if (request.getDeckId() != null) {
			collectionId = request.getDeckId();
			type = CollectionTypes.DECK;
		} else {
			collectionId = null;
			type = null;
		}

		if (collectionId == null) {
			throw new NullPointerException("No collection was specified");
		}

		throw new UnsupportedOperationException();
		/*
		var results = await(mongoClient.find(INVENTORY, new JsonObject().put("collectionIds", collectionId)));
		final var inventoryRecords = results.stream().map(r -> r.mapTo(InventoryRecord.class)).collect(toList());

		var collection = await(mongoClient.find(COLLECTIONS, new JsonObject().put("_id", collectionId))).get(0).mapTo(CollectionRecord.class);

		return GetCollectionResponse.collection(collection, inventoryRecords);

		 */
	}

	/**
	 * A mongodb record of the user's collection metadata. Does not contain the inventory IDs themselves.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public final static class CollectionRecord extends MongoRecord {
		public static final String FORMAT = "format";
		public static final String HERO_CLASS = "heroClass";
		public static final String TYPE = "type";
		public static final String USER_ID = "userId";
		public static final String VALIDATION_REPORT = "validationReport";
		public static final String HERO_CARD_ID = "heroCardId";
		private String userId;
		private CollectionTypes type;
		private boolean trashed;
		@JsonProperty
		private DeckType deckType;
		private List<String> friendUserIds;
		private boolean isStandardDeck;
		private int wins;
		private int totalGames;
		private Map<String, AttributeMap> inventoryAttributes;
		private AttributeMap playerEntityAttributes;

		/**
		 * Hero class for deck collection records.
		 */
		private String heroClass;

		/**
		 * Names for alliance and deck collection records.
		 */
		private String name;
		private String heroCardId;
		private String format;

		protected CollectionRecord() {
		}

		public CollectionRecord setId(final String id) {
			this._id = id;
			return this;
		}

		public boolean isTrashed() {
			return trashed;
		}

		public CollectionRecord setFriendUserIds(final List<String> friendUserIds) {
			this.friendUserIds = friendUserIds;
			return this;
		}

		@JsonIgnore
		public boolean isDraft() {
			return deckType == DeckType.DRAFT;
		}

		@JsonIgnore
		public void setDraft(boolean draft) {
			deckType = DeckType.DRAFT;
		}

		public CollectionRecord withDraft(final boolean draft) {
			if (draft) {
				deckType = DeckType.DRAFT;
			} else {
				deckType = DeckType.CONSTRUCTED;
			}
			return this;
		}

		public static CollectionRecord deck(final String userId, final String name, final String heroClass, final boolean draft) {
			return new CollectionRecord()
					.withDraft(draft)
					.setUserId(userId)
					.setName(name)
					.setHeroClass(heroClass)
					.setType(CollectionTypes.DECK);
		}

		public static CollectionRecord user(final String userId) {
			return new CollectionRecord()
					.setId(userId)
					.setUserId(userId)
					.setType(CollectionTypes.USER);
		}

		public static CollectionRecord alliance(String allianceId, String ownerUserId) {
			return new CollectionRecord()
					.setId(allianceId)
					.setType(CollectionTypes.ALLIANCE)
					.setUserId(ownerUserId)
					.setFriendUserIds(Arrays.asList(ownerUserId));
		}

		public int getWins() {
			return wins;
		}

		public CollectionRecord setWins(int wins) {
			this.wins = wins;
			return this;
		}

		public int getTotalGames() {
			return totalGames;
		}

		public CollectionRecord setTotalGames(int totalGames) {
			this.totalGames = totalGames;
			return this;
		}

		public CollectionRecord setTrashed(boolean trashed) {
			this.trashed = trashed;
			return this;
		}

		public DeckType getDeckType() {
			return deckType;
		}

		public CollectionRecord setDeckType(DeckType deckType) {
			this.deckType = deckType;
			return this;
		}

		public List<String> getFriendUserIds() {
			return friendUserIds;
		}

		public String getHeroCardId() {
			return heroCardId;
		}

		public CollectionRecord setHeroCardId(String heroCardId) {
			this.heroCardId = heroCardId;
			return this;
		}

		public String getFormat() {
			return format;
		}

		public CollectionRecord setFormat(String format) {
			this.format = format;
			return this;
		}

		public String getUserId() {
			return userId;
		}

		public CollectionRecord setUserId(String userId) {
			this.userId = userId;
			return this;
		}

		public CollectionTypes getType() {
			return type;
		}

		public CollectionRecord setType(CollectionTypes type) {
			this.type = type;
			return this;
		}

		public String getHeroClass() {
			return heroClass;
		}

		public CollectionRecord setHeroClass(String heroClass) {
			this.heroClass = heroClass;
			return this;
		}

		public String getName() {
			return name;
		}

		public CollectionRecord setName(String name) {
			this.name = name;
			return this;
		}

		public boolean isStandardDeck() {
			return isStandardDeck;
		}

		public CollectionRecord setStandardDeck(boolean standardDeck) {
			isStandardDeck = standardDeck;
			return this;
		}

		public Map<String, AttributeMap> getInventoryAttributes() {
			return inventoryAttributes;
		}

		public CollectionRecord setInventoryAttributes(Map<String, AttributeMap> inventoryAttributes) {
			this.inventoryAttributes = inventoryAttributes;
			return this;
		}

		/**
		 * Attributes that should be put on the player entity before the start of the game.
		 *
		 * @return
		 */
		public AttributeMap getPlayerEntityAttributes() {
			return playerEntityAttributes;
		}

		public CollectionRecord setPlayerEntityAttributes(AttributeMap playerEntityAttributes) {
			this.playerEntityAttributes = playerEntityAttributes;
			return this;
		}
	}

	public enum CollectionTypes {
		USER,
		ALLIANCE,
		DECK
	}

	/**
	 * A type of deck in the collection.
	 */
	public enum DeckType {
		/**
		 * A deck constructed by the player in the Collection view in the client.
		 */
		CONSTRUCTED,
		/**
		 * A deck built by drafting cards.
		 */
		DRAFT
	}

	//	//
	public final static class EmailRecord implements Serializable {
		private String address;
		private boolean verified;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public boolean isVerified() {
			return verified;
		}

		public void setVerified(boolean verified) {
			this.verified = verified;
		}
	}

	/**
	 * Information about a friend relationship. Stored on both friends.
	 */
//	// 
	public static class FriendRecord implements Serializable {
		private String friendId;
		private long since;
		private String displayName;

		public long getSince() {
			return since;
		}

		public String getFriendId() {
			return friendId;
		}

		public FriendRecord setFriendId(String friendId) {
			this.friendId = friendId;
			return this;
		}

		public FriendRecord setSince(long since) {
			this.since = since;
			return this;
		}

		public String getDisplayName() {
			return displayName;
		}

		public FriendRecord setDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Friend toFriendDto() {
			return new Friend().friendId(this.friendId).since(this.since).friendName(this.displayName);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			FriendRecord that = (FriendRecord) o;

			if (since != that.since) return false;
			if (friendId != null ? !friendId.equals(that.friendId) : that.friendId != null) return false;
			return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
		}

		@Override
		public int hashCode() {
			int result = friendId != null ? friendId.hashCode() : 0;
			result = 31 * result + (int) (since ^ (since >>> 32));
			result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
			return result;
		}
	}

	public static final class GetCollectionRequest implements Serializable {
		private String userId;
		private String deckId;
		private List<GetCollectionRequest> requests;

		public GetCollectionRequest() {
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}


		public String getUserId() {
			return userId;
		}

		public GetCollectionRequest withUserId(final String userId) {
			this.userId = userId;
			return this;
		}

		public GetCollectionRequest withDeckId(String deckId) {
			this.deckId = deckId;
			return this;
		}

		public String getDeckId() {
			return deckId;
		}

		public void setDeckId(String deckId) {
			this.deckId = deckId;
		}

		public static GetCollectionRequest user(String userId) {
			return new GetCollectionRequest()
					.withUserId(userId);
		}

		public static GetCollectionRequest deck(String deckId) {
			return new GetCollectionRequest()
					.withDeckId(deckId);
		}

		public static GetCollectionRequest decks(String userId, List<String> decks) {
			return new GetCollectionRequest()
					.withUserId(userId)
					.withRequests(decks.stream().map(GetCollectionRequest::deck).map(request -> request.withUserId(userId)).collect(toList()));
		}

		public List<GetCollectionRequest> getRequests() {
			return requests;
		}

		public void setRequests(List<GetCollectionRequest> requests) {
			this.requests = requests;
		}

		public GetCollectionRequest withRequests(final List<GetCollectionRequest> requests) {
			this.requests = requests;
			return this;
		}

		public boolean isBatchRequest() {
			return this.requests != null && this.requests.size() > 0;
		}
	}

	public static final class GetCollectionResponse implements Serializable {
		private List<GetCollectionResponse> responses;
		private List<InventoryRecord> inventoryRecords;
		private CollectionRecord collectionRecord;

		private GetCollectionResponse() {
		}

		public static GetCollectionResponse batch(List<GetCollectionResponse> responses) {
			return new GetCollectionResponse()
					.withResponses(responses);
		}

		public static GetCollectionResponse collection(CollectionRecord collectionRecord, List<InventoryRecord> inventoryRecords) {
			return new GetCollectionResponse()
					.setCardRecords(inventoryRecords)
					.setCollectionRecord(collectionRecord);
		}

		/**
		 * Turns this response into a {@link net.demilich.metastone.game.decks.Deck} that can actually be used in a {@link
		 * GameContext}.
		 *
		 * @param userId
		 * @return
		 */
		public GameDeck asDeck(String userId) {
			return asDeck(this, userId);
		}

		/**
		 * Turns this response into a {@link net.demilich.metastone.game.decks.Deck} that can actually be used in a {@link
		 * GameContext}.
		 *
		 * @param response
		 * @param userId
		 * @return
		 */
		public static GameDeck asDeck(GetCollectionResponse response, String userId) {
			GameDeck deck = new GameDeck();
			deck.setDeckId(response.getCollectionRecord().getId());
			deck.setHeroClass(response.getCollectionRecord().getHeroClass());
			deck.setName(response.getCollectionRecord().getName());
			String heroCardId = response.getCollectionRecord().getHeroCardId();
			if (heroCardId != null) {
				deck.setHeroCard(ClasspathCardCatalogue.CLASSPATH.getCardById(heroCardId));
			}

			response.getInventoryRecords().stream().map(cardRecord -> ClasspathCardCatalogue.CLASSPATH.getCardById(cardRecord.getCardId()))
					.forEach(deck.getCards()::addCard);

			deck.setPlayerAttributes(response.getCollectionRecord().getPlayerEntityAttributes());

			return deck;
		}

		public List<InventoryRecord> getInventoryRecords() {
			return inventoryRecords;
		}

		public GetCollectionResponse setCardRecords(List<InventoryRecord> inventoryRecords) {
			this.inventoryRecords = inventoryRecords;
			return this;
		}


		public List<GetCollectionResponse> getResponses() {
			return responses;
		}

		public GetCollectionResponse withResponses(final List<GetCollectionResponse> responses) {
			this.responses = responses;
			return this;
		}

		public InventoryCollection asInventoryCollection() {
			if (getResponses() != null
					&& getResponses().size() > 0) {
				throw new RuntimeException();
			}

			if (getCollectionRecord() == null) {
				Tracing.error(new NullPointerException("collectionRecord"), GlobalTracer.get().activeSpan(), false);
				return null;
			}

			String displayName = getCollectionRecord().getId();

			if (getCollectionRecord().getName() != null) {
				displayName = getCollectionRecord().getName();
			}

			List<InventoryRecord> inventoryRecords = getInventoryRecords();
			List<CardRecord> records = new ArrayList<>();

			for (InventoryRecord cr : inventoryRecords) {
				CardDesc record = ClasspathCardCatalogue.CLASSPATH.getCardById(cr.getCardId()).getDesc();

				if (record == null) {
					continue;
				}
				// Send significantly less data
				// TODO: Just look it up by the card ID in the client
				records.add(new CardRecord()
						.entity(new Entity()
								.cardId(record.getId()))
						.id(cr.getId())
						.allianceId(cr.getAllianceId())
						.donorUserId(cr.getDonorUserId()));
			}

			InventoryCollection collection = new InventoryCollection()
					.name(displayName)
					.id(getCollectionRecord().getId())
					.type(InventoryCollection.TypeEnum.valueOf(getCollectionRecord().getType().toString()))
					.format(getCollectionRecord().getFormat())
					.deckType(getCollectionRecord().getType() == CollectionTypes.DECK ? InventoryCollection.DeckTypeEnum.valueOf(getCollectionRecord().getDeckType().toString()) : null)
					.isStandardDeck(getCollectionRecord().isStandardDeck())
					.inventory(records);

			if (getCollectionRecord().getHeroClass() != null) {
				collection.heroClass(getCollectionRecord().getHeroClass());
			}

			return collection;
		}


		public static GetCollectionResponse empty() {
			return new GetCollectionResponse();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof GetCollectionResponse)) return false;
			GetCollectionResponse that = (GetCollectionResponse) o;
			return Objects.equals(responses, that.responses) &&
					Objects.equals(inventoryRecords, that.inventoryRecords) &&
					Objects.equals(collectionRecord, that.collectionRecord);
		}

		@Override
		public int hashCode() {
			return Objects.hash(responses, inventoryRecords, collectionRecord);
		}

		public CollectionRecord getCollectionRecord() {
			return collectionRecord;
		}

		public GetCollectionResponse setCollectionRecord(CollectionRecord collectionRecord) {
			this.collectionRecord = collectionRecord;
			return this;
		}
	}

	/**
	 * Represents the hashed secret portion of the token string.
	 */
//	// 
	public static class HashedLoginTokenRecord implements Serializable {

		private String hashedToken;
		private Date when;

		public HashedLoginTokenRecord() {
		}

		/**
		 * Gets the hashed login token for comparisons using Scrypt.
		 *
		 * @return The hash of the secret part of the login token corresponding to this user.
		 */
		public String getHashedToken() {
			return hashedToken;
		}

		public void setHashedToken(String hashedToken) {
			this.hashedToken = hashedToken;
		}

		/**
		 * Get the expiration of those token.
		 *
		 * @return The expiration date.
		 */
		public Date getWhen() {
			return when;
		}


		public void setWhen(Date when) {
			this.when = when;
		}
	}

	/**
	 * A record in the inventory collection. Describes the history of a card and its ID.
	 */
//	// 
	public final static class InventoryRecord extends MongoRecord {
		public static final String CARDDESC_ID = "cardDesc.id";
		private static Logger logger = LoggerFactory.getLogger(InventoryRecord.class);

		@JsonProperty
		private CardDesc cardDesc;

		/**
		 * The userId of the player who originally opened the pack containing the card.
		 */
		@JsonProperty
		private String userId;

		/**
		 * The ID of the alliance this card belongs to, or null if this card is not shared with an alliance. The ID is also
		 * a collection ID.
		 */
		@JsonProperty
		private String allianceId;

		@JsonProperty
		private List<String> collectionIds;

		@JsonProperty
		private String borrowedByUserId;

		@JsonProperty
		private Map<String, Object> facts = new HashMap<>();

		public InventoryRecord() {
		}

		public InventoryRecord(String id, JsonObject card) {
			super(id);
			this.cardDesc = card.mapTo(CardDesc.class);
		}

		public InventoryRecord(String id, CardDesc cardDesc) {
			super(id);
			this.cardDesc = cardDesc;
		}

		@JsonIgnore
		public CardDesc getCardDesc() {
			return cardDesc;
		}

		@JsonIgnore
		public InventoryRecord withUserId(final String userId) {
			this.userId = userId;
			return this;
		}

		@JsonIgnore
		public String getUserId() {
			return userId;
		}

		@JsonIgnore
		public void setUserId(String userId) {
			this.userId = userId;
		}

		@JsonIgnore
		public List<String> getCollectionIds() {
			return collectionIds;
		}

		@JsonIgnore
		public InventoryRecord withCollectionIds(List<String> collectionIds) {
			this.collectionIds = collectionIds;
			return this;
		}

		@JsonIgnore
		public String getDonorUserId() {
			// If this card is not currently in its owner's collection, it must be donated
			if (getCollectionIds().contains(getUserId())) {
				return null;
			} else {
				return getUserId();
			}
		}

		@JsonIgnore
		public String getAllianceId() {
			return this.allianceId;
		}

		@JsonIgnore
		public void setAllianceId(String allianceId) {
			this.allianceId = allianceId;
		}

		@JsonIgnore
		public boolean isBorrowed() {
			return borrowedByUserId != null;
		}

		@JsonIgnore
		public String getBorrowedByUserId() {
			return borrowedByUserId;
		}

		@JsonIgnore
		public void setBorrowedByUserId(String borrowedByUserId) {
			this.borrowedByUserId = borrowedByUserId;
		}

		@JsonIgnore
		public Map<String, Object> getFacts() {
			return facts;
		}

		@JsonIgnore
		public void setFacts(Map<String, Object> facts) {
			this.facts = facts;
		}

		@JsonIgnore
		public String getCardId() {
			return cardDesc != null && cardDesc.getId() != null
					? cardDesc.getId()
					: null;
		}

		@JsonIgnore
		public Object getPersistentAttribute(Attribute attribute) {
			return getFacts().getOrDefault(ParseUtils.toCamelCase(attribute.toString()), null);
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public <T> T getPersistentAttribute(Attribute attribute, T defaultValue) {
			return (T) getFacts().getOrDefault(attribute.toKeyCase(), defaultValue);
		}

		@JsonIgnore
		public void putPersistentAttribute(Attribute attribute, Object value) {
			getFacts().put(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, attribute.toString()), value);
		}

		@JsonIgnore
		public AttributeMap getPersistentAttributes() {
			Map<Attribute, Object> collect = getFacts().entrySet().stream().collect(toMap(kv -> Attribute.valueOf
					(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, kv.getKey())), Map.Entry::getValue));
			if (collect.isEmpty()) {
				return new AttributeMap();
			}
			return new AttributeMap(collect);
		}
	}

	/**
	 * A LoginToken contains the expiration date and token string to be passed to API calls by clients.
	 */
//	// 
	public final static class LoginToken implements Serializable {
		private Date expiresAt;
		private String token;

		/**
		 * Create a secure token for the given user ID. Uses java.security.SecureRandom internally with a year-long
		 * expiration.
		 *
		 * @param userId The user ID to create the token for. This becomes the public part / access key of the token.
		 * @return A LoginToken object.
		 */
		public static LoginToken createSecure(String userId) {
			SecureRandom random = new SecureRandom();
			return new LoginToken(expiration(), userId, new BigInteger(130, random).toString(32));
		}

		public static Date expiration() {
			return Date.from(Instant.now().plus(60L * 60L * 24L * 365L, ChronoUnit.SECONDS));
		}

		protected LoginToken() {
		}

		/**
		 * Get the secret portion of the token string.
		 *
		 * @return The token secret.
		 */
		@JsonIgnore
		public String getSecret() {
			return this.token.split(":")[1];
		}

		/**
		 * Get the public portion of the token string (typically the User ID).
		 *
		 * @return The token access key.
		 */
		@JsonIgnore
		public String getAccessKey() {
			return this.token.split(":")[0];
		}

		protected LoginToken(Date expiresAt, String accessKey, String secret) {
			this.expiresAt = expiresAt;
			this.token = accessKey + ":" + secret;
		}

		/**
		 * Gets the complete login token clients should use in the HTTP API's X-Auth-Token header.
		 *
		 * @return The token.
		 */
		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		/**
		 * Gets the token's expiration date.
		 *
		 * @return The token's expiration date.
		 */
		public Date getExpiresAt() {
			return expiresAt;
		}

		public void setExpiresAt(Date expiresAt) {
			this.expiresAt = expiresAt;
		}
	}

	/**
	 * A base class for data that should be stored as a top-level document in mongo.
	 */
	public abstract static class MongoRecord implements Serializable {
		public static final String ID = "_id";
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty(value = ID)
		public String _id;

		@JsonIgnore
		public String getId() {
			return _id;
		}

		protected MongoRecord() {
		}

		public MongoRecord(String id) {
			this._id = id;
		}

		@Override
		public int hashCode() {
			return _id.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}

			if (!(other instanceof MongoRecord)) {
				return false;
			}

			MongoRecord rhs = (MongoRecord) other;
			return this._id.equals(rhs._id);
		}
	}

	//	//
	public final static class PasswordRecord implements Serializable {
		private String bcrypt;
		private String scrypt;

		public String getBcrypt() {
			return bcrypt;
		}

		public void setBcrypt(String bcrypt) {
			this.bcrypt = bcrypt;
		}

		public String getScrypt() {
			return scrypt;
		}

		public void setScrypt(String scrypt) {
			this.scrypt = scrypt;
		}
	}

	// 
	public final static class ResumeRecord implements Serializable {
		private List<HashedLoginTokenRecord> loginTokens;

		public List<HashedLoginTokenRecord> getLoginTokens() {
			return loginTokens;
		}

		public void setLoginTokens(List<HashedLoginTokenRecord> loginTokens) {
			this.loginTokens = loginTokens;
		}
	}

	/**
	 * An ServicesRecord contains the sensitive information the server needs to authenticate clients.
	 * <p>
	 * In this protocol, user passwords are hashed with sha256. The hash is stored on the server. Later, when the user
	 * logs in with an email and password, the password is used with the Scrypt hash comparison function. If the password
	 * matches the hash, a LoginToken is issued to the client.
	 * <p>
	 * The {@link LoginToken} contains a public (user ID) and private (randomly generated string) portion. The server
	 * stores an Scrypt hash of the private portion of the LoginToken. When the user authenticates using a token, the
	 * accounts security system finds the appropriate user record with the public (user ID) field of the token. Then it
	 * compares the secret to the stored hash of the secret. If it matches, the client is authorized.
	 * <p>
	 * No secrets are ever leaked by this object. The stored data cannot be used by the public API to authenticate the
	 * user, since the original secrets pre-hash (either user password or randomly generated token) were never stored.
	 */
//	// 
	public final static class ServicesRecord implements Serializable {
		private PasswordRecord password;
		private ResumeRecord resume;

		public PasswordRecord getPassword() {
			return password;
		}

		public void setPassword(PasswordRecord password) {
			this.password = password;
		}

		public ResumeRecord getResume() {
			return resume;
		}

		public void setResume(ResumeRecord resume) {
			this.resume = resume;
		}
	}

	/**
	 * A user record.
	 * <p>
	 * The fields in this object correspond to the ones stored in Mongo. This also implements the Vertx User interface,
	 * which allows queries to see if a user is authorized to do a particular task (very lightly implemented).
	 */
//	// 
	public final static class UserRecord extends MongoRecord implements User, Serializable {

		public static final String EMAILS_ADDRESS = "emails.address";
		public static final String SERVICES = "services";
		public static final String RESUME = "resume";
		public static final String LOGIN_TOKENS = "loginTokens";
		public static final String SERVICES_RESUME_LOGIN_TOKENS = SERVICES + "." + RESUME + "." + LOGIN_TOKENS;
		public static final String SERVICES_PASSWORD_SCRYPT = "services.password.scrypt";
		public static final String BOT = "bot";

		private List<EmailRecord> emails = new ArrayList<>();
		private String username;
		private Date createdAt;
		private List<String> decks = new ArrayList<>();
		private List<String> roles = new ArrayList<>();
		private List<FriendRecord> friends = new ArrayList<>();
		private ServicesRecord services = new ServicesRecord();
		private Map<String, Object> attributes = new HashMap<>();
		private boolean bot;
		private String privacyToken;

		/**
		 * Creates an empty user record.
		 */
		public UserRecord() {
			super();
		}

		/**
		 * Creates a user record with the specified ID.
		 *
		 * @param id The user ID.
		 */
		public UserRecord(String id) {
			super(id);
		}

		@Override
		public JsonObject attributes() {
			return new JsonObject(attributes == null ? new HashMap<>() : attributes);
		}

		@Override
		public boolean expired() {
			return User.super.expired();
		}

		@Override
		public boolean expired(int leeway) {
			return User.super.expired(leeway);
		}

		@Override
		public <T> @Nullable T get(String key) {
			return User.super.get(key);
		}

		@Override
		public boolean containsKey(String key) {
			return User.super.containsKey(key);
		}

		@Override
		public Authorizations authorizations() {
			return User.super.authorizations();
		}

		@Override
		public User isAuthorized(Authorization authorization, Handler<AsyncResult<Boolean>> handler) {
			handler.handle(Future.succeededFuture(true));
			return this;
		}

		/**
		 * Authorization checks are not supported.
		 *
		 * @param authority
		 * @param resultHandler
		 * @return
		 */
		@Override
		@JsonIgnore
		public UserRecord isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
			throw new UnsupportedOperationException("authorities are not supported");
		}

		@Override
		@JsonIgnore
		public User clearCache() {
			return null;
		}

		@Override
		@JsonIgnore
		public JsonObject principal() {
			return new JsonObject().put("_id", this._id);
		}

		@Override
		@JsonIgnore
		public void setAuthProvider(AuthProvider authProvider) {
		}

		@Override
		public User merge(User other) {
			return null;
		}

		/**
		 * Gets a list of deck IDs associated with this user. Query the collection service for their complete contents.
		 *
		 * @return A list of deck IDs.
		 */
		public List<String> getDecks() {
			return decks;
		}


		/**
		 * get freinds list
		 *
		 * @return list of friend records
		 */
		public List<FriendRecord> getFriends() {
			return friends;
		}

		/**
		 * Sets the UserRecord's deck IDs. Does not have side effects.
		 *
		 * @param decks A list of deck IDs.
		 */
		public UserRecord setDecks(List<String> decks) {
			this.decks = decks;
			return this;
		}

		/**
		 * Does this user record belong to a bot?
		 *
		 * @return True if the user is actually a bot account user.
		 */
		public boolean isBot() {
			return bot;
		}

		/**
		 * Mark that this user record belongs to a bot.
		 *
		 * @param bot True if the record belongs to a bot.
		 */
		public UserRecord setBot(boolean bot) {
			this.bot = bot;
			return this;
		}

		/**
		 * Set user's friends list
		 */
		public UserRecord setFriends(List<FriendRecord> friends) {
			this.friends = friends;
			return this;
		}

		/**
		 * Check if given friendId belongs to a friend
		 *
		 * @param friendId
		 * @return true if friend with friendId exists
		 */
		public boolean isFriend(String friendId) {
			return this.friends.stream().anyMatch(friend -> friend.getFriendId().equals(friendId));
		}

		/**
		 * Get friend by friend Id
		 *
		 * @param friendId
		 * @return friend object if friends, null otherwise
		 */
		public FriendRecord getFriendById(String friendId) {
			return this.friends.stream().filter(friend -> friend.getFriendId().equals(friendId)).findFirst().orElse(null);
		}

		public List<EmailRecord> getEmails() {
			return emails;
		}

		public UserRecord setEmails(List<EmailRecord> emails) {
			this.emails = emails;
			return this;
		}

		public String getUsername() {
			return username;
		}

		public UserRecord setUsername(String username) {
			this.username = username;
			return this;
		}

		public Date getCreatedAt() {
			return createdAt;
		}

		public UserRecord setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public ServicesRecord getServices() {
			return services;
		}

		public UserRecord setServices(ServicesRecord services) {
			this.services = services;
			return this;
		}

		public String getPrivacyToken() {
			return privacyToken;
		}

		public UserRecord setPrivacyToken(String privacyToken) {
			this.privacyToken = privacyToken;
			return this;
		}

		/**
		 * Gets the non-default (non-player) roles that are additionally specified for this user.
		 *
		 * @return
		 */
		public List<String> getRoles() {
			return roles;
		}

		public UserRecord setRoles(List<String> roles) {
			this.roles = roles;
			return this;
		}
	}


	/**
	 * CardRecord
	 */
	// 
	public static class CardRecord implements Serializable {

		@JsonProperty("_id")
		private String id = null;

		@JsonProperty("entity")
		private Entity entity = null;

		@JsonProperty("userId")
		private String userId = null;

		@JsonProperty("collectionIds")
		private List<String> collectionIds = null;

		@JsonProperty("borrowedByUserId")
		private String borrowedByUserId = null;

		@JsonProperty("allianceId")
		private String allianceId = null;

		@JsonProperty("donorUserId")
		private String donorUserId = null;

		public CardRecord id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Get id
		 *
		 * @return id
		 **/

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public CardRecord entity(Entity entity) {
			this.entity = entity;
			return this;
		}

		/**
		 * Get entity
		 *
		 * @return entity
		 **/

		public Entity getEntity() {
			return entity;
		}

		public void setEntity(Entity entity) {
			this.entity = entity;
		}

		public CardRecord userId(String userId) {
			this.userId = userId;
			return this;
		}

		/**
		 * Get userId
		 *
		 * @return userId
		 **/

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public CardRecord collectionIds(List<String> collectionIds) {
			this.collectionIds = collectionIds;
			return this;
		}

		public CardRecord addCollectionIdsItem(String collectionIdsItem) {
			if (this.collectionIds == null) {
				this.collectionIds = new ArrayList<>();
			}
			this.collectionIds.add(collectionIdsItem);
			return this;
		}

		/**
		 * Get collectionIds
		 *
		 * @return collectionIds
		 **/

		public List<String> getCollectionIds() {
			return collectionIds;
		}

		public void setCollectionIds(List<String> collectionIds) {
			this.collectionIds = collectionIds;
		}

		public CardRecord borrowedByUserId(String borrowedByUserId) {
			this.borrowedByUserId = borrowedByUserId;
			return this;
		}

		/**
		 * Get borrowedByUserId
		 *
		 * @return borrowedByUserId
		 **/

		public String getBorrowedByUserId() {
			return borrowedByUserId;
		}

		public void setBorrowedByUserId(String borrowedByUserId) {
			this.borrowedByUserId = borrowedByUserId;
		}

		public CardRecord allianceId(String allianceId) {
			this.allianceId = allianceId;
			return this;
		}

		/**
		 * Get allianceId
		 *
		 * @return allianceId
		 **/

		public String getAllianceId() {
			return allianceId;
		}

		public void setAllianceId(String allianceId) {
			this.allianceId = allianceId;
		}

		public CardRecord donorUserId(String donorUserId) {
			this.donorUserId = donorUserId;
			return this;
		}

		/**
		 * Get donorUserId
		 *
		 * @return donorUserId
		 **/

		public String getDonorUserId() {
			return donorUserId;
		}

		public void setDonorUserId(String donorUserId) {
			this.donorUserId = donorUserId;
		}


		@Override
		public boolean equals(java.lang.Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			CardRecord cardRecord = (CardRecord) o;
			return Objects.equals(this.id, cardRecord.id) &&
					Objects.equals(this.entity, cardRecord.entity) &&
					Objects.equals(this.userId, cardRecord.userId) &&
					Objects.equals(this.collectionIds, cardRecord.collectionIds) &&
					Objects.equals(this.borrowedByUserId, cardRecord.borrowedByUserId) &&
					Objects.equals(this.allianceId, cardRecord.allianceId) &&
					Objects.equals(this.donorUserId, cardRecord.donorUserId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, entity, userId, collectionIds, borrowedByUserId, allianceId, donorUserId);
		}


		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class CardRecord {\n");

			sb.append("    id: ").append(toIndentedString(id)).append("\n");
			sb.append("    entity: ").append(toIndentedString(entity)).append("\n");
			sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
			sb.append("    collectionIds: ").append(toIndentedString(collectionIds)).append("\n");
			sb.append("    borrowedByUserId: ").append(toIndentedString(borrowedByUserId)).append("\n");
			sb.append("    allianceId: ").append(toIndentedString(allianceId)).append("\n");
			sb.append("    donorUserId: ").append(toIndentedString(donorUserId)).append("\n");
			sb.append("}");
			return sb.toString();
		}

		/**
		 * Convert the given object to string with each line indented by 4 spaces (except the first line).
		 */
		private String toIndentedString(java.lang.Object o) {
			if (o == null) {
				return "null";
			}
			return o.toString().replace("\n", "\n    ");
		}

	}

	public enum CardType {

		HERO("HERO"),

		MINION("MINION"),

		SPELL("SPELL"),

		WEAPON("WEAPON"),

		HERO_POWER("HERO_POWER"),

		GROUP("GROUP"),

		CHOOSE_ONE("CHOOSE_ONE"),

		ENCHANTMENT("ENCHANTMENT"),

		CLASS("CLASS"),

		FORMAT("FORMAT");

		private String value;

		CardType(String value) {
			this.value = value;
		}

		@JsonValue
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static CardType fromValue(String text) {
			for (CardType b : CardType.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	//
	public final static class Entity {

		public Entity() {
		}

		private String cardId;

		public String getCardId() {
			return cardId;
		}

		public Entity setCardId(String cardId) {
			this.cardId = cardId;
			return this;
		}

		public Entity cardId(String cardId) {
			this.cardId = cardId;
			return this;
		}
	}

	// 
	public final static class InventoryCollection implements Serializable {

		@JsonProperty("_id")
		private String id = null;

		@JsonProperty("userId")
		private String userId = null;

		@JsonProperty("name")
		private String name = null;

		@JsonProperty("heroClass")
		private String heroClass = null;

		@JsonProperty("format")
		private String format = null;

		/**
		 * The type of collection this object is. A user&#39;s personal collection is of type USER. A deck is of type DECK.
		 */
		public enum TypeEnum {
			USER("USER"),

			ALLIANCE("ALLIANCE"),

			DECK("DECK");

			private String value;

			TypeEnum(String value) {
				this.value = value;
			}

			@JsonValue
			public String getValue() {
				return value;
			}

			@Override
			public String toString() {
				return String.valueOf(value);
			}

			@JsonCreator
			public static TypeEnum fromValue(String text) {
				for (TypeEnum b : TypeEnum.values()) {
					if (String.valueOf(b.value).equals(text)) {
						return b;
					}
				}
				return null;
			}
		}

		@JsonProperty("type")
		private TypeEnum type = null;

		/**
		 * Indicates whether this is a deck meant for draft or constructed play.
		 */
		public enum DeckTypeEnum {
			DRAFT("DRAFT"),

			CONSTRUCTED("CONSTRUCTED");

			private String value;

			DeckTypeEnum(String value) {
				this.value = value;
			}

			@JsonValue
			public String getValue() {
				return value;
			}

			@Override
			public String toString() {
				return String.valueOf(value);
			}

			@JsonCreator
			public static DeckTypeEnum fromValue(String text) {
				for (DeckTypeEnum b : DeckTypeEnum.values()) {
					if (String.valueOf(b.value).equals(text)) {
						return b;
					}
				}
				return null;
			}
		}

		@JsonProperty("deckType")
		private DeckTypeEnum deckType = null;

		@JsonProperty("isStandardDeck")
		private Boolean isStandardDeck = null;

		@JsonProperty("inventory")
		private List<CardRecord> inventory = null;

		@JsonProperty("playerEntityAttributes")
		private List<Spellsource.AttributeValueTuple> playerEntityAttributes = null;

		public InventoryCollection id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * The identifier of this collection. Corresponds to a deckId when this is a deck collection.
		 *
		 * @return id
		 **/

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public InventoryCollection userId(String userId) {
			this.userId = userId;
			return this;
		}

		/**
		 * The owner of this collection.
		 *
		 * @return userId
		 **/

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public InventoryCollection name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * The name of this collection. Corresponds to the deck name when this is a deck collection.
		 *
		 * @return name
		 **/

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public InventoryCollection heroClass(String heroClass) {
			this.heroClass = heroClass;
			return this;
		}

		/**
		 * The hero class when this is a deck collection.
		 *
		 * @return heroClass
		 **/

		public String getHeroClass() {
			return heroClass;
		}

		public void setHeroClass(String heroClass) {
			this.heroClass = heroClass;
		}

		public InventoryCollection format(String format) {
			this.format = format;
			return this;
		}

		/**
		 * The format when this is a deck collection.
		 *
		 * @return format
		 **/

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public InventoryCollection type(TypeEnum type) {
			this.type = type;
			return this;
		}

		/**
		 * The type of collection this object is. A user&#39;s personal collection is of type USER. A deck is of type DECK.
		 *
		 * @return type
		 **/

		public TypeEnum getType() {
			return type;
		}

		public void setType(TypeEnum type) {
			this.type = type;
		}

		public InventoryCollection deckType(DeckTypeEnum deckType) {
			this.deckType = deckType;
			return this;
		}

		/**
		 * Indicates whether this is a deck meant for draft or constructed play.
		 *
		 * @return deckType
		 **/

		public DeckTypeEnum getDeckType() {
			return deckType;
		}

		public void setDeckType(DeckTypeEnum deckType) {
			this.deckType = deckType;
		}

		public InventoryCollection isStandardDeck(Boolean isStandardDeck) {
			this.isStandardDeck = isStandardDeck;
			return this;
		}

		/**
		 * When true, indicates this is a standard deck provided by the server.
		 *
		 * @return isStandardDeck
		 **/

		public Boolean isIsStandardDeck() {
			return isStandardDeck;
		}

		public void setIsStandardDeck(Boolean isStandardDeck) {
			this.isStandardDeck = isStandardDeck;
		}

		public InventoryCollection inventory(List<CardRecord> inventory) {
			this.inventory = inventory;
			return this;
		}

		public InventoryCollection addInventoryItem(CardRecord inventoryItem) {
			if (this.inventory == null) {
				this.inventory = new ArrayList<>();
			}
			this.inventory.add(inventoryItem);
			return this;
		}

		/**
		 * Get inventory
		 *
		 * @return inventory
		 **/

		public List<CardRecord> getInventory() {
			return inventory;
		}

		public void setInventory(List<CardRecord> inventory) {
			this.inventory = inventory;
		}

		/**
		 * Get validationReport
		 *
		 * @return validationReport
		 **/


		public InventoryCollection playerEntityAttributes(List<Spellsource.AttributeValueTuple> playerEntityAttributes) {
			this.playerEntityAttributes = playerEntityAttributes;
			return this;
		}

		public InventoryCollection addPlayerEntityAttributesItem(Spellsource.AttributeValueTuple playerEntityAttributesItem) {
			if (this.playerEntityAttributes == null) {
				this.playerEntityAttributes = new ArrayList<>();
			}
			this.playerEntityAttributes.add(playerEntityAttributesItem);
			return this;
		}

		/**
		 * A list of player entity attributes associated with this deck.  A player entity attribute is an attribute that
		 * comes into play before the game starts. It is used to implement the Signature spell of the Ringmaster class.
		 *
		 * @return playerEntityAttributes
		 **/

		public List<Spellsource.AttributeValueTuple> getPlayerEntityAttributes() {
			return playerEntityAttributes;
		}

		public void setPlayerEntityAttributes(List<Spellsource.AttributeValueTuple> playerEntityAttributes) {
			this.playerEntityAttributes = playerEntityAttributes;
		}


		@Override
		public boolean equals(java.lang.Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			InventoryCollection inventoryCollection = (InventoryCollection) o;
			return Objects.equals(this.id, inventoryCollection.id) &&
					Objects.equals(this.userId, inventoryCollection.userId) &&
					Objects.equals(this.name, inventoryCollection.name) &&
					Objects.equals(this.heroClass, inventoryCollection.heroClass) &&
					Objects.equals(this.format, inventoryCollection.format) &&
					Objects.equals(this.type, inventoryCollection.type) &&
					Objects.equals(this.deckType, inventoryCollection.deckType) &&
					Objects.equals(this.isStandardDeck, inventoryCollection.isStandardDeck) &&
					Objects.equals(this.inventory, inventoryCollection.inventory) &&
					Objects.equals(this.playerEntityAttributes, inventoryCollection.playerEntityAttributes);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, userId, name, heroClass, format, type, deckType, isStandardDeck, inventory, playerEntityAttributes);
		}


		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class InventoryCollection {\n");

			sb.append("    id: ").append(toIndentedString(id)).append("\n");
			sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
			sb.append("    name: ").append(toIndentedString(name)).append("\n");
			sb.append("    heroClass: ").append(toIndentedString(heroClass)).append("\n");
			sb.append("    format: ").append(toIndentedString(format)).append("\n");
			sb.append("    type: ").append(toIndentedString(type)).append("\n");
			sb.append("    deckType: ").append(toIndentedString(deckType)).append("\n");
			sb.append("    isStandardDeck: ").append(toIndentedString(isStandardDeck)).append("\n");
			sb.append("    inventory: ").append(toIndentedString(inventory)).append("\n");
			sb.append("    playerEntityAttributes: ").append(toIndentedString(playerEntityAttributes)).append("\n");
			sb.append("}");
			return sb.toString();
		}

		/**
		 * Convert the given object to string with each line indented by 4 spaces (except the first line).
		 */
		private String toIndentedString(java.lang.Object o) {
			if (o == null) {
				return "null";
			}
			return o.toString().replace("\n", "\n    ");
		}

	}

	// 
	public final static class Friend implements Serializable {

		@JsonProperty("presence")
		private Spellsource.PresenceMessage.Presence presence = null;

		@JsonProperty("friendId")
		private String friendId = null;

		@JsonProperty("since")
		private Long since = null;

		@JsonProperty("friendName")
		private String friendName = null;

		public Friend presence(Spellsource.PresenceMessage.Presence presence) {
			this.presence = presence;
			return this;
		}

		public Spellsource.PresenceMessage.Presence getPresence() {
			return presence;
		}

		public void setPresence(Spellsource.PresenceMessage.Presence presence) {
			this.presence = presence;
		}

		public Friend friendId(String friendId) {
			this.friendId = friendId;
			return this;
		}

		/**
		 * Get friendId
		 *
		 * @return friendId
		 **/
		public String getFriendId() {
			return friendId;
		}

		public void setFriendId(String friendId) {
			this.friendId = friendId;
		}

		public Friend since(Long since) {
			this.since = since;
			return this;
		}

		/**
		 * Get since
		 *
		 * @return since
		 **/
		public Long getSince() {
			return since;
		}

		public void setSince(Long since) {
			this.since = since;
		}

		public Friend friendName(String friendName) {
			this.friendName = friendName;
			return this;
		}

		/**
		 * Get friendName
		 *
		 * @return friendName
		 **/
		public String getFriendName() {
			return friendName;
		}

		public void setFriendName(String friendName) {
			this.friendName = friendName;
		}


		@Override
		public boolean equals(java.lang.Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Friend friend = (Friend) o;
			return Objects.equals(this.presence, friend.presence) &&
					Objects.equals(this.friendId, friend.friendId) &&
					Objects.equals(this.since, friend.since) &&
					Objects.equals(this.friendName, friend.friendName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(presence, friendId, since, friendName);
		}


		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("class Friend {\n");

			sb.append("    presence: ").append(toIndentedString(presence)).append("\n");
			sb.append("    friendId: ").append(toIndentedString(friendId)).append("\n");
			sb.append("    since: ").append(toIndentedString(since)).append("\n");
			sb.append("    friendName: ").append(toIndentedString(friendName)).append("\n");
			sb.append("}");
			return sb.toString();
		}

		/**
		 * Convert the given object to string with each line indented by 4 spaces (except the first line).
		 */
		private String toIndentedString(java.lang.Object o) {
			if (o == null) {
				return "null";
			}
			return o.toString().replace("\n", "\n    ");
		}
	}

	private static class FriendRecordWithId extends FriendRecord {
		private final String id;

		public FriendRecordWithId(String id, FriendRecord other) {
			this.id = id;
			setFriendId(other.getFriendId());
			setSince(other.getSince());
		}

		public String getId() {
			return id;
		}
	}
}
