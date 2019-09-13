package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hiddenswitch.spellsource.client.models.Entity;
import com.hiddenswitch.spellsource.client.models.EntityLocation;
import com.hiddenswitch.spellsource.client.models.GameActions;
import com.hiddenswitch.spellsource.client.models.GameState;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.behaviour.ChooseLastBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(BlockJUnit4ClassRunner.class)
public class ModelsTest {

	@Test
	public void testChooseOnesDelivered() {
		runGym((context, player, opponent) -> {
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_test_choose_one"));
			Card card = CardCatalogue.getCardById("minion_test_3_2");
			context.getLogic().summon(opponent.getId(), card.summon(), card, 0, false);
			assertTrue("The player has Choose One Card", player.getHand().stream().anyMatch(c -> c.getCardId().equals("spell_test_choose_one")));
			if (context.getActivePlayerId() != player.getId()) {
				context.endTurn();
				context.startTurn(player.getId());
			}
			player.setMana(10);
			List<GameAction> validActions = context.getValidActions();
			GameActions clientActions = Games.getClientActions(context, validActions, 0);
			assertEquals(1, clientActions.getChooseOnes().size());
			assertEquals(2, clientActions.getChooseOnes().get(0).getEntities().size());
		});
	}

	@Test
	public void testChooseOneDeliveredNotPlayable() {
		runGym((context, player, opponent) -> {
			Card chooseOne = CardCatalogue.getCardById("spell_test_choose_one");
			context.getLogic().receiveCard(player.getId(), chooseOne);
			assertTrue("The player has Wrath", player.getHand().stream().anyMatch(c -> c.getCardId().equals("spell_test_choose_one")));
			assertTrue("The player has Wrath", player.getHand().stream().anyMatch(c -> c.getCardId().equals("spell_test_choose_one")));
			if (context.getActivePlayerId() != player.getId()) {
				context.endTurn();
				context.startTurn(player.getId());
			}
			player.setMana(10);
			List<GameAction> validActions = context.getValidActions();
			GameActions clientActions = Games.getClientActions(context, validActions, 0);
			assertEquals(0, clientActions.getChooseOnes().size());
			Entity clientCard = Games.getEntity(context, chooseOne, player.getId());
			Assert.assertFalse(clientCard.isPlayable());
			GameState state = Games.getGameState(context, player, opponent);
			Entity clientCard2 = state.getEntities().stream().filter(e -> e.getId() == chooseOne.getId()).findFirst().get();
			Assert.assertFalse(clientCard2.isPlayable());
		});

	}

	@Test
	public void testQuestFiresDelivered() {
		runGym((context, player, opponent) -> {
			Card questCard = CardCatalogue.getCardById("quest_into_the_mines");
			context.getLogic().receiveCard(0, questCard);
			context.performAction(0, questCard.play());
			assertEquals("quest_into_the_mines", player.getQuests().get(0).getSourceCard().getCardId());
			Card freeze = CardCatalogue.getCardById("spell_test_freeze");
			context.getLogic().receiveCard(0, freeze);
			context.performAction(0, freeze.play());
			freeze = CardCatalogue.getCardById("spell_test_freeze");
			context.getLogic().receiveCard(0, freeze);
			context.performAction(0, freeze.play());
			assertEquals(2, player.getQuests().get(0).getFires());
			GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			Assert.assertTrue(state.getEntities().stream().anyMatch(e -> e.getEntityType() == Entity.EntityTypeEnum.QUEST && e.getFires() == 2));
			JsonObject jsonObject = JsonObject.mapFrom(state);
			Assert.assertTrue(jsonObject.getJsonArray("entities").stream().anyMatch(obj -> {
				JsonObject jo = (JsonObject) obj;
				return Objects.equals(jo.getString("entityType"), "QUEST")
						&& jo.getJsonObject("state").getInteger("fires") == 2;
			}));
			String json = Json.encode(state);
			Assert.assertTrue(json.contains("\"fires\":2"));
		});
	}

	@Test
	public void testHandbuffsAppearCorrectly() {
		runGym((context, player, opponent) -> {
			Card neutralMinion = CardCatalogue.getCardById("minion_cost_three_test");
			context.getLogic().shuffleToDeck(player, neutralMinion);
			Card buffCard = CardCatalogue.getCardById("minion_test_card_buff");
			context.getLogic().receiveCard(0, buffCard);
			context.performAction(0, buffCard.play());
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(3, neutralMinion.getAttack() + neutralMinion.getBonusAttack());
			context.getLogic().endOfSequence();
			context = context.clone();
			GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			Entity entity = state.getEntities().stream().filter(e -> "minion_cost_three_test".equals(e.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(3L, (long) entity.getAttack());
			assertEquals(3L, (long) entity.getHp());
		});

		runGym((context, player, opponent) -> {
			Card neutralMinion = CardCatalogue.getCardById("minion_cost_three_test");
			context.getLogic().shuffleToDeck(player, neutralMinion);
			Card kelesethCard = CardCatalogue.getCardById("minion_test_card_buff");
			context.getLogic().receiveCard(0, kelesethCard);
			context.performAction(0, kelesethCard.play());
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(neutralMinion.getZone(), Zones.HAND);
			assertEquals(3, neutralMinion.getAttack() + neutralMinion.getBonusAttack());
			context.getLogic().endOfSequence();
			context.performAction(player.getId(), neutralMinion.play());
			PlayCardAction roll = context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_roll"), player, true).play();
			roll.setTarget(player.getMinions().get(1));
			context.performAction(player.getId(), roll);
			context = context.clone();
			GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			Entity entity = state.getEntities().stream().filter(e -> e.getL().getZ() == EntityLocation.ZEnum.H && "minion_cost_three_test".equals(e.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(3L, (long) entity.getAttack());
			assertEquals(3L, (long) entity.getHp());
		});
	}

	private void runGym(GymConsumer consume) {
		CardCatalogue.loadCardsFromPackage();
		GameContext context = new GameContext("BLACK", "BLACK");
		context.setLogic(new GameLogic(101010L));
		context.setBehaviour(0, new ChooseLastBehaviour());
		context.setBehaviour(1, new ChooseLastBehaviour());
		context.setActivePlayerId(0);
		context.init();
		context.startTurn(0);
		consume.run(context, context.getPlayer1(), context.getPlayer2());
	}

	@FunctionalInterface
	public interface GymConsumer {
		@Suspendable
		void run(GameContext context, Player player, Player opponent);

		@Suspendable
		default GymConsumer andThen(GymConsumer after) {
			Objects.requireNonNull(after);
			return (c, p, o) -> {
				run(c, p, o);
				after.run(c, p, o);
			};
		}
	}
}
