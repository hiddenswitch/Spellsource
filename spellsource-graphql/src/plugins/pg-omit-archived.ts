/**
 * This plugin was sponsored by Sprout LLC. 🙏
 *
 * https://sprout.io
 */

import type {} from "graphile-config";
import type {} from "graphile-build";
import type {} from "graphile-build-pg";
import type { PgSQL, SQL } from "pg-sql2";
import type { ConnectionStep, FieldArg, ExecutableStep } from "grafast";
import type {
  PgCodecRelation,
  PgCodecWithAttributes,
  PgRegistry,
  PgSelectStep,
  PgSelectQueryBuilder,
} from "@dataplan/pg";

declare global {
  namespace GraphileBuild {
    interface Inflection {
      // If you use other keywords, you will need to declaration merge your own inflectors for TypeScript.
      includeArchivedArgument(
        this: Inflection,
        details: {
          codec: PgCodecWithAttributes;
          registry: PgRegistry;
          relationName?: string;
        },
      ): string;
    }
    interface SchemaOptions {
      /**
       * The name of the column to use to determine if the record is archived
       * or not. Defaults to 'is_archived'
       */
      pgArchivedColumnName?: string;
      /**
       * Set this true to invert the column logic - i.e. if your column is
       * `is_visible` instead of `is_archived`.
       */
      pgArchivedColumnImpliesVisible?: boolean;
      /**
       * If your determination of whether a record is archived or not is more complex
       * than checking if a column is not null/not false then you can define an SQL
       * expression instead.
       */
      pgArchivedExpression?: (sql: PgSQL, tableAlias: SQL) => SQL;
      /**
       * The default option to use for the 'includeArchived' argument. Defaults
       * to 'NO', but will be replaced with 'INHERIT' where possible unless you set
       * `pgArchivedDefaultInherit` to false.
       */
      pgArchivedDefault?: "INHERIT" | "NO" | "YES" | "EXCLUSIVELY";
      /**
       * Set false if you don't want the system to default to 'INHERIT' if it's
       * able to do so.
       */
      pgArchivedDefaultInherit?: boolean;
      /**
       * Set true if you want related record collections to have the
       * pg-omit-archived behavior if they belong to a table that explicitly
       * matches.
       */
      pgArchivedRelations?: boolean;
      /**
       * If you want the system to apply the archived filter to a specific list of tables, list their names here:
       */
      pgArchivedTables?: string[];
    }
    interface ScopeObjectFieldsFieldArgs {
      /**
       * Set true if child fields should always include archived entries.
       */
      includeArchived?: boolean;
    }
  }
}
declare module "graphile-build-pg" {
  interface PgCodecRelationTags {
    archivedRelation?: boolean;
  }
}

/**
 * Build utils
 *
 * @param build - Graphile Build object
 * @param keyword - 'archived' or 'deleted' or similar
 * @param table - The table we're building a query against
 * @param parentTable - The table of the `parentQueryBuilder`, if any
 * @param allowInherit - Should we allow inheritance if it seems possible?
 */
const makeUtils = (
  build: GraphileBuild.Build,
  keyword: string,
  table: PgCodecWithAttributes,
  parentTable: PgCodecWithAttributes | undefined,
  allowInherit?: boolean,
  relevantRelationName?: string | null,
) => {
  const relevantRelation = relevantRelationName
    ? (build.input.pgRegistry.pgRelations[table.name][
        relevantRelationName
      ] as PgCodecRelation)
    : undefined;
  const Keyword = keyword[0].toUpperCase() + keyword.slice(1);
  const {
    inflection,
    getTypeByName,
    dataplanPg: { TYPES, PgSelectSingleStep },
    options: {
      [`pg${Keyword}ColumnName` as "pgArchivedColumnName"]:
        columnNameToCheck = `is_${keyword}`,
      // If true inverts the omitting logic (e.g. true for `is_published` or
      // `published_at`; false for `is_archived` or `archived_at`).
      [`pg${Keyword}ColumnImpliesVisible` as "pgArchivedColumnImpliesVisible"]:
        invert = false,
      [`pg${Keyword}Relations` as "pgArchivedRelations"]:
        applyToRelations = false,
      [`pg${Keyword}Expression` as "pgArchivedExpression"]: expression = null,
      [`pg${Keyword}Tables` as "pgArchivedTables"]: rawTables = null,
    },
  } = build;
  const sql = build.sql;
  const OptionType = getTypeByName(`Include${Keyword}Option`);
  if (!build.graphql.isEnumType(OptionType)) {
    throw new Error(`Include${Keyword}Option must be an enum`);
  }

  if (
    expression &&
    build.options[`pg${Keyword}ColumnName` as "pgArchivedColumnName"]
  ) {
    throw new Error(
      `'pg${Keyword}Expression' cannot be combined with 'pg${Keyword}ColumnName'`,
    );
  }
  if (
    expression &&
    build.options[
      `pg${Keyword}ColumnImpliesVisible` as "pgArchivedColumnImpliesVisible"
    ]
  ) {
    throw new Error(
      `'pg${Keyword}Expression' cannot be combined with 'pg${Keyword}ColumnImpliesVisible'`,
    );
  }
  if (
    expression &&
    !build.options[`pg${Keyword}Tables` as "pgArchivedTables"]
  ) {
    throw new Error(
      `'pg${Keyword}Expression' requires 'pg${Keyword}Tables' to be set to a list of the tables to which this expression can apply.`,
    );
  }

  const defaultValue =
    build.options[`pg${Keyword}Default` as "pgArchivedDefault"] || "NO";

  const tables = rawTables
    ? (rawTables as string[]).map((t) => {
        const [schemaOrTable, tableOnly, ...rest] = t.split(".");
        if (rest.length) {
          throw new Error("Could not parse ${t} into schema + table.");
        }
        return tableOnly
          ? [schemaOrTable, tableOnly]
          : ["public", schemaOrTable];
      })
    : null;

  const getRelevantColumn = (
    tableToCheck: PgCodecWithAttributes | null | undefined,
  ) => {
    const col = tableToCheck?.attributes[columnNameToCheck];
    if (col) {
      return { columnName: columnNameToCheck, column: col };
    }
  };

  const _tableIsAllowed = (table: PgCodecWithAttributes | null | undefined) =>
    table != null &&
    (tables == null ||
      tables.some(
        (t) =>
          table.extensions?.pg?.schemaName === t[0] &&
          table.extensions.pg.name === t[1],
      ));

  const appliesToTable = (table: PgCodecWithAttributes | undefined) =>
    _tableIsAllowed(table) && (expression || getRelevantColumn(table));

  if (relevantRelation) {
    if (
      !applyToRelations &&
      !relevantRelation.extensions?.tags?.[
        `${keyword}Relation` as "archivedRelation"
      ]
    ) {
      return null;
    }
    if (
      !appliesToTable(
        relevantRelation.remoteResource.codec as PgCodecWithAttributes,
      )
    ) {
      return null;
    }
  }

  const relevantClass = relevantRelation
    ? (relevantRelation.remoteResource.codec as PgCodecWithAttributes)
    : table;

  if (!relevantClass) {
    return null;
  }

  const relevantColumnDetails = getRelevantColumn(relevantClass);

  const argumentName = inflection[
    `include${Keyword}Argument` as "includeArchivedArgument"
  ]({
    codec: table,
    registry: build.input.pgRegistry,
    relationName: relevantRelationName ?? undefined,
  });

  const parentTableRelevantColumnDetails = getRelevantColumn(parentTable);
  const capableOfInherit = allowInherit && appliesToTable(parentTable);
  // TODO: In v4, both of these checks used the "type category" `B` to
  // determine it was boolean. Here we're only supporting the builtin boolean
  // (and not even a domain over it). I doubt this will cause issues for many
  // people (if any), but we should revisit.
  const pgRelevantColumnIsBoolean =
    relevantColumnDetails?.column.codec.name === "bool";
  const pgParentRelevantColumnIsBoolean =
    parentTableRelevantColumnDetails &&
    parentTableRelevantColumnDetails.column.codec.name === "bool";

  const booleanVisibleFragment = invert
    ? sql.fragment`true`
    : sql.fragment`false`;

  const booleanInvisibleFragment = invert
    ? sql.fragment`not true`
    : sql.fragment`not false`; // Keep in mind booleans are trinary in Postgres: true, false, null

  const nullableVisibleFragment = invert
    ? sql.fragment`not null`
    : sql.fragment`null`;

  const nullableInvisibleFragment = invert
    ? sql.fragment`null`
    : sql.fragment`not null`;

  const rawLocalDetails = expression
    ? appliesToTable(relevantClass)
      ? {
          expression: (_sql: PgSQL, tableAlias: SQL) =>
            sql.fragment`(${expression(sql, tableAlias)})`,
          visibleFragment: booleanVisibleFragment,
          invisibleFragment: booleanInvisibleFragment,
        }
      : null
    : relevantColumnDetails
    ? {
        expression: (_sql: typeof sql, tableAlias: SQL) =>
          sql.fragment`${tableAlias}.${sql.identifier(
            relevantColumnDetails.columnName,
          )}`,
        visibleFragment: pgRelevantColumnIsBoolean
          ? booleanVisibleFragment
          : nullableVisibleFragment,
        invisibleFragment: pgRelevantColumnIsBoolean
          ? booleanInvisibleFragment
          : nullableInvisibleFragment,
      }
    : null;

  if (!rawLocalDetails) {
    return null;
  }
  const localDetails = rawLocalDetails;

  const parentDetails = appliesToTable(parentTable)
    ? expression
      ? {
          expression: (_sql: typeof sql, tableAlias: SQL) =>
            sql.fragment`(${expression(sql, tableAlias)})`,
          visibleFragment: booleanVisibleFragment,
          invisibleFragment: booleanInvisibleFragment,
        }
      : parentTableRelevantColumnDetails
      ? {
          expression: (_sql: typeof sql, tableAlias: SQL) =>
            sql.fragment`${tableAlias}.${sql.identifier(
              parentTableRelevantColumnDetails.columnName,
            )}`,
          visibleFragment: pgParentRelevantColumnIsBoolean
            ? booleanVisibleFragment
            : nullableVisibleFragment,
          invisibleFragment: pgParentRelevantColumnIsBoolean
            ? booleanInvisibleFragment
            : nullableInvisibleFragment,
        }
      : null
    : null;

  function addWhereClause(
    $parent: ExecutableStep,
    $select: PgSelectStep,
    fieldArgs: FieldArg,
  ) {
    const $parentSelectSingle =
      $parent instanceof PgSelectSingleStep ? $parent : null;
    const { lambda } = build.lib.grafast;
    const inherited = capableOfInherit && $parentSelectSingle && parentDetails
      ? $select.placeholder($parentSelectSingle.select(
          sql.fragment`(${parentDetails.expression(sql, $parentSelectSingle.getClassStep().alias)} is ${parentDetails.invisibleFragment})`,
          TYPES.boolean,
        ))
      : null;
    // Evaluate enum variables per execution, including when Grafast reuses a plan.
    $select.apply(lambda(fieldArgs.getRaw(), (relevantSetting) => (queryBuilder: PgSelectQueryBuilder) => {
    let fragment: SQL | null = null;

    const myAlias =
      relevantClass !== table ? sql.identifier(Symbol("me")) : $select.alias;
    if (
      relevantRelation &&
      relevantClass !== table &&
      relevantRelation.remoteResource.codec === parentTable &&
      capableOfInherit &&
      $parentSelectSingle &&
      parentDetails &&
      ["INHERIT", "YES"].includes(relevantSetting)
    ) {
      // In this case the work is already done by the parent record and it
      // cannot be overridden by this level (since we don't have the relevant
      // field and we just import ours from the parent); no need to add
      // any extra WHERE clauses.
      return;
    }
    // INHERIT is equivalent to defaultValue if there's no valid parent
    const relevantSettingIfNotInherit =
      relevantSetting !== "INHERIT"
        ? relevantSetting
        : defaultValue !== "INHERIT"
        ? defaultValue
        : "NO";
    if (
      capableOfInherit &&
      relevantSetting === "INHERIT" &&
      $parentSelectSingle &&
      parentDetails &&
      inherited
    ) {
      fragment = sql.fragment`(${inherited} is true or ${localDetails.expression(sql, myAlias)} is ${localDetails.visibleFragment})`;
    } else if (relevantSettingIfNotInherit === "NO") {
      fragment = sql.fragment`${localDetails.expression(sql, myAlias)} is ${
        localDetails.visibleFragment
      }`;
    } else if (relevantSettingIfNotInherit === "EXCLUSIVELY") {
      fragment = sql.fragment`${localDetails.expression(sql, myAlias)} is ${
        localDetails.invisibleFragment
      }`;
    }
    if (fragment) {
      if (relevantRelation && relevantClass !== table) {
        const localAlias = $select.alias;
        const relationConditions = relevantRelation.localAttributes.map(
          (attrName, i) => {
            const otherAttrName = relevantRelation.remoteAttributes[i];
            return sql.fragment`${localAlias}.${sql.identifier(
              attrName,
            )} = ${myAlias}.${sql.identifier(otherAttrName)}`;
          },
        );
        const subquery = sql.fragment`exists (select 1 from ${
          relevantRelation.remoteResource.from as SQL
        } as ${myAlias} where (${sql.join(
          relationConditions,
          ") and (",
        )}) and (${fragment}))`;
        queryBuilder.where(subquery);
      } else {
        queryBuilder.where(fragment);
      }
    }
    }));
  }
  return {
    OptionType,
    addWhereClause,
    capableOfInherit,
    argumentName,
  };
};

/*
 * keyword should probably end in 'ed', e.g. 'archived', 'deleted',
 * 'eradicated', 'unpublished', though 'scheduledForDeletion' is probably okay,
 * as is 'template' or 'draft' - have a read through where it's used and judge
 * for yourself
 */
const generator = (keyword = "archived"): GraphileConfig.Plugin => {
  const Keyword = keyword[0].toUpperCase() + keyword.slice(1);

  return {
    name: `PgOmit${Keyword}Plugin`,
    version: "1.0.0",

    inflection: {
      add: {
        [`include${Keyword}Argument` as "includeArchivedArgument"](
          _options,
          { codec, registry, relationName },
        ) {
          const relationPart = relationName
            ? this.singleRelation({
                codec,
                registry,
                relationName,
              })
            : null;
          const argumentName = relationPart
            ? `includeWhen${this.upperCamelCase(relationPart)}${Keyword}`
            : `include${Keyword}`;
          return argumentName;
        },
      },
    },

    schema: {
      hooks: {
        init(_, build) {
          const defaultValue =
            build.options[`pg${Keyword}Default` as "pgArchivedDefault"] || "NO";
          build.registerEnumType(
            `Include${Keyword}Option`,
            {
              [`isInclude${Keyword}OptionEnum`]: true,
            },
            () => ({
              name: `Include${Keyword}Option`,
              description: `Indicates whether ${keyword} items should be included in the results or not.`,
              values: {
                NO: {
                  value: "NO",
                  description: `Exclude ${keyword} items.`,
                },
                YES: {
                  description: `Include ${keyword} items.`,
                  value: "YES",
                },
                EXCLUSIVELY: {
                  description: `Only include ${keyword} items (i.e. exclude non-${keyword} items).`,
                  value: "EXCLUSIVELY",
                },
                INHERIT: {
                  description: `If there is a parent GraphQL record and it is ${keyword} then this is equivalent to YES, in all other cases this is equivalent to ${
                    defaultValue === "INHERIT" ? "NO" : defaultValue
                  }.`,
                  value: "INHERIT",
                },
              },
            }),
            "",
          );
          return _;
        },
        GraphQLObjectType_fields_field_args(args, build, context) {
          const { extend } = build;
          const {
            scope: {
              isPgFieldConnection,
              isPgFieldSimpleCollection,
              pgRelationDetails,
              pgCodec: rawPgCodec,
              pgTypeResource,
              pgFieldCodec: rawPgFieldCodec,
              pgFieldResource,
              //pgFieldIntrospection,
              // pgIntrospection: parentTable,
              [`include${Keyword}` as "includeArchived"]: includeArchived,
              fieldName,
            },
            Self,
          } = context;
          const pgFieldCodec = rawPgFieldCodec ?? pgFieldResource?.codec;
          const pgCodec = rawPgCodec ?? pgTypeResource?.codec;
          const interesting = fieldName === "allParentsList";
          const relation = pgRelationDetails
            ? pgRelationDetails.registry.pgRelations[
                pgRelationDetails.codec.name
              ][pgRelationDetails.relationName]
            : null;
          const isPgBackwardRelationField = relation?.isReferencee;
          const defaultValue =
            build.options[`pg${Keyword}Default` as "pgArchivedDefault"] || "NO";
          const defaultInherit =
            build.options[
              `pg${Keyword}DefaultInherit` as "pgArchivedDefaultInherit"
            ] !== false;
          if (
            !(isPgFieldConnection || isPgFieldSimpleCollection) ||
            !pgFieldCodec ||
            !pgFieldCodec.attributes ||
            includeArchived
          ) {
            return args;
          }
          const allowInherit = isPgBackwardRelationField;
          const relationsLookup = build.input.pgRegistry.pgRelations[
            pgFieldCodec.name
          ] as Record<string, PgCodecRelation>;
          const relationNames = relationsLookup
            ? (Object.entries(relationsLookup)
                .filter(([, relation]) => !relation.isReferencee)
                .map(([name]) => name) as string[])
            : [];
          const selfAndRelationNames = [null, ...relationNames];
          const allUtils = selfAndRelationNames.map((relationName) =>
            makeUtils(
              build,
              keyword,
              pgFieldCodec,
              pgCodec,
              allowInherit,
              relationName,
            ),
          );
          if (!allUtils) {
            return args;
          }
          return allUtils.reduce((args, utils) => {
            if (!utils) {
              return args;
            }
            const {
              addWhereClause,
              OptionType,
              capableOfInherit,
              argumentName,
            } = utils;
            if (!!args[argumentName]) {
              return args;
            }

            return extend(
              args,
              {
                [argumentName]: {
                  description: `Indicates whether ${keyword} items should be included in the results or not.`,
                  type: OptionType,
                  defaultValue:
                    capableOfInherit && defaultInherit
                      ? "INHERIT"
                      : defaultValue,
                  applyPlan: isPgFieldConnection
                    ? (
                        $parent: ExecutableStep,
                        $connection: ConnectionStep<any, any, any, any, any, PgSelectStep>,
                        arg,
                      ) => {
                        const $select = $connection.getSubplan();
                        addWhereClause($parent, $select, arg);
                      }
                    : ($parent: ExecutableStep, $select: PgSelectStep, arg) => {
                        addWhereClause($parent, $select, arg);
                      },
                },
              },
              `Adding ${argumentName} argument to connection field '${fieldName}' of '${Self.name}'`,
            );
          }, args);
        },
      },
    },
  };
};

const PgOmitArchivedPlugin = Object.assign(generator(), {
  custom: generator,
  makeUtils,
});
export default PgOmitArchivedPlugin;
export { generator as custom, makeUtils, PgOmitArchivedPlugin };
