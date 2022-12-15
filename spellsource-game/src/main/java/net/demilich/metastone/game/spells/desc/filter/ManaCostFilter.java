package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;

import java.util.Map;

/**
 * Gets the {@code target}'s in-hand mana cost or its base mana cost, and compares it using {@link
 * EntityFilterArg#OPERATION} to the value {@link EntityFilterArg#VALUE}.
 * <p>
 * The value is evaluated with a {@code null} target.
 */
public class ManaCostFilter extends EntityFilter {

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
		int actualValue = getManaCost(context, player, entity);
		int mana = getDesc().getValue(EntityFilterArg.VALUE, context, player, null, host, 0);
		ComparisonOperation operation = (ComparisonOperation) getDesc().get(EntityFilterArg.OPERATION);
		return SpellUtils.evaluateOperation(operation, actualValue, mana);
	}

	protected int getManaCost(GameContext context, Player player, Entity entity) {
		Card card = entity.getSourceCard();
		return context.getLogic().getModifiedManaCost(player, card);
	}
}
