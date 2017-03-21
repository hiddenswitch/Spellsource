package net.demilich.metastone.gui.deckbuilder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;

public class FillDeckWithRandomCardsCommand extends SimpleCommand<GameNotification> {

	private static Logger logger = LoggerFactory.getLogger(FillDeckWithRandomCardsCommand.class);

	@Override
	public void execute(INotification<GameNotification> notification) {
		DeckEditor deckEditor = (DeckEditor) getFacade().retrieveProxy(DeckEditor.NAME);

		List<Card> cards = deckEditor.getCards(deckEditor.getActiveDeck().getHeroClass());
		if (deckEditor.getActiveDeck().isTooBig()) {
			while (!deckEditor.getActiveDeck().isComplete()) {
				Card randomCard = deckEditor.getActiveDeck().getCards().getRandom();
				deckEditor.removeCardFromDeck(randomCard);
				logger.debug("Removing card {} to deck.", randomCard);
			}
		} else {
			while (!deckEditor.getActiveDeck().isComplete()) {
				Card randomCard = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
				if (deckEditor.addCardToDeck(randomCard)) {
					logger.debug("Adding card {} to deck.", randomCard);
				}
			}
		}
		getFacade().sendNotification(GameNotification.ACTIVE_DECK_CHANGED, deckEditor.getActiveDeck());
	}

}
