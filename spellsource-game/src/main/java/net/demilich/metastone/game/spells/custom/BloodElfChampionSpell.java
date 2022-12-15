package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.SwapHpSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Swaps the {@code source} summoning minion's health with the {@code targets}, splitting the health of the source among
 * all the targets and gaining the sum of the targets' health. The remainder from the split goes to the first minion.
 */
public final class BloodElfChampionSpell extends SwapHpSpell {
	private static Logger LOGGER = LoggerFactory.getLogger(BloodElfChampionSpell.class);

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		int targetCount = targets.size();
		if (targetCount == 0) {
			return;
		}
		if (desc.getBool(SpellArg.RANDOM_TARGET)) {
			targets = Collections.singletonList(context.getLogic().getRandom(targets));
		}
		EntityReference peek = context.getSummonReferenceStack().peek();
		if (peek == null) {
			LOGGER.warn("cast {} {}: Trying to Blood Elf during a non-summoning effect, cancelling", context.getGameId(), source);
			return;
		}
		Minion sourceMinion = (Minion) context.resolveSingleTarget(peek);
		int hp = sourceMinion.getHp();
		int targetHealth = hp / targetCount;
		int targetHealthWithRemainder = (hp / targetCount) + (hp % targetCount);
		int newHp = 0;
		newHp += ((Actor) targets.get(0)).getHp();
		targets.get(0).setAttribute(Attribute.HP_BONUS, 0);
		context.getLogic().setHpAndMaxHp((Actor) targets.get(0), targetHealthWithRemainder);
		for (int i = 1; i < targetCount; i++) {
			newHp += ((Actor) targets.get(i)).getHp();
			targets.get(i).setAttribute(Attribute.HP_BONUS, 0);
			context.getLogic().setHpAndMaxHp((Actor) targets.get(i), targetHealth);
		}
		context.getLogic().setHpAndMaxHp(sourceMinion, newHp);
	}
}
