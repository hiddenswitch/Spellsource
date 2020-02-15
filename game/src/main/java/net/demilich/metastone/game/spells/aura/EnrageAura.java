package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.EnrageChangedTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.List;

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
 * 		whose {@link net.demilich.metastone.game.spells.desc.condition.ConditionArg#TARGET} is the {@link
 * 		EntityReference#TRIGGER_HOST} (i.e. the aura host) instead.
 */
@Deprecated
public final class EnrageAura extends Aura {

	private boolean active;

	public EnrageAura(AuraDesc desc) {
		this(desc.getApplyEffect(), desc.getRemoveEffect(), desc.getTarget());
		setDesc(desc);
	}

	private EnrageAura(SpellDesc applyAuraEffect, SpellDesc removeAuraEffect, EntityReference targetSelection) {
		super(new EnrageChangedTrigger(), applyAuraEffect, removeAuraEffect, targetSelection);
	}

	@Override
	protected boolean affects(GameContext context, Player player, Entity target, List<Entity> resolvedTargets) {
		return active && super.affects(context, player, target, resolvedTargets);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		if (event.getEventType() == com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ENRAGE_CHANGED) {
			active = event.getEventTarget().hasAttribute(Attribute.ENRAGED);
		}
		super.onGameEvent(event);
	}

}
