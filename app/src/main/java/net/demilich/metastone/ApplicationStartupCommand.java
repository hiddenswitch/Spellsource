package net.demilich.metastone;

import com.hiddenswitch.proto3.net.client.ApiCallback;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.GetAccountsResponse;
import net.demilich.nittygrittymvc.SimpleCommand;
import net.demilich.nittygrittymvc.interfaces.INotification;
import net.demilich.metastone.gui.cards.CardProxy;
import net.demilich.metastone.gui.autoupdate.AutoUpdateMediator;
import net.demilich.metastone.gui.deckbuilder.DeckFormatProxy;
import net.demilich.metastone.gui.deckbuilder.DeckProxy;
import net.demilich.metastone.gui.dialog.DialogMediator;
import net.demilich.metastone.gui.main.ApplicationMediator;
import net.demilich.metastone.gui.playmode.animation.AnimationProxy;
import net.demilich.metastone.gui.sandboxmode.SandboxProxy;
import net.demilich.metastone.gui.trainingmode.TrainingProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class ApplicationStartupCommand extends SimpleCommand<GameNotification> {
	Logger logger = LoggerFactory.getLogger(ApplicationStartupCommand.class);

	@Override
	public void execute(INotification<GameNotification> notification) {
		getFacade().registerMediator(new DialogMediator());

		getFacade().registerProxy(new CardProxy());
		getFacade().registerProxy(new DeckProxy());
		getFacade().registerProxy(new DeckFormatProxy());
		getFacade().registerProxy(new TrainingProxy());
		getFacade().registerProxy(new SandboxProxy());
		getFacade().registerProxy(new AnimationProxy());

		getFacade().registerMediator(new ApplicationMediator());
		getFacade().registerMediator(new AutoUpdateMediator());

		// Load the preferences and set the network API if it exists
		String loginToken = Preferences.userRoot().get("token", "");
		if (!loginToken.isEmpty()) {
			Configuration.getDefaultApiClient().setApiKey(loginToken);
			// Try to use the token. If we get an unauthorized error, we have to "logout"
			DefaultApi api = new DefaultApi();
			final String targetUserId = loginToken.split(":")[0];
			try {
				api.getAccountAsync(targetUserId, new ApiCallback<GetAccountsResponse>() {
					@Override
					public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
						// Logout.
						logger.error("Failed to login with the provided token. Token: {}", loginToken);
						Configuration.getDefaultApiClient().setApiKey(null);
						Preferences.userRoot().put("token", "");
					}

					@Override
					public void onSuccess(GetAccountsResponse result, int statusCode, Map<String, List<String>> responseHeaders) {
						// Great! Maybe populate some basic stuff.
						logger.warn(result.getAccounts().get(0).toString());
					}

					@Override
					public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
					}

					@Override
					public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
					}
				});
			} catch (ApiException serializationException) {
			}
		}
	}

}
