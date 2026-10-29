package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.spells.Default;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.ReservoirsAlwaysActiveAura;
import net.demilich.metastone.game.spells.aura.ReservoirsNeverActiveAura;

import java.util.Comparator;
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

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			Integer value1,
			@Default(15) Integer value2,
			@Default(20) int value) {
		var forced = reservoirsForced(context, player, source);
		if (forced != null) {
			return forced;
		}

		int deckSize = player.getDeck().size();
		if (value1 != null && value2 != null) {
			return deckSize >= value1 && deckSize <= value2;
		} else {
			return deckSize >= value;
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
