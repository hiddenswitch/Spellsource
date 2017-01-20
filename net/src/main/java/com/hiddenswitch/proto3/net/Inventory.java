package com.hiddenswitch.proto3.net;

import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 1/19/17.
 */
public interface Inventory {
	OpenCardPackResponse openCardPack(OpenCardPackRequest request);

	CreateCollectionResponse createCollection(CreateCollectionRequest request);

	AddToCollectionResponse addToCollection(AddToCollectionRequest request);

	BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request);

	ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request);
}
