/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.records;


import com.hiddenswitch.framework.schema.spellsource.tables.Games;
import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IGames;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GamesRecord extends UpdatableRecordImpl<GamesRecord> implements VertxPojo, Record3<Long, String, JSONB>, IGames {

    private static final long serialVersionUID = 1997853735;

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
     * Setter for <code>spellsource.games.git_hash</code>.
     */
    @Override
    public GamesRecord setGitHash(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.git_hash</code>.
     */
    @Override
    public String getGitHash() {
        return (String) get(1);
    }

    /**
     * Setter for <code>spellsource.games.trace</code>.
     */
    @Override
    public GamesRecord setTrace(JSONB value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.games.trace</code>.
     */
    @Override
    public JSONB getTrace() {
        return (JSONB) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, String, JSONB> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, String, JSONB> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Games.GAMES.ID;
    }

    @Override
    public Field<String> field2() {
        return Games.GAMES.GIT_HASH;
    }

    @Override
    public Field<JSONB> field3() {
        return Games.GAMES.TRACE;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getGitHash();
    }

    @Override
    public JSONB component3() {
        return getTrace();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getGitHash();
    }

    @Override
    public JSONB value3() {
        return getTrace();
    }

    @Override
    public GamesRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public GamesRecord value2(String value) {
        setGitHash(value);
        return this;
    }

    @Override
    public GamesRecord value3(JSONB value) {
        setTrace(value);
        return this;
    }

    @Override
    public GamesRecord values(Long value1, String value2, JSONB value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IGames from) {
        setId(from.getId());
        setGitHash(from.getGitHash());
        setTrace(from.getTrace());
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
    public GamesRecord(Long id, String gitHash, JSONB trace) {
        super(Games.GAMES);

        set(0, id);
        set(1, gitHash);
        set(2, trace);
    }

    public GamesRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}