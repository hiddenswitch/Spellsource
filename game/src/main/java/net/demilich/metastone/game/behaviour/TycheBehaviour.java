package net.demilich.metastone.game.behaviour;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.spells.trigger.InspireTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * A faithful recreation of the Tyche AI agent.
 * <p>
 * This documentation is a work in progress.
 */
public class TycheBehaviour extends IntelligentBehaviour {
	public final static DeckCreateRequest midrangeShaman() {
		return new DeckCreateRequest().withFormat("Standard")
				.withHeroClass("SILVER")
				.withCardIds(Arrays.asList(
						"minion_tunnel_trogg",
						"minion_tunnel_trogg",
						"minion_totem_golem",
						"minion_totem_golem",
						"minion_thing_from_below",
						"minion_thing_from_below",
						"weapon_spirit_claws",
						"weapon_spirit_claws",
						"spell_maelstrom_portal",
						"spell_maelstrom_portal",
						"spell_lightning_storm",
						"spell_lightning_bolt",
						"spell_jade_lightning",
						"spell_jade_lightning",
						"weapon_jade_claws",
						"weapon_jade_claws",
						"spell_hex",
						"spell_hex",
						"minion_flametongue_totem",
						"minion_flametongue_totem",
						"minion_al_akir_the_windlord",
						"minion_patches_the_pirate",
						"minion_small-time_buccaneer",
						"minion_bloodmage_thalnos",
						"minion_barnes",
						"minion_azure_drake",
						"minion_aya_blackpaw",
						"minion_ragnaros_the_firelord"
				));
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(TycheBehaviour.class);
	private final static double EXPLORE_THRESHOLD = 0.75;
	private final static int DEFAULT_NUM_EPISODES_MULTIPLIER = 100;
	private final static int LEARNING_NUM_EPISODES_MULTIPLIER = 20;

	public enum Algorithm {
		GREEDY,
		SEARCH_TREE
	}

	private StateAnalyzer analyzer;
	private SimTree simTree;
	private XORShiftRandom random;
	private boolean isTurnBegin = true;
	private boolean hasInitialized;
	private boolean heroBasedWeights;
	private int curEpisodeMultiplier;
	private int defaultEpisodeMultiplier;
	private double secondsStarted = System.currentTimeMillis() / 1000.0;
	private double turnTimeStart;
	private Algorithm usedAlgorithm = Algorithm.SEARCH_TREE;
	private boolean adjustEpisodeMultiplier = false;
	private boolean printTurnTime = false;

	private TycheBehaviour(StateWeights weights, boolean heroBasedWeights, int episodeMultiplier, boolean adjustEpisodeMultiplier) {
		this.defaultEpisodeMultiplier = episodeMultiplier;
		this.curEpisodeMultiplier = episodeMultiplier;
		this.heroBasedWeights = heroBasedWeights;
		this.analyzer = new StateAnalyzer(weights);
		this.simTree = new SimTree();
		this.random = new XORShiftRandom(System.currentTimeMillis());
		this.adjustEpisodeMultiplier = adjustEpisodeMultiplier;
	}

	public TycheBehaviour() {
		this(StateWeights.getDefault(), true, DEFAULT_NUM_EPISODES_MULTIPLIER, true);
	}

	private double getSecondsSinceStart() {
		return System.currentTimeMillis() / 1000.0 - secondsStarted;
	}

	@Override
	public String getName() {
		return "Tyche Behaviour";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		hasInitialized = false;
		customInit(context, player);
		return super.mulligan(context, player, cards);
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (!hasInitialized) {
			customInit(context, player);
		}

		if (isTurnBegin) {
			onMyTurnBegin(context, player);
		}

		context = GameContext.fromTrace(context.getTrace());

		GameAction chosenTask = chooseTask(context, player, validActions);

		// Choose a random task
		if (chosenTask == null) {
			LOGGER.error("requestAction {} {}: Chosen task was null", context.getGameId(), player);
			chosenTask = validActions.get(ThreadLocalRandom.current().nextInt(validActions.size()));
		}

		if (chosenTask.getActionType() == ActionType.END_TURN) {
			onMyTurnEnd();
		}

		return chosenTask;
	}

	@Suspendable
	private GameAction chooseTask(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}

		if (usedAlgorithm == Algorithm.SEARCH_TREE) {
			return getSimulationTreeTask(context, player, validActions);
		} else if (usedAlgorithm == Algorithm.GREEDY) {
			return getGreedyBestTask(context, player, validActions);
		}

		return null;
	}

	@Suspendable
	private GameAction getSimulationTreeTask(GameContext context, Player player, List<GameAction> options) {
		double time = getSecondsSinceStart() - turnTimeStart;

		if (time >= Const.MAX_TURN_TIME) {
			LOGGER.error("getSimulationTreeTask {} {}: Turn time takes too long, fall back to greedy", context.getGameId(), player);
			return getGreedyBestTask(context, player, options);
		}

		simTree.initTree(analyzer, context, player, options);

		// -1 because TurnEnd won't be looked at
		int optionCount = options.size() - 1;
		int numEpisodes = optionCount * curEpisodeMultiplier;
		double simStart = getSecondsSinceStart();

		for (int i = 0; i < numEpisodes; i++) {
			if (!isAllowedToSimulate(simStart, i, numEpisodes, optionCount)) {
				break;
			}

			boolean shouldExploit = (double) i / numEpisodes > EXPLORE_THRESHOLD;
			simTree.simulateEpisode(random, i, shouldExploit);
		}


		TaskNode bestNode = simTree.getBestNode();
		return bestNode.task;
	}

	@Suspendable
	private GameAction getGreedyBestTask(GameContext context, Player player, List<GameAction> validActions) {
		List<SimResult> bestTasks = StateUtility.getSimulatedBestTasks(1, context, player, validActions, analyzer);
		return bestTasks.get(0).task;
	}


	private boolean isAllowedToSimulate(double startTime, int curEpisode, int maxEpisode, int options) {
		double time = getSecondsSinceStart() - startTime;
		if (time >= Const.MAX_SIMULATION_TIME) {
			LOGGER.warn("isAllowedToSimulate: Stopped simulations after {}s and {} of {} episodes. Having {} options.", time, curEpisode, maxEpisode, options);
			return false;
		}
		return true;
	}

	@Suspendable
	private void onMyTurnBegin(GameContext context, Player player) {
		isTurnBegin = false;
		turnTimeStart = getSecondsSinceStart();
	}

	@Suspendable
	private void onMyTurnEnd() {
		isTurnBegin = false;
		double timeNeeded = getSecondsSinceStart() - turnTimeStart;
		if (adjustEpisodeMultiplier && usedAlgorithm == Algorithm.SEARCH_TREE) {
			final double MAX_DIFF = 4.0;
			double diff = Math.min(Const.DECREASE_SIMULATION_TIME - timeNeeded, MAX_DIFF);
			double factor = 0.5;

			// Reduce more if above the time limit
			if (diff <= 0d) {
				factor = 0.2;
			}

			// Simulate at max this value * defaultEpisodeMultiplier
			final int MAX_EPISODE_MULTIPLIER = 4;
			curEpisodeMultiplier = MathUtils.clamp(
					curEpisodeMultiplier + (int) (factor * diff * defaultEpisodeMultiplier),
					defaultEpisodeMultiplier,
					defaultEpisodeMultiplier * MAX_EPISODE_MULTIPLIER);
		}

		if (printTurnTime) {
			LOGGER.info("onMyTurnEnd: Turn took {}s", timeNeeded);
		}

		if (timeNeeded >= Const.MAX_TURN_TIME) {
			LOGGER.warn("onMyTurnEnd: Turn took {}s", timeNeeded);
		}
	}

	public static TycheBehaviour getLearningAgent(StateWeights weights) {
		return new TycheBehaviour(weights, false, LEARNING_NUM_EPISODES_MULTIPLIER, false);
	}

	public static TycheBehaviour getSearchTreeAgent(int episodeMultiplier) {
		return new TycheBehaviour(StateWeights.getDefault(), true, episodeMultiplier, true);
	}

	public static TycheBehaviour getTrainingAgent() {
		return getTrainingAgent(-1d, false);
	}

	private static TycheBehaviour getTrainingAgent(double biasFactor, boolean useSecrets) {
		final boolean ADJUST_EPISODES = false;
		final boolean HERO_BASED_WEIGHTS = false;

		StateWeights weights = StateWeights.getDefault();

		if (biasFactor >= 0) {
			weights.setWeight(StateWeights.WeightType.BiasFactor, biasFactor);
		}

		TycheBehaviour agent = new TycheBehaviour(weights, HERO_BASED_WEIGHTS, 0, ADJUST_EPISODES);
		agent.usedAlgorithm = Algorithm.GREEDY;
		agent.analyzer.estimateSecretsAndSpells = useSecrets;
		return agent;
	}


	/**
	 * Called the first round (might be second round game wise) this agents is able to see the game and his opponent.
	 *
	 * @param context
	 * @param player
	 */
	@Suspendable
	private void customInit(GameContext context, Player player) {
		hasInitialized = true;
		secondsStarted = System.currentTimeMillis() / 1000.0;
		analyzer.ownPlayerId = player.getId();
		if (heroBasedWeights) {
			analyzer.weights = StateWeights.getHeroBased(player.getHero().getHeroClass(), context.getOpponent(player).getHero().getHeroClass());
		}
	}

	@FunctionalInterface
	private interface SecretValueCalculation {
		void mutate(State playerState, State opponentState, Player player, Player opponent, Card card);
	}

	@FunctionalInterface
	private interface SpellValueCalculation {
		void mutate(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card card);
	}


	private static class StateAnalyzer {
		StateWeights weights;
		int ownPlayerId = -1;
		boolean estimateSecretsAndSpells = true;

		StateAnalyzer(StateWeights weights) {
			this.weights = weights;
		}

		boolean isMyPlayer(Player player) {
			return player.getId() == ownPlayerId;
		}

		double getStateValue(GameContext context, State playerState, State enemyState, Player player, Player opponent, GameAction task) {

			if (estimateSecretsAndSpells) {
				SecretUtil.calculateValues(playerState, enemyState, player, opponent);
				SecretUtil.estimateValues(enemyState, opponent);

				if (task instanceof PlaySpellCardAction) {
					Card spellCard = (Card) task.getSource(context);
					if (!spellCard.isSecret()) {
						SpellUtil.calculateValues(context, enemyState, player, opponent, task, spellCard, playerState);
					}
				}
			}

			if (hasLost(enemyState)) {
				return Double.POSITIVE_INFINITY;
			}

			if (hasLost(playerState)) {
				return Double.NEGATIVE_INFINITY;
			}

			return getStateValueFor(playerState, enemyState) - getStateValueFor(enemyState, playerState);
		}

		private double getStateValueFor(State player, State enemy) {
			double emptyFieldValue = weights.getWeight(StateWeights.WeightType.EmptyField) * getEmptyFieldValue(enemy);
			double healthValue = weights.getWeight(StateWeights.WeightType.HealthFactor) * getHeroHealthArmorValue(player);
			double deckValue = weights.getWeight(StateWeights.WeightType.DeckFactor) * getDeckValue(player);
			double handValue = weights.getWeight(StateWeights.WeightType.HandFactor) * getHandValues(player);
			double minionValue = weights.getWeight(StateWeights.WeightType.MinionFactor) * getMinionValues(player);
			double biasValues = weights.getWeight(StateWeights.WeightType.BiasFactor) * getBiasValue(player);
			return emptyFieldValue + deckValue + healthValue + handValue + minionValue + biasValues;
		}

		private double getEmptyFieldValue(State state) {
			// It's better to clear the board in later stages of the game (more enemies might appear each round)
			if (state.NumMinionsOnBoard == 0) {
				return 2 + Math.min(state.TurnNumber, 10);
			}
			return 0;
		}

		/**
		 * Gives points for having cards in the deck. Having no cards give additional penalty.
		 *
		 * @param state
		 * @return
		 */
		private double getDeckValue(State state) {
			int numCards = state.NumDeckCards;
			return Math.sqrt(numCards - state.Fatigue);
		}

		/**
		 * Gives points for having health, treat armor as additional health.
		 *
		 * @param state
		 * @return
		 */
		private double getHeroHealthArmorValue(State state) {
			return Math.sqrt(state.HeroHealth + state.HeroArmor);
		}

		private double getHandValues(State state) {
			int firstThree = Math.min(state.NumHandCards, 3);
			int remaining = Math.abs(state.NumHandCards - firstThree);
			//3 times the points for the first three cards, 2 for all remaining cards:
			return 3 * firstThree + 2 * remaining;
		}

		private double getMinionValues(State player) {
			return player.MinionValues + (player.WeaponDamage * player.WeaponDurability);
		}

		private double getBiasValue(State player) {
			return player.BiasValue;
		}

		private boolean hasLost(State state) {
			return state.HeroHealth <= 0;
		}
	}

	private static class SecretUtil {
		private static final double SECRET_VALUE_FACTOR = 3.0d;
		private static final double ESTIMATED_SECRET_COST = 2.5d;
		private static final double ESTIMATED_SECRET_VALUE = ESTIMATED_SECRET_COST * SECRET_VALUE_FACTOR;
		private static Map<String, SecretValueCalculation> SECRET_DICTIONARY = new HashMap<>();

		static {
			SECRET_DICTIONARY.put("Potion of Polymorph", SecretUtil::potionOfPolymorph);
			SECRET_DICTIONARY.put("Explosive Runes", SecretUtil::explosiveRunes);
			SECRET_DICTIONARY.put("Mirror Entity", SecretUtil::mirrorEntity);
			SECRET_DICTIONARY.put("Frozen Clone", SecretUtil::frozenClone);
			SECRET_DICTIONARY.put("Spellbender", SecretUtil::spellbender);
			SECRET_DICTIONARY.put("Ice Barrier", SecretUtil::iceBarrier);
			SECRET_DICTIONARY.put("Ice Block", SecretUtil::iceBlock);
			SECRET_DICTIONARY.put("Vaporize", SecretUtil::vaporize);

			// TODO: Counterspell: <b>Secret:</b> When your opponent casts a spell, <b>Counter</b> it.
			// TODO: Mana Bind: <b>Secret:</b> When your opponent casts a spell, add a copy to your hand that costs (0).
		}

		static void estimateValues(State state, Player player) {
			state.BiasValue += player.getSecrets().size() * ESTIMATED_SECRET_VALUE;
		}

		static void calculateValues(State playerState, State opponentState, Player player, Player opponent) {
			for (Secret secret : player.getSecrets()) {
				if (SECRET_DICTIONARY.containsKey(secret.getName())) {
					SECRET_DICTIONARY.get(secret.getName()).mutate(playerState, opponentState, player, opponent, secret.getSourceCard());
				} else {
					LOGGER.warn("calculateValues: Unknown secret {}", secret.getName());
					playerState.BiasValue += secret.getSourceCard().getBaseManaCost() * SECRET_VALUE_FACTOR;
				}
			}
		}


		private static void potionOfPolymorph(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			int opponentMana = opponent.getMana();

			//punish playing early:
			playerState.BiasValue += StateUtility.LateReward(opponentMana, 5, 5.0d);

			//value is the difference between an average minion and the sheep:
			double sheepValue = MinionUtil.computeMinionValue(1, 1, 1);
			double averageMinionValue = MinionUtil.estimatedValueFromMana(opponentMana);
			double polymorphedValue = (sheepValue - averageMinionValue);
			opponentState.MinionValues += polymorphedValue;
		}

		private static void mirrorEntity(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			int mana = opponent.getMana();
			double minion = MinionUtil.estimatedValueFromMana(mana);
			playerState.BiasValue += StateUtility.LateReward(mana, 4, 5.0d);
			playerState.MinionValues += minion;
		}

		private static void iceBlock(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			//give punishment when at full hp, give reward if hp lessens

			final int MAX_HEALTH = 30;
			final int MIN_HEALTH = 1;
			double healthPercent = 1.0d - Utility.InverseLerp(playerState.HeroHealth, MIN_HEALTH, MAX_HEALTH);

			//punishment when at full hp:
			final double MIN_VALUE = -30.0d;
			//reward when at 1 hp:
			final double MAX_VALUE = 45.0d;

			double value = Utility.Lerp(MIN_VALUE, MAX_VALUE, healthPercent);
			playerState.BiasValue += value;
		}

		private static void iceBarrier(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			playerState.HeroArmor += 8;
		}

		private static void frozenClone(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			int mana = opponent.getMana();
			double minion = MinionUtil.estimatedValueFromMana(mana);
			//dont multiply by 2, because player still has to play the minions:
			playerState.BiasValue += minion * 1.75d + StateUtility.LateReward(mana, 4, 4.0d);
		}

		private static void spellbender(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			int myMana = player.getMana();
			double possibleAverageMinion = MinionUtil.estimatedValueFromMana(myMana);
			double myAverageMinion = playerState.getAverageMinionValue();
			//dont play if my minions are weaker than a "good" minion at that point in game, also punish when played early:
			playerState.BiasValue += (myAverageMinion - possibleAverageMinion) + StateUtility.LateReward(myMana, 4, 2.0d);
		}

		private static void vaporize(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			int opponentMana = opponent.getMana();

			//punish playing early:
			playerState.BiasValue += StateUtility.LateReward(opponentMana, 5, 5.0d);

			//estimate destroying an enemy minion:
			double avgMinionValue = MinionUtil.estimatedValueFromMana(opponentMana);
			opponentState.MinionValues -= avgMinionValue;
		}

		private static void explosiveRunes(State playerState, State opponentState, Player player, Player opponent, Card secret) {
			//doesnt matter if played early or late (early: deals damage to hero, later will most likely kill a minion)

			//multiply with a factor because either it kills a minion (higher value than just the damage dealt)
			//or/and it deals damage to the hero (also worth more than just reducing the hp)
			final double FACTOR = 2.0d;
			final int BASE_DAMAGE = 6;
			int currentSpellpower = 0;
			for (Minion minion : player.getMinions()) {
				currentSpellpower += minion.getAttributeValue(Attribute.SPELL_DAMAGE) + minion.getAttributeValue(Attribute.AURA_SPELL_DAMAGE);
			}
			currentSpellpower += player.getAttributeValue(Attribute.SPELL_DAMAGE) + player.getAttributeValue(Attribute.AURA_SPELL_DAMAGE);
			opponentState.BiasValue -= ((BASE_DAMAGE + currentSpellpower) * FACTOR);
		}

	}

	private static class SpellUtil {
		private static Map<String, SpellValueCalculation> SPELL_DICTIONARY = new HashMap<>();

		static {
			SPELL_DICTIONARY.put("The Coin", SpellUtil::TheCoin);
			SPELL_DICTIONARY.put("Maelstrom Portal", SpellUtil::MaelstromPortal);
			SPELL_DICTIONARY.put("Jade Lightning", SpellUtil::JadeLightning);
			SPELL_DICTIONARY.put("Lightning Bolt", SpellUtil::LightningBolt);
			SPELL_DICTIONARY.put("Lightning Storm", SpellUtil::LightningStorm);
			SPELL_DICTIONARY.put("Hex", SpellUtil::Hex);
		}

		static void calculateValues(GameContext context, State enemyState, Player player, Player opponent, GameAction task, Card spellCard, State playerState) {
			//give reward/punishment if spells cost less/more than usual:
			int modifiedCost = context.getLogic().getModifiedManaCost(player, spellCard);
			double diff = spellCard.getBaseManaCost() - modifiedCost;
			playerState.BiasValue += diff * 1.25d;


			if (SPELL_DICTIONARY.containsKey(spellCard.getName())) {
				SPELL_DICTIONARY.get(spellCard.getName()).mutate(context, playerState, enemyState, player, opponent, task, spellCard);
			}
		}


		//Gain 1 Mana Crystal this turn only.
		private static void TheCoin(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card) {
			int curMana = player.getMana();
			int newMana = curMana + 1;

			boolean enablesNewCards = false;

			for (Card card : player.getHand()) {
				//if the card can only be played after using the coin, then it is not bad:
				if (card.getBaseManaCost() > curMana && card.getBaseManaCost() <= newMana) {
					enablesNewCards = true;
					break;
				}
			}

			//if the coin does not enable to play new cards, give punishment.
			if (!enablesNewCards)
				playerState.BiasValue -= 100.0d;
		}

		//Lightning Storm: Deal 2-3 damage to all enemy minions. Overload: (2)
		private static void LightningStorm(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card) {
			//give punishment when having less than this enemies:
			final int NUM_ENEMY_TARGETS = 3;
			playerState.BiasValue += (opponentState.NumMinionsOnBoard - NUM_ENEMY_TARGETS) * 1.25d;
		}

		//Lightning Bolt: Deal 3 damage. Overload: (1)
		private static void LightningBolt(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card) {
			if (task.getTargetReference() != null && !task.getTargetReference().equals(EntityReference.NONE)) {
				//reward if the Card does NOT overkill an enemy:
				Actor target = (Actor) task.getTargets(context, player.getId()).get(0);
				if (target.getEntityType() == EntityType.MINION) {
					CardDamageReward(context, playerState, opponentState, player, opponent, task, Card, 3, 1.25d, target);
				}
			}
		}

		//Hex: Transform a minion into a 0/1 Frog with Taunt.
		private static void Hex(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card) {
			int myMana = player.getMana();
			playerState.BiasValue += StateUtility.LateReward(myMana, 3, 1.25d);
		}

		//Jade Lightning: Deal 4 damage. Summon a{1} {0} Jade Golem.@Deal 4 damage. Summon a Jade Golem.
		private static void JadeLightning(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card) {
			if (task.getTargetReference() != null && !task.getTargetReference().equals(EntityReference.NONE)) {
				//reward if the Card does NOT overkill an enemy:
				Actor target = (Actor) task.getTargets(context, player.getId()).get(0);
				if (target.getEntityType() == EntityType.MINION) {
					CardDamageReward(context, playerState, opponentState, player, opponent, task, Card, 4, 1.25d, target);
				}
			}
		}

		//Maelstrom Portal: Deal 1 damage to all enemy minions. Summon a random 1-Cost minion.
		private static void MaelstromPortal(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card) {
			final int NUM_TARGET_MINIONS = 3;

			//negative if below NUM_TARGET_MINIONS, neutral at NUM_TARGET_MINIONS, then positive:
			int diff = opponentState.NumMinionsOnBoard - NUM_TARGET_MINIONS;
			playerState.BiasValue += diff * 1.25d;
		}


		private static void CardDamageReward(GameContext context, State playerState, State opponentState, Player player, Player opponent, GameAction task, Card Card, int damage, double reward, Actor target) {
			damage = context.getLogic().applySpellpower(player, Card, damage);

			if (target.hasAttribute(Attribute.DIVINE_SHIELD) && damage > 1) {
				//punishment for wasting damage for divine shield
				playerState.BiasValue -= 5.0d;
				return;
			}

			int targetHealth = target.getHp();

			int diff = targetHealth - damage;

			double finalReward = diff * reward;

			//if the Card kills a minion on point, give additional bonus:
			if (diff == 0)
				finalReward += reward;

			playerState.BiasValue += finalReward;
		}
	}

	private static class MinionUtil {
		static final double DIVINE_SHIELD_VALUE = 2.0d;
		static final int MAX_MANA_COST = 10;
		static final int NUM_MANA_TIERS = MAX_MANA_COST + 1;
		private static double[] ESTIMATED_FROM_MANA;
		private static double[] ESTIMATED_BELOW_MANA;

		static {
			ESTIMATED_FROM_MANA = new double[NUM_MANA_TIERS];
			ESTIMATED_BELOW_MANA = new double[NUM_MANA_TIERS];

			for (int manaCost = 0; manaCost < NUM_MANA_TIERS; manaCost++) {
				ESTIMATED_FROM_MANA[manaCost] = ((double) manaCost + 1.0d) * 2.0d;
			}

			for (int manaCost = 0; manaCost < NUM_MANA_TIERS; manaCost++) {
				double value = 0.0d;

				int count = manaCost + 1;

				for (int i = 0; i < count; i++)
					value += estimatedValueFromMana(manaCost);

				ESTIMATED_BELOW_MANA[manaCost] = value / count;
			}
		}


		static double computeMinionValues(Player player) {
			double value = 0;
			for (int i = 0; i < player.getMinions().size(); i++) {
				value += computeMinionValue(player.getMinions().get(i));
			}
			return value;
		}

		private static double computeMinionValueX(
				int health,
				int attackDmg,
				int attacksPerTurn,
				boolean hasTaunt,
				boolean poisonous,
				boolean hasDeathRattle,
				boolean hasInspire,
				boolean hasDivineShield,
				boolean hasLifeSteal,
				boolean hasCharge,
				boolean hasStealth,
				boolean hasBattleCry,
				boolean isFrozen) {
			double value = 0.0d;

			//if its frozen, it cant attack;
			if (!isFrozen) {
				int numBonusAttacks = Math.max(attacksPerTurn - 1, 0);
				value += (health + attackDmg + attackDmg * numBonusAttacks);
			}

			if (hasTaunt)
				value += 2;

			if (poisonous)
				value += 2;

			if (hasDeathRattle)
				value += 2;

			if (hasInspire)
				value += 2;

			if (hasDivineShield)
				value += DIVINE_SHIELD_VALUE;

			if (hasLifeSteal)
				value += 2;

			if (hasCharge)
				value += 1;

			if (hasStealth)
				value += 1;

			if (hasBattleCry)
				value += 1;

			return value;
		}

		private static double computeMinionValue(Minion minion) {
			return computeMinionValueX(
					minion.getHp(),
					minion.getAttack(),
					minion.getAttributeValue(Attribute.NUMBER_OF_ATTACKS) + minion.getAttributeValue(Attribute.EXTRA_ATTACKS),
					minion.hasAttribute(Attribute.TAUNT) || minion.hasAttribute(Attribute.AURA_TAUNT),
					minion.hasAttribute(Attribute.POISONOUS) || minion.hasAttribute(Attribute.AURA_POISONOUS),
					minion.hasAttribute(Attribute.DEATHRATTLES) || (minion.getDeathrattles() != null && minion.getDeathrattles().size() > 0),
					minion.getSourceCard() != null && minion.getSourceCard().getDesc().getTrigger() != null && minion.getSourceCard().getDesc().getTrigger().eventTrigger.getDescClass() == InspireTrigger.class,
					minion.hasAttribute(Attribute.DIVINE_SHIELD),
					minion.hasAttribute(Attribute.LIFESTEAL) || minion.hasAttribute(Attribute.AURA_LIFESTEAL),
					minion.hasAttribute(Attribute.CHARGE) || minion.hasAttribute(Attribute.AURA_CHARGE),
					minion.hasAttribute(Attribute.STEALTH) || minion.hasAttribute(Attribute.AURA_STEALTH),
					minion.getSourceCard().hasBattlecry(),
					minion.hasAttribute(Attribute.FROZEN)
			);
		}

		static double computeMinionValue(int hp, int attack, int attacksPerTurn) {
			return computeMinionValueX(hp, attack, attacksPerTurn, false, false, false, false, false, false, false, false, false, false);
		}

		static double estimatedValueFromMana(int manaCost) {
			if (manaCost > MAX_MANA_COST) {
				manaCost = MAX_MANA_COST;
			}

			return ESTIMATED_FROM_MANA[manaCost];
		}
	}

	private static class State {
		int HeroHealth;
		int HeroArmor;

		int WeaponDamage;
		int WeaponDurability;

		int TurnNumber;

		int NumDeckCards;
		int NumHandCards;
		int NumMinionsOnBoard;

		int Fatigue;

		double BiasValue;
		double MinionValues;

		static State fromSimulatedGame(GameContext newState, Player me, @Nullable GameAction task) {
			State s = new State();

			s.HeroHealth = me.getHero().getHp();
			s.HeroArmor = me.getHero().getArmor();
			s.TurnNumber = newState.getTurn();
			s.NumDeckCards = me.getDeck().getCount();
			s.NumHandCards = me.getHand().getCount();
			s.NumMinionsOnBoard = me.getMinions().size();
			s.Fatigue = me.getAttributeValue(Attribute.FATIGUE);
			s.MinionValues = MinionUtil.computeMinionValues(me);

			if (me.getHero().getWeapon() != null) {
				s.WeaponDurability = me.getHero().getWeapon().getDurability();
				s.WeaponDamage = me.getHero().getWeapon().getWeaponDamage();
			}

			//this case is met, if the player uses a card that temporarily boosts attack:
			if (me.getHero().getAttack() > s.WeaponDamage) {
				s.WeaponDamage = me.getHero().getAttack();

				//assume that the player can at least attack once:
				if (s.WeaponDurability == 0) {
					s.WeaponDurability = 1;
				}
			}

			//aka, can't attack:
			if (!me.getHero().canAttackThisTurn()) {
				s.WeaponDamage = 0;
			}

			if (task instanceof PlayMinionCardAction) {
				//give reward/punishment of minions cost less/more than usual:
				Card card = (Card) task.getSource(newState);
				double diff = card.getBaseManaCost() - newState.getLogic().getModifiedManaCost(me, card);
				s.BiasValue += diff * 1.5d;
			}

			return s;
		}

		static boolean correctBuggySimulation(State myState, State enemyState, GameContext parent, GameAction task) {
			// TODO: I'm not sure if this really applies to Spellsource, since there aren't issues equipping weapons.
			return true;
		}

		double getAverageMinionValue() {
			if (NumMinionsOnBoard <= 0) {
				return 0;
			}

			return (double) MinionValues / (double) NumMinionsOnBoard;
		}
	}

	private static class SimTree {
		private StateAnalyzer analyzer;
		private GameContext rootGame;
		private Map<GameAction, TaskNode> nodesToEstimate = new HashMap<>();
		private List<TaskNode> explorableNodes = new ArrayList<>();
		private List<TaskNode> sortedNodes = new ArrayList<>();

		void initTree(StateAnalyzer analyzer, GameContext root, Player player, List<GameAction> options) {
			sortedNodes.clear();
			explorableNodes.clear();
			nodesToEstimate.clear();
			this.analyzer = analyzer;
			this.rootGame = root;

			for (int i = 0; i < options.size(); i++) {
				GameAction task = options.get(i);
				TaskNode node = new TaskNode(analyzer, task, 0.0d);
				// End turn is pretty straight forward, should not really be looked at later in the simulations, just simulate once and keep the value:
				if (task.getActionType() == ActionType.END_TURN) {
					SimResult sim = StateUtility.getSimulatedGame(root, task, analyzer);
				} else {
					explorableNodes.add(node);
					sortedNodes.add(node);
				}

				nodesToEstimate.put(task, node);
			}
		}

		void simulateEpisode(XORShiftRandom random, int curEpisode, boolean shouldExploit) {
			TaskNode nodeToExplore = null;

			// Exploiting
			if (shouldExploit) {
				// EDIT: Descending sort
				sortedNodes.sort(Comparator.comparingDouble(TaskNode::getTotalValue).reversed());
				// exploit only 50% best nodes:
				int count = (int) (sortedNodes.size() * 0.5 + 0.5);
				nodeToExplore = Utility.getUniformRandom(sortedNodes, random, count);
			} else {
				// Explore
				nodeToExplore = explorableNodes.get(curEpisode % explorableNodes.size());
			}

			if (nodeToExplore == null) {
				LOGGER.warn("simulateEpisode: Should not be possible (nodeToExplore is null)");
				return;
			}

			GameAction task = nodeToExplore.task;
			SimResult result = StateUtility.getSimulatedGame(rootGame, task, analyzer);
			nodeToExplore.explore(result, random);
		}

		TaskNode getBestNode() {
			List<TaskNode> nodes = new ArrayList<>(nodesToEstimate.values());
			nodes.sort(Comparator.comparingDouble(TaskNode::getAverage).reversed());
			return nodes.get(0);
		}
	}

	private static class StateWeights {
		private final double[] weights = new double[WeightType.values().length];

		public enum WeightType {
			EmptyField,
			HealthFactor,
			DeckFactor,
			HandFactor,
			MinionFactor,
			/**
			 * Used for "stuff" that doesn't fit to the other categories e.g. unknown secrets
			 */
			BiasFactor
		}

		public StateWeights() {
		}

		StateWeights(double defaultValues) {
			Arrays.fill(weights, defaultValues);
		}

		StateWeights(double... values) {
			System.arraycopy(values, 0, weights, 0, values.length);
		}

		static StateWeights getHeroBased(String playerHeroClass, String opponentHeroClass) {
			switch (playerHeroClass) {
				case "RED":
					return new StateWeights(6.083261d, 3.697277d, 3.603937d, 9.533023d, 8.534495d, 8.220309d);
				case "SILVER":
					return new StateWeights(3.168855d, 5.913401d, 3.937068d, 9.007857d, 8.526226d, 5.678857d);
				case "BLUE":
					return new StateWeights(3.133729d, 9.927018d, 2.963968d, 6.498888d, 4.516192d, 4.645887d);
				case "BROWN":
					return new StateWeights(1.995913d, 4.501529d, 1.888616d, 1.096681d, 3.516505d, 1.0d);
				case "VIOLET":
					return new StateWeights(6.338876d, 8.568761d, 1.863452d, 3.182807d, 4.967152d, 1.0d);
				default:
					return getDefault();
			}
		}

		public static StateWeights getDefault() {
			StateWeights weights = new StateWeights(1d);
			weights.setWeight(WeightType.HealthFactor, 8.7d);
			weights.setWeight(WeightType.BiasFactor, 4d);
			return weights;
		}

		void setWeight(WeightType weightType, double value) {
			weights[weightType.ordinal()] = value;
		}

		double getWeight(WeightType weightType) {
			return weights[weightType.ordinal()];
		}
	}

	private static class Utility {

		static <E> E getUniformRandom(List<E> sortedNodes, XORShiftRandom random, int count) {
			return sortedNodes.get(random.nextInt(count));
		}

		static double InverseLerp(double value, double min, double max) {
			return (value - min) / (max - min);
		}

		static double Lerp(double a, double b, double t) {
			return (1.0d - t) * a + t * b;
		}
	}

	private static class StateUtility {

		static List<SimResult> getSimulatedBestTasks(int numTasks, GameContext context, Player player, List<GameAction> options, StateAnalyzer analyzer) {
			return getSortedBestTasks(numTasks, getSimulatedGames(context, player, options, analyzer));
		}

		private static List<SimResult> getSortedBestTasks(int numTasks, List<SimResult> simulatedGames) {
			return simulatedGames
					.stream()
					.sorted(Comparator.comparingDouble(SimResult::getStateValue).reversed())
					.limit(numTasks)
					.collect(Collectors.toList());
		}

		private static List<SimResult> getSimulatedGames(GameContext context, Player player, List<GameAction> options, StateAnalyzer analyzer) {
			return options
					.stream()
					// Parallelize the expansion of game states here
					.parallel()
					.map(option -> getSimulatedGame(context, option, analyzer)).collect(toList());
		}

		static SimResult getSimulatedGame(GameContext parent, GameAction task, StateAnalyzer analyzer) {
			GameContext simulatedState = simulate(parent, new GameAction[]{task}).getOrDefault(task, null);
			double stateValue = getStateValue(parent, simulatedState, task, analyzer);
			return new SimResult(simulatedState, task, stateValue);
		}

		private static double getStateValue(GameContext parent, GameContext child, GameAction task, StateAnalyzer analyzer) {
			double valueFactor = 1;
			State myState = null;
			State enemyState = null;
			Player player = null;
			Player opponent = null;

			// it's a buggy state, mostly related to equipping/using weapons on heroes etc.
			// in this case use the old state and estimate the new state manually:
			if (child == null) {
				throw new UnsupportedOperationException("no bugs");
				/*
				LOGGER.warn("getStateValue: Received null child");

				player = parent.getActivePlayer();
				opponent = parent.getOpponent(player);
				myState = State.fromSimulatedGame(parent, player, task);
				enemyState = State.fromSimulatedGame(parent, opponent, null);
				if (State.correctBuggySimulation(myState, enemyState, parent, task)) {
					valueFactor = 1.25;
				}
				*/
			} else {
				player = child.getActivePlayer();
				opponent = child.getOpponent(player);

				// Happens sometimes even with/without TURN_END, idk
				if (!analyzer.isMyPlayer(player)) {
					player = child.getOpponent(child.getActivePlayer());
					opponent = child.getActivePlayer();
				}

				myState = State.fromSimulatedGame(child, player, task);
				enemyState = State.fromSimulatedGame(child, opponent, null);
			}

			GameContext context = child == null ? parent : child;
			return analyzer.getStateValue(context, myState, enemyState, player, opponent, task) * valueFactor;
		}

		private static @NotNull
		Map<GameAction, GameContext> simulate(GameContext parent, GameAction[] gameActions) {
			Map<GameAction, GameContext> result = new HashMap<>();
			for (GameAction gameAction : gameActions) {
				try {
					GameContext context = parent.clone();
					context.setLoggingLevel(Level.OFF);
					/*
					if (gameAction.getActionType() == ActionType.BATTLECRY || gameAction.getActionType() == ActionType.DISCOVER) {
						result.put(gameAction, context);
						continue;
					}
					// Guard for recursive calls
					context.setBehaviour(context.getActivePlayerId(), new RequestActionFunction((context1, player, validActions) -> {
						return validActions.get(0);
					}));
					*/
					context.performAction(context.getActivePlayerId(), gameAction);
					result.put(gameAction, context);
				} catch (Throwable any) {
					LOGGER.error("simulate {} {}: Could not simulate due to exception", parent, gameAction, any);
					result.put(gameAction, null);
				}
			}
			return result;
		}

		static double LateReward(int mana, int neutralMana, double reward) {
			return reward * (mana - neutralMana);
		}
	}

	private static class Const {
		static final double MAX_SIMULATION_TIME = 10d;
		static final double DECREASE_SIMULATION_TIME = MAX_SIMULATION_TIME * 0.4;
		static final double MAX_TURN_TIME = 15d;
	}

	private static class TaskNode {
		private final StateAnalyzer analyzer;
		GameAction task;
		double totalValue;
		int visits;

		double getTotalValue() {
			return totalValue;
		}

		TaskNode(StateAnalyzer analyzer, GameAction task, double totalValue) {
			this.analyzer = analyzer;
			this.task = task;
			this.totalValue = totalValue;
		}

		void explore(SimResult simResult, XORShiftRandom random) {
			GameContext game = simResult.getSimulatedState();
			List<GameAction> options = game.getValidActions();

			if (options.isEmpty() || game.updateAndGetGameOver()) {
				addValue(simResult.getStateValue());
				return;
			}

			GameAction task = options.get(random.nextInt(options.size()));
			SimResult childState = StateUtility.getSimulatedGame(game, task, analyzer);
			if (childState.task.getActionType() != ActionType.END_TURN
					&& !childState.simulatedState.updateAndGetGameOver()) {
				explore(childState, random);
			} else {
				addValue(childState.getStateValue());
			}
		}

		private void addValue(double stateValue) {
			totalValue += stateValue;
			visits++;
		}

		double getAverage() {
			if (visits == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			return totalValue / visits;
		}
	}

	private static class SimResult {

		private final GameContext simulatedState;
		private final GameAction task;
		private final double stateValue;


		SimResult(GameContext simulatedState, GameAction task, double stateValue) {
			this.simulatedState = simulatedState;
			this.task = task;
			this.stateValue = stateValue;
		}

		GameContext getSimulatedState() {
			return simulatedState;
		}

		public GameAction getTask() {
			return task;
		}

		double getStateValue() {
			return stateValue;
		}

		boolean isBuggy() {
			return simulatedState == null;
		}
	}
}
