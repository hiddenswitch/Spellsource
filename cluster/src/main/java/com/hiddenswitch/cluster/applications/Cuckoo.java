package com.hiddenswitch.cluster.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Simulation;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.shared.threat.FeatureVector;
import net.demilich.metastone.game.shared.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.shared.threat.WeightedFeature;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Cuckoo {
	public static final int NUMBER_OF_GAMES_IN_BATCH = 20;
	static Logger logger = LoggerFactory.getLogger(Cuckoo.class);
	static AtomicInteger counter = new AtomicInteger();

	static DoubleChromosome weightedChromosome() {
		WeightedFeature[] values = WeightedFeature.values();
		FeatureVector best = FeatureVector.getFittest();
		DoubleGene[] genes = new DoubleGene[values.length];
		for (int i = 0; i < values.length; i++) {
			final WeightedFeature feature = values[i];
			switch (feature) {
				case MINION_ATTACK_FACTOR:
				case MINION_DEFAULT_TAUNT_MODIFIER:
				case MINION_YELLOW_TAUNT_MODIFIER:
				case MINION_RED_TAUNT_MODIFIER:
				case MINION_DIVINE_SHIELD_MODIFIER:
				case MINION_INTRINSIC_VALUE:
					genes[i] = DoubleGene.of(best.get(feature), -10, 100);
					break;
				case MINION_HP_FACTOR:
				case MINION_SPELL_POWER_MODIFIER:
				case MINION_STEALTHED_MODIFIER:
				case MINION_UNTARGETABLE_BY_SPELLS_MODIFIER:
				case MINION_WINDFURY_MODIFIER:
				case HARD_REMOVAL_VALUE:
				case EMPTY_MANA_CRYSTAL_VALUE:
				case QUEST_COUNTER_VALUE:
				case QUEST_REWARD_VALUE:
					genes[i] = DoubleGene.of(best.get(feature), 0, 100);
					break;
				case OWN_CARD_COUNT:
				case OWN_HP_FACTOR:
				case OPPONENT_HP_FACTOR:
				case OPPONENT_CARD_COUNT:
				case OPPOSING_EMPTY_MANA_CRYSTAL_VALUE:
					genes[i] = DoubleGene.of(best.get(feature), -100, 100);
					break;
				case RED_MODIFIER:
				case CURSED_FACTOR:
					genes[i] = DoubleGene.of(best.get(feature), -100, 0);
					break;
				case YELLOW_MODIFIER:
					genes[i] = DoubleGene.of(best.get(feature), -100, 10);
				default:
					genes[i] = DoubleGene.of(best.get(feature), -100, 100);
					break;
			}
		}

		return DoubleChromosome.of(genes);
	}

	static FeatureVector fromGenotype(Genotype<DoubleGene> genotype) {
		FeatureVector featureVector = new FeatureVector();
		WeightedFeature[] values = WeightedFeature.values();
		for (int i = 0; i < values.length; i++) {
			featureVector.set(values[i], genotype.get(0, i).doubleValue());
		}
		return featureVector;
	}

	static double evaluate(Genotype<DoubleGene> genotype) {
		List<DeckCreateRequest> decks = Spellsource.spellsource().getStandardDecks();
		List<GameDeck> gameDecks = Arrays.asList(decks.get(0).toGameDeck(), decks.get(1).toGameDeck());
		SimulationResult result = GameContext.simulate(gameDecks, () -> {
			FeatureVector featureVector = fromGenotype(genotype);
			GameStateValueBehaviour agent = new GameStateValueBehaviour(featureVector, "Agent");
			agent.setMaxDepth(2);
			return agent;
		}, () -> {
			GameStateValueBehaviour gsvb = new GameStateValueBehaviour();
			gsvb.setMaxDepth(2);
			return gsvb;
		}, NUMBER_OF_GAMES_IN_BATCH, true, counter, (gameContext -> {
			gameContext.setLogic(new GameLogic(1010101010101L));
		}));

		return result.getPlayer1Stats().getDouble(Statistic.WIN_RATE);
	}

	public static void main(String[] args) {
//		SparkConf conf = new SparkConf().setAppName("Perform a Cuckoo optimization over nodes");
//		JavaSparkContext sc = new JavaSparkContext(conf);
		Logging.setLoggingLevel(Level.INFO);
		Factory<Genotype<DoubleGene>> gtf = Genotype.of(Cuckoo.weightedChromosome());

		double mutatorProbability = 0.8;
		int populationSize = 15;
		int crossoverCount = 3;
		double discoveryRate = 0.25;
		int generations = 10;

		Engine<DoubleGene, Double> engine = Engine
				.builder(Cuckoo::evaluate, gtf)
				.populationSize(populationSize)
				.selector(new EliteSelector<>(1, new LinearRankSelector<DoubleGene, Double>((int) (populationSize * discoveryRate))))
				.alterers(new GaussianMutator<>(mutatorProbability), new MultiPointCrossover<>(crossoverCount))
				.build();

		AtomicReference<Phenotype<DoubleGene, Double>> best = new AtomicReference<>();
		EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();

		int totalMatches = generations * NUMBER_OF_GAMES_IN_BATCH * populationSize;
		Thread monitor = Simulation.getMonitor(counter, totalMatches);
		monitor.start();

		Genotype<DoubleGene> result =
				engine.stream()
						.peek(statistics)
						.peek(result1 -> {
							final Phenotype<DoubleGene, Double> bestPhenotype = result1.getBestPhenotype();
							if (best.get() == null
									|| best.get().compareTo(bestPhenotype) < 0) {
								best.set(bestPhenotype);
								logger.info("main: Vector on generation {}: \n{}", result1.getTotalGenerations(),
										fromGenotype(bestPhenotype.getGenotype()));
							}
						})
						.limit(generations).collect(EvolutionResult.toBestGenotype());

		monitor.interrupt();
		logger.info("main: Vector=\n{}", fromGenotype(result));
	}
}
