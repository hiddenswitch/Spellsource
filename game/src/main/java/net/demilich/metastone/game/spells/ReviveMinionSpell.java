package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Reviving a minion, unlike resurrecting it, puts a minion back into the position on the board where it died. The
 * {@link SpellArg#HP_BONUS} field must be specified, and this amount of hitpoints is what the target minion's health is
 * set to. The minion is summoned from the base card.
 * <p>
 * If a {@link SpellArg#SPELL} is specified, cast it on the newly revived minion as {@link EntityReference#OUTPUT}.
 */
public final class ReviveMinionSpell extends Spell {

	public static SpellDesc create(EntityReference target) {
		return create(target, 0);
	}

	public static SpellDesc create(EntityReference target, int hpAdjustment) {
		Map<SpellArg, Object> arguments = new SpellDesc(ReviveMinionSpell.class);
		arguments.put(SpellArg.HP_BONUS, hpAdjustment);
		arguments.put(SpellArg.TARGET, target);

		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int hpAdjustment = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		Actor targetActor = (Actor) target;
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		Card card = targetActor.getSourceCard();
		Minion minion = card.summon();
		if (hpAdjustment != 0) {
			minion.setHp(hpAdjustment);
		}
		if (context.getLogic().summon(player.getId(), minion, source, boardPosition, false)) {
			// The minion may have been transformed during summon
			Entity resultingMinion = minion.transformResolved(context);
			if (resultingMinion.isInPlay() && desc.containsKey(SpellArg.SPELL)) {
				SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target, resultingMinion);
			}
		}
	}
}
