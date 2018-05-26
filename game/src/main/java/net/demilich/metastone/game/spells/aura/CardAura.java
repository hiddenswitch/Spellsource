package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.SetCardSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * Changes the targeted card's card ID to match the specified {@link AuraArg#CARD_ID}, allowing its behaviour to change.
 *
 * For example, to change your hero power to DIE, INSECT while the aura's host actor is in play:
 * <pre>
 *   {
 *     "class": "CardAura",
 *     "target": "FRIENDLY_HERO_POWER",
 *     "cardId": "hero_power_die_insect"
 *   }
 * </pre>
 */
public class CardAura extends Aura {

	public CardAura(AuraDesc desc) {
		super(desc.getSecondaryTrigger() != null ? desc.getSecondaryTrigger().create() : new WillEndSequenceTrigger(),
				SetCardSpell.create((String) desc.get(AuraArg.CARD_ID), true),
				SetCardSpell.revert(true),
				desc.getTarget(),
				(EntityFilter) desc.get(AuraArg.FILTER),
				desc.getCondition());
		setDesc(desc);
	}
}

