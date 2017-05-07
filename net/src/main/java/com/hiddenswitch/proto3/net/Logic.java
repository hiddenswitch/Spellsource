package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;

/**
 * Created by bberman on 1/30/17.
 */
public interface Logic {
	String[] STARTING_DECKS = {"Basic Resurrector", "Basic Octopod Demo", "Basic Cyborg", "Basic Biologist", "Basic Biologist", "Basic Gamer",/* "Test Discoveries",  "Test Battlecries"*/};

	@Suspendable
	InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException;

	EndGameResponse endGame(EndGameRequest request) throws SuspendExecution, InterruptedException;

	@Suspendable
	StartGameResponse startGame(StartGameRequest request) throws SuspendExecution, InterruptedException;

	@Suspendable
	LogicResponse beforeSummon(EventLogicRequest<BeforeSummonEvent> request);

	@Suspendable
	LogicResponse afterPhysicalAttack(EventLogicRequest<AfterPhysicalAttackEvent> logicRequest);
}
