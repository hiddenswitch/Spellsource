package net.demilich.metastone.game.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.utils.AttributeMap;

public abstract class Entity extends CustomCloneable implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int NO_OWNER = -1;

	private String name;
	private AttributeMap attributes = new AttributeMap();
	private int id = IdFactory.UNASSIGNED;
	private int ownerIndex = NO_OWNER;
	private boolean entityLocationHistory = false;
	protected List<EntityLocation> entityLocations = new ArrayList<>();

	protected Entity() {
		super();
		entityLocations.add(EntityLocation.NONE);
	}

	@Override
	public Entity clone() {
		Entity clone = (Entity) super.clone();
		clone.entityLocations = new ArrayList<>();
		clone.entityLocations.add(EntityLocation.NONE);
		return clone;
	}

	public Object getAttribute(Attribute attribute) {
		return getAttributes().get(attribute);
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	public int getAttributeValue(Attribute attribute) {
		return getAttributes().containsKey(attribute) ? (int) getAttributes().get(attribute) : 0;
	}

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
		return entityLocations.get(entityLocations.size() - 1);
	}

	public void pushEntityLocation(EntityLocation entityLocation) {
		if (entityLocationHistory) {
			this.entityLocations.add(entityLocation);
		} else {
			this.entityLocations.set(entityLocations.size() - 1, entityLocation);
		}
	}

	public void resetEntityLocations() {
		entityLocations.clear();
		entityLocations.add(EntityLocation.NONE);
	}

	public void forgetEntityLocations() {
		EntityLocation last = getEntityLocation();
		entityLocations.clear();
		entityLocations.add(last);
	}

	public List<EntityLocation> getEntityLocations() {
		return entityLocations;
	}
}
