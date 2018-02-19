package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.TargetPlayer;

public class HeroClassCondition extends Condition {

	public HeroClassCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		HeroClass heroClass = (HeroClass) desc.get(ConditionArg.HERO_CLASS);
		TargetPlayer targetPlayer = (TargetPlayer) desc.getOrDefault(ConditionArg.TARGET_PLAYER, TargetPlayer.SELF);
		Player opponent = context.getOpponent(player);
		switch (targetPlayer) {
			case BOTH:
				return player.getHero().getSourceCard().hasHeroClass(heroClass)
						&& opponent.getHero().getSourceCard().hasHeroClass(heroClass);
			case OPPONENT:
				return opponent.getHero().getSourceCard().hasHeroClass(heroClass);
			case ACTIVE:
				return context.getActivePlayer().getHero().getSourceCard().hasHeroClass(heroClass);
			case INACTIVE:
				return context.getOpponent(context.getActivePlayer()).getHero().getSourceCard().hasHeroClass(heroClass);
			case OWNER:
				return context.getPlayer(target.getOwner()).getHero().getSourceCard().hasHeroClass(heroClass);
			case SELF:
			default:
				return player.getHero().getSourceCard().hasHeroClass(heroClass);
		}

	}
}
