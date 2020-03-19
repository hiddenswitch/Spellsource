package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copies the {@code target} actor's deathrattles onto the {@code source} (i.e., result of {@link EntityReference#SELF})
 * of this spell.
 * <p>
 * If a {@link SpellArg#CARD_SOURCE} and {@link SpellArg#CARD_FILTER} is specified, generate cards instead.
 * <p>
 * If {@link SpellArg#SECONDARY_TARGET} is specified, use that as the {@code source} actor instead of the actual {@code
 * source}.
 * <p>
 * If the target is a {@link Card}, the deathrattles specified on the card are put on the {@code source} actor. If the
 * target is an actor, its currently active deathrattles (i.e., excluding those that were silenced, including those
 * added by other cards) are copied.
 * <p>
 * If a {@link SpellArg#SPELL} is specified and it had a deathrattle, cast it on each of the {@code target} as {@link
 * EntityReference#OUTPUT} whose deathrattle was copied. If cards were generated and their deathrattles added, the
 * {@link SpellArg#CARD} argument will be added to the sub-spell corresponding to each of the generated cards.
 */
public class CopyDeathrattleSpell extends Spell {
	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(CopyDeathrattleSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor copyTo = (Actor) source;
		if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
			copyTo = context.resolveSingleTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
		}
		int max = (int) desc.getOrDefault(SpellArg.HOW_MANY, 16);
		List<SpellDesc> deathrattles = new ArrayList<>();
		CardList impliedCards = SpellUtils.getCards(context, player, target, source, desc, max);
		if (target instanceof Actor) {
			deathrattles.addAll(((Actor) target).getDeathrattles());
		} else if (!impliedCards.isEmpty()) {
			if (desc.containsKey(SpellArg.RANDOM_TARGET)) {
				impliedCards.shuffle(context.getLogic().getRandom());
			}
			for (Card impliedCard : impliedCards) {
				if (impliedCard.getDesc().getDeathrattle() != null) {
					deathrattles.add(impliedCard.getDesc().getDeathrattle());
				}
			}
		} else if (target instanceof Card) {
			final CardDesc actorCardDesc = ((Card) target).getDesc();
			if (actorCardDesc.getDeathrattle() != null) {
				deathrattles.add(actorCardDesc.getDeathrattle());
			}
		}
		for (SpellDesc deathrattle : deathrattles) {
			copyTo.addDeathrattle(deathrattle.clone());
		}
		if (impliedCards.isEmpty() && target != null && !deathrattles.isEmpty() && desc.getSpell() != null) {
			SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target, target);
		} else if (!impliedCards.isEmpty() && !deathrattles.isEmpty() && desc.getSpell() != null) {
			for (Card card : impliedCards) {
				SpellDesc spell = desc.getSpell().clone();
				spell.put(SpellArg.CARD, card.getCardId());
				SpellUtils.castChildSpell(context, player, spell, source, target, card);
			}
		}
	}

}
