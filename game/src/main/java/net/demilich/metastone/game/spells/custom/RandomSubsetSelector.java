package net.demilich.metastone.game.spells.custom;

import java.util.Random;
import java.util.function.Predicate;

/**
 * A stateful predicate that, given a total number of items and the number to choose, will return 'true' the chosen
 * number of times distributed randomly across the total number of calls to its test() method.
 */
class RandomSubsetSelector implements Predicate<Object> {
	int total;  // total number items remaining
	int remain; // number of items remaining to select
	Random random;

	RandomSubsetSelector(int total, int remain, Random random) {

		this.total = total;
		this.remain = remain;
		this.random = random;
	}

	@Override
	public synchronized boolean test(Object o) {
		assert total > 0;
		if (random.nextInt(total--) < remain) {
			remain--;
			return true;
		} else {
			return false;
		}
	}
}
