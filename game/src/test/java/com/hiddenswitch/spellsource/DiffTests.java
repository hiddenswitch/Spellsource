package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.util.DiffContext;
import com.hiddenswitch.spellsource.util.DiffSequence;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DiffTests {
	@Test
	public void testDiffSequence() {
		testBothWays(Arrays.asList("a"), Arrays.asList());
		testBothWays(Arrays.asList("a"), Arrays.asList("A"));
		testBothWays(Arrays.asList("a", "b", "c"), Arrays.asList("c", "b", "a"));
		testBothWays(Arrays.asList("a", "b", "c"), Collections.emptyList());
		testBothWays(Arrays.asList("a", "b", "c"), Arrays.asList("e", "f"));
		testBothWays(Arrays.asList("a", "b", "c", "d"), Arrays.asList("c", "b", "a"));
		testBothWays(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'),
				Arrays.asList('A', 'B', 'F', 'G', 'C', 'D', 'I', 'L', 'M', 'N', 'H'));
		testBothWays(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'), Arrays.asList('A', 'B', 'C', 'D', 'F', 'G', 'H', 'E', 'I'));

	}

	<M extends Comparable<M>> void testMutation(List<M> a, List<M> b) {
		List<JsonObject> aa = makeDocs(a);
		List<JsonObject> bb = makeDocs(b);
		List<JsonObject> aaCopy = aa.stream().map(obj -> new JsonObject(obj.getMap())).collect(Collectors.toList());
		TestDiffContext1<M> context = new TestDiffContext1<>(aaCopy);
		DiffSequence.diffQueryOrderedChanges(aa, bb, context);
		Assert.assertEquals(context.getResult(), bb);

	}

	<M extends Comparable<M>> void testBothWays(List<M> a, List<M> b) {
		testMutation(a, b);
		testMutation(b, a);
	}

	<M extends Comparable<M>> List<JsonObject> makeDocs(List<M> ids) {
		return ids.stream().map(id -> new JsonObject().put("_id", id)).collect(Collectors.toList());
	}

	class TestDiffContext1<K extends Comparable<K>> implements DiffContext<JsonObject, K> {
		private final List<JsonObject> aaCopy;

		public TestDiffContext1(List<JsonObject> aaCopy) {
			this.aaCopy = aaCopy;
		}

		@Override
		public void removed(K id) {
			JsonObject found = null;
			for (int i = 0; i < aaCopy.size(); i++) {
				if (getKeyer().apply(aaCopy.get(i)).equals(id)) {
					found = aaCopy.remove(i);
				}
			}
		}

		@Override
		public void addedBefore(K newDocId, JsonObject newDoc, K beforeId) {
			if (beforeId == null) {
				aaCopy.add(newDoc);
				return;
			}

			for (int i = 0; i < aaCopy.size(); i++) {
				if (getKeyer().apply(aaCopy.get(i)).equals(beforeId)) {
					aaCopy.add(i, newDoc);
					return;
				}
			}
		}

		@Override
		public void added(K newDocId, JsonObject newDoc) {
		}

		@Override
		public void possiblyChanged(K newDocId, JsonObject oldDoc, JsonObject newDoc) {
		}

		@Override
		public void movedBefore(K id, @Nullable K beforeId) {
			JsonObject found = null;
			for (int i = 0; i < aaCopy.size(); i++) {
				if (getKeyer().apply(aaCopy.get(i)).equals(id)) {
					found = aaCopy.remove(i);
				}
			}

			if (beforeId == null) {
				aaCopy.add(found);
				return;
			}

			for (int i = 0; i < aaCopy.size(); i++) {
				if (getKeyer().apply(aaCopy.get(i)).equals(beforeId)) {
					aaCopy.add(i, found);
					return;
				}
			}
		}

		@Override
		public Function<JsonObject, K> getKeyer() {
			return (obj) -> (K) obj.getMap().get("_id");
		}

		public List<JsonObject> getResult() {
			return aaCopy;
		}
	}
}
