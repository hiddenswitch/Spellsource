/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.records;


import com.hiddenswitch.framework.schema.spellsource.tables.CardCatalogueGetClassCards;
import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.ICardCatalogueGetClassCards;

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
public class CardCatalogueGetClassCardsRecord extends TableRecordImpl<CardCatalogueGetClassCardsRecord> implements VertxPojo, Record10<String, String, String, JsonObject, JsonObject, OffsetDateTime, OffsetDateTime, Boolean, Boolean, Long>, ICardCatalogueGetClassCards {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>spellsource.card_catalogue_get_class_cards.id</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_get_class_cards.id</code>.
     */
    @Override
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.created_by</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setCreatedBy(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.created_by</code>.
     */
    @Override
    public String getCreatedBy() {
        return (String) get(1);
    }

    /**
     * Setter for <code>spellsource.card_catalogue_get_class_cards.uri</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setUri(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_get_class_cards.uri</code>.
     */
    @Override
    public String getUri() {
        return (String) get(2);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.blockly_workspace</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setBlocklyWorkspace(JsonObject value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.blockly_workspace</code>.
     */
    @Override
    public JsonObject getBlocklyWorkspace() {
        return (JsonObject) get(3);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.card_script</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setCardScript(JsonObject value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.card_script</code>.
     */
    @Override
    public JsonObject getCardScript() {
        return (JsonObject) get(4);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.created_at</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setCreatedAt(OffsetDateTime value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.created_at</code>.
     */
    @Override
    public OffsetDateTime getCreatedAt() {
        return (OffsetDateTime) get(5);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.last_modified</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setLastModified(OffsetDateTime value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.last_modified</code>.
     */
    @Override
    public OffsetDateTime getLastModified() {
        return (OffsetDateTime) get(6);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.is_archived</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setIsArchived(Boolean value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.is_archived</code>.
     */
    @Override
    public Boolean getIsArchived() {
        return (Boolean) get(7);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.is_published</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setIsPublished(Boolean value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.is_published</code>.
     */
    @Override
    public Boolean getIsPublished() {
        return (Boolean) get(8);
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_class_cards.succession</code>.
     */
    @Override
    public CardCatalogueGetClassCardsRecord setSuccession(Long value) {
        set(9, value);
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_class_cards.succession</code>.
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
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.ID;
    }

    @Override
    public Field<String> field2() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.CREATED_BY;
    }

    @Override
    public Field<String> field3() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.URI;
    }

    @Override
    public Field<JsonObject> field4() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.BLOCKLY_WORKSPACE;
    }

    @Override
    public Field<JsonObject> field5() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.CARD_SCRIPT;
    }

    @Override
    public Field<OffsetDateTime> field6() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.CREATED_AT;
    }

    @Override
    public Field<OffsetDateTime> field7() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.LAST_MODIFIED;
    }

    @Override
    public Field<Boolean> field8() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.IS_ARCHIVED;
    }

    @Override
    public Field<Boolean> field9() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.IS_PUBLISHED;
    }

    @Override
    public Field<Long> field10() {
        return CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS.SUCCESSION;
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
    public CardCatalogueGetClassCardsRecord value1(String value) {
        setId(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value2(String value) {
        setCreatedBy(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value3(String value) {
        setUri(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value4(JsonObject value) {
        setBlocklyWorkspace(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value5(JsonObject value) {
        setCardScript(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value6(OffsetDateTime value) {
        setCreatedAt(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value7(OffsetDateTime value) {
        setLastModified(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value8(Boolean value) {
        setIsArchived(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value9(Boolean value) {
        setIsPublished(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord value10(Long value) {
        setSuccession(value);
        return this;
    }

    @Override
    public CardCatalogueGetClassCardsRecord values(String value1, String value2, String value3, JsonObject value4, JsonObject value5, OffsetDateTime value6, OffsetDateTime value7, Boolean value8, Boolean value9, Long value10) {
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
    public void from(ICardCatalogueGetClassCards from) {
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
    public <E extends ICardCatalogueGetClassCards> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CardCatalogueGetClassCardsRecord
     */
    public CardCatalogueGetClassCardsRecord() {
        super(CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS);
    }

    /**
     * Create a detached, initialised CardCatalogueGetClassCardsRecord
     */
    public CardCatalogueGetClassCardsRecord(String id, String createdBy, String uri, JsonObject blocklyWorkspace, JsonObject cardScript, OffsetDateTime createdAt, OffsetDateTime lastModified, Boolean isArchived, Boolean isPublished, Long succession) {
        super(CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS);

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
     * Create a detached, initialised CardCatalogueGetClassCardsRecord
     */
    public CardCatalogueGetClassCardsRecord(com.hiddenswitch.framework.schema.spellsource.tables.pojos.CardCatalogueGetClassCards value) {
        super(CardCatalogueGetClassCards.CARD_CATALOGUE_GET_CLASS_CARDS);

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

        public CardCatalogueGetClassCardsRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}