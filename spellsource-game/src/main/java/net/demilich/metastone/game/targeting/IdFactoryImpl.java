package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.logic.CustomCloneable;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class IdFactoryImpl extends CustomCloneable implements Serializable, IdFactory {

	private AtomicInteger nextId;

	public IdFactoryImpl() {
		nextId = new AtomicInteger(PLAYER_2 + 1);
	}

	public IdFactoryImpl(int resumeId) {
		this.nextId = new AtomicInteger(resumeId);
	}

	@Override
	public IdFactoryImpl clone() {
		return new IdFactoryImpl(nextId.get());
	}

	@Override
	public synchronized int generateId() {
		return nextId.getAndIncrement();
	}

	public int getInternalId() {
		return nextId.get();
	}
}
