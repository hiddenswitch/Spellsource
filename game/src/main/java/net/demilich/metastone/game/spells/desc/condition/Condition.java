package net.demilich.metastone.game.spells.desc.condition;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;

/**
 * A condition is used for true or false comparisons in the {@link net.demilich.metastone.game.cards.desc.CardDesc} card
 * JSON.
 * <p>
 * The core function is {@link #isFulfilled(GameContext, Player, Entity, Entity)}.
 */
public abstract class Condition implements Serializable, HasDesc<ConditionDesc> {
	private ConditionDesc desc;

	public Condition(ConditionDesc desc) {
		this.desc = desc;
	}

	@Suspendable
	protected abstract boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target);

	@Suspendable
	public boolean isFulfilled(GameContext context, Player player, Entity source, Entity target) {
		boolean invert = desc.getBool(ConditionArg.INVERT);
		return isFulfilled(context, player, desc, source, target) != invert;
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (ConditionDesc) desc;
	}

	@Override
	public ConditionDesc getDesc() {
		return desc;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Condition)) {
			return false;
		}
		Condition rhs = (Condition) obj;
		return desc.equals(rhs.desc);
	}

	@Override
	public int hashCode() {
		return desc.hashCode();
	}
}
