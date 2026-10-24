const assert = require('node:assert/strict');
const { Pool } = require('pg');
const { postgraphile } = require('postgraphile');
const { makePgService, makePgAdaptorWithPgClient } = require('@dataplan/pg/adaptors/pg');
const { PostGraphileAmberPreset } = require('postgraphile/presets/amber');
const { makeV4Preset } = require('postgraphile/presets/v4');
const { grafast } = require('grafast');
const plugin = require('../build/plugins/pg-omit-archived').default;

async function main() {
  if (!process.env.ARCHIVE_TEST_DATABASE_URL) throw new Error('Set ARCHIVE_TEST_DATABASE_URL to a disposable PostgreSQL database');
  const pool = new Pool({connectionString:process.env.ARCHIVE_TEST_DATABASE_URL});
  try {
    for (const timestamp of [false,true]) {
      const schemaName = `archive_test_${process.pid}_${timestamp ? 'time' : 'bool'}`;
      const column = timestamp ? 'archived_at' : 'is_archived';
      const type = timestamp ? 'timestamptz' : 'boolean';
      const visible = timestamp ? 'null' : 'false';
      const archived = timestamp ? "'2026-01-01'" : 'true';
      await pool.query(`create schema ${schemaName}; create table ${schemaName}.parents(id int primary key, ${column} ${type}); create table ${schemaName}.children(id int primary key, parent_id int references ${schemaName}.parents, ${column} ${type}); insert into ${schemaName}.parents values (1,${visible}),(2,${archived}); insert into ${schemaName}.children values (11,1,${visible}),(12,1,${archived}),(21,2,${visible}),(22,2,${archived});`);
      const instance = postgraphile({extends:[PostGraphileAmberPreset,makeV4Preset({simpleCollections:'both'})],plugins:[plugin],schema:{pgArchivedColumnName:column},pgServices:[makePgService({pool,schemas:[schemaName]})]});
      try {
        const schema = await instance.getSchema();
        const contextValue = {pgSettings:{},withPgClient:makePgAdaptorWithPgClient(pool)};
        const query = 'query($mode:IncludeArchivedOption){allParentsList(includeArchived:$mode,orderBy:ID_ASC){id childrenByParentIdList(orderBy:ID_ASC){id}}}';
        for (const [mode, expected] of [['NO',[{id:1,childrenByParentIdList:[{id:11}]}]],['YES',[{id:1,childrenByParentIdList:[{id:11}]},{id:2,childrenByParentIdList:[{id:21},{id:22}]}]],['EXCLUSIVELY',[{id:2,childrenByParentIdList:[{id:21},{id:22}]}]],['INHERIT',[{id:1,childrenByParentIdList:[{id:11}]}]],['NO',[{id:1,childrenByParentIdList:[{id:11}]}]]]) {
          const result = await grafast({schema,source:query,variableValues:{mode},contextValue});
          assert.equal(result.errors,undefined,JSON.stringify(result.errors));
          assert.deepEqual(JSON.parse(JSON.stringify(result.data.allParentsList)),expected,`${column} ${mode}`);
        }
        const result = await grafast({schema,source:'{allParentsList(orderBy:ID_ASC){id}}',contextValue});
        assert.equal(result.errors,undefined,JSON.stringify(result.errors));
        assert.deepEqual(JSON.parse(JSON.stringify(result.data.allParentsList)),[{id:1}]);
      } finally { await instance.release(); await pool.query(`drop schema ${schemaName} cascade`); }
    }
    console.log('Archive filter integration passed: boolean, timestamp, default, variables, inheritance and reused plans');
  } finally { await pool.end(); }
}
main().catch(error=>{console.error(error);process.exitCode=1;});
