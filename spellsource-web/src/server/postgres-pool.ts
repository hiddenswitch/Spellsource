import { Pool } from "pg";
import { pgDatabase, pgHost, pgPassword, pgPort, pgUser } from "../lib/config";

export const pool = new Pool({
  user: pgUser,
  password: pgPassword,
  host: pgHost,
  database: pgDatabase,
  port: pgPort,
  connectionTimeoutMillis: 1000, // Set connection timeout to 1 second
});
export { pool as pgPool };
