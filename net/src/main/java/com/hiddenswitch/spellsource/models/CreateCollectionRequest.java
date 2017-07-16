package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCollectionRequest implements Serializable {
	private CollectionTypes type;
	private String userId;
	private List<String> userIds;
	private String allianceId;
	private List<String> cardIds;
	private List<String> inventoryIds;
	private String name;
	private QueryCardsRequest queryCardsRequest;
	private HeroClass heroClass;
	private boolean draft;
	private int copies = 1;

	protected CreateCollectionRequest() {
	}

	public static CreateCollectionRequest deck(String userId, String name, HeroClass heroClass, List<String> inventoryIds, boolean draft) {
		return new CreateCollectionRequest()
				.withType(CollectionTypes.DECK)
				.withName(name)
				.withUserId(userId)
				.withHeroClass(heroClass)
				.withInventoryIds(inventoryIds)
				.withDraft(draft);
	}

	public static CreateCollectionRequest startingCollection(String userId) {
		return new CreateCollectionRequest()
				.withType(CollectionTypes.USER)
				.withUserId(userId)
				.withName(userId + "'s Collection")
				.withCopies(2)
				.withCardsQuery(new QueryCardsRequest()
						.withSets(CardSet.Spellsource));
	}

	public static CreateCollectionRequest alliance(String allianceId, String ownerUserId, List<String> userIds, List<String> inventoryIds) {
		return new CreateCollectionRequest()
				.withType(CollectionTypes.ALLIANCE)
				.withName(ownerUserId + "'s Alliance")
				.withAllianceId(allianceId)
				.withUserId(ownerUserId)
				.withUserIds(userIds)
				.withInventoryIds(inventoryIds);

	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public OpenCardPackRequest getOpenCardPackRequest() {
		return openCardPackRequest;
	}

	public void setOpenCardPackRequest(OpenCardPackRequest openCardPackRequest) {
		this.openCardPackRequest = openCardPackRequest;
	}

	private OpenCardPackRequest openCardPackRequest;

	public QueryCardsRequest getQueryCardsRequest() {
		return queryCardsRequest;
	}

	public void setQueryCardsRequest(QueryCardsRequest queryCardsRequest) {
		this.queryCardsRequest = queryCardsRequest;
	}

	public CreateCollectionRequest withType(CollectionTypes type) {
		this.type = type;
		return this;
	}

	public CreateCollectionRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public CollectionTypes getType() {
		return type;
	}

	public void setType(CollectionTypes type) {
		this.type = type;
	}

	public CreateCollectionRequest withCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}

	public CreateCollectionRequest withCardsQuery(QueryCardsRequest queryCardsRequest) {
		this.queryCardsRequest = queryCardsRequest;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public CreateCollectionRequest withOpenCardPack(OpenCardPackRequest openCardPackRequest) {
		this.openCardPackRequest = openCardPackRequest;
		return this;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public CreateCollectionRequest withInventoryIds(final List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CreateCollectionRequest withName(final String name) {
		this.name = name;
		return this;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public CreateCollectionRequest withHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
		return this;
	}

	public String getAllianceId() {
		return allianceId;
	}

	public void setAllianceId(String allianceId) {
		this.allianceId = allianceId;
	}

	public CreateCollectionRequest withAllianceId(final String allianceId) {
		this.allianceId = allianceId;
		return this;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}

	public CreateCollectionRequest withUserIds(final List<String> userIds) {
		this.userIds = userIds;
		return this;
	}

	public int getCopies() {
		return copies;
	}

	public void setCopies(int copies) {
		this.copies = copies;
	}

	public CreateCollectionRequest withCopies(final int copies) {
		this.copies = copies;
		return this;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public CreateCollectionRequest withDraft(final boolean draft) {
		this.draft = draft;
		return this;
	}
}
