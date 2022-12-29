package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Modifies the durability of a weapon regardless of which weapon is equipped.
 */
public class ModifyDurabilitySpell extends Spell {

	public static SpellDesc create(EntityReference target, int durability) {
		Map<SpellArg, Object> arguments = new SpellDesc(ModifyDurabilitySpell.class);
		arguments.put(SpellArg.VALUE, durability);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int durability) {
		return create(null, durability);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// if there is no weapon, just do nothing
		if (player.getWeaponZone().isEmpty()) {
			return;
		}

		int durabilityChange = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		context.getLogic().modifyDurability(player.getWeaponZone().get(0), durabilityChange);
	}

}
