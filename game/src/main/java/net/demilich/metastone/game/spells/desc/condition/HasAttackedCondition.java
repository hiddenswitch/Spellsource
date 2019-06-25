package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HasAttackedCondition extends Condition {

	public HasAttackedCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		// TODO: This target and filter logic really needs to be hoisted up to the general condition logic
		List<Entity> targets;
		if (desc.containsKey(ConditionArg.TARGET)) {
			targets = context.resolveTarget(player, source, (EntityReference) desc.get(ConditionArg.TARGET));
		} else {
			targets = Collections.singletonList(target);
		}

		if (desc.containsKey(ConditionArg.FILTER)) {
			targets = targets.stream()
					.filter(((EntityFilter) desc.get(ConditionArg.FILTER)).matcher(context, player, source))
					.collect(Collectors.toList());
		}

		if (!targets.isEmpty()) {
			return targets.stream().anyMatch(t -> (int) t.getAttributes().getOrDefault(Attribute.ATTACKS_THIS_TURN, 0) > 0);
		}
		return false;
	}
}
