package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardPlayedTrigger;

/**
 * Actors affected by this aura will get spells cast on them twice if the {@link AuraArg#SPELL_CONDITION} is met on the
 * spell's target.
 */
public class SpellsCastTwiceAura extends AbstractFriendlyCardAura {

	public SpellsCastTwiceAura(AuraDesc desc) {
		super(desc);
	}

	public Condition getSpellCondition() {
		return (Condition) getDesc().get(AuraArg.SPELL_CONDITION);
	}

	public boolean isFulfilled(GameContext context, Player player, Entity card, Entity target) {
		return getSpellCondition() == null || getSpellCondition().isFulfilled(context, player, card, target);
	}
}

