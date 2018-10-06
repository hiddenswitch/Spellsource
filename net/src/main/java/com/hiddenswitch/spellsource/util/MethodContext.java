package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.client.models.EnvelopeResult;
import com.hiddenswitch.spellsource.impl.UserId;

public interface MethodContext<T> {
	UserId user();

	EnvelopeResult result();

	T request();
}
