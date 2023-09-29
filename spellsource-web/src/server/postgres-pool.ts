import { Pool } from "pg";
import { pgDatabase, pgHost, pgPassword, pgPort, pgUser } from "../lib/config";

export const pool = new Pool({
  user: pgUser,
  password: pgPassword,
  host: pgHost,
  database: pgDatabase,
  port: pgPort,
});
export { pool as pgPool };
