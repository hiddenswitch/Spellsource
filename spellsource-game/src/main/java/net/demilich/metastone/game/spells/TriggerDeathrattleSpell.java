package net.demilich.metastone.game.spells;

import com.google.common.base.Objects;
import com.google.common.collect.Streams;
import jdk.incubator.concurrent.ScopedValue;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Aftermath;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Triggers the {@code target} entity's aftermaths.
 * <p>
 * If a {@link Actor} target is specified, the aftermath is resolved from exactly where it is located.
 * <p>
 * If it is a {@link Card}, the aftermaths written on the card will be used instead.
 */
public class TriggerDeathrattleSpell extends Spell {
	private final static ScopedValue<LinkedHashMap<Card, Aftermath>> aftermathsCache = ScopedValue.newInstance();

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(TriggerDeathrattleSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
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
								.map(t -> ((Aftermath) t))
								.collect(toList())
						, target.getOwner(), -1, true);
			}

		} else if (target instanceof Card card) {
			LinkedHashMap<Card, Aftermath> aftermathsCacheCheck;
			if (aftermathsCache.isBound()) {
				aftermathsCacheCheck = aftermathsCache.get();
			} else {
				aftermathsCacheCheck = new LinkedHashMap<>();
			}
			ScopedValue.where(aftermathsCache, aftermathsCacheCheck)
					.run(() -> {
						var aftermathsPrintedOnCards = aftermathsCache.get();
						Aftermath printedOnCard = null;
						if (aftermathsPrintedOnCards.containsKey(card)) {
							printedOnCard = aftermathsPrintedOnCards.get(card);
						} else if (card.getDesc().getDeathrattle() != null) {
							printedOnCard = new Aftermath(card.getDesc().getDeathrattle(), card, player.getHero());
							aftermathsPrintedOnCards.put(card, printedOnCard);
						}

						var aftermaths = Streams.concat(Stream.ofNullable(printedOnCard),
										context.getTriggers()
												.stream()
												.filter(t -> t instanceof Aftermath && Objects.equal(t.getHostReference(), card.getReference()) && !t.isExpired())
												.map(t -> ((Aftermath) t)))
								.toList();

						// More aftermaths may be put into play here
						context.getLogic().resolveAftermaths(player.getId(), source.getReference(), aftermaths, target.getOwner(), -1, true);
					});

		}
	}
}
