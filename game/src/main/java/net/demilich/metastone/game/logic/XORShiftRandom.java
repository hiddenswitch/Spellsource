package net.demilich.metastone.game.logic;

import java.io.Serializable;
import java.util.Random;

/**
 * A non-thread-safe random number generator that uses the "XOR Shift" pattern to produce numbers.
 * <p>
 * This instance is serializable and cloneable. It can be used to reproducibly create sequences of random numbers.
 */
public class XORShiftRandom extends Random implements Serializable, Cloneable {
	static final long serialVersionUID = 401645908935123052L;
	private volatile long state;

	public XORShiftRandom(long state) {
		this.state = state;
	}

	protected int next(int nbits) {
		long x = this.state;
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		this.state = x;
		x &= ((1L << nbits) - 1);
		return (int) x;
	}

	@Override
	public XORShiftRandom clone() {
		return new XORShiftRandom(this.state);
	}

	public long getState() {
		return state;
	}
}