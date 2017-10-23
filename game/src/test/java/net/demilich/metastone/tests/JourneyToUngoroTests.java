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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class JourneyToUngoroTests extends TestBase {
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
