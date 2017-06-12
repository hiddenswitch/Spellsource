package com.hiddenswitch.proto3.net.impl.util;

import com.hiddenswitch.proto3.draft.PrivateDraftState;
import com.hiddenswitch.proto3.draft.PublicDraftState;

import java.io.Serializable;

public class DraftRecord extends MongoRecord implements Serializable {
	private PublicDraftState publicDraftState;
	private PrivateDraftState privateDraftState;


	public PublicDraftState getPublicDraftState() {
		return publicDraftState;
	}

	public void setPublicDraftState(PublicDraftState publicDraftState) {
		this.publicDraftState = publicDraftState;
	}

	public PrivateDraftState getPrivateDraftState() {
		return privateDraftState;
	}

	public void setPrivateDraftState(PrivateDraftState privateDraftState) {
		this.privateDraftState = privateDraftState;
	}
}
