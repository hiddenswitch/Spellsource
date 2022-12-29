package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Use {@link SetHpSpell} instead.
 * <p>
 * Sets the target actor's HP to the specified {@link SpellArg#VALUE}, including increasing the maximum HP if
 * necessary.
 * <p>
 * This spell does work for other actors besides heroes, but it is recommended to use {@link SetHpSpell} instead.
 *
 * @deprecated
 */
@Deprecated
public class SetHeroHpSpell extends Spell {

	private static final Logger logger = LoggerFactory.getLogger(SetHeroHpSpell.class);

	public static SpellDesc create(int value) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetHeroHpSpell.class);
		arguments.put(SpellArg.VALUE, value);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor actor = (Actor) target;
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		if (actor.getMaxHp() < value) {
			actor.setMaxHp(value);
			logger.debug("onCast {} {}: {}'s Max Hp have been set to {}", context.getGameId(), source, actor, actor.getMaxHp());
		}
		actor.setHp(value);
		logger.debug("onCast {} {}: {}'s Hp have been set to {}", context.getGameId(), source, actor, actor.getHp());
	}
}
