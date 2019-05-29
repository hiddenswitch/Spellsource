package com.hiddenswitch.deckgeneration;


import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.statistics.Statistic;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class DeckGenerationTests {
    List<GameDeck> basicTournamentDecks = new ArrayList<>();
    List<Card> indexInBitmap;
    int GAMES_PER_MATCH = 18;
    int maxCardsPerDeck = 4;
    int startingHp = 10;
    boolean winCardAppeared;

    @Test
    public void testJeneticsBasic() {
        // Our goal: Try to make an optimal deck under some very simple constraints where the winrates in a tournament
        // of these decks by random play is maximized.
        // General outline:
        // 1. Generate N decks
        // 2. Each deck is played against a fixed pool of other test decks (in the case of the paper, the hearthstone meta game). The winrate is its fitness.
        // 3. Mix aspects of the best decks together.
        // 4. Remove the worst performing decks and replace them with new random decks.

        // Generate decks under a simple encoding. A bitmap that corresponds to whether or not a card is in a deck.
        // In our simple scheme, we are only going to deal with mage cards. We're going to use only basic cards from
        // mage and basic neutrals. We will only have at most one card. Decks of size 30. Any decks that are invalid
        // will always be ranked at the bottom when sorted by whatever evaluated Jenetics actually uses.

        CardCatalogue.loadCardsFromPackage();
        indexInBitmap = CardCatalogue.getAll()
                .stream()
                .filter(card -> card.isCollectible()
                        && (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
                        && card.getCardSet() == CardSet.BASIC)
                .collect(toList());

        XORShiftRandom random = new XORShiftRandom(101010L);
        // Create random decks for the tournament
        for (int i = 0; i < 10; i++) {
            GameDeck tournamentDeck = new GameDeck(HeroClass.BLUE);
            for (int j = 0; j < maxCardsPerDeck; j++) {
                tournamentDeck.getCards().add(indexInBitmap.get(random.nextInt(indexInBitmap.size())));
            }
            basicTournamentDecks.add(tournamentDeck);
        }
        indexInBitmap.add(CardCatalogue.getCardById("spell_win_the_game"));
            winCardAppeared = false;
            Factory<Genotype<BitGene>> bitGeneFactory = new DeckGeneFactory(maxCardsPerDeck, indexInBitmap.size());
            Engine<BitGene, Double> engine = Engine.builder(this::fitness, bitGeneFactory)
                    .populationSize(5)
                    .alterers(new CardSwapMutator<>(1), new CardSwapMutator<>(1))
                    .mapping(x -> {
                        System.out.println(x.getGenotypes().stream().collect(toList()));
                        return x;
                    })
                    .build();

            EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();

            Genotype<BitGene> result = engine.stream()
                    .limit(10)
                    .peek(statistics)
                    .collect(EvolutionResult.toBestGenotype());

            System.out.println(statistics);
            int winTheGameIndex = indexInBitmap.size() - 1;
            assertTrue(result.getChromosome().getGene(winTheGameIndex).booleanValue());
        }

    private static class DeckGeneFactory implements Factory<Genotype<BitGene>> {
        private int deckSize = 6;
        private int totalCardsInCollection;

        public DeckGeneFactory(int deckSize, int totalCardsInCollection) {
            this.deckSize = deckSize;
            this.totalCardsInCollection = totalCardsInCollection;
        }

        public DeckGeneFactory(int totalCardsInCollection) {
            this.totalCardsInCollection = totalCardsInCollection;
        }

        @Override
        public Genotype<BitGene> newInstance() {
            BitSet bits = new BitSet(totalCardsInCollection);
            Random random = RandomRegistry.getRandom();
            for (int i = 0; i < deckSize; i++) {
                int bitIndex = random.nextInt(totalCardsInCollection);

                while (bits.get(bitIndex) || bitIndex == totalCardsInCollection - 1) {
                    bitIndex = random.nextInt(totalCardsInCollection);
                }

                bits.flip(bitIndex);
            }
            return Genotype.of(BitChromosome.of(bits, totalCardsInCollection));
        }
    }

    private class DeckCardSwapMutator<
            G extends Gene<?, G>,
            C extends Comparable<? super C>
            >
            extends MultiPointCrossover<G, C> {


        /**
         * Constructs an alterer that takes two decks and swaps one card with each other
         *
         * @param probability the crossover probability.
         * @throws IllegalArgumentException if the {@code probability} is not in the
         *                                  valid range of {@code [0, 1]}.
         */
        public DeckCardSwapMutator(final double probability) {
            super(probability, 1);
        }

        /**
         * Create a new single point crossover object with crossover probability of
         * {@code 0.05}.
         */
        public DeckCardSwapMutator() {
            this(0.05);
        }

        @Override
        protected int crossover(final MSeq<G> that, final MSeq<G> other) {
            // No relevant swaps can be made if they are equal
            if (that.equals(other)) {
                return 2;
            }
            int firstToSwap;
            int secondToSwap;
            Random random = RandomRegistry.getRandom();

            // Randomly find a card that is in one deck but not the other
            while (true) {
                firstToSwap = random.nextInt(indexInBitmap.size());
                if (that.get(firstToSwap) != other.get(firstToSwap)) {
                    break;
                }
            }

            // Randomly find a card that is in the other deck but not in the deck
            // that our first card is in
            while (true) {
                secondToSwap = random.nextInt(indexInBitmap.size());
                if ((that.get(secondToSwap) != other.get(secondToSwap)) && (that.get(firstToSwap) != that.get(secondToSwap))) {
                    break;
                }
            }

            that.swap(firstToSwap, other);
            that.swap(secondToSwap, other);
            return 2;
        }

    }

    private static class CardSwapMutator<
            G extends Gene<?, G>,
            C extends Comparable<? super C>
            >
            extends SwapMutator<G, C> {
        /**
         * Constructs an alterer that swaps out a card in a deck with another randomly chosen one
         *
         * @param probability the crossover probability.
         * @throws IllegalArgumentException if the {@code probability} is not in the
         *                                  valid range of {@code [0, 1]}.
         */
        public CardSwapMutator(final double probability) {
            super(probability);
        }

        /**
         * Default constructor, with default mutation probability
         * ({@link AbstractAlterer#DEFAULT_ALTER_PROBABILITY}).
         */
        public CardSwapMutator() {
            this(DEFAULT_ALTER_PROBABILITY);
        }

        /**
         * Swaps the genes in the given array, with the mutation probability of this
         * mutation.
         */
        @Override
        protected MutatorResult<Chromosome<G>> mutate(
                final Chromosome<G> chromosome,
                final double p,
                final Random random
        ) {
            return getCardSwapMutatorResult(chromosome, p, random);
        }

        @NotNull
        public static <G extends Gene<?, G>> MutatorResult<Chromosome<G>> getCardSwapMutatorResult(Chromosome<G> chromosome, double p, Random random) {
            final MutatorResult<Chromosome<G>> result;
            if (chromosome.length() > 1 && random.nextDouble() <= p) {
                final MSeq<G> genes = chromosome.toSeq().copy();
                int firstCard = random.nextInt(chromosome.length());
                while (true) {
                    // If i is in the deck then randomly select cards until we find one
                    // not in the deck, or vice versa
                    int secondCard = random.nextInt(genes.length());
                    if (!genes.get(secondCard).equals(genes.get(firstCard))) {
                        genes.swap(firstCard, secondCard);
                        break;
                    }
                }
                result = MutatorResult.of(
                        chromosome.newInstance(genes.toISeq())
                );
            } else {
                result = MutatorResult.of(chromosome);
            }
            return result;
        }
    }

    @Test
    public void testBasicCardSwapMutation() {
        int size = 100;
        Random random = new XORShiftRandom(101010L);
        BitSet bits = new BitSet(16);
        for (int i=0; i<14; i++){
            int toFlip = random.nextInt(size - 1);
            while (bits.get(toFlip)) {
                toFlip = random.nextInt(size - 1);
            }
            bits.flip(toFlip);
        }
        bits.flip(size - 1);
        Chromosome<BitGene> testChromosome = BitChromosome.of(bits);
        MutatorResult<Chromosome<BitGene>> value = CardSwapMutator.getCardSwapMutatorResult(testChromosome, 1, random);

        int count1 = 0;
        int count2 = 0;
        int differences = 0;
        for (int i = 0; i < testChromosome.length(); i++){
            if (testChromosome.getGene(i) != value.getResult().getGene(i)){
                differences++;
            }
            if (testChromosome.getGene(i).booleanValue()) {
                count1++;
            }
            if (value.getResult().getGene(i).booleanValue()){
                count2++;
            }
        }
        assertTrue(differences == 2);
        assertTrue(count1 == count2);
    }

    @NotNull
    public static <G extends Gene<?, G>> MutatorResult<Chromosome<G>> getSmartSwapMutatorResult(Chromosome<G> chromosome, double p, Random random, List<Card> indexInBitmap) {
        final MutatorResult<Chromosome<G>> result;
        if (chromosome.length() > 1 && random.nextDouble() <= p) {
            final MSeq<G> genes = chromosome.toSeq().copy();
            int firstCard = random.nextInt(chromosome.length());
            while (true) {
                // If i is in the deck then randomly select cards until we find one
                // not in the deck, or vice versa
                int secondCard = random.nextInt(genes.length());
                if (!genes.get(secondCard).equals(genes.get(firstCard)) &&
                        (indexInBitmap.get(firstCard).getBaseManaCost() == indexInBitmap.get(secondCard).getBaseManaCost())) {
                    genes.swap(firstCard, secondCard);
                    break;
                }
            }
            result = MutatorResult.of(
                    chromosome.newInstance(genes.toISeq())
            );
        } else {
            result = MutatorResult.of(chromosome);
        }
        return result;
    }

    @Test
    public void testSmartSwapMutation() {
        CardCatalogue.loadCardsFromPackage();
        indexInBitmap = CardCatalogue.getAll()
                .stream()
                .filter(card -> card.isCollectible()
                        && (card.getHeroClass() == HeroClass.BLUE || card.getHeroClass() == HeroClass.ANY)
                        && card.getCardSet() == CardSet.BASIC)
                .collect(toList());
        int size = indexInBitmap.size();
        Random random = new XORShiftRandom(101010L);
        BitSet bits = new BitSet(16);
        for (int i=0; i<14; i++){
            int toFlip = random.nextInt(size - 1);
            while (bits.get(toFlip)) {
                toFlip = random.nextInt(size - 1);
            }
            bits.flip(toFlip);
        }
        bits.flip(size - 1);
        Chromosome<BitGene> testChromosome = BitChromosome.of(bits);
        MutatorResult<Chromosome<BitGene>> value = getSmartSwapMutatorResult(testChromosome, 1, random, indexInBitmap);

        int count1 = 0;
        int count2 = 0;
        int differences = 0;
        int firstCard = -1;
        for (int i = 0; i < testChromosome.length(); i++){
            if (testChromosome.getGene(i) != value.getResult().getGene(i)){
                differences++;
                if (firstCard == -1) {
                    firstCard = i;
                }
                else {
                    assertTrue(indexInBitmap.get(firstCard).getBaseManaCost() == indexInBitmap.get(i).getBaseManaCost());
                }
            }
            if (testChromosome.getGene(i).booleanValue()) {
                count1++;
            }
            if (value.getResult().getGene(i).booleanValue()){
                count2++;
            }
        }
        assertTrue(differences == 2);
        assertTrue(count1 == count2);
    }

    private class SmartSwapMutator<
            G extends Gene<?, G>,
            C extends Comparable<? super C>
            >
            extends SwapMutator<G, C> {
        List<Card> indexInBitmap;
        /**
         * Constructs an alterer that swaps a card in a deck
         * for one not in the deck, but of the same mana cost
         *
         * @param probability the crossover probability.
         * @throws IllegalArgumentException if the {@code probability} is not in the
         *                                  valid range of {@code [0, 1]}.
         */
        public SmartSwapMutator(final double probability, List<Card> indexInBitmap) {
            super(probability);
            this.indexInBitmap = indexInBitmap;
        }

        /**
         * Swaps the genes in the given array, with the mutation probability of this
         * mutation.
         */
        @Override
        protected MutatorResult<Chromosome<G>> mutate(
                final Chromosome<G> chromosome,
                final double p,
                final Random random
        ) {
            return getSmartSwapMutatorResult(chromosome, p, random, indexInBitmap);
        }
    }

    /**
     * Selects a deck bit string only if it had a certain minimum fitness (i.e. winrate in the tournament)
     *
     * @param <G>
     */
//    private static class MinimumFitnessSelector<G extends Gene<?, G>> implements Selector<G, Double> {
//
//        private double minimumFitness;
//
//        public MinimumFitnessSelector(double minimumFitness) {
//            this.minimumFitness = minimumFitness;
//        }
//
//        @Override
//        public ISeq<Phenotype<G, Double>> select(Seq<Phenotype<G, Double>> population, int count, Optimize opt) {
//            final MSeq<Phenotype<G, Double>> selection = MSeq.empty();
//
//            population.stream()
//                    .filter(phenotype -> phenotype.getFitness() > minimumFitness)
//                    .limit(count)
//                    .forEach(selection::append);
//
//            return selection.toISeq();
//        }
//    }

    /**
     * Turns a Genotype into a {@link GameDeck}
     *
     * @param individual
     * @return
     */
    private GameDeck fromBitGeneIndividual(Genotype<BitGene> individual) {
        GameDeck deck = new GameDeck(HeroClass.BLUE);
        int count = 0;
        for (int i = 0; i < individual.getChromosome().length(); i++) {
            if (individual.getChromosome().getGene(i).booleanValue()) {
                deck.getCards().add(indexInBitmap.get(i));
                count++;

                if (count > maxCardsPerDeck) {
                    break;
                }
            }
        }
        return deck;
    }

    /**
     * Compute the fitness
     *
     * @param individual
     * @return {@code Double.MIN_VALUE} if the deck is invalid, otherwise the average winrate of the deck under test with
     * random players
     */
    private double fitness(Genotype<BitGene> individual) {
        GameDeck gameDeck = fromBitGeneIndividual(individual);

        if (individual.getChromosome().stream().map(gene -> (gene.getBit() ? 1 : 0)).mapToInt(Integer::intValue).sum() != maxCardsPerDeck) {
            return Double.MIN_VALUE;
        }

        return basicTournamentDecks.stream()
                .map(opposingDeck -> GameContext.simulate(Arrays.asList(gameDeck, opposingDeck), PlayRandomBehaviour::new, PlayRandomBehaviour::new, GAMES_PER_MATCH, true, true, null, this::handleContext))
                .mapToDouble(res -> res.getPlayer1Stats().getDouble(Statistic.WIN_RATE))
                .average().orElse(Double.MIN_VALUE);
    }

    /**
     * Set up a match in the fitness function
     *
     * @param ctx
     */
    private void handleContext(GameContext ctx) {
        ctx.getPlayer1().getHero().setHp(startingHp);
        ctx.getPlayer2().getHero().setHp(startingHp);

    }
}

