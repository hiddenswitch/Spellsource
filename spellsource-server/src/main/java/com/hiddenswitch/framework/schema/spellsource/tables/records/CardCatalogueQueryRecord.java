/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.records;


import com.hiddenswitch.framework.schema.spellsource.tables.CardCatalogueQuery;
import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.ICardCatalogueQuery;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import java.time.OffsetDateTime;

import org.jooq.Field;
import org.jooq.Record10;
import org.jooq.Row10;
import org.jooq.impl.TableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CardCatalogueQueryRecord extends TableRecordImpl<CardCatalogueQueryRecord> implements VertxPojo, Record10<String, String, String, JsonObject, JsonObject, OffsetDateTime, OffsetDateTime, Boolean, Boolean, Long>, ICardCatalogueQuery {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>spellsource.card_catalogue_query.id</code>.
     */
    @Override
    public CardCatalogueQueryRecord setId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.id</code>.
     */
    @Override
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.created_by</code>.
     */
    @Override
    public CardCatalogueQueryRecord setCreatedBy(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.created_by</code>.
     */
    @Override
    public String getCreatedBy() {
        return (String) get(1);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.uri</code>.
     */
    @Override
    public CardCatalogueQueryRecord setUri(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.uri</code>.
     */
    @Override
    public String getUri() {
        return (String) get(2);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_query.blockly_workspace</code>.
     */
    @Override
    public CardCatalogueQueryRecord setBlocklyWorkspace(JsonObject value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_query.blockly_workspace</code>.
     */
    @Override
    public JsonObject getBlocklyWorkspace() {
        return (JsonObject) get(3);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.card_script</code>.
     */
    @Override
    public CardCatalogueQueryRecord setCardScript(JsonObject value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.card_script</code>.
     */
    @Override
    public JsonObject getCardScript() {
        return (JsonObject) get(4);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.created_at</code>.
     */
    @Override
    public CardCatalogueQueryRecord setCreatedAt(OffsetDateTime value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.created_at</code>.
     */
    @Override
    public OffsetDateTime getCreatedAt() {
        return (OffsetDateTime) get(5);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.last_modified</code>.
     */
    @Override
    public CardCatalogueQueryRecord setLastModified(OffsetDateTime value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.last_modified</code>.
     */
    @Override
    public OffsetDateTime getLastModified() {
        return (OffsetDateTime) get(6);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.is_archived</code>.
     */
    @Override
    public CardCatalogueQueryRecord setIsArchived(Boolean value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.is_archived</code>.
     */
    @Override
    public Boolean getIsArchived() {
        return (Boolean) get(7);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.is_published</code>.
     */
    @Override
    public CardCatalogueQueryRecord setIsPublished(Boolean value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.is_published</code>.
     */
    @Override
    public Boolean getIsPublished() {
        return (Boolean) get(8);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_query.succession</code>.
     */
    @Override
    public CardCatalogueQueryRecord setSuccession(Long value) {
        set(9, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_query.succession</code>.
     */
    @Override
    public Long getSuccession() {
        return (Long) get(9);
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row10<String, String, String, JsonObject, JsonObject, OffsetDateTime, OffsetDateTime, Boolean, Boolean, Long> fieldsRow() {
        return (Row10) super.fieldsRow();
    }

    @Override
    public Row10<String, String, String, JsonObject, JsonObject, OffsetDateTime, OffsetDateTime, Boolean, Boolean, Long> valuesRow() {
        return (Row10) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.ID;
    }

    @Override
    public Field<String> field2() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.CREATED_BY;
    }

    @Override
    public Field<String> field3() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.URI;
    }

    @Override
    public Field<JsonObject> field4() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.BLOCKLY_WORKSPACE;
    }

    @Override
    public Field<JsonObject> field5() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.CARD_SCRIPT;
    }

    @Override
    public Field<OffsetDateTime> field6() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.CREATED_AT;
    }

    @Override
    public Field<OffsetDateTime> field7() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.LAST_MODIFIED;
    }

    @Override
    public Field<Boolean> field8() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.IS_ARCHIVED;
    }

    @Override
    public Field<Boolean> field9() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.IS_PUBLISHED;
    }

    @Override
    public Field<Long> field10() {
        return CardCatalogueQuery.CARD_CATALOGUE_QUERY.SUCCESSION;
    }

    @Override
    public String component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getCreatedBy();
    }

    @Override
    public String component3() {
        return getUri();
    }

    @Override
    public JsonObject component4() {
        return getBlocklyWorkspace();
    }

    @Override
    public JsonObject component5() {
        return getCardScript();
    }

    @Override
    public OffsetDateTime component6() {
        return getCreatedAt();
    }

    @Override
    public OffsetDateTime component7() {
        return getLastModified();
    }

    @Override
    public Boolean component8() {
        return getIsArchived();
    }

    @Override
    public Boolean component9() {
        return getIsPublished();
    }

    @Override
    public Long component10() {
        return getSuccession();
    }

    @Override
    public String value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getCreatedBy();
    }

    @Override
    public String value3() {
        return getUri();
    }

    @Override
    public JsonObject value4() {
        return getBlocklyWorkspace();
    }

    @Override
    public JsonObject value5() {
        return getCardScript();
    }

    @Override
    public OffsetDateTime value6() {
        return getCreatedAt();
    }

    @Override
    public OffsetDateTime value7() {
        return getLastModified();
    }

    @Override
    public Boolean value8() {
        return getIsArchived();
    }

    @Override
    public Boolean value9() {
        return getIsPublished();
    }

    @Override
    public Long value10() {
        return getSuccession();
    }

    @Override
    public CardCatalogueQueryRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value2(String value) {
        setCreatedBy(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value3(String value) {
        setUri(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value4(JsonObject value) {
        setBlocklyWorkspace(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value5(JsonObject value) {
        setCardScript(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value6(OffsetDateTime value) {
        setCreatedAt(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value7(OffsetDateTime value) {
        setLastModified(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value8(Boolean value) {
        setIsArchived(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value9(Boolean value) {
        setIsPublished(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord value10(Long value) {
        setSuccession(value);
        return this;
    }

    @Override
    public CardCatalogueQueryRecord values(String value1, String value2, String value3, JsonObject value4, JsonObject value5, OffsetDateTime value6, OffsetDateTime value7, Boolean value8, Boolean value9, Long value10) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ICardCatalogueQuery from) {
        setId(from.getId());
        setCreatedBy(from.getCreatedBy());
        setUri(from.getUri());
        setBlocklyWorkspace(from.getBlocklyWorkspace());
        setCardScript(from.getCardScript());
        setCreatedAt(from.getCreatedAt());
        setLastModified(from.getLastModified());
        setIsArchived(from.getIsArchived());
        setIsPublished(from.getIsPublished());
        setSuccession(from.getSuccession());
    }

    @Override
    public <E extends ICardCatalogueQuery> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CardCatalogueQueryRecord
     */
    public CardCatalogueQueryRecord() {
        super(CardCatalogueQuery.CARD_CATALOGUE_QUERY);
    }

    /**
     * Create a detached, initialised CardCatalogueQueryRecord
     */
    public CardCatalogueQueryRecord(String id, String createdBy, String uri, JsonObject blocklyWorkspace, JsonObject cardScript, OffsetDateTime createdAt, OffsetDateTime lastModified, Boolean isArchived, Boolean isPublished, Long succession) {
        super(CardCatalogueQuery.CARD_CATALOGUE_QUERY);

        setId(id);
        setCreatedBy(createdBy);
        setUri(uri);
        setBlocklyWorkspace(blocklyWorkspace);
        setCardScript(cardScript);
        setCreatedAt(createdAt);
        setLastModified(lastModified);
        setIsArchived(isArchived);
        setIsPublished(isPublished);
        setSuccession(succession);
    }

    /**
     * Create a detached, initialised CardCatalogueQueryRecord
     */
    public CardCatalogueQueryRecord(com.hiddenswitch.framework.schema.spellsource.tables.pojos.CardCatalogueQuery value) {
        super(CardCatalogueQuery.CARD_CATALOGUE_QUERY);

        if (value != null) {
            setId(value.getId());
            setCreatedBy(value.getCreatedBy());
            setUri(value.getUri());
            setBlocklyWorkspace(value.getBlocklyWorkspace());
            setCardScript(value.getCardScript());
            setCreatedAt(value.getCreatedAt());
            setLastModified(value.getLastModified());
            setIsArchived(value.getIsArchived());
            setIsPublished(value.getIsPublished());
            setSuccession(value.getSuccession());
        }
    }

        public CardCatalogueQueryRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}