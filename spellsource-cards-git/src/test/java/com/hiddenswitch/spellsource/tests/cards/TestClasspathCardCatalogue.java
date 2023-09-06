package com.hiddenswitch.spellsource.tests.cards;

import com.google.common.collect.Maps;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestClasspathCardCatalogue extends ClasspathCardCatalogue {
	AtomicBoolean initializing = new AtomicBoolean(true);

	@Override
	public @NotNull CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		var cards = super.query(deckFormat, cardType, rarity, heroClass, tag, clone);
		if (!initializing.get() && cards.stream().anyMatch(card -> CardSet.TEST.equals(card.getCardSet()))) {
			throw new IllegalStateException("return test");
		}
		return cards;
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		var cards = super.queryUncollectible(deckFormat);
		if (!initializing.get() && cards.stream().anyMatch(card -> CardSet.TEST.equals(card.getCardSet()))) {
			throw new IllegalStateException("returned test");
		}
		return cards;
	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {
		var card = super.getCardById(id);
		if (!initializing.get() && CardSet.TEST.equals(card.getCardSet())) {
			throw new IllegalStateException("returned test");
		}
		return card;
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		var cards = super.getCards();
		return Maps.transformValues(cards, card -> {
			if (CardSet.TEST.equals(card.getCardSet())) {
				throw new IllegalStateException("returned test");
			}
			return card;
		});
	}

	@Override
	public @NotNull Map<String, CardCatalogueRecord> getRecords() {
		var records = super.getRecords();
		return Maps.transformValues(records, record -> {
			if (CardSet.TEST.equals(record.getDesc().getSet())) {
				throw new IllegalStateException("returned test");
			}
			return record;
		});
	}

	@Override
	protected void updatedWith(Map<String, CardDesc> cardDescs) {
		var filtered = Maps.filterValues(cardDescs, desc -> !Objects.equals(desc.getSet(), CardSet.TEST));
		super.updatedWith(filtered);
	}

	@Override
	public void loadCardsFromPackage() {
		super.loadCardsFromPackage();
		initializing.set(false);
	}
}
