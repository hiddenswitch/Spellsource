package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.TargetAcquisitionEvent;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.TargetAcquisitionTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * Noggenfogger auras override the target acquisitions of both players whenever {@link #getValidTargets(GameContext,
 * TargetAcquisitionEvent)} returns a non-empty list of entities.
 * <p>
 * This aura's {@code getValidTargets} method can be overridden to support other kinds of retargeting.
 */
public class NoggenfoggerAura extends Aura {

	public NoggenfoggerAura(AuraDesc desc) {
		super(desc);
		setTriggers(Collections.singletonList(new TargetAcquisitionTrigger()));
	}

	@Override
	public void onGameEvent(GameEvent originalEvent) {
		TargetAcquisitionEvent event = (TargetAcquisitionEvent) originalEvent;
		if (event.getAction() == null) {
			throw new NullPointerException("All target acquisition events now should come in with valid actions.");
		}

		GameContext gc = originalEvent.getGameContext();
		List<Entity> validTargets;

		validTargets = getValidTargets(gc, event);

		if (validTargets.size() == 0) {
			// An earlier event removed all valid targets
			return;
		}
		gc.setTargetOverride(gc.getLogic().getRandom(validTargets).getReference());
	}


	/**
	 * Based on the specified target acquisition event, override the target to a random one from the list returned by this
	 * method. If the targets are empty, no override occurs.
	 *
	 * @param context The context
	 * @param event   The event
	 * @return A {@link List} of entities to choose randomly from, or an empty list if there are no valid targets
	 * 		/ an override should not occur.
	 */
	protected List<Entity> getValidTargets(GameContext context, TargetAcquisitionEvent event) {
		List<Entity> validTargets;
		if (event.getAction().getActionType() == ActionType.PHYSICAL_ATTACK) {
			// Noggenfogger ignores taunt and stealth but doesn't choose friendlies
			Player sourcePlayer = context.getPlayer(event.getSourcePlayerId());
			validTargets = context.resolveTarget(sourcePlayer, event.getAction().getSource(context), EntityReference.ENEMY_CHARACTERS);
		} else {
			validTargets = context.getLogic().getValidTargets(event.getSourcePlayerId(), event.getAction());
		}
		return validTargets;
	}
}

