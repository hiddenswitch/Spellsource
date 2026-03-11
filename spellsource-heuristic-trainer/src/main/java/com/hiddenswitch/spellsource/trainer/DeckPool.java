package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.GameDeck;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Loads decks from resource file {@code /decks/index.txt}. Each line is:
 * <pre>
 * DeckName|Deckstring
 * </pre>
 * Deckstrings are decoded via {@link DeckStringDecoder} and DBF IDs
 * are mapped to Spellsource card IDs via {@link CardIdMapper}.
 */
public class DeckPool {
	private static final Logger LOG = Logger.getLogger(DeckPool.class.getName());

	private final List<GameDeck> decks = new ArrayList<>();

	public DeckPool(CardIdMapper mapper) throws IOException {
		loadDecks(mapper);
	}

	private void loadDecks(CardIdMapper mapper) throws IOException {
		try (var stream = getClass().getResourceAsStream("/decks/index.txt")) {
			if (stream == null) {
				LOG.warning("No /decks/index.txt found on classpath");
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;

				int pipe = line.indexOf('|');
				if (pipe < 0) {
					LOG.warning("Skipping malformed line (no pipe): " + line);
					continue;
				}

				String deckName = line.substring(0, pipe).trim();
				String deckstring = line.substring(pipe + 1).trim();

				try {
					DeckStringDecoder.DecodedDeck decoded = DeckStringDecoder.decode(deckstring);
					String heroClass = mapper.getHeroClass(decoded.heroDbfId());
					if (heroClass == null) {
						LOG.warning("Unknown hero DBF ID " + decoded.heroDbfId() + " for deck " + deckName);
						continue;
					}

					List<String> cardIds = new ArrayList<>();
					int missing = 0;
					for (var entry : decoded.cards().entrySet()) {
						String cardId = mapper.getSpellsourceId(entry.getKey());
						if (cardId != null) {
							for (int i = 0; i < entry.getValue(); i++) {
								cardIds.add(cardId);
							}
						} else {
							missing += entry.getValue();
							mapper.trackMissing(entry.getKey());
						}
					}

					if (cardIds.size() < 20) {
						LOG.warning("Deck '" + deckName + "' has only " + cardIds.size() + "/30 cards resolved (" + missing + " missing), skipping");
						continue;
					}

					DeckCreateRequest request = DeckCreateRequest.fromCardIds(heroClass, cardIds);
					request.setName(deckName);
					GameDeck deck = request.toGameDeck();
					decks.add(deck);

					if (missing > 0) {
						LOG.info("Loaded deck: " + deckName + " (" + cardIds.size() + "/30 cards, " + missing + " missing)");
					} else {
						LOG.info("Loaded deck: " + deckName + " (30/30 cards)");
					}
				} catch (Exception e) {
					LOG.warning("Failed to decode deck '" + deckName + "': " + e.getMessage());
				}
			}
		}
		LOG.info("Loaded " + decks.size() + " valid decks");
	}

	public List<GameDeck> getDecks() {
		return Collections.unmodifiableList(decks);
	}

	public int size() {
		return decks.size();
	}
}
