package com.hiddenswitch.minionate;

import com.hiddenswitch.minionate.tasks.ApiTask;
import com.hiddenswitch.proto3.net.client.ApiCallback;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.*;
import com.sun.javafx.collections.ObservableListWrapper;
import io.vertx.core.json.JsonObject;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardParser;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckWithId;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.utils.AttributeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableArrayList;

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
	private SimpleListProperty<CardRecord> cardRecords = new SimpleListProperty<>(observableArrayList());
	private SimpleListProperty<InventoryCollection> decks = new SimpleListProperty<>(observableArrayList());
	private TokenProperty token = new TokenProperty("token");
	private Map<String, Card> cardParseCache = new HashMap<>();

	public ReadOnlyObjectProperty<Account> getAccount() {
		return account;
	}

	public TokenProperty getToken() {
		return token;
	}

	public ObservableList<CardRecord> getCardRecords() {
		return cardRecords.get();
	}

	public ReadOnlyListProperty<CardRecord> cardRecordsProperty() {
		return cardRecords;
	}

	public ObservableList<InventoryCollection> getDecks() {
		return decks.get();
	}

	public SimpleListProperty<InventoryCollection> decksProperty() {
		return decks;
	}

	public Card parseCard(CardRecord cardRecord) {
		if (!cardParseCache.containsKey(cardRecord.getId())) {
			cacheCard(cardRecord);
		}

		return cardParseCache.get(cardRecord.getId());
	}

	public DeckWithId parseDeck(InventoryCollection collection) {
		DeckWithId deck = new DeckWithId(collection.getId());
		deck.setName(collection.getName());
		deck.setHeroClass(HeroClass.valueOf(collection.getHeroClass()));
		collection.getInventory().stream().map(this::parseCard).forEach(deck.getCards()::add);
		return deck;
	}

	private void cacheCard(CardRecord cardRecord) {
		try {
			final CardDesc desc = CardParser.parseCard(new JsonObject(cardRecord.getCardDesc())).getDesc();
			// TODO: Make sure that the new card attributes are parsed correctly in general
			if (desc.attributes == null) {
				desc.attributes = new AttributeMap();
			}
			desc.attributes.put(Attribute.CARD_INVENTORY_ID, cardRecord.getId());
			cardParseCache.put(cardRecord.getId(), desc.createInstance());
		} catch (IOException e) {
			cardParseCache.put(cardRecord.getId(), null);
		}
	}

	public void loadAccount() {
		String loginToken = getToken().getValue();
		if (!loginToken.isEmpty()) {
			// Try to use the token. If we get an unauthorized error, we have to "logout"
			DefaultApi api = new DefaultApi();
			// TODO: Load initial account data from disk for convenience
			final String targetUserId = getToken().getUserId();
			if (account.get() == null) {
				setAccount(new Account().id(targetUserId));
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
						final Account account = result.getAccounts().get(0);
						setAccount(account);
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
		setAccount(null);

	}

	public ApiTask<CreateAccountResponse> createAccount(String username, String email, String password) {
		return new ApiTask<>(api -> {
			CreateAccountResponse response = api.createAccount(new CreateAccountRequest()
					.name(username)
					.email(email)
					.password(password));

			token.set(response.getLoginToken());
			final Account account = response.getAccount();
			setAccount(account);

			return response;
		});
	}

	private void setAccount(Account account) {
		this.account.set(account);
		if (account == null
				|| account.getPersonalCollection() == null) {
			cardRecords.setAll(emptyObservableList());
			decks.setAll(emptyObservableList());
		} else {
			// Cache all the cards since we're overwriting
			account.getPersonalCollection().getInventory().forEach(this::cacheCard);
			cardRecords.setAll(account.getPersonalCollection().getInventory());
			decks.setAll(account.getDecks());
		}
	}

	public ApiTask<LoginResponse> login(String email, String password) {
		return new ApiTask<>(api -> {
			LoginResponse response = api.login(new LoginRequest().email(email).password(password));

			token.set(response.getLoginToken());
			setAccount(response.getAccount());

			return response;
		});
	}

	public ApiTask<DecksGetResponse> addCardToDeck(String deckId, String cardInventoryId) {
		return new ApiTask<>(api -> {
			DecksGetResponse response = api.decksUpdate(deckId,
					new DecksUpdateCommand()
							.pushInventoryIds(new DecksUpdateCommandPushInventoryIds()
									.each(Collections.singletonList(cardInventoryId))));

			updateDecksWith(response);

			return response;
		});
	}

	private void updateDecksWith(DecksGetResponse response) {
		// Replace the deck
		for (int i = 0; i < decks.size(); i++) {
			if (decks.get(i).getId().equals(response.getCollection().getId())) {
				decks.set(i, response.getCollection());
				break;
			}
		}
	}


	public ApiTask<DecksGetResponse> removeCardFromDeck(String deckId, String cardInventoryId) {
		return new ApiTask<>(api -> {
			DecksGetResponse response = api.decksUpdate(deckId,
					new DecksUpdateCommand()
							.pullAllInventoryIds(Collections.singletonList(cardInventoryId)));

			// Replace the deck
			updateDecksWith(response);

			return response;
		});
	}

	public ApiTask<DecksGetResponse> renameDeck(String deckId, String newDeckName) {
		return new ApiTask<>(api -> {
			DecksGetResponse response = api.decksUpdate(deckId,
					new DecksUpdateCommand()
							.setName(newDeckName));

			updateDecksWith(response);

			return response;
		});
	}

	public ApiTask<Void> deleteDeck(String deckId) {
		return new ApiTask<>(api -> {
			api.decksDelete(deckId);

			// Delete the deck
			for (int i = 0; i < decks.size(); i++) {
				if (decks.get(i).getId().equals(deckId)) {
					decks.remove(i);
					break;
				}
			}

			return null;
		});
	}

	public ApiTask<DecksPutResponse> createDeck(HeroClass heroClass) {
		return new ApiTask<>(api -> {
			DecksPutResponse response = api.decksPut(new DecksPutRequest().heroClass(heroClass.toString()).name("Custom Deck"));

			decks.add(api.decksGet(response.getDeckId()).getCollection());

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
