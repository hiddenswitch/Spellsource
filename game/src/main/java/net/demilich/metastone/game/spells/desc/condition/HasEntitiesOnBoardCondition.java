package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code true} if all the {@link ConditionArg#CARDS} are on the board. Duplicates are counted.
 */
public class HasEntitiesOnBoardCondition extends Condition {

	public HasEntitiesOnBoardCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var cardNames = (String[]) desc.get(ConditionArg.CARDS);

		List<Actor> checkedActors = new ArrayList<Actor>(player.getMinions());
		if (!player.getWeaponZone().isEmpty()) {
			checkedActors.add(player.getWeaponZone().get(0));
		}
		checkedActors.add(player.getHero());

		for (var cardName : cardNames) {
			var check = false;
			for (var actor : checkedActors) {
				if (actor.getSourceCard().getCardId().equalsIgnoreCase(cardName)) {
					check = true;
					checkedActors.remove(actor);
					break;
				}
			}
			if (!check) {
				return false;
			}
		}
		return true;
	}

}
