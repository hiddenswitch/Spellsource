package com.hiddenswitch.cluster.applications;

import com.hiddenswitch.spellsource.util.Simulation;
import io.jenetics.util.DoubleRange;
import net.demilich.metastone.game.decks.DeckCreateRequest;
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
import net.demilich.metastone.game.statistics.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Computes new weights for the {@link GameStateValueBehaviour}.
 * <p>
 * Demonstrates the use of Jenetics.io and basic training techniques.
 */
public class Weights {
	private static final int NUMBER_OF_GAMES_IN_BATCH = 1;
	private static final boolean INCLUDE_MIRRORS = true;
	private static Logger LOGGER = LoggerFactory.getLogger(Weights.class);
	private static AtomicInteger counter = new AtomicInteger();

	static List<GameDeck> decks() {
		CardCatalogue.loadCardsFromPackage();
		List<GameDeck> gameDecks = new ArrayList<>();
		var designedDeck = DeckCreateRequest.fromDeckList("### Baron: Big Baron\n" +
				"# Class: NAVY\n" +
				"# Format: Spellsource\n" +
				"#\n" +
				"# 2x (1) Enchanted Shield\n" +
				"# 2x (1) Gather Strength\n" +
				"# 2x (3) Bewitch\n" +
				"# 2x (3) Defenses Up\n" +
				"# 2x (3) Duplimancy\n" +
				"# 2x (4) Defender of Tomorrow\n" +
				"# 2x (4) Hidden Treasure\n" +
				"# 2x (4) Self-Appoint\n" +
				"# 2x (5) Bog Mutant\n" +
				"# 2x (5) Savage Werewolf\n" +
				"# 2x (7) Clash!\n" +
				"# 2x (7) Landsieged Drake\n" +
				"# 2x (7) Unstable Artifact\n" +
				"# 1x (8) Maskless Manhorse, Revengeance\n" +
				"# 1x (9) Gor'thal the Ravager\n" +
				"# 1x (10) Raid Boss Gnaxx\n" +
				"# 1x (10) Sorceress Eka\n" +
				"#\n").toGameDeck();
		// Prevent fatigue loops
		designedDeck.getCards().addCard("passive_concede_fatigue");
		gameDecks.add(designedDeck);
		var randomDeck = Deck.randomDeck(DeckFormat.spellsource());
		randomDeck.getCards().addCard("passive_concede_fatigue");
		gameDecks.add(randomDeck);
		return gameDecks;
	}

	private static DoubleChromosome weightedDoubleChromosome() {
		var values = WeightedFeature.values();
		return DoubleChromosome.of(DoubleRange.of(0.001, 1), values.length);
	}

	private static int sign(int index) {
		var values = WeightedFeature.values();
		var feature = values[index];
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

	private static FeatureVector fromDoubleGenotype(Genotype<DoubleGene> genotype) {
		var featureVector = new FeatureVector();
		var values = WeightedFeature.values();
		for (var i = 0; i < values.length; i++) {
			featureVector.set(values[i], sign(i) * genotype.get(0).get(i).doubleValue());
		}
		return featureVector;
	}

	private static double evaluate(Genotype<DoubleGene> genotype) {
		var result = GameContext.simulate(decks(), () -> {
					var featureVector = fromDoubleGenotype(genotype);
					var agent = new GameStateValueBehaviour(featureVector, "Agent")
							.setTimeout(5000)
							.setParallel(false)
							.setMaxDepth(3)
							.setThrowsExceptions(false);
					return agent;
				}, () -> {
					var agent = new GameStateValueBehaviour()
							.setTimeout(5000)
							.setParallel(false)
							.setMaxDepth(3)
							.setThrowsExceptions(false);
					return agent;
				},
				NUMBER_OF_GAMES_IN_BATCH,
				false,
				INCLUDE_MIRRORS,
				counter,
				gameContext -> {
					gameContext.setDeckFormat(DeckFormat.spellsource());
					// Ensure the game starts a little faster
					gameContext.getPlayer1().setMana(3);
					gameContext.getPlayer2().setMana(3);
					gameContext.getPlayer1().setMaxMana(3);
					gameContext.getPlayer2().setMaxMana(3);
				}, null);

		var fatigue = result.getPlayer1Stats().getLong(Statistic.FATIGUE_DAMAGE);
		if (fatigue >= 2) {
			LOGGER.warn("evaluate: Had {} fatigue", fatigue);
		}
		if (result.getExceptionCount() > 0) {
			LOGGER.warn("evaluate: Had {} errors", result.getExceptionCount());
		}
		return result.getPlayer1Stats().getDouble(Statistic.WIN_RATE);
	}

	public static void main(String[] args) {
		Factory<Genotype<DoubleGene>> gtf = Genotype.of(Weights.weightedDoubleChromosome());

		var mutatorProbability = 0.5;
		var populationSize = 20;
		var crossoverPercentage = 0.3;
		var crossoverCount = (int) (crossoverPercentage * WeightedFeature.values().length);
		var generations = 20;

		var engine = Engine
				.builder(Weights::evaluate, gtf)
				.populationSize(populationSize)
				.selector(new RouletteWheelSelector<>())
				.alterers(new GaussianMutator<>(mutatorProbability), new MultiPointCrossover<>(crossoverCount))
				.build();

		EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();

		var totalMatches = generations * populationSize * GameContext.simulationCount(decks().size(), NUMBER_OF_GAMES_IN_BATCH, INCLUDE_MIRRORS);
		var monitor = Simulation.getMonitor(counter, totalMatches);
		monitor.start();

		LOGGER.info("main: Starting simulation");
		var result =
				engine.stream()
						.peek(statistics)
						.peek(result1 -> {
							LOGGER.info("main: Statistics on generation {}: \n{}", result1.totalGenerations(), statistics);
							var bestPhenotype = result1.bestPhenotype();
							LOGGER.info("main: Vector on generation {}: \n{}", result1.totalGenerations(),
									fromDoubleGenotype(bestPhenotype.genotype()));
						})
						.limit(generations)
						.collect(EvolutionResult.toBestGenotype());

		monitor.interrupt();
		LOGGER.info("main: Vector=\n{}", fromDoubleGenotype(result));
	}
}
