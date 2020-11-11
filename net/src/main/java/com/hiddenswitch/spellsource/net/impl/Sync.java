package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.strands.SuspendableAction1;

import java.util.function.*;

import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.getContextScheduler;

/**
 * Contains utilities for:
 * <ul>
 * <li>Converting {@link Thread}-blocking calls into fiber suspendable calls using {@link io.vertx.ext.sync.Sync#invoke(Supplier)}
 * methods</li>
 * <li>Creating handlers for vertx callbacks that support throwing {@link co.paralleluniverse.fibers.SuspendExecution}
 * (i.e. checked exception that ensures your fiber will be instrumented) using {@link
 * io.vertx.ext.sync.Sync#fiber(SuspendableAction1)}</li>
 * <li>Calling a fiber-synchronous method at a later time using {@link io.vertx.ext.sync.Sync#defer(SuspendableAction1)}.</li>
 * </ul>
 */
public class Sync {


}
