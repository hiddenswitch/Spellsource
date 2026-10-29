package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} if the player's champion is {@link ConditionArg#HERO_CLASS}.
 */
public class HeroClassCondition extends Condition {

	public HeroClassCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			String heroClass) {
		return player.getHero().getSourceCard().hasHeroClass(heroClass);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
