package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.Games;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.behaviour.ChooseLastBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ModelsTest {

	@Test
	public void testQuestFiresDelivered() {
		runGym((context, player, opponent) -> {
			var questCard = CardCatalogue.getCardById("quest_into_the_mines");
			context.getLogic().receiveCard(0, questCard);
			context.performAction(0, questCard.play());
			assertEquals("quest_into_the_mines", player.getQuests().get(0).getSourceCard().getCardId());
			var freeze = CardCatalogue.getCardById("spell_test_freeze");
			context.getLogic().receiveCard(0, freeze);
			context.performAction(0, freeze.play());
			freeze = CardCatalogue.getCardById("spell_test_freeze");
			context.getLogic().receiveCard(0, freeze);
			context.performAction(0, freeze.play());
			assertEquals(2, player.getQuests().get(0).getFires());
			var state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			assertTrue(state.getEntities().stream().anyMatch(e -> e.getEntityType() == EntityType.QUEST && e.getFires() == 2));
			var jsonObject = JsonObject.mapFrom(state);
			assertTrue(jsonObject.getJsonArray("entities").stream().anyMatch(obj -> {
				var jo = (JsonObject) obj;
				return Objects.equals(jo.getString("entityType"), "QUEST")
						&& jo.getInteger("fires") == 2;
			}));
			var json = Json.encode(state);
			assertTrue(json.contains("\"fires\":2"));
		});
	}

	@Test
	public void testHandbuffsAppearCorrectly() {
		runGym((context, player, opponent) -> {
			var neutralMinion = CardCatalogue.getCardById("minion_cost_three_test");
			context.getLogic().shuffleToDeck(player, neutralMinion);
			var buffCard = CardCatalogue.getCardById("minion_test_card_buff");
			context.getLogic().receiveCard(0, buffCard);
			context.performAction(0, buffCard.play());
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(3, neutralMinion.getAttack() + neutralMinion.getBonusAttack());
			context.getLogic().endOfSequence();
			context = context.clone();
			var state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			var entity = state.getEntities().stream().filter(e -> "minion_cost_three_test".equals(e.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(3L, (long) entity.getAttack());
			assertEquals(3L, (long) entity.getHp());
		});

		runGym((context, player, opponent) -> {
			var neutralMinion = CardCatalogue.getCardById("minion_cost_three_test");
			context.getLogic().shuffleToDeck(player, neutralMinion);
			var kelesethCard = CardCatalogue.getCardById("minion_test_card_buff");
			context.getLogic().receiveCard(0, kelesethCard);
			context.performAction(0, kelesethCard.play());
			context.getLogic().drawCard(player.getId(), player);
			assertEquals(neutralMinion.getZone(), Zones.HAND);
			assertEquals(3, neutralMinion.getAttack() + neutralMinion.getBonusAttack());
			context.getLogic().endOfSequence();
			context.performAction(player.getId(), neutralMinion.play());
			var roll = context.getLogic().receiveCard(player.getId(), CardCatalogue.getCardById("spell_roll"), player, true).play();
			roll.setTarget(player.getMinions().get(1));
			context.performAction(player.getId(), roll);
			context = context.clone();
			var state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
			var entity = state.getEntities().stream().filter(e -> e.getL().getZ() == EntityLocation.ZEnum.H && "minion_cost_three_test".equals(e.getCardId())).findFirst().orElseThrow(AssertionError::new);
			assertEquals(3L, (long) entity.getAttack());
			assertEquals(3L, (long) entity.getHp());
		});
	}

	private void runGym(GymConsumer consume) {
		CardCatalogue.loadCardsFromPackage();
		var context = new GameContext("BLACK", "BLACK");
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
