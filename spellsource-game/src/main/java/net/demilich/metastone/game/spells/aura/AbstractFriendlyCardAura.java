package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardPlayedTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Configures an aura with the appropriate trigger to include friendly cards, including cards that are currently being
 * played, in its affected entities list.
 *
 * @see net.demilich.metastone.game.targeting.EntityReference#FRIENDLY_CARDS
 * @see net.demilich.metastone.game.cards.Attribute#BEING_PLAYED
 * @see Aura#getAffectedEntities()
 */
public abstract class AbstractFriendlyCardAura extends EffectlessAura {

	public AbstractFriendlyCardAura(AuraDesc desc) {
		super(desc);
		addFriendlyCardTriggers(this);
	}

	public static void addFriendlyCardTriggers(Aura abstractFriendlyCardAura) {
		EventTriggerDesc cardPlayedTrigger = CardPlayedTrigger.create();
		cardPlayedTrigger.put(EventTriggerArg.TARGET_PLAYER, TargetPlayer.SELF);
		abstractFriendlyCardAura.getTriggers().add(cardPlayedTrigger.create());
	}

	@Override
	protected EntityReference getTargets() {
		return (EntityReference) getDesc().getOrDefault(AuraArg.TARGET, EntityReference.FRIENDLY_CARDS);
	}
}
