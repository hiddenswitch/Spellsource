package net.demilich.metastone.game.spells;

import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissilesSpell extends DamageSpell {

	private static Logger logger = LoggerFactory.getLogger(MissilesSpell.class);

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		if (desc.getTarget() == null
				|| desc.getTarget().equals(EntityReference.TARGET)) {
			logger.warn("cast {} {}: Probable incorrect usage of MissilesSpell.", context.getGameId(), source);
		}
		int missiles = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 2);
		int damage = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);

		if (damage == 1 && source.getEntityType() == EntityType.CARD && ((Card) source).getCardType().isCardType(CardType.SPELL)) {
			missiles = context.getLogic().applySpellpower(player, source, missiles);
			missiles = context.getLogic().applyAmplify(player, missiles, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
		} else if (source.getEntityType() == EntityType.CARD && ((Card) source).getCardType().isCardType(CardType.SPELL)) {
			damage = context.getLogic().applySpellpower(player, source, damage);
			damage = context.getLogic().applyAmplify(player, damage, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
		}
		for (int i = 0; i < missiles; i++) {
			List<Entity> validTargets;
			if (desc.containsKey(SpellArg.FILTER)) {
				EntityFilter targetFilter = desc.getEntityFilter();
				List<Entity> filteredTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter);
				validTargets = SpellUtils.getValidRandomTargets(filteredTargets);
			} else {
				validTargets = SpellUtils.getValidRandomTargets(targets);
			}

			if (validTargets.isEmpty()) {
				return;
			}
			Actor randomTarget = (Actor)context.getLogic().getRandom(validTargets);
			context.getLogic().damage(player, randomTarget, damage, source, true);
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
