/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables;


import com.hiddenswitch.framework.schema.spellsource.Keys;
import com.hiddenswitch.framework.schema.spellsource.Spellsource;
import com.hiddenswitch.framework.schema.spellsource.tables.records.BannedDraftCardsRecord;

import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function1;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row1;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BannedDraftCards extends TableImpl<BannedDraftCardsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>spellsource.banned_draft_cards</code>
     */
    public static final BannedDraftCards BANNED_DRAFT_CARDS = new BannedDraftCards();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BannedDraftCardsRecord> getRecordType() {
        return BannedDraftCardsRecord.class;
    }

    /**
     * The column <code>spellsource.banned_draft_cards.card_id</code>.
     */
    public final TableField<BannedDraftCardsRecord, String> CARD_ID = createField(DSL.name("card_id"), SQLDataType.CLOB.nullable(false), this, "");

    private BannedDraftCards(Name alias, Table<BannedDraftCardsRecord> aliased) {
        this(alias, aliased, null);
    }

    private BannedDraftCards(Name alias, Table<BannedDraftCardsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>spellsource.banned_draft_cards</code> table
     * reference
     */
    public BannedDraftCards(String alias) {
        this(DSL.name(alias), BANNED_DRAFT_CARDS);
    }

    /**
     * Create an aliased <code>spellsource.banned_draft_cards</code> table
     * reference
     */
    public BannedDraftCards(Name alias) {
        this(alias, BANNED_DRAFT_CARDS);
    }

    /**
     * Create a <code>spellsource.banned_draft_cards</code> table reference
     */
    public BannedDraftCards() {
        this(DSL.name("banned_draft_cards"), null);
    }

    public <O extends Record> BannedDraftCards(Table<O> child, ForeignKey<O, BannedDraftCardsRecord> key) {
        super(child, key, BANNED_DRAFT_CARDS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Spellsource.SPELLSOURCE;
    }

    @Override
    public UniqueKey<BannedDraftCardsRecord> getPrimaryKey() {
        return Keys.BANNED_DRAFT_CARDS_PKEY;
    }

    @Override
    public BannedDraftCards as(String alias) {
        return new BannedDraftCards(DSL.name(alias), this);
    }

    @Override
    public BannedDraftCards as(Name alias) {
        return new BannedDraftCards(alias, this);
    }

    @Override
    public BannedDraftCards as(Table<?> alias) {
        return new BannedDraftCards(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public BannedDraftCards rename(String name) {
        return new BannedDraftCards(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BannedDraftCards rename(Name name) {
        return new BannedDraftCards(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public BannedDraftCards rename(Table<?> name) {
        return new BannedDraftCards(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row1<String> fieldsRow() {
        return (Row1) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function1<? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function1<? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}