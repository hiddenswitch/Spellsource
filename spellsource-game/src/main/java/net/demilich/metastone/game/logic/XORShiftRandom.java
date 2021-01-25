package net.demilich.metastone.game.logic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A non-thread-safe random number generator that uses the "XOR Shift" pattern to produce numbers.
 * <p>
 * This instance is serializable and cloneable. It can be used to reproducibly create sequences of random numbers.
 */
@JsonSerialize(using = XORShiftRandom.XORShiftRandomSerializer.class)
@JsonDeserialize(using = XORShiftRandom.XORShiftRandomDeserializer.class)
public class XORShiftRandom extends Random implements Serializable, Cloneable {
	static final long serialVersionUID = 401645908935123052L;
	private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);
	private volatile long state;

	public XORShiftRandom(long state) {
		this.state = state;
	}

	/**
	 * Ensures {@link GameLogic} has a valid, unique seed in this JVM instance.
	 * <p>
	 * Adapted from the JVM's implementation of {@link Random}
	 *
	 * @return A {@link Long} corresponding to a unique value useful for "oring" to the {@link System#nanoTime()}.
	 */
	private static long seedUniquifier() {
		for (; ; ) {
			long current = seedUniquifier.get();
			long next = current * 181783497276652981L;
			if (seedUniquifier.compareAndSet(current, next))
				return next;
		}
	}

	/**
	 * Creates a valid, highly probably unique seed.
	 *
	 * @return A {@link Long} seed that can be passed to a constructor of {@link Random}
	 */
	public static long createSeed() {
		return seedUniquifier() ^ System.nanoTime();
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

	public XORShiftRandom setState(long state) {
		this.state = state;
		return this;
	}

	public static class XORShiftRandomSerializer extends StdSerializer<XORShiftRandom> {

		protected XORShiftRandomSerializer() {
			super(XORShiftRandom.class);
		}

		@Override
		public void serialize(XORShiftRandom value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeNumber(value.getState());
		}
	}

	public static class XORShiftRandomDeserializer extends StdDeserializer<XORShiftRandom> {

		protected XORShiftRandomDeserializer() {
			super(XORShiftRandom.class);
		}

		@Override
		public XORShiftRandom deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			long state = p.getLongValue();
			if (state == 0) {
				throw ctxt.weirdNumberException(state, XORShiftRandom.class, "invalid state");
			}
			return new XORShiftRandom(state);
		}
	}
}