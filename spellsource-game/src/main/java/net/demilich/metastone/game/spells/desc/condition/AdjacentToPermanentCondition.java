package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * {@code true} if the {@code source} (or the enchantment host) is adjacent to a {@link
 * net.demilich.metastone.game.cards.Attribute#PERMANENT}, which is ordinarily untargetable.
 */
public final class AdjacentToPermanentCondition extends Condition {

	public AdjacentToPermanentCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (source == null) {
			source = context.resolveSingleTarget(player, null, EntityReference.TRIGGER_HOST);
		}
		// use the owner for these zone comparisons
		player = context.getPlayer(source.getOwner());

		if (source.getZone() != Zones.BATTLEFIELD) {
			return false;
		}

		if (player.getMinions().size() > source.getIndex() + 1 && player.getMinions().get(source.getIndex() + 1).hasAttribute(Attribute.PERMANENT)) {
			return true;
		}

		if (source.getIndex() - 1 > 0 && player.getMinions().get(source.getIndex() - 1).hasAttribute(Attribute.PERMANENT)) {
			return true;
		}

		return false;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
