package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.UpdateFactRequest;
import com.hiddenswitch.proto3.net.models.UpdateFactResponse;

/**
 * Created by bberman on 2/2/17.
 */
public interface Facts {
	@Suspendable
	UpdateFactResponse updateFactResponse(UpdateFactRequest request);
}
