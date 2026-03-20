import { Transport, TransportFactory, TransportOptions } from "@improbable-eng/grpc-web/dist/typings/transports/Transport";
import { BrowserHeaders as Metadata } from "browser-headers";
import { fetch as crossFetch, Response, Headers } from "cross-fetch";

type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;
export type CrossFetchTransportInit = Omit<RequestInit, "headers" | "method" | "body" | "signal">;

function debug(msg: string, other?: any) {
  console.log(msg, other);
}

export function throwExpression(errorMessage: string): never {
  throw new Error(errorMessage);
}

export function CrossFetchReadableStreamTransport(init: CrossFetchTransportInit): TransportFactory {
  return (opts: TransportOptions) => {
    return fetchRequest(opts, init);
  };
}

function fetchRequest(options: TransportOptions, init: CrossFetchTransportInit): Transport {
  options.debug && debug("fetchRequest", options);
  return new CrossFetch(options, init);
}

// declare const Response: any;
// declare const Headers: any;

class CrossFetch implements Transport {
  cancelled: boolean = false;
  options: TransportOptions;
  init: CrossFetchTransportInit;
  reader?: ReadableStreamReader<Uint8Array>;
  metadata: Metadata = new Metadata();
  controller: AbortController | undefined = (self as any).AbortController && new AbortController();

  constructor(transportOptions: TransportOptions, init: CrossFetchTransportInit) {
    this.options = transportOptions;
    this.init = init;
  }

  pump(readerArg: ReadableStreamReader<Uint8Array>, res: Response) {
    this.reader = readerArg;
    if (this.cancelled) {
      // If the request was cancelled before the first pump then cancel it here
      this.options.debug && debug("Fetch.pump.cancel at first pump");
      this.reader.cancel().catch((e) => {
        // This can be ignored. It will likely throw an exception due to the request being aborted
        this.options.debug && debug("Fetch.pump.reader.cancel exception", e);
      });
      return;
    }
    this.reader
      // @ts-ignore
      .read()
      .then((result) => {
        if (result.done) {
          this.options.onEnd();
          return res;
        }
        this.options.onChunk(result.value);
        this.pump(this.reader ?? throwExpression("reader never null"), res);
        return;
      })
      .catch((err) => {
        if (this.cancelled) {
          this.options.debug && debug("Fetch.catch - request cancelled");
          return;
        }
        this.cancelled = true;
        this.options.debug && debug("Fetch.catch", err.message);
        this.options.onEnd(err);
      });
  }

  send(msgBytes: Uint8Array) {
    const headers = new Headers();
    this.metadata.forEach((key, values) => {
      values.forEach((value) => {
        headers.append(key, value);
      });
    });

    const thisFetch = typeof window == "undefined" ? crossFetch : window.fetch;
    thisFetch(this.options.url, {
      ...this.init,
      headers: headers,
      method: "POST",
      body: msgBytes,
      signal: this.controller && this.controller.signal,
    })
      .then((res: Response) => {
        this.options.debug && debug("Fetch.response", res);
        this.options.onHeaders(new Metadata(res.headers as any), res.status);
        if (res.body) {
          this.pump(res.body.getReader(), res);
          return;
        }
        return res;
      })
      .catch((err) => {
        if (this.cancelled) {
          this.options.debug && debug("Fetch.catch - request cancelled");
          return;
        }
        this.cancelled = true;
        this.options.debug && debug("Fetch.catch", err.message);
        this.options.onEnd(err);
      });
  }

  sendMessage(msgBytes: Uint8Array) {
    this.send(msgBytes);
  }

  finishSend() {}

  start(metadata: Metadata) {
    this.metadata = metadata;
  }

  cancel() {
    if (this.cancelled) {
      this.options.debug && debug("Fetch.cancel already cancelled");
      return;
    }
    this.cancelled = true;

    if (this.controller) {
      this.options.debug && debug("Fetch.cancel.controller.abort");
      this.controller.abort();
    } else {
      this.options.debug && debug("Fetch.cancel.missing abort controller");
    }

    if (this.reader) {
      // If the reader has already been received in the pump then it can be cancelled immediately
      this.options.debug && debug("Fetch.cancel.reader.cancel");
      this.reader.cancel().catch((e) => {
        // This can be ignored. It will likely throw an exception due to the request being aborted
        this.options.debug && debug("Fetch.cancel.reader.cancel exception", e);
      });
    } else {
      this.options.debug && debug("Fetch.cancel before reader");
    }
  }
}

export function detectFetchSupport(): boolean {
  return typeof Response !== "undefined" && Response.prototype.hasOwnProperty("body") && typeof Headers === "function";
}
