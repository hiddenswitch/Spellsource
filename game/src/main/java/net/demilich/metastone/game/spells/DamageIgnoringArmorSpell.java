package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.DamageType;

/**
 * Deals damage to the specified actor, bypassing
 */
public final class DamageIgnoringArmorSpell extends DamageSpell {

	@Override
	protected DamageType getDamageType() {
		return DamageType.IGNORES_ARMOR;
	}
}
