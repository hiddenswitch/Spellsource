package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Transforms the {@code target} minion into the {@link SpellArg#CARD} or the <b>source card</b> of the entity pointed
 * to by {@link SpellArg#SECONDARY_TARGET}.
 *
 * @see net.demilich.metastone.game.logic.GameLogic#transformMinion(SpellDesc, Minion, Minion) for the complete rules on
 * transformations.
 */
public class TransformMinionSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(TransformMinionSpell.class);

	public static SpellDesc create(EntityReference target, Minion transformTarget, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(TransformMinionSpell.class);
		if (transformTarget != null) {
			arguments.put(SpellArg.SECONDARY_TARGET, transformTarget.getReference());
		}
		if (target != null) {
			arguments.put(SpellArg.TARGET, target);
		}

		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target, String templateCard, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(TransformMinionSpell.class);
		arguments.put(SpellArg.CARD, templateCard);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(Minion transformTarget) {
		return create(null, transformTarget, false);
	}

	public static SpellDesc create(String templateCard) {
		return create(null, templateCard, false);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(LOGGER, context, source, desc, SpellArg.CARD, SpellArg.SECONDARY_TARGET);
		Card card = SpellUtils.getCard(context, desc);
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			card = context.resolveSingleTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET)).getSourceCard();
		}
		if (!(target instanceof Minion)) {
			LOGGER.warn("onCast {} {}: Target {} is not a minion, skipping transform", context.getGameId(), source, target);
			return;
		}
		if (!card.getCardType().isCardType(CardType.MINION)) {
			LOGGER.warn("onCast {} {}: Card {} is not a minion card, skipping transform", context.getGameId(), source, card);
			return;
		}
		context.getLogic().transformMinion(desc, (Minion) target, card.summon());
	}
}

