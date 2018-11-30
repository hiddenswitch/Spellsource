package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;
import java.util.stream.Collectors;

public class EventTargetMatchesReferenceCondition extends Condition {
    public EventTargetMatchesReferenceCondition(ConditionDesc desc) {
        super(desc);
    }

    @Override
    protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
        Entity eventTarget = context.resolveSingleTarget(EntityReference.EVENT_TARGET);
        List<Entity> targets = context.resolveTarget(player, context.resolveTarget(player, source, EntityReference.TRIGGER_HOST).get(0), (EntityReference) desc.get(ConditionArg.TARGET));
        return targets.stream().map(Entity::getId).collect(Collectors.toList()).contains(eventTarget.getId());
    }
}
