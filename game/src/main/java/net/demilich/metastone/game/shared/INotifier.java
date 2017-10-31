package net.demilich.metastone.game.shared;

public interface INotifier<T> {
	void notifyObservers(INotification<GameNotification> notification);

	void sendNotification(T notification);

	void sendNotification(T notification, Object data);
}
