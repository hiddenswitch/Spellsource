package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
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
