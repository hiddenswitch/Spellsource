package com.hiddenswitch.proto3.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.util.List;

import static com.hiddenswitch.proto3.net.util.QuickJson.fromJson;
import static com.hiddenswitch.proto3.net.util.QuickJson.toJson;

/**
 * Created by bberman on 1/22/17.
 */
public class CardRecord extends MongoRecord {
	@JsonProperty
	private JsonObject cardDesc;

	@JsonProperty
	private String cardDescClass;

	@JsonProperty
	private String userId;

	@JsonProperty
	private List<String> collectionIds;

	@JsonIgnore
	private transient CardDesc cardDescCached;

	protected CardRecord() {
	}

	public CardRecord(Card card) {
		this(card.getOriginalDesc());
	}

	public CardRecord(CardDesc cardDesc) {
		this();
		this.cardDesc = toJson(cardDesc);
		this.cardDescClass = cardDesc.getClass().getName();
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public CardDesc getCardDesc() {
		if (cardDescCached == null) {
			try {
				cardDescCached = fromJson(cardDesc, (Class<? extends CardDesc>) Class.forName(cardDescClass));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException();
			}
		}
		return cardDescCached;
	}

	@JsonIgnore
	public String getCardDescClass() {
		return cardDescClass;
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
}

