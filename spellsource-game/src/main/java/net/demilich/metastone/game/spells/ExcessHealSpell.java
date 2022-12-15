package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Performs a {@link HealSpell} effect. If a {@link SpellArg#SPELL} is specified, cast it. For the duration of that
 * execution context, put the excess healing into the {@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider}
 * value.
 */
public final class ExcessHealSpell extends HealSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var healing = getHealing(context, player, desc, source, target);
		var res = context.getLogic().heal(player, (Actor) target, Math.max(0, healing), source);
		if (res.getExcess() > 0) {
			var spell = desc.getSpell();
			context.getEventValueStack().push(res.getExcess());
			SpellUtils.castChildSpell(context, player, spell, source, target);
			context.getEventValueStack().pop();
		}
	}
}
