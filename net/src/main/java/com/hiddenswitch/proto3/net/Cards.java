package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 1/19/17.
 */
public interface Cards {
	@Suspendable
	GetCardResponse getCard(GetCardRequest request);

	@Suspendable
	QueryCardsResponse queryCards(QueryCardsRequest request) throws SuspendExecution;

	@Suspendable
	InsertCardResponse insertCard(InsertCardRequest request);

	@Suspendable
	UpdateCardResponse updateCard(UpdateCardRequest request);
}
