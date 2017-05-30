package net.demilich.metastone.game.entities;

import java.io.Serializable;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.AttributeMap;

/**
 * An in-game entity.
 * <p>
 * Entities are targetable objects in a match. The player, hero, hero power card, minions, cards in hand,
 * cards in deck, cards in graveyard, secrets and certain kinds of triggers are all entities.
 * <p>
 * Entities are only created, never destroyed. Entities have a {@link EntityLocation}; each location (index, zone and
 * player) can have only one entity occupying it at any time. Destroyed entities go to the {@link Zones#GRAVEYARD} or
 * {@link Zones#REMOVED_FROM_PLAY} zone. {@link EntityZone} lists located in the {@link Player} objects are reponsible
 * for making sure entities are in only one place at a time.
 * <p>
 * Entities all have attributes, which contain their state. As simple maps, entity attributes can be manipulated,
 * copied, etc. Most effects interact with an entity's attributes.
 * <p>
 * Entities are mutable. Use {@link #clone()} to create an "immutable" view of an entity. However, for effects that
 * need copies of entities, typically a {@code getCopy()} method is used, like {@link Card#getCopy()}.
 * <p>
 * This entity class will contain all the game engine information. It is not suitable to show to clients directly,
 * because it may contain information that should be secret from an opponent. For example, {@link net.demilich.metastone.game.spells.trigger.secrets.Secret}
 * entities should have their description or card IDs visible to their opponents.
 */
public abstract class Entity extends CustomCloneable implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int NO_OWNER = -1;

	private String name;
	private AttributeMap attributes = new AttributeMap();
	private int id = IdFactory.UNASSIGNED;
	private int ownerIndex = NO_OWNER;
	protected EntityLocation entityLocation = EntityLocation.NONE;

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
	 * @param attribute The attribute to look up.
	 * @return The value of the attribute.
	 * @see #getAttributeValue(Attribute) to get the value of {@link Integer} attributes.
	 * @see Attribute for a list of attributes.
	 */
	public Object getAttribute(Attribute attribute) {
		return getAttributes().get(attribute);
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	public int getAttributeValue(Attribute attribute) {
		return getAttributes().containsKey(attribute) ? (int) getAttributes().get(attribute) : 0;
	}

	/**
	 * Gets the type of entity this is. These will very nearly match up with the classes, but are primarily used for
	 * filters that e.g. draw a Spell or destroy all Secrets.
	 *
	 * @return An {@link EntityType}
	 */
	public abstract EntityType getEntityType();

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getOwner() {
		return ownerIndex;
	}

	public EntityReference getReference() {
		return EntityReference.pointTo(this);
	}

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

	public boolean isDestroyed() {
		return hasAttribute(Attribute.DESTROYED);
	}

	public void modifyAttribute(Attribute attribute, int value) {
		if (!getAttributes().containsKey(attribute)) {
			setAttribute(attribute, 0);
		}
		setAttribute(attribute, getAttributeValue(attribute) + value);
	}

	public void modifyHpBonus(int value) {
		modifyAttribute(Attribute.HP_BONUS, value);
	}

	public void removeAttribute(Attribute attribute) {
		getAttributes().remove(attribute);
	}

	public void setAttribute(Attribute attribute) {
		if (!GameLogic.immuneToSilence.contains(attribute)) {
			removeAttribute(Attribute.SILENCED);
		}
		getAttributes().put(attribute, 1);
	}

	public void setAttribute(Attribute attribute, int value) {
		if (!GameLogic.immuneToSilence.contains(attribute)) {
			removeAttribute(Attribute.SILENCED);
		}
		getAttributes().put(attribute, value);
	}

	public void setAttribute(Attribute attribute, Object value) {
		if (!GameLogic.immuneToSilence.contains(attribute)) {
			removeAttribute(Attribute.SILENCED);
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

	public void setAttributes(AttributeMap attributes) {
		this.attributes = attributes;
	}

	public boolean hasAllianceEffects() {
		// TODO: look through the card description to see if it uses any network attributes or effects.
		return true;
	}

	public String getUserId() {
		return (String) getAttribute(Attribute.USER_ID);
	}

	protected void setUserId(String userId) {
		setAttribute(Attribute.USER_ID, userId);
	}

	public String getCardInventoryId() {
		return (String) getAttribute(Attribute.CARD_INVENTORY_ID);
	}

	public EntityLocation getEntityLocation() {
		return entityLocation;
	}

	public void setEntityLocation(EntityLocation entityLocation) {
		this.entityLocation = entityLocation;
	}

	public void resetEntityLocations() {
		entityLocation = EntityLocation.NONE;
	}

	@SuppressWarnings("unchecked")
	public void moveOrAddTo(GameContext context, Zones destination) {
		if (getOwner() == -1) {
			throw new RuntimeException("No owner for card.");
		}

		final Player player = context.getPlayer(getOwner());
		if (getEntityLocation().equals(EntityLocation.NONE)) {
			player.getZone(destination).add(this);
		} else if (getEntityLocation().getZone() == destination) {
			// Already in the destination.
			throw new RuntimeException("Already in destination.");
		} else {
			final Zones currentZone = getEntityLocation().getZone();
			player.getZone(currentZone).move(this, player.getZone(destination));
		}
	}

	public Zones getZone() {
		return entityLocation.getZone();
	}
}
