package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DestroyOnDeathrattleSpell extends AddDeathrattleSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc newDesc = new SpellDesc(AddDeathrattleSpell.class);
		SpellDesc destroySpell = new SpellDesc(DestroySpell.class);
		destroySpell.put(SpellArg.TARGET, target.getReference());
		newDesc.put(SpellArg.SPELL, destroySpell);
		super.onCast(context, player, newDesc, source, source);
	}
}
