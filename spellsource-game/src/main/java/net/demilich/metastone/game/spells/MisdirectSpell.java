package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Misdirection overrides the {@link EntityReference#ATTACKER}'s current target to another random target within {@link
 * SpellArg#SECONDARY_TARGET}.
 * <p>
 * By default, another character is chosen from {@link EntityReference#ALL_CHARACTERS}.
 * <p>
 * The attacker itself is always excluded from the possible misdirection.
 *
 * @see OverrideTargetSpell for a more general target override function.
 * @see FightSpell to cause an actor to attack a target generally.
 */
public class MisdirectSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(MisdirectSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor attacker = (Actor) context.resolveSingleTarget(context.getAttackerReferenceStack().peek());
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
		context.setTargetOverride(randomTarget.getReference());
	}
}
