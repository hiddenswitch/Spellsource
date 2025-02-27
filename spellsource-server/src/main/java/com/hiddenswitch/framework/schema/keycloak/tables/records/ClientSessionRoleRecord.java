/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables.records;


import com.hiddenswitch.framework.schema.keycloak.tables.ClientSessionRole;
import com.hiddenswitch.framework.schema.keycloak.tables.interfaces.IClientSessionRole;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ClientSessionRoleRecord extends UpdatableRecordImpl<ClientSessionRoleRecord> implements VertxPojo, Record2<String, String>, IClientSessionRole {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>keycloak.client_session_role.role_id</code>.
     */
    @Override
    public ClientSessionRoleRecord setRoleId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.client_session_role.role_id</code>.
     */
    @Override
    public String getRoleId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>keycloak.client_session_role.client_session</code>.
     */
    @Override
    public ClientSessionRoleRecord setClientSession(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>keycloak.client_session_role.client_session</code>.
     */
    @Override
    public String getClientSession() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, String> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return ClientSessionRole.CLIENT_SESSION_ROLE.ROLE_ID;
    }

    @Override
    public Field<String> field2() {
        return ClientSessionRole.CLIENT_SESSION_ROLE.CLIENT_SESSION;
    }

    @Override
    public String component1() {
        return getRoleId();
    }

    @Override
    public String component2() {
        return getClientSession();
    }

    @Override
    public String value1() {
        return getRoleId();
    }

    @Override
    public String value2() {
        return getClientSession();
    }

    @Override
    public ClientSessionRoleRecord value1(String value) {
        setRoleId(value);
        return this;
    }

    @Override
    public ClientSessionRoleRecord value2(String value) {
        setClientSession(value);
        return this;
    }

    @Override
    public ClientSessionRoleRecord values(String value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IClientSessionRole from) {
        setRoleId(from.getRoleId());
        setClientSession(from.getClientSession());
    }

    @Override
    public <E extends IClientSessionRole> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ClientSessionRoleRecord
     */
    public ClientSessionRoleRecord() {
        super(ClientSessionRole.CLIENT_SESSION_ROLE);
    }

    /**
     * Create a detached, initialised ClientSessionRoleRecord
     */
    public ClientSessionRoleRecord(String roleId, String clientSession) {
        super(ClientSessionRole.CLIENT_SESSION_ROLE);

        setRoleId(roleId);
        setClientSession(clientSession);
    }

    /**
     * Create a detached, initialised ClientSessionRoleRecord
     */
    public ClientSessionRoleRecord(com.hiddenswitch.framework.schema.keycloak.tables.pojos.ClientSessionRole value) {
        super(ClientSessionRole.CLIENT_SESSION_ROLE);

        if (value != null) {
            setRoleId(value.getRoleId());
            setClientSession(value.getClientSession());
        }
    }

        public ClientSessionRoleRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
