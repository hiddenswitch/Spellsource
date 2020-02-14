package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public class AfterSpellCastedEvent extends GameEvent implements HasCard {

	private final Card sourceCard;
	private final Entity spellTarget;

	public AfterSpellCastedEvent(GameContext context, int playerId, Card sourceCard, Entity target) {
		super(context, target == null ? -1 : target.getOwner(), playerId);
		this.sourceCard = sourceCard;
		this.spellTarget = target;
	}

	@Override
	public Entity getEventSource() {
		return getSourceCard();
	}

	@Override
	public Entity getEventTarget() {
		return spellTarget;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_SPELL_CASTED;
	}

	@Override
	public Card getSourceCard() {
		return sourceCard;
	}
}
