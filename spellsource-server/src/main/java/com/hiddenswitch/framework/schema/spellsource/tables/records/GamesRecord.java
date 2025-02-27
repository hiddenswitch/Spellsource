/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.records;


import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.schema.spellsource.tables.Games;
import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IGames;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import java.time.OffsetDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GamesRecord extends UpdatableRecordImpl<GamesRecord> implements VertxPojo, Record5<Long, GameStateEnum, String, JsonObject, OffsetDateTime>, IGames {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>spellsource.games.id</code>.
     */
    @Override
    public GamesRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.id</code>.
     */
    @Override
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>spellsource.games.status</code>.
     */
    @Override
    public GamesRecord setStatus(GameStateEnum value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.status</code>.
     */
    @Override
    public GameStateEnum getStatus() {
        return (GameStateEnum) get(1);
    }

    /**
     * Setter for <code>spellsource.games.git_hash</code>.
     */
    @Override
    public GamesRecord setGitHash(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.git_hash</code>.
     */
    @Override
    public String getGitHash() {
        return (String) get(2);
    }

    /**
     * Setter for <code>spellsource.games.trace</code>.
     */
    @Override
    public GamesRecord setTrace(JsonObject value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.trace</code>.
     */
    @Override
    public JsonObject getTrace() {
        return (JsonObject) get(3);
    }

    /**
     * Setter for <code>spellsource.games.created_at</code>.
     */
    @Override
    public GamesRecord setCreatedAt(OffsetDateTime value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.created_at</code>.
     */
    @Override
    public OffsetDateTime getCreatedAt() {
        return (OffsetDateTime) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<Long, GameStateEnum, String, JsonObject, OffsetDateTime> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<Long, GameStateEnum, String, JsonObject, OffsetDateTime> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Games.GAMES.ID;
    }

    @Override
    public Field<GameStateEnum> field2() {
        return Games.GAMES.STATUS;
    }

    @Override
    public Field<String> field3() {
        return Games.GAMES.GIT_HASH;
    }

    @Override
    public Field<JsonObject> field4() {
        return Games.GAMES.TRACE;
    }

    @Override
    public Field<OffsetDateTime> field5() {
        return Games.GAMES.CREATED_AT;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public GameStateEnum component2() {
        return getStatus();
    }

    @Override
    public String component3() {
        return getGitHash();
    }

    @Override
    public JsonObject component4() {
        return getTrace();
    }

    @Override
    public OffsetDateTime component5() {
        return getCreatedAt();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public GameStateEnum value2() {
        return getStatus();
    }

    @Override
    public String value3() {
        return getGitHash();
    }

    @Override
    public JsonObject value4() {
        return getTrace();
    }

    @Override
    public OffsetDateTime value5() {
        return getCreatedAt();
    }

    @Override
    public GamesRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public GamesRecord value2(GameStateEnum value) {
        setStatus(value);
        return this;
    }

    @Override
    public GamesRecord value3(String value) {
        setGitHash(value);
        return this;
    }

    @Override
    public GamesRecord value4(JsonObject value) {
        setTrace(value);
        return this;
    }

    @Override
    public GamesRecord value5(OffsetDateTime value) {
        setCreatedAt(value);
        return this;
    }

    @Override
    public GamesRecord values(Long value1, GameStateEnum value2, String value3, JsonObject value4, OffsetDateTime value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IGames from) {
        setId(from.getId());
        setStatus(from.getStatus());
        setGitHash(from.getGitHash());
        setTrace(from.getTrace());
        setCreatedAt(from.getCreatedAt());
    }

    @Override
    public <E extends IGames> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GamesRecord
     */
    public GamesRecord() {
        super(Games.GAMES);
    }

    /**
     * Create a detached, initialised GamesRecord
     */
    public GamesRecord(Long id, GameStateEnum status, String gitHash, JsonObject trace, OffsetDateTime createdAt) {
        super(Games.GAMES);

        setId(id);
        setStatus(status);
        setGitHash(gitHash);
        setTrace(trace);
        setCreatedAt(createdAt);
    }

    /**
     * Create a detached, initialised GamesRecord
     */
    public GamesRecord(com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games value) {
        super(Games.GAMES);

        if (value != null) {
            setId(value.getId());
            setStatus(value.getStatus());
            setGitHash(value.getGitHash());
            setTrace(value.getTrace());
            setCreatedAt(value.getCreatedAt());
        }
    }

        public GamesRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
