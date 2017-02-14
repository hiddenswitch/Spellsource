package net.demilich.metastone.gui.accounts;

import com.hiddenswitch.minionate.tasks.ApiTask;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.auth.ApiKeyAuth;
import com.hiddenswitch.proto3.net.client.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.client.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.client.models.LoginRequest;
import com.hiddenswitch.proto3.net.client.models.LoginResponse;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import org.apache.commons.validator.routines.EmailValidator;

import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Created by bberman on 2/13/17.
 */
public class AccountsView extends BorderPane implements EventHandler<ActionEvent> {
	@FXML
	protected TextField usernameField;

	@FXML
	protected TextField emailField;

	@FXML
	protected PasswordField passwordField;

	@FXML
	protected Button createOrLoginButton;

	@FXML
	protected Hyperlink switchTypeButton;

	@FXML
	protected Button logoutButton;

	@FXML
	protected Label messageLabel;

	@FXML
	protected Button backButton;

	private LoginInfo loginInfo;

	public AccountsView(LoginInfo loginInfo) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AccountsView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		createOrLoginButton.setOnAction(this);
		switchTypeButton.setOnAction(this);
		logoutButton.setOnAction(this);
		backButton.setOnAction(this);

		injectLoginInfo(loginInfo);
	}

	@Override
	public void handle(ActionEvent event) {
		// Until we have some sort of login info, don't do anything.
		if (loginInfo == null) {
			return;
		}

		if (event.getSource() == createOrLoginButton) {
			final String username = usernameField.getText();
			final String email = emailField.getText();
			final String password = passwordField.getText();

			String loginToken = null;
			String error = null;

			if (loginInfo.isCreate()) {
				if (!validateUsername(username)
						|| !validateEmail(email)
						|| !validatePassword(password)) {
					return;
				}

				// Everything seems valid!
				ApiTask<CreateAccountResponse> createAccount = new ApiTask<CreateAccountResponse>(api -> api
						.createAccount(new CreateAccountRequest().email(email).name(username).password(password)));

				createAccount.blockingDialogExecute("Accounts", "Creating the requested account...");

				if (createAccount.getError() != null) {
					error = createAccount.getError().getMessage();
				} else {
					loginToken = createAccount.getResult().getLoginToken();
				}
			} else {
				if (!validateEmail(email)
						|| !validatePassword(password)) {
					return;
				}

				ApiTask<LoginResponse> login = new ApiTask<LoginResponse>(api -> api
						.login(new LoginRequest().email(email).password(password)));

				login.blockingDialogExecute("Accounts", "Logging in...");

				if (login.getError() != null) {
					error = login.getError().getMessage();
				} else {
					loginToken = login.getResult().getLoginToken();
				}
			}

			if (error != null) {
				messageLabel.setText("Error: " + error);
			} else {
				NotificationProxy.sendNotification(GameNotification.SAVE_LOGIN_INFO, loginToken);
			}
		} else if (event.getSource() == logoutButton) {
			NotificationProxy.sendNotification(GameNotification.SAVE_LOGIN_INFO, null);
		} else if (event.getSource() == switchTypeButton) {
			injectLoginInfo(new LoginInfo().withCreate(!this.loginInfo.isCreate()).withToken(this.loginInfo.getToken()));
		} else if (event.getSource() == backButton) {
			NotificationProxy.sendNotification(GameNotification.MAIN_MENU);
		}
	}

	private boolean validatePassword(String password) {
		if (password.isEmpty()) {
			messageLabel.setText("The password is empty.");
			return false;
		} else if (password.length() < 6) {
			messageLabel.setText("You must use a password at least 6 characters long.");
			return false;
		}
		return true;
	}

	private boolean validateEmail(String email) {
		if (email.isEmpty()) {
			messageLabel.setText("The email is empty.");
			return false;
		} else if (!(EmailValidator.getInstance().isValid(email))) {
			messageLabel.setText("The provided email is invalid.");
			return false;
		}
		return true;
	}

	private boolean validateUsername(String username) {
		if (username.isEmpty()) {
			messageLabel.setText("The username is empty.");
			return false;
		} else if (username.length() < 3) {
			messageLabel.setText("The username must be at least 3 characters long.");
			return false;
		} else if (username.length() > 25) {
			messageLabel.setText("The username must be less than 26 characters long.");
			return false;
		}

		return true;
	}

	public void injectLoginInfo(LoginInfo info) {
		this.loginInfo = info;

		final boolean loggedIn = info.getToken() != null && !info.getToken().isEmpty();
		final boolean creatingAccount = info.isCreate();

		emailField.setEditable(!loggedIn);
		passwordField.setEditable(!loggedIn);
		createOrLoginButton.setDisable(loggedIn);
		logoutButton.setDisable(!loggedIn);
		switchTypeButton.setDisable(loggedIn);
		usernameField.setDisable(loggedIn || !creatingAccount);

		if (creatingAccount) {
			switchTypeButton.setText("Already have an account?");
			createOrLoginButton.setText("Create Account");
		} else {
			switchTypeButton.setText("Create an account instead.");
			createOrLoginButton.setText("Login");
		}
	}
}
