// todo: use a unified config for this
export const pgPort = parseInt(process.env.PG_PORT || "5432");
// this is also from redis.uri in the master config
export const redisUri = parseInt(process.env.REDIS_URI || "redis://localhost:6379");
export const keycloakPort = parseInt(process.env.KEYCLOAK_PORT || "8080");

export const clientId = "spellsource";
export const clientSecret = "clientsecret";
export const issuer = `http://localhost:${keycloakPort}/realms/hiddenswitch`;
export const baseUrl = "http://localhost:3000";
