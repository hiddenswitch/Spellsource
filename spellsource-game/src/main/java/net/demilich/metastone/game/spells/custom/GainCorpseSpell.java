package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Gains corpses equal to VALUE (default 1).
 *
 * Corpses are tracked via the player's graveyard zone. This spell adds ghost
 * minion entities (token_dk_corpse) directly to the graveyard so that
 * {@link net.demilich.metastone.game.spells.desc.valueprovider.GraveyardMinionCountValueProvider}
 * correctly counts them as corpses without triggering death events.
 *
 * Used to implement "Gain a Corpse" effects for Death Knight cards like Body Bagger.
 */
public class GainCorpseSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < count; i++) {
			var card = context.getCardById("token_dk_corpse");
			if (card == null) {
				continue;
			}
			var ghost = card.minion();
			ghost.setId(context.getLogic().generateId());
			ghost.setOwner(player.getId());
			// Add directly to graveyard without triggering death events or kill events
			player.getGraveyard().add(ghost);
			// Set DIED_ON_TURN so GraveyardMinionCountValueProvider counts this entity as a corpse
			ghost.setAttribute(Attribute.DIED_ON_TURN, context.getTurn());
		}
	}
}
