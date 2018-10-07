package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.utils.Attribute;

import java.util.List;

public class AttributeFilter extends EntityFilter {

	public AttributeFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		List<Entity> entities = getTargetedEntities(context, player, host);
		Attribute attribute = (Attribute) getDesc().get(EntityFilterArg.ATTRIBUTE);
		Operation operation = (Operation) getDesc().get(EntityFilterArg.OPERATION);

		if (operation == null) {
			operation = Operation.HAS;
		}

		if (operation == Operation.HAS) {
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
		}
			else if (attribute == Attribute.INDEX) {
			actualValue += entity.getEntityLocation().getIndex();
			} else if (attribute == Attribute.INDEX_FROM_END) {
			actualValue += entity.getEntityLocation().getIndex() - context.getPlayer(entity.getOwner()).getZone(entity.getZone()).size();
		} else {
			actualValue = entity.getAttributeValue(attribute);
		}

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

}