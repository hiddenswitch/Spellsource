package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

public class AttributeAppliedEvent extends GameEvent {
	private final Entity target;
	private final Entity source;
	private final Attribute attribute;

	public AttributeAppliedEvent(GameContext context, int targetPlayerId, Entity target, Entity source, Attribute attribute) {
		super(context, targetPlayerId, source == null ? -1 : source.getOwner());
		this.target = target;
		this.source = source;
		this.attribute = attribute;
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ATTRIBUTE_APPLIED;
	}

	@Override
	public Entity getEventSource() {
		return source;
	}

	public Attribute getAttribute() {
		return attribute;
	}
}

