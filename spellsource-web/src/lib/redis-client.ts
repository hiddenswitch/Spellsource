import Redis from "ioredis";
import { redisUri } from "./config";

const redis = new Redis(redisUri);

export default redis;
