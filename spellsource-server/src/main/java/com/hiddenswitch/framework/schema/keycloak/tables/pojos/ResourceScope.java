/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IResourceScope;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ResourceScope implements VertxPojo, IResourceScope {

    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String scopeId;

    public ResourceScope() {}

    public ResourceScope(IResourceScope value) {
        this.resourceId = value.getResourceId();
        this.scopeId = value.getScopeId();
    }

    public ResourceScope(
        String resourceId,
        String scopeId
    ) {
        this.resourceId = resourceId;
        this.scopeId = scopeId;
    }

        public ResourceScope(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.resource_scope.resource_id</code>.
     */
    @Override
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * Setter for <code>keycloak.resource_scope.resource_id</code>.
     */
    @Override
    public ResourceScope setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Getter for <code>keycloak.resource_scope.scope_id</code>.
     */
    @Override
    public String getScopeId() {
        return this.scopeId;
    }

    /**
     * Setter for <code>keycloak.resource_scope.scope_id</code>.
     */
    @Override
    public ResourceScope setScopeId(String scopeId) {
        this.scopeId = scopeId;
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
        final ResourceScope other = (ResourceScope) obj;
        if (this.resourceId == null) {
            if (other.resourceId != null)
                return false;
        }
        else if (!this.resourceId.equals(other.resourceId))
            return false;
        if (this.scopeId == null) {
            if (other.scopeId != null)
                return false;
        }
        else if (!this.scopeId.equals(other.scopeId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.resourceId == null) ? 0 : this.resourceId.hashCode());
        result = prime * result + ((this.scopeId == null) ? 0 : this.scopeId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceScope (");

        sb.append(resourceId);
        sb.append(", ").append(scopeId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IResourceScope from) {
        setResourceId(from.getResourceId());
        setScopeId(from.getScopeId());
    }

    @Override
    public <E extends IResourceScope> E into(E into) {
        into.from(this);
        return into;
    }
}
