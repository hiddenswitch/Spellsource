package net.demilich.metastone.game.spells;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
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
 * <p>
 * If a {@link SpellArg#SPELL1} is specified, it will be cast BEFORE THE REVIVAL, with the original targeted minion as
 * {@link EntityReference#OUTPUT}. This can be useful for the case of ensuring that the exact original minion is
 * revived, not something that it might transform into by killing it. (Interaction between Crazed Cultist and Pavel,
 * Elemental of Surprise)
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
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int hpAdjustment = desc.getValue(SpellArg.HP_BONUS, context, player, target, source, 0);
		Actor targetActor = (Actor) target;
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		Card card = targetActor.getSourceCard();
		// Bring back the weapon. Happens when a weapon gets a minion's aftermath
		if (GameLogic.isCardType(card.getCardType(), CardType.WEAPON)) {
			context.getLogic().equipWeapon(player.getId(), card.weapon(), card, false);
			return;
		}

		if (desc.containsKey(SpellArg.SPELL1)) {
			SpellUtils.castChildSpell(context, player, (SpellDesc) desc.get(SpellArg.SPELL1), source, target, targetActor);
		}

		Minion minion = card.minion();
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
