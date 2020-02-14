package com.hiddenswitch.spellsource.net.impl;

import com.google.common.base.Joiner;
import com.hiddenswitch.spellsource.net.impl.StringEx;
import com.hiddenswitch.spellsource.net.impl.UserId;

import java.util.Comparator;
import java.util.List;

public class ConversationId extends StringEx {

	@Deprecated
	public ConversationId() {
		super();
	}

	@SuppressWarnings("deprecation")
	public ConversationId(List<UserId> userIds) {
		userIds.sort(Comparator.comparing(StringEx::toString));
		id = Joiner.on("-").join(userIds);
	}

	@SuppressWarnings("deprecation")
	public ConversationId(String id) {
		super(id);
	}
}
