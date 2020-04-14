package net.demilich.metastone.game.entities;

import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.cards.dynamicdescription.DynamicDescriptionDesc;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.*;
import net.demilich.metastone.game.targeting.*;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An in-game entity.
 * <p>
 * Entities are targetable objects in a match. The player, hero, hero power card, minions, cards in hand, cards in deck,
 * cards in graveyard, secrets and certain kinds of triggers are all entities.
 * <p>
 * Entities are only created, never destroyed. Entities have a {@link EntityLocation}; each location (index, zone and
 * player) can have only one entity occupying it at any time. Destroyed entities go to the {@link Zones#GRAVEYARD} or
 * {@link Zones#REMOVED_FROM_PLAY} zone. {@link EntityZone} lists located in the {@link Player} objects are reponsible
 * for making sure entities are in only one place at a time.
 * <p>
 * Entities all have attributes, which contain their state. As simple maps, entity attributes can be manipulated,
 * copied, etc. Most effects interact with an entity's attributes.
 * <p>
 * Entities are mutable. Use {@link #clone()} to create an "immutable" view of an entity. However, for effects that need
 * copies of entities, typically a {@code getCopy()} method is used, like {@link Card#getCopy()}.
 * <p>
 * This entity class will contain all the game engine information. It is not suitable to show to clients directly,
 * because it may contain information that should be secret from an opponent. For example, {@link
 * net.demilich.metastone.game.spells.trigger.secrets.Secret} entities should have their description or card IDs visible
 * to their opponents.
 */
public abstract class Entity extends CustomCloneable implements Serializable, HasCard, Comparable<Entity> {
	private static Pattern BONUS_DAMAGE_IN_DESCRIPTION = Pattern.compile("\\$(\\d+)");
	private static Pattern BONUS_HEALING_IN_DESCRIPTION = Pattern.compile("#(\\d+)");
	private static final long serialVersionUID = 1L;
	/**
	 * The value for the {@link #ownerIndex} when no owner has been assigned.
	 * <p>
	 * All entities should have an owner.
	 */
	public static final int NO_OWNER = -1;

	protected String name;
	protected AttributeMap attributes;
	/**
	 * @see #getId()
	 */
	private int id = IdFactory.UNASSIGNED;
	/**
	 * @see #getOwner()
	 */
	private int ownerIndex = NO_OWNER;
	/**
	 * @see #getEntityLocation()
	 */
	protected EntityLocation entityLocation = EntityLocation.UNASSIGNED;

	protected Entity() {
		super();
		attributes = new AttributeMap();
	}

	public static boolean hasEntityType(EntityType thisEntity, EntityType other) {
		if (thisEntity == EntityType.ANY || other == EntityType.ANY) {
			return true;
		}

		if (thisEntity == EntityType.ACTOR) {
			return other == EntityType.HERO || other == EntityType.MINION || other == EntityType.WEAPON;
		}

		if (other == EntityType.ACTOR) {
			return thisEntity == EntityType.HERO || thisEntity == EntityType.MINION || thisEntity == EntityType.WEAPON;
		}

		return Objects.equals(thisEntity, other);
	}

	/**
	 * Clone an entity, including its ID and location.
	 * <p>
	 * Use this method for emulating an "immutable" view on an entity. This kind of cloning is not suitable for most
	 * gameplay situations, because using the clone will cause two entities with identical IDs and locations to exist.
	 * Instead, a subclass will provide a {@code getCopy()} method that is more helpful for gameplay.
	 *
	 * @return An exact clone.
	 */
	@Override
	public Entity clone() {
		Entity clone = (Entity) super.clone();
		// The attributes need to be cloned
		if (attributes != null) {
			clone.attributes = attributes.clone();
		}
		return clone;
	}

	/**
	 * Gets the specified attribute.
	 * <p>
	 * Attributes are {@link Integer}, {@link String}, {@link String} array or {@link Enum} types.
	 *
	 * @param attribute The attribute to look up.
	 * @return The value of the attribute.
	 * @see #getAttributeValue(Attribute) to get the value of {@link Integer} attributes.
	 * @see Attribute for a list of attributes.
	 */
	public Object getAttribute(Attribute attribute) {
		return getAttributes().get(attribute);
	}

	/**
	 * Gets the complete attribute map reference (not a copy). This can be mutated like a normal {@link java.util.Map}.
	 *
	 * @return The {@link AttributeMap}.
	 */
	public AttributeMap getAttributes() {
		return attributes;
	}

	/**
	 * Gets the specified attribute as an {@link Integer} value or {@code 0} if the specified attribute is of the wrong
	 * type or is not found.
	 *
	 * @param attribute The {@link Attribute} to look up.
	 * @return The attribute's value or 0 if it isn't set.
	 */
	public int getAttributeValue(Attribute attribute) {
		return (int) getAttributes().getOrDefault(attribute, 0);
	}

	/**
	 * Gets the specified attribute as an {@link Integer} value, defaulting to the specified value if the value is not an
	 * integer.
	 *
	 * @param attribute The {@link Attribute} to look up.
	 * @return The attribute's value or 0 if it isn't set.
	 */
	public int getAttributeValue(Attribute attribute, int defaultValue) {
		return (int) getAttributes().getOrDefault(attribute, defaultValue);
	}

	/**
	 * Gets the type of entity this is. These will very nearly match up with the classes, but are primarily used for
	 * filters that e.g. draw a Spell or destroy all Secrets.
	 *
	 * @return An {@link EntityType}
	 */
	public abstract EntityType getEntityType();

	/**
	 * The entity's ID in the match.
	 * <p>
	 * IDs are set by default to {@link IdFactoryImpl#UNASSIGNED}. This means entity IDs are mutable; entity IDs must be
	 * mutable because entities can be cloned with {@link #clone()}. In practice, once an entity's ID is set, it is not
	 * set again.
	 *
	 * @return The entity's ID, or {@link IdFactoryImpl#UNASSIGNED} if it is unassigned.
	 * @see IdFactoryImpl for the class that generates IDs.
	 * @see GameLogic#summon(int, Minion, Entity, int, boolean) for the place where minion IDs are set.
	 * @see GameLogic#assignEntityIds(Iterable, int) for the place where IDs are set for all the cards that start in the
	 * game.
	 * @see EntityReference for a class used to store the notion of a "target."
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the name of the entity (typically the name of the card that created this entity). Or, overridden by the {@link
	 * Attribute#NAME} attribute set in this entity's attributes.
	 *
	 * @return The name.
	 */
	public String getName() {
		if ((getEntityType() == EntityType.CARD
				&& getSourceCard() != null
				&& getSourceCard().getCardSet() == "SPELLSOURCE")
				|| getEntityType() == EntityType.PLAYER) {
			return (String) getAttributes().getOrDefault(Attribute.NAME, name);
		} else {
			return name;
		}
	}

	/**
	 * Gets the owner of this entity, or {@link IdFactoryImpl#UNASSIGNED} if it has no owner.
	 * <p>
	 * Owners are mutable because the owner of an entity, especially minions, can change.
	 * <p>
	 * The owner should match the {@link #getEntityLocation()}'s owner. The minion's location should be changed first,
	 * then its owner.
	 *
	 * @return {@link GameContext#PLAYER_1}, {@link GameContext#PLAYER_2}, or {@link IdFactoryImpl#UNASSIGNED}.
	 */
	public int getOwner() {
		return ownerIndex;
	}

	/**
	 * Gets an {@link EntityReference} that points to this entity.
	 *
	 * @return An {@link EntityReference}.
	 * @see EntityReference for a better understanding of how references can point to a specific entity or to some notion
	 * of a group of entities (like {@link EntityReference#ENEMY_MINIONS}).
	 */
	public EntityReference getReference() {
		return EntityReference.pointTo(this);
	}

	/**
	 * Checks if the {@link Entity} has the specified {@link Attribute}.
	 *
	 * @param attribute The {@link Attribute}.
	 * @return {@code true} if it has the attribute.
	 */
	public boolean hasAttribute(Attribute attribute) {
		Object value = getAttributes().get(attribute);
		if (value == null) {
			return false;
		}

		if (value instanceof Boolean) {
			return (boolean) value;
		}

		if (value instanceof Integer && !Attribute.getStoresTurnNumberAttributes().contains(attribute)) {
			return ((int) value) != 0;
		}

		return true;
	}

	public int getMaxNumberOfAttacks() {
		if (hasAttribute(Attribute.MEGA_WINDFURY)) {
			return GameLogic.MEGA_WINDFURY_ATTACKS;
		} else if (hasAttribute(Attribute.WINDFURY) || hasAttribute(Attribute.AURA_WINDFURY)) {
			return GameLogic.WINDFURY_ATTACKS;
		}
		return 1;
	}

	/**
	 * Checks if the entity is destroyed. Overridden to take into account entities with hitpoints.
	 *
	 * @return {@code true} if it is destroyed.
	 * @see Actor#isDestroyed() for a more complete implementation.
	 */
	public boolean isDestroyed() {
		return hasAttribute(Attribute.DESTROYED);
	}

	/**
	 * Increments or decrements the specified {@link Integer} {@link Attribute} by the value given.
	 *
	 * @param attribute The attribute.
	 * @param value     The amount to increment or decrement the attribute by.
	 */
	public void modifyAttribute(Attribute attribute, int value) {
		if (!getAttributes().containsKey(attribute)) {
			setAttribute(attribute, 0);
		}
		setAttribute(attribute, getAttributeValue(attribute) + value);
	}

	/**
	 * Modifies the HP bonus for the given entity.
	 *
	 * @param value The amount to increment or decrement the HP bonus by.
	 */
	public void modifyHpBonus(int value) {
		modifyAttribute(Attribute.HP_BONUS, value);
	}

	/**
	 * Sets an attribute. This will remove silencing when it is called. Since boolean values are not stored in attributes,
	 * attributes that are "boolean" are just set to 1. Setting the value to 0 is not equivalent to not having the
	 * attribute.
	 *
	 * @param attribute The attribute to set.
	 */
	public void setAttribute(Attribute attribute) {
		clearSilence(attribute);
		getAttributes().put(attribute, true);
	}

	private void clearSilence(Attribute attribute) {
		if (!GameLogic.IMMUNE_TO_SILENCE.contains(attribute)) {
			getAttributes().remove(Attribute.SILENCED);
		}
	}

	/**
	 * Sets an attribute to a specific integer value. This will remove silencing when it is called. It does not enforce
	 * that the attribute is something that only accepts {@link Integer} values.
	 *
	 * @param attribute The attribute to set.
	 * @param value     The value.
	 */
	public void setAttribute(Attribute attribute, int value) {
		clearSilence(attribute);
		getAttributes().put(attribute, value);
	}

	/**
	 * Sets an attribute to a generic object, like a string. This clears silencing when it is called.
	 *
	 * @param attribute The attribute to set.
	 * @param value     Its new object value.
	 */
	public void setAttribute(Attribute attribute, Object value) {
		clearSilence(attribute);
		if (value == null) {
			return;
		}
		getAttributes().put(attribute, value);
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOwner(int ownerIndex) {
		this.ownerIndex = ownerIndex;
	}

	/**
	 * Entities with persistent effects need their events to be processed differently in order to record those persistent
	 * values to a database.
	 *
	 * @return {@code true} if the entity needs to have its persistent effects persisted.
	 * @see Attribute#LAST_MINION_DESTROYED_CARD_ID for an example of a persistent attribute that needs to be stored
	 * between matches.
	 */
	public boolean hasPersistentEffects() {
		// TODO: look through the card description to see if it uses any network attributes or effects.
		if (getSourceCard() != null) {
			return getSourceCard().hasPersistentEffects();
		}
		return false;
	}

	/**
	 * Gets the user ID of the owner of this card.
	 *
	 * @return The user ID.
	 */
	public String getUserId() {
		return (String) getAttribute(Attribute.USER_ID);
	}

	protected void setUserId(String userId) {
		setAttribute(Attribute.USER_ID, userId);
	}

	/**
	 * Gets the card's inventory ID (unique instance of the card).
	 *
	 * @return The card inventory ID.
	 */
	public String getCardInventoryId() {
		return (String) getAttribute(Attribute.CARD_INVENTORY_ID);
	}

	/**
	 * Gets the {@link EntityLocation} of the entity, which includes its {@link EntityLocation#zone}, {@link
	 * EntityLocation#player} and {@link EntityLocation#index}.
	 * <p>
	 *
	 * @return The entity's location in the match encoded as a {@link EntityLocation}, or {@link
	 * EntityLocation#UNASSIGNED} if the entity has not yet been assigned a location or placed into an {@link
	 * EntityZone}.
	 * @see EntityLocation for a complete description of how to use {@link EntityLocation} objects.
	 */
	public EntityLocation getEntityLocation() {
		return entityLocation;
	}

	/**
	 * Should not be called.
	 * <p>
	 * Sets the entity location. Typically only called by an {@link EntityZone}.
	 *
	 * @param entityLocation The new location of the entity.
	 */
	public void setEntityLocation(EntityLocation entityLocation) {
		this.entityLocation = entityLocation;
	}

	/**
	 * Should not be called.
	 * <p>
	 * Resets the entity's location by setting it to {@link EntityLocation#UNASSIGNED}. Typically only called by an {@link
	 * EntityZone}.
	 */
	public void resetEntityLocations() {
		entityLocation = EntityLocation.UNASSIGNED;
	}

	/**
	 * Moves this entity to a new zone ({@link Zones}) belonging to the {@link Player} indexed by {@link #getOwner()}.
	 *
	 * @param context     The game context this entity is in.
	 * @param destination The destination zone belonging to the player to move to.
	 * @throws ArrayStoreException if the entity has no owner; or if the entity already exists in the destination.
	 */
	@SuppressWarnings("unchecked")
	public void moveOrAddTo(GameContext context, Zones destination) throws ArrayStoreException {
		if (getId() == IdFactory.UNASSIGNED) {
			throw new RuntimeException();
		}
		moveOrAddTo(context, destination, context.getPlayer(getOwner()).getZone(destination).size());
	}

	/**
	 * Moves this entity to a new zone ({@link Zones}) belonging to the {@link Player} indexed by {@link #getOwner()}.
	 *
	 * @param context     The game context this entity is in.
	 * @param destination The destination zone belonging to the player to move to.
	 * @param index       The location in the zone to move or add to
	 * @throws ArrayStoreException if the entity has no owner; or if the entity already exists in the destination.
	 */
	@SuppressWarnings("unchecked")
	public void moveOrAddTo(GameContext context, Zones destination, int index) throws ArrayStoreException {
		if (getOwner() == Entity.NO_OWNER) {
			throw new ArrayStoreException("No owner for entity.");
		}

		Player player = context.getPlayer(getOwner());
		if (getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			player.getZone(destination).add(index, this);
		} else if (getEntityLocation().getZone() == destination) {
			// Already in the destination.
			throw new ArrayStoreException("Already in destination.");
		} else {
			Zones currentZone = getEntityLocation().getZone();
			player.getZone(currentZone).move(getEntityLocation().getIndex(), player.getZone(destination), index);
		}
	}

	/**
	 * Gets the current zone the entity is located in.
	 *
	 * @return The {@link Zones} zone.
	 */
	public Zones getZone() {
		return entityLocation.getZone();
	}

	/**
	 * Follows {@link Attribute#TRANSFORM_REFERENCE} until the resolved entity is found.
	 * <p>
	 * Limits the number of transformations to follow to 14.
	 *
	 * @param context A {@link GameContext} to perform lookups in.
	 * @return This entity if no transform is found, otherwise follows the chain of resolved entities until no transformed
	 * entity is found.
	 */
	public Entity transformResolved(GameContext context) {
		return transformResolved(context, 14);
	}

	protected Entity transformResolved(GameContext context, int depth) {
		if (depth < 0) {
			throw new TransformCycleException(this);
		}
		if (!getAttributes().containsKey(Attribute.TRANSFORM_REFERENCE)
				|| getAttributes().get(Attribute.TRANSFORM_REFERENCE) == null) {
			return this;
		}

		EntityReference reference = (EntityReference) getAttributes().get(Attribute.TRANSFORM_REFERENCE);
		Entity entity = context.tryFind(reference, false);
		if (entity == null) {
			throw new TargetNotFoundException("transform ref", reference);
		}
		entity = entity.transformResolved(context, depth - 1);

		return entity;
	}

	/**
	 * Gets the possibly modified description of the entity to render to the end user.
	 *
	 * @return The {@link #getSourceCard()}'s {@link Card#getDescription()} field, or the value specified in {@link
	 * Attribute#DESCRIPTION}.
	 */
	public String getDescription() {
		return (hasAttribute(Attribute.DESCRIPTION) && getAttribute(Attribute.DESCRIPTION) != null) ?
				(String) getAttribute(Attribute.DESCRIPTION)
				: (getSourceCard() != null ? getSourceCard().getDescription() : "");
	}

	/**
	 * Sets the description by setting the {@link Attribute#DESCRIPTION} attribute.
	 *
	 * @param description
	 * @return
	 */
	public Entity setDescription(String description) {
		getAttributes().put(Attribute.DESCRIPTION, description);
		return this;
	}

	public abstract Entity getCopy();

	/**
	 * Gets a reference to the entity that this entity was potentially copied from.
	 *
	 * @return {@code null} if this entity was not copied from another entity in the game, or an {@link EntityReference}
	 * of another entity.
	 */
	public EntityReference getCopySource() {
		return (EntityReference) getAttributes().get(Attribute.COPIED_FROM);
	}

	/**
	 * Gets a list of triggers that are active as soon as the game starts.
	 *
	 * @return The entity's defined game triggers
	 * @see GameLogic#processGameTriggers(Player, Entity) for the place to activate these triggers.
	 */
	public EnchantmentDesc[] getGameTriggers() {
		return (EnchantmentDesc[]) getAttributes().getOrDefault(Attribute.GAME_TRIGGERS, new EnchantmentDesc[0]);
	}

	public DynamicDescriptionDesc[] getDynamicDescription() {
		if (getAttributes().containsKey(Attribute.DYNAMIC_DESCRIPTION)) {
			return (DynamicDescriptionDesc[]) getAttribute(Attribute.DYNAMIC_DESCRIPTION);
		}
		return getSourceCard() != null ? getSourceCard().getDesc().getDynamicDescription() : null;
	}

	public String[] evaluateDescriptions(GameContext context, Player player) {
		DynamicDescriptionDesc[] dynamicDescriptionDescs = getDynamicDescription();
		String[] strings = new String[dynamicDescriptionDescs.length];

		for (int i = 0; i < dynamicDescriptionDescs.length; i++) {
			strings[i] = dynamicDescriptionDescs[i].create().resolveFinalString(context, player, this);
		}
		return strings;
	}

	@Override
	public int compareTo(@NotNull Entity o) {
		return Integer.compare(this.getId(), o.getId());
	}

	@NotNull
	public String getRace() {
		return (String) getAttributes().getOrDefault(Attribute.RACE, Race.NONE);
	}

	/**
	 * Indicates that the entity is in play by being in an in-play zone.
	 *
	 * @return {@code} true if the entity is visible to both players
	 */
	public boolean isInPlay() {
		switch (getZone()) {
			case QUEST:
			case SECRET:
			case HERO:
			case HERO_POWER:
			case BATTLEFIELD:
			case WEAPON:
			case PLAYER:
				return true;
		}

		return false;
	}

	/**
	 * The entity's index in its zone.
	 *
	 * @return {@link EntityLocation#UNASSIGNED} 's index if it isn't yet in a zone (typically {@code -1}), or the index
	 * in the {@link #getZone()} this entity is in.
	 */
	public int getIndex() {
		return getEntityLocation().getIndex();
	}

	/**
	 * Is this entity removed peacefully?
	 *
	 * @return {@code true} if it's in the graveyard and didn't die violently, otherwise false.
	 */
	public boolean isRemovedPeacefully() {
		return getZone() == Zones.GRAVEYARD && !hasAttribute(Attribute.DIED_ON_TURN);
	}

	/**
	 * Gets an entity's description applying its {@link CardDesc#getDynamicDescription()} fields and parsing spell damage
	 * and health restoration.
	 *
	 * @param context The context
	 * @param player  The player whose POV this description should be evaluated
	 * @return The dynamic description if this entity is a card, otherwise the {@link #getDescription()}.
	 */
	public String getDescription(GameContext context, Player player) {
		String description = getDescription();
		Card card = getSourceCard();

		if (getDynamicDescription() != null
				&& getDynamicDescription().length > 0
				&& (isInPlay() || getZone() == Zones.HAND || getZone() == Zones.DECK)) {
			// First parse dynamic descriptions
			if (description.contains("[") && getDynamicDescription() != null) {
				int i = 0;
				String[] descriptions = evaluateDescriptions(context, player);
				while (description.contains("[")) {
					int start = description.indexOf("[");
					int end = description.indexOf("]");
					description = description.substring(0, start) + descriptions[i] + description.substring(end + 1, description.length());
					i++;
				}
			}
		} else {
			description = description.replace("[", "").replace("]", "");
		}

		// Handle spell damage
		if (card.isSpell() || card.isHeroPower()) {
			// Find the $ damages
			Matcher matcher = BONUS_DAMAGE_IN_DESCRIPTION.matcher(description);
			StringBuffer newDescription = new StringBuffer();

			boolean matchedAtLeastOnce = false;
			while (matcher.find()) {
				// Skip the dollar sign in the beginning
				int damage = Integer.parseInt(matcher.group(1));
				int modifiedDamage;
				if (card.getId() != GameLogic.UNASSIGNED) {
					ValueProviderDesc desc = new ValueProviderDesc();
					desc.put(ValueProviderArg.VALUE, damage);
					desc.put(ValueProviderArg.CLASS, card.isSpell()
							? SpellDamageValueProvider.class
							: HeroPowerDamageValueProvider.class);
					ValueProvider provider = desc.create();
					modifiedDamage = provider.getValue(context, player, player.getHero(), card);
				} else {
					modifiedDamage = damage;
				}
				modifyDescription(matcher, newDescription, damage, modifiedDamage);
				matchedAtLeastOnce = true;
			}
			if (matchedAtLeastOnce) {
				matcher.appendTail(newDescription);
				description = newDescription.toString();
			}
		}

		// Healing
		Matcher matcher = BONUS_HEALING_IN_DESCRIPTION.matcher(description);
		StringBuffer newDescription = new StringBuffer();

		boolean matchedAtLeastOnce = false;
		while (matcher.find()) {
			// Skip the # in the beginning
			int healing = Integer.parseInt(matcher.group(1));
			int modifiedHealing = healing;
			if (card.getId() != GameLogic.UNASSIGNED) {
				modifiedHealing = context.getLogic().getModifiedHealing(player, healing, this, true);
			}
			modifyDescription(matcher, newDescription, healing, modifiedHealing);
			matchedAtLeastOnce = true;
		}
		if (matchedAtLeastOnce) {
			matcher.appendTail(newDescription);
			description = newDescription.toString();
		}

		return description;
	}

	private void modifyDescription(Matcher matcher, StringBuffer newDescription, int originalValue, int newValue) {
		if (newValue != originalValue) {
			matcher.appendReplacement(newDescription, String.format("*%d*", newValue));
		} else {
			matcher.appendReplacement(newDescription, Integer.toString(newValue));
		}
	}

	/**
	 * Indicates whether the minion died a natural death (not removed peacefully, not removed by spells, not a permanent)
	 * on the battlefield.
	 *
	 * @return {@code true} if this minion died a natural death on the battlefield, is not a permanent, is in the
	 * graveyard and is definitely a minion.
	 */
	public boolean diedOnBattlefield() {
		return getEntityType() == EntityType.MINION
				&& getZone() == Zones.GRAVEYARD
				&& hasAttribute(Attribute.DIED_ON_TURN)
				&& !hasAttribute(Attribute.PERMANENT);
	}
}
