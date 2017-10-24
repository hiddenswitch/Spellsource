package net.demilich.metastone.tests;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.targeting.CardReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class JourneyToUngoroTests extends TestBase {
	@Test
	public void testGalvadon() {
		GameContext context = createContext(HeroClass.PALADIN, HeroClass.PALADIN);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		playCard(context, player, "quest_the_last_kaleidosaur");

		Minion target = playMinionCard(context, player, "minion_bloodfen_raptor");

		for (int i = 0; i < 5; i++) {
			playCardWithTarget(context, player, "spell_adaptation", target);
		}

		// Only spells that target a specific friendly minion will count towards the quest, meaning that randomly
		// targeted and AoE spells such as Smuggler's Run, Competitive Spirit and Avenge will not count.

		playCard(context, player, "spell_savage_roar");
		Assert.assertFalse(player.getHand().containsCard("token_galvadon"));

		context.endTurn();
		Minion opponentTarget = playMinionCard(context, opponent, "minion_bloodfen_raptor");
		playCardWithTarget(context, opponent, "spell_adaptation", opponentTarget);
		Assert.assertFalse(player.getHand().containsCard("token_galvadon"));
		context.endTurn();
		playCardWithTarget(context, player, "spell_adaptation", opponentTarget);
		Assert.assertFalse(player.getHand().containsCard("token_galvadon"));
		playCardWithTarget(context, player, "spell_adaptation", target);
		Assert.assertTrue(player.getHand().containsCard("token_galvadon"));
	}

	@Test()
	public void testTimeWarp() {
		GameContext context = createContext(HeroClass.PALADIN, HeroClass.PALADIN);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		playCard(context, player, "quest_open_the_waygate");
		// TODO: Test stolen cards from the opponent's deck.

		// Didn't start in the deck.
		for (int i = 0; i < 6; i++) {
			playCard(context, player, "spell_arcane_explosion");
		}

		Assert.assertTrue(player.getHand().containsCard("spell_time_warp"));
		// Multiple Time Warps stack - you take that many extra turns in a row.

		playCard(context, player, "spell_time_warp");
		playCard(context, player, "spell_time_warp");
		context.endTurn();
		Assert.assertEquals(context.getActivePlayer(), player);
		context.endTurn();
		Assert.assertEquals(context.getActivePlayer(), player);
		context.endTurn();
		Assert.assertEquals(context.getActivePlayer(), opponent);
	}

	@Test()
	public void testPrimalfinChampion() {
		GameContext context = createContext(HeroClass.PALADIN, HeroClass.PALADIN);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		Minion primalfinChampion = playMinionCard(context, player, "minion_primalfin_champion");
		Minion bloodfenRaptor = playMinionCard(context, player, "minion_bloodfen_raptor");
		playCardWithTarget(context, player, "spell_adaptation", primalfinChampion);
		playCardWithTarget(context, player, "spell_adaptation", primalfinChampion);
		playCardWithTarget(context, player, "spell_bananas", bloodfenRaptor);
		context.endTurn();
		Minion bloodfenRaptor2 = playMinionCard(context, opponent, "minion_bloodfen_raptor");
		playCardWithTarget(context, opponent, "spell_bananas", bloodfenRaptor2);
		playCardWithTarget(context, opponent, "spell_assassinate", primalfinChampion);
		Assert.assertEquals(player.getHand().size(), 2);
		Assert.assertTrue(player.getHand().containsCard("spell_adaptation"));
		Assert.assertFalse(player.getHand().containsCard("spell_bananas"));
	}

	@Test
	public void testTheVoraxx() {
		GameContext context = createContext(HeroClass.MAGE, HeroClass.MAGE);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		Minion voraxx = playMinionCard(context, player, "minion_the_voraxx");
		playCardWithTarget(context, player, "spell_bananas", voraxx);
		Assert.assertEquals(player.getMinions().size(), 2);
		Assert.assertEquals(voraxx.getAttack(), 4, "The Voraxx should have been buffed by 1. ");
		Assert.assertEquals(player.getMinions().get(1).getAttack(), 2, "The plant should be buffed");
	}

	@Test
	public void testSteamSurger() {
		GameContext context = createContext(HeroClass.MAGE, HeroClass.MAGE);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		player.setMaxMana(10);
		player.setMana(10);
		playCard(context, player, "minion_pyros");
		playCard(context, player, "minion_steam_surger");
		Assert.assertFalse(player.getHand().containsCard("spell_flame_geyser"));
		context.endTurn();
		context.endTurn();
		playCard(context, player, "minion_steam_surger");
		Assert.assertTrue(player.getHand().containsCard("spell_flame_geyser"));
		context.endTurn();
		context.endTurn();
		context.endTurn();
		context.endTurn();
		clearHand(context, player);
		playCard(context, player, "minion_steam_surger");
		Assert.assertFalse(player.getHand().containsCard("spell_flame_geyser"));
	}

	@Test
	public void testJungleGiants() {
		GameContext context = createContext(HeroClass.ROGUE, HeroClass.ROGUE);
		Player player = context.getActivePlayer();
		Player opponent = context.getOpponent(player);
		clearHand(context, player);
		clearHand(context, opponent);
		clearZone(context, player.getDeck());
		clearZone(context, opponent.getDeck());

		playCard(context, player, "quest_jungle_giants");
		Assert.assertEquals(player.getQuests().size(), 1);
		player.setMaxMana(10);
		player.setMana(10);
		context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("quest_jungle_giants"));
		Assert.assertFalse(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getCardReference()),
				"Since we already have a quest in play, we should not be able to play another quest.");

		// Play 5 minions with 5 or more attack.
		for (int i = 0; i < 5; i++) {
			Assert.assertFalse(player.getHand().containsCard("token_barnabus_the_stomper"));
			playMinionCard(context, player, "minion_leeroy_jenkins");
		}
		Assert.assertTrue(player.getHand().containsCard("token_barnabus_the_stomper"));
		Assert.assertEquals(player.getQuests().size(), 0);
		player.setMana(1);
		Assert.assertTrue(context.getLogic().canPlayCard(player.getId(), player.getHand().get(0).getCardReference()));
	}

	@Test
	public void testLivingMana() {
		zip(Stream.of(5, 6, 7, 8, 9, 10), Stream.of(5, 6, 7, 7, 7, 7), (mana, maxMinionsSummoned) -> {
			for (int i = 0; i <= 7; i++) {
				GameContext context = createContext(HeroClass.DRUID, HeroClass.DRUID);
				Player player = context.getActivePlayer();
				Player opponent = context.getOpponent(player);
				clearHand(context, player);
				clearHand(context, opponent);
				clearZone(context, player.getDeck());
				clearZone(context, opponent.getDeck());

				for (int j = 0; j < i; j++) {
					playMinionCard(context, player, "minion_wisp");
				}

				player.setMaxMana(mana);
				player.setMana(mana);
				playCard(context, player, "spell_living_mana");
				int minionsOnBoard = Math.min((int) maxMinionsSummoned + i, 7);
				int minionsSummonedByLivingMana = Math.min(7, minionsOnBoard - i);
				Assert.assertEquals(player.getMinions().size(), minionsOnBoard);
				Assert.assertEquals(player.getMaxMana(), mana - minionsSummonedByLivingMana,
						String.format("Prior max mana: %d, prior minions on  board: %d", mana, i));
			}


			return null;
		}).collect(Collectors.toList());
	}

	@Test
	public void testMoltenBladeAndShifterZerus() {
		for (String cardId : new String[]{"weapon_molten_blade", "minion_shifter_zerus"}) {
			GameContext context = createContext(HeroClass.ROGUE, HeroClass.ROGUE);
			Player player = context.getActivePlayer();
			Player opponent = context.getOpponent(player);
			clearHand(context, player);
			clearHand(context, opponent);
			clearZone(context, player.getDeck());
			clearZone(context, opponent.getDeck());

			player.setMana(10);
			player.setMaxMana(10);
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById(cardId));
			int oldId = player.getHand().get(0).getId();
			Assert.assertEquals(player.getHand().get(0).getCardId(), cardId, String.format("%s should not have transformed yet: ", cardId));
			context.endTurn();
			context.endTurn();
			int oldId1 = player.getHand().get(0).getId();
			Assert.assertNotEquals(oldId1, oldId);
			context.endTurn();
			context.endTurn();
			int oldId2 = player.getHand().get(0).getId();
			Assert.assertNotEquals(oldId2, oldId1);
			Card card = player.getHand().get(0);
			context.getLogic().performGameAction(player.getId(), card.play());
			context.endTurn();
			context.endTurn();
			Assert.assertEquals(player.getHand().size(), 0, String.format("%s should have been played as %s, but the size of the hand was: ", cardId, card.getCardId()));
		}

	}

	@Test
	public void testEarthenScales() {
		GameContext context = createContext(HeroClass.DRUID, HeroClass.DRUID);
		Player player = context.getPlayer1();

		playCard(context, player, CardCatalogue.getCardById("token_sapling"));
		Minion sapling = player.getMinions().get(0);
		Assert.assertEquals(sapling.getAttack(), 1);
		playCardWithTarget(context, player, CardCatalogue.getCardById("spell_earthen_scales"), sapling);
		Assert.assertEquals(player.getHero().getArmor(), 2);
	}

	@Test
	public void testBarnabusTheStomper() {
		GameContext context = createContext(HeroClass.DRUID, HeroClass.DRUID);
		Player player = context.getPlayer1();
		clearHand(context, player);
		clearZone(context, player.getDeck());
		context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById("token_sapling"));
		playCard(context, player, CardCatalogue.getCardById("token_barnabus_the_stomper"));
		context.getLogic().drawCard(player.getId(), null);
		Card sapling = player.getHand().get(0);
		Assert.assertEquals(sapling.getCardId(), "token_sapling");
		Assert.assertEquals(context.getLogic().getModifiedManaCost(player, sapling), 0);
	}

	@Test
	public void testManaBind() {
		GameContext context = createContext(HeroClass.MAGE, HeroClass.DRUID);
		Player player = context.getPlayer1();
		clearHand(context, player);
		clearZone(context, player.getDeck());
		Player opponent = context.getPlayer2();
		clearHand(context, opponent);
		clearZone(context, opponent.getDeck());
		playCard(context, player, CardCatalogue.getCardById("secret_mana_bind"));
		context.endTurn();
		playCardWithTarget(context, opponent, CardCatalogue.getCardById("spell_fireball"), player.getHero());
		Card copiedFireball = player.getHand().get(0);
		Assert.assertEquals(copiedFireball.getCardId(), "spell_fireball");
		SpellCard graveyardFireball = (SpellCard) opponent.getGraveyard().get(opponent.getGraveyard().size() - 1);
		Assert.assertEquals(graveyardFireball.getCardId(), "spell_fireball");
		Assert.assertNotEquals(copiedFireball.getId(), graveyardFireball);
		Assert.assertEquals(context.getLogic().getModifiedManaCost(player, copiedFireball), 0);
	}

	@Test
	public void testFreeFromAmber() {
		GameContext context = createContext(HeroClass.PRIEST, HeroClass.PRIEST);
		Player player = context.getActivePlayer();
		final DiscoverAction[] action = {null};
		final Minion[] originalMinion = new Minion[1];
		final int[] handSize = new int[1];
		player.setBehaviour(new TestBehaviour() {
			boolean first = true;

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				if (first) {
					Assert.assertTrue(validActions.stream().allMatch(ga -> ga.getActionType() == ActionType.DISCOVER));
					action[0] = (DiscoverAction) validActions.get(0);
					MinionCard original = (MinionCard) action[0].getCard();
					originalMinion[0] = original.summon();
					handSize[0] = player.getHand().size();
				}
				first = false;
				return super.requestAction(context, player, validActions);
			}
		});
		SpellCard freeFromAmber = (SpellCard) CardCatalogue.getCardById("spell_free_from_amber");
		playCard(context, player, freeFromAmber);
		Assert.assertEquals(player.getHand().size(), handSize[0]);
		Assert.assertEquals(player.getDiscoverZone().size(), 0);
		// TODO: Should the player really receive the card and then summon it?
		Assert.assertEquals(player.getGraveyard().size(), 2, "The graveyard should only contain Free From Amber and the summoned card");
		Assert.assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), originalMinion[0].getSourceCard().getCardId());
	}
}
