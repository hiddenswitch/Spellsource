package com.hiddenswitch.framework.schema.spellsource.tables.mappers;

import io.vertx.sqlclient.Row;
import java.util.function.Function;

public class RowMappers {

    private RowMappers(){}

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.BotUsers> getBotUsersMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.BotUsers pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.BotUsers();
            pojo.setId(row.getString("id"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards> getCardsMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards();
            pojo.setId(row.getString("id"));
            pojo.setCreatedBy(row.getString("created_by"));
            pojo.setUri(row.getString("uri"));
            // Omitting unrecognized type DataType [ t=xml; p=0; s=0; u="pg_catalog"."xml"; j=org.w3c.dom.Element ] (org.w3c.dom.Element) for column blockly_workspace!
            try {
                if (row.get(Object.class, "card_script") instanceof io.vertx.core.json.JsonObject) {
                    pojo.setCardScript(row.get(io.vertx.core.json.JsonObject.class,row.getColumnIndex("card_script")));
                } else {
                    String card_scriptString = row.getString("card_script");
                    pojo.setCardScript(card_scriptString == null ? null : new io.vertx.core.json.JsonObject(card_scriptString));
                }
            } catch (UnsupportedOperationException t) {
                String card_scriptString = row.getString("card_script");
                pojo.setCardScript(card_scriptString == null ? null : new io.vertx.core.json.JsonObject(card_scriptString));
            }
            pojo.setCreatedAt(row.getOffsetDateTime("created_at"));
            pojo.setLastModified(row.getOffsetDateTime("last_modified"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.CardsInDeck> getCardsInDeckMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.CardsInDeck pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.CardsInDeck();
            pojo.setId(row.getLong("id"));
            pojo.setDeckId(row.getString("deck_id"));
            pojo.setCardId(row.getString("card_id"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckPlayerAttributeTuples> getDeckPlayerAttributeTuplesMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckPlayerAttributeTuples pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckPlayerAttributeTuples();
            pojo.setId(row.getLong("id"));
            pojo.setDeckId(row.getString("deck_id"));
            pojo.setAttribute(row.getInteger("attribute"));
            pojo.setStringValue(row.getString("string_value"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckShares> getDeckSharesMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckShares pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckShares();
            pojo.setId(row.getLong("id"));
            pojo.setDeckId(row.getString("deck_id"));
            pojo.setShareRecipientId(row.getString("share_recipient_id"));
            pojo.setTrashed(row.getBoolean("trashed"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.Decks> getDecksMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.Decks pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.Decks();
            pojo.setId(row.getString("id"));
            pojo.setCreatedBy(row.getString("created_by"));
            pojo.setLastEditedBy(row.getString("last_edited_by"));
            pojo.setName(row.getString("name"));
            pojo.setHeroClass(row.getString("hero_class"));
            pojo.setTrashed(row.getBoolean("trashed"));
            pojo.setFormat(row.getString("format"));
            pojo.setDeckType(row.getInteger("deck_type"));
            pojo.setIsPremade(row.getBoolean("is_premade"));
            pojo.setPermittedToDuplicate(row.getBoolean("permitted_to_duplicate"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.GameUsers> getGameUsersMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.GameUsers pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.GameUsers();
            pojo.setPlayerIndex(row.getShort("player_index"));
            pojo.setGameId(row.getLong("game_id"));
            pojo.setUserId(row.getString("user_id"));
            pojo.setDeckId(row.getString("deck_id"));
            pojo.setVictoryStatus(java.util.Arrays.stream(com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum.values()).filter(td -> td.getLiteral().equals(row.getString("victory_status"))).findFirst().orElse(null));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games> getGamesMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games();
            pojo.setId(row.getLong("id"));
            pojo.setStatus(java.util.Arrays.stream(com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum.values()).filter(td -> td.getLiteral().equals(row.getString("status"))).findFirst().orElse(null));
            pojo.setGitHash(row.getString("git_hash"));
            try {
                if (row.get(Object.class, "trace") instanceof io.vertx.core.json.JsonObject) {
                    pojo.setTrace(row.get(io.vertx.core.json.JsonObject.class,row.getColumnIndex("trace")));
                } else {
                    String traceString = row.getString("trace");
                    pojo.setTrace(traceString == null ? null : new io.vertx.core.json.JsonObject(traceString));
                }
            } catch (UnsupportedOperationException t) {
                String traceString = row.getString("trace");
                pojo.setTrace(traceString == null ? null : new io.vertx.core.json.JsonObject(traceString));
            }
            pojo.setCreatedAt(row.getOffsetDateTime("created_at"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues> getMatchmakingQueuesMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues();
            pojo.setId(row.getString("id"));
            pojo.setName(row.getString("name"));
            pojo.setBotOpponent(row.getBoolean("bot_opponent"));
            pojo.setPrivateLobby(row.getBoolean("private_lobby"));
            pojo.setStartsAutomatically(row.getBoolean("starts_automatically"));
            pojo.setStillConnectedTimeout(row.getLong("still_connected_timeout"));
            pojo.setEmptyLobbyTimeout(row.getLong("empty_lobby_timeout"));
            pojo.setAwaitingLobbyTimeout(row.getLong("awaiting_lobby_timeout"));
            pojo.setOnce(row.getBoolean("once"));
            pojo.setAutomaticallyClose(row.getBoolean("automatically_close"));
            pojo.setLobbySize(row.getInteger("lobby_size"));
            pojo.setQueueCreatedAt(row.getOffsetDateTime("queue_created_at"));
            return pojo;
        };
    }

    public static Function<Row,com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets> getMatchmakingTicketsMapper() {
        return row -> {
            com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets pojo = new com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets();
            pojo.setId(row.getLong("id"));
            pojo.setQueueId(row.getString("queue_id"));
            pojo.setUserId(row.getString("user_id"));
            pojo.setDeckId(row.getString("deck_id"));
            pojo.setBotDeckId(row.getString("bot_deck_id"));
            pojo.setCreatedAt(row.getOffsetDateTime("created_at"));
            return pojo;
        };
    }

}
