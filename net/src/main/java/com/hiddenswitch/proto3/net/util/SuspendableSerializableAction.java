package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.strands.SuspendableAction1;

import java.io.Serializable;

@FunctionalInterface
public interface SuspendableSerializableAction<T> extends Serializable, SuspendableAction1{
}
