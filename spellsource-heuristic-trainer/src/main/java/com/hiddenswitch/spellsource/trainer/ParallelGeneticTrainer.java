package com.hiddenswitch.spellsource.trainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVectorConstraints;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.decks.GameDeck;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

/**
 * Multi-node synchronized parallel Genetic Algorithm for optimizing GSVB {@link FeatureVector} weights.
 * <p>
 * Key design principles so logical, well-calibrated weights emerge naturally from self-play:
 * <ol>
 *   <li><b>Unit-Scale Gauge Normalization</b>: Anchored at {@link FeatureVector#getDefault()} where
 *       {@code OWN_HP_FACTOR = 1.0}. Because {@code ThreatBasedHeuristic} is homogeneous of degree 1, every candidate
 *       is normalized after crossover/mutation so {@code OWN_HP_FACTOR == 1.0}, eliminating arbitrary scale drift.</li>
 *   <li><b>Log-Normal Multiplicative Mutation</b>: Weights evolve multiplicatively via
 *       {@code w_d <- w_d * exp(sigma * N(0, 1))} within a natural scale cone around {@code FeatureVector.getDefault()}.
 *       Because {@code exp(z) > 0}, sign polarity is strictly preserved and every feature mutates proportionally to its
 *       natural order of magnitude (e.g., {@code OWN_DECK_COUNT = 0.1} stays O(0.1), while {@code WEAPON_VALUE = 3.0},
 *       {@code HAND_MANA_REDUCTION_VALUE = 1.5}, and {@code OPPONENT_MINION_ATTACK_FACTOR = -1.0} can freely explore
 *       factor-of-2 to factor-of-5 adjustments).</li>
 *   <li><b>Common Random Numbers (CRN) + Out-of-Sample Validation</b>: Contenders are screened on each generation's
 *       shared CRN seed and must pass an independent out-of-sample validation match against
 *       {@code [FeatureVector.getDefault(), bestSoFar]} before replacing {@code bestSoFar}, eliminating winner's curse.</li>
 * </ol>
 */
public class ParallelGeneticTrainer {
	private static final Logger LOG = Logger.getLogger(ParallelGeneticTrainer.class.getName());
	private static final int DIMENSION = WeightedFeature.values().length;
	private static final int HOF_MAX_SIZE = 6;
	private static final double BLX_ALPHA = 0.25;
	private static final Gson GSON = new GsonBuilder().create();

	private static final int[] FEATURE_GROUP_MAP = buildFeatureGroupMap();
	private static final boolean[] IS_CORE_GAMEPLAY_FEATURE = buildCoreFeatureMask();

	private final int generations;
	private final int localPopSize;
	private final int numIslands;
	private final long islandId;
	private final int matchupsPerEval;
	private final int gamesPerMatchup;
	private final List<GameDeck> decks;
	private final FitnessEvaluator evaluator;
	private final MlflowReporter mlflow;
	private final long baseSeed;
	private final double sigmaScale;
	private final long barrierTimeoutMs;

	private final FeatureVector baselineAnchor;
	private final double[] anchorArray;
	private final HallOfFame hof;
	private FeatureVector bestSoFar;
	private double bestWinRate = 0.50;
	private int evaluationCounter = 0;

	public record EvaluatedIndividual(
			int islandId,
			int localIndex,
			double winRate,
			int gamesPlayed,
			double[] weights
	) {}

	public ParallelGeneticTrainer(
			int generations,
			int localPopSize,
			int numIslands,
			long islandId,
			int matchupsPerEval,
			int gamesPerMatchup,
			List<GameDeck> decks,
			FitnessEvaluator evaluator,
			MlflowReporter mlflow,
			Long seed,
			double sigmaInit,
			double[] initialPoint,
			long barrierTimeoutMs) {
		this.generations = generations;
		this.localPopSize = Math.max(2, localPopSize);
		this.numIslands = Math.max(1, numIslands);
		this.islandId = Math.max(0L, islandId);
		this.matchupsPerEval = matchupsPerEval;
		this.gamesPerMatchup = gamesPerMatchup;
		this.decks = decks;
		this.evaluator = evaluator;
		this.mlflow = mlflow;
		this.baseSeed = seed != null ? seed : 20261021L;
		this.sigmaScale = sigmaInit <= 0 ? 1.0 : (sigmaInit > 2.0 ? sigmaInit / 20.0 : sigmaInit);
		this.barrierTimeoutMs = barrierTimeoutMs > 0 ? barrierTimeoutMs : 60_000L;

		this.baselineAnchor = FeatureVectorConstraints.project(FeatureVector.getDefault().clone());
		this.anchorArray = featureVectorToArray(this.baselineAnchor);
		this.hof = new HallOfFame(HOF_MAX_SIZE);
		this.hof.seed(this.baselineAnchor);

		if (initialPoint != null && initialPoint.length == DIMENSION) {
			double[] normalized = normalizeAndProject(Arrays.copyOf(initialPoint, DIMENSION));
			FeatureVector loaded = CmaesTrainer.arrayToFeatureVector(normalized);
			this.hof.add(loaded);
			this.bestSoFar = loaded.clone();
		} else {
			this.bestSoFar = this.baselineAnchor.clone();
		}
	}

	public FeatureVector train() {
		double[][] currentSubPop = initGenerationZero();

		LOG.info(String.format(
				"Starting ParallelGeneticTrainer: dim=%d, generations=%d, localPop=%d, numIslands=%d (globalPop=%d), islandId=%d, baseSeed=%d",
				DIMENSION, generations, localPopSize, numIslands, localPopSize * numIslands, islandId, baseSeed));

		for (int gen = 0; gen < generations; gen++) {
			long crnSeed = baseSeed + gen * 10007L;
			List<FeatureVector> opponents = buildOpponentSet();

			List<EvaluatedIndividual> evaluatedSubPop = new ArrayList<>(localPopSize);
			for (int idx = 0; idx < localPopSize; idx++) {
				evaluationCounter++;
				double[] candidateWeights = normalizeAndProject(Arrays.copyOf(currentSubPop[idx], DIMENSION));
				FeatureVector candidateFv = CmaesTrainer.arrayToFeatureVector(candidateWeights);
				String evalId = String.format("gen%d_i%d_c%d", gen, islandId, idx);

				EvalResult result = evaluator.evaluateWithCrn(
						evalId, candidateFv, opponents, decks, matchupsPerEval, gamesPerMatchup, crnSeed);
				double winRate = result.winRate();

				if (mlflow != null) {
					mlflow.logCandidate(evalId, candidateFv, winRate, evaluationCounter);
				}

				evaluatedSubPop.add(new EvaluatedIndividual((int) islandId, idx, winRate, result.gamesPlayed(), candidateWeights));
			}

			// Upload this island's evaluated sub-population to MLflow and gather the global pool
			List<EvaluatedIndividual> globalPool = new ArrayList<>(evaluatedSubPop);
			if (mlflow != null) {
				String payload = GSON.toJson(evaluatedSubPop);
				mlflow.uploadGenerationIslandPopulation(gen, islandId, payload);

				Map<Integer, String> allIslandJsons = mlflow.gatherGenerationIslandPopulations(gen, numIslands, barrierTimeoutMs);
				globalPool.clear();
				Type listType = new TypeToken<List<EvaluatedIndividual>>() {}.getType();
				for (Map.Entry<Integer, String> entry : allIslandJsons.entrySet()) {
					try {
						List<EvaluatedIndividual> parsed = GSON.fromJson(entry.getValue(), listType);
						if (parsed != null) {
							for (EvaluatedIndividual ind : parsed) {
								if (ind != null && ind.weights() != null && ind.weights().length == DIMENSION) {
									double[] clean = normalizeAndProject(ind.weights());
									globalPool.add(new EvaluatedIndividual(
											ind.islandId(), ind.localIndex(), ind.winRate(), ind.gamesPlayed(), clean));
								}
							}
						}
					} catch (Exception e) {
						LOG.warning("Failed to parse sub-population from island " + entry.getKey() + ": " + e.getMessage());
					}
				}
				if (globalPool.isEmpty()) {
					globalPool.addAll(evaluatedSubPop);
				}
			}

			// Find the incumbent bestSoFar's win rate on this generation's CRN seed (island 0, localIndex 0)
			double incumbentGenWinRate = 0.50;
			for (EvaluatedIndividual ind : globalPool) {
				if (ind.islandId() == 0 && ind.localIndex() == 0) {
					incumbentGenWinRate = ind.winRate();
					break;
				}
			}

			// Sort global pool by descending winRate, tie-breaking deterministically
			globalPool.sort((a, b) -> {
				int cmp = Double.compare(b.winRate(), a.winRate());
				if (cmp != 0) return cmp;
				cmp = Integer.compare(a.islandId(), b.islandId());
				if (cmp != 0) return cmp;
				return Integer.compare(a.localIndex(), b.localIndex());
			});

			EvaluatedIndividual topGlobal = globalPool.get(0);
			double meanWinRate = globalPool.stream().mapToDouble(EvaluatedIndividual::winRate).average().orElse(0.5);
			LOG.info(String.format(
					"Generation %d complete: globalPool=%d, topWinRate=%.1f%% (island %d idx %d), incumbentWinRate=%.1f%%, meanWinRate=%.1f%%",
					gen, globalPool.size(), topGlobal.winRate() * 100, topGlobal.islandId(), topGlobal.localIndex(),
					incumbentGenWinRate * 100, meanWinRate * 100));

			// Out-of-sample validation gate to prevent winner's curse before updating bestSoFar / HallOfFame.
			// We test up to the top 2 contenders that beat the incumbent bestSoFar on this generation's CRN seed.
			int checkedContenders = 0;
			for (EvaluatedIndividual candidate : globalPool) {
				if (candidate.islandId() == 0 && candidate.localIndex() == 0) {
					continue; // skip incumbent itself
				}
				if (candidate.winRate() < incumbentGenWinRate || checkedContenders >= 2) {
					break;
				}
				checkedContenders++;

				FeatureVector contenderFv = CmaesTrainer.arrayToFeatureVector(candidate.weights());
				long valSeed = baseSeed + 777767L + gen * 23333L + checkedContenders * 997L;
				EvalResult valResult = evaluator.evaluateWithCrn(
						String.format("val_gen%d_i%d_c%d", gen, candidate.islandId(), candidate.localIndex()),
						contenderFv,
						List.of(baselineAnchor, bestSoFar),
						decks,
						matchupsPerEval,
						gamesPerMatchup,
						valSeed);
				double combinedWinRate = 0.5 * candidate.winRate() + 0.5 * valResult.winRate();
				LOG.info(String.format(
						"Generation %d validation of island %d idx %d: genWR=%.1f%% (vs incumbent %.1f%%), valWR=%.1f%%, combinedWR=%.1f%%",
						gen, candidate.islandId(), candidate.localIndex(),
						candidate.winRate() * 100, incumbentGenWinRate * 100,
						valResult.winRate() * 100, combinedWinRate * 100));

				// Promote contender if it beat the incumbent on the generation seed AND scored >= 53% out-of-sample
				// against [baselineDefault, bestSoFar].
				if (valResult.winRate() >= 0.53) {
					bestWinRate = Math.max(bestWinRate, combinedWinRate);
					bestSoFar = contenderFv.clone();
					hof.add(bestSoFar);
					if (mlflow != null && islandId == 0) {
						mlflow.logBestSoFar(bestSoFar, bestWinRate, evaluationCounter);
					}
					break;
				}
			}

			if (gen + 1 < generations) {
				currentSubPop = breedNextGenerationSubPop(gen + 1, globalPool);
			}
		}

		double[] finalWeights = normalizeAndProject(featureVectorToArray(bestSoFar));
		bestSoFar = CmaesTrainer.arrayToFeatureVector(finalWeights);
		if (mlflow != null && islandId == 0) {
			mlflow.publishTrustedWeights(bestSoFar, bestWinRate, evaluationCounter);
		}
		LOG.info(String.format("ParallelGeneticTrainer finished after %d evaluations. Best validated win rate: %.1f%%",
				evaluationCounter, bestWinRate * 100));
		return bestSoFar;
	}

	private List<FeatureVector> buildOpponentSet() {
		List<FeatureVector> opponents = new ArrayList<>();
		opponents.add(baselineAnchor);
		opponents.add(bestSoFar);
		for (FeatureVector member : hof.getMembers()) {
			if (opponents.size() >= 4) {
				break;
			}
			opponents.add(member);
		}
		return opponents;
	}

	private double[][] initGenerationZero() {
		double[][] pop = new double[localPopSize][DIMENSION];
		double[] bestArr = featureVectorToArray(bestSoFar);
		for (int idx = 0; idx < localPopSize; idx++) {
			if (islandId == 0 && idx == 0) {
				// Exact incumbent bestSoFar is always evaluated on island 0 slot 0
				pop[idx] = normalizeAndProject(Arrays.copyOf(bestArr, DIMENSION));
			} else {
				Random rng = new Random(baseSeed + islandId * 1009L + idx * 131L);
				double[] candidate = Arrays.copyOf(bestArr, DIMENSION);
				// Broad log-normal initial exploration across core gameplay features
				mutateCandidate(candidate, 1.25, rng);
				pop[idx] = normalizeAndProject(candidate);
			}
		}
		return pop;
	}

	private double[][] breedNextGenerationSubPop(int nextGen, List<EvaluatedIndividual> globalPool) {
		double[][] nextPop = new double[localPopSize][DIMENSION];
		Random rng = new Random(baseSeed + nextGen * 31337L + islandId * 997L);
		double decay = Math.max(0.45, Math.pow(0.92, nextGen));

		for (int idx = 0; idx < localPopSize; idx++) {
			// Island 0 slot 0 always preserves bestSoFar so its win rate on nextGen's CRN seed is directly measured
			if (idx == 0 && islandId == 0) {
				nextPop[idx] = normalizeAndProject(featureVectorToArray(bestSoFar));
				continue;
			}
			// Island 1 slot 0 preserves topGlobal from the previous generation
			if (idx == 0 && islandId == 1) {
				nextPop[idx] = normalizeAndProject(Arrays.copyOf(globalPool.get(0).weights(), DIMENSION));
				continue;
			}

			double[] p1 = tournamentSelect(globalPool, 3, rng).weights();
			double[] p2 = tournamentSelect(globalPool, 3, rng).weights();
			double[] child = new double[DIMENSION];

			if (rng.nextBoolean()) {
				// BLX-alpha blend crossover
				for (int d = 0; d < DIMENSION; d++) {
					double min = Math.min(p1[d], p2[d]);
					double max = Math.max(p1[d], p2[d]);
					double range = max - min;
					double low = min - BLX_ALPHA * range;
					double high = max + BLX_ALPHA * range;
					child[d] = low + rng.nextDouble() * (high - low);
				}
			} else {
				// Coherent feature-group block crossover
				boolean[] takeFromP1 = new boolean[5];
				for (int g = 0; g < 5; g++) {
					takeFromP1[g] = rng.nextBoolean();
				}
				for (int d = 0; d < DIMENSION; d++) {
					int group = FEATURE_GROUP_MAP[d];
					child[d] = takeFromP1[group] ? p1[d] : p2[d];
				}
			}

			mutateCandidate(child, decay, rng);
			nextPop[idx] = normalizeAndProject(child);
		}
		return nextPop;
	}

	/**
	 * Applies log-normal multiplicative mutations {@code w_d <- w_d * exp(sigma * N(0, 1))}, prioritizing active core
	 * gameplay features (board stats, intrinsic/count, hero HP, weapon, armor, card advantage, handbuff/mana discount,
	 * modern mechanics, and key combat keywords) so the evolutionary search focuses signal on features that govern
	 * turn-by-turn decisions while keeping all weights strictly sign-consistent and proportionally scaled.
	 */
	private void mutateCandidate(double[] candidate, double decay, Random rng) {
		int hpOrdinal = WeightedFeature.OWN_HP_FACTOR.ordinal();

		for (int d = 0; d < DIMENSION; d++) {
			if (d == hpOrdinal) {
				continue;
			}
			boolean isCore = IS_CORE_GAMEPLAY_FEATURE[d];
			double mutProb = isCore ? 0.45 : 0.12;
			if (rng.nextDouble() < mutProb) {
				double sigma = (isCore ? 0.52 : 0.22) * sigmaScale * decay;
				double z = rng.nextGaussian() * sigma;
				if (Math.abs(anchorArray[d]) < 1e-6) {
					// Feature that is 0.0 in getDefault() (e.g. OPPONENT_MINION_RUSH_MODIFIER)
					candidate[d] = -Math.abs(candidate[d] + rng.nextGaussian() * 0.5 * decay);
				} else {
					candidate[d] *= Math.exp(z);
				}
			}
		}
	}

	/**
	 * Projects onto sign-polarity constraints, fixes the linear gauge so {@code OWN_HP_FACTOR == 1.0}, and bounds
	 * each feature within a wide log-normal trust region {@code [0.15x, 2.5x]} of {@link FeatureVector#getDefault()}
	 * so inactive features cannot drift by orders of magnitude.
	 */
	private double[] normalizeAndProject(double[] weights) {
		FeatureVectorConstraints.project(weights);
		int hpIdx = WeightedFeature.OWN_HP_FACTOR.ordinal();
		double hpScale = weights[hpIdx];
		if (Double.isFinite(hpScale) && hpScale > 0.05 && Math.abs(hpScale - 1.0) > 1e-6) {
			for (int i = 0; i < DIMENSION; i++) {
				weights[i] /= hpScale;
			}
		}
		weights[hpIdx] = 1.0;

		for (int d = 0; d < DIMENSION; d++) {
			if (d == hpIdx) {
				continue;
			}
			double def = anchorArray[d];
			if (Math.abs(def) > 1e-6) {
				double minMag = Math.abs(def) * 0.15;
				double maxMag = Math.abs(def) * 2.50;
				double mag = Math.max(minMag, Math.min(maxMag, Math.abs(weights[d])));
				weights[d] = Math.copySign(mag, def);
			} else {
				weights[d] = Math.max(-3.0, Math.min(0.0, weights[d]));
			}
		}

		// Round to 3 decimal places for clean, readable output
		for (int d = 0; d < DIMENSION; d++) {
			weights[d] = Math.round(weights[d] * 1000.0) / 1000.0;
		}
		weights[hpIdx] = 1.0;
		return FeatureVectorConstraints.project(weights);
	}

	private EvaluatedIndividual tournamentSelect(List<EvaluatedIndividual> pool, int k, Random rng) {
		EvaluatedIndividual best = pool.get(rng.nextInt(pool.size()));
		for (int i = 1; i < k; i++) {
			EvaluatedIndividual contender = pool.get(rng.nextInt(pool.size()));
			if (contender.winRate() > best.winRate()) {
				best = contender;
			}
		}
		return best;
	}

	public static double[] featureVectorToArray(FeatureVector fv) {
		WeightedFeature[] features = WeightedFeature.values();
		double[] arr = new double[features.length];
		for (int i = 0; i < features.length; i++) {
			arr[i] = fv.get(features[i]);
		}
		return arr;
	}

	public double getBestWinRate() {
		return bestWinRate;
	}

	private static int[] buildFeatureGroupMap() {
		WeightedFeature[] features = WeightedFeature.values();
		int[] map = new int[features.length];
		for (int i = 0; i < features.length; i++) {
			String name = features[i].name();
			if (name.contains("LOCATION") || name.contains("DORMANT") || name.contains("HAND_BUFF")
					|| name.contains("HAND_MANA") || name.contains("IMBUE") || name.contains("STARSHIP")) {
				map[i] = 4;
			} else if (name.startsWith("OPPONENT_MINION")) {
				map[i] = 3;
			} else if (name.startsWith("MINION_") || name.equals("OWN_MINION_COUNT")) {
				map[i] = 2;
			} else if (name.contains("HP_FACTOR") || name.contains("MODIFIER") || name.contains("ARMOR") || name.contains("WEAPON")) {
				map[i] = 0;
			} else {
				map[i] = 1;
			}
		}
		return map;
	}

	private static boolean[] buildCoreFeatureMask() {
		WeightedFeature[] features = WeightedFeature.values();
		boolean[] mask = new boolean[features.length];
		Set<WeightedFeature> core = EnumSet.of(
				WeightedFeature.OPPONENT_HP_FACTOR,
				WeightedFeature.OWN_CARD_COUNT,
				WeightedFeature.OPPONENT_CARD_COUNT,
				WeightedFeature.MINION_INTRINSIC_VALUE,
				WeightedFeature.MINION_ATTACK_FACTOR,
				WeightedFeature.MINION_HP_FACTOR,
				WeightedFeature.OWN_MINION_COUNT,
				WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE,
				WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR,
				WeightedFeature.OPPONENT_MINION_HP_FACTOR,
				WeightedFeature.OPPONENT_MINION_COUNT,
				WeightedFeature.WEAPON_VALUE,
				WeightedFeature.OPPONENT_WEAPON_VALUE,
				WeightedFeature.OWN_ARMOR_FACTOR,
				WeightedFeature.OPPONENT_ARMOR_FACTOR,
				WeightedFeature.HAND_BUFF_VALUE,
				WeightedFeature.HAND_MANA_REDUCTION_VALUE,
				WeightedFeature.OWN_LOCATION_VALUE,
				WeightedFeature.OPPONENT_LOCATION_VALUE,
				WeightedFeature.OWN_DORMANT_VALUE,
				WeightedFeature.OPPONENT_DORMANT_VALUE,
				WeightedFeature.IMBUE_VALUE,
				WeightedFeature.STARSHIP_VALUE,
				WeightedFeature.CORPSE_COUNT_VALUE,
				WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER,
				WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER,
				WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER,
				WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER,
				WeightedFeature.MINION_DEATHRATTLE_MODIFIER,
				WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER,
				WeightedFeature.MINION_RUSH_MODIFIER,
				WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER,
				WeightedFeature.MINION_LIFESTEAL_MODIFIER,
				WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER,
				WeightedFeature.MINION_REBORN_MODIFIER,
				WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER
		);
		for (int i = 0; i < features.length; i++) {
			mask[i] = core.contains(features[i]);
		}
		return mask;
	}
}
