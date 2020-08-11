package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.List;

public class TargetSelectionFilter extends EntityFilter {
	public TargetSelectionFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		var card = entity.getSourceCard();

		if (getDesc().containsKey(EntityFilterArg.SECONDARY_TARGET) && card.isSpell()) {
			Entity target = context.resolveSingleTarget(player, host, (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET));

			if (card.isSpell() && !card.canBeCastOn(context, player, target)) {
				return false;
			}
			TargetSelection targetSelection = card.getTargetSelection();
			List<Entity> entities = context.getTargetLogic().getEntities(context, player, targetSelection, true);
			return entities.stream().anyMatch(e -> e.getId() == target.getId());
		}

		var targetSelection = (TargetSelection) getDesc().get(EntityFilterArg.TARGET_SELECTION);

		if (targetSelection == null) {
			targetSelection = TargetSelection.NONE;
		}

		if (card.hasBattlecry()) {
			return card.getDesc().getBattlecry().getTargetSelection() == targetSelection;
		} else {
			return card.getTargetSelection() == targetSelection;
		}
	}
}
