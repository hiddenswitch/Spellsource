package net.demilich.metastone.gui.deckbuilder;

import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.cards.Card;

public class RemoveCardFromDeckCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);
		Card card = (Card) notification.getBody();
		deckEditor.removeCardFromDeck(card);

		getFacade().sendNotification(GameNotification.ACTIVE_DECK_CHANGED, deckEditor.getActiveDeck());
	}

}
