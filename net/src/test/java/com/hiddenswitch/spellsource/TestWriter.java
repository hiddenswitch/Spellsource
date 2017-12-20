package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.common.Writer;
import com.hiddenswitch.spellsource.common.GameState;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.utils.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.Notification;

import java.util.List;

public class TestWriter implements Writer {
	@Override
	public void onNotification(Notification event, GameState gameState) {

	}

	@Override
	public void onGameEnd(Player winner) {

	}

	@Override
	public void setPlayers(Player localPlayer, Player remotePlayer) {

	}

	@Override
	public void onActivePlayer(Player activePlayer) {

	}

	@Override
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {

	}

	@Override
	public void onUpdate(GameState state) {

	}

	@Override
	public void onRequestAction(String messageId, GameState state, List<GameAction> actions) {

	}

	@Override
	public void onMulligan(String messageId, GameState state, List<Card> cards, int playerId) {

	}

	@Override
	public void close() {

	}

	@Override
	public Object getPrivateSocket() {
		return null;
	}

	@Override
	public void lastEvent() {

	}
}
