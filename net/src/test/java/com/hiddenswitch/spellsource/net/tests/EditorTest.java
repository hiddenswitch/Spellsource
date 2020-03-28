package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.SuspendableAction2;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeMethodPutCard;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.net.Editor;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class EditorTest extends SpellsourceTestBase {

	private void drawCardHarness(TestContext testContext, String code, SuspendableAction2<ServerGameContext, Envelope> handler) {
		sync(() -> {
			var keepPlaying = new AtomicBoolean(true);
			try (var client = new UnityClient(testContext) {
				@Override
				protected boolean onRequestAction(ServerToClientMessage message) {
					return keepPlaying.get();
				}
			}) {
				client.createUserAccount();
				client.matchmakeQuickPlay(null);
				client.play(1);
				keepPlaying.set(false);
				// Check that the server game context gets edited properly
				var serverGameContext = getServerGameContext(client.getUserId()).orElseThrow();
				var envelope = Editor.putCard(new EnvelopeMethodPutCard()
								.editableCardId(null)
								.draw(true)
								.source(code),
						client.getUserId().toString());

				handler.call(serverGameContext, envelope);
				keepPlaying.set(true);
				client.respondRandomAction();
				client.waitUntilDone();
			}
		}, testContext);
	}

	@Test
	public void testBasicEditing(TestContext testContext) {
		var code = Json.encode(CardCatalogue.getCardById(CardCatalogue.getOneOneNeutralMinionCardId()).getDesc());
		drawCardHarness(testContext, code, (serverGameContext, envelope) -> {
			testContext.assertNotNull(envelope.getResult().getPutCard().getEditableCardId());
			testContext.assertTrue(serverGameContext.getPlayers().stream().anyMatch(p -> p.getHand().containsCard(CardCatalogue.getOneOneNeutralMinionCardId())), "the player should have drawn the card");
			testContext.assertTrue(serverGameContext.isRunning());
		});
	}

	@Test
	public void testDecodeExceptionAppearsAsError(TestContext testContext) {
		var code = "{\"name\": \"Test\",,}";
		drawCardHarness(testContext, code, (serverGameContext, envelope) -> {
			testContext.assertNotNull(envelope.getResult().getPutCard().getEditableCardId(), "should still save card");
			testContext.assertEquals(envelope.getResult().getPutCard().getCardScriptErrors().size(), 1);
			testContext.assertFalse(serverGameContext.getPlayers().stream().anyMatch(p -> p.getHand().containsCard(CardCatalogue.getOneOneNeutralMinionCardId())), "the player should NOT have drawn the card");
			testContext.assertTrue(serverGameContext.isRunning());
		});
	}
}
