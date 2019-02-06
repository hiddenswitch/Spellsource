package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Discards or removes all cards from {@link SpellArg#TARGET_PLAYER}'s deck, up to {@link SpellArg#VALUE} random cards.
 * <p>
 * To discard the caster's entire deck:
 * <pre>
 *   {
 *     "class": "DiscardCardsFromDeckSpell",
 *     "value": 60,
 *     "targetPlayer": "SELF"
 *   }
 * </pre>
 * TODO: Fel Reaver currently incorrectly removes random cards from the deck instead of the top cards.
 */
public class DiscardCardsFromDeckSpell extends Spell {

	public static SpellDesc create(int howMany) {
		Map<SpellArg, Object> arguments = new SpellDesc(DiscardCardsFromDeckSpell.class);
		arguments.put(SpellArg.VALUE, howMany);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int howMany = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		for (int i = 0; i < howMany; i++) {
			// Question: If I have no cards left and my Fel Reaver discards 3,
			// do I draw 3 Fatigues or do I only Fatigue more when I draw a
			// card?
			// Answer: Fel Reaver won't trigger fatigue
			// Source: Blue post
			if (player.getDeck().isEmpty()) {
				return;
			}
			Card card = context.getLogic().getRandom(player.getDeck());
			context.getLogic().removeCard(card);
		}
	}

}
