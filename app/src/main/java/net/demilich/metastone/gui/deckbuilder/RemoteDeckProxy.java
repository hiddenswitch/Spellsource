package net.demilich.metastone.gui.deckbuilder;

import com.hiddenswitch.minionate.Client;
import com.hiddenswitch.minionate.tasks.ApiTask;
import com.hiddenswitch.proto3.net.client.models.DecksPutResponse;
import com.hiddenswitch.proto3.net.client.models.InventoryCollection;
import javafx.collections.ListChangeListener;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckCatalogue;
import net.demilich.metastone.game.decks.DeckWithId;
import net.demilich.metastone.game.decks.validation.IDeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.nittygrittymvc.Proxy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by bberman on 3/17/17.
 */
public class RemoteDeckProxy extends Proxy<GameNotification> implements DeckEditor, ListChangeListener<InventoryCollection> {
	private String activeDeckId;

	public RemoteDeckProxy() {
		super(NAME);
		Client.getInstance().getDecks().addListener(this);
	}

	@Override
	public boolean addCardToDeck(Card card) {
		Client.getInstance().addCardToDeck(activeDeckId, card.getCardInventoryId())
				.blockingDialogExecute("Add Card", "Adding card to deck...");
		return true;
	}

	@Override
	public Deck getActiveDeck() {
		final Optional<InventoryCollection> activeDeck = Client.getInstance().getDecks().stream().filter(d -> d.getId().equals(activeDeckId)).findFirst();
		if (!activeDeck.isPresent()) {
			return null;
		}
		return Client.getInstance().parseDeck(activeDeck.get());
	}

	@Override
	public List<Card> getCards(HeroClass heroClass) {
		return Client.getInstance()
				.getCardRecords()
				.stream()
				.map(Client.getInstance()::parseCard)
				.filter(c -> c.getHeroClass().equals(heroClass))
				.collect(Collectors.toList());
	}

	@Override
	public List<Deck> getDecks() {
		return Client.getInstance()
				.getDecks()
				.stream()
				.map(Client.getInstance()::parseDeck)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteDeck(Deck deck) {
		Client.getInstance().deleteDeck(((DeckWithId) deck).getDeckId())
				.blockingDialogExecute("Delete Deck", "Deleting deck...");
		this.getFacade().sendNotification(GameNotification.DECKS_LOADED, Client.getInstance().getDecks().stream().map(Client.getInstance()::parseDeck).collect(Collectors.toList()));
	}

	@Override
	public void loadDecks() throws IOException, URISyntaxException {
		// Do nothing, since we've already loaded the decks from the account ostensibly.
	}

	@Override
	public boolean nameAvailable(Deck deck) {
		return true;
	}

	@Override
	public void removeCardFromDeck(Card card) {
		Client.getInstance().removeCardFromDeck(activeDeckId, card.getCardInventoryId())
				.blockingDialogExecute("Remove Card", "Removing card from deck...");
	}

	@Override
	public void saveActiveDeck() {
		// Do nothing, since decks are modified without saving
	}

	@Override
	public void setActiveDeck(Deck activeDeck) {
		this.activeDeckId = ((DeckWithId) activeDeck).getDeckId();
	}

	@Override
	public void setActiveDeckValidator(IDeckValidator deckValidator) {
		// Don't do deck validation for now
	}

	@Override
	public void setDeckName(String newDeckName) {
		Client.getInstance().renameDeck(activeDeckId, newDeckName)
				.blockingDialogExecute("Renaming Deck", "Renaming deck...");
	}

	@Override
	public void createDeck(HeroClass heroClass) {
		ApiTask<DecksPutResponse> response = Client.getInstance().createDeck(heroClass);
		response.blockingDialogExecute("Creating Deck", "Creating deck...");
		final String deckId = response.getValue().getDeckId();
		final Optional<InventoryCollection> first = Client.getInstance().getDecks().stream().filter(d -> d.getId().equals(deckId)).findFirst();
		if (!first.isPresent()) {
			return;
		}
		setActiveDeck(Client.getInstance().parseDeck(first.get()));

	}

	@Override
	public void onChanged(Change<? extends InventoryCollection> c) {
		// TODO: Do we need this?
	}
}
