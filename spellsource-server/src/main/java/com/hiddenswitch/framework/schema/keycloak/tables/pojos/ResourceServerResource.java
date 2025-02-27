/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IResourceServerResource;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ResourceServerResource implements VertxPojo, IResourceServerResource {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String type;
    private String iconUri;
    private String owner;
    private String resourceServerId;
    private Boolean ownerManagedAccess;
    private String displayName;

    public ResourceServerResource() {}

    public ResourceServerResource(IResourceServerResource value) {
        this.id = value.getId();
        this.name = value.getName();
        this.type = value.getType();
        this.iconUri = value.getIconUri();
        this.owner = value.getOwner();
        this.resourceServerId = value.getResourceServerId();
        this.ownerManagedAccess = value.getOwnerManagedAccess();
        this.displayName = value.getDisplayName();
    }

    public ResourceServerResource(
        String id,
        String name,
        String type,
        String iconUri,
        String owner,
        String resourceServerId,
        Boolean ownerManagedAccess,
        String displayName
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.iconUri = iconUri;
        this.owner = owner;
        this.resourceServerId = resourceServerId;
        this.ownerManagedAccess = ownerManagedAccess;
        this.displayName = displayName;
    }

        public ResourceServerResource(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.resource_server_resource.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.id</code>.
     */
    @Override
    public ResourceServerResource setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.name</code>.
     */
    @Override
    public ResourceServerResource setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.type</code>.
     */
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.type</code>.
     */
    @Override
    public ResourceServerResource setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.icon_uri</code>.
     */
    @Override
    public String getIconUri() {
        return this.iconUri;
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.icon_uri</code>.
     */
    @Override
    public ResourceServerResource setIconUri(String iconUri) {
        this.iconUri = iconUri;
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.owner</code>.
     */
    @Override
    public String getOwner() {
        return this.owner;
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.owner</code>.
     */
    @Override
    public ResourceServerResource setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Getter for
     * <code>keycloak.resource_server_resource.resource_server_id</code>.
     */
    @Override
    public String getResourceServerId() {
        return this.resourceServerId;
    }

    /**
     * Setter for
     * <code>keycloak.resource_server_resource.resource_server_id</code>.
     */
    @Override
    public ResourceServerResource setResourceServerId(String resourceServerId) {
        this.resourceServerId = resourceServerId;
        return this;
    }

    /**
     * Getter for
     * <code>keycloak.resource_server_resource.owner_managed_access</code>.
     */
    @Override
    public Boolean getOwnerManagedAccess() {
        return this.ownerManagedAccess;
    }

    /**
     * Setter for
     * <code>keycloak.resource_server_resource.owner_managed_access</code>.
     */
    @Override
    public ResourceServerResource setOwnerManagedAccess(Boolean ownerManagedAccess) {
        this.ownerManagedAccess = ownerManagedAccess;
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_server_resource.display_name</code>.
     */
    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Setter for <code>keycloak.resource_server_resource.display_name</code>.
     */
    @Override
    public ResourceServerResource setDisplayName(String displayName) {
        this.displayName = displayName;
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
        final ResourceServerResource other = (ResourceServerResource) obj;
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
        if (this.type == null) {
            if (other.type != null)
                return false;
        }
        else if (!this.type.equals(other.type))
            return false;
        if (this.iconUri == null) {
            if (other.iconUri != null)
                return false;
        }
        else if (!this.iconUri.equals(other.iconUri))
            return false;
        if (this.owner == null) {
            if (other.owner != null)
                return false;
        }
        else if (!this.owner.equals(other.owner))
            return false;
        if (this.resourceServerId == null) {
            if (other.resourceServerId != null)
                return false;
        }
        else if (!this.resourceServerId.equals(other.resourceServerId))
            return false;
        if (this.ownerManagedAccess == null) {
            if (other.ownerManagedAccess != null)
                return false;
        }
        else if (!this.ownerManagedAccess.equals(other.ownerManagedAccess))
            return false;
        if (this.displayName == null) {
            if (other.displayName != null)
                return false;
        }
        else if (!this.displayName.equals(other.displayName))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        result = prime * result + ((this.iconUri == null) ? 0 : this.iconUri.hashCode());
        result = prime * result + ((this.owner == null) ? 0 : this.owner.hashCode());
        result = prime * result + ((this.resourceServerId == null) ? 0 : this.resourceServerId.hashCode());
        result = prime * result + ((this.ownerManagedAccess == null) ? 0 : this.ownerManagedAccess.hashCode());
        result = prime * result + ((this.displayName == null) ? 0 : this.displayName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceServerResource (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(type);
        sb.append(", ").append(iconUri);
        sb.append(", ").append(owner);
        sb.append(", ").append(resourceServerId);
        sb.append(", ").append(ownerManagedAccess);
        sb.append(", ").append(displayName);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IResourceServerResource from) {
        setId(from.getId());
        setName(from.getName());
        setType(from.getType());
        setIconUri(from.getIconUri());
        setOwner(from.getOwner());
        setResourceServerId(from.getResourceServerId());
        setOwnerManagedAccess(from.getOwnerManagedAccess());
        setDisplayName(from.getDisplayName());
    }

    @Override
    public <E extends IResourceServerResource> E into(E into) {
        into.from(this);
        return into;
    }
}
