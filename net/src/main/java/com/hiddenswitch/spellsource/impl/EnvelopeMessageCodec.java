package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.client.models.Envelope;

public class EnvelopeMessageCodec extends JsonMessageCodec<Envelope> {

	@Override
	protected Class<? extends Envelope> getMessageClass() {
		return Envelope.class;
	}

	@Override
	public String name() {
		return "envelope";
	}
}