package com.hiddenswitch.framework.migrations;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multisets;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.tables.records.FriendsRecord;
import com.hiddenswitch.spellsource.client.models.CardRecord;
import com.hiddenswitch.spellsource.client.models.Entity;
import com.hiddenswitch.spellsource.net.Inventory;
import com.hiddenswitch.spellsource.net.impl.util.FriendRecord;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import com.hiddenswitch.spellsource.net.models.GetCollectionRequest;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.InsertValuesStepN;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import static com.hiddenswitch.framework.schema.keycloak.Tables.USER_ENTITY;
import static com.hiddenswitch.framework.schema.spellsource.Tables.FRIENDS;
import static com.hiddenswitch.framework.schema.spellsource.Tables.USER_ENTITY_ADDONS;
import static io.vertx.core.CompositeFuture.all;
import static io.vertx.ext.sync.Sync.*;

public class V8__Migrate_from_previous_server extends BaseJavaMigration {

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
		await(fiber(vertx, () -> {
			var executor = Environment.queryExecutor();
			var mongoClient = MongoClient.create(vertx, new JsonObject().put("connection_string", mongoUri));
			// retrieve all non bot accounts
			var accountJsonStream = mongoClient.findBatch(com.hiddenswitch.spellsource.net.Accounts.USERS, new JsonObject()
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
			for (var accountJson : await(accountJsonStream)) {
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

				// migrate the privacy token
				var privacyToken = userRecord.getPrivacyToken();
				if (privacyToken != null && !privacyToken.isEmpty()) {
					await(executor.execute(dsl ->
							dsl.insertInto(USER_ENTITY_ADDONS).set(USER_ENTITY_ADDONS.newRecord()
									.setId(userEntity.getId())
									.setPrivacyToken(privacyToken))));
				}

				// migrate the decks
				if (userRecord.getDecks() != null && !userRecord.getDecks().isEmpty()) {
					for (var deckId : userRecord.getDecks()) {
						var getCollectionResponse = Inventory.getCollection(GetCollectionRequest.deck(deckId));
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
			}

			if (!friends.isEmpty()) {
				// now that we've created all the accounts, the friend constraints should be okay
				executor.execute(dsl -> {
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
				});
			}

			return (Void) null;
		}));
	}

	private String getMongoUrl() {
		return System.getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", ""));
	}
}
