package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MisdirectSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(MisdirectSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor attacker = (Actor) context.resolveSingleTarget((EntityReference) context.getEnvironment().get(Environment.ATTACKER_REFERENCE));
		if (attacker == null) {
			logger.warn("onCast: Cannot misdirect a null attacker.");
			return;
		}
		if (attacker.isDestroyed()) {
			logger.debug("onCast: The attack who was selected to misdirect is already destroyed.");
			return;
		}

		EntityReference secondaryTarget = desc.containsKey(SpellArg.SECONDARY_TARGET) ? (EntityReference) desc.get(SpellArg.SECONDARY_TARGET) : EntityReference.ALL_CHARACTERS;
		Actor randomTarget = context.getLogic().getAnotherRandomTarget(player, attacker, (Actor) target, secondaryTarget);
		context.getEnvironment().put(Environment.TARGET_OVERRIDE, randomTarget.getReference());
	}
}
