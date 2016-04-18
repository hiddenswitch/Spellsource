package net.demilich.metastone.gui.trainingmode;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.gui.common.DeckStringConverter;

public class TrainingConfigView extends BorderPane {

	@FXML
	private ComboBox<Integer> numberOfGamesBox;
	@FXML
	private ComboBox<Deck> deckBox;

	@FXML
	private ListView<Deck> selectedDecksListView;
	@FXML
	private ListView<Deck> availableDecksListView;

	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Button startButton;
	@FXML
	private Button backButton;

	public TrainingConfigView() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/TrainingConfigView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		setupDeckBox();
		setupNumberOfGamesBox();

		selectedDecksListView.setCellFactory(TextFieldListCell.forListView(new DeckStringConverter()));
		selectedDecksListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		availableDecksListView.setCellFactory(TextFieldListCell.forListView(new DeckStringConverter()));
		availableDecksListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		addButton.setOnAction(this::handleAddButton);
		removeButton.setOnAction(this::handleRemoveButton);

		backButton.setOnAction(event -> NotificationProxy.sendNotification(GameNotification.MAIN_MENU));
		startButton.setOnAction(this::handleStartButton);
	}

	private void handleAddButton(ActionEvent event) {
		Collection<Deck> selectedDecks = availableDecksListView.getSelectionModel().getSelectedItems();
		selectedDecksListView.getItems().addAll(selectedDecks);
		availableDecksListView.getItems().removeAll(selectedDecks);
	}

	private void handleRemoveButton(ActionEvent event) {
		Collection<Deck> selectedDecks = selectedDecksListView.getSelectionModel().getSelectedItems();
		availableDecksListView.getItems().addAll(selectedDecks);
		selectedDecksListView.getItems().removeAll(selectedDecks);
	}

	private void handleStartButton(ActionEvent event) {
		int numberOfGames = numberOfGamesBox.getSelectionModel().getSelectedItem();
		Deck deckToTrain = deckBox.getSelectionModel().getSelectedItem();
		Collection<Deck> decks = selectedDecksListView.getItems();

		TrainingConfig trainingConfig = new TrainingConfig(deckToTrain);
		trainingConfig.setNumberOfGames(numberOfGames);
		trainingConfig.getDecks().addAll(decks);
		NotificationProxy.sendNotification(GameNotification.COMMIT_TRAININGMODE_CONFIG, trainingConfig);
	}

	public void injectDecks(List<Deck> decks) {
		List<Deck> filteredDecks = FXCollections.observableArrayList();
		for (Deck deck : decks) {
			if (deck.getHeroClass() != HeroClass.DECK_COLLECTION) {
				filteredDecks.add(deck);
			}
		}
		selectedDecksListView.getItems().clear();
		availableDecksListView.getItems().setAll(filteredDecks);
		deckBox.getItems().setAll(filteredDecks);
		deckBox.getSelectionModel().selectFirst();
	}

	private void setupDeckBox() {
		deckBox.setConverter(new DeckStringConverter());
	}

	private void setupNumberOfGamesBox() {
		ObservableList<Integer> numberOfGamesEntries = FXCollections.observableArrayList();
		numberOfGamesEntries.add(1);
		numberOfGamesEntries.add(10);
		numberOfGamesEntries.add(100);
		numberOfGamesEntries.add(1000);
		numberOfGamesEntries.add(10000);
		numberOfGamesBox.setItems(numberOfGamesEntries);
		numberOfGamesBox.getSelectionModel().select(2);
	}

	public void injectDeckFormats(List<DeckFormat> body) {
//		this.deckFormats = deckFormats;
//		player1Config.setDeckFormat(deckFormats.get(0));
//		player2Config.setDeckFormat(deckFormats.get(0));
	}

}
