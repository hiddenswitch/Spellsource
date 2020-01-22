/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2013-2014, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.strands.channels;

/**
 * A message-passing channel.
 * Implementations of this interface are encouraged (though not required) to implement {@link StandardChannel}
 * @param Message the type of messages that can be sent to this channel.
 * @author pron
 */
public interface Channel<Message> extends SendPort<Message>, ReceivePort<Message> {
}
