package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.TargetSelection;

public class TargetSelectionFilter extends EntityFilter {
	public TargetSelectionFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		var card = entity.getSourceCard();

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
