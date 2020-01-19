package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Modifies {@link net.demilich.metastone.game.entities.minions.Race#hasRace(GameContext, Entity, String)} calls to
 * behave as though the entity being compared (typically the left hand side of the comparison) is also every race
 * specified in {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#RACES}.
 */
public final class MenagerieMogulAura extends Aura {

	public MenagerieMogulAura(AuraDesc desc) {
		super(desc);
		setDesc(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}

	public String[] getRaces() {
		return (String[]) getDesc().get(AuraArg.RACES);
	}
}

