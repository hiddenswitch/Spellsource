package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Puts the {@code target} that's passed into this spell as the sub spell's {@code source}. Puts the {@link
 * SpellArg#SECONDARY_TARGET} as the sub spell's {@code target}. Casts the sub spell.
 * <p>
 * For example, to implement the card, "Force an enemy to attack another random character except your hero.":
 * <pre>
 *   "targetSelection": "ENEMY_CHARACTERS",
 *   "spell": {
 *     "class": "TargetToSourceSecondaryToTargetSpell",
 *     "spell": {
 *       "class": "FightSpell",
 *       "target": "ALL_OTHER_CHARACTERS",
 *       "filter": {
 *         "class": "AndFilter",
 *         "filters": [
 *           {
 *             "class": "EntityTypeFilter",
 *             "entityType": "HERO"
 *           },
 *           {
 *             "class": "OwnedByPlayerFilter",
 *             "targetPlayer": "OPPONENT"
 *           }
 *         ],
 *         "invert": true
 *       },
 *       "randomTarget": true
 *     }
 *   }
 * </pre>
 * Observe that the player chooses an enemy character. {@link TargetToSourceSecondaryToTargetSpell} puts the chosen
 * target into {@link FightSpell}'s {@code source}, which is interpreted as the fight spell's attacker.
 */
public class TargetToSourceSecondaryToTargetSpell extends Spell {
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity secondTarget = context.resolveSingleTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
		List<SpellDesc> subSpells = desc.subSpells(0);
		if (target == null && (subSpells == null || subSpells.isEmpty())) {
			return;
		}
		for (SpellDesc subSpell : subSpells) {
			SpellUtils.castChildSpell(context, player, subSpell, target, secondTarget);
		}
	}
}
