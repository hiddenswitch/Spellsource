package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class ExecuteAsSpell extends Spell {
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity secondTarget = context.resolveSingleTarget((EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
		List<SpellDesc> subSpells = desc.subSpells(0);
		if (target == null && (subSpells == null || subSpells.isEmpty())) {
			return;
		}
		for (SpellDesc subSpell : subSpells) {
			SpellUtils.castChildSpell(context, player, subSpell, target, secondTarget);
		}
	}
}
