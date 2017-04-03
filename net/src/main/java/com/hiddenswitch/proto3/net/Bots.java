package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

/**
 * Created by bberman on 12/8/16.
 */
public interface Bots {
	@Suspendable
	MulliganResponse mulligan(MulliganRequest request);

	@Suspendable
	RequestActionResponse requestAction(RequestActionRequest request);

	@Suspendable
	BotsStartGameResponse startGame(BotsStartGameRequest request) throws SuspendExecution, InterruptedException;

	@Suspendable
	NotifyGameOverResponse notifyGameOver(NotifyGameOverRequest request);
}
