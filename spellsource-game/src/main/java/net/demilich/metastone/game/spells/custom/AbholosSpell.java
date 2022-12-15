package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Destroys friendly minions. Then, resummons the {@link SpellArg#CARD} and sets its stats to the sum of all the destroyed
 * minions.
 */
public final class AbholosSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Minion> minions = player.getMinions();
		int attack = 0;
		int hp = 0;
		for (Minion minion : minions) {
			context.getLogic().markAsDestroyed(minion, source);
			attack += minion.getAttack();
			hp += minion.getHp();
		}

		if (hp == 0) {
			return;
		}

		SpellDesc summonAbholosAndBuff = new SpellDesc(SummonSpell.class);
		// Summon in place
		if (desc.containsKey(SpellArg.BOARD_POSITION_ABSOLUTE)) {
			summonAbholosAndBuff.put(SpellArg.BOARD_POSITION_ABSOLUTE, desc.get(SpellArg.BOARD_POSITION_ABSOLUTE));
		}
		summonAbholosAndBuff.put(SpellArg.CARD, desc.getString(SpellArg.CARD));
		summonAbholosAndBuff.put(SpellArg.SPELL,
				MetaSpell.create(EntityReference.OUTPUT, false,
						SetAttackSpell.create(attack),
						SetHpSpell.create(hp)
				));

		SpellDesc spell = CastAfterSequenceSpell.create(summonAbholosAndBuff);
		SpellUtils.castChildSpell(context, player, spell, source, target);
	}
}
