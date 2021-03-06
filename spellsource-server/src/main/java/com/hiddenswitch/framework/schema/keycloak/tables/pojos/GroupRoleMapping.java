/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IGroupRoleMapping;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GroupRoleMapping implements VertxPojo, IGroupRoleMapping {

    private static final long serialVersionUID = 347338587;

    private String roleId;
    private String groupId;

    public GroupRoleMapping() {}

    public GroupRoleMapping(IGroupRoleMapping value) {
        this.roleId = value.getRoleId();
        this.groupId = value.getGroupId();
    }

    public GroupRoleMapping(
        String roleId,
        String groupId
    ) {
        this.roleId = roleId;
        this.groupId = groupId;
    }

    public GroupRoleMapping(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getRoleId() {
        return this.roleId;
    }

    @Override
    public GroupRoleMapping setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public GroupRoleMapping setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GroupRoleMapping (");

        sb.append(roleId);
        sb.append(", ").append(groupId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IGroupRoleMapping from) {
        setRoleId(from.getRoleId());
        setGroupId(from.getGroupId());
    }

    @Override
    public <E extends IGroupRoleMapping> E into(E into) {
        into.from(this);
        return into;
    }
}
