package com.hiddenswitch.spellsource.net.impl.util;

import com.hiddenswitch.spellsource.draft.PrivateDraftState;
import com.hiddenswitch.spellsource.draft.PublicDraftState;

import java.io.Serializable;

/**
 * The state of a player's draft.
 * <p>
 * The {@link #getPublicDraftState()} value is sent to the client. It does not leak information about what cards the
 * player will see.
 * <p>
 * The {@link #getPrivateDraftState()} value is used to implement the actual draft. This includes all the possible
 * options the client will see. It should not be shared with the client.
 */
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
