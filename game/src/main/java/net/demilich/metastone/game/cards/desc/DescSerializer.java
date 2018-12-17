package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DescSerializer extends StdSerializer<Desc> {

	private static final long serialVersionUID = -6740666907899558371L;

	public DescSerializer() {
		super(Desc.class);
	}

	@Override
	public void serialize(Desc value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();

		for (Object key : value.keySet()) {
			gen.writeFieldName(ParseUtils.toCamelCase(key.toString()));
			gen.writeObject(value.get(key));
		}
		gen.writeEndObject();
	}
}
