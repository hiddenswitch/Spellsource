/* eslint-disable */
import { type CallOptions, ChannelCredentials, Client, type ClientOptions, type ClientUnaryCall, type handleUnaryCall, makeGenericClientConstructor, Metadata, type ServiceError, type UntypedServiceImplementation } from "@grpc/grpc-js";
import Long from "long";
import _m0 from "protobufjs/minimal";
import { Empty } from "./google/protobuf/empty";
import { BoolValue, StringValue } from "./google/protobuf/wrappers";
import { CardRecord } from "./spellsource";

export const protobufPackage = "hiddenswitch";

/** Specifies a complete server configuration */
export interface ServerConfiguration {
  pg: ServerConfiguration_PostgresConfiguration | undefined;
  keycloak: ServerConfiguration_KeycloakConfiguration | undefined;
  redis: ServerConfiguration_RedisConfiguration | undefined;
  grpcConfiguration: ServerConfiguration_GrpcConfiguration | undefined;
  matchmaking: ServerConfiguration_MatchmakingConfiguration | undefined;
  application: ServerConfiguration_ApplicationConfiguration | undefined;
  decks: ServerConfiguration_DecksConfiguration | undefined;
  migration: ServerConfiguration_MigrationConfiguration | undefined;
  metrics: ServerConfiguration_MetricsConfiguration | undefined;
  rateLimiter: ServerConfiguration_RateLimiterConfiguration | undefined;
  jaeger: ServerConfiguration_JaegerConfiguration | undefined;
  vertx: ServerConfiguration_VertxConfiguration | undefined;
  cards: ServerConfiguration_CardsConfiguration | undefined;
  graphql: ServerConfiguration_GraphQLConfiguration | undefined;
}

export interface ServerConfiguration_PostgresConfiguration {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
}

export interface ServerConfiguration_KeycloakConfiguration {
  authUrl: string;
  publicAuthUrl: string;
  adminUsername: string;
  adminPassword: string;
  clientId: string;
  clientSecret: string;
  realmDisplayName: string;
  realmId: string;
}

export interface ServerConfiguration_RedisConfiguration {
  connection?:
    | { $case: "hostPortUser"; hostPortUser: ServerConfiguration_RedisConfiguration_HostPortUser }
    | {
        $case: "uri";
        uri: string;
      }
    | undefined;
}

export interface ServerConfiguration_RedisConfiguration_HostPortUser {
  host: string;
  port: number;
  user: string;
}

export interface ServerConfiguration_GrpcConfiguration {
  serverKeepAliveTimeMillis: number;
  serverKeepAliveTimeoutMillis: number;
  serverPermitKeepAliveWithoutCalls: boolean;
  port: number;
}

export interface ServerConfiguration_MatchmakingConfiguration {
  enqueueLockTimeoutMillis: number;
  scanFrequencyMillis: number;
  maxTicketsToProcess: number;
}

export interface ServerConfiguration_DecksConfiguration {
  cachedDeckTimeToLiveMinutes: number;
}

export interface ServerConfiguration_ApplicationConfiguration {
  useBroadcaster: boolean;
}

export interface ServerConfiguration_MigrationConfiguration {
  shouldMigrate: boolean;
}

export interface ServerConfiguration_MetricsConfiguration {
  port: number;
  livenessRoute: string;
  readinessRoute: string;
  metricsRoute: string;
}

export interface ServerConfiguration_RateLimiterConfiguration {
  enabled: boolean;
}

export interface ServerConfiguration_JaegerConfiguration {
  enabled: boolean;
  agentHost: string;
  agentPort: number;
}

export interface ServerConfiguration_VertxConfiguration {
  useInfinispanClusterManager: boolean;
  infinspanPort?: number | undefined;
  infinispanAddresses: string;
}

export interface ServerConfiguration_CardsConfiguration {}

export interface ServerConfiguration_GraphQLConfiguration {
  url: string;
}

export interface ClientConfiguration {
  accounts: ClientConfiguration_AccountsConfiguration | undefined;
  graphQl: ClientConfiguration_GraphQlConfiguration | undefined;
}

export interface ClientConfiguration_AccountsConfiguration {
  keycloakResetPasswordUrl: string;
  keycloakAccountManagementUrl: string;
}

export interface ClientConfiguration_GraphQlConfiguration {
  graphQlUrl: string;
}

export interface GetCardsRequest {
  /**
   * The value returned in the ETag header from the server when this was last called, or empty if this is the
   * first call to this resource.
   */
  IfNoneMatch: string;
  /** the creator of the cards to retrieve */
  userId: string;
}

/** A cacheable copy of the entire card catalogue. */
export interface GetCardsResponse {
  content: GetCardsResponse_Content | undefined;
  /** A token used in the If-None-Match argument when checking for new card catalogue content. */
  version: string;
  cachedOk: boolean;
}

export interface GetCardsResponse_Content {
  /** The actual array of cards representing the complete Spellsource catalogue. */
  cards: CardRecord[];
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface CreateAccountRequest {
  email: string;
  username: string;
  password: string;
  decks: boolean;
  guest: boolean;
}

export interface ChangePasswordRequest {
  newPassword: string;
}

export interface LoginOrCreateReply {
  accessTokenResponse: AccessTokenResponse | undefined;
  userEntity: UserEntity | undefined;
}

export interface AccessTokenResponse {
  token: string;
}

export interface UserEntity {
  id: string;
  email: string;
  username: string;
  privacyToken: string;
}

export interface GetAccountsRequest {
  ids: string[];
}

export interface GetAccountsReply {
  userEntities: UserEntity[];
}

function createBaseServerConfiguration(): ServerConfiguration {
  return {
    pg: undefined,
    keycloak: undefined,
    redis: undefined,
    grpcConfiguration: undefined,
    matchmaking: undefined,
    application: undefined,
    decks: undefined,
    migration: undefined,
    metrics: undefined,
    rateLimiter: undefined,
    jaeger: undefined,
    vertx: undefined,
    cards: undefined,
    graphql: undefined,
  };
}

export const ServerConfiguration = {
  encode(message: ServerConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.pg !== undefined) {
      ServerConfiguration_PostgresConfiguration.encode(message.pg, writer.uint32(10).fork()).ldelim();
    }
    if (message.keycloak !== undefined) {
      ServerConfiguration_KeycloakConfiguration.encode(message.keycloak, writer.uint32(18).fork()).ldelim();
    }
    if (message.redis !== undefined) {
      ServerConfiguration_RedisConfiguration.encode(message.redis, writer.uint32(26).fork()).ldelim();
    }
    if (message.grpcConfiguration !== undefined) {
      ServerConfiguration_GrpcConfiguration.encode(message.grpcConfiguration, writer.uint32(42).fork()).ldelim();
    }
    if (message.matchmaking !== undefined) {
      ServerConfiguration_MatchmakingConfiguration.encode(message.matchmaking, writer.uint32(50).fork()).ldelim();
    }
    if (message.application !== undefined) {
      ServerConfiguration_ApplicationConfiguration.encode(message.application, writer.uint32(58).fork()).ldelim();
    }
    if (message.decks !== undefined) {
      ServerConfiguration_DecksConfiguration.encode(message.decks, writer.uint32(66).fork()).ldelim();
    }
    if (message.migration !== undefined) {
      ServerConfiguration_MigrationConfiguration.encode(message.migration, writer.uint32(74).fork()).ldelim();
    }
    if (message.metrics !== undefined) {
      ServerConfiguration_MetricsConfiguration.encode(message.metrics, writer.uint32(82).fork()).ldelim();
    }
    if (message.rateLimiter !== undefined) {
      ServerConfiguration_RateLimiterConfiguration.encode(message.rateLimiter, writer.uint32(90).fork()).ldelim();
    }
    if (message.jaeger !== undefined) {
      ServerConfiguration_JaegerConfiguration.encode(message.jaeger, writer.uint32(98).fork()).ldelim();
    }
    if (message.vertx !== undefined) {
      ServerConfiguration_VertxConfiguration.encode(message.vertx, writer.uint32(106).fork()).ldelim();
    }
    if (message.cards !== undefined) {
      ServerConfiguration_CardsConfiguration.encode(message.cards, writer.uint32(114).fork()).ldelim();
    }
    if (message.graphql !== undefined) {
      ServerConfiguration_GraphQLConfiguration.encode(message.graphql, writer.uint32(122).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.pg = ServerConfiguration_PostgresConfiguration.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.keycloak = ServerConfiguration_KeycloakConfiguration.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.redis = ServerConfiguration_RedisConfiguration.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.grpcConfiguration = ServerConfiguration_GrpcConfiguration.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.matchmaking = ServerConfiguration_MatchmakingConfiguration.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.application = ServerConfiguration_ApplicationConfiguration.decode(reader, reader.uint32());
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.decks = ServerConfiguration_DecksConfiguration.decode(reader, reader.uint32());
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.migration = ServerConfiguration_MigrationConfiguration.decode(reader, reader.uint32());
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.metrics = ServerConfiguration_MetricsConfiguration.decode(reader, reader.uint32());
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.rateLimiter = ServerConfiguration_RateLimiterConfiguration.decode(reader, reader.uint32());
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.jaeger = ServerConfiguration_JaegerConfiguration.decode(reader, reader.uint32());
          continue;
        case 13:
          if (tag !== 106) {
            break;
          }

          message.vertx = ServerConfiguration_VertxConfiguration.decode(reader, reader.uint32());
          continue;
        case 14:
          if (tag !== 114) {
            break;
          }

          message.cards = ServerConfiguration_CardsConfiguration.decode(reader, reader.uint32());
          continue;
        case 15:
          if (tag !== 122) {
            break;
          }

          message.graphql = ServerConfiguration_GraphQLConfiguration.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration>, I>>(base?: I): ServerConfiguration {
    return ServerConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration>, I>>(object: I): ServerConfiguration {
    const message = createBaseServerConfiguration();
    message.pg = object.pg !== undefined && object.pg !== null ? ServerConfiguration_PostgresConfiguration.fromPartial(object.pg) : undefined;
    message.keycloak = object.keycloak !== undefined && object.keycloak !== null ? ServerConfiguration_KeycloakConfiguration.fromPartial(object.keycloak) : undefined;
    message.redis = object.redis !== undefined && object.redis !== null ? ServerConfiguration_RedisConfiguration.fromPartial(object.redis) : undefined;
    message.grpcConfiguration = object.grpcConfiguration !== undefined && object.grpcConfiguration !== null ? ServerConfiguration_GrpcConfiguration.fromPartial(object.grpcConfiguration) : undefined;
    message.matchmaking = object.matchmaking !== undefined && object.matchmaking !== null ? ServerConfiguration_MatchmakingConfiguration.fromPartial(object.matchmaking) : undefined;
    message.application = object.application !== undefined && object.application !== null ? ServerConfiguration_ApplicationConfiguration.fromPartial(object.application) : undefined;
    message.decks = object.decks !== undefined && object.decks !== null ? ServerConfiguration_DecksConfiguration.fromPartial(object.decks) : undefined;
    message.migration = object.migration !== undefined && object.migration !== null ? ServerConfiguration_MigrationConfiguration.fromPartial(object.migration) : undefined;
    message.metrics = object.metrics !== undefined && object.metrics !== null ? ServerConfiguration_MetricsConfiguration.fromPartial(object.metrics) : undefined;
    message.rateLimiter = object.rateLimiter !== undefined && object.rateLimiter !== null ? ServerConfiguration_RateLimiterConfiguration.fromPartial(object.rateLimiter) : undefined;
    message.jaeger = object.jaeger !== undefined && object.jaeger !== null ? ServerConfiguration_JaegerConfiguration.fromPartial(object.jaeger) : undefined;
    message.vertx = object.vertx !== undefined && object.vertx !== null ? ServerConfiguration_VertxConfiguration.fromPartial(object.vertx) : undefined;
    message.cards = object.cards !== undefined && object.cards !== null ? ServerConfiguration_CardsConfiguration.fromPartial(object.cards) : undefined;
    message.graphql = object.graphql !== undefined && object.graphql !== null ? ServerConfiguration_GraphQLConfiguration.fromPartial(object.graphql) : undefined;
    return message;
  },
};

function createBaseServerConfiguration_PostgresConfiguration(): ServerConfiguration_PostgresConfiguration {
  return { host: "", port: 0, database: "", user: "", password: "" };
}

export const ServerConfiguration_PostgresConfiguration = {
  encode(message: ServerConfiguration_PostgresConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.host !== "") {
      writer.uint32(10).string(message.host);
    }
    if (message.port !== 0) {
      writer.uint32(16).int32(message.port);
    }
    if (message.database !== "") {
      writer.uint32(26).string(message.database);
    }
    if (message.user !== "") {
      writer.uint32(34).string(message.user);
    }
    if (message.password !== "") {
      writer.uint32(42).string(message.password);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_PostgresConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_PostgresConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.host = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.port = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.database = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.user = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.password = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_PostgresConfiguration>, I>>(base?: I): ServerConfiguration_PostgresConfiguration {
    return ServerConfiguration_PostgresConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_PostgresConfiguration>, I>>(object: I): ServerConfiguration_PostgresConfiguration {
    const message = createBaseServerConfiguration_PostgresConfiguration();
    message.host = object.host ?? "";
    message.port = object.port ?? 0;
    message.database = object.database ?? "";
    message.user = object.user ?? "";
    message.password = object.password ?? "";
    return message;
  },
};

function createBaseServerConfiguration_KeycloakConfiguration(): ServerConfiguration_KeycloakConfiguration {
  return {
    authUrl: "",
    publicAuthUrl: "",
    adminUsername: "",
    adminPassword: "",
    clientId: "",
    clientSecret: "",
    realmDisplayName: "",
    realmId: "",
  };
}

export const ServerConfiguration_KeycloakConfiguration = {
  encode(message: ServerConfiguration_KeycloakConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.authUrl !== "") {
      writer.uint32(10).string(message.authUrl);
    }
    if (message.publicAuthUrl !== "") {
      writer.uint32(66).string(message.publicAuthUrl);
    }
    if (message.adminUsername !== "") {
      writer.uint32(18).string(message.adminUsername);
    }
    if (message.adminPassword !== "") {
      writer.uint32(26).string(message.adminPassword);
    }
    if (message.clientId !== "") {
      writer.uint32(34).string(message.clientId);
    }
    if (message.clientSecret !== "") {
      writer.uint32(42).string(message.clientSecret);
    }
    if (message.realmDisplayName !== "") {
      writer.uint32(50).string(message.realmDisplayName);
    }
    if (message.realmId !== "") {
      writer.uint32(58).string(message.realmId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_KeycloakConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_KeycloakConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.authUrl = reader.string();
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.publicAuthUrl = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.adminUsername = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.adminPassword = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.clientId = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.clientSecret = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.realmDisplayName = reader.string();
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.realmId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_KeycloakConfiguration>, I>>(base?: I): ServerConfiguration_KeycloakConfiguration {
    return ServerConfiguration_KeycloakConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_KeycloakConfiguration>, I>>(object: I): ServerConfiguration_KeycloakConfiguration {
    const message = createBaseServerConfiguration_KeycloakConfiguration();
    message.authUrl = object.authUrl ?? "";
    message.publicAuthUrl = object.publicAuthUrl ?? "";
    message.adminUsername = object.adminUsername ?? "";
    message.adminPassword = object.adminPassword ?? "";
    message.clientId = object.clientId ?? "";
    message.clientSecret = object.clientSecret ?? "";
    message.realmDisplayName = object.realmDisplayName ?? "";
    message.realmId = object.realmId ?? "";
    return message;
  },
};

function createBaseServerConfiguration_RedisConfiguration(): ServerConfiguration_RedisConfiguration {
  return { connection: undefined };
}

export const ServerConfiguration_RedisConfiguration = {
  encode(message: ServerConfiguration_RedisConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    switch (message.connection?.$case) {
      case "hostPortUser":
        ServerConfiguration_RedisConfiguration_HostPortUser.encode(message.connection.hostPortUser, writer.uint32(10).fork()).ldelim();
        break;
      case "uri":
        writer.uint32(18).string(message.connection.uri);
        break;
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_RedisConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_RedisConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.connection = {
            $case: "hostPortUser",
            hostPortUser: ServerConfiguration_RedisConfiguration_HostPortUser.decode(reader, reader.uint32()),
          };
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.connection = { $case: "uri", uri: reader.string() };
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_RedisConfiguration>, I>>(base?: I): ServerConfiguration_RedisConfiguration {
    return ServerConfiguration_RedisConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_RedisConfiguration>, I>>(object: I): ServerConfiguration_RedisConfiguration {
    const message = createBaseServerConfiguration_RedisConfiguration();
    if (object.connection?.$case === "hostPortUser" && object.connection?.hostPortUser !== undefined && object.connection?.hostPortUser !== null) {
      message.connection = {
        $case: "hostPortUser",
        hostPortUser: ServerConfiguration_RedisConfiguration_HostPortUser.fromPartial(object.connection.hostPortUser),
      };
    }
    if (object.connection?.$case === "uri" && object.connection?.uri !== undefined && object.connection?.uri !== null) {
      message.connection = { $case: "uri", uri: object.connection.uri };
    }
    return message;
  },
};

function createBaseServerConfiguration_RedisConfiguration_HostPortUser(): ServerConfiguration_RedisConfiguration_HostPortUser {
  return { host: "", port: 0, user: "" };
}

export const ServerConfiguration_RedisConfiguration_HostPortUser = {
  encode(message: ServerConfiguration_RedisConfiguration_HostPortUser, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.host !== "") {
      writer.uint32(10).string(message.host);
    }
    if (message.port !== 0) {
      writer.uint32(16).int32(message.port);
    }
    if (message.user !== "") {
      writer.uint32(26).string(message.user);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_RedisConfiguration_HostPortUser {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_RedisConfiguration_HostPortUser();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.host = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.port = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.user = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_RedisConfiguration_HostPortUser>, I>>(base?: I): ServerConfiguration_RedisConfiguration_HostPortUser {
    return ServerConfiguration_RedisConfiguration_HostPortUser.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_RedisConfiguration_HostPortUser>, I>>(object: I): ServerConfiguration_RedisConfiguration_HostPortUser {
    const message = createBaseServerConfiguration_RedisConfiguration_HostPortUser();
    message.host = object.host ?? "";
    message.port = object.port ?? 0;
    message.user = object.user ?? "";
    return message;
  },
};

function createBaseServerConfiguration_GrpcConfiguration(): ServerConfiguration_GrpcConfiguration {
  return {
    serverKeepAliveTimeMillis: 0,
    serverKeepAliveTimeoutMillis: 0,
    serverPermitKeepAliveWithoutCalls: false,
    port: 0,
  };
}

export const ServerConfiguration_GrpcConfiguration = {
  encode(message: ServerConfiguration_GrpcConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.serverKeepAliveTimeMillis !== 0) {
      writer.uint32(8).int64(message.serverKeepAliveTimeMillis);
    }
    if (message.serverKeepAliveTimeoutMillis !== 0) {
      writer.uint32(16).int64(message.serverKeepAliveTimeoutMillis);
    }
    if (message.serverPermitKeepAliveWithoutCalls === true) {
      writer.uint32(24).bool(message.serverPermitKeepAliveWithoutCalls);
    }
    if (message.port !== 0) {
      writer.uint32(32).int32(message.port);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_GrpcConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_GrpcConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.serverKeepAliveTimeMillis = longToNumber(reader.int64() as Long);
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.serverKeepAliveTimeoutMillis = longToNumber(reader.int64() as Long);
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.serverPermitKeepAliveWithoutCalls = reader.bool();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.port = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_GrpcConfiguration>, I>>(base?: I): ServerConfiguration_GrpcConfiguration {
    return ServerConfiguration_GrpcConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_GrpcConfiguration>, I>>(object: I): ServerConfiguration_GrpcConfiguration {
    const message = createBaseServerConfiguration_GrpcConfiguration();
    message.serverKeepAliveTimeMillis = object.serverKeepAliveTimeMillis ?? 0;
    message.serverKeepAliveTimeoutMillis = object.serverKeepAliveTimeoutMillis ?? 0;
    message.serverPermitKeepAliveWithoutCalls = object.serverPermitKeepAliveWithoutCalls ?? false;
    message.port = object.port ?? 0;
    return message;
  },
};

function createBaseServerConfiguration_MatchmakingConfiguration(): ServerConfiguration_MatchmakingConfiguration {
  return { enqueueLockTimeoutMillis: 0, scanFrequencyMillis: 0, maxTicketsToProcess: 0 };
}

export const ServerConfiguration_MatchmakingConfiguration = {
  encode(message: ServerConfiguration_MatchmakingConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.enqueueLockTimeoutMillis !== 0) {
      writer.uint32(8).int64(message.enqueueLockTimeoutMillis);
    }
    if (message.scanFrequencyMillis !== 0) {
      writer.uint32(16).int64(message.scanFrequencyMillis);
    }
    if (message.maxTicketsToProcess !== 0) {
      writer.uint32(24).int32(message.maxTicketsToProcess);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_MatchmakingConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_MatchmakingConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.enqueueLockTimeoutMillis = longToNumber(reader.int64() as Long);
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.scanFrequencyMillis = longToNumber(reader.int64() as Long);
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.maxTicketsToProcess = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_MatchmakingConfiguration>, I>>(base?: I): ServerConfiguration_MatchmakingConfiguration {
    return ServerConfiguration_MatchmakingConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_MatchmakingConfiguration>, I>>(object: I): ServerConfiguration_MatchmakingConfiguration {
    const message = createBaseServerConfiguration_MatchmakingConfiguration();
    message.enqueueLockTimeoutMillis = object.enqueueLockTimeoutMillis ?? 0;
    message.scanFrequencyMillis = object.scanFrequencyMillis ?? 0;
    message.maxTicketsToProcess = object.maxTicketsToProcess ?? 0;
    return message;
  },
};

function createBaseServerConfiguration_DecksConfiguration(): ServerConfiguration_DecksConfiguration {
  return { cachedDeckTimeToLiveMinutes: 0 };
}

export const ServerConfiguration_DecksConfiguration = {
  encode(message: ServerConfiguration_DecksConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.cachedDeckTimeToLiveMinutes !== 0) {
      writer.uint32(8).int64(message.cachedDeckTimeToLiveMinutes);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_DecksConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_DecksConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.cachedDeckTimeToLiveMinutes = longToNumber(reader.int64() as Long);
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_DecksConfiguration>, I>>(base?: I): ServerConfiguration_DecksConfiguration {
    return ServerConfiguration_DecksConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_DecksConfiguration>, I>>(object: I): ServerConfiguration_DecksConfiguration {
    const message = createBaseServerConfiguration_DecksConfiguration();
    message.cachedDeckTimeToLiveMinutes = object.cachedDeckTimeToLiveMinutes ?? 0;
    return message;
  },
};

function createBaseServerConfiguration_ApplicationConfiguration(): ServerConfiguration_ApplicationConfiguration {
  return { useBroadcaster: false };
}

export const ServerConfiguration_ApplicationConfiguration = {
  encode(message: ServerConfiguration_ApplicationConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.useBroadcaster === true) {
      writer.uint32(8).bool(message.useBroadcaster);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_ApplicationConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_ApplicationConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.useBroadcaster = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_ApplicationConfiguration>, I>>(base?: I): ServerConfiguration_ApplicationConfiguration {
    return ServerConfiguration_ApplicationConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_ApplicationConfiguration>, I>>(object: I): ServerConfiguration_ApplicationConfiguration {
    const message = createBaseServerConfiguration_ApplicationConfiguration();
    message.useBroadcaster = object.useBroadcaster ?? false;
    return message;
  },
};

function createBaseServerConfiguration_MigrationConfiguration(): ServerConfiguration_MigrationConfiguration {
  return { shouldMigrate: false };
}

export const ServerConfiguration_MigrationConfiguration = {
  encode(message: ServerConfiguration_MigrationConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.shouldMigrate === true) {
      writer.uint32(8).bool(message.shouldMigrate);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_MigrationConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_MigrationConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.shouldMigrate = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_MigrationConfiguration>, I>>(base?: I): ServerConfiguration_MigrationConfiguration {
    return ServerConfiguration_MigrationConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_MigrationConfiguration>, I>>(object: I): ServerConfiguration_MigrationConfiguration {
    const message = createBaseServerConfiguration_MigrationConfiguration();
    message.shouldMigrate = object.shouldMigrate ?? false;
    return message;
  },
};

function createBaseServerConfiguration_MetricsConfiguration(): ServerConfiguration_MetricsConfiguration {
  return { port: 0, livenessRoute: "", readinessRoute: "", metricsRoute: "" };
}

export const ServerConfiguration_MetricsConfiguration = {
  encode(message: ServerConfiguration_MetricsConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.port !== 0) {
      writer.uint32(8).int32(message.port);
    }
    if (message.livenessRoute !== "") {
      writer.uint32(18).string(message.livenessRoute);
    }
    if (message.readinessRoute !== "") {
      writer.uint32(26).string(message.readinessRoute);
    }
    if (message.metricsRoute !== "") {
      writer.uint32(34).string(message.metricsRoute);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_MetricsConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_MetricsConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.port = reader.int32();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.livenessRoute = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.readinessRoute = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.metricsRoute = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_MetricsConfiguration>, I>>(base?: I): ServerConfiguration_MetricsConfiguration {
    return ServerConfiguration_MetricsConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_MetricsConfiguration>, I>>(object: I): ServerConfiguration_MetricsConfiguration {
    const message = createBaseServerConfiguration_MetricsConfiguration();
    message.port = object.port ?? 0;
    message.livenessRoute = object.livenessRoute ?? "";
    message.readinessRoute = object.readinessRoute ?? "";
    message.metricsRoute = object.metricsRoute ?? "";
    return message;
  },
};

function createBaseServerConfiguration_RateLimiterConfiguration(): ServerConfiguration_RateLimiterConfiguration {
  return { enabled: false };
}

export const ServerConfiguration_RateLimiterConfiguration = {
  encode(message: ServerConfiguration_RateLimiterConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.enabled === true) {
      writer.uint32(8).bool(message.enabled);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_RateLimiterConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_RateLimiterConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.enabled = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_RateLimiterConfiguration>, I>>(base?: I): ServerConfiguration_RateLimiterConfiguration {
    return ServerConfiguration_RateLimiterConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_RateLimiterConfiguration>, I>>(object: I): ServerConfiguration_RateLimiterConfiguration {
    const message = createBaseServerConfiguration_RateLimiterConfiguration();
    message.enabled = object.enabled ?? false;
    return message;
  },
};

function createBaseServerConfiguration_JaegerConfiguration(): ServerConfiguration_JaegerConfiguration {
  return { enabled: false, agentHost: "", agentPort: 0 };
}

export const ServerConfiguration_JaegerConfiguration = {
  encode(message: ServerConfiguration_JaegerConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.enabled === true) {
      writer.uint32(8).bool(message.enabled);
    }
    if (message.agentHost !== "") {
      writer.uint32(18).string(message.agentHost);
    }
    if (message.agentPort !== 0) {
      writer.uint32(24).int32(message.agentPort);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_JaegerConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_JaegerConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.enabled = reader.bool();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.agentHost = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.agentPort = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_JaegerConfiguration>, I>>(base?: I): ServerConfiguration_JaegerConfiguration {
    return ServerConfiguration_JaegerConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_JaegerConfiguration>, I>>(object: I): ServerConfiguration_JaegerConfiguration {
    const message = createBaseServerConfiguration_JaegerConfiguration();
    message.enabled = object.enabled ?? false;
    message.agentHost = object.agentHost ?? "";
    message.agentPort = object.agentPort ?? 0;
    return message;
  },
};

function createBaseServerConfiguration_VertxConfiguration(): ServerConfiguration_VertxConfiguration {
  return { useInfinispanClusterManager: false, infinspanPort: undefined, infinispanAddresses: "" };
}

export const ServerConfiguration_VertxConfiguration = {
  encode(message: ServerConfiguration_VertxConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.useInfinispanClusterManager === true) {
      writer.uint32(8).bool(message.useInfinispanClusterManager);
    }
    if (message.infinspanPort !== undefined) {
      writer.uint32(16).int32(message.infinspanPort);
    }
    if (message.infinispanAddresses !== "") {
      writer.uint32(26).string(message.infinispanAddresses);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_VertxConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_VertxConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.useInfinispanClusterManager = reader.bool();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.infinspanPort = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.infinispanAddresses = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_VertxConfiguration>, I>>(base?: I): ServerConfiguration_VertxConfiguration {
    return ServerConfiguration_VertxConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_VertxConfiguration>, I>>(object: I): ServerConfiguration_VertxConfiguration {
    const message = createBaseServerConfiguration_VertxConfiguration();
    message.useInfinispanClusterManager = object.useInfinispanClusterManager ?? false;
    message.infinspanPort = object.infinspanPort ?? undefined;
    message.infinispanAddresses = object.infinispanAddresses ?? "";
    return message;
  },
};

function createBaseServerConfiguration_CardsConfiguration(): ServerConfiguration_CardsConfiguration {
  return {};
}

export const ServerConfiguration_CardsConfiguration = {
  encode(_: ServerConfiguration_CardsConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_CardsConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_CardsConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_CardsConfiguration>, I>>(base?: I): ServerConfiguration_CardsConfiguration {
    return ServerConfiguration_CardsConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_CardsConfiguration>, I>>(_: I): ServerConfiguration_CardsConfiguration {
    const message = createBaseServerConfiguration_CardsConfiguration();
    return message;
  },
};

function createBaseServerConfiguration_GraphQLConfiguration(): ServerConfiguration_GraphQLConfiguration {
  return { url: "" };
}

export const ServerConfiguration_GraphQLConfiguration = {
  encode(message: ServerConfiguration_GraphQLConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.url !== "") {
      writer.uint32(42).string(message.url);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerConfiguration_GraphQLConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerConfiguration_GraphQLConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 5:
          if (tag !== 42) {
            break;
          }

          message.url = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ServerConfiguration_GraphQLConfiguration>, I>>(base?: I): ServerConfiguration_GraphQLConfiguration {
    return ServerConfiguration_GraphQLConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ServerConfiguration_GraphQLConfiguration>, I>>(object: I): ServerConfiguration_GraphQLConfiguration {
    const message = createBaseServerConfiguration_GraphQLConfiguration();
    message.url = object.url ?? "";
    return message;
  },
};

function createBaseClientConfiguration(): ClientConfiguration {
  return { accounts: undefined, graphQl: undefined };
}

export const ClientConfiguration = {
  encode(message: ClientConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.accounts !== undefined) {
      ClientConfiguration_AccountsConfiguration.encode(message.accounts, writer.uint32(10).fork()).ldelim();
    }
    if (message.graphQl !== undefined) {
      ClientConfiguration_GraphQlConfiguration.encode(message.graphQl, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ClientConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseClientConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.accounts = ClientConfiguration_AccountsConfiguration.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.graphQl = ClientConfiguration_GraphQlConfiguration.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ClientConfiguration>, I>>(base?: I): ClientConfiguration {
    return ClientConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ClientConfiguration>, I>>(object: I): ClientConfiguration {
    const message = createBaseClientConfiguration();
    message.accounts = object.accounts !== undefined && object.accounts !== null ? ClientConfiguration_AccountsConfiguration.fromPartial(object.accounts) : undefined;
    message.graphQl = object.graphQl !== undefined && object.graphQl !== null ? ClientConfiguration_GraphQlConfiguration.fromPartial(object.graphQl) : undefined;
    return message;
  },
};

function createBaseClientConfiguration_AccountsConfiguration(): ClientConfiguration_AccountsConfiguration {
  return { keycloakResetPasswordUrl: "", keycloakAccountManagementUrl: "" };
}

export const ClientConfiguration_AccountsConfiguration = {
  encode(message: ClientConfiguration_AccountsConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.keycloakResetPasswordUrl !== "") {
      writer.uint32(10).string(message.keycloakResetPasswordUrl);
    }
    if (message.keycloakAccountManagementUrl !== "") {
      writer.uint32(18).string(message.keycloakAccountManagementUrl);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ClientConfiguration_AccountsConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseClientConfiguration_AccountsConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.keycloakResetPasswordUrl = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.keycloakAccountManagementUrl = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ClientConfiguration_AccountsConfiguration>, I>>(base?: I): ClientConfiguration_AccountsConfiguration {
    return ClientConfiguration_AccountsConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ClientConfiguration_AccountsConfiguration>, I>>(object: I): ClientConfiguration_AccountsConfiguration {
    const message = createBaseClientConfiguration_AccountsConfiguration();
    message.keycloakResetPasswordUrl = object.keycloakResetPasswordUrl ?? "";
    message.keycloakAccountManagementUrl = object.keycloakAccountManagementUrl ?? "";
    return message;
  },
};

function createBaseClientConfiguration_GraphQlConfiguration(): ClientConfiguration_GraphQlConfiguration {
  return { graphQlUrl: "" };
}

export const ClientConfiguration_GraphQlConfiguration = {
  encode(message: ClientConfiguration_GraphQlConfiguration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.graphQlUrl !== "") {
      writer.uint32(10).string(message.graphQlUrl);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ClientConfiguration_GraphQlConfiguration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseClientConfiguration_GraphQlConfiguration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.graphQlUrl = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ClientConfiguration_GraphQlConfiguration>, I>>(base?: I): ClientConfiguration_GraphQlConfiguration {
    return ClientConfiguration_GraphQlConfiguration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ClientConfiguration_GraphQlConfiguration>, I>>(object: I): ClientConfiguration_GraphQlConfiguration {
    const message = createBaseClientConfiguration_GraphQlConfiguration();
    message.graphQlUrl = object.graphQlUrl ?? "";
    return message;
  },
};

function createBaseGetCardsRequest(): GetCardsRequest {
  return { IfNoneMatch: "", userId: "" };
}

export const GetCardsRequest = {
  encode(message: GetCardsRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.IfNoneMatch !== "") {
      writer.uint32(10).string(message.IfNoneMatch);
    }
    if (message.userId !== "") {
      writer.uint32(18).string(message.userId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetCardsRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetCardsRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.IfNoneMatch = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.userId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetCardsRequest>, I>>(base?: I): GetCardsRequest {
    return GetCardsRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetCardsRequest>, I>>(object: I): GetCardsRequest {
    const message = createBaseGetCardsRequest();
    message.IfNoneMatch = object.IfNoneMatch ?? "";
    message.userId = object.userId ?? "";
    return message;
  },
};

function createBaseGetCardsResponse(): GetCardsResponse {
  return { content: undefined, version: "", cachedOk: false };
}

export const GetCardsResponse = {
  encode(message: GetCardsResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.content !== undefined) {
      GetCardsResponse_Content.encode(message.content, writer.uint32(10).fork()).ldelim();
    }
    if (message.version !== "") {
      writer.uint32(18).string(message.version);
    }
    if (message.cachedOk === true) {
      writer.uint32(24).bool(message.cachedOk);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetCardsResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetCardsResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.content = GetCardsResponse_Content.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.version = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.cachedOk = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetCardsResponse>, I>>(base?: I): GetCardsResponse {
    return GetCardsResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetCardsResponse>, I>>(object: I): GetCardsResponse {
    const message = createBaseGetCardsResponse();
    message.content = object.content !== undefined && object.content !== null ? GetCardsResponse_Content.fromPartial(object.content) : undefined;
    message.version = object.version ?? "";
    message.cachedOk = object.cachedOk ?? false;
    return message;
  },
};

function createBaseGetCardsResponse_Content(): GetCardsResponse_Content {
  return { cards: [] };
}

export const GetCardsResponse_Content = {
  encode(message: GetCardsResponse_Content, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.cards) {
      CardRecord.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetCardsResponse_Content {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetCardsResponse_Content();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.cards.push(CardRecord.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetCardsResponse_Content>, I>>(base?: I): GetCardsResponse_Content {
    return GetCardsResponse_Content.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetCardsResponse_Content>, I>>(object: I): GetCardsResponse_Content {
    const message = createBaseGetCardsResponse_Content();
    message.cards = object.cards?.map((e) => CardRecord.fromPartial(e)) || [];
    return message;
  },
};

function createBaseLoginRequest(): LoginRequest {
  return { usernameOrEmail: "", password: "" };
}

export const LoginRequest = {
  encode(message: LoginRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.usernameOrEmail !== "") {
      writer.uint32(10).string(message.usernameOrEmail);
    }
    if (message.password !== "") {
      writer.uint32(18).string(message.password);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): LoginRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseLoginRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.usernameOrEmail = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.password = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<LoginRequest>, I>>(base?: I): LoginRequest {
    return LoginRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<LoginRequest>, I>>(object: I): LoginRequest {
    const message = createBaseLoginRequest();
    message.usernameOrEmail = object.usernameOrEmail ?? "";
    message.password = object.password ?? "";
    return message;
  },
};

function createBaseCreateAccountRequest(): CreateAccountRequest {
  return { email: "", username: "", password: "", decks: false, guest: false };
}

export const CreateAccountRequest = {
  encode(message: CreateAccountRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.email !== "") {
      writer.uint32(10).string(message.email);
    }
    if (message.username !== "") {
      writer.uint32(18).string(message.username);
    }
    if (message.password !== "") {
      writer.uint32(26).string(message.password);
    }
    if (message.decks === true) {
      writer.uint32(32).bool(message.decks);
    }
    if (message.guest === true) {
      writer.uint32(40).bool(message.guest);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CreateAccountRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCreateAccountRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.email = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.username = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.password = reader.string();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.decks = reader.bool();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.guest = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<CreateAccountRequest>, I>>(base?: I): CreateAccountRequest {
    return CreateAccountRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CreateAccountRequest>, I>>(object: I): CreateAccountRequest {
    const message = createBaseCreateAccountRequest();
    message.email = object.email ?? "";
    message.username = object.username ?? "";
    message.password = object.password ?? "";
    message.decks = object.decks ?? false;
    message.guest = object.guest ?? false;
    return message;
  },
};

function createBaseChangePasswordRequest(): ChangePasswordRequest {
  return { newPassword: "" };
}

export const ChangePasswordRequest = {
  encode(message: ChangePasswordRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.newPassword !== "") {
      writer.uint32(10).string(message.newPassword);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ChangePasswordRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseChangePasswordRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.newPassword = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<ChangePasswordRequest>, I>>(base?: I): ChangePasswordRequest {
    return ChangePasswordRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ChangePasswordRequest>, I>>(object: I): ChangePasswordRequest {
    const message = createBaseChangePasswordRequest();
    message.newPassword = object.newPassword ?? "";
    return message;
  },
};

function createBaseLoginOrCreateReply(): LoginOrCreateReply {
  return { accessTokenResponse: undefined, userEntity: undefined };
}

export const LoginOrCreateReply = {
  encode(message: LoginOrCreateReply, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.accessTokenResponse !== undefined) {
      AccessTokenResponse.encode(message.accessTokenResponse, writer.uint32(10).fork()).ldelim();
    }
    if (message.userEntity !== undefined) {
      UserEntity.encode(message.userEntity, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): LoginOrCreateReply {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseLoginOrCreateReply();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.accessTokenResponse = AccessTokenResponse.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.userEntity = UserEntity.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<LoginOrCreateReply>, I>>(base?: I): LoginOrCreateReply {
    return LoginOrCreateReply.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<LoginOrCreateReply>, I>>(object: I): LoginOrCreateReply {
    const message = createBaseLoginOrCreateReply();
    message.accessTokenResponse = object.accessTokenResponse !== undefined && object.accessTokenResponse !== null ? AccessTokenResponse.fromPartial(object.accessTokenResponse) : undefined;
    message.userEntity = object.userEntity !== undefined && object.userEntity !== null ? UserEntity.fromPartial(object.userEntity) : undefined;
    return message;
  },
};

function createBaseAccessTokenResponse(): AccessTokenResponse {
  return { token: "" };
}

export const AccessTokenResponse = {
  encode(message: AccessTokenResponse, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.token !== "") {
      writer.uint32(10).string(message.token);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AccessTokenResponse {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAccessTokenResponse();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.token = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<AccessTokenResponse>, I>>(base?: I): AccessTokenResponse {
    return AccessTokenResponse.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AccessTokenResponse>, I>>(object: I): AccessTokenResponse {
    const message = createBaseAccessTokenResponse();
    message.token = object.token ?? "";
    return message;
  },
};

function createBaseUserEntity(): UserEntity {
  return { id: "", email: "", username: "", privacyToken: "" };
}

export const UserEntity = {
  encode(message: UserEntity, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    if (message.email !== "") {
      writer.uint32(18).string(message.email);
    }
    if (message.username !== "") {
      writer.uint32(82).string(message.username);
    }
    if (message.privacyToken !== "") {
      writer.uint32(90).string(message.privacyToken);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserEntity {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserEntity();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.email = reader.string();
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.username = reader.string();
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.privacyToken = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<UserEntity>, I>>(base?: I): UserEntity {
    return UserEntity.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserEntity>, I>>(object: I): UserEntity {
    const message = createBaseUserEntity();
    message.id = object.id ?? "";
    message.email = object.email ?? "";
    message.username = object.username ?? "";
    message.privacyToken = object.privacyToken ?? "";
    return message;
  },
};

function createBaseGetAccountsRequest(): GetAccountsRequest {
  return { ids: [] };
}

export const GetAccountsRequest = {
  encode(message: GetAccountsRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.ids) {
      writer.uint32(10).string(v!);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetAccountsRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetAccountsRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.ids.push(reader.string());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetAccountsRequest>, I>>(base?: I): GetAccountsRequest {
    return GetAccountsRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetAccountsRequest>, I>>(object: I): GetAccountsRequest {
    const message = createBaseGetAccountsRequest();
    message.ids = object.ids?.map((e) => e) || [];
    return message;
  },
};

function createBaseGetAccountsReply(): GetAccountsReply {
  return { userEntities: [] };
}

export const GetAccountsReply = {
  encode(message: GetAccountsReply, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.userEntities) {
      UserEntity.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetAccountsReply {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetAccountsReply();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.userEntities.push(UserEntity.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create<I extends Exact<DeepPartial<GetAccountsReply>, I>>(base?: I): GetAccountsReply {
    return GetAccountsReply.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetAccountsReply>, I>>(object: I): GetAccountsReply {
    const message = createBaseGetAccountsReply();
    message.userEntities = object.userEntities?.map((e) => UserEntity.fromPartial(e)) || [];
    return message;
  },
};

export type UnauthenticatedService = typeof UnauthenticatedService;
export const UnauthenticatedService = {
  createAccount: {
    path: "/hiddenswitch.Unauthenticated/CreateAccount",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: CreateAccountRequest) => Buffer.from(CreateAccountRequest.encode(value).finish()),
    requestDeserialize: (value: Buffer) => CreateAccountRequest.decode(value),
    responseSerialize: (value: LoginOrCreateReply) => Buffer.from(LoginOrCreateReply.encode(value).finish()),
    responseDeserialize: (value: Buffer) => LoginOrCreateReply.decode(value),
  },
  login: {
    path: "/hiddenswitch.Unauthenticated/Login",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: LoginRequest) => Buffer.from(LoginRequest.encode(value).finish()),
    requestDeserialize: (value: Buffer) => LoginRequest.decode(value),
    responseSerialize: (value: LoginOrCreateReply) => Buffer.from(LoginOrCreateReply.encode(value).finish()),
    responseDeserialize: (value: Buffer) => LoginOrCreateReply.decode(value),
  },
  /** Verify that the token is valid */
  verifyToken: {
    path: "/hiddenswitch.Unauthenticated/VerifyToken",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: AccessTokenResponse) => Buffer.from(AccessTokenResponse.encode(value).finish()),
    requestDeserialize: (value: Buffer) => AccessTokenResponse.decode(value),
    responseSerialize: (value: boolean | undefined) => Buffer.from(BoolValue.encode({ value: value ?? false }).finish()),
    responseDeserialize: (value: Buffer) => BoolValue.decode(value).value,
  },
  /** Returns a configuration for the client from the server. */
  getConfiguration: {
    path: "/hiddenswitch.Unauthenticated/GetConfiguration",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: Empty) => Buffer.from(Empty.encode(value).finish()),
    requestDeserialize: (value: Buffer) => Empty.decode(value),
    responseSerialize: (value: ClientConfiguration) => Buffer.from(ClientConfiguration.encode(value).finish()),
    responseDeserialize: (value: Buffer) => ClientConfiguration.decode(value),
  },
} as const;

export interface UnauthenticatedServer extends UntypedServiceImplementation {
  createAccount: handleUnaryCall<CreateAccountRequest, LoginOrCreateReply>;
  login: handleUnaryCall<LoginRequest, LoginOrCreateReply>;
  /** Verify that the token is valid */
  verifyToken: handleUnaryCall<AccessTokenResponse, boolean | undefined>;
  /** Returns a configuration for the client from the server. */
  getConfiguration: handleUnaryCall<Empty, ClientConfiguration>;
}

export interface UnauthenticatedClient extends Client {
  createAccount(request: CreateAccountRequest, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  createAccount(request: CreateAccountRequest, metadata: Metadata, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  createAccount(request: CreateAccountRequest, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  login(request: LoginRequest, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  login(request: LoginRequest, metadata: Metadata, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  login(request: LoginRequest, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  /** Verify that the token is valid */
  verifyToken(request: AccessTokenResponse, callback: (error: ServiceError | null, response: boolean | undefined) => void): ClientUnaryCall;
  verifyToken(request: AccessTokenResponse, metadata: Metadata, callback: (error: ServiceError | null, response: boolean | undefined) => void): ClientUnaryCall;
  verifyToken(request: AccessTokenResponse, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: boolean | undefined) => void): ClientUnaryCall;
  /** Returns a configuration for the client from the server. */
  getConfiguration(request: Empty, callback: (error: ServiceError | null, response: ClientConfiguration) => void): ClientUnaryCall;
  getConfiguration(request: Empty, metadata: Metadata, callback: (error: ServiceError | null, response: ClientConfiguration) => void): ClientUnaryCall;
  getConfiguration(request: Empty, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: ClientConfiguration) => void): ClientUnaryCall;
}

export const UnauthenticatedClient = makeGenericClientConstructor(UnauthenticatedService, "hiddenswitch.Unauthenticated") as unknown as {
  new (address: string, credentials: ChannelCredentials, options?: Partial<ClientOptions>): UnauthenticatedClient;
  service: typeof UnauthenticatedService;
};

export type UnauthenticatedCardsService = typeof UnauthenticatedCardsService;
export const UnauthenticatedCardsService = {
  /** Gets a complete catalogue of all the cards available in Spellsource as a list of CardRecords */
  getCards: {
    path: "/hiddenswitch.UnauthenticatedCards/GetCards",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: GetCardsRequest) => Buffer.from(GetCardsRequest.encode(value).finish()),
    requestDeserialize: (value: Buffer) => GetCardsRequest.decode(value),
    responseSerialize: (value: GetCardsResponse) => Buffer.from(GetCardsResponse.encode(value).finish()),
    responseDeserialize: (value: Buffer) => GetCardsResponse.decode(value),
  },
} as const;

export interface UnauthenticatedCardsServer extends UntypedServiceImplementation {
  /** Gets a complete catalogue of all the cards available in Spellsource as a list of CardRecords */
  getCards: handleUnaryCall<GetCardsRequest, GetCardsResponse>;
}

export interface UnauthenticatedCardsClient extends Client {
  /** Gets a complete catalogue of all the cards available in Spellsource as a list of CardRecords */
  getCards(request: GetCardsRequest, callback: (error: ServiceError | null, response: GetCardsResponse) => void): ClientUnaryCall;
  getCards(request: GetCardsRequest, metadata: Metadata, callback: (error: ServiceError | null, response: GetCardsResponse) => void): ClientUnaryCall;
  getCards(request: GetCardsRequest, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: GetCardsResponse) => void): ClientUnaryCall;
}

export const UnauthenticatedCardsClient = makeGenericClientConstructor(UnauthenticatedCardsService, "hiddenswitch.UnauthenticatedCards") as unknown as {
  new (address: string, credentials: ChannelCredentials, options?: Partial<ClientOptions>): UnauthenticatedCardsClient;
  service: typeof UnauthenticatedCardsService;
};

export type AuthenticatedCardsService = typeof AuthenticatedCardsService;
export const AuthenticatedCardsService = {
  /** Returns the cards that belong to this user */
  getCardsByUser: {
    path: "/hiddenswitch.AuthenticatedCards/GetCardsByUser",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: GetCardsRequest) => Buffer.from(GetCardsRequest.encode(value).finish()),
    requestDeserialize: (value: Buffer) => GetCardsRequest.decode(value),
    responseSerialize: (value: GetCardsResponse) => Buffer.from(GetCardsResponse.encode(value).finish()),
    responseDeserialize: (value: Buffer) => GetCardsResponse.decode(value),
  },
} as const;

export interface AuthenticatedCardsServer extends UntypedServiceImplementation {
  /** Returns the cards that belong to this user */
  getCardsByUser: handleUnaryCall<GetCardsRequest, GetCardsResponse>;
}

export interface AuthenticatedCardsClient extends Client {
  /** Returns the cards that belong to this user */
  getCardsByUser(request: GetCardsRequest, callback: (error: ServiceError | null, response: GetCardsResponse) => void): ClientUnaryCall;
  getCardsByUser(request: GetCardsRequest, metadata: Metadata, callback: (error: ServiceError | null, response: GetCardsResponse) => void): ClientUnaryCall;
  getCardsByUser(request: GetCardsRequest, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: GetCardsResponse) => void): ClientUnaryCall;
}

export const AuthenticatedCardsClient = makeGenericClientConstructor(AuthenticatedCardsService, "hiddenswitch.AuthenticatedCards") as unknown as {
  new (address: string, credentials: ChannelCredentials, options?: Partial<ClientOptions>): AuthenticatedCardsClient;
  service: typeof AuthenticatedCardsService;
};

export type AccountsService = typeof AccountsService;
export const AccountsService = {
  /** Returns the account for the token passed */
  getAccount: {
    path: "/hiddenswitch.Accounts/GetAccount",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: Empty) => Buffer.from(Empty.encode(value).finish()),
    requestDeserialize: (value: Buffer) => Empty.decode(value),
    responseSerialize: (value: GetAccountsReply) => Buffer.from(GetAccountsReply.encode(value).finish()),
    responseDeserialize: (value: Buffer) => GetAccountsReply.decode(value),
  },
  /** Gets an account by ID. */
  getAccounts: {
    path: "/hiddenswitch.Accounts/GetAccounts",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: GetAccountsRequest) => Buffer.from(GetAccountsRequest.encode(value).finish()),
    requestDeserialize: (value: Buffer) => GetAccountsRequest.decode(value),
    responseSerialize: (value: GetAccountsReply) => Buffer.from(GetAccountsReply.encode(value).finish()),
    responseDeserialize: (value: Buffer) => GetAccountsReply.decode(value),
  },
  /** Doesn't invalidate the token right now */
  changePassword: {
    path: "/hiddenswitch.Accounts/ChangePassword",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: ChangePasswordRequest) => Buffer.from(ChangePasswordRequest.encode(value).finish()),
    requestDeserialize: (value: Buffer) => ChangePasswordRequest.decode(value),
    responseSerialize: (value: LoginOrCreateReply) => Buffer.from(LoginOrCreateReply.encode(value).finish()),
    responseDeserialize: (value: Buffer) => LoginOrCreateReply.decode(value),
  },
  /** Sends a password reset e-mail to the token passed */
  requestPasswordResetEmail: {
    path: "/hiddenswitch.Accounts/RequestPasswordResetEmail",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: Empty) => Buffer.from(Empty.encode(value).finish()),
    requestDeserialize: (value: Buffer) => Empty.decode(value),
    responseSerialize: (value: Empty) => Buffer.from(Empty.encode(value).finish()),
    responseDeserialize: (value: Buffer) => Empty.decode(value),
  },
} as const;

export interface AccountsServer extends UntypedServiceImplementation {
  /** Returns the account for the token passed */
  getAccount: handleUnaryCall<Empty, GetAccountsReply>;
  /** Gets an account by ID. */
  getAccounts: handleUnaryCall<GetAccountsRequest, GetAccountsReply>;
  /** Doesn't invalidate the token right now */
  changePassword: handleUnaryCall<ChangePasswordRequest, LoginOrCreateReply>;
  /** Sends a password reset e-mail to the token passed */
  requestPasswordResetEmail: handleUnaryCall<Empty, Empty>;
}

export interface AccountsClient extends Client {
  /** Returns the account for the token passed */
  getAccount(request: Empty, callback: (error: ServiceError | null, response: GetAccountsReply) => void): ClientUnaryCall;
  getAccount(request: Empty, metadata: Metadata, callback: (error: ServiceError | null, response: GetAccountsReply) => void): ClientUnaryCall;
  getAccount(request: Empty, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: GetAccountsReply) => void): ClientUnaryCall;
  /** Gets an account by ID. */
  getAccounts(request: GetAccountsRequest, callback: (error: ServiceError | null, response: GetAccountsReply) => void): ClientUnaryCall;
  getAccounts(request: GetAccountsRequest, metadata: Metadata, callback: (error: ServiceError | null, response: GetAccountsReply) => void): ClientUnaryCall;
  getAccounts(request: GetAccountsRequest, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: GetAccountsReply) => void): ClientUnaryCall;
  /** Doesn't invalidate the token right now */
  changePassword(request: ChangePasswordRequest, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  changePassword(request: ChangePasswordRequest, metadata: Metadata, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  changePassword(request: ChangePasswordRequest, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: LoginOrCreateReply) => void): ClientUnaryCall;
  /** Sends a password reset e-mail to the token passed */
  requestPasswordResetEmail(request: Empty, callback: (error: ServiceError | null, response: Empty) => void): ClientUnaryCall;
  requestPasswordResetEmail(request: Empty, metadata: Metadata, callback: (error: ServiceError | null, response: Empty) => void): ClientUnaryCall;
  requestPasswordResetEmail(request: Empty, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: Empty) => void): ClientUnaryCall;
}

export const AccountsClient = makeGenericClientConstructor(AccountsService, "hiddenswitch.Accounts") as unknown as {
  new (address: string, credentials: ChannelCredentials, options?: Partial<ClientOptions>): AccountsClient;
  service: typeof AccountsService;
};

export type GamesService = typeof GamesService;
export const GamesService = {
  isInMatch: {
    path: "/hiddenswitch.Games/IsInMatch",
    requestStream: false,
    responseStream: false,
    requestSerialize: (value: Empty) => Buffer.from(Empty.encode(value).finish()),
    requestDeserialize: (value: Buffer) => Empty.decode(value),
    responseSerialize: (value: string | undefined) => Buffer.from(StringValue.encode({ value: value ?? "" }).finish()),
    responseDeserialize: (value: Buffer) => StringValue.decode(value).value,
  },
} as const;

export interface GamesServer extends UntypedServiceImplementation {
  isInMatch: handleUnaryCall<Empty, string | undefined>;
}

export interface GamesClient extends Client {
  isInMatch(request: Empty, callback: (error: ServiceError | null, response: string | undefined) => void): ClientUnaryCall;
  isInMatch(request: Empty, metadata: Metadata, callback: (error: ServiceError | null, response: string | undefined) => void): ClientUnaryCall;
  isInMatch(request: Empty, metadata: Metadata, options: Partial<CallOptions>, callback: (error: ServiceError | null, response: string | undefined) => void): ClientUnaryCall;
}

export const GamesClient = makeGenericClientConstructor(GamesService, "hiddenswitch.Games") as unknown as {
  new (address: string, credentials: ChannelCredentials, options?: Partial<ClientOptions>): GamesClient;
  service: typeof GamesService;
};

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T : T extends globalThis.Array<infer U> ? globalThis.Array<DeepPartial<U>> : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>> : T extends { $case: string } ? { [K in keyof Omit<T, "$case">]?: DeepPartial<T[K]> } & { $case: T["$case"] } : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> } : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function longToNumber(long: Long): number {
  if (long.gt(globalThis.Number.MAX_SAFE_INTEGER)) {
    throw new globalThis.Error("Value is larger than Number.MAX_SAFE_INTEGER");
  }
  return long.toNumber();
}

if (_m0.util.Long !== Long) {
  _m0.util.Long = Long as any;
  _m0.configure();
}
