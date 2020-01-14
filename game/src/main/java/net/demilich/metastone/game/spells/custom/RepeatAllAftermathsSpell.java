package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.environment.EnvironmentDeathrattleTriggeredList;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repeats all other aftermaths the casting player has triggered as long as the source is in play.
 */
public final class RepeatAllAftermathsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		TargetPlayer castingTargetPlayer = desc.getTargetPlayer() == null ? TargetPlayer.OWNER : desc.getTargetPlayer();
		List<EnvironmentDeathrattleTriggeredList.EnvironmentDeathrattleTriggeredItem> deathrattles = new ArrayList<>(context.getDeathrattles().getDeathrattles());
		boolean isItselfDeathrattle = desc.containsKey(SpellArg.DEATHRATTLE_ID);

		int index;
		if (isItselfDeathrattle) {
			index = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE);
		} else {
			index = -1;
		}
		if (isItselfDeathrattle) {
			List<SpellDesc> spells = deathrattles.stream().filter(item -> {
				if (RepeatAllAftermathsSpell.class.isAssignableFrom(item.getSpell().getDescClass())) {
					return false;
				}

				if (item.getPlayerId() != player.getId()) {
					return false;
				}

				return true;
			}).map(EnvironmentDeathrattleTriggeredList.EnvironmentDeathrattleTriggeredItem::getSpell)
					.collect(Collectors.toList());

			context.getLogic().resolveDeathrattles(player.getId(), source.getReference(), spells, player.getId(), index, false);
		} else {
			for (EnvironmentDeathrattleTriggeredList.EnvironmentDeathrattleTriggeredItem item : deathrattles) {
				if (RepeatAllAftermathsSpell.class.isAssignableFrom(item.getSpell().getDescClass())) {
					continue;
				}

				if (item.getPlayerId() != player.getId()) {
					continue;
				}


				SpellUtils.DetermineCastingPlayer determineCastingPlayer = SpellUtils.determineCastingPlayer(context, player, source, castingTargetPlayer);
				if (!determineCastingPlayer.isSourceInPlay()) {
					break;
				}
				Player castingPlayer = determineCastingPlayer.getCastingPlayer();
				SpellUtils.castChildSpell(context, castingPlayer, item.getSpell(), source, null);
			}
		}

	}
}
