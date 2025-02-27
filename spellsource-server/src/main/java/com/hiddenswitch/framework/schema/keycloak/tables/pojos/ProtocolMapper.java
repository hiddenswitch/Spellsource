/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.pojos;


import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IProtocolMapper;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProtocolMapper implements VertxPojo, IProtocolMapper {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String protocol;
    private String protocolMapperName;
    private String clientId;
    private String clientScopeId;

    public ProtocolMapper() {}

    public ProtocolMapper(IProtocolMapper value) {
        this.id = value.getId();
        this.name = value.getName();
        this.protocol = value.getProtocol();
        this.protocolMapperName = value.getProtocolMapperName();
        this.clientId = value.getClientId();
        this.clientScopeId = value.getClientScopeId();
    }

    public ProtocolMapper(
        String id,
        String name,
        String protocol,
        String protocolMapperName,
        String clientId,
        String clientScopeId
    ) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.protocolMapperName = protocolMapperName;
        this.clientId = clientId;
        this.clientScopeId = clientScopeId;
    }

        public ProtocolMapper(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }

    /**
     * Getter for <code>keycloak.protocol_mapper.id</code>.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Setter for <code>keycloak.protocol_mapper.id</code>.
     */
    @Override
    public ProtocolMapper setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>keycloak.protocol_mapper.name</code>.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>keycloak.protocol_mapper.name</code>.
     */
    @Override
    public ProtocolMapper setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Getter for <code>keycloak.protocol_mapper.protocol</code>.
     */
    @Override
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Setter for <code>keycloak.protocol_mapper.protocol</code>.
     */
    @Override
    public ProtocolMapper setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Getter for <code>keycloak.protocol_mapper.protocol_mapper_name</code>.
     */
    @Override
    public String getProtocolMapperName() {
        return this.protocolMapperName;
    }

    /**
     * Setter for <code>keycloak.protocol_mapper.protocol_mapper_name</code>.
     */
    @Override
    public ProtocolMapper setProtocolMapperName(String protocolMapperName) {
        this.protocolMapperName = protocolMapperName;
        return this;
    }

    /**
     * Getter for <code>keycloak.protocol_mapper.client_id</code>.
     */
    @Override
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Setter for <code>keycloak.protocol_mapper.client_id</code>.
     */
    @Override
    public ProtocolMapper setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Getter for <code>keycloak.protocol_mapper.client_scope_id</code>.
     */
    @Override
    public String getClientScopeId() {
        return this.clientScopeId;
    }

    /**
     * Setter for <code>keycloak.protocol_mapper.client_scope_id</code>.
     */
    @Override
    public ProtocolMapper setClientScopeId(String clientScopeId) {
        this.clientScopeId = clientScopeId;
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
        final ProtocolMapper other = (ProtocolMapper) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.protocol == null) {
            if (other.protocol != null)
                return false;
        }
        else if (!this.protocol.equals(other.protocol))
            return false;
        if (this.protocolMapperName == null) {
            if (other.protocolMapperName != null)
                return false;
        }
        else if (!this.protocolMapperName.equals(other.protocolMapperName))
            return false;
        if (this.clientId == null) {
            if (other.clientId != null)
                return false;
        }
        else if (!this.clientId.equals(other.clientId))
            return false;
        if (this.clientScopeId == null) {
            if (other.clientScopeId != null)
                return false;
        }
        else if (!this.clientScopeId.equals(other.clientScopeId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.protocol == null) ? 0 : this.protocol.hashCode());
        result = prime * result + ((this.protocolMapperName == null) ? 0 : this.protocolMapperName.hashCode());
        result = prime * result + ((this.clientId == null) ? 0 : this.clientId.hashCode());
        result = prime * result + ((this.clientScopeId == null) ? 0 : this.clientScopeId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProtocolMapper (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(protocol);
        sb.append(", ").append(protocolMapperName);
        sb.append(", ").append(clientId);
        sb.append(", ").append(clientScopeId);

        sb.append(")");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IProtocolMapper from) {
        setId(from.getId());
        setName(from.getName());
        setProtocol(from.getProtocol());
        setProtocolMapperName(from.getProtocolMapperName());
        setClientId(from.getClientId());
        setClientScopeId(from.getClientScopeId());
    }

    @Override
    public <E extends IProtocolMapper> E into(E into) {
        into.from(this);
        return into;
    }
}
