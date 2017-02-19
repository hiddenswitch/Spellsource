package net.demilich.metastone.gui.accounts;

import com.hiddenswitch.proto3.net.client.Configuration;
import net.demilich.metastone.GameNotification;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

/**
 * Created by bberman on 2/14/17.
 */
public class SaveLoginTokenCommand extends SimpleCommand<GameNotification> {
	Logger logger = LoggerFactory.getLogger(SaveLoginTokenCommand.class);
	@Override
	public void execute(INotification<GameNotification> iNotification) {
		String loginToken = (String) iNotification.getBody();
		Preferences.userRoot().put("token", loginToken == null ? "" : loginToken);
		Configuration.getDefaultApiClient().setApiKey(loginToken);
		logger.warn("Token saved: {}", loginToken);
	}
}
