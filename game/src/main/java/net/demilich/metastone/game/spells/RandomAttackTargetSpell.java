package net.demilich.metastone.game.spells;

import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAttackTargetSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(RandomAttackTargetSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor attacker = (Actor) context.resolveSingleTarget(context.getAttackerReferenceStack().peek());
		if (attacker == null) {
			logger.error("onCast {} {}: No attacker was found", context.getGameId(), source);
			return;
		}
		if (attacker.isDestroyed()) {
			logger.error("onCast {} {}: Attack {} was destroyed", context.getGameId(), source, attacker);
			return;
		}
		PhysicalAttackAction action = new PhysicalAttackAction(attacker.getReference());
		List<Entity> targets = context.getLogic().getValidTargets(context.getActivePlayerId(), action);
		Entity randomTarget = context.getLogic().getRandom(targets);
		context.getEnvironment().put(Environment.TARGET_OVERRIDE, randomTarget.getReference());
	}
}
