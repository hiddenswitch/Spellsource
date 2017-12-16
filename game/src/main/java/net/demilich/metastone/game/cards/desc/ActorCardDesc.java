package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public abstract class ActorCardDesc extends CardDesc {
	public BattlecryDesc battlecry;
	public SpellDesc deathrattle;
	public TriggerDesc trigger;
	public TriggerDesc[] triggers;
	public AuraDesc aura;
	public Race race;
	public CardCostModifierDesc cardCostModifier;

	public BattlecryAction getBattlecryAction() {
		if (battlecry == null) {
			return null;
		}
		BattlecryAction battlecryAction = BattlecryAction.createBattlecry(battlecry.spell, battlecry.getTargetSelection());
		if (battlecry.condition != null) {
			battlecryAction.setCondition(battlecry.condition.create());
		}
		return battlecryAction;
	}
}
