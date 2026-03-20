"use strict";
var __assign =
  (this && this.__assign) ||
  function () {
    __assign =
      Object.assign ||
      function (t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
          s = arguments[i];
          for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
        }
        return t;
      };
    return __assign.apply(this, arguments);
  };
Object.defineProperty(exports, "__esModule", { value: true });
exports.detectFetchSupport = exports.CrossFetchReadableStreamTransport = exports.throwExpression = void 0;
var browser_headers_1 = require("browser-headers");
var cross_fetch_1 = require("cross-fetch");
function debug(msg, other) {
  console.log(msg, other);
}
function throwExpression(errorMessage) {
  throw new Error(errorMessage);
}
exports.throwExpression = throwExpression;
function CrossFetchReadableStreamTransport(init) {
  return function (opts) {
    return fetchRequest(opts, init);
  };
}
exports.CrossFetchReadableStreamTransport = CrossFetchReadableStreamTransport;
function fetchRequest(options, init) {
  options.debug && debug("fetchRequest", options);
  return new CrossFetch(options, init);
}
// declare const Response: any;
// declare const Headers: any;
var CrossFetch = /** @class */ (function () {
  function CrossFetch(transportOptions, init) {
    this.cancelled = false;
    this.metadata = new browser_headers_1.BrowserHeaders();
    this.controller = self.AbortController && new AbortController();
    this.options = transportOptions;
    this.init = init;
  }
  CrossFetch.prototype.pump = function (readerArg, res) {
    var _this = this;
    this.reader = readerArg;
    if (this.cancelled) {
      // If the request was cancelled before the first pump then cancel it here
      this.options.debug && debug("Fetch.pump.cancel at first pump");
      this.reader.cancel().catch(function (e) {
        // This can be ignored. It will likely throw an exception due to the request being aborted
        _this.options.debug && debug("Fetch.pump.reader.cancel exception", e);
      });
      return;
    }
    this.reader
      // @ts-ignore
      .read()
      .then(function (result) {
        var _a;
        if (result.done) {
          _this.options.onEnd();
          return res;
        }
        _this.options.onChunk(result.value);
        _this.pump((_a = _this.reader) !== null && _a !== void 0 ? _a : throwExpression("reader never null"), res);
        return;
      })
      .catch(function (err) {
        if (_this.cancelled) {
          _this.options.debug && debug("Fetch.catch - request cancelled");
          return;
        }
        _this.cancelled = true;
        _this.options.debug && debug("Fetch.catch", err.message);
        _this.options.onEnd(err);
      });
  };
  CrossFetch.prototype.send = function (msgBytes) {
    var _this = this;
    var headers = new cross_fetch_1.Headers();
    this.metadata.forEach(function (key, values) {
      values.forEach(function (value) {
        headers.append(key, value);
      });
    });
    var thisFetch = typeof window == "undefined" ? cross_fetch_1.fetch : window.fetch;
    thisFetch(this.options.url, __assign(__assign({}, this.init), { headers: headers, method: "POST", body: msgBytes, signal: this.controller && this.controller.signal }))
      .then(function (res) {
        _this.options.debug && debug("Fetch.response", res);
        _this.options.onHeaders(new browser_headers_1.BrowserHeaders(res.headers), res.status);
        if (res.body) {
          _this.pump(res.body.getReader(), res);
          return;
        }
        return res;
      })
      .catch(function (err) {
        if (_this.cancelled) {
          _this.options.debug && debug("Fetch.catch - request cancelled");
          return;
        }
        _this.cancelled = true;
        _this.options.debug && debug("Fetch.catch", err.message);
        _this.options.onEnd(err);
      });
  };
  CrossFetch.prototype.sendMessage = function (msgBytes) {
    this.send(msgBytes);
  };
  CrossFetch.prototype.finishSend = function () {};
  CrossFetch.prototype.start = function (metadata) {
    this.metadata = metadata;
  };
  CrossFetch.prototype.cancel = function () {
    var _this = this;
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
      this.reader.cancel().catch(function (e) {
        // This can be ignored. It will likely throw an exception due to the request being aborted
        _this.options.debug && debug("Fetch.cancel.reader.cancel exception", e);
      });
    } else {
      this.options.debug && debug("Fetch.cancel before reader");
    }
  };
  return CrossFetch;
})();
function detectFetchSupport() {
  return typeof cross_fetch_1.Response !== "undefined" && cross_fetch_1.Response.prototype.hasOwnProperty("body") && typeof cross_fetch_1.Headers === "function";
}
exports.detectFetchSupport = detectFetchSupport;
//# sourceMappingURL=cross-fetch.js.map
