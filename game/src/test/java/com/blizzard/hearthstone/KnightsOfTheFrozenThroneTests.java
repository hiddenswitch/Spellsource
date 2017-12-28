package com.blizzard.hearthstone;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.ChooseBattlecryHeroCard;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.toList;

public class KnightsOfTheFrozenThroneTests extends TestBase {
	@Test
	public void testStubbornGatropodObsidianStatueInteraction() {
		// Tests that a mysterious exception due to a minion already being dead doesn't occur
		runGym((context, player, opponent) -> {
			Minion obsidianStatue = playMinionCard(context, player, "minion_obsidian_statue");
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, obsidianStatue, stubbornGastropod);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion obsidianStatue = playMinionCard(context, player, "minion_obsidian_statue");
			obsidianStatue.setHp(1);
			context.endTurn();
			Minion stubbornGastropod = playMinionCard(context, opponent, "minion_stubborn_gastropod");
			context.endTurn();
			attack(context, player, obsidianStatue, stubbornGastropod);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion obsidianStatue = playMinionCard(context, opponent, "minion_obsidian_statue");
			context.endTurn();
			attack(context, player, stubbornGastropod, obsidianStatue);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion stubbornGastropod = playMinionCard(context, player, "minion_stubborn_gastropod");
			context.endTurn();
			Minion obsidianStatue = playMinionCard(context, opponent, "minion_obsidian_statue");
			obsidianStatue.setHp(1);
			context.endTurn();
			attack(context, player, stubbornGastropod, obsidianStatue);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDeathstalkerRexxar() {
		runGym((GameContext context, Player player, Player opponent) -> {
			Behaviour spiedBehavior = Mockito.spy(player.getBehaviour());
			player.setBehaviour(spiedBehavior);
			List<MinionCard> minionCards = new ArrayList<>();

			final Answer<GameAction> answer = invocation -> {
				final List<GameAction> gameActions = (List<GameAction>) invocation.getArguments()[2];
				final DiscoverAction discoverAction = (DiscoverAction) gameActions.get(0);
				minionCards.add((MinionCard) discoverAction.getCard());
				return discoverAction;
			};

			Mockito.doAnswer(answer)
					.when(spiedBehavior)
					.requestAction(Mockito.any(), Mockito.any(), Mockito.anyList());

			playCard(context, player, "hero_deathstalker_rexxar");
			context.getLogic().performGameAction(player.getId(), player.getHero().getHeroPower().play());
			MinionCard cardInHand = (MinionCard) player.getHand().get(0);
			Assert.assertEquals(cardInHand.getBaseHp(), minionCards.stream().collect(summarizingInt(MinionCard::getBaseHp)).getSum());
			Assert.assertEquals(cardInHand.getBaseAttack(), minionCards.stream().collect(summarizingInt(MinionCard::getBaseAttack)).getSum());
			Assert.assertEquals(cardInHand.getBaseManaCost(), minionCards.stream().collect(summarizingInt(MinionCard::getBaseManaCost)).getSum());
		});
	}

	@Test
	public void testLichKing() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_the_lich_king");
			context.endTurn();
			Assert.assertEquals(player.getHand().get(0).getHeroClass(), HeroClass.SPIRIT);
		});
	}

	@Test
	public void testDefile() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "spell_defile");
			Assert.assertEquals(bloodfenRaptor.getHp(), 1);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			Minion patches = playMinionCard(context, opponent, "minion_patches_the_pirate");
			context.endTurn();
			playCard(context, player, "spell_defile");
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testMoorabi() {
		runGym((context, player, opponent) -> {
			Minion moorabi = playMinionCard(context, player, "minion_moorabi");
			Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_freezing_potion", bloodfenRaptor);
			Assert.assertEquals(player.getHand().size(), 1);
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_freezing_potion", moorabi);
			Assert.assertEquals(player.getHand().size(), 1,
					"Freezing Moorabi should not put a copy of Moorabi into your hand.");
			context.endTurn();
			Minion noviceEngineer = playMinionCard(context, player, "minion_novice_engineer");
			playCardWithTarget(context, player, "spell_freezing_potion", noviceEngineer);
			Assert.assertEquals(player.getHand().size(), 2);
			Assert.assertEquals(player.getHand().get(1).getCardId(), "minion_novice_engineer");
		});
	}

	@Test
	public void testValeeraTheHollow() {
		runGym((context, player, opponent) -> {
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "hero_valeera_the_hollow");
			Assert.assertTrue(player.getHand().containsCard("token_shadow_reflection"));
			Assert.assertFalse(context.getLogic().canPlayCard(player.getId(),
					player.getHand().get(0).getEntityReference()),
					"You should not be able to play the Shadow Reflection because it doesn't do anything until a card is played.");
			playCard(context, player, "minion_wisp");
			Assert.assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getEntityReference()),
					"Since you have 1 mana left and we last played a Wisp, the Shadow Reflection should have transformed into the Wisp and it should be playable.");
			context.endTurn();
			Assert.assertEquals(player.getHand().size(), 0, "The Shadow Reflection-as-Wisp should have removed itself from the player's hand");
			Minion bluegillWarrior = playMinionCard(context, opponent, "minion_bluegill_warrior");
			List<GameAction> validActions = context.getValidActions();
			Assert.assertFalse(validActions.stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK
							&& ga.getTargetReference().equals(player.getHero().getReference())),
					"Valeera has STEALTH, so she should not be targetable by the Bluegill Warrior");
			context.endTurn();
			Assert.assertTrue(player.getHand().containsCard("token_shadow_reflection"));
			playCard(context, player, "minion_water_elemental");
			playCard(context, player, "minion_wisp");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "minion_wisp",
					"Since Wisp was the last card the player played, Shadow Reflection should be a Wisp");
			context.endTurn();
			playCard(context, opponent, "minion_mindbreaker");
			context.endTurn();
			Assert.assertEquals(player.getHand().size(), 0, "The presence of Mindbreaker should prevent Shadow Reflection from entering the player's hand.");
		});
	}

	@Test
	public void testDoomerang() {
		runGym((context, player, opponent) -> {
			// 4/2, Deathrattle deals 1 damage to all minions
			playCard(context, player, CardCatalogue.getCardById("weapon_deaths_bite"));

			Assert.assertTrue(player.getHero().getWeapon().isActive());
			context.endTurn();
			Minion tarCreeper1 = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_tar_creeper"));
			Minion tarCreeper2 = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_tar_creeper"));
			context.endTurn();
			context.getLogic().performGameAction(player.getId(), new PhysicalAttackAction(player.getHero().getReference()).withTargetReference(tarCreeper1.getReference()));
			Assert.assertEquals(tarCreeper1.getHp(), 1);
			playCardWithTarget(context, player, CardCatalogue.getCardById("spell_doomerang"), tarCreeper2);
			Assert.assertEquals(tarCreeper1.getHp(), 1, "Deathrattle should not have triggered and should not have killed the first Tar Creeper.");
			Assert.assertEquals(tarCreeper2.getHp(), 1, "The second Tar Creeper should have been damaged by the Doomerang");
			Card card = player.getHand().get(player.getHand().getCount() - 1);
			Assert.assertEquals(card.getSourceCard().getCardId(), "weapon_deaths_bite", "Doomerang should now be in the player's hand.");
			Assert.assertEquals(player.getWeaponZone().size(), 0);
			context.getLogic().performGameAction(player.getId(), card.play());
			Assert.assertEquals(player.getHero().getWeapon().getDurability(), 2, "Doomerang should have 2 durability, not 1, since it was played fresh from the hand.");
		});
	}

	@Test
	public void testEternalServitude() {
		runGym((context, player, opponent) -> {
			Minion friendlyMinion = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_bloodfen_raptor"));
			context.endTurn();
			Minion opposingMinion = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_bloodfen_raptor"));
			context.endTurn();
			context.getLogic().performGameAction(player.getId(), new PhysicalAttackAction(friendlyMinion.getReference()).withTargetReference(opposingMinion.getReference()));
			Assert.assertEquals(player.getMinions().size(), 0);
			playCard(context, player, CardCatalogue.getCardById("spell_eternal_servitude"));
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});
	}

	@Test
	public void testSpiritLash() {
		runGym((context, player, opponent) -> {
			int originalHp = 1;
			player.getHero().setHp(originalHp);
			int expectedHealing = 5;
			for (int i = 0; i < expectedHealing; i++) {
				playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_bloodfen_raptor"));
			}
			playCard(context, player, CardCatalogue.getCardById("spell_spirit_lash"));
			Assert.assertEquals(player.getHero().getHp(), originalHp + expectedHealing);
		});
	}

	@Test
	public void testShadowEssence() {
		runGym((context, player, opponent) -> {
			context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("minion_bloodfen_raptor"));

			playCard(context, player, CardCatalogue.getCardById("spell_shadow_essence"));

			Minion minion = player.getMinions().get(0);
			Assert.assertEquals(minion.getAttack(), 5);
			Assert.assertEquals(minion.getHp(), 5);
		});
	}

	@Test
	public void testEmbraceDarkness() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_bloodfen_raptor"));
			context.endTurn();
			playCardWithTarget(context, player, CardCatalogue.getCardById("spell_embrace_darkness"), bloodfenRaptor);
			Assert.assertEquals(bloodfenRaptor.getOwner(), opponent.getId());
			context.endTurn();
			Assert.assertEquals(bloodfenRaptor.getOwner(), opponent.getId());
			for (int i = 0; i < 4; i++) {
				context.endTurn();
				Assert.assertEquals(bloodfenRaptor.getOwner(), player.getId());
			}
		});
	}

	@Test
	public void testArchibishopBenedictus() {
		runGym((context, player, opponent) -> {
			Stream.of("minion_water_elemental", "minion_bloodfen_raptor")
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().shuffleToDeck(opponent, c));
			playCard(context, player, CardCatalogue.getCardById("minion_archbishop_benedictus"));
			Assert.assertEquals(player.getDeck().size(), 2);
			Assert.assertEquals(opponent.getDeck().size(), 2);
			Assert.assertTrue(player.getDeck().containsCard("minion_water_elemental"));
			Assert.assertTrue(player.getDeck().containsCard("minion_bloodfen_raptor"));
		});

	}

	@Test
	public void testPrinceTaldaram() {
		runGym((context, player, opponent) -> {
			Minion waterElemental = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_water_elemental"));
			Minion princeTaldaram = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_prince_taldaram"));
			Assert.assertEquals(princeTaldaram.getZone(), Zones.BATTLEFIELD);
			Assert.assertEquals(princeTaldaram.getAttack(), 3);
			Assert.assertEquals(princeTaldaram.getHp(), 3);
			context.endTurn();
			Minion arcaneGiant = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_arcane_giant"));
			context.endTurn();
			context.getLogic().fight(player, princeTaldaram, arcaneGiant);
			Assert.assertTrue(arcaneGiant.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testMeatWagon() {
		runGym((context, player, opponent) -> {
			Stream.of("minion_dragon_egg" /*0*/, "minion_voidwalker" /*1*/, "minion_bloodfen_raptor" /*3*/)
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().shuffleToDeck(player, c));

			Minion meatWagon = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_meat_wagon"));
			context.getLogic().destroy(meatWagon);
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_dragon_egg");

			// Remove dragon egg
			player.getDeck().stream().filter(c -> c.getCardId().equals("minion_dragon_egg"))
					.findFirst()
					.orElseThrow(AssertionError::new).moveOrAddTo(context, Zones.GRAVEYARD);

			meatWagon = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_meat_wagon"));
			playCardWithTarget(context, player, CardCatalogue.getCardById("spell_divine_strength" /*+1/+2*/), meatWagon);
			Assert.assertEquals(meatWagon.getAttack(), 2);
			context.getLogic().destroy(meatWagon);
			Assert.assertEquals(player.getMinions().size(), 2);
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_voidwalker");
		});
	}

	@Test
	public void testFurnacefireColossus() {
		runGym((context, player, opponent) -> {
			Stream.of("weapon_arcanite_reaper" /*5/2*/, "weapon_coghammer" /*2/3*/, "minion_bloodfen_raptor")
					.map(CardCatalogue::getCardById)
					.forEach(c -> context.getLogic().receiveCard(player.getId(), c));
			Minion furnacefireColossus = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_furnacefire_colossus"));
			Assert.assertEquals(furnacefireColossus.getAttack(), 6 + 5 + 2);
			Assert.assertEquals(furnacefireColossus.getHp(), 6 + 2 + 3);
			Assert.assertEquals(player.getHand().size(), 1);
		});

	}

	@Test
	public void testFrostmourne() {
		runGym((context, player, opponent) -> {
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
		});
	}

	@Test
	public void testSimulacrum() {
		runGym((context, player, opponent) -> {
			// TODO: Should Simulacrum always copy the most recently drawn card when there are multiple cards of the same
			// mana cost?
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
		});


		// Test simulacrum with no minion cards
		runGym((context, player, opponent) -> {
			playCard(context, player, CardCatalogue.getCardById("spell_simulacrum"));
			Assert.assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testIceWalker() {
		// Notes on interaction from https://twitter.com/ThePlaceMatt/status/891810684551311361
		// Any Hero Power that targets will also freeze that target. So it does work with Frost Lich Jaina. (And even
		// works with Anduin's basic HP!)
		checkIceWalker(true, new HeroClass[]{
				HeroClass.BLUE,
				HeroClass.WHITE
		});
		checkIceWalker(false, new HeroClass[]{
				HeroClass.BLACK,
				HeroClass.VIOLET,
				HeroClass.BROWN,
				HeroClass.GOLD,
				HeroClass.SILVER,
				HeroClass.RED,
				HeroClass.GREEN
		});
	}

	private void checkIceWalker(final boolean expected, final HeroClass[] classes) {
		for (HeroClass heroClass : classes) {
			runGym((context, player, opponent) -> {
				player.setMaxMana(2);
				player.setMana(2);

				Minion icyVeins = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_ice_walker"));
				PlayCardAction play = player.getHero().getHeroPower().play();
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
			}, heroClass, HeroClass.BLUE);
		}
	}

	@Test
	public void testSpreadingPlague() {
		Stream.of(0, 3, 7).forEach(minionCount -> {
			GameContext context = createContext(HeroClass.BROWN, HeroClass.BROWN);
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

						GameContext context1 = createContext(HeroClass.BROWN, HeroClass.RED);
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
}
