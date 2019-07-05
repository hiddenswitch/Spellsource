package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;

public class ServerToClientMessageCodec extends JsonMessageCodec<ServerToClientMessage> {
	@Override
	protected Class<? extends ServerToClientMessage> getMessageClass() {
		return ServerToClientMessage.class;
	}

	@Override
	public String name() {
		return "serverToClientMessage";
	}
}
