package com.hiddenswitch.spellsource.common;

import net.demilich.metastone.game.shared.GameNotification;
import net.demilich.metastone.game.shared.INotification;
import net.demilich.metastone.game.shared.INotifier;

public class NullNotifier implements INotifier<GameNotification> {
	@Override
	public void notifyObservers(INotification<GameNotification> iNotification) {
	}

	@Override
	public void sendNotification(GameNotification gameNotification) {
	}

	@Override
	public void sendNotification(GameNotification gameNotification, Object o) {
	}
}
