/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.ICardCatalogueGetFormat;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import java.time.OffsetDateTime;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CardCatalogueGetFormat implements VertxPojo, ICardCatalogueGetFormat {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private String uri;
    private JsonObject blocklyWorkspace;
    private JsonObject cardScript;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModified;
    private Boolean isArchived;
    private Boolean isPublished;
    private Long succession;

    public CardCatalogueGetFormat() {}

    public CardCatalogueGetFormat(ICardCatalogueGetFormat value) {
        this.id = value.getId();
        this.createdBy = value.getCreatedBy();
        this.uri = value.getUri();
        this.blocklyWorkspace = value.getBlocklyWorkspace();
        this.cardScript = value.getCardScript();
        this.createdAt = value.getCreatedAt();
        this.lastModified = value.getLastModified();
        this.isArchived = value.getIsArchived();
        this.isPublished = value.getIsPublished();
        this.succession = value.getSuccession();
    }

    public CardCatalogueGetFormat(
        String id,
        String createdBy,
        String uri,
        JsonObject blocklyWorkspace,
        JsonObject cardScript,
        OffsetDateTime createdAt,
        OffsetDateTime lastModified,
        Boolean isArchived,
        Boolean isPublished,
        Long succession
    ) {
        this.id = id;
        this.createdBy = createdBy;
        this.uri = uri;
        this.blocklyWorkspace = blocklyWorkspace;
        this.cardScript = cardScript;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.isArchived = isArchived;
        this.isPublished = isPublished;
        this.succession = succession;
    }

        public CardCatalogueGetFormat(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>spellsource.card_catalogue_get_format.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>spellsource.card_catalogue_get_format.id</code>.
     */
    @Override
    public CardCatalogueGetFormat setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_get_format.created_by</code>.
     */
    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Setter for <code>spellsource.card_catalogue_get_format.created_by</code>.
     */
    @Override
    public CardCatalogueGetFormat setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_get_format.uri</code>.
     */
    @Override
    public String getUri() {
        return this.uri;
    }

    /**
     * Setter for <code>spellsource.card_catalogue_get_format.uri</code>.
     */
    @Override
    public CardCatalogueGetFormat setUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_format.blockly_workspace</code>.
     */
    @Override
    public JsonObject getBlocklyWorkspace() {
        return this.blocklyWorkspace;
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_format.blockly_workspace</code>.
     */
    @Override
    public CardCatalogueGetFormat setBlocklyWorkspace(JsonObject blocklyWorkspace) {
        this.blocklyWorkspace = blocklyWorkspace;
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_format.card_script</code>.
     */
    @Override
    public JsonObject getCardScript() {
        return this.cardScript;
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_format.card_script</code>.
     */
    @Override
    public CardCatalogueGetFormat setCardScript(JsonObject cardScript) {
        this.cardScript = cardScript;
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_get_format.created_at</code>.
     */
    @Override
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Setter for <code>spellsource.card_catalogue_get_format.created_at</code>.
     */
    @Override
    public CardCatalogueGetFormat setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_format.last_modified</code>.
     */
    @Override
    public OffsetDateTime getLastModified() {
        return this.lastModified;
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_format.last_modified</code>.
     */
    @Override
    public CardCatalogueGetFormat setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_format.is_archived</code>.
     */
    @Override
    public Boolean getIsArchived() {
        return this.isArchived;
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_format.is_archived</code>.
     */
    @Override
    public CardCatalogueGetFormat setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
        return this;
    }

    /**
     * Getter for
     * <code>spellsource.card_catalogue_get_format.is_published</code>.
     */
    @Override
    public Boolean getIsPublished() {
        return this.isPublished;
    }

    /**
     * Setter for
     * <code>spellsource.card_catalogue_get_format.is_published</code>.
     */
    @Override
    public CardCatalogueGetFormat setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
        return this;
    }

    /**
     * Getter for <code>spellsource.card_catalogue_get_format.succession</code>.
     */
    @Override
    public Long getSuccession() {
        return this.succession;
    }

    /**
     * Setter for <code>spellsource.card_catalogue_get_format.succession</code>.
     */
    @Override
    public CardCatalogueGetFormat setSuccession(Long succession) {
        this.succession = succession;
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
        final CardCatalogueGetFormat other = (CardCatalogueGetFormat) obj;
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
        if (this.uri == null) {
            if (other.uri != null)
                return false;
        }
        else if (!this.uri.equals(other.uri))
            return false;
        if (this.blocklyWorkspace == null) {
            if (other.blocklyWorkspace != null)
                return false;
        }
        else if (!this.blocklyWorkspace.equals(other.blocklyWorkspace))
            return false;
        if (this.cardScript == null) {
            if (other.cardScript != null)
                return false;
        }
        else if (!this.cardScript.equals(other.cardScript))
            return false;
        if (this.createdAt == null) {
            if (other.createdAt != null)
                return false;
        }
        else if (!this.createdAt.equals(other.createdAt))
            return false;
        if (this.lastModified == null) {
            if (other.lastModified != null)
                return false;
        }
        else if (!this.lastModified.equals(other.lastModified))
            return false;
        if (this.isArchived == null) {
            if (other.isArchived != null)
                return false;
        }
        else if (!this.isArchived.equals(other.isArchived))
            return false;
        if (this.isPublished == null) {
            if (other.isPublished != null)
                return false;
        }
        else if (!this.isPublished.equals(other.isPublished))
            return false;
        if (this.succession == null) {
            if (other.succession != null)
                return false;
        }
        else if (!this.succession.equals(other.succession))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.createdBy == null) ? 0 : this.createdBy.hashCode());
        result = prime * result + ((this.uri == null) ? 0 : this.uri.hashCode());
        result = prime * result + ((this.blocklyWorkspace == null) ? 0 : this.blocklyWorkspace.hashCode());
        result = prime * result + ((this.cardScript == null) ? 0 : this.cardScript.hashCode());
        result = prime * result + ((this.createdAt == null) ? 0 : this.createdAt.hashCode());
        result = prime * result + ((this.lastModified == null) ? 0 : this.lastModified.hashCode());
        result = prime * result + ((this.isArchived == null) ? 0 : this.isArchived.hashCode());
        result = prime * result + ((this.isPublished == null) ? 0 : this.isPublished.hashCode());
        result = prime * result + ((this.succession == null) ? 0 : this.succession.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CardCatalogueGetFormat (");

        sb.append(id);
        sb.append(", ").append(createdBy);
        sb.append(", ").append(uri);
        sb.append(", ").append(blocklyWorkspace);
        sb.append(", ").append(cardScript);
        sb.append(", ").append(createdAt);
        sb.append(", ").append(lastModified);
        sb.append(", ").append(isArchived);
        sb.append(", ").append(isPublished);
        sb.append(", ").append(succession);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ICardCatalogueGetFormat from) {
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
    public <E extends ICardCatalogueGetFormat> E into(E into) {
        into.from(this);
        return into;
    }
}