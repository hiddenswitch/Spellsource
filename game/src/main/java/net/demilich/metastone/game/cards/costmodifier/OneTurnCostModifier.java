package net.demilich.metastone.game.cards.costmodifier;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;

/**
 * A card cost modifier that lasts only one turn.
 */
public final class OneTurnCostModifier extends CardCostModifier {
	public OneTurnCostModifier(CardCostModifierDesc desc) {
		super(desc);
		oneTurn = true;
	}

	@Override
	public boolean oneTurnOnly() {
		return true;
	}
}
