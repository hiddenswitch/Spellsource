/*
 * This file is generated by jOOQ.
 */
package com.hiddenswitch.framework.schema.keycloak.tables;


import com.hiddenswitch.framework.schema.keycloak.Indexes;
import com.hiddenswitch.framework.schema.keycloak.Keycloak;
import com.hiddenswitch.framework.schema.keycloak.Keys;
import com.hiddenswitch.framework.schema.keycloak.tables.records.ClientInitialAccessRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row6;
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
public class ClientInitialAccess extends TableImpl<ClientInitialAccessRecord> {

    private static final long serialVersionUID = -450553076;

    /**
     * The reference instance of <code>keycloak.client_initial_access</code>
     */
    public static final ClientInitialAccess CLIENT_INITIAL_ACCESS = new ClientInitialAccess();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ClientInitialAccessRecord> getRecordType() {
        return ClientInitialAccessRecord.class;
    }

    /**
     * The column <code>keycloak.client_initial_access.id</code>.
     */
    public final TableField<ClientInitialAccessRecord, String> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>keycloak.client_initial_access.realm_id</code>.
     */
    public final TableField<ClientInitialAccessRecord, String> REALM_ID = createField(DSL.name("realm_id"), org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>keycloak.client_initial_access.timestamp</code>.
     */
    public final TableField<ClientInitialAccessRecord, Integer> TIMESTAMP = createField(DSL.name("timestamp"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>keycloak.client_initial_access.expiration</code>.
     */
    public final TableField<ClientInitialAccessRecord, Integer> EXPIRATION = createField(DSL.name("expiration"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>keycloak.client_initial_access.count</code>.
     */
    public final TableField<ClientInitialAccessRecord, Integer> COUNT = createField(DSL.name("count"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>keycloak.client_initial_access.remaining_count</code>.
     */
    public final TableField<ClientInitialAccessRecord, Integer> REMAINING_COUNT = createField(DSL.name("remaining_count"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>keycloak.client_initial_access</code> table reference
     */
    public ClientInitialAccess() {
        this(DSL.name("client_initial_access"), null);
    }

    /**
     * Create an aliased <code>keycloak.client_initial_access</code> table reference
     */
    public ClientInitialAccess(String alias) {
        this(DSL.name(alias), CLIENT_INITIAL_ACCESS);
    }

    /**
     * Create an aliased <code>keycloak.client_initial_access</code> table reference
     */
    public ClientInitialAccess(Name alias) {
        this(alias, CLIENT_INITIAL_ACCESS);
    }

    private ClientInitialAccess(Name alias, Table<ClientInitialAccessRecord> aliased) {
        this(alias, aliased, null);
    }

    private ClientInitialAccess(Name alias, Table<ClientInitialAccessRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> ClientInitialAccess(Table<O> child, ForeignKey<O, ClientInitialAccessRecord> key) {
        super(child, key, CLIENT_INITIAL_ACCESS);
    }

    @Override
    public Schema getSchema() {
        return Keycloak.KEYCLOAK;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.IDX_CLIENT_INIT_ACC_REALM);
    }

    @Override
    public UniqueKey<ClientInitialAccessRecord> getPrimaryKey() {
        return Keys.CNSTR_CLIENT_INIT_ACC_PK;
    }

    @Override
    public List<UniqueKey<ClientInitialAccessRecord>> getKeys() {
        return Arrays.<UniqueKey<ClientInitialAccessRecord>>asList(Keys.CNSTR_CLIENT_INIT_ACC_PK);
    }

    @Override
    public List<ForeignKey<ClientInitialAccessRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ClientInitialAccessRecord, ?>>asList(Keys.CLIENT_INITIAL_ACCESS__FK_CLIENT_INIT_ACC_REALM);
    }

    public Realm realm() {
        return new Realm(this, Keys.CLIENT_INITIAL_ACCESS__FK_CLIENT_INIT_ACC_REALM);
    }

    @Override
    public ClientInitialAccess as(String alias) {
        return new ClientInitialAccess(DSL.name(alias), this);
    }

    @Override
    public ClientInitialAccess as(Name alias) {
        return new ClientInitialAccess(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientInitialAccess rename(String name) {
        return new ClientInitialAccess(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ClientInitialAccess rename(Name name) {
        return new ClientInitialAccess(name, null);
    }

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row6<String, String, Integer, Integer, Integer, Integer> fieldsRow() {
        return (Row6) super.fieldsRow();
    }
}
