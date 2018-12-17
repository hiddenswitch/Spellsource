package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;

import java.util.Map;

public class ManaCostFilter extends EntityFilter {

	private static final long serialVersionUID = 3107071217913251106L;

	public static ManaCostFilter create(int manaCost, ComparisonOperation operation) {
		Map<EntityFilterArg, Object> arguments = new EntityFilterDesc(ManaCostFilter.class);
		arguments.put(EntityFilterArg.VALUE, manaCost);
		arguments.put(EntityFilterArg.OPERATION, operation);
		return new ManaCostFilter(new EntityFilterDesc(arguments));
	}

	public ManaCostFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		Card card = entity.getSourceCard();
		int mana = getDesc().getValue(EntityFilterArg.VALUE, context, player, null, host, 0);
		ComparisonOperation operation = (ComparisonOperation) getDesc().get(EntityFilterArg.OPERATION);
		int actualValue = context.getLogic().getModifiedManaCost(player, card);
		return SpellUtils.evaluateOperation(operation, actualValue, mana);
	}
}
