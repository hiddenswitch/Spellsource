package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A base class for deserializers of the "component" or object types in the Spellsource card JSON. Each type, like
 * {@link net.demilich.metastone.game.spells.Spell}, has a corresponding {@link Desc}, like {@link SpellDesc}, which is
 * deserialized by this class's implementor {@link SpellDescDeserializer}. In that class, the {@code
 * #init(SerializationContext)} implementation adds the mappings from each enum value of {@link SpellArg} to its
 * corresponding {@link ParseValueType}.
 * <p>
 * To help visualize, this means that if {@code <K>} is your enum type and {@code <V>} is your class deserialize to,
 * you'll have:
 *
 * <ul>
 * <li>A type {@code <T>} that is a "desc", which is a map of {@code <K>} to objects.</li>
 * <li>A {@code DescDeserializer} that reads {@code camelCased} versions of {@code <K>} and applies them as keys to
 * {@code <T extends Map<K, Object>>}</li>
 * <li>An entry in {@link ParseValueType} that corresponds to {@code <T>}, deserialized by {@link
 * ParseUtils#parse(JsonNode, ParseValueType, DeserializationContext)}.</li>
 * </ul>
 * <p>
 * For example, for a {@code Spell} subclass like {@link net.demilich.metastone.game.spells.DamageSpell}, you might see
 * the JSON:
 * <pre>
 *   "spell": {
 *     "class": "DamageSpell",
 *     "value": {
 *       "class": "CurrentTurnValueProvider"
 *     },
 *     "target": "ENEMY_CHARACTERS"
 *   }
 * </pre>
 * <p>
 * {@link ParseUtils#parse(JsonNode, ParseValueType, DeserializationContext)}  knows to turn {@code "spell"} into a
 * {@code SpellDesc} instance because in some {@code #init(SerializationContext)}, an arg (like {@link SpellArg#SPELL})
 * is configured to be of type {@link ParseValueType#SPELL}.
 * <p>
 * Then, a field like {@code "class"} is known to be the "class arg" and is {@code put} with
 * {@link SpellArg#CLASS} as the key and a {@link Class} instance as a value. For a field like {@code "value"}, it is
 * turned into {@code UPPER_CASE} (so {@code "VALUE"}) and simply passed to {@link Enum#valueOf(Class, String)} to find
 * the corresponding enum constant to use as the key. The {@code init} implementation specifies that {@link
 * SpellArg#VALUE} is a {@link ParseValueType#VALUE}, which is a union of types of integer or a {@link
 * ValueProviderDesc}. If it encounters a number, it'll put a number into the {@code desc} map. Otherwise, it will
 * decode a {@link ValueProviderDesc} using a similar process and actually instantiate it into its concrete type using
 * {@link Desc#create()}. That means it will look up the {@code "class"} field as a class (in this case, {@code
 * "CurrentTurnValueProvider"}) and create an instance of it. So all in all, the Java equivalent of deserializing the
 * JSON example above looks like:
 *
 * <pre>
 *   {@code
 *   SpellDesc desc = new SpellDesc(DamageSpell.class);
 *   desc.put(SpellArg.TARGET, EntityReference.ENEMY_CHARACTERS);
 *   ValueProviderDesc valueProviderDesc = new ValueProviderDesc(CurrentTurnValueProvider.class);
 *   ValueProvider valueProvider = valueProviderDesc.create();
 *   desc.put(SpellArg.VALUE, valueProvider);
 *   }
 * </pre>
 *
 * @param <T>
 * @param <K>
 * @param <V>
 */
public abstract class DescDeserializer<T extends Desc<K, V>, K extends Enum<K>, V extends HasDesc<T>> extends StdDeserializer<T> {
	private LinkedHashMap<K, ParseValueType> interpreter = new LinkedHashMap<>();

	protected DescDeserializer(Class<? extends T> vc) {
		super(vc);
		init(new SerializationContext());
	}

	protected abstract T createDescInstance();

	/**
	 * Gives the implementing deserializer the opportunity to specify how a key in its enum should be deserialized.
	 * <p>
	 * Call the {@link SerializationContext#add(Enum, ParseValueType)} method to specify this mapping.
	 *
	 * @param ctx The serialization context.
	 */
	public abstract void init(SerializationContext ctx);

	protected abstract Class<V> getAbstractComponentClass();

	protected abstract Class<K> getEnumType();

	@SuppressWarnings("unchecked")
	public T deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = ctxt.readValue(p, JsonNode.class);

		return innerDeserialize(ctxt, node);
	}

	@SuppressWarnings("unchecked")
	public T innerDeserialize(DeserializationContext ctxt, JsonNode node) throws JsonMappingException {
		T inst = createDescInstance();
		final String suppliedClassName = node.get("class").asText();
		String[] spellClassNames = new String[]{getAbstractComponentClass().getPackage().getName() + "." + suppliedClassName, getAbstractComponentClass().getPackage().getName() + ".custom." + suppliedClassName};
		Class<? extends V> spellClass = null;
		for (String concreteClass : spellClassNames) {
			try {
				spellClass = (Class<? extends V>) Class.forName(concreteClass);
				break;
			} catch (ClassNotFoundException ignored) {
			}
		}

		if (spellClass == null) {
			throw ctxt.weirdStringException(suppliedClassName, getAbstractComponentClass(), "parser encountered an invalid class");
		}


		inst.put(inst.getClassArg(), spellClass);

		Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> e = iter.next();
			String camelCased = ParseUtils.toUpperCase(e.getKey());
			K key = Enum.valueOf(getEnumType(), camelCased);
			if (key.equals(inst.getClassArg())) {
				continue;
			}
			inst.put(key, ParseUtils.parse(e.getValue(), interpreter.get(key), ctxt));
		}
		return inst;
	}

	protected class SerializationContext {
		public SerializationContext add(K key, ParseValueType vt) {
			interpreter.put(key, vt);
			return this;
		}
	}
}
