package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.draft.PrivateDraftState;
import com.hiddenswitch.spellsource.draft.PublicDraftState;

import java.io.Serializable;

/**
 * Created by bberman on 6/11/17.
 */
public class GetDraftResponse implements Serializable {
	public String userId;
	public PublicDraftState publicDraftState;
	public PrivateDraftState privateDraftState;
}
