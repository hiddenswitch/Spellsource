package com.hiddenswitch.proto3.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.hiddenswitch.proto3.net.util.QuickJson.json;

/**
 * A user record.
 *
 * The fields in this object correspond to the ones stored in Mongo. This also implements the Vertx User interface,
 * which allows queries to see if a user is authorized to do a particular task (very lightly implemented).
 */
public class UserRecord extends MongoRecord implements User, Serializable {
	private Profile profile;
	private AuthorizationRecord auth;
	private List<String> decks;
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
	 * @param id The user ID.
	 */
	public UserRecord(String id) {
		super(id);
	}

	/**
	 * Gets user profile information. This isn't safe to share with the public, because it contains the user's email
	 * address.
	 * @return A Profile object.
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Sets the user's profile.
	 * @param profile A Profile object.
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@Override
	@JsonIgnore
	public User isAuthorised(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
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
		return json(this);
	}

	@Override
	@JsonIgnore
	public void setAuthProvider(AuthProvider authProvider) {
		this.authProvider = new WeakReference<>(authProvider);
	}

	/**
	 * Gets the user's login tokens and password hash. Not public safe.
	 * @return An AuthorizationRecord object.
	 */
	public AuthorizationRecord getAuth() {
		return auth;
	}

	/**
	 * Sets the user's login token and password hash information.
	 * @param auth An AuthorizationRecord object.
	 */
	public void setAuth(AuthorizationRecord auth) {
		this.auth = auth;
	}

	/**
	 * Gets a list of deck IDs associated with this user. Query the collection service for their complete contents.
	 * @return A list of deck IDs.
	 */
	public List<String> getDecks() {
		return decks;
	}

	/**
	 * Sets the UserRecord's deck IDs. Does not have side effects.
	 * @param decks A list of deck IDs.
	 */
	public void setDecks(List<String> decks) {
		this.decks = decks;
	}

	/**
	 * Does this user record belong to a bot?
	 * @return True if the user is actually a bot account user.
	 */
	public boolean isBot() {
		return bot;
	}

	/**
	 * Mark that this user record belongs to a bot.
	 * @param bot True if the record belongs to a bot.
	 */
	public void setBot(boolean bot) {
		this.bot = bot;
	}
}
