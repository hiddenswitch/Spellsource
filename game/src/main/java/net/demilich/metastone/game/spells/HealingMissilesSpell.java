package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterDesc;
import net.demilich.metastone.game.utils.Attribute;

import java.util.HashMap;
import java.util.List;

public class HealingMissilesSpell extends HealSpell {

	public static SpellDesc create(int healing) {
		return create(null, healing);
	}

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		int missiles = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 2);
		int healing = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);

		if (healing == 1 && source.getEntityType() == EntityType.CARD && ((Card) source).getCardType().isCardType(CardType.SPELL)) {
			missiles = context.getLogic().applySpellpower(player, source, missiles);
			missiles = context.getLogic().applyAmplify(player, missiles, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		} else if (source.getEntityType() == EntityType.CARD && ((Card) source).getCardType().isCardType(CardType.SPELL)) {
			healing = context.getLogic().applySpellpower(player, source, healing);
			healing = context.getLogic().applyAmplify(player, healing, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		}
		for (int i = 0; i < missiles; i++) {
			List<Actor> validTargets;
			if (desc.containsKey(SpellArg.FILTER)) {
				EntityFilter targetFilter = desc.getEntityFilter();
				List<Entity> filteredTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter);
				validTargets = SpellUtils.getValidRandomTargets(filteredTargets);
			} else {
				EntityFilter targetFilter = new EntityFilter(new FilterDesc(new HashMap<>())) {
					@Override
					protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
						return ((Actor) entity).isWounded();
					}
				};
				List<Entity> filteredTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter);
				validTargets = SpellUtils.getValidRandomTargets(filteredTargets);
			}

			if (validTargets.isEmpty()) {
				return;
			}

			Actor randomTarget = context.getLogic().getRandom(validTargets);
			context.getLogic().heal(player, randomTarget, healing, source, false);
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
