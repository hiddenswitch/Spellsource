package io.vertx.grpc.server.impl;

import java.util.List;

public interface MountPoint<I, O> {

  List<String> paths();

}
