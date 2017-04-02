package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 12/8/16.
 */
public interface Matchmaking {
	MatchmakingResponse matchmakeAndJoin(MatchmakingRequest matchmakingRequest) throws SuspendExecution, InterruptedException;

	CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException;

	MatchExpireResponse expireOrEndMatch(MatchExpireRequest request) throws SuspendExecution, InterruptedException;

	MatchCancelResponse cancel(MatchCancelRequest matchCancelRequest);
}
