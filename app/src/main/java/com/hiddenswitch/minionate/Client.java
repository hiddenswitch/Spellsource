package com.hiddenswitch.minionate;

import com.hiddenswitch.minionate.tasks.ApiTask;
import com.hiddenswitch.proto3.net.client.ApiCallback;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by bberman on 2/18/17.
 */
public class Client {
	static Logger logger = LoggerFactory.getLogger(Client.class);
	private static Client instance;

	static {
		instance = new Client();
	}

	public static Client getInstance() {
		return instance;
	}

	private SimpleObjectProperty<Account> account = new SimpleObjectProperty<>();
	private TokenProperty token = new TokenProperty("token");

	public ReadOnlyObjectProperty<Account> getAccount() {
		return account;
	}

	public TokenProperty getToken() {
		return token;
	}

	public void loadAccount() {
		String loginToken = getToken().getValue();
		if (!loginToken.isEmpty()) {
			// Try to use the token. If we get an unauthorized error, we have to "logout"
			DefaultApi api = new DefaultApi();
			// TODO: Load initial account data from disk for convenience
			final String targetUserId = getToken().getUserId();
			if (account.get() == null) {
				account.set(new Account().id(targetUserId));
			}

			try {
				api.getAccountAsync(targetUserId, new ApiCallback<GetAccountsResponse>() {
					@Override
					public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
						// Logout.
						logger.error("Failed to login with the provided token. Token: {}", loginToken);
						getToken().set(null);
					}

					@Override
					public void onSuccess(GetAccountsResponse result, int statusCode, Map<String, List<String>> responseHeaders) {
						// Great! Maybe populate some basic stuff.
						account.set(result.getAccounts().get(0));
					}

					@Override
					public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
					}

					@Override
					public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
					}
				});
			} catch (ApiException serializationException) {
			}
		}
	}

	public void logout() {
		token.set(null);
		account.set(null);
	}

	public ApiTask<CreateAccountResponse> createAccount(String username, String email, String password) {
		return new ApiTask<>(api -> {
			CreateAccountResponse response = api.createAccount(new CreateAccountRequest()
					.name(username)
					.email(email)
					.password(password));

			token.set(response.getLoginToken());
			account.set(response.getAccount());

			return response;
		});
	}

	public ApiTask<LoginResponse> login(String email, String password) {
		return new ApiTask<>(api -> {
			LoginResponse response = api.login(new LoginRequest().email(email).password(password));

			token.set(response.getLoginToken());
			account.set(response.getAccount());

			return response;
		});
	}

	public class PreferencesStringProperty extends StringPropertyBase {
		private final String key;

		public PreferencesStringProperty(String key) {
			super(Preferences.userRoot().get(key, ""));
			Configuration.getDefaultApiClient().setApiKey(this.getValue());
			this.key = key;
		}

		@Override
		public void set(String newValue) {
			super.set(newValue);
			if (newValue == null) {
				newValue = "";
			}
			Preferences.userRoot().put(key, newValue);
			Configuration.getDefaultApiClient().setApiKey(newValue);
		}

		@Override
		public Object getBean() {
			return Client.this;
		}

		@Override
		public String getName() {
			return "Client";
		}
	}

	public class TokenProperty extends PreferencesStringProperty {
		public TokenProperty(String preferencesKey) {
			super(preferencesKey);
		}

		public String getUserId() {
			String value = this.get();
			if (value != null && !value.isEmpty()) {
				return value.split(":")[0];
			} else {
				return null;
			}
		}

		public String getSecret() {
			String value = this.get();
			if (value != null && !value.isEmpty()) {
				return value.split(":")[1];
			} else {
				return null;
			}
		}
	}
}
