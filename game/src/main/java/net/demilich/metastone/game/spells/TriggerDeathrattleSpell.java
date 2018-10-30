package net.demilich.metastone.game.spells;

import java.util.Map;
import java.util.stream.Stream;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Triggers the {@code target} entity's deathrattles.
 */
public class TriggerDeathrattleSpell extends Spell {

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(TriggerDeathrattleSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = (int) desc.getOrDefault(SpellArg.VALUE, 1);
		if (target instanceof Actor) {
			Actor actor = (Actor) target;
			for (int i = 0; i < value; i++) {
				context.getLogic().resolveDeathrattles(player, actor);
			}
		} else if (target instanceof Card) {
			Card card = (Card) target;
			Stream.concat(Stream.of(card.getDesc().getDeathrattle()), card.getDeathrattleEnchantments().stream()).forEach(deathrattle -> {
				SpellUtils.castChildSpell(context, player, deathrattle, source, target);
			});

		}

	}

}
