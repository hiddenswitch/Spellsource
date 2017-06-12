package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Draft;
import com.hiddenswitch.proto3.net.models.*;

public class DraftImpl extends AbstractService<DraftImpl> implements Draft {
	@Override
	@Suspendable
	public GetDraftResponse get(GetDraftRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public DraftActionResponse doDraftAction(DraftActionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public MatchDraftResponse matchDraft(MatchDraftRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public RetireDraftResponse retireDraftEarly(RetireDraftRequest request) {
		return null;
	}
}
