package com.hiddenswitch.spellsource.tests.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.tests.util.DebugContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class KoboldsAndCatacombsTests extends TestBase {

	@Test
	public void testLesserPearlSpellstone() {
		runGym((context, player, opponent) -> {
			Card spellstone = receiveCard(context, player, "spell_lesser_pearl_spellstone");
			opponent.getHero().setHp(27);
			playCard(context, player, "spell_healing_touch", opponent.getHero());
			assertEquals(spellstone.transformResolved(context).getSourceCard().getCardId(), "spell_pearl_spellstone");
		});
	}

	@Test
	public void testPsychicScreamTwilightsCallInteraction() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion deathrattleMinion = playMinionCard(context, opponent, "minion_loot_hoarder");
			context.endTurn();
			playCard(context, player, "spell_psychic_scream");
			// Give the opponent something to draw on the start of their next turn
			putOnTopOfDeck(context, opponent, "spell_the_coin");
			assertEquals(opponent.getDeck().get(0).getCardId(), deathrattleMinion.getSourceCard().getCardId(), "Deathrattle Minion should be at the bottom of the opponent's deck");
			context.endTurn();
			playCard(context, opponent, "spell_twilights_call");
			assertEquals(opponent.getMinions().size(), 0, "Deathrattle Minion should not have been summoned");
			assertEquals(player.getDeck().size(), 0);
			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getDeck().get(0).getCardId(), deathrattleMinion.getSourceCard().getCardId());
			assertEquals(opponent.getHand().size(), 1);
			assertEquals(opponent.getHand().get(0).getCardId(), "spell_the_coin");
		});
	}

	@Test
	public void testLeylineManipulatorSimulacrumInteraction() {
		runGym((context, player, opponent) -> {
			Card raptor = shuffleToDeck(context, player, "minion_bloodfen_raptor");
			raptor.getAttributes().put(Attribute.STARTED_IN_DECK, true);
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_simulacrum");
			playCard(context, player, "minion_leyline_manipulator");
			assertEquals(costOf(context, player, player.getHand().get(0)), raptor.getBaseManaCost());
			assertEquals(costOf(context, player, player.getHand().get(1)), raptor.getBaseManaCost() - 2);
		});
	}

	@Test
	public void testBladedGauntlet() {
		runGym((context, player, opponent) -> {
			// Prevent fatigue damage
			shuffleToDeck(context, player, "spell_the_coin");
			playCard(context, player, "weapon_bladed_gauntlet");
			Weapon weapon = player.getHero().getWeapon();
			assertEquals(weapon.getAttack(), 0);
			useHeroPower(context, player);
			assertEquals(player.getHero().getArmor(), 2);
			assertEquals(weapon.getAttack(), 2);
			context.endTurn();
			Minion bloodfenRaptor = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			assertEquals(weapon.getAttack(), 2);
			attack(context, player, player.getHero(), bloodfenRaptor);
			assertTrue(bloodfenRaptor.isDestroyed());
			assertEquals(player.getHero().getArmor(), 0);
			assertEquals(weapon.getAttack(), 0);
		}, "RED", "RED");
	}

	@Test
	public void testTwigOfTheWorldTreeRestoreMana() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_twig_of_the_world_tree");
			playCard(context, player, "weapon_twig_of_the_world_tree");
			assertEquals(player.getMana(), 10);
			assertEquals(player.getMaxMana(), 10);
			player.setMana(4);
			playCard(context, player, "weapon_twig_of_the_world_tree");
			assertEquals(player.getMana(), 10);
		});
	}

	@Test
	public void testSpellstoneKeepsManaCostChange() {
		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "spell_amethyst_spellstone");
			playMinionCard(context, player, "minion_emperor_thaurissan");
			context.endTurn();
			context.endTurn();
			assertEquals(costOf(context, player, card), card.getBaseManaCost() - 1);
			playCard(context, player, "spell_fireball", player.getHero());
			card = (Card) card.transformResolved(context);
			assertEquals(card.getCardId(), "spell_greater_amethyst_spellstone");
			assertEquals(costOf(context, player, card), card.getBaseManaCost() - 1);
		});
	}

	@Test
	public void testRummagingKobold() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_wicked_knife");
			// Wicked Knife is now destroyed
			playCard(context, player, "weapon_ashbringer");
			playCard(context, player, "minion_rummaging_kobold");
			assertEquals(player.getHand().get(0).getCardId(), "weapon_wicked_knife");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_wicked_knife");
			playCard(context, player, "minion_rummaging_kobold");
			assertEquals(player.getHand().size(), 0, "Wicked Knife was never destroyed");
		});
	}

	@Test
	public void testScrollOfWonderDoesNotTriggerFlamewaker() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("spell_the_coin"));
			playMinionCard(context, player, "minion_flamewaker");
			shuffleToDeck(context, player, "spell_scroll_of_wonder");
			context.getLogic().drawCard(player.getId(), player);
			context.getLogic().endOfSequence();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp(), "Should not have triggered Flamewaker");
		});
	}

	@Test
	@Disabled("does not consistently produce errors")
	public void testThirtyScrollsOfWonder() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "spell_scroll_of_wonder");
			}
			context.endTurn();
			int startingRemoved = player.getRemovedFromPlay().size();
			// A spell might be cast that shuffles a card into the player's deck, therefore interrupting this process.
			context.endTurn();
			assertTrue(player.getRemovedFromPlay().size() >= startingRemoved + 30);
		});
	}

	@Test
	public void testIxlidFungalLord() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_ixlid_fungal_lord");
			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(player.getMinions()
					.stream()
					.filter(c -> c.getSourceCard().getCardId().equals("minion_bloodfen_raptor"))
					.count(), 2, "There should be two copies of the Bloodfen Raptor.");
		});
	}

	@Test
	public void testGeosculptorYip() {
		for (int j = 0; j <= 20; j++) {
			final int i = j;
			final int expectedCost = Math.min(10, i);
			runGym((context, player, opponent) -> {
				context.setDeckFormat(new DeckFormat().withCardSets("BASIC", "CLASSIC"));
				context.getLogic().gainArmor(player, i);
				playCard(context, player, "minion_geosculptor_yip");
				context.endTurn();
				int expectedMinions = 2;
				expectedMinions += player.getMinions().stream().filter(c -> c.getSourceCard().getCardId().equals("minion_drakkari_enchanter")).count();
				assertEquals(player.getMinions().size(), expectedMinions);
				assertEquals(player.getMinions().get(1).getSourceCard().getBaseManaCost(), expectedCost);
			});
		}

	}

	@Test
	public void testDiamondSpellstone() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_fireball", bloodfen);
			playCard(context, player, "spell_diamond_spellstone");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_bloodfen_raptor");
		});

		/*
		  Diamond Spellstone will only resurrect a distinct minion, meaning if you have multiple versions of the same
		  minion die, you'll only get one copy of it back. For example, if you have multiple dead Saronite Chain Gangs,
		  it will only resurrect one of them. This also means that minions that summon separate token minions instead
		  of copies, such as Doppelgangster or Big-Time Racketeer will function as you expect with the spellstone,
		  being capable of summoning the base minion as well as its generated minions.

		  Saronite no longer qualifies as a minion that copies itself. We'll use Doubling Imp instead.
		 */
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doubling_imp");
			playCard(context, player, "spell_fireball", player.getMinions().get(1));
			playCard(context, player, "spell_fireball", player.getMinions().get(0));
			playCard(context, player, "spell_diamond_spellstone");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_doubling_imp");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doppelgangster");
			playCard(context, player, "spell_fireball", player.getMinions().get(2));
			playCard(context, player, "spell_fireball", player.getMinions().get(1));
			playCard(context, player, "spell_fireball", player.getMinions().get(0));
			playCard(context, player, "spell_diamond_spellstone");
			assertEquals(player.getMinions().size(), 3);
			assertTrue(player.getMinions().stream().allMatch(m -> m.getSourceCard().getCardId().equals("minion_doppelgangster")));
		});
	}

	@Test
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
			playCard(context, opponent, "spell_fireball", bloodfen);
			assertEquals(costOf(context, player, player.getHand().get(0)), bloodfen.getSourceCard().getBaseManaCost() - 2);
		});
	}

	@Test
	public void testGoldenKobold() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_bloodfen_raptor");
			receiveCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "token_golden_kobold");
			assertEquals(player.getHand().stream().filter(c -> c.getRarity() == Rarity.LEGENDARY
					&& c.getCardType() == CardType.MINION).count(), 3);
		});
	}

	@Test
	public void testUnidentifiedElixirStartsInHand() {
		DebugContext context = createContext("WHITE", "WHITE", false, DeckFormat.getFormat("Standard"));
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck ->
				Stream.generate(() -> "spell_unidentified_elixir")
						.map(CardCatalogue::getCardById)
						.limit(30)
						.forEach(deck::addCard));
		context.init();
		assertTrue(context.getPlayers().stream().flatMap(p -> p.getHand().stream()).noneMatch(c -> c.getCardId().equals("spell_unidentified_elixir")));
	}


	@Test
	public void testAmethystSpellstone() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_amethyst_spellstone");
			playCard(context, player, "minion_flame_imp");
			assertEquals(player.getHand().get(0).getCardId(), "spell_amethyst_spellstone");
		}, "VIOLET", "VIOLET");

		// Amethyst should not trigger on warlock hero power
		// Also tests that amethyst doesn't trigger on fatigue
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_amethyst_spellstone");
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play());
			assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_amethyst_spellstone");
		}, "VIOLET", "VIOLET");
	}

	@Test
	public void testBranchingPaths() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_mirror_image");
			shuffleToDeck(context, player, "spell_mirror_image");
			overrideDiscover(context, player, discoveries -> discoveries.stream().filter(c -> c.getCard().getName().equals("Eat the Mushroom")).findFirst().orElseThrow(AssertionError::new));
			playCard(context, player, "spell_branching_paths");
			assertEquals(player.getHand().stream().filter(c -> c.getCardId().equals("spell_mirror_image")).count(), 2L, "Should have drawn cards twice.");
		});
	}

	@Test
	public void testTheDarkness() {
		final String regularDescription = "Starts dormant. Battlecry: Shuffle 3 Candles into the enemy deck. When drawn, this awakens.";
		final String permanentDescription = "When your opponent draws 3 Candles, this awakens!";
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			assertTrue(theDarkness.hasAttribute(Attribute.PERMANENT), "Comes into play permanent.");
			assertEquals(theDarkness.getDescription(context, player), permanentDescription, "Should have different description.");
			// Note that the opponent is going to draw three cards next turn, so let's remove one
			context.getLogic().removeCard(opponent.getDeck().get(0));
			context.endTurn();
			context.endTurn();
			assertFalse(theDarkness.canAttackThisTurn());
			assertFalse(context.getValidActions().stream().anyMatch(
					ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK
							&& ga.getSourceReference().equals(theDarkness.getReference())
			));
		});

		// Dirty rat play
		// When The Darkness is summoned by any means other than being played, it will start dormant but no Darkness
		// Candles are generated. When copied while on the board as a minion, the copy will not start dormant.
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_the_darkness");
			context.endTurn();
			playCard(context, opponent, "minion_dirty_rat");
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.PERMANENT), "Comes into play permanent.");
		});

		// Test that drawing candles removes the permanent attribute
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 3L);
			context.endTurn();
			// Opponent's turn, opponent will draw three candles immediately in one turn
			context.endTurn();

			assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 0L);
			assertFalse(theDarkness.hasAttribute(Attribute.PERMANENT));
			assertEquals(theDarkness.getDescription(context, player), regularDescription, "Should have different description.");
			assertTrue(theDarkness.canAttackThisTurn());
		});

		// Test that milling a candle does not trigger The Darkness
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 3L);
			context.getLogic().receiveCard(opponent.getId(), CardCatalogue.getCardById("spell_mirror_image"), 10);
			for (int i = 0; i < 3; i++) {
				context.endTurn();
				// Opponent's turn
				context.endTurn();
			}
			assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 0L);
			assertTrue(theDarkness.hasAttribute(Attribute.PERMANENT));
			assertEquals(theDarkness.getDescription(context, player), permanentDescription, "Should have different description.");
		});

		// When copied while on the board as a minion, the copy will not start dormant
		// Test that drawing candles removes the permanent attribute
		runGym((context, player, opponent) -> {
			Minion theDarkness = playMinionCard(context, player, "minion_the_darkness");
			assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 3L);
			context.endTurn();
			// Opponent's turn
			context.endTurn();

			assertEquals(opponent.getDeck().stream().filter(c -> c.getCardId().equals("spell_candle")).count(), 0L);
			assertFalse(theDarkness.hasAttribute(Attribute.PERMANENT));
			assertEquals(theDarkness.getDescription(context, player), regularDescription, "Should have different description.");
			Minion faceless = (Minion) playMinionCard(context, player, "minion_faceless_manipulator").transformResolved(context);
			assertEquals(faceless.getSourceCard().getCardId(), "minion_the_darkness");
			assertFalse(faceless.hasAttribute(Attribute.PERMANENT));
		});
	}

	@Test
	public void testKoboldMonk() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			receiveCard(context, opponent, "spell_fireball");
			opponent.setMaxMana(4);
			opponent.setMana(4);
			assertTrue(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			context.endTurn();
			Minion kobold = playMinionCard(context, player, "minion_kobold_monk");
			context.endTurn();
			opponent.setMaxMana(4);
			opponent.setMana(4);
			assertFalse(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
			playCard(context, opponent, "spell_fireball", kobold);
			opponent.setMaxMana(4);
			opponent.setMana(4);
			assertTrue(context.getValidActions().stream().anyMatch(a -> a.getTargetReference() != null && a.getTargetReference().equals(player.getHero().getReference())));
		});
	}

	@Test
	public void testKingTogwaggle() {
		runGym((context, player, opponent) -> {
			Card card1a = CardCatalogue.getCardById("spell_mirror_image");
			Card card1b = CardCatalogue.getCardById("spell_fireball");
			Card card2a = CardCatalogue.getCardById("minion_bloodfen_raptor");
			Card card2b = CardCatalogue.getCardById("minion_acidic_swamp_ooze");
			Stream.of(card1a, card1b).forEach(c -> context.getLogic().shuffleToDeck(player, c));
			Stream.of(card2a, card2b).forEach(c -> context.getLogic().shuffleToDeck(opponent, c));
			playCard(context, player, "minion_king_togwaggle");
			assertTrue(opponent.getDeck().containsAll(Arrays.asList(card1a, card1b)));
			assertTrue(opponent.getHand().containsCard("spell_ransom"));
			assertTrue(player.getDeck().containsAll(Arrays.asList(card2a, card2b)));
			// Put a coin on the top of the opponent's deck so that they don't draw one of the shuffled-in cards
			putOnTopOfDeck(context, opponent, "spell_the_coin");
			context.endTurn();
			Card ransomCard = opponent.getHand().filtered(c -> c.getCardId().equals("spell_ransom")).get(0);
			playCard(context, opponent, ransomCard);
			assertTrue(opponent.getDeck().containsAll(Arrays.asList(card2a, card2b)));
			assertTrue(player.getDeck().containsAll(Arrays.asList(card1a, card1b)));
		});
	}

	@Test
	public void testGrandArchivist() {
		runGym((context, player, opponent) -> {
			final Card card = CardCatalogue.getCardById("spell_mirror_image");
			context.getLogic().shuffleToDeck(player, card);
			assertEquals(player.getDeck().size(), 1);
			playCard(context, player, "minion_grand_archivist");
			context.endTurn();
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_mirror_image");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_mirror_image");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "secret_counterspell");
			playCard(context, player, "minion_grand_archivist");
			context.endTurn();
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_counterspell");
			context.endTurn();
			shuffleToDeck(context, player, "secret_counterspell");
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testEbonDragonsmith() {
		runGym((context, player, opponent) -> {
			final Card card = CardCatalogue.getCardById("weapon_arcanite_reaper");
			context.getLogic().receiveCard(player.getId(), card);
			int initialCost = costOf(context, player, card);
			playCard(context, player, "minion_ebon_dragonsmith");
			assertEquals(costOf(context, player, card), initialCost - 2);
		});
	}

	@Test
	public void testArcaneTyrant() {
		runGym((context, player, opponent) -> {
			final Card card = CardCatalogue.getCardById("minion_arcane_tyrant");
			context.getLogic().receiveCard(player.getId(), card);
			assertEquals(costOf(context, player, card), 5);
			context.endTurn();
			playCard(context, opponent, "spell_mirror_image");
			assertEquals(costOf(context, player, card), 5);
			playCard(context, opponent, "spell_doom");
			assertEquals(costOf(context, player, card), 5);
			context.endTurn();
			playCard(context, player, "spell_doom");
			assertEquals(costOf(context, player, card), 0);
			context.endTurn();
			assertEquals(costOf(context, player, card), 5);
			context.endTurn();
			playCard(context, player, "spell_mirror_image");
			assertEquals(costOf(context, player, card), 5);
			playCard(context, player, "spell_doom");
			assertEquals(costOf(context, player, card), 0);
		});
	}

	@Test
	public void testArcaneTyrantChooseOneInteraction() {
		runGym((context, player, opponent) -> {
			final Card card = receiveCard(context, player, "minion_arcane_tyrant");
			playChooseOneCard(context, player, "spell_nourish", "spell_nourish_1");
			assertEquals(costOf(context, player, card), 0);
		});
	}

	@Test
	public void testKoboldBarbarian() {
		runGym((context, player, opponent) -> {
			Minion kobold = playMinionCard(context, player, "minion_kobold_barbarian");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponentHp - kobold.getAttack());
			assertFalse(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK));
		});
	}

	@Test
	public void testUnstableEvolution() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new DeckFormat().withCardSets("BASIC"));
			Minion minion = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_unstable_evolution", minion);
			assertEquals(player.getHand().size(), 1);
			for (int i = 0; i < 5; i++) {
				playCard(context, player, player.getHand().get(0), minion.transformResolved(context));
			}
			assertEquals(minion.transformResolved(context).getSourceCard().getBaseManaCost(), 8);
			context.endTurn();
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testMurmuringElemental() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_murmuring_elemental");
			playCard(context, player, "minion_kobold_hermit");
			assertEquals(player.getMinions().size(), 4);
			playCard(context, player, "minion_kobold_hermit");
			assertEquals(player.getMinions().size(), 6);
		});
	}

	@Test
	public void testKoboldHermit() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_kobold_hermit");
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getRace(), "TOTEM");
		});
	}

	@Test
	public void testHealingRainLightwardenInteraction() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(15);
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			playCard(context, player, "spell_healing_rain");
			assertEquals(lightwarden.getAttack(), 1 + 2 * 12);
		});
	}

	@Test
	public void testCollectibilityOfSpellstoneCards() {
		CardCatalogue.getRecords().values().forEach(ccr -> {
			if (ccr.getDesc().getName().contains("Spellstone")) {
				assertEquals(ccr.getDesc().isCollectible(), ccr.getDesc().getName().contains("Lesser"), "Invalid collectibility for spellstone " + ccr.getDesc().getName());
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
			assertTrue(player.getHero().getWeapon().hasAttribute(Attribute.POISONOUS));
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
			playCard(context, player, "spell_doomerang", bloodfen);
			assertTrue(bloodfen.isDestroyed(), "The raptor should be destroyed because the Kingsbane had poisonous.");
			playCard(context, player, player.getHand().get(0));
			assertTrue(player.getHero().getWeapon().hasAttribute(Attribute.POISONOUS));
		});
	}


	@Test
	public void testCallPetUnidentifiedElixirInteraction() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_unidentified_elixir");
			playCard(context, player, "spell_call_pet");
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testToMySide() {
		runGym((context, player, opponent) -> {
			// Test putting a spell in the deck
			shuffleToDeck(context, player, "spell_mirror_image");
			playCard(context, player, "spell_to_my_side");
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).distinct().count(), 2L);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "spell_to_my_side");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).distinct().count(), 1L);
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
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_sudden_betrayal");
		});

		// Has adjacent minions: does trigger!
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			context.endTurn();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, opponent, "minion_stonetusk_boar");
			attack(context, opponent, boar, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
		});

		// Has adjacent minions but attacks opponent hero: doesn't trigger!
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			context.endTurn();
			Minion boar = playMinionCard(context, opponent, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, opponent, "minion_stonetusk_boar");
			attack(context, opponent, boar, opponent.getHero());
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_sudden_betrayal");
		});

		// Friendly has adjacent minions and attacks opponent hero: doesn't trigger!
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_sudden_betrayal");
			Minion boar = playMinionCard(context, player, "minion_stonetusk_boar");
			Minion boar2 = playMinionCard(context, player, "minion_stonetusk_boar");
			attack(context, player, boar, opponent.getHero());
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_sudden_betrayal");
		});
	}

	@Test
	public void testSonyaShadowdancer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_sonya_shadowdancer");
			playCard(context, player, "spell_fireball", player.getMinions().get(0));
			final Card card = player.getHand().get(0);
			assertEquals(costOf(context, player, card), 1);
			player.setMaxMana(10);
			player.setMana(10);
			assertEquals(card.getBaseAttack(), 1);
			assertEquals(card.getBaseHp(), 1);
			context.performAction(player.getId(), card.play());
			assertEquals(player.getMana(), 9);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_bloodfen_raptor");
			assertEquals(player.getMinions().get(1).getHp(), 1);
			assertEquals(player.getMinions().get(1).getAttack(), 1);
		});
	}

	@Test
	public void testSonyaShadowdancerGadgetzanFerrymanInteraction() {
		runGym((context, player, opponent) -> {
			Minion bloodfen = playMinionCard(context, player, "minion_bloodfen_raptor");
			playCard(context, player, "minion_sonya_shadowdancer");
			playMinionCard(context, player, "minion_gadgetzan_ferryman", bloodfen);
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testTemporus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_temporus");
			assertEquals(context.getActivePlayerId(), player.getId());
			context.endTurn();
			assertEquals(context.getActivePlayerId(), opponent.getId());
			context.endTurn();
			assertEquals(context.getActivePlayerId(), opponent.getId());
			context.endTurn();
			assertEquals(context.getActivePlayerId(), player.getId());
			context.endTurn();
			assertEquals(context.getActivePlayerId(), player.getId());
			context.endTurn();
			assertEquals(context.getActivePlayerId(), opponent.getId());
			context.endTurn();
			assertEquals(context.getActivePlayerId(), player.getId());
		});
	}

	@Test
	public void testDragonSoul() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_dragon_soul");
			for (int i = 0; i < 3; i++) {
				assertEquals(player.getMinions().size(), 0);
				playCard(context, player, "spell_innervate");
			}
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_dragon_spirit");
			for (int i = 0; i < 3; i++) {
				assertEquals(player.getMinions().size(), 1);
				playCard(context, player, "spell_innervate");
			}
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_dragon_spirit");
			// Tick up the spell casting count just before the turn ends
			playCard(context, player, "spell_innervate");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_innervate");
			playCard(context, player, "spell_innervate");
			// An extra dragon should not have appeared
			assertEquals(player.getMinions().size(), 2);
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
			assertEquals(player.getHero().getWeaponZone().size(), 0);
			context.performAction(player.getId(), bloodfenCard.play());
			context.endTurn();
			playCard(context, player, "spell_assassinate", player.getMinions().get(0));
			assertEquals(player.getHero().getWeapon().getSourceCard().getCardId(), "weapon_valanyr");
		});
	}

	@Test
	public void testLynessaSunsorrow() {
		// Confirm that the opponent's spells don't count
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");
			context.endTurn();
			Stream.of("spell_divine_strength").forEach(cardId -> {
				playCard(context, opponent, cardId, silverHand1);
			});
			context.endTurn();
			Stream.of("spell_divine_strength", "spell_divine_strength").forEach(cardId -> {
				playCard(context, player, cardId, silverHand1);
			});
			Minion lynessaSunsorrow = playMinionCard(context, player, "minion_lynessa_sunsorrow");
			assertEquals(lynessaSunsorrow.getAttack(), 1 + 2);
			assertEquals(lynessaSunsorrow.getHp(), 1 + 4);
		});

		// Confirm that group spells don't count
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");

			Stream.of("spell_level_up", "spell_divine_strength").forEach(cardId -> {
				playCard(context, player, cardId, silverHand1);
			});
			Minion lynessaSunsorrow = playMinionCard(context, player, "minion_lynessa_sunsorrow");
			assertEquals(lynessaSunsorrow.getAttack(), 1 + 1);
			assertEquals(lynessaSunsorrow.getHp(), 1 + 2);
			assertFalse(lynessaSunsorrow.hasAttribute(Attribute.TAUNT));
		});

		// Confirm that shadowstep will pull lynessa off the board
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");
			Minion silverHand2 = playMinionCard(context, player, "token_silver_hand_recruit");
			Stream.of("spell_divine_strength").forEach(cardId -> {
				playCard(context, player, cardId, silverHand1);
			});

			Stream.of("spell_shadowstep").forEach(cardId -> {
				playCard(context, player, cardId, silverHand2);
			});
			playCard(context, player, "minion_lynessa_sunsorrow");
			// Note the silver hand recruit is in index 0 in the hand
			assertEquals(costOf(context, player, player.getHand().get(1)),
					CardCatalogue.getCardById("minion_lynessa_sunsorrow").getBaseManaCost() - 2);
		});

		// Confirm that hero powers don't count
		runGym((context, player, opponent) -> {
			Minion silverHand1 = playMinionCard(context, player, "token_silver_hand_recruit");

			Stream.of("spell_divine_strength").forEach(cardId -> {
				playCard(context, player, cardId, silverHand1);
			});

			GameAction heroPowerAction = player.getHeroPowerZone().get(0).play();
			heroPowerAction.setTarget(silverHand1);
			context.performAction(player.getId(), heroPowerAction);
			context.endTurn();

			Minion lynessaSunsorrow = playMinionCard(context, player, "minion_lynessa_sunsorrow");
			assertEquals(lynessaSunsorrow.getAttack(), 1 + 1);
			assertEquals(lynessaSunsorrow.getHp(), 1 + 2);
		});
	}

	@Test
	public void testRhokdelar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_rhokdelar");
			assertEquals(player.getHand().size(), GameLogic.MAX_HAND_CARDS);
			assertEquals(player.getHand().stream()
					.filter(c -> c.getCardType() == CardType.SPELL)
					.filter(c -> c.getHeroClass().equals("GREEN"))
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
			assertEquals(costOf(context, player, cardInDeck), 2);
			assertEquals(costOf(context, player, newCard), 0);
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
						testExplosiveRunesSituation(context, opponent, minionCardId, false);
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
						testExplosiveRunesSituation(context, opponent, minionCardId, true);
					});
				});
	}

	private static void testExplosiveRunesSituation(GameContext context, Player opponent, final String minionCardId, boolean playedSnipe) {
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
				assertEquals(minion.getHp(), baseHp);
			} else {
				assertEquals(minion.getHp(), Math.max(baseHp - 6, 0));
			}
			assertEquals(opponent.getHero().getHp(), opponentStartingHp);
		} else {
			if (isDivineShield) {
				assertEquals(minion.getHp(), baseHp);
			} else {
				assertTrue(minion.isDestroyed());
			}
			assertEquals(opponent.getHero().getHp(), opponentStartingHp - Math.min(6, 6 - baseHp), minionCardId);
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
			assertEquals(waterElemental.getHp(), 1);
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
			player.getMinions().forEach(m -> m.setAttribute(Attribute.DESTROYED));
			context.getLogic().endOfSequence();
			playCard(context, player, "minion_dragoncaller_alanna");
			assertEquals(player.getMinions()
					.stream()
					.map(Entity::getSourceCard)
					.filter(c -> c.getCardId().equals("token_fire_dragon"))
					.count(), 4L);
		});
	}

	@Test
	public void testOakenSummonsIronwoodGolemInteraction() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_ironwood_golem");
			playCard(context, player, "spell_oaken_summons");
			Minion ironwoodGolem = player.getMinions().get(0);
			assertFalse(ironwoodGolem.canAttackThisTurn());
			assertFalse(ironwoodGolem.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
			context.endTurn();
			context.endTurn();
			assertTrue(ironwoodGolem.canAttackThisTurn());
		});
	}

	@Test
	public void testIronwoodGolem() {
		runGym((context, player, opponent) -> {
			Minion ironwoodGolem = playMinionCard(context, player, "minion_ironwood_golem");
			context.endTurn();
			context.endTurn();
			assertFalse(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_claw");
			assertFalse(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_claw");
			assertTrue(ironwoodGolem.canAttackThisTurn());
			context.endTurn();
			playCard(context, opponent, "spell_fireball", player.getHero());
			context.endTurn();
			assertFalse(ironwoodGolem.canAttackThisTurn());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_oaken_summons");
			Minion ironwoodGolem = playMinionCard(context, player, "minion_ironwood_golem");
			context.endTurn();
			context.endTurn();
			assertTrue(ironwoodGolem.canAttackThisTurn());
			playCard(context, player, "spell_fireball", player.getHero());
			assertFalse(ironwoodGolem.canAttackThisTurn());
		});
	}

	@Test
	public void testJasperSpellstone() {
		// Spellstones should accumulate progress
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			assertEquals(player.getHand().get(0).getCardId(), "spell_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
		});

		// Losing three armor shouldn't trigger Jasper Spellstone
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_shield_block");
			receiveCard(context, player, "spell_lesser_jasper_spellstone");
			context.endTurn();
			Minion charger = playMinionCard(context, player, "minion_charge_test");
			attack(context, opponent, charger, player.getHero());
			assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
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

			assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_astral_tiger")).count(), 0L);
			Minion astralTiger = playMinionCard(context, player, "minion_astral_tiger");
			playCard(context, player, "spell_play_dead", astralTiger);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_astral_tiger");
			assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_astral_tiger")).count(), 1L);
		});

		runGym((context, player, opponent) -> {
			assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_malorne")).count(), 0L);
			Minion malorne = playMinionCard(context, player, "minion_malorne");
			playCard(context, player, "spell_play_dead", malorne);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getDeck().stream().map(Card::getCardId).filter(c -> c.equals("minion_malorne")).count(), 1L);
		});
	}

	@Test
	public void testArcaneArtificer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_arcane_artificer");
			playCard(context, player, "minion_sorcerers_apprentice");
			playCard(context, player, "minion_sorcerers_apprentice");
			playCard(context, player, "spell_pyroblast", opponent.getHero());
			assertEquals(player.getHero().getArmor(), 8);
		});

	}

	@Test
	public void testWanderingMonster() {
		runGym((context, player, opponent) -> {
			// Always summon a 3 3/3 so that there's nothing with a deathrattle that will mess things up
			context.setDeckFormat(new FixedCardsDeckFormat("minion_mind_control_tech"));
			playCard(context, player, "secret_wandering_monster");
			context.endTurn();
			Minion bloodfen = playMinionCard(context, opponent, "minion_bloodfen_raptor");
			context.endTurn();
			context.endTurn();
			int startingHp = player.getHero().getHp();
			attack(context, opponent, bloodfen, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHero().getHp(), startingHp);
			assertTrue((int) player.getAttributes().get(Attribute.MINIONS_SUMMONED_THIS_TURN) > 0);
		});
	}

	@Test
	public void testCallToArmsPintSized() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_pint-sized_summoner");
			Card lumi = receiveCard(context, player, "minion_faithful_lumi");
			playCard(context, player, "spell_call_to_arms");
			assertEquals(costOf(context, player, lumi), 0);
		});
	}
}
