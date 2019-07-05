package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.client.models.ClientToServerMessage;
import com.hiddenswitch.spellsource.impl.JsonMessageCodec;

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
