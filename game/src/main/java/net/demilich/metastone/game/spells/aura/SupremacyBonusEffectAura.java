package net.demilich.metastone.game.spells.aura;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

public final class SupremacyBonusEffectAura extends Aura {

	public SupremacyBonusEffectAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
	}
}
