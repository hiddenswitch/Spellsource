package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.JoustEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * A joust causes both players to show a random card satisfying the {@link SpellArg#CARD_FILTER} and performs an action
 * if the mana cost of the caster's card is higher than the opponent's card's cost.
 * <p>
 * If the casting player wins, {@link SpellArg#SPELL1} or {@link SpellArg#SPELL} is cast. If the player loses, {@link
 * SpellArg#SPELL2} (if defined) is cast.
 *
 * @see net.demilich.metastone.game.logic.GameLogic#joust(Player, EntityFilter, Entity) for a full description of joust.
 */
public class JoustSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		JoustEvent joustEvent = context.getLogic().joust(player, desc.getCardFilter(), source);
		if (!joustEvent.isWon()) {
			SpellDesc spell1 = (SpellDesc) desc.get(SpellArg.SPELL1);
			if (spell1 != null) {
				SpellUtils.castChildSpell(context, player, spell1, source, target, joustEvent.getTarget());
			}

			return;
		}

		SpellDesc spell2 = (SpellDesc) desc.get(SpellArg.SPELL2);
		if (spell2 != null) {
			SpellUtils.castChildSpell(context, player, spell2, source, target, joustEvent.getTarget());
			return;
		}

		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		SpellUtils.castChildSpell(context, player, spell, source, joustEvent.getTarget());
	}

}

