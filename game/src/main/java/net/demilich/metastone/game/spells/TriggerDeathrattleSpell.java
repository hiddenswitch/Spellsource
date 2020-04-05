package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers the {@code target} entity's aftermaths.
 * <p>
 * If a {@link Actor} target is specified, the aftermath is resolved from exactly where it is located.
 * <p>
 * If it is a {@link Card}, the aftermaths written on the card will be used instead.
 */
public class TriggerDeathrattleSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(TriggerDeathrattleSpell.class);

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(TriggerDeathrattleSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int value = (int) desc.getOrDefault(SpellArg.VALUE, 1);
		if (target instanceof Actor) {
			Actor actor = (Actor) target;
			for (int i = 0; i < value; i++) {
				context.getLogic().resolveAftermaths(player, actor);
			}
		} else if (target instanceof Card) {
			Card card = (Card) target;
			if (card.getDesc().getDeathrattle() != null) {
				SpellUtils.castChildSpell(context, player, card.getDesc().getDeathrattle(), source, target);
				context.getAftermaths().addAftermath(player.getId(), source.getReference(), card.getDesc().getDeathrattle(), source.getSourceCard().getCardId());
			}
			for (SpellDesc deathrattle : new ArrayList<>(card.getDeathrattleEnchantments())) {
				SpellUtils.castChildSpell(context, player, deathrattle, source, target);
				context.getAftermaths().addAftermath(player.getId(), source.getReference(), deathrattle, source.getSourceCard().getCardId());
			}

			if (card.getDesc().getDeathrattle() == null && card.getDeathrattleEnchantments().isEmpty()) {
				logger.warn("onCast {} {}: Tried to trigger a deathrattle on a card {} that doesn't have one.", context.getGameId(), source, card);
			}
		}

	}

}
