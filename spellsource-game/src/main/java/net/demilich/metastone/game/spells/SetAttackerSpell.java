package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * During a {@link net.demilich.metastone.game.events.TargetAcquisitionEvent} or a {@link
 * net.demilich.metastone.game.events.PhysicalAttackEvent} firing, changes the {@link
 * net.demilich.metastone.game.targeting.EntityReference#ATTACKER} to the specified {@code target}.
 * <p>
 * When using a {@link net.demilich.metastone.game.spells.trigger.TargetAcquisitionTrigger}, stealth is not lost. When
 * using {@link net.demilich.metastone.game.spells.trigger.PhysicalAttackTrigger}, stealth is lost.
 * <p>
 * If the attacker is changed during a physical attack target acquisition, the original attacker will not lose an {@link
 * net.demilich.metastone.game.cards.Attribute#NUMBER_OF_ATTACKS}. The card using this spell is responsible for
 * decrementing the number of attacks in that scenario.
 * <p>
 * For example, to implement the text, "Whenever this attacks, summon a 1/1 Sapling that attacks the target instead:"
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *       "class": "TargetAcquisitionTrigger",
 *       "hostTargetType": "IGNORE_OTHER_SOURCES"
 *     },
 *     "spell": {
 *       "class": "SummonSpell",
 *       "spell": {
 *         "class": "SetAttackerSpell",
 *         "target": "OUTPUT"
 *       },
 *       "card": "token_sapling"
 *     }
 *   }
 * </pre>
 */
public class SetAttackerSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(SetAttackerSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (context.getAttackerReferenceStack().isEmpty()) {
			LOGGER.warn("onCast {} {}: Tried to set attacker while no attacker is available to change", context.getGameId(), source);
			return;
		}

		// Remove the current attacker
		context.getAttackerReferenceStack().pollFirst();

		// Set target to be the new attacker
		context.getAttackerReferenceStack().addFirst(target.getReference());
	}
}
