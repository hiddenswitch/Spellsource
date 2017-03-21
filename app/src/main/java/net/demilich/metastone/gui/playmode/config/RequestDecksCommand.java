package net.demilich.metastone.gui.playmode.config;

import java.util.List;

import net.demilich.metastone.gui.deckbuilder.DeckEditor;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.decks.Deck;

public class RequestDecksCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);

		getFacade().sendNotification(GameNotification.LOAD_DECKS);

		List<Deck> decks = deckEditor.getDecks();
		getFacade().sendNotification(GameNotification.REPLY_DECKS, decks);
	}

}
