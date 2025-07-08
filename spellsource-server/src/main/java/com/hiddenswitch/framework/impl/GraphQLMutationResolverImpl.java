package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.graphql.MutationResolver;
import graphql.kickstart.tools.GraphQLMutationResolver;
import io.vertx.core.Future;

public class GraphQLMutationResolverImpl implements MutationResolver, GraphQLMutationResolver {

	@Override
	public Future<String> startRogueRun(String heroClass, String seed) throws Exception {
		return null;
	}
}