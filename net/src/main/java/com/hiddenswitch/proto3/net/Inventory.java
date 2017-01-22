package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 1/19/17.
 */
public interface Inventory {
	@Suspendable
	OpenCardPackResponse openCardPack(OpenCardPackRequest request);

	@Suspendable
	CreateCollectionResponse createCollection(CreateCollectionRequest request);

	@Suspendable
	AddToCollectionResponse addToCollection(AddToCollectionRequest request);

	@Suspendable
	BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request);

	@Suspendable
	ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request);

	@Suspendable
	GetCollectionResponse getCollection(GetCollectionRequest request);
}
