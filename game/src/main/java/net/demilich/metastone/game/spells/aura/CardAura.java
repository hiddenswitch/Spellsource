package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.SetCardSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Changes the targeted card's card ID to match the specified {@link AuraArg#CARD}, allowing its behaviour to change.
 *
 * <p>
 * For example, to change your hero power to DIE, INSECT while the aura's host actor is in play:
 * <pre>
 *   {
 *     "class": "CardAura",
 *     "target": "FRIENDLY_HERO_POWER",
 *     "card": "hero_power_die_insect"
 *   }
 * </pre>
 * <p>
 * This aura has not been tested with any targets besides hero powers ({@link EntityReference#FRIENDLY_HERO_POWER} and
 * {@link EntityReference#ENEMY_HERO_POWER}). It should work correctly with spells played from the hand.
 */
public class CardAura extends Aura {

	public CardAura(AuraDesc desc) {
		super(desc.getSecondaryTrigger() != null ? desc.getSecondaryTrigger().create() : new WillEndSequenceTrigger(),
				SetCardSpell.create((String) desc.get(AuraArg.CARD), true),
				SetCardSpell.revert(true),
				desc.getTarget(),
				(EntityFilter) desc.get(AuraArg.FILTER),
				desc.getCondition());
		setDesc(desc);
	}
}

