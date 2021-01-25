package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * When this aura is active, spells from {@link net.demilich.metastone.game.spells.trigger.secrets.Secret} will be
 * performed twice
 */
public class SecretsTriggerTwiceAura extends EffectlessAura {

	public SecretsTriggerTwiceAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	protected EntityReference getTargets() {
		return EntityReference.FRIENDLY_SECRETS;
	}
}
