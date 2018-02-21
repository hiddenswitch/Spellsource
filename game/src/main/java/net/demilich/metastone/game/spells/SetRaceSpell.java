package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.ActorCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SetRaceSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(SetRaceSpell.class);

	public static SpellDesc create(Race race) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SetRaceSpell.class);
		arguments.put(SpellArg.RACE, race);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		final Race race = (Race) desc.getOrDefault(SpellArg.RACE, Race.NONE);
		if (target instanceof Actor) {
			Actor actor = (Actor) target;
			actor.setRace(race);
		} else if (target instanceof ActorCard) {
			ActorCard actorCard = (ActorCard) target;
			actorCard.getAttributes().put(Attribute.RACE, race);
		} else {
			logger.warn("onCast {}: Trying to set a race on {}, which is neither an Actor nor an ActorCard.", context.getGameId(), target);
		}
	}
}
