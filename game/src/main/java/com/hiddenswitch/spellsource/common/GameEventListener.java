package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Emote;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.logic.TurnState;

public interface GameEventListener {
	@Suspendable
	void sendNotification(Notification event, GameState gameState);

	@Suspendable
	void sendGameOver(GameState gameState, Player winner);

	@Suspendable
	void onActivePlayer(Player activePlayer);

	@Suspendable
	void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState);

	@Suspendable
	void onUpdate(GameState state);

	@Suspendable
	void sendEmote(int entityId, Emote.MessageEnum emote);
}
