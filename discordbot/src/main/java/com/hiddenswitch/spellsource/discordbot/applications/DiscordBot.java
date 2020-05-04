package com.hiddenswitch.spellsource.discordbot.applications;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class DiscordBot extends ListenerAdapter {
	Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

	public static void main(String[] args) throws InterruptedException, LoginException {
		var apiKey = System.getenv("DISCORD_BOT_API_KEY");
		var builder = JDABuilder.createDefault(apiKey);
		builder.addEventListeners(new DiscordBot());
		// Disable parts of the cache
		builder.disableCache(EnumSet.allOf(CacheFlag.class));
		// Set activity (like "playing Something")
		builder.setActivity(Activity.playing("being a bot"));
		var jda = builder.build();
		jda.awaitReady();
	}

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		super.onMessageReceived(event);

		if (event.isFromType(ChannelType.TEXT)) {
			LOGGER.info("Received: {}", event.getMessage().getContentDisplay());
		}
	}
}
