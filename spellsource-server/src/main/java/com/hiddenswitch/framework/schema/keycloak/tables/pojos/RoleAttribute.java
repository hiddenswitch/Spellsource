/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IRoleAttribute;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RoleAttribute implements VertxPojo, IRoleAttribute {

    private static final long serialVersionUID = 1999229869;

    private String id;
    private String roleId;
    private String name;
    private String value;

    public RoleAttribute() {}

    public RoleAttribute(IRoleAttribute value) {
        this.id = value.getId();
        this.roleId = value.getRoleId();
        this.name = value.getName();
        this.value = value.getValue();
    }

    public RoleAttribute(
        String id,
        String roleId,
        String name,
        String value
    ) {
        this.id = id;
        this.roleId = roleId;
        this.name = name;
        this.value = value;
    }

    public RoleAttribute(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public RoleAttribute setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getRoleId() {
        return this.roleId;
    }

    @Override
    public RoleAttribute setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public RoleAttribute setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public RoleAttribute setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RoleAttribute (");

        sb.append(id);
        sb.append(", ").append(roleId);
        sb.append(", ").append(name);
        sb.append(", ").append(value);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IRoleAttribute from) {
        setId(from.getId());
        setRoleId(from.getRoleId());
        setName(from.getName());
        setValue(from.getValue());
    }

    @Override
    public <E extends IRoleAttribute> E into(E into) {
        into.from(this);
        return into;
    }
}
