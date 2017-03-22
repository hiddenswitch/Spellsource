package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 1/19/17.
 */
public interface Inventory {
	String INVENTORY = "inventory.cards";
	String COLLECTIONS = "inventory.collections";

	OpenCardPackResponse openCardPack(OpenCardPackRequest request) throws SuspendExecution, InterruptedException;

	CreateCollectionResponse createCollection(CreateCollectionRequest request) throws SuspendExecution, InterruptedException;

	AddToCollectionResponse addToCollection(AddToCollectionRequest request) throws SuspendExecution, InterruptedException;

	RemoveFromCollectionResponse removeFromCollection(RemoveFromCollectionRequest request) throws SuspendExecution, InterruptedException;

	DonateToCollectionResponse donateToCollection(DonateToCollectionRequest request) throws SuspendExecution, InterruptedException;

	BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request) throws SuspendExecution, InterruptedException;

	@Suspendable
	ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request);

	GetCollectionResponse getCollection(GetCollectionRequest request) throws SuspendExecution, InterruptedException;

	TrashCollectionResponse trashCollection(TrashCollectionRequest request) throws SuspendExecution, InterruptedException;

	SetCollectionResponse setCollection(SetCollectionRequest setCollectionRequest) throws SuspendExecution, InterruptedException;
}
