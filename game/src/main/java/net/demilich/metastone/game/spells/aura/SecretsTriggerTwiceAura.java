package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * When this aura is active, spells from {@link net.demilich.metastone.game.spells.trigger.secrets.Secret}s will be performed twice
 */
public class SecretsTriggerTwiceAura extends Aura {


    public SecretsTriggerTwiceAura(AuraDesc desc) {
        super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
    }

    @Override
    @Suspendable
    public void onGameEvent(GameEvent event) {
    }
}
