package net.demilich.metastone.game.fibers;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.strands.SuspendableRunnable;
import net.demilich.metastone.game.GameContext;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * An extension of fiber that can schedules itself on a default scheduler and can change the game context objects on its
 * stack.
 */
public class GameContextFiber extends Fiber<Void> implements Serializable {

	private static FiberScheduler DEFAULT_SCHEDULER = new FiberExecutorScheduler("__GameContextFiber", command -> command.run());

	public GameContextFiber(SuspendableRunnable target) {
		super(DEFAULT_SCHEDULER, target);

	}

	public GameContext getGameContext() {
		// Find the first game context instance in this fiber's stack
		try {
			Object[] dataObject = getObjects();

			for (int i = 0; i < dataObject.length; i++) {
				if (dataObject[i] != null && dataObject[i] instanceof GameContext) {
					return (GameContext) dataObject[i];
				}
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		throw new IllegalStateException("dataObject");
	}

	public void setGameContext(GameContext context) {
		try {
			Object[] dataObject = getObjects();

			for (int i = 0; i < dataObject.length; i++) {
				if (dataObject[i] != null && dataObject[i] instanceof GameContext) {
					dataObject[i] = context;
				}
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	protected Object[] getObjects() throws NoSuchFieldException, IllegalAccessException {
		Field thisStackVariable = getClass().getField("stack");
		thisStackVariable.setAccessible(true);
		Stack stack = (Stack) thisStackVariable.get(this);

		Field thisDataObject = Stack.class.getField("dataObject");
		thisDataObject.setAccessible(true);
		return (Object[]) thisDataObject.get(stack);
	}
}
