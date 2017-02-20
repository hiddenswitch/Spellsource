package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.models.DeckDeleteResponse;
import com.hiddenswitch.proto3.net.util.WebResult;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by bberman on 1/22/17.
 */
public interface Server {
	WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException;

	WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException;

	WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException;

	WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException;

	WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException;

	WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext context, String userId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException;

	WebResult<MatchCancelResponse> matchmakingConstructedQueueDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;
}

