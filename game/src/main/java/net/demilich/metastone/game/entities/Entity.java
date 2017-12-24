package net.demilich.metastone.game.entities;

import java.io.Serializable;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.AttributeMap;

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
public abstract class Entity extends CustomCloneable implements Serializable, HasCard {
	private static final long serialVersionUID = 1L;
	/**
	 * The value for the {@link #ownerIndex} when no owner has been assigned.
	 * <p>
	 * All entities should have an owner.
	 */
	public static final int NO_OWNER = -1;

	private String name;
	private AttributeMap attributes = new AttributeMap();
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
		return clone;
	}

	/**
	 * Gets the specified attribute.
	 * <p>
	 * Attributes are {@link Integer}, {@link String}, {@link String[]} or {@link Enum} types.
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
	 * Gets the specified attribute as an {@link Integer} value or fails with an exception.
	 *
	 * @param attribute The {@link Attribute} to look up.
	 * @return The attribute's value or 0 if it isn't set.
	 * @throws ClassCastException if the {@link Attribute} is not an {@link Integer}
	 */
	public int getAttributeValue(Attribute attribute) throws ClassCastException {
		return getAttributes().containsKey(attribute) ? (int) getAttributes().get(attribute) : 0;
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
	 * IDs are set by default to {@link IdFactory#UNASSIGNED}. This means entity IDs are mutable; entity IDs must be
	 * mutable because entities can be cloned with {@link #clone()}. In practice, once an entity's ID is set, it is not
	 * set again.
	 *
	 * @return The entity's ID, or {@link IdFactory#UNASSIGNED} if it is unassigned.
	 * @see IdFactory for the class that generates IDs.
	 * @see GameLogic#summon(int, Minion, Card, int, boolean) for the place where minion IDs are set.
	 * @see GameLogic#assignCardIds(CardList, int) for the place where IDs are set for all the cards that start in the
	 * game.
	 * @see EntityReference for a class used to store the notion of a "target."
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the name of the entity (typically the name of the card that created this entity). Or, overridden by the
	 * {@link Attribute#NAME} attribute set in this entity's attributes.
	 *
	 * @return The name.
	 */
	public String getName() {
		if (hasAttribute(Attribute.NAME)) {
			final String attributeName = (String) getAttribute(Attribute.NAME);
			if (attributeName == null) {
				return name;
			}
			return attributeName;
		}

		return name;
	}

	/**
	 * Gets the owner of this entity, or {@link IdFactory#UNASSIGNED} if it has no owner.
	 * <p>
	 * Owners are mutable because the owner of an entity, especially minions, can change.
	 * <p>
	 * The owner should match the {@link #getEntityLocation()}'s owner. The minion's location should be changed first,
	 * then its owner.
	 *
	 * @return {@link GameContext#PLAYER_1}, {@link GameContext#PLAYER_2}, or {@link IdFactory#UNASSIGNED}.
	 */
	public int getOwner() {
		return ownerIndex;
	}

	/**
	 * Gets an {@link EntityReference} that points to this entity.
	 *
	 * @return An {@link EntityReference}.
	 * @see EntityReference for a better understanding of how references can point to a specific entity or to some
	 * notion of a group of entities (like {@link EntityReference#ENEMY_MINIONS}).
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
		if (value instanceof Integer) {
			return ((int) value) != 0;
		}
		return true;
	}

	/**
	 * Checks if the entity is destroyed. Overridden to take into account entities with hitpoints.
	 *
	 * @return {@code true} if it is destroyed.
	 * @see {@link Actor#isDestroyed()} for a more complete implementation.
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
	 * Sets an attribute. This will remove silencing when it is called. Since boolean values are not stored in
	 * attributes, attributes that are "boolean" are just set to 1. Setting the value to 0 is not equivalent to not
	 * having the attribute.
	 *
	 * @param attribute The attribute to set.
	 */
	public void setAttribute(Attribute attribute) {
		clearSilence(attribute);
		getAttributes().put(attribute, 1);
	}

	private void clearSilence(Attribute attribute) {
		if (!GameLogic.immuneToSilence.contains(attribute)) {
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
	 * Overwrites all the entity's attributes with the specified {@link AttributeMap}. Does not copy the attribute map,
	 * so be careful with multiple entities references the same map.
	 *
	 * @param attributes The {@link AttributeMap}
	 */
	public void setAttributes(AttributeMap attributes) {
		this.attributes = attributes;
	}

	/**
	 * Entities with persistent effects need their events to be processed differently in order to record those
	 * persistent values to a database.
	 *
	 * @return {@code true} if the entity needs to have its persistent effects persisted.
	 * @see Attribute#LAST_MINION_DESTROYED_CARD_ID for an example of a persistent attribute that needs to be stored
	 * between matches.
	 */
	public boolean hasPersistentEffects() {
		// TODO: look through the card description to see if it uses any network attributes or effects.
		return true;
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
	 * Resets the entity's location by setting it to {@link EntityLocation#UNASSIGNED}. Typically only called by an
	 * {@link EntityZone}.
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
		if (getOwner() == Entity.NO_OWNER) {
			throw new ArrayStoreException("No owner for entity.");
		}

		final Player player = context.getPlayer(getOwner());
		if (getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			player.getZone(destination).add(this);
		} else if (getEntityLocation().getZone() == destination) {
			// Already in the destination.
			throw new ArrayStoreException("Already in destination.");
		} else {
			final Zones currentZone = getEntityLocation().getZone();
			player.getZone(currentZone).move(this, player.getZone(destination));
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
	 * Limits the number of transformations to follow to 16.
	 *
	 * @param context A {@link GameContext} to perform lookups in.
	 * @return This entity if no transform is found, otherwise follows the chain of resolved entities until no
	 * transformed entity is found.
	 */
	public Entity transformResolved(GameContext context) {
		if (!getAttributes().containsKey(Attribute.TRANSFORM_REFERENCE)
				|| getAttributes().get(Attribute.TRANSFORM_REFERENCE) == null) {
			return this;
		}

		EntityReference reference = (EntityReference) getAttributes().get(Attribute.TRANSFORM_REFERENCE);
		Entity entity = context.getEntities().filter(e -> e.getId() == reference.getId()).findFirst().orElseThrow(RuntimeException::new);
		int i = 16;
		while (!entity.transformResolved(context).equals(entity)) {
			entity = entity.transformResolved(context);
			i--;
			if (i == -1) {
				throw new RuntimeException("Cycle likely in entity transformation chain.");
			}
		}

		return entity;
	}
}
