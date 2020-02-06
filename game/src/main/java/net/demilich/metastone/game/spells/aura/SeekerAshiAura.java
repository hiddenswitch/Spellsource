package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforePhysicalAttackEvent;
import net.demilich.metastone.game.events.DidEndSequenceEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

import java.util.HashMap;

/**
 * Casts {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#SPELL} with {@link
 * net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider} returning the amount of damage dealt.
 */
public final class SeekerAshiAura extends Aura {
	private HashMap<Integer, Integer> damageAtStart = new HashMap<>();

	public SeekerAshiAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	public SeekerAshiAura clone() {
		var clone = (SeekerAshiAura) super.clone();
		clone.damageAtStart = new HashMap<>(damageAtStart);
		return clone;
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		if (event.getSource() != null && !getAffectedEntities().contains(event.getSource().getId())) {
			return;
		}
		// Case 1: this is before the physical attack
		if (event instanceof BeforePhysicalAttackEvent) {
			damageAtStart.put(event.getSource().getId(), event.getSource().getAttributeValue(Attribute.TOTAL_DAMAGE_DEALT_THIS_GAME));
		} else if (event instanceof AfterPhysicalAttackEvent) {
			var dealtThisGame = event.getSource().getAttributeValue(Attribute.TOTAL_DAMAGE_DEALT_THIS_GAME);
			var damageDealt = dealtThisGame - damageAtStart.getOrDefault(event.getSource().getId(), dealtThisGame);
			var context = event.getGameContext();
			var player = context.getPlayer(event.getSourcePlayerId());
			var host = context.resolveSingleTarget(getHostReference());
			context.getEventValueStack().push(damageDealt);
			SpellUtils.castChildSpell(context, player, getDesc().getSpell(), host, event.getTarget());
			context.getEventValueStack().pop();
		} else if (event instanceof DidEndSequenceEvent) {
			damageAtStart.clear();
		}
	}
}
