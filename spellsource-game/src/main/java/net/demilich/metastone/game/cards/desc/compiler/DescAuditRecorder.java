package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.cards.desc.Desc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lightweight runtime instrumentation recorder for {@link Desc} argument reads, parent-to-child inspections,
 * and dynamic runtime mutations/injections during gameplay auditing.
 * <p>
 * Disabled by default ({@link #ENABLED} = {@code false}), incurring zero overhead in normal execution.
 */
public final class DescAuditRecorder {
	public static volatile boolean ENABLED = false;

	public record EvalFrame(Class<?> componentClass, Desc<?, ?> desc) {
	}

	private static final class ThreadState {
		final Deque<EvalFrame> stack = new ArrayDeque<>(16);
		int internalDepth = 0;
		boolean inHook = false;
	}

	private static final ThreadLocal<ThreadState> STATE = ThreadLocal.withInitial(ThreadState::new);

	private static final Map<String, Set<String>> RUNTIME_SELF_READS = new ConcurrentHashMap<>();
	private static final Map<String, Set<String>> RUNTIME_PARENT_READS_CHILD = new ConcurrentHashMap<>();
	private static final Map<String, Set<String>> RUNTIME_DYNAMIC_MUTATIONS = new ConcurrentHashMap<>();
	private static final Map<String, LongAdder> RUNTIME_INVOCATIONS = new ConcurrentHashMap<>();

	private DescAuditRecorder() {
	}

	public static void reset() {
		RUNTIME_SELF_READS.clear();
		RUNTIME_PARENT_READS_CHILD.clear();
		RUNTIME_DYNAMIC_MUTATIONS.clear();
		RUNTIME_INVOCATIONS.clear();
	}

	public static Map<String, Set<String>> getRuntimeSelfReads() {
		return RUNTIME_SELF_READS;
	}

	public static Map<String, Set<String>> getRuntimeParentReadsChild() {
		return RUNTIME_PARENT_READS_CHILD;
	}

	public static Map<String, Set<String>> getRuntimeDynamicMutations() {
		return RUNTIME_DYNAMIC_MUTATIONS;
	}

	public static Map<String, LongAdder> getRuntimeInvocations() {
		return RUNTIME_INVOCATIONS;
	}

	public static void push(Class<?> componentClass, Desc<?, ?> desc) {
		if (!ENABLED || componentClass == null) {
			return;
		}
		ThreadState st = STATE.get();
		st.stack.push(new EvalFrame(componentClass, desc));
		RUNTIME_INVOCATIONS.computeIfAbsent(componentClass.getName(), k -> new LongAdder()).increment();
	}

	public static void pop() {
		if (!ENABLED) {
			return;
		}
		ThreadState st = STATE.get();
		if (!st.stack.isEmpty()) {
			st.stack.pop();
		}
	}

	public static void enterInternalCopy() {
		if (!ENABLED) {
			return;
		}
		STATE.get().internalDepth++;
	}

	public static void exitInternalCopy() {
		if (!ENABLED) {
			return;
		}
		ThreadState st = STATE.get();
		if (st.internalDepth > 0) {
			st.internalDepth--;
		}
	}

	public static void onGet(Desc<?, ?> desc, Object key) {
		if (!ENABLED || !(key instanceof Enum<?> enumKey)) {
			return;
		}
		ThreadState st = STATE.get();
		if (st.inHook || st.internalDepth > 0) {
			return;
		}
		st.inHook = true;
		try {
			if (enumKey == desc.getClassArg()) {
				return;
			}
			Class<?> descClass = desc.getDescClass();
			String keyName = enumKey.name();
			EvalFrame top = st.stack.peek();
			if (top != null) {
				if (top.desc() == desc || top.componentClass() == descClass) {
					RUNTIME_SELF_READS
							.computeIfAbsent(top.componentClass().getName(), k -> ConcurrentHashMap.newKeySet())
							.add(keyName);
				} else if (top.desc() != null && top.desc().getClass() == desc.getClass()) {
					// Same Desc type (e.g., Parent Spell reading Child SpellDesc)
					String childName = descClass != null ? descClass.getName() : desc.getClass().getSimpleName();
					String rel = top.componentClass().getName() + " -> " + childName;
					RUNTIME_PARENT_READS_CHILD
							.computeIfAbsent(rel, k -> ConcurrentHashMap.newKeySet())
							.add(keyName);
				} else {
					// Different Desc type: could be e.g. BuffSpell reading ModifyBuffSpellAura's AuraDesc,
					// or an un-pushed Aura/CardCostModifier reading its own Desc.
					if (descClass != null) {
						RUNTIME_SELF_READS
								.computeIfAbsent(descClass.getName(), k -> ConcurrentHashMap.newKeySet())
								.add(keyName);
						if (top.componentClass() != descClass) {
							String rel = top.componentClass().getName() + " -> " + descClass.getName();
							RUNTIME_PARENT_READS_CHILD
									.computeIfAbsent(rel, k -> ConcurrentHashMap.newKeySet())
									.add(keyName);
						}
					}
				}
			} else if (descClass != null) {
				RUNTIME_SELF_READS
						.computeIfAbsent(descClass.getName(), k -> ConcurrentHashMap.newKeySet())
						.add(keyName);
			}
		} finally {
			st.inHook = false;
		}
	}

	public static void onPut(Desc<?, ?> desc, Object key, Object value) {
		if (!ENABLED || !(key instanceof Enum<?> enumKey)) {
			return;
		}
		ThreadState st = STATE.get();
		if (st.inHook || st.internalDepth > 0) {
			return;
		}
		st.inHook = true;
		try {
			if (enumKey == desc.getClassArg()) {
				return;
			}
			EvalFrame top = st.stack.peek();
			if (top != null) {
				Class<?> targetClass = desc.getDescClass();
				String targetName = targetClass != null ? targetClass.getName() : desc.getClass().getSimpleName();
				String rel = top.componentClass().getName() + " -> " + targetName;
				RUNTIME_DYNAMIC_MUTATIONS
						.computeIfAbsent(rel, k -> ConcurrentHashMap.newKeySet())
						.add(enumKey.name());
			}
		} finally {
			st.inHook = false;
		}
	}
}
