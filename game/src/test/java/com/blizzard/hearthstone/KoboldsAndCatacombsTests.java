package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

public class KoboldsAndCatacombsTests extends TestBase {

	@Test
	public void testGeosculptorYip() {
		for (int j = 0; j <= 20; j++) {
			final int i = j;
			final int expectedCost = Math.min(10, i);
			runGym((context, player, opponent) -> {
				context.getLogic().gainArmor(player, i);
				playCard(context, player, "minion_geosculptor_yip");
				context.endTurn();
				Assert.assertEquals(player.getMinions().size(), 2);
				Assert.assertEquals(player.getMinions().get(1).getSourceCard().getBaseManaCost(), expectedCost);
			});
		}

	}

	@Test
	public void testDiamondSpellstone() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_fireball", bloodfen);
			playCard(context, player, "spell_diamond_spellstone");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});

		/**
		 * Diamond Spellstone will only resurrect a distinct minion, meaning if you have multiple versions of the same
		 * minion die, you'll only get one copy of it back. For example, if you have multiple dead Saronite Chain Gangs,
		 * it will only resurrect one of them. This also means that minions that summon separate token minions instead
		 * of copies, such as Doppelgangster or Big-Time Racketeer will function as you expect with the spellstone,
		 * being capable of summoning the base minion as well as its generated minions.
		 */
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_saronite_chain_gang");
			playCardWithTarget(context, player, "spell_fireball", player.getMinions().get(1));
			playCardWithTarget(context, player, "spell_fireball", player.getMinions().get(0));
			playCard(context, player, "spell_diamond_spellstone");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_saronite_chain_gang");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doppelgangster");
			playCardWithTarget(context, player, "spell_fireball", player.getMinions().get(2));
			playCardWithTarget(context, player, "spell_fireball", player.getMinions().get(1));
			playCardWithTarget(context, player, "spell_fireball", player.getMinions().get(0));
			playCard(context, player, "spell_diamond_spellstone");
			Assert.assertEquals(player.getMinions().size(), 3);
			Assert.assertTrue(player.getMinions().stream().allMatch(m -> m.getSourceCard().getCardId().equals("minion_doppelgangster")));
		});
	}

	@Test(invocationCount = 6)
	public void testGrandArchivistRenounceDarknessInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_voidwalker");
			}
			shuffleToDeck(context, player, "spell_renounce_darkness");
			playCard(context, player, "minion_grand_archivist");
			context.endTurn();
			shuffleToDeck(context, player, "spell_simulacrum");
			for (int i = 0; i < 10; i++) {
				context.endTurn();
				context.endTurn();
			}
		});
	}

	@Test
	public void testCheatDeath() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_cheat_death");
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", bloodfen);
			Assert.assertEquals(costOf(context, player, player.getHand().get(0)), bloodfen.getSourceCard().getBaseManaCost() - 2);
		});
	}

	@Test
	public void testGoldenKobold() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "token_golden_kobold");
			Assert.assertEquals(player.getHand().stream().filter(c -> c.getRarity() == Rarity.LEGENDARY
					&& c.getCardType() == CardType.MINION).count(), 3);
		});
	}

	@Test
	public void testUnidentifiedElixirStartsInHand() {
		DebugContext context = createContext(HeroClass.WHITE, HeroClass.WHITE, false);
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck ->
				Stream.generate(() -> "spell_unidentified_elixir")
						.map(CardCatalogue::getCardById)
						.limit(30)
						.forEach(deck::addCard));
		context.init();
		Assert.assertTrue(context.getPlayers().stream().flatMap(p -> p.getHand().stream()).noneMatch(c -> c.getCardId().equals("spell_unidentified_elixir")));
	}


	@Test
	public void testAmethystSpellstone() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_amethyst_spellstone");
			playCard(context, player, "minion_flame_imp");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_amethyst_spellstone");
		}, HeroClass.VIOLET, HeroClass.VIOLET);

		// Amethyst should not trigger on warlock hero power
		// Also tests that amethyst doesn't trigger on fatigue
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_amethyst_spellstone");
			context.getLogic().performGameAction(player.getId(), player.getHeroPowerZone().get(0).play());
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_amethyst_spellstone");
		}, HeroClass.VIOLET, HeroClass.VIOLET);
	}

	@Test
	public void testBranchingPaths() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_mirror_image");
			shuffleToDeck(context, player, "spell_mirror_image");
			overrideDiscover(player, discoveries -> discoveries.stream().filter(c -> c.getCard().getName().equals("Eat the Mushroom")).findFirst().orElseThrow(AssertionError::new));
			playCard(context, player, "spell_branching_paths");
			Assert.assertEquals(player.getHand().stream().filter(c -> c.getCardId().equals("spell_mirror_image")).count(), 2L, "Should have drawn cards twice.");
		});
	}

	@Test
	public void testTheDarkness() {
		final String regularDescription = "Starts dormant. Battlecry: Shuffle 3 Candles into the enemy deck. When drawn, this awakens.";
		final String permanentDescription = "When your opponent draws 3 Candles, this awakens!";
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			Assert.assertTrue(theDarkness.hasAttribute(Attribute.PERMANENT), "Comes into play permanent.");
			Assert.assertEquals(theDarkness.getDescription(), permanentDescription, "Should have different description.");
		});

		// Dirty rat play
		// When The Darkness is summoned by any means other than being played, it will start dormant but no Darkness
		// Candles are generated. When copied while on the board as a minion, the copy will not start dormant.
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_the_darkness");
			context.endTurn();
			playCard(context, opponent, "minion_dirty_rat");
			Assert.assertTrue(player.getMinions().get(0).hasAttribute(Attribute.PERMANENT), "Comes into play permanent.");
		});

		// Test that drawing candles removes the permanent attribute
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			Assert.assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 3L);
			for (int i = 0; i < 3; i++) {
				context.endTurn();
				// Opponent's turn
				context.endTurn();
			}
			Assert.assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 0L);
			Assert.assertFalse(theDarkness.hasAttribute(Attribute.PERMANENT));
			Assert.assertEquals(theDarkness.getDescription(), regularDescription, "Should have different description.");
		});

		// Test that milling a candle does not trigger The Darkness
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			Assert.assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 3L);
			context.getLogic().receiveCard(opponent.getId(), CardCatalogue.getCardById("spell_mirror_image"), 10);
			for (int i = 0; i < 3; i++) {
				context.endTurn();
				// Opponent's turn
				context.endTurn();
			}
			Assert.assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 0L);
			Assert.assertTrue(theDarkness.hasAttribute(Attribute.PERMANENT));
			Assert.assertEquals(theDarkness.getDescription(), permanentDescription, "Should have different description.");
		});

		// When copied while on the board as a minion, the copy will not start dormant
		// Test that drawing candles removes the permanent attribute
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			Assert.assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 3L);
			for (int i = 0; i < 3; i++) {
				context.endTurn();
				// Opponent's turn
				context.endTurn();
			}
			Assert.assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 0L);
			Assert.assertFalse(theDarkness.hasAttribute(Attribute.PERMANENT));
			Assert.assertEquals(theDarkness.getDescription(), regularDescription, "Should have different description.");
			Minion faceless = (Minion) playMinionCard(context, player, "minion_faceless_manipulator").transformResolved(context);
			Assert.assertEquals(faceless.getSourceCard().getCardId(), "minion_the_darkness");
			Assert.assertFalse(faceless.hasAttribute(Attribute.PERMANENT));
		});
	}

	@Test
	public void testKoboldMonk() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			receiveCard(context, opponent, "spell_fireball");
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertTrue(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			context.endTurn();
			Minion kobold = playMinionCard(context, player, "minion_kobold_monk");
			context.endTurn();
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertFalse(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			playCardWithTarget(context, opponent, "spell_fireball", kobold);
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertTrue(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
		});

		// Two Kobold Monks, one dies, the player shouldn't be targetable
		runGym((context, player, opponent) -> {
			context.endTurn();
			receiveCard(context, opponent, "spell_fireball");
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertTrue(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			context.endTurn();
			Minion kobold1 = playMinionCard(context, player, "minion_kobold_monk");
			Minion kobold2 = playMinionCard(context, player, "minion_kobold_monk");
			context.endTurn();
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertFalse(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			playCardWithTarget(context, opponent, "spell_fireball", kobold2);
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertFalse(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			playCardWithTarget(context, opponent, "spell_fireball", kobold1);
			opponent.setMaxMana(4);
			opponent.setMana(4);
			Assert.assertTrue(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
		});
	}

	@Test
	public void testKingTogwaggle() {
		runGym((context, player, opponent) -> {
			final Card card1a = CardCatalogue.getCardById("spell_mirror_image");
			final Card card1b = CardCatalogue.getCardById("spell_fireball");
			final Card card2a = CardCatalogue.getCardById("minion_bloodfen_raptor");
			final Card card2b = CardCatalogue.getCardById("minion_acidic_swamp_ooze");
			Stream.of(card1a, card1b).forEach(c -> context.getLogic().shuffleToDeck(player, c));
			Stream.of(card2a, card2b).forEach(c -> context.getLogic().shuffleToDeck(opponent, c));
			playCard(context, player, "minion_king_togwaggle");
			Assert.assertTrue(opponent.getDeck().containsAll(Arrays.asList(card1a, card1b)));
			Assert.assertTrue(opponent.getDeck().containsCard("spell_ransom"));
			Assert.assertTrue(player.getDeck().containsAll(Arrays.asList(card2a, card2b)));
			GameLogic spyLogic = Mockito.spy(context.getLogic());
			context.setLogic(spyLogic);
			Mockito.doReturn(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_ransom")).findFirst().orElseThrow(AssertionError::new))
					.when(spyLogic).getRandom(Mockito.any(CardList.class));
			context.endTurn();
			Assert.assertTrue(opponent.getHand().containsCard("spell_ransom"));
			playCard(context, opponent, opponent.getHand().get(0));
			Assert.assertTrue(opponent.getDeck().containsAll(Arrays.asList(card2a, card2b)));
			Assert.assertTrue(player.getDeck().containsAll(Arrays.asList(card1a, card1b)));
		});
	}

	@Test
	public void testGrandArchivist() {
		runGym((context, player, opponent) -> {
			final Card card = CardCatalogue.getCardById("spell_mirror_image");
			context.getLogic().shuffleToDeck(player, card);
			Assert.assertEquals(player.getDeck().size(), 1);
			playCard(context, player, "minion_grand_archivist");
			context.endTurn();
			Assert.assertEquals(player.getDeck().size(), 0);
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_mirror_image");
			Assert.assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_mirror_image");
		});
	}

	@Test
	public void testEbonDragonsmith() {
		runGym((context, player, opponent) -> {
			final Card card = CardCatalogue.getCardById("weapon_arcanite_reaper");
			context.getLogic().receiveCard(player.getId(), card);
			int initialCost = costOf(context, player, card);
			playCard(context, player, "minion_ebon_dragonsmith");
			Assert.assertEquals(costOf(context, player, card), initialCost - 2);
		});
	}

	@Test
	public void testArcaneTyrant() {
		runGym((context, player, opponent) -> {
			final Card card = CardCatalogue.getCardById("minion_arcane_tyrant");
			context.getLogic().receiveCard(player.getId(), card);
			Assert.assertEquals(costOf(context, player, card), 5);
			context.endTurn();
			playCard(context, opponent, "spell_mirror_image");
			Assert.assertEquals(costOf(context, player, card), 5);
			playCard(context, opponent, "spell_doom");
			Assert.assertEquals(costOf(context, player, card), 5);
			context.endTurn();
			playCard(context, player, "spell_doom");
			Assert.assertEquals(costOf(context, player, card), 0);
			context.endTurn();
			Assert.assertEquals(costOf(context, player, card), 5);
			context.endTurn();
			playCard(context, player, "spell_mirror_image");
			Assert.assertEquals(costOf(context, player, card), 5);
			playCard(context, player, "spell_doom");
			Assert.assertEquals(costOf(context, player, card), 0);
		});
	}

	@Test
	public void testKoboldBarbarian() {
		runGym((context, player, opponent) -> {
			Minion kobold = playMinionCard(context, player, "minion_kobold_barbarian");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			context.endTurn();
			Assert.assertEquals(opponent.getHero().getHp(), opponentHp - kobold.getAttack());
			Assert.assertFalse(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK));
		});
	}

	@Test
	@Ignore
	public void testUnstableEvolution() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCardWithTarget(context, player, "spell_unstable_evolution", minion);
			Assert.assertEquals(player.getHand().size(), 1);
			for (int i = 0; i < 5; i++) {
				playCardWithTarget(context, player, player.getHand().get(0), minion.transformResolved(context));
			}
			Assert.assertEquals(minion.transformResolved(context).getSourceCard().getBaseManaCost(), 8);
			context.endTurn();
			Assert.assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testMurmuringElemental() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_murmuring_elemental");
			playCard(context, player, "minion_kobold_hermit");
			Assert.assertEquals(player.getMinions().size(), 4);
			playCard(context, player, "minion_kobold_hermit");
			Assert.assertEquals(player.getMinions().size(), 6);
		});
	}

	@Test
	public void testKoboldHermit() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_kobold_hermit");
			Assert.assertEquals(player.getMinions().size(), 2);
			Assert.assertEquals(player.getMinions().get(1).getRace(), Race.TOTEM);
		});
	}

	@Test
	public void testHealingRainLightwardenInteraction() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			playCard(context, player, "spell_healing_rain");
			Assert.assertEquals(lightwarden.getAttack(), 1 + 2 * 12);
		});
	}

	@Test
	public void testCollectibilityOfSpellstoneCards() {
		CardCatalogue.getRecords().values().forEach(ccr -> {
			if (ccr.getDesc().name.contains("Spellstone")) {
				Assert.assertEquals(ccr.getDesc().collectible, ccr.getDesc().name.contains("Lesser"), "Invalid collectibility for spellstone " + ccr.getDesc().name);
			}
		});
	}

	@Test
	public void testKingsbane() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_kingsbane");
			playCard(context, player, "spell_envenom_weapon");
			for (int i = 0; i < 3; i++) {
				attack(context, player, player.getHero(), opponent.getHero());
				context.endTurn();
				context.endTurn();
			}
			context.endTurn();
			context.endTurn();
			playCard(context, player, player.getHand().get(0));
			Assert.assertTrue(player.getHero().getWeapon().hasAttribute(Attribute.POISONOUS));
		});
	}

	@Test
	public void testKingsbaneDoomerangInteraction() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			context.endTurn();
			playCard(context, player, "weapon_kingsbane");
			playCard(context, player, "spell_envenom_weapon");
			playCardWithTarget(context, player, "spell_doomerang", bloodfen);
			Assert.assertTrue(bloodfen.isDestroyed(), "The raptor should be destroyed because the Kingsbane had poisonous.");
			playCard(context, player, player.getHand().get(0));
			Assert.assertTrue(player.getHero().getWeapon().hasAttribute(Attribute.POISONOUS));
		});
	}


	@Test
	public void testCallPetUnidentifiedElixirInteraction() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_unidentified_elixir");
			playCard(context, player, "spell_call_pet");
			Assert.assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testToMySide() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_to_my_side");
			Assert.assertEquals(player.getMinions().size(), 2);
			Assert.assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).distinct().count(), 2L);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_to_my_side");
			Assert.assertEquals(player.getMinions().size(), 1);
			Assert.assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).distinct().count(), 1L);
		});
	}

	@Test
	public void testSuddenBetrayal() {
		// No adjacent minions: doesn't trigger
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			context.endTurn();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			attack(context, opponent, boar, player.getHero());
			Assert.assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_sudden_betrayal");
		});

		// Has adjacent minions: does trigger!
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			context.endTurn();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, opponent, "minion_stonetusk_boar");
			attack(context, opponent, boar, player.getHero());
			Assert.assertEquals(player.getSecrets().size(), 0);
			Assert.assertEquals(opponent.getMinions().size(), 0);
		});

		// Has adjacent minions but attacks opponent hero: doesn't trigger!
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			context.endTurn();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, opponent, "minion_stonetusk_boar");
			attack(context, opponent, boar, opponent.getHero());
			Assert.assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_sudden_betrayal");
		});

		// Friendly has adjacent minions and attacks opponent hero: doesn't trigger!
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			Minion boar = playMinionCard(context, player, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, player, "minion_stonetusk_boar");
			attack(context, player, boar, opponent.getHero());
			Assert.assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_sudden_betrayal");
		});
	}

	@Test
	public void testSonyaShadowdancer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_sonya_shadowdancer");
			playCardWithTarget(context, player, "spell_fireball", player.getMinions().get(0));
			final Card card = player.getHand().get(0);
			Assert.assertEquals(costOf(context, player, card), 1);
			player.setMaxMana(10);
			player.setMana(10);
			Assert.assertEquals(((MinionCard) card).getBaseAttack(), 1);
			Assert.assertEquals(((MinionCard) card).getBaseHp(), 1);
			context.getLogic().performGameAction(player.getId(), card.play());
			Assert.assertEquals(player.getMana(), 9);
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_bloodfen_raptor");
			Assert.assertEquals(player.getMinions().get(1).getHp(), 1);
			Assert.assertEquals(player.getMinions().get(1).getAttack(), 1);
		});
	}

	@Test
	public void testTemporus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_temporus");
			Assert.assertEquals(context.getActivePlayerId(), player.getId());
			context.endTurn();
			Assert.assertEquals(context.getActivePlayerId(), opponent.getId());
			context.endTurn();
			Assert.assertEquals(context.getActivePlayerId(), opponent.getId());
			context.endTurn();
			Assert.assertEquals(context.getActivePlayerId(), player.getId());
			context.endTurn();
			Assert.assertEquals(context.getActivePlayerId(), player.getId());
			context.endTurn();
			Assert.assertEquals(context.getActivePlayerId(), opponent.getId());
			context.endTurn();
			Assert.assertEquals(context.getActivePlayerId(), player.getId());
		});
	}

	@Test
	public void testDragonSoul() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_dragon_soul");
			for (int i = 0; i < 3; i++) {
				Assert.assertEquals(player.getMinions().size(), 0);
				playCard(context, player, "spell_innervate");
			}
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_dragon_spirit");
			for (int i = 0; i < 3; i++) {
				Assert.assertEquals(player.getMinions().size(), 1);
				playCard(context, player, "spell_innervate");
			}
			Assert.assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_dragon_spirit");
			// Tick up the spell casting count just before the turn ends
			playCard(context, player, "spell_innervate");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_innervate");
			playCard(context, player, "spell_innervate");
			// An extra dragon should not have appeared
			Assert.assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testValanyr() {
		runGym((context, player, opponent) -> {
			final Card bloodfenCard = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), bloodfenCard);
			playCard(context, player, "weapon_valanyr");
			attack(context, player, player.getHero(), opponent.getHero());
			context.endTurn();
			context.endTurn();
			attack(context, player, player.getHero(), opponent.getHero());
			Assert.assertEquals(player.getHero().getWeaponZone().size(), 0);
			context.getLogic().performGameAction(player.getId(), bloodfenCard.play());
			context.endTurn();
			playCardWithTarget(context, player, "spell_assassinate", player.getMinions().get(0));
			Assert.assertEquals(player.getHero().getWeapon().getSourceCard().getCardId(), "weapon_valanyr");
		});
	}

	@Test
	public void testLynessaSunsorrow() {
		// Confirm that the opponent's spells don't count
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");
			context.endTurn();
			Stream.of("spell_divine_strength").forEach(cardId -> {
				playCardWithTarget(context, opponent, cardId, silverHand1);
			});
			context.endTurn();
			Stream.of("spell_divine_strength", "spell_divine_strength").forEach(cardId -> {
				playCardWithTarget(context, player, cardId, silverHand1);
			});
			Minion lynessaSunsorrow = playMinionCard(context, player, "minion_lynessa_sunsorrow");
			Assert.assertEquals(lynessaSunsorrow.getAttack(), 1 + 2);
			Assert.assertEquals(lynessaSunsorrow.getHp(), 1 + 4);
		});

		// Confirm that group spells don't count
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");

			Stream.of("spell_level_up", "spell_divine_strength").forEach(cardId -> {
				playCardWithTarget(context, player, cardId, silverHand1);
			});
			Minion lynessaSunsorrow = playMinionCard(context, player, "minion_lynessa_sunsorrow");
			Assert.assertEquals(lynessaSunsorrow.getAttack(), 1 + 1);
			Assert.assertEquals(lynessaSunsorrow.getHp(), 1 + 2);
			Assert.assertFalse(lynessaSunsorrow.hasAttribute(Attribute.TAUNT));
		});

		// Confirm that shadowstep will pull lynessa off the board
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");
			Minion silverHand2 = playMinionCard(context, player, "token_silver_hand_recruit");
			Stream.of("spell_divine_strength").forEach(cardId -> {
				playCardWithTarget(context, player, cardId, silverHand1);
			});

			Stream.of("spell_shadowstep").forEach(cardId -> {
				playCardWithTarget(context, player, cardId, silverHand2);
			});
			playCard(context, player, "minion_lynessa_sunsorrow");
			// Note the silver hand recruit is in index 0 in the hand
			Assert.assertEquals(costOf(context, player, player.getHand().get(1)),
					CardCatalogue.getCardById("minion_lynessa_sunsorrow").getBaseManaCost() - 2);
		});

		// Confirm that hero powers don't count
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");

			Stream.of("spell_divine_strength").forEach(cardId -> {
				playCardWithTarget(context, player, cardId, silverHand1);
			});

			GameAction heroPowerAction = player.getHeroPowerZone().get(0).play();
			heroPowerAction.setTarget(silverHand1);
			context.getLogic().performGameAction(player.getId(), heroPowerAction);
			context.endTurn();

			Minion lynessaSunsorrow = playMinionCard(context, player, "minion_lynessa_sunsorrow");
			Assert.assertEquals(lynessaSunsorrow.getAttack(), 1 + 1);
			Assert.assertEquals(lynessaSunsorrow.getHp(), 1 + 2);
		});
	}

	@Test
	public void testRhokdelar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_rhokdelar");
			Assert.assertEquals(player.getHand().size(), GameLogic.MAX_HAND_CARDS);
			Assert.assertEquals(player.getHand().stream()
					.filter(c -> c.getCardType() == CardType.SPELL)
					.filter(c -> c.getHeroClass() == HeroClass.GREEN)
					.count(), 10L);
		});
	}

	@Test
	public void testLeylineManipulator() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			final Card cardInDeck = CardCatalogue.getCardById("minion_bloodfen_raptor");
			cardInDeck.setAttribute(Attribute.STARTED_IN_DECK);
			context.getLogic().shuffleToDeck(player, cardInDeck);
			context.endTurn();
			final Card newCard = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(player.getId(), newCard);
			playCard(context, player, "minion_leyline_manipulator");
			Assert.assertEquals(costOf(context, player, cardInDeck), 2);
			Assert.assertEquals(costOf(context, player, newCard), 0);
		});
	}

	@Test
	public void testExplosiveRunes() {
		Stream.of(
				// less than 6 health, exactly 6 health, more than 6 health
				"minion_bloodfen_raptor", "minion_water_elemental", "minion_ironbark_protector",
				// divine shield + (less than 6 health, exactly 6 health, more than 6 health)
				"minion_argent_squire", "minion_tirion_fordring", "minion_force-tank_max"
		)
				.forEach((minionCardId) -> {
					runGym((context, player, opponent) -> {
						// Explosive Runes, Less than 6 health
						playCard(context, player, "secret_explosive_runes");
						context.endTurn();
						testExplosiveRuinsSituation(context, opponent, minionCardId, false);
					});
				});

		// Now play snipe first
		Stream.of(
				// less than 6 health, exactly 6 health, more than 6 health
				"minion_bloodfen_raptor", "minion_water_elemental", "minion_ironbark_protector",
				// divine shield + (less than 6 health, exactly 6 health, more than 6 health)
				"minion_argent_squire", "minion_tirion_fordring", "minion_force-tank_max"
		)
				.forEach((minionCardId) -> {
					runGym((context, player, opponent) -> {
						playCard(context, player, "secret_snipe");
						playCard(context, player, "secret_explosive_runes");
						context.endTurn();
						testExplosiveRuinsSituation(context, opponent, minionCardId, true);
					});
				});
	}

	private static void testExplosiveRuinsSituation(GameContext context, Player opponent, final String minionCardId, boolean playedSnipe) {
		final int opponentStartingHp = opponent.getHero().getHp();
		playCard(context, opponent, minionCardId);
		Minion minion = (Minion) context.getEntities().filter(c ->
				c instanceof Minion
						&& c.getSourceCard().getCardId().equals(minionCardId))
				.findFirst().orElseThrow(AssertionError::new);
		boolean isDivineShield = minion.getSourceCard().getAttributes().containsKey(Attribute.DIVINE_SHIELD);
		int baseHp = minion.getBaseHp() - (playedSnipe && !isDivineShield ? 4 : 0);
		if (playedSnipe && isDivineShield) {
			isDivineShield = false;
		}

		if (baseHp > 6) {
			if (isDivineShield) {
				Assert.assertEquals(minion.getHp(), baseHp);
			} else {
				Assert.assertEquals(minion.getHp(), Math.max(baseHp - 6, 0));
			}
			Assert.assertEquals(opponent.getHero().getHp(), opponentStartingHp);
		} else {
			if (isDivineShield) {
				Assert.assertEquals(minion.getHp(), baseHp);
			} else {
				Assert.assertTrue(minion.isDestroyed());
			}
			Assert.assertEquals(opponent.getHero().getHp(), opponentStartingHp - Math.min(6, 6 - baseHp), minionCardId);
		}
	}

	@Test
	public void testDragonsFury() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion waterElemental = playMinionCard(context, opponent, "minion_water_elemental");
			context.endTurn();
			// Cost 5 spell in deck
			shuffleToDeck(context, player, "spell_deck_of_wonders");
			playCard(context, player, "spell_dragons_fury");
			Assert.assertEquals(waterElemental.getHp(), 1);
		});
	}

	@Test
	public void testDragoncallerAlanna() {
		runGym((context, player, opponent) -> {
			// Does not count as a spell
			playCard(context, player, "minion_tortollan_primalist");
			// Does count as spells, but less than cost 5
			playCard(context, player, "spell_innervate");
			playCard(context, player, "spell_innervate");
			// More than cost 5
			playCard(context, player, "spell_flamestrike");
			playCard(context, player, "spell_flamestrike");
			playCard(context, player, "spell_flamestrike");
			playCard(context, player, "spell_flamestrike");
			playCard(context, player, "minion_dragoncaller_alanna");
			Assert.assertEquals(player.getMinions()
					.stream()
					.map(Entity::getSourceCard)
					.filter(c -> c.getCardId().equals("token_fire_dragon"))
					.count(), 4L);
		});
	}

	@Test
	public void testIronwoodGolem() {
		runGym((context, player, opponent) -> {
			Minion ironwoodGolem = playMinionCard(context, player, "minion_ironwood_golem");
			context.endTurn();
			context.endTurn();
			Assert.assertFalse(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_claw");
			Assert.assertFalse(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_claw");
			Assert.assertTrue(ironwoodGolem.canAttackThisTurn());
			context.endTurn();
			playCardWithTarget(context, opponent, "spell_fireball", player.getHero());
			context.endTurn();
			Assert.assertFalse(ironwoodGolem.canAttackThisTurn());
		});
	}

	@Test
	public void testJasperSpellstone() {
		// Spellstones should accumulate progress
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
		});

		// Losing three armor shouldn't trigger Jasper Spellstone
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_shield_block");
			receiveCard(context, player, "spell_lesser_jasper_spellstone");
			context.endTurn();
			Minion wolfrider = playMinionCard(context, player, "minion_wolfrider");
			attack(context, opponent, wolfrider, player.getHero());
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
		});
	}

	@Test
	public void testAstralTigerVersusMalorne() {
		runGym((context, player, opponent) -> {
			/*
			  Astral Tiger shuffles a COPY. If you play cards that trigger a deathrattle, this
			  will put a copy on to your deck without removing the current one.
			  (Malorne's says "Shuffle this minion in to your deck" so it literally puts that specific minion from the
			  board in to your deck)
			 */

			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_astral_tiger")).count(), 0L);
			Minion astralTiger = playMinionCard(context, player, "minion_astral_tiger");
			playCardWithTarget(context, player, "spell_play_dead", astralTiger);
			Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_astral_tiger");
			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_astral_tiger")).count(), 1L);
		});

		runGym((context, player, opponent) -> {
			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_malorne")).count(), 0L);
			Minion malorne = playMinionCard(context, player, "minion_malorne");
			playCardWithTarget(context, player, "spell_play_dead", malorne);
			Assert.assertEquals(player.getMinions().size(), 0);
			Assert.assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_malorne")).count(), 1L);
		});
	}
}
