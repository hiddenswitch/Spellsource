/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IMigrationModel;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MigrationModel implements VertxPojo, IMigrationModel {

    private static final long serialVersionUID = 40655771;

    private String id;
    private String version;
    private Long   updateTime;

    public MigrationModel() {}

    public MigrationModel(IMigrationModel value) {
        this.id = value.getId();
        this.version = value.getVersion();
        this.updateTime = value.getUpdateTime();
    }

    public MigrationModel(
        String id,
        String version,
        Long   updateTime
    ) {
        this.id = id;
        this.version = version;
        this.updateTime = updateTime;
    }

    public MigrationModel(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public MigrationModel setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public MigrationModel setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public Long getUpdateTime() {
        return this.updateTime;
    }

    @Override
    public MigrationModel setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MigrationModel (");

        sb.append(id);
        sb.append(", ").append(version);
        sb.append(", ").append(updateTime);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IMigrationModel from) {
        setId(from.getId());
        setVersion(from.getVersion());
        setUpdateTime(from.getUpdateTime());
    }

    @Override
    public <E extends IMigrationModel> E into(E into) {
        into.from(this);
        return into;
    }
}
