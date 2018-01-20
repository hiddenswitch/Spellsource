package com.blizzard.hearthstone;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LeagueOfExplorersTests extends TestBase {
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
			Assert.assertTrue(player.getHand().stream().allMatch(c -> context.getLogic().getModifiedManaCost(player, c) == 5));
			context.endTurn();
			Assert.assertFalse(opponent.getHand().stream().anyMatch(c -> context.getLogic().getModifiedManaCost(opponent, c) == 5));
			playCardWithTarget(context, opponent, "spell_fireball", nagaSeaWitch);
			context.endTurn();
			Assert.assertFalse(player.getHand().stream().anyMatch(c -> context.getLogic().getModifiedManaCost(player, c) == 5));
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
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_arcane_giant")), 12 - numberOfSpellsPlayed);

			receiveCard(context, opponent, "spell_the_coin");
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_clockwork_giant")), 12 - 1);

			context.getLogic().performGameAction(player.getId(), player.getHero().getHeroPower().play().withTargetReference(opponent.getHero().getReference()));
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_frost_giant")), 10 - 1);

			playCardWithTarget(context, player, "spell_fireball", player.getHero());
			numberOfSpellsPlayed++;
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_molten_giant")), 25 - 6);

			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_mountain_giant")), 12 - player.getHand().size() + 1);

			playCard(context, player, "minion_bloodfen_raptor");
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_sea_giant")), 10 - 1);

			playCard(context, player, "spell_lightning_storm" /*Overloads 2*/);
			numberOfSpellsPlayed++;
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_snowfury_giant")), 11 - 2);

			playCard(context, player, "minion_naga_sea_witch");

			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_arcane_giant")), 5 - numberOfSpellsPlayed);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_clockwork_giant")), 5 - 1);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_frost_giant")), 5 - 1);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_molten_giant")), Math.max(5 - 6, 0));
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_mountain_giant")), Math.max(5 - player.getHand().size() + 1, 0));
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_sea_giant")), 5 - 2);
			Assert.assertEquals(context.getLogic().getModifiedManaCost(player, cards.get("minion_snowfury_giant")), 5 - 2);
		});
	}

	@Test(description =
			"Tests Sir Finley Mrrgglton and also confirms that players can do stuff to discovered cards besides receive them.")
	public void testSirFinleyMrrgglton() {
		GameContext context = createContext(HeroClass.WHITE, HeroClass.WHITE);
		Player player = context.getActivePlayer();
		int oldId = player.getHero().getHeroPower().getId();
		final DiscoverAction[] action = {null};
		final HeroPowerCard[] discoveryCard = new HeroPowerCard[1];
		final int[] handSize = new int[1];
		// Set up a trick to catch the discover action.
		player.setBehaviour(new TestBehaviour() {
			boolean shouldIntercept = true;

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				if (shouldIntercept) {
					Assert.assertTrue(validActions.stream()
							.allMatch(ga -> ga.getActionType() == ActionType.DISCOVER));
					action[0] = (DiscoverAction) validActions.get(0);
					HeroPowerCard original = (HeroPowerCard) action[0].getCard();
					discoveryCard[0] = original;
					handSize[0] = player.getHand().size();
				}
				// We should only intercept once
				shouldIntercept = false;
				return super.requestAction(context, player, validActions);
			}
		});
		MinionCard sirFinley = (MinionCard) CardCatalogue.getCardById("minion_sir_finley_mrrgglton");
		playCard(context, player, sirFinley);
		// Control flow will first go to request action above, then proceed.
		Assert.assertEquals(player.getHand().size(), handSize[0],
				"Nothing should be added to the hand.");
		Assert.assertEquals(player.getDiscoverZone().size(), 0,
				"The discover zone should be empty");
		Assert.assertEquals(player.getGraveyard().size(), 1,
				"The graveyard should only Sir Finley's source card.");
		Assert.assertEquals(discoveryCard[0].getZone(), Zones.REMOVED_FROM_PLAY,
				"The discovered card should be removed from play");
		HeroPowerCard currentHeroPower = player.getHeroPowerZone().get(0);
		Assert.assertEquals(discoveryCard[0].getCardId(), currentHeroPower.getCardId(),
				"But the hero power card should be the discovered hero power.");
		Assert.assertNotEquals(currentHeroPower.getId(), oldId,
				"The old hero power should not be the current one");
	}

	@Test
	public void testSummoningStone() {
		GameContext context = createContext(HeroClass.BLACK, HeroClass.RED);
		Player player = context.getPlayer1();

		playCard(context, player, CardCatalogue.getCardById("minion_summoning_stone"));
		playCard(context, player, CardCatalogue.getCardById("spell_preparation"));
		playCard(context, player, CardCatalogue.getCardById("secret_ice_block"));

		Assert.assertEquals(player.getMinions().size(), 3);
		for (Minion minion : player.getMinions()) {
			if (minion.getSourceCard().getCardId().equalsIgnoreCase("minion_summoning_stone")) {
				continue;
			}

			Assert.assertEquals(minion.getSourceCard().getBaseManaCost(), 0);
		}
	}


	@Test
	public void testCurseOfRafaam() {
		GameContext context = createContext(HeroClass.RED, HeroClass.VIOLET);

		Player player = context.getPlayer1();
		Card koboldGeomancerCard = CardCatalogue.getCardById("minion_kobold_geomancer");
		playCard(context, player, koboldGeomancerCard);
		context.endTurn();

		Player opponent = context.getPlayer2();
		Card curseOfRafaamCard = CardCatalogue.getCardById("spell_curse_of_rafaam");
		playCard(context, opponent, curseOfRafaamCard);
		context.endTurn();

		final int CURSE_OF_RAFAAM_DAMAGE = 2;
		// first player should take exactly 2 damage (NOT 3, because the spell
		// damage should not be applied)
		Assert.assertEquals(player.getHero().getHp(), player.getHero().getMaxHp() - CURSE_OF_RAFAAM_DAMAGE);

	}
}
