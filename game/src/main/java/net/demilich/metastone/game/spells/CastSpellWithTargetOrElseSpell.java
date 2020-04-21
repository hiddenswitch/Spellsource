package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Tries to cast the spell card (given either by {@link SpellArg#CARD} or {@link SpellArg#SECONDARY_TARGET}) onto the specified
 * target. If that is unable to happen, then the {@link SpellArg#SPELL} is cast instead, with the card's id passed down into the
 * {@link SpellArg#CARD} attribute.
 * <p>
 * If a {@link SpellArg#TRIGGER} is specified, then it is used to delay the casting of the spell until that event occurs.
 * <p>
 * See Finale Architect
 */

public class CastSpellWithTargetOrElseSpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(CastSpellWithTargetOrElseSpell.class);

	@Suspendable
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.TARGET, SpellArg.SECONDARY_TARGET, SpellArg.CARD, SpellArg.TRIGGER, SpellArg.SPELL);

		Card card;
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			card = (Card) context.resolveSingleTarget(desc.getSecondaryTarget());
		} else {
			card = SpellUtils.getCard(context, desc);
		}

		if (!card.isSpell()) {
			LOGGER.error("Needs to be a spell");
		}

		SpellDesc orElse = desc.getSpell();

		EnchantmentDesc trigger = (EnchantmentDesc) desc.get(SpellArg.TRIGGER);

		if (trigger != null) {
			SpellDesc thisButLater = desc.removeArg(SpellArg.TRIGGER);
			if (target != null) {
				thisButLater.put(SpellArg.TARGET, target.getReference());
			}
			thisButLater.put(SpellArg.SECONDARY_TARGET, card.getReference());
			trigger.setSpell(thisButLater);
			SpellDesc addEnchantmentSpellDesc = AddEnchantmentSpell.create(trigger);
			SpellUtils.castChildSpell(context, player, addEnchantmentSpellDesc, source, player);
		} else {
			if (target == null) {
				SpellUtils.castChildSpell(context, player, card.getSpell(), source, null);
				context.getLogic().revealCard(player, card);
			} else {
				List<Entity> targets = context.getTargetLogic().getValidTargets(context, player, card.play());
				if (targets.contains(target)) {
					SpellUtils.castChildSpell(context, player, card.getSpell(), source, target);
					context.getLogic().revealCard(player, card);
				} else {
					orElse.put(SpellArg.CARD, card.getCardId());
					SpellUtils.castChildSpell(context, player, orElse, source, null);
				}
			}
		}
	}
}
