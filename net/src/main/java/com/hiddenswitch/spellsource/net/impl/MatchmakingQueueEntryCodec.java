package com.hiddenswitch.spellsource.net.impl;

public class MatchmakingQueueEntryCodec extends JsonMessageCodec<MatchmakingQueueEntry> {
	@Override
	protected Class<? extends MatchmakingQueueEntry> getMessageClass() {
		return MatchmakingQueueEntry.class;
	}

	@Override
	public String name() {
		return "MatchmakingQueueEntry";
	}
}
