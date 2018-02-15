package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by bberman on 2/6/17.
 */
public class MongoRecord implements Serializable {
	public static final String ID = "_id";
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty(value = ID)
	@SerializedName(ID)
	protected String _id;

	@JsonIgnore
	public String getId() {
		return _id;
	}

	protected MongoRecord() {
	}

	public MongoRecord(String id) {
		this._id = id;
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof MongoRecord)) {
			return false;
		}

		MongoRecord rhs = (MongoRecord) other;
		return this._id.equals(rhs._id);
	}
}
