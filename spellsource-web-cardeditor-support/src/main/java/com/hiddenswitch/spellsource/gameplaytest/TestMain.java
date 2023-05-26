package com.hiddenswitch.spellsource.gameplaytest;

import io.vertx.core.json.Json;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.tests.util.TestBase;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is used by the website to implement card editor based testing
 */
@SuppressWarnings("unused")
public class TestMain extends TestBase {

	public static GameContext runGym() {
		final TestBase testBase = new TestBase();
		AtomicReference<GameContext> gameContext = new AtomicReference<>();
		testBase.runGym((context, player, opponent) -> {
			gameContext.set(context);
		});
		return gameContext.get();
	}

	public static GameContext runGym(String friendlyClass, String enemyClass) {
		final TestBase testBase = new TestBase();
		if (friendlyClass == null || friendlyClass.equals("ANY")) {
			friendlyClass = testBase.getDefaultHeroClass();
		}
		if (enemyClass == null || enemyClass.equals("ANY")) {
			enemyClass = testBase.getDefaultHeroClass();
		}
		AtomicReference<GameContext> gameContext = new AtomicReference<>();
		testBase.runGym((context, player, opponent) -> {
			gameContext.set(context);
		}, friendlyClass, enemyClass);
		return gameContext.get();
	}

	public static void addCard(String json) {
		try {
            ClasspathCardCatalogue.CLASSPATH.addOrReplaceCard(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void spellEffect(String json, GameContext context, Entity source, Entity target) {
		SpellDesc spellDesc = Json.decodeValue(json, SpellDesc.class);
		if (source == null) {
			source = context.getPlayer1();
		}
		SpellUtils.castChildSpell(context, context.getPlayer1(), spellDesc, source, target);
	}

	public static boolean evalCondition(String json, GameContext context, Entity source, Entity target) {
		ConditionDesc conditionDesc = Json.decodeValue(json, ConditionDesc.class);
		if (source == null) {
			source = context.getPlayer1();
		}
		return conditionDesc.create().isFulfilled(context, context.getPlayer1(), source, target);
	}

	public static int evalValue(String json, GameContext context, Entity source, Entity target) {
		ValueProviderDesc valueProviderDesc = Json.decodeValue(json, ValueProviderDesc.class);
		if (source == null) {
			source = context.getPlayer1();
		}
		return valueProviderDesc.create().getValue(context, context.getPlayer1(), target, source);
	}

	public static Entity singleEntity(String entityReference, GameContext context, Player player, Entity source) {
		if (player == null) {
			player = context.getPlayer1();
		}
		if (source == null) {
			source = player;
		}
		EntityReference reference;
		try {
			reference = new EntityReference(Integer.parseInt(entityReference));
		} catch (NumberFormatException e) {
			reference = ParseUtils.parseEntityReference(entityReference);
		}
		return context.resolveTarget(player, source, reference).get(0);
	}

	public static Player player(String p, GameContext context) {
		TargetPlayer targetPlayer = Enum.valueOf(TargetPlayer.class, p);
		switch (targetPlayer) {
			case PLAYER_1:
			case SELF:
			default:
				return context.getPlayer1();
			case PLAYER_2:
			case OPPONENT:
				return context.getPlayer2();
			case ACTIVE:
				return context.getActivePlayer();
			case INACTIVE:
				return context.getOpponent(context.getActivePlayer());
		}
	}

	public static void card(GameContext context, Player player, String cardId) {
		playCard(context, player, cardId);
	}

	public static void cardRef(GameContext context, Player player, Entity card) {
		playCard(context, player, (Card) card);
	}

	public static void cardTarget(GameContext context, Player player, String cardId, Entity target) {
		playCard(context, player, cardId, target);
	}

	public static void cardRefTarget(GameContext context, Player player, Entity card, Entity target) {
		playCard(context, player, (Card) card, target);
	}

	public static int minionCard(GameContext context, Player player, String cardId) {
		return playMinionCard(context, player, cardId).getReference().getId();
	}

	public static int minionCardRef(GameContext context, Player player, Entity card) {
		return playMinionCard(context, player, (Card) card).getReference().getId();
	}

	public static int minionCardTarget(GameContext context, Player player, String cardId, Entity entity) {
		return playMinionCard(context, player, cardId, entity).getReference().getId();
	}

	public static int minionCardRefTarget(GameContext context, Player player, Entity card, Entity entity) {
		return playMinionCard(context, player, (Card) card, entity).getReference().getId();
	}

	public static int receive(GameContext context, Player player, String cardId) {
		return receiveCard(context, player, cardId).getReference().getId();
	}

}
