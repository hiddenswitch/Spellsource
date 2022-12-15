package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TransformMinionSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Transforms the {@code target} into the {@link SpellArg#CARD}. When {@link SpellArg#SECONDARY_TARGET} dies, it
 * transforms the {@code target} back into its original card. This effect lives on as a deathrattle on {@link
 * SpellArg#SECONDARY_TARGET}.
 * <p>
 * Implements Anobii, the Trapper.
 */
public final class AnobiiSpell extends AddDeathrattleSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Entity deathrattleHost = context.resolveSingleTarget(player, source, (EntityReference) desc.getOrDefault(SpellArg.SECONDARY_TARGET, EntityReference.SELF));
		Card cocoonCard = SpellUtils.getCard(context, desc);
		Card originalCard = target.getSourceCard();
		Minion cocoon = cocoonCard.minion();
		context.getLogic().transformMinion(desc, source, (Minion) target, cocoon, true);
		SpellDesc revertDeathrattle = TransformMinionSpell.create(cocoon.getReference(), originalCard.getCardId(), false);
		// TODO: This should probably check that the minion isn't a cocoon anymore
		super.onCast(context, player, AddDeathrattleSpell.create(revertDeathrattle), source, deathrattleHost);
	}
}