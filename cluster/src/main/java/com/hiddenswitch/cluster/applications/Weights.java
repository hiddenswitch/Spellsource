package com.hiddenswitch.cluster.applications;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.strands.Strand;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Simulation;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;
import io.jenetics.util.IntRange;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Computes new weights for the {@link GameStateValueBehaviour}.
 * <p>
 * Demonstrates the use of Jenetics.io and basic training techniques.
 */
public class Weights {
	private static final int NUMBER_OF_GAMES_IN_BATCH = 20;
	private static final boolean INCLUDE_MIRRORS = true;
	private static Logger logger = LoggerFactory.getLogger(Weights.class);
	private static AtomicInteger counter = new AtomicInteger();

	static List<GameDeck> decks() {
		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> gameDecks = new ArrayList<>();
		gameDecks.add(DeckCreateRequest.fromDeckList("### Midrange Hunter - Standard Meta Snapshot - Jan. 27, 2019\n" +
				"# Class: Hunter\n" +
				"# Format: Standard\n" +
				"# Year of the Raven\n" +
				"#\n" +
				"# 2x (1) Candleshot\n" +
				"# 2x (1) Dire Mole\n" +
				"# 2x (1) Springpaw\n" +
				"# 2x (1) Timber Wolf\n" +
				"# 2x (1) Tracking\n" +
				"# 2x (2) Crackling Razormaw\n" +
				"# 2x (2) Hunter's Mark\n" +
				"# 2x (3) Animal Companion\n" +
				"# 2x (3) Kill Command\n" +
				"# 2x (3) Master's Call\n" +
				"# 2x (3) Unleash the Hounds\n" +
				"# 2x (4) Dire Frenzy\n" +
				"# 2x (4) Flanking Strike\n" +
				"# 2x (4) Lifedrinker\n" +
				"# 1x (5) Tundra Rhino\n" +
				"# 1x (6) Deathstalker Rexxar\n" +
				"#\n" +
				"AAECAR8CuwWG0wIO4eMCi+UCoIUD3gSXCI7DAo0BtQOoAqSIA9sJyfgC3dIC7/ECAA==\n" +
				"#\n" +
				"# To use this deck, copy it to your clipboard and create a new deck in Hearthstone").toGameDeck());
		gameDecks.add(Deck.randomDeck(DeckFormat.spellsource()));
		return gameDecks;
	}

	private static IntegerChromosome weightedChromosome() {
		WeightedFeature[] values = WeightedFeature.values();
		return IntegerChromosome.of(IntRange.of(1, 1000), values.length);
	}

	private static int sign(int index) {
		WeightedFeature[] values = WeightedFeature.values();
		WeightedFeature feature = values[index];
		switch (feature) {
			case MINION_ATTACK_FACTOR:
			case MINION_DEFAULT_TAUNT_MODIFIER:
			case MINION_YELLOW_TAUNT_MODIFIER:
			case MINION_RED_TAUNT_MODIFIER:
			case MINION_DIVINE_SHIELD_MODIFIER:
			case MINION_INTRINSIC_VALUE:
			case MINION_HP_FACTOR:
			case MINION_SPELL_POWER_MODIFIER:
			case MINION_STEALTHED_MODIFIER:
			case MINION_UNTARGETABLE_BY_SPELLS_MODIFIER:
			case MINION_WINDFURY_MODIFIER:
			case HARD_REMOVAL_VALUE:
			case EMPTY_MANA_CRYSTAL_VALUE:
			case QUEST_COUNTER_VALUE:
			case QUEST_REWARD_VALUE:
			case OWN_CARD_COUNT:
			case OWN_HP_FACTOR:
			case OPPONENT_ROASTED_VALUE:
				return 1;
			case OPPONENT_HP_FACTOR:
			case OPPONENT_CARD_COUNT:
			case OPPOSING_EMPTY_MANA_CRYSTAL_VALUE:
			case RED_MODIFIER:
			case CURSED_FACTOR:
			case YELLOW_MODIFIER:
			case OWN_ROASTED_VALUE:
				return -1;
			default:
				throw new UnsupportedOperationException();
		}
	}

	private static FeatureVector fromGenotype(Genotype<IntegerGene> genotype) {
		FeatureVector featureVector = new FeatureVector();
		WeightedFeature[] values = WeightedFeature.values();
		for (int i = 0; i < values.length; i++) {
			featureVector.set(values[i], sign(i) * genotype.get(0, i).doubleValue());
		}
		return featureVector;
	}

	private static double evaluate(Genotype<IntegerGene> genotype) {
		SimulationResult result = GameContext.simulate(decks(), () -> {
					FeatureVector featureVector = fromGenotype(genotype);
					GameStateValueBehaviour agent = new GameStateValueBehaviour(featureVector, "Agent");
					agent.setParallel(false);
					agent.setMaxDepth(1);
					return agent;
				}, () -> {
					GameStateValueBehaviour agent = new GameStateValueBehaviour();
					agent.setParallel(false);
					agent.setMaxDepth(1);
					return agent;
				},
				NUMBER_OF_GAMES_IN_BATCH,
				true,
				INCLUDE_MIRRORS,
				counter,
				gameContext -> {
					gameContext.setDeckFormat(DeckFormat.spellsource());
					// Ensure the game starts a little faster
					gameContext.getPlayer1().setMana(3);
					gameContext.getPlayer2().setMana(3);
					gameContext.getPlayer1().setMaxMana(3);
					gameContext.getPlayer2().setMaxMana(3);

					// Set a fixed seed so that the bot is more likely to improve
					gameContext.setLogic(new GameLogic(1010101010101L));
				});

		return result.getPlayer1Stats().getDouble(Statistic.WIN_RATE);
	}

	public static void main(String[] args) {
		Logging.setLoggingLevel(Level.INFO);
		Factory<Genotype<IntegerGene>> gtf = Genotype.of(Weights.weightedChromosome());

		double mutatorProbability = 0.5;
		int populationSize = 20;
		double discoveryRate = 0.25;
		int crossoverCount = (int) (.3 * WeightedFeature.values().length);
		int generations = 20;

		Engine<IntegerGene, Double> engine = Engine
				.builder(Weights::evaluate, gtf)
				.populationSize(populationSize)
				.selector(new EliteSelector<>(1, new LinearRankSelector<IntegerGene, Double>((int) (populationSize * discoveryRate))))
				.alterers(new GaussianMutator<>(mutatorProbability), new MultiPointCrossover<>(crossoverCount))
				.build();

		AtomicReference<Phenotype<IntegerGene, Double>> best = new AtomicReference<>();
		EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();

		int totalMatches = generations * populationSize * GameContext.simulationCount(decks().size(), NUMBER_OF_GAMES_IN_BATCH, INCLUDE_MIRRORS);
		Strand monitor = Simulation.getMonitor(counter, totalMatches);
		monitor.start();

		logger.info("main: Starting simulation");
		Genotype<IntegerGene> result =
				engine.stream()
						.peek(statistics)
						.peek(result1 -> {
							logger.info("main: Statistics on generation {}: \n{}", result1.getTotalGenerations(), statistics);
							final Phenotype<IntegerGene, Double> bestPhenotype = result1.getBestPhenotype();
							if (best.get() == null
									|| best.get().compareTo(bestPhenotype) < 0) {
								best.set(bestPhenotype);
								logger.info("main: Vector on generation {}: \n{}", result1.getTotalGenerations(),
										fromGenotype(bestPhenotype.getGenotype()));
							}
						})
						.limit(generations)
						.collect(EvolutionResult.toBestGenotype());

		monitor.interrupt();
		logger.info("main: Vector=\n{}", fromGenotype(result));
	}
}
