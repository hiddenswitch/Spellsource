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
import net.demilich.metastone.game.actions.PlaySpellCardAction;
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
			context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_wrath"));
			Card card = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().summon(opponent.getId(), card.summon(), card, 0, false);
			assertTrue("The player has Wrath", player.getHand().stream().anyMatch(c -> c.getCardId().equals("spell_wrath")));
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
			Card wrath = CardCatalogue.getCardById("spell_wrath");
			context.getLogic().receiveCard(player.getId(), wrath);
			assertTrue("The player has Wrath", player.getHand().stream().anyMatch(c -> c.getCardId().equals("spell_wrath")));
			assertTrue("The player has Wrath", player.getHand().stream().anyMatch(c -> c.getCardId().equals("spell_wrath")));
			if (context.getActivePlayerId() != player.getId()) {
				context.endTurn();
				context.startTurn(player.getId());
			}
			player.setMana(10);
			List<GameAction> validActions = context.getValidActions();
			GameActions clientActions = Games.getClientActions(context, validActions, 0);
			assertEquals(0, clientActions.getChooseOnes().size());
			Entity clientWrath2 = Games.getEntity(context, wrath, player.getId());
			Assert.assertFalse(clientWrath2.getState().isPlayable());
			GameState state = Games.getGameState(context, player, opponent);
			Entity clientWrath = state.getEntities().stream().filter(e -> e.getId() == wrath.getId()).findFirst().get();
			Assert.assertFalse(clientWrath.getState().isPlayable());
		});

	}

	@Test
	public void testQuestFiresDelivered() {
		runGym((context, player, opponent) -> {
			Card cavernsCard = CardCatalogue.getCardById("quest_the_caverns_below");
			context.getLogic().receiveCard(0, cavernsCard);
			context.getLogic().performGameAction(0, cavernsCard.play());
			assertEquals("quest_the_caverns_below", player.getQuests().get(0).getSourceCard().getCardId());
			Card bloodfen1 = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(0, bloodfen1);
			context.getLogic().performGameAction(0, bloodfen1.play());
			bloodfen1 = CardCatalogue.getCardById("minion_bloodfen_raptor");
			context.getLogic().receiveCard(0, bloodfen1);
			context.getLogic().performGameAction(0, bloodfen1.play());
			assertEquals(2, player.getQuests().get(0).getFires());
			GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			Assert.assertTrue(state.getEntities().stream().anyMatch(e -> e.getEntityType() == Entity.EntityTypeEnum.QUEST && e.getState().getFires() == 2));
			Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
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
			Card kelesethCard = CardCatalogue.getCardById("minion_prince_keleseth");
			context.getLogic().receiveCard(0, kelesethCard);
			context.getLogic().performGameAction(0, kelesethCard.play());
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(3, neutralMinion.getAttack() + neutralMinion.getBonusAttack());
			context.getLogic().endOfSequence();
			context = context.clone();
			GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			Entity entity = state.getEntities().stream().filter(e -> "minion_cost_three_test".equals(e.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(3L, (long) entity.getState().getAttack());
			assertEquals(3L, (long) entity.getState().getHp());
		});

		runGym((context, player, opponent) -> {
			Card neutralMinion = CardCatalogue.getCardById("minion_cost_three_test");
			context.getLogic().shuffleToDeck(player, neutralMinion);
			Card kelesethCard = CardCatalogue.getCardById("minion_prince_keleseth");
			context.getLogic().receiveCard(0, kelesethCard);
			context.getLogic().performGameAction(0, kelesethCard.play());
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(3, neutralMinion.getAttack() + neutralMinion.getBonusAttack());
			context.getLogic().endOfSequence();
			context.getLogic().performGameAction(player.getId(), neutralMinion.play());
			PlayCardAction roll = context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_roll"), player, true).play();
			roll.setTarget(player.getMinions().get(1));
			context.getLogic().performGameAction(player.getId(), roll);
			context = context.clone();
			GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			Entity entity = state.getEntities().stream().filter(e -> e.getState().getLocation().getZone() == EntityLocation.ZoneEnum.HAND && "minion_cost_three_test".equals(e.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(3L, (long) entity.getState().getAttack());
			assertEquals(3L, (long) entity.getState().getHp());
		});
	}

	private void runGym(GymConsumer consume) {
		CardCatalogue.loadCardsFromPackage();
		GameContext context = GameContext.uninitialized(HeroClass.BLACK, HeroClass.BLACK);
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
