package net.demilich.metastone.game.cards.desc;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

/**
 * Indicates the implementing class can be iterated through using its {@link #entrySet()} method.
 *
 * @param <T> A enum representing the keys of this instance
 * @param <V> A value type, which can be Object
 */
public interface HasEntrySet<T extends Enum<T>, V> {
	Set<Map.Entry<T, V>> entrySet();

	default Stream.Builder<BfsNode<Enum, Object>> bfs() {
		Stream.Builder<BfsNode<Enum, Object>> builder = Stream.builder();

		Queue<BfsNode<Enum, Object>> queue = new ArrayDeque<>();
		queue.add(new BfsNode<>(BfsEnum.SELF, this, null, 0));
		while (!queue.isEmpty()) {
			BfsNode<Enum, Object> node = queue.poll();

			Set entrySet;
			if (node.value instanceof HasDesc) {
				entrySet = ((HasDesc) node.value).getDesc().entrySet();
			} else if (node.value instanceof HasEntrySet) {
				entrySet = ((HasEntrySet) node.value).entrySet();
			} else {
				entrySet = null;
			}

			Iterator iterator;
			// Deal with arrays or lists
			if (node.value != null && node.value.getClass().isArray()) {
				iterator = Iterators.forArray((Object[]) node.value);
			} else if (node.value instanceof List) {
				iterator = ((List) node.value).iterator();
			} else {
				iterator = null;
			}

			if (iterator != null) {
				while (iterator.hasNext()) {
					Object res = iterator.next();
					queue.add(new BfsNode<Enum, Object>(node.key, res, node, node.depth));
				}
			}

			if (entrySet != null) {
				for (Object entryUntyped : entrySet) {
					@SuppressWarnings("unchecked")
					Map.Entry<? extends Enum, ?> entry = (Map.Entry<? extends Enum, ?>) entryUntyped;
					queue.add(new BfsNode<>(entry.getKey(), entry.getValue(), node, node.depth + 1));
				}
			}

			builder.add(node);
		}

		return builder;
	}

	class BfsNode<T extends Enum, V> {
		T key;
		V value;
		BfsNode<T, V> parent;
		int depth = 0;
		private transient Stream.Builder<BfsNode<T, V>> predecessors;

		boolean isArrayNode() {
			return value != null && (value.getClass().isArray() || value instanceof List);
		}

		BfsNode(T key, V value, BfsNode<T, V> parent, int depth) {
			this.key = key;
			this.value = value;
			this.parent = parent;
			this.depth = depth;
		}

		public T getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public BfsNode<T, V> getParent() {
			return parent;
		}

		public int getDepth() {
			return depth;
		}

		public Stream<BfsNode<T, V>> predecessors() {
			if (predecessors == null) {
				predecessors = Stream.builder();
				BfsNode<T, V> predecessor = parent;
				while (predecessor != null) {
					predecessors.add(predecessor);
					predecessor = predecessor.parent;
				}
			}

			return predecessors.build();
		}
	}

	enum BfsEnum {
		SELF
	}

	@NotNull
	static <T> T[] link(T single, T[] multi, Class<? extends T> tClass) {
		if (single == null && (multi == null || multi.length == 0)) {
			Object o = Array.newInstance(tClass, 0);
			@SuppressWarnings("unchecked")
			T[] ts = (T[]) o;
			return ts;
		}
		if (single != null && (multi == null || multi.length == 0)) {
			@SuppressWarnings("unchecked")
			T[] out = (T[]) Array.newInstance(tClass, 1);
			out[0] = single;
			return out;
		}
		return multi;
	}
}
