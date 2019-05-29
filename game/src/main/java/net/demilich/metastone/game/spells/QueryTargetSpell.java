package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.CardFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.DeckSource;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interprets the spell arguments as a query of targets and casts {@link SpellArg#SPELL} on each resulting {@code
 * target} in order.
 * <p>
 * This is a very powerful decorator spell that can be used to do selections on cards, like "Destroy the three highest
 * health minions on the board," or "Draw the next three murlocs in your deck." If you encounter a pattern like this,
 * use {@link QueryTargetSpell}.
 * <p>
 * No effects are cast when the result of the query is empty.
 * <h1>Targeting:</h1>
 * First, the original list of targets is collected. When {@link SpellArg#TARGET} is specified, the query is executed
 * against the full collection of targets. When it is not specified, the query is executed on the cards returned by
 * {@link SpellArg#CARD_SOURCE}.
 *
 * <h1>Shuffling:</h1>
 * If {@link SpellArg#RANDOM_TARGET} is {@code true}, the list is now shuffled.
 * <h1>Filtering:</h1>
 * The the targets are filtered using {@link SpellArg#FILTER} or {@link SpellArg#CARD_FILTER}. If {@link
 * SpellArg#TARGET} is specified with a {@link SpellArg#CARD_FILTER}, the <b>source cards</b> of the targets are used.
 * Otherwise, if a {@link SpellArg#FILTER} is specified, the filter is evaluated normally.
 * <h1>Sorting:</h1>
 * If a {@link SpellArg#VALUE} is provided, that value is evaluated against every element in the targets list and used
 * to sort in ascending order. If a value is not provided, the natural order (i.e., index in the zone or catalogue) is
 * used. To reverse the list, set {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg#MULTIPLIER}
 * to {@code -1}.
 * <h1>Skips and Limits:</h1>
 * The first {@link SpellArg#SECONDARY_VALUE} targets are skipped, defaulting to {@code 0}. Then, the next {@link
 * SpellArg#HOW_MANY} targets are taken, defaulting to {@code 1}.
 * <p>
 * If the player chose a target ({@link EntityReference#TARGET} is set) or we are currently evaluating an event ({@link
 * EntityReference#EVENT_TARGET} is set), {@link ValueProvider} specifications given for either of these arguments will
 * be evaluated with the {@code target} given by {@link EntityReference#TARGET} or {@link EntityReference#EVENT_TARGET},
 * or {@code null}.
 * <h1>Casting:</h1>
 * Finally, the {@link SpellArg#SPELL} is cast on each target in order. If a {@link SpellArg#CONDITION} is specified,
 * casting only continues so long as there are elements <b>and</b> the condition is {@code true}, evaluated with the
 * {@code target} we are currently potentially casting on.
 * <p>
 * For example, to target the top 5 minions in your deck:
 * <pre>
 *   {
 *       "class": "QueryTargetSpell",
 *       "howMany": 5,
 *       "cardSource": {
 *         "class": "DeckSource",
 *         "targetPlayer": "SELF"
 *       },
 *       "cardFilter": {
 *         "class": "CardFilter",
 *         "cardType": "MINION"
 *       },
 *       "value": {
 *         "class": "AttributeValueProvider",
 *         "attribute": "INDEX",
 *         "multiplier": -1
 *       },
 *       "spell": {
 *         (Your spell here)
 *       }
 *     }
 * </pre>
 */
public class QueryTargetSpell extends Spell {

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		Entity target = context.resolveSingleTarget(player, source, EntityReference.TARGET);
		if (target == null) {
			target = context.resolveSingleTarget(player, source, EntityReference.EVENT_TARGET);
		}

		int limit = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		int skip = desc.getValue(SpellArg.SECONDARY_VALUE, context, player, target, source, 0);
		boolean shuffled = desc.getBool(SpellArg.RANDOM_TARGET);
		EntityFilter filter = desc.getEntityFilter();
		// Determine whether this is a card source query or a targets query
		if (desc.hasPredefinedTarget() || (desc.getCardSource() == null && desc.getCardFilter() == null)) {
			// Targets query
		} else if (desc.getCardSource() != null || desc.getCardFilter() != null) {
			// Cards query
			CardSource cardSource = desc.getCardSource();
			if (cardSource == null) {
				cardSource = DeckSource.create();
			}
			targets = new ArrayList<>(cardSource.getCards(context, source, player));
			if (desc.getCardFilter() != null) {
				filter = desc.getCardFilter();
			}
		}

		if (filter == null) {
			filter = AndFilter.create();
		}

		if (targets == null || targets.isEmpty()) {
			return;
		}
		if (shuffled) {
			Collections.shuffle(targets, context.getLogic().getRandom());
		}
		targets = targets.stream().filter(filter.matcher(context, player, source)).collect(Collectors.toList());
		if (desc.containsKey(SpellArg.VALUE)) {
			targets.sort(Comparator.comparingInt(t -> desc.getValue(SpellArg.VALUE, context, player, t, source, t.getIndex())));
		}
		Condition condition = (Condition) desc.get(SpellArg.CONDITION);
		for (int i = skip; i < targets.size() && i < skip + limit; i++) {
			if (condition != null && !condition.isFulfilled(context, player, source, targets.get(i))) {
				break;
			}
			onCast(context, player, desc.getSpell(), source, targets.get(i));
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellUtils.castChildSpell(context, player, desc, source, target);
	}
}
