package net.demilich.metastone.game.spells.desc.condition;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.ConditionDescSerializer;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.lang.reflect.Type;

public abstract class Condition implements Serializable {
	private ConditionDesc desc;

	public Condition(ConditionDesc desc) {
		this.desc = desc;
	}

	@Suspendable
	protected abstract boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target);

	@Suspendable
	public boolean isFulfilled(GameContext context, Player player, Entity source, Entity target) {
		boolean invert = desc.getBool(ConditionArg.INVERT);
		return isFulfilled(context, player, desc, source, target) != invert;
	}

	public static class Serializer implements JsonSerializer<Condition>, JsonDeserializer<Condition> {
		@Override
		public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			ConditionDesc desc = context.deserialize(json.getAsJsonObject().getAsJsonObject("desc"), ConditionDesc.class);
			return desc == null ? null : desc.create();
		}

		@Override
		public JsonElement serialize(Condition src, Type typeOfSrc, JsonSerializationContext context) {
			return context.serialize(src.desc);
		}
	}
}
