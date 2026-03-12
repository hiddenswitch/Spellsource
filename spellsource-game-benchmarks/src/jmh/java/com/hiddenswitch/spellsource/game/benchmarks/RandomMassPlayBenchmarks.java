package com.hiddenswitch.spellsource.game.benchmarks;

import com.google.common.collect.Maps;
import com.hiddenswitch.spellsource.testutils.RandomDeck;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.XORShiftRandom;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

@BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class RandomMassPlayBenchmarks {
	private static final int TARGET_SEED_COUNT = 1;
	private static final int MAX_SEED_ATTEMPTS = 512;

	private static final class BenchmarkCardCatalogue extends ClasspathCardCatalogue {
		private final AtomicBoolean initializing = new AtomicBoolean(true);

		@Override
		public CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
			var cards = super.query(deckFormat, cardType, rarity, heroClass, tag, clone);
			if (!initializing.get() && cards.stream().anyMatch(card -> CardSet.TEST.equals(card.getCardSet()))) {
				throw new IllegalStateException("returned test cards");
			}
			return cards;
		}

		@Override
		public CardList queryUncollectible(DeckFormat deckFormat) {
			var cards = super.queryUncollectible(deckFormat);
			if (!initializing.get() && cards.stream().anyMatch(card -> CardSet.TEST.equals(card.getCardSet()))) {
				throw new IllegalStateException("returned test cards");
			}
			return cards;
		}

		@Override
		public Card getCardById(String id) {
			var card = super.getCardById(id);
			if (!initializing.get() && CardSet.TEST.equals(card.getCardSet())) {
				throw new IllegalStateException("returned test card");
			}
			return card;
		}

		@Override
		public Map<String, Card> getCards() {
			var cards = super.getCards();
			return Maps.transformValues(cards, card -> {
				if (CardSet.TEST.equals(card.getCardSet())) {
					throw new IllegalStateException("returned test cards");
				}
				return card;
			});
		}

		@Override
		protected void updatedWith(Map<String, CardDesc> cardDescs) {
			super.updatedWith(Maps.filterValues(cardDescs, desc -> !Objects.equals(desc.getSet(), CardSet.TEST)));
		}

		@Override
		public void loadCardsFromPackage() {
			super.loadCardsFromPackage();
			initializing.set(false);
		}
	}

	@State(Scope.Benchmark)
	public static class BenchmarkState {
		CardCatalogue cardCatalogue;
		DeckFormat format;
		long[] successfulSeeds;
		AtomicInteger seedIndex;

		@Setup(Level.Trial)
		public void setup() {
			ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
			cardCatalogue = new BenchmarkCardCatalogue();
			((BenchmarkCardCatalogue) cardCatalogue).loadCardsFromPackage();
			format = Objects.requireNonNull(cardCatalogue.getFormat("Standard"), "Standard format not found");
			successfulSeeds = collectSuccessfulSeeds(cardCatalogue);
			seedIndex = new AtomicInteger();
		}

		public long nextSeed() {
			return successfulSeeds[Math.floorMod(seedIndex.getAndIncrement(), successfulSeeds.length)];
		}

		private long[] collectSuccessfulSeeds(CardCatalogue cardCatalogue) {
			var random = new XORShiftRandom(0x5EED5EED5EED5EEDL);
			var seeds = new ArrayList<Long>(TARGET_SEED_COUNT);

			for (var attempt = 0; attempt < MAX_SEED_ATTEMPTS && seeds.size() < TARGET_SEED_COUNT; attempt++) {
				var seed = random.nextLong();
				var context = createRandomStandardContext(seed, cardCatalogue, format);
				try {
					context.play();
					seeds.add(seed);
				} catch (Throwable ignored) {
				}
			}

			if (seeds.size() < TARGET_SEED_COUNT) {
				throw new IllegalStateException("Could not find enough successful random-game seeds for benchmarking");
			}

			return seeds.stream().mapToLong(Long::longValue).toArray();
		}
	}

	@Benchmark
	public int randomMassPlay(BenchmarkState benchmarkState) {
		var context = createRandomStandardContext(benchmarkState.nextSeed(), benchmarkState.cardCatalogue, benchmarkState.format);
		context.play();
		return context.getTurn();
	}

	private static GameContext createRandomStandardContext(long seed, CardCatalogue cardCatalogue, DeckFormat format) {
		var random = new XORShiftRandom(seed);
		var heroClasses = new ArrayList<>(cardCatalogue.getBaseClasses(format));
		var deck1 = new RandomDeck(random.nextLong(), heroClasses.get(random.nextInt(heroClasses.size())), format, cardCatalogue);
		var deck2 = new RandomDeck(random.nextLong(), heroClasses.get(random.nextInt(heroClasses.size())), format, cardCatalogue);
		var context = GameContext.fromDecks(
				Arrays.asList(deck1, deck2),
				createBenchmarkBehaviour(),
				createBenchmarkBehaviour(),
				cardCatalogue,
				format);
		context.setLogic(new GameLogic(random.nextLong()));
		context.setDeckFormat(format);
		return context;
	}

	private static GameStateValueBehaviour createBenchmarkBehaviour() {
		return new GameStateValueBehaviour()
				.setThrowsExceptions(false)
				.setMaxDepth(2)
				.setParallel(false)
				.setTimeout(0)
				.setLethalTimeout(0);
	}
}
