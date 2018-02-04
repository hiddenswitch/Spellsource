package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.hiddenswitch.spellsource.util.QuickJson;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardParser;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.utils.AttributeMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bberman on 1/22/17.
 */
public class InventoryRecord extends MongoRecord {
	@JsonProperty
	private Map<String, Object> cardDesc;

	/**
	 * The userId of the player who originally opened the pack containing the card.
	 */
	@JsonProperty
	private String userId;

	/**
	 * The ID of the alliance this card belongs to, or null if this card is not shared with an alliance. The ID is also
	 * a collection ID.
	 */
	@JsonProperty
	private String allianceId;

	@JsonProperty
	private List<String> collectionIds;

	@JsonProperty
	private String borrowedByUserId;

	@JsonProperty
	private Map<String, Object> facts = new HashMap<>();

	@JsonIgnore
	private transient CardDesc cardDescCached;

	protected InventoryRecord() {
	}

	public InventoryRecord(Card card) {
		this(card.getDesc());
	}

	public InventoryRecord(JsonObject card) {
		this.cardDesc = card.getMap();
	}

	public InventoryRecord(String id, JsonObject card) {
		super(id);
		this.cardDesc = card.getMap();
	}

	public InventoryRecord(CardDesc cardDesc) {
		this();
		this.cardDesc = QuickJson.toJson(cardDesc).getMap();
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
	public InventoryRecord withUserId(final String userId) {
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
	public InventoryRecord withCollectionIds(List<String> collectionIds) {
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
	public String getCardId() {
		return (String) cardDesc.get("id");
	}

	public Object getPersistentAttribute(Attribute attribute) {
		return getFacts().getOrDefault(ParseUtils.toCamelCase(attribute.toString()), null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getPersistentAttribute(Attribute attribute, T defaultValue) {
		return (T) getFacts().getOrDefault(attribute.toKeyCase(), defaultValue);
	}

	public void putPersistentAttribute(Attribute attribute, Object value) {
		getFacts().put(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, attribute.toString()), value);
	}

	public AttributeMap getPersistentAttributes() {
		return new AttributeMap(getFacts().entrySet().stream().collect(Collectors.toMap(kv -> Attribute.valueOf
				(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, kv.getKey())), Map.Entry::getValue)));
	}
}

