package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * Casts the subspell after the sequence has ended.
 *
 * Equivalent to the following:
 * <pre>
 *   {
 *     "class": "AddEnchantmentSpell",
 *     "target": "FRIENDLY_PLAYER"
 *     "trigger": {
 *       "eventTrigger": {
 *         "class": "WillEndSequenceTrigger"
 *       },
 *       "spell": {
 *         "class": "NullSpell" // the sub spell
 *       },
 *       "maxFires": 1
 *     }
 *   }
 * </pre>
 * @see ForceDeathPhaseSpell for an alternative way to "clean up" the board during a spell's execution.
 */
public class CastAfterSequenceSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc spell = desc.getSpell();

		EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
		enchantmentDesc.spell = spell;
		enchantmentDesc.maxFires = 1;
		enchantmentDesc.eventTrigger = new EventTriggerDesc(WillEndSequenceTrigger.class);
		context.getLogic().addGameEventListener(player, enchantmentDesc.create(), player);
	}
}
