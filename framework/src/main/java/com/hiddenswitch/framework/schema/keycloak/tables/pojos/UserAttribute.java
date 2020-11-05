/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IUserAttribute;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserAttribute implements VertxPojo, IUserAttribute {

    private static final long serialVersionUID = -1725414997;

    private String name;
    private String value;
    private String userId;
    private String id;

    public UserAttribute() {}

    public UserAttribute(IUserAttribute value) {
        this.name = value.getName();
        this.value = value.getValue();
        this.userId = value.getUserId();
        this.id = value.getId();
    }

    public UserAttribute(
        String name,
        String value,
        String userId,
        String id
    ) {
        this.name = name;
        this.value = value;
        this.userId = userId;
        this.id = id;
    }

    public UserAttribute(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UserAttribute setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public UserAttribute setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public UserAttribute setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public UserAttribute setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UserAttribute (");

        sb.append(name);
        sb.append(", ").append(value);
        sb.append(", ").append(userId);
        sb.append(", ").append(id);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IUserAttribute from) {
        setName(from.getName());
        setValue(from.getValue());
        setUserId(from.getUserId());
        setId(from.getId());
    }

    @Override
    public <E extends IUserAttribute> E into(E into) {
        into.from(this);
        return into;
    }
}