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

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		String heroClass = (String) desc.get(ConditionArg.HERO_CLASS);
		return player.getHero().getSourceCard().hasHeroClass(heroClass);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
