package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.Stream;

public class KoboldsAndCatacombsTests extends TestBase {
	@Test
	public void testCallPetUnidentifiedElixirInteraction() {
		runGym((context, player, opponent) -> {
			context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("spell_unidentified_elixir"));
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
			context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("minion_bloodfen_raptor"));
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
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, card), 1);
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
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, player.getHand().get(1)),
					CardCatalogue.getCardById("minion_lynessa_sunsorrow").getBaseManaCost() - 2);
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
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cardInDeck), 2);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, newCard), 0);
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
			context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("spell_deck_of_wonders"));
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
		runGym((context, player, opponent) -> {
			/*
			Spellstones accumulate progress...
			 */
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_lesser_jasper_spellstone"));
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_lesser_jasper_spellstone");
			playCard(context, player, "spell_claw");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
			playCard(context, player, "spell_shield_block");
			Assert.assertEquals(player.getHand().get(0).getCardId(), "spell_greater_jasper_spellstone");
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
