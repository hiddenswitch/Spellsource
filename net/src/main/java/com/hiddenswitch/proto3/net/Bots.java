package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.MulliganRequest;
import com.hiddenswitch.proto3.net.models.MulliganResponse;
import com.hiddenswitch.proto3.net.models.RequestActionRequest;
import com.hiddenswitch.proto3.net.models.RequestActionResponse;

/**
 * Created by bberman on 12/8/16.
 */
public interface Bots {
	@Suspendable
	MulliganResponse mulligan(MulliganRequest request);

	@Suspendable
	RequestActionResponse requestAction(RequestActionRequest request);
}
