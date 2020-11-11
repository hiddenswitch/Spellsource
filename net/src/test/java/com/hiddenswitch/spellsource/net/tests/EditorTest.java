package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction2;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeMethodPutCard;
import com.hiddenswitch.spellsource.net.Editor;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.jupiter.api.Test;

import static io.vertx.ext.sync.Sync.invoke;
import static io.vertx.ext.sync.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.*;

public class EditorTest extends SpellsourceTestBase {

	private void drawCardHarness(Vertx vertx, VertxTestContext testContext, String code, SuspendableAction2<ServerGameContext, Envelope> handler) {
		runOnFiberContext(() -> {
			try (var client = new UnityClient(testContext)) {
				invoke0(client::createUserAccount);
				invoke0(client::matchmakeQuickPlay, null);
				invoke0(client::play, 1);
				Strand.sleep(100);
				// Check that the server game context gets edited properly
				var serverGameContext = getServerGameContext(client.getUserId()).orElseThrow();
				var envelope = Editor.putCard(new EnvelopeMethodPutCard()
								.editableCardId(null)
								.draw(true)
								.source(code),
						client.getUserId().toString());

				handler.call(serverGameContext, envelope);
				invoke0(client::respondRandomAction);
				invoke0(client::waitUntilDone);
			}
		}, testContext, vertx);
	}

	@Test
	public void testBasicEditing(Vertx vertx, VertxTestContext testContext) {
		var code = Json.encode(CardCatalogue.getCardById(CardCatalogue.getOneOneNeutralMinionCardId()).getDesc());
		drawCardHarness(vertx, testContext, code, (serverGameContext, envelope) -> {
			verify(testContext, () -> {
				var cardId = envelope.getResult().getPutCard().getCardId();
				assertNotNull(cardId);
				assertNotNull(envelope.getResult().getPutCard().getEditableCardId());
				assertTrue(serverGameContext.getPlayers().stream().anyMatch(p -> p.getHand().containsCard(cardId)), "the player should have drawn the card");
				assertTrue(serverGameContext.isRunning());
			});

		});
	}

	@Test
	public void testDecodeExceptionAppearsAsError(Vertx vertx, VertxTestContext testContext) {
		var code = "{\"name\": \"Test\",,}";
		drawCardHarness(vertx, testContext, code, (serverGameContext, envelope) -> {
			verify(testContext, () -> {
				assertNotNull(envelope.getResult().getPutCard().getEditableCardId(), "should still save card");
				assertEquals(envelope.getResult().getPutCard().getCardScriptErrors().size(), 1);
				assertFalse(serverGameContext.getPlayers().stream().anyMatch(p -> p.getHand().containsCard(CardCatalogue.getOneOneNeutralMinionCardId())), "the player should NOT have drawn the card");
				assertTrue(serverGameContext.isRunning());
			});
		});
	}
}
