package com.hiddenswitch.proto3.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardParser;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.hiddenswitch.proto3.net.util.QuickJson.toJson;

/**
 * Created by bberman on 1/22/17.
 */
public class CardRecord extends MongoRecord {
	@JsonProperty
	private Map<String, Object> cardDesc;

	/**
	 * The userId of the player who originally opened the pack containing the card.
	 */
	@JsonProperty
	private String userId;

	/**
	 * The ID of the alliance this card belongs to, or null if this card is not shared with an alliance.
	 * The ID is also a collection ID.
	 */
	@JsonProperty
	private String allianceId;

	@JsonProperty
	private List<String> collectionIds;

	@JsonProperty
	private String borrowedByUserId;

	@JsonProperty
	private Map<String, Object> facts;

	@JsonIgnore
	private transient CardDesc cardDescCached;
	private int firstTimePlays;

	protected CardRecord() {
	}

	public CardRecord(Card card) {
		this(card.getOriginalDesc());
	}

	public CardRecord(JsonObject card) {
		this.cardDesc = card.getMap();
	}

	public CardRecord(CardDesc cardDesc) {
		this();
		this.cardDesc = toJson(cardDesc).getMap();
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public CardDesc getCardDesc() {
		if (cardDescCached == null) {
			try {
				cardDescCached = CardParser.parseCard(new JsonObject(cardDesc)).getDesc();
			} catch (IOException e) {
				throw new RuntimeException();
			}
		}
		return cardDescCached;
	}

	@JsonIgnore
	public CardRecord withUserId(final String userId) {
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
	public CardRecord withCollectionIds(List<String> collectionIds) {
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
	public Map<String, Object> getCardDescMap() {
		return cardDesc;
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
	public int getFirstTimePlays() {
		return (int) facts.getOrDefault("uniqueChampionIdsSize", 0);
	}
}

