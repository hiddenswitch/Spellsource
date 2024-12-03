package io.grpc.override;

import io.grpc.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * A {@link io.grpc.Context.Storage} implementation that uses Vert.x local context data maps when running on a duplicated context.
 * Otherwise, it falls back to thread-local storage.
 */
public class ContextStorageOverride extends Context.Storage {

  private static final Logger LOG = LoggerFactory.getLogger(ContextStorageOverride.class);

  private static final Object CONTEXT_KEY = new Object();
  private static final ThreadLocal<Context> fallback = new ThreadLocal<>();

  public ContextStorageOverride() {
    // Do not remove, empty constructor required by gRPC
  }

  @Override
  public Context doAttach(Context toAttach) {
    ContextInternal vertxContext = vertxContext();
    Context toRestoreLater;
    if (vertxContext != null) {
      toRestoreLater = (Context) vertxContext.localContextData().put(CONTEXT_KEY, toAttach);
    } else {
      toRestoreLater = fallback.get();
      fallback.set(toAttach);
    }
    return rootIfNull(toRestoreLater);
  }

  @Override
  public void detach(Context toDetach, Context toRestore) {
    ContextInternal vertxContext = vertxContext();
    Context current;
    if (vertxContext != null) {
      ConcurrentMap<Object, Object> dataMap = vertxContext.localContextData();
      if (toRestore == Context.ROOT) {
        current = (Context) dataMap.remove(CONTEXT_KEY);
      } else {
        current = (Context) dataMap.put(CONTEXT_KEY, toRestore);
      }
    } else {
      current = fallback.get();
      fallback.remove();
      fallback.set(toRestore == Context.ROOT ? null : toRestore);
    }
    if (rootIfNull(current) != toDetach) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Context was not attached when detaching", new Exception("Stack trace"));
      }
    }
  }

  @Override
  public Context current() {
    ContextInternal vertxContext = vertxContext();
    return rootIfNull(vertxContext != null ? vertxContext.getLocal(CONTEXT_KEY) : fallback.get());
  }

  private static ContextInternal vertxContext() {
    ContextInternal ctx = (ContextInternal) Vertx.currentContext();
    return ctx != null && ctx.isDuplicate() ? ctx : null;
  }

  private static Context rootIfNull(Context context) {
    return context == null ? Context.ROOT : context;
  }
}
