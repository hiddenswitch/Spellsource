package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies the {@code target} actor's deathrattles onto the {@code source} (i.e., result of {@link EntityReference#SELF})
 * of this spell.
 */
public class CopyDeathrattleSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(CopyDeathrattleSpell.class);

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(CopyDeathrattleSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor copyTo = (Actor) source;
		List<SpellDesc> deathrattles = new ArrayList<>();
		// Only actors have copyable deathrattles
		if (target instanceof Card) {
			final CardDesc actorCardDesc = ((Card) target).getDesc();
			if (actorCardDesc.getDeathrattle() != null) {
				deathrattles.add(actorCardDesc.getDeathrattle());
			}
		} else if (target instanceof Actor) {
			deathrattles.addAll(((Actor) target).getDeathrattles());
		} else {
			logger.error("onCast {} {}: Cannot copy target from {}", context.getGameId(), source, target);
			return;
		}
		for (SpellDesc deathrattle : deathrattles) {
			copyTo.addDeathrattle(deathrattle.clone());
		}
	}

}
