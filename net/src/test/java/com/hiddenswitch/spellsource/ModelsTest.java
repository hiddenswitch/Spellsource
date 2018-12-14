package com.hiddenswitch.spellsource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hiddenswitch.spellsource.client.models.Entity;
import com.hiddenswitch.spellsource.client.models.GameActions;
import com.hiddenswitch.spellsource.client.models.GameState;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.behaviour.ChooseLastBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;
import java.util.Objects;

@RunWith(BlockJUnit4ClassRunner.class)
public class ModelsTest {

	@Test
	public void testChooseOnesDelivered() {
		CardCatalogue.loadCardsFromPackage();
		GameContext context = GameContext.uninitialized(HeroClass.BLACK, HeroClass.BLACK);
		context.setLogic(new GameLogic(1010101L));
		context.setBehaviour(0, new ChooseLastBehaviour());
		context.setBehaviour(1, new ChooseLastBehaviour());
		context.init(0);
		context.startTurn(0);
		context.getLogic().receiveCard(0, CardCatalogue.getCardById("spell_wrath"));
		Card card = CardCatalogue.getCardById("minion_bloodfen_raptor");
		context.getLogic().summon(1, card.summon(), card, 0, false);
		Assert.assertEquals("The player has Wrath (first card is The Coin)", "spell_wrath", context.getPlayer1().getHand().get(0).getSourceCard().getCardId());
		context.getPlayer1().setMana(10);
		List<GameAction> validActions = context.getValidActions();
		GameActions clientActions = Games.getClientActions(context, validActions, 0);
		Assert.assertEquals(0, clientActions.getSpells().size());
		Assert.assertEquals(1, clientActions.getChooseOnes().size());
	}

	@Test
	public void testChooseOneDeliveredNotPlayable() {
		CardCatalogue.loadCardsFromPackage();
		GameContext context = GameContext.uninitialized(HeroClass.BLACK, HeroClass.BLACK);
		context.setLogic(new GameLogic(1010101L));
		context.setBehaviour(0, new ChooseLastBehaviour());
		context.setBehaviour(1, new ChooseLastBehaviour());
		context.init(0);
		context.startTurn(0);
		Card wrath = CardCatalogue.getCardById("spell_wrath");
		context.getLogic().receiveCard(0, wrath);
		Assert.assertEquals("The player has Wrath (first card is The Coin)", "spell_wrath", context.getPlayer1().getHand().get(0).getSourceCard().getCardId());
		context.getPlayer1().setMana(10);
		List<GameAction> validActions = context.getValidActions();
		GameActions clientActions = Games.getClientActions(context, validActions, 0);
		Assert.assertEquals(2, validActions.size());
		Assert.assertEquals(0, clientActions.getChooseOnes().size());
		Assert.assertEquals(0, clientActions.getSpells().size());
		Entity clientWrath2 = Games.getEntity(context, wrath, 0);
		Assert.assertFalse(clientWrath2.getState().isPlayable());
		GameState state = Games.getGameState(context, context.getPlayer1(), context.getPlayer2());
		Entity clientWrath = state.getEntities().stream().filter(e -> e.getId() == wrath.getId()).findFirst().get();
		Assert.assertFalse(clientWrath.getState().isPlayable());
	}

	@Test
	public void testQuestFiresDelivered() {
		CardCatalogue.loadCardsFromPackage();
		GameContext context = GameContext.uninitialized(HeroClass.BLACK, HeroClass.BLACK);
		Player player = context.getPlayer1();
		context.setLogic(new GameLogic(101010L));
		context.setBehaviour(0, new ChooseLastBehaviour());
		context.setBehaviour(1, new ChooseLastBehaviour());
		context.setActivePlayerId(0);
		context.init();
		context.startTurn(0);
		Card cavernsCard = CardCatalogue.getCardById("quest_the_caverns_below");
		context.getLogic().receiveCard(0, cavernsCard);
		context.getLogic().performGameAction(0, cavernsCard.play());
		Assert.assertEquals("quest_the_caverns_below", player.getQuests().get(0).getSourceCard().getCardId());
		Card bloodfen1 = CardCatalogue.getCardById("minion_bloodfen_raptor");
		context.getLogic().receiveCard(0, bloodfen1);
		context.getLogic().performGameAction(0, bloodfen1.play());
		bloodfen1 = CardCatalogue.getCardById("minion_bloodfen_raptor");
		context.getLogic().receiveCard(0, bloodfen1);
		context.getLogic().performGameAction(0, bloodfen1.play());
		Assert.assertEquals(2, player.getQuests().get(0).getFires());
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
	}
}
