package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.cards.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Filters entities according to their {@link Attribute} set on {@link EntityFilterArg#ATTRIBUTE}.
 * <p>
 * If a {@link EntityFilterArg#OPERATION} and {@link EntityFilterArg#VALUE} are specified, uses it to evaluate the
 * attribute. Otherwise, the operation defaults to {@link ComparisonOperation#HAS}.
 */
public final class AttributeFilter extends EntityFilter {

	public AttributeFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@NotNull
	private static EntityFilter create(Attribute attribute, Object value) {
		EntityFilterDesc desc = new EntityFilterDesc(AttributeFilter.class);
		desc.put(EntityFilterArg.ATTRIBUTE, attribute);
		desc.put(EntityFilterArg.VALUE, value);
		desc.put(EntityFilterArg.OPERATION, ComparisonOperation.EQUAL);
		return new AttributeFilter(desc);
	}

	public static EntityFilter create(Attribute attribute, ValueProvider value) {
		return create(attribute, (Object) value);
	}

	public static EntityFilter create(Attribute attribute, int value) {
		return create(attribute, (Object) value);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		List<Entity> entities = getTargetedEntities(context, player, host);
		Attribute attribute = (Attribute) getDesc().get(EntityFilterArg.ATTRIBUTE);
		ComparisonOperation operation = (ComparisonOperation) getDesc().get(EntityFilterArg.OPERATION);

		if (operation == null) {
			operation = ComparisonOperation.HAS;
		}

		if (operation == ComparisonOperation.HAS) {
			return entity.hasAttribute(attribute);
		}

		int targetValue;
		if (entities == null) {
			targetValue = getDesc().getValue(EntityFilterArg.VALUE, context, player, null, host, 0);
		} else {
			targetValue = getDesc().getValue(EntityFilterArg.VALUE, context, player, entities.get(0), host, 0);
		}

		int actualValue = -1;
		if (attribute == Attribute.ATTACK) {
			if (entity instanceof Card) {
				final Card actorCard = (Card) entity;
				actualValue = actorCard.getBaseAttack() + actorCard.getBonusAttack();
			} else if (entity instanceof Actor) {
				actualValue = ((Actor) entity).getAttack();
			} else {
				actualValue = entity.getAttributeValue(attribute);
			}
		} else if (attribute == Attribute.HP) {
			if (entity instanceof Card) {
				final Card actorCard = (Card) entity;
				actualValue = actorCard.getBaseHp() + actorCard.getBonusHp();
			} else if (entity instanceof Actor) {
				actualValue = ((Actor) entity).getHp();
			} else {
				actualValue = entity.getAttributeValue(attribute);
			}
		} else if (attribute == Attribute.INDEX) {
			actualValue += entity.getEntityLocation().getIndex();
		} else if (attribute == Attribute.INDEX_FROM_END) {
			actualValue += entity.getEntityLocation().getIndex() - context.getPlayer(entity.getOwner()).getZone(entity.getZone()).size();
		} else {
			actualValue = entity.getAttributeValue(attribute);
		}

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}
}