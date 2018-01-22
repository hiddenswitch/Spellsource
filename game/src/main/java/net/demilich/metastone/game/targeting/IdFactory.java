package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.logic.CustomCloneable;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class IdFactory extends CustomCloneable implements Serializable {
	public static final int UNASSIGNED = -1;
	public static final int PLAYER_1 = GameContext.PLAYER_1;
	public static final int PLAYER_2 = GameContext.PLAYER_2;

	private AtomicInteger nextId;

	public IdFactory() {
		nextId = new AtomicInteger(PLAYER_2 + 1);
	}

	public IdFactory(int resumeId) {
		this.nextId = new AtomicInteger(resumeId);
	}

	@Override
	public IdFactory clone() {
		return new IdFactory(nextId.get());
	}

	public synchronized int generateId() {
		return nextId.getAndIncrement();
	}

	public int getInternalId() {
		return nextId.get();
	}
}
