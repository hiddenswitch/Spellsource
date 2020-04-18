package com.hiddenswitch.spellsource.net.models;

import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.Logic;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.util.CollectionRecord;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import io.opentracing.util.GlobalTracer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.GameDeck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public final class GetCollectionResponse implements Serializable {
	private List<GetCollectionResponse> responses;
	private List<InventoryRecord> inventoryRecords;
	private CollectionRecord collectionRecord;

	private GetCollectionResponse() {
	}

	public static GetCollectionResponse batch(List<GetCollectionResponse> responses) {
		return new GetCollectionResponse()
				.withResponses(responses);
	}

	public static GetCollectionResponse collection(CollectionRecord collectionRecord, List<InventoryRecord> inventoryRecords) {
		return new GetCollectionResponse()
				.setCardRecords(inventoryRecords)
				.setCollectionRecord(collectionRecord);
	}

	/**
	 * Turns this response into a {@link net.demilich.metastone.game.decks.Deck} that can actually be used in a
	 * {@link GameContext}.
	 *
	 * @param userId
	 * @return
	 */
	public GameDeck asDeck(String userId) {
		GameDeck deck = new GameDeck();
		deck.setDeckId(getCollectionRecord().getId());
		deck.setHeroClass(getCollectionRecord().getHeroClass());
		deck.setName(getCollectionRecord().getName());
		String heroCardId = getCollectionRecord().getHeroCardId();
		if (heroCardId != null) {
			deck.setHeroCard(CardCatalogue.getCardById(heroCardId));
		}

		getInventoryRecords().stream().map(cardRecord -> Logic.getDescriptionFromRecord(cardRecord, userId, getCollectionRecord().getId()))
				.filter(Objects::nonNull)
				.map(CardDesc::create)
				.forEach(deck.getCards()::addCard);

		deck.setPlayerAttributes(getCollectionRecord().getPlayerEntityAttributes());

		return deck;
	}

	public List<InventoryRecord> getInventoryRecords() {
		return inventoryRecords;
	}

	public GetCollectionResponse setCardRecords(List<InventoryRecord> inventoryRecords) {
		this.inventoryRecords = inventoryRecords;
		return this;
	}


	public List<GetCollectionResponse> getResponses() {
		return responses;
	}

	public GetCollectionResponse withResponses(final List<GetCollectionResponse> responses) {
		this.responses = responses;
		return this;
	}

	public InventoryCollection asInventoryCollection() {
		if (getResponses() != null
				&& getResponses().size() > 0) {
			throw new RuntimeException();
		}

		if (getCollectionRecord() == null) {
			Tracing.error(new NullPointerException("collectionRecord"), GlobalTracer.get().activeSpan(), false);
			return null;
		}

		String displayName = getCollectionRecord().getId();

		if (getCollectionRecord().getName() != null) {
			displayName = getCollectionRecord().getName();
		}

		List<InventoryRecord> inventoryRecords = getInventoryRecords();
		List<CardRecord> records = new ArrayList<>();

		for (InventoryRecord cr : inventoryRecords) {
			final CardDesc record = Logic.getDescriptionFromRecord(cr, cr.getUserId(), getCollectionRecord().getId());

			if (record == null) {
				continue;
			}
			boolean isActor = record.getType() == CardType.MINION || record.getType() == CardType.WEAPON;
			// Send significantly less data
			// TODO: Just look it up by the card ID in the client
			records.add(new CardRecord()
					.userId(cr.getUserId())
					.collectionIds(cr.getCollectionIds())
					.entity(new Entity()
							.cardId(record.getId())
							.description(record.getDescription())
							.entityType(EntityType.CARD)
							.name(record.getName())
							.baseAttack(isActor ? record.getBaseAttack() + record.getDamage() : null)
							.baseHp(isActor ? record.getBaseHp() + record.getDurability() : null)
							.tribe(record.getRace())
							.cardType(CardType.valueOf(record.getType().toString()))
							.rarity(record.getRarity())
							.manaCost(record.getBaseManaCost())
							.baseManaCost(record.getBaseManaCost())
							.heroClass(record.getHeroClass()))
					.id(cr.getId())
					.allianceId(cr.getAllianceId())
					.donorUserId(cr.getDonorUserId()));
		}

		InventoryCollection collection = new InventoryCollection()
				.name(displayName)
				.id(getCollectionRecord().getId())
				.type(InventoryCollection.TypeEnum.valueOf(getCollectionRecord().getType().toString()))
				.format(getCollectionRecord().getFormat())
				.deckType(getCollectionRecord().getType() == CollectionTypes.DECK ? InventoryCollection.DeckTypeEnum.valueOf(getCollectionRecord().getDeckType().toString()) : null)
				.isStandardDeck(getCollectionRecord().isStandardDeck())
				.validationReport(getCollectionRecord().getValidationReport() == null ? new com.hiddenswitch.spellsource.client.models.ValidationReport() : getCollectionRecord().getValidationReport())
				.playerEntityAttributes(getCollectionRecord().getPlayerEntityAttributes() != null ? getCollectionRecord().getPlayerEntityAttributes()
						.entrySet()
						.stream()
						.map(kv -> {
							// TODO: Correctly check the type of the value when we support more than just string values for an attribute value tuple.
							return new AttributeValueTuple().attribute(PlayerEntityAttributes.valueOf(kv.getKey().name())).stringValue((String) kv.getValue());
						})
						.collect(toList()) : Collections.emptyList())
				.inventory(records);

		if (getCollectionRecord().getHeroClass() != null) {
			collection.heroClass(getCollectionRecord().getHeroClass());
		}

		return collection;
	}


	public static GetCollectionResponse empty() {
		return new GetCollectionResponse();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GetCollectionResponse)) return false;
		GetCollectionResponse that = (GetCollectionResponse) o;
		return com.google.common.base.Objects.equal(responses, that.responses) &&
				com.google.common.base.Objects.equal(inventoryRecords, that.inventoryRecords) &&
				com.google.common.base.Objects.equal(getCollectionRecord(), that.getCollectionRecord());
	}

	@Override
	public int hashCode() {
		return com.google.common.base.Objects.hashCode(responses, inventoryRecords, getCollectionRecord());
	}

	public CollectionRecord getCollectionRecord() {
		return collectionRecord;
	}

	public GetCollectionResponse setCollectionRecord(CollectionRecord collectionRecord) {
		this.collectionRecord = collectionRecord;
		return this;
	}
}


