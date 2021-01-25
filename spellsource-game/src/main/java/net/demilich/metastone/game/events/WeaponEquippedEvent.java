package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.weapons.Weapon;

/**
 * The player equipped a weapon.
 */
public final class WeaponEquippedEvent extends CardEvent {

	public WeaponEquippedEvent(GameContext context, Weapon weapon, Card source) {
		super(GameEventType.WEAPON_EQUIPPED, true, context, context.getPlayer(weapon.getOwner()), source, weapon, source);
	}
}
