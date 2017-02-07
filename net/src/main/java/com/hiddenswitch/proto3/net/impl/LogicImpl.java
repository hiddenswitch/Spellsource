package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;

/**
 * Created by bberman on 1/30/17.
 */
public class LogicImpl extends Service<LogicImpl> implements Logic {
	private ServiceProxy<Inventory> inventory;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		Broker.of(this, Logic.class, vertx.eventBus());
		inventory = Broker.proxy(Inventory.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public InitializeUserResponse initializeUser(InitializeUserRequest request) throws InterruptedException, SuspendExecution {
		final InitializeUserResponse response = new InitializeUserResponse();
		final String userId = request.getUserId();

		response.createCollectionResponse = inventory.sync()
				.createCollection(CreateCollectionRequest.startingCollection(userId));

		return response;
	}
}
