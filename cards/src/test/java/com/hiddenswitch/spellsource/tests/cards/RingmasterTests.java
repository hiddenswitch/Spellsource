package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class RingmasterTests extends TestBase {

	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.CANDY;
	}

	@Test
	public void testDefaultSignature() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_circus_supplier");
			assertEquals(1, player.getHand().size());
			assertEquals(GameLogic.DEFAULT_SIGNATURE, player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testOpeningActor() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			Card signature = receiveCard(context, player, "spell_chain_dance");
			Card signature2 = receiveCard(context, player, "spell_chain_dance");
			Card nope = receiveCard(context, player, "spell_prestidigitation");
			assertEquals(3, costOf(context, player, signature));
			assertEquals(3, costOf(context, player, signature2));
			assertEquals(3, costOf(context, player, nope));
			Minion actor = playMinionCard(context, player, "minion_opening_actor");
			assertEquals(2, costOf(context, player, signature));
			assertEquals(2, costOf(context, player, signature2));
			assertEquals(3, costOf(context, player, nope));
			playCard(context, player, signature, actor);
			context.getLogic().endOfSequence();
			assertEquals(3, costOf(context, player, signature2));
			assertEquals(3, costOf(context, player, nope));
		});
	}

	@Test
	public void testBunglingBusker() {
		runGym((context, player, opponent) -> {
			Minion busker = playMinionCard(context, player, "minion_bungling_busker");
			Minion bad = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, player, "spell_prestidigitation", bad);
			assertEquals(bad.getHp(), bad.getMaxHp() - 4);
			assertEquals(busker.getHp(), busker.getMaxHp() - 4);
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testRoadTrip() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_road_trip");
			player.getHero().setHp(1);
			playCard(context, player, "spell_road_trip", player.getHero());
			assertEquals(player.getHero().getHp(), 1 + 4);
			playCard(context, player, "spell_road_trip", player.getHero());
			assertEquals(player.getHero().getHp(), 1 + 4 + 8);
			playCard(context, player, "spell_initial_act");
			assertEquals(player.getHero().getHp(), 1 + 4 + 8);
		});
	}

	@Test
	public void testSavageDancer() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_meteor_spin");
			Minion leftLeft = playMinionCard(context, opponent, "minion_neutral_test");
			Minion left = playMinionCard(context, opponent, "minion_neutral_test");
			Minion middle = playMinionCard(context, opponent, "minion_neutral_test");
			Minion right = playMinionCard(context, opponent, "minion_neutral_test");
			Minion rightRight = playMinionCard(context, opponent, "minion_neutral_test");

			playCard(context, player, "minion_savage_dancer");
			playCard(context, player, "spell_show_planning");
			playCard(context, player, player.getHand().get(0), middle);

			assertEquals(leftLeft.getHp(), leftLeft.getMaxHp());
			assertEquals(left.getHp(), left.getMaxHp() - 6);
			assertEquals(middle.getHp(), leftLeft.getMaxHp() - 6);
			assertEquals(right.getHp(), right.getMaxHp() - 6);
			assertEquals(rightRight.getHp(), rightRight.getMaxHp());

			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 6);

			context.getLogic().drawCard(player.getId(), null);

			playCard(context, player, player.getHand().get(0), leftLeft);
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 6);
		});
	}

	@Test
	public void testGreatShowman() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			assertEquals(player.getDeck().size(), 10);
			playCard(context, player, "minion_great_showman");
			assertEquals(player.getDeck().size(), 9);
			assertEquals(player.getHand().size(), 1);

			shuffleToDeck(context, player, "spell_chain_dance");
			playCard(context, player, "minion_great_showman");

			assertEquals(player.getDeck().size(), 9);
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getHand().get(1).getCardId(), "spell_chain_dance");
		});
	}

	@Test
	public void testFiredancer() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			Minion leftLeft = playMinionCard(context, opponent, "minion_neutral_test");
			Minion left = playMinionCard(context, opponent, "minion_neutral_test");
			Minion middle = playMinionCard(context, opponent, "minion_neutral_test");
			Minion right = playMinionCard(context, opponent, "minion_neutral_test");
			Minion rightRight = playMinionCard(context, opponent, "minion_neutral_test");

			playCard(context, player, "minion_firedancer");
			playCard(context, player, "spell_show_planning");
			playCard(context, player, player.getHand().get(0), middle);

			assertEquals(leftLeft.getHp(), leftLeft.getMaxHp() - 1);
			assertEquals(left.getHp(), left.getMaxHp() - 4);
			assertEquals(middle.getHp(), leftLeft.getMaxHp() - 5);
			assertEquals(right.getHp(), right.getMaxHp() - 4);
			assertEquals(rightRight.getHp(), rightRight.getMaxHp() - 1);

			context.getLogic().drawCard(player.getId(), null);

			playCard(context, player, player.getHand().get(0), leftLeft);
			assertEquals(leftLeft.getHp(), leftLeft.getMaxHp() - 4);
			assertEquals(rightRight.getHp(), rightRight.getMaxHp() - 2);
		});
	}

	@Test
	public void testGazalTheGlorious() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_jaunty_tune");
			playCard(context, player, "minion_gazal_the_glorious");
			playCard(context, player, "spell_jaunty_tune");

			for (int i = 0; i < 4; i++) {
				assertEquals(player.getMinions().get(i).getHp(), player.getMinions().get(i).getBaseHp() + 2);
			}

			playCard(context, player, "spell_jaunty_tune");

			for (int i = 0; i < 4; i++) {
				assertEquals(player.getMinions().get(i).getHp(), player.getMinions().get(i).getBaseHp() + 2);
			}
			for (int i = 4; i < 7; i++) {
				assertEquals(player.getMinions().get(i).getHp(), player.getMinions().get(i).getBaseHp());
			}
		});
	}

	@Test
	public void testTalentScout() {
		runGym((context, player, opponent) -> {
			Minion mine = playMinionCard(context, player, "minion_neutral_test_1");
			Minion yours = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "minion_talent_scout", mine);
			playCard(context, player, "minion_talent_scout", yours);
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(player.getMinions().size(), 5);
		});
	}

	@Test
	public void testFrontRowFun() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_front-row_fun");
			Minion guy = playMinionCard(context, player, "minion_neutral_test");
			Minion gal = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_front-row_fun", gal);
			assertEquals(guy.getHp(), guy.getBaseHp());
			assertEquals(gal.getHp(), gal.getBaseHp() + 4);
			playCard(context, player, "spell_front-row_fun", gal);
			assertEquals(guy.getHp(), guy.getBaseHp() + 4);
			assertEquals(gal.getHp(), gal.getBaseHp() + 4 + 4);
		});
	}

	@Test
	public void testHandsomeHandler() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_front-row_fun");
			shuffleToDeck(context, player, "spell_chain_dance");
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "spell_front-row_fun");
				playMinionCard(context, player, "minion_neutral_test");
			}
			shuffleToDeck(context, player, "spell_chain_dance");

			playMinionCard(context, player, "minion_handsome_handler");

			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getHand().size(), 5);

			for (Card card : player.getHand()) {
				assertEquals(card.getCardId(), "spell_front-row_fun");
			}
			for (Card card : player.getDeck()) {
				assertEquals(card.getCardId(), "spell_chain_dance");
			}
		});
	}

	@Test
	public void testAperration() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_front-row_fun");
			receiveCard(context, player, "spell_chain_dance");
			for (int i = 0; i < 5; i++) {
				receiveCard(context, player, "minion_neutral_test");
			}
			playCard(context, player, "minion_aperration");

			boolean chain = false;
			boolean fun = false;

			for (Card card : player.getHand()) {
				if (card.getCardId().equals("spell_chain_dance")) chain = true;
				if (card.getCardId().equals("spell_front-row_fun")) fun = true;
			}

			assertTrue(chain);
			assertTrue(fun);
		});
	}

	@Test
	public void testSpiritbladeDancer() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(1);
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			Minion sbd = playMinionCard(context, player, "minion_spiritblade_dancer");
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			destroy(context, sbd);
			playCard(context, player, "spell_chain_dance", enemy);
			assertEquals(player.getHero().getHp(), 1 + 3);
		});
	}

	@Test
	public void testFreakshowMutant() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_freakshow_mutant");
			playCard(context, player, "spell_test_deal_10", opponent.getHero());
			assertEquals(10, player.getMinions().get(0).getSourceCard().getBaseManaCost());
		});
	}

	@Test
	public void testSwordSwallower() {
		runGym((context, player, opponent) -> {
			Minion left = playMinionCard(context, player, "minion_neutral_test");
			Minion swordSwallower = playMinionCard(context, player, "minion_sword_swallower");
			Minion right = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, swordSwallower);
			assertEquals(left.getAttack(), left.getBaseAttack() + 3);
			assertEquals(right.getAttack(), right.getBaseAttack() + 3);
		});
	}

	@Test
	public void testVaudevilleHook() {
		runGym((context, player, opponent) -> {
			Minion enemy = playMinionCard(context, opponent, "minion_test_deathrattle_2");
			playCard(context, player, "spell_vaudeville_hook", enemy);
			assertEquals(player.getHand().size(), 1);
			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card hook = receiveCard(context, player, "spell_vaudeville_hook");
			assertEquals(12, costOf(context, player, hook));
			for (int i = 0; i < 5; i++) {
				receiveCard(context, player, "spell_lunstone");
				assertEquals(11 - i, costOf(context, player, hook));
			}
		});
	}

	@Test
	public void testCircusSupplier() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			playCard(context, player, "minion_circus_supplier");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_chain_dance");
		});
	}

	@Test
	@Disabled("spotty with error expected: <spell_unidentified_mushroom> but was: <spell_toxic_mushroom>")
	public void testElenaDreamhaze() {
		for (int i = 0; i < 20; i++) {
			runGym((context, player, opponent) -> {
				context.setDeckFormat(DeckFormat.ALL);
				playCard(context, player, "minion_elena_dreamhaze");
				assertNotNull(player.getAttribute(Attribute.SIGNATURE));
				assertNotEquals(player.getAttribute(Attribute.SIGNATURE), "");
				playCard(context, player, "minion_circus_supplier");
				assertEquals(player.getHand().size(), 2);
				assertEquals(player.getAttribute(Attribute.SIGNATURE), player.getHand().get(0).getCardId());
				assertEquals(player.getAttribute(Attribute.SIGNATURE), player.getHand().get(1).getCardId());
			});
		}
	}

	@Test
	public void testCenterStageScreamer() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_vaudeville_hook");
			Minion screamer = playMinionCard(context, player, "minion_center-stage_screamer");
			assertEquals(screamer.getAttack(), 1 + 12);
		});
	}

	@Test
	public void testSleightOfHands() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "spell_sleight_of_hands");
			}
			playCard(context, player, "spell_sleight_of_hands");
			assertEquals(player.getHand().size(), 9);
			assertEquals(player.getDeck().size(), 21);
		});
	}

	@Test
	public void testMaryDeMasque() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			Minion big = playMinionCard(context, opponent, "minion_neutral_test_big");
			playCard(context, player, "minion_mary_demasque", big);
			assertEquals(player.getMinions().size(), 1);
			receiveCard(context, player, "spell_chain_dance");
			playCard(context, player, "minion_mary_demasque", big);
			assertEquals(player.getMinions().size(), 2);
			receiveCard(context, player, "spell_chain_dance");
			playCard(context, player, "minion_mary_demasque", big);
			assertEquals(player.getMinions().size(), 3);
			receiveCard(context, player, "spell_chain_dance");
			playCard(context, player, "minion_mary_demasque", big);
			assertEquals(player.getMinions().size(), 4);
			receiveCard(context, player, "spell_chain_dance");
			playCard(context, player, "minion_mary_demasque", big);
			assertEquals(player.getMinions().size(), 6);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testMisc() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_assistant_tumbler");
		});
	}

	@Test
	public void testRingsideImpresario() {
		runGym((context, player, opponent) -> {
			player.setAttribute(Attribute.SIGNATURE, "spell_chain_dance");
			playCard(context, player, "minion_ringside_impresario");
			assertEquals(0, player.getHand().size());
			playCard(context, player, "minion_ringside_impresario");
			assertEquals(1, player.getDeck().size());
		});
	}

	@Test
	public void testFinaleArchitect() {
		runGym((context, player, opponent) -> {
			Minion arc = playMinionCard(context, player, "minion_finale_architect");
			Card dance = receiveCard(context, player, "spell_chain_dance");
			assertEquals(2, costOf(context, player, dance));
			playCard(context, player, dance, arc);
			assertFalse(arc.isDestroyed());
			context.endTurn();
			assertFalse(arc.isDestroyed());
			context.endTurn();
			assertTrue(arc.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			Minion arc = playMinionCard(context, player, "minion_finale_architect");
			Card star = receiveCard(context, player, "spell_star_performance");
			assertEquals(1, costOf(context, player, star));
			playCard(context, player, star, arc);
			assertEquals(arc.getBaseAttack(), arc.getAttack());
			context.endTurn();
			destroy(context, arc);
			Minion surprise = playMinionCard(context, opponent, "minion_assistant_tumbler");
			context.endTurn();
			assertEquals(surprise.getBaseAttack() + 3, surprise.getAttack());
		});
	}

}
