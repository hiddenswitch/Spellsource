package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.draft.PrivateDraftState;
import com.hiddenswitch.proto3.draft.PublicDraftState;

import java.io.Serializable;

/**
 * Created by bberman on 6/11/17.
 */
public class GetDraftRequest implements Serializable {
	public PublicDraftState publicDraftState;
	public PrivateDraftState privateDraftState;
	public String userId;
}
