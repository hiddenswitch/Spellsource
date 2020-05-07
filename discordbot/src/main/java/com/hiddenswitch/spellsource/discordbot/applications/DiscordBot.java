package com.hiddenswitch.spellsource.discordbot.applications;

import com.hiddenswitch.spellsource.client.models.CardType;
import com.hiddenswitch.spellsource.core.JsonConfiguration;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.EmoteManager;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.requests.Route;
import org.apache.commons.collections4.ComparatorUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.dv8tion.jda.api.MessageBuilder.Formatting.*;

public class DiscordBot extends ListenerAdapter {
	static {
		JsonConfiguration.configureJson();
		CardCatalogue.loadCardsFromFilesystemDirectories("cards/src/main/resources/cards", "game/src/main/resources/cards");
	}

//	Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	final static String CARD_COMMAND_REGEX = "^\\s*![Cc][Aa][Rr][Dd]\\s+(?<nameOrId>.*\\b)\\s*$";
	final static String HELP_COMMAND_REGEX = "^\\s*![Hh][Ee][Ll][Pp]";

	static Comparator<Card> CARD_SORTER;

	public static void main(String[] args) throws InterruptedException, LoginException {
		var apiKey = System.getenv("DISCORD_BOT_API_KEY");
		var builder = JDABuilder.createDefault(apiKey);
		builder.addEventListeners(new DiscordBot());
		// Disable parts of the cache
		builder.disableCache(EnumSet.allOf(CacheFlag.class));
		// Set activity (like "playing Something")
		builder.setActivity(Activity.playing("Spellsource"));
		var jda = builder.build();
		jda.awaitReady();

		CARD_SORTER = (o1, o2) -> ComparatorUtils.booleanComparator(true).compare(o1.isCollectible(), o2.isCollectible());
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		super.onMessageReceived(event);

		if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {
			String messageContent = event.getMessage().getContentDisplay();
//			LOGGER.info("Received: {}", messageContent);
			handleMessage(messageContent, event);
		}

		if (event.getMessage().getMentionedUsers().stream().anyMatch(User::isBot)) {
			event.getMessage().addReaction("❤️").submit();
		}
	}

	public static Message handleMessage(String messageContent, MessageReceivedEvent event) {
		Message response = null;
		Matcher cardCommandMatcher = Pattern.compile(CARD_COMMAND_REGEX).matcher(messageContent);
		MessageBuilder messageBuilder = new MessageBuilder();

		if (cardCommandMatcher.find()) {
			if (event != null) messageBuilder.append(event.getAuthor().getAsMention());

			var nameOrId = cardCommandMatcher.group("nameOrId").trim();
			CardList cards = new CardArrayList();
			if (nameOrId.contains("_")) {
				cards = CardCatalogue.query(DeckFormat.all(), c -> c.getCardId().equalsIgnoreCase(nameOrId));
			} else {
				cards = CardCatalogue.query(DeckFormat.all(), c -> c.getName().equalsIgnoreCase(nameOrId));
			}
			cards.sort(CARD_SORTER);

			if (cards.isEmpty()) {
				messageBuilder.append(" Sorry, I couldn't find a card with name/id \"").append(nameOrId).append("\" :/");
			} else {
				messageBuilder.append(" Here you go: ");
				if (cards.size() == 1) {
					sayCard(cards.get(0), messageBuilder);
				} else {
					for (Card card : cards) {
						sayCard(card, messageBuilder);
						messageBuilder.append("\n");
					}
				}
			}

			response = messageBuilder.build();
		} else if (messageContent.matches(HELP_COMMAND_REGEX)) {
			if (event != null) messageBuilder.append(event.getAuthor().getAsMention());

			messageBuilder.append(" Currently my only command is the `!card` command.\n");
			messageBuilder.append("Put it at the start of your message followed by the name or id of a card.\n");
			messageBuilder.append("If it matches a card, I'll give you its information.\n");
			messageBuilder.append("(An ");
			messageBuilder.append("italicized", ITALICS);
			messageBuilder.append(" card id means it's uncollectible, and ");
			messageBuilder.append("strikethrough", STRIKETHROUGH);
			messageBuilder.append(" means it's from an uncollectible class).\n");


			response = messageBuilder.build();
		}

		if (event != null && response != null) {
			event.getChannel().sendMessage(response).submit();
		}
		return response;
	}

	public static void sayCard(Card card, MessageBuilder messageBuilder) {
		messageBuilder.append("\n");

		boolean cardCollectible = card.isCollectible();
		boolean classCollectible = HeroClass.getClassCard(card.getHeroClass()) == null ||
				HeroClass.getClassCard(card.getHeroClass()).isCollectible();
		if (!cardCollectible && !classCollectible) {
			messageBuilder.append(card.getCardId(), STRIKETHROUGH, ITALICS);
		} else if (!cardCollectible) {
			messageBuilder.append(card.getCardId(), ITALICS);
		} else if (!classCollectible) {
			messageBuilder.append(card.getCardId(), STRIKETHROUGH);
		} else {
			messageBuilder.append(card.getCardId());
		}

		messageBuilder.append(" - ").append(stringify(card));
	}


	public static String stringify(Card card) {
		StringBuilder builder = new StringBuilder();
		builder.append(card.getBaseManaCost()).append(" Cost ");
		if (card.getCardType() == CardType.MINION) {
			builder.append(card.getBaseAttack()).append("/").append(card.getBaseHp()).append(" ");
		}
		builder.append(card.getRarity().toString()).append(" ");
		builder.append(card.getHeroClass()).append(" (");
		if (card.getHeroClass().equalsIgnoreCase(HeroClass.ANY)) {
			builder.append("Neutral) ");
		} else builder.append(HeroClass.getClassCard(card.getHeroClass()).getName()).append(") ");
		builder.append(card.getCardType().toString()).append(" ");
		if (!card.getDescription().equals("")) {
			builder.append("\"").append(card.getDescription().replace("#", "").replace("$", "")
					.replace("[", "").replace("]","").trim()).append("\"");
		}
		return builder.toString();
	}
}
