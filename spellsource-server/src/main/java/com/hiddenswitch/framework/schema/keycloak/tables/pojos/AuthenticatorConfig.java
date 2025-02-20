/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IAuthenticatorConfig;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AuthenticatorConfig implements VertxPojo, IAuthenticatorConfig {

    private static final long serialVersionUID = 1L;

    private String id;
    private String alias;
    private String realmId;

    public AuthenticatorConfig() {}

    public AuthenticatorConfig(IAuthenticatorConfig value) {
        this.id = value.getId();
        this.alias = value.getAlias();
        this.realmId = value.getRealmId();
    }

    public AuthenticatorConfig(
        String id,
        String alias,
        String realmId
    ) {
        this.id = id;
        this.alias = alias;
        this.realmId = realmId;
    }

        public AuthenticatorConfig(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.authenticator_config.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>keycloak.authenticator_config.id</code>.
     */
    @Override
    public AuthenticatorConfig setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>keycloak.authenticator_config.alias</code>.
     */
    @Override
    public String getAlias() {
        return this.alias;
    }

    /**
     * Setter for <code>keycloak.authenticator_config.alias</code>.
     */
    @Override
    public AuthenticatorConfig setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Getter for <code>keycloak.authenticator_config.realm_id</code>.
     */
    @Override
    public String getRealmId() {
        return this.realmId;
    }

    /**
     * Setter for <code>keycloak.authenticator_config.realm_id</code>.
     */
    @Override
    public AuthenticatorConfig setRealmId(String realmId) {
        this.realmId = realmId;
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
        final AuthenticatorConfig other = (AuthenticatorConfig) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.alias == null) {
            if (other.alias != null)
                return false;
        }
        else if (!this.alias.equals(other.alias))
            return false;
        if (this.realmId == null) {
            if (other.realmId != null)
                return false;
        }
        else if (!this.realmId.equals(other.realmId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.alias == null) ? 0 : this.alias.hashCode());
        result = prime * result + ((this.realmId == null) ? 0 : this.realmId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AuthenticatorConfig (");

        sb.append(id);
        sb.append(", ").append(alias);
        sb.append(", ").append(realmId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IAuthenticatorConfig from) {
        setId(from.getId());
        setAlias(from.getAlias());
        setRealmId(from.getRealmId());
    }

    @Override
    public <E extends IAuthenticatorConfig> E into(E into) {
        into.from(this);
        return into;
    }
}
