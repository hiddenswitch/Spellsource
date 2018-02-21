package net.demilich.metastone.game.spells;

import java.util.Map;
import java.util.stream.Stream;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeHeroSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(ChangeHeroSpell.class);

	public static SpellDesc create(String heroCardId) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ChangeHeroSpell.class);
		arguments.put(SpellArg.CARD, heroCardId);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String heroCardId = (String) desc.get(SpellArg.CARD);
		if (heroCardId == null) {
			logger.error("onCast {} {}: Requires hero card ID, none specified.", context.getGameId(), source);
			return;
		}
		HeroCard heroCard = (HeroCard) context.getCardById(heroCardId);
		if (heroCard == null) {
			logger.error("onCast {} {}: Invalid heroCardId {}", context.getGameId(), source, heroCardId);
		}

		Hero hero = heroCard.createHero();
		context.getLogic().changeHero(player, hero);

		desc.subSpells(0).forEach(subSpell -> {
			SpellUtils.castChildSpell(context, player, subSpell, source, target, hero);
		});
	}
}
