package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.GraphQLMutationResolverImpl;
import com.hiddenswitch.framework.impl.GraphQLQueryResolverImpl;
import com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.scalars.ExtendedScalars;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.instrumentation.VertxFutureAdapter;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.graphql.GraphQLHandler;

import static io.vertx.await.Async.await;


public class GraphQL extends AbstractVirtualThreadVerticle {

	@Override
	public void startVirtual() throws Exception {
		var router = Router.router(vertx);
		var jwtAuth = JWTAuth.create(vertx, Accounts.jwtAuthOptions());
		var realm = await(Accounts.realm());

		var schema = SchemaParser.newParser()
				.file("schema.graphqls")
				.options(new SchemaParserOptions.Builder()
						.genericWrappers(new SchemaParserOptions.GenericWrapper(Future.class, 0))
						.build())
				.resolvers(new GraphQLQueryResolverImpl(), new GraphQLMutationResolverImpl())
				.scalars(ExtendedScalars.newAliasedScalar("BigInt").aliasedScalar(ExtendedScalars.GraphQLLong).build())
				.build()
				.makeExecutableSchema();

		var gql = graphql.GraphQL.newGraphQL(schema)
				.instrumentation(VertxFutureAdapter.create())
				.build();

		var handler = GraphQLHandler.create(gql);

		router.post("/graphql")
				.handler(BodyHandler.create())
				.handler(JWTAuthHandler.create(jwtAuth, realm.toRepresentation().getRealm()))
				.failureHandler(ctx -> {
					var json = ctx.body().asJsonObject();
					var query = json.getString("query");

					if (ctx.user() == null && query.startsWith("query IntrospectionQuery")) {
						ctx.setUser(User.create(new JsonObject().put("name", "Introspection Query")));
						ctx.reroute(ctx.normalizedPath());
					} else {
						ctx.response().setStatusCode(401).end("Unauthorized");
					}
				})
				.handler(VirtualThreadRoutingContextHandler.create(ctx -> handler.handle(RoutingContext.newInstance(ctx))));


		router.route("/graphiql*").subRouter(GraphiQLHandler.create(vertx, new GraphiQLHandlerOptions().setEnabled(true)).router());


		var server = vertx.createHttpServer(new HttpServerOptions().setPort(4000)); // TODO configurable port
		server.requestHandler(router);
		await(server.listen());
	}

}