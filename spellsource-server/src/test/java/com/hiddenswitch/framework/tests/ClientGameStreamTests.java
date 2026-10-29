package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.spellsource.rpc.Spellsource.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.MessageTypeMessage.MessageType;
import com.hiddenswitch.spellsource.rpc.VertxHiddenSwitchSpellsourceAPIServiceGrpcClient;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientGameStreamTests {
	@SuppressWarnings("unchecked")
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void gameStreamMustDeliverGameEndBeforeClosing(boolean deliversGameEnd) {
		var rpc = mock(VertxHiddenSwitchSpellsourceAPIServiceGrpcClient.class);
		var reader = (ReadStream<ServerToClientMessage>) mock(ReadStream.class);
		var writer = (WriteStream<ClientToServerMessage>) mock(WriteStream.class);
		when(writer.write(any())).thenReturn(Future.succeededFuture());
		when(writer.end()).thenReturn(Future.succeededFuture());
		when(rpc.subscribeGame(any())).thenAnswer(call -> {
			call.<Handler<WriteStream<ClientToServerMessage>>>getArgument(0).handle(writer);
			return Future.succeededFuture(reader);
		});
		var client = new Client(null, null, "https://example.invalid") {
			@Override
			public VertxHiddenSwitchSpellsourceAPIServiceGrpcClient legacy() { return rpc; }
		};
		var result = client.playUntilGameOver();
		assertFalse(result.isComplete());
		ArgumentCaptor<Handler<ServerToClientMessage>> messages = ArgumentCaptor.forClass(Handler.class);
		ArgumentCaptor<Handler<Void>> end = ArgumentCaptor.forClass(Handler.class);
		verify(reader).handler(messages.capture());
		verify(reader).endHandler(end.capture());
		if (deliversGameEnd) {
			messages.getValue().handle(ServerToClientMessage.newBuilder().setMessageType(MessageType.ON_GAME_END).build());
		}
		end.getValue().handle(null);
		assertTrue(result.isComplete(), "a closed stream must not leave the client waiting forever");
		if (deliversGameEnd) assertTrue(result.succeeded());
		else assertEquals("Game stream ended before ON_GAME_END", result.cause().getMessage());
		verify(writer).end();
	}
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void failedSubscriptionBeforeWriterCreationFailsClient(boolean playGame) {
		var rpc = mock(VertxHiddenSwitchSpellsourceAPIServiceGrpcClient.class);
		var cause = new IllegalStateException("subscription setup failed");
		when(rpc.subscribeGame(any())).thenReturn(Future.failedFuture(cause));
		var client = new Client(null, null, "https://example.invalid") {
			@Override
			public VertxHiddenSwitchSpellsourceAPIServiceGrpcClient legacy() { return rpc; }
		};
		var result = playGame ? client.playUntilGameOver() : client.connectToGame();
		assertTrue(result.isComplete(), "failed subscription setup must not wait for a writer");
		assertSame(cause, result.cause());
	}

}
