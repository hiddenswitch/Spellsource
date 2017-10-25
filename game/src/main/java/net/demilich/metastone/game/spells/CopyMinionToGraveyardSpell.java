package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class CopyMinionToGraveyardSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Minion minion = (Minion) target;
		Minion copy = minion.getCopy();
		copy.setId(context.getLogic().getIdFactory().generateId());
		copy.setOwner(minion.getOwner());
		copy.moveOrAddTo(context, Zones.GRAVEYARD);
	}
}
