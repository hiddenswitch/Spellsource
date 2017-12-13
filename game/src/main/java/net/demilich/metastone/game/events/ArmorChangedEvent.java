package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;

public final class ArmorChangedEvent extends GameEvent implements HasValue {
	private final Hero hero;
	private final int armor;

	public ArmorChangedEvent(GameContext context, Hero hero, int armor) {
		super(context, hero.getOwner(), -1);
		this.hero = hero;
		this.armor = armor;
	}

	@Override
	public Entity getEventTarget() {
		return hero;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.ARMOR_GAINED;
	}

	@Override
	public int getValue() {
		return armor;
	}
}
