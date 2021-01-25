package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;

import java.util.EnumSet;

/**
 * Deals damage to the specified actor, bypassing its armor.
 */
public final class DamageIgnoringArmorSpell extends DamageSpell {

	@Override
	protected EnumSet<DamageType> getDamageType(GameContext context, Player player, Entity source) {
		return EnumSet.of(DamageType.IGNORES_ARMOR);
	}
}
