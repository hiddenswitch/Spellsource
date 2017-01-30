package com.hiddenswitch.proto3.net.amazon;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.hiddenswitch.proto3.net.util.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.io.Serializable;
import java.lang.ref.WeakReference;

@DynamoDBTable(tableName = "users")
public class UserRecord implements User, Serializable {
	private String id;
	private Profile profile;
	private transient WeakReference<AuthProvider> authProvider;

	@DynamoDBHashKey
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@DynamoDBAttribute
	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@Override
	@DynamoDBIgnore
	public User isAuthorised(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
		resultHandler.handle(new Result<>(true));
		return this;
	}

	@Override
	@DynamoDBIgnore
	public User clearCache() {
		return null;
	}

	@Override
	@DynamoDBIgnore
	public JsonObject principal() {
		return new JsonObject().put("username", id);
	}

	@Override
	@DynamoDBIgnore
	public void setAuthProvider(AuthProvider authProvider) {
		this.authProvider = new WeakReference<>(authProvider);
	}
}
