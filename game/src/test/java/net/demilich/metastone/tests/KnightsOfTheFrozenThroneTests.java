package net.demilich.metastone.tests;


import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.HeroPowerAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.ChooseBattlecryHeroCard;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class KnightsOfTheFrozenThroneTests extends TestBase {
	@Test
	public void testFrostmourne() {
		GameContext context = createContext(HeroClass.MAGE, HeroClass.MAGE);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearZone(context, player.getDeck());

		playCard(context, player, CardCatalogue.getCardById("weapon_frostmourne"));
		Minion leeroyJenkins = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_leeroy_jenkins"));

		Minion bloodfenRaptor = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_bloodfen_raptor"));
		Minion waterElemental = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_water_elemental"));

		PhysicalAttackAction heroAttack = new PhysicalAttackAction(player.getHero().getReference());
		heroAttack.setTarget(bloodfenRaptor);

		Weapon frostmourne = player.getHero().getWeapon();
		frostmourne.setAttribute(Attribute.HP, 1);

		PhysicalAttackAction leeroyAttack = new PhysicalAttackAction(leeroyJenkins.getReference());
		leeroyAttack.setTarget(waterElemental);
		context.getLogic().performGameAction(player.getId(), leeroyAttack);
		context.getLogic().performGameAction(player.getId(), heroAttack);

		Assert.assertEquals(player.getMinions().size(), 1);
		Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
	}

	@Test
	public void testSimulacrum() {
		{
			// TODO: Should Simulacrum always copy the most recently drawn card when there are multiple cards of the same
			// mana cost?
			GameContext context = createContext(HeroClass.MAGE, HeroClass.MAGE);
			Player player = context.getPlayer1();
			clearHand(context, player);
			clearZone(context, player.getDeck());

			Stream.of("minion_water_elemental" /*4*/,
					"spell_the_coin"/*0*/,
					"minion_acolyte_of_pain"/*3*/)
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().receiveCard(player.getId(), c));

			playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_emperor_thaurissan"));
			context.endTurn();
			context.endTurn();
			context.endTurn();
			context.endTurn();
			// Now the minions in the hand are 2, 1
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("minion_bloodfen_raptor" /*2*/));
			playCard(context, player, CardCatalogue.getCardById("spell_simulacrum"));
			Assert.assertEquals(player.getHand().stream().filter(c -> c.getCardId().equals("minion_acolyte_of_pain")).count(), 2L);
		}

		// Test simulacrum with no minion cards
		{
			GameContext context = createContext(HeroClass.MAGE, HeroClass.MAGE);
			Player player = context.getPlayer1();
			clearHand(context, player);
			clearZone(context, player.getDeck());

			playCard(context, player, CardCatalogue.getCardById("spell_simulacrum"));
			Assert.assertEquals(player.getHand().size(), 0);
		}
	}

	@Test
	public void testIceWalker() {
		// Notes on interaction from https://twitter.com/ThePlaceMatt/status/891810684551311361
		// Any Hero Power that targets will also freeze that target. So it does work with Frost Lich Jaina. (And even
		// works with Anduin's basic HP!)
		checkIceWalker(true, new HeroClass[]{
				HeroClass.MAGE,
				HeroClass.PRIEST
		});
		checkIceWalker(false, new HeroClass[]{
				HeroClass.ROGUE,
				HeroClass.WARLOCK,
				HeroClass.DRUID,
				HeroClass.PALADIN,
				HeroClass.SHAMAN,
				HeroClass.WARRIOR,
				HeroClass.HUNTER
		});
	}

	private void checkIceWalker(final boolean expected, final HeroClass[] classes) {
		for (HeroClass heroClass : classes) {
			GameContext context = createContext(heroClass, HeroClass.ROGUE);
			Player player = context.getPlayer1();
			player.setMaxMana(2);
			player.setMana(2);

			Minion icyVeins = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_ice_walker"));
			PlayCardAction play = player.getHero().getHeroPower().play();
			Player opponent = context.getOpponent(player);
			Entity target;

			if (play.getTargetRequirement() != TargetSelection.NONE) {
				target = opponent.getHero();
			} else {
				List<Entity> entities = context.resolveTarget(player, player.getHero().getHeroPower(), player.getHero().getHeroPower().getSpell().getTarget());
				if (entities == null || entities.size() == 0) {
					target = null;
				} else {
					target = entities.get(0);
				}
			}

			play.setTarget(target);
			context.getLogic().performGameAction(player.getId(), play);
			if (target == null) {
				Assert.assertFalse(expected);
			} else {
				Assert.assertEquals(target.hasAttribute(Attribute.FROZEN), expected);
			}
			context.endTurn();
			context.endTurn();
			context.getLogic().destroy(icyVeins);
			play = player.getHero().getHeroPower().play();
			play.setTarget(target);
			context.getLogic().performGameAction(player.getId(), play);
			if (target == null) {
				Assert.assertFalse(expected);
			} else {
				Assert.assertFalse(target.hasAttribute(Attribute.FROZEN));
			}
		}
	}

	@Test
	public void testSpreadingPlague() {
		Stream.of(0, 3, 7).forEach(minionCount -> {
			GameContext context = createContext(HeroClass.DRUID, HeroClass.DRUID);
			Player player = context.getActivePlayer();
			Player opponent = context.getOpponent(player);
			context.endTurn();
			for (int i = 0; i < minionCount; i++) {
				playCard(context, opponent, CardCatalogue.getCardById("minion_wisp"));
			}
			context.endTurn();
			player.setMaxMana(6);
			player.setMana(6);
			playCard(context, player, CardCatalogue.getCardById("spell_spreading_plague"));
			// Should summon at least one minion
			Assert.assertEquals(player.getMinions().size(), Math.max(minionCount, 1));
		});
	}

	@Test
	public void testMalfurionThePestilent() {
		BiFunction<GameContext, String, GameContext> joinHeroPowerCardId = (context, heroPowerCardId) -> {
			Player player = context.getPlayer1();

			HeroPowerAction action = context.getValidActions().stream().filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
					.map(ga -> (HeroPowerAction) ga)
					.filter(ga -> ga.getChoiceCardId().equals(heroPowerCardId))
					.findFirst().orElseThrow(AssertionError::new);

			context.getLogic().performGameAction(player.getId(), action);

			return context;
		};

		Stream<Consumer<GameContext>> heroPowerChecks = Stream.of(
				(context) -> {
					Assert.assertEquals(context.getPlayer1().getHero().getAttack(), 3);
				},
				(context) -> {
					// Expect 5 armor + additional  3 from hero power
					Assert.assertEquals(context.getPlayer1().getHero().getArmor(), 8);
				}
		);

		Stream<String> heroPowers = Stream.of("hero_power_plague_lord1", "hero_power_plague_lord2");

		List<Object> allChecked = zip(heroPowers, heroPowerChecks, (heroPowerCardId, heroPowerChecker) -> {
			Stream<GameContext> contexts = Stream.of((Function<ChooseBattlecryHeroCard, PlayCardAction>) (card -> card.playOptions()[0]),
					card -> card.playOptions()[1],
					ChooseBattlecryHeroCard::playBothOptions)
					.map(actionGetter -> {
						ChooseBattlecryHeroCard malfurion = (ChooseBattlecryHeroCard) CardCatalogue.getCardById("hero_malfurion_the_pestilent");

						GameContext context1 = createContext(HeroClass.DRUID, HeroClass.WARRIOR);
						Player player = context1.getPlayer1();
						clearHand(context1, player);
						clearZone(context1, player.getDeck());

						context1.getLogic().receiveCard(player.getId(), malfurion);
						// Mana = 7 for Hero + 2 for the hero power
						player.setMaxMana(9);
						player.setMana(9);

						PlayCardAction action = actionGetter.apply(malfurion);
						context1.getLogic().performGameAction(player.getId(), action);
						Assert.assertEquals(player.getHero().getArmor(), 5);

						// Assert that the player has both choose one hero powers present
						Assert.assertEquals(context1.getValidActions().stream()
								.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
								.count(), 2L);

						return context1;
					});

			Stream<Function<GameContext, GameContext>> battlecryChecks = Stream.of((context1) -> {
						Player player = context1.getPlayer1();

						Assert.assertEquals(player.getMinions().size(), 2);
						Assert.assertTrue(player.getMinions().stream().allMatch(m -> m.getSourceCard().getCardId().equals("token_frost_widow")));
						return context1;
					},
					(context11) -> {
						Player player = context11.getPlayer1();

						Assert.assertEquals(player.getMinions().size(), 2);
						Assert.assertTrue(player.getMinions().stream().allMatch(m -> m.getSourceCard().getCardId().equals("token_scarab_beetle")));
						return context11;
					},
					(context12) -> {
						Player player = context12.getPlayer1();

						Assert.assertEquals(player.getMinions().size(), 4);
						int scarabs = 0;
						int frosts = 0;
						for (Minion minion : player.getMinions()) {
							if (minion.getSourceCard().getCardId().equals("token_frost_widow")) {
								frosts += 1;
							}
							if (minion.getSourceCard().getCardId().equals("token_scarab_beetle")) {
								scarabs += 1;
							}
						}
						Assert.assertEquals(scarabs, 2);
						Assert.assertEquals(frosts, 2);
						return context12;
					});

			// Do everything: zip the contexts with the battlecry checks, which executes the battlecry
			// then try the hero power specified hero and check that it worked
			zip(contexts, battlecryChecks, (context, checker) -> checker.apply(context))
					.map(context -> joinHeroPowerCardId.apply(context, heroPowerCardId))
					.forEach(heroPowerChecker);

			return null;
		}).collect(toList());


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
}
