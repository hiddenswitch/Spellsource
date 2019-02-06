package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Destroys a minion, and gives the {@code source} the deathrattle, "Resummon that minion."
 * <p>
 * Implements Moat Lurker and Carnivorous Cube.
 */
public final class JailMinionSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Minion minion = (Minion) target;
		TargetPlayer targetPlayer = TargetPlayer.SELF;
		if (minion.getOwner() != source.getOwner()) {
			targetPlayer = TargetPlayer.OPPONENT;
		}
		source.getAttributes().remove(Attribute.DEATHRATTLES);
		SpellDesc deathrattle = SummonSpell.create(targetPlayer, minion.getSourceCard());
		SpellDesc addDeathrattleSpell = AddDeathrattleSpell.create(deathrattle);
		SpellDesc destroySpell = DestroySpell.create(target.getReference());
		SpellUtils.castChildSpell(context, player, destroySpell, source, target);
		for (int i = 0; i < desc.getValue(SpellArg.VALUE, context, player, target, source, 1); i++) {
			SpellUtils.castChildSpell(context, player, addDeathrattleSpell.clone(), source, source);
		}
	}

}
