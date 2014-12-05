package net.pferdimanzug.hearthstone.analyzer.gui.trainingmode;

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
import net.pferdimanzug.hearthstone.analyzer.ApplicationFacade;
import net.pferdimanzug.hearthstone.analyzer.GameNotification;
import net.pferdimanzug.hearthstone.analyzer.game.behaviour.IBehaviour;
import net.pferdimanzug.hearthstone.analyzer.game.behaviour.PlayRandomBehaviour;
import net.pferdimanzug.hearthstone.analyzer.game.behaviour.threat.FeatureVector;
import net.pferdimanzug.hearthstone.analyzer.game.behaviour.threat.GameStateValueBehaviour;
import net.pferdimanzug.hearthstone.analyzer.game.decks.Deck;
import net.pferdimanzug.hearthstone.analyzer.gui.common.BehaviourStringConverter;
import net.pferdimanzug.hearthstone.analyzer.gui.common.DeckStringConverter;

public class TrainingConfigView extends BorderPane {
	
	@FXML
	private ComboBox<Integer> numberOfGamesBox;
	@FXML
	private ComboBox<IBehaviour> behaviourBox;

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
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TrainingConfigView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		setupBehaviourBox();
		setupNumberOfGamesBox();

		selectedDecksListView.setCellFactory(TextFieldListCell.forListView(new DeckStringConverter()));
		selectedDecksListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		availableDecksListView.setCellFactory(TextFieldListCell.forListView(new DeckStringConverter()));
		availableDecksListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		addButton.setOnAction(this::handleAddButton);
		removeButton.setOnAction(this::handleRemoveButton);

		backButton.setOnAction(event -> ApplicationFacade.getInstance().sendNotification(GameNotification.MAIN_MENU));
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
		IBehaviour behaviour = behaviourBox.getSelectionModel().getSelectedItem();
		Collection<Deck> decks = selectedDecksListView.getItems();
		
		TrainingConfig trainingConfig = new TrainingConfig(behaviour);
		trainingConfig.setNumberOfGames(numberOfGames);
		trainingConfig.getDecks().addAll(decks);
		ApplicationFacade.getInstance().sendNotification(GameNotification.COMMIT_TRAININGMODE_CONFIG, trainingConfig);
	}

	public void injectDecks(List<Deck> decks) {
		selectedDecksListView.getItems().clear();
		availableDecksListView.getItems().setAll(decks);
	}

	private void setupBehaviourBox() {
		behaviourBox.setConverter(new BehaviourStringConverter());
		behaviourBox.getItems().setAll(new GameStateValueBehaviour(), new GameStateValueBehaviour(FeatureVector.getFittest(), "(fittest)"),
				new PlayRandomBehaviour());
		behaviourBox.getSelectionModel().selectFirst();
	}

	private void setupNumberOfGamesBox() {
		ObservableList<Integer> numberOfGamesEntries = FXCollections.observableArrayList();
		numberOfGamesEntries.add(1);
		numberOfGamesEntries.add(10);
		numberOfGamesEntries.add(100);
		numberOfGamesEntries.add(1000);
		numberOfGamesBox.setItems(numberOfGamesEntries);
		numberOfGamesBox.getSelectionModel().select(2);
	}


}
