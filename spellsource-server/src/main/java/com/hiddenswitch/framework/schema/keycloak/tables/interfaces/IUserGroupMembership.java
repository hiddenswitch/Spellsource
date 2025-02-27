/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IUserGroupMembership extends VertxPojo, Serializable {

    /**
     * Setter for <code>keycloak.user_group_membership.group_id</code>.
     */
    public IUserGroupMembership setGroupId(String value);

    /**
     * Getter for <code>keycloak.user_group_membership.group_id</code>.
     */
    public String getGroupId();

    /**
     * Setter for <code>keycloak.user_group_membership.user_id</code>.
     */
    public IUserGroupMembership setUserId(String value);

    /**
     * Getter for <code>keycloak.user_group_membership.user_id</code>.
     */
    public String getUserId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common
     * interface IUserGroupMembership
     */
    public void from(IUserGroupMembership from);

    /**
     * Copy data into another generated Record/POJO implementing the common
     * interface IUserGroupMembership
     */
    public <E extends IUserGroupMembership> E into(E into);

        @Override
        public default IUserGroupMembership fromJson(io.vertx.core.json.JsonObject json) {
                setOrThrow(this::setGroupId,json::getString,"group_id","java.lang.String");
                setOrThrow(this::setUserId,json::getString,"user_id","java.lang.String");
                return this;
        }


        @Override
        public default io.vertx.core.json.JsonObject toJson() {
                io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
                json.put("group_id",getGroupId());
                json.put("user_id",getUserId());
                return json;
        }

}
