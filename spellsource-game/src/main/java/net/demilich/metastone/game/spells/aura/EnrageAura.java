package net.demilich.metastone.game.spells.aura;

import com.google.common.collect.ObjectArrays;
;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.condition.IsDamagedCondition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.EnrageChangedTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * This aura casts its {@link AuraArg#APPLY_EFFECT} only while the aura's host entity is damaged.
 * <p>
 * Implements Spiteful Smith.
 * <p>
 * For example, "Enrage: Your weapon has +2 Attack.":
 * <pre>
 *      {
 * 		    "class": "EnrageAura",
 * 		    "target": "FRIENDLY_WEAPON",
 * 		    "applyEffect": {
 * 		      "class": "BuffSpell",
 * 		      "attackBonus": 2
 *        },
 * 		    "removeEffect": {
 * 		      "class": "BuffSpell",
 * 		      "attackBonus": -2
 *        }
 *      }
 * </pre>
 *
 * @deprecated Use a conventional aura with a {@link net.demilich.metastone.game.spells.desc.condition.IsDamagedCondition}
 * whose {@link net.demilich.metastone.game.spells.desc.condition.ConditionArg#TARGET} is the {@link
 * EntityReference#TRIGGER_HOST} (i.e. the aura host) instead.
 */
@Deprecated
public final class EnrageAura extends AttributeValueAura {

	private static final EventTriggerDesc[] DEFAULT_ENRAGE_TRIGGER = ObjectArrays.concat(new EventTriggerDesc(EnrageChangedTrigger.class), DEFAULT_TRIGGERS);
	private static final Condition ENRAGED_CONDITION = new ConditionDesc(IsDamagedCondition.class).create();

	public EnrageAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	protected EventTriggerDesc[] getDefaultTriggers() {
		return DEFAULT_ENRAGE_TRIGGER;
	}

	@Override
	public Condition getCondition() {
		return ENRAGED_CONDITION;
	}
}
