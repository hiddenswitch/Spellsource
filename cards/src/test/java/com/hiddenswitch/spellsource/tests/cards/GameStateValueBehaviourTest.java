package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.PlayGameLogicRandomBehaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.Trace;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
public class GameStateValueBehaviourTest extends TestBase implements Serializable {

	@Test
	public void testRequestDiscovers() {
		var traceJson = "{\n" +
				"  \"seed\" : 1010101010101,\n" +
				"  \"catalogueVersion\" : 2,\n" +
				"  \"heroClasses\" : [ \"RUST\", \"RUST\" ],\n" +
				"  \"deckCardIds\" : [ {\n" +
				"    \"playerId\" : 0,\n" +
				"    \"cardIds\" : [ \"weapon_cleaver_of_glory\", \"minion_fireguard_bulwark\", \"minion_vohkrovanis\", \"minion_freyas_familliar\", \"minion_majestic_fennec\", \"minion_sinestra\", \"minion_sable_explorer\", \"minion_recurring_torrent\", \"minion_sable_explorer\", \"minion_dracomancer\", \"spell_rising_flame\", \"minion_treasure_hunter\", \"minion_wyrmrest_aspirant\", \"minion_beloved_benji\", \"spell_vehemence\", \"minion_bibliothecat\", \"minion_mindswapper\", \"minion_dreamway_whale\", \"minion_reckless_hero\", \"minion_amathyst_panther\", \"minion_dracomancer\", \"minion_treeleach\", \"minion_tick_and_tock\", \"minion_cursing_disciple\", \"minion_trailblazer\", \"minion_oni_entrapper\", \"spell_zagroz__inferno_bomb\", \"minion_shedding_chameleon\", \"minion_devouring_devilsaur\", \"weapon_lava_saber\" ]\n" +
				"  }, {\n" +
				"    \"playerId\" : 1,\n" +
				"    \"cardIds\" : [ \"weapon_cleaver_of_glory\", \"minion_fireguard_bulwark\", \"minion_vohkrovanis\", \"minion_freyas_familliar\", \"minion_majestic_fennec\", \"minion_sinestra\", \"minion_sable_explorer\", \"minion_recurring_torrent\", \"minion_sable_explorer\", \"minion_dracomancer\", \"spell_rising_flame\", \"minion_treasure_hunter\", \"minion_wyrmrest_aspirant\", \"minion_beloved_benji\", \"spell_vehemence\", \"minion_bibliothecat\", \"minion_mindswapper\", \"minion_dreamway_whale\", \"minion_reckless_hero\", \"minion_amathyst_panther\", \"minion_dracomancer\", \"minion_treeleach\", \"minion_tick_and_tock\", \"minion_cursing_disciple\", \"minion_trailblazer\", \"minion_oni_entrapper\", \"spell_zagroz__inferno_bomb\", \"minion_shedding_chameleon\", \"minion_devouring_devilsaur\", \"weapon_lava_saber\" ]\n" +
				"  } ],\n" +
				"  \"deckFormatName\" : \"Spellsource\",\n" +
				"  \"deckFormatSets\" : [ \"VERDANT_DREAMS\", \"SPELLSOURCE\", \"SANDS_OF_TIME\", \"WHAT_LIES_BENEATH\", \"BATTLE_FOR_ASHENVALE\", \"CUSTOM\", \"SPELLSOURCE_BASIC\", \"SOURCESTORM\" ],\n" +
				"  \"secondPlayerBonusCards\" : [ \"spell_lunstone\" ],\n" +
				"  \"mulligans\" : [ {\n" +
				"    \"playerId\" : 1,\n" +
				"    \"entityIds\" : [ 55, 65 ]\n" +
				"  }, {\n" +
				"    \"playerId\" : 0,\n" +
				"    \"entityIds\" : [ 10, 28, 27 ]\n" +
				"  } ],\n" +
				"  \"id\" : null,\n" +
				"  \"traceErrors\" : false,\n" +
				"  \"version\" : 4\n" +
				"}";

		var context = Trace.load(traceJson).replayContext();
		context.setBehaviour(0, new GameStateValueBehaviour()
				.setThrowsExceptions(false));
		context.setBehaviour(1, new GameStateValueBehaviour()
				.setThrowsExceptions(false));
		context.resume();
		assertTrue(context.updateAndGetGameOver(), "Gracefully handles issues with Celestial Conduit here");
	}

	@Test
	public void testBailsOutInfiniteDiscover() {
		runGym((context, player, opponent) -> {
			context.setBehaviour(player.getId(), new GameStateValueBehaviour().setThrowsExceptions(true));
			receiveCard(context, player, "spell_test_discover_loop");
			assertThrows(context::resume);
		});
	}

	@Test
	public void testShouldNotHealEnemyChampion() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, DeckFormat.spellsource().getSecondPlayerBonusCards()[0]);
			}
			opponent.getHero().setHp(29);
			GameStateValueBehaviour gsvb = new GameStateValueBehaviour()
					.setParallel(false)
					.setTimeout(150)
					.setLethalTimeout(1500);
			player.setMana(10);
			context.setBehaviour(player.getId(), gsvb);
			while (context.takeActionInTurn()) {
			}
			assertEquals(29, opponent.getHero().getHp());
		}, HeroClass.OLIVE, HeroClass.OLIVE);

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "spell_restorative_words");
			}
			opponent.getHero().setHp(29);
			GameStateValueBehaviour gsvb = new GameStateValueBehaviour()
					.setParallel(false)
					.setTimeout(150)
					.setLethalTimeout(1500);
			player.setMana(10);
			context.setBehaviour(player.getId(), gsvb);
			while (context.takeActionInTurn()) {
			}
			assertEquals(29, opponent.getHero().getHp());
		}, HeroClass.OLIVE, HeroClass.OLIVE);

		runGym((context, player, opponent) -> {
			player.getHero().setHp(14);
			GameStateValueBehaviour gsvb = new GameStateValueBehaviour()
					.setParallel(false)
					.setTimeout(150)
					.setLethalTimeout(1500);
			player.setMana(10);
			context.setBehaviour(player.getId(), gsvb);
			while (context.takeActionInTurn()) {
			}
			assertEquals(17, player.getHero().getHp());
		}, HeroClass.OLIVE, HeroClass.OLIVE);
	}

	@Test
	public void testDesertMaiden() {
		runGym((context, player, opponent) -> {
			// Temporarily disable logging
			shuffleToDeck(context, player, "spell_test_gain_mana");
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_black_test");
			playMinionCard(context, player, "minion_desert_maiden");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			// Return a valid action
			behaviour.setThrowsExceptions(false);
			context.setBehaviour(player.getId(), behaviour);
			context.endTurn();
			while (context.takeActionInTurn()) {
			}
			context.endTurn();
			while (context.takeActionInTurn()) {
			}
		});
	}

	@Test
	public void testAIMakesOpponentMillCards() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion felReaver = playMinionCard(context, opponent, "minion_test_discarder");
			context.endTurn();
			for (int i = 0; i < 60; i++) {
				shuffleToDeck(context, opponent, "spell_test_gain_mana");
			}
			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "spell_a_f_kupcake");
			}
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			context.setBehaviour(player.getId(), behaviour);

			while (context.takeActionInTurn()) {
			}

			assertEquals(player.getHand().size(), 0);
		});
	}

	@RepeatedTest(20)
	public void testAIFindsPhysicalAttackLethal() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 7; i++) {
				playMinionCard(context, player, "minion_charge_test_1");
			}
			opponent.getHero().setHp(7);
			Minion highValueTarget = playMinionCard(context, opponent, "minion_neutral_test");
			highValueTarget.setAttack(1000);
			highValueTarget.setAttribute(Attribute.SPELL_DAMAGE, 1000);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTimeout(behaviour.getTimeout() * 3)
					.setLethalTimeout(behaviour.getLethalTimeout() * 3)
					.setExpandDepthForLethal(true)
					.setParallel(false)
					.setPruneContextStack(true);
			context.setBehaviour(player.getId(), behaviour);

			while (context.takeActionInTurn()) {
			}

			assertTrue(context.updateAndGetGameOver());
		});
	}

	@Test
	public void testDiscoverActionsWithLowDepth() {
		runGym((context, player, opponent) -> {
			GameStateValueBehaviour checkDepth = new GameStateValueBehaviour();
			checkDepth.setMaxDepth(2);
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "spell_test_discover3");
			}
			shuffleToDeck(context, player, "passive_zero_cost");
			context.getLogic().fireGameEvent(new GameStartEvent(context, player));
			assertEquals(costOf(context, player, player.getHand().get(0)), 0);
			context.setBehaviour(player.getId(), checkDepth);

			assertTrue(context.takeActionInTurn());
		});
	}

	@Test
	public void testPlaysWildGrowth() {
		runGym((context, player, opponent) -> {
			// Should play wild growth
			receiveCard(context, player, "spell_test_ramp_mana");
			receiveCard(context, player, "minion_neutral_test");
			player.setMana(3);
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getMaxMana(), 2);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			playMinionCard(context, opponent, "minion_test_3_2");
			// should not play wild growth
			context.endTurn();
			// Set hp to yellow threat
			player.getHero().setHp(3);
			receiveCard(context, player, "spell_test_ramp_mana");
			receiveCard(context, player, "minion_test_taunts");
			receiveCard(context, player, "minion_test_taunts");
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());

			while (context.takeActionInTurn()) {
			}

			// Note: this is turn 2 for the player
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_test_taunts");
			assertEquals(player.getMaxMana(), 2);
		});
	}

	@Test
	public void testBrannSpiteful() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_neutral_test_big"));
			Card brann = receiveCard(context, player, "minion_test_double_openers");
			Card spiteful = receiveCard(context, player, "minion_test_spell_summoner");
			putOnTopOfDeck(context, player, "spell_test_deal_10");
			player.setMana(10);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			List<GameAction> actions = new ArrayList<>();
			for (int i = 0; i < 2; i++) {
				GameAction chosen = behaviour.requestAction(context, player, context.getValidActions());
				actions.add(chosen);
				context.performAction(player.getId(), chosen);
			}
			assertEquals(actions.get(0).getSourceReference(), brann.getReference());
			assertEquals(actions.get(1).getSourceReference(), spiteful.getReference());
		});
	}

	@Test
	public void testAIExploresDiscoversForWinning() {
		runGym((context, player, opponent) -> {

			// There is a branch of discover actions that wins the bot the game.
			context.setBehaviour(player.getId(), new GameStateValueBehaviour());
			receiveCard(context, player, "spell_cascading_discovers");

			while (context.takeActionInTurn()) {
			}

			assertTrue(context.updateAndGetGameOver());
		});
	}

	@Test
	public void testCorrectOrder() {
		runGym((context, player, opponent) -> {
			GameStateValueBehaviour checkDepth = new GameStateValueBehaviour();
			checkDepth.setMaxDepth(6);
			checkDepth.setTimeout(24000L);
			checkDepth.setParallel(false);
			putOnTopOfDeck(context, opponent, "minion_test_3_2_beast");
			opponent.getHero().setHp(4);
			// Your hero power is, Equip a 1/1 Weapon (costs 2)
			Minion cannotAttack = playMinionCard(context, player, "minion_1_1_hero_power");
			// Whenever this minion is targeted by a battlecry, gain 10,000 HP
			Minion gains = playMinionCard(context, player, "minion_gains_huge_hp");
			player.setMana(6);
			// Cost 3, Give a minion +1/+1
			receiveCard(context, player, "minion_test_buff");
			// Cost 1, Has Charge while you have a weapon equipped
			receiveCard(context, player, "minion_test_charge_weapon");
			context.setBehaviour(player.getId(), checkDepth);

			while (context.takeActionInTurn()) {
			}

			// Should equip weapon, play Southsea, then play Shattered Sun Cleric, battlecry target Southsea, then attack with
			// southsea, then attack with hero
			// Southsea MUST attack before weapon attacks
			// Sun cleric MUST be played after southsea and should NOT buff a minion that gains a huge amount of hp
			// This is a depth 6 puzzle.
			assertTrue(player.getWeaponZone().isEmpty());
			assertTrue(context.updateAndGetGameOver());
		});
	}

	@Test
	public void testSingleMatchBenchmark() {
		for (var i = 0; i < 10; i++) {
			GameContext gameContext = new GameContext();
			GameDeck deck1 = DeckCreateRequest.fromCardIds(HeroClass.CAMO,
					"minion_shadowveiler",
					"minion_swift_stinger",
					"spell_smokescreen",
					"minion_natural_convert",
					"minion_towering_treant",
					"minion_mother_matron",
					"minion_lightning_elemental",
					"minion_blackboar",
					"minion_sly_conquistador",
					"minion_oni_entrapper",
					"minion_peacock_mystic",
					"minion_smug_theorist",
					"minion_cinderslinger",
					"minion_holdout_soldier",
					"spell_espionage",
					"minion_bighand_brute",
					"minion_roadblock_pufferfish",
					"minion_novice_enchantress",
					"minion_lounge_brownie",
					"minion_mutated_brute",
					"minion_anarking",
					"minion_captain_stashin",
					"minion_uccian_hydra",
					"minion_divine_cleric",
					"minion_draining_ooze",
					"minion_holdover_lich",
					"minion_haunted_armor",
					"minion_lava_manticore",
					"minion_twilight_mercenary",
					"spell_last_man_standing").toGameDeck();
			GameDeck deck2 = DeckCreateRequest.fromCardIds(HeroClass.NAVY,
					"minion_little_helper",
					"spell_gather_strength",
					"minion_sleepy_sprite",
					"minion_refracting_golem",
					"spell_scurvy_sights",
					"minion_fairytale_tracker",
					"minion_sea_stowaway",
					"spell_ensnare",
					"minion_sly_conquistador",
					"minion_fae_trickster",
					"minion_sky_high_surveyor",
					"minion_gustbreaker",
					"spell_reinforcements",
					"minion_wolfcrier",
					"minion_seven_shot_gunner",
					"minion_reckless_hero",
					"minion_jungle_operative",
					"minion_primal_mother",
					"spell_alternate_timeline",
					"minion_holdout_soldier",
					"minion_nightmare_dragonflight",
					"minion_yokai_shapeshifter",
					"minion_polychrome_hydra",
					"minion_emerald_cleanser",
					"minion_doctor_hatchett",
					"minion_crazed_explorer",
					"minion_magma_hound",
					"minion_oppressor_defender",
					"minion_sourceborn_aelin",
					"spell_clash").toGameDeck();
			gameContext.setDeck(0, deck1);
			gameContext.setDeck(1, deck2);
			gameContext.setDeckFormat(DeckFormat.spellsource());
			// set a seed
			gameContext.setLogic(new GameLogic(10101L));
			gameContext.setBehaviour(1, new GameStateValueBehaviour().setParallel(false));
			gameContext.setBehaviour(0, new PlayGameLogicRandomBehaviour());
			gameContext.play();
		}
	}

	@Test
	public void testDestroyAt2HP() {
		runGym((context, player, opponent) -> {
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			player.setMana(4);
			player.setMaxMana(4);
			playCard(context, player, "spell_set_deal_1_hero_power");
			opponent.getHero().setHp(2);
			List<GameAction> actions = context.getValidActions();
			assertTrue(actions.stream().anyMatch(ga -> fireball.getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to cast the Fireball on the opponent's hero");
			assertTrue(actions.stream().anyMatch(ga -> player.getHeroPowerZone().get(0).getReference().equals(ga.getSourceReference())
					&& opponent.getHero().getReference().equals(ga.getTargetReference())), "The player should be able to Fireblast on the opponent's hero");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction chosen = behaviour.requestAction(context, player, actions);
			assertEquals(chosen.getSourceReference(), fireball.getReference(), "The AI should have chosen to play the Fireball");
			assertEquals(chosen.getTargetReference(), opponent.getHero().getReference(), "The AI should have chosen to cast it against the enemy hero.");
		});
	}


	@Test
	public void testAIWillPlayQuests() {
		runGym((context, player, opponent) -> {
			Card powerTrip = receiveCard(context, player, "quest_into_the_mines");
			receiveCard(context, player, "minion_neutral_test_1");
			// The AI loves Taunts
			receiveCard(context, player, "spell_test_summon_tokens");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, player, context.getValidActions());
			assertEquals(action.getActionType(), ActionType.SPELL);
			assertEquals(action.getSourceReference(), powerTrip.getReference());
		});
	}

	@Test
	public void testAIWillPlayCursed() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			opponent.setMana(2);
			opponent.setMaxMana(2);
			Card curse = receiveCard(context, opponent, "spell_test_curse");
			assertTrue(curse.hasAttribute(Attribute.CURSE));
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			assertEquals(action.getActionType(), ActionType.SPELL);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			opponent.setMana(4);
			opponent.setMaxMana(4);
			Card cursed = receiveCard(context, opponent, "spell_test_curse");
			receiveCard(context, opponent, "spell_test_deal_6");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			context.performAction(opponent.getId(), action);
			action = behaviour.requestAction(context, opponent, context.getValidActions());
			context.performAction(opponent.getId(), action);
			assertFalse(opponent.getHand().contains(cursed));
		});
	}

	@Test
	public void testAIWillNotPlayIntoDoomsayer() {
		runGym((context, player, opponent) -> {
			var boardClear = playMinionCard(context, player, "minion_test_start_turns");
			var minion = playMinionCard(context, player, 1, 1);
			context.endTurn();
			destroy(context, boardClear);
			context.endTurn();
			assertFalse(minion.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			var boardClear = playMinionCard(context, player, "minion_test_start_turns");
			context.endTurn();
			var minion = playMinionCard(context, opponent, 1, 1);
			assertFalse(minion.isDestroyed());
			assertFalse(boardClear.isDestroyed());
			context.endTurn();
			assertTrue(minion.isDestroyed());
			assertTrue(boardClear.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			var boardClear = playMinionCard(context, player, "minion_test_start_turns");
			context.endTurn();
			var card = receiveCard(context, opponent, "minion_rapier_rodent");
			destroy(context, boardClear);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTriggerStartTurns(true);
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			assertEquals(ActionType.SUMMON, action.getActionType(), "should play rapier rodent");
			assertEquals(card.getReference(), action.getSourceReference(), "should play rapier rodent");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_test_start_turns");
			context.endTurn();
			receiveCard(context, opponent, "minion_rapier_rodent");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTriggerStartTurns(true);
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			assertEquals(ActionType.END_TURN, action.getActionType(), "should not play rapier rodent");
		});

		runGym((context, player, opponent) -> {
			Minion clearsStartTurns = playMinionCard(context, player, "minion_test_start_turns");
			context.endTurn();
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, opponent, "minion_charge_test");
			}
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour.setTriggerStartTurns(true);
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			assertEquals(ActionType.PHYSICAL_ATTACK, action.getActionType(), "should attack");
			assertEquals(clearsStartTurns.getReference(), action.getTargetReference(), "should attack board clear");
		});

		runGym((context, player, opponent) -> {
			Minion clearsStartTurns = playMinionCard(context, player, "minion_test_start_turns");
			context.endTurn();
			playMinionCard(context, opponent, "minion_floating_crystal");
			Card card = receiveCard(context, opponent, "spell_test_deal_6");
			opponent.setMaxMana(4);
			opponent.setMana(4);
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			behaviour
					.setDisposeNodes(false)
					.setTriggerStartTurns(true)
					.setParallel(false);
			var validActions = context.getValidActions();
			GameAction action = behaviour.requestAction(context, opponent, validActions);
			assertEquals(action.getSourceReference(), card.getReference(), "should deal 7 damage");
			assertEquals(context.resolveSingleTarget(clearsStartTurns.getReference()), context.resolveSingleTarget(action.getTargetReference()), "should target board clear");
		});
	}

	@Test
	public void testAIPlaysIntoSecrets() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_trap");
			Minion targetDummy = playMinionCard(context, player, "minion_test_taunts");
			targetDummy.setAttack(0);
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			GameStateValueBehaviour behaviour = new GameStateValueBehaviour();
			GameAction action = behaviour.requestAction(context, opponent, context.getValidActions());
			assertEquals(action.getActionType(), ActionType.PHYSICAL_ATTACK);
			assertEquals(action.getTargetReference(), targetDummy.getReference());
			assertEquals(action.getSourceReference(), charger.getReference());
		});
	}
}
