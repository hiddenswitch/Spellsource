/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.spellsource.tables.pojos;


import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.IFriends;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Friends implements VertxPojo, IFriends {

    private static final long serialVersionUID = -1433970637;

    private String         id;
    private String         friend;
    private OffsetDateTime createdAt;

    public Friends() {}

    public Friends(IFriends value) {
        this.id = value.getId();
        this.friend = value.getFriend();
        this.createdAt = value.getCreatedAt();
    }

    public Friends(
        String         id,
        String         friend,
        OffsetDateTime createdAt
    ) {
        this.id = id;
        this.friend = friend;
        this.createdAt = createdAt;
    }

    public Friends(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Friends setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getFriend() {
        return this.friend;
    }

    @Override
    public Friends setFriend(String friend) {
        this.friend = friend;
        return this;
    }

    @Override
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public Friends setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Friends (");

        sb.append(id);
        sb.append(", ").append(friend);
        sb.append(", ").append(createdAt);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IFriends from) {
        setId(from.getId());
        setFriend(from.getFriend());
        setCreatedAt(from.getCreatedAt());
    }

    @Override
    public <E extends IFriends> E into(E into) {
        into.from(this);
        return into;
    }
}