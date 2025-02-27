/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IGetCollectionCards;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import java.time.OffsetDateTime;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetCollectionCards implements VertxPojo, IGetCollectionCards {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private JsonObject cardScript;
    private JsonObject blocklyWorkspace;
    private String name;
    private String type;
    private String class_;
    private Integer cost;
    private Boolean collectible;
    private String searchMessage;
    private OffsetDateTime lastModified;
    private OffsetDateTime createdAt;

    public GetCollectionCards() {}

    public GetCollectionCards(IGetCollectionCards value) {
        this.id = value.getId();
        this.createdBy = value.getCreatedBy();
        this.cardScript = value.getCardScript();
        this.blocklyWorkspace = value.getBlocklyWorkspace();
        this.name = value.getName();
        this.type = value.getType();
        this.class_ = value.getClass_();
        this.cost = value.getCost();
        this.collectible = value.getCollectible();
        this.searchMessage = value.getSearchMessage();
        this.lastModified = value.getLastModified();
        this.createdAt = value.getCreatedAt();
    }

    public GetCollectionCards(
        String id,
        String createdBy,
        JsonObject cardScript,
        JsonObject blocklyWorkspace,
        String name,
        String type,
        String class_,
        Integer cost,
        Boolean collectible,
        String searchMessage,
        OffsetDateTime lastModified,
        OffsetDateTime createdAt
    ) {
        this.id = id;
        this.createdBy = createdBy;
        this.cardScript = cardScript;
        this.blocklyWorkspace = blocklyWorkspace;
        this.name = name;
        this.type = type;
        this.class_ = class_;
        this.cost = cost;
        this.collectible = collectible;
        this.searchMessage = searchMessage;
        this.lastModified = lastModified;
        this.createdAt = createdAt;
    }

        public GetCollectionCards(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>spellsource.get_collection_cards.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.id</code>.
     */
    @Override
    public GetCollectionCards setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.created_by</code>.
     */
    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.created_by</code>.
     */
    @Override
    public GetCollectionCards setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.card_script</code>.
     */
    @Override
    public JsonObject getCardScript() {
        return this.cardScript;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.card_script</code>.
     */
    @Override
    public GetCollectionCards setCardScript(JsonObject cardScript) {
        this.cardScript = cardScript;
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.get_collection_cards.blockly_workspace</code>.
     */
    @Override
    public JsonObject getBlocklyWorkspace() {
        return this.blocklyWorkspace;
    }

    /**
     * Setter for
     * <code>spellsource.get_collection_cards.blockly_workspace</code>.
     */
    @Override
    public GetCollectionCards setBlocklyWorkspace(JsonObject blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.name</code>.
     */
    @Override
    public GetCollectionCards setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.type</code>.
     */
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.type</code>.
     */
    @Override
    public GetCollectionCards setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.class</code>.
     */
    @Override
    public String getClass_() {
        return this.class_;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.class</code>.
     */
    @Override
    public GetCollectionCards setClass_(String class_) {
        this.class_ = class_;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.cost</code>.
     */
    @Override
    public Integer getCost() {
        return this.cost;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.cost</code>.
     */
    @Override
    public GetCollectionCards setCost(Integer cost) {
        this.cost = cost;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.collectible</code>.
     */
    @Override
    public Boolean getCollectible() {
        return this.collectible;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.collectible</code>.
     */
    @Override
    public GetCollectionCards setCollectible(Boolean collectible) {
        this.collectible = collectible;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.search_message</code>.
     */
    @Override
    public String getSearchMessage() {
        return this.searchMessage;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.search_message</code>.
     */
    @Override
    public GetCollectionCards setSearchMessage(String searchMessage) {
        this.searchMessage = searchMessage;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.last_modified</code>.
     */
    @Override
    public OffsetDateTime getLastModified() {
        return this.lastModified;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.last_modified</code>.
     */
    @Override
    public GetCollectionCards setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_collection_cards.created_at</code>.
     */
    @Override
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Setter for <code>spellsource.get_collection_cards.created_at</code>.
     */
    @Override
    public GetCollectionCards setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GetCollectionCards other = (GetCollectionCards) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.createdBy == null) {
            if (other.createdBy != null)
                return false;
        }
        else if (!this.createdBy.equals(other.createdBy))
            return false;
        if (this.cardScript == null) {
            if (other.cardScript != null)
                return false;
        }
        else if (!this.cardScript.equals(other.cardScript))
            return false;
        if (this.blocklyWorkspace == null) {
            if (other.blocklyWorkspace != null)
                return false;
        }
        else if (!this.blocklyWorkspace.equals(other.blocklyWorkspace))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.type == null) {
            if (other.type != null)
                return false;
        }
        else if (!this.type.equals(other.type))
            return false;
        if (this.class_ == null) {
            if (other.class_ != null)
                return false;
        }
        else if (!this.class_.equals(other.class_))
            return false;
        if (this.cost == null) {
            if (other.cost != null)
                return false;
        }
        else if (!this.cost.equals(other.cost))
            return false;
        if (this.collectible == null) {
            if (other.collectible != null)
                return false;
        }
        else if (!this.collectible.equals(other.collectible))
            return false;
        if (this.searchMessage == null) {
            if (other.searchMessage != null)
                return false;
        }
        else if (!this.searchMessage.equals(other.searchMessage))
            return false;
        if (this.lastModified == null) {
            if (other.lastModified != null)
                return false;
        }
        else if (!this.lastModified.equals(other.lastModified))
            return false;
        if (this.createdAt == null) {
            if (other.createdAt != null)
                return false;
        }
        else if (!this.createdAt.equals(other.createdAt))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.createdBy == null) ? 0 : this.createdBy.hashCode());
        result = prime * result + ((this.cardScript == null) ? 0 : this.cardScript.hashCode());
        result = prime * result + ((this.blocklyWorkspace == null) ? 0 : this.blocklyWorkspace.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.class_ == null) ? 0 : this.class_.hashCode());
        result = prime * result + ((this.cost == null) ? 0 : this.cost.hashCode());
        result = prime * result + ((this.collectible == null) ? 0 : this.collectible.hashCode());
        result = prime * result + ((this.searchMessage == null) ? 0 : this.searchMessage.hashCode());
        result = prime * result + ((this.lastModified == null) ? 0 : this.lastModified.hashCode());
        result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GetCollectionCards (");

        sb.append(id);
        sb.append(", ").append(createdBy);
        sb.append(", ").append(cardScript);
        sb.append(", ").append(blocklyWorkspace);
        sb.append(", ").append(name);
        sb.append(", ").append(type);
        sb.append(", ").append(class_);
        sb.append(", ").append(cost);
        sb.append(", ").append(collectible);
        sb.append(", ").append(searchMessage);
        sb.append(", ").append(lastModified);
        sb.append(", ").append(createdAt);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IGetCollectionCards from) {
        setId(from.getId());
        setCreatedBy(from.getCreatedBy());
        setCardScript(from.getCardScript());
        setBlocklyWorkspace(from.getBlocklyWorkspace());
        setName(from.getName());
        setType(from.getType());
        setClass_(from.getClass_());
        setCost(from.getCost());
        setCollectible(from.getCollectible());
        setSearchMessage(from.getSearchMessage());
        setLastModified(from.getLastModified());
        setCreatedAt(from.getCreatedAt());
    }

    @Override
    public <E extends IGetCollectionCards> E into(E into) {
        into.from(this);
        return into;
    }
}
