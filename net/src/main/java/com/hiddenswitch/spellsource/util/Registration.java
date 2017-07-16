package com.hiddenswitch.spellsource.util;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.List;
import java.util.function.Consumer;

public class Registration {
	private List<MessageConsumer> messageConsumers;

	public List<MessageConsumer> getMessageConsumers() {
		return messageConsumers;
	}

	public void setMessageConsumers(List<MessageConsumer> messageConsumers) {
		this.messageConsumers = messageConsumers;
	}
}
