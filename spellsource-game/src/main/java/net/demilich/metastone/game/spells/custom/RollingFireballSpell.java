package net.demilich.metastone.game.spells.custom;

import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;

/**
 * Deals {@link SpellArg#VALUE} damage to the target minion. Any excess damage (beyond the target's remaining HP)
 * continues to a random adjacent minion, repeating until no excess remains or no adjacent targets exist.
 * <p>
 * Implements Rolling Fireball's "Deal $8 damage to a minion. Any excess damage continues to the left or right."
 */
public class RollingFireballSpell extends DamageSpell {
	private static final Logger logger = LoggerFactory.getLogger(RollingFireballSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!(target instanceof Actor)) {
			logger.error("onCast {} {}: Cannot deal damage to non-Actor target {}", context.getGameId(), source, target);
			return;
		}

		int damage = getDamage(context, player, desc, source, target);
		Actor currentTarget = (Actor) target;

		while (damage > 0 && currentTarget != null && !currentTarget.isDestroyed()) {
			int hpBefore = currentTarget.getHp();
			context.getLogic().damage(player, currentTarget, damage, source, false, getDamageType(context, player, source));

			int excess = damage - hpBefore;
			if (excess <= 0) {
				break;
			}

			// Find adjacent minions that are alive
			List<Actor> adjacent = context.getAdjacentMinions(currentTarget.getReference());
			adjacent.removeIf(Actor::isDestroyed);

			if (adjacent.isEmpty()) {
				break;
			}

			// Pick a random adjacent minion
			currentTarget = adjacent.get(context.getLogic().getRandom().nextInt(adjacent.size()));
			damage = excess;
		}
	}

	@Override
	protected EnumSet<DamageType> getDamageType(GameContext context, Player player, Entity source) {
		return EnumSet.of(DamageType.MAGICAL);
	}
}
