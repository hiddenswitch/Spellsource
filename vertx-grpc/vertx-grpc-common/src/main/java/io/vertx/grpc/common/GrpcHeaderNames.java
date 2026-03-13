package io.vertx.grpc.common;

import io.netty.util.AsciiString;

/**
 * Header names used by gRPC.
 *
 * @see <a href="https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md#requests">gRPC HTTP/2 requests</a>
 */
public final class GrpcHeaderNames {

  /**
   * Header for specifying the timeout for a gRPC call.
   * Format is an integer followed by a time unit: 'S', 'M', 'H' (seconds, minutes, hours).
   * Example: "10S" for a 10-second timeout.
   */
  public static final AsciiString GRPC_TIMEOUT = AsciiString.cached("grpc-timeout");

  /**
   * Header indicating the compression algorithm used for the message payload.
   * Common values include "gzip", "snappy", and "identity" (no compression).
   */
  public static final AsciiString GRPC_ENCODING = AsciiString.cached("grpc-encoding");

  /**
   * Header specifying which compression algorithms the client or server supports.
   * Multiple algorithms can be specified as a comma-separated list.
   */
  public static final AsciiString GRPC_ACCEPT_ENCODING = AsciiString.cached("grpc-accept-encoding");

  /**
   * Header specifying the type of the message being transmitted.
   */
  public static final AsciiString GRPC_MESSAGE_TYPE = AsciiString.cached("grpc-message-type");

  /**
   * Header containing the status code of a gRPC response.
   * This is used to communicate error conditions from the server to the client.
   * Values are defined in the gRPC protocol specification.
   */
  public static final AsciiString GRPC_STATUS = AsciiString.cached("grpc-status");

  /**
   * Header containing the error message when a request fails.
   * The message is percent-encoded.
   */
  public static final AsciiString GRPC_MESSAGE = AsciiString.cached("grpc-message");

  /**
   * Header containing additional error details when a request fails.
   * The value is base64 encoded.
   */
  public static final AsciiString GRPC_STATUS_DETAILS_BIN = AsciiString.cached("grpc-status-details-bin");
}
