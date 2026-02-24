package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Deals {@link SpellArg#VALUE} damage to a random minion and freezes it. If the minion survives, repeats on another
 * random minion. Continues until a minion is destroyed or no valid targets remain. Each minion can only be hit once.
 * <p>
 * Implements Snowball Fight!
 */
public class SnowballFightSpell extends Spell {

	private static final Logger logger = LoggerFactory.getLogger(SnowballFightSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int damage = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		List<Entity> allMinions = context.resolveTarget(player, source, EntityReference.ALL_MINIONS);
		if (allMinions == null || allMinions.isEmpty()) {
			return;
		}

		List<Entity> hitMinions = new ArrayList<>();

		while (true) {
			List<Entity> validTargets = new ArrayList<>();
			for (Entity m : allMinions) {
				if (m instanceof Actor && !((Actor) m).isDestroyed() && m.isInPlay() && !hitMinions.contains(m)) {
					validTargets.add(m);
				}
			}

			if (validTargets.isEmpty()) {
				break;
			}

			Actor randomTarget = (Actor) context.getLogic().getRandom(validTargets);
			hitMinions.add(randomTarget);

			context.getLogic().damage(player, randomTarget, damage, source, true);
			randomTarget.setAttribute(Attribute.FROZEN);

			if (randomTarget.isDestroyed()) {
				break;
			}
		}
	}
}
