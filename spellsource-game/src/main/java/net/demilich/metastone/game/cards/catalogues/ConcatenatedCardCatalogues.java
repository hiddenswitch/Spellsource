package net.demilich.metastone.game.cards.catalogues;

import com.hiddenswitch.spellsource.rpc.Spellsource;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Concatenates card catalogues.
 * <p>
 * When cards are the same between the card catalogues, the catalogues are queried in order and the first result is the
 * card that is used.
 */
public final class ConcatenatedCardCatalogues implements CardCatalogue {

	private final List<CardCatalogue> cardCatalogues;

	public ConcatenatedCardCatalogues(List<CardCatalogue> cardCatalogues) {
		this.cardCatalogues = cardCatalogues;
	}

	@Override
	public Map<String, DeckFormat> formats() {
		return cardCatalogues.stream().flatMap(cc -> cc.formats().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (deckFormat, deckFormat2) -> deckFormat));
	}

	@Override
	public DeckFormat getFormat(String name) {
		for (var cc : cardCatalogues) {
			var df = cc.getFormat(name);
			if (df != null) {
				return df;
			}
		}
		return null;
	}

	@Override
	public Set<String> getBannedDraftCards() {
		return cardCatalogues.stream().flatMap(cc -> cc.getBannedDraftCards().stream()).collect(Collectors.toSet());
	}

	@Override
	public Set<String> getHardRemovalCardIds() {
		return cardCatalogues.stream().flatMap(cc -> cc.getHardRemovalCardIds().stream()).collect(Collectors.toSet());
	}

	@Override
	public @NotNull CardList getAll() {
		return cardCatalogues.stream().flatMap(cc -> cc.getAll().stream()).collect(Collectors.toCollection(CardArrayList::new));
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		return cardCatalogues.stream().flatMap(cc -> cc.getCards().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (card, card2) -> card));
	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {
		for (var cc : cardCatalogues) {
			try {
				return cc.getCardById(id);
			} catch (NullPointerException ignored) {
			}
		}
		throw new NullPointerException(id);
	}

	@Override
	public @Nullable Card getCardByName(String name) {
		for (var cc : cardCatalogues) {
			var card = cc.getCardByName(name);
			if (card != null) {
				return card;
			}
		}
		return null;
	}

	@Override
	public Card getCardByName(String name, String heroClass) {
		for (var cc : cardCatalogues) {
			try {
				var card = cc.getCardByName(name, heroClass);
				if (card != null) {
					return card;
				}
			} catch (NullPointerException ignored) {
			}
		}
		throw new NullPointerException("%s %s".formatted(name, heroClass));
	}

	@Override
	public @NotNull CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		return cardCatalogues.stream().flatMap(cc -> cc.query(deckFormat, cardType, rarity, heroClass, tag, clone).stream()).collect(Collectors.toCollection(CardArrayList::new));
	}

	@Override
	public Card getFormatCard(String name) {
		for (var cc : cardCatalogues) {
			var card = cc.getFormatCard(name);
			if (card != null) {
				return card;
			}
		}
		return null;
	}

	@Override
	public Card getHeroCard(String heroClass) {
		for (var cc : cardCatalogues) {
			try {
				var card = cc.getHeroCard(heroClass);
				if (card != null) {
					return card;
				}
			} catch (NullPointerException ignored) {
			}
		}
		return null;
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		return cardCatalogues.stream().flatMap(cc -> cc.getClassCards(format).stream()).collect(Collectors.toCollection(CardArrayList::new));
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		return cardCatalogues.stream().flatMap(cc -> cc.getBaseClasses(deckFormat).stream()).collect(Collectors.toList());
	}

	@Override
	public Stream<Card> stream() {
		return cardCatalogues.stream().flatMap(CardCatalogue::stream);
	}

	@Override
	public CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		return cardCatalogues.stream().flatMap(cc -> cc.queryClassCards(format, hero, bannedCards, rarity, validCardTypes).stream()).collect(Collectors.toCollection(CardArrayList::new));
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		return cardCatalogues.stream().flatMap(cc -> cc.queryNeutrals(format, bannedCards, rarity, validCardTypes).stream()).collect(Collectors.toCollection(CardArrayList::new));
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		return cardCatalogues.stream().flatMap(cc -> cc.queryUncollectible(deckFormat).stream()).collect(Collectors.toCollection(CardArrayList::new));
	}
}
