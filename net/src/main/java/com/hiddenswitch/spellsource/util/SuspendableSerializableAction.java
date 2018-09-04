package com.hiddenswitch.spellsource.util;

import com.github.fromage.quasi.strands.SuspendableAction1;

import java.io.Serializable;

@FunctionalInterface
public interface SuspendableSerializableAction<T> extends Serializable, SuspendableAction1<T> {
}
