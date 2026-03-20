import { TransportFactory } from "@improbable-eng/grpc-web/dist/typings/transports/Transport";
type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;
export type CrossFetchTransportInit = Omit<RequestInit, "headers" | "method" | "body" | "signal">;
export declare function throwExpression(errorMessage: string): never;
export declare function CrossFetchReadableStreamTransport(init: CrossFetchTransportInit): TransportFactory;
export declare function detectFetchSupport(): boolean;
export {};
