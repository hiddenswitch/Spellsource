/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.common;

/**
 * Signals a message that is longer than the maximum configured size.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public final class MessageSizeOverflowException extends InvalidMessageException {

    private final long messageSize;

    public MessageSizeOverflowException(long messageSize) {
        this.messageSize = messageSize;
    }

    public long messageSize() {
        return messageSize;
    }
}
