package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;

/**
 * The player equipped a weapon.
 */
public final class WeaponEquippedEvent extends CardEvent {

	public WeaponEquippedEvent(GameContext context, Weapon weapon, Card source) {
		super(GameEvent.EventTypeEnum.WEAPON_EQUIPPED, true, context, context.getPlayer(weapon.getOwner()), source, weapon, source);
	}
}
