package net.demilich.metastone.gui.deckbuilder.metadeck;

import net.demilich.metastone.gui.deckbuilder.DeckEditor;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.MetaDeck;

public class RemoveDeckFromMetaDeckCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);
		MetaDeck metaDeck = (MetaDeck) deckEditor.getActiveDeck();

		Deck deck = (Deck) notification.getBody();
		if (!metaDeck.getDecks().contains(deck)) {
			return;
		}

		metaDeck.getDecks().remove(deck);
		getFacade().sendNotification(GameNotification.ACTIVE_DECK_CHANGED, deckEditor.getActiveDeck());

	}

}
