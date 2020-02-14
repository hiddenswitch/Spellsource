package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.client.models.ClientToServerMessage;

public class ClientToServerMessageCodec extends JsonMessageCodec<ClientToServerMessage> {
	@Override
	protected Class<? extends ClientToServerMessage> getMessageClass() {
		return ClientToServerMessage.class;
	}

	@Override
	public String name() {
		return "clientToServerMessage";
	}
}
