package net.demilich.metastone.game.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import io.vertx.core.impl.ConcurrentHashSet;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Set;

/**
 * A hashmap that can contain "overrides" to a {@link CardDesc}. This allows cards to assume other identities while
 * retaining their enchantments by changing their {@link Attribute#CARD_ID} or {@link Attribute#AURA_CARD_ID}.
 */
public final class CardAttributes extends AttributeMap implements Cloneable, JsonSerializable, Serializable {
	@JsonIgnore
	private Card card;

	public CardAttributes(Card card) {
		super();
		this.card = card;
	}

	public CardAttributes() {
		super();
	}

	public Set<Attribute> unsafeKeySet() {
		Set<Attribute> keys = new ConcurrentHashSet<>();
		keys.addAll(super.keySet());
		CardDesc desc = getCard().getDesc();
		AttributeMap attributes = desc.attributes;
		if (attributes != null) {
			keys.addAll(attributes.keySet());
		}
		keys.add(Attribute.BASE_MANA_COST);
		if (desc.manaCostModifier != null) {
			keys.add(Attribute.MANA_COST_MODIFIER);
		}
		if (desc.passiveTrigger != null || (desc.passiveTriggers != null && desc.passiveTriggers.length > 0)) {
			keys.add(Attribute.PASSIVE_TRIGGERS);
		}
		if (desc.deckTrigger != null || (desc.deckTriggers != null && desc.deckTriggers.length > 0)) {
			keys.add(Attribute.DECK_TRIGGERS);
		}
		if (desc.gameTriggers != null) {
			keys.add(Attribute.GAME_TRIGGERS);
		}
		if (desc.race != null) {
			keys.add(Attribute.RACE);
		}
		if (desc.secret != null) {
			keys.add(Attribute.SECRET);
		}
		if (desc.quest != null) {
			keys.add(Attribute.QUEST);
		}
		if (desc.heroClass != null) {
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

			if (desc.attributes != null
					&& desc.attributes.containsKey(attr)) {
				return desc.attributes.get(attr);
			}

			switch (attr) {
				case BASE_MANA_COST:
					return desc.baseManaCost;
				case HERO_CLASS:
					return desc.heroClass;
				case MANA_COST_MODIFIER:
					return desc.manaCostModifier == null ? null : desc.manaCostModifier.create();
				case PASSIVE_TRIGGERS:
					return link(desc.passiveTrigger, desc.passiveTriggers, EnchantmentDesc.class);
				case DECK_TRIGGERS:
					return link(desc.deckTrigger, desc.deckTriggers, EnchantmentDesc.class);
				case GAME_TRIGGERS:
					return desc.gameTriggers;
				case RACE:
					return desc.race == null ? Race.NONE : desc.race;
				case SECRET:
					return desc.secret != null;
				case QUEST:
					return desc.quest != null;
			}

			CardType cardType = getCard().getCardType();
			switch (cardType) {
				case WEAPON:
					switch (attr) {
						case BASE_ATTACK:
						case ATTACK:
							return desc.damage;
						case BASE_HP:
						case HP:
						case MAX_HP:
							return desc.durability;
					}
					break;
				case MINION:
					switch (attr) {
						case BASE_ATTACK:
						case ATTACK:
							return desc.baseAttack;
						case BASE_HP:
						case HP:
						case MAX_HP:
							return desc.baseHp;
					}
					break;
				case HERO:
					switch (attr) {
						case BASE_HP:
							return desc.attributes.get(Attribute.MAX_HP);
					}
			}
		}

		return super.get(key);
	}

	@NotNull
	public <T> T[] link(T single, T[] multi, Class<? extends T> tClass) {
		if (single == null && (multi == null || multi.length == 0)) {
			return (T[]) Array.newInstance(tClass, 0);
		}
		if (single != null && (multi == null || multi.length == 0)) {
			T[] out = (T[]) Array.newInstance(tClass, 1);
			out[0] = single;
			return out;
		}
		return multi;
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
				return desc.manaCostModifier != null;
			case PASSIVE_TRIGGERS:
				return desc.passiveTrigger != null || (desc.passiveTriggers != null && desc.passiveTriggers.length > 0);
			case DECK_TRIGGERS:
				return desc.deckTrigger != null || (desc.deckTriggers != null && desc.deckTriggers.length > 0);
			case GAME_TRIGGERS:
				return desc.gameTriggers != null;
			case RACE:
				return desc.race != null;
			case SECRET:
				return desc.secret != null;
			case QUEST:
				return desc.quest != null;
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

		boolean containsDescAttributes = desc.attributes != null
				&& desc.attributes.containsKey(key);

		return containsDescAttributes || (super.get(key) != null);
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	@Override
	public CardAttributes clone() {
		CardAttributes clone = new CardAttributes(getCard());
		synchronized (this) {
			clone.putAll(this);
		}
		return clone;
	}

	public Set<Entry<Attribute, Object>> unsafeEntrySet() {
		ConcurrentHashSet<Entry<Attribute, Object>> unsafe = new ConcurrentHashSet<>();
		for (Attribute key : unsafeKeySet()) {
			unsafe.add(new SimpleEntry<>(key, get(key)));
		}
		return unsafe;
	}

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
