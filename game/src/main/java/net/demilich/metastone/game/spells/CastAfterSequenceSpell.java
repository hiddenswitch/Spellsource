package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Casts the subspell after the sequence has ended.
 * <p>
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
 *
 * @see ForceDeathPhaseSpell for an alternative way to "clean up" the board during a spell's execution.
 */
public final class CastAfterSequenceSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(CastAfterSequenceSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc spell = desc.getSpell();
		// Pass a card to the sub spell. Addresses an issue where a change hero spell performed in a discover action causes
		// the source to be removed from play, breaking the null "not chosen" spells to fail.
		if (desc.containsKey(SpellArg.CARD) && !spell.containsKey(SpellArg.CARD)) {
			spell = spell.clone();
			spell.put(SpellArg.CARD, desc.get(SpellArg.CARD));
		}

		// Special casing spell target
		if (spell.containsKey(SpellArg.TARGET)
				&& spell.get(SpellArg.TARGET).equals(EntityReference.SPELL_TARGET)
				&& target != null) {
			spell = spell.addArg(SpellArg.TARGET, target.getReference());
		} else if (spell.containsKey(SpellArg.TARGET) && spell.getTarget().isTargetGroup()) {
			// If the subspell contains a group target that resolves to a single target, resolve it now.
			try {
				spell = spell.addArg(SpellArg.TARGET, context.resolveSingleTarget(player, source, spell.getTarget()).getReference());
			} catch (ArrayStoreException notSingleTarget) {
				LOGGER.warn("onCast {} {}: Passed a non-single-target target group {}, not resolving", context.getGameId(), source, spell.getTarget());
			}
		}

		EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
		enchantmentDesc.spell = spell;
		enchantmentDesc.maxFires = 1;
		enchantmentDesc.eventTrigger = new EventTriggerDesc(WillEndSequenceTrigger.class);
		context.getLogic().addGameEventListener(player, enchantmentDesc.create(), player);
	}
}
