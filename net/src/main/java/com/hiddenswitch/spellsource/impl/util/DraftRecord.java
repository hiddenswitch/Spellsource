package com.hiddenswitch.spellsource.impl.util;

import com.hiddenswitch.spellsource.draft.PrivateDraftState;
import com.hiddenswitch.spellsource.draft.PublicDraftState;

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
