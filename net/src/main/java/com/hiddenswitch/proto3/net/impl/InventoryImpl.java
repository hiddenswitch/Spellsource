package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.impl.util.CardInstanceRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;

/**
 * Created by bberman on 1/19/17.
 */
public class InventoryImpl extends Service<InventoryImpl> implements Inventory {
	// TODO: Use a proper database for this

	@Override
	public void start() {
		Broker.of(this, Inventory.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public OpenCardPackResponse openCardPack(OpenCardPackRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public CreateCollectionResponse createCollection(CreateCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public AddToCollectionResponse addToCollection(AddToCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request) {
		return null;
	}
}
