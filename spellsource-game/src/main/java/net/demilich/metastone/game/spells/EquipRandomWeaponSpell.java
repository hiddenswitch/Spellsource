package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * @deprecated by {@link EquipWeaponSpell}.
 */
@Deprecated
public class EquipRandomWeaponSpell extends EquipWeaponSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		super.onCast(context, player, EquipWeaponSpell.create(), source, target);
	}

}
