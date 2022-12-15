package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Sets the {@code target}'s {@link Race} to the {@link SpellArg#RACE} value.
 * <p>
 * For <b>example</b>, to make all minions Murlocs:
 * <pre>
 *   {
 *     "class": "SetRaceSpell",
 *     "target": "ALL_MINIONS",
 *     "race": "MURLOC"
 *   }
 * </pre>
 */
public class SetRaceSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(SetRaceSpell.class);

	public static SpellDesc create(String race) {
		Map<SpellArg, Object> arguments = new SpellDesc(SetRaceSpell.class);
		arguments.put(SpellArg.RACE, race);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		final String race = (String) desc.getOrDefault(SpellArg.RACE, Race.NONE);
		if (target instanceof Actor) {
			Actor actor = (Actor) target;
			actor.setRace(race);
		} else if (target instanceof Card) {
			Card actorCard = (Card) target;
			actorCard.getAttributes().put(Attribute.RACE, race);
		} else {
			logger.warn("onCast {}: Trying to set a race on {}, which is neither an Actor nor an ActorCard.", context.getGameId(), target);
		}
	}
}
