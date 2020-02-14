package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class DamageCausedTrigger extends EventTrigger {

	public DamageCausedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		DamageEvent damageEvent = (DamageEvent) event;

		CardType cardType = (CardType) getDesc().get(EventTriggerArg.CARD_TYPE);
		if (cardType != null) {
			if (damageEvent.getSource() instanceof Card && !((Card) damageEvent.getSource()).getCardType().isCardType(cardType)) {
				return false;
			} else if (damageEvent.getSource() instanceof Actor && !((Actor) damageEvent.getSource()).getSourceCard().getCardType().isCardType(cardType)) {
				return false;
			} else if (damageEvent.getSource() instanceof Player) {
				return false;
			}
		}

		return damageEvent.getDamage() > 0;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DAMAGE;
	}

}
