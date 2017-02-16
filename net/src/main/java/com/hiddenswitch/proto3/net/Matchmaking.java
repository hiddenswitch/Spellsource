package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.MatchmakingRequest;
import com.hiddenswitch.proto3.net.models.MatchmakingResponse;
import com.hiddenswitch.proto3.net.models.MatchCancelRequest;
import com.hiddenswitch.proto3.net.models.MatchCancelResponse;
import com.hiddenswitch.proto3.net.models.MatchExpireRequest;
import com.hiddenswitch.proto3.net.models.MatchExpireResponse;

/**
 * Created by bberman on 12/8/16.
 */
public interface Matchmaking {
	MatchmakingResponse matchmakeAndJoin(MatchmakingRequest matchmakingRequest) throws SuspendExecution, InterruptedException;

	MatchExpireResponse expireOrEndMatch(MatchExpireRequest request) throws SuspendExecution, InterruptedException;

	MatchCancelResponse cancel(MatchCancelRequest matchCancelRequest);
}
