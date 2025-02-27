/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IRealmAttribute;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RealmAttribute implements VertxPojo, IRealmAttribute {

    private static final long serialVersionUID = 1L;

    private String name;
    private String realmId;
    private String value;

    public RealmAttribute() {}

    public RealmAttribute(IRealmAttribute value) {
        this.name = value.getName();
        this.realmId = value.getRealmId();
        this.value = value.getValue();
    }

    public RealmAttribute(
        String name,
        String realmId,
        String value
    ) {
        this.name = name;
        this.realmId = realmId;
        this.value = value;
    }

        public RealmAttribute(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.realm_attribute.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>keycloak.realm_attribute.name</code>.
     */
    @Override
    public RealmAttribute setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_attribute.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return this.realmId;
    }

    /**
     * Setter for <code>keycloak.realm_attribute.realm_id</code>.
     */
    @Override
    public RealmAttribute setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    /**
     * Getter for <code>keycloak.realm_attribute.value</code>.
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * Setter for <code>keycloak.realm_attribute.value</code>.
     */
    @Override
    public RealmAttribute setValue(String value) {
        this.value = value;
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
        final RealmAttribute other = (RealmAttribute) obj;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.realmId == null) {
            if (other.realmId != null)
                return false;
        }
        else if (!this.realmId.equals(other.realmId))
            return false;
        if (this.value == null) {
            if (other.value != null)
                return false;
        }
        else if (!this.value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.realmId == null) ? 0 : this.realmId.hashCode());
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RealmAttribute (");

        sb.append(name);
        sb.append(", ").append(realmId);
        sb.append(", ").append(value);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IRealmAttribute from) {
        setName(from.getName());
        setRealmId(from.getRealmId());
        setValue(from.getValue());
    }

    @Override
    public <E extends IRealmAttribute> E into(E into) {
        into.from(this);
        return into;
    }
}
