package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PhysicalAttackAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Causes the {@code source} or each entity in {@link SpellArg#SECONDARY_TARGET} to {@link
 * net.demilich.metastone.game.logic.GameLogic#fight(Player, Actor, Actor, PhysicalAttackAction)} the {@code target}.
 * <p>
 * Activates the hero's weapon regardless of whose turn it is if a {@link Hero} is an attacker.
 * <p>
 * After the attack occurs, casts the {@link SpellArg#SPELL} sub-spell with the {@code source} as the source of this
 * spell cast, the {@code target} as the defender, and the {@link EntityReference#OUTPUT} set to the attacker.
 * <p>
 * For example, consider the text from Birb's You from the Future, "Summon a copy of a friendly minion. Then, it attacks
 * the original":
 * <pre>
 *   {
 *     "class": "SummonSpell",
 *     "spell": {
 *       "class": "FightSpell",
 *       "secondaryTarget": "OUTPUT"
 *     }
 *   }
 * </pre>
 * Note that the {@link EntityReference#OUTPUT} refers to the newly copied minion. Since the copied minion is in the
 * {@link SpellArg#SECONDARY_TARGET} specifier, the copied minion is the attacker. The {@code target} is implied to be
 * the selected target of the spell, i.e., the original minion.
 * <p>
 * {@link SpellArg#EXCLUSIVE to not use up the attacker's attack with this spell}
 */
public class FightSpell extends Spell {

	private static final long serialVersionUID = 2133264044113375467L;
	private static Logger logger = LoggerFactory.getLogger(FightSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.SECONDARY_TARGET);
		EntityReference secondaryTarget = (EntityReference) desc.getOrDefault(SpellArg.SECONDARY_TARGET, source == null ? EntityReference.NONE : source.getReference());
		List<Entity> resolvedSources = context.resolveTarget(player, source, secondaryTarget);
		if (resolvedSources == null) {
			logger.error("onCast {} {}: Could not resolve source key {} targeting {}", context.getGameId(), source, secondaryTarget, target);
			return;
		}

		if (target == null) {
			return;
		}

		// Only attack sources that aren't destroyed
		if (!target.isInPlay() || target.isDestroyed()) {
			logger.warn("onCast {} {}: Target {} is not in play or is destroyed and thus cannot defend itself anymore", context.getGameId(), source, target);
			return;
		}

		for (Entity resolvedSource : resolvedSources) {
			if (!(resolvedSource instanceof Actor)) {
				logger.error("onCast {} {}: Source entity {} targeting {} is not an Actor", context.getGameId(), source, resolvedSource, target);
				continue;
			}

			if (!(target instanceof Actor)) {
				logger.error("onCast {} {}: Target {} is not an Actor (trying to attack with {})", context.getGameId(), source, target, resolvedSource);
				continue;
			}

			if (!resolvedSource.isInPlay() || resolvedSource.isDestroyed()) {
				logger.warn("onCast {} {}: Source {} is no longer in play or is destroyed and will not initiate a fight.", context.getGameId(), source, resolvedSource);
				continue;
			}

			if (resolvedSource.equals(target)) {
				logger.warn("onCast {} {}: Source {} is trying to attack itself, which is not allowed. Skipping.", context.getGameId(), source, resolvedSource);
				continue;
			}

			if (resolvedSource instanceof Hero) {
				// Activate the weapon if the hero has one
				((Hero) resolvedSource).activateWeapon(true);
			}

			int numberOfAttacksBefore = resolvedSource.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);

			context.getLogic().fight(player, (Actor) resolvedSource, (Actor) target, null);
			for (SpellDesc subSpell : desc.subSpells(0)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, resolvedSource);
			}
			if (desc.containsKey(SpellArg.EXCLUSIVE)) {
				resolvedSource.setAttribute(Attribute.NUMBER_OF_ATTACKS, numberOfAttacksBefore);
			}

			if (resolvedSource instanceof Hero) {
				// Deactivate the weapon when we're done.
				((Hero) resolvedSource).activateWeapon(false);
			}
		}
	}
}

