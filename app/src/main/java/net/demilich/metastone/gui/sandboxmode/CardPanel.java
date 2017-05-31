package net.demilich.metastone.gui.sandboxmode;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.logic.GameLogic;

public class CardPanel extends VBox {

	@FXML
	private Button editHandButton;

	@FXML
	private Button editDeckButton;

	private Player selectedPlayer;

	public CardPanel() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/CardPanel.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		editHandButton.setOnAction(this::handleEditHandButton);
		editDeckButton.setOnAction(this::handleEditDeckButton);
	}

	private void handleEditDeckButton(ActionEvent actionEvent) {
		CardList deck = selectedPlayer.getDeck();
		CardCollectionEditor cardCollectionEditor = new CardCollectionEditor("Edit deck", deck, this::onDeckFinishedEditing,
				GameLogic.MAX_DECK_SIZE);
		NotificationProxy.sendNotification(GameNotification.SHOW_MODAL_DIALOG, cardCollectionEditor);
	}

	private void handleEditHandButton(ActionEvent actionEvent) {
		CardList hand = selectedPlayer.getHand();
		CardCollectionEditor cardCollectionEditor = new CardCollectionEditor("Edit hand", hand, this::onHandFinishedEditing,
				GameLogic.MAX_HAND_CARDS);
		NotificationProxy.sendNotification(GameNotification.SHOW_MODAL_DIALOG, cardCollectionEditor);
	}

	private void onDeckFinishedEditing(CardList cardList) {
		NotificationProxy.sendNotification(GameNotification.MODIFY_PLAYER_DECK, cardList);
	}

	private void onHandFinishedEditing(CardList cardList) {
		NotificationProxy.sendNotification(GameNotification.MODIFY_PLAYER_HAND, cardList);
	}

	public void onPlayerSelectionChanged(Player selectedPlayer) {
		this.selectedPlayer = selectedPlayer;
		editHandButton.setDisable(selectedPlayer == null);
		editDeckButton.setDisable(selectedPlayer == null);
	}

}
