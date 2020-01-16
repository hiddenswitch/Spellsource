package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.client.models.EnvelopeResult;
import com.hiddenswitch.spellsource.net.impl.UserId;

public interface MethodContext<T> {
	UserId user();

	EnvelopeResult result();

	T request();
}
