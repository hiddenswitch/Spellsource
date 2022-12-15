package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Gives the {@code target} the {@link SpellArg#SPELL} as a deathrattle.
 * <p>
 * Resolves the {@link SpellArg#SECONDARY_TARGET} and puts it as the {@link SpellArg#TARGET} of the {@link
 * SpellArg#SPELL} (the deathrattle).
 * <p>
 * This is useful for "saving" the player's choices or a state of the board. For example, to implement a battlecry,
 * "Battlecry: Choose a minion. Deathrattle: Destroy it.":
 * <pre>
 *   "battlecry" {
 *     "targetSelection": "MINIONS",
 *     "spell": {
 *       "class": "AddDeathrattleSecondaryAsTargetSpell",
 *       "secondaryTarget": "TARGET",
 *       "target": "SELF",
 *       "spell": {
 *         "class": "DestroySpell"
 *       }
 *     }
 *   },
 *   "deathrattle": {
 *     "class": "NullSpell"
 *   },
 *   "attributes": {
 *     "BATTLECRY": true,
 *     "DEATHRATTLES": true
 *   }
 * </pre>
 * Observe that the {@code "secondaryTarget"} is {@link net.demilich.metastone.game.targeting.EntityReference#TARGET},
 * which is the player's chosen target (the minion). This effect then resolves {@link
 * net.demilich.metastone.game.targeting.EntityReference#TARGET} and puts it into the {@link DestroySpell}'s {@link
 * SpellArg#TARGET}. Observe also that the minion's {@code "deathrattle"} is a {@link NullSpell}; if other effects try
 * to cast its deathrattle from the deck, for example, it will correctly do nothing.
 *
 * @see AddDeathrattleSpell for more about adding deathrattles.
 */
public final class AddDeathrattleSecondaryAsTargetSpell extends AddDeathrattleSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		desc = desc.clone();
		SpellDesc deathrattle = desc.getSpell();
		deathrattle.put(SpellArg.TARGET, context.resolveSingleTarget(player, source, desc.getSecondaryTarget()).getReference());
		desc.remove(SpellArg.SECONDARY_TARGET);
		super.onCast(context, player, desc, source, target);
	}
}

