/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.time.OffsetDateTime;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IGetCollectionCards extends VertxPojo, Serializable {

    /**
     * Setter for <code>spellsource.get_collection_cards.id</code>.
     */
    public IGetCollectionCards setId(String value);

    /**
     * Getter for <code>spellsource.get_collection_cards.id</code>.
     */
    public String getId();

    /**
     * Setter for <code>spellsource.get_collection_cards.created_by</code>.
     */
    public IGetCollectionCards setCreatedBy(String value);

    /**
     * Getter for <code>spellsource.get_collection_cards.created_by</code>.
     */
    public String getCreatedBy();

    /**
     * Setter for <code>spellsource.get_collection_cards.card_script</code>.
     */
    public IGetCollectionCards setCardScript(JsonObject value);

    /**
     * Getter for <code>spellsource.get_collection_cards.card_script</code>.
     */
    public JsonObject getCardScript();

    /**
     * Setter for
     * <code>spellsource.get_collection_cards.blockly_workspace</code>.
     */
    public IGetCollectionCards setBlocklyWorkspace(JsonObject value);

    /**
     * Getter for
     * <code>spellsource.get_collection_cards.blockly_workspace</code>.
     */
    public JsonObject getBlocklyWorkspace();

    /**
     * Setter for <code>spellsource.get_collection_cards.name</code>.
     */
    public IGetCollectionCards setName(String value);

    /**
     * Getter for <code>spellsource.get_collection_cards.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>spellsource.get_collection_cards.type</code>.
     */
    public IGetCollectionCards setType(String value);

    /**
     * Getter for <code>spellsource.get_collection_cards.type</code>.
     */
    public String getType();

    /**
     * Setter for <code>spellsource.get_collection_cards.class</code>.
     */
    public IGetCollectionCards setClass_(String value);

    /**
     * Getter for <code>spellsource.get_collection_cards.class</code>.
     */
    public String getClass_();

    /**
     * Setter for <code>spellsource.get_collection_cards.cost</code>.
     */
    public IGetCollectionCards setCost(Integer value);

    /**
     * Getter for <code>spellsource.get_collection_cards.cost</code>.
     */
    public Integer getCost();

    /**
     * Setter for <code>spellsource.get_collection_cards.collectible</code>.
     */
    public IGetCollectionCards setCollectible(Boolean value);

    /**
     * Getter for <code>spellsource.get_collection_cards.collectible</code>.
     */
    public Boolean getCollectible();

    /**
     * Setter for <code>spellsource.get_collection_cards.search_message</code>.
     */
    public IGetCollectionCards setSearchMessage(String value);

    /**
     * Getter for <code>spellsource.get_collection_cards.search_message</code>.
     */
    public String getSearchMessage();

    /**
     * Setter for <code>spellsource.get_collection_cards.last_modified</code>.
     */
    public IGetCollectionCards setLastModified(OffsetDateTime value);

    /**
     * Getter for <code>spellsource.get_collection_cards.last_modified</code>.
     */
    public OffsetDateTime getLastModified();

    /**
     * Setter for <code>spellsource.get_collection_cards.created_at</code>.
     */
    public IGetCollectionCards setCreatedAt(OffsetDateTime value);

    /**
     * Getter for <code>spellsource.get_collection_cards.created_at</code>.
     */
    public OffsetDateTime getCreatedAt();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IGetCollectionCards
     */
    public void from(IGetCollectionCards from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IGetCollectionCards
     */
    public <E extends IGetCollectionCards> E into(E into);

        @Override
        public default IGetCollectionCards fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setId,json::getString,"id","java.lang.String");
                setOrThrow(this::setCreatedBy,json::getString,"created_by","java.lang.String");
                setCardScript(com.hiddenswitch.framework.schema.spellsource.tables.converters.Converters.IO_GITHUB_JKLINGSPORN_VERTX_JOOQ_SHARED_POSTGRES_JSONBTOJSONOBJECTCONVERTER_INSTANCE.pgConverter().from(json.getJsonObject("card_script")));
                setBlocklyWorkspace(com.hiddenswitch.framework.schema.spellsource.tables.converters.Converters.IO_GITHUB_JKLINGSPORN_VERTX_JOOQ_SHARED_POSTGRES_JSONBTOJSONOBJECTCONVERTER_INSTANCE.pgConverter().from(json.getJsonObject("blockly_workspace")));
                setOrThrow(this::setName,json::getString,"name","java.lang.String");
                setOrThrow(this::setType,json::getString,"type","java.lang.String");
                setOrThrow(this::setClass_,json::getString,"class","java.lang.String");
                setOrThrow(this::setCost,json::getInteger,"cost","java.lang.Integer");
                setOrThrow(this::setCollectible,json::getBoolean,"collectible","java.lang.Boolean");
                setOrThrow(this::setSearchMessage,json::getString,"search_message","java.lang.String");
                setOrThrow(this::setLastModified,key -> {String s = json.getString(key); return s==null?null:java.time.OffsetDateTime.parse(s);},"last_modified","java.time.OffsetDateTime");
                setOrThrow(this::setCreatedAt,key -> {String s = json.getString(key); return s==null?null:java.time.OffsetDateTime.parse(s);},"created_at","java.time.OffsetDateTime");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("id",getId());
                json.put("created_by",getCreatedBy());
                json.put("card_script",com.hiddenswitch.framework.schema.spellsource.tables.converters.Converters.IO_GITHUB_JKLINGSPORN_VERTX_JOOQ_SHARED_POSTGRES_JSONBTOJSONOBJECTCONVERTER_INSTANCE.pgConverter().to(getCardScript()));
                json.put("blockly_workspace",com.hiddenswitch.framework.schema.spellsource.tables.converters.Converters.IO_GITHUB_JKLINGSPORN_VERTX_JOOQ_SHARED_POSTGRES_JSONBTOJSONOBJECTCONVERTER_INSTANCE.pgConverter().to(getBlocklyWorkspace()));
                json.put("name",getName());
                json.put("type",getType());
                json.put("class",getClass_());
                json.put("cost",getCost());
                json.put("collectible",getCollectible());
                json.put("search_message",getSearchMessage());
                json.put("last_modified",getLastModified()==null?null:getLastModified().toString());
                json.put("created_at",getCreatedAt()==null?null:getCreatedAt().toString());
                return json;
        }

}