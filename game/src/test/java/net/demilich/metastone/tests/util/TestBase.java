package net.demilich.metastone.tests.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.collections4.Bag;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import org.testng.annotations.BeforeMethod;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

public class TestBase {
	protected static Card playChooseOneCard(GameContext context, Player player, String baseCardId, String chosenCardId) {
		Card baseCard = receiveCard(context, player, baseCardId);
		int cost = CardCatalogue.getCardById(chosenCardId).getManaCost(context, player);
		player.setMana(cost);
		context.getLogic().performGameAction(player.getId(), context.getValidActions()
				.stream()
				.filter(ga -> ga instanceof PlayChooseOneCardAction)
				.map(ga -> (PlayChooseOneCardAction) ga)
				.filter(coca -> coca.getChoiceCardId().equals(chosenCardId))
				.findFirst()
				.orElseThrow(AssertionError::new));
		return baseCard;
	}

	protected static <T extends Card> T putOnTopOfDeck(GameContext context, Player player, String cardId) {
		final T card = (T) CardCatalogue.getCardById(cardId);
		context.getLogic().putOnTopOfDeck(player, card);
		return card;
	}

	public static class OverrideHandle<T> {
		private T object;
		private AtomicBoolean stopped = new AtomicBoolean(false);

		public void set(T object) {
			this.object = object;
		}

		public T get() {
			return object;
		}

		public void stop() {
			stopped.set(true);
		}
	}

	protected static void overrideMissilesTrigger(GameContext context, Entity source, Entity target) {
		Enchantment enchantment = (Enchantment) context.getTriggersAssociatedWith(source.getReference()).get(0);
		SpellDesc spell = enchantment.getSpell().clone();
		spell.remove(SpellArg.RANDOM_TARGET);
		spell.setTarget(target.getReference());
		enchantment.setSpell(spell);
	}

	protected static OverrideHandle<Card> overrideRandomCard(GameContext context, String cardId) {
		OverrideHandle<Card> handle = new OverrideHandle<>();
		MockingDetails mockingDetails = Mockito.mockingDetails(context.getLogic());
		GameLogic spyLogic = mockingDetails.isSpy() ? context.getLogic() : Mockito.spy(context.getLogic());
		context.setLogic(spyLogic);
		Answer answer = invocation -> {
			handle.set(CardCatalogue.getCardById(cardId));
			if (!handle.stopped.get()) {
				return handle.get();
			} else {
				return invocation.callRealMethod();
			}
		};

		Mockito.doAnswer(answer).when(spyLogic).getRandom(Mockito.anyList());
		Mockito.doAnswer(answer).when(spyLogic).removeRandom(Mockito.any(Bag.class));
		Mockito.doAnswer(answer).when(spyLogic).removeRandom(Mockito.anyList());
		return handle;
	}

	protected static OverrideHandle<Card> overrideDiscover(Player player, Function<List<DiscoverAction>, GameAction> discovery) {
		Behaviour overriden = Mockito.spy(player.getBehaviour());
		player.setBehaviour(overriden);
		OverrideHandle<Card> handle = new OverrideHandle<>();
		Mockito.doAnswer(invocation -> {
			List<GameAction> actions = invocation.getArgument(2);
			if (actions.stream().allMatch(a -> a instanceof DiscoverAction)
					&& !handle.stopped.get()) {
				List<DiscoverAction> discoveries = new ArrayList<>();
				actions.forEach(a -> discoveries.add((DiscoverAction) a));
				DiscoverAction result = (DiscoverAction) discovery.apply(discoveries);
				handle.set(result.getCard());
				return result;
			} else {
				return invocation.callRealMethod();
			}
		}).when(overriden).requestAction(any(), any(), anyList());
		return handle;
	}

	protected static OverrideHandle<EntityReference> overrideBattlecry(Player player, Function<List<BattlecryAction>, GameAction> battlecry) {
		Behaviour overriden = Mockito.spy(player.getBehaviour());
		player.setBehaviour(overriden);
		OverrideHandle<EntityReference> handle = new OverrideHandle<>();
		Mockito.doAnswer(invocation -> {
			List<GameAction> actions = invocation.getArgument(2);
			if (actions.stream().allMatch(a -> a instanceof BattlecryAction)
					&& !handle.stopped.get()) {
				List<BattlecryAction> battlecryActions = new ArrayList<>();

				if (handle.get() != null
						&& battlecryActions.stream().anyMatch(ba -> ba.getTargetReference().equals(handle.get()))) {
					return battlecryActions.stream().filter(ba -> ba.getTargetReference().equals(handle.get())).findFirst().orElseThrow(AssertionError::new);
				}

				actions.forEach(a -> battlecryActions.add((BattlecryAction) a));
				BattlecryAction result = (BattlecryAction) battlecry.apply(battlecryActions);
				handle.set(result.getTargetReference());
				return result;
			} else {
				return invocation.callRealMethod();
			}
		}).when(overriden).requestAction(any(), any(), anyList());
		return handle;
	}

	protected static void overrideDiscover(GameContext context, Player player, String cardId) {
		OverrideHandle<Card> handle = overrideRandomCard(context, cardId);
		overrideDiscover(player, discovers -> {
			DiscoverAction action = discovers.stream().filter(da -> da.getCard().getCardId().equals(cardId)).findFirst().orElseThrow(AssertionError::new);
			handle.stop();
			return action;
		});
	}

	public static Card receiveCard(GameContext context, Player player, String cardId) {
		Card card = CardCatalogue.getCardById(cardId);
		context.getLogic().receiveCard(player.getId(), card);
		return card;
	}

	public static <T extends Card> T shuffleToDeck(GameContext context, Player player, String cardId) {
		T card = (T) CardCatalogue.getCardById(cardId);
		context.getLogic().shuffleToDeck(player, card);
		return card;
	}

	public static int costOf(GameContext context, Player player, Card deckCard) {
		return context.getLogic().getModifiedManaCost(player, deckCard);
	}

	@FunctionalInterface
	public interface GymConsumer {
		void run(GameContext context, Player player, Player opponent);

		default GymConsumer andThen(GymConsumer after) {
			Objects.requireNonNull(after);
			return (c, p, o) -> {
				run(c, p, o);
				after.run(c, p, o);
			};
		}
	}

	public static class GymFactory {
		GymConsumer first;
		GymConsumer after = ((context, player, opponent) -> {
		});

		public void run(GymConsumer consumer) {
			runGym(first.andThen(consumer).andThen(after));
		}
	}

	public static GymFactory getGymFactory(GymConsumer initializer) {
		GymFactory factory = new GymFactory();
		factory.first = initializer;
		return factory;
	}

	public static GymFactory getGymFactory(GymConsumer initializer, GymConsumer after) {
		GymFactory factory = getGymFactory(initializer);
		factory.after = after;
		return factory;
	}

	public static void runGym(GymConsumer consumer, HeroClass heroClass1, HeroClass heroClass2) {
		GameContext context = createContext(heroClass1, heroClass2);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());
		clearZone(context, player.getGraveyard());
		clearZone(context, opponent.getGraveyard());

		consumer.run(context, player, opponent);
	}

	public static void runGym(GymConsumer consumer) {
		runGym(consumer, HeroClass.BLUE, HeroClass.BLUE);
	}

	public static void clearHand(GameContext context, Player player) {
		for (int i = player.getHand().getCount() - 1; i >= 0; i--) {
			context.getLogic().removeCard(player.getHand().get(i));
		}
	}

	public static void clearZone(GameContext context, EntityZone<? extends Entity> zone) {
		if (zone.getZone() == Zones.GRAVEYARD) {
			for (int i = zone.size() - 1; i >= 0; i--) {
				zone.get(i).moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			}

			return;
		}

		for (int i = zone.size() - 1; i >= 0; i--) {
			Entity entity = zone.get(i);
			if (Card.class.isAssignableFrom(entity.getClass())) {
				context.getLogic().removeCard((Card) entity);
			} else if (Actor.class.isAssignableFrom(entity.getClass())) {
				context.getLogic().destroy((Actor) entity);
			} else {
				entity.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			}
		}
	}

	public static <A, B, C> Stream<C> zip(Stream<? extends A> a,
	                                      Stream<? extends B> b,
	                                      BiFunction<? super A, ? super B, ? extends C> zipper) {
		Objects.requireNonNull(zipper);
		Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
		Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();

		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
				~(Spliterator.DISTINCT | Spliterator.SORTED);

		long zipSize = ((characteristics & Spliterator.SIZED) != 0)
				? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
				: -1;

		Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
		Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
		Iterator<C> cIterator = new Iterator<C>() {
			@Override
			public boolean hasNext() {
				return aIterator.hasNext() && bIterator.hasNext();
			}

			@Override
			public C next() {
				return zipper.apply(aIterator.next(), bIterator.next());
			}
		};

		Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
		return (a.isParallel() || b.isParallel())
				? StreamSupport.stream(split, true)
				: StreamSupport.stream(split, false);
	}

	public static class TestBehaviour extends AbstractBehaviour {

		private EntityReference targetPreference;

		@Override
		public String getName() {
			return "Null Behaviour";
		}

		public EntityReference getTargetPreference() {
			return targetPreference;
		}

		@Override
		public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
			return new ArrayList<Card>();
		}

		@Override
		public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
			if (targetPreference != null) {
				for (GameAction action : validActions) {
					if (action.getTargetReference().equals(targetPreference)) {
						return action;
					}
				}
			}

			return validActions.get(0);
		}

		public void setTargetPreference(EntityReference targetPreference) {
			this.targetPreference = targetPreference;
		}

	}

	@BeforeMethod
	public void loadCards() throws Exception {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
		CardCatalogue.loadCardsFromPackage();
	}

	protected static void attack(GameContext context, Player player, Entity attacker, Entity target) {
		PhysicalAttackAction physicalAttackAction = new PhysicalAttackAction(attacker.getReference());
		physicalAttackAction.setTarget(target);
		context.getLogic().performGameAction(player.getId(), physicalAttackAction);
	}

	protected static DebugContext createContext(HeroClass hero1, HeroClass hero2) {
		return createContext(hero1, hero2, true);
	}

	protected static DebugContext createContext(HeroClass hero1, HeroClass hero2, boolean shouldInit) {
		DeckFormat deckFormat = new DeckFormat().withCardSets(
				CardSet.BASIC,
				CardSet.CLASSIC,
				CardSet.BLACKROCK_MOUNTAIN,
				CardSet.GOBLINS_VS_GNOMES,
				CardSet.LEAGUE_OF_EXPLORERS,
				CardSet.MEAN_STREETS_OF_GADGETZAN,
				CardSet.NAXXRAMAS,
				CardSet.ONE_NIGHT_IN_KARAZHAN,
				CardSet.PROMO,
				CardSet.REWARD,
				CardSet.THE_GRAND_TOURNAMENT,
				CardSet.JOURNEY_TO_UNGORO,
				CardSet.KNIGHTS_OF_THE_FROZEN_THRONE,
				CardSet.THE_OLD_GODS,
				CardSet.KOBOLDS_AND_CATACOMBS,
				CardSet.CUSTOM
		);

		PlayerConfig player1Config = new PlayerConfig(DeckFactory.getRandomDeck(hero1, new DeckFormat().withCardSets(
				CardSet.BASIC,
				CardSet.CLASSIC)), new TestBehaviour());
		player1Config.setName("Player 1");
		player1Config.setHeroCard(getHeroCardForClass(hero1));
		Player player1 = new Player(player1Config);

		PlayerConfig player2Config = new PlayerConfig(DeckFactory.getRandomDeck(hero2, new DeckFormat().withCardSets(
				CardSet.BASIC,
				CardSet.CLASSIC)), new TestBehaviour());
		player2Config.setName("Player 2");
		player2Config.setHeroCard(getHeroCardForClass(hero2));
		Player player2 = new Player(player2Config);

		GameLogic logic = new GameLogic();
		DebugContext context = new DebugContext(player1, player2, logic, deckFormat);
		logic.setContext(context);
		if (shouldInit) {
			context.init();
		}
		return context;
	}

	protected static Entity find(GameContext context, String cardId) {
		for (Player player : context.getPlayers()) {
			for (Minion minion : player.getMinions()) {
				if (minion.getSourceCard().getCardId().equals(cardId)) {
					return minion;
				}
			}
		}
		return null;
	}

	protected static HeroCard getHeroCardForClass(HeroClass heroClass) {
		for (Card card : CardCatalogue.getHeroes()) {
			HeroCard heroCard = (HeroCard) card;
			if (heroCard.getHeroClass() == heroClass) {
				return heroCard;
			}
		}
		return null;
	}

	protected static Actor getSingleMinion(List<Minion> minions) {
		for (Actor minion : minions) {
			if (minion == null) {
				continue;
			}
			return minion;
		}
		return null;
	}

	protected static Minion getSummonedMinion(List<Minion> minions) {
		List<Minion> minionList = new ArrayList<>(minions);
		Collections.sort(minionList, (m1, m2) -> Integer.compare(m1.getId(), m2.getId()));
		return minionList.get(minionList.size() - 1);
	}

	protected static void playCard(GameContext context, Player player, String cardId) {
		playCard(context, player, CardCatalogue.getCardById(cardId));
	}

	protected static void playCard(GameContext context, Player player, Card card) {
		if (card.getZone() != Zones.HAND) {
			context.getLogic().receiveCard(player.getId(), card);
		}
		context.getLogic().performGameAction(player.getId(), card.play());
	}

	protected static void playCardWithTarget(GameContext context, Player player, String cardId, Entity target) {
		playCardWithTarget(context, player, CardCatalogue.getCardById(cardId), target);
	}

	protected static void playCardWithTarget(GameContext context, Player player, Card card, Entity target) {
		if (card.getZone() != Zones.HAND) {
			context.getLogic().receiveCard(player.getId(), card);
		}
		GameAction action = card.play();
		action.setTarget(target);
		context.getLogic().performGameAction(player.getId(), action);
	}

	protected static Minion playMinionCard(GameContext context, Player player, String minionCardId) {
		return playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById(minionCardId));
	}

	protected static Minion playMinionCard(GameContext context, Player player, MinionCard minionCard) {
		if (minionCard.getZone() != Zones.HAND) {
			context.getLogic().receiveCard(player.getId(), minionCard);
		}

		context.getLogic().performGameAction(player.getId(), minionCard.play());
		return getSummonedMinion(player.getMinions());
	}

	protected static void target(Player player, Entity target) {
		TestBehaviour testBehaviour = (TestBehaviour) player.getBehaviour();
		testBehaviour.setTargetPreference(target != null ? target.getReference() : null);
	}

}
