package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Summons all the friendly minions that died this turn.
 * <p>
 * Implements Kel'Thuzad, Altered Fate and others.
 */
public class SummonFriendlyMinionsThatDiedSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SummonFriendlyMinionsThatDiedSpell.class);

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(SummonFriendlyMinionsThatDiedSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		int currentTurn = context.getTurn();
		List<Entity> graveyardSnapshot = new ArrayList<>(player.getGraveyard());
		for (Entity deadEntity : graveyardSnapshot) {
			if (!deadEntity.diedOnBattlefield()) {
				continue;
			}
			if (desc.containsKey(SpellArg.FILTER)) {
				EntityFilter filter = (EntityFilter) desc.get(SpellArg.FILTER);
				if (!filter.matches(context, player, deadEntity, source)) {
					continue;
				}
			}
			Minion deadMinion = (Minion) deadEntity;
			if (deadMinion.getAttributeValue(Attribute.DIED_ON_TURN) == currentTurn) {
				Card card = deadMinion.getSourceCard();
				Minion summon = card.summon();
				boolean summoned = context.getLogic().summon(player.getId(), summon, source, -1, false);
				SpellDesc subSpell = desc.getSpell();
				if (summoned && subSpell != null && summon.isInPlay()) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, summon);
				}
			}
		}
	}
}

