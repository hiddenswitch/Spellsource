/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables;


import com.hiddenswitch.framework.schema.keycloak.Keycloak;
import com.hiddenswitch.framework.schema.keycloak.Keys;
import com.hiddenswitch.framework.schema.keycloak.tables.records.ClientSessionRoleRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ClientSessionRole extends TableImpl<ClientSessionRoleRecord> {

    private static final long serialVersionUID = 1062594221;

    /**
     * The reference instance of <code>keycloak.client_session_role</code>
     */
    public static final ClientSessionRole CLIENT_SESSION_ROLE = new ClientSessionRole();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ClientSessionRoleRecord> getRecordType() {
        return ClientSessionRoleRecord.class;
    }

    /**
     * The column <code>keycloak.client_session_role.role_id</code>.
     */
    public final TableField<ClientSessionRoleRecord, String> ROLE_ID = createField(DSL.name("role_id"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>keycloak.client_session_role.client_session</code>.
     */
    public final TableField<ClientSessionRoleRecord, String> CLIENT_SESSION = createField(DSL.name("client_session"), org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * Create a <code>keycloak.client_session_role</code> table reference
     */
    public ClientSessionRole() {
        this(DSL.name("client_session_role"), null);
    }

    /**
     * Create an aliased <code>keycloak.client_session_role</code> table reference
     */
    public ClientSessionRole(String alias) {
        this(DSL.name(alias), CLIENT_SESSION_ROLE);
    }

    /**
     * Create an aliased <code>keycloak.client_session_role</code> table reference
     */
    public ClientSessionRole(Name alias) {
        this(alias, CLIENT_SESSION_ROLE);
    }

    private ClientSessionRole(Name alias, Table<ClientSessionRoleRecord> aliased) {
        this(alias, aliased, null);
    }

    private ClientSessionRole(Name alias, Table<ClientSessionRoleRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> ClientSessionRole(Table<O> child, ForeignKey<O, ClientSessionRoleRecord> key) {
        super(child, key, CLIENT_SESSION_ROLE);
    }

    @Override
    public Schema getSchema() {
        return Keycloak.KEYCLOAK;
    }

    @Override
    public UniqueKey<ClientSessionRoleRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_5;
    }

    @Override
    public List<UniqueKey<ClientSessionRoleRecord>> getKeys() {
        return Arrays.<UniqueKey<ClientSessionRoleRecord>>asList(Keys.CONSTRAINT_5);
    }

    @Override
    public List<ForeignKey<ClientSessionRoleRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ClientSessionRoleRecord, ?>>asList(Keys.CLIENT_SESSION_ROLE__FK_11B7SGQW18I532811V7O2DV76);
    }

    public ClientSession clientSession() {
        return new ClientSession(this, Keys.CLIENT_SESSION_ROLE__FK_11B7SGQW18I532811V7O2DV76);
    }

    @Override
    public ClientSessionRole as(String alias) {
        return new ClientSessionRole(DSL.name(alias), this);
    }

    @Override
    public ClientSessionRole as(Name alias) {
        return new ClientSessionRole(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientSessionRole rename(String name) {
        return new ClientSessionRole(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientSessionRole rename(Name name) {
        return new ClientSessionRole(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
