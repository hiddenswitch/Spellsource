package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

/**
 * A user record.
 * <p>
 * The fields in this object correspond to the ones stored in Mongo. This also implements the Vertx User interface,
 * which allows queries to see if a user is authorized to do a particular task (very lightly implemented).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRecord extends MongoRecord implements User, Serializable, ClusterSerializable {
	public static final String EMAILS_ADDRESS = "emails.address";
	public static final String SERVICES = "services";
	public static final String RESUME = "resume";
	public static final String LOGIN_TOKENS = "loginTokens";
	public static final String SERVICES_RESUME_LOGIN_TOKENS = SERVICES + "." + RESUME + "." + LOGIN_TOKENS;

	private List<EmailRecord> emails;
	private String username;
	private Date createdAt;
	private List<String> decks;
	private List<FriendRecord> friends;
	private ServicesRecord services;
	private boolean bot;

	/**
	 * A weak reference to the auth provider, automatically connected by Vertx.
	 */
	@JsonIgnore
	private transient WeakReference<AuthProvider> authProvider;

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
	@JsonIgnore
	public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
		resultHandler.handle(Future.succeededFuture(true));
		return this;
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
		this.authProvider = new WeakReference<>(authProvider);
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
	public void setDecks(List<String> decks) {
		this.decks = decks;
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
	public void setBot(boolean bot) {
		this.bot = bot;
	}

	/**
	 * Set user's friends list
	 *
	 * @return
	 */
	public void setFriends(List<FriendRecord> friends) {
		this.friends = friends;
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

	@Override
	public void writeToBuffer(Buffer buffer) {
		Json.encodeToBuffer(this).writeToBuffer(buffer);
	}

	@Override
	public int readFromBuffer(int pos, Buffer buffer) {
		throw new UnsupportedOperationException();
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<EmailRecord> getEmails() {
		return emails;
	}

	public void setEmails(List<EmailRecord> emails) {
		this.emails = emails;
	}

	public ServicesRecord getServices() {
		return services;
	}

	public void setServices(ServicesRecord services) {
		this.services = services;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}