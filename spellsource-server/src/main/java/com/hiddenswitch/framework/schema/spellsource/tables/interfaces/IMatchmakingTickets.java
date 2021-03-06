/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.UnexpectedJsonValueType;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;
import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IMatchmakingTickets extends VertxPojo, Serializable {

    /**
     * Setter for <code>spellsource.matchmaking_tickets.id</code>.
     */
    public IMatchmakingTickets setId(Long value);

    /**
     * Getter for <code>spellsource.matchmaking_tickets.id</code>.
     */
    public Long getId();

    /**
     * Setter for <code>spellsource.matchmaking_tickets.queue_id</code>.
     */
    public IMatchmakingTickets setQueueId(String value);

    /**
     * Getter for <code>spellsource.matchmaking_tickets.queue_id</code>.
     */
    public String getQueueId();

    /**
     * Setter for <code>spellsource.matchmaking_tickets.user_id</code>.
     */
    public IMatchmakingTickets setUserId(String value);

    /**
     * Getter for <code>spellsource.matchmaking_tickets.user_id</code>.
     */
    public String getUserId();

    /**
     * Setter for <code>spellsource.matchmaking_tickets.deck_id</code>.
     */
    public IMatchmakingTickets setDeckId(String value);

    /**
     * Getter for <code>spellsource.matchmaking_tickets.deck_id</code>.
     */
    public String getDeckId();

    /**
     * Setter for <code>spellsource.matchmaking_tickets.bot_deck_id</code>.
     */
    public IMatchmakingTickets setBotDeckId(String value);

    /**
     * Getter for <code>spellsource.matchmaking_tickets.bot_deck_id</code>.
     */
    public String getBotDeckId();

    /**
     * Setter for <code>spellsource.matchmaking_tickets.created_at</code>.
     */
    public IMatchmakingTickets setCreatedAt(OffsetDateTime value);

    /**
     * Getter for <code>spellsource.matchmaking_tickets.created_at</code>.
     */
    public OffsetDateTime getCreatedAt();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IMatchmakingTickets
     */
    public void from(IMatchmakingTickets from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IMatchmakingTickets
     */
    public <E extends IMatchmakingTickets> E into(E into);

    @Override
    public default IMatchmakingTickets fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setId(json.getLong("id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("id","java.lang.Long",e);
        }
        try {
            setQueueId(json.getString("queue_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("queue_id","java.lang.String",e);
        }
        try {
            setUserId(json.getString("user_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("user_id","java.lang.String",e);
        }
        try {
            setDeckId(json.getString("deck_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("deck_id","java.lang.String",e);
        }
        try {
            setBotDeckId(json.getString("bot_deck_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("bot_deck_id","java.lang.String",e);
        }
        try {
            String created_atString = json.getString("created_at");
            setCreatedAt(created_atString == null?null:java.time.OffsetDateTime.parse(created_atString));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("created_at","java.time.OffsetDateTime",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("id",getId());
        json.put("queue_id",getQueueId());
        json.put("user_id",getUserId());
        json.put("deck_id",getDeckId());
        json.put("bot_deck_id",getBotDeckId());
        json.put("created_at",getCreatedAt()==null?null:getCreatedAt().toString());
        return json;
    }

}
