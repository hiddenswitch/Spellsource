package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DiscardEvent;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class DiscardTrigger extends EventTrigger {

	public DiscardTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		DiscardEvent discardEvent = (DiscardEvent) event;
		EntityReference target = (EntityReference) getDesc().get(EventTriggerArg.TARGET);
		TargetPlayer targetPlayer = (TargetPlayer) getDesc().get(EventTriggerArg.TARGET_PLAYER);


		final int owner = host.getOwner();

		boolean targetPlayerSatisfied = targetPlayer == null
				|| (targetPlayer == TargetPlayer.SELF && owner == event.getTargetPlayerId())
				|| (targetPlayer == TargetPlayer.OWNER && owner == event.getTargetPlayerId());

		boolean targetSatisfied = target == null;

		if (target != null) {
			List<Entity> resolvedTargets = event.getGameContext().resolveTarget(event.getGameContext().getPlayer(owner), host, target);
			targetSatisfied = resolvedTargets != null && resolvedTargets.stream().anyMatch(e -> e.getId() == discardEvent.getSourceCard().getId());
		}

		return targetPlayerSatisfied && targetSatisfied;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DISCARD;
	}

}

