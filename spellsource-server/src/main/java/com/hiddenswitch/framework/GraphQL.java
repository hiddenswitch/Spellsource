package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.GraphQLMutationResolverImpl;
import com.hiddenswitch.framework.impl.GraphQLQueryResolverImpl;
import com.hiddenswitch.framework.impl.GraphQLSubscriptionResolverImpl;
import com.hiddenswitch.framework.impl.RogueManager;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.instrumentation.VertxFutureAdapter;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSHandler;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.graphql.GraphQLHandler;

import static io.vertx.await.Async.await;


public class GraphQL extends AbstractVirtualThreadVerticle {

	@Override
	public void startVirtual() throws Exception {
		RogueManager.initCardCatalogue();
		var cardCatalogue = new SqlCachedCardCatalogue();
		cardCatalogue.subscribe();
		cardCatalogue.invalidateAllAndRefresh();

		var router = Router.router(vertx);
		var jwtAuth = JWTAuth.create(vertx, Accounts.jwtAuthOptions());
		var realm = await(Accounts.realm());

		var schema = SchemaParser.newParser()
				.file("schema.graphqls")
				.options(new SchemaParserOptions.Builder()
						.genericWrappers(new SchemaParserOptions.GenericWrapper(Future.class, 0))
						.build())
				.resolvers(new GraphQLQueryResolverImpl(cardCatalogue), new GraphQLMutationResolverImpl(cardCatalogue), new GraphQLSubscriptionResolverImpl())
				.scalars(
						GraphQLScalarType.newScalar()
								.name("BigInt")
								.description("A 64-bit integer serialized as a string for JavaScript compatibility")
								.coercing(new Coercing<Long, String>() {
									@Override
									public String serialize(Object input) throws CoercingSerializeException {
										if (input instanceof Long l) return l.toString();
										if (input instanceof Integer i) return Integer.toString(i);
										if (input instanceof Number n) return Long.toString(n.longValue());
										if (input instanceof String s) return s;
										throw new CoercingSerializeException("Cannot serialize " + input + " as BigInt");
									}

									@Override
									public Long parseValue(Object input) throws CoercingParseValueException {
										if (input instanceof Long l) return l;
										if (input instanceof Integer i) return i.longValue();
										if (input instanceof Number n) return n.longValue();
										if (input instanceof String s) return Long.parseLong(s);
										throw new CoercingParseValueException("Cannot parse " + input + " as BigInt");
									}

									@Override
									public Long parseLiteral(Object input) throws CoercingParseLiteralException {
										if (input instanceof IntValue v) return v.getValue().longValueExact();
										if (input instanceof StringValue v) return Long.parseLong(v.getValue());
										throw new CoercingParseLiteralException("Cannot parse literal " + input + " as BigInt");
									}
								})
								.build(),
						ExtendedScalars.DateTime
				)
				.build()
				.makeExecutableSchema();

		var gql = graphql.GraphQL.newGraphQL(schema)
				.instrumentation(VertxFutureAdapter.create())
				.build();

		var handler = GraphQLHandler.create(gql);

		router.post("/graphql")
				.handler(BodyHandler.create().setBodyLimit(10 * 1024 * 1024))
				.handler(JWTAuthHandler.create(jwtAuth, realm.toRepresentation().getRealm()))
				.handler(VirtualThreadRoutingContextHandler.create(ctx -> handler.handle(RoutingContext.newInstance(ctx))))
				.failureHandler(ctx -> {
					if (ctx.failed() && ctx.failure() instanceof HttpException httpException && httpException.getMessage().contains("Unauthorized")) {
						// Allow unauthenticated requests through to the GraphQL handler.
						// Resolvers that require auth check Accounts.userId() and return
						// appropriate errors. This lets createAccount, login, introspection
						// and other public operations work without a token.
						handler.handle(RoutingContext.newInstance(ctx));
						return;
					}

					ctx.next();
				});


		// WebSocket handler for GraphQL subscriptions (graphql-ws protocol)
		// Authenticate via connection_init payload: { "Authorization": "Bearer <token>" }
		router.route("/graphql")
				.handler(GraphQLWSHandler.builder(gql)
						.onConnectionInit(event -> {
							var msg = event.message();
							var payload = msg.message().getJsonObject("payload");
							if (payload != null) {
								var authHeader = payload.getString("Authorization");
								if (authHeader != null && authHeader.startsWith("Bearer ")) {
									var token = authHeader.substring(7);
									jwtAuth.authenticate(new TokenCredentials(token))
											.onSuccess(user -> {
												// Store the authenticated user so beforeExecute can use it
												event.complete(user);
											})
											.onFailure(t -> event.complete(null));
									return;
								}
							}
							event.complete(null);
						})
						.beforeExecute(handler1 -> {
							// Set up VirtualThreadRoutingContextHandler thread-local so
							// Accounts.userId() works inside subscription resolvers.
							var connParams = handler1.context().connectionParams();
							if (connParams instanceof User user) {
								VirtualThreadRoutingContextHandler.setUser(user);
							}
						})
						.build());

		router.route("/graphiql*").subRouter(GraphiQLHandler.create(vertx, new GraphiQLHandlerOptions().setEnabled(true)).router());


		var server = vertx.createHttpServer(new HttpServerOptions()
				.setPort(4000) // TODO configurable port
				.setWebSocketSubProtocols(java.util.List.of("graphql-transport-ws")));
		server.requestHandler(router);
		await(server.listen());
	}

}