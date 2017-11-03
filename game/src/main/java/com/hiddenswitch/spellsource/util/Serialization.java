package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.cards.costmodifier.OneTurnCostModifier;
import net.demilich.metastone.game.cards.costmodifier.ToggleCostModifier;
import net.demilich.metastone.game.cards.desc.*;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.spells.aura.AttributeAura;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.aura.BuffAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.filter.FilterDesc;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDescSerializer;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.AttributeMap;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class Serialization {
	static private Gson gson;

	static {
		configureGson();
	}

	@SuppressWarnings("unchecked")
	private static void configureGson() {
		RuntimeTypeAdapterFactory<GameAction> gameActions = RuntimeTypeAdapterFactory.of(GameAction.class, "actionType");
		gameActions.registerSubtype(EndTurnAction.class, ActionType.END_TURN.toString());
		gameActions.registerSubtype(PhysicalAttackAction.class, ActionType.PHYSICAL_ATTACK.toString());
		gameActions.registerSubtype(PlaySpellCardAction.class, ActionType.SPELL.toString());
		gameActions.registerSubtype(PlayMinionCardAction.class, ActionType.SUMMON.toString());
		gameActions.registerSubtype(PlayHeroCardAction.class, ActionType.HERO.toString());
		gameActions.registerSubtype(HeroPowerAction.class, ActionType.HERO_POWER.toString());
		gameActions.registerSubtype(BattlecryAction.class, ActionType.BATTLECRY.toString());
		gameActions.registerSubtype(PlayWeaponCardAction.class, ActionType.EQUIP_WEAPON.toString());
		gameActions.registerSubtype(DiscoverAction.class, ActionType.DISCOVER.toString());

		RuntimeTypeAdapterFactory<CardDesc> descs = RuntimeTypeAdapterFactory.of(CardDesc.class, "descType");
		descs.registerSubtype(ChooseOneCardDesc.class, CardType.CHOOSE_ONE.toString());
		descs.registerSubtype(ActorCardDesc.class, "ACTOR");
		descs.registerSubtype(MinionCardDesc.class, CardType.MINION.toString());
		descs.registerSubtype(ChooseBattlecryCardDesc.class, "CHOOSE_BATTLECRY");
		descs.registerSubtype(ChooseBattlecryHeroCardDesc.class, "CHOOSE_BATTLECRY_HERO");
		descs.registerSubtype(WeaponCardDesc.class, CardType.WEAPON.toString());
		descs.registerSubtype(SpellCardDesc.class, CardType.SPELL.toString());
		descs.registerSubtype(QuestCardDesc.class, "QUEST");
		descs.registerSubtype(GroupCardDesc.class, CardType.GROUP.toString());
		descs.registerSubtype(SecretCardDesc.class, "SECRET");
		descs.registerSubtype(HeroPowerCardDesc.class, CardType.HERO_POWER.toString());
		descs.registerSubtype(HeroCardDesc.class, CardType.HERO.toString());

		RuntimeTypeAdapterFactory<Entity> entities = RuntimeTypeAdapterFactory.of(Entity.class, "entityType");
		entities.registerSubtype(Player.class, "PLAYER");
		RuntimeTypeAdapterFactory<Actor> actors = RuntimeTypeAdapterFactory.of(Actor.class, "entityType");
		for (RuntimeTypeAdapterFactory factory : new RuntimeTypeAdapterFactory[]{entities, actors}) {
			factory.registerSubtype(Hero.class, "HERO");
			factory.registerSubtype(Minion.class, "MINION");
			factory.registerSubtype(Weapon.class, "WEAPON");
		}

		RuntimeTypeAdapterFactory<Card> cards = RuntimeTypeAdapterFactory.of(Card.class, "entityType");
		for (RuntimeTypeAdapterFactory factory : new RuntimeTypeAdapterFactory[]{entities, cards}) {
			factory.registerSubtype(ChooseOneCard.class, "CHOOSE_ONE_CARD");
			factory.registerSubtype(ChooseBattlecryMinionCard.class, "CHOOSE_BATTLECRY_CARD");
			factory.registerSubtype(HeroCard.class, "HERO_CARD");
			factory.registerSubtype(ChooseBattlecryHeroCard.class, "CHOOSE_BATTLECRY_HERO_CARD");
			factory.registerSubtype(MinionCard.class, "MINION_CARD");
			factory.registerSubtype(SpellCard.class, "SPELL_CARD");
			factory.registerSubtype(WeaponCard.class, "WEAPON_CARD");
			factory.registerSubtype(HeroPowerCard.class, "HERO_POWER_CARD");
			factory.registerSubtype(SecretCard.class, "SECRET_CARD");
			factory.registerSubtype(QuestCard.class, "QUEST_CARD");
			factory.registerSubtype(GroupCard.class, "GROUP_CARD");
		}

		RuntimeTypeAdapterFactory<Trigger> listeners = RuntimeTypeAdapterFactory.of(Trigger.class, "type");
		listeners.registerSubtype(Enchantment.class, "SPELL_TRIGGER");
		listeners.registerSubtype(Secret.class, "SECRET");
		listeners.registerSubtype(Quest.class, "QUEST");
		listeners.registerSubtype(Aura.class, "AURA");
		listeners.registerSubtype(BuffAura.class, "BUFF_AURA");
		listeners.registerSubtype(AttributeAura.class, "ATTRIBUTE_AURA");
		listeners.registerSubtype(CardCostModifier.class, "CARD_COST_MODIFIER");
		listeners.registerSubtype(ToggleCostModifier.class, "TOGGLE_COST_MODIFIER");
		listeners.registerSubtype(OneTurnCostModifier.class, "ONE_TURN_COST_MODIFIER");

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapterFactory(gameActions);
		gsonBuilder.registerTypeAdapterFactory(descs);
		gsonBuilder.registerTypeAdapterFactory(entities);
		gsonBuilder.registerTypeAdapterFactory(cards);
		gsonBuilder.registerTypeAdapterFactory(actors);
		gsonBuilder.registerTypeAdapterFactory(listeners);
		Type mapType = new TypeToken<Map<Attribute, Object>>() {
		}.getType();
		gsonBuilder.registerTypeAdapter(mapType, new AttributeSerializer());
		gsonBuilder.registerTypeAdapter(AttributeMap.class, new AttributeSerializer());
		gsonBuilder.registerTypeAdapter(ClientConnectionConfiguration.class, new ClientConnectionConfigurationSerializer());
		gsonBuilder.registerTypeHierarchyAdapter(Deck.class, new DeckSerializer());
		gsonBuilder.registerTypeAdapter(EntityReference.class, new EntityReferenceSerializer());
		// Descriptions
		gsonBuilder.registerTypeAdapter(SpellDesc.class, new SpellDescSerializer());
		gsonBuilder.registerTypeAdapter(ConditionDesc.class, new ConditionDescSerializer());
		gsonBuilder.registerTypeAdapter(EventTriggerDesc.class, new EventTriggerDescSerializer());
		gsonBuilder.registerTypeAdapter(AuraDesc.class, new AuraDescSerializer());
		gsonBuilder.registerTypeAdapter(ValueProviderDesc.class, new ValueProviderDescSerializer());
		gsonBuilder.registerTypeAdapter(CardCostModifierDesc.class, new CardCostModifierDescSerializer());
		gsonBuilder.registerTypeAdapter(FilterDesc.class, new FilterDescSerializer());
		gsonBuilder.registerTypeAdapter(SourceDesc.class, new SourceDescSerializer());

		// Concrete types
		gsonBuilder.registerTypeHierarchyAdapter(Condition.class, new Condition.Serializer());
		gsonBuilder.registerTypeHierarchyAdapter(EventTrigger.class, new EventTrigger.Serializer());
		gsonBuilder.registerTypeHierarchyAdapter(ValueProvider.class, new ValueProvider.Serializer());
		gsonBuilder.enableComplexMapKeySerialization();
		gson = gsonBuilder.create();
	}

	public static Gson getGson() {
		return gson;
	}

	@Suspendable
	public static String serialize(Object object) {
		return gson.toJson(object);
	}

	@Suspendable
	public static byte[] serializeBytes(Object object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serialize(object, bos);
		return bos.toByteArray();
	}

	@Suspendable
	public static String serializeBase64(Object object) throws IOException {
		return ObjectSerializer.serializeBase64(object);
	}

	@Suspendable
	public static <T> T deserializeBase64(String base64String) {
		return ObjectSerializer.deserializeBase64(base64String);
	}

	@Suspendable
	public static <T> T deserialize(String json, Class<T> classOfT) throws JsonSyntaxException {
		return gson.fromJson(json, classOfT);
	}

	@Suspendable
	public static <T> T deserialize(String json, Type typeOfT) throws JsonSyntaxException {
		return gson.fromJson(json, typeOfT);
	}

	@SuppressWarnings("unchecked")
	@Suspendable
	public static <T> T deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
		return deserialize(new ByteArrayInputStream(buffer));
	}

	@SuppressWarnings("unchecked")
	@Suspendable
	public static <T> T deserialize(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(stream);
		T result = (T) ois.readObject();
		ois.close();
		return result;
	}

	@Suspendable
	public static <T> T deserialize(InputStream stream, Class<? extends T> returnClass) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(stream);
		T result = returnClass.cast(ois.readObject());
		ois.close();
		return result;
	}

	@Suspendable
	public static void serialize(Object obj, OutputStream output) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}

	public static <T> T deserialize(JsonObject body, Class<? extends T> returnClass) {
		return gson.fromJson(body.encode(), returnClass);
	}
}
