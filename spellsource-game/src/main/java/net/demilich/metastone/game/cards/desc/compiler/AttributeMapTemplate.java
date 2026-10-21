package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.BaseMap;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.Tier;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Bytecode template for {@link net.demilich.metastone.game.cards.AttributeMap}. Woven into {@code AttributeMap.class}
 * at compile time by {@link SpellsourceClassfileWeaver} so that {@code AttributeMap.java} remains clean in source.
 */
public class AttributeMapTemplate extends BaseMap<Attribute, Object> implements Serializable, Cloneable, HasEntrySet<Attribute, Object> {
	private static final byte KIND_OVERFLOW = 0;
	private static final byte KIND_INT = 1;
	private static final byte KIND_BOOL = 2;
	private static final byte KIND_OBJ = 3;

	private static final Attribute[] INT_SLOT_TO_ATTR;
	private static final Attribute[] BOOL_SLOT_TO_ATTR;
	private static final Attribute[] OBJ_SLOT_TO_ATTR;
	private static final byte[] ATTR_KIND;
	private static final byte[] ATTR_SLOT;
	private static final boolean[] STORES_TURN_NUMBER;
	private static final boolean[] IMMUNE_TO_SILENCE_ARRAY;

	static {
		Attribute[] universe = Attribute.values();
		ATTR_KIND = new byte[universe.length];
		ATTR_SLOT = new byte[universe.length];
		STORES_TURN_NUMBER = new boolean[universe.length];
		IMMUNE_TO_SILENCE_ARRAY = new boolean[universe.length];
		Attribute[] intSlots = new Attribute[32];
		Attribute[] boolSlots = new Attribute[64];
		Attribute[] objSlots = new Attribute[6];
		int intCount = 0;
		int boolCount = 0;
		int objCount = 0;
		for (java.lang.reflect.Field field : Attribute.class.getDeclaredFields()) {
			if (!field.isEnumConstant()) {
				continue;
			}
			Tier tier = field.getAnnotation(Tier.class);
			if (tier == null || tier.value() == Tier.Type.OVERFLOW) {
				continue;
			}
			Attribute attr = Attribute.valueOf(field.getName());
			int ord = attr.ordinal();
			if (tier.value() == Tier.Type.INLINE_INT) {
				if (intCount >= 32) {
					throw new IllegalStateException("Exceeded 32 INLINE_INT @Tier attributes on Attribute: " + attr);
				}
				ATTR_KIND[ord] = KIND_INT;
				ATTR_SLOT[ord] = (byte) intCount;
				intSlots[intCount++] = attr;
			} else if (tier.value() == Tier.Type.INLINE_BOOL) {
				if (boolCount >= 64) {
					throw new IllegalStateException("Exceeded 64 INLINE_BOOL @Tier attributes on Attribute: " + attr);
				}
				ATTR_KIND[ord] = KIND_BOOL;
				ATTR_SLOT[ord] = (byte) boolCount;
				boolSlots[boolCount++] = attr;
			} else if (tier.value() == Tier.Type.INLINE_OBJECT) {
				if (objCount >= 6) {
					throw new IllegalStateException("Exceeded 6 INLINE_OBJECT @Tier attributes on Attribute: " + attr);
				}
				ATTR_KIND[ord] = KIND_OBJ;
				ATTR_SLOT[ord] = (byte) objCount;
				objSlots[objCount++] = attr;
			}
		}
		INT_SLOT_TO_ATTR = java.util.Arrays.copyOf(intSlots, intCount);
		BOOL_SLOT_TO_ATTR = java.util.Arrays.copyOf(boolSlots, boolCount);
		OBJ_SLOT_TO_ATTR = java.util.Arrays.copyOf(objSlots, objCount);
		for (Attribute a : Attribute.getStoresTurnNumberAttributes()) {
			STORES_TURN_NUMBER[a.ordinal()] = true;
		}
		for (Attribute a : Attribute.getAuraAttributes()) {
			IMMUNE_TO_SILENCE_ARRAY[a.ordinal()] = true;
		}
		Attribute[] immune = new Attribute[]{
				Attribute.HP, Attribute.MAX_HP, Attribute.BASE_HP, Attribute.BASE_ATTACK,
				Attribute.SUMMONING_SICKNESS, Attribute.RACE, Attribute.DESTROYED, Attribute.NUMBER_OF_ATTACKS,
				Attribute.PERMANENT, Attribute.COPIED_FROM, Attribute.TRANSFORM_REFERENCE, Attribute.DAMAGE_THIS_TURN,
				Attribute.HERO_CLASS, Attribute.STARTED_IN_DECK, Attribute.USER_ID, Attribute.LAST_HIT,
				Attribute.LAST_HEAL, Attribute.NAME, Attribute.DESCRIPTION, Attribute.DRAINED_LAST_TURN,
				Attribute.LAST_MINION_DESTROYED_CARD_ID, Attribute.LAST_MINION_DESTROYED_INVENTORY_ID,
				Attribute.RESERVED_BOOLEAN_1, Attribute.RESERVED_BOOLEAN_2, Attribute.RESERVED_BOOLEAN_3,
				Attribute.RESERVED_BOOLEAN_4, Attribute.RESERVED_BOOLEAN_5, Attribute.RESERVED_INTEGER_1,
				Attribute.RESERVED_INTEGER_2, Attribute.RESERVED_INTEGER_3, Attribute.RESERVED_INTEGER_4,
				Attribute.RESERVED_INTEGER_5, Attribute.CARD_INVENTORY_ID, Attribute.TREANT, Attribute.LACKEY
		};
		for (Attribute a : immune) {
			IMMUNE_TO_SILENCE_ARRAY[a.ordinal()] = true;
		}
		END_OF_TURN_INT_MASK = (1 << ATTR_SLOT[Attribute.HEALING_THIS_TURN.ordinal()])
				| (1 << ATTR_SLOT[Attribute.DAMAGE_THIS_TURN.ordinal()])
				| (1 << ATTR_SLOT[Attribute.EXCESS_HEALING_THIS_TURN.ordinal()]);
		byte auraCardIdSlot = ATTR_SLOT[Attribute.AURA_CARD_ID.ordinal()];
		byte cardIdSlot = ATTR_SLOT[Attribute.CARD_ID.ordinal()];
		AURA_CARD_ID_SLOT = auraCardIdSlot;
		CARD_ID_SLOT = cardIdSlot;
		OVERRIDE_CARD_ID_OBJ_MASK = (1 << auraCardIdSlot) | (1 << cardIdSlot);
		byte heroClassSlot = ATTR_SLOT[Attribute.HERO_CLASS.ordinal()];
		HERO_CLASS_SLOT = heroClassSlot;
		HERO_CLASS_OBJ_MASK = 1 << heroClassSlot;
	}

	private static final int END_OF_TURN_INT_MASK;
	private static final byte AURA_CARD_ID_SLOT;
	private static final byte CARD_ID_SLOT;
	private static final int OVERRIDE_CARD_ID_OBJ_MASK;
	private static final byte HERO_CLASS_SLOT;
	private static final int HERO_CLASS_OBJ_MASK;

	public final void clearEndOfTurnAttributes() {
		presentInts &= ~END_OF_TURN_INT_MASK;
	}

	public final String getOverrideCardIdFast() {
		if ((presentObjs & OVERRIDE_CARD_ID_OBJ_MASK) == 0) {
			return null;
		}
		if ((presentObjs & (1 << AURA_CARD_ID_SLOT)) != 0) {
			Object v = getObjSlot(AURA_CARD_ID_SLOT);
			if (v != null) {
				return (String) v;
			}
		}
		return (presentObjs & (1 << CARD_ID_SLOT)) != 0 ? (String) getObjSlot(CARD_ID_SLOT) : null;
	}

	public final String getInlineHeroClass() {
		return (presentObjs & HERO_CLASS_OBJ_MASK) != 0 ? (String) getObjSlot(HERO_CLASS_SLOT) : null;
	}

	public static boolean hasHeroClassFast(Card card, String heroClass) {
		if (Objects.equals(heroClass, HeroClass.INHERIT)) {
			return true;
		}
		CardDesc d = card.getDesc();
		String singleClass = ((AttributeMapTemplate) (Object) card.getAttributes()).getInlineHeroClass();
		if (singleClass == null) {
			AttributeMapTemplate descAttrs = (AttributeMapTemplate) (Object) d.getAttributes();
			if (descAttrs != null) {
				singleClass = descAttrs.getInlineHeroClass();
			}
			if (singleClass == null) {
				singleClass = d.getHeroClass();
			}
		}
		if (heroClass.equals(singleClass)) {
			return true;
		}
		String[] descClasses = d.getHeroClasses();
		if (descClasses != null) {
			for (String h : descClasses) {
				if (heroClass.equals(h)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isStoresTurnNumber(Attribute attr) {
		return STORES_TURN_NUMBER[attr.ordinal()];
	}

	public static boolean isImmuneToSilence(Attribute attr) {
		return IMMUNE_TO_SILENCE_ARRAY[attr.ordinal()];
	}

	private int presentInts;
	private long presentBools;
	private long boolValues;
	private byte presentObjs;
	private int i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15;
	private int i16, i17, i18, i19, i20, i21, i22, i23, i24, i25, i26, i27, i28, i29, i30, i31;
	private Object o0, o1, o2, o3, o4, o5;
	private transient Set<Entry<Attribute, Object>> attributeEntrySet;

	public AttributeMapTemplate() {
		super(Attribute.class);
	}

	public AttributeMapTemplate(Map<Attribute, Object> attributes) {
		super(Attribute.class);
		if (attributes != null && !attributes.isEmpty()) {
			putAll(attributes);
		}
	}

	private int getIntSlot(int slot) {
		return switch (slot) {
			case 0 -> i0;
			case 1 -> i1;
			case 2 -> i2;
			case 3 -> i3;
			case 4 -> i4;
			case 5 -> i5;
			case 6 -> i6;
			case 7 -> i7;
			case 8 -> i8;
			case 9 -> i9;
			case 10 -> i10;
			case 11 -> i11;
			case 12 -> i12;
			case 13 -> i13;
			case 14 -> i14;
			case 15 -> i15;
			case 16 -> i16;
			case 17 -> i17;
			case 18 -> i18;
			case 19 -> i19;
			case 20 -> i20;
			case 21 -> i21;
			case 22 -> i22;
			case 23 -> i23;
			case 24 -> i24;
			case 25 -> i25;
			case 26 -> i26;
			case 27 -> i27;
			case 28 -> i28;
			case 29 -> i29;
			case 30 -> i30;
			default -> i31;
		};
	}

	private void setIntSlot(int slot, int val) {
		switch (slot) {
			case 0 -> i0 = val;
			case 1 -> i1 = val;
			case 2 -> i2 = val;
			case 3 -> i3 = val;
			case 4 -> i4 = val;
			case 5 -> i5 = val;
			case 6 -> i6 = val;
			case 7 -> i7 = val;
			case 8 -> i8 = val;
			case 9 -> i9 = val;
			case 10 -> i10 = val;
			case 11 -> i11 = val;
			case 12 -> i12 = val;
			case 13 -> i13 = val;
			case 14 -> i14 = val;
			case 15 -> i15 = val;
			case 16 -> i16 = val;
			case 17 -> i17 = val;
			case 18 -> i18 = val;
			case 19 -> i19 = val;
			case 20 -> i20 = val;
			case 21 -> i21 = val;
			case 22 -> i22 = val;
			case 23 -> i23 = val;
			case 24 -> i24 = val;
			case 25 -> i25 = val;
			case 26 -> i26 = val;
			case 27 -> i27 = val;
			case 28 -> i28 = val;
			case 29 -> i29 = val;
			case 30 -> i30 = val;
			default -> i31 = val;
		}
	}

	private Object getObjSlot(int slot) {
		return switch (slot) {
			case 0 -> o0;
			case 1 -> o1;
			case 2 -> o2;
			case 3 -> o3;
			case 4 -> o4;
			default -> o5;
		};
	}

	private void setObjSlot(int slot, Object val) {
		switch (slot) {
			case 0 -> o0 = val;
			case 1 -> o1 = val;
			case 2 -> o2 = val;
			case 3 -> o3 = val;
			case 4 -> o4 = val;
			default -> o5 = val;
		}
	}

	@Override
	public AttributeMapTemplate clone() {
		AttributeMapTemplate clone = (AttributeMapTemplate) super.clone();
		clone.attributeEntrySet = null;
		return clone;
	}

	@Override
	public AttributeMapTemplate cloneAndUnfreeze() {
		AttributeMapTemplate clone = (AttributeMapTemplate) super.cloneAndUnfreeze();
		clone.attributeEntrySet = null;
		return clone;
	}

	public int getInt(Attribute attr, int defaultValue) {
		int ord = attr.ordinal();
		byte kind = ATTR_KIND[ord];
		if (kind == KIND_INT) {
			int slot = ATTR_SLOT[ord];
			return (presentInts & (1 << slot)) != 0 ? getIntSlot(slot) : defaultValue;
		} else if (kind == KIND_BOOL) {
			int slot = ATTR_SLOT[ord];
			long bit = 1L << slot;
			if ((presentBools & bit) != 0L) {
				return (boolValues & bit) != 0L ? 1 : 0;
			}
			return defaultValue;
		} else if (kind == KIND_OBJ) {
			int slot = ATTR_SLOT[ord];
			if ((presentObjs & (1 << slot)) != 0) {
				Object v = getObjSlot(slot);
				return v instanceof Boolean b ? (b ? 1 : 0) : (int) v;
			}
			return defaultValue;
		}
		if (super.isEmpty()) {
			return defaultValue;
		}
		Object value = super.get(attr);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Boolean b) {
			return b ? 1 : 0;
		}
		return (int) value;
	}

	public boolean hasAttributeValue(Attribute attr) {
		int ord = attr.ordinal();
		byte kind = ATTR_KIND[ord];
		if (kind == KIND_BOOL) {
			long bit = 1L << ATTR_SLOT[ord];
			return (presentBools & bit) != 0L && (boolValues & bit) != 0L;
		} else if (kind == KIND_INT) {
			int slot = ATTR_SLOT[ord];
			return (presentInts & (1 << slot)) != 0 && (getIntSlot(slot) != 0 || STORES_TURN_NUMBER[ord]);
		} else if (kind == KIND_OBJ) {
			int slot = ATTR_SLOT[ord];
			if ((presentObjs & (1 << slot)) != 0) {
				Object value = getObjSlot(slot);
				if (value instanceof Boolean b) {
					return b;
				}
				if (value instanceof Integer i && !STORES_TURN_NUMBER[ord]) {
					return i != 0;
				}
				return true;
			}
			return false;
		}
		if (super.isEmpty()) {
			return false;
		}
		Object value = super.get(attr);
		if (value == null) {
			return false;
		}
		if (value instanceof Boolean b) {
			return b;
		}
		if (value instanceof Integer i && !STORES_TURN_NUMBER[ord]) {
			return i != 0;
		}
		return true;
	}

	public void putInt(@NotNull Attribute key, int value) {
		checkNotFrozen();
		int ord = key.ordinal();
		if (ATTR_KIND[ord] == KIND_INT) {
			int slot = ATTR_SLOT[ord];
			presentInts |= (1 << slot);
			setIntSlot(slot, value);
			return;
		}
		put(key, Integer.valueOf(value));
	}

	public void putBoolean(@NotNull Attribute key, boolean value) {
		checkNotFrozen();
		int ord = key.ordinal();
		if (ATTR_KIND[ord] == KIND_BOOL) {
			long bit = 1L << ATTR_SLOT[ord];
			presentBools |= bit;
			if (value) {
				boolValues |= bit;
			} else {
				boolValues &= ~bit;
			}
			return;
		}
		put(key, Boolean.valueOf(value));
	}

	@Override
	public Object put(@NotNull Attribute key, Object value) {
		checkNotFrozen();
		if (value == null) {
			if (this.containsKey(key)) {
				throw new IllegalStateException("Cannot clear a key with a null value");
			}
			return null;
		}
		int ord = key.ordinal();
		byte kind = ATTR_KIND[ord];
		if (kind == KIND_INT) {
			int slot = ATTR_SLOT[ord];
			int bit = 1 << slot;
			int intVal = value instanceof Integer iv ? iv : (value instanceof Boolean bv ? (bv ? 1 : 0) : (int) value);
			Object prev = (presentInts & bit) != 0 ? getIntSlot(slot) : null;
			presentInts |= bit;
			setIntSlot(slot, intVal);
			return prev;
		} else if (kind == KIND_BOOL) {
			int slot = ATTR_SLOT[ord];
			long bit = 1L << slot;
			boolean boolVal = value instanceof Boolean bv ? bv : (value instanceof Integer iv ? iv != 0 : (boolean) value);
			Object prev = (presentBools & bit) != 0L ? ((boolValues & bit) != 0L) : null;
			presentBools |= bit;
			if (boolVal) {
				boolValues |= bit;
			} else {
				boolValues &= ~bit;
			}
			return prev;
		} else if (kind == KIND_OBJ) {
			int slot = ATTR_SLOT[ord];
			int bit = 1 << slot;
			Object prev = (presentObjs & bit) != 0 ? getObjSlot(slot) : null;
			presentObjs |= (byte) bit;
			setObjSlot(slot, value);
			return prev;
		}
		return super.put(key, value);
	}

	@Override
	public Object get(Object key) {
		if (!(key instanceof Attribute attr)) {
			return null;
		}
		int ord = attr.ordinal();
		byte kind = ATTR_KIND[ord];
		if (kind == KIND_INT) {
			int slot = ATTR_SLOT[ord];
			return (presentInts & (1 << slot)) != 0 ? getIntSlot(slot) : null;
		} else if (kind == KIND_BOOL) {
			long bit = 1L << ATTR_SLOT[ord];
			return (presentBools & bit) != 0L ? ((boolValues & bit) != 0L) : null;
		} else if (kind == KIND_OBJ) {
			int slot = ATTR_SLOT[ord];
			return (presentObjs & (1 << slot)) != 0 ? getObjSlot(slot) : null;
		}
		return super.getByOrdinal(ord);
	}

	@Override
	public Object getOrDefault(Object key, Object defaultValue) {
		Object v = get(key);
		return v != null ? v : defaultValue;
	}

	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof Attribute attr)) {
			return false;
		}
		int ord = attr.ordinal();
		byte kind = ATTR_KIND[ord];
		if (kind == KIND_INT) {
			return (presentInts & (1 << ATTR_SLOT[ord])) != 0;
		} else if (kind == KIND_BOOL) {
			return (presentBools & (1L << ATTR_SLOT[ord])) != 0L;
		} else if (kind == KIND_OBJ) {
			return (presentObjs & (1 << ATTR_SLOT[ord])) != 0;
		}
		return super.containsOrdinal(ord);
	}

	@Override
	public Object remove(Object key) {
		checkNotFrozen();
		if (!(key instanceof Attribute attr)) {
			return null;
		}
		int ord = attr.ordinal();
		byte kind = ATTR_KIND[ord];
		if (kind == KIND_INT) {
			int slot = ATTR_SLOT[ord];
			int bit = 1 << slot;
			if ((presentInts & bit) != 0) {
				Object prev = getIntSlot(slot);
				presentInts &= ~bit;
				return prev;
			}
			return null;
		} else if (kind == KIND_BOOL) {
			int slot = ATTR_SLOT[ord];
			long bit = 1L << slot;
			if ((presentBools & bit) != 0L) {
				Object prev = (boolValues & bit) != 0L;
				presentBools &= ~bit;
				boolValues &= ~bit;
				return prev;
			}
			return null;
		} else if (kind == KIND_OBJ) {
			int slot = ATTR_SLOT[ord];
			int bit = 1 << slot;
			if ((presentObjs & bit) != 0) {
				Object prev = getObjSlot(slot);
				presentObjs &= (byte) ~bit;
				setObjSlot(slot, null);
				return prev;
			}
			return null;
		}
		return super.removeByOrdinal(ord);
	}

	@Override
	public void clear() {
		checkNotFrozen();
		presentInts = 0;
		presentBools = 0L;
		boolValues = 0L;
		presentObjs = 0;
		o0 = o1 = o2 = o3 = o4 = o5 = null;
		if (!super.isEmpty()) {
			super.clear();
		}
	}

	@Override
	public int size() {
		return Integer.bitCount(presentInts) + Long.bitCount(presentBools) + Integer.bitCount(presentObjs & 0xFF) + super.size();
	}

	@Override
	public boolean isEmpty() {
		return presentInts == 0 && presentBools == 0L && presentObjs == 0 && super.isEmpty();
	}

	public Set<Attribute> unsafeKeySet() {
		return keySet();
	}

	@Override
	public Set<Entry<Attribute, Object>> entrySet() {
		if (attributeEntrySet == null) {
			attributeEntrySet = new AttributeEntrySet();
		}
		return attributeEntrySet;
	}

	private final class AttributeEntrySet extends AbstractSet<Entry<Attribute, Object>> {
		@Override
		public Iterator<Entry<Attribute, Object>> iterator() {
			return new Iterator<>() {
				private int remInts = presentInts;
				private long remBools = presentBools;
				private int remObjs = presentObjs & 0xFF;
				private Iterator<Entry<Attribute, Object>> overflowIter = AttributeMapTemplate.super.isEmpty() ? null : AttributeMapTemplate.super.entrySet().iterator();
				private Attribute lastKey;
				private boolean lastFromOverflow;

				@Override
				public boolean hasNext() {
					return remInts != 0 || remBools != 0L || remObjs != 0 || (overflowIter != null && overflowIter.hasNext());
				}

				@Override
				public Entry<Attribute, Object> next() {
					if (remInts != 0) {
						int slot = Integer.numberOfTrailingZeros(remInts);
						remInts &= remInts - 1;
						Attribute attr = INT_SLOT_TO_ATTR[slot];
						lastKey = attr;
						lastFromOverflow = false;
						return new InlineEntry(attr);
					}
					if (remBools != 0L) {
						int slot = Long.numberOfTrailingZeros(remBools);
						remBools &= remBools - 1L;
						Attribute attr = BOOL_SLOT_TO_ATTR[slot];
						lastKey = attr;
						lastFromOverflow = false;
						return new InlineEntry(attr);
					}
					if (remObjs != 0) {
						int slot = Integer.numberOfTrailingZeros(remObjs);
						remObjs &= remObjs - 1;
						Attribute attr = OBJ_SLOT_TO_ATTR[slot];
						lastKey = attr;
						lastFromOverflow = false;
						return new InlineEntry(attr);
					}
					if (overflowIter != null && overflowIter.hasNext()) {
						Entry<Attribute, Object> entry = overflowIter.next();
						lastKey = entry.getKey();
						lastFromOverflow = true;
						return entry;
					}
					throw new NoSuchElementException();
				}

				@Override
				public void remove() {
					if (lastKey == null) {
						throw new IllegalStateException();
					}
					if (lastFromOverflow) {
						overflowIter.remove();
					} else {
						AttributeMapTemplate.this.remove(lastKey);
					}
					lastKey = null;
				}
			};
		}

		@Override
		public int size() {
			return AttributeMapTemplate.this.size();
		}

		@Override
		public void clear() {
			AttributeMapTemplate.this.clear();
		}
	}

	private final class InlineEntry implements Entry<Attribute, Object> {
		private final Attribute key;

		private InlineEntry(Attribute key) {
			this.key = key;
		}

		@Override
		public Attribute getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return AttributeMapTemplate.this.get(key);
		}

		@Override
		public Object setValue(Object value) {
			return AttributeMapTemplate.this.put(key, value);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Entry<?, ?> other)) {
				return false;
			}
			return Objects.equals(key, other.getKey()) && Objects.equals(getValue(), other.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(key) ^ Objects.hashCode(getValue());
		}
	}
}
