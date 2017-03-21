package net.demilich.metastone.gui.deckbuilder;

import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;

public class CreateNewDeckCommand extends SimpleCommand<GameNotification> {
	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);
		HeroClass heroClass = (HeroClass) notification.getBody();
		deckEditor.createDeck(heroClass);
		getFacade().sendNotification(GameNotification.SET_ACTIVE_DECK, deckEditor.getActiveDeck());
	}
}
