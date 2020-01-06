package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.strands.SuspendableAction1;

import java.io.Serializable;

@FunctionalInterface
public interface SuspendableSerializableAction<T> extends Serializable, SuspendableAction1<T> {
}
