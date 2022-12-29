package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Makes each actor in {@link SpellArg#SECONDARY_TARGET} attack another random actor in {@link SpellArg#TARGET}.
 * <p>
 * No minion attacks more than once, but some minions make be attacked more than once.
 * <p>
 * Dueling does not consume attacks.
 */
public class DuelSpell extends FightSpell {

	private static Logger logger = LoggerFactory.getLogger(DuelSpell.class);

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		EntityFilter filter = desc.getEntityFilter();
		List<Entity> validDefenders = SpellUtils.getValidTargets(context, player, targets, filter, source);
		List<Entity> validAttackers = SpellUtils.getValidTargets(context, player, context.resolveTarget(player, source, desc.getSecondaryTarget()), filter, source);

		duel(context, player, source, validAttackers, validDefenders);
	}

	protected void duel(GameContext context, Player player, Entity source, List<Entity> validAttackers, List<Entity> validDefenders) {
		for (Entity attacker : validAttackers) {
			if (!attacker.isInPlay() || attacker.isDestroyed()) {
				continue;
			}

			if (!(attacker instanceof Actor)) {
				logger.error("onCast {} {}: Tried to duel attacker {} which is not an actor.", context.getGameId(), source, attacker);
				continue;
			}

			List<Entity> validDefendersWithoutAttacker = validDefenders.stream().filter(
					e -> e.getId() != attacker.getId()
					&& !e.isDestroyed()
					&& e.isInPlay()
			).collect(Collectors.toList());

			if (validDefendersWithoutAttacker.isEmpty()) {
				logger.debug("onCast {} {}: Valid defenders are empty without the attacker", context.getGameId(), source);
				continue;
			}

			Entity defender = context.getLogic().getRandom(validDefendersWithoutAttacker);

			if (!(defender instanceof Actor)) {
				logger.error("onCast {} {}: Tried to duel attacker {} with defender {} which is not an actor.", context.getGameId(), source, attacker, defender);
				continue;
			}

			context.getSpellTargetStack().push(defender.getReference());
			int attacksBefore = attacker.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
			SpellDesc fight = new SpellDesc(FightSpell.class);
			fight.put(SpellArg.TARGET, defender.getReference());
			fight.put(SpellArg.SECONDARY_TARGET, attacker.getReference());
			castForPlayer(context, player, fight, attacker, defender);
			attacker.setAttribute(Attribute.NUMBER_OF_ATTACKS, attacksBefore);
			context.getSpellTargetStack().pop();
		}
	}

}
