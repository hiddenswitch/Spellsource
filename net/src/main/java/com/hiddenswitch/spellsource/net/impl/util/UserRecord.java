package com.hiddenswitch.spellsource.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorization;

import java.io.Serializable;
import java.util.*;

/**
 * A user record.
 * <p>
 * The fields in this object correspond to the ones stored in Mongo. This also implements the Vertx User interface,
 * which allows queries to see if a user is authorized to do a particular task (very lightly implemented).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRecord extends MongoRecord implements User, Serializable {

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