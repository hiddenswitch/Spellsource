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

	protected boolean test(GameContext context, Player player, Entity entity, Entity host, EntityReference secondaryTarget, TargetSelection targetSelection) {
		var card = entity.getSourceCard();

		if (secondaryTarget != null && card.isSpell()) {
			Entity target = context.resolveSingleTarget(player, host, secondaryTarget);

			if (target == null || (card.isSpell() && !card.canBeCastOn(context, player, target))) {
				return false;
			}
			TargetSelection cardTargetSelection = card.getTargetSelection();
			List<Entity> entities = context.getTargetLogic().getEntities(context, player, cardTargetSelection, true);

			return entities.stream().anyMatch(e -> e.getId() == target.getId());
		}

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
