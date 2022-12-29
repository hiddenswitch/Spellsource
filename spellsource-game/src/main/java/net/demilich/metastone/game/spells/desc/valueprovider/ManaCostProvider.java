package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

public class ManaCostProvider extends ValueProvider {

	public ManaCostProvider(ValueProviderDesc desc) {
		super(desc);
	}

	public static ValueProviderDesc create() {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(ManaCostProvider.class);
		return new ValueProviderDesc(arguments);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference targetOverride = (EntityReference) getDesc().get(ValueProviderArg.TARGET);
		if (targetOverride != null) {
			var entities = context.resolveTarget(player, host, targetOverride);
			if (entities.size() == 0) {
				return 0;
			}
			target = entities.get(0);
		}
		Card targetCard = target.getSourceCard();
		if (targetCard == null) {
			return 0;
		}

		return context.getLogic().getModifiedManaCost(player, targetCard);
	}
}
