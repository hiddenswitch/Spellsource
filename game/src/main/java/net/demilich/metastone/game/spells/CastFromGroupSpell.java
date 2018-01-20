package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class CastFromGroupSpell extends Spell {

	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CastFromGroupSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}

	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, final List<Entity> originalTargets) {
		final List<Entity> targets;
		if (originalTargets == null) {
			targets = Collections.emptyList();
		} else {
			targets = originalTargets;
		}
		EntityFilter targetFilter = desc.getEntityFilter();
		List<Entity> validTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter);
		Entity randomTarget = null;
		if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
			randomTarget = context.getLogic().getRandom(validTargets);
		}
		SpellDesc[] group = SpellUtils.getGroup(context, desc);
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 3);
		int count = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);
		boolean exclusive = (boolean) desc.getOrDefault(SpellArg.EXCLUSIVE, false);
		List<SpellDesc> allChoices = new ArrayList<SpellDesc>();
		Collections.addAll(allChoices, group);

		for (int j = 0; j < count; j++) {
			List<SpellDesc> thisRoundsPossibleChoices = new ArrayList<SpellDesc>(allChoices);
			List<SpellDesc> thisRoundsChoices = new ArrayList<SpellDesc>();
			for (int i = 0; i < howMany; i++) {
				SpellDesc spell;
				spell = context.getLogic().removeRandom(thisRoundsPossibleChoices);
				thisRoundsChoices.add(spell);
			}

			if (thisRoundsChoices.isEmpty()) {
				return;
			}

			SpellDesc chosen = SpellUtils.getSpellDiscover(context, player, desc, thisRoundsChoices, source).getSpell();

			if (exclusive) {
				allChoices.remove(chosen);
			}

			if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
				onCast(context, player, chosen, source, randomTarget);
			} else if (validTargets.size() == 0 && originalTargets == null) {
				onCast(context, player, chosen, source, null);
			} else {
				// there is at least one target and RANDOM_TARGET flag is not set,
				// cast in on all targets
				for (Entity target : validTargets) {
					context.getSpellTargetStack().push(target.getReference());
					onCast(context, player, chosen, source, target);
					context.getSpellTargetStack().pop();
				}
			}
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellUtils.castChildSpell(context, player, desc, source, target);
	}

}
