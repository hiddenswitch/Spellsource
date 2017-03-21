package net.demilich.metastone.gui.deckbuilder;

import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.validation.ArbitraryDeckValidator;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;

public class SetActiveDeckCommand extends SimpleCommand<GameNotification> {

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);
		Deck activeDeck = (Deck) notification.getBody();
		if (activeDeck.isArbitrary()) {
			deckEditor.setActiveDeckValidator(new ArbitraryDeckValidator());
		} else {
			deckEditor.setActiveDeckValidator(new DefaultDeckValidator());
		}
		deckEditor.setActiveDeck(activeDeck);

		getFacade().sendNotification(GameNotification.EDIT_DECK, activeDeck);
		getFacade().sendNotification(GameNotification.FILTERED_CARDS, deckEditor.getCards(activeDeck.getHeroClass()));
		getFacade().sendNotification(GameNotification.ACTIVE_DECK_CHANGED, activeDeck);
	}

}
