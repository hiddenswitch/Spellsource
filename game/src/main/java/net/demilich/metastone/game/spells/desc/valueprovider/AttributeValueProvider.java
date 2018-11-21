package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeValueProvider extends ValueProvider {
	public static ValueProviderDesc create(Attribute attribute, EntityReference target) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(AttributeValueProvider.class);
		arguments.put(ValueProviderArg.ATTRIBUTE, attribute);
		arguments.put(ValueProviderArg.TARGET, target);
		return new ValueProviderDesc(arguments);
	}

	public AttributeValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference sourceReference = (EntityReference) getDesc().get(ValueProviderArg.TARGET);
		Attribute attribute = (Attribute) getDesc().get(ValueProviderArg.ATTRIBUTE);
		List<Entity> entities = null;
		if (sourceReference != null) {
			entities = context.resolveTarget(player, host, sourceReference);
		} else {
			entities = new ArrayList<>();
			entities.add(target);
		}

		if (entities == null) {
			return 0;
		}
		int value = 0;
		for (Entity entity : entities) {
			if (attribute == Attribute.INDEX) {
				value = entity.getEntityLocation().getIndex();
				continue;
			} else if (attribute == Attribute.INDEX_FROM_END) {
				value = entity.getEntityLocation().getIndex() - context.getPlayer(entity.getOwner()).getZone(entity.getZone()).size();
				continue;
			}

			if (entity instanceof Card) {
				Card card = (Card) entity;
				if (attribute == Attribute.ATTACK) {
					value += card.getDesc().baseAttack + card.getBonusAttack();
				} else if (attribute == Attribute.HP) {
					value += card.getDesc().baseHp + card.getBonusHp();
				} else value += card.getAttributeValue(attribute);
			} else {
				if (entity instanceof Actor) {
					Actor source = (Actor) entity;
					if (attribute == Attribute.ATTACK) {
						value += source.getAttack();
					} else if (attribute == Attribute.MAX_HP) {
						value += source.getMaxHp();
					} else if (attribute == Attribute.HP) {
						value += source.getHp();
					} else if (attribute == Attribute.BASE_MANA_COST) {
						value += source.getSourceCard().getBaseManaCost();
					} else {
						value += source.getAttributeValue(attribute);
					}
				} else {
					value += entity.getAttributeValue(attribute);
				}
			}
		}
		return value;
	}
}