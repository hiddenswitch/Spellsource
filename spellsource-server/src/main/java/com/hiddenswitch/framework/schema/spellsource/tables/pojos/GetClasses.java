/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IGetClasses;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetClasses implements VertxPojo, IGetClasses {

    private static final long serialVersionUID = 1L;

    private String createdBy;
    private String class_;
    private Boolean isPublished;
    private Boolean collectible;
    private JsonObject cardScript;
    private String id;
    private String name;

    public GetClasses() {}

    public GetClasses(IGetClasses value) {
        this.createdBy = value.getCreatedBy();
        this.class_ = value.getClass_();
        this.isPublished = value.getIsPublished();
        this.collectible = value.getCollectible();
        this.cardScript = value.getCardScript();
        this.id = value.getId();
        this.name = value.getName();
    }

    public GetClasses(
        String createdBy,
        String class_,
        Boolean isPublished,
        Boolean collectible,
        JsonObject cardScript,
        String id,
        String name
    ) {
        this.createdBy = createdBy;
        this.class_ = class_;
        this.isPublished = isPublished;
        this.collectible = collectible;
        this.cardScript = cardScript;
        this.id = id;
        this.name = name;
    }

        public GetClasses(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>spellsource.get_classes.created_by</code>.
     */
    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Setter for <code>spellsource.get_classes.created_by</code>.
     */
    @Override
    public GetClasses setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_classes.class</code>.
     */
    @Override
    public String getClass_() {
        return this.class_;
    }

    /**
     * Setter for <code>spellsource.get_classes.class</code>.
     */
    @Override
    public GetClasses setClass_(String class_) {
        this.class_ = class_;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_classes.is_published</code>.
     */
    @Override
    public Boolean getIsPublished() {
        return this.isPublished;
    }

    /**
     * Setter for <code>spellsource.get_classes.is_published</code>.
     */
    @Override
    public GetClasses setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_classes.collectible</code>.
     */
    @Override
    public Boolean getCollectible() {
        return this.collectible;
    }

    /**
     * Setter for <code>spellsource.get_classes.collectible</code>.
     */
    @Override
    public GetClasses setCollectible(Boolean collectible) {
        this.collectible = collectible;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_classes.card_script</code>.
     */
    @Override
    public JsonObject getCardScript() {
        return this.cardScript;
    }

    /**
     * Setter for <code>spellsource.get_classes.card_script</code>.
     */
    @Override
    public GetClasses setCardScript(JsonObject cardScript) {
        this.cardScript = cardScript;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_classes.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>spellsource.get_classes.id</code>.
     */
    @Override
    public GetClasses setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>spellsource.get_classes.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>spellsource.get_classes.name</code>.
     */
    @Override
    public GetClasses setName(String name) {
        this.name = name;
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
        final GetClasses other = (GetClasses) obj;
        if (this.createdBy == null) {
            if (other.createdBy != null)
                return false;
        }
        else if (!this.createdBy.equals(other.createdBy))
            return false;
        if (this.class_ == null) {
            if (other.class_ != null)
                return false;
        }
        else if (!this.class_.equals(other.class_))
            return false;
        if (this.isPublished == null) {
            if (other.isPublished != null)
                return false;
        }
        else if (!this.isPublished.equals(other.isPublished))
            return false;
        if (this.collectible == null) {
            if (other.collectible != null)
                return false;
        }
        else if (!this.collectible.equals(other.collectible))
            return false;
        if (this.cardScript == null) {
            if (other.cardScript != null)
                return false;
        }
        else if (!this.cardScript.equals(other.cardScript))
            return false;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.createdBy == null) ? 0 : this.createdBy.hashCode());
        result = prime * result + ((this.class_ == null) ? 0 : this.class_.hashCode());
        result = prime * result + ((this.isPublished == null) ? 0 : this.isPublished.hashCode());
        result = prime * result + ((this.collectible == null) ? 0 : this.collectible.hashCode());
        result = prime * result + ((this.cardScript == null) ? 0 : this.cardScript.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GetClasses (");

        sb.append(createdBy);
        sb.append(", ").append(class_);
        sb.append(", ").append(isPublished);
        sb.append(", ").append(collectible);
        sb.append(", ").append(cardScript);
        sb.append(", ").append(id);
        sb.append(", ").append(name);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IGetClasses from) {
        setCreatedBy(from.getCreatedBy());
        setClass_(from.getClass_());
        setIsPublished(from.getIsPublished());
        setCollectible(from.getCollectible());
        setCardScript(from.getCardScript());
        setId(from.getId());
        setName(from.getName());
    }

    @Override
    public <E extends IGetClasses> E into(E into) {
        into.from(this);
        return into;
    }
}
