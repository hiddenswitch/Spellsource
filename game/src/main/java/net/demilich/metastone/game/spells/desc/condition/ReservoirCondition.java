package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.ReservoirsAlwaysActiveAura;
import net.demilich.metastone.game.spells.aura.ReservoirsNeverActiveAura;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@code true} when the number of cards in the player's deck is greater or equal to the {@link ConditionArg#VALUE}.
 * <p>
 * If two values are specified, evalutes to {@code true} if the number of cards in the player's deck is equal to or
 * between the two values.
 */
public class ReservoirCondition extends Condition {

	public ReservoirCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var forced = reservoirsForced(context, player, source);
		if (forced != null) {
			return forced;
		}

		if (desc.containsKey(ConditionArg.VALUE1) && desc.containsKey(ConditionArg.VALUE2)) {
			return player.getDeck().size() >= desc.getValue(ConditionArg.VALUE1, context, player, target, source, 0)
					&& player.getDeck().size() <= desc.getValue(ConditionArg.VALUE2, context, player, target, source, 15);
		} else {
			return player.getDeck().size() >= desc.getValue(ConditionArg.VALUE, context, player, target, source, 20);
		}
	}

	public static Boolean reservoirsForced(GameContext context, Player player, Entity source) {
		var activeAuras = SpellUtils.getAuras(context, player.getId(), ReservoirsAlwaysActiveAura.class);
		var inactiveAuras = SpellUtils.getAuras(context, player.getId(), ReservoirsNeverActiveAura.class);
		return Stream.concat(activeAuras.stream(), inactiveAuras.stream())
				.min(Comparator.comparingInt(aura -> aura.getHostReference().getId()))
				.map(aura -> aura instanceof ReservoirsAlwaysActiveAura)
				.orElse(null);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
