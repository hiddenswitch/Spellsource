package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class StorytellerTests extends TestBase {
	@Test
	public void testBookOfLife() {
		runGym((context, player, opponent) -> {
			assertEquals(player.getHand().size(), 0);
			useHeroPower(context, player);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getSourceCard().getCardId(), "spell_restorative_words");
		}, HeroClass.OLIVE, HeroClass.OLIVE);
	}

	@Test
	public void testEngagingStory() {
		runGym(((context, player, opponent) -> {
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			player.getDeck().addCard("spell_lunstone");
			playCard(context, player, "spell_engaging_story");
			assertEquals(player.getHand().size(), 0);
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getDeck().size(), 3);
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 2);
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 2);
		}));
	}

	@Test
	public void testMagicWordMishap() {
		runGym(((context, player, opponent) -> {
			Minion yeti = playMinionCard(context, player, "minion_test_4_5");
			Minion raptor = playMinionCard(context, player, "minion_test_3_2");
			yeti.setHp(3);
			playCard(context, player, "spell_magic_word_mishap", yeti);
			playCard(context, player, "spell_magic_word_mishap", raptor);
			assertEquals(yeti.getHp(), yeti.getBaseHp());
			assertFalse(yeti.isDestroyed());
			assertTrue(raptor.isDestroyed());
		}));
	}

	@Test
	public void testWritersBlock() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "spell_writers_block");
			context.endTurn();
			opponent.setMana(5);
			playCard(context, opponent, "spell_heal_all_minions");
			assertEquals(opponent.getMana(), 2);
			context.endTurn();
			context.endTurn();
			opponent.setMana(5);
			playCard(context, opponent, "spell_heal_all_minions");
			assertEquals(opponent.getMana(), 5);
		}));
	}

	@Test
	public void testDramaticPlaywright() {
		runGym(((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2");
			Minion playwright = playMinionCard(context, player, "minion_dramatic_playwright");
			destroy(context, playwright);
			assertEquals(raptor.getAttack(), 1);
			context.endTurn();
			assertEquals(raptor.getAttack(), 1);
			context.endTurn();
			assertEquals(raptor.getAttack(), raptor.getBaseAttack());
		}));

		runGym(((context, player, opponent) -> {
			Minion raptor = playMinionCard(context, player, "minion_test_3_2");
			Minion playwright = playMinionCard(context, player, "minion_dramatic_playwright");
			destroy(context, playwright);
			assertEquals(raptor.getAttack(), raptor.getBaseAttack());
		}));
	}

	@Test
	public void testTallTale() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "secret_tall_tale");
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, player, "spell_frostfire", opponent.getHero());
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			playCard(context, opponent, "spell_5_to_enemy");
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, opponent, "spell_frostfire", player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_skeptic");
		}));
	}

	@Test
	public void testMiserableConclusion() {
		runGym((context, player, opponent) -> {
			List<Minion> minionTest32 = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				minionTest32.add(playMinionCard(context, player, "minion_test_3_2"));
				minionTest32.add(playMinionCard(context, opponent, "minion_test_3_2"));
			}
			minionTest32.add(playMinionCard(context, player, "minion_test_3_2"));
			Minion minion45 = playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_miserable_conclusion");
			for (Minion minion : minionTest32) {
				assertTrue(minion.isDestroyed());
			}
			assertFalse(minion45.isDestroyed());
		});
	}

	@Test
	public void testWhatBigFangs() {
		runGym((context, player, opponent) -> {
			Minion toBeDestroyed = playMinionCard(context, opponent, "minion_dramatic_playwright");
			Minion toAttack = playMinionCard(context, player, "minion_timeworn_archivist");
			playCard(context, player, "spell_what_big_fangs", toAttack);
			attack(context, player, toAttack, toBeDestroyed);
			assertTrue(toBeDestroyed.isDestroyed());
			assertTrue(toAttack.isDestroyed());
		});
	}

	@Test
	public void testManlyMountaineer() {
		runGym((context, player, opponent) -> {
			Minion mountaineer = playMinionCard(context, player, "minion_manly_mountaineer");
			mountaineer.setHp(4);
			attack(context, player, mountaineer, opponent.getHero());
			assertEquals(mountaineer.getHp(), 4);
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2");
			attack(context, player, mountaineer, raptor);
			assertEquals(mountaineer.getHp(), 5);
			Minion mountaineer2 = playMinionCard(context, opponent, "minion_manly_mountaineer");
			attack(context, player, mountaineer, mountaineer2);
			assertTrue(mountaineer.isDestroyed());
		});
	}

	@Test
	public void testWhimsicalGenerator() {
		runGym((context, player, opponent) -> {
			Minion raptor1 = playMinionCard(context, player, "minion_test_3_2");
			Minion raptor2 = playMinionCard(context, player, "minion_test_3_2");
			Minion raptor3 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_whimsical_generator", raptor2);
			assertEquals(raptor1.getAttack(), raptor1.getBaseAttack() + 3);
			assertEquals(raptor2.getAttack(), raptor2.getBaseAttack() + 3);
			assertEquals(raptor3.getAttack(), raptor3.getBaseAttack() + 3);
			assertEquals(raptor1.getHp(), raptor1.getBaseHp() + 4);
			assertEquals(raptor2.getHp(), raptor2.getBaseHp() + 4);
			assertEquals(raptor3.getHp(), raptor3.getBaseHp() + 4);
			assertTrue(raptor1.hasAttribute(Attribute.TAUNT));
			assertTrue(raptor2.hasAttribute(Attribute.TAUNT));
			assertTrue(raptor3.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testHouseOfCandy() {
		runGym(((context, player, opponent) -> {
			context.endTurn();
			Minion raptor = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "secret_house_of_candy");
			context.endTurn();
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_test_3_2");
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(opponent.getMinions().size(), 2);
			assertTrue(opponent.getMinions().contains(yeti));
			assertTrue(opponent.getMinions().contains(raptor));
		}));
	}


	@Test
	public void testAlagardsInfusion() {
		runGym((context, player, opponent) -> {
			Card minion = receiveCard(context, player, "minion_big_bad_coyote");
			playCard(context, player, "spell_alagards_infusion");
			playCard(context, player, minion);
		});
	}

	@Test
	public void testUrelaSeekerOfPower() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_urela_seeker_of_power");
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 1);
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 3);
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 5);
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 8);
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 11);
			context.endTurn();
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 15);
			playCard(context, player, "minion_timeworn_archivist");
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 20);
			playCard(context, player, "minion_timeworn_archivist");
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 26);
		});
	}

	@Test
	public void testOverflowingEnergy() {
		runGym((context, player, opponent) -> {
			Random random = new XORShiftRandom(101010L);
			CardCatalogue.loadCardsFromPackage();
			List<Card> cards = CardCatalogue.getAll().stream()
					.filter(card -> card.getBaseManaCost() > 0)
					.filter(card -> card.getCardType().equals(CardType.SPELL))
					.collect(Collectors.toList());
			for (int i = 0; i < 7; i++) {
				receiveCard(context, player, cards.get(random.nextInt(cards.size())).getCardId());
			}
			receiveCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_overflowing_energy");
			assertEquals(player.getHand().size(), 10);
			int count = 0;
			for (Card card : player.getHand()) {
				if (card.getCardId().equals("spell_lunstone")) {
					count++;
				}
			}
			assertEquals(count, 3);
		});

		runGym((context, player, opponent) -> {
			Random random = new Random();
			CardCatalogue.loadCardsFromPackage();
			List<Card> cards = CardCatalogue.getAll().stream()
					.filter(card -> card.getBaseManaCost() == 0)
					.filter(card -> card.getCardType().equals(CardType.SPELL))
					.collect(Collectors.toList());
			for (int i = 0; i < 8; i++) {
				receiveCard(context, player, cards.get(random.nextInt(cards.size())).getCardId());
			}
			playCard(context, player, "spell_overflowing_energy");
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getHand().get(8).getCardId(), player.getHand().get(9).getCardId());
			for (int i = 0; i < player.getHand().size() - 2; i++) {
				if (player.getHand().get(i).getCardId().equals(player.getHand().get(9).getCardId())) {
					return;
				}
			}
			assertTrue(false);
		});
	}

	@Test
	public void testChainedChimera() {
		runGym((context, player, opponent) -> {
			Card chimera = receiveCard(context, player, "minion_chained_chimera");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			player.setMana(4);
			playCard(context, player, chimera);
			assertEquals(player.getMana(), 1);
		});
	}

	@Test
	public void testDiscardedCreation() {
		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_discarded_creation");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			Minion minion = playMinionCard(context, player, card);
			assertTrue(minion.hasAttribute(Attribute.CHARGE));
			assertFalse(minion.hasAttribute(Attribute.RUSH));
		});

		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_discarded_creation");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			Minion minion = playMinionCard(context, player, card);
			assertFalse(minion.hasAttribute(Attribute.CHARGE));
			assertTrue(minion.hasAttribute(Attribute.RUSH));
		});

		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_discarded_creation");
			playCard(context, player, "spell_lunstone");
			playCard(context, player, "spell_lunstone");
			Minion minion = playMinionCard(context, player, card);
			assertFalse(minion.hasAttribute(Attribute.CHARGE));
			assertTrue(minion.hasAttribute(Attribute.RUSH));
		});

		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_discarded_creation");
			playCard(context, player, "spell_lunstone");
			Minion minion = playMinionCard(context, player, card);
			assertFalse(minion.hasAttribute(Attribute.CHARGE));
			assertFalse(minion.hasAttribute(Attribute.RUSH));
		});
	}

	@Test
	public void testUrgentExperiment() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_urgent_experiment");
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_chained_chimera");
			attack(context, opponent, attacker, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			context.endTurn();
			player.setMana(10);
			Minion reducedCost = playMinionCard(context, player, "minion_chained_chimera");
			assertEquals(player.getMana(), 10 - reducedCost.getSourceCard().getBaseManaCost() + 3);
			player.setMana(10);
			reducedCost = playMinionCard(context, player, "minion_chained_chimera");
			assertEquals(player.getMana(), 10 - reducedCost.getSourceCard().getBaseManaCost());
			context.endTurn();
			context.endTurn();
			player.setMana(10);
			reducedCost = playMinionCard(context, player, "minion_chained_chimera");
			assertEquals(player.getMana(), 10 - reducedCost.getSourceCard().getBaseManaCost());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_urgent_experiment");
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_chained_chimera");
			attack(context, opponent, attacker, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			context.endTurn();
			context.endTurn();
			context.endTurn();
			player.setMana(10);
			Minion reducedCost = playMinionCard(context, player, "minion_chained_chimera");
			assertEquals(player.getMana(), 10 - reducedCost.getSourceCard().getBaseManaCost());
		});
	}

	@Test
	public void testCeaselessPower() {
		runGym((context, player, opponent) -> {
			Minion shouldDie = playMinionCard(context, player, "minion_alagard_mythologist");
			Minion shouldNotDie = playMinionCard(context, player, "minion_chained_chimera");
			playCard(context, player, "spell_ceaseless_power", shouldDie);
			playCard(context, player, "spell_ceaseless_power", shouldNotDie);
			assertTrue(shouldDie.isDestroyed());
			assertFalse(shouldNotDie.isDestroyed());
			assertEquals(shouldNotDie.getAttack(), shouldNotDie.getBaseAttack() + 4);
			assertEquals(shouldNotDie.getHp(), shouldNotDie.getBaseHp() + 3);
		});
	}

	@Test
	public void testTerribleStrength() {
		runGym((context, player, opponent) -> {
			Minion toBuff = playMinionCard(context, player, "minion_chained_chimera");
			Minion shouldDie = playMinionCard(context, opponent, "minion_chained_chimera");
			Minion shouldNotDie = playMinionCard(context, opponent, "minion_discarded_creation");
			playCard(context, player, "spell_terrible_strength", toBuff);
			assertTrue(shouldDie.isDestroyed());
			assertFalse(shouldNotDie.isDestroyed());
			assertEquals(toBuff.getAttack(), toBuff.getBaseAttack() + 2);
			assertEquals(toBuff.getHp(), toBuff.getBaseHp() + 2);
		});

		runGym((context, player, opponent) -> {
			Minion toBuff = playMinionCard(context, player, "minion_chained_chimera");
			Minion canDie1 = playMinionCard(context, opponent, "minion_chained_chimera");
			Minion canDie2 = playMinionCard(context, opponent, "minion_chained_chimera");
			playCard(context, player, "spell_terrible_strength", toBuff);
			assertNotEquals(canDie1.isDestroyed(), canDie2.isDestroyed());
			assertEquals(toBuff.getAttack(), toBuff.getBaseAttack() + 2);
			assertEquals(toBuff.getHp(), toBuff.getBaseHp() + 2);
		});

		runGym((context, player, opponent) -> {
			Minion toBuff = playMinionCard(context, player, "minion_chained_chimera");
			Minion shouldNotDie1 = playMinionCard(context, opponent, "minion_discarded_creation");
			Minion shouldNotDie2 = playMinionCard(context, opponent, "minion_discarded_creation");
			playCard(context, player, "spell_terrible_strength", toBuff);
			assertFalse(shouldNotDie1.isDestroyed() || shouldNotDie2.isDestroyed());
			assertEquals(toBuff.getAttack(), toBuff.getBaseAttack() + 2);
			assertEquals(toBuff.getHp(), toBuff.getBaseHp() + 2);
		});
	}

	@Test
	public void testUnstableIronbeast() {
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, player, "minion_chained_chimera");
			Minion minion2 = playMinionCard(context, player, "minion_chained_chimera");
			Minion ironbeast = playMinionCard(context, player, "minion_unstable_ironbeast");
			destroy(context, ironbeast);
			assertNotEquals(minion1.hasAttribute(Attribute.TAUNT), minion2.hasAttribute(Attribute.TAUNT));
			assertEquals(Math.abs(minion1.getAttack() - minion2.getAttack()), 1);
			assertEquals(Math.abs(minion1.getHp() - minion2.getHp()), 1);
		});
	}

}
