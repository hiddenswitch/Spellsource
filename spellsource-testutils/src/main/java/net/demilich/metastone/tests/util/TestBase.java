package net.demilich.metastone.tests.util;

import com.google.common.base.Throwables;
import com.google.common.collect.Multiset;
import com.hiddenswitch.spellsource.cards.test.TestCardResources;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import com.hiddenswitch.spellsource.testutils.RandomDeck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentMatchers;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

public class TestBase {
	static {
	}

	protected static Card playChooseOneCard(GameContext context, Player player, String baseCardId, String chosenCardId) {
		return playChooseOneCard(context, player, baseCardId, chosenCardId, null);
	}

	protected static Card playChooseOneCard(GameContext context, Player player, String baseCardId, String chosenCardId, Entity target) {
		Card baseCard = receiveCard(context, player, baseCardId);
		int cost = context.getCardCatalogue().getCardById(chosenCardId).getManaCost(context, player);
		player.setMana(cost);
		GameAction action = context.getValidActions()
				.stream()
				.filter(ga -> ga instanceof PlayChooseOneCardAction)
				.map(ga -> (PlayChooseOneCardAction) ga)
				.filter(coca -> coca.getChoiceCardId().equals(chosenCardId))
				.findFirst()
				.orElseThrow(AssertionError::new);
		if (target != null) {
			action.setTarget(target);
		}
		context.performAction(player.getId(), action);
		return baseCard;
	}

	protected static <T extends Card> T putOnTopOfDeck(GameContext context, Player player, String cardId) {
		@SuppressWarnings("unchecked") final T card = (T) context.getCardCatalogue().getCardById(cardId);
		context.getLogic().insertIntoDeck(player, card, player.getDeck().size());
		return card;
	}

	protected static void destroy(GameContext context, Actor target) {
		target.getAttributes().put(Attribute.DESTROYED, true);
		context.getLogic().endOfSequence();
	}

	protected static void assertAdapted(String name, Minion minion, GameContext context) {
		if (name.equals("Crackling Shield")) {
			assertTrue(minion.hasAttribute(Attribute.DIVINE_SHIELD));
		} else if (name.equals("Flaming Claws")) {
			assertEquals(minion.getAttack(), minion.getBaseAttack() + 3);
		} else if (name.equals("Lightning Speed")) {
			assertTrue(minion.hasAttribute(Attribute.WINDFURY));
		} else if (name.equals("Liquid Membrane")) {
			assertTrue(minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		} else if (name.equals("Living Spores")) {
			assertEquals(context.getLogic().getAftermaths(minion).count(), 1L);
		} else if (name.equals("Massive")) {
			assertTrue(minion.hasAttribute(Attribute.TAUNT));
		} else if (name.equals("Poison Spit")) {
			assertTrue(minion.hasAttribute(Attribute.POISONOUS));
		} else if (name.equals("Rocky Carapace")) {
			assertEquals(minion.getHp(), minion.getBaseHp() + 3);
		} else if (name.equals("Shrouding Mist")) {
			assertTrue(minion.hasAttribute(Attribute.STEALTH));
		} else if (name.equals("Volcanic Might")) {
			assertEquals(minion.getHp(), minion.getBaseHp() + 1);
			assertEquals(minion.getAttack(), minion.getBaseAttack() + 1);
		}
	}

	protected static void assertNotAdapted(String name, Minion minion, GameContext context) {
		try {
			assertAdapted(name, minion, context);
		} catch (AssertionError ex) {
			return;
		}
		throw new AssertionError("Adapted");
	}

	protected static void overrideMissilesTrigger(GameContext context, Entity source, Entity target) {
		Enchantment enchantment = (Enchantment) context.getLogic().getActiveTriggers(source.getReference()).get(0);
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
			handle.set(context.getCardCatalogue().getCardById(cardId));
			if (!handle.stopped.get()) {
				return handle.get();
			} else {
				return invocation.callRealMethod();
			}
		};

		Mockito.doAnswer(answer).when(spyLogic).getRandom(Mockito.anyList());
		Mockito.doAnswer(answer).when(spyLogic).removeRandom(ArgumentMatchers.<Multiset<?>>any());
		Mockito.doAnswer(answer).when(spyLogic).removeRandom(Mockito.anyList());
		return handle;
	}

	protected static OverrideHandle<Card> overrideDiscover(GameContext context, Player player, Function<List<DiscoverAction>, GameAction> discovery) {
		Behaviour overriden = Mockito.spy(context.getBehaviours().get(player.getId()));
		context.setBehaviour(player.getId(), overriden);
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
		}).when(overriden).requestAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyList());
		return handle;
	}

	protected static OverrideHandle<EntityReference> overrideBattlecry(GameContext context, Player player, Function<List<OpenerAction>, GameAction> battlecry) {
		Behaviour overriden = Mockito.spy(context.getBehaviours().get(player.getId()));
		context.setBehaviour(player.getId(), overriden);
		OverrideHandle<EntityReference> handle = new OverrideHandle<>();
		Mockito.doAnswer(invocation -> {
			List<GameAction> actions = invocation.getArgument(2);
			if (actions.stream().allMatch(a -> a instanceof OpenerAction)
					&& !handle.stopped.get()) {
				List<OpenerAction> openerActions = new ArrayList<>();

				if (handle.get() != null
						&& openerActions.stream().anyMatch(ba -> ba.getTargetReference().equals(handle.get()))) {
					return openerActions.stream().filter(ba -> ba.getTargetReference().equals(handle.get())).findFirst().orElseThrow(AssertionError::new);
				}

				actions.forEach(a -> openerActions.add((OpenerAction) a));
				OpenerAction result = (OpenerAction) battlecry.apply(openerActions);
				handle.set(result.getTargetReference());
				return result;
			} else {
				return invocation.callRealMethod();
			}
		}).when(overriden).requestAction(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyList());
		return handle;
	}

	protected static Minion playMinionCard(GameContext context, Player player, String cardId, Entity target) {
		OverrideHandle<EntityReference> handle = overrideBattlecry(context, player, battlecryActions -> battlecryActions.stream().filter(c -> c.getTargetReference().equals(target.getReference())).findFirst().orElseThrow(AssertionError::new));
		Minion result = playMinionCard(context, player, cardId);
		handle.stop();
		return result;
	}

	protected static Minion playMinionCard(GameContext context, Player player, Card card, Entity target) {
		OverrideHandle<EntityReference> handle = overrideBattlecry(context, player, battlecryActions -> battlecryActions.stream().filter(c -> c.getTargetReference().equals(target.getReference())).findFirst().orElseThrow(AssertionError::new));
		return playMinionCard(context, player, card);
	}

	protected static void overrideDiscover(GameContext context, Player player, String cardId) {
		OverrideHandle<Card> handle = overrideRandomCard(context, cardId);
		overrideDiscover(context, player, discovers -> {
			DiscoverAction action = discovers.stream().filter(da -> da.getCard().getCardId().equals(cardId)).findFirst().orElseThrow(AssertionError::new);
			handle.stop();
			return action;
		});
	}

	public static <T extends Card> T receiveCard(GameContext context, Player player, String cardId) {
		@SuppressWarnings("unchecked")
		T card = (T) context.getCardCatalogue().getCardById(cardId);
		context.getLogic().receiveCard(player.getId(), card);
		return card;
	}

	public static <T extends Card> T receiveCard(GameContext context, Player player, T card) {
		context.getLogic().receiveCard(player.getId(), card);
		return card;
	}

	public static <T extends Card> T shuffleToDeck(GameContext context, Player player, String cardId) {
		@SuppressWarnings("unchecked")
		T card = (T) context.getCardCatalogue().getCardById(cardId);
		context.getLogic().shuffleToDeck(player, card);
		return card;
	}

	public static int costOf(GameContext context, Player player, Card deckCard) {
		return context.getLogic().getModifiedManaCost(player, deckCard);
	}

	public static void assertThrows(Runnable runnable) {
		assertThrows(Throwable.class, runnable);
	}

	/**
	 * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed. If it does not
	 * throw an exception, an {@link AssertionError} is thrown. If it throws the wrong type of exception, an {@code
	 * AssertionError} is thrown describing the mismatch; the exception that was actually thrown can be obtained by
	 * calling {@link AssertionError#getCause}.
	 *
	 * @param throwableClass the expected type of the exception
	 * @param runnable       A function that is expected to throw an exception when invoked
	 * @since 6.9.5
	 */
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	public static <T extends Throwable> void assertThrows(Class<T> throwableClass, Runnable runnable) {
		expectThrows(throwableClass, runnable);
	}

	public static <T extends Throwable> T expectThrows(Class<T> throwableClass, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			if (throwableClass.isInstance(t)) {
				return throwableClass.cast(t);
			} else {
				String mismatchMessage = String.format("Expected %s to be thrown, but %s was thrown",
						throwableClass.getSimpleName(), t.getClass().getSimpleName());

				final AssertionError cause = new AssertionError(mismatchMessage, t);
				fail(cause.getMessage());
				return null;
			}
		}
		String message = String.format("Expected %s to be thrown, but nothing was thrown",
				throwableClass.getSimpleName());
		fail(new AssertionError(message).getMessage());
		return null;
	}

	public static Card receive(GameContext context, Player player, int attack, int hp, int manaCost, Attribute... attributes) {
		CardDesc desc = new CardDesc();
		desc.setId(context.getLogic().generateCardId());
		desc.setName("Test Minion");
		desc.setRarity(Rarity.FREE);
		desc.setBaseAttack(attack);
		desc.setBaseHp(hp);
		desc.setType(CardType.MINION);
		desc.setHeroClass(HeroClass.ANY);
		desc.setAttributes(new AttributeMap());
		desc.setBaseManaCost(manaCost);
		for (Attribute gameTag : attributes) {
			desc.getAttributes().put(gameTag, true);
		}
		var card = desc.create();
		context.addTempCard(card);
		return receiveCard(context, player, card.clone());
	}

	public static @NotNull
	GameDeck randomDeck() {
		return new RandomDeck(randomHeroCard(ClasspathCardCatalogue.INSTANCE.spellsource()), ClasspathCardCatalogue.INSTANCE.spellsource());
	}

	/**
	 * Creates a game with two random decks in the specified format.
	 *
	 * @param format
	 * @return
	 */
	public static GameContext fromTwoRandomDecks(DeckFormat format) {
		return GameContext.fromDecks(Arrays.asList(RandomDeck.randomDeck(format), RandomDeck.randomDeck(format)))
				.setDeckFormat(format);
	}

	public static GameContext fromTwoRandomDecks(long seed) {
		var random = new XORShiftRandom(seed);
		return GameContext.fromDecks(random.nextLong(), Arrays.asList(RandomDeck.randomDeck(random.nextLong()), RandomDeck.randomDeck(random.nextLong())));
	}

	/**
	 * Retrieves a random hero in the specified {@code deckFormat}
	 *
	 * @param deckFormat
	 * @return
	 */
	public static String randomHeroCard(DeckFormat deckFormat) {
		List<String> baseHeroes = ClasspathCardCatalogue.INSTANCE.getBaseClasses(deckFormat);
		return baseHeroes.get(RandomUtils.nextInt(0, baseHeroes.size()));
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

	public GymFactory getGymFactory(GymConsumer initializer) {
		GymFactory factory = new GymFactory(this);
		factory.first = initializer;
		return factory;
	}

	public GymFactory getGymFactory(GymConsumer initializer, GymConsumer after) {
		GymFactory factory = getGymFactory(initializer);
		factory.after = after;
		return factory;
	}

	public void runGym(GymConsumer consumer, String heroClass1, String heroClass2) {
		var defaultFormat = getDefaultFormat();
		var format = new DeckFormat()
				.withName(defaultFormat.getName())
				.withCardSets(defaultFormat.getCardSets());
		// Remove the starting card
		format.setSecondPlayerBonusCards(new String[0]);
		GameContext context = new DebugContext(new Player(heroClass1, ClasspathCardCatalogue.INSTANCE), new Player(heroClass2, ClasspathCardCatalogue.INSTANCE), new GameLogic() {
			@Override
			public int determineBeginner(int... playerIds) {
				return GameContext.PLAYER_1;
			}
		}, format);
		context.setBehaviours(new Behaviour[]{new TestBehaviour(), new TestBehaviour()});
		// Disable fatigue
		context.getPlayers().forEach(p -> p.setAttribute(Attribute.DISABLE_FATIGUE));
		context.init();
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		consumer.run(context, player, opponent);
	}

	public static void runGym(GymConsumer consumer, GameDeck deck1, GameDeck deck2) {
		GameContext context = new DebugContext(new Player(deck1, ClasspathCardCatalogue.INSTANCE), new Player(deck2, ClasspathCardCatalogue.INSTANCE), new GameLogic() {
			@Override
			public int determineBeginner(int... playerIds) {
				return 0;
			}

			@Override
			protected int getStarterCards() {
				return 0;
			}

			@Override
			protected int getSecondPlayerBonusStarterCards() {
				return 0;
			}
		});
		context.setDeckFormat(context.getCardCatalogue().getSmallestSupersetFormat(deck1, deck2));
		context.setBehaviours(new Behaviour[]{new TestBehaviour(), new TestBehaviour()});
		context.init();
		consumer.run(context, context.getActivePlayer(), context.getOpponent(context.getActivePlayer()));
	}

	public void runGym(GymConsumer consumer) {
		runGym(consumer, getDefaultHeroClass(), getDefaultHeroClass());
	}

	@NotNull
	public String getDefaultHeroClass() {
		return "TEST";
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

	@BeforeAll
	public static void loadCards() {
	}

	protected static void attack(GameContext context, Player player, Entity attacker, Entity target) {
		PhysicalAttackAction physicalAttackAction = new PhysicalAttackAction(attacker.getReference());
		physicalAttackAction.setTarget(target);
		context.performAction(player.getId(), physicalAttackAction);
	}

	public DebugContext createContext(String hero1, String hero2) {
		return createContext(hero1, hero2, true, getDefaultFormat());
	}

	public DeckFormat getDefaultFormat() {
		return ClasspathCardCatalogue.INSTANCE.all;
	}

	public DebugContext createContext(String hero1, String hero2, boolean shouldInit, DeckFormat deckFormat) {
		Player player1 = new Player(RandomDeck.randomDeck(hero1, deckFormat), "Player 1", ClasspathCardCatalogue.INSTANCE);
		Player player2 = new Player(RandomDeck.randomDeck(hero2, deckFormat), "Player 2", ClasspathCardCatalogue.INSTANCE);

		DebugContext context = new DebugContext(player1, player2, new GameLogic() {
			@Override
			public int determineBeginner(int... playerIds) {
				return GameContext.PLAYER_1;
			}
		}, deckFormat);
		context.setBehaviours(new Behaviour[]{new TestBehaviour(), new TestBehaviour()});
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

	protected static Entity findCard(GameContext context, String cardId) {
		for (Player player : context.getPlayers()) {
			for (Card card : player.getHand()) {
				if (card.getSourceCard().getCardId().equals(cardId)) {
					return card;
				}
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
		Collections.sort(minionList, Comparator.comparingInt(Entity::getId));
		return minionList.get(minionList.size() - 1);
	}

	protected static void playCard(GameContext context, Player player, String cardId) {
		playCard(context, player, context.getCardCatalogue().getCardById(cardId));
	}

	protected static void playCard(GameContext context, Player player, Card card) {
		if (card.getZone() != Zones.HAND) {
			context.getLogic().receiveCard(player.getId(), card);
		}
		if (card.getTargetSelection() != TargetSelection.NONE && card.getTargetSelection() != null) {
			throw new UnsupportedOperationException(String.format("This card %s requires a target.", card.getName()));
		}
		context.performAction(player.getId(), card.play());
	}

	protected static void useHeroPower(GameContext context, Player player) {
		context.performAction(player.getId(), player.getHeroPowerZone().get(0).play());
	}

	protected static void useHeroPower(GameContext context, Player player, EntityReference target) {
		PlayCardAction action = player.getHeroPowerZone().get(0).play();
		action.setTargetReference(target);
		context.performAction(player.getId(), action);
	}

	protected static void playCard(GameContext context, Player player, String cardId, Entity target) {
		playCard(context, player, context.getCardCatalogue().getCardById(cardId), target);
	}

	protected static void playCard(GameContext context, Player player, Card card, Entity target) {
		if (card.getZone() != Zones.HAND) {
			context.getLogic().receiveCard(player.getId(), card);
		}
		if (target != null && !target.isInPlay()) {
			throw new UnsupportedOperationException("cannot target not in play entities");
		}
		GameAction action = card.play();
		if (card.hasBattlecry()) {
			overrideBattlecry(context, player, choices -> choices.stream().filter(choice -> Objects.equals(target != null ? target.getReference() : null, choice.getTargetReference())).findFirst().orElseThrow());
		} else {
			action.setTarget(target);
		}
		context.performAction(player.getId(), action);
	}

	protected static Minion playMinionCard(GameContext context, Player player, String minionCardId) {
		return playMinionCard(context, player, context.getCardCatalogue().getCardById(minionCardId));
	}

	protected static Minion playMinionCard(GameContext context, Player player, int attack, int hp) {
		Minion minion = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
		minion.setAttack(attack);
		minion.setBaseAttack(attack);
		context.getLogic().setHpAndMaxHp(minion, hp);
		minion.setBaseHp(hp);
		return minion;
	}

	protected static void castDamageSpell(GameContext context, Player player, int damage, Entity target) {
		CardDesc damageSpell = new CardDesc();
		damageSpell.setId(context.getLogic().generateCardId());
		damageSpell.setTargetSelection(TargetSelection.ANY);
		damageSpell.setSpell(DamageSpell.create(damage));
		damageSpell.setSet(TestCardResources.TEST);
		damageSpell.setType(CardType.SPELL);
		Card card = new Card(damageSpell);
		context.addTempCard(card);
		playCard(context, player, card, target);
	}

	protected static Minion playMinionCard(GameContext context, Player player, Card card) {
		if (card.getZone() != Zones.HAND) {
			context.getLogic().receiveCard(player.getId(), card);
		}
		if (card.getCardType() != CardType.MINION) {
			throw new AssertionError(String.format("cannot PlayMinionCard on non-minion card %s", card));
		}

		PlayCardAction play = card.isChooseOne() ? card.playOptions(context)[0] : card.play();
		context.performAction(player.getId(), play);
		return getSummonedMinion(player.getMinions());
	}

	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
	                                              Object messageOrSupplier) {

		var thread = new AtomicReference<Thread>();
		var factory = new ThreadFactory() {
			@Override
			public Thread newThread(@NotNull Runnable r) {
				thread.set(new Thread(r));
				return thread.get();
			}
		};

		ExecutorService executorService = Executors.newSingleThreadExecutor(factory);

		try {
			Future<T> future = executorService.submit(() -> {
				try {
					return supplier.get();
				} catch (Throwable throwable) {
					throw ExceptionUtils.throwAsUncheckedException(throwable);
				}
			});

			long timeoutInMillis = timeout.toMillis();
			try {
				return future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
			} catch (TimeoutException ex) {
				ex.setStackTrace(thread.get().getStackTrace());
				throw new AssertionFailedError(buildPrefix(nullSafeGet(messageOrSupplier))
						+ "execution timed out after " + timeoutInMillis + " ms\ntask stacktrace:\n" + Throwables.getStackTraceAsString(ex));
			} catch (ExecutionException ex) {
				throw ExceptionUtils.throwAsUncheckedException(ex.getCause());
			} catch (Throwable ex) {
				throw ExceptionUtils.throwAsUncheckedException(ex);
			}
		} finally {
			executorService.shutdownNow();
		}
	}

	static String nullSafeGet(Object messageOrSupplier) {
		if (messageOrSupplier instanceof String) {
			return (String) messageOrSupplier;
		}
		if (messageOrSupplier instanceof Supplier) {
			Object message = ((Supplier<?>) messageOrSupplier).get();
			if (message != null) {
				return message.toString();
			}
		}
		return null;
	}

	static String buildPrefix(String message) {
		return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
	}
}
