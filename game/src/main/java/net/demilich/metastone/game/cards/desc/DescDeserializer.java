package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class DescDeserializer<T extends Desc<K, V>, K extends Enum<K>, V> extends StdDeserializer<T> {
	private LinkedHashMap<K, ParseValueType> interpreter = new LinkedHashMap<>();

	protected DescDeserializer(Class<? extends T> vc) {
		super(vc);
		init(new SerializationContext());
	}

	protected abstract T createDescInstance();

	protected abstract void init(SerializationContext ctx);

	protected abstract Class<V> getAbstractComponentClass();

	protected abstract Class<K> getEnumType();

	@SuppressWarnings("unchecked")
	public T deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();

		return innerDeserialize(ctxt, node);
	}

	@SuppressWarnings("unchecked")
	public T innerDeserialize(DeserializationContext ctxt, JsonNode node) {
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
			throw new RuntimeException("parser encountered an invalid class: " + suppliedClassName);
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
		protected SerializationContext add(K key, ParseValueType vt) {
			interpreter.put(key, vt);
			return this;
		}
	}
}
