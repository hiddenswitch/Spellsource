package net.demilich.metastone.game.cards.desc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.demilich.metastone.game.spells.trigger.Enchantment;

import java.io.IOException;

public class EnchantmentSerializer extends StdSerializer<Enchantment> {
	public EnchantmentSerializer() {
		super(Enchantment.class);
	}

	@Override
	public void serialize(Enchantment value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeObject(value.getEntrySet());
	}
}
