package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DrawCardEvent;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardDrawnTrigger extends AbstractCardTrigger {

	public CardDrawnTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		DrawCardEvent drawEvent = (DrawCardEvent) event;

		if (!drawEvent.isDrawn()) {
			return false;
		}

		return super.innerQueues(event, host);
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DRAW_CARD;
	}

}
