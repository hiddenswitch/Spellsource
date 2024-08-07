// todo: use a unified config for this
export const pgHost = process.env.PGHOST || process.env.PG_HOST || "localhost";
export const pgPort = parseInt(process.env.PGPORT || process.env.PG_PORT || "5432");
export const pgUser = process.env.PGUSER || process.env.PG_USER || "admin";
export const pgPassword = process.env.PGPASSWORD || process.env.PG_PASSWORD || "password";
export const pgDatabase = process.env.PGDATABASE || process.env.PG_DATABASE || "spellsource";
// this is also from redis.uri in the master config
export const redisUri = process.env.REDIS_URI || "redis://localhost:6379";
export const keycloakUrl =
  process.env.KEYCLOAK_URL || `http://localhost:${parseInt(process.env.KEYCLOAK_PORT || "8080")}`;

export const clientId = "spellsource";
export const clientSecret = "clientsecret";
export const issuer = `${keycloakUrl}/realms/hiddenswitch`;
export const baseUrl = process.env.BASE_URL || "http://localhost:3000";
export const awsAccessKeyId = process.env.AWS_ACCESS_KEY_ID ?? "";
export const awsSecretAccessKey = process.env.AWS_SECRET_ACCESS_KEY ?? "";
export const awsBucketName = process.env.AWS_BUCKET_NAME || "spellsource-test";
export const comfyUrl = process.env.COMFY_URL || "http://127.0.0.1:8188";
