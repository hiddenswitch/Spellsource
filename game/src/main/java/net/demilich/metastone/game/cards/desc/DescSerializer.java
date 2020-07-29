package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DescSerializer extends StdSerializer<Desc> {

	public DescSerializer() {
		super(Desc.class);
	}

	@Override
	public void serialize(Desc value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();

		for (Object key : value.keySet()) {
			gen.writeFieldName(ParseUtils.toCamelCase(key.toString()));
			var descValue = value.get(key);
			if (key.equals(value.getClassArg())) {
				var descClass = (Class<?>) descValue;
				gen.writeString(descClass.getSimpleName());
			} else if (descValue instanceof HasDesc) {
				var descValueDesc = (HasDesc<?>) descValue;
				gen.writeObject(descValueDesc.getDesc());
			} else {
				gen.writeObject(descValue);
			}
		}
		gen.writeEndObject();
	}
}
