package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resurrects minions from the caster's graveyard.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is set to true, only unique copies are resurrected.
 * <p>
 * If {@link SpellArg#ATTRIBUTE} is set to {@link Attribute#MAGNETS}, the minion is resummoned with its magnetized
 * elements.
 * <p>
 * If the minion was successfully summoned, {@link SpellArg#SPELL} will be cast with {@link EntityReference#OUTPUT}
 * <b>and</b> {@code target} as the summoned minion.
 * <p>
 * Uses the filter specified in {@link SpellArg#CARD_FILTER}. Always only resurrects minions.
 * <p>
 * For <b>example</b>, to resurrect <b>2 different</b> friendly minions:
 * <pre>
 *   {
 *     "class": "ResurrectSpell",
 *     "value": 2,
 *     "exclusive": true
 *   }
 * </pre>
 */
public class ResurrectSpell extends Spell {

	@SuppressWarnings("unchecked")
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Minion> deadMinions = new ArrayList<>();
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		List<Entity> graveyard = new ArrayList<>(player.getGraveyard());
		for (Entity deadEntity : graveyard) {
			if (deadEntity.diedOnBattlefield()) {
				if (cardFilter == null || cardFilter.matches(context, player, deadEntity, source)) {
					deadMinions.add((Minion) deadEntity);
				}
			}
		}

		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		// Implements unusual Diamond Spellstone unique minions behaviour
		boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);
		if (exclusive) {
			Set<EntityReference> references = deadMinions.stream().map(Entity::getReference).collect(Collectors.toSet());
			deadMinions.removeIf(deadMinion -> deadMinion.hasAttribute(Attribute.COPIED_FROM)
					&& references.contains((EntityReference) deadMinion.getAttribute(Attribute.COPIED_FROM)));
		}
		for (int i = 0; i < count; i++) {
			if (deadMinions.isEmpty()) {
				return;
			}
			Minion resurrectedMinion = context.getLogic().getRandom(deadMinions);
			Card card = resurrectedMinion.getSourceCard();
			var minion = card.minion();
			boolean summoned = context.getLogic().summon(player.getId(), minion, source, -1, false);
			if (summoned) {
				minion = (Minion) minion.transformResolved(context);
				if (desc.containsKey(SpellArg.SPELL)
						&& minion.isInPlay()) {
					SpellUtils.castChildSpell(context, player, (SpellDesc) desc.get(SpellArg.SPELL), source, minion, minion);
				}

				var attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE);
				if (attribute != null && resurrectedMinion.hasAttribute(attribute)) {
					// special coding to remagnetize the mechs for Kangor's Endless Army
					if (attribute == Attribute.MAGNETS) {
						context.getLogic().removeAttribute(player, source, minion, Attribute.MAGNETS);
						String[] magnets = (String[]) resurrectedMinion.getAttribute(Attribute.MAGNETS);
						for (String magnet : magnets) {
							Card magnetCard = context.getCardById(magnet);
							context.getLogic().magnetize(player.getId(), magnetCard, minion);
						}
					} else {
						card.setAttribute(attribute, resurrectedMinion.getAttributeValue(attribute));
					}
				}
			}

			deadMinions.remove(resurrectedMinion);
		}
	}

}
