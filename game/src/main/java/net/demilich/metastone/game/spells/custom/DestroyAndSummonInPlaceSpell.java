package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public final class DestroyAndSummonInPlaceSpell extends SummonSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int originalIndex = target.getEntityLocation().getIndex();
		SpellUtils.castChildSpell(context, player, DestroySpell.create(target.getReference()), source, target);
		context.getLogic().endOfSequence();
		desc = desc.clone();
		desc.put(SpellArg.BOARD_POSITION_ABSOLUTE, Math.min(originalIndex, player.getMinions().size()));
		super.onCast(context, player, desc, source, null);
	}
}
