/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IMigrationModel;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MigrationModel implements VertxPojo, IMigrationModel {

    private static final long serialVersionUID = 1L;

    private String id;
    private String version;
    private Long updateTime;

    public MigrationModel() {}

    public MigrationModel(IMigrationModel value) {
        this.id = value.getId();
        this.version = value.getVersion();
        this.updateTime = value.getUpdateTime();
    }

    public MigrationModel(
        String id,
        String version,
        Long updateTime
    ) {
        this.id = id;
        this.version = version;
        this.updateTime = updateTime;
    }

        public MigrationModel(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.migration_model.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>keycloak.migration_model.id</code>.
     */
    @Override
    public MigrationModel setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>keycloak.migration_model.version</code>.
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Setter for <code>keycloak.migration_model.version</code>.
     */
    @Override
    public MigrationModel setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Getter for <code>keycloak.migration_model.update_time</code>.
     */
    @Override
    public Long getUpdateTime() {
        return this.updateTime;
    }

    /**
     * Setter for <code>keycloak.migration_model.update_time</code>.
     */
    @Override
    public MigrationModel setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
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
        final MigrationModel other = (MigrationModel) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.version == null) {
            if (other.version != null)
                return false;
        }
        else if (!this.version.equals(other.version))
            return false;
        if (this.updateTime == null) {
            if (other.updateTime != null)
                return false;
        }
        else if (!this.updateTime.equals(other.updateTime))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
        result = prime * result + ((this.updateTime == null) ? 0 : this.updateTime.hashCode());
        return result;
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
