package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.MissilesSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Fires {@link SpellArg#HOW_MANY} missiles at the {@link SpellArg#TARGET} entities. If all the targets actually hit
 * match {@link SpellArg#CARD_FILTER}, call the subspell {@link SpellArg#SPELL}.
 * <p>
 * Implements Formless Agony.
 */
public final class FormlessAgonyMissilesSpell extends MissilesSpell {

	private List<Actor> targets;

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		this.targets = new ArrayList<>(targets.size());
		super.cast(context, player, desc, source, targets);
		if (targets.stream().allMatch(desc.getCardFilter().matcher(context, player, source))) {
			SpellUtils.castChildSpell(context, player, desc.getSpell(), source, null);
		}
		targets = null;
	}

	@Override
	public Actor getRandomTarget(GameContext context, List<Entity> validTargets) {
		Actor selected = super.getRandomTarget(context, validTargets);
		targets.add(selected);
		return selected;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}
