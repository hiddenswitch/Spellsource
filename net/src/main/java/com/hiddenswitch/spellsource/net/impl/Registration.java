package com.hiddenswitch.spellsource.net.impl;

import io.vertx.core.eventbus.MessageConsumer;

import java.util.List;

public class Registration {
	private List<MessageConsumer> messageConsumers;

	public List<MessageConsumer> getMessageConsumers() {
		return messageConsumers;
	}

	public void setMessageConsumers(List<MessageConsumer> messageConsumers) {
		this.messageConsumers = messageConsumers;
	}
}
