package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

public class ModifyAttributeSpell extends Spell {
	public static SpellDesc create(EntityReference target, Attribute tag, int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ModifyAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, tag);
		arguments.put(SpellArg.VALUE, value);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		target.modifyAttribute(attribute, value);
	}

}
