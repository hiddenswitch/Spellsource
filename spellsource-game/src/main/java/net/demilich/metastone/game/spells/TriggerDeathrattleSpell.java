package net.demilich.metastone.game.spells;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.paralleluniverse.common.util.Objects;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Streams;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Aftermath;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * Triggers the {@code target} entity's aftermaths.
 * <p>
 * If a {@link Actor} target is specified, the aftermath is resolved from exactly where it is located.
 * <p>
 * If it is a {@link Card}, the aftermaths written on the card will be used instead.
 */
public class TriggerDeathrattleSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(TriggerDeathrattleSpell.class);

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
			if (actor.isInPlay()) {
				for (int i = 0; i < value; i++) {
					context.getLogic().resolveAftermaths(player, actor);
				}
			} else {
				// Include expired aftermaths
				context.getLogic().resolveAftermaths(player.getId(), actor.getReference(),
						context.getTriggers().stream().filter(t -> t instanceof Aftermath && t.getHostReference().equals(target.getReference()))
								.map(t -> ((Aftermath) t).getSpell())
								.collect(toList())
						, target.getOwner(), -1, true);
			}

		} else if (target instanceof Card) {
			Card card = (Card) target;

			var aftermaths = Streams.concat(Stream.ofNullable(card.getDesc().getDeathrattle()),
					context.getTriggers()
							.stream()
							.filter(t -> t instanceof Aftermath && Objects.equal(t.getHostReference(), card.getReference()) && !t.isExpired())
							.map(t -> ((Aftermath) t).getSpell()))
					.collect(Collectors.toUnmodifiableList());

			// More aftermaths may be put into play here
			aftermaths.forEach(aftermath -> {
				SpellUtils.castChildSpell(context, player, aftermath, source, target);
				context.getAftermaths().addAftermath(player.getId(), source.getReference(), aftermath, source.getSourceCard().getCardId());
			});
		}
	}
}
