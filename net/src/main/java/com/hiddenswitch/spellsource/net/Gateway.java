package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.impl.GatewayImpl;
import com.hiddenswitch.spellsource.net.impl.util.HandlerFactory;
import com.hiddenswitch.spellsource.net.models.DeckDeleteResponse;
import com.hiddenswitch.spellsource.net.impl.WebResult;
import com.hiddenswitch.spellsource.client.models.*;
import io.vertx.core.Verticle;
import io.vertx.core.VertxException;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A specification for the HTTP API that clients can access. See the Swagger specification for detailed documentation of
 * each of these methods.
 * <p>
 * To create a new HTTP API endpoint:
 *
 * <ol>
 * <li>Create an operation with its associated response and optionally request schemas in {@code
 * resources/server.yaml}. A new operation consists of an entry in the Swagger file's Definition section for its
 * response {@code R} and optionally request type {@code T}. It then consists of an entry in the Paths section. Use the
 * <a href="http://editor.swagger.io">Swagger Code Editor</a> to conveniently test and define the new code that should
 * be added to the {@code server.yaml} file. </li>
 * <li>Create an entry in this interface corresponding to the new operation that returns a {@link WebResult} typed with
 * your new response type {@code R}. Depending on which parameters you supply to the method, you will imply different
 * behaviour for the serialization and authorization boilerplate provided by {@link HandlerFactory}. (1) Every method
 * should start with an {@link RoutingContext} argument; (2) if the method requires the user to be authenticated and
 * authorized, the next argument should be {@code String userId}, or omit the argument; (3) if the path is variable,
 * e.g., {@code /decks/:deckId}, the next argument should be a {@link String} whose name matches the variable name,
 * or omit the argument; (4) finally, if the method accepts a request body of type {@code T}, the next argument should
 * be {@code T request}, or omit the argument. Every supported pattern is shown below:
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
 * </pre></li>
 * <li>Implement this interface in {@link GatewayImpl}, or whatever {@link io.vertx.core.Verticle} or class will serve
 * as an <a href="https://www.linkedin.com/pulse/api-gateway-pattern-subhash-chandran">API gateway</a>. This method
 * should return a {@link WebResult}.</li><li>In the body of {@link GatewayImpl#start()}, add a {@link Router#route()}
 * call actually handle the request. Idiosyncratically, you cannot chain route handlers, so adding the route typically
 * looks like this:
 * <pre>
 *     {@code
 *     // Parse the body
 *     router.route("/accounts").handler(bodyHandler);
 *     // If required, authorize and authenticate the user based on the token in the X-Auth-UserId header they provide.
 *     // Specify that this auth is required for an Http.GET
 *     router.route("/accounts").method(HttpMethod.GET).handler(authHandler);
 *     // Finally, handle the request.
 *     // Read on for how to build a HandlerFactor.handler call.
 *     router.route("/accounts").method(HttpMethod.GET).handler(HandlerFactory.handler(GetAccountsRequest.class,
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
 *         .route("/my-method")
 *         .method(HttpMethod.GET).handler(HandlerFactory.handler(this::myMethod));
 *
 *     // [X] Request body, [ ] Path variable.
 *     router
 *         .route("/my-method")
 *         .method(HttpMethod.POST).handler(HandlerFactory.handler(MyMethodRequest.class, this::myMethod));
 *
 *     // [ ] Request body, [X] Path variable.
 *     router
 *         .route("/my-method/:objectId")
 *         .method(HttpMethod.GET).handler(HandlerFactory.handler("objectId", this::myMethod));
 *
 *     // [X] Request body, [X] Path variable.
 *     router
 *         .route("/my-method/:objectId")
 *         .method(HttpMethod.POST).handler(HandlerFactory.handler(MyMethodRequest.class, "objectId", this::myMethod));
 *     }
 * </pre></li></ol>
 */
public interface Gateway extends Verticle {
	static Gateway create() {
		return new GatewayImpl(Configuration.apiGatewayPort());
	}

	static Gateway create(int port) {
		return new GatewayImpl(port);
	}

	@Suspendable
	WebResult<com.hiddenswitch.spellsource.net.models.MatchCancelResponse> matchmakingDelete(RoutingContext context) throws SuspendExecution;

	WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException;

	WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException;

	WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException;

	WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException;

	WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException;

	WebResult<MatchmakingQueuesResponse> matchmakingGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<FriendPutResponse> friendPut(RoutingContext context, String userId, FriendPutRequest req) throws SuspendExecution, InterruptedException;

	WebResult<UnfriendResponse> unFriend(RoutingContext context, String userId, String friendId) throws SuspendExecution, InterruptedException;

	WebResult<DraftState> draftsGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<DraftState> draftsPost(RoutingContext context, String userId, DraftsPostRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DraftState> draftsChooseHero(RoutingContext context, String userId, DraftsChooseHeroRequest request) throws SuspendExecution, InterruptedException;

	WebResult<DraftState> draftsChooseCard(RoutingContext context, String userId, DraftsChooseCardRequest request) throws SuspendExecution, InterruptedException;

	WebResult<AcceptInviteResponse> acceptInvite(RoutingContext context, String userId, String inviteId, AcceptInviteRequest request) throws SuspendExecution, InterruptedException;

	WebResult<InviteResponse> getInvite(RoutingContext context, String userId, String inviteId) throws SuspendExecution, InterruptedException;

	WebResult<InviteResponse> deleteInvite(RoutingContext context, String userId, String inviteId) throws SuspendExecution, InterruptedException;

	WebResult<InviteResponse> postInvite(RoutingContext context, String userId, InvitePostRequest request) throws SuspendExecution, InterruptedException;

	WebResult<InviteGetResponse> getInvites(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	WebResult<Void> healthCheck(RoutingContext context) throws SuspendExecution, InterruptedException;

	WebResult<com.hiddenswitch.spellsource.net.models.ChangePasswordResponse> changePassword(RoutingContext context, String userId, ChangePasswordRequest request) throws SuspendExecution, InterruptedException;

	WebResult<GetCardsResponse> getCards(RoutingContext context) throws SuspendExecution, InterruptedException;

	WebResult<GetGameRecordResponse> getGameRecord(RoutingContext context, String userId, String gameId) throws SuspendExecution, InterruptedException;

	WebResult<GetGameRecordIdsResponse> getGameRecordIds(RoutingContext context, String userId) throws SuspendExecution, InterruptedException;

	/**
	 * Heuristically retrieves the primary networking interface for this device.
	 *
	 * @return A Java {@link NetworkInterface} object that can be used by {@link io.vertx.core.Vertx}.
	 */
	static NetworkInterface mainInterface() {
		final ArrayList<NetworkInterface> interfaces;
		try {
			interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		final NetworkInterface networkInterface = interfaces.stream().filter(ni -> {
			boolean isLoopback = false;
			boolean supportsMulticast = false;
			boolean isVirtualbox = false;
			boolean isSelfAssigned = false;
			try {
				isSelfAssigned = ni.inetAddresses().anyMatch(i -> i.getHostAddress().startsWith("169"));
				isLoopback = ni.isLoopback();
				supportsMulticast = ni.supportsMulticast();
				isVirtualbox = ni.getDisplayName().contains("VirtualBox") || ni.getDisplayName().contains("Host-Only");
			} catch (IOException failure) {
			}
			final boolean hasIPv4 = ni.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
			return supportsMulticast && !isSelfAssigned && !isLoopback && !ni.isVirtual() && hasIPv4 && !isVirtualbox;
		}).sorted(Comparator.comparing(NetworkInterface::getName)).findFirst().orElse(null);
		return networkInterface;
	}

	/**
	 * Retrieves a local-network-accessible IPv4 address for this instance by heuristically picking the "primary" network
	 * interface on this device.
	 *
	 * @return A string in the form of "192.168.0.1"
	 */
	@NotNull
	static String getHostIpAddress() {
		try {
			final InterfaceAddress hostAddress = mainInterface().getInterfaceAddresses().stream().filter(ia -> ia.getAddress() instanceof Inet4Address).findFirst().orElse(null);
			if (hostAddress == null) {
				return "127.0.0.1";
			}
			return hostAddress.getAddress().getHostAddress();
		} catch (Throwable ex) {
			throw new VertxException(ex);
		}
	}
}