package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.GameStartEvent;
import net.demilich.metastone.game.events.WillEndSequenceEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSourceArg;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;
import net.demilich.metastone.game.spells.desc.source.TopCardsOfDeckSource;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.GymFactory;
import net.demilich.metastone.tests.util.OverrideHandle;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CustomCardsTests extends TestBase {

	@Test
	public void testVohkrovanis() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("spell_test_spellpower"));
			Card shouldBeRemoved1 = shuffleToDeck(context, player, "spell_lunstone");
			Card shouldBeRemoved2 = receiveCard(context, player, "spell_lunstone");
			SpellUtils.castChildSpell(context, player, context.getCardById("minion_vohkrovanis").getDesc().getGameTriggers()[0].spell, player, null);
			context.getLogic().endOfSequence();
			assertEquals(shouldBeRemoved1.getZone(), Zones.GRAVEYARD);
			assertEquals(shouldBeRemoved2.getZone(), Zones.GRAVEYARD);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_test_spellpower", "should have drawn 1 card");
			assertEquals(player.getDeck().size(), 29, "should have drawn 1 card");
		});
	}

	@Test
	public void testVohkrovanisStartOfGame() {
		DebugContext context = createContext(HeroClass.TEST, HeroClass.TEST, false, new DeckFormat().withCardSets(CardSet.SPELLSOURCE_BASIC));
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(CardCatalogue::getOneOneNeutralMinionCardId)
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_vohkrovanis"));
		});

		context.init();
		for (Player player : context.getPlayers()) {
			assertTrue(player.getDeck().stream().noneMatch(c -> c.getCardId().equals(CardCatalogue.getOneOneNeutralMinionCardId())));
			assertTrue(player.getHand().stream().noneMatch(c -> c.getCardId().equals(CardCatalogue.getOneOneNeutralMinionCardId())));
			assertTrue(player.getHand().size() >= GameLogic.STARTER_CARDS);
		}
	}

	@Test
	public void testSneakyKaeru() {
		runGym((context, player, opponent) -> {
			Minion sneakyKaeru = playMinionCard(context, player, "minion_sneaky_kaeru");
			castDamageSpell(context, player, sneakyKaeru.getMaxHp() - 1, sneakyKaeru);
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_sneaky_kaeru");
		});
	}

	@Test
	public void testSecretOfTwilightAffectedBySpellDamage() {
		runGym((context, player, opponent) -> {
			Minion spellDamage = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			spellDamage.modifyAttribute(Attribute.SPELL_DAMAGE, 1);
			playCard(context, player, "secret_secret_of_twilight");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp() + 1;
			playCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			int targetHp = 1;
			if (opponent.getMinions().size() == 0) {
				targetHp = 0;
			}
			assertEquals(opponent.getHero().getHp() + targetHp, opponentHp - 4, "Spell damage should have been applied");
		});
	}

	@Test
	public void testKorvasBloodthorn() {
		// Test casting spell on my own minion, opponent can't target it during their turn
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_korvas_bloodthorn");
			Minion test = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Card minionTargetingSpellInOpponentsHand = receiveCard(context, opponent, "spell_test_cost_3_buff");
			opponent.setMana(minionTargetingSpellInOpponentsHand.getBaseManaCost());
			GameAction action = context.getValidActions()
					.stream()
					.filter(ga -> ga.getTargetReference() != null && ga.getTargetReference().equals(test.getReference()))
					.findFirst().orElseThrow(AssertionError::new);
			assertEquals(action.getSource(context), minionTargetingSpellInOpponentsHand, "Before Korvas's effect comes into play, the opponent can currently target my minion with a spell.");
			context.endTurn();
			playCard(context, player, "spell_test_cost_3_buff", test);
			Card minionTargetingSpellInPlayerHand = receiveCard(context, player, "spell_test_cost_3_buff");
			// Make sure we have enough mana
			player.setMana(minionTargetingSpellInPlayerHand.getBaseManaCost());
			// Check that we can target it
			action = context.getValidActions()
					.stream()
					.filter(ga -> ga.getTargetReference() != null && ga.getTargetReference().equals(test.getReference()))
					.findFirst().orElseThrow(AssertionError::new);
			assertEquals(action.getSource(context), minionTargetingSpellInPlayerHand, "I should be able to cast a spell still on my own minion.");
			context.endTurn();
			// Now my opponent's turn
			opponent.setMana(minionTargetingSpellInOpponentsHand.getBaseManaCost());
			assertEquals(context.getValidActions()
					.stream()
					.filter(ga -> ga.getTargetReference() != null && ga.getTargetReference().equals(test.getReference()))
					.count(), 0L, "There should be no actions that target the minion currently under the influence of Korvas's effect from my opponent's point of view.");
		}, HeroClass.ANY, HeroClass.ANY);
		// Test casting spell on opponent's minion, opponent can't target it during their turn
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_korvas_bloodthorn");
			context.endTurn();
			Minion test = playMinionCard(context, opponent, "minion_neutral_test");
			Card minionTargetingSpellInOpponentsHand = receiveCard(context, opponent, "spell_test_cost_3_buff");
			opponent.setMana(minionTargetingSpellInOpponentsHand.getBaseManaCost());
			GameAction action = context.getValidActions()
					.stream()
					.filter(ga -> ga.getTargetReference() != null && ga.getTargetReference().equals(test.getReference()))
					.findFirst().orElseThrow(AssertionError::new);
			assertEquals(action.getSource(context), minionTargetingSpellInOpponentsHand,
					"Before Korvas's effect comes into play, the opponent can currently target the opponent's minion with a spell.");
			context.endTurn();
			playCard(context, player, "spell_test_cost_3_buff", test);
			Card minionTargetingSpellInPlayerHand = receiveCard(context, player, "spell_test_cost_3_buff");
			// Make sure we have enough mana
			player.setMana(minionTargetingSpellInPlayerHand.getBaseManaCost());
			// Check that we can target it
			action = context.getValidActions()
					.stream()
					.filter(ga -> ga.getTargetReference() != null
							&& ga.getSource(context).equals(minionTargetingSpellInPlayerHand)
							&& ga.getTargetReference().equals(test.getReference()))
					.findFirst().orElseThrow(AssertionError::new);
			context.endTurn();
			// Now my opponent's turn
			opponent.setMana(minionTargetingSpellInOpponentsHand.getBaseManaCost());
			assertEquals(context.getValidActions()
					.stream()
					.filter(ga -> ga.getTargetReference() != null && ga.getTargetReference().equals(test.getReference()))
					.count(), 0L, "There should be no actions that target the minion currently under the influence of Korvas's effect from my opponent's point of view.");
		}, HeroClass.ANY, HeroClass.ANY);
	}

	@Test
	public void testAbholos() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion shouldNotDie1 = playMinionCard(context, opponent, "minion_neutral_test");
			shouldNotDie1.setAttack(99);
			context.endTurn();
			Minion shouldDie1 = playMinionCard(context, player, "minion_neutral_test");
			Minion shouldDie2 = playMinionCard(context, player, "minion_neutral_test");
			shouldDie2.setAttack(10);
			Minion abholos = playMinionCard(context, player, "minion_abholos");
			destroy(context, abholos);
			assertFalse(shouldNotDie1.isDestroyed(), "Opposing minions should not have been destroyed");
			assertTrue(shouldDie1.isDestroyed(), "Friendly minions should be destroyed");
			assertTrue(shouldDie2.isDestroyed(), "Friendly minions should be destroyed");
			assertTrue(abholos.isDestroyed(), "Original Abholos should be destroyed");
			Minion newAbholos = player.getMinions().get(0);
			assertEquals(newAbholos.getSourceCard().getCardId(), "minion_abholos", "New minion should be an Abholos");
			assertEquals(player.getMinions().size(), 1, "Abholos should be the only minion");
			assertEquals(newAbholos.getAttack(), shouldDie1.getAttack() + shouldDie2.getAttack(), "Combined attack should be dead minions' attack summed");
			assertEquals(newAbholos.getHp(), shouldDie1.getHp() + shouldDie2.getHp(), "Combine HP should be dead minions' hp summed");
		});
	}

	@Test
	public void testUccianHydra() {
		runGym((context, player, opponent) -> {
			Minion hydra = playMinionCard(context, player, "minion_uccian_hydra");
			Minion dead = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, dead);
			assertEquals(hydra.getAttack(), hydra.getBaseAttack());
		});

		runGym((context, player, opponent) -> {
			Minion hydra = playMinionCard(context, player, "minion_uccian_hydra");
			Minion dead = playMinionCard(context, player, "minion_neutral_test");
			dead.setAttack(hydra.getBaseAttack() + 1);
			destroy(context, dead);
			assertEquals(hydra.getAttack(), hydra.getBaseAttack() * 2);
		});
	}

	@Test
	public void testShufflingHorror() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_shuffling_horror");
			playCard(context, player, "spell_shuffle_minion_to_deck", target);
			assertEquals(player.getHand().size(), 2);
			assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_shuffling_horror")));
		});
	}

	@Test
	public void testMariAnette() {
		runGym((context, player, opponent) -> {
			Minion friendly = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "minion_mari_anette");
			friendly = (Minion) friendly.transformResolved(context);
			enemy = (Minion) enemy.transformResolved(context);
			for (Minion minion : new Minion[]{friendly, enemy}) {
				assertEquals(minion.getDescription(), "At the end of your next turn, transforms back into Neutral Test");
				assertEquals(minion.getSourceCard().getCardId(), "token_mari_puppet");
			}
			context.endTurn();
			for (Minion minion : new Minion[]{friendly, enemy}) {
				assertEquals(minion.getDescription(), "At the end of your next turn, transforms back into Neutral Test");
				assertEquals(minion.getSourceCard().getCardId(), "token_mari_puppet");
			}
			context.endTurn();
			for (Minion minion : new Minion[]{friendly, enemy}) {
				assertEquals(minion.getDescription(), "At the end of your next turn, transforms back into Neutral Test");
				assertEquals(minion.getSourceCard().getCardId(), "token_mari_puppet");
			}
			context.endTurn();
			friendly = (Minion) friendly.transformResolved(context);
			enemy = (Minion) enemy.transformResolved(context);
			for (Minion minion : new Minion[]{friendly, enemy}) {
				assertEquals(minion.getSourceCard().getCardId(), "minion_neutral_test");
				assertEquals(minion.getDescription(), "");
			}
		});
	}

	@Test
	public void testDoodles() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doodles");
			String originalHeroPower = player.getHeroPowerZone().get(0).getCardId();
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_draw_a_card");
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_draw_a_card");
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), originalHeroPower);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doodles");
			String originalHeroPower = player.getHeroPowerZone().get(0).getCardId();
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_draw_a_card");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_draw_a_card");
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), originalHeroPower);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_doodles");
			String originalHeroPower = player.getHeroPowerZone().get(0).getCardId();
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_draw_a_card");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_11", player.getHero());
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), originalHeroPower);
		});
	}

	@Test
	public void testHoffisTheDunewalker() {
		// This has to test two effects:
		//   1. The QueryTargetSpell effect
		//   2. The AddBattlecrySpell effect
		runGym((context, player, opponent) -> {
			Card shouldNotSpawnSandpile2 = putOnTopOfDeck(context, player, "minion_neutral_test");
			Card shouldSpawnSandpileDespiteSixth = putOnTopOfDeck(context, player, "minion_neutral_test");
			Card shouldSpawnSandpile1 = putOnTopOfDeck(context, player, "minion_neutral_test");
			Card shouldNotSpawnSandpile1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldSpawnSandpile2 = putOnTopOfDeck(context, player, "minion_test_untargeted_battlecry");
			Card shouldSpawnSandpile3 = putOnTopOfDeck(context, player, "minion_neutral_test");
			Card shouldSpawnSandpile4 = putOnTopOfDeck(context, player, "minion_neutral_test");
			Minion hoffis = playMinionCard(context, player, "minion_hoffis_the_dunewalker");
			for (int i = 0; i < 6; i++) {
				context.getLogic().drawCard(player.getId(), player.getHero());
			}
			assertEquals(player.getHand().size(), 6);
			destroy(context, hoffis);
			int hp = player.getHero().getHp();
			for (Card card : new Card[]{shouldSpawnSandpile1, shouldSpawnSandpile2, shouldSpawnSandpile3, shouldSpawnSandpile4, shouldSpawnSandpileDespiteSixth}) {
				assertEquals(player.getMinions().size(), 0);
				assertEquals(card.getZone(), Zones.HAND);
				playCard(context, player, card);
				assertEquals(player.getMinions().size(), 2);
				assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_sandpile");
				destroy(context, player.getMinions().get(1));
				destroy(context, player.getMinions().get(0));
			}
			// Test that both battlecries were evaluated
			assertEquals(player.getHero().getHp(), hp - 1);
			for (Card card : new Card[]{shouldNotSpawnSandpile1, shouldNotSpawnSandpile2}) {
				playCard(context, player, card);
				assertTrue(player.getMinions().stream().noneMatch(c -> c.getSourceCard().getCardId().equals("token_sandpile")));
			}
		});
	}

	@Test
	public void testDaringDuelist() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion source = playMinionCard(context, player, "minion_daring_duelist");
			// Ensure Daring Duelist dies
			source.setHp(2);
			context.endTurn();
			context.endTurn();
			Card shouldBeInDeck = shuffleToDeck(context, player, "minion_neutral_test");
			attack(context, player, source, target);
			assertTrue(source.isDestroyed());
			assertEquals(shouldBeInDeck.getZone(), Zones.DECK);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			target.setAttack(0);
			context.endTurn();
			Minion source = playMinionCard(context, player, "minion_daring_duelist");
			context.endTurn();
			context.endTurn();
			Card shouldBeInHand = shuffleToDeck(context, player, "minion_neutral_test");
			attack(context, player, source, target);
			assertFalse(source.isDestroyed());
			assertEquals(shouldBeInHand.getZone(), Zones.HAND);
		});
	}

	@Test
	public void testDoomerDiver() {
		runGym((context, player, opponent) -> {
			Card shouldDraw = shuffleToDeck(context, player, "minion_neutral_test");
			Minion pirate = playMinionCard(context, player, "minion_charge_pirate");
			attack(context, player, pirate, opponent.getHero());
			playMinionCard(context, player, "minion_doomed_diver");
			assertEquals(shouldDraw.getZone(), Zones.HAND);
		});

		runGym((context, player, opponent) -> {
			Card shouldDraw = shuffleToDeck(context, player, "minion_neutral_test");
			Minion pirate = playMinionCard(context, player, "minion_charge_pirate");
			playMinionCard(context, player, "minion_doomed_diver");
			assertEquals(shouldDraw.getZone(), Zones.DECK);
		});
	}

	@Test
	public void testShapeseeper() {
		runGym((context, player, opponent) -> {
			Card shapeseeperCard = receiveCard(context, player, "minion_shapeseeper");
			playCard(context, player, "minion_neutral_test");
			assertEquals(costOf(context, player, shapeseeperCard), shapeseeperCard.getBaseManaCost() + 1);
			Minion shapeseeper = playMinionCard(context, player, shapeseeperCard);
			assertEquals(shapeseeper.getAttack(), shapeseeper.getBaseAttack() + 1);
			assertEquals(shapeseeper.getHp(), shapeseeper.getBaseHp() + 1);
		});
	}

	@Test
	public void testDiscoInfero() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			Minion disco = playMinionCard(context, player, "minion_disco_inferno", target);
			assertEquals(disco.getAttack(), target.getBaseAttack());
			assertEquals(disco.getHp(), target.getBaseHp());
			assertEquals(target.getAttack(), disco.getBaseAttack());
			assertEquals(target.getHp(), disco.getBaseHp());
		});
	}

	@Test
	public void testRecurringTorrent() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_cost_2_card");
			playCard(context, player, "minion_recurring_torrent");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_cost_2_card");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_cost_2_card");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "minion_recurring_torrent");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			playCard(context, opponent, "spell_cost_2_card");
			context.endTurn();
			playCard(context, player, "minion_recurring_torrent");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testMindswapper() {
		runGym((context, player, opponent) -> {
			Card formerlyOpponentCard = receiveCard(context, opponent, "spell_test_overload");
			Card formerlyPlayerCard = receiveCard(context, player, "spell_test_spellpower");
			Card shouldNotSwapOpponent = receiveCard(context, opponent, "spell_cost_2_card");
			Card shouldNotSwapPlayer = receiveCard(context, player, "spell_cost_2_card");
			playCard(context, player, "minion_mindswapper");
			assertEquals(formerlyOpponentCard.getOwner(), player.getId());
			assertEquals(formerlyPlayerCard.getOwner(), opponent.getId());
			assertEquals(shouldNotSwapOpponent.getOwner(), opponent.getId());
			assertEquals(shouldNotSwapPlayer.getOwner(), player.getId());
		});
	}

	@Test
	public void testFassnuAvenger() {
		runGym((context, player, opponent) -> {
			Minion toDestroy = playMinionCard(context, player, "minion_test_deathrattle");
			Minion fassnu = playMinionCard(context, player, "minion_fassnu_avenger");
			destroy(context, toDestroy);
			Card shouldBeDrawn = shuffleToDeck(context, player, "spell_cost_2_card");
			Card shouldNotBeDrawn = shuffleToDeck(context, player, "spell_lunstone");
			destroy(context, fassnu);
			assertEquals(shouldBeDrawn.getZone(), Zones.HAND);
			assertEquals(shouldNotBeDrawn.getZone(), Zones.DECK);
		});
	}

	@Test
	public void testCursingChimp() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_cursing_chimp", target);
			assertEquals(target.transformResolved(context).getSourceCard().getCardId(), "minion_cursing_chimp");
		});
	}

	@Test
	public void testChromaticVohlok() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_chromatic_vohlok");
			assertEquals(target.transformResolved(context).getSourceCard().getCardId(), "minion_chromatic_vohlok");
		});
	}

	@Test
	public void testBulletBull() {
		runGym((context, player, opponent) -> {
			Minion bullet = playMinionCard(context, player, "minion_bullet_bull");
			assertEquals(bullet.getAttack(), bullet.getBaseAttack() * 2);
			context.endTurn();
			assertEquals(bullet.getAttack(), bullet.getBaseAttack());
			context.endTurn();
			assertEquals(bullet.getAttack(), bullet.getBaseAttack() * 2);
		});
	}

	@Test
	public void testHoldoverLich() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_holdover_lich");
			Minion target1 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_deal_6", target1);
			assertEquals(target1.getHp(), 1);
			assertEquals(target1.getZone(), Zones.BATTLEFIELD);
			assertTrue(target1.hasAttribute(Attribute.CANNOT_REDUCE_HP_BELOW_1));
			context.endTurn();
			assertTrue(target1.isDestroyed());
			assertEquals(target1.getZone(), Zones.GRAVEYARD);
		});
	}

	@Test
	public void testMutamiteTerror() {
		runGym((context, player, opponent) -> {
			Card leftCard = receiveCard(context, player, "minion_neutral_test");
			Card rightCard = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_mutamite_terror");
			assertFalse(leftCard.hasAttribute(Attribute.DISCARDED));
			assertTrue(rightCard.hasAttribute(Attribute.DISCARDED));
		});
	}

	@Test
	@Disabled("What to do do about adapt?")
	public void testAberaSwarmEvolver() {
		runGym((context, player, opponent) -> {
			AtomicReference<String> name = new AtomicReference<>();
			AtomicInteger counter = new AtomicInteger(0);
			overrideDiscover(context, player, discoverActions -> {
				name.set(discoverActions.get(0).getCard().getName());
				counter.incrementAndGet();
				return discoverActions.get(0);
			});
			Minion larva = playMinionCard(context, player, "token_spiderling");
			playMinionCard(context, player, "token_abera_swarm_evolver");

			assertAdapted(name.get(), larva);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(counter.get(), 1);
			playCard(context, player, "spell_spider_swarm");
			assertEquals(counter.get(), 1);
			assertEquals(player.getMinions().size(), 4);
			assertAdapted(name.get(), player.getMinions().get(2));
			assertAdapted(name.get(), player.getMinions().get(3));
		});
	}

	@Test
	public void testPastryCook() {
		// Check condition isn't met when nothing is roasted
		runGym((context, player, opponent) -> {
			Card pastryCook = receiveCard(context, player, "minion_pastry_cook");
			Card shouldNotBeRoasted = shuffleToDeck(context, player, "minion_neutral_test");
			player.setMana(pastryCook.getBaseManaCost());
			assertFalse(context.getLogic().conditionMet(player.getId(), pastryCook));
		});

		// Roasted using a spell
		runGym((context, player, opponent) -> {
			// Make sure the number stuck into the ROASTED attribute as the current turn isn't idiosyncratically zero
			context.endTurn();
			context.endTurn();
			Card pastryCook = receiveCard(context, player, "minion_pastry_cook");
			Card shouldBeRoasted = shuffleToDeck(context, player, "minion_neutral_test");
			playCard(context, player, "minion_food_critic");
			assertEquals(shouldBeRoasted.getAttributeValue(Attribute.ROASTED), context.getTurn());
			player.setMana(pastryCook.getBaseManaCost());
			assertTrue(context.getLogic().conditionMet(player.getId(), pastryCook));
		});

		// Roasted by having a full hand
		runGym((context, player, opponent) -> {
			// Make sure the number stuck into the ROASTED attribute as the current turn isn't idiosyncratically zero
			context.endTurn();
			context.endTurn();
			Card pastryCook = receiveCard(context, player, "minion_pastry_cook");
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "minion_neutral_test");
			}
			Card shouldBeRoasted = shuffleToDeck(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_draw_cards");
			assertEquals(shouldBeRoasted.getAttributeValue(Attribute.ROASTED), context.getTurn());
			player.setMana(pastryCook.getBaseManaCost());
			assertTrue(context.getLogic().conditionMet(player.getId(), pastryCook));
		});
	}

	@Test
	@Disabled("What to do about bananas?")
	public void testBossHarambo() {
		runGym((context, player, opponent) -> {
			int BANANAS_EXPECTED_IN_HAND = 7;
			int BANANAS_EXPECTED_IN_DECK = 10 - BANANAS_EXPECTED_IN_HAND;
			for (int i = 0; i < 10 - BANANAS_EXPECTED_IN_HAND; i++) {
				receiveCard(context, player, "minion_neutral_test");
			}
			playCard(context, player, "minion_boss_harambo");
			assertEquals(player.getHand().filtered(c -> c.getCardId().equals("spell_apple")).size(), BANANAS_EXPECTED_IN_HAND);
			assertEquals(player.getDeck().size(), BANANAS_EXPECTED_IN_DECK);
			assertEquals(Stream.concat(player.getHand().stream(),
					player.getDeck().stream()).filter(c -> c.getCardId().equals("spell_apple")).count(), 10);
		});
	}

	@Test
	public void testFantasticFeast() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(1);
			playCard(context, player, "spell_fantastic_feast");
			assertEquals(player.getDeck().size(), 2);
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
		});
	}

	@Test
	public void testFleshMonstrosity() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_divine_shield_test");
			target.setAttack(10);
			target.setHp(15);
			Minion fleshMonstrosity = playMinionCard(context, player, "minion_flesh_monstrosity", target);
			assertEquals(fleshMonstrosity.getAttack(), fleshMonstrosity.getBaseAttack() + target.getAttack());
			assertEquals(fleshMonstrosity.getMaxHp(), fleshMonstrosity.getBaseHp() + target.getHp());
			assertTrue(fleshMonstrosity.hasAttribute(Attribute.DIVINE_SHIELD));
			assertTrue(fleshMonstrosity.getDescription().contains("Divine Shield"));
		});
	}

	@Test
	public void testBlackOxBrew() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_black_ox_brew");
			int hp = player.getHero().getHp();
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHero().getHp(), hp - 1);
			context.endTurn();
			hp = player.getHero().getHp();
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHero().getHp(), hp - 1);
			context.endTurn();
			hp = player.getHero().getHp();
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(player.getHero().getHp(), hp - 6);
		});
	}

	@Test
	public void testKinruTheBenevolent() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_kinru_the_benevolent");
			playCard(context, player, "spell_test_destroy_all");
			assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test
	public void testSeaWitchShufflesCard() {
		runGym((context, player, opponent) -> {
			useHeroPower(context, player, player.getHero().getReference());
			assertEquals(player.getDeck().get(0).getCardId(), "spell_ocean_depths");
		}, "TEAL", "TEAL");
	}

	@Test
	public void testLoyalLandowner() {
		runGym((context, player, opponent) -> {
			Minion loyal = playMinionCard(context, player, "minion_loyal_landowner");
			context.endTurn();
			context.endTurn();
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK));
			attack(context, player, loyal, opponent.getHero());
			assertEquals(player.getMinions().size(), 2);
			assertFalse(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK));
		});
	}

	@Test
	public void testBrothersInBlood() {
		runGym((context, player, opponent) -> {
			/*
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_brothers_in_blood", target);
			Minion buffed = playMinionCard(context, player, "minion_neutral_test");
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() * 2);
			assertEquals(buffed.getMaxHp(), buffed.getBaseHp() * 2);
			Minion notBuffed = playMinionCard(context, player, "minion_black_test");
			assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
			assertEquals(notBuffed.getMaxHp(), notBuffed.getBaseHp());
			*/


			Minion target = playMinionCard(context, player, "minion_neutral_test");
			assertEquals(player.getMinions().size(), 1);
			playCard(context, player, "spell_brothers_in_blood", target);
			assertEquals(player.getMinions().size(), 1);
			playMinionCard(context, player, "minion_neutral_test");
			assertEquals(player.getMinions().size(), 3);
			playMinionCard(context, player, "minion_black_test");
			assertEquals(player.getMinions().size(), 4);
		});
	}

	@Test
	public void testUnearthedHorror() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_unearthed_horror");
			for (int i = 1; i < 4; i++) {
				playCard(context, player, "spell_underwater_horrors", target);
				context.getLogic().drawCard(player.getId(), player);
				assertEquals(player.getHand().get(0).getCardId(), "minion_unearthed_horror");
				target = playMinionCard(context, player, player.getHand().get(0));
				assertEquals(target.getAttack(), target.getBaseAttack() + i * 7);
				assertEquals(target.getMaxHp(), target.getBaseHp() + i * 7);
			}
		});
	}

	@Test
	public void testBodyswap() {
		runGym((context, player, opponent) -> {
			Minion test = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_bodyswap", test);
			assertEquals(test.getHp(), 30);
			assertEquals(player.getHero().getHp(), CardCatalogue.getCardById("minion_neutral_test").getBaseHp() + 10);
		});
	}

	@Test
	public void testSoulcallerRoten() {
		runGym((context, player, opponent) -> {
			// Cost 1
			playMinionCard(context, player, "minion_neutral_test_1");
			// Cost 2
			Minion destroyed = playMinionCard(context, player, "minion_test_3_2");
			// Cost 3
			playMinionCard(context, player, "minion_smug_theorist");
			destroy(context, destroyed);
			Card card1 = receiveCard(context, player, "minion_cost_three_test");
			Card card2 = receiveCard(context, player, "minion_cost_three_test");
			Card card3 = receiveCard(context, player, "minion_neutral_test");
			Card roten = receiveCard(context, player, "minion_soulcaller_roten");
			assertEquals(roten.getDescription(context, player), "Opener: Summon all (3)-Cost minions from your hand. (Equals the cost of the last minion you played)");
			playCard(context, player, roten);
			assertEquals(card1.getZone(), Zones.GRAVEYARD);
			assertEquals(card2.getZone(), Zones.GRAVEYARD);
			assertEquals(card3.getZone(), Zones.HAND);
			assertEquals(player.getMinions().size(), 5);
		});
	}

	@Test
	@Disabled("Weird fatigue stuff happening here")
	public void testBloodseeker() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			player.setAttribute(Attribute.DISABLE_FATIGUE);
			opponent.setAttribute(Attribute.DISABLE_FATIGUE);
		});
		factory.run((context, player, opponent) -> {
			Minion bloodseeker = playMinionCard(context, player, "minion_bloodseeker");
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			playCard(context, player, "spell_test_deal_1", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 1);
			context.endTurn();
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
		});

		factory.run((context, player, opponent) -> {
			Minion bloodseeker = playMinionCard(context, player, "minion_bloodseeker");
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			context.endTurn();
			playMinionCard(context, opponent, "minion_kobold_librarian");
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 6);
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 12);
			context.endTurn();
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
		});

		factory.run((context, player, opponent) -> {
			Minion bloodseeker1 = playMinionCard(context, player, "minion_bloodseeker");
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(bloodseeker1.getAttack(), bloodseeker1.getBaseAttack() + 6);
			playCard(context, player, "minion_herald_volazj");
			Minion bloodseeker2 = player.getMinions().get(2);
			assertEquals(bloodseeker2.getSourceCard().getCardId(), "minion_bloodseeker");
			assertNotEquals(bloodseeker1, bloodseeker2);
			assertEquals(bloodseeker2.getAttack(), 7, "it's a 1/1 + 6");
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(bloodseeker1.getAttack(), bloodseeker1.getBaseAttack() + 12);
			assertEquals(bloodseeker2.getAttack(), 1 + 12, "it's a 1/1 + 12");
			Minion bloodseeker3 = playMinionCard(context, player, "minion_bloodseeker");
			assertEquals(bloodseeker3.getAttack(), bloodseeker3.getBaseAttack() + 12);
			Minion bloodseeker4 = playMinionCard(context, player, "minion_faceless_manipulator", bloodseeker3);
			assertEquals(bloodseeker4.getSourceCard().getCardId(), "minion_bloodseeker");
			assertNotEquals(bloodseeker1, bloodseeker4);
			assertNotEquals(bloodseeker2, bloodseeker4);
			assertNotEquals(bloodseeker3, bloodseeker4);
			assertEquals(bloodseeker4.getAttack(), bloodseeker4.getBaseAttack() + 12);
			context.endTurn();
			for (Minion bloodseeker : new Minion[]{bloodseeker1, bloodseeker3, bloodseeker4}) {
				assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack());
			}
			assertEquals(bloodseeker2.getAttack(), 1);
		});

		factory.run((context, player, opponent) -> {
			Minion bloodseeker = playMinionCard(context, player, "minion_bloodseeker");
			playCard(context, player, "spell_test_deal_6", player.getHero());
			assertEquals(bloodseeker.getAttack(), bloodseeker.getBaseAttack() + 6);
			Minion faceless = playMinionCard(context, player, "minion_faceless_manipulator", bloodseeker);
			assertEquals(faceless.getAttack(), faceless.getBaseAttack() + 6);
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			playCard(context, opponent, "spell_test_deal_1", opponent.getHero());
			assertEquals(faceless.getAttack(), faceless.getBaseAttack() + 6);
			playCard(context, opponent, "spell_mind_control", faceless);
			assertEquals(faceless.getOwner(), opponent.getId());
			assertEquals(faceless.getAttack(), faceless.getBaseAttack() + 1, "+1 from razorpetal damage this turn");
			context.endTurn();
			assertEquals(faceless.getAttack(), faceless.getBaseAttack());
		});
	}

	@Test
	public void testOnyxPawn() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 15; i++) {
				putOnTopOfDeck(context, player, "spell_test_gain_mana");
			}
			Minion transformed = playMinionCard(context, player, "minion_onyx_pawn");
			assertEquals(transformed.getSourceCard().getCardId(), "token_onyx_queen");
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				putOnTopOfDeck(context, player, "spell_test_gain_mana");
			}
			Minion transformed = playMinionCard(context, player, "minion_onyx_pawn");
			assertEquals(transformed.getSourceCard().getCardId(), "token_onyx_queen");
		});
	}

	@Test
	public void testHeadlessHorseman() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_headless_horseman");
			Minion headlessHorseman = player.getMinions().stream().filter(m -> m.getSourceCard().getCardId().equals("minion_headless_horseman")).findFirst().orElseThrow(AssertionError::new);
			assertEquals(player.getMinions().size(), 7);
			destroy(context, headlessHorseman);
			assertTrue(player.getMinions().stream().noneMatch(m -> m.hasAttribute(Attribute.PERMANENT)));
		});
	}

	@Test
	public void testChaugnarTheCorruptor() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test");
			playCard(context, player, "minion_chaugnar_the_corruptor");
			context.endTurn();
			int hp = player.getHero().getHp();
			context.endTurn();
			assertEquals(hp - 5, player.getHero().getHp());
		});
	}

	@Test
	public void testReaderEaterGhahnbTheJudicatorInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 20; i++) {
				shuffleToDeck(context, player, "spell_test_gain_mana");
			}
			playCard(context, player, "minion_ghahnb_the_judicator");
			Minion buffed = playMinionCard(context, player, "minion_reader_eater");
			useHeroPower(context, player, player.getHero().getReference());
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() + 15);
			assertEquals(buffed.getMaxHp(), buffed.getBaseHp() + 15);
			assertEquals(player.getDeck().size(), 20 - 15, "Drew 15 cards, never added 15");
		});
	}

	@Test
	public void testUnderwaterHorrors() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "spell_underwater_horrors", target);
			putOnTopOfDeck(context, opponent, "spell_test_gain_mana");
			context.endTurn();
			assertEquals(opponent.getHand().size(), 1);
			assertEquals(opponent.getHand().get(0).getCardId(), "spell_test_gain_mana");
			context.endTurn();
			assertEquals(opponent.getHand().size(), 2);
			assertEquals(opponent.getHand().get(1).getCardId(), "minion_neutral_test");
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_underwater_horrors", target);
			putOnTopOfDeck(context, player, "spell_test_gain_mana");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_neutral_test");
		});
	}

	@Test
	public void testForgottenScience() {
		runGym((context, player, opponent) -> {
			Minion intendedTarget = playMinionCard(context, player, "minion_neutral_test");
			Minion target1 = playMinionCard(context, player, "minion_neutral_test");
			Minion target2 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_forgotten_science");
			playCard(context, player, "spell_test_deal_6", intendedTarget);
			assertTrue(intendedTarget.isDestroyed());
			assertTrue(target1.isDestroyed());
			// Not adjacent!
			assertFalse(target2.isDestroyed());
			target1 = playMinionCard(context, player, "minion_neutral_test");
			intendedTarget = playMinionCard(context, player, "minion_neutral_test");
			target2 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_deal_6", intendedTarget);
			assertTrue(intendedTarget.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
		});

		runGym((context, player, opponent) -> {
			Minion intendedTarget = playMinionCard(context, player, "minion_neutral_test");
			Minion target1 = playMinionCard(context, player, "minion_neutral_test");
			Minion target2 = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_forgotten_science");
			playCard(context, player, "spell_test_deal_1", intendedTarget);
			assertFalse(intendedTarget.isDestroyed());
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
		});
	}

	@Test
	public void testKahlOfTheDeep() {
		runGym((context, player, opponent) -> {
			Minion kahl = playMinionCard(context, player, "minion_kahl_of_the_deep");
			destroy(context, kahl);
			assertEquals(opponent.getDeck().size(), 1);
			for (int i = 0; i < 4; i++) {
				// Inserts to the bottom of the deck
				context.getLogic().insertIntoDeck(opponent, CardCatalogue.getCardById("spell_test_gain_mana"), 0);
			}
			assertEquals(opponent.getDeck().size(), 5);
			context.endTurn();
			assertEquals(opponent.getHand().size(), 4, "Drew 3 cards + Kahl");
			assertEquals(opponent.getDeck().size(), 1, "1 card left in the deck");
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_kahl_of_the_deep");
		});
	}

	@Test
	public void testTaintedRavenSilenceInteraction() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				receiveCard(context, opponent, "spell_test_gain_mana");
			}
			Minion taintedRaven = playMinionCard(context, player, "minion_tainted_raven");
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			assertEquals(context.getLogic().applySpellpower(player, fireball, 6), 8);
			context.getLogic().silence(player.getId(), taintedRaven);
			assertEquals(context.getLogic().applySpellpower(player, fireball, 6), 6);
		});
	}

	@Test
	public void testPrimordialSupremacy() {
		runGym((context, player, opponent) -> {
			Minion titan = playMinionCard(context, player, "minion_degenerator");
			Minion xenodrone = playMinionCard(context, player, "token_xenodrone_01");
			Minion buffed = playMinionCard(context, player, "minion_neutral_test");
			buffed.setAttribute(Attribute.WITHER, 1);
			Minion notBuffed = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_primordial_supremacy");
			for (Minion shouldBeBuffed : new Minion[]{titan, xenodrone}) {
				assertEquals(shouldBeBuffed.getAttack(), shouldBeBuffed.getBaseAttack() + 1);
				assertEquals(shouldBeBuffed.getMaxHp(), shouldBeBuffed.getBaseHp() + 2);
			}
			assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
			assertEquals(notBuffed.getMaxHp(), notBuffed.getBaseHp());
		});
	}

	@Test
	public void testHeartpiercer() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion hit = playMinionCard(context, player, "minion_neutral_test");
			hit.setHp(CardCatalogue.getCardById("weapon_heartpiercer").getDamage() + 2);
			Minion notHit = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			playCard(context, player, "weapon_heartpiercer");
			player.getHero().getWeapon().setHp(1);
			attack(context, player, player.getHero(), hit);
			assertTrue(hit.isDestroyed());
			assertEquals(player.getHero().getAttributeValue(Attribute.DRAINED_THIS_TURN), 2);
		});
	}

	@Test
	public void testTheBloodEngine() {
		runGym((context, player, opponent) -> {
			// Drain 2 total
			playCard(context, player, "spell_test_drain", opponent.getHero());
			playCard(context, player, "spell_test_drain", opponent.getHero());
			context.endTurn();
			context.endTurn();
			// Drain three this turn
			playCard(context, player, "spell_test_drain", opponent.getHero());
			playCard(context, player, "spell_test_drain", opponent.getHero());
			playCard(context, player, "spell_test_drain", opponent.getHero());
			int opponentHealth = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_the_blood_engine");
			assertEquals(opponent.getHero().getHp(), opponentHealth - 2);
		});
	}

	@Test
	public void testBloodToIron() {
		runGym((context, player, opponent) -> {
			Minion mech = playMinionCard(context, player, "minion_mech_test");
			Card bloodToIronCard = receiveCard(context, player, "spell_blood_to_iron");
			assertTrue(bloodToIronCard.getDescription(context, player).contains("Take 2"));
			int hp = player.getHero().getHp();
			playCard(context, player, bloodToIronCard);
			assertEquals(player.getHero().getHp(), hp - 2);
			assertEquals(player.getMinions().size(), 2);
		});

		runGym((context, player, opponent) -> {
			Minion mech = playMinionCard(context, player, "minion_mech_test");
			mech.setAttribute(Attribute.SPELL_DAMAGE, 1);
			Card bloodToIronCard = receiveCard(context, player, "spell_blood_to_iron");
			assertTrue(bloodToIronCard.getDescription(context, player).contains("Take *3*"));
			int hp = player.getHero().getHp();
			playCard(context, player, bloodToIronCard);
			assertEquals(player.getHero().getHp(), hp - 3);
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testTaintedRaven() {
		runGym((context, player, opponent) -> {
			Minion taintedRaven = playMinionCard(context, player, "minion_tainted_raven");
			for (int i = 0; i < 5; i++) {
				receiveCard(context, opponent, "spell_test_gain_mana");
			}
			context.getLogic().endOfSequence();
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 6, "No spell damage yet");
			receiveCard(context, opponent, "spell_test_gain_mana");
			context.getLogic().endOfSequence();
			hp = opponent.getHero().getHp();
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 6 - 2, "+2 spell damage");
			context.getLogic().discardCard(opponent, opponent.getHand().get(0));
			context.getLogic().discardCard(opponent, opponent.getHand().get(0));
			hp = opponent.getHero().getHp();
			context.getLogic().endOfSequence();
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 6, "No spell damage");
		});
	}

	@Test
	public void testBloodMoonRising() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_blood_moon_rising");
			Minion charger = playMinionCard(context, player, "minion_charge_test");
			attack(context, player, charger, opponent.getHero());
			assertEquals(player.getHero().getMaxHp(), player.getHero().getBaseHp() + charger.getAttack());
		});
	}

	@Test
	public void testVampiricSavage() {
		runGym((context, player, opponent) -> {
			Minion savage = playMinionCard(context, player, "minion_vampiric_savage");
			playCard(context, player, "spell_hemoshield");
			assertEquals(savage.getMaxHp(), savage.getBaseHp() + 5);
		});
	}

	@Test
	public void testDeathsCaress() {
		runGym((context, player, opponent) -> {
			Minion spellDamage1 = playMinionCard(context, player, "minion_neutral_test");
			spellDamage1.setAttribute(Attribute.SPELL_DAMAGE, 1);
			Minion testTarget = playMinionCard(context, player, "minion_neutral_test");
			testTarget.setHp(10);
			playCard(context, player, "spell_deaths_caress", testTarget);
			context.endTurn();
			assertEquals(testTarget.getHp(), 6, "Should have been dealt 3 + 1 spell damage");
		});
	}

	@Test
	public void testRoll() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_test_cost_3_buff", target);
			playCard(context, player, "spell_roll", target);
			Minion newTarget = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(newTarget.getAttack(), target.getBaseAttack() + 1);
		});
	}

	@Test
	public void testScatterstorm() {
		runGym((context, player, opponent) -> {
			Minion friendly = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			Card newFriendly = shuffleToDeck(context, player, "minion_test_3_2");
			Card newEnemy = shuffleToDeck(context, opponent, "minion_test_3_2");
			playCard(context, player, "spell_scatterstorm");
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), friendly.getSourceCard().getCardId());
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), newFriendly.getSourceCard().getCardId());
			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getDeck().get(0).getCardId(), enemy.getSourceCard().getCardId());
			assertEquals(opponent.getMinions().get(0).getSourceCard().getCardId(), newEnemy.getSourceCard().getCardId());
		});
	}

	@Test
	public void testSweetStrategy() {
		runGym((context, player, opponent) -> {
			Card shouldNotShuffle = receiveCard(context, player, "spell_test_gain_mana");
			Card shouldShuffle = receiveCard(context, player, "spell_test_deal_6");
			playCard(context, player, "spell_sweet_strategy");
			assertEquals(player.getDeck().size(), 2);
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals(shouldShuffle.getCardId())));
		});
	}

	@Test
	public void testOneOnOne() {
		// Test 2 enemies, CAN choose enemy because there is another enemy to attack it.
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion enemy2 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Card checkTargets = receiveCard(context, player, "spell_one_on_one");
			player.setMana(3);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getSourceReference().equals(checkTargets.getReference()) && ga.getTargetReference().equals(enemy1.getReference())));
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getSourceReference().equals(checkTargets.getReference()) && ga.getTargetReference().equals(enemy2.getReference())));
		});

		// Test 1 enemy, two friendlies, cannot choose enemy because there won't be another enemy to attack it
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			Minion friendly1 = playMinionCard(context, player, "minion_neutral_test");
			Minion friendly2 = playMinionCard(context, player, "minion_neutral_test");
			Card checkTargets = receiveCard(context, player, "spell_one_on_one");
			player.setMana(3);
			assertFalse(context.getValidActions().stream().anyMatch(ga -> ga.getSourceReference().equals(checkTargets.getReference()) && ga.getTargetReference().equals(enemy.getReference())));
		});

		// Test 2 enemies, choose one, check that the other initiates the attack
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion enemy1 = playMinionCard(context, opponent, "minion_neutral_test");
			// Choosing values for unambiguous differences
			enemy1.setAttack(1);
			enemy1.setHp(3);
			Minion enemy2 = playMinionCard(context, opponent, "minion_neutral_test");
			enemy2.setAttack(2);
			enemy2.setHp(5);
			context.endTurn();
			Card checkTargets = receiveCard(context, player, "spell_one_on_one");
			player.setMana(3);
			playCard(context, player, checkTargets, enemy1);
			assertEquals(enemy1.getHp(), 3 - 2);
			assertEquals(enemy2.getHp(), 5 - 1);
		});
	}

	@Test
	public void testDoubleDown() {
		// On your side: A marin, another marin, a treasure chest, a seven shot gunner, and another seven shot gunner. Note we destroyed the enemy treasure chest
		runGym((context, player, opponent) -> {
			Minion shouldNotHave = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, shouldNotHave);
			context.endTurn();
			context.endTurn();
			playCard(context, player, "spell_summon_for_opponent");
			Minion treasure = opponent.getMinions().get(0);
			playMinionCard(context, player, "minion_seven_shot_gunner");
			assertTrue(treasure.isDestroyed());
			playCard(context, player, "spell_double_down");
			assertEquals(player.getMinions().size(), 3);
			Map<String, Integer> counts = new HashMap<>();
			counts.put("minion_neutral_test_1", 1);
			counts.put("minion_seven_shot_gunner", 2);
			counts.put("minion_neutral_test", 0);
			for (Map.Entry<String, Integer> count : counts.entrySet()) {
				assertEquals(player.getMinions().stream().filter(e -> e.getSourceCard().getCardId().equals(count.getKey())).count(), (long) count.getValue());
			}
		});
	}

	@Test
	public void testFairyFixpicker() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_murloc_fixpicker");
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(player.getHand().size(), 2);
			assertTrue(Race.hasRace(context, player.getHand().get(0), Race.FAE));
			assertTrue(Race.hasRace(context, player.getHand().get(1), Race.FAE));
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testKitesailRavager() {
		// Should pick off minions one by one
		runGym((context, player, opponent) -> {
			Card kitesailCard = receiveCard(context, player, "minion_kitesail_ravager");
			int quantity = kitesailCard.getBaseHp() - 1;
			List<Minion> targets = new ArrayList<>();
			int friendlyCount = quantity / 2 + (quantity % 2);
			for (int i = 0; i < friendlyCount; i++) {
				targets.add(playMinionCard(context, player, "minion_neutral_test_1"));
			}
			int enemyCount = quantity / 2;
			context.endTurn();
			for (int i = 0; i < enemyCount; i++) {
				targets.add(playMinionCard(context, opponent, "minion_neutral_test_1"));
			}
			context.endTurn();
			Minion kitesail = playMinionCard(context, player, kitesailCard);
			assertTrue(targets.stream().allMatch(Actor::isDestroyed));
			assertEquals(kitesail.getHp(), 1);
		});
	}

	@Test
	public void testDoomgunners() {
		// Attack 3 times, required 4 to kill
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_doomgunners");
			context.endTurn();
			Minion defender = playMinionCard(context, player, "minion_neutral_test");
			defender.setAttack(0);
			defender.setHp(attacker.getAttack() * 3 + 1);
			context.endTurn();
			attack(context, player, attacker, defender);
			assertFalse(attacker.isDestroyed());
			assertFalse(defender.isDestroyed());
			assertEquals(defender.getHp(), 1, "Should still be alive, after 3 attacks");
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - 3);
		});

		// Just needs 2 attacks to finish
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_doomgunners");
			context.endTurn();
			Minion defender = playMinionCard(context, player, "minion_neutral_test");
			defender.setAttack(0);
			defender.setHp(attacker.getAttack() * 2);
			context.endTurn();
			attack(context, player, attacker, defender);
			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - 2);
		});

		// Defender kills attacker during the second attack, should not trigger a third attack. Third attack would kill the defender
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_doomgunners");
			context.endTurn();
			Minion defender = playMinionCard(context, player, "minion_neutral_test");
			defender.setAttack(attacker.getHp() / 2);
			defender.setHp(attacker.getAttack() * 2 + 1);
			context.endTurn();
			attack(context, player, attacker, defender);
			assertTrue(attacker.isDestroyed());
			assertFalse(defender.isDestroyed());
			assertEquals(defender.getHp(), 1, "Should still be alive, after 2 attacks");
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - 2, "Attacker should have only get 2 attacks");
		});
	}

	@Test
	@Disabled
	public void testCattaTheMerciless() {
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();

			attack(context, player, attacker, defender);

			assertTrue(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 0);
		});

		// Test interaction that requires 2 attacks for defender to die
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			defender.setAttack(0);
			attacker.setAttack(1);
			context.endTurn();

			int expectedAttacks = defender.getHp() / attacker.getAttack();
			attack(context, player, attacker, defender);

			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - expectedAttacks);
		});

		// Test interaction with immune
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			defender.setAttack(96);
			defender.setHp(96);
			attacker.setAttribute(Attribute.IMMUNE);
			attacker.setAttack(1);
			context.endTurn();

			int expectedAttacks = defender.getHp() / attacker.getAttack();
			attack(context, player, attacker, defender);

			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), 1 - expectedAttacks);
		});

		// Test infinite recursion mitigation
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");

			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test");
			defender.setAttack(0);
			defender.setHp(97);
			attacker.setAttack(1);
			context.endTurn();

			int expectedAttacks = defender.getHp() / attacker.getAttack();
			attack(context, player, attacker, defender);

			assertFalse(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertEquals(attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS), -95, "Hit limit on attacks");
		});

		// Test interaction with garbasu monster and immune while attack
		runGym((context, player, opponent) -> {
			Minion catta = playMinionCard(context, player, "minion_catta_the_merciless");
			Minion attacker = playMinionCard(context, player, "minion_neutral_test");
			attacker.setAttribute(Attribute.IMMUNE_WHILE_ATTACKING);
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_garbasu_monster");
			context.endTurn();
			attack(context, player, attacker, defender);
		});
	}

	@Test
	public void testAysaCloudsinger() {
		GymFactory factory = getGymFactory((context, player, opponent) -> {
			int heroHp = 15;
			player.getHero().setHp(heroHp);
			playCard(context, player, "minion_aysa_cloudsinger");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_aysa_cloudsinger");
			assertEquals(player.getMinions().size(), 1);
			Minion chenToken = player.getMinions().get(0);
			assertEquals(chenToken.getSourceCard().getCardId(), "token_chen_stormstout");
			assertEquals(chenToken.getHp(), heroHp);
			assertEquals(chenToken.getMaxHp(), 30);
		});

		// Test 1: Fatal damage, chen destroyed, player loses
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			destroy(context, chenToken);
			playCard(context, player, "spell_test_deal_10", player.getHero());
			assertTrue(context.updateAndGetGameOver());
			assertEquals(context.getWinner(), opponent);
		});

		// Test 2: fatal damage, chen on board, player restores chen with health
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			int hpValue = 17;
			chenToken.setHp(hpValue);
			playCard(context, player, "spell_test_deal_10", player.getHero());
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_mienzhou");
			assertEquals(player.getHero().getHp(), hpValue);
			assertEquals(player.getMinions().size(), 0);
		});

		// Test 3: fatal damage, chen in hand, player restores chen with full health
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			context.endTurn();
			playCard(context, opponent, "spell_test_return_to_hand", chenToken);
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_test_deal_10", player.getHero());
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_mienzhou");
			assertEquals(player.getHero().getHp(), 30);
			assertEquals(player.getHand().size(), 0);
		});

		// Test 4: fatal damage, chen in deck, player restores chen with full health.
		factory.run((context, player, opponent) -> {
			Minion chenToken = player.getMinions().get(0);
			context.endTurn();
			playCard(context, player, "spell_shuffle_minion_to_deck", chenToken);
			// Give the player something to draw
			putOnTopOfDeck(context, player, "spell_test_gain_mana");
			context.endTurn();
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_test_deal_10", player.getHero());
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_mienzhou");
			assertEquals(player.getHero().getHp(), 30);
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testFissureLordXahdorahInteraction() {
		runGym((context, player, opponent) -> {
			Minion xahDorah = playMinionCard(context, player, "minion_lord_xah_dorah");
			context.endTurn();
			Minion warGolem = playMinionCard(context, opponent, "minion_neutral_test_big");
			context.endTurn();
			useHeroPower(context, player, xahDorah.getReference());
			assertEquals(xahDorah.getAttack(), xahDorah.getBaseAttack() + 1);
			playCard(context, player, "spell_fissure");
			assertTrue(warGolem.isDestroyed());
		}, "RUST", "RUST");
	}

	@Test
	public void testIdiotSandwich() {
		runGym((context, player, opponent) -> {
			Minion notBuffed1 = playMinionCard(context, player, "minion_neutral_test");
			Minion left = playMinionCard(context, player, "minion_neutral_test");
			Minion idiot = playMinionCard(context, player, "minion_idiot_sandwich");
			Minion right = playMinionCard(context, player, "minion_neutral_test");
			Minion notBuffed2 = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, idiot);
			assertEquals(left.getAttack(), left.getBaseAttack() + 2);
			assertEquals(left.getMaxHp(), left.getBaseHp() + 2);
			assertEquals(right.getAttack(), right.getBaseAttack() + 2);
			assertEquals(right.getMaxHp(), right.getBaseHp() + 2);
			for (Minion notBuffed : new Minion[]{notBuffed1, notBuffed2}) {
				assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
				assertEquals(notBuffed.getMaxHp(), notBuffed.getBaseHp());
			}
		});
	}

	/*
	@Test
	public void testAncestralPlaneGrumbleTheWorldshakerInteraction() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_grumble_worldshaker");
			context.endTurn();
			playCard(context, opponent, "spell_psychic_scream");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_grumble_worldshaker");
			putOnTopOfDeck(context, player, "spell_test_gain_mana");
			context.endTurn();
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "spell_test_gain_mana", "Should draw the Coin");
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getDeck().get(0).getCardId(), "minion_grumble_worldshaker");
			AtomicBoolean didDiscover = new AtomicBoolean();
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardId(), "minion_grumble_worldshaker");
				assertTrue(didDiscover.compareAndSet(false, true), "should discover once");
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_ancestral_plane");
			assertTrue(didDiscover.get());
			assertEquals(player.getHand().get(1).getCardId(), "minion_grumble_worldshaker");
		});
	}

	 */

	@Test
	public void testLuminaLightOfTheforest() {
		// Test that Lumina doesn't trigger off herself
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				fail("should not discover");
				return discoverActions.get(0);
			});
			playMinionCard(context, player, "minion_lumina");
		});

		// Test that Lumina only discovers minions and not spells
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardType(), CardType.MINION);
				return discoverActions.get(0);
			});
			context.setDeckFormat(new FixedCardsDeckFormat("minion_neutral_test", "spell_test_deal_6"));
			playMinionCard(context, player, "minion_lumina");
			playMinionCard(context, player, "minion_neutral_test");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_neutral_test");
		});

		// Test that Lumina discovers minions of the same tribe
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(discoverActions.size(), 1);
				assertEquals(discoverActions.get(0).getCard().getCardType(), CardType.MINION);
				return discoverActions.get(0);
			});
			context.setDeckFormat(new FixedCardsDeckFormat("minion_test_3_2_fae", "spell_test_deal_6"));
			playMinionCard(context, player, "minion_lumina");
			playMinionCard(context, player, "minion_test_3_2_fae");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2_fae");
		}, "ANY", "ANY");
	}

	@Test
	@Disabled("What to do about adapt")
	public void testTikrakazzLordJaraxxusInteraction() {
		runGym((context, player, opponent) -> {
			// Adapting Lord Jaraxxus in this phase shouldn't cause an exception, because changing heroes the way the Lord
			// does seems to put the minion into the removed from play zone.
			playCard(context, player, "minion_tikrakazz");
			playCard(context, player, "minion_lord_jaraxxus");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_jaraxxus");
			playCard(context, player, "minion_lord_jaraxxus");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_jaraxxus");
		});
	}

	@Test
	public void testFendOff() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 2; i++) {
				playMinionCard(context, player, "minion_neutral_test_1");
			}
			context.endTurn();
			for (int i = 0; i < 3; i++) {
				Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
				context.getLogic().setHpAndMaxHp(enemy, 10);
			}
			context.endTurn();
			playCard(context, player, "spell_fend_off");
			for (Minion source : player.getMinions()) {
				assertEquals(source.getHp(), source.getMaxHp() - 1, "Should take 1 damage from attacking a random target");
			}
			assertEquals(opponent.getMinions().stream().mapToInt(m -> m.getMaxHp() - m.getHp()).sum(), player.getMinions().stream().mapToInt(Minion::getAttack).sum(), "Each source minion should have attacked");
		});
	}

	@Test
	public void testRhunokTheBear() {
		runGym((context, player, opponent) -> {
			Minion bear = playMinionCard(context, player, "minion_rhunok_the_bear");
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_apple", bear);
			assertEquals(player.getMinions().size(), 2);
			playCard(context, player, "spell_apple", wisp);
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "minion_neutral_test_1", "Wisp copied");
			Minion wispCopy = player.getMinions().get(2);
			assertEquals(wispCopy.getAttack(), wispCopy.getBaseAttack() + 1, "Keeps buffs since it was copied AFTER the spell was cast");
			assertEquals(wispCopy.getMaxHp(), wispCopy.getBaseHp() + 1, "Keeps buffs since it was copied AFTER the spell was cast");
			assertTrue(wispCopy.hasAttribute(Attribute.TAUNT), "Gained taunt");
		});
	}

	@Test
	public void testElaborateSchemeGloatInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_elaborate_scheme");
			Card gloat = putOnTopOfDeck(context, player, "secret_gloat");
			putOnTopOfDeck(context, player, "spell_test_gain_mana");
			putOnTopOfDeck(context, player, "spell_test_gain_mana");
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "secret_gloat");
		});
	}

	@Test
	public void testGiantBarbecue() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion target2 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion target3 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_giant_barbecue", target2);
			assertTrue(target1.isDestroyed());
			assertTrue(target2.isDestroyed());
			assertTrue(target3.isDestroyed());
		});
	}

	@Test
	public void testSidelineCoach() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_sideline_coach");
			Minion attacker = playMinionCard(context, player, "minion_neutral_test_1");
			context.endTurn();
			Minion defender = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, player, attacker, defender);
			assertEquals(attacker.getAttack(), attacker.getBaseAttack() + 1);
			assertEquals(attacker.getMaxHp(), attacker.getBaseHp() + 1);
			assertEquals(defender.getAttack(), defender.getBaseAttack());
			assertEquals(defender.getMaxHp(), defender.getBaseHp());
		});
	}

	@Test
	public void testDoctorHatchett() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2");
			playMinionCard(context, player, "minion_doctor_hatchett");
			// Destroy the egg
			destroy(context, player.getMinions().get(1));
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_test_3_2");
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testAnobii() {
		runGym((context, player, opponent) -> {
			Minion beasttest32 = playMinionCard(context, player, "minion_test_3_2");
			playMinionCard(context, player, "minion_anobii", beasttest32);
			Minion anobii = player.getMinions().get(1);
			beasttest32 = (Minion) beasttest32.transformResolved(context);
			assertEquals(beasttest32.getSourceCard().getCardId(), "permanent_cocoon");
			destroy(context, anobii);
			beasttest32 = (Minion) beasttest32.transformResolved(context);
			assertEquals(beasttest32.getSourceCard().getCardId(), "minion_test_3_2");
		});
	}

	@Test
	public void testCryptladyZara() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "hero_cryptlady_zara");
			playCard(context, player, "spell_test_deal_6", target);
			assertEquals(target.getHp(), target.getMaxHp() - 1);
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_4_to_enemies");
			assertEquals(target.getHp(), target.getMaxHp() - 1 - 4);
		});
	}

	@Test
	public void testColosseumBehemoth() {
		runGym((context, player, opponent) -> {
			Minion behemoth = playMinionCard(context, player, "minion_colosseum_behemoth");
			context.endTurn();
			Minion beasttest32 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			assertTrue(context.getValidActions().stream().filter(pa -> pa.getActionType() == ActionType.PHYSICAL_ATTACK).allMatch(pa -> pa.getTargetReference().equals(opponent.getHero().getReference())));
		});
	}

	@Test
	public void testEchoingPotion() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_echoing_potion");
			playCard(context, player, "minion_neutral_test_1");
			assertEquals(player.getMinions().size(), 2);
			Minion copy = player.getMinions().get(1);
			assertEquals(copy.getSourceCard().getCardId(), "minion_neutral_test_1");
			assertEquals(copy.getAttack(), 3);
			assertEquals(copy.getMaxHp(), 3);
		});
	}

	@Test
	public void testMushrooms() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion big = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			context.endTurn();
			playCard(context, player, "spell_clarity_mushroom");
			player.setMana(10);
			assertTrue(context.getValidActions().stream().anyMatch(hp -> hp.getActionType() == ActionType.HERO_POWER && hp.getTargetReference().equals(big.getReference())));
			useHeroPower(context, player, big.getReference());
			assertEquals(big.getHp(), big.getMaxHp() - 4);
		}, "TOAST", "TOAST");

		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_hallucinogenic_mushroom");
			player.setMana(10);
			int hp = opponent.getHero().getHp();
			useHeroPower(context, player);
			assertEquals(opponent.getHero().getHp(), hp - 4);
			assertEquals(player.getHand().size(), 1);
			assertTrue(Arrays.asList("spell_clarity_mushroom", "spell_healing_mushroom", "spell_toxic_mushroom", "spell_hallucinogenic_mushroom").contains(player.getHand().get(0).getCardId()));
		}, "TOAST", "TOAST");

		runGym((context, player, opponent) -> {
			player.getHero().setHp(10);
			playCard(context, player, "spell_healing_mushroom");
			player.setMana(10);
			int hp = opponent.getHero().getHp();
			useHeroPower(context, player);
			assertEquals(opponent.getHero().getHp(), hp - 4);
			assertEquals(player.getHero().getHp(), 10 + 4);
		}, "TOAST", "TOAST");

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion big = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			context.endTurn();
			playCard(context, player, "spell_toxic_mushroom");
			player.setMana(10);
			// Temporarily override the target of the cook hero power
			CardDesc clone = player.getHeroPowerZone().get(0).getDesc().clone();
			clone.setSpell(clone.getSpell().clone());
			SpellDesc damageSpell = (SpellDesc) clone.getSpell().getSpell().get(SpellArg.SPELL2);
			damageSpell.put(SpellArg.RANDOM_TARGET, false);
			damageSpell.put(SpellArg.TARGET, big.getReference());
			player.getHeroPowerZone().get(0).setDesc(clone);
			useHeroPower(context, player);
			assertTrue(big.isDestroyed());
		}, "TOAST", "TOAST");
	}

	@Test
	public void testSorrowstone() {
		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion target3 = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "secret_sorrowstone");
			context.endTurn();
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			destroy(context, target1);
			assertEquals(player.getSecrets().size(), 1);
			destroy(context, target2);
			assertEquals(player.getSecrets().size(), 1);
			destroy(context, target3);
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 3);
		});
	}

	@Test
	@Disabled("What to do about murlocs")
	public void testCatacombCandlefin() {
		runGym((context, player, opponent) -> {
			Minion shouldNotBeSummoned1 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion shouldBeSummoned = playMinionCard(context, player, "minion_murloc_warleader");
			Minion shouldNotBeSummoned2 = playMinionCard(context, player, "minion_test_3_2");
			destroy(context, shouldNotBeSummoned1);
			destroy(context, shouldBeSummoned);
			destroy(context, shouldNotBeSummoned2);
			playCard(context, player, "minion_catacomb_candlefin");
			assertEquals(player.getHand().get(0).getCardId(), shouldBeSummoned.getSourceCard().getCardId());
		});
	}

	@Test
	public void testWindsweptStrike() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion damaged = playMinionCard(context, opponent, "minion_test_3_2");
			playMinionCard(context, opponent, "minion_immune_test");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_windswept_strike");
			assertEquals(opponent.getHero().getHp(), opponentHp - 2, "Now includes immune minions");
			assertFalse(damaged.isDestroyed());
			assertEquals(damaged.getHp(), damaged.getMaxHp() - 1);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion damaged = playMinionCard(context, opponent, "minion_test_3_2");
			playMinionCard(context, opponent, "minion_immune_test");
			context.endTurn();
			player.setAttribute(Attribute.SPELL_DAMAGE, 1);
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "spell_windswept_strike");
			assertEquals(opponent.getHero().getHp(), opponentHp - 3, "Now uses spell damage");
			assertTrue(damaged.isDestroyed());
		});
	}

	@Test
	public void testCromwell() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_test_gain_mana");
			putOnTopOfDeck(context, player, "minion_neutral_test_big");
			assertEquals(context.resolveSingleTarget(player, player, EntityReference.FRIENDLY_TOP_CARD).getSourceCard().getCardId(), "minion_neutral_test_big");
			playCard(context, player, "minion_cromwell");
			assertEquals(context.resolveSingleTarget(player, player, EntityReference.FRIENDLY_TOP_CARD).getSourceCard().getCardId(), "spell_test_gain_mana");
		});
	}

	@Test
	public void testLavaSoup() {
		runGym((context, player, opponent) -> {
			Card shouldNotBeRoasted1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldBeRoasted1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldBeRoasted2 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			assertEquals(player.getDeck().size(), 3);
			Card cost2Card = receiveCard(context, player, "spell_cost_2_card");
			assertEquals(costOf(context, player, cost2Card), 2);
			player.setMaxMana(2);
			player.setMana(2);
			assertTrue(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			player.setMana(1);
			assertFalse(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			playCard(context, player, "spell_lava_soup");
			assertTrue(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			playCard(context, player, cost2Card);
			assertEquals(player.getDeck().size(), 1);
			assertTrue(shouldBeRoasted1.hasAttribute(Attribute.ROASTED));
			assertTrue(shouldBeRoasted2.hasAttribute(Attribute.ROASTED));
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
		});

		runGym((context, player, opponent) -> {
			Card shouldNotBeRoasted1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			assertEquals(player.getDeck().size(), 1);
			Card cost2Card = receiveCard(context, player, "spell_cost_2_card");
			assertEquals(costOf(context, player, cost2Card), 2);
			player.setMaxMana(2);
			player.setMana(2);
			assertTrue(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			player.setMana(1);
			assertFalse(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			playCard(context, player, "spell_lava_soup");
			assertFalse(context.getLogic().canPlayCard(player.getId(), cost2Card.getReference()));
			assertEquals(player.getDeck().size(), 1);
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
		});
	}

	@Test
	public void testDeathwingsDinner() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_neutral_test_1");
			Minion target = playMinionCard(context, player, "minion_neutral_test_6_7");
			Card shouldNotBeRoasted1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldBeRoasted1 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			Card shouldBeRoasted2 = putOnTopOfDeck(context, player, "spell_test_gain_mana");
			playCard(context, player, "spell_deathwing_s_dinner");
			assertEquals(player.getDeck().size(), 1);
			assertTrue(shouldBeRoasted1.hasAttribute(Attribute.ROASTED));
			assertTrue(shouldBeRoasted2.hasAttribute(Attribute.ROASTED));
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
			playCard(context, player, "spell_test_deal_6", target);
			assertTrue(target.isDestroyed());
			assertTrue(shouldBeRoasted1.hasAttribute(Attribute.ROASTED));
			assertTrue(shouldBeRoasted2.hasAttribute(Attribute.ROASTED));
			assertFalse(shouldNotBeRoasted1.hasAttribute(Attribute.ROASTED));
		});
	}

	@Test
	public void testChiliDragonbreath() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_chili_dragonbreath");
			Minion minion = playMinionCard(context, player, "minion_test_dragon_hand");
			assertEquals(minion.getAttack(), minion.getBaseAttack() + 1);
			assertEquals(minion.getMaxHp(), minion.getBaseHp() + 1);
		});
	}

	@Test
	public void testButcher() {
		// Destroy friendly, should get butcher in the same place. Should work with full board
		runGym((context, player, opponent) -> {
			Minion wisp0 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion wisp1 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion wisp2 = playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_neutral_test_1");
			assertEquals(wisp1.getEntityLocation().getIndex(), 1);
			playCard(context, player, "spell_butcher", wisp1);
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_pile_of_meat");
		});

		// Destroy enemy
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion wisp0 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion wisp1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion wisp2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			assertEquals(wisp1.getEntityLocation().getIndex(), 1);
			playCard(context, player, "spell_butcher", wisp1);
			assertEquals(opponent.getMinions().get(1).getSourceCard().getCardId(), "token_pile_of_meat");
		});
	}

	@Test
	public void testFogburner() {
		runGym((context, player, opponent) -> {
			Minion fogburner = playMinionCard(context, player, "minion_fogburner");
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_test_deal_2_to_all_minions");
			assertEquals(fogburner.getAttack(), fogburner.getBaseAttack() + 2, "+2 from two indirect damages");
			assertEquals(fogburner.getMaxHp(), fogburner.getBaseHp() + 2, "+2 from two indirect damages");
		});

		runGym((context, player, opponent) -> {
			Minion fogburner = playMinionCard(context, player, "minion_fogburner");
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_test_deal_6", wisp);
			assertEquals(fogburner.getAttack(), fogburner.getBaseAttack());
			assertEquals(fogburner.getMaxHp(), fogburner.getBaseHp());
		});
	}

	@Test
	public void testBananamancer() {
		runGym((context, player, opponent) -> {
			// Giving a hero bonus armor with a spell played from hand
			playMinionCard(context, player, "minion_bananamancer");
			playCard(context, player, "spell_test_attack_armor_3");
			assertEquals(player.getHero().getAttack(), 4, "3 + 1 spell damage");
			assertEquals(player.getHero().getArmor(), 4, "3 + 1 spell damage");
		});

		runGym((context, player, opponent) -> {
			// Giving a minion a buff from a spell should buff it, from a subsequent battlecry should not
			playMinionCard(context, player, "minion_bananamancer");
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_test_buff_minions_1");
			assertEquals(wisp.getAttack(), wisp.getBaseAttack() + 2, "1 + 1 spell damage");
			assertEquals(wisp.getHp(), wisp.getBaseHp() + 2, "1 + 1 spell damage");
			playMinionCard(context, player, "minion_test_buff", wisp);
			assertEquals(wisp.getAttack(), wisp.getBaseAttack() + 3, "1 + 1 spell damage + 1 Sun Cleric buff");
			assertEquals(wisp.getHp(), wisp.getBaseHp() + 3, "1 + 1 spell damage + 1 Sun Cleric buff");
		});
	}

	@Test
	public void testFlamewarper() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_flamewarper");
			int hp = opponent.getHero().getHp();
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 12);
			playCard(context, player, "spell_test_deal_6", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 18);
		});
	}

	@Test
	public void testWyrmrestAspirant() {
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_aspirant");
			int TEMPORARY_ATTACK_BONUS = 2;
			playMinionCard(context, player, "minion_test_buffs", wyrmrest);
			assertEquals(wyrmrest.getAttack(), wyrmrest.getBaseAttack() + 2 * TEMPORARY_ATTACK_BONUS);
			int ATTACK_BONUS = 4;
			playCard(context, player, "spell_test_cost_3_buff", wyrmrest);
			playCard(context, player, "spell_test_cost_3_buff", wyrmrest);
			playCard(context, player, "spell_test_cost_3_buff", wyrmrest);
			playCard(context, player, "spell_test_cost_3_buff", wyrmrest);
			assertEquals(wyrmrest.getAttack(), wyrmrest.getBaseAttack() + 2 * (TEMPORARY_ATTACK_BONUS + ATTACK_BONUS));
			context.getLogic().castSpell(player.getId(), DoubleAttackSpell.create(), player.getReference(), wyrmrest.getReference(), TargetSelection.NONE, true, null);
			// current attack: 4 base + 2 temporary + 4 attack bonus
			// doubling from blessed champion: 4 base + 2 temporary + 4 attack bonuses + 2 temporary + 4 attack bonuses + 4 base
			// to bonuses: 4 base + 16 bonuses
			// doubling bonuses: 4 base + 32 bonuses
			assertEquals(wyrmrest.getAttack(), 36);
			context.endTurn();
			// doubling from blessed champion: 4 base + 4 attack bonuses + 4 attack bonuses + 4 base
			// to bonuses: 4 base + 12 bonuses
			// doubling of bonuses: 4 base + 24 bonuses
			assertEquals(wyrmrest.getAttack(), 28);
		});
	}

	@Test
	public void testBlackflameRitual() {
		for (int i = 0; i < 7; i++) {
			final int count = i;
			runGym((context, player, opponent) -> {
				List<Minion> minions = new ArrayList<>();
				for (int j = 0; j < count; j++) {
					minions.add(playMinionCard(context, player, "minion_neutral_test_1"));
				}
				for (Minion minion : minions) {
					destroy(context, minion);
				}
				player.setMana(10);
				playCard(context, player, "spell_blackflame_ritual");
				assertEquals(player.getMana(), 10 - count);
				if (count == 0) {
					assertEquals(player.getMinions().size(), 0);
				} else {
					assertEquals(player.getMinions().size(), 2);
					for (int k = 0; k < 2; k++) {
						assertEquals(player.getMinions().get(k).getAttack(), count);
						assertEquals(player.getMinions().get(k).getHp(), count);
					}
				}
			});
		}
	}

	@Test
	public void testBlackflameRitualMadProphecyInteraction() {
		// Interaction with Mad Prophet Rosea should cast a 2x 10/10 minions
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_mad_prophet_rosea");
			player.setMaxMana(10);
			player.setMana(10);
			Card blackflameRitual = receiveCard(context, player, "spell_blackflame_ritual");
			playCard(context, player, blackflameRitual);
			for (int i = 2; i < 4; i++) {
				Minion token = player.getMinions().get(i);
				assertEquals(token.getAttack(), 10);
				assertEquals(token.getHp(), 10);
			}
		});
	}

	/*
	@Test
	public void testArcaneTyrantInvokeInteraction() {
		runGym((context, player, opponent) -> {
			Minion beast = playMinionCard(context, player, "minion_test_3_2");
			player.setMana(10);
			player.setMaxMana(10);
			// Petrifying Gaze is a cost 3 with an invoke of 9
			playCard(context, player, "spell_petrifying_gaze", beast);
			Card arcaneTyrant = receiveCard(context, player, "minion_arcane_tyrant");
			assertEquals(costOf(context, player, arcaneTyrant), 0, "Petrifying Gaze should have been played as a Cost-9 card.");
		});
	}

	 */

	@Test
	public void testElaborateScheme() {
		runGym((context, player, opponent) -> {
			String[] cardIds = {"secret_hoard_keeper", "secret_conjured_assistance", "secret_watchful_gaze"};
			Stream.of(cardIds).forEach(cardId -> shuffleToDeck(context, player, cardId));
			playCard(context, player, "secret_elaborate_scheme");
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getHand().size(), 2, "Should draw a card at start of the turn and due to Elaborate Scheme");
			assertEquals(player.getSecrets().size(), 1, "Should have triggered Elaborate Scheme and put a secret into play.");
			Set<String> secretsInHand = player.getHand().stream().map(Card::getCardId).collect(Collectors.toSet());
			assertFalse(secretsInHand.contains(player.getSecrets().get(0).getSourceCard().getCardId()));
			Set<String> remainingSecret = new HashSet<>(Arrays.asList(cardIds));
			remainingSecret.removeAll(secretsInHand);
			assertTrue(remainingSecret.contains(player.getSecrets().get(0).getSourceCard().getCardId()));
			assertEquals(remainingSecret.size(), 1);
		});
	}

	@Test
	public void testHeartstopAura() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			Minion defender = playMinionCard(context, player, "minion_neutral_test_1");
			Minion doubleDefender = playMinionCard(context, player, "minion_neutral_test_6_7");
			playCard(context, player, "spell_heartstop_aura");
			attack(context, player, attacker, target1);
			assertFalse(attacker.isDestroyed());
			assertTrue(target1.isDestroyed());
			context.endTurn();
			Minion opponentAttacker = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, opponentAttacker, defender);
			assertTrue(opponentAttacker.isDestroyed());
			assertFalse(defender.isDestroyed());
			// Deal 6 damage to 7 hp boulderfist
			playCard(context, opponent, "spell_test_deal_6", doubleDefender);
			assertEquals(doubleDefender.getHp(), 1);
			context.endTurn();
			assertTrue(attacker.isDestroyed());
			assertTrue(defender.isDestroyed());
			assertFalse(doubleDefender.isDestroyed(), "Boulderfist Ogre should not have taken enough damage to be killed.");
			attacker = playMinionCard(context, player, "minion_charge_test");
			attack(context, player, attacker, target2);
			assertTrue(attacker.isDestroyed(), "Hearstopped enchantment should have expired.");
		});

		runGym((context, player, opponent) -> {
			// TODO: Test playing Heartstopped two turns in a row
		});
	}

	@Test
	public void testFissure() {
		runGym((context, player, opponent) -> {
			Minion threeTwo = playMinionCard(context, opponent, "minion_test_3_2");
			Minion oneOneBuffed = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_test_cost_3_buff", oneOneBuffed);
			playCard(context, player, "spell_fissure");
			assertFalse(oneOneBuffed.isDestroyed());
			assertTrue(threeTwo.isDestroyed());
		});
	}

	@Test
	public void testHeavyDutyDragoons() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_heavy_duty_dragoon");
			playMinionCard(context, player, "minion_heavy_duty_dragoon");
			context.performAction(player.getId(), player.getHeroPowerZone().get(0).play().withTargetReference(player.getHero().getReference()));
			context.getLogic().endOfSequence();
			assertEquals(player.getHero().getAttack(), 3);
		}, "RUST", "RUST");
	}

	@Test
	public void testHeavyDutyDragoonChenStormstoutInteraction() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_heavy_duty_dragoon");
			useHeroPower(context, player, target.getReference());
			assertEquals(player.getHero().getAttack(), 2);
		}, "JADE", "JADE");
	}

	@Test
	public void testLadyDeathwhisper() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				Minion penguin = playMinionCard(context, opponent, "minion_neutral_test_1");
				for (int j = 0; j < i; j++) {
					playCard(context, opponent, "spell_apple", penguin);
				}
				assertEquals(penguin.getHp(), 1 + i);
			}
			context.endTurn();
			playMinionCard(context, player, "minion_lady_deathwhisper");
			assertTrue(opponent.getMinions().stream().allMatch(m -> m.getHp() == 1));
		});
	}

	@Test
	public void testGrimestreetVigilante() {
		runGym((context, player, opponent) -> {
			Minion vigilante = playMinionCard(context, player, "minion_grimestreet_vigilante");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			Card beast = receiveCard(context, player, "minion_test_3_2");
			attack(context, player, vigilante, target);
			assertEquals(beast.getAttributeValue(Attribute.ATTACK_BONUS), 4);
			Minion beastMinion = playMinionCard(context, player, beast);
			assertEquals(beastMinion.getAttack(), beastMinion.getBaseAttack() + 4);
		});
	}

	@Test
	public void testColdsteelBlade() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_coldsteel");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, player, player.getHero(), target);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_44dragon");
		});
	}

	@Test
	public void testMenacingDragotron() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2_elemental");
			Minion shouldBeDestroyed = playMinionCard(context, player, "minion_neutral_test_1");
			Minion shouldNotBeDestroyed = playMinionCard(context, player, 1, 4);
			playMinionCard(context, player, "minion_menacing_dragotron");
			assertTrue(shouldBeDestroyed.isDestroyed());
			assertFalse(shouldNotBeDestroyed.isDestroyed());
		});
	}

	@Test
	public void testBloodPresence() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(27);
			Minion charger = playMinionCard(context, player, "minion_charge_test");
			SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
			spell.put(SpellArg.CARD, "hero_power_blood_presence");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, TargetSelection.NONE, false, null);
			// Make sure aura actually gets recalculated
			context.getLogic().endOfSequence();
			assertEquals(player.getHero().getHeroPower().getCardId(), "hero_power_blood_presence");
			attack(context, player, charger, opponent.getHero());
			assertEquals(player.getHero().getHp(), 30);
			spell.put(SpellArg.CARD, "hero_power_1_1_weapon");
			context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, TargetSelection.NONE, false, null);
			// Make sure aura actually gets recalculated
			context.getLogic().endOfSequence();
			assertFalse(charger.hasAttribute(Attribute.AURA_LIFESTEAL));
			player.getHero().setHp(27);
			attack(context, player, charger, opponent.getHero());
			assertEquals(player.getHero().getHp(), 27);
		});
	}

	@Test
	public void testSilverboneClaw() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_silverbone_claw");
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			Card dragon = receiveCard(context, player, "token_44dragon");
			playCard(context, player, "weapon_silverbone_claw");
			assertEquals(dragon.getBonusAttack(), 2);
			assertEquals(dragon.getBonusHp(), 0);
		});

		runGym((context, player, opponent) -> {
			Card dragon = receiveCard(context, player, "token_44dragon");
			Card dragon2 = receiveCard(context, player, "token_44dragon");
			playCard(context, player, "weapon_silverbone_claw");
			assertEquals(dragon.getBonusAttack(), 2);
			assertEquals(dragon.getBonusHp(), 0);
			assertEquals(dragon2.getBonusAttack(), 2);
			assertEquals(dragon2.getBonusHp(), 0);
		});
	}

	@Test
	public void testSentryJumper() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_test_3_2");
			Minion source = playMinionCard(context, player, "minion_sentry_jumper", target);
			assertTrue(target.isDestroyed());
			assertEquals(source.getHp(), source.getBaseHp() - target.getAttack());
		});
	}

	@Test
	public void testFortunaHunter() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_fortuna_hunter");
			Minion buffed = playMinionCard(context, player, "minion_rapier_rodent");
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() + 1);
			assertEquals(buffed.getHp(), buffed.getBaseHp() + 1);
			Minion notBuffed = playMinionCard(context, player, "minion_test_3_2");
			assertEquals(notBuffed.getAttack(), notBuffed.getBaseAttack());
			assertEquals(notBuffed.getHp(), notBuffed.getBaseHp());
			player.setMana(6);
			playMinionCard(context, player, "minion_baby_gryphon");
			// Get the right card
			buffed = player.getMinions().get(player.getMinions().size() - 2);
			// Should have invoked
			assertEquals(buffed.getSourceCard().getCardId(), "minion_baby_gryphon");
			assertTrue(buffed.getSourceCard().hasAttribute(Attribute.INVOKED));
			assertEquals(buffed.getAttack(), buffed.getBaseAttack() + 1);
			assertEquals(buffed.getHp(), buffed.getBaseHp() + 1);
		});
	}

	@Test
	public void testGrandArtificerPipiAndWaxGolem() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_grand_artificer_pipi");
			playCard(context, player, "spell_test_deal_5_to_enemy_hero");
			Minion waxGolem = player.getMinions().get(1);
			assertEquals(waxGolem.getSourceCard().getCardId(), "token_wax_golem");
			assertEquals(opponent.getHero().getHp(), 30 - 5, "Should have been hit by Mind Blast #1");
			destroy(context, waxGolem);
			assertEquals(opponent.getHero().getHp(), 30 - 10, "Should have been hit by Mind Blast #2");
		});
	}

	@Test
	public void testOzumatOfTheDepths() {
		runGym((context, player, opponent) -> {
			destroy(context, playMinionCard(context, player, "token_ozumat_of_the_depths"));
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().get(0).getSourceCard().getCardId(), "token_ozumat's_nightmare");
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			playCard(context, opponent, "spell_test_deal_6", player.getHero());
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_nightmare_tentacle");
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
			assertEquals(player.getSecrets().size(), 1);
			context.endTurn();
			assertEquals(player.getSecrets().size(), 1);
		});
	}

	@Test
	public void testStudy() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_study");
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2");
		});
	}

	@Test
	public void testPanickedSummoning() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_test_3_2");
			receiveCard(context, player, "minion_neutral_test_big");
			playCard(context, player, "secret_panicked_summoning");
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, charger, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "secret_panicked_summoning");
			context.endTurn();
			attack(context, opponent, charger, player.getHero());
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_test_3_2");
		});
	}

	@Test
	public void testLanternCarrier() {
		runGym((context, player, opponent) -> {
			Minion lanternCarrier = playMinionCard(context, player, "minion_lantern_carrier");
			assertEquals(lanternCarrier.getAttack(), lanternCarrier.getBaseAttack());
			assertEquals(lanternCarrier.getHp(), lanternCarrier.getBaseHp());
			Minion beast = playMinionCard(context, player, "minion_test_3_2");
			assertEquals(beast.getAttack(), beast.getBaseAttack() + 1);
			assertEquals(beast.getHp(), beast.getBaseHp() + 1);
		});
	}

	@Test
	public void testSignsOfTheEnd() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_signs_of_the_end");
			assertEquals(player.getMinions().size(), 0);
			playCard(context, player, "spell_test_gain_mana");
			assertEquals(player.getMinions().get(0).getSourceCard().getBaseManaCost(), 0);
		});

		/* Sometimes the Earthquake kills the 7 cost card
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_signs_of_the_end");
			player.setMana(7);
			playCard(context, player, "spell_earthquake");
			assertTrue(player.getMinions().stream().anyMatch(m -> m.getSourceCard().getBaseManaCost() == 7));
		});
		*/
	}

	@Test
	public void testSouldrinkerDrake() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_souldrinker_drake");
			Card fireball = receiveCard(context, player, "spell_test_deal_6");
			Card fireball2 = receiveCard(context, player, "spell_test_deal_6");
			player.getHero().setHp(1);
			playCard(context, player, fireball, opponent.getHero());
			assertEquals(player.getHero().getHp(), 1 + 6);
			playCard(context, player, fireball2, opponent.getHero());
			assertEquals(player.getHero().getHp(), 1 + 6, "Lifesteal should not have been applied");
		});

		runGym((context, player, opponent) -> {
			// Souldrinker Drake should give Watchful Gaze, a secret, lifesteal
			playMinionCard(context, player, "minion_souldrinker_drake");
			playCard(context, player, "secret_watchful_gaze");
			context.endTurn();
			player.getHero().setHp(1);
			playCard(context, opponent, "minion_test_3_2");
			assertEquals(player.getSecrets().size(), 0);
			assertEquals(player.getHero().getHp(), 1 + 8);
		});
	}

	@Test
	public void testSkuggTheUnclean() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_skugg_the_unclean");
			player.setMana(3);
			playCard(context, player, "minion_hoardling_of_tolin");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_skugg_rat");
		});
	}

	@Test
	public void testMagmaSpewer() {
		runGym((context, player, opponent) -> {
			// 2 mana total
			playCard(context, player, "token_ember_elemental");
			playCard(context, player, "spell_test_destroy_all");
			context.endTurn();
			context.endTurn();
			// 4 mana total
			playCard(context, player, "token_ember_elemental");
			playCard(context, player, "token_ember_elemental");
			context.endTurn();
			context.endTurn();
			// 8 mana total
			playCard(context, player, "token_ember_elemental");
			playCard(context, player, "token_ember_elemental");
			playCard(context, player, "token_ember_elemental");
			playCard(context, player, "token_ember_elemental");
			int opponentHp = opponent.getHero().getHp();
			playMinionCard(context, player, "minion_magma_spewer", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponentHp - 4);
		});
	}

	@Test
	public void testMadProphetRosea() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_mad_prophet_rosea");
			player.setMana(9);
			Card theCoin = receiveCard(context, player, "spell_test_gain_mana");
			assertEquals(costOf(context, player, theCoin), 0);
			playCard(context, player, theCoin);
			assertEquals(player.getMana(), 10);
			assertEquals(player.getMinions().size(), 1, "Only Mad Prophet Rosea");
			theCoin = receiveCard(context, player, "spell_test_gain_mana");
			assertEquals(costOf(context, player, theCoin), 0, "Aura Invoke is gone!");
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_mad_prophet_rosea");
			player.setMana(10);
			Card theCoin = receiveCard(context, player, "spell_test_gain_mana");
			assertEquals(costOf(context, player, theCoin), 10, "Aura Invoke applies");
			playCard(context, player, theCoin);
			assertEquals(player.getMana(), 1, "The coin gained 1 mana");
			assertEquals(player.getMinions().size(), 2, "Two: Mad Prophet Rosea & Yoth'al");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_yoth'al_the_devourer");
		});
	}

	@Test
	public void testPathOfFrost() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test_1");
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_path_of_frost", attacker);
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			attack(context, opponent, attacker, target);
			assertEquals(opponent.getHero().getHp(), opponentHp - attacker.getAttack());
			assertEquals(target.getHp(), target.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			playCard(context, player, "spell_path_of_frost", attacker);
			int opponentHp = opponent.getHero().getHp();
			attack(context, player, attacker, target);
			assertEquals(opponent.getHero().getHp(), opponentHp - attacker.getAttack());
			assertEquals(target.getHp(), target.getBaseHp());
		});
	}

	@Test
	public void testRafaamPhilanthropist() {
		runGym((context, player, opponent) -> {
			overrideDiscover(context, player, "minion_test_3_2");
			playMinionCard(context, player, "minion_rafaam_philanthropist");
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_test_3_2");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testMiniKnight() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				receiveCard(context, player, "minion_test_3_2");
			}
			Minion knight = playMinionCard(context, player, "minion_mini_knight");
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack());
			receiveCard(context, player, "minion_test_3_2");
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 1);
			receiveCard(context, opponent, "minion_test_3_2");
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 1, "Opponent card should not buff mini knight");
			context.getLogic().removeCard(player.getHand().get(0));
			context.getLogic().endOfSequence();
			assertEquals(knight.getAttack(), knight.getBaseAttack() + 1);
		});
	}

	@Test
	public void testScissorsofDots() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_scissors_of_dots");
			assertTrue(player.getHero().canAttackThisTurn(), "Should be able to attack now");
			attack(context, player, player.getHero(), opponent.getHero());
			assertTrue(player.getHero().canAttackThisTurn(), "Should be able to attack still (Windfury weapon)");
			attack(context, player, player.getHero(), opponent.getHero());
			assertFalse(player.getHero().canAttackThisTurn(), "Should NOT be able to attack still (Windfury weapon)");
		});
	}

	@Test
	public void testEnergeticMentee() {
		runGym((context, player, opponent) -> {
			player.setMana(2);
			playMinionCard(context, player, "minion_energetic_mentee");
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_deathwhelp");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_deathwhelp");
			assertFalse(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
			assertFalse(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
		});

		runGym((context, player, opponent) -> {
			player.setMana(3);
			playMinionCard(context, player, "minion_energetic_mentee");
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_deathwhelp");
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "token_deathwhelp");
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.CHARGE));
			assertTrue(player.getMinions().get(2).hasAttribute(Attribute.CHARGE));
		});
	}

	@Test
	public void testEvilCounterpart() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();

			// Does not trigger invoke
			player.setMana(7);
			Card card = receiveCard(context, player, "spell_evil_counterpart");
			assertEquals(costOf(context, player, card), 4);
			playCard(context, player, card, target);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMana(), 7 - 4);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();

			// Does trigger invoke
			player.setMana(8);
			Card card = receiveCard(context, player, "spell_evil_counterpart");
			assertEquals(costOf(context, player, card), 8);
			playCard(context, player, card, target);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMana(), 0);
		});

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();

			// Does not trigger invoke
			player.setMana(5);
			playCard(context, player, "spell_test_discount_spell");
			Card card = receiveCard(context, player, "spell_evil_counterpart");
			assertEquals(costOf(context, player, card), 1, "Discounted by Preparation");
			playCard(context, player, card, target);
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMana(), 5 - 4 + 3);
		});
	}

	@Test
	public void testHaplessKnight() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion greaterAttack = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			Minion lessAttack = playMinionCard(context, opponent, "minion_test_dodge");
			context.endTurn();
			Minion source = playMinionCard(context, player, "minion_hapless_knight");
			// Just opponent's hero and Argent Squire
			assertEquals(getPhysicalAttackActionStream(context).count(), 2L);
			assertTrue(getPhysicalAttackActionStream(context).noneMatch(ga -> ga.getTargetReference().equals(greaterAttack.getReference())));
			assertEquals(getPhysicalAttackActionStream(context).filter(ga -> ga.getTargetReference().equals(lessAttack.getReference())).count(), 1L);
		});
	}

	@NotNull
	public Stream<PhysicalAttackAction> getPhysicalAttackActionStream(GameContext context) {
		return context.getValidActions().stream().filter(ga -> ga instanceof PhysicalAttackAction)
				.map(ga -> (PhysicalAttackAction) ga);
	}

	@Test
	public void testVoidReaper() {
		runGym((context, player, opponent) -> {
			for (Player p : new Player[]{player, opponent}) {
				shuffleToDeck(context, p, "minion_test_3_2");
				shuffleToDeck(context, p, "minion_test_dodge");
				shuffleToDeck(context, p, "minion_test_dodge");
				receiveCard(context, p, "minion_test_3_2");
				receiveCard(context, p, "minion_test_dodge");
				receiveCard(context, p, "minion_test_dodge");
				playMinionCard(context, p, "minion_test_3_2");
				playMinionCard(context, p, "minion_test_dodge");
				playMinionCard(context, p, "minion_test_dodge");
			}

			// Removing beast should leave two of everything on the board
			playMinionCard(context, player, "minion_void_reaper",
					player.getMinions().get(0));

			assertEquals(player.getMinions().size(), 2 + 1, "2 argents + void reaper");
			for (Player p : new Player[]{player, opponent}) {
				assertEquals(p.getHand().size(), 2);
				assertEquals(p.getDeck().size(), 2);
			}
			assertEquals(opponent.getMinions().size(), 2);
		});
	}

	@Test
	public void testForcesOfGilneas() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "spell_summon_for_opponent");
			}
			assertEquals(player.getHand().get(0).getCardId(), "minion_king_archibald");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_test_3_2");
			}
			assertEquals(player.getQuests().size(), 1);
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				playCard(context, opponent, "minion_test_3_2");
			}
			assertEquals(player.getQuests().size(), 1);
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_forces_of_gilneas");
			context.endTurn();
			for (int i = 0; i < 5; i++) {
				playCard(context, opponent, "spell_summon_for_opponent");
			}
			assertEquals(player.getQuests().size(), 1);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testSuspiciousWanderer() {
		runGym((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_suspicious_wanderer");
			Minion minion1 = playMinionCard(context, player, card);
			Minion minion2 = playMinionCard(context, player, player.getHand().get(0));
			context.endTurn();
			assertEquals(player.getHand().size(), 0);
			assertEquals(minion1.getZone(), Zones.BATTLEFIELD);
			assertEquals(minion2.getZone(), Zones.BATTLEFIELD);
		});
	}

	@Test
	public void testSlamhammerKnight() {
		// Test divine shield
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_slamhammer_knight");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_dodge");
			Minion bigMinion = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			context.endTurn();
			attack(context, player, attacker, target);
			assertEquals(target.getHp(), 1);
			assertEquals(bigMinion.getHp(), bigMinion.getBaseHp() - attacker.getAttack());
			assertEquals(attacker.getHp(), attacker.getBaseHp() - target.getAttack());
		});

		// Confirm that minions go into negative HP
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_slamhammer_knight");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			Minion bigMinion = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			context.endTurn();
			attack(context, player, attacker, target);
			assertTrue(target.isDestroyed());
			assertEquals(bigMinion.getHp(), bigMinion.getBaseHp() - attacker.getAttack() + 2);
			assertEquals(attacker.getHp(), attacker.getBaseHp() - target.getAttack());
		});

		// If there's no excess damage, no damage
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_slamhammer_knight");
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			Minion bigMinion = playMinionCard(context, opponent, "minion_neutral_test_6_7");
			context.endTurn();
			attack(context, player, attacker, target);
			assertFalse(target.isDestroyed());
			assertEquals(bigMinion.getHp(), bigMinion.getBaseHp());
			assertEquals(attacker.getHp(), attacker.getBaseHp() - target.getAttack());
		});
	}

	@Test
	public void testMistyManaTea() {
		// Test basic
		runGym((context, player, opponent) -> {
			player.setMana(8);
			for (int i = 0; i < 8; i++) {
				playCard(context, player, "spell_test_deal_1", opponent.getHero());
			}
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 4);
		});

		// Test basic
		runGym((context, player, opponent) -> {
			player.setMana(7);
			for (int i = 0; i < 7; i++) {
				playCard(context, player, "spell_test_deal_1", opponent.getHero());
			}
			assertEquals(player.getMana(), 0);
			assertEquals(player.getAttributeValue(Attribute.MANA_SPENT_THIS_TURN), 7);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 0);
		});

		// Test spend all your mana effects
		runGym((context, player, opponent) -> {
			player.setMana(8);
			playCard(context, player, "spell_spend_all_your_mana");
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 4);
		});

		// Test resets at end of turn
		runGym((context, player, opponent) -> {
			player.setMana(7);
			playCard(context, player, "spell_spend_all_your_mana");
			assertEquals(player.getMana(), 0);
			context.endTurn();
			context.endTurn();
			player.setMana(1);
			playCard(context, player, "spell_spend_all_your_mana");
			assertEquals(player.getMana(), 0);
			playCard(context, player, "spell_misty_mana_tea");
			assertEquals(player.getMana(), 0);
		});
	}

	@Test
	public void testTouchOfKarma() {
		// Target friendly
		runGym((context, player, opponent) -> {
			Minion attacker = playMinionCard(context, player, "minion_charge_test");
			playCard(context, player, "spell_touch_of_karma", attacker);
			int hp = opponent.getHero().getHp();
			attack(context, player, attacker, opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - attacker.getAttack() - 2);
		});

		// Target opponent
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_charge_test");
			context.endTurn();
			playCard(context, player, "spell_touch_of_karma", attacker);
			context.endTurn();
			int hp = opponent.getHero().getHp();
			attack(context, opponent, attacker, player.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 2);
		});
	}

	@Test
	public void testMarkOfDespair() {
		// Test regular
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion target2 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_mark_of_despair", target2);
			playCard(context, player, "spell_test_deal_1", target1);
			assertFalse(target1.isDestroyed());
			assertFalse(target2.isDestroyed());
			playCard(context, player, "spell_test_deal_1", target2);
			assertFalse(target1.isDestroyed());
			assertTrue(target2.isDestroyed());
		});

		// Test AoE
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion target2 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_mark_of_despair", target2);
			playCard(context, player, "spell_test_deal_1_to_enemies");
			assertFalse(target1.isDestroyed());
			assertTrue(target2.isDestroyed());
			context.endTurn();
			Minion target3 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_test_deal_1_to_enemies");
			assertFalse(target3.isDestroyed());
		});

		// Test hero attacking
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion target2 = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_mark_of_despair", target2);
			playCard(context, player, "weapon_stick");

			attack(context, player, player.getHero(), target2);
			assertTrue(target2.isDestroyed());
			assertFalse(target1.isDestroyed());
		});
	}

	@Test
	public void testSoothingMists() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(1);
			Minion twoHp = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_soothing_mists", twoHp);
			assertEquals(player.getHero().getHp(), 4);
		});
	}

	@Test
	public void testSteadfastDefense() {
		runGym((context, player, opponent) -> {
			// Your minions can only take 1 damage at a time until the start of your next turn.
			Minion target = playMinionCard(context, player, "minion_neutral_test_big");
			playCard(context, player, "spell_steadfast_defense");
			playCard(context, player, "spell_test_deal_6", target);
			assertEquals(target.getHp(), target.getBaseHp() - 1);
			playCard(context, player, "spell_test_deal_6", target);
			assertEquals(target.getHp(), target.getBaseHp() - 2);
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_charge_test");
			attack(context, opponent, attacker, target);
			assertEquals(target.getHp(), target.getBaseHp() - 3);
		});
	}

	@Test
	@Disabled("too many changes to test")
	public void testANewChallenger() {
		runGym((context, player, opponent) -> {
			overrideRandomCard(context, "hero_nefarian");
			playCard(context, player, "spell_a_new_challenger");
			assertEquals(player.getHero().getSourceCard().getCardId(), "hero_nefarian");
			final String[] nefarianCards = (String[]) CardCatalogue.getCardById("hero_nefarian").getDesc()
					.getBattlecry().getSpell().subSpells(0).get(1).get(SpellArg.CARDS);
			final int drawnCards = (int) CardCatalogue.getCardById("hero_nefarian").getDesc()
					.getBattlecry().getSpell().subSpells(0).get(2).get(SpellArg.VALUE);
			// Draws a card
			assertEquals(player.getDeck().size(), nefarianCards.length - drawnCards);
		});
	}

	@Test
	public void testPrinceTenris() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_prince_tenris");
			assertEquals(player.getHero().getAttack(), 1);
			context.endTurn();
			assertEquals(player.getHero().getAttack(), 0);
			context.endTurn();
			assertEquals(player.getHero().getAttack(), 1);
		});
	}

	@Test
	public void testDeepBorer() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2");
			receiveCard(context, player, "minion_deep_borer");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_deep_borer");
		});

		// Should not produce infinite loop
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_deep_borer");
			receiveCard(context, player, "minion_deep_borer");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "minion_deep_borer");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_deep_borer");
		});
	}

	@Test
	public void testTheEndTime() {
		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 40);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 39);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 38);
			context.endTurn();
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 35, "The infinite wardens in play reduce this by 2 each.");
		});

		runGym((context, player, opponent) -> {
			Minion endTime = playMinionCard(context, player, "permanent_the_end_time");
			assertEquals(endTime.getAttributeValue(Attribute.RESERVED_INTEGER_1), 40);
			endTime.setAttribute(Attribute.RESERVED_INTEGER_1, 1);
			context.endTurn();
			assertEquals(context.getStatus(), GameStatus.RUNNING);
			assertFalse(opponent.getHero().isDestroyed());
		});
	}

	@Test
	public void testHypnotist() {
		runGym((context, player, opponent) -> {
			Minion giant = playMinionCard(context, player, "minion_crystal_giant");
			playMinionCard(context, player, "minion_hypnotist", giant);
			assertEquals(giant.getAttack(), giant.getSourceCard().getBaseManaCost());
			assertEquals(giant.getHp(), giant.getSourceCard().getBaseManaCost());
		});
	}

	@Test
	public void testFifiFizzlewarpPermanentsInteraction() {
		// Should not choose a permanent to write on a card
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("permanent_test"));
			putOnTopOfDeck(context, player, "minion_neutral_test_6_7");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			context.getLogic().drawCard(player.getId(), player);
			context.getLogic().discardCard(player, fifi);

			Card shouldNotBePermanent = player.getHand().get(0);
			assertFalse(shouldNotBePermanent.hasAttribute(Attribute.PERMANENT));
		});
	}

	@Test
	public void testFifiFizzlewarp() {
		// Test that cards that have race-filtered battlecries work correctly after Fifi Fizzlewarp
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_neutral_test");

			for (int i = 0; i < 2; i++) {
				putOnTopOfDeck(context, player, "minion_test_3_2_fae");
			}

			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_test_race_filtered_opener");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();

			context.getLogic().discardCard(player, fifi);

			for (int i = 0; i < 3; i++) {
				context.getLogic().drawCard(player.getId(), player);
			}

			for (Card card : player.getHand().subList(0, 2)) {
				assertEquals(card.getCardId(), "minion_test_race_filtered_opener");
				assertEquals(card.getRace(), Race.FAE);
			}

			final Card blank = player.getHand().get(2);
			assertEquals(blank.getCardId(), "minion_test_race_filtered_opener");
			assertEquals(blank.getRace(), Race.NONE);

			final Card opener2 = player.getHand().get(0);
			final Card vermin2 = player.getHand().get(1);

			Minion target = playMinionCard(context, player, opener2);
			Minion notTarget = playMinionCard(context, player, blank);

			CountDownLatch latch = new CountDownLatch(1);
			// Checks that the race filtered opener can target the true FAE on the board and not the Race.NONE
			// race-filtered opener that was created by rewriting the minion_neutral_test
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 1);
				assertEquals(battlecryActions.get(0).getTargetReference(), target.getReference());
				latch.countDown();
				return battlecryActions.get(0);
			});

			playCard(context, player, vermin2);
			assertEquals(latch.getCount(), 0, "Should have requested battlecries");
		});

		// Getting Divine Shield minions from Fifi Fizzlewarp should work
		runGym((context, player, opponent) -> {
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_rapier_rodent");
			OverrideHandle<Card> handle = overrideRandomCard(context, "minion_test_dodge");
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			handle.stop();
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (Card) shouldBeDrawn.transformResolved(context);
			Minion argentSquire = playMinionCard(context, player, shouldBeDrawn);
			assertTrue(argentSquire.hasAttribute(Attribute.DIVINE_SHIELD));
		});

		// Card cost modifiers like Timebidding Magi's should work
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_cost_modifier_zero"));
			Card fifi = receiveCard(context, player, "minion_fifi_fizzlewarp");
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "minion_mechanical_monstrosity");
			int baseCost = shouldBeDrawn.getBaseManaCost();
			assertTrue(baseCost > 4);
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			context.getLogic().discardCard(player, fifi);
			Card drawnCard = context.getLogic().drawCard(player.getId(), player);
			assertEquals(drawnCard, shouldBeDrawn.transformResolved(context));
			shouldBeDrawn = (Card) shouldBeDrawn.transformResolved(context);
			assertEquals(0, costOf(context, player, shouldBeDrawn));
		});
	}

	@Test
	public void testParadox() {
		runGym((context, player, opponent) -> {
			Minion paradox = playMinionCard(context, player, "minion_paradox");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_paradox");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_test_gain_mana");
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getHand().get(0).getCardId(), "minion_paradox");
		});
	}

	@Test
	public void testChromie() {
		// Test no excess cards
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				shuffleToDeck(context, player, "minion_test_3_2");
				receiveCard(context, player, "spell_test_summon_tokens");
			}

			playCard(context, player, "minion_chromie");

			assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_test_3_2")));
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_test_summon_tokens")));
			assertEquals(player.getHand().size(), 6);
			assertEquals(player.getDeck().size(), 6);
		});

		// Test excess cards
		runGym((context, player, opponent) -> {
			player.getGraveyard().clear();

			for (int i = 0; i < 20; i++) {
				shuffleToDeck(context, player, "minion_test_3_2");
			}

			for (int i = 0; i < 6; i++) {
				receiveCard(context, player, "spell_test_summon_tokens");
			}

			playCard(context, player, "minion_chromie");

			assertTrue(player.getHand().stream().allMatch(c -> c.getCardId().equals("minion_test_3_2")));
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals("spell_test_summon_tokens")));
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 6);
		});
	}

	@Test
	public void testHighmountainPrimalist() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 29; i++) {
				shuffleToDeck(context, player, "minion_test_3_2");
			}
			overrideDiscover(context, player, "spell_test_summon_tokens");
			playCard(context, player, "minion_highmountain_primalist");
			playCard(context, player, "minion_test_draw");
			assertEquals(player.getHand().get(0).getCardId(), "spell_test_summon_tokens");
		});
	}

	@Test
	public void testDimensionalCourier() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2");
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "minion_dimensional_courier");
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2");
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "minion_dimensional_courier");
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testHandsOnHistorian() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2");
			for (int i = 0; i < 3; i++) {
				receiveCard(context, player, "minion_test_3_2");
			}
			int size = player.getHand().size();
			playCard(context, player, "minion_hands_on_historian");
			assertEquals(player.getHand().size(), size);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_3_2");
			for (int i = 0; i < 2; i++) {
				receiveCard(context, player, "minion_test_3_2");
			}
			int size = player.getHand().size();
			playCard(context, player, "minion_hands_on_historian");
			assertEquals(player.getHand().size(), size + 1);
		});
	}

	/*
	@Test
	public void testBlackLotus() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			playCard(context, player, "spell_test_deal_1", opponent.getHero());
			Assert.assertFalse(opponent.getHero().isDestroyed());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_black_lotus");
			context.endTurn();
			Minion beast = playMinionCard(context, opponent, "minion_test_3_2");
			context.endTurn();
			playCard(context, player, "spell_test_deal_1", beast);
			assertTrue(beast.isDestroyed());
		});
	}

	 */


	/*
	@Test
	public void testArmaggedonVanguardBolfRamshieldInteraction() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_armageddon_vanguard");
			playCard(context, player, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, opponent, "minion_armageddon_vanguard");
			playCard(context, opponent, "minion_bolf_ramshield");
			context.endTurn();
			playCard(context, player, "spell_arcane_missiles");
		});
	}
	*/


	@Test
	public void testMysticSkull() {
		runGym((context, player, opponent) -> {
			Minion beasttest32 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_mystic_skull", beasttest32);
			assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2");
			Minion newbeasttest32 = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(newbeasttest32.getAttack(), 5);
		});
	}

	@Test
	public void testGiantDisappointment() {
		runGym((context, player, opponent) -> {
			Card card = CardCatalogue.getCardById("minion_giant_disappointment");
			context.getLogic().receiveCard(player.getId(), card);
			assertEquals(costOf(context, player, card), 8);
		});
	}

	@Test
	public void testQuestGiver() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2");
			playMinionCard(context, player, "minion_quest_giver");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_test_3_2");
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_quest_giver");
			context.endTurn();
			context.endTurn();
			Minion newbeast = playMinionCard(context, player, player.getHand().get(0));
			assertEquals(newbeast.getAttack(), 6);
			assertEquals(newbeast.getHp(), 5);
		});
	}

	@Test
	public void testDragonlingPet() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_dragonling_pet");
			for (int i = 0; i < 28; i++) {
				shuffleToDeck(context, player, "minion_dragon_test");
			}
			shuffleToDeck(context, player, "minion_neutral_test");
			assertEquals(player.getDeck().size(), 30);
			assertEquals(player.getHand().size(), 0);
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(player.getDeck().size(), 30);
			assertEquals(player.getHand().size(), 0);
		});

		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_dragonling_pet");
			for (int i = 0; i < 29; i++) {
				shuffleToDeck(context, player, "minion_dragon_test");
			}
			assertEquals(player.getDeck().size(), 30);
			assertEquals(player.getHand().size(), 0);
			context.fireGameEvent(new GameStartEvent(context, player.getId()));
			assertEquals(player.getDeck().size(), 29);
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testWyrmrestSniper() {
		// Friendly Dragon survives damage so 3 damage is dealt to the opponent hero
		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, player, "minion_neutral_test_big");
			dragon.setRace("DRAGON");
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_test_deal_1_to_enemies");
			assertEquals(opponent.getHero().getHp(), opponentHp - 3);
			assertFalse(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Friendly Dragon does not survive damage, no damage is dealt
		runGym((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_neutral_test_big");
			minion.setRace("DRAGON");
			// Set hp to 1 so it dies
			minion.setHp(1);
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			// Damages minions by 1
			playCard(context, opponent, "spell_test_deal_1_to_enemies");
			assertTrue(minion.isDestroyed());
			assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});

		// Enemy Dragon survives damage, no damage is dealt to the opponent's hero
		runGym((context, player, opponent) -> {
			Minion wyrmrest = playMinionCard(context, player, "minion_wyrmrest_sniper");
			context.endTurn();
			int opponentHp = opponent.getHero().getHp();
			Minion minion = playMinionCard(context, opponent, "minion_neutral_test_big");
			minion.setRace("DRAGON");

			// Damages minions by 1
			context.endTurn();
			playCard(context, player, "spell_test_deal_1_to_enemies");
			assertFalse(minion.isDestroyed());
			assertEquals(minion.getHp(), minion.getBaseHp() - 1);
			assertEquals(opponent.getHero().getHp(), opponentHp, "Opponent's HP should not have changed.");
			assertTrue(wyrmrest.hasAttribute(Attribute.STEALTH));
		});
	}

	@Test
	public void testAegwynn() {
		Map<String, Integer> cardMap = new HashMap<>();
		cardMap.put("minion_neutral_test", 2);
		cardMap.put("spell_test_deal_6", 0);
		cardMap.put("spell_test_deal_10", 0);
		for (String spell : cardMap.keySet()) {
			runGym((context, player, opponent) -> {
				shuffleToDeck(context, player, spell);
				shuffleToDeck(context, player, "minion_aegwynn");
				context.fireGameEvent(new GameStartEvent(context, player.getId()));
				assertEquals(player.hasAttribute(Attribute.SPELL_DAMAGE) ? player.getAttribute(Attribute.SPELL_DAMAGE) : 0, cardMap.get(spell));
			});
		}
	}

	@Test
	public void testLittleHelper() {
		CardList heroPowers = CardCatalogue.getAll().filtered(Card::isHeroPower);
		for (Card heroPower : heroPowers) {
			runGym((context, player, opponent) -> {
				SpellDesc spell = new SpellDesc(ChangeHeroPowerSpell.class);
				spell.put(SpellArg.CARD, heroPower.getCardId());
				context.getLogic().castSpell(player.getId(), spell, player.getReference(), null, TargetSelection.NONE, false, null);

				playCard(context, player, "minion_little_helper");
				Card card = player.getHand().get(0);
				//System.out.println(heroPower.getCardId() + " " + card.getBaseManaCost() + " " + card.getDescription());

			});
		}
	}

	@Test
	public void testTeronGorefiend() {
		runGym((context, player, opponent) -> {
			Minion gorefiend = playMinionCard(context, player, "minion_teron_gorefiend");
			receiveCard(context, player, "minion_test_deathrattle_2");
			shuffleToDeck(context, player, "minion_test_deathrattle_2");
			context.fireGameEvent(new WillEndSequenceEvent(context));
			assertTrue(gorefiend.hasAttribute(Attribute.DEATHRATTLES));
			playCard(context, player, "spell_test_destroy_all");
			assertEquals(player.getMinions().size(), 2);
		});
	}

	@Test
	public void testMidnightRide() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_neutral_test_1");
			playCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_midnight_ride");
			assertEquals(player.getMinions().size(), 0);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getDeck().size(), 2);

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(player.getDeck().size(), 1);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getMinions().size(), 1);

			context.getLogic().drawCard(player.getId(), null);
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHand().size(), 0);
			assertEquals(player.getMinions().size(), 2);


			assertEquals(opponent.getDeck().size(), 1);
			assertEquals(opponent.getHand().size(), 0);
			assertEquals(opponent.getMinions().size(), 0);
			context.getLogic().drawCard(opponent.getId(), null);
			assertEquals(opponent.getDeck().size(), 0);
			assertEquals(opponent.getHand().size(), 0);
			assertEquals(opponent.getMinions().size(), 1);
		});
	}

	@Test
	public void testIceCap() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}

			playCard(context, player, "spell_icecap");
			playCard(context, player, "spell_test_draw_cards");
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 27);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 27);
			playCard(context, player, "spell_test_draw_cards");
			assertEquals(player.getHand().size(), 3);
			assertEquals(player.getDeck().size(), 27);
		});
	}

	@Test
	public void testMealcatcher() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_dodge");
			shuffleToDeck(context, opponent, "minion_neutral_test_big");
			Minion mealcatcher = playMinionCard(context, player, "minion_mealcatcher");
			assertEquals(mealcatcher.getAttack(), mealcatcher.getBaseAttack() + 1);
			assertEquals(mealcatcher.getHp(), mealcatcher.getBaseHp() + 1);
			assertTrue(mealcatcher.hasAttribute(Attribute.DIVINE_SHIELD));
			assertEquals(player.getDeck().size(), 0);
		});
	}

	@Test
	public void testPuzzleBox() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 4; i++) {
				shuffleToDeck(context, player, "minion_neutral_test_1");
			}
			playCard(context, player, "permanent_puzzle_box");
			assertEquals(player.getDeck().size(), 0);
			for (int i = 0; i < 4; i++) {
				assertEquals(player.getHand().size(), 0);
				context.endTurn();
			}
			assertEquals(player.getHand().size(), 4);
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().size(), 4);
		});
	}

	@Test
	public void testDerangedShifter() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_test_deflect");
			Minion shifter = playMinionCard(context, player, "minion_deranged_shifter");
			assertTrue(shifter.hasAttribute(Attribute.DEFLECT));
		});
	}

	@Test
	public void testContinuity() {
		runGym((context, player, opponent) -> {
			Card flame1 = receiveCard(context, player, "minion_neutral_test_1");
			Card flame2 = shuffleToDeck(context, player, "minion_neutral_test_1");
			Minion flame3 = playMinionCard(context, player, "minion_neutral_test_1");
			Minion flame4 = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_continuity", flame3);
			assertEquals(flame4.getAttack(), 3);
			assertEquals(flame3.getAttack(), 3);
			assertEquals(flame2.getBaseAttack() + flame2.getBonusAttack(), 3);
			assertEquals(flame1.getBaseAttack() + flame1.getBonusAttack(), 3);
		});
	}


	/*
	@Test
	public void testEchoLocate() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test_1");
			shuffleToDeck(context, player, "minion_neutral_test_1");
			shuffleToDeck(context, player, "minion_neutral_test_1");
			playCardWithTarget(context, player, "spell_forgotten_torch", opponent.getHero());
			playCardWithTarget(context, player, "spell_echolocate", opponent.getHero());

		});
	}
	*/

	@Test
	public void testIntoTheMines() {
		runGym((context, player, opponent) -> {
			Card intoTheMines = receiveCard(context, player, "quest_into_the_mines");
			assertTrue(intoTheMines.isQuest());
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_into_the_mines");
			for (int i = 0; i < 9; i++) {
				context.getLogic().castSpell(player.getId(), AddAttributeSpell.create(Attribute.FROZEN), player.getReference(), opponent.getHero().getReference(), TargetSelection.NONE, true, null);
			}
			assertEquals(player.getHand().size(), 1);

		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_into_the_mines");
			playCard(context, player, "spell_test_deal_10", player.getHero());
			for (int i = 0; i < 10; i++) {
				context.getLogic().castSpell(player.getId(), HealSpell.create(1), player.getReference(), player.getHero().getReference(), TargetSelection.NONE, true, null);
			}
			assertEquals(player.getHand().size(), 1);

		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "quest_into_the_mines");
			Minion target = playMinionCard(context, opponent, "minion_neutral_test_1");
			for (int i = 0; i < 9; i++) {
				context.getLogic().castSpell(player.getId(), SilenceSpell.create(), player.getReference(), target.getReference(), TargetSelection.NONE, true, null);
			}
			assertEquals(player.getHand().size(), 1);
		});
	}

	@Test
	public void testAkarador() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_neutral_test");
			playCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_test_destroy_all");
			playCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_test_destroy_all");
			playCard(context, player, "minion_akarador");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "minion_neutral_test_1");
		});
	}

	@Test
	public void testBonefetcher() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_test_deflect");
			Minion bonefetcher = playMinionCard(context, player, "minion_bonefetcher");
			assertTrue(bonefetcher.hasAttribute(Attribute.DEFLECT));
		});
	}

	@Test
	public void testSlainParty() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "minion_test_3_2_beast");
			playCard(context, player, "minion_test_3_2_beast");
			playCard(context, opponent, "minion_mech_test");
			playCard(context, opponent, "minion_mech_test");
			playCard(context, opponent, "minion_mech_test");
			playCard(context, opponent, "minion_mech_test");
			playCard(context, player, "spell_test_destroy_all");
			playCard(context, player, "spell_slain_party");
			for (Minion minion : player.getMinions()) {
				assertTrue(Race.hasRace(context, minion, "MECH"));
			}
		});
	}

	@Test
	public void testSummonAttackCards() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_neutral_test_1");
			}
			Minion dummy = playMinionCard(context, opponent, "minion_neutral_test");
			playMinionCard(context, player, "minion_boneyard_brute", dummy);
			assertEquals(dummy.getHp(), 1);
		});

		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playCard(context, player, "minion_neutral_test_1");
			}
			Minion dummy = playMinionCard(context, opponent, "minion_neutral_test_big");
			dummy.setAttack(0);
			dummy.setHp(9);
			playCard(context, player, "spell_bat_swarm", dummy);
			assertEquals(dummy.getHp(), 5);
		});
	}

	@Test
	public void testArthasMenethil() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "hero_arthas_menethil");
			player.setMana(10);
			playCard(context, player, "spell_summon_for_opponent");
			assertEquals(opponent.getMinions().size(), 1);
			destroy(context, opponent.getMinions().get(0));
			assertEquals(opponent.getMinions().size(), 0);
			assertTrue(context.getValidActions().stream().anyMatch(ga -> ga.getActionType() == ActionType.HERO_POWER));
			useHeroPower(context, player);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_neutral_test_1");
		});

		runGym((context, player, opponent) -> {
			playCard(context, opponent, "minion_mech_test");
			playCard(context, player, "spell_test_destroy_all");
			context.endTurn();
			context.endTurn();
			playCard(context, player, "hero_arthas_menethil");
			player.setMana(10);
			assertTrue(!context.getLogic().canPlayCard(player.getId(), player.getHeroPowerZone().get(0).getReference()));
			playCard(context, opponent, "minion_dragon_test");
			playCard(context, player, "spell_test_destroy_all");
			assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHeroPowerZone().get(0).getReference()));
			useHeroPower(context, player);
			assertTrue(Race.hasRace(context, player.getMinions().get(0), "DRAGON"));
		});
	}

	@Test
	public void testLadyDeathwhisper2() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_lady_deathwhisper2");
			Minion wurm = playMinionCard(context, opponent, "minion_test_deathrattle_2");
			playCard(context, player, "spell_test_deal_10", wurm);
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testPayRespects() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "spell_pay_respects");
			playCard(context, player, "spell_test_summon_tokens");
			playCard(context, player, "spell_test_summon_tokens");
			playCard(context, player, "spell_test_deal_10", player.getMinions().get(0));
			playCard(context, player, "spell_pay_respects");
			for (Minion minion : player.getMinions()) {
				assertEquals(minion.getHp(), 2);
			}
		});
	}

	@Test
	public void testObsidianSpellstone() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_obsidian_spellstone");
			playCard(context, player, "spell_overtap");
		});
	}

	/*
	@Test
	public void testAlternateBaku() {
		DebugContext context = createContext("SILVER", "SILVER", false, DeckFormat.getFormat("Custom"));
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_neutral_test_1")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_alternate_baku_the_mooneater"));
		});
		context.init();
		assertEquals(context.getPlayer1().getHeroPowerZone().get(0).getCardId(), "hero_power_alternate_totemic_slam");

		DebugContext context2 = createContext("WHITE", "WHITE", false, DeckFormat.getFormat("Custom"));
		context2.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context2.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_neutral_test_1")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_alternate_baku_the_mooneater"));
		});
		context2.init();
		assertEquals(context2.getPlayer1().getHeroPowerZone().get(0).getCardId(), "hero_power_heal");
	}

	 */

	/*
	@Test
	public void testAlternateGenn() {
		DebugContext context = createContext("WHITE", "WHITE", false, DeckFormat.getFormat("Custom"));
		context.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
		context.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
			Stream.generate(() -> "minion_test_3_2")
					.map(CardCatalogue::getCardById)
					.limit(29)
					.forEach(deck::addCard);
			deck.addCard(CardCatalogue.getCardById("minion_alternate_genn_greymane"));
		});

		context.init();
		assertTrue(context.getEntities().anyMatch(c -> c.getSourceCard().getCardId().equals("spell_lunstone")));
		playCard(context, context.getPlayer1(), "hero_shadowreaper_anduin");
		// Both player's hero powers should cost one
		assertEquals(context.getEntities().filter(c -> c.getEntityType() == EntityType.CARD)
				.map(c -> (Card) c)
				.filter(c -> c.getCardType() == CardType.HERO_POWER)
				.filter(c -> costOf(context, context.getPlayer(c.getOwner()), c) == 1)
				.count(), 2L);
	}

	 */

	/*
	@Test
	public void testAlternateStartingHeroPowers() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "passive_dire_beast");
			context.fireGameEvent(new PreGameStartEvent(context, player.getId()));
			assertEquals(player.getHeroPowerZone().get(0).getCardId(), "hero_power_dire_beast");
		});

		int direStables = 0;

		for (int i = 0; i < 100; i++) {
			DebugContext debug = createContext("GREEN", "GREEN", false, DeckFormat.getFormat("All"));
			debug.getPlayers().stream().map(Player::getDeck).forEach(CardZone::clear);
			debug.getPlayers().stream().map(Player::getDeck).forEach(deck -> {
				for (int j = 0; j < 10; j++) {
					deck.addCard("minion_faithful_lumi");
				}
			});
			debug.getPlayer1().getDeck().addCard(debug.getCardById("passive_dire_beast"));
			debug.getPlayer1().getDeck().addCard(debug.getCardById("minion_baku_the_mooneater"));
			debug.init();
			if (debug.getPlayer1().getHeroPowerZone().get(0).getCardId().equals("hero_power_dire_stable")) {
				direStables++;
			}
		}
		assertEquals(direStables, 100);
	}

	 */

	@Test
	public void testDragonfly() {
		runGym((context, player, opponent) -> {
			Minion dragonfly = playMinionCard(context, player, "minion_dragonfly");
			assertTrue(Race.hasRace(context, dragonfly, "BEAST"));
			assertTrue(Race.hasRace(context, dragonfly, "DRAGON"));
		});

	}

	@Test
	public void testBronzeTimekeeper() {
		runGym((context, player, opponent) -> {
			Minion jelly = playMinionCard(context, player, "minion_end_of_turn_trigger");
			playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_bronze_timekeeper", jelly);
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 1);
				return battlecryActions.get(0);
			});
			assertEquals(player.getDeck().size(), 1);
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, opponent, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "minion_bronze_timekeeper");
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 0);
				return battlecryActions.get(0);
			});
		});
	}

	@Test
	public void testTrophyHuntress() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion fae = playMinionCard(context, opponent, "minion_neutral_test_1");
			fae.setRace(Race.FAE);
			Minion beast = playMinionCard(context, opponent, "minion_neutral_test_1");
			beast.setRace("BEAST");
			Minion dragon = playMinionCard(context, opponent, "minion_neutral_test_1");
			dragon.setRace("DRAGON");
			for (Minion minion : opponent.getMinions()) {
				context.getLogic().setHpAndMaxHp(minion, 4);
			}
			context.endTurn();
			playMinionCard(context, player, "minion_trophy_huntress", fae);
			assertEquals(fae.getHp(), fae.getMaxHp() - 1);
			playMinionCard(context, player, "minion_trophy_huntress", beast);
			assertEquals(beast.getHp(), beast.getMaxHp() - 2);
			playMinionCard(context, player, "minion_trophy_huntress", dragon);
			assertEquals(dragon.getHp(), dragon.getMaxHp() - 3);
		});
	}

	/*
	@Test
	public void testEnchantments() {
		runGym((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			playCard(context, player, "spell_blessing_of_wisdom", wisp);
			assertEquals(wisp.getEnchantmentsFromContext(context).size(), 1);

		});
	}
	*/


	@Test
	public void testDefensiveBearing() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_defensive_bearing", target);
			assertTrue(target.hasAttribute(Attribute.TAUNT));
			assertEquals(target.getHp(), target.getBaseHp() + 2);
		});
	}

	@Test
	public void testDawnsMight() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			assertEquals(player.getHand().size(), 0);
			playCard(context, player, "spell_dawns_might", target);
			assertEquals(target.getAttack(), target.getBaseAttack() + 3);
			assertEquals(target.getHp(), target.getBaseHp() + 2);
			// Test whether twinspell exists
			assertEquals(player.getHand().size(), 1);
			Card copy = player.getHand().get(0);
			assertFalse(copy.getDescription().contains("Twinspell"));
			playCard(context, player, copy, target);
			assertEquals(player.getHand().size(), 0);
		});
	}

	//Energized Spear - 3 Mana 3/2 Weapon Common "Supremacy: Restore your minions to full Health."
	@Test
	public void testEnergizedSpear() {
		runGym((context, player, opponent) -> {
			Minion dragon1 = playMinionCard(context, player, "minion_neutral_test_big");
			Minion dragon2 = playMinionCard(context, player, "minion_neutral_test_big");
			playCard(context, player, "weapon_energized_spear");
			// Opponent's turn
			context.endTurn();
			Minion target3 = playMinionCard(context, opponent, "minion_neutral_test");
			playCard(context, opponent, "spell_test_deal_6", dragon1);
			playCard(context, opponent, "spell_test_deal_6", dragon2);
			assertEquals(dragon1.getHp(), dragon1.getBaseHp() - 6);
			assertEquals(dragon2.getHp(), dragon2.getBaseHp() - 6);
			// Player's turn
			context.endTurn();
			// Test energized spear's attack and durability
			attack(context, player, player.getHero(), target3);
			// assertTrue(target3.isDestroyed());
			assertEquals(opponent.getMinions().size(), 0);
			assertEquals(player.getWeaponZone().get(0).getDurability(), 1);
			assertEquals(dragon1.getHp(), dragon1.getBaseHp());
			assertEquals(dragon2.getHp(), dragon2.getBaseHp());
		});
	}

	// Ethereal Knight - 3 Mana 2/3 Rare "Has +1 Attack for each friendly minion."
	@Test
	public void testEtherealKnight() {
		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, "minion_neutral_test");
			Minion target2 = playMinionCard(context, player, "minion_neutral_test");
			Minion knight = playMinionCard(context, player, "minion_ethereal_knight");
			assertEquals(knight.getHp(), 3);
			assertEquals(knight.getAttack(), 5);
			assertEquals(target1.getAttack(), 2);
			assertEquals(target2.getAttack(), 2);
		});
	}

	// Tempest Sentinel - 5 Mana 4/5 Elemental Rare "Spells cast on minions costs (1) less."
	@Test
	public void testTempestSentinel() {
		runGym((context, player, opponent) -> {
			Card costly = receiveCard(context, player, "spell_test_cost_3_buff");
			Minion tempest = playMinionCard(context, player, "minion_tempest_sentinel");
			assertEquals(costOf(context, player, costly), costly.getBaseManaCost() - 1);
			assertEquals(tempest.getAttack(), 4);
			assertEquals(tempest.getHp(), 5);
		});
	}

	// Magical Haze - 7 Mana Spell Rare "Twinspell. Summon a random Elemental from your hand and give it Guard."
	@Test
	public void testMagicalHaze() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "minion_lake_elemental");
			playCard(context, player, "spell_magical_haze");
			Minion ele = player.getMinions().get(0);
			assertEquals(ele.getSourceCard().getCardId(), "minion_lake_elemental");
			assertTrue(ele.hasAttribute(Attribute.TAUNT));
			// Check twinspell
			assertEquals(player.getHand().size(), 1);
			Card copy = player.getHand().get(0);
			assertFalse(copy.getDescription().contains("Twinspell"));
			playCard(context, player, copy);
			assertEquals(player.getHand().size(), 0);
		});
	}

	// Silver Warmaiden - 4 Mana 3/3 Epic "Opener: Damage an enemy minion until it has 1 Health."
	@Test
	public void testSilverWarmaiden() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target = playMinionCard(context, opponent, "minion_neutral_test_big");
			context.endTurn();
			Minion maiden = playMinionCard(context, player, "minion_silver_warmaiden", target);
			assertEquals(target.getHp(), 1);
			assertEquals(maiden.getHp(), 3);
			assertEquals(maiden.getAttack(), 3);
		});
	}

	// Hydraquatom (Atomius in Spellsource, as seen above) - 8 Mana 8/8 Legendary Elemental "Aftermath: Release four blasts at random enemies that deals 4 damage each."
	@Test
	public void testHydraquatom() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "permanent_puzzle_box");
			Minion target2 = playMinionCard(context, opponent, "permanent_puzzle_box");
			context.endTurn();
			Minion hydra = playMinionCard(context, player, "minion_hydraquatom");
			context.endTurn();
			int basehp = opponent.getHero().getHp();
			playCard(context, opponent, "spell_test_deal_6", hydra);
			playCard(context, opponent, "spell_test_deal_6", hydra);
			assertTrue(hydra.isDestroyed());
			assertEquals(opponent.getHero().getHp() + target1.getHp() + target2.getHp(), basehp + target1.getBaseHp() + target2.getBaseHp() - 16);
		});
	}

	// Blastflame Dragon - 8 Mana 7/7 Dragon Common "Supremacy: Deal 7 damage to a random enemy minion."
	@Test
	public void testBlastflameDragon() {
		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, player, "minion_blastflame_dragon");
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test_14");
			context.endTurn();
			attack(context, player, dragon, target1);
			assertTrue(target1.isDestroyed());
			assertEquals(target2.getHp(), 7);
		});
	}

	// Tower Ophidian - 5 Mana 2/6 Dragon Common "Supremacy: Summon a copy of this minion."
	@Test
	public void testTowerOphidian() {
		runGym((context, player, opponent) -> {
			Minion tower = playMinionCard(context, player, "minion_tower_ophidian");
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			attack(context, player, tower, target1);
			Minion original = player.getMinions().get(0);
			assertEquals(original.getSourceCard().getCardId(), "minion_tower_ophidian");
			Minion copy = player.getMinions().get(1);
			assertEquals(copy.getSourceCard().getCardId(), "minion_tower_ophidian");
		});
	}

	// Dragon Caretaker - 1 Mana 2/1 Rare "Battlecry: Give a friendly Dragon +2 Health."
	@Test
	public void testDragonCaretaker() {
		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, player, "minion_blastflame_dragon");
			playMinionCard(context, player, "minion_dragon_caretaker");
			assertEquals(dragon.getHp(), dragon.getBaseHp() + 2);
		});

		runGym((context, player, opponent) -> {
			Minion nonDragon = playMinionCard(context, player, "minion_test_3_2");
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 0);
				return battlecryActions.get(0);
			});
			playMinionCard(context, player, "minion_dragon_caretaker");
			assertEquals(nonDragon.getHp(), nonDragon.getBaseHp());
		});

		runGym((context, player, opponent) -> {
			Minion dragon = playMinionCard(context, opponent, "minion_blastflame_dragon");
			overrideBattlecry(context, player, battlecryActions -> {
				assertEquals(battlecryActions.size(), 0);
				return battlecryActions.get(0);
			});
			playMinionCard(context, player, "minion_dragon_caretaker");
			assertEquals(dragon.getHp(), dragon.getBaseHp());
		});
	}

	// Siphonscale Blade - 8 Mana 6/3 Weapon Epic "Can attack again after a friendly minion attacks and kills a minion."
	@Test
	public void testSiphonscaleBlade() {
		runGym((context, player, opponent) -> {
			Minion friend = playMinionCard(context, player, "minion_neutral_test");
			playCard(context, player, "weapon_siphonscale_blade");
			context.endTurn();
			Minion target1 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion target2 = playMinionCard(context, opponent, "minion_neutral_test_1");
			Minion target3 = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, player, player.getHero(), target1);
			attack(context, player, friend, target2);
			assertTrue(target2.isDestroyed());
			attack(context, player, player.getHero(), target3);
			assertTrue(target3.isDestroyed());
		});
	}

	// Irena, Dragon Knight - 3 Mana 2/1 Legendary "Blitz. Adjacent Dragons have double their Attack."
	@Test
	public void testIrenaDragonKnight() {
		// dragon, dragon, Irena
		runGym((context, player, opponent) -> {
			Minion dragon1 = playMinionCard(context, player, "minion_blastflame_dragon");
			Minion dragon2 = playMinionCard(context, player, "minion_tower_ophidian");
			playMinionCard(context, player, "minion_irena_dragon_knight");
			assertEquals(dragon1.getAttack(), dragon1.getBaseAttack());
			assertEquals(dragon2.getAttack(), dragon2.getBaseAttack() * 2);
		});
		// Irena, Dragon, Dragon
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_irena_dragon_knight");
			Minion dragon1 = playMinionCard(context, player, "minion_blastflame_dragon");
			Minion dragon2 = playMinionCard(context, player, "minion_tower_ophidian");
			assertEquals(dragon1.getAttack(), dragon1.getBaseAttack() * 2);
			assertEquals(dragon2.getAttack(), dragon2.getBaseAttack());
		});
		// Dragon, Irena, Dragon
		runGym((context, player, opponent) -> {
			Minion dragon1 = playMinionCard(context, player, "minion_blastflame_dragon");
			playMinionCard(context, player, "minion_irena_dragon_knight");
			Minion dragon2 = playMinionCard(context, player, "minion_tower_ophidian");
			assertEquals(dragon1.getAttack(), dragon1.getBaseAttack() * 2);
			assertEquals(dragon2.getAttack(), dragon2.getBaseAttack() * 2);
		});
		// Non-dragon, Irena, Dragon
		runGym((context, player, opponent) -> {
			Minion nondragon = playMinionCard(context, player, "minion_neutral_test_1");
			playMinionCard(context, player, "minion_irena_dragon_knight");
			Minion dragon2 = playMinionCard(context, player, "minion_tower_ophidian");
			assertEquals(nondragon.getAttack(), nondragon.getBaseAttack());
			assertEquals(dragon2.getAttack(), dragon2.getBaseAttack() * 2);
		});
		// Irena, Dragon, Irena
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_irena_dragon_knight");
			Minion dragon = playMinionCard(context, player, "minion_tower_ophidian");
			playMinionCard(context, player, "minion_irena_dragon_knight");
			assertEquals(dragon.getAttack(), dragon.getBaseAttack() * 4);
		});
	}

	// Mollusk Meister: "Opener: Give a friendly minion +8 Health. Gain Armor equal to its Health.",
	@Test
	public void testMolluskMeister() {
		runGym((context, player, opponent) -> {
			Minion friendly = playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_mollusk_meister", friendly);
			assertEquals(friendly.getHp(), friendly.getBaseHp() + 8);
			assertEquals(player.getHero().getArmor(), friendly.getMaxHp());
		});
	}

	@Test
	public void testAncientBlood() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(2);
			playCard(context, player, "spell_ancient_blood");
			assertEquals(player.getHero().getHp(), 2);
			for (int i = 0; i < 3; i++) {
				playCard(context, player, "minion_test_3_2");
				playCard(context, opponent, "minion_test_3_2");
			}
			playCard(context, player, "spell_test_deal_4_to_enemies");
			playCard(context, player, "spell_ancient_blood");
			assertEquals(player.getHero().getHp(), 11);
			playCard(context, player, "spell_test_deal_3_to_all");
			assertEquals(player.getHero().getHp(), 8);
			playCard(context, player, "spell_ancient_blood");
			assertEquals(player.getHero().getHp(), 26);
		});
	}

	@Test
	public void testBloodyBlow() {
		CardCatalogue.loadCardsFromPackage();
		CardCatalogue.getCardById("spell_bloody_blow");
		runGym((context, player, opponent) -> {
			Minion minion1 = playMinionCard(context, opponent, "minion_test_3_2");
			Minion minion2 = playMinionCard(context, opponent, "minion_test_3_2");

			playCard(context, player, "spell_bloody_blow");
			assertFalse(minion1.isDestroyed());
			assertFalse(minion2.isDestroyed());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - 2);

			playCard(context, player, "spell_bloody_blow");
			assertTrue(minion1.isDestroyed());
			assertTrue(minion2.isDestroyed());
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - 4);
		});
	}

	@Test
	public void testRitualShaman() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "spell_test_counter_secret");
			playCard(context, player, "minion_ritual_shaman");
			assertEquals(player.getSecrets().size(), 1);
		});
	}

	@Test
	public void testGhastlyVisage() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_ghastly_visage");
			context.endTurn();
			playCard(context, opponent, "minion_test_opener_summon");
			assertEquals(opponent.getMinions().size(), 1);
			assertEquals(opponent.getHand().get(0).getCardId(), "minion_test_opener_summon");
		});
	}

	@Test
	public void testFuryOfTheElements() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "token_bellowing_spirit");
			playCard(context, player, "token_unearthed_spirit");
			playCard(context, player, "token_burning_spirit");
			player.setMana(10);
			receiveCard(context, player, "spell_fury_of_the_elements");
			assertEquals(context.getValidActions().size(), 7);
			playCard(context, player, "spell_fury_of_the_elements");
			assertEquals(player.getMinions().size(), 5);
		}, "JADE", "JADE");
		runGym((context, player, opponent) -> {
			player.setMana(10);
			playCard(context, player, "spell_fury_of_the_elements");
			assertFalse(player.getMinions().get(0).equals(player.getMinions().get(1)));
		});
	}

	@Test
	public void testGreaterGood() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_greater_good");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - 5);
			receiveCard(context, player, "spell_test_gain_mana");
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - 5);
			assertEquals(player.getHand().size(), 0);
		});
	}

	@Test
	public void testSubjectToSacrifice() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_subject_to_sacrifice");
			Minion playerMinion1 = playMinionCard(context, player, "minion_charge_test_1");
			Minion playerMinion2 = playMinionCard(context, player, "minion_charge_test_1");
			Minion playerMinion3 = playMinionCard(context, player, "minion_charge_test_1");
			Minion playerMinion4 = playMinionCard(context, player, "minion_charge_test_1");
			Minion playerMinion5 = playMinionCard(context, player, "minion_charge_test_1");
			context.endTurn();
			playCard(context, opponent, "minion_test_4_5");

			assertTrue(playerMinion1.isDestroyed());
			assertTrue(playerMinion2.isDestroyed());
			assertTrue(playerMinion3.isDestroyed());
			assertTrue(playerMinion4.isDestroyed());
			assertTrue(playerMinion5.isDestroyed());
			assertEquals(opponent.getMinions().size(), 0);
		});
	}

	@Test
	public void testEmbuePhantasms() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_charge_test_1");
			shuffleToDeck(context, player, "minion_charge_test_1");
			playCard(context, player, "spell_embue_phantasms");
			Minion yeti = playMinionCard(context, opponent, "minion_test_4_5");
			Minion boar1 = playMinionCard(context, player, player.getHand().peekFirst());
			Minion boar2 = playMinionCard(context, player, player.getHand().peekFirst());
			attack(context, player, boar1, yeti);
			attack(context, player, boar2, yeti);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_voodoo_spirit");
			assertEquals(player.getMinions().get(1).getSourceCard().getCardId(), "token_voodoo_spirit");
		});
	}

	@Test
	public void testSacrificialBlade() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			playCard(context, player, "weapon_sacrificial_blade");
			assertEquals(player.getHero().getHp(), 20);
			playMinionCard(context, opponent, "minion_test_4_5");
			playCard(context, player, "spell_test_destroy_all");
			playCard(context, player, "weapon_sacrificial_blade");
			assertEquals(player.getHero().getHp(), 25);
		});

		runGym((context, player, opponent) -> {
			player.getHero().setHp(20);
			playCard(context, player, "weapon_sacrificial_blade");
			assertEquals(player.getHero().getHp(), 20);
			playMinionCard(context, player, "minion_test_4_5");
			playCard(context, player, "spell_test_destroy_all");
			playCard(context, player, "weapon_sacrificial_blade");
			assertEquals(player.getHero().getHp(), 25);
		});
	}

	@Test
	public void testShitakiriSlitTongueSuzumue() {
		runGym((context, player, opponent) -> {
			Minion shitakiri = playMinionCard(context, player, "token_shitakiri_slit_tongue_suzume");
			Minion hadDeflect = playMinionCard(context, player, "minion_test_deflect");
			Minion didNotHaveDeflect = playMinionCard(context, player, "minion_test_3_2");
			context.endTurn();
			assertTrue(shitakiri.hasAttribute(Attribute.DEFLECT));
			assertTrue(hadDeflect.hasAttribute(Attribute.DEFLECT));
			assertTrue(didNotHaveDeflect.hasAttribute(Attribute.DEFLECT));
			context.endTurn();
			assertTrue(shitakiri.hasAttribute(Attribute.DEFLECT));
			assertTrue(hadDeflect.hasAttribute(Attribute.DEFLECT));
			assertFalse(didNotHaveDeflect.hasAttribute(Attribute.DEFLECT));
		});
	}

	@Test
	public void testProperCardIds() {
		CardCatalogue.loadCardsFromPackage();
		CardCatalogue.getCardById("minion_jade_cloud_serpent");
		CardCatalogue.getCardById("spell_honed_potion");
		CardCatalogue.getCardById("token_bellowing_spirit");
		CardCatalogue.getCardById("token_burning_spirit");
	}

	@Test
	public void testThitazov() {
		// attacker from friendly side dies, won't get buffed
		runGym(((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			Minion friend1 = playMinionCard(context, player, "minion_neutral_test");
			Minion thitazov = playMinionCard(context, player, "minion_thitazov");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, player, wisp, enemy);
			assertTrue(enemy.isDestroyed());
			assertTrue(wisp.isDestroyed());
			assertEquals(friend1.getHp(), friend1.getBaseHp() + 1);
			assertEquals(thitazov.getHp(), thitazov.getBaseHp());
			assertEquals(thitazov.getAttack(), thitazov.getBaseAttack());
		}));
		// attacker from friendly side survives, gets buffed
		runGym(((context, player, opponent) -> {
			Minion wisp = playMinionCard(context, player, "minion_neutral_test_1");
			Minion friend1 = playMinionCard(context, player, "minion_neutral_test");
			Minion thitazov = playMinionCard(context, player, "minion_thitazov");
			context.endTurn();
			Minion enemy = playMinionCard(context, opponent, "minion_neutral_test_1");
			context.endTurn();
			attack(context, player, friend1, enemy);
			assertTrue(enemy.isDestroyed());
			// friend1 gets the 1 hp that it loses due to the attack back through thitazov's buff
			assertEquals(friend1.getHp(), friend1.getBaseHp());
			assertEquals(wisp.getHp(), wisp.getBaseHp() + 1);
			assertEquals(thitazov.getAttack(), thitazov.getBaseAttack());
		}));
	}

	@Test
	public void testStanceChange() {
		runGym(((context, player, opponent) -> {
			Minion test32 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_kitsune_stance");
			assertEquals(test32.getAttack(), test32.getBaseAttack() + 2);
		}));

		runGym(((context, player, opponent) -> {
			playMinionCard(context, player, "minion_test_3_2");
			playMinionCard(context, player, "minion_test_3_2");
			playMinionCard(context, player, "minion_test_3_2");
			player.getHero().setHp(20);
			playCard(context, player, "spell_koi_stance");
			assertEquals(player.getHero().getHp(), 20 + (2 * player.getMinions().size()));
		}));

		runGym(((context, player, opponent) -> {
			Minion test32 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_suzume_stance");
			assertEquals(test32.getAttack(), test32.getBaseAttack() + 1);
			assertEquals(test32.getHp(), test32.getBaseHp() + 1);
			assertTrue(test32.hasAttribute(Attribute.DIVINE_SHIELD));
		}));

		runGym(((context, player, opponent) -> {
			Minion test32 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_tanuki_stance");
			assertEquals(test32.getHp(), test32.getBaseHp() + 2);
			assertTrue(test32.hasAttribute(Attribute.TAUNT));
		}));
	}

	@Test
	public void testEbisusChosen() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "minion_ebisus_chosen");
			player.getHero().setHp(20);
			player.getHero().setAttack(2);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getHp(), 24);
			Minion test32 = playMinionCard(context, opponent, "minion_test_3_2");
			attack(context, player, player.getHero(), test32);
			assertEquals(player.getHero().getHp(), 28 - test32.getAttack());
		}));
	}

	@Test
	public void testWardenSaihan() {
		runGym(((context, player, opponent) -> {
			Minion warden = playMinionCard(context, player, "minion_warden_saihan");
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			assertEquals(warden.getHp(), warden.getMaxHp());

			playCard(context, player, "spell_test_deal_4_to_enemies");
			assertEquals(warden.getHp(), warden.getMaxHp());

			Minion test32 = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_test_deal_6", test32);
			assertFalse(test32.isDestroyed());
			assertEquals(test32.getHp(), test32.getMaxHp());
			assertEquals(warden.getHp(), warden.getMaxHp() - 6);
		}));

		runGym(((context, player, opponent) -> {
			Minion warden1 = playMinionCard(context, player, "minion_warden_saihan");
			Minion warden2 = playMinionCard(context, player, "minion_warden_saihan");
			playCard(context, player, "spell_test_deal_6", warden1);
			assertEquals(warden1.getHp(), warden1.getMaxHp() - 6);
			assertEquals(warden2.getHp(), warden2.getMaxHp());
		}));
	}

	@Test
	public void testStringShot() {
		runGym(((context, player, opponent) -> {
			Minion target = playMinionCard(context, opponent, "minion_test_3_2");
			Card stringShotCard = receiveCard(context, player, "spell_string_shot");
			playCard(context, player, "minion_lil_wormy");
			player.setMana(1);

			assertEquals(context.getValidActions().stream().filter(action ->
					Objects.equals(action.getSourceReference(), stringShotCard.getReference()) &&
							Objects.equals(action.getTargetReference(), target.getReference())).count(), 1L);
		}));
	}

	@Test
	public void testScopeOut() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			playCard(context, player, "spell_scope_out");
			assertEquals(player.getHand().size(), 4);
			assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_4_test");
			playCard(context, player, "spell_scope_out");
			assertEquals(player.getHand().size(), 4);
			assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_4_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			playCard(context, player, "spell_scope_out");
			assertEquals(player.getHand().size(), 4);
			assertEquals(player.getDeck().size(), 0);
		});

		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_11_test");
			putOnTopOfDeck(context, player, "minion_cost_4_test");
			putOnTopOfDeck(context, player, "minion_cost_4_test");
			playCard(context, player, "spell_scope_out");
			assertEquals(player.getHand().size(), 2);
			assertEquals(player.getDeck().size(), 2);
		});
	}

	@Test
	public void testGravekeeperGallows() {
		runGym(((context, player, opponent) -> {
			Minion grallows = playMinionCard(context, player, "minion_gravekeeper_grallows");
			Card notWeapon = receiveCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			Card weapon = receiveCard(context, player, "weapon_slapdagger");
			destroy(context, grallows);
			assertEquals(weapon.getDescription(), "Aftermath: Summon Grallows.");
		}));

		runGym(((context, player, opponent) -> {
			Minion grallows = playMinionCard(context, player, "minion_gravekeeper_grallows");
			Card weapon = receiveCard(context, player, "weapon_dig_up_shovel");
			destroy(context, grallows);
			assertEquals(weapon.getDescription(), "Decay. At the end of your turn, draw a card. Aftermath: Summon Grallows.");
		}));
	}

	@Test
	public void testOmegaRune() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "spell_the_omega_rune");
			player.getHero().setHp(20);
			opponent.getHero().setHp(20);

			playCard(context, player, "spell_test_deal_5_to_enemy_hero");
			assertEquals(opponent.getHero().getHp(), 15);
			assertEquals(player.getHero().getHp(), 25);

			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			assertEquals(opponent.getHero().getHp(), 15);
			assertEquals(player.getHero().getHp(), 20);
		}));
	}

	@Test
	public void testTheGlutton() {
		runGym(((context, player, opponent) -> {
			Minion glutton = playMinionCard(context, player, "minion_the_glutton");
			Minion toBeEaten = playMinionCard(context, player, "minion_black_test");
			assertEquals(glutton.getAttack(), glutton.getBaseAttack());
			context.endTurn();
			assertTrue(toBeEaten.isDestroyed());
			assertEquals(glutton.getAttack(), glutton.getBaseAttack() + 1);
			context.endTurn();
			context.endTurn();
			assertEquals(glutton.getAttack(), glutton.getBaseAttack() + 1);
		}));

		runGym(((context, player, opponent) -> {
			context.endTurn();
			Minion toBeEaten = playMinionCard(context, opponent, "minion_black_test");
			context.endTurn();
			Minion glutton = playMinionCard(context, player, "minion_the_glutton");
			assertEquals(glutton.getAttack(), glutton.getBaseAttack());
			context.endTurn();
			assertTrue(toBeEaten.isDestroyed());
			assertEquals(glutton.getAttack(), glutton.getBaseAttack() + 1);
			context.endTurn();
			context.endTurn();
			assertEquals(glutton.getAttack(), glutton.getBaseAttack() + 1);
		}));
	}

	@Test
	public void testMatriarchAiiranDescription() {
		runGym((context, player, opponent) -> {
			Minion aiiranOnBoard = playMinionCard(context, player, "minion_matriarch_aiiran");
			Card aiiranInHand = receiveCard(context, player, "minion_matriarch_aiiran");
			assertEquals(aiiranOnBoard.getDescription(context, player), "Opener: Deal X damage. (Increases by 2 for each other Dragon in your hand)");
			assertEquals(aiiranInHand.getDescription(context, player), "Opener: Deal 0 damage. (Increases by 2 for each other Dragon in your hand)");
		});
	}

	@Test
	public void testArmorLostAndGnomechanic() {
		runGym(((context, player, opponent) -> {
			player.getHero().modifyArmor(10);
			Card card = receiveCard(context, player, "minion_gnomechanic");
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			playCard(context, opponent, "spell_test_deal_5_to_enemy_hero");
			assertEquals(player.getHero().getArmor(), 0);
			playCard(context, player, card);
			assertEquals(player.getHero().getArmor(), 10);
		}));
	}

	@Test
	public void testPaven() {
		runGym(((context, player, opponent) -> {
			Minion paven1 = playMinionCard(context, player, "minion_paven_elemental_of_surprise");
			destroy(context, paven1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "permanent_paven_captured");
			playCard(context, opponent, "spell_test_discover1");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "permanent_paven_captured");
			playCard(context, player, "spell_test_discover1");
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "minion_paven_elemental_of_surprise");
		}));

		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion minion = playMinionCard(context, opponent, 10, 10);
			context.endTurn();
			// Forces miserable conclusion to be added to paven elemental of surprise, whose aftermath is a transform effect
			context.setDeckFormat(new FixedCardsDeckFormat("spell_miserable_conclusion"));
			Card paven = receiveCard(context, player, "minion_paven_elemental_of_surprise");
			playCard(context, player, "spell_alagards_infusion");
			playCard(context, player, paven);
		});
	}

	@Test
	public void testSotMountainExcavation() {
		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_legendary_test"));
			playCard(context, player, "spell_sot_mountain_excavation");
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getRarity().isRarity(Rarity.LEGENDARY)).count(), 1L);
		});

		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_legendary_test"));
			player.getHero().modifyArmor(8);
			playCard(context, player, "spell_sot_mountain_excavation");
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getRarity().isRarity(Rarity.LEGENDARY)).count(), 3L);
		});

		runGym((context, player, opponent) -> {
			context.setDeckFormat(new FixedCardsDeckFormat("minion_legendary_test"));
			player.getHero().modifyArmor(28);
			playCard(context, player, "spell_sot_mountain_excavation");
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getRarity().isRarity(Rarity.LEGENDARY)).count(), 7L);
			assertEquals(player.getHero().getArmor(), 4);
		});
	}

	@RepeatedTest(100)
	public void testSurveyorSkag() {
		FixedCardsDeckFormat fixedCardsDeckFormat = new FixedCardsDeckFormat(
				"minion_alien_ravager",
				"minion_ankylo_devotee",
				"minion_barside_slinker",
				"minion_bighand_brute",
				"minion_bromeliad_pup",
				"minion_buffeting_elemental",
				"minion_channeled_spirit",
				"minion_daring_duelist",
				"minion_disco_inferno",
				"minion_doomed_diver"
		);
		runGym(((context, player, opponent) -> {
			context.setDeckFormat(fixedCardsDeckFormat);
			for (int i = 0; i < 5; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			playCard(context, player, "minion_surveyor_skag");
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 5);
		}));

		runGym(((context, player, opponent) -> {
			context.setDeckFormat(fixedCardsDeckFormat);
			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "spell_lunstone");
			}
			playCard(context, player, "minion_surveyor_skag");
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 10);
		}));

		runGym(((context, player, opponent) -> {
			context.setDeckFormat(fixedCardsDeckFormat);
			playCard(context, player, "minion_surveyor_skag");
			assertEquals(player.getHand().size(), 10);
			assertEquals(player.getDeck().size(), 0);
		}));
	}

	@Test
	public void testDigIn() {
		runGym(((context, player, opponent) -> {
			receiveCard(context, player, "minion_black_test");
			receiveCard(context, opponent, "minion_blue_test");
			playCard(context, player, "spell_dig_in");
			assertEquals(player.getDeck().get(0).getCardId(), "minion_blue_test");
			assertEquals(opponent.getDeck().get(0).getCardId(), "minion_black_test");
		}));
	}

	@Test
	public void testMonolithOfDoomDescription() {
		runGym((context, player, opponent) -> {
			Minion monolithOnBoard = playMinionCard(context, player, "minion_monolith_of_doom");
			Card monolithInHand = receiveCard(context, player, "minion_monolith_of_doom");
			assertEquals(monolithOnBoard.getDescription(context, player), "Opener: Deal X damage. (Doubles for each Monolith of Doom you played this turn)");
			assertEquals(monolithInHand.getDescription(context, player), "Opener: Deal 2 damage. (Doubles for each Monolith of Doom you played this turn)");
		});
	}

	@Test
	public void testBerryHoarder() {
		runGym(((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_knight_eternal");
			putOnTopOfDeck(context, player, "minion_knight_eternal");
			putOnTopOfDeck(context, player, "weapon_stick");
			putOnTopOfDeck(context, player, "weapon_stick");
			playCard(context, player, "minion_berry_hoarder");
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHand().size(), 0);
		}));

		runGym(((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_knight_eternal");
			putOnTopOfDeck(context, player, "minion_knight_eternal");
			putOnTopOfDeck(context, player, "weapon_stick");
			putOnTopOfDeck(context, player, "weapon_stick");
			player.setMana(5);
			playCard(context, player, "minion_berry_hoarder");
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHand().size(), 4);
		}));
	}

	@Test
	public void testFlamerunner() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "minion_flamerunner");
			player.getHero().setHp(25);
			playCard(context, player, "spell_test_heal_8", player.getHero());
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getCardId().equals("token_ember_elemental")).count(), 0L);
			context.endTurn();
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getCardId().equals("token_ember_elemental")).count(), 1L);
			playCard(context, player, "spell_test_heal_8", player.getHero());
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getCardId().equals("token_ember_elemental")).count(), 1L);
			context.endTurn();
			assertEquals((int) player.getMinions().stream().filter(minion -> minion.getSourceCard().getCardId().equals("token_ember_elemental")).count(), 1L);
		}));
	}

	@Test
	public void testIronPreserver() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "minion_iron_preserver");
			playCard(context, player, "weapon_test_1_1");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), "weapon_test_1_1");
		}));
	}

	@Test
	public void testRejanAndExcalibur() {
		runGym(((context, player, opponent) -> {
			Minion rejan = playMinionCard(context, player, "minion_rejan_last_defender");
			Weapon weapon = player.getHero().getWeapon();
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(weapon.getDurability(), weapon.getMaxDurability());
			context.endTurn();
			assertEquals(weapon.getDurability(), weapon.getMaxDurability());
			context.endTurn();

			destroy(context, rejan);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(weapon.getDurability(), weapon.getMaxDurability() - 1);
			context.endTurn();
			assertEquals(weapon.getDurability(), weapon.getMaxDurability() - 2);
			assertTrue(weapon.isBroken());
		}));
	}

	@Test
	public void testFoundGoods() {
		runGym(((context, player, opponent) -> {
			Card card = receiveCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_found_goods");
			player.setMana(2);
			playCard(context, player, card);
			assertEquals(player.getMana(), 2);
		}));
	}

	@Test
	public void testMasterEroder() {
		// test basic master eroder
		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			playCard(context, player, "minion_master_eroder");
			context.endTurn();
			assertEquals(minion.getHp(), minion.getMaxHp());
			context.endTurn();
			assertEquals(minion.getHp(), minion.getMaxHp() - 1);
		}));

		// test master eroder with mind control
		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			playCard(context, player, "minion_master_eroder");
			playCard(context, opponent, "spell_test_give_away", minion);
			context.endTurn();
			assertEquals(minion.getHp(), minion.getMaxHp() - 1);
			context.endTurn();
			assertEquals(minion.getHp(), minion.getMaxHp() - 1);
		}));

		// test that master eroder doesn't double stack its effect
		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			playCard(context, player, "minion_master_eroder");
			playCard(context, player, "minion_master_eroder");
			context.endTurn();
			assertEquals(minion.getHp(), minion.getMaxHp());
			context.endTurn();
			assertEquals(minion.getHp(), minion.getMaxHp() - 1);
		}));
	}

	@Test
	public void testPlantPlating() {
		runGym(((context, player, opponent) -> {
			Minion minion = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_plant_plating", minion);
			assertEquals(player.getHero().getArmor(), minion.getBaseHp() + 3);
		}));
	}

	@Test
	public void testPortableFlytrap() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "weapon_portable_flytrap");
			Minion minion = playMinionCard(context, opponent, "minion_test_3_2");
			attack(context, opponent, minion, player.getHero());
			assertTrue(minion.isDestroyed());
		}));
	}

	@Test
	public void testXitalu() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_xitalu");
			Minion playerMinion = playMinionCard(context, player, "minion_test_3_2");
			playCard(context, player, "spell_shuffle_minion_to_deck", playerMinion);
			assertEquals(player.getDeck().stream().filter(card -> card.getSourceCard().getCardId().equals("minion_test_3_2")).collect(Collectors.toList()).size(), 3);

			Minion opponentMinion = playMinionCard(context, opponent, "minion_test_3_2");
			playCard(context, player, "spell_shuffle_minion_to_opponents_deck", opponentMinion);
			assertEquals(opponent.getDeck().stream().filter(card -> card.getSourceCard().getCardId().equals("minion_test_3_2")).collect(Collectors.toList()).size(), 3);
		});
	}

	@Test
	public void test8thHeavenFormation() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_8th_heaven_formation");
			player.setMana(6);
			assertEquals(1, context.getValidActions().stream()
					.filter(ga -> ga.getActionType() == ActionType.SPELL).count());
			Minion demon = playMinionCard(context, player, "minion_demon_test");
			destroy(context, demon);
			playMinionCard(context, player, "minion_demon_test");
			playMinionCard(context, opponent, "minion_demon_test");
			player.setMana(6);
			assertEquals(4, context.getValidActions().stream()
					.filter(ga -> ga.getActionType() == ActionType.SPELL).count());
		});
	}

	@Test
	public void testMosshorn() {
		runGym((context, player, opponent) -> {
			Card mosshorn = receiveCard(context, player, "minion_mosshorn");
			assertEquals(costOf(context, player, mosshorn), mosshorn.getBaseManaCost());
			playCard(context, player, "spell_lunstone");
			assertEquals(costOf(context, player, mosshorn), mosshorn.getBaseManaCost() - 1);
			playCard(context, player, "spell_revelation");
			assertEquals(costOf(context, player, mosshorn), 1);
			playCard(context, player, "spell_lunstone");
			assertEquals(costOf(context, player, mosshorn), 0);
		});
	}

	@Test
	public void testAllForOne() {
		for (int i = 1; i <= 7; i++) {
			int finalI = i;
			runGym((context, player, opponent) -> {
				Minion guy = null;
				for (int j = 0; j < finalI; j++) {
					guy = playMinionCard(context, player, "minion_neutral_test_1");
				}
				playCard(context, player, "spell_all_for_one", guy);
				assertEquals(guy.getAttack(), guy.getBaseAttack() + finalI);
			});
		}
	}

	@Test
	public void testSourceforgedSword() {
		runGym(((context, player, opponent) -> {
			playCard(context, player, "weapon_portable_flytrap");
			playCard(context, player, "weapon_the_sourceforged_sword");
			assertEquals(player.getHero().getWeapon().getDurability(), player.getHero().getWeapon().getBaseDurability() + 1);
		}));
	}

	@Test
	public void testChefStitchesInteractions() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_chef_stitches");
			for (int i = 0; i < 10; i++) {
				receiveCard(context, player, "minion_neutral_test");
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			playCard(context, player, "minion_burping_whelp");
			playCard(context, player, "minion_food_critic");
			assertEquals(player.getDeck().size(), 9); //doesn't cause a loop that would destroy your whole deck
			assertEquals(opponent.getHero().getHp(), 29); //overdrawing does still counts as roasting for other purposes
		});
	}

	@Test
	public void testTopCardsOfDeckSource() {
		runGym((context, player, opponent) -> {
			CardDesc cardDesc = context.getCardById("spell_otherwordly_truth").getDesc().clone();
			CardSourceDesc sourceDesc = new CardSourceDesc(TopCardsOfDeckSource.class);
			sourceDesc.put(CardSourceArg.TARGET_PLAYER, TargetPlayer.OPPONENT);
			sourceDesc.put(CardSourceArg.VALUE, 5);
			cardDesc.getSpell().put(SpellArg.CARD_SOURCE, sourceDesc.create());
			cardDesc.getSpell().put(SpellArg.VALUE, 5);
			Card card = cardDesc.create();
			context.addTempCard(card);

			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, opponent, "spell_otherwordly_truth");
			}

			playCard(context, player, card);
			assertEquals(30, opponent.getDeck().size());
			assertEquals(5, player.getDeck().size());
		});
	}

	@Test
	public void testKliveIcetoothSolo() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(1);
			playCard(context, player, "minion_klive_icetooth");
			assertEquals(6, player.getHero().getHp());
		});
	}
}
