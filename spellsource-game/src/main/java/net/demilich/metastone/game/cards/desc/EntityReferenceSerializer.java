package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.IOException;

public class EntityReferenceSerializer extends StdSerializer<EntityReference> {
	public EntityReferenceSerializer() {
		super(EntityReference.class);
	}

	@Override
	public void serialize(EntityReference value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value == null) {
			gen.writeString("NONE");
			return;
		}
		gen.writeNumber(value.getId());
	}
}
