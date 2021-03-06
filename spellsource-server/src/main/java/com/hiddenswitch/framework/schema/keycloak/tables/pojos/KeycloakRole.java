/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IKeycloakRole;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class KeycloakRole implements VertxPojo, IKeycloakRole {

    private static final long serialVersionUID = -2122999392;

    private String  id;
    private String  clientRealmConstraint;
    private Boolean clientRole;
    private String  description;
    private String  name;
    private String  realmId;
    private String  client;
    private String  realm;

    public KeycloakRole() {}

    public KeycloakRole(IKeycloakRole value) {
        this.id = value.getId();
        this.clientRealmConstraint = value.getClientRealmConstraint();
        this.clientRole = value.getClientRole();
        this.description = value.getDescription();
        this.name = value.getName();
        this.realmId = value.getRealmId();
        this.client = value.getClient();
        this.realm = value.getRealm();
    }

    public KeycloakRole(
        String  id,
        String  clientRealmConstraint,
        Boolean clientRole,
        String  description,
        String  name,
        String  realmId,
        String  client,
        String  realm
    ) {
        this.id = id;
        this.clientRealmConstraint = clientRealmConstraint;
        this.clientRole = clientRole;
        this.description = description;
        this.name = name;
        this.realmId = realmId;
        this.client = client;
        this.realm = realm;
    }

    public KeycloakRole(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public KeycloakRole setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getClientRealmConstraint() {
        return this.clientRealmConstraint;
    }

    @Override
    public KeycloakRole setClientRealmConstraint(String clientRealmConstraint) {
        this.clientRealmConstraint = clientRealmConstraint;
        return this;
    }

    @Override
    public Boolean getClientRole() {
        return this.clientRole;
    }

    @Override
    public KeycloakRole setClientRole(Boolean clientRole) {
        this.clientRole = clientRole;
        return this;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public KeycloakRole setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public KeycloakRole setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getRealmId() {
        return this.realmId;
    }

    @Override
    public KeycloakRole setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    @Override
    public String getClient() {
        return this.client;
    }

    @Override
    public KeycloakRole setClient(String client) {
        this.client = client;
        return this;
    }

    @Override
    public String getRealm() {
        return this.realm;
    }

    @Override
    public KeycloakRole setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KeycloakRole (");

        sb.append(id);
        sb.append(", ").append(clientRealmConstraint);
        sb.append(", ").append(clientRole);
        sb.append(", ").append(description);
        sb.append(", ").append(name);
        sb.append(", ").append(realmId);
        sb.append(", ").append(client);
        sb.append(", ").append(realm);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IKeycloakRole from) {
        setId(from.getId());
        setClientRealmConstraint(from.getClientRealmConstraint());
        setClientRole(from.getClientRole());
        setDescription(from.getDescription());
        setName(from.getName());
        setRealmId(from.getRealmId());
        setClient(from.getClient());
        setRealm(from.getRealm());
    }

    @Override
    public <E extends IKeycloakRole> E into(E into) {
        into.from(this);
        return into;
    }
}
