package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.QuestCard;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class QuestCardDesc extends SpellCardDesc {
	public EventTriggerDesc quest;
	public int countUntilCast;

	@Override
	public Card createInstance() {
		return new QuestCard(this);
	}

}
