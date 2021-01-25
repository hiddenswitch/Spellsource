package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HasDescSerializer extends StdSerializer<HasDesc> {
	private static final StdSerializer<Desc> serializer = new DescSerializer();

	public HasDescSerializer() {
		super(HasDesc.class);
	}

	@Override
	public void serialize(HasDesc value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		serializer.serialize(value != null ? value.getDesc() : null, gen, provider);
	}
}
