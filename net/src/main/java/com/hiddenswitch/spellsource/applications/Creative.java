package com.hiddenswitch.spellsource.applications;

import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckCatalogue;
import net.demilich.metastone.game.entities.heroes.MetaHero;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Creative {
	public static void main(String[] args) throws CardParseException, IOException, URISyntaxException {
		CardCatalogue.loadCardsFromPackage();
		DeckCatalogue.loadDecksFromPackage();

		StringBuilder tsv = new StringBuilder();

		tsv.append("Deck\tHero Class\tCard\tDescription\tType\tAttack\tHp\tCost\tRace\n");

		for (String deckName : new String[]{"Basic Resurrector", "Basic Octopod Demo", "Basic Cyborg", "Basic Biologist"}) {
			Deck deck = DeckCatalogue.getDeckByName(deckName);
			final HeroCard heroCard = MetaHero.getHeroCard(deck.getHeroClass());
			Stream<Card> cards = Stream.concat(Stream.of(
					// Hero card
					heroCard,
					// Hero power card
					heroCard.createHero().getHeroPower()),
					// The cards in this deck
					deck.getCards().toList().stream().filter(distinctByKey(Card::getCardId)));
			cards.forEach(card -> {
				int attack = 0;
				int hp = 0;
				int cost = 0;
				String heroClass;
				String race = "";

				switch (deck.getHeroClass()) {
					case ROGUE:
						heroClass = "COMBOS";
						break;
					case HUNTER:
						heroClass = "OVERWHELMING_MINIONS";
						break;
					case WARLOCK:
						heroClass = "SELF_SACRIFICE";
						break;
					case PRIEST:
						heroClass = "HEAL_AND_STEAL";
						break;
					default:
						heroClass = deck.getHeroClass().toString();
						break;
				}

				cost = card.getBaseManaCost();
				switch (card.getCardType()) {
					case MINION:
						MinionCard minionCard = (MinionCard) card;
						attack = minionCard.getBaseAttack();
						hp = minionCard.getBaseHp();
						race = minionCard.getRace().toString();
						break;
					case WEAPON:
						WeaponCard weaponCard = (WeaponCard) card;
						attack = weaponCard.getBaseDamage();
						hp = weaponCard.getBaseDurability();
						break;
				}

				tsv.append(String.join("\t", Stream.of(
						deck.getName(),
						heroClass,
						card.getName(),
						card.getDescription(),
						card.getCardType(),
						attack,
						hp,
						cost,
						race
				).map(Object::toString).collect(Collectors.toList())));
				tsv.append("\n");
			});
		}

		Writer writer = new OutputStreamWriter(System.out);
		writer.write(tsv.toString());
		writer.flush();
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
