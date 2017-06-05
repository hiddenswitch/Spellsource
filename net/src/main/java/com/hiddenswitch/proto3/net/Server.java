package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.impl.ServerImpl;
import com.hiddenswitch.proto3.net.impl.util.HandlerFactory;
import com.hiddenswitch.proto3.net.models.DeckDeleteResponse;
import com.hiddenswitch.proto3.net.util.WebResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * A specification for the HTTP API that clients can access. See the Swagger specification for detailed documentation of
 * each of these methods.
 * <p>
 * To create a new HTTP API endpoint:
 * <p>
 * <ul><li>Create an operation with its associated response and optionally request schemas in {@code
 * resources/server.yaml}. A new operation consists of an entry in the Swagger file's Definition section for its
 * response {@code R} and optionally request type {@code T}. It then consists of an entry in the Paths section. Use the
 * <a href="http://editor.swagger.io">Swagger Code Editor</a> to conveniently test and define the new code that should
 * be added to the {@code server.yaml} file. </li><li>Create an entry in this interface corresponding to the new
 * operation that returns a {@link WebResult} typed with your new response type {@code R}. Depending on which parameters
 * you supply to the method, you will imply different behaviour for the serialization and authorization boilerplate
 * provided by {@link HandlerFactory}. (1) Every method should start with an {@link RoutingContext} argument; (2) if the
 * method requires the user to be authenticated and authorized, the next argument should be {@code String userId}, or
 * omit the argument; (3) if the path is variable, e.g., {@code /v1/decks/:deckId}, the next argument should be a {@link
 * String} whose name matches the variable name, or omit the argument; (4) finally, if the method accepts a request body
 * of type {@code T}, the next argument should be {@code T request}, or omit the argument. Every supported pattern is
 * shown below:
 * <pre>
 *     {@code
 *     // [ ] Authorization, [X] Request body, [ ] Path variable.
 *     WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution,
 * InterruptedException;
 *
 *     // [X] Authorization, [ ] Request body, [ ] Path variable.
 *     WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution,
 * InterruptedException;
 *
 *     // [X] Authorization, [X] Request body, [ ] Path variable.
 *     WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws
 * SuspendExecution, InterruptedException;
 *
 *     // [X] Authorization, [X] Request body, [X] Path variable.
 *     WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand
 * updateCommand) throws SuspendExecution, InterruptedException;
 *
 *     // [X] Authorization, [ ] Request body, [X] Path variable.
 *     WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws
 * SuspendExecution, InterruptedException;
 *
 *     // All other patterns are unsupported. Implement your own serialization, authorization, deserialization pattern
 *     // by accessing fields in the routingContext and adapting one of the HandlerFactor methods.
 *     }
 * </pre></li><li>Implement this
 * interface in {@link com.hiddenswitch.proto3.net.impl.ServerImpl}, or whatever {@link io.vertx.core.Verticle} or class
 * will serve as an <a href="https://www.linkedin.com/pulse/api-gateway-pattern-subhash-chandran">API gateway</a>. This
 * method should return a {@link WebResult}.</li><li>In the body of {@link ServerImpl#start()}, add a {@link
 * Router#route()} call actually handle the request. Idiosyncratically, you cannot chain route handlers, so adding the
 * route typically looks like this:
 * <pre>
 *     {@code
 *     // Parse the body
 *     router.route("/v1/accounts").handler(bodyHandler);
 *     // If required, authorize and authenticate the user based on the token in the X-Auth-UserId header they provide.
 *     // Specify that this auth is required for an Http.GET
 *     router.route("/v1/accounts").method(HttpMethod.GET).handler(authHandler);
 *     // Finally, handle the request.
 *     // Read on for how to build a HandlerFactor.handler call.
 *     router.route("/v1/accounts").method(HttpMethod.GET).handler(HandlerFactory.handler(GetAccountsRequest.class,
 * this::getAccounts));
 *     }
 * </pre>
 * In order to not use Java annotations, this codebase uses {@link HandlerFactory} to wrap your API methods. It provides
 * serialization of requests, deserialization of responses and user authentication/authorization for your API method
 * body. It will figure out automatically based on the method signature whether or not you need authorization and what
 * your response type is. Some examples:
 * <pre>
 *     {@code
 *     // [ ] Request body, [ ] Path variable.
 *     router
 *         .route("/v1/my-method")
 *         .method(HttpMethod.GET).handler(HandlerFactory.handler(this::myMethod));
 *
 *     // [X] Request body, [ ] Path variable.
 *     router
 *         .route("/v1/my-method")
 *         .method(HttpMethod.POST).handler(HandlerFactory.handler(MyMethodRequest.class, this::myMethod));
 *
 *     // [ ] Request body, [X] Path variable.
 *     router
 *         .route("/v1/my-method/:objectId")
 *         .method(HttpMethod.GET).handler(HandlerFactory.handler("objectId", this::myMethod));
 *
 *     // [X] Request body, [X] Path variable.
 *     router
 *         .route("/v1/my-method/:objectId")
 *         .method(HttpMethod.POST).handler(HandlerFactory.handler(MyMethodRequest.class, "objectId", this::myMethod));
 *     }
 * </pre></li></ul>
 *
 * Some new methods may require references to other services. In {@link ServerImpl}, our default API gateway, services
 * are hosted locally as package-private fields (e.g., {@link ServerImpl#bots}), and then actually deployed in the
 * {@link ServerImpl#start()} method.
 */
public interface Server {
	WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException;

	WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException;

	WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException;

	WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException;

	WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext context, String userId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException;

	WebResult<MatchCancelResponse> matchmakingConstructedQueueDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<MatchConcedeResponse> matchmakingConstructedDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<GameState> matchmakingConstructedGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;
}

