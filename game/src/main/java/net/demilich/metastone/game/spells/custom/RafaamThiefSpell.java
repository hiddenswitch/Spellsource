package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.AddDeathrattleSpell;
import net.demilich.metastone.game.spells.ShuffleMinionToDeckSpell;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Gives the target minion, "Deathrattle: Shuffle this minion into the caster's deck."
 * <p>
 * Implements I think I will take it!
 */
public final class RafaamThiefSpell extends AddDeathrattleSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		desc = desc.clone();
		SpellDesc deathrattle = new SpellDesc(ShuffleMinionToDeckSpell.class);
		deathrattle.put(SpellArg.TARGET, EntityReference.SELF);
		deathrattle.put(SpellArg.TARGET_PLAYER, player.getId() == GameContext.PLAYER_1 ? TargetPlayer.PLAYER_1 : TargetPlayer.PLAYER_2);
		desc.put(SpellArg.SPELL, deathrattle);
		super.onCast(context, player, desc, source, target);
	}
}
