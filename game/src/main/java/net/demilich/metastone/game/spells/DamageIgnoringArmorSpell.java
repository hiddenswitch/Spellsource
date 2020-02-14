package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;

/**
 * Deals damage to the specified actor, bypassing its armor.
 */
public final class DamageIgnoringArmorSpell extends DamageSpell {

	@Override
	protected DamageTypeEnum getDamageType(GameContext context, Player player, Entity source) {
		return DamageTypeEnum.IGNORES_ARMOR;
	}
}
