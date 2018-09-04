package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Acts like an {@link AdjacentEffectSpell} except on minions opposite of the {@code target}
 */
public final class OppositeEffectSpell extends RelativeToTargetEffectSpell {

	@Override
	protected List<Actor> getActors(GameContext context, Entity target) {
		return context.getOppositeMinions(target.getReference());
	}
}
