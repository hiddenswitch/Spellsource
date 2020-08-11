package com.hiddenswitch.spellsource.tests.cards;

import co.paralleluniverse.common.util.Objects;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class OccultistTests extends TestBase {

	@Test
	public void testRevelation() {
		for (int j = 0; j < 10; j++) {
			final int k = j;
			runGym((context, player, opponent) -> {
				for (int i = 0; i < k; i++) {
					context.endTurn();
					context.endTurn();
				}

				Card card = receiveCard(context, player, "spell_lunstone");
				playCard(context, player, "spell_revelation");
				assertEquals(player.getMana(), 0);
				assertEquals(player.getMaxMana(), 0);
				assertEquals(costOf(context, player, card), 1);
				shuffleToDeck(context, player, "spell_lunstone");
				context.getLogic().drawCard(player.getId(), null);
				assertEquals(0, costOf(context, player, player.getHand().get(1)));
			});
		}

	}

	@Test
	public void testImperfectDuplicate() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			assertEquals(target.getSourceCard().getBaseManaCost(), 1);
			playCard(context, player, "spell_imperfect_duplicate", target);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getAttack(), 1);
			assertEquals(player.getMinions().get(1).getHp(), 1);
			assertNotEquals(player.getMinions().get(1).getSourceCard().getCardId(), CardCatalogue.getOneOneNeutralMinionCardId());
		});
	}

	@Test
	public void testUnearthedHorrorXitaluInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_xitalu");
			Minion target = playMinionCard(context, player, "minion_unearthed_horror");
			playCard(context, player, "spell_underwater_horrors", target);
			assertEquals(player.getDeck().size(), 3);
			for (Card card : player.getDeck()) {
				assertEquals(card.getBonusAttack(), 7);
				assertEquals(card.getBonusHp(), 7);
			}
		});
	}

	@Test
	public void testGhatanothoa() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "token_ghatanothoa");
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			String firstHp = player.getHeroPowerZone().get(0).getCardId();
			target.setMaxHp(5);
			target.setHp(3);
			player.setMana(GameLogic.MAX_MANA);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> Objects.equal(ga.getSourceReference(), player.getHeroPowerZone().get(0).getReference())));
			useHeroPower(context, player, target.getReference());
			assertTrue(target.getHp() == 1 || target.getHp() == 5);
			String secondHp = player.getHeroPowerZone().get(0).getCardId();
			assertNotEquals(firstHp, secondHp);
			player.setMana(GameLogic.MAX_MANA);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> Objects.equal(ga.getSourceReference(), player.getHeroPowerZone().get(0).getReference())));
		});
	}

	@Test
	public void testLostCitysGuardian() {
		for (int i = 0; i < 5; i++) {
			runGym((context, player, opponent) -> {
				shuffleToDeck(context, player, "minion_neutral_test_1");
				shuffleToDeck(context, player, "minion_neutral_test");
				shuffleToDeck(context, player, "minion_neutral_test_big");

				overrideDiscover(context, player, "minion_neutral_test");
				playCard(context, player, "spell_lost_city_champion");
				assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_neutral_test");
				assertTrue(player.getMinions().get(0).hasAttribute(Attribute.TAUNT));
			});
		}
	}

	@Test
	public void testStarSculptor() {
		runGym((context, player, opponent) -> {
			Minion test = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_star_sculptor", test);
			assertEquals(3, player.getMinions().size());
			assertFalse(test.hasAttribute(Attribute.TAUNT));
			assertTrue(player.getMinions().get(2).hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testCosmicApparitions() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context,player, discoverActions -> {
				for (DiscoverAction discoverAction : discoverActions) {
					assertEquals(CardType.MINION, discoverAction.getCard().getCardType());
				}
				return discoverActions.get(context.getLogic().random(discoverActions.size()));
			});
			playCard(context, player, "spell_cosmic_apparitions");
			assertEquals(1, player.getHand().size());
		});
	}

	@Test
	public void testYigsMastermind() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playCard(context, player, "spell_yig_mastermind");
			assertEquals(3, player.getMinions().size());
			assertEquals(2, player.getDeck().size());
		});
	}

	@Test
	public void testSunkenTerrorOtherwordlyTruth() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, opponent, "minion_neutral_test_1");
			shuffleToDeck(context, opponent, "minion_neutral_test");
			shuffleToDeck(context, opponent, "minion_neutral_test_big");

			playCard(context, player, "minion_sunken_terror");
			playCard(context, player, "spell_otherwordly_truth");
			assertEquals(opponent.getDeck().size(), 3);
			assertEquals(player.getDeck().size(), 3);
			assertEquals(player.getMinions().size(), 4);
			int totalAttack = player.getMinions().stream().mapToInt(Minion::getAttack).sum();
			assertEquals(5 + 2 + 1 + 20, totalAttack);
		});
	}

	@Test
	public void testCrazedCultlordPaven() {
		runGym((context, player, opponent) -> {
			Minion paven = playMinionCard(context, player, "minion_paven_elemental_of_surprise");
			playMinionCard(context, player, "minion_crazed_cultlord", paven);
			assertEquals(3, player.getMinions().size());
			int pavens = 0;
			int capturedPavens = 0;
			for (Minion minion : player.getMinions()) {
				if (minion.getSourceCard().getCardId().equals("permanent_paven_captured")) {
					capturedPavens++;
				}
				if (minion.getSourceCard().getCardId().equals("minion_paven_elemental_of_surprise")) {
					pavens++;
				}
			}
			assertEquals(1, pavens);
			assertEquals(1, capturedPavens);
		});
	}
  
	@Test
	public void testHeraldOfFate() {
		runGym((context, player, opponent) -> {
			Minion herald = playMinionCard(context, player, "minion_herald_of_fate");
			destroy(context, herald);
			playCard(context, player, "minion_test_untargeted_battlecry");
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			assertEquals(28, player.getHero().getHp());
			assertEquals(28, opponent.getHero().getHp());
			playCard(context, player, "minion_test_untargeted_battlecry");
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			assertEquals(27, player.getHero().getHp());
			assertEquals(27, opponent.getHero().getHp());
			playCard(context, player, "minion_test_untargeted_battlecry");
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			assertEquals(26, player.getHero().getHp());
			assertEquals(26, opponent.getHero().getHp());
		});

		runGym((context, player, opponent) -> {
			Minion herald = playMinionCard(context, player, "minion_herald_of_fate");
			destroy(context, herald);
			playCard(context, player, "minion_test_untargeted_battlecry");
			playCard(context, player, "minion_test_untargeted_battlecry");
			assertEquals(26, player.getHero().getHp());
			assertEquals(30, opponent.getHero().getHp());
			playCard(context, player, "minion_test_untargeted_battlecry");
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			assertEquals(25, player.getHero().getHp());
			assertEquals(29, opponent.getHero().getHp());
		});

		runGym((context, player, opponent) -> {
			Minion herald = playMinionCard(context, player, "minion_herald_of_fate");
			destroy(context, herald);
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			assertEquals(26, opponent.getHero().getHp());
			assertEquals(30, player.getHero().getHp());
			playCard(context, opponent, "minion_test_untargeted_battlecry");
			playCard(context, player, "minion_test_untargeted_battlecry");
			assertEquals(25, opponent.getHero().getHp());
			assertEquals(29, player.getHero().getHp());
		});
	}

	@Test
	public void testRelicsOfDeities() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_test_deal_10");
			playCard(context, player, "spell_relics_of_deities");
			assertEquals(1, player.getHand().size());
			assertNotEquals("spell_test_deal_10", player.getHand().get(0).getCardId());
		});
	}

	@Test
	public void testPrimordialMiner() {
		runGym((context, player, opponent) -> {
			Minion miner = playMinionCard(context, player, "minion_primordial_miner");
			assertEquals("Aftermath: Receive 1 random Artifact. (Increases for each copy of this in your Graveyard)", miner.getDescription(context, player));
			destroy(context, miner);
			assertEquals(1, player.getHand().size());

			miner = playMinionCard(context, player, "minion_primordial_miner");
			assertEquals("Aftermath: Receive 2 random Artifacts. (Increases for each copy of this in your Graveyard)", miner.getDescription(context, player));
			destroy(context, miner);
			assertEquals(3, player.getHand().size());
		});
	}

	@Test
	public void testVolatileWisdom() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			receiveCard(context, opponent, "spell_lunstone");
			shuffleToDeck(context, opponent, "spell_lunstone");
			playCard(context, player, "spell_volatile_wisdom");
			for (Card card : player.getHand()) {
				assertEquals("spell_lunstone", card.getCardId());
			}
			for (Card card : opponent.getHand()) {
				assertEquals("minion_neutral_test", card.getCardId());
			}
		});
	}

	@Test
	public void testForScience() {
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, 1, 1);
			playCard(context, player, "spell_freezing_over", minion);
			Minion minion2 = playMinionCard(context, player, 2, 2);
			assertEquals(4, minion.getAttack());
			assertEquals(2, minion2.getAttack());
		});
	}
}
