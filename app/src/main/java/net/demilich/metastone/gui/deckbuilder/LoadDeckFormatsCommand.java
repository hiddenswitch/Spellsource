package net.demilich.metastone.gui.deckbuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;

public class LoadDeckFormatsCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckFormatProxy deckFormatProxy = (DeckFormatProxy) getFacade().retrieveProxy(DeckFormatProxy.NAME);

		try {
			deckFormatProxy.loadDeckFormats();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		getFacade().sendNotification(GameNotification.DECK_FORMATS_LOADED, deckFormatProxy.getDeckFormats());
	}

}
