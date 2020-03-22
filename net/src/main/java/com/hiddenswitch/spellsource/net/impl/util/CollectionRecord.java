package com.hiddenswitch.spellsource.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hiddenswitch.spellsource.client.models.ValidationReport;
import com.hiddenswitch.spellsource.net.models.CollectionTypes;
import net.demilich.metastone.game.cards.AttributeMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A mongodb record of the user's collection metadata. Does not contain the inventory IDs themselves.
 */
public class CollectionRecord extends MongoRecord {
	public static final String FORMAT = "format";
	public static final String HERO_CLASS = "heroClass";
	public static final String TYPE = "type";
	public static final String USER_ID = "userId";
	public static final String VALIDATION_REPORT = "validationReport";
	public static final String HERO_CARD_ID = "heroCardId";
	private String userId;
	private CollectionTypes type;
	private boolean trashed;
	@JsonProperty
	private DeckType deckType;
	private List<String> friendUserIds;
	private boolean isStandardDeck;
	private int wins;
	private int totalGames;
	private ValidationReport validationReport;
	private Map<String, AttributeMap> inventoryAttributes;
	private AttributeMap playerEntityAttributes;

	/**
	 * Hero class for deck collection records.
	 */
	private String heroClass;

	/**
	 * Names for alliance and deck collection records.
	 */
	private String name;
	private String heroCardId;
	private String format;

	protected CollectionRecord() {
	}

	public CollectionRecord setId(final String id) {
		this._id = id;
		return this;
	}

	public boolean isTrashed() {
		return trashed;
	}

	public CollectionRecord setFriendUserIds(final List<String> friendUserIds) {
		this.friendUserIds = friendUserIds;
		return this;
	}

	@JsonIgnore
	public boolean isDraft() {
		return deckType == DeckType.DRAFT;
	}

	@JsonIgnore
	public void setDraft(boolean draft) {
		deckType = DeckType.DRAFT;
	}

	public CollectionRecord withDraft(final boolean draft) {
		if (draft) {
			deckType = DeckType.DRAFT;
		} else {
			deckType = DeckType.CONSTRUCTED;
		}
		return this;
	}

	public static CollectionRecord deck(final String userId, final String name, final String heroClass, final boolean draft) {
		return new CollectionRecord()
				.withDraft(draft)
				.setUserId(userId)
				.setName(name)
				.setHeroClass(heroClass)
				.setType(CollectionTypes.DECK);
	}

	public static CollectionRecord user(final String userId) {
		return new CollectionRecord()
				.setId(userId)
				.setUserId(userId)
				.setType(CollectionTypes.USER);
	}

	public static CollectionRecord alliance(String allianceId, String ownerUserId) {
		return new CollectionRecord()
				.setId(allianceId)
				.setType(CollectionTypes.ALLIANCE)
				.setUserId(ownerUserId)
				.setFriendUserIds(Arrays.asList(ownerUserId));
	}

	public int getWins() {
		return wins;
	}

	public CollectionRecord setWins(int wins) {
		this.wins = wins;
		return this;
	}

	public int getTotalGames() {
		return totalGames;
	}

	public CollectionRecord setTotalGames(int totalGames) {
		this.totalGames = totalGames;
		return this;
	}

	public CollectionRecord setTrashed(boolean trashed) {
		this.trashed = trashed;
		return this;
	}

	public DeckType getDeckType() {
		return deckType;
	}

	public CollectionRecord setDeckType(DeckType deckType) {
		this.deckType = deckType;
		return this;
	}

	public List<String> getFriendUserIds() {
		return friendUserIds;
	}

	public String getHeroCardId() {
		return heroCardId;
	}

	public CollectionRecord setHeroCardId(String heroCardId) {
		this.heroCardId = heroCardId;
		return this;
	}

	public String getFormat() {
		return format;
	}

	public CollectionRecord setFormat(String format) {
		this.format = format;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public CollectionRecord setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public CollectionTypes getType() {
		return type;
	}

	public CollectionRecord setType(CollectionTypes type) {
		this.type = type;
		return this;
	}

	public String getHeroClass() {
		return heroClass;
	}

	public CollectionRecord setHeroClass(String heroClass) {
		this.heroClass = heroClass;
		return this;
	}

	public String getName() {
		return name;
	}

	public CollectionRecord setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isStandardDeck() {
		return isStandardDeck;
	}

	public CollectionRecord setStandardDeck(boolean standardDeck) {
		isStandardDeck = standardDeck;
		return this;
	}

	public Map<String, AttributeMap> getInventoryAttributes() {
		return inventoryAttributes;
	}

	public CollectionRecord setInventoryAttributes(Map<String, AttributeMap> inventoryAttributes) {
		this.inventoryAttributes = inventoryAttributes;
		return this;
	}

	public CollectionRecord setValidationReport(ValidationReport validationReport) {
		this.validationReport = validationReport;
		return this;
	}

	public ValidationReport getValidationReport() {
		return validationReport;
	}

	/**
	 * Attributes that should be put on the player entity before the start of the game.
	 *
	 * @return
	 */
	public AttributeMap getPlayerEntityAttributes() {
		return playerEntityAttributes;
	}

	public CollectionRecord setPlayerEntityAttributes(AttributeMap playerEntityAttributes) {
		this.playerEntityAttributes = playerEntityAttributes;
		return this;
	}
}


