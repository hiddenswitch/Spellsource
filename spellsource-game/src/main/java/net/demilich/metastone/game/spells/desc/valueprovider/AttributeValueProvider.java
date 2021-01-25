package net.demilich.metastone.game.spells.desc.valueprovider;

import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reduces the sum of all values of attribute {@link ValueProviderArg#ATTRIBUTE} on target entities for {@link
 * ValueProviderArg#TARGET}. Correctly accounts for {@link Attribute#ATTACK} and {@link Attribute#HP} when retrieved
 * from a {@link Card} or an {@link Actor} to include buffs and bonuses.
 */
public class AttributeValueProvider extends ValueProvider {
	public static ValueProviderDesc create(Attribute attribute, EntityReference target) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(AttributeValueProvider.class);
		arguments.put(ValueProviderArg.ATTRIBUTE, attribute);
		arguments.put(ValueProviderArg.TARGET, target);
		return new ValueProviderDesc(arguments);
	}

	public static ValueProviderDesc create(Attribute attribute) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(AttributeValueProvider.class);
		arguments.put(ValueProviderArg.ATTRIBUTE, attribute);
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

		if (getDesc().containsKey(ValueProviderArg.FILTER)) {
			EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
			entities = entities.stream().filter(filter.matcher(context, player, host)).collect(Collectors.toList());
		}

		if (entities == null) {
			return 0;
		}
		int value = 0;
		for (Entity entity : entities) {
			value += provideValueForAttribute(context, attribute, entity);
		}
		return value;
	}

	public static int provideValueForAttribute(GameContext context, Attribute attribute, Entity entity) {
		int value = 0;
		if (attribute == Attribute.INDEX) {
			value = entity.getEntityLocation().getIndex();
			return value;
		} else if (attribute == Attribute.INDEX_FROM_END) {
			value = entity.getEntityLocation().getIndex() - context.getPlayer(entity.getOwner()).getZone(entity.getZone()).size();
			return value;
		}

		if (entity instanceof Card) {
			Card card = (Card) entity;
			if (attribute == Attribute.ATTACK && card.getCardType() == CardType.MINION) {
				value += card.getDesc().getBaseAttack() + card.getBonusAttack();
			} else if (attribute == Attribute.HP && card.getCardType() == CardType.MINION) {
				value += card.getDesc().getBaseHp() + card.getBonusHp();
			} else {
				value += card.getAttributeValue(attribute);
			}
		} else {
			if (entity instanceof Actor) {
				Actor source = (Actor) entity;
				if (attribute == Attribute.ATTACK) {
					var player = context.getPlayer(source.getOwner());
					if (source.getEntityType()== EntityType.HERO && !player.getWeaponZone().isEmpty() && player.getWeaponZone().get(0).isActive()) {
						value += Math.max(0, player.getWeaponZone().get(0).getAttack());
					}
					value += Math.max(0, source.getAttack());
				} else if (attribute == Attribute.MAX_HP) {
					value += source.getMaxHp();
				} else if (attribute == Attribute.HP) {
					value += source.getHp();
				} else if (attribute == Attribute.BASE_MANA_COST) {
					value += source.getSourceCard().getBaseManaCost();
				} else if (attribute == Attribute.MAX_ATTACKS) {
					value += source.getMaxNumberOfAttacks();
				} else {
					value += source.getAttributeValue(attribute);
				}
			} else {
				value += entity.getAttributeValue(attribute);
			}
		}
		return value;
	}
}