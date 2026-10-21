package com.hiddenswitch.spellsource.trainer;

import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Command(name = "audit", description = "Audit turn-by-turn GSVB gameplay and benchmark tactical quality")
public class GameplayAuditor implements Callable<Integer> {

	@Option(names = "--games", description = "Number of games to audit with transcripts", defaultValue = "4")
	int gamesToTranscript;

	@Option(names = "--benchmark-pairs", description = "Number of deck pairs for head-to-head benchmark (2 swap-side games each)", defaultValue = "16")
	int benchmarkPairs;

	public static class AuditStats {
		public int totalTurns = 0;
		public int totalActions = 0;
		public int actionsSearched = 0;
		public int actionsFromPlan = 0;
		public int replansOnCardDraw = 0;
		public int endTurnWithFaceAttackAvailable = 0;
		public int endTurnWithAnyAttackAvailable = 0;
		public int endTurnWithPlayableCardsAfterDraw = 0;
		public List<String> anomalies = new ArrayList<>();
	}

	public static class AuditingGsvb extends GameStateValueBehaviour {
		private final AuditStats stats;
		private final boolean verbose;
		private final List<String> transcript;
		private int lastTurn = -1;
		private Set<Integer> lastHandIds = new HashSet<>();

		public AuditingGsvb(FeatureVector fv, String name, AuditStats stats, boolean verbose, List<String> transcript) {
			super(fv, name);
			this.stats = stats;
			this.verbose = verbose;
			this.transcript = transcript;
			this.setTimeout(2500);
			this.setLethalTimeout(4000);
		}

		@Override
		public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
			int turn = context.getTurn();
			Player opponent = context.getOpponent(player);
			boolean newTurn = (turn != lastTurn);
			if (newTurn) {
				lastTurn = turn;
				lastHandIds = player.getHand().stream().map(Card::getId).collect(Collectors.toSet());
				stats.totalTurns++;
				if (verbose) {
					transcript.add(String.format(
							"\n--- Turn %d: P%d (%s, HP=%d+%d, Mana=%d/%d, Hand=%d) vs P%d (%s, HP=%d+%d, Board=%d) ---",
							turn, player.getId(), player.getName(),
							player.getHero().getHp(), player.getHero().getArmor(),
							player.getMana(), player.getMaxMana(), player.getHand().getCount(),
							opponent.getId(), opponent.getName(),
							opponent.getHero().getHp(), opponent.getHero().getArmor(),
							opponent.getMinions().size()));
					transcript.add("  Own Board : " + formatBoard(context, player));
					transcript.add("  Opp Board : " + formatBoard(context, opponent));
					transcript.add("  Own Hand  : " + formatHand(context, player));
				}
			}

			Set<Integer> currentHandIds = player.getHand().stream().map(Card::getId).collect(Collectors.toSet());
			boolean drewNewCard = currentHandIds.stream().anyMatch(id -> !lastHandIds.contains(id));
			boolean hadNonEmptyPlanBefore = (getStrictPlan() != null && !getStrictPlan().isEmpty());
			int planSizeBefore = hadNonEmptyPlanBefore ? getStrictPlan().size() : 0;

			GameAction chosen = super.requestAction(context, player, validActions);
			int planSizeAfter = (getStrictPlan() != null) ? getStrictPlan().size() : 0;

			// Determine if super.requestAction actually popped from the pre-existing plan vs ran a new search
			boolean usedPreExistingPlan = hadNonEmptyPlanBefore && (planSizeAfter == planSizeBefore - 1) && !(drewNewCard && !newTurn);
			if (hadNonEmptyPlanBefore && drewNewCard && !newTurn) {
				stats.replansOnCardDraw++;
			}

			stats.totalActions++;
			if (usedPreExistingPlan) {
				stats.actionsFromPlan++;
			} else {
				stats.actionsSearched++;
			}

			if (chosen != null) {
				if (chosen.getActionType() == ActionType.END_TURN) {
					boolean hasFaceAttack = validActions.stream().anyMatch(a ->
							a.getActionType() == ActionType.PHYSICAL_ATTACK
									&& Objects.equals(a.getTargetReference(), opponent.getHero().getReference()));
					boolean hasAnyAttack = validActions.stream().anyMatch(a ->
							a.getActionType() == ActionType.PHYSICAL_ATTACK);
					if (hasFaceAttack) {
						stats.endTurnWithFaceAttackAvailable++;
						String msg = String.format("Turn %d P%d (%s): END_TURN chosen while face attack was available! (fromPlan=%s, ownBoard=%s)",
								turn, player.getId(), player.getName(), usedPreExistingPlan, formatBoard(context, player));
						stats.anomalies.add(msg);
					} else if (hasAnyAttack) {
						stats.endTurnWithAnyAttackAvailable++;
					}

					boolean hasPlayableCard = validActions.stream().anyMatch(a ->
							a.getActionType() == ActionType.SUMMON
									|| a.getActionType() == ActionType.SPELL
									|| a.getActionType() == ActionType.EQUIP_WEAPON);
					if (drewNewCard && hasPlayableCard && usedPreExistingPlan) {
						stats.endTurnWithPlayableCardsAfterDraw++;
						String msg = String.format("Turn %d P%d (%s): END_TURN chosen from cached strictPlan right after drawing a card with playable card(s) and %d mana left!",
								turn, player.getId(), player.getName(), player.getMana());
						stats.anomalies.add(msg);
					}
				}

				if (verbose) {
					transcript.add(String.format("  [Action%s] %s",
							usedPreExistingPlan ? " (PLAN)" : " (SEARCHED)",
							describeAction(context, player, chosen)));
				}
			}

			lastHandIds = currentHandIds;
			return chosen;
		}
	}

	/**
	 * Prior baseline GSVB (before postProcess friendly TurnEndTrigger fix, before receding-horizon / card-draw plan
	 * invalidation, and with the pre-audit unprojected weapon/handbuff/threat weights).
	 */
	public static class LegacyGsvb extends GameStateValueBehaviour {
		public LegacyGsvb() {
			super(getLegacyWeights(), "Legacy-GSVB");
			this.setParallel(false);
			this.setTimeout(2000);
			this.setLethalTimeout(3500);
		}

		private static FeatureVector getLegacyWeights() {
			FeatureVector v = FeatureVector.getFittest().clone();
			v.set(WeightedFeature.RED_MODIFIER, -34.931);
			v.set(WeightedFeature.YELLOW_MODIFIER, -71.119);
			v.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, -20.423);
			v.set(WeightedFeature.MINION_RUSH_MODIFIER, -45.326);
			v.set(WeightedFeature.WEAPON_VALUE, 49.062);
			v.set(WeightedFeature.HAND_BUFF_VALUE, 95.339);
			v.set(WeightedFeature.HAND_MANA_REDUCTION_VALUE, 90.670);
			return v;
		}

		@Override
		protected boolean shouldInvalidateTopLevelPlan(GameContext context, Player player, List<GameAction> validActions, GameAction planAction) {
			return false;
		}

		@Override
		protected boolean isRecedingHorizonPlan() {
			return false;
		}

		@Override
		protected void postProcess(int playerId, GameContext context) {
			if (isTriggerStartTurns() && !context.updateAndGetGameOver()) {
				var player = context.getPlayer(playerId);
				var opponent = context.getOpponent(player);
				var opponentHp = opponent.getHero().getHp();
				for (var trigger : new ArrayList<>(context.getTriggers())) {
					if (trigger instanceof Enchantment && !(trigger instanceof Aura) && !trigger.isExpired()) {
						var enchantment = (Enchantment) trigger;
						// Legacy bug: did not fire friendly TurnEndTrigger on depth-horizon leaves
						if (enchantment.getTriggers().stream().anyMatch(e -> e.getClass().equals(TurnStartTrigger.class)
								|| (e.getClass().equals(TurnEndTrigger.class) && enchantment.getOwner() == opponent.getId()))) {
							context.getTriggerHostStack().push(trigger.getHostReference());
							context.getLogic().castSpell(trigger.getOwner(), enchantment.getSpell(), trigger.getHostReference(), EntityReference.NONE, TargetSelection.NONE, true, null);
							context.getTriggerHostStack().pop();
						}
					}
				}
				if (opponent.getHero().getHp() <= 0) {
					opponent.getHero().setHp(opponentHp);
				}
				if (opponent.getHero().isDestroyed()) {
					opponent.getHero().getAttributes().remove(Attribute.DESTROYED);
				}
				context.getLogic().endOfSequence();
			}
		}
	}

	private static String formatBoard(GameContext context, Player player) {
		if (player.getMinions().isEmpty()) {
			return "(empty)";
		}
		List<String> parts = new ArrayList<>();
		for (Minion m : player.getMinions()) {
			parts.add(String.format("%s[%d/%d%s%s]",
					m.getName(), m.getAttack(), m.getHp(),
					m.canAttackThisTurn(context) ? ",RDY" : "",
					m.hasAttribute(Attribute.TAUNT) ? ",TAUNT" : ""));
		}
		return String.join(", ", parts);
	}

	private static String formatHand(GameContext context, Player player) {
		if (player.getHand().isEmpty()) {
			return "(empty)";
		}
		List<String> parts = new ArrayList<>();
		for (Card c : player.getHand()) {
			int cost = context.getLogic().getModifiedManaCost(player, c);
			parts.add(String.format("%s(%dm)", c.getName(), cost));
		}
		return String.join(", ", parts);
	}

	private static String describeAction(GameContext context, Player player, GameAction action) {
		try {
			Entity src = action.getSourceReference() != null ? context.tryFind(action.getSourceReference()) : null;
			Entity tgt = action.getTargetReference() != null ? context.tryFind(action.getTargetReference()) : null;
			String srcStr = src != null ? src.getName() : "";
			String tgtStr = tgt != null ? " -> " + tgt.getName() + (tgt instanceof Actor a ? "[" + a.getAttack() + "/" + a.getHp() + "]" : "") : "";
			return action.getActionType() + (srcStr.isEmpty() ? "" : ": " + srcStr) + tgtStr;
		} catch (Exception e) {
			return action.getActionType().toString();
		}
	}

	private record MatchTask(GameDeck d1, GameDeck d2, boolean candidateIsP0, long seed) {}

	private void runBenchmark(String label, List<GameDeck> decks, int numPairs,
	                          Supplier<Behaviour> candidateSupplier, Supplier<Behaviour> opponentSupplier) {
		List<MatchTask> tasks = new ArrayList<>();
		for (int i = 0; i < numPairs; i++) {
			GameDeck d1 = decks.get((i * 5) % decks.size());
			GameDeck d2 = decks.get((i * 5 + 7) % decks.size());
			long seed = 50000L + i * 997L;
			tasks.add(new MatchTask(d1, d2, true, seed));
			tasks.add(new MatchTask(d1, d2, false, seed));
		}

		AtomicInteger wins = new AtomicInteger(0);
		AtomicInteger completed = new AtomicInteger(0);
		long start = System.currentTimeMillis();

		tasks.parallelStream().forEach(t -> {
			try {
				GameContext game = GameContext.fromDecks(t.seed, List.of(t.d1, t.d2));
				game.setBehaviour(0, t.candidateIsP0 ? candidateSupplier.get() : opponentSupplier.get());
				game.setBehaviour(1, t.candidateIsP0 ? opponentSupplier.get() : candidateSupplier.get());
				game.play();
				Player winner = game.getWinner();
				boolean candidateWon = winner != null && ((t.candidateIsP0 && winner.getId() == 0) || (!t.candidateIsP0 && winner.getId() == 1));
				if (candidateWon) {
					wins.incrementAndGet();
				}
				completed.incrementAndGet();
			} catch (Exception ignored) {
			}
		});

		long elapsed = (System.currentTimeMillis() - start) / 1000;
		double wr = 100.0 * wins.get() / Math.max(1, completed.get());
		System.out.printf("BENCHMARK %-48s : %2d / %2d wins (%5.1f%%) in %ds%n",
				label, wins.get(), completed.get(), wr, elapsed);
	}

	@Override
	public Integer call() throws Exception {
		TrainerMain.loadCards();
		CardIdMapper mapper = new CardIdMapper();
		DeckPool deckPool = new DeckPool(mapper);
		List<GameDeck> decks = deckPool.getDecks();

		AuditStats stats = new AuditStats();
		List<String> transcript = new ArrayList<>();

		for (int g = 0; g < gamesToTranscript; g++) {
			GameDeck d1 = decks.get((g * 3) % decks.size());
			GameDeck d2 = decks.get((g * 3 + 5) % decks.size());
			long seed = 1000L + g;

			transcript.add("\n================================================================================");
			transcript.add(String.format("GAME %d (seed=%d): %s vs %s", g + 1, seed, d1.getName(), d2.getName()));
			transcript.add("================================================================================");

			try {
				GameContext game = GameContext.fromDecks(seed, List.of(d1, d2));
				game.setBehaviour(0, new AuditingGsvb(FeatureVector.getFittest(), "Audit-P0", stats, g == 0, transcript));
				game.setBehaviour(1, new AuditingGsvb(FeatureVector.getFittest(), "Audit-P1", stats, g == 0, transcript));
				game.play();

				Player winner = game.getWinner();
				transcript.add(String.format("RESULT Game %d: Winner=%s in %d turns (P0 HP=%d, P1 HP=%d)",
						g + 1, winner != null ? (winner.getId() == 0 ? d1.getName() : d2.getName()) : "DRAW",
						game.getTurn(),
						game.getPlayer1().getHero().getHp(),
						game.getPlayer2().getHero().getHp()));
			} catch (Exception e) {
				transcript.add("ERROR Game " + (g + 1) + ": " + e.getMessage());
			}
		}

		for (String line : transcript) {
			System.out.println(line);
		}

		System.out.println("\n=== AUDIT SUMMARY ACROSS " + gamesToTranscript + " GAMES ===");
		System.out.println("Total turns                     : " + stats.totalTurns);
		System.out.println("Total actions                   : " + stats.totalActions);
		System.out.println("Actions searched freshly        : " + stats.actionsSearched);
		System.out.println("Actions taken from cached plan  : " + stats.actionsFromPlan);
		System.out.println("Mid-turn replans on card draw   : " + stats.replansOnCardDraw);
		System.out.println("END_TURN with face attack avail : " + stats.endTurnWithFaceAttackAvailable);
		System.out.println("END_TURN after draw w/ playable : " + stats.endTurnWithPlayableCardsAfterDraw);
		if (!stats.anomalies.isEmpty()) {
			System.out.println("\nAnomalies detected:");
			for (String a : stats.anomalies) {
				System.out.println("  - " + a);
			}
		}

		if (benchmarkPairs > 0) {
			System.out.println("\n=== HEAD-TO-HEAD CRN SWAP-SIDE BENCHMARKS (" + (benchmarkPairs * 2) + " GAMES EACH) ===");
			Supplier<Behaviour> improvedGsvb = () -> new GameStateValueBehaviour(FeatureVector.getFittest(), "Improved-GSVB")
					.setParallel(false).setTimeout(2000).setLethalTimeout(3500);
			Supplier<Behaviour> defaultWeightsGsvb = () -> new GameStateValueBehaviour(FeatureVector.getDefault(), "Default-Weights-GSVB")
					.setParallel(false).setTimeout(2000).setLethalTimeout(3500);
			Supplier<Behaviour> greedy1Ply = () -> new GameStateValueBehaviour(FeatureVector.getFittest(), "Greedy-1Ply")
					.setMaxDepth(1).setExpandDepthForLethal(false).setParallel(false).setTimeout(1500);
			Supplier<Behaviour> legacyGsvb = LegacyGsvb::new;

			runBenchmark("Trained Fittest vs Default Weights (Fixed GSVB)", decks, benchmarkPairs, improvedGsvb, defaultWeightsGsvb);
			runBenchmark("Improved GSVB vs PlayRandomBehaviour", decks, benchmarkPairs, improvedGsvb, PlayRandomBehaviour::new);
			runBenchmark("Improved GSVB vs 1-Ply Greedy Heuristic", decks, benchmarkPairs, improvedGsvb, greedy1Ply);
			runBenchmark("Improved GSVB vs Legacy GSVB (Pre-Audit)", decks, benchmarkPairs, improvedGsvb, legacyGsvb);
		}
		return 0;
	}
}
