package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

public class SetAttributeSpell extends Spell {
	public static SpellDesc create(EntityReference target, Attribute tag, Object value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SetAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		Object value = desc.get(SpellArg.VALUE);
		if (value instanceof Integer
				|| ValueProvider.class.isAssignableFrom(value.getClass())) {
			value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		}
		target.setAttribute(attribute, value);
	}

}
