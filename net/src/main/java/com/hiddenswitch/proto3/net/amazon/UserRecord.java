package com.hiddenswitch.proto3.net.amazon;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hiddenswitch.proto3.net.impl.util.MongoRecord;
import com.hiddenswitch.proto3.net.util.Result;
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

public class UserRecord extends MongoRecord implements User, Serializable {
	private Profile profile;
	private AuthorizationRecord auth;
	private List<String> decks;

	public UserRecord() {
		super();
	}

	public UserRecord(String id) {
		super(id);
	}

	@JsonIgnore
	private transient WeakReference<AuthProvider> authProvider;

	public Profile getProfile() {
		return profile;
	}

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

	public AuthorizationRecord getAuth() {
		return auth;
	}

	public void setAuth(AuthorizationRecord auth) {
		this.auth = auth;
	}

	public List<String> getDecks() {
		return decks;
	}

	public void setDecks(List<String> decks) {
		this.decks = decks;
	}
}
