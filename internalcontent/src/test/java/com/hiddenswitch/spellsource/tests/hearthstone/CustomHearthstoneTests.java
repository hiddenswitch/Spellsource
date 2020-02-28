package com.hiddenswitch.spellsource.tests.hearthstone;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.events.TurnEndEvent;
import net.demilich.metastone.game.events.TurnStartEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.ChangeHeroPowerSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public class CustomHearthstoneTests extends TestBase {

	@Override
	public DeckFormat getDefaultFormat() {
		return DeckFormat.getFormat("Custom Hearthstone");
	}

	@Test
	public void testTheMaelstrom() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			int playerHp = player.getHero().getHp();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(player.getHero().getHp() + opponent.getHero().getHp(), playerHp + opponentHp - 12);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			context.endTurn();
			int playerHp = player.getHero().getHp();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHero().getHp() + opponent.getHero().getHp(), playerHp + opponentHp - 12);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			playCard(context, player, "spell_test_summon_tokens");
			assertEquals(player.getMinions().size(), 5, "Maelstrom + 4 tokens");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_the_maelstrom");
			context.endTurn();
			playCard(context, opponent, "spell_test_summon_tokens");
			assertEquals(opponent.getMinions().size(), 4, "4 tokens");
		});
	}

	@Test
	public void testSunslayer() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				putOnTopOfDeck(context, player, "spell_test_gain_mana");
			}
			playCard(context, player, "weapon_sunslayer");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getDescription(context, player), "After your champion attacks, draw 0 cards. (Increases for every spell you've cast this turn)");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_test_gain_mana");
			assertEquals(player.getWeaponZone().get(0).getDescription(context, player), "After your champion attacks, draw 1 card. (Increases for every spell you've cast this turn)");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testArcaneSigil() {
		runGym((context, player, opponent) -> {
			// Shouldn't fire itself infinitely
			playCard(context, player, "secret_arcane_sigil");
			playCard(context, player, "spell_test_counter_secret");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_test_summon_tokens");
			assertEquals(opponent.getHero().getHp(), opponentHp - 2, "Should have triggered Arcane Sigil");
			assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testAFinalStrike() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_a_final_strike");
			final Quest quest = player.getQuests().get(0);
			opponent.getHero().setHp(100);
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "spell_test_deal_10", opponent.getHero());
			}
			assertTrue(quest.isExpired());
			assertEquals(opponent.getHero().getHp(), 40);
		});
	}

	@Test
	public void testAcceleratedGrowth() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2");
			shuffleToDeck(context, opponent, "minion_test_3_2");
			playCard(context, player, "spell_accelerated_growth");
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2");
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_test_3_2", "Testing the TargetPlayer.BOTH attribute on DrawCardSpell");
		});
	}

	@Test
	public void testAncestralLegacy() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_opener_summon");
			assertEquals(player.getMinions().size(), 2);
			playCard(context, player, "spell_test_destroy_all");
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getGraveyard().size(), 4, "Should contain Doom, Murloc Tidehunter the card, Murloc Tidehunter the minion and Murloc Scout the minion.");
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_test_opener_summon");
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_ancestral_legacy");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_opener_summon");
		});
	}


	@Test
	public void testAnnoyingBeetle() {
		runGym((context, player, opponent) -> {
			Minion annoyingBeetle = playMinionCard(context, player, "minion_annoying_beetle");
			assertEquals(opponent.getHeroPowerZone().get(0).getCardId(), "hero_power_die_insect");
			context.endTurn();
			GameLogic spy = spy(context.getLogic());
			context.setLogic(spy);
			doAnswer(invocation -> player.getHero()).when(spy).getRandom(anyList());
			int hp = player.getHero().getHp();
			context.performAction(opponent.getId(), opponent.getHeroPowerZone().get(0).play());
			assertEquals(player.getHero().getHp(), hp - 8);
			context.endTurn();

			destroy(context, annoyingBeetle);
			assertEquals(opponent.getHeroPowerZone().get(0).getCardId(), "hero_power_fireblast");
		}, "BLUE", "BLUE");
	}

	@Test
	public void testArmageddonVanguard() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2");
			Minion armageddon = playMinionCard(context, player, "minion_armageddon_vanguard");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_test_deal_1", beast);
			assertEquals(opponent.getHero().getHp(), opponentHp - 1);
		});

		runGym((context, player, opponent) -> {
			GameLogic spyLogic = spy(context.getLogic());
			context.setLogic(spyLogic);

			final Minion armageddon1 = playMinionCard(context, player, "minion_armageddon_vanguard");
			context.endTurn();
			final Minion armageddon2 = playMinionCard(context, opponent, "minion_armageddon_vanguard");
			doAnswer(invocation -> {
				List<Entity> randomTargets = invocation.getArgument(0);
				if (randomTargets.contains(armageddon1)) {
					return armageddon1;
				} else if (randomTargets.contains(armageddon2)) {
					return armageddon2;
				} else {
					throw new AssertionError("Unexpected random request");
				}
			}).when(spyLogic).getRandom(anyList());

			while (!armageddon1.isDestroyed()) {
				playCard(context, opponent, "spell_test_deal_1", armageddon1);
			}
			assertTrue(armageddon1.isDestroyed());
			assertTrue(armageddon2.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			final Minion armageddon1 = playMinionCard(context, player, "minion_armageddon_vanguard");
			Minion target = playMinionCard(context, player, "minion_neutral_test_1");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_test_deal_1", target);
			assertEquals(opponent.getHero().getHp(), opponentHp - 1);
		});
	}

	@Test
	public void testAutomedicAndrone() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_automedic_androne");
			player.getHero().setHp(10);
			playCard(context, player, "spell_test_heal_8", player.getHero());
			assertEquals(player.getHero().getHp(), 10);
			assertEquals(player.getHero().getArmor(), 8);
			context.endTurn();
			opponent.getHero().setHp(10);
			playCard(context, opponent, "spell_test_heal_8", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), 10);
			assertEquals(opponent.getHero().getArmor(), 8);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test_big");
			playMinionCard(context, player, "minion_automedic_androne");
			target.setHp(3);
			playCard(context, player, "spell_test_heal_8", target);
			assertEquals(target.getHp(), 3);
			assertEquals(player.getHero().getArmor(), 8);
			context.endTurn();
			target = playMinionCard(context, opponent, "minion_neutral_test_big");
			target.setHp(3);
			playCard(context, opponent, "spell_test_heal_8", target);
			assertEquals(target.getHp(), 3);
			assertEquals(opponent.getHero().getArmor(), 8);
		});
	}

	@Test
	public void testAwakenTheAncients() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_awaken_the_ancients");
			player.setMaxMana(10);
			player.setMana(10);
			playCard(context, player, "minion_test_3_2");
			assertEquals(player.getMana(), 10);
			playCard(context, player, "minion_test_3_2");
			assertEquals(player.getMana(), 8);
		});
	}

	@Test
	public void testBwonsamdi() {
		// Test that a deathrattle minion played from the hand doesn't get its own deathrattle copied onto it
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bwonsamdi");
			Card leper = receiveCard(context, player, "minion_leper_gnome");
			playCard(context, player, leper);
			// Destroy the Leper Gnome
			destroy(context, player.getMinions().get(1));
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2, "Leper Gnome should not have gotten its deathrattle doubled from Bwonsamdi.");
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_bwonsamdi");
			Card leper = receiveCard(context, player, "minion_leper_gnome");
			Card hoarder = receiveCard(context, player, "minion_loot_hoarder");
			shuffleToDeck(context, player, "spell_test_gain_mana");
			playCard(context, player, leper);
			// Destroy the Leper Gnome
			destroy(context, player.getMinions().get(1));
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 2, "Leper Gnome should not have gotten its deathrattle doubled from Bwonsamdi.");
			assertEquals(player.getHand().peek().getCardId(), "spell_test_gain_mana", "Should have drawn The Coin from a Loot Hoarder deathrattle.");
		});
	}

	@Test
	public void testCastleGiant() {
		runGym((context, player, opponent) -> {
			Card castleGiant = receiveCard(context, player, "minion_castle_giant");
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost());
			useHeroPower(context, player);
			context = context.clone();
			player = context.getPlayer1();
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost() - 1);
		}, "GOLD", "GOLD");

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_justicar_trueheart");
			Card castleGiant = receiveCard(context, player, "minion_castle_giant");
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost());
			useHeroPower(context, player);
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost() - 2);
		}, "GOLD", "GOLD");

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_justicar_trueheart");
			useHeroPower(context, player);
			Card castleGiant = receiveCard(context, player, "minion_castle_giant");
			assertEquals(costOf(context, player, castleGiant), castleGiant.getBaseManaCost() - 2);
		}, "GOLD", "GOLD");
	}

	@Test
	public void testThrowGlaive() {
		runGym((context, player, opponent) -> {
			Minion wisp1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion wisp2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion wisp3 = playMinionCard(context, opponent, "minion_neutral_test_1");

			playCard(context, player, "weapon_warglaives_of_azzinoth");

			playCard(context, player, "spell_leeching_poison");
			playCard(context, player, "minion_high_priest_thekal"); //health to 1

			playCard(context, player, "spell_throw_glaive", wisp2);

			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(player.getHero().getHp(), 13);
		});
	}

	@Test
	public void testEyeBeam() {
		runGym((context, player, opponent) -> {
			Minion dino1 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion dino2 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion dino3 = playMinionCard(context, opponent, "minion_ultrasaur");

			playCard(context, player, "spell_eye_beam", dino2);

			assertEquals(opponent.getMinions().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion dino1 = playMinionCard(context, opponent, "minion_stegodon");
			Minion dino2 = playMinionCard(context, opponent, "minion_ultrasaur");
			Minion dino3 = playMinionCard(context, opponent, "minion_tyrantus");

			playCard(context, player, "spell_eye_beam", dino2);
			assertEquals(dino1.getHp(), 0);
			assertEquals(dino2.getHp(), 8);
			assertEquals(dino3.getHp(), 6);
		});

		runGym((context, player, opponent) -> {
			Minion harvest = playMinionCard(context, opponent, "minion_harvest_golem");
			playCard(context, player, "spell_eye_beam", harvest);
			assertEquals(opponent.getMinions().size(), 1);
		});
	}

	@Test
	public void testNemesis() {
		runGym((context, player, opponent) -> {
			Minion demon1 = playMinionCard(context, opponent, "minion_fearsome_doomguard");
			Minion demon2 = playMinionCard(context, opponent, "minion_voidwalker");
			Minion ogre = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			playCard(context, opponent, "minion_lord_jaraxxus");

			playCard(context, player, "spell_nemesis", ogre);
			assertEquals(ogre.getHp(), 1);
			assertEquals(demon1.getHp(), 8);
			assertEquals(demon2.getHp(), 3);
			assertEquals(opponent.getHero().getHp(), 15);

			playCard(context, player, "spell_nemesis", demon1);
			assertEquals(demon1.getHp(), 1);
			assertEquals(demon2.getHp(), 1);
			assertEquals(opponent.getHero().getHp(), 1);
		});

	}

	@Test
	public void testFelblade() {
		runGym((context, player, opponent) -> {
			Card felblade = receiveCard(context, player, "weapon_felblade");
			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "weapon_illidari_warglaives");
			assertEquals(costOf(context, player, felblade), 3);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(costOf(context, player, felblade), 2);
			useHeroPower(context, player, wisp.getReference());
			assertTrue(wisp.isDestroyed());
			assertEquals(costOf(context, player, felblade), 1);
		}, "PURPLE", "PURPLE");
	}

	@Test
	public void testVengefulRetreat() {
		runGym((context, player, opponent) -> {
			Minion badWisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion goodWisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_vengeful_retreat", goodWisp);
			playCard(context, player, player.getHand().get(0));
			assertTrue(player.getMinions().get(0).canAttackThisTurn());

		});
	}

	@Test
	public void testMetamorphosis() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			playCard(context, player, "spell_metamorphosis");
			assertEquals(player.getHand().size(), 0);

			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");

			Minion thek = playMinionCard(context, player, "minion_high_priest_thekal");
			assertTrue(thek.hasAttribute(Attribute.LIFESTEAL));
			assertEquals(player.getHand().size(), 1);

			playCard(context, player, "weapon_warglaives_of_azzinoth");
			assertTrue(player.getWeaponZone().get(0).hasAttribute(Attribute.LIFESTEAL));
			assertEquals(player.getHand().size(), 2);

			context.endTurn();
			context.endTurn();

			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");

			playCard(context, player, "spell_chaos_nova");
			assertEquals(player.getHero().getHp(), 3);

			context.endTurn();
			context.endTurn();

			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");

			context.endTurn();
			context.endTurn();

			assertNotEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_demonic_form");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_metamorphosis");

			Card demonForm = player.getHeroPowerZone().get(0);


			assertEquals(demonForm.getCardId(), "hero_power_demonic_form");
			String[] strings = demonForm.evaluateDescriptions(context, player);

			assertEquals(strings[0], "2");
			assertEquals(strings[1], "s");

			context.endTurn();
			context.endTurn();


			strings = demonForm.evaluateDescriptions(context, player);
			assertEquals(strings[0], "1");
			assertEquals(strings[1], "");
		});
	}

	@Test
	public void testDemonSpikes() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_demon_spikes");
			assertEquals(player.getHero().getAttack(), 1);
			context.endTurn();
			assertEquals(player.getHero().getAttack(), 1);
			context.endTurn();

			assertEquals(player.getHero().getAttack(), 0);
		});
	}

	@Test
	public void testFelRush() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 10; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}
			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "weapon_illidari_warglaives");
			playCard(context, player, "spell_fel_rush");
			assertEquals(player.getHand().size(), 1);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHand().size(), 2);
			useHeroPower(context, player, wisp.getReference());
			assertEquals(player.getHand().size(), 3);
		}, "PURPLE", "PURPLE");
	}

	@Test
	public void testGlaivesOfTheFallen() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_glaives_of_the_fallen");
			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			attack(context, player, player.getHero(), wisp);
			assertEquals(player.getHand().size(), 1);

			Minion croc = playMinionCard(context, opponent, "minion_river_crocolisk");
			attack(context, player, player.getHero(), croc);
			assertEquals(player.getHand().size(), 1);
			assertTrue(croc.hasAttribute(Attribute.DEATHRATTLES));

			playCard(context, player, "weapon_glaives_of_the_fallen");
			attack(context, player, player.getHero(), croc);

			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testChaosBlades() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_chaos_blades");
			Minion adept = playMinionCard(context, player, "minion_illidari_adept");
			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(adept.getAttack(), 3);
			assertTrue(wisp.isDestroyed());
			assertEquals(player.getWeaponZone().get(0).getDurability(), 4);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_chaos_blades");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getDurability(), 5);

			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			attack(context, player, player.getHero(), opponent.getHero());
			assertTrue(wisp.isDestroyed());

			assertEquals(player.getWeaponZone().get(0).getDurability(), 3);
		});
	}

	@Test
	public void testIllidariEnforcer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			context.endTurn();
			context.getTurn();
			Minion enforcer = playMinionCard(context, opponent, "minion_illidari_enforcer");
			assertTrue(enforcer.hasAttribute(Attribute.TAUNT));
		});
	}

	@Test
	public void testImmolationAura() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_immolation_aura");
			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, opponent, wisp, player.getHero());
			assertTrue(wisp.isDestroyed());
		});
	}

	@Test
	public void testMomentum() {
		runGym((context, player, opponent) -> {
			Minion azzinoth = playMinionCard(context, player, "minion_azzinoth");
			shuffleToDeck(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_momentum");
			context.getLogic().drawCard(player.getId(), null);
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getHand().size(), 1);
			destroy(context, azzinoth);
			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testFelBarrage() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_fel_barrage");
			assertEquals(opponent.getHero().getHp(), 20);
			playCard(context, player, "minion_bloodmage_thalnos");
			playCard(context, player, "spell_fel_barrage");
			assertEquals(opponent.getHero().getHp(), 8);
		});
	}

	@Test
	public void testCommanderGarrosh() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_commander_garrosh");
			player.setMana(10);
			useHeroPower(context, player);
			assertEquals(player.getHero().getArmor(), 13);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_commander_garrosh");
			playCard(context, player, "minion_raza_the_chained");
			player.setMana(10);
			useHeroPower(context, player);
			assertEquals(player.getHero().getArmor(), 14);
			assertEquals(player.getMana(), 0);
		});
	}

	@Test
	public void testCriminologist() {
		final int MAGE = 0;
		final int HUNTER = 1;
		final int PALADIN = 2;
		final int ROGUE = 3;
		Stream.of(MAGE, HUNTER, PALADIN, ROGUE).forEach(heroClass -> {
			runGym((context, player, opponent) -> {
				playCard(context, player, "spell_test_gain_mana");
				CardArrayList cards = new CardArrayList();
				overrideDiscover(context, player, discoveries -> {
					assertEquals(discoveries.size(), 4);
					discoveries.stream().map(DiscoverAction::getCard).forEach(cards::addCard);
					return discoveries.get(heroClass);
				});
				playCard(context, player, "minion_criminologist");
				Card card = player.getHand().get(0);
				assertTrue(card.isSecret());
				String secretClass = card.getHeroClass();
				switch (heroClass) {
					case MAGE:
						assertEquals(secretClass, "BLUE");
						break;
					case HUNTER:
						assertEquals(secretClass, "GREEN");
						break;
					case PALADIN:
						assertEquals(secretClass, "GOLD");
						break;
					case ROGUE:
						assertEquals(secretClass, "BLACK");
						break;
				}
			});
		});
	}

	@Test
	public void testCryWolf() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_cry_wolf");
			playCard(context, player, player.getHand().peekFirst());
			playCard(context, player, player.getHand().peekFirst());

			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_sheep");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_wolf");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_wolf");
		});

	}

	@Test
	public void testCrypticRuins() {
		for (int i = 0; i < 8; i++) {
			final int j = i;
			runGym((context, player, opponent) -> {
				Minion beasttest32 = playMinionCard(context, player, "minion_test_3_2");
				beasttest32.setAttribute(Attribute.SPELL_DAMAGE, j);
				AtomicInteger didDiscover = new AtomicInteger(0);
				Card spellCard = receiveCard(context, player, "spell_test_gain_mana");
				int spellpower = context.getLogic().applySpellpower(player, spellCard, 3);
				overrideDiscover(context, player, discoverActions -> {
					assertTrue(discoverActions.size() > 0);
					assertTrue(spellpower >= j);
					int whichDiscover = didDiscover.getAndIncrement();
					for (DiscoverAction action : discoverActions) {
						switch (whichDiscover) {
							case 0:
								assertEquals(action.getCard().getBaseManaCost(), spellpower);
								break;
							case 1:
								assertEquals(action.getCard().getAttack(), spellpower);
								break;
							case 2:
								assertEquals(action.getCard().getBaseHp(), spellpower);
								break;
						}
					}
					return discoverActions.get(0);
				});

				playCard(context, player, "spell_cryptic_ruins");
				assertEquals(didDiscover.get(), 3);
			});
		}
	}

	@Test
	public void testDejaVu() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_deja_vu");
			assertEquals(player.getMinions().size(), 2);
			assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
			playCard(context, player, player.getHand().get(1));
			playCard(context, player, player.getHand().get(0));
			for (int i = 2; i < 4; i++) {
				assertEquals(player.getMinions().get(i).getAttack(), 1);
				assertEquals(player.getMinions().get(i).getHp(), 1);
			}
		});
	}

	@Test
	public void testDesolationOfKaresh() {
		// No combos played, should die player's next turn
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, should die in 4 turns
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			context.endTurn();
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Not combo card played, should die player's next turn
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_test_3_2");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played. Should die in 4 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});

		// Activated combo card played, then not activated combo card played, then activated combo card played. Should die in 6 turns.
		runGym((context, player, opponent) -> {
			Minion desolation = playMinionCard(context, player, "permanent_desolation_of_karesh");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			playCard(context, player, "minion_defias_ringleader");
			playCard(context, player, "minion_defias_ringleader");
			context.endTurn();
			assertFalse(desolation.isDestroyed());
			context.endTurn();
			context.endTurn();
			context.endTurn();
			assertTrue(desolation.isDestroyed());
		});
	}

	@Test
	public void testDivineIntervention() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(5);
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHero().getHp(), 11, "Should have healed for 6");
			assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack() + 2, "Lightwarden should have buffed");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_divine_intervention");
			Minion lightwarden = playMinionCard(context, player, "minion_lightwarden");
			player.getHero().setHp(7);
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getHero().getHp(), 1, "Should not have healed.");
			assertEquals(lightwarden.getAttack(), lightwarden.getBaseAttack(), "Lightwarden should not have buffed");
		});
	}

	@Test
	public void testLunasOtherPocketGalaxy() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_lunas_other_pocket_galaxy");
			for (int i = 0; i < 100; i++) {
				playCard(context, player, "spell_excess_mana");
				assertEquals(player.getDeck().size(), 60);
			}

			assertFalse(player.isDestroyed());
		});
	}

	@Test
	public void testRelicRaider() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "weapon_vinecleaver");
			playCard(context, player, "minion_relic_raider");
			if (!player.getWeaponZone().isEmpty()) {
				Weapon weapon = player.getWeaponZone().get(0);
				assertEquals(weapon.getName(), "Vinecleaver");
				assertEquals(weapon.getAttack(), 1);
				assertEquals(weapon.getHp(), 2);
			}
		});
	}

	@Test
	public void testQuartz() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_quartz_spellstone");
			playCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_frost_nova");
			playCard(context, player, "spell_frost_nova");
			assertEquals(player.getHand().get(0).getCardId(), "spell_quartz_spellstone");
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_quartz_spellstone");
			playCard(context, opponent, "minion_neutral_test_1");
			playCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_frost_nova");
			playCard(context, player, "spell_frost_nova");
			assertEquals(player.getHand().get(0).getCardId(), "spell_greater_quartz_spellstone");
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_quartz_spellstone");
			playCard(context, opponent, "minion_neutral_test_1");
			playCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_frost_nova");
			assertEquals(player.getHand().get(0).getCardId(), "spell_quartz_spellstone");
		});
	}

	@Test
	public void testImmolate() {
		runGym(((context, player, opponent) -> {
			Minion watcher = playMinionCard(context, opponent, "minion_ancient_watcher");
			playCard(context, player, "minion_spellshifter");
			playCard(context, player, "spell_immolate", watcher);

			for (int i = 1; i < 5; i++) {
				if (find(context, "minion_ancient_watcher") != null) {
					assertEquals(watcher.getHp(), 5 - i * 2);
					context.endTurn();
				}
			}
		}));
	}

	@Test
	public void testHagaraTheStormbinder() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_hagara_the_stormbinder");
			shuffleToDeck(context, player, "minion_neutral_test");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(context.getTriggersAssociatedWith(player.getReference()).size(), 0, "Should not have activated");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_hagara_the_stormbinder");
			shuffleToDeck(context, player, "minion_silver_test");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(context.getTriggersAssociatedWith(player.getReference()).size(), 1, "Should have activated");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_hagara_the_stormbinder");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			playCard(context, player, "minion_earth_elemental");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 3);
			useHeroPower(context, player);
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
			playCard(context, player, "hero_hagatha_the_witch");
			playCard(context, player, "minion_earth_elemental");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
			playCard(context, player, "spell_volcano");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 2);
			playCard(context, player, "minion_earth_elemental");
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
		}, "SILVER", "SILVER");
	}

	@Test
	public void testVereesaWindrunner2() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_cloaked_huntress");
			receiveCard(context, player, "secret_freezing_trap");
			assertEquals(player.getHand().get(0).getCardId(), "secret_freezing_trap");
			assertEquals(context.getLogic().getModifiedManaCost(player, player.getHand().get(0)), 0);
			Minion vereesa = playMinionCard(context, player, "minion_vereesa_windrunner");
			assertEquals(player.getHand().get(0).getCardId(), "spell_freezing_trap");
			assertEquals(context.getLogic().getModifiedManaCost(player, player.getHand().get(0)), 0);
			destroy(context, vereesa);
			assertEquals(player.getHand().get(0).getCardId(), "secret_freezing_trap");
		});

		runGym((context, player, opponent) -> {
			List<String> secrets = Arrays.asList("freezing_trap", "misdirection", "explosive_trap",
					"bear_trap", "rat_trap", "hidden_cache", "snipe", "snake_trap", "venomstrike_trap",
					"wandering_monster", "dart_trap");
			for (String secret : secrets) {
				Card card = shuffleToDeck(context, player, "secret_" + secret);
				context.getLogic().drawCard(player.getId(), player);
				playMinionCard(context, player, "minion_vereesa_windrunner");
				assertEquals(player.getHand().get(0).getCardId(), "spell_" + secret, player.getHand().get(0).getCardId());
				assertTrue(player.getHand().get(0).hasAttribute(Attribute.SECRET), secret);
				playCard(context, player, "spell_cataclysm");
			}
		});

		runGym((context, player, opponent) -> {
			receiveCard(context, player, "secret_explosive_trap");
			receiveCard(context, player, "spell_lesser_emerald_spellstone");
			playCard(context, player, "weapon_eaglehorn_bow");
			playCard(context, player, "minion_vereesa_windrunner");
			playCard(context, player, player.getHand().get(0));
			assertEquals(player.getHand().get(0).getCardId(), "spell_emerald_spellstone");
			assertEquals(player.getWeaponZone().get(0).getDurability(), 3);
		});
	}

	@Test
	public void testScaleOfTheEarthWarder() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scale_of_the_earth_warder");
			assertEquals(player.getHero().getWeapon().getDurability(), 12);
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHero().getHp(), 30);
			assertEquals(player.getHero().getWeapon().getDurability(), 6);
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, opponent, "spell_test_deal_10", player.getHero());
			assertEquals(player.getHero().getHp(), 30);
			assertNull(player.getHero().getWeapon());
			assertTrue(wisp.isDestroyed());
		});
	}

	@Test
	public void testXalatath() {
		runGym((context, player, opponent) -> {
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 2);
			Minion maiden = playMinionCard(context, player, "minion_maiden_of_the_lake");
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 1);
			playCard(context, player, "weapon_xalatath");
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 0);
			destroy(context, maiden);
			assertEquals(costOf(context, player, player.getHeroPowerZone().get(0)), 1);
		});
	}

	@Test
	public void testTheSilverHand() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_onyxia");
			playCard(context, player, "weapon_the_silver_hand");
			playCard(context, player, "spell_level_up");
			player.getMinions().forEach(minion -> assertEquals(minion.getAttack(), minion.getBaseAttack() + 2));
		});
	}

	@Test
	public void testWarSwords() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "weapon_warswords_of_the_valarjar");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(player.getHero().getHp(), 20);
			assertEquals(player.getWeaponZone().get(0).getName(), "Warswords of the Valarjar");
		});
	}

	@Test
	public void testUlthalesh() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			Card darkPact = receiveCard(context, player, "spell_dark_pact");
			playCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_neutral_test_1");
			playCard(context, opponent, "minion_neutral_test_1");
			int actionsBefore = (int) context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(darkPact.getReference()))
					.count();
			assertEquals(actionsBefore, 2);
			playCard(context, player, "weapon_ulthalesh");
			int actionsAfter = (int) context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(darkPact.getReference()))
					.count();

			assertEquals(actionsAfter, 3);
			playCard(context, player, darkPact, opponent.getMinions().get(0));

			assertTrue(!player.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			player.setMana(10);
			Card boi = receiveCard(context, player, "minion_sanguine_reveler");
			playCard(context, player, "minion_neutral_test_1");
			Minion enemyWisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			int i = 0;
			try {
				playMinionCard(context, player, boi, enemyWisp);
			} catch (AssertionError e) {
				i++;
			}
			assertEquals(i, 1);
			playCard(context, player, "weapon_ulthalesh");

			playMinionCard(context, player, boi, enemyWisp);

			assertTrue(!player.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			Card brew = receiveCard(context, player, "minion_youthful_brewmaster");
			Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "weapon_ulthalesh");
			playMinionCard(context, player, brew, wisp);
			assertEquals(player.getHand().size(), 1);
			assertEquals(opponent.getHand().size(), 0);
		});

		for (int i = 0; i < 10; i++) {
			runGym((context, player, opponent) -> {
				Card sac = receiveCard(context, player, "spell_unwilling_sacrifice");
				Minion wisp = playMinionCard(context, opponent, "minion_neutral_test_1");
				Minion wisp2 = playMinionCard(context, opponent, "minion_neutral_test_1");
				playCard(context, player, "weapon_ulthalesh");
				playCard(context, player, sac, wisp);
				assertTrue(wisp.isDestroyed());
				assertTrue(wisp2.isDestroyed());
			});
		}
	}

	@Test
	public void testScepterOfSargerasDiluteSoulInteraction() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_test_gain_mana");
			receiveCard(context, player, "minion_neutral_test");
			receiveCard(context, player, "minion_red_test");
			playCard(context, player, "weapon_scepter_of_sargeras");
			AtomicInteger discovers = new AtomicInteger();
			overrideDiscover(context, player, discoverActions -> {
				discovers.incrementAndGet();
				assertEquals(discoverActions.size(), 3);
				return discoverActions.stream().filter(c -> c.getCard().getCardId().equals("spell_test_gain_mana")).findFirst().orElseThrow(AssertionError::new);
			});
			playCard(context, player, "spell_dilute_soul");
			assertEquals(discovers.get(), 1);
			assertEquals(player.getHand().size(), 2);
			context.endTurn();
			assertEquals(player.getHand().size(), 4);
			assertEquals(player.getHand().stream().filter(c -> c.getCardId().equals("spell_test_gain_mana")).count(), 2L);
		});
	}

	@Test
	public void testScepterOfSargeras() {
		for (int i = 0; i < 10; i++) {
			runGym((context, player, opponent) -> {
				receiveCard(context, player, "minion_target_dummy");
				receiveCard(context, player, "minion_neutral_test_1");
				receiveCard(context, player, "minion_neutral_test_1");

				playCard(context, player, "weapon_scepter_of_sargeras");
				overrideDiscover(context, player, "minion_target_dummy");
				playCard(context, player, "spell_soulfire", opponent.getHero());
				assertEquals(player.getHand().size(), 2);
				assertEquals(player.getHand().get(0).getCardId(), "minion_neutral_test_1");
				assertEquals(player.getHand().get(1).getCardId(), "minion_neutral_test_1");
			});
		}
	}

	@Test
	public void testFandralStaghelmPlagueLordInteraction() {
		runGym((context, player, opponent) -> {
			player.setMana(2);
			SpellUtils.castChildSpell(context, player, ChangeHeroPowerSpell.create("hero_power_plague_lord"), player, player);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream().count(), 3);
			playCard(context, player, "minion_fandral_staghelm");
			player.setMana(2);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream().count(), 2);
			context.performAction(player.getId(), context.getLogic().getValidActions(player.getId())
					.stream().filter(gameAction -> gameAction.getActionType().equals(ActionType.HERO_POWER)).findFirst().get());
			context.getLogic().canPlayCard(player.getId(), player.getHeroPowerZone().get(0).getReference());
			assertEquals(player.getHero().getAttack(), 3);
			assertEquals(player.getHero().getArmor(), 3);
		});
	}

	@Test
	public void testScytheOfElune() {
		runGym((context, player, opponent) -> {
			player.setMana(10);
			Card roots = receiveCard(context, player, "spell_living_roots");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 1);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);

			playCard(context, player, "weapon_scythe_of_elune");
			assertEquals(player.getWeaponZone().get(0).getDescription(), "Your Choose One effects have both options combined. Swaps each turn.");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 0);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getWeaponZone().get(0).getDescription(), "Your Choose One effects have only their first option. Swaps each turn.");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 0);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getWeaponZone().get(0).getDescription(), "Your Choose One effects have only their second option. Swaps each turn.");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 1);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 0);
			playCard(context, player, "weapon_rusty_hook");
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.NONE))
					.count(), 1);
			assertEquals(context.getLogic().getValidActions(player.getId()).stream()
					.filter(gameAction -> gameAction.getSourceReference().equals(roots.getReference()))
					.filter(gameAction -> gameAction.getTargetRequirement().equals(TargetSelection.ANY))
					.count(), 2);
		});
	}

	@Test
	public void testEbonChill() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_ebonchill");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, player, "spell_frost_nova");
			for (Minion minion : opponent.getMinions()) {
				assertEquals(minion.getHp(), minion.getMaxHp() - 3);
			}
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_ebonchill");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, opponent, "minion_unpowered_steambot");
			playCard(context, player, "spell_frost_nova");
			for (int i = 0; i < 6; i++) {
				assertEquals(opponent.getMinions().get(i).getHp(), 6);
			}
		});
	}

	@Test
	public void testFelomelorn() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_felomelorn");
			receiveCard(context, player, "spell_flamestrike");
			shuffleToDeck(context, player, "spell_flamestrike");
			playCard(context, player, "minion_kobold_geomancer");
			Minion dummy = playMinionCard(context, opponent, "minion_unpowered_steambot");
			context.fireGameEvent(new TurnEndEvent(context, player.getId()));
			assertEquals(dummy.getHp(), 4);
			assertEquals(player.getHand().size() + player.getDeck().size(), 1);
		});
	}

	@Test
	public void testTuure() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_tuure");
			for (int i = 0; i < 10; i++) {
				context.fireGameEvent(new TurnStartEvent(context, player.getId()));
			}
		});
	}

	@Test
	public void testFangsOfAshmane() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_fangs_of_ashmane_artifact");
			playCard(context, player, "spell_bite");
			assertEquals(player.getHero().getAttack(), 8);
			destroy(context, player.getWeaponZone().get(0));
			assertEquals(player.getHero().getAttack(), 4);
		});
	}

	@Test
	public void testTheDreadblade() {
		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			assertFalse(dreadBlade.hasAttribute(Attribute.LIFESTEAL));
			playCard(context, player, "weapon_spectral_cutlass");
			assertTrue(dreadBlade.hasAttribute(Attribute.LIFESTEAL));
			playCard(context, player, dreadBlade);
			assertTrue(player.getWeaponZone().get(0).hasAttribute(Attribute.LIFESTEAL));
		}, "BLACK", "BLACK");

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			assertFalse(dreadBlade.hasAttribute(Attribute.BATTLECRY));
			playCard(context, player, "weapon_jade_claws");
			assertTrue(dreadBlade.hasAttribute(Attribute.BATTLECRY));
			playCard(context, player, dreadBlade);
			assertEquals(player.getMinions().size(), 2);
		}, "BLACK", "BLACK");

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			assertFalse(dreadBlade.hasAttribute(Attribute.DEATHRATTLES));
			playCard(context, player, "weapon_hammer_of_twilight");
			assertTrue(dreadBlade.hasAttribute(Attribute.DEATHRATTLES));
			playCard(context, player, dreadBlade);
			destroy(context, player.getWeaponZone().get(0));
			assertEquals(player.getMinions().size(), 2);
		}, "BLACK", "BLACK");

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			Card shard = receiveCard(context, player, "weapon_obsidian_shard");
			assertEquals(costOf(context, player, dreadBlade), 7);
			assertEquals(costOf(context, player, shard), 4);
			playCard(context, player, "spell_arcane_missiles");
			playCard(context, player, "spell_arcane_missiles");
			assertEquals(costOf(context, player, dreadBlade), 7);
			assertEquals(costOf(context, player, shard), 2);
			playCard(context, player, shard);
			assertEquals(costOf(context, player, dreadBlade), 5);
			playCard(context, player, "weapon_obsidian_shard");
			assertEquals(costOf(context, player, dreadBlade), 3);
			playCard(context, player, "spell_arcane_missiles");
			assertEquals(costOf(context, player, dreadBlade), 1);
		}, "BLACK", "BLACK");

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			receiveCard(context, player, "minion_voidlord");
			receiveCard(context, player, "minion_voidlord");
			receiveCard(context, player, "minion_voidlord");
			receiveCard(context, player, "minion_voidlord");
			playCard(context, player, "weapon_skull_of_the_manari");
			context.fireGameEvent(new TurnStartEvent(context, player.getId()));
			assertEquals(player.getMinions().size(), 1);
			playCard(context, player, dreadBlade);
			context.fireGameEvent(new TurnStartEvent(context, player.getId()));
			assertEquals(player.getMinions().size(), 2);
		});

		runGym((context, player, opponent) -> {
			Card dreadBlade = receiveCard(context, player, "weapon_the_dreadblade");
			playCard(context, player, "weapon_candleshot");
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
			destroy(context, player.getWeaponZone().get(0));
			assertFalse(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
			playCard(context, player, dreadBlade);
			assertTrue(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
			destroy(context, player.getWeaponZone().get(0));
			assertFalse(player.getHero().hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
		});
	}

	@Test
	public void testEchoOfGuldan() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "token_echo_of_guldan");
			int hp = player.getHero().getHp();
			playMinionCard(context, player, "minion_test_3_2");
			assertEquals(player.getHero().getHp(), hp - 2);
		});
	}

	@Test
	public void testEchoOfMalfurion() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2");
			Card boulderfist = receiveCard(context, player, "minion_boulderfist_ogre");
			Minion echo = playMinionCard(context, player, "token_echo_of_malfurion");
			assertEquals(echo.getAttack(), boulderfist.getAttack() + echo.getBaseAttack());
			assertEquals(echo.getHp(), boulderfist.getHp() + echo.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			Minion echo = playMinionCard(context, player, "token_echo_of_malfurion");
			assertEquals(echo.getAttack(), echo.getBaseAttack());
			assertEquals(echo.getHp(), echo.getBaseHp());
		});
	}

	@Test
	public void testEmeraldDreamEscapeFromDurnholdeDesolationOfKareshInSameGame() {
		runGym((context, player, opponent) -> {
			Minion emeraldDream = playMinionCard(context, player, "permanent_the_emerald_dream");
			Minion escapeFromDurnholde = playMinionCard(context, player, "permanent_escape_from_durnholde");
			Minion desolationOfKaresh = playMinionCard(context, player, "permanent_desolation_of_karesh");
			Minion twoTwoWisp = playMinionCard(context, player, "minion_neutral_test_1");
			Minion threeThreeWisp = playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_undercity_valiant", opponent.getHero());
			assertEquals(twoTwoWisp.getAttack(), twoTwoWisp.getBaseAttack() + 1);
			assertEquals(twoTwoWisp.getHp(), twoTwoWisp.getBaseHp() + 1);
			assertEquals(threeThreeWisp.getAttack(), threeThreeWisp.getBaseAttack() + 2);
			assertEquals(threeThreeWisp.getHp(), threeThreeWisp.getBaseHp() + 2);
			assertEquals(desolationOfKaresh.getAttributeValue(Attribute.RESERVED_INTEGER_1), 4);
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "spell_test_gain_mana");
			}
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testEndOfTheLineSapInteraction() {
		// Ensure minion without taunt no longer has taunt after End of the Line 'd + Sap'ped
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, opponent, "minion_test_3_2");
			playCard(context, opponent, "spell_end_of_the_line");
			Minion beast = playMinionCard(context, opponent, opponent.getHand().get(0));
			assertTrue(beast.hasAttribute(Attribute.TAUNT));
			assertEquals(beast.getAttack(), beast.getBaseAttack() + 5);
			context.endTurn();
			playCard(context, player, "spell_test_return_to_hand", beast);
			context.endTurn();
			beast = playMinionCard(context, opponent, opponent.getHand().get(0));
			assertFalse(beast.hasAttribute(Attribute.TAUNT));
			assertEquals(beast.getAttack(), beast.getBaseAttack());
		});
	}

	@Test
	public void testFarseerNobundo() {
		// Test that battlecries from the hand are triggered.
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInDeck = putOnTopOfDeck(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_king_mukla");
			playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			assertEquals(onBoardBefore.getAttack(), 1);
			assertEquals(onBoardBefore.getHp(), 1);
			assertEquals(opponent.getHand().size(), 2, "The opponent should have two bananas at the moment.");
			playCard(context, player, startedInHand);
			assertEquals(opponent.getHand().size(), 4, "The opponent should have four bananas.");
			context.endTurn();
			context.endTurn();
			assertEquals(startedInDeck.getZone(), Zones.HAND);
			playCard(context, player, startedInDeck);
			assertEquals(opponent.getHand().size(), 6);
		});

		// Test auras and triggers
		runGym((context, player, opponent) -> {
			int stormwinds = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_stormwind_champion");
			stormwinds++;
			playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			stormwinds++;
			assertEquals(onBoardBefore.getAttack(), onBoardBefore.getBaseAttack() + stormwinds - 1);
			assertEquals(onBoardBefore.getHp(), onBoardBefore.getBaseHp() + stormwinds - 1);
			playCard(context, player, startedInHand);
			stormwinds++;
			assertEquals(onBoardBefore.getAttack(), onBoardBefore.getBaseAttack() + stormwinds - 1);
			assertEquals(onBoardBefore.getHp(), onBoardBefore.getBaseHp() + stormwinds - 1);
		});

		runGym((context, player, opponent) -> {
			int clerics = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_northshire_cleric");
			clerics++;
			Minion damaged = playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			clerics++;
			playCard(context, player, startedInHand);
			clerics++;
			damaged.setHp(damaged.getHp() - 1);
			assertTrue(damaged.isWounded());
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_test_3_2");
			}
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "hero_power_heal", damaged);
			assertEquals(player.getHand().size(), clerics);
		});

		// Test deathrattle
		runGym((context, player, opponent) -> {
			int lootHoarders = 0;
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Card startedInHand = receiveCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_loot_hoarder");
			lootHoarders++;
			Minion damaged = playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			lootHoarders++;
			playCard(context, player, startedInHand);
			lootHoarders++;
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_test_3_2");
			}
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_twisting_nether");
			assertEquals(player.getHand().size(), lootHoarders);
		});

		// Test copies text attribute of source card even when silenced
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
			playCard(context, player, "spell_silence", copyCard);
			assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCard(context, player, "spell_silence", copyCard);
			playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			assertTrue(onBoardBefore.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Test does not copy non-text attributes (buffs or whatever)
		runGym((context, player, opponent) -> {
			Minion onBoardBefore = playMinionCard(context, player, "token_searing_totem");
			Minion copyCard = playMinionCard(context, player, "minion_argent_Squire");
			playCard(context, player, "spell_windfury", copyCard);
			playMinionCard(context, player, "minion_farseer_nobundo", copyCard);
			assertFalse(onBoardBefore.hasAttribute(Attribute.WINDFURY));
		});
	}

	@Test
	public void testStablePortal() {
		// Correctly adds a Beast to player's hand with a mana cost 2 less
		runGym((context, player, opponent) -> {
			GameLogic spiedLogic = spy(context.getLogic());
			context.setLogic(spiedLogic);

			Mockito.doAnswer(invocation ->
					CardCatalogue.getCardById("minion_malorne"))
					.when(spiedLogic)
					.removeRandom(anyList());

			playCard(context, player, "spell_stable_portal");
			Card card = player.getHand().get(0);
			assertEquals(card.getCardId(), "minion_malorne");
			int baseMana = card.getBaseManaCost();
			assertEquals(baseMana, 7);
			assertEquals(card.getRace(), "BEAST");
			assertEquals(costOf(context, player, card), baseMana - 2);
		});
	}

	@Test
	public void testGnarlRoot() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 8; i++) {
				receiveCard(context, player, "minion_neutral_test_1");
			}
			playCard(context, player, "minion_gnarlroot");

			assertEquals(player.getHand().get(8).getCardId(), "token_treant");
			assertEquals(player.getHand().get(9).getCardId(), "token_treant");

			for (int i = 1; i < 4; i++) {
				assertEquals(player.getMinions().get(i).getSourceCard().getCardId(), "token_treant");
			}
		});
	}

	@Test
	public void testFlame() {
		runGym(((context, player, opponent) -> {
			receiveCard(context, player, "minion_rebellious_flame");
			playCard(context, player, "minion_neutral_test_1");
			assertEquals(player.getHand().get(0).getCardId(), "spell_rebellious_flame");
			playCard(context, player, "spell_arcane_explosion");
			assertEquals(player.getHand().get(0).getCardId(), "minion_rebellious_flame");
			playCard(context, player, player.getHand().get(0));
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_rebellious_flame");
			receiveCard(context, player, "spell_rebellious_flame");
			playCard(context, player, player.getHand().get(0), opponent.getHero());
			assertEquals(player.getHand().size(), 0);
		}));
	}

	@Test
	public void testTriplicate() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_triplicate", wisp);
			assertTrue(player.getMinions().size() > 1);
			assertTrue(player.getHand().size() > 0);
			assertTrue(player.getDeck().size() > 0);
		});
	}

	@Test
	public void testPolyDragon() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_polymorph_dragon", wisp);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_whelp");
			playCard(context, player, player.getHand().peekFirst(), player.getMinions().get(0));
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_1212dragon");
		});
	}

	@Test
	public void testForeverAStudent() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_forever_a_student", beast);
			Minion beast2 = playMinionCard(context, player, "minion_test_3_2");
			assertEquals(beast.getAttack(), beast.getBaseAttack() + 1);
			assertEquals(beast.getHp(), beast.getBaseHp() + 1);
			assertEquals(beast2.getAttack(), beast2.getBaseAttack(), "The newly summoned minion should not be the benefit of the buff.");
			assertEquals(beast2.getHp(), beast2.getBaseHp());
			context.endTurn();
			playCard(context, opponent, "minion_test_3_2");
			assertEquals(beast.getAttack(), beast.getBaseAttack() + 1, "Opponent summoning a minion should not affect the stats of the enchanted minion.");
			assertEquals(beast.getHp(), beast.getBaseHp() + 1);
		});
	}

	@Test
	public void testFrenziedDiabolist() {
		runGym((context, player, opponent) -> {
			Card card1 = receiveCard(context, player, "minion_test_3_2");
			Card card2 = receiveCard(context, player, "minion_test_3_2");
			playCard(context, player, "minion_doomguard");
			assertTrue(card1.hasAttribute(Attribute.DISCARDED));
			assertTrue(card2.hasAttribute(Attribute.DISCARDED));
			CountDownLatch latch = new CountDownLatch(1);
			overrideDiscover(context, player, discoverActions -> {
				latch.countDown();
				assertEquals(discoverActions.size(), 1, "Should not show duplicate cards due to discover rules");
				assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_test_3_2");
				return discoverActions.get(0);
			});
			playCard(context, player, "minion_frenzied_diabolist");
			assertEquals(latch.getCount(), 0);
		});
	}

	@Test
	public void testDreadCaptainBones() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_wicked_knife");
			final Weapon weapon = player.getWeaponZone().get(0);
			assertEquals(weapon.getDurability(), weapon.getBaseDurability());
			playCard(context, player, "minion_dread_captain_bones");
			assertEquals(weapon.getDurability(), weapon.getBaseDurability() + 1);
		});
	}

	@Test
	public void testFreya() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_freya");
			Minion nordrassil = player.getMinions().get(1);
			assertEquals(nordrassil.getSourceCard().getCardId(), "permanent_seed_of_nordrassil");
			assertEquals(nordrassil.getAttributeValue(Attribute.RESERVED_INTEGER_1), 0, "Freya should not trigger Seed");
			Minion beast = playMinionCard(context, player, "minion_test_3_2");
			assertEquals(nordrassil.getAttributeValue(Attribute.RESERVED_INTEGER_1), beast.getAttack() + beast.getHp());
			for (int i = 0; i < 2; i++) {
				playCard(context, player, "minion_faceless_behemoth");
			}

			assertEquals(nordrassil.transformResolved(context).getSourceCard().getCardId(), "token_nordrassil", "Seed transformed into Nordrassil");
		});
	}

	@Test
	public void testFrostBomb() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			Minion other = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_frost_bomb", target);
			assertTrue(target.hasAttribute(Attribute.FROZEN));
			assertFalse(other.hasAttribute(Attribute.FROZEN));
			assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			assertTrue(target.hasAttribute(Attribute.FROZEN));
			assertFalse(other.hasAttribute(Attribute.FROZEN));
			assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			assertFalse(target.hasAttribute(Attribute.FROZEN));
			assertTrue(other.hasAttribute(Attribute.FROZEN));
			assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			Minion other = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			Minion friendly = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_frost_bomb", target);
			assertTrue(target.hasAttribute(Attribute.FROZEN));
			assertFalse(other.hasAttribute(Attribute.FROZEN));
			assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", target);
			assertTrue(target.isDestroyed());
			assertFalse(other.hasAttribute(Attribute.FROZEN));
			assertFalse(friendly.hasAttribute(Attribute.FROZEN));
			context.endTurn();
			assertFalse(other.hasAttribute(Attribute.FROZEN));
			assertFalse(friendly.hasAttribute(Attribute.FROZEN));
		});
	}

	@Test
	public void testInfiniteTimereaver() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			Card toDraw = putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "minion_infinite_timereaver");
			playCard(context, player, "spell_test_deal_6", target);
			assertEquals(player.getHand().get(0), toDraw);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			Card toDraw = putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "minion_infinite_timereaver");
			playCard(context, player, "spell_test_deal_4_to_enemies");
			assertEquals(player.getHand().get(0), toDraw);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "minion_infinite_timereaver");
			playCard(context, player, "spell_test_deal_1", target);
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_test_deal_1", target);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testKthirCorruptor() {
		runGym((context, player, opponent) -> {
			Minion kthir = playMinionCard(context, player, "minion_kthir_corruptor");
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(kthir.getAttack(), kthir.getBaseAttack() + 2);
			assertEquals(kthir.getMaxHp(), kthir.getBaseHp() + 2);
			playCard(context, player, "spell_test_summon_tokens");
			assertEquals(kthir.getAttack(), kthir.getBaseAttack() + 2);
			assertEquals(kthir.getMaxHp(), kthir.getBaseHp() + 2);
			playCard(context, player, "secret_dart_trap");
			assertEquals(kthir.getAttack(), kthir.getBaseAttack() + 4);
			assertEquals(kthir.getMaxHp(), kthir.getBaseHp() + 4);
		});
	}

	@Test
	public void testLieInWait() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_lie_in_wait");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, charger, player.getHero());

			assertEquals(player.getWeaponZone().get(0).getDurability(),
					CardCatalogue.getCardById("weapon_eaglehorn_bow").getBaseDurability() - 1,
					"Eaglehorn Bow loses durability because the secret triggered before it was in play.");
			assertTrue(charger.isDestroyed());
		});
	}

	@Test
	public void testMasterSorcerer() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			shuffleToDeck(context, player, "minion_test_3_2");
			playCard(context, player, "spell_test_deal_6", target);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_master_sorcerer");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			shuffleToDeck(context, player, "minion_test_3_2");
			playCard(context, player, "spell_test_deal_6", target);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getDeck().size(), 1);
		});
	}

	@Test
	public void testMetamagic() {
		// Costs (2) less.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_quickened");
			playCard(context, player, "spell_metamagic");
			player.setMaxMana(10);
			player.setMana(10);
			Card explosion = receiveCard(context, player, "spell_arcane_explosion");
			assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost() - 2);
			playCard(context, player, explosion);
			explosion = receiveCard(context, player, "spell_arcane_explosion");
			assertEquals(costOf(context, player, explosion), explosion.getBaseManaCost());
		});

		// Deals 1 damage to all enemy minions.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion villager = playMinionCard(context, opponent, "minion_possessed_villager");
			Minion beast = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			overrideDiscover(context, player, "spell_unbounded");
			playCard(context, player, "spell_metamagic");
			assertFalse(villager.isDestroyed());
			assertEquals(beast.getHp(), beast.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCard(context, player, "spell_arcane_explosion");
			assertTrue(villager.isDestroyed());
			assertTrue(beast.isDestroyed(), "Two damage should have been dealt in this sequence.");
			assertEquals(opponent.getMinions().size(), 1, "There should just be a shadowbeast, because the additional spell effect does not happen in its own sequence.");
			context.endTurn();
			beast = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			assertEquals(opponent.getMinions().size(), 2, "There should be a shadowbeast and a beast.");
			playCard(context, player, "spell_arcane_explosion");
			assertFalse(beast.isDestroyed(), "The next arcane explosion should not have destroyed the beast since it only dealt 1 damage");
			assertEquals(opponent.getMinions().size(), 1, "But the Shadowbeast should have been destroyed.");
		});

		// Returns to your deck after you cast it.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_memorized");
			playCard(context, player, "spell_metamagic");
			playCard(context, player, "minion_test_3_2");
			assertEquals(player.getDeck().size(), 0, "We should not have shuffled a minion card into the deck.");
			context.endTurn();
			// We should still apply the effect to the next spell the player cast
			playCard(context, opponent, "spell_test_gain_mana");
			assertEquals(player.getDeck().size(), 0, "The opponent's spell should not have been shuffled.");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			assertEquals(player.getDeck().get(0).getCardId(), "spell_arcane_explosion");
			playCard(context, player, "spell_arcane_explosion");
			assertEquals(player.getDeck().size(), 1, "Only one copy of the card should have been shuffled.");
		});

		// Freezes two random enemies.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_chilled");
			playCard(context, player, "spell_metamagic");
			context.endTurn();
			Minion minion1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion minion2 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_arcane_explosion");
			assertTrue(minion1.hasAttribute(Attribute.FROZEN));
			assertTrue(minion2.hasAttribute(Attribute.FROZEN));
			assertEquals(minion1.getHp(), minion1.getBaseHp() - 1);
			assertEquals(minion2.getHp(), minion1.getBaseHp() - 1);
		});

		// The next spell you cast costs (2) more and has Spell Damage +2.
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_enhanced");
			playCard(context, player, "spell_metamagic");
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost() + 2);
			assertEquals(player.getAttributeValue(Attribute.SPELL_DAMAGE), 2);
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 8);
			fireball = receiveCard(context, player, "spell_test_deal_6");
			assertEquals(costOf(context, player, fireball), fireball.getBaseManaCost(), "The 2nd spell should not be more expensive");
			opponentHp = opponent.getHero().getHp();
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 2nd spell should not have gotten spell damage +2.");
			opponentHp = opponent.getHero().getHp();
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 6, "The 3nd spell should not have gotten spell damage -2.");
		});

		// Deals 3 damage to a random enemy minion.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion chillwind = playMinionCard(context, opponent, "minion_test_4_5");
			context.endTurn();
			overrideDiscover(context, player, "spell_empowered");
			playCard(context, player, "spell_metamagic");
			assertEquals(chillwind.getHp(), chillwind.getBaseHp(), "Metamagic should not have triggered its own effect.");
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3);
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(chillwind.getHp(), chillwind.getBaseHp() - 3, "The empowered effect should have expired");
		});
	}

	@Test
	public void testMetamagicTemporalFluxInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				shuffleToDeck(context, player, "spell_test_gain_mana");
			}
			overrideDiscover(context, player, "spell_enhanced");
			playCard(context, player, "spell_metamagic");
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_temporal_flux", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			assertEquals(player.getHand().size(), 3);
		});
	}

	@Test
	public void testNazmiriStalker() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_nazmiri_stalker");
			Minion target1 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion target2 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion target3 = playMinionCard(context, player, "minion_neutral_test_1");
			// Cast a +1/+2 on a target
			playCard(context, player, "spell_sound_the_bells", target2);
			assertEquals(target1.getAttack(), target1.getBaseAttack() + 1);
			assertEquals(target1.getMaxHp(), target1.getBaseHp() + 2);
			assertEquals(target2.getAttack(), target1.getBaseAttack());
			assertEquals(target2.getMaxHp(), target1.getBaseHp());
			assertEquals(target3.getAttack(), target1.getBaseAttack() + 1);
			assertEquals(target3.getMaxHp(), target1.getBaseHp() + 2);
		});
	}

	@Test
	public void testNexusKingSalhadaar() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "minion_nexus_king_salhadaar");
			assertEquals(player.getMinions().size(), 1);
			assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 1));
		});
	}

	@Test
	public void testNickOfTime() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			shuffleToDeck(context, player, "minion_nick_of_time");
			context.endTurn();
			assertEquals(player.getMinions().stream().map(Minion::getSourceCard).map(Card::getCardId).filter(cid -> cid.equals("token_silver_hand_recruit")).count(), 2L);
		});
	}

	@Test
	public void testOwnWorstEnemey() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_own_worst_enemy");
			Minion target = playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			Minion source = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, source, target);
			assertTrue(source.isDestroyed());
			assertFalse(target.isDestroyed());
			assertTrue(player.getGraveyard().stream().anyMatch(c -> c.getEntityType() == EntityType.MINION
					&& c.getSourceCard().getCardId().equals("minion_charge_test")));
		});
	}

	@Test
	public void testPowerTrip() {
		// We reach turn 10 so we have 10 mana, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 10; i++) {
				context.endTurn();
				context.endTurn();
			}
			assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Our opponent gives us 10 mana somehow, we die
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			assertEquals(player.getQuests().get(0).getSourceCard().getCardId(), "spell_power_trip");
			for (int i = 0; i < 2; i++) {
				context.endTurn();
				context.endTurn();
			}
			context.endTurn();
			assertEquals(player.getMaxMana(), 3);
			for (int i = 0; i < 7; i++) {
				playCard(context, opponent, "minion_arcane_golem");
				assertEquals(player.getMaxMana(), 3 + i + 1);
			}
			assertEquals(player.getMaxMana(), 10);
			assertTrue(context.getLogic().getMatchResult(player, opponent) != GameStatus.RUNNING);
		});

		// Check that minions have +1/+1
		runGym((context, player, opponent) -> {
			Minion beasttest32 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_power_trip");
			assertEquals(beasttest32.getAttack(), beasttest32.getBaseAttack() + 1);
			assertEquals(beasttest32.getHp(), beasttest32.getBaseHp() + 1);
			context.endTurn();
			Minion opponentMinion = playMinionCard(context, player, "minion_test_4_5");
			context.endTurn();
			playCard(context, player, "spell_mind_control", opponentMinion);
			assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack() + 1);
			assertEquals(opponentMinion.getHp(), opponentMinion.getBaseHp() + 1);
		});

		// Check that Saronite Chain Gang has correct stats
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_power_trip");
			playCard(context, player, "minion_saronite_chain_gang");
			player.getMinions().forEach(m -> assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> assertEquals(m.getHp(), 4));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_saronite_chain_gang");
			playCard(context, player, "spell_power_trip");
			player.getMinions().forEach(m -> assertEquals(m.getAttack(), 3));
			player.getMinions().forEach(m -> assertEquals(m.getHp(), 4));
		});
	}


	@Test
	public void testRafaamSupremeThief() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_test_gain_mana");
			shuffleToDeck(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "hero_rafaam_supreme_thief");
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test_1");
			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getDeck().get(0).getCardId(), "minion_neutral_test_1");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "hero_rafaam_supreme_thief");
			useHeroPower(context, player, target.getReference());
			destroy(context, target);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "hero_rafaam_supreme_thief");
			playCard(context, player, "spell_mind_control", target);
			useHeroPower(context, player, target.getReference());
			destroy(context, target);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "hero_rafaam_supreme_thief");
			useHeroPower(context, player, target.getReference());
			context.endTurn();
			playCard(context, opponent, "spell_mind_control", target);
			destroy(context, target);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_neutral_test");
		});
	}

	@Test
	public void testRebelliousFlame() {
		runGym((context, player, opponent) -> {
			Card rebelliousFlame = receiveCard(context, player, "minion_rebellious_flame");
			playCard(context, player, rebelliousFlame);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_rebellious_flame");
		});

		runGym((context, player, opponent) -> {
			Card rebelliousFlame = receiveCard(context, player, "minion_rebellious_flame");
			destroy(context, playMinionCard(context, player, "minion_test_3_2"));
			Card spellRebelliousFlame = (Card) rebelliousFlame.transformResolved(context);
			assertEquals(spellRebelliousFlame.getCardId(), "spell_rebellious_flame");
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, spellRebelliousFlame, opponent.getHero());
			assertEquals(player.getMinions().size(), 0);
			assertEquals(opponent.getHero().getHp(), opponentHp - 3);
		});
	}

	@Test
	public void testRiseOfTheAncientOnesChange() {
		runGym((context, player, opponent) -> {
			Card rise = receiveCard(context, player, "spell_rise_of_the_ancient_ones");
			assertEquals(context.getLogic().getModifiedManaCost(player, rise), 30);
			playCard(context, player, "minion_onyxia");
			playCard(context, opponent, "minion_onyxia");
			playCard(context, player, "spell_twisting_nether");
			assertEquals(context.getLogic().getModifiedManaCost(player, rise), 16);
		});
	}

	@Test
	public void testSageOfFoursight() {
		runGym((context, player, opponent) -> {
			Minion sage = playMinionCard(context, player, "minion_sage_of_foursight");
			assertEquals(sage.getAttack(), sage.getBaseAttack(), "Sage should not buff itself.");
			assertEquals(sage.getHp(), sage.getBaseHp(), "Sage should not buff itself.");
			Card beastCard = CardCatalogue.getCardById("minion_test_3_2");
			context.getLogic().receiveCard(player.getId(), beastCard);
			assertEquals(costOf(context, player, beastCard), beastCard.getBaseManaCost() + 4, "beast should cost more because it's the next card the player will play.");

			// It should work with a one turn gap in the middle
			context.endTurn();
			context.endTurn();

			Minion beast = playMinionCard(context, player, beastCard);
			assertEquals(beast.getAttack(), beast.getBaseAttack() + 4, "beast should be buffed.");
			assertEquals(beast.getHp(), beast.getBaseHp() + 4, "beast should be buffed.");
			Card beastCard2 = CardCatalogue.getCardById("minion_test_3_2");
			context.getLogic().receiveCard(player.getId(), beastCard2);
			assertEquals(costOf(context, player, beastCard), beastCard.getBaseManaCost(), "beast 2 should not cost more.");
			Minion beast2 = playMinionCard(context, player, beastCard2);
			assertEquals(beast2.getAttack(), beast2.getBaseAttack(), "The second beast should not be buffed");
			assertEquals(beast2.getHp(), beast2.getBaseHp(), "The second beast should not be buffed");
		});
	}

	@Test
	public void testScavengerThrun() {
		runGym((context, player, opponent) -> {
			Minion beast1 = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion scavengerThrun = playMinionCard(context, player, "minion_scavenger_thrun");
			Minion beast2 = playMinionCard(context, player, "minion_test_3_2_beast");
			Minion killThis = playMinionCard(context, player, "minion_test_3_2_beast");
			AtomicReference<String> adapted = new AtomicReference<>(null);
			overrideDiscover(context, player, discoverActions -> {
				adapted.set(discoverActions.get(0).getCard().getName());
				return discoverActions.get(0);
			});
			destroy(context, killThis);
			assertNotAdapted(adapted.get(), scavengerThrun);
			assertAdapted(adapted.get(), beast1);
			assertAdapted(adapted.get(), beast2);
		});
	}

	@Test
	public void testScorpidStinger() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion flipper = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, player, player.getHero(), flipper);
			assertTrue(player.getHand().containsCard("spell_inner_rage"));
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scorpid_stinger");
			context.endTurn();
			Minion beast = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			attack(context, player, player.getHero(), beast);
			assertFalse(player.getHand().containsCard("spell_inner_rage"));
		});
	}

	@Test
	public void testSecretGarden() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_test_3_2");
			}
			playCard(context, player, "secret_secret_garden");
			context.endTurn();
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_test_3_2");
			}
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getHand().size(), 0);
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHand().size(), 3);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_test_3_2");
			}
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_secret_garden");
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "minion_test_3_2");
			}
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getHand().size(), 0);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playMinionCard(context, opponent, "minion_novice_engineer");
			assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testShadowOfThePast() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "minion_charge_test");
			playCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			Minion shadow = playMinionCard(context, player, "minion_shadow_of_the_past");
			playCard(context, player, "minion_boulderfist_ogre");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", shadow);
			assertEquals(player.getHand().get(0).getCardId(), "spell_test_deal_6");
		});
	}

	@Test
	public void testShadowhornStag() {
		runGym((context, player, opponent) -> {
			Minion stag = playMinionCard(context, player, "minion_shadowhorn_stag");
			context.getLogic().setHpAndMaxHp(stag, 100);
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			assertTrue(stag.canAttackThisTurn());
			assertTrue(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> ga.getSourceReference().equals(stag.getReference())));
			attack(context, player, stag, target1);
			assertTrue(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> ga.getSourceReference().equals(stag.getReference())));
			attack(context, player, stag, target2);
			assertTrue(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> ga.getSourceReference().equals(stag.getReference())));
			attack(context, player, stag, opponent.getHero());
			assertFalse(context.getLogic().getValidActions(player.getId()).stream().anyMatch(ga -> {
				EntityReference sourceReference = ga.getSourceReference();
				return sourceReference != null && sourceReference.equals(stag.getReference());
			}));
		});
	}

	@Test
	public void testShieldOfNature() {
		runGym((context, player, opponent) -> {
			// Using life tap with shield of nature should not stack overflow
			playCard(context, player, "weapon_shield_of_nature");
			Weapon shield = player.getWeaponZone().get(0);
			player.setMana(2);
			context.performAction(player.getId(), player.getHero().getHeroPower().play());
			// It should have run out of durability and been put to the graveyard
			assertEquals(shield.getZone(), Zones.GRAVEYARD);
		}, "VIOLET", "VIOLET");
	}

	@Test
	public void testSilvermoonOperative() {
		runGym((context, player, opponent) -> {
			Card silvermoonCard = receiveCard(context, player, "minion_silvermoon_operative");
			assertEquals(silvermoonCard.getAttributeValue(Attribute.RECEIVED_ON_TURN), context.getTurn());
			Minion silvermoon = playMinionCard(context, player, silvermoonCard);
			assertEquals(silvermoon.getAttack(), silvermoon.getBaseAttack() + 2, "Did buff");
		});
	}

	@Test
	public void testSolarPower() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "spell_test_gain_mana");
			playCard(context, player, "spell_solar_power");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_test_gain_mana");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getHand().get(1).getCardId(), "spell_test_gain_mana");
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
		});
	}

	@Test
	public void testSpaceMoorine() {
		runGym((context, player, opponent) -> {
			Minion spaceMoorine = playMinionCard(context, player, "minion_space_moorine");
			assertFalse(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			playCard(context, player, "spell_iron_hide");
			assertTrue(spaceMoorine.hasAttribute(Attribute.AURA_TAUNT));
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			assertTrue(context.getValidActions().stream().filter(va -> va.getActionType() == ActionType.PHYSICAL_ATTACK)
					.allMatch(t -> t.getTargetReference().equals(spaceMoorine.getReference())));
		});
	}

	@Test
	public void testSpikeToedBooterang() {
		// Attacks a opponent's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, opponent, "minion_river_crocolisk");
			context.endTurn();
			playCard(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			assertEquals(opponent.getMinions().get(0).getHp(), 1);
		});

		// Attacks player's minion twice
		runGym((context, player, opponent) -> {
			Minion riverCrocolisk = playMinionCard(context, player, "minion_river_crocolisk");
			playCard(context, player, "spell_spike_toed_booterang", riverCrocolisk);
			assertEquals(player.getMinions().get(0).getHp(), 1);
		});

		// Defeats a Divine Shield
		runGym((context, player, opponent) -> {
			Minion silvermoonGuardian = playMinionCard(context, opponent, "minion_silvermoon_guardian");
			context.endTurn();
			playCard(context, player, "spell_spike_toed_booterang", silvermoonGuardian);
			assertEquals(opponent.getMinions().get(0).getHp(), 2);
		});

		// If attacking Imp Gang Boss, summons two 1/1 Imps for opponent
		runGym((context, player, opponent) -> {
			Minion impGangBoss = playMinionCard(context, opponent, "minion_imp_gang_boss");
			context.endTurn();
			playCard(context, player, "spell_spike_toed_booterang", impGangBoss);
			assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_imp");
			assertEquals(opponent.getMinions().get(2).getSourceCard().getCardId(), "token_imp");
		});
	}

	@Test
	public void testStitches() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion stitches = playMinionCard(context, player, "minion_stitches", target);
			assertTrue(target.isDestroyed());
			assertEquals(stitches.getAttack(), target.getBaseAttack() + stitches.getBaseAttack());
			assertEquals(stitches.getMaxHp(), target.getBaseHp() + stitches.getBaseHp());
		});
	}

	@Test
	public void testSunlance() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test_1");
			for (int i = 0; i < 4; i++) {
				shuffleToDeck(context, player, "spell_test_gain_mana");
			}
			playCard(context, player, "spell_sunlance", target);
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 1);
		});
	}

	@Test
	public void testTerrorscaleStalkerBlinkDogInteraction() {
		runGym((context, player, opponent) -> {
			// Deathrattle: Give a random friendly Beast \"Deathrattle: Summon a Blink Dog\"
			Minion blinkDog = playMinionCard(context, player, "minion_blink_dog");
			playCard(context, player, "minion_terrorscale_stalker");
			// Now Blink Dog summons a blink dog and gives a randomly friendly beast an extra deathrattle
			playCard(context, player, "spell_test_deal_6", blinkDog);
			assertEquals(player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals("minion_blink_dog")).count(), 1L);
		});
	}

	@Test
	public void testTheBigGameHunt() {
		runGym((context, player, opponent) -> {
			Minion bigGameHunt = playMinionCard(context, player, "permanent_the_big_game_hunt");
			int elapsedLocalPlayerTurns = 0;
			Minion beast1 = playMinionCard(context, player, "minion_test_3_2");
			Minion beast2 = playMinionCard(context, player, "minion_test_3_2");
			Minion beast3 = playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			elapsedLocalPlayerTurns++;
			Minion beast4 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion beast5 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion beast6 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			// one point for player
			attack(context, player, beast1, beast4);
			context.endTurn();
			elapsedLocalPlayerTurns++;
			// two points for opponent
			attack(context, opponent, beast5, beast2);
			attack(context, opponent, beast6, beast3);
			context.endTurn();
			for (int i = elapsedLocalPlayerTurns; i < 4; i++) {
				context.endTurn();
				context.endTurn();
			}
			assertTrue(bigGameHunt.isDestroyed());
			// Should be a total of -1
			assertEquals(bigGameHunt.getAttributeValue(Attribute.RESERVED_INTEGER_1), -1);
			Minion kingBangalash1 = player.getMinions().get(0);
			assertEquals(kingBangalash1.getSourceCard().getCardId(), "minion_king_bangalash");
			assertEquals(kingBangalash1.getAttack(), kingBangalash1.getBaseAttack() - 1);
			assertEquals(kingBangalash1.getHp(), kingBangalash1.getBaseHp() - 1);

			// Play a King Bangalash from the hand, observe it has the same buffs.
			Minion kingBangalash2 = playMinionCard(context, player, "minion_king_bangalash");
			assertEquals(kingBangalash2.getAttack(), kingBangalash2.getBaseAttack() - 1);
			assertEquals(kingBangalash2.getHp(), kingBangalash2.getBaseHp() - 1);
		});
	}

	@Test
	public void testTheEmeraldDream() {
		runGym((context, player, opponent) -> {
			Minion emeraldDream = playMinionCard(context, player, "permanent_the_emerald_dream");
			int count = 0;
			Minion snowflipper;
			for (int i = 0; i < 5; i++) {
				snowflipper = playMinionCard(context, player, "minion_neutral_test_1");
				count++;
				assertEquals(snowflipper.getAttack(), snowflipper.getBaseAttack() + count);
				assertEquals(snowflipper.getHp(), snowflipper.getBaseHp() + count);
			}
		});
	}

	@Test
	public void testThinkFast() {
		runGym((context, player, opponent) -> {
			// TODO: This should still work if it's a different class
			playCard(context, player, "spell_test_summon_tokens");
			int[] cost = new int[1];
			overrideDiscover(context, player, actions -> {
				cost[0] = actions.get(0).getCard().getBaseManaCost();
				return actions.get(0);
			});
			playCard(context, player, "spell_think_fast");
			assertEquals(costOf(context, player, player.getHand().get(0)), cost[0] - 1);
			context.endTurn();
			context.endTurn();
			assertEquals(costOf(context, player, player.getHand().get(0)), cost[0]);
		}, "BLACK", "BLACK");
	}

	@Test
	public void testThunderfury() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_thunderfury");
			player.getHero().getWeapon().setMaxHp(4);
			player.getHero().getWeapon().setHp(4);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 1);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 2);
			playCard(context, player, "spell_test_overload");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 2);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getAttributeValue(Attribute.OVERLOAD), 0);
			assertFalse(player.getHero().getWeapon().hasAttribute(Attribute.AURA_IMMUNE));
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getMaxDurability() - 3);
		});
	}

	@Test
	public void testVampiricTouch() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			context.endTurn();
			playCard(context, player, "spell_vampiric_touch", charger);
			assertTrue(charger.isDestroyed());
			assertEquals(player.getMinions().get(0).getAttack(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_charge_test");
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion mindControlTech = playMinionCard(context, opponent, "minion_mind_control_tech");
			context.endTurn();
			playCard(context, player, "spell_vampiric_touch", mindControlTech);
			assertFalse(mindControlTech.isDestroyed());
			assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test
	public void testYrel() {
		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(4);
			player.setMana(4);
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(player.getMana(), 0);
			player.setMana(4);
			playCard(context, player, "spell_test_deal_6", yrel);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			Minion yrel = playMinionCard(context, player, "minion_yrel");
			player.setMaxMana(5);
			player.setMana(5);
			playCard(context, player, "spell_power_word_tentacles", yrel);
			assertEquals(player.getMana(), 5);
		});
	}

	@Test
	public void testVindication() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_vindication", target);
			assertTrue(player.getHero().hasAttribute(Attribute.DIVINE_SHIELD));
			playCard(context, player, "spell_test_deal_1", player.getHero());
			assertFalse(player.getHero().hasAttribute(Attribute.DIVINE_SHIELD));
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
		});
	}

	@Test
	public void testVindicatorMaraad() {
		runGym((context, player, opponent) -> {
			Card cost1Card = putOnTopOfDeck(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "spell_test_summon_tokens");
			assertEquals(player.getHand().get(0), cost1Card);
		});

		runGym((context, player, opponent) -> {
			Card cost2Card = putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "spell_test_summon_tokens");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card cost1Card = putOnTopOfDeck(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "minion_test_3_2");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card cost2Card = putOnTopOfDeck(context, player, "minion_test_3_2");
			playCard(context, player, "minion_vindicator_maraad");
			playCard(context, player, "minion_test_3_2");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testWorgenAmbusher() {
		runGym((context, player, opponent) -> {
			Minion worgen1 = playMinionCard(context, player, "minion_worgen_ambusher");
			assertEquals(worgen1.getAttack(), worgen1.getBaseAttack());
			Minion worgen2 = playMinionCard(context, player, "minion_worgen_ambusher");
			assertEquals(worgen2.getAttack(), worgen2.getBaseAttack() + 1);
		});
	}


	@Test
	public void testYouFromTheFutureKargath() {
		// You from the Future on Kargath Baldefist causes doubly triggered end of turn effects
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_kargath_bladefist");
			playCard(context, player, "spell_you_from_the_future", target);
			Minion target2 = player.getMinions().get(1);
			assertTrue(target.isWounded());
			assertTrue(target2.isWounded());
			context.endTurn();
			assertEquals(target.getAttack(), target.getBaseAttack() + 4);
			assertEquals(target2.getAttack(), target2.getBaseAttack() + 4);
		});
	}

	@Test
	public void testCracklingArrows() {
		runGym((context, player, opponent) -> {
			List<Minion> minions = new ArrayList<>();
			context.endTurn();
			for (int i = 0; i < 6; i++) {
				minions.add(playMinionCard(context, opponent, "minion_neutral_test_1"));
			}
			context.endTurn();
			int opponentHealth = opponent.getHero().getHp();
			playCard(context, player, "secret_avenge");
			playCard(context, player, "spell_test_counter_secret");
			assertEquals(player.getSecrets().size(), 2);
			playCard(context, player, "spell_crackling_arrows");
			assertEquals(player.getSecrets().size(), 2);
			// 2 secrets + 1 minimum call = spell cast 3 times, all 6 minions should be dead
			int count = 0;
			for (Minion minion : minions) {
				if (minion.isDestroyed()) {
					count++;
				}
			}
			assertEquals(6, count + opponentHealth - opponent.getHero().getHp());
		});
	}

	@Test
	public void testDancemistress() {
		// When this minion is healed, check if Crazed Dancer is summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCard(context, player, "spell_ancestral_healing", dancemistress);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_crazed_dancer");
			// Check if the Crazed Dancer has attack and hp of 2
			assertEquals(player.getMinions().get(1).getBaseAttack(), 2);
			assertEquals(player.getMinions().get(1).getBaseHp(), 2);
		});

		// When a different minion is healed, Crazed Dancer is NOT summoned
		runGym((context, player, opponent) -> {
			Minion dancemistress = playMinionCard(context, player, "minion_dancemistress");
			Minion beasttest32 = playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			// Damages minions by 1
			playCard(context, opponent, "spell_arcane_explosion");
			context.endTurn();
			// Heals the dancemistress Minion
			playCard(context, player, "spell_ancestral_healing", beasttest32);
			assertFalse(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getCardId().equals("minion_crazed_dancer")));
		});
	}

	@Test
	public void testElorthaNoShadra() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_ice_rager");
			playCard(context, player, "minion_elortha_no_shadra");
			context.getLogic().drawCard(player.getId(), player);
			Minion iceRager = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(iceRager.getDeathrattles().size(), 1);
			destroy(context, iceRager);
			assertEquals(player.getMinions().size(), 2);
			Minion revived = player.getMinions().get(1);
			destroy(context, revived);
			assertTrue(revived.isDestroyed());
			assertEquals(player.getMinions().size(), 1);
		});
	}

	@Test
	public void testInstantEvolution() {
		runGym((context, player, opponent) -> {
			// Adds up to more than 12
			Minion target = playMinionCard(context, player, "minion_arcane_giant");
			playCard(context, player, "spell_instant_evolution", target);
			assertEquals(target.transformResolved(context).getSourceCard().getBaseManaCost(), 12);
		});
	}

	@Test
	public void testLordStormsong() {
		runGym((context, player, opponent) -> {
			Minion diedWhileNotAlive = playMinionCard(context, player, "minion_neutral_test_1");
			Minion diedWhileAlive1 = playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			Minion diedWhileAlive2 = playMinionCard(context, opponent, "token_treant");
			context.endTurn();
			destroy(context, diedWhileNotAlive);
			Minion stormsong = playMinionCard(context, player, "minion_lord_stormsong");
			destroy(context, diedWhileAlive1);
			destroy(context, diedWhileAlive2);
			destroy(context, stormsong);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), diedWhileAlive1.getSourceCard().getCardId());
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), diedWhileAlive2.getSourceCard().getCardId());
			assertEquals(player.getMinions().size(), 1, "Should contain resurrected beast");
			assertEquals(opponent.getMinions().size(), 1, "Should contain Treant");
		});

		// Test with transformation
		runGym((context, player, opponent) -> {
			Minion diedWhileNotAlive = playMinionCard(context, player, "minion_neutral_test_1");
			Minion transformedWhileAlive = playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			Minion diedWhileAlive2 = playMinionCard(context, opponent, "token_treant");
			context.endTurn();
			destroy(context, diedWhileNotAlive);
			Minion stormsong = playMinionCard(context, player, "minion_lord_stormsong");
			playCard(context, player, "spell_polymorph", transformedWhileAlive);
			transformedWhileAlive = (Minion) transformedWhileAlive.transformResolved(context);
			destroy(context, transformedWhileAlive);
			destroy(context, diedWhileAlive2);
			destroy(context, stormsong);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), transformedWhileAlive.getSourceCard().getCardId());
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), diedWhileAlive2.getSourceCard().getCardId());
			assertEquals(player.getMinions().size(), 1, "Should contain resurrected beast");
			assertEquals(opponent.getMinions().size(), 1, "Should contain Treant");
		});
	}

	@Test
	public void testPulseBomb() {
		// Test excess on adjacents
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion beast = playMinionCard(context, opponent, "minion_test_3_2");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			context.endTurn();
			playCard(context, player, "spell_pulse_bomb", beast);
			assertTrue(beast.isDestroyed());
			// Up to 18 damage rule
			assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + beast.getBaseHp());
			assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + beast.getBaseHp());
		});

		// Test excess in event of divine shield using Explosive Runes rules
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion boulderfist1 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			Minion beast = playMinionCard(context, opponent, "minion_test_3_2");
			Minion boulderfist2 = playMinionCard(context, opponent, "minion_boulderfist_ogre");
			beast.setAttribute(Attribute.DIVINE_SHIELD);
			context.endTurn();
			playCard(context, player, "spell_pulse_bomb", beast);
			assertFalse(beast.isDestroyed());
			assertEquals(beast.getHp(), beast.getBaseHp());
			// Up to 18 damage rule
			assertEquals(boulderfist1.getHp(), boulderfist1.getBaseHp() - 10 + beast.getBaseHp());
			assertEquals(boulderfist2.getHp(), boulderfist2.getBaseHp() - 10 + beast.getBaseHp());
		});
	}


	@Test
	public void testTestYourMight() {
		runGym((context, player, opponent) -> {
			Minion loser = playMinionCard(context, player, "token_treant");
			Minion notSelected1 = playMinionCard(context, player, "minion_neutral_test_1");
			context.endTurn();
			// It just has to be a 3/3
			Minion winner = playMinionCard(context, opponent, "minion_mind_control_tech");
			Minion notSelected2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			playCard(context, player, "spell_test_your_might");
			assertTrue(loser.isDestroyed());
			assertFalse(notSelected1.isDestroyed());
			assertFalse(notSelected2.isDestroyed());
			assertFalse(winner.isDestroyed());
			assertEquals(winner.getAttack(), winner.getBaseAttack() + 2);
			assertEquals(winner.getMaxHp(), winner.getBaseHp() + 2);
			assertEquals(winner.getHp(), winner.getBaseHp() + 2 - loser.getAttack());
		});

		// Flip player controlling winning minion
		runGym((context, player, opponent) -> {
			Minion winner = playMinionCard(context, player, "minion_mind_control_tech");
			Minion notSelected1 = playMinionCard(context, player, "minion_neutral_test_1");
			context.endTurn();
			// It just has to be a 3/3
			Minion loser = playMinionCard(context, opponent, "token_treant");
			Minion notSelected2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			playCard(context, player, "spell_test_your_might");
			assertTrue(loser.isDestroyed());
			assertFalse(notSelected1.isDestroyed());
			assertFalse(notSelected2.isDestroyed());
			assertFalse(winner.isDestroyed());
			assertEquals(winner.getAttack(), winner.getBaseAttack() + 2);
			assertEquals(winner.getMaxHp(), winner.getBaseHp() + 2);
			assertEquals(winner.getHp(), winner.getBaseHp() + 2 - loser.getAttack());
		});
	}


	@Test
	public void testEscapeFromDurnholde() {
		runGym((context, player, opponent) -> {
			Card shouldntDraw = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldDraw = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			assertEquals(shouldntDraw.getZone(), Zones.DECK);
			assertEquals(shouldDraw.getZone(), Zones.DECK);
			playCard(context, player, "permanent_escape_from_durnholde");
			context.endTurn();
			assertEquals(shouldntDraw.getZone(), Zones.DECK);
			assertEquals(shouldDraw.getZone(), Zones.DECK);
			context.endTurn();
			assertEquals(shouldDraw.getZone(), Zones.HAND);
			assertEquals(shouldntDraw.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			Card shouldDraw1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldDraw2 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			playCard(context, player, "permanent_escape_from_durnholde");
			playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			context.endTurn();
			assertEquals(shouldDraw1.getZone(), Zones.HAND);
			assertEquals(shouldDraw2.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testPermanentCallOfTheCrusade() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "permanent_call_of_the_crusade");
			Minion beast = playMinionCard(context, player, "minion_test_3_2");
			for (int i = 0; i < 3; i++) {
				assertEquals(beast.getAttack(), beast.getBaseAttack() + 1);
				context.endTurn();
				context.endTurn();
			}
			assertEquals(beast.getAttack(), beast.getBaseAttack());
			assertEquals(player.getMinions().size(), 1);
		});
	}
}
