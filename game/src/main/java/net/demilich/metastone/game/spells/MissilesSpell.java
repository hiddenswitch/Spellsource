package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This spell casts {@link SpellArg#HOW_MANY} missiles, each dealing {@link SpellArg#VALUE} damage (with spell damage)
 * to random targets.
 * <p>
 * When a target becomes mortally wounded ({@link Actor#isDestroyed()} resolves to {@code true}), it is no longer
 * eligible to receive missiles.
 * <p>
 * When the amount of damage a missile deals is 1, the spell damage modifier is interpreted to increase the number of
 * missiles. In all other instances, spell damage increases the damage <b>per missile.</b>
 * <p>
 * Every target hit by a missile will have {@link SpellArg#SPELL} cast on it if specified, with the hit target as the
 * {@code target}. {@link EntityReference#OUTPUT} will not be set.
 * <p>
 * When {@link SpellArg#EXCLUSIVE} is set to {@code true}, applies spell damage to the number of missiles (the {@link
 * SpellArg#HOW_MANY} value).
 */
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

		SpellDesc subSpell = desc.getSpell();

		if ((damage == 1 || desc.getBool(SpellArg.EXCLUSIVE))
				&& ((source.getEntityType() == EntityType.CARD && GameLogic.isCardType(((Card) source).getCardType(), CardType.SPELL))
				|| source.getEntityType() == EntityType.SECRET)) {
			missiles = context.getLogic().applySpellpower(player, source, missiles);
			missiles = context.getLogic().applyAmplify(player, missiles, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
		} else if (source.getEntityType() == EntityType.CARD && GameLogic.isCardType(((Card) source).getCardType(), CardType.SPELL)) {
			damage = context.getLogic().applySpellpower(player, source, damage);
			damage = context.getLogic().applyAmplify(player, damage, Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);
		}
		for (int i = 0; i < missiles; i++) {
			List<Entity> validTargets;
			if (desc.containsKey(SpellArg.FILTER)) {
				EntityFilter targetFilter = desc.getEntityFilter();
				List<Entity> filteredTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter, source);
				validTargets = SpellUtils.getValidRandomTargets(filteredTargets);
			} else {
				validTargets = SpellUtils.getValidRandomTargets(targets);
			}

			if (validTargets.isEmpty()) {
				return;
			}
			Actor randomTarget = getRandomTarget(context, validTargets);
			context.getLogic().damage(player, randomTarget, damage, source, true);

			if (subSpell != null) {
				SpellUtils.castChildSpell(context, player, subSpell, source, randomTarget);
			}
		}
	}

	public Actor getRandomTarget(GameContext context, List<Entity> validTargets) {
		return (Actor) context.getLogic().getRandom(validTargets);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
