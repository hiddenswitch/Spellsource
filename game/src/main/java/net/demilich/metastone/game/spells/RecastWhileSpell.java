package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Recasts the {@link SpellArg#SPELL} {@link SpellArg#HOW_MANY} times or up to 60 (14 if this is Grim Patron) by
 * default.
 * <p>
 * Continues casting as long as the {@link SpellArg#CONDITION} is met. Stops casting if the {@link SpellArg#CONDITION}
 * is not met. Always casts <b>at least once.</b>
 */
public class RecastWhileSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// If Grim Patron is on the board
		int limit = context.getPlayers().stream()
				.map(Player::getMinions)
				.flatMap(EntityZone::stream)
				.anyMatch(c -> c.getSourceCard().getCardId().equals("minion_grim_patron"))
				? 14 : 60;
		limit = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, limit);
		Condition condition = (Condition) desc.get(SpellArg.CONDITION);
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		do {
			if (Strand.currentStrand().isInterrupted()) {
				break;
			}
			if (limit == 0) {
				break;
			}
			// Cast the spell at least once before evaluating the condition
			beforeCast(context, desc);
			SpellUtils.castChildSpell(context, player, spell, source, target);
			afterCast(context, desc);
			context.getLogic().endOfSequence();
			limit--;
			if (limit < 0) {
				return;
			}
		} while (isFulfilled(context, player, source, target, condition, desc));
	}

	protected void beforeCast(GameContext context, SpellDesc desc) {
	}

	protected void afterCast(GameContext context, SpellDesc desc) {
	}

	@Suspendable
	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition condition, SpellDesc desc) {
		return condition == null || condition.isFulfilled(context, player, source, target);
	}
}