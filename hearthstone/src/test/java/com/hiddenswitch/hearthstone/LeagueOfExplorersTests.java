package com.hiddenswitch.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.FixedCardsDeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import net.demilich.metastone.tests.util.TestBehaviour;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

public class LeagueOfExplorersTests extends TestBase {

	@Test
	public void testRenoJackson() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, player, "spell_mirror_image");
			player.getHero().setHp(1);
			playCard(context, player, "minion_reno_jackson");
			assertEquals(player.getHero().getHp(), 30);
		});

		// Test a specific deck from a user
		{
			String deckListReno = "### Full Legend Paladin\n" +
					"# Class: GOLD\n" +
					"# Format: Custom\n" +
					"#\n" +
					"# 1x (1) Sir Finley Mrrgglton\n" +
					"# 1x (2) Prince Keleseth\n" +
					"# 1x (3) Moroes\n" +
					"# 1x (3) Wickerflame Burnbristle\n" +
					"# 1x (3) Zola the Gorgon\n" +
					"# 1x (4) Spiritsinger Umbra\n" +
					"# 1x (5) Elise the Trailblazer\n" +
					"# 1x (5) Feugen\n" +
					"# 1x (5) Nexus-Champion Saraad\n" +
					"# 1x (5) Prince Malchezaar\n" +
					"# 1x (5) Stalagg\n" +
					"# 1x (5) Unnerfed Sylvanas Windrunner\n" +
					"# 1x (6) Cairne Bloodhoof\n" +
					"# 1x (6) Emperor Thaurissan\n" +
					"# 1x (6) Justicar Trueheart\n" +
					"# 1x (6) Reno Jackson\n" +
					"# 1x (6) Sunkeeper Tarim\n" +
					"# 1x (6) Val'anyr\n" +
					"# 1x (7) Dr. Boom\n" +
					"# 1x (7) Nexus-King Salhadaar\n" +
					"# 1x (8) Kel'Thuzad\n" +
					"# 1x (8) Ragnaros the Firelord\n" +
					"# 1x (8) The Lich King\n" +
					"# 1x (8) Tirion Fordring\n" +
					"# 1x (9) King Krush\n" +
					"# 1x (9) Uther of the Ebon Blade\n" +
					"# 1x (9) Ysera\n" +
					"# 1x (10) N'Zoth, the Corruptor\n" +
					"# 1x (10) Y'Shaarj, Rage Unbound\n" +
					"# 1x (30) Rise of the Ancient Ones";

			GameContext context = GameContext.fromDeckLists(Arrays.asList(deckListReno, deckListReno));
			// Prevent Prince Malchezaar from shuffling a repeat legendary into your deck
			context.setDeckFormat(new FixedCardsDeckFormat("minion_legendary_test"));
			context.init();
			Player player = context.getActivePlayer();
			context.startTurn(player.getId());
			// Put Reno into the player's hand
			Card reno = Stream.concat(player.getDeck().stream(), player.getHand().stream()).filter(c -> c.getCardId().equals("minion_reno_jackson"))
					.findFirst()
					.orElseThrow(AssertionError::new);
			if (reno.getZone() != Zones.HAND) {
				context.getLogic().receiveCard(player.getId(), reno);
			}
			player.getHero().setHp(1);
			assertEquals(player.getDeck().stream().map(Card::getCardId).distinct().count(), (long) player.getDeck().size());
			playCard(context, player, reno);
			assertEquals(player.getHero().getHp(), 30);
		}
	}

	@Test
	public void testAnimatedArmor() {
		// Dealing zero damage should not cause Animated Armor to deal one damage instead
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_animated_armor");
			playCard(context, player, "weapon_wicked_knife");
			attack(context, player, player.getHero(), opponent.getHero());
			Assert.assertEquals(player.getHero().getHp(), player.getHero().getMaxHp(), "Should not have changed 0 damage to 1");
		});
	}

	@Test
	public void testNagaSeaWitch() {
		// Test basic cards
		runGym((context, player, opponent) -> {
			Stream.of("minion_bloodfen_raptor", "minion_chillwind_yeti")
					.map(CardCatalogue::getCardById)
					.forEach(card -> context.getLogic().receiveCard(player.getId(), card));

			Stream.of("minion_bloodfen_raptor", "minion_chillwind_yeti")
					.map(CardCatalogue::getCardById)
					.forEach(card -> context.getLogic().receiveCard(opponent.getId(), card));

			Minion nagaSeaWitch = playMinionCard(context, player, "minion_naga_sea_witch");
			Assert.assertTrue(player.getHand().stream().allMatch(c -> costOf(context, player, c) == 5));
			context.endTurn();
			Assert.assertFalse(opponent.getHand().stream().anyMatch(c -> costOf(context, opponent, c) == 5));
			playCard(context, opponent, "spell_fireball", nagaSeaWitch);
			context.endTurn();
			Assert.assertFalse(player.getHand().stream().anyMatch(c -> costOf(context, player, c) == 5));
		});

		// Test cards with their own modifiers
		runGym((context, player, opponent) -> {
			Map<String, Card> cards = Stream.of("minion_arcane_giant", /*Costs (1) less for each spell you've cast this game.*/
					"minion_clockwork_giant", /*Costs (1) less Mana for each card in your opponent's hand.*/
					"minion_frost_giant", /*Costs (1) less for each time you used your Hero Power this game.*/
					"minion_molten_giant",/*Costs (1) less for each damage your hero has taken.*/
					"minion_mountain_giant", /*Costs (1) less for each other card in your hand."*/
					"minion_sea_giant", /*Costs (1) less for each other minion on the battlefield.*/
					"minion_snowfury_giant" /*Costs (1) less for each Mana Crystal you've Overloaded this game.*/)
					.map(CardCatalogue::getCardById)
					.peek(card -> context.getLogic().receiveCard(player.getId(), card))
					.collect(Collectors.toMap(Card::getCardId, Function.identity()));
			int numberOfSpellsPlayed = 0;
			playCard(context, player, "spell_the_coin");
			numberOfSpellsPlayed++;
			assertEquals(costOf(context, player, cards.get("minion_arcane_giant")), 12 - numberOfSpellsPlayed);

			receiveCard(context, opponent, "spell_the_coin");
			assertEquals(costOf(context, player, cards.get("minion_clockwork_giant")), 12 - 1);

			context.performAction(player.getId(), player.getHero().getHeroPower().play().withTargetReference(opponent.getHero().getReference()));
			assertEquals(costOf(context, player, cards.get("minion_frost_giant")), 10 - 1);

			playCard(context, player, "spell_fireball", player.getHero());
			numberOfSpellsPlayed++;
			assertEquals(costOf(context, player, cards.get("minion_molten_giant")), 20 - 6);

			assertEquals(costOf(context, player, cards.get("minion_mountain_giant")), 12 - player.getHand().size() + 1);

			playCard(context, player, "minion_bloodfen_raptor");
			assertEquals(costOf(context, player, cards.get("minion_sea_giant")), 10 - 1);

			playCard(context, player, "spell_lightning_storm" /*Overloads 2*/);
			numberOfSpellsPlayed++;
			assertEquals(costOf(context, player, cards.get("minion_snowfury_giant")), 11 - 2);

			playCard(context, player, "minion_naga_sea_witch");

			assertEquals(costOf(context, player, cards.get("minion_arcane_giant")), 5 - numberOfSpellsPlayed);
			assertEquals(costOf(context, player, cards.get("minion_clockwork_giant")), 5 - 1);
			assertEquals(costOf(context, player, cards.get("minion_frost_giant")), 5 - 1);
			assertEquals(costOf(context, player, cards.get("minion_molten_giant")), Math.max(5 - 6, 0));
			assertEquals(costOf(context, player, cards.get("minion_mountain_giant")), Math.max(5 - player.getHand().size() + 1, 0));
			assertEquals(costOf(context, player, cards.get("minion_sea_giant")), 5 - 2);
			assertEquals(costOf(context, player, cards.get("minion_snowfury_giant")), 5 - 2);
		});
	}

	@Test(description =
			"Tests Sir Finley Mrrgglton and also confirms that players can do stuff to discovered cards besides receive them.")
	public void testSirFinleyMrrgglton() {
		GameContext context = createContext(HeroClass.WHITE, HeroClass.WHITE);
		Player player = context.getActivePlayer();
		int oldId = player.getHero().getHeroPower().getId();
		final DiscoverAction[] action = {null};
		final Card[] discoveryCard = new Card[1];
		final int[] handSize = new int[1];
		// Set up a trick to catch the discover action.
		context.setBehaviour(player.getId(), new TestBehaviour() {
			boolean shouldIntercept = true;

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				if (shouldIntercept) {
					Assert.assertTrue(validActions.stream()
							.allMatch(ga -> ga.getActionType() == ActionType.DISCOVER));
					action[0] = (DiscoverAction) validActions.get(0);
					Card original = action[0].getCard();
					discoveryCard[0] = original;
					handSize[0] = player.getHand().size();
				}
				// We should only intercept once
				shouldIntercept = false;
				return super.requestAction(context, player, validActions);
			}
		});
		Card sirFinley = CardCatalogue.getCardById("minion_sir_finley_mrrgglton");
		playCard(context, player, sirFinley);
		// Control flow will first go to request action above, then proceed.
		assertEquals(player.getHand().size(), handSize[0],
				"Nothing should be added to the hand.");
		assertEquals(player.getDiscoverZone().size(), 0,
				"The discover zone should be empty");
		assertEquals(player.getGraveyard().size(), 1,
				"The graveyard should only Sir Finley's source card.");
		assertEquals(discoveryCard[0].getZone(), Zones.REMOVED_FROM_PLAY,
				"The discovered card should be removed from play");
		Card currentHeroPower = player.getHeroPowerZone().get(0);
		assertEquals(discoveryCard[0].getCardId(), currentHeroPower.getCardId(),
				"But the hero power card should be the discovered hero power.");
		Assert.assertNotEquals(currentHeroPower.getId(), oldId,
				"The old hero power should not be the current one");
	}

	@Test
	public void testSummoningStone() {
		GameContext context = createContext(HeroClass.BLACK, HeroClass.RED);
		Player player = context.getPlayer1();

		playCard(context, player, "minion_summoning_stone");
		playCard(context, player, "spell_preparation");
		playCard(context, player, "secret_ice_block");

		assertEquals(player.getMinions().size(), 3);
		for (Minion minion : player.getMinions()) {
			if (minion.getSourceCard().getCardId().equalsIgnoreCase("minion_summoning_stone")) {
				continue;
			}

			assertEquals(minion.getSourceCard().getBaseManaCost(), 0);
		}
	}

	@Test
	public void testCurseOfRafaam() {
		runGym((context, player, opponent) -> {
			// Prevent fatigue damage
			shuffleToDeck(context, player, "spell_the_coin");
			shuffleToDeck(context, opponent, "spell_the_coin");
			shuffleToDeck(context, opponent, "spell_the_coin");
			Card koboldGeomancerCard = CardCatalogue.getCardById("minion_kobold_geomancer");
			playCard(context, player, koboldGeomancerCard);
			context.endTurn();
			Card curseOfRafaamCard = CardCatalogue.getCardById("spell_curse_of_rafaam");
			playCard(context, opponent, curseOfRafaamCard);
			context.endTurn();
			final int CURSE_OF_RAFAAM_DAMAGE = 2;
			// first player should take exactly 2 damage (NOT 3, because the spell
			// damage should not be applied)
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - CURSE_OF_RAFAAM_DAMAGE);
		});
	}

	@Test
	public void testNewBrannBronzebeard() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_brann_bronzebeard");
			playCard(context, player, "minion_saronite_chain_gang");
			assertEquals(player.getMinions().size(), 4);
			playCard(context, player, "hero_hagatha_the_witch");
			assertEquals(player.getMinions().size(), 0);
		});

	}
}
