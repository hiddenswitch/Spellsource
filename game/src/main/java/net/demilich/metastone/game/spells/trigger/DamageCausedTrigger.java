package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class DamageCausedTrigger extends EventTrigger {

	public DamageCausedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		DamageEvent damageEvent = (DamageEvent) event;

		CardType cardType = (CardType) getDesc().get(EventTriggerArg.CARD_TYPE);
		if (cardType != null) {
			if (damageEvent.getSource() instanceof Card && !GameLogic.isCardType(((Card) damageEvent.getSource()).getCardType(), cardType)) {
				return false;
			} else if (damageEvent.getSource() instanceof Actor && !GameLogic.isCardType(((Actor) damageEvent.getSource()).getSourceCard().getCardType(), cardType)) {
				return false;
			} else if (damageEvent.getSource() instanceof Player) {
				return false;
			}
		}

		return damageEvent.getValue() > 0;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DAMAGE;
	}

}
