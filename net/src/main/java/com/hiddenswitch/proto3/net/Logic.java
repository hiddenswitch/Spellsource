package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.InitializeUserRequest;
import com.hiddenswitch.proto3.net.models.InitializeUserResponse;

/**
 * Created by bberman on 1/30/17.
 */
public interface Logic {
	@Suspendable
	InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException;
}
