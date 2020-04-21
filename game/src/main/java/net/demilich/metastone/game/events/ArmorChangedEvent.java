package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;

import static com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ARMOR_GAINED;

/**
 * The amount of armor on the hero has changed. {@link #getValue()} / {@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider}
 * will return the change in armor.
 * <p>
 * The armor has been modified at this point.
 */
public final class ArmorChangedEvent extends ValueEvent {
	public ArmorChangedEvent(GameContext context, Hero hero, int armor) {
		super(ARMOR_GAINED, context, hero.getOwner(), hero, hero, armor);
	}
}
