package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Actors affected by this aura will get spells cast on them thrice if the {@link AuraArg#SPELL_CONDITION} is met on the
 * spell's target.
 */
public class SpellsCastThriceAura extends AbstractFriendlyCardAura {

	public SpellsCastThriceAura(AuraDesc desc) {
		super(desc);
	}

	public Condition getSpellCondition() {
		return (Condition) getDesc().get(AuraArg.SPELL_CONDITION);
	}

	public boolean isFulfilled(GameContext context, Player player, Entity card, Entity target) {
		return getSpellCondition() == null || getSpellCondition().isFulfilled(context, player, card, target);
	}
}

