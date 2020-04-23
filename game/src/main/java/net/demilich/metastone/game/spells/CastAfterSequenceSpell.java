package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.DidEndSequenceTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

	public static SpellDesc create(SpellDesc spell) {
		SpellDesc desc = new SpellDesc(CastAfterSequenceSpell.class);
		desc.put(SpellArg.SPELL, spell);
		return desc;
	}

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
		for (SpellArg targetAttribute : new SpellArg[]{SpellArg.TARGET, SpellArg.SECONDARY_TARGET}) {
			if (spell.containsKey(targetAttribute)
					&& spell.get(targetAttribute).equals(EntityReference.SPELL_TARGET)
					&& target != null) {
				spell = spell.addArg(targetAttribute, target.getReference());
			} else if (spell.containsKey(targetAttribute) && spell.getTarget().isTargetGroup()) {
				// If the subspell contains a group target that resolves to a single target, resolve it now.
				List<Entity> targets = context.resolveTarget(player, source, (EntityReference) spell.get(targetAttribute));
				// The targets may not be on the board at the end of the sequence!
				final SpellDesc finalSpell = spell;
				spell = MetaSpell.create(targets.stream()
						.map(t -> finalSpell.addArg(targetAttribute, t.getReference()))
						.toArray(SpellDesc[]::new));
			}
		}

		// Special casing a summon in a deathrattle context so that it has the appropriate board position
		if (SummonSpell.class.isAssignableFrom(spell.getDescClass())
				&& desc.containsKey(SpellArg.BOARD_POSITION_ABSOLUTE)) {
			spell = spell.addArg(SpellArg.BOARD_POSITION_ABSOLUTE, desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE));
		}

		EnchantmentDesc enchantmentDesc = new EnchantmentDesc();
		enchantmentDesc.setSpell(spell);
		enchantmentDesc.setMaxFires(1);
		enchantmentDesc.setEventTrigger(new EventTriggerDesc(DidEndSequenceTrigger.class));
		Enchantment gameEventListener = enchantmentDesc.create();
		gameEventListener.setSourceCard(source.getSourceCard());
		context.getLogic().addEnchantment(player, gameEventListener, source, player);
	}
}
