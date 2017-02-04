package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.PutFactResponse;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bberman on 2/2/17.
 */
public interface Facts {
	@Suspendable
	UpdateFactResponse updateFactResponse(UpdateFactRequest request);
}
