package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.DamageType;

/**
 * Deals damage to the specified actor, bypassing its armor.
 */
public final class DamageIgnoringArmorSpell extends DamageSpell {

	@Override
	protected DamageType getDamageType(GameContext context, Player player, Entity source) {
		return DamageType.IGNORES_ARMOR;
	}
}
