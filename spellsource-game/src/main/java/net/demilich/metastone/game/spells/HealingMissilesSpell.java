package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;
import net.demilich.metastone.game.cards.Attribute;

import java.util.HashMap;
import java.util.List;

/**
 * Casts healing missiles.
 */
public class HealingMissilesSpell extends HealSpell {

	public static SpellDesc create(int healing) {
		return create(null, healing);
	}

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		int missiles = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 2);
		int healing = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);

		if (healing == 1 && source.getEntityType() == EntityType.CARD && GameLogic.isCardType(((Card) source).getCardType(), CardType.SPELL)) {
			missiles = context.getLogic().applySpellpower(player, source, missiles);
			missiles = context.getLogic().applyAmplify(player, missiles, Attribute.SPELL_HEAL_AMPLIFY_MULTIPLIER);
			missiles = context.getLogic().applyAmplify(player, missiles, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		} else if (source.getEntityType() == EntityType.CARD && GameLogic.isCardType(((Card) source).getCardType(), CardType.SPELL)) {
			healing = context.getLogic().applySpellpower(player, source, healing);
			healing = context.getLogic().applyAmplify(player, healing, Attribute.SPELL_HEAL_AMPLIFY_MULTIPLIER);
			healing = context.getLogic().applyAmplify(player, healing, Attribute.HEAL_AMPLIFY_MULTIPLIER);
		}
		for (int i = 0; i < missiles; i++) {
			List<Entity> validTargets;
			if (desc.containsKey(SpellArg.FILTER)) {
				EntityFilter targetFilter = desc.getEntityFilter();
				List<Entity> filteredTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter, source);
				validTargets = SpellUtils.getValidRandomTargets(filteredTargets);
			} else {
				EntityFilter targetFilter = new EntityFilter(new EntityFilterDesc(new HashMap<>())) {
					@Override
					protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
						return ((Actor) entity).isWounded();
					}
				};
				List<Entity> filteredTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter, source);
				validTargets = SpellUtils.getValidRandomTargets(filteredTargets);
			}

			if (validTargets.isEmpty()) {
				return;
			}

			Entity randomTarget = context.getLogic().getRandom(validTargets);
			context.getLogic().heal(player, (Actor) randomTarget, healing, source, false).getHealing();
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
