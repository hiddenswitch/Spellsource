package com.hiddenswitch.spellsource.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Helps compute differences in sequences.
 */
public interface DiffSequence {
	Logger logger = LoggerFactory.getLogger(DiffSequence.class);

	/**
	 * Computes a correct sequence of added, changed, removed and moved commands against a context between two lists. O(N
	 * log N).
	 *
	 * @param oldResults The original sequence.
	 * @param newResults The new sequence.
	 * @param context    A context against which to execute ordered commands.
	 * @param <T>        The document type
	 * @param <K>        The key type.
	 * @return The context.
	 */
	static <T, K extends Comparable<K>> DiffContext<T, K> diffQueryOrderedChanges(List<T> oldResults, List<T> newResults, DiffContext<T, K> context) {
		// Only one context can be sent commands at a time
		synchronized (context.lock()) {
			Function<T, K> keyer = context.getKeyer();
			Set<Comparable> newPresenceOfId = new HashSet<>();
			for (T doc : newResults) {
				final Comparable<?> key = keyer.apply(doc);
				if (newPresenceOfId.contains(key)) {
					logger.debug("diffQueryOrderedChanges: Duplicate id {} in new results", key);
				}
				newPresenceOfId.add(key);
			}

			Map<Comparable, Integer> oldIndexOfId = new HashMap<>();
			for (int i = 0; i < oldResults.size(); i++) {
				T doc = oldResults.get(i);
				final Comparable<?> key = keyer.apply(doc);
				if (oldIndexOfId.containsKey(key)) {
					logger.debug("diffQueryOrderedChanges: Duplicate id {} in old results", key);
				}
				oldIndexOfId.put(key, i);
			}

			List<Integer> unmoved = new ArrayList<>();
			int maxSeqLen = 0;
			int N = newResults.size();
			int[] seqEnds = new int[N];
			int[] ptrs = new int[N];
			Function<Integer, Integer> oldIdxSeq = (i) -> oldIndexOfId.get(keyer.apply(newResults.get(i)));
			for (int i = 0; i < N; i++) {
				if (oldIndexOfId.containsKey(keyer.apply(newResults.get(i)))) {
					int j = maxSeqLen;
					while (j > 0) {
						if (oldIdxSeq.apply(seqEnds[j - 1]) < oldIdxSeq.apply(i)) {
							break;
						}
						j--;
					}

					ptrs[i] = (j == 0 ? -1 : seqEnds[j - 1]);
					seqEnds[j] = i;
					if (j + 1 > maxSeqLen) {
						maxSeqLen = j + 1;
					}
				}
			}

			int idx = maxSeqLen == 0 ? -1 : seqEnds[maxSeqLen - 1];
			while (idx >= 0) {
				unmoved.add(idx);
				idx = ptrs[idx];
			}

			Collections.reverse(unmoved);
			unmoved.add(newResults.size());

			for (T doc : oldResults) {
				final K key = keyer.apply(doc);
				if (!newPresenceOfId.contains(key)) {
					context.removed(key);
				}
			}

			int startOfGroup = 0;
			for (Integer endOfGroup : unmoved) {
				K groupId = (newResults.size() > endOfGroup && newResults.get(endOfGroup) != null)
						? keyer.apply(newResults.get(endOfGroup)) : null;
				T oldDoc;
				T newDoc;

				for (int i = startOfGroup; i < endOfGroup; i++) {
					newDoc = newResults.get(i);
					final K newDocId = keyer.apply(newDoc);
					if (!oldIndexOfId.containsKey(newDocId)) {
						context.addedBefore(newDocId, newDoc, groupId);
						context.added(newDocId, newDoc);
					} else {
						oldDoc = oldResults.get(oldIndexOfId.get(newDocId));
						// TODO: Perform actual comparison here
						context.possiblyChanged(newDocId, oldDoc, newDoc);
						context.movedBefore(newDocId, groupId);
					}
				}
				if (groupId != null) {
					newDoc = newResults.get(endOfGroup);
					final K newDocId = keyer.apply(newDoc);
					oldDoc = oldResults.get(oldIndexOfId.get(newDocId));
					context.possiblyChanged(newDocId, oldDoc, newDoc);
				}
				startOfGroup = endOfGroup + 1;
			}
		}
		return context;
	}
}
