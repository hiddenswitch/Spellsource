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
import com.google.common.collect.Multiset;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardZone;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
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

	/**
	 * Determines who is the first player from a list of player IDs.
	 *
	 * @param playerIds The candidate player IDs.
	 * @return The ID of the player that should go first.
	 */
	public int determineBeginner(int... playerIds) {
		return nextBoolean() ? playerIds[0] : playerIds[1];
	}

	/**
	 * Chooses a random item from a list of options without removing it.
	 *
	 * @param options The list of options.
	 * @param <T>     The item type.
	 * @return A randomly chosen item, or {@code null} if the list is empty.
	 */
	public <T> T choose(List<T> options) {
		if (options == null || options.isEmpty()) {
			return null;
		}
		return options.get(nextInt(options.size()));
	}

	/**
	 * Chooses and removes a random item from a list of options.
	 *
	 * @param options The list of options.
	 * @param <T>     The item type.
	 * @return The removed item, or {@code null} if the list is empty.
	 */
	public <T> T remove(List<T> options) {
		if (options == null || options.isEmpty()) {
			return null;
		}
		return options.remove(nextInt(options.size()));
	}

	/**
	 * Chooses and removes a random item from a weighted multiset of options.
	 *
	 * @param weightedOptions The weighted options.
	 * @param <T>             The item type.
	 * @return The removed item, or {@code null} if the multiset is empty.
	 */
	public <T> T removeWeighted(Multiset<T> weightedOptions) {
		if (weightedOptions == null || weightedOptions.isEmpty()) {
			return null;
		}

		var index = nextInt(weightedOptions.size());
		var iterator = weightedOptions.iterator();
		while (index > 0) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			iterator.next();
			index--;
		}
		var item = iterator.next();
		weightedOptions.remove(item);
		return item;
	}

	/**
	 * Shuffles a {@link CardList} in place using this random generator.
	 *
	 * @param cards The card list or card zone to shuffle.
	 */
	public void shuffle(CardList cards) {
		if (cards instanceof CardZone cardZone) {
			cardZone.shuffleInternal(this);
		} else if (cards instanceof CardArrayList cardArrayList) {
			cardArrayList.shuffleInternal(this);
		} else if (cards != null) {
			Collections.shuffle(cards, this);
		}
	}

	/**
	 * Shuffles a generic {@link List} in place using this random generator.
	 *
	 * @param list The list to shuffle.
	 * @param <T>  The element type.
	 */
	public <T> void shuffleList(List<T> list) {
		if (list instanceof CardList cardList) {
			shuffle(cardList);
		} else if (list != null) {
			Collections.shuffle(list, this);
		}
	}

	/**
	 * Selects the next card to draw from a deck.
	 *
	 * @param deck The player's deck.
	 * @return The card to draw, or {@code null} if the deck is empty.
	 */
	public Card draw(CardList deck) {
		return deck == null || deck.isEmpty() ? null : deck.peek();
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