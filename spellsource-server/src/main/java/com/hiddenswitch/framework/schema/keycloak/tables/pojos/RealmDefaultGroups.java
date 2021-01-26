/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IRealmDefaultGroups;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RealmDefaultGroups implements VertxPojo, IRealmDefaultGroups {

    private static final long serialVersionUID = -1268728793;

    private String realmId;
    private String groupId;

    public RealmDefaultGroups() {}

    public RealmDefaultGroups(IRealmDefaultGroups value) {
        this.realmId = value.getRealmId();
        this.groupId = value.getGroupId();
    }

    public RealmDefaultGroups(
        String realmId,
        String groupId
    ) {
        this.realmId = realmId;
        this.groupId = groupId;
    }

    public RealmDefaultGroups(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getRealmId() {
        return this.realmId;
    }

    @Override
    public RealmDefaultGroups setRealmId(String realmId) {
        this.realmId = realmId;
        return this;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public RealmDefaultGroups setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RealmDefaultGroups (");

        sb.append(realmId);
        sb.append(", ").append(groupId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IRealmDefaultGroups from) {
        setRealmId(from.getRealmId());
        setGroupId(from.getGroupId());
    }

    @Override
    public <E extends IRealmDefaultGroups> E into(E into) {
        into.from(this);
        return into;
    }
}