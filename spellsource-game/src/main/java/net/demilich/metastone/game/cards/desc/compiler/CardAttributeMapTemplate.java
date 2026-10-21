package net.demilich.metastone.game.cards.desc.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Bytecode template for {@link net.demilich.metastone.game.cards.CardAttributeMap}. Woven into
 * {@code CardAttributeMap.class} at compile time by {@link SpellsourceClassfileWeaver}.
 */
public final class CardAttributeMapTemplate extends AttributeMapTemplate implements Cloneable, JsonSerializable, Serializable {

	@JsonIgnore
	private transient Card card;

	public CardAttributeMapTemplate(Card card) {
		super();
		this.card = card;
	}

	public boolean hasNoOverrides() {
		return super.isEmpty();
	}

	@Override
	public Set<Attribute> unsafeKeySet() {
		CardDesc desc = getCard().getDesc();
		if (super.isEmpty() && desc.isReadOnly()) {
			return desc.getStaticAttributeKeys();
		}
		Set<Attribute> keys = EnumSet.copyOf(desc.getStaticAttributeKeys());
		if (!super.isEmpty()) {
			keys.addAll(super.keySet());
		}
		final boolean weaponOrMinion = getCard().getCardType() == CardType.MINION
				|| getCard().getCardType() == CardType.WEAPON;
		if (weaponOrMinion) {
			keys.add(Attribute.HP);
			keys.add(Attribute.BASE_ATTACK);
			keys.add(Attribute.ATTACK);
		}
		if (getCard().getCardType() == CardType.HERO
				|| weaponOrMinion) {
			keys.add(Attribute.BASE_HP);
			keys.add(Attribute.MAX_HP);
		}
		return Collections.unmodifiableSet(keys);
	}

	@Override
	public Object getOrDefault(Object key, Object defaultValue) {
		Object value = get(key);
		return value != null ? value : defaultValue;
	}

	@Override
	public int getInt(Attribute attr, int defaultValue) {
		if (super.containsKey(attr)) {
			return super.getInt(attr, defaultValue);
		}
		CardDesc desc = getCard().getDesc();
		AttributeMapTemplate descAttributes = (AttributeMapTemplate) (Object) desc.getAttributes();
		if (descAttributes != null && descAttributes.containsKey(attr)) {
			return descAttributes.getInt(attr, defaultValue);
		}
		return switch (attr) {
			case BASE_MANA_COST -> desc.getBaseManaCost();
			case SECRET -> desc.getSecret() != null ? 1 : 0;
			case QUEST -> desc.getQuest() != null ? 1 : 0;
			case BASE_ATTACK, ATTACK -> {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					yield desc.getDamage() != 0 ? desc.getDamage() : desc.getBaseAttack();
				} else if (cardType == CardType.MINION) {
					yield desc.getBaseAttack();
				}
				yield defaultValue;
			}
			case BASE_HP -> {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					yield desc.getDurability();
				} else if (cardType == CardType.MINION) {
					yield desc.getBaseHp();
				} else if (cardType == CardType.HERO && descAttributes != null) {
					yield descAttributes.getInt(Attribute.MAX_HP, defaultValue);
				}
				yield defaultValue;
			}
			case HP, MAX_HP -> {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					yield desc.getDurability();
				} else if (cardType == CardType.MINION) {
					yield desc.getBaseHp();
				}
				yield defaultValue;
			}
			default -> defaultValue;
		};
	}

	@Override
	public boolean hasAttributeValue(Attribute attr) {
		if (super.containsKey(attr)) {
			return super.hasAttributeValue(attr);
		}
		CardDesc desc = getCard().getDesc();
		AttributeMapTemplate descAttributes = (AttributeMapTemplate) (Object) desc.getAttributes();
		if (descAttributes != null && descAttributes.containsKey(attr)) {
			return descAttributes.hasAttributeValue(attr);
		}
		return switch (attr) {
			case BASE_MANA_COST -> desc.getBaseManaCost() != 0 || AttributeMapTemplate.isStoresTurnNumber(attr);
			case HERO_CLASS -> desc.getHeroClass() != null;
			case MANA_COST_MODIFIER -> desc.getManaCostModifier() != null;
			case PASSIVE_TRIGGERS, DECK_TRIGGERS, GAME_TRIGGERS, RACE -> true;
			case SECRET -> desc.getSecret() != null;
			case QUEST -> desc.getQuest() != null;
			case BASE_ATTACK, ATTACK -> {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					yield (desc.getDamage() != 0 ? desc.getDamage() : desc.getBaseAttack()) != 0;
				} else if (cardType == CardType.MINION) {
					yield desc.getBaseAttack() != 0;
				}
				yield false;
			}
			case BASE_HP -> {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					yield desc.getDurability() != 0;
				} else if (cardType == CardType.MINION) {
					yield desc.getBaseHp() != 0;
				} else if (cardType == CardType.HERO && descAttributes != null) {
					yield descAttributes.hasAttributeValue(Attribute.MAX_HP);
				}
				yield false;
			}
			case HP, MAX_HP -> {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					yield desc.getDurability() != 0;
				} else if (cardType == CardType.MINION) {
					yield desc.getBaseHp() != 0;
				}
				yield false;
			}
			default -> false;
		};
	}

	private static final EnchantmentDesc[] EMPTY_TRIGGERS = new EnchantmentDesc[0];

	private static EnchantmentDesc[] linkTriggers(EnchantmentDesc single, EnchantmentDesc[] multi) {
		if (single == null && (multi == null || multi.length == 0)) {
			return EMPTY_TRIGGERS;
		}
		if (single != null && (multi == null || multi.length == 0)) {
			return new EnchantmentDesc[]{single};
		}
		return multi;
	}

	@Override
	public Object get(Object key) {
		Object value = super.get(key);
		if (value != null) {
			return value;
		}
		Attribute attr = (Attribute) key;
		Card card = getCard();
		CardDesc desc = card.getDesc();
		AttributeMapTemplate descAttributes = (AttributeMapTemplate) (Object) desc.getAttributes();
		if (descAttributes != null) {
			Object descVal = descAttributes.get(attr);
			if (descVal != null) {
				return descVal;
			}
		}

		switch (attr) {
			case BASE_MANA_COST:
				return desc.getBaseManaCost();
			case HERO_CLASS:
				return desc.getHeroClass();
			case MANA_COST_MODIFIER:
				return desc.getManaCostModifier() == null ? null : desc.getManaCostModifier().create();
			case PASSIVE_TRIGGERS:
				return linkTriggers(desc.getPassiveTrigger(), desc.getPassiveTriggers());
			case DECK_TRIGGERS:
				return linkTriggers(desc.getDeckTrigger(), desc.getDeckTriggers());
			case GAME_TRIGGERS:
				return desc.getGameTriggers();
			case RACE:
				return desc.getRace() == null ? Race.NONE : desc.getRace();
			case SECRET:
				return desc.getSecret() != null;
			case QUEST:
				return desc.getQuest() != null;
			case BASE_ATTACK:
			case ATTACK: {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					return desc.getDamage() != 0 ? desc.getDamage() : desc.getBaseAttack();
				} else if (cardType == CardType.MINION) {
					return desc.getBaseAttack();
				}
				return null;
			}
			case BASE_HP: {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					return desc.getDurability();
				} else if (cardType == CardType.MINION) {
					return desc.getBaseHp();
				} else if (cardType == CardType.HERO) {
					return descAttributes != null ? descAttributes.get(Attribute.MAX_HP) : null;
				}
				return null;
			}
			case HP:
			case MAX_HP: {
				CardType cardType = desc.getType();
				if (cardType == CardType.WEAPON) {
					return desc.getDurability();
				} else if (cardType == CardType.MINION) {
					return desc.getBaseHp();
				}
				return null;
			}
			default:
				return null;
		}
	}

	@Override
	public boolean containsKey(Object key) {
		if (super.containsKey(key)) {
			return true;
		}
		Attribute attr = (Attribute) key;
		Card card = getCard();
		CardDesc desc = card.getDesc();
		switch (attr) {
			case BASE_MANA_COST:
			case HERO_CLASS:
				return true;
			case MANA_COST_MODIFIER:
				return desc.getManaCostModifier() != null;
			case PASSIVE_TRIGGERS:
				return desc.getPassiveTrigger() != null || (desc.getPassiveTriggers() != null && desc.getPassiveTriggers().length > 0);
			case DECK_TRIGGERS:
				return desc.getDeckTrigger() != null || (desc.getDeckTriggers() != null && desc.getDeckTriggers().length > 0);
			case GAME_TRIGGERS:
				return desc.getGameTriggers() != null && desc.getGameTriggers().length > 0;
			case RACE:
				return desc.getRace() != null && !desc.getRace().equals(Race.NONE);
			case SECRET:
				return desc.getSecret() != null || (desc.getAttributes() != null && desc.getAttributes().containsKey(Attribute.SECRET));
			case QUEST:
				return desc.getQuest() != null;
			case BASE_ATTACK:
			case ATTACK:
			case HP: {
				CardType cardType = desc.getType();
				return cardType == CardType.MINION
						|| cardType == CardType.WEAPON;
			}
			case BASE_HP:
			case MAX_HP: {
				CardType cardType = desc.getType();
				return cardType == CardType.MINION
						|| cardType == CardType.WEAPON
						|| cardType == CardType.HERO;
			}
			default:
				break;
		}

		return desc.getAttributes() != null
				&& desc.getAttributes().containsKey(key);
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	@Override
	public CardAttributeMapTemplate clone() {
		CardAttributeMapTemplate clone = (CardAttributeMapTemplate) super.clone();
		clone.setCard(getCard());
		return clone;
	}

	public String getOverrideCardId() {
		return super.getOverrideCardIdFast();
	}

	@Override
	public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStartObject();
		innerSerialize(gen);
		gen.writeEndObject();
	}

	private void innerSerialize(JsonGenerator gen) throws IOException {
		for (Entry<Attribute, Object> entry : super.entrySet()) {
			gen.writeFieldName(entry.getKey().name());
			gen.writeObject(entry.getValue());
		}
	}

	@Override
	public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
		serialize(gen, serializers);
	}
}
