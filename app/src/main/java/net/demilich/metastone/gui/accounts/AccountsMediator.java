package net.demilich.metastone.gui.accounts;

import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.auth.ApiKeyAuth;
import net.demilich.metastone.GameNotification;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.nittygrittymvc.Mediator;
import net.demilich.nittygrittymvc.interfaces.INotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bberman on 2/13/17.
 */
public class AccountsMediator extends Mediator<GameNotification> {
	public static String getToken() {
		return ((ApiKeyAuth) Configuration.getDefaultApiClient().getAuthentication("TokenSecurity")).getApiKey();
	}

	public static final String NAME = "AccountsMediator";
	private final AccountsView view;

	public AccountsMediator() {
		super(NAME);
		view = new AccountsView(new LoginInfo().withCreate(true).withToken(getToken()));
	}


	@Override
	public void handleNotification(final INotification<GameNotification> notification) {
		switch (notification.getId()) {
			case SAVE_LOGIN_INFO:
				String loginToken = (String) notification.getBody();
				view.injectLoginInfo(new LoginInfo().withToken(loginToken).withCreate(false));
				break;
			default:
				break;
		}
	}

	@Override
	public List<GameNotification> listNotificationInterests() {
		List<GameNotification> notificationInterests = new ArrayList<GameNotification>();
		notificationInterests.add(GameNotification.SAVE_LOGIN_INFO);
		return notificationInterests;
	}

	@Override
	public void onRegister() {
		getFacade().sendNotification(GameNotification.SHOW_VIEW, view);
	}
}
