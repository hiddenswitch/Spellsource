package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class AfterDamagingSpellCastedTrigger extends AfterSpellCastedTrigger {

	public AfterDamagingSpellCastedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		if (!super.innerQueues(event, host)) {
			return false;
		}

		Card card = ((HasCard) event).getSourceCard();
		return card.getDesc().bfs()
				.build()
				.anyMatch(node -> node.getKey().equals(SpellArg.CLASS)
						&& DamageSpell.class.isAssignableFrom((Class) node.getValue()));
	}
}
