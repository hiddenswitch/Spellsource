package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class OpenCardPackResponse implements Serializable {
	private final List<String> createdInventoryIds;

	public List<String> getCreatedInventoryIds() {
		return createdInventoryIds;
	}

	public OpenCardPackResponse(List<String> ids) {
		createdInventoryIds = ids;
	}
}
