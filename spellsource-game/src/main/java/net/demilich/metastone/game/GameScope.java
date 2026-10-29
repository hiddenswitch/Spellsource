package net.demilich.metastone.game;

import net.demilich.metastone.game.cards.desc.compiler.GameStateArena;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.concurrent.Callable;

/**
 * Structured execution scope for {@link GameContext} and its arena-backed environment stacks.
 * <p>
 * Combines JDK {@link ScopedValue} binding ({@link #CURRENT_CONTEXT}) with $O(1)$ stack-pointer mark/restore and
 * allocation-free push/pop helpers on {@link GameStateArena}.
 */
public final class GameScope {
	public static final ScopedValue<GameContext> CURRENT_CONTEXT = ScopedValue.newInstance();

	private GameScope() {
	}

	public static void where(GameContext context, Runnable op) {
		ScopedValue.where(CURRENT_CONTEXT, context).run(op);
	}

	public static <T> T call(GameContext context, Callable<T> op) throws Exception {
		return ScopedValue.where(CURRENT_CONTEXT, context).call(op::call);
	}

	public static GameContext currentContext() {
		if (CURRENT_CONTEXT.isBound()) {
			return CURRENT_CONTEXT.get();
		}
		return GameContext.current();
	}

	/**
	 * Captures all 8 environment stack pointers in a single 64-bit word.
	 */
	public static long mark(GameContext context) {
		return GameStateArena.getContextArena(context).stackTops;
	}

	/**
	 * Restores all 8 environment stack pointers in a single 64-bit store.
	 */
	public static void restore(GameContext context, long mark) {
		GameStateArena.getContextArena(context).stackTops = mark;
	}

	public static void withSpellTarget(GameContext context, EntityReference target, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushRef(GameStateArena.STACK_SPELL_TARGET_REF, target);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_SPELL_TARGET_REF);
		}
	}

	public static void withOutput(GameContext context, EntityReference output, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushRef(GameStateArena.STACK_OUTPUT_REF, output);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_OUTPUT_REF);
		}
	}

	public static void withSpellValue(GameContext context, int value, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushInt(GameStateArena.STACK_SPELL_VALUE, value);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_SPELL_VALUE);
		}
	}

	public static void withEventTarget(GameContext context, EntityReference target, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushRef(GameStateArena.STACK_EVENT_TARGET_REF, target);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_EVENT_TARGET_REF);
		}
	}

	public static void withEventSource(GameContext context, EntityReference source, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushRef(GameStateArena.STACK_EVENT_SOURCE_REF, source);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_EVENT_SOURCE_REF);
		}
	}

	public static void withSummonReference(GameContext context, EntityReference summon, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushRef(GameStateArena.STACK_SUMMON_REF, summon);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_SUMMON_REF);
		}
	}

	public static void withTriggerHost(GameContext context, EntityReference host, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushRef(GameStateArena.STACK_TRIGGER_HOST_REF, host);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_TRIGGER_HOST_REF);
		}
	}

	public static void withEventValue(GameContext context, int value, Runnable body) {
		GameStateArena arena = GameStateArena.getContextArena(context);
		arena.pushInt(GameStateArena.STACK_EVENT_VALUE, value);
		try {
			body.run();
		} finally {
			arena.popInt(GameStateArena.STACK_EVENT_VALUE);
		}
	}
}
