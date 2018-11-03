package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Summons all the friendly minions that died this turn.
 * <p>
 * Implements Kel'Thuzad.
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
			if (deadEntity.getEntityType() != EntityType.MINION) {
				continue;
			}
			Minion deadMinion = (Minion) deadEntity;
			if (deadMinion.getAttributeValue(Attribute.DIED_ON_TURN) == currentTurn) {
				Card card = deadMinion.getSourceCard();
				context.getLogic().summon(player.getId(), card.summon(), null, -1, false);
			}
		}
	}

}

