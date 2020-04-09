package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Steals {@link net.demilich.metastone.game.spells.desc.SpellArg#HP_BONUS} health from the {@code target} actor and
 * gives it to the {@code source}.
 * <p>
 * The {@link net.demilich.metastone.game.spells.desc.SpellArg#HP_BONUS} should be <b>negative</b>.
 */
public final class StealHealthSpell extends BuffSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(StealHealthSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!Entity.hasEntityType(target.getEntityType(), EntityType.ACTOR)) {
			return;
		}
		var hpBonus = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		if (hpBonus > 0) {
			LOGGER.error("onCast {} {}: HP_BONUS should be negative, is {}", context.getGameId(), source, hpBonus);
			return;
		}
		var actor = (Actor) target;
		var targetHpBeforeBuff = actor.getHp();
		super.onCast(context, player, desc, source, target);
		var stolen = Math.min(-hpBonus, targetHpBeforeBuff);
		desc = desc.addArg(SpellArg.VALUE, stolen);
		super.onCast(context, player, desc, source, source);
	}
}
