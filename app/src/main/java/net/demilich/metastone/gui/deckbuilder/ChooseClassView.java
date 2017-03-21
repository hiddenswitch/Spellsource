package net.demilich.metastone.gui.deckbuilder;

import java.io.IOException;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.MetaStone;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.MetaDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public class ChooseClassView extends BorderPane implements EventHandler<ActionEvent> {
	@FXML
	private Button warriorButton;

	@FXML
	private Button paladinButton;

	@FXML
	private Button druidButton;

	@FXML
	private Button rogueButton;

	@FXML
	private Button warlockButton;

	@FXML
	private Button hunterButton;

	@FXML
	private Button shamanButton;

	@FXML
	private Button mageButton;

	@FXML
	private Button priestButton;

	@FXML
	private Button collectionButton;

	@FXML
	private CheckBox arbitraryCheckBox;

	private boolean arbitrary;

	public ChooseClassView() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChooseClassView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		arbitrary = false;

		setupArbitraryBox();

		warriorButton.setOnAction(this);
		paladinButton.setOnAction(this);
		druidButton.setOnAction(this);

		rogueButton.setOnAction(this);
		warlockButton.setOnAction(this);
		hunterButton.setOnAction(this);

		shamanButton.setOnAction(this);
		mageButton.setOnAction(this);
		priestButton.setOnAction(this);
		collectionButton.setDisable(true);

		collectionButton.setOnAction(this);
	}

	@Override
	public void handle(ActionEvent event) {
		Deck newDeck = null;
		HeroClass heroClass = null;

		if (event.getSource() == warriorButton) {
			heroClass = HeroClass.WARRIOR;
		} else if (event.getSource() == paladinButton) {
			heroClass = HeroClass.PALADIN;
		} else if (event.getSource() == druidButton) {
			heroClass = HeroClass.DRUID;
		} else if (event.getSource() == rogueButton) {
			heroClass = HeroClass.ROGUE;
		} else if (event.getSource() == warlockButton) {
			heroClass = HeroClass.WARLOCK;
		} else if (event.getSource() == hunterButton) {
			heroClass = HeroClass.HUNTER;
		} else if (event.getSource() == shamanButton) {
			heroClass = HeroClass.SHAMAN;
		} else if (event.getSource() == mageButton) {
			heroClass = HeroClass.MAGE;
		} else if (event.getSource() == priestButton) {
			heroClass = HeroClass.PRIEST;
		} else if (event.getSource() == collectionButton) {
			newDeck = new MetaDeck();
		}

		NotificationProxy.sendNotification(GameNotification.NEW_DECK_CLASS_SELECTED, heroClass);
		NotificationProxy.sendNotification(GameNotification.EDIT_DECK);
	}

	private void onArbitraryBoxChanged(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
		arbitrary = newValue;
//		deckProxy = (DeckProxy) getFacade().retrieveProxy(DeckProxy.SINGLE_PLAYER_NAME);
//		deckProxy.setActiveDeckValidator(new ArbitraryDeckValidator());
	}

	private void setupArbitraryBox() {
		arbitraryCheckBox.selectedProperty().addListener(this::onArbitraryBoxChanged);
	}
}
