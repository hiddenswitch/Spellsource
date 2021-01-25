package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * Removes the {@code target} {@link Card} by putting it directly from the zone it's currently in into the {@link
 * Zones#GRAVEYARD}.
 * <p>
 * For example, to remove the top card from the opponent's deck:
 * <pre>
 *   {
 *     "class": "RevealCardSpell",
 *     "target": "ENEMY_TOP_CARD",
 *     "spell": {
 *       "class": "RemoveCardSpell",
 *       "target": "OUTPUT"
 *     }
 *   }
 * </pre>
 */
public class RemoveCardSpell extends AbstractRemoveCardSpell {

	public static SpellDesc create(EntityReference target, EntityFilter filter, boolean randomTarget) {
		return new SpellDesc(RemoveCardSpell.class, target, filter, randomTarget);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}
		Card card = (Card) target;
		SpellDesc subSpell = desc.getSpell();
		card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		if (subSpell != null) {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
		}
		context.getLogic().removeCard(card);
	}
}