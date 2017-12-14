package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

public class ManaCostProvider extends ValueProvider {

	public ManaCostProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference targetOverride = (EntityReference) desc.get(ValueProviderArg.TARGET);
		if (target == null
				&& targetOverride != null) {
			target = context.resolveSingleTarget(targetOverride);
		}
		Card targetCard = target.getSourceCard();
		if (targetCard == null) {
			return 0;
		}

		return context.getLogic().getModifiedManaCost(player, targetCard);
	}
}
