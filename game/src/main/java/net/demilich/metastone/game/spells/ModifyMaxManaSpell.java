package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Adds or destroys {@link SpellArg#VALUE} mana crystals. Gives the player full mana crystals when {@link
 * SpellArg#FULL_MANA_CRYSTALS} is {@code true}.
 * <p>
 * This <b>example</b> shows the text "Transform your Mana Crystals into 2/2 minions. Recover the mana when they die."
 * Observe that a {@link net.demilich.metastone.game.spells.desc.valueprovider.GameValueProvider} is used to only
 * destroy as many mana crystals as the player has empty minion spots, but before the minions themselves are summoned.
 * This ensures everything occurs in the right order.
 * <pre>
 *   {
 *     "class": "MetaSpell",
 *     "value": {
 *       "class": "AlgebraicValueProvider",
 *       "operation": "MINIMUM",
 *       "value1": {
 *         "class": "PlayerAttributeValueProvider",
 *         "playerAttribute": "MAX_MANA",
 *         "targetPlayer": "SELF"
 *       },
 *       "value2": {
 *         "class": "AlgebraicValueProvider",
 *         "operation": "SUBTRACT",
 *         "value1": 7,
 *         "value2": {
 *           "class": "EntityCountValueProvider",
 *           "target": "FRIENDLY_MINIONS"
 *         }
 *       }
 *     },
 *     "spells": [
 *       {
 *         "class": "SummonSpell",
 *         "value": {
 *           "class": "GameValueProvider",
 *           "gameValue": "SPELL_VALUE"
 *         },
 *         "card": "token_mana_treant"
 *       },
 *       {
 *         "class": "ModifyMaxManaSpell",
 *         "value": {
 *           "class": "GameValueProvider",
 *           "gameValue": "SPELL_VALUE",
 *           "multiplier": -1
 *         }
 *       }
 *     ]
 * </pre>
 */
public class ModifyMaxManaSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		boolean fullManaCrystals = desc.getBool(SpellArg.FULL_MANA_CRYSTALS);

		context.getLogic().modifyMaxMana(player, value);
		if (fullManaCrystals) {
			context.getLogic().modifyCurrentMana(player.getId(), value, false);
		}
	}
}
