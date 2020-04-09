package net.demilich.metastone.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.hiddenswitch.spellsource.client.models.CardType;
import io.vertx.core.impl.ConcurrentHashSet;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

/**
 * A hashmap that can contain "overrides" to a {@link CardDesc}. This allows cards to assume other identities while
 * retaining their enchantments by changing their {@link Attribute#CARD_ID} or {@link Attribute#AURA_CARD_ID}.
 */
public final class CardAttributeMap extends AttributeMap implements Cloneable, JsonSerializable, Serializable {

	@JsonIgnore
	private Card card;

	public CardAttributeMap(Card card) {
		super();
		this.card = card;
	}

	public CardAttributeMap() {
		super();
	}

	public Set<Attribute> unsafeKeySet() {
		Set<Attribute> keys = new ConcurrentHashSet<>();
		keys.addAll(super.keySet());
		CardDesc desc = getCard().getDesc();
		AttributeMap attributes = desc.getAttributes();
		if (attributes != null) {
			keys.addAll(attributes.keySet());
		}
		keys.add(Attribute.BASE_MANA_COST);
		if (desc.getManaCostModifier() != null) {
			keys.add(Attribute.MANA_COST_MODIFIER);
		}
		if (desc.getPassiveTrigger() != null || (desc.getPassiveTriggers() != null && desc.getPassiveTriggers().length > 0)) {
			keys.add(Attribute.PASSIVE_TRIGGERS);
		}
		if (desc.getAuras() != null && desc.getAuras().length > 0) {
			keys.add(Attribute.PASSIVE_AURAS);
		}
		if (desc.getDeckTrigger() != null
				|| desc.getDeckTriggers() != null) {
			keys.add(Attribute.DECK_TRIGGERS);
		}
		if (desc.getGameTriggers() != null) {
			keys.add(Attribute.GAME_TRIGGERS);
		}
		if (desc.getRace() != null) {
			keys.add(Attribute.RACE);
		}
		if (desc.getSecret() != null) {
			keys.add(Attribute.SECRET);
		}
		if (desc.getQuest() != null) {
			keys.add(Attribute.QUEST);
		}
		if (desc.getHeroClass() != null) {
			keys.add(Attribute.HERO_CLASS);
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

		return keys;
	}

	@Override
	// TODO: Cache this
	public Object get(Object key) {
		Attribute attr = (Attribute) key;
		CardDesc desc = getCard().getDesc();
		if (super.get(key) != null) {
			return super.get(key);
		} else {
			// Retrieves things from the desc specified in the card

			if (desc == null) {
				return super.get(key);
			}

			if (desc.getAttributes() != null
					&& desc.getAttributes().containsKey(attr)) {
				return desc.getAttributes().get(attr);
			}

			switch (attr) {
				case BASE_MANA_COST:
					return desc.getBaseManaCost();
				case HERO_CLASS:
					return desc.getHeroClass();
				case MANA_COST_MODIFIER:
					return desc.getManaCostModifier() == null ? null : desc.getManaCostModifier().create();
				case PASSIVE_TRIGGERS:
					return HasEntrySet.link(desc.getPassiveTrigger(), desc.getPassiveTriggers(), EnchantmentDesc.class);
				case DECK_TRIGGERS:
					return HasEntrySet.link(desc.getDeckTrigger(), desc.getDeckTriggers(), EnchantmentDesc.class);
				case GAME_TRIGGERS:
					return desc.getGameTriggers();
				case PASSIVE_AURAS:
					return desc.getPassiveAuras();
				case RACE:
					return desc.getRace() == null ? Race.NONE : desc.getRace();
				case SECRET:
					return desc.getSecret() != null;
				case QUEST:
					return desc.getQuest() != null;
			}

			CardType cardType = getCard().getCardType();
			switch (cardType) {
				case WEAPON:
					switch (attr) {
						case BASE_ATTACK:
						case ATTACK:
							return desc.getDamage();
						case BASE_HP:
						case HP:
						case MAX_HP:
							return desc.getDurability();
					}
					break;
				case MINION:
					switch (attr) {
						case BASE_ATTACK:
						case ATTACK:
							return desc.getBaseAttack();
						case BASE_HP:
						case HP:
						case MAX_HP:
							return desc.getBaseHp();
					}
					break;
				case HERO:
					switch (attr) {
						case BASE_HP:
							return desc.getAttributes().get(Attribute.MAX_HP);
					}
			}
		}

		return super.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		CardDesc desc = getCard().getDesc();
		Attribute attr = (Attribute) key;
		CardType cardType = getCard().getCardType();
		switch (attr) {
			case BASE_MANA_COST:
			case HERO_CLASS:
				return true;
			case MANA_COST_MODIFIER:
				return desc.getManaCostModifier() != null;
			case PASSIVE_TRIGGERS:
				return desc.getPassiveTrigger() != null || (desc.getPassiveTriggers() != null && desc.getPassiveTriggers().length > 0);
			case PASSIVE_AURAS:
				return desc.getPassiveAuras() != null && desc.getPassiveAuras().length > 0;
			case DECK_TRIGGERS:
				return desc.getDeckTrigger() != null || (desc.getDeckTriggers() != null && desc.getDeckTriggers().length > 0);
			case GAME_TRIGGERS:
				return desc.getGameTriggers() != null;
			case RACE:
				return desc.getRace() != null;
			case SECRET:
				return desc.getSecret() != null || getCard().hasAttribute(Attribute.SECRET);
			case QUEST:
				return desc.getQuest() != null;
			case BASE_ATTACK:
			case ATTACK:
			case HP:
				return cardType == CardType.MINION
						|| cardType == CardType.WEAPON;
			case BASE_HP:
			case MAX_HP:
				return cardType == CardType.MINION
						|| cardType == CardType.WEAPON
						|| cardType == CardType.HERO;
		}

		boolean containsDescAttributes = desc.getAttributes() != null
				&& desc.getAttributes().containsKey(key);

		return containsDescAttributes || (super.get(key) != null);
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	@Override
	public CardAttributeMap clone() {
		CardAttributeMap clone = new CardAttributeMap(getCard());
		clone.putAll(this);
		return clone;
	}

	public Set<Entry<Attribute, Object>> unsafeEntrySet() {
		ConcurrentHashSet<Entry<Attribute, Object>> unsafe = new ConcurrentHashSet<>();
		for (Attribute key : unsafeKeySet()) {
			unsafe.add(new SimpleEntry<>(key, get(key)));
		}
		return unsafe;
	}

	/**
	 * Returns the card ID from the {@link Attribute#AURA_CARD_ID} or {@link Attribute#CARD_ID}, attributes that can
	 * override the card's ID and change how it behaves.
	 *
	 * @return
	 */
	String getOverrideCardId() {
		String cardId = (String) super.get(Attribute.AURA_CARD_ID);
		if (cardId == null) {
			cardId = (String) super.get(Attribute.CARD_ID);
		}
		return cardId;
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
