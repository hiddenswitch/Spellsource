package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.targeting.EntityReference;

public abstract class RevertableSpell extends Spell {

	protected abstract SpellDesc getReverseSpell(SpellDesc desc, EntityReference target);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EventTriggerDesc triggerDesc = (EventTriggerDesc) desc.get(SpellArg.REVERT_TRIGGER);

		if (triggerDesc != null) {
			EventTrigger revertTrigger = triggerDesc.create();
			EventTriggerDesc secondTriggerDesc = (EventTriggerDesc) desc.get(SpellArg.SECOND_REVERT_TRIGGER);
			EventTrigger secondRevertTrigger = secondTriggerDesc != null ? secondTriggerDesc.create() : null;
			SpellDesc revert = getReverseSpell(desc, target.getReference());
			Enchantment removeTrigger = new Enchantment(revertTrigger, secondRevertTrigger, revert, true);
			context.getLogic().addGameEventListener(player, removeTrigger, target);
		}
	}

}
