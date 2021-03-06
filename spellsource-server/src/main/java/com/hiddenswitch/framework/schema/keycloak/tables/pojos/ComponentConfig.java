/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IComponentConfig;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ComponentConfig implements VertxPojo, IComponentConfig {

    private static final long serialVersionUID = -620772551;

    private String id;
    private String componentId;
    private String name;
    private String value;

    public ComponentConfig() {}

    public ComponentConfig(IComponentConfig value) {
        this.id = value.getId();
        this.componentId = value.getComponentId();
        this.name = value.getName();
        this.value = value.getValue();
    }

    public ComponentConfig(
        String id,
        String componentId,
        String name,
        String value
    ) {
        this.id = id;
        this.componentId = componentId;
        this.name = name;
        this.value = value;
    }

    public ComponentConfig(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ComponentConfig setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getComponentId() {
        return this.componentId;
    }

    @Override
    public ComponentConfig setComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ComponentConfig setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public ComponentConfig setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ComponentConfig (");

        sb.append(id);
        sb.append(", ").append(componentId);
        sb.append(", ").append(name);
        sb.append(", ").append(value);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IComponentConfig from) {
        setId(from.getId());
        setComponentId(from.getComponentId());
        setName(from.getName());
        setValue(from.getValue());
    }

    @Override
    public <E extends IComponentConfig> E into(E into) {
        into.from(this);
        return into;
    }
}
