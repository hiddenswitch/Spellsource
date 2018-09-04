package examples;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.HandlerReceiverAdaptor;

import static io.vertx.ext.sync.Sync.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Examples {

  public void syncResultExample(Vertx vertx) {

    EventBus eb = vertx.eventBus();

    // Send a message and get the reply synchronously

    Message<String> reply = awaitResult(h -> eb.send("someaddress", "ping", h));

    System.out.println("Received reply " + reply.body());

  }

  public void syncEventExample(Vertx vertx) {

    // Set up a timer to fire
    long tid = awaitEvent(h -> vertx.setTimer(1000, h));

    System.out.println("Timer has now fired");

  }

  public void streamExample(Vertx vertx) {

    EventBus eb = vertx.eventBus();

    HandlerReceiverAdaptor<Message<String>> adaptor = streamAdaptor();

    eb.<String>consumer("some-address").handler(adaptor);

    // Receive 10 messages from the consumer:
    for (int i = 0; i < 10; i++) {

      Message<String> received1 = adaptor.receive();

      System.out.println("got message: " + received1.body());

    }

  }

  public void fiberHandlerExample(Vertx vertx) {

    EventBus eb = vertx.eventBus();

    vertx.createHttpServer().requestHandler(fiberHandler(req -> {

      // Send a message to address and wait for a reply
      Message<String> reply = awaitResult(h -> eb.send("some-address", "blah", h));

      System.out.println("Got reply: " + reply.body());

      // Now end the response
      req.response().end("blah");

    })).listen(8080, "localhost");

  }
}
