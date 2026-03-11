package com.hiddenswitch.spellsource.trainer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Maps Hearthstone DBF IDs to Spellsource card IDs.
 * <p>
 * Resolution order:
 * <ol>
 *   <li>Exact tag match (e.g. HS ID "AT_055" matches tag "AT_055")</li>
 *   <li>CORE_ stripped tag match (e.g. HS ID "CORE_AT_055" matches tag "AT_055")</li>
 *   <li>Card name match (case-insensitive, first collectible match preferred)</li>
 * </ol>
 */
public class CardIdMapper {
	private static final Logger LOG = Logger.getLogger(CardIdMapper.class.getName());
	private static final String HS_JSON_URL = "https://api.hearthstonejson.com/v1/latest/enUS/cards.json";
	private static final Path CACHE_FILE = Path.of(System.getProperty("user.home"), ".cache", "spellsource-trainer", "cards.json");

	private final Map<Integer, String> dbfIdToSpellsourceId = new HashMap<>();
	private final Set<String> missingCards = new TreeSet<>();

	private static final Map<Integer, String> HERO_DBF_TO_CLASS = new HashMap<>();

	static {
		HERO_DBF_TO_CLASS.put(274, "BROWN");   // Malfurion (Druid)
		HERO_DBF_TO_CLASS.put(31, "GREEN");    // Rexxar (Hunter)
		HERO_DBF_TO_CLASS.put(637, "BLUE");    // Jaina (Mage)
		HERO_DBF_TO_CLASS.put(671, "GOLD");    // Uther (Paladin)
		HERO_DBF_TO_CLASS.put(813, "WHITE");   // Anduin (Priest)
		HERO_DBF_TO_CLASS.put(930, "BLACK");   // Valeera (Rogue)
		HERO_DBF_TO_CLASS.put(1066, "SILVER"); // Thrall (Shaman)
		HERO_DBF_TO_CLASS.put(893, "VIOLET");  // Gul'dan (Warlock)
		HERO_DBF_TO_CLASS.put(7, "RED");       // Garrosh (Warrior)
		HERO_DBF_TO_CLASS.put(56550, "PURPLE"); // Illidan (DH)
		HERO_DBF_TO_CLASS.put(78065, "SPIRIT"); // Death Knight
		HERO_DBF_TO_CLASS.put(50484, "BROWN"); // Lunara
		HERO_DBF_TO_CLASS.put(2826, "GREEN");  // Alleria
		HERO_DBF_TO_CLASS.put(2829, "BLUE");   // Medivh
		HERO_DBF_TO_CLASS.put(2827, "VIOLET"); // Lady Liadrin
		HERO_DBF_TO_CLASS.put(46116, "WHITE"); // Tyrande
		HERO_DBF_TO_CLASS.put(40195, "BLACK"); // Maiev
		HERO_DBF_TO_CLASS.put(40183, "SILVER"); // Morgl
		HERO_DBF_TO_CLASS.put(47817, "VIOLET"); // Nemsy
		HERO_DBF_TO_CLASS.put(2828, "RED");    // Magni
	}

	public CardIdMapper() throws IOException, InterruptedException {
		loadData();
	}

	private void loadData() throws IOException, InterruptedException {
		ClasspathCardCatalogue catalogue = ClasspathCardCatalogue.INSTANCE;

		// Build tag → card ID index
		Map<String, String> tagToCardId = new HashMap<>();
		// Build name → card ID index (prefer collectible cards)
		Map<String, String> nameToCardId = new HashMap<>();
		for (Card card : catalogue.getAll()) {
			if (card.getDesc() != null && card.getDesc().getTags() != null) {
				for (String tag : card.getDesc().getTags()) {
					tagToCardId.put(tag, card.getCardId());
				}
			}
			if (card.getName() != null) {
				String lowerName = card.getName().toLowerCase();
				String existing = nameToCardId.get(lowerName);
				// Prefer collectible cards over tokens
				if (existing == null || card.isCollectible()) {
					nameToCardId.put(lowerName, card.getCardId());
				}
			}
		}
		LOG.info("Built tag index: " + tagToCardId.size() + " tags, name index: " + nameToCardId.size() + " names");

		// Load HearthstoneJSON
		String json = loadOrFetchJson();
		Gson gson = new Gson();
		JsonArray cards = gson.fromJson(json, JsonArray.class);

		int byTag = 0, byStrippedTag = 0, byName = 0;

		for (JsonElement element : cards) {
			JsonObject card = element.getAsJsonObject();
			if (!card.has("dbfId") || !card.has("id")) continue;

			int dbfId = card.get("dbfId").getAsInt();
			String hsId = card.get("id").getAsString();
			String hsName = card.has("name") ? card.get("name").getAsString() : null;

			// 1. Exact tag match
			String spellsourceId = tagToCardId.get(hsId);
			if (spellsourceId != null) {
				dbfIdToSpellsourceId.put(dbfId, spellsourceId);
				byTag++;
				continue;
			}

			// 2. Strip CORE_ prefix and try again
			if (hsId.startsWith("CORE_")) {
				spellsourceId = tagToCardId.get(hsId.substring(5));
				if (spellsourceId != null) {
					dbfIdToSpellsourceId.put(dbfId, spellsourceId);
					byStrippedTag++;
					continue;
				}
			}

			// 3. Name fallback
			if (hsName != null) {
				spellsourceId = nameToCardId.get(hsName.toLowerCase());
				if (spellsourceId != null) {
					dbfIdToSpellsourceId.put(dbfId, spellsourceId);
					byName++;
				}
			}
		}

		LOG.info(String.format("Mapped %d DBF IDs (tag: %d, core-stripped: %d, name: %d)",
				dbfIdToSpellsourceId.size(), byTag, byStrippedTag, byName));
	}

	private String loadOrFetchJson() throws IOException, InterruptedException {
		if (Files.exists(CACHE_FILE)) {
			long age = System.currentTimeMillis() - Files.getLastModifiedTime(CACHE_FILE).toMillis();
			if (age < 7 * 24 * 60 * 60 * 1000L) {
				LOG.info("Using cached HearthstoneJSON data from " + CACHE_FILE);
				return Files.readString(CACHE_FILE);
			}
		}

		LOG.info("Fetching HearthstoneJSON data from " + HS_JSON_URL);
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(HS_JSON_URL))
				.GET()
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String json = response.body();

		Files.createDirectories(CACHE_FILE.getParent());
		Files.writeString(CACHE_FILE, json);

		return json;
	}

	public String getSpellsourceId(int dbfId) {
		return dbfIdToSpellsourceId.get(dbfId);
	}

	public String getHeroClass(int heroDbfId) {
		return HERO_DBF_TO_CLASS.getOrDefault(heroDbfId, null);
	}

	public Set<String> getMissingCards() {
		return Collections.unmodifiableSet(missingCards);
	}

	public void trackMissing(int dbfId) {
		missingCards.add("dbf:" + dbfId);
	}
}
