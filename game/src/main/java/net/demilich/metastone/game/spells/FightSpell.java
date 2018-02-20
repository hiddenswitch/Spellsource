package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FightSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(FightSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityReference secondaryTarget = (EntityReference) desc.getOrDefault(SpellArg.SECONDARY_TARGET, source == null ? EntityReference.NONE : source.getReference());
		List<Entity> resolvedSources = context.resolveTarget(player, source, secondaryTarget);
		if (resolvedSources == null) {
			logger.error("onCast {} {}: Could not resolve source key {} targeting {}", context.getGameId(), source, secondaryTarget, target);
			return;
		}

		for (Entity resolvedSource : resolvedSources) {
			if (!(resolvedSource instanceof Actor)) {
				logger.error("onCast {} {}: Source entity {} targeting {} is not an Actor", context.getGameId(), source, resolvedSource, target);
				return;
			}

			if (!(target instanceof Actor)) {
				logger.error("onCast {} {}: Target {} is not an Actor (trying to attack with {})", context.getGameId(), source, target, resolvedSource);
				return;
			}

			if (resolvedSource instanceof Hero) {
				// Activate the weapon if the hero has one
				((Hero) resolvedSource).activateWeapon(true);
			}

			context.getLogic().fight(player, (Actor) resolvedSource, (Actor) target);
			desc.subSpells(0).forEach(subSpell -> {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, resolvedSource);
			});

			if (resolvedSource instanceof Hero) {
				// Deactivate the weapon when we're done.
				((Hero) resolvedSource).activateWeapon(false);
			}
		}
	}
}
