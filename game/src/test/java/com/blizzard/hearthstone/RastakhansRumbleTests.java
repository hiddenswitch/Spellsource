package com.blizzard.hearthstone;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RastakhansRumbleTests extends TestBase {

	@Test
	public void testSpiritOfTheShark() {
		runGym((context, player, opponent) -> {
			Minion spirit = playMinionCard(context, player, "minion_spirit_of_the_shark");
			Minion edwin = playMinionCard(context, player, "minion_edwin_vancleef");
			assertEquals(edwin.getAttack(), 6);
			playMinionCardWithBattlecry(context, player, "minion_si7_agent", edwin);
			assertEquals(edwin.getHp(), 2);
			assertTrue(spirit.hasAttribute(Attribute.STEALTH));
			context.endTurn();
			context.endTurn();
			assertTrue(!spirit.hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testShirvallahTheTiger() {
		runGym((context, player, opponent) -> {
			Card tiger = receiveCard(context, player, "minion_shirvallah_the_tiger");
			assertEquals(costOf(context, player, tiger), 25);
			playCard(context, player, "spell_anyfin_can_happen");
			assertEquals(costOf(context, player, tiger), 15);
			playCard(context, player, "minion_ultrasaur");
			assertEquals(costOf(context, player, tiger), 15);
		});
	}

	@Test
	public void testSpiritOfTheTiger() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			playCard(context, player, "minion_spirit_of_the_tiger");
			playCard(context, player, "minion_cult_master");
			playCard(context, player, "spell_the_coin");
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testHexLordMalacrass() {
		runGym((context, player, opponent) -> {
			player.getRemovedFromPlay().clear();
			Card startsInHand = receiveCard(context, player, "minion_bloodfen_raptor");
			startsInHand.getAttributes().put(Attribute.STARTED_IN_HAND, true);
			Card doesntStartInHand = receiveCard(context, player, "spell_the_coin");
			playCard(context, player, "spell_demonic_project");
			int handSize = player.getHand().size();
			playCard(context, player, "minion_hex_lord_malacrass");
			assertEquals(player.getHand().size(), handSize + 1);
			startsInHand = startsInHand.transformResolved(context).getSourceCard();
			assertNotEquals(startsInHand.getCardId(), "minion_bloodfen_raptor");
			assertEquals(player.getHand().get(2).getCardId(), startsInHand.getCardId(),
					"Following rules on transforming cards, Hex Lord Malacrass should put another copy of the demon into your hand");
		});
	}

	@Test
	public void testVoidContract() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			for (int i = 0; i < 15; i++) {
				shuffleToDeck(context, player, "minion_wisp");
				shuffleToDeck(context, opponent, "minion_wisp");
			}

			assertEquals(player.getDeck().size(), 16);
			assertEquals(opponent.getDeck().size(), 15);
			playCard(context, player, "spell_void_contract");
			assertEquals(player.getDeck().size(), 8);
			assertEquals(opponent.getDeck().size(), 7);
		});

	}

	@Test
	public void testBaitedArrow() {
		runGym((context, player, opponent) -> {
			playCard(context, opponent, "spell_kara_kazham");
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "spell_baited_arrow", opponent.getMinions().get(0));
			}
			assertEquals(player.getMinions().size(), 2);
		});

	}

	@Test
	public void testPyromaniac() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			playCard(context, player, "minion_pyromaniac");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			useHeroPower(context, player, wisp.getReference());
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testTicketScalper() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			Minion ticket = playMinionCard(context, player, "minion_ticket_scalper");
			Minion wisp = playMinionCard(context, opponent, "minion_wisp");
			Minion ultrasaur = playMinionCard(context, opponent, "minion_ultrasaur");
			attack(context, player, ticket, wisp);
			assertEquals(player.getHand().size(), 2);
			attack(context, player, ticket, ultrasaur);
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testSpiritOfTheDead() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_spirit_of_the_dead");
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			destroy(context, wisp);
			assertEquals(player.getDeck().size(), 1);
			assertEquals(costOf(context, player, player.getDeck().get(0)), 1);
		});
	}

	@Test
	public void testBwonsamdiTheDead() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_faithful_lumi");
			playCard(context, player, "minion_spirit_of_the_dead");
			for (int i = 0; i < 5; i++) {
				shuffleToDeck(context, player, "minion_faithful_lumi");
				Minion wisp = playMinionCard(context, player, "minion_wisp");
				destroy(context, wisp);
			}

			playCard(context, player, "minion_bwonsamdi_the_dead");
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 3);
		});
	}

	@Test
	public void testZentimo() {
		runGym((context, player, opponent) -> {
			Minion ultra1 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion ultra2 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion ultra3 = playMinionCard(context, opponent, "minion_ultrasaur");
			playCard(context, player, "minion_zentimo");
			playCard(context, player, "spell_hex", ultra2);
			for (int i = 0; i < 3; i++) {
				assertEquals(opponent.getMinions().get(i).getSourceCard().getCardId(), "token_frog", i + "");
			}
		});
	}

	@Test
	public void testSpiritOfTheDragonhawk() {
		runGym((context, player, opponent) -> {
			Minion ultra1 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion ultra2 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion ultra3 = playMinionCard(context, opponent, "minion_ultrasaur");
			playCard(context, player, "minion_spirit_of_the_dragonhawk");
			useHeroPower(context, player, ultra2.getReference());
			for (int i = 0; i < 3; i++) {
				assertEquals(opponent.getMinions().get(i).getHp(), 13, i + "");
			}
		}, HeroClass.BLUE, HeroClass.BLUE);
	}

	@Test
	public void testDaringFireEater() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_daring_fire_eater");
			useHeroPower(context, player, opponent.getHero().getReference());
			useHeroPower(context, player, opponent.getHero().getReference());
			assertEquals(opponent.getHero().getHp(), 26);
		});
	}

	@Test
	public void testGralTheShark() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_ultrasaur");
			Minion gral = playMinionCard(context, player, "minion_gral_the_shark");
			assertEquals(gral.getAttack(), 9);
			assertEquals(gral.getHp(), 16);
			destroy(context, gral);
			assertEquals(player.getHand().size(), 1);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_ultrasaur");
			shuffleToDeck(context, player, "minion_ultrasaur");
			shuffleToDeck(context, player, "minion_ultrasaur");
			playMinionCard(context, player, "minion_spirit_of_the_shark");
			Minion gral = playMinionCard(context, player, "minion_gral_the_shark");
			assertEquals(gral.getAttack(), 16);
			assertEquals(gral.getHp(), 30);
			destroy(context, gral);
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testBlastWave() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_stand_against_darkness");
			playCard(context, player, "spell_blast_wave");
			assertEquals(player.getHand().size(), 5);
		});
	}

	@Test
	public void testJanalaiTheDragonhawk() {
		runGym((context, player, opponent) -> {
			Card janalai = receiveCard(context, player, "minion_janalai_the_dragonhawk");
			for (int i = 0; i < 8; i++) {
				useHeroPower(context, player, opponent.getHero().getReference());
				for (String s : janalai.evaluateDescriptions(context, player)) {
					System.out.println(s);
				}
			}

			Card spellstone = receiveCard(context, player, "spell_lesser_pearl_spellstone");
			for (String s : spellstone.evaluateDescriptions(context, player)) {
				System.out.println(s);
			}
			playCard(context, player, "minion_voodoo_doctor", opponent.getHero());
			for (String s : spellstone.evaluateDescriptions(context, player)) {
				System.out.println(s);
			}
		});
	}

	@Test
	public void testPredatoryInstincts() {
		runGym((context, player, opponent) -> {
			Card grizzly = shuffleToDeck(context, player, "minion_witchwood_grizzly");
			playCard(context, player, "spell_predatory_instincts");
			Minion grizzlyNow = playMinionCard(context, player, grizzly);
			assertEquals(grizzlyNow.getHp(), 24);
		});
	}

	@Test
	public void testGriftah() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_griftah");
			assertEquals(player.getHand().size(), 1);
			assertEquals(opponent.getHand().size(), 1);
		});
	}

	@Test
	public void testMastersCall() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_snowflipper_penguin");
			}
			playCard(context, player, "spell_masters_call");
			assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, player, "minion_snowflipper_penguin");
			shuffleToDeck(context, player, "minion_snowflipper_penguin");
			playCard(context, player, "spell_masters_call");

			assertEquals(player.getHand().size(), 1);
		});

		//TODO should Master's Call draw all options if it's two beasts and not three?
		//for now, yes
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 2; i++) {
				shuffleToDeck(context, player, "minion_snowflipper_penguin");
			}
			playCard(context, player, "spell_masters_call");
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testDaUndatakah() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_leper_gnome");
			playCard(context, player, "minion_hoarding_dragon");
			playCard(context, player, "minion_drygulch_jailor");
			playCard(context, player, "spell_twisting_nether");
			assertEquals(opponent.getHero().getHp(), 28);
			assertEquals(opponent.getHand().size(), 2);
			assertEquals(player.getHand().size(), 3);
			Minion shouldRevealCard = playMinionCard(context, player, "minion_reveal_cards_test");
			playCard(context, player, "minion_da_undatakah");
			assertEquals(shouldRevealCard.getAttributeValue(Attribute.RESERVED_INTEGER_1), 3, "Should have revealed 3 cards (see implementation of minion_reveal_cards_test");
			playCard(context, player, "spell_twisting_nether");
			assertEquals(opponent.getHero().getHp(), 26);
			assertEquals(opponent.getHand().size(), 4);
			assertEquals(player.getHand().size(), 6);
		});
	}

	@Test
	public void testSnapjawShellfighter() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_wisp");
			Minion ss = playMinionCard(context, player, "minion_snapjaw_shellfighter");
			playCard(context, player, "spell_fireball", wisp);
			assertFalse(wisp.isDestroyed());
			assertEquals(ss.getHp(), 2);
		});

		runGym((context, player, opponent) -> {
			Minion ss = playMinionCard(context, player, "minion_snapjaw_shellfighter");
			Minion snapjawAdjacent = playMinionCard(context, player, "minion_snapjaw_shellfighter");
			playCard(context, player, "spell_fireball", ss);
			assertEquals(ss.getHp(), ss.getMaxHp() - 6);
			assertEquals(snapjawAdjacent.getHp(), snapjawAdjacent.getMaxHp(), "Snapjaw doesn't bounce to another Snapjaw.");
		});
	}

	@Test
	@Ignore("card needs to be re-enabled")
	public void testImmortalPrelate() {
		runGym((context, player, opponent) -> {
			Minion ip = playMinionCard(context, player, "minion_immortal_prelate");
			playCard(context, player, "spell_blessing_of_wisdom", ip);
			destroy(context, ip);
			context.getLogic().drawCard(player.getId(), null);
			shuffleToDeck(context, player, "minion_wisp");
			playCard(context, player, player.getHand().get(0));
			attack(context, player, player.getMinions().get(0), opponent.getHero());
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testDrakkariTrickster() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_wisp");
			shuffleToDeck(context, opponent, "minion_wisp");
			playCard(context, player, "minion_drakkari_trickster");
			assertEquals(player.getHand().size(), 1);
			assertEquals(opponent.getHand().size(), 1);
		});
	}

	@Test
	public void testHighPriestessJeklik() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_high_priestess_jeklik");
			playCard(context, player, "spell_soul_infusion");
			playCard(context, player, "spell_soulfire", opponent.getHero());
			Minion j1 = playMinionCard(context, player, player.getHand().get(1));
			Minion j2 = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(j1.getHp(), 6);
			assertEquals(j2.getHp(), 6);
		});
	}
}
