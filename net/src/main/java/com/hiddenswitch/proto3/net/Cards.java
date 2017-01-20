package com.hiddenswitch.proto3.net;

import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 1/19/17.
 */
public interface Cards {
	GetCardResponse getCard(GetCardRequest request);

	QueryCardsResponse queryCards(QueryCardsRequest request);

	InsertCardResponse insertCard(InsertCardRequest request);

	UpdateCardResponse updateCard(UpdateCardRequest request);
}
