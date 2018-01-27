package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.AuraBuffSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

public class BuffAura extends Aura {

	public BuffAura(AuraDesc desc) {
		this(desc.getInt(AuraArg.ATTACK_BONUS), desc.getInt(AuraArg.HP_BONUS), desc.getTarget(), desc.getFilter());
	}

	public BuffAura(int attackBonus, int hpBonus, EntityReference targetSelection, EntityFilter filter) {
		/*
		 Rule 4a: After the outermost Phase ends, Hearthstone does an Aura Update (Health/Attack), then does the Death
		 Creation Step (Looks for all mortally wounded (0 or less Health)/pending destroy (hit with a destroy effect)
		 Entities and kills them, removing them from play simultaneously), then does an Aura Update (Other). Entities
		 that have been removed from play cannot trigger, be triggered, or emit auras, and do not take up space.
		 */
		super(new WillEndSequenceTrigger(), AuraBuffSpell.create(attackBonus, hpBonus), AuraBuffSpell.create(-attackBonus, -hpBonus), targetSelection);
		this.setEntityFilter(filter);
	}
}
