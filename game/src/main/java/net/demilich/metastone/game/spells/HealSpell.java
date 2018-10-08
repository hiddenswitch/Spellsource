package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;
import java.util.function.Predicate;

public class HealSpell extends Spell {

	public static SpellDesc create(EntityReference target, int healing) {
		return create(target, healing, false);
	}

	public static SpellDesc create(EntityReference target, int healing, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(HealSpell.class);
		arguments.put(SpellArg.VALUE, healing);
		arguments.put(SpellArg.TARGET, target);
		if (randomTarget) {
			arguments.put(SpellArg.RANDOM_TARGET, true);
			arguments.put(SpellArg.FILTER, (Predicate<Entity>) entity -> ((Actor) entity).isWounded());
		}
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int healing) {
		return create(null, healing);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int healing = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		context.getLogic().heal(player, (Actor) target, Math.max(0, healing), source);
	}

}

