package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.desc.ActorCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class CopyDeathrattleSpell extends Spell {

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CopyDeathrattleSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor copyTo = (Actor) source;
		List<SpellDesc> deathrattles = new ArrayList<>();
		// Only actors have copyable deathrattles
		if (target instanceof Card &&
				((Card) target).getDesc() instanceof ActorCardDesc) {
			final ActorCardDesc actorCardDesc = (ActorCardDesc) ((Card) target).getDesc();
			if (actorCardDesc.deathrattle != null) {
				deathrattles.add(actorCardDesc.deathrattle);
			}
		} else if (target instanceof Actor) {
			deathrattles.addAll(((Actor) target).getDeathrattles());
		}
		for (SpellDesc deathrattle : deathrattles) {
			copyTo.addDeathrattle(deathrattle.clone());
		}
	}

}
