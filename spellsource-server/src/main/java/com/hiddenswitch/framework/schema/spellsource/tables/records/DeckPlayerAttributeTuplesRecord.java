/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.records;


import com.hiddenswitch.framework.schema.spellsource.tables.DeckPlayerAttributeTuples;
import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IDeckPlayerAttributeTuples;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DeckPlayerAttributeTuplesRecord extends UpdatableRecordImpl<DeckPlayerAttributeTuplesRecord> implements VertxPojo, Record4<Long, String, Integer, String>, IDeckPlayerAttributeTuples {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>spellsource.deck_player_attribute_tuples.id</code>.
     */
    @Override
    public DeckPlayerAttributeTuplesRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.deck_player_attribute_tuples.id</code>.
     */
    @Override
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>spellsource.deck_player_attribute_tuples.deck_id</code>.
     */
    @Override
    public DeckPlayerAttributeTuplesRecord setDeckId(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.deck_player_attribute_tuples.deck_id</code>.
     */
    @Override
    public String getDeckId() {
        return (String) get(1);
    }

    /**
     * Setter for
     * <code>spellsource.deck_player_attribute_tuples.attribute</code>.
     */
    @Override
    public DeckPlayerAttributeTuplesRecord setAttribute(Integer value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.deck_player_attribute_tuples.attribute</code>.
     */
    @Override
    public Integer getAttribute() {
        return (Integer) get(2);
    }

    /**
     * Setter for
     * <code>spellsource.deck_player_attribute_tuples.string_value</code>.
     */
    @Override
    public DeckPlayerAttributeTuplesRecord setStringValue(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.deck_player_attribute_tuples.string_value</code>.
     */
    @Override
    public String getStringValue() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, String, Integer, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Long, String, Integer, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES.ID;
    }

    @Override
    public Field<String> field2() {
        return DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES.DECK_ID;
    }

    @Override
    public Field<Integer> field3() {
        return DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES.ATTRIBUTE;
    }

    @Override
    public Field<String> field4() {
        return DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES.STRING_VALUE;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getDeckId();
    }

    @Override
    public Integer component3() {
        return getAttribute();
    }

    @Override
    public String component4() {
        return getStringValue();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getDeckId();
    }

    @Override
    public Integer value3() {
        return getAttribute();
    }

    @Override
    public String value4() {
        return getStringValue();
    }

    @Override
    public DeckPlayerAttributeTuplesRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public DeckPlayerAttributeTuplesRecord value2(String value) {
        setDeckId(value);
        return this;
    }

    @Override
    public DeckPlayerAttributeTuplesRecord value3(Integer value) {
        setAttribute(value);
        return this;
    }

    @Override
    public DeckPlayerAttributeTuplesRecord value4(String value) {
        setStringValue(value);
        return this;
    }

    @Override
    public DeckPlayerAttributeTuplesRecord values(Long value1, String value2, Integer value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IDeckPlayerAttributeTuples from) {
        setId(from.getId());
        setDeckId(from.getDeckId());
        setAttribute(from.getAttribute());
        setStringValue(from.getStringValue());
    }

    @Override
    public <E extends IDeckPlayerAttributeTuples> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached DeckPlayerAttributeTuplesRecord
     */
    public DeckPlayerAttributeTuplesRecord() {
        super(DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES);
    }

    /**
     * Create a detached, initialised DeckPlayerAttributeTuplesRecord
     */
    public DeckPlayerAttributeTuplesRecord(Long id, String deckId, Integer attribute, String stringValue) {
        super(DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES);

        setId(id);
        setDeckId(deckId);
        setAttribute(attribute);
        setStringValue(stringValue);
    }

    /**
     * Create a detached, initialised DeckPlayerAttributeTuplesRecord
     */
    public DeckPlayerAttributeTuplesRecord(com.hiddenswitch.framework.schema.spellsource.tables.pojos.DeckPlayerAttributeTuples value) {
        super(DeckPlayerAttributeTuples.DECK_PLAYER_ATTRIBUTE_TUPLES);

        if (value != null) {
            setId(value.getId());
            setDeckId(value.getDeckId());
            setAttribute(value.getAttribute());
            setStringValue(value.getStringValue());
        }
    }

        public DeckPlayerAttributeTuplesRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
