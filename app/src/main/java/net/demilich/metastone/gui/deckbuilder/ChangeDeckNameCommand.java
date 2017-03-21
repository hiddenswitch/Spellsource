package net.demilich.metastone.gui.deckbuilder;

import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;

public class ChangeDeckNameCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);

		String newDeckName = (String) notification.getBody();
		deckEditor.setDeckName(newDeckName);
	}

}
