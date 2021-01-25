package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.weapons.Weapon;

/**
 * The player destroyed their weapon.
 */
public class WeaponDestroyedEvent extends BasicGameEvent {

	public WeaponDestroyedEvent(GameContext context, Weapon weapon) {
		super(GameEventType.WEAPON_DESTROYED, context, weapon, weapon.getOwner(), -1);
	}
}
