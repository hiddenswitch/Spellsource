package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * Gives spells decorated with {@link net.demilich.metastone.game.spells.HeroPowerSpell} the given bonus affect in
 * {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#APPLY_EFFECT}.
 */
public class HeroPowerBonusAura extends Aura {

	private static final long serialVersionUID = -4063552480519313767L;

	public HeroPowerBonusAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		setDesc(desc);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
	}
}
