package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.ISpellConditionChecker;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.trigger.CardReceivedTrigger;

/**
 * When this is in play, the player can only afford to play a card if {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#CAN_AFFORD_CONDITION}
 * is met. If it is, the {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#APPLY_EFFECT} will be cast with the
 * card as the {@code target}.
 */
public class CardCostInsteadAura extends Aura {

	public CardCostInsteadAura(AuraDesc desc) {
		super(desc);
		this.applyAuraEffect = NullSpell.create();
		this.removeAuraEffect = NullSpell.create();
		this.triggers.add(CardReceivedTrigger.create());
	}

	public int getAmountOfCurrency(GameContext context, Player player, Entity target, Entity host) {
		return getDesc().getValue(AuraArg.AMOUNT_OF_CURRENCY, context, player, target, host, 0);
	}

	public Condition getCanAffordCondition() {
		return (Condition) getDesc().get(AuraArg.CAN_AFFORD_CONDITION);
	}

	public SpellDesc getPayEffect() {
		return (SpellDesc) getDesc().get(AuraArg.PAY_EFFECT);
	}
}
