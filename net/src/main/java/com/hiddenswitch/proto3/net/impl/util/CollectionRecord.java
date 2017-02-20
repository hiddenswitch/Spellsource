package com.hiddenswitch.proto3.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hiddenswitch.proto3.net.models.CollectionTypes;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;

/**
 * Created by bberman on 2/6/17.
 */
public class CollectionRecord extends MongoRecord {
	private String userId;
	private CollectionTypes type;
	private boolean trashed;

	/**
	 * Hero class for deck collection records.
	 */
	private HeroClass heroClass;

	/**
	 * Names for alliance and deck collection records.
	 */
	private String name;

	protected CollectionRecord() {
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public CollectionTypes getType() {
		return type;
	}

	public void setType(CollectionTypes type) {
		this.type = type;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CollectionRecord withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public CollectionRecord withType(final CollectionTypes type) {
		this.type = type;
		return this;
	}

	public CollectionRecord withHeroClass(final HeroClass heroClass) {
		this.heroClass = heroClass;
		return this;
	}

	public CollectionRecord withName(final String name) {
		this.name = name;
		return this;
	}

	public CollectionRecord withId(final String id) {
		this._id = id;
		return this;
	}

	public boolean isTrashed() {
		return trashed;
	}

	public void setTrashed(boolean trashed) {
		this.trashed = trashed;
	}

	public CollectionRecord withTrashed(boolean trashed) {
		this.trashed = trashed;
		return this;
	}

	public static CollectionRecord deck(final String userId, final String name, final HeroClass heroClass) {
		return new CollectionRecord()
				.withUserId(userId)
				.withName(name)
				.withHeroClass(heroClass)
				.withType(CollectionTypes.DECK);
	}

	public static CollectionRecord user(final String userId) {
		return new CollectionRecord()
				.withId(userId)
				.withUserId(userId)
				.withType(CollectionTypes.USER);
	}

}


