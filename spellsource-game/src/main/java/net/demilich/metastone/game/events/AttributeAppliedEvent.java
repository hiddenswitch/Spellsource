package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

/**
 * An attribute was applied to the {@code target}.
 */
public class AttributeAppliedEvent extends BasicGameEvent {
	private final Attribute attribute;

	public AttributeAppliedEvent(GameContext context, int targetPlayerId, Entity target, Entity source, Attribute attribute) {
		super(GameEventType.ATTRIBUTE_APPLIED, context, source, target, targetPlayerId, source != null ? source.getOwner() : -1);
		this.attribute = attribute;
	}

	public Attribute getAttribute() {
		return attribute;
	}
}

