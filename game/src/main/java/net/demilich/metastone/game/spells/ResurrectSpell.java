package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;

public class ResurrectSpell extends Spell {
	@SuppressWarnings("unchecked")
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Minion> deadMinions = new ArrayList<>();
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		List<Entity> graveyard = new ArrayList<Entity>();
		graveyard.addAll(player.getGraveyard());
		for (Entity deadEntity : graveyard) {
			if (deadEntity.getEntityType() == EntityType.MINION) {
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
			final Minion summonedMinion;
			Attribute attribute = (Attribute) desc.get(SpellArg.ATTRIBUTE); //allow functionality to resurrect cards with certain attributes they died with
			if (attribute != null && resurrectedMinion.hasAttribute(attribute)) {
				if (attribute == Attribute.MAGNETS) { //special coding to remagnetize the mechs for Kangor's Endless Army
					summonedMinion = card.summon();
					context.getLogic().removeAttribute(summonedMinion, Attribute.MAGNETS);
					String[] magnets = (String[])resurrectedMinion.getAttribute(Attribute.MAGNETS);
					for (String magnet : magnets) {
						Card magnetCard = context.getCardById(magnet);
						context.getLogic().magnetize(player.getId(), magnetCard, summonedMinion);
					}
				} else {
					card.setAttribute(attribute, resurrectedMinion.getAttributeValue(attribute));
					summonedMinion = card.summon();
				}
			} else summonedMinion = card.summon();

			final boolean summoned = context.getLogic().summon(player.getId(), summonedMinion, null, -1, false);
			if (summoned
					&& desc.containsKey(SpellArg.SPELL)
					&& summonedMinion.getZone() == Zones.BATTLEFIELD) {
				SpellUtils.castChildSpell(context, player, (SpellDesc) desc.get(SpellArg.SPELL), source, summonedMinion, summonedMinion);
			}
			deadMinions.remove(resurrectedMinion);
		}
	}

}
