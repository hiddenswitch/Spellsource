package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class SpellTargetsAdjacentAura extends Aura {

	private static final long serialVersionUID = -6885841304807846414L;

	public SpellTargetsAdjacentAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		setDesc(desc);
	}
}
