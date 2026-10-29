using System;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Reflection;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Net.Client;
using Grpc.Net.Client.Web;

namespace HiddenSwitch.Proto
{
    /// <summary>
    /// Transport protocol mode for <see cref="UnityHttpMessageHandler"/>.
    /// </summary>
    public enum UnityGrpcTransportMode
    {
        /// <summary>
        /// Standard HTTP/2 gRPC (application/grpc). Supports unary, server-streaming, client-streaming,
        /// and bidirectional streaming RPCs, plus HTTP/2 PING keepalive frames.
        /// </summary>
        Http2 = 0,

        /// <summary>
        /// Binary gRPC-Web (application/grpc-web). Compatible with HTTP/1.1 and HTTP/2 proxies and UnityWebRequest.
        /// </summary>
        GrpcWeb = 1,

        /// <summary>
        /// Base64-encoded gRPC-Web-Text (application/grpc-web-text). Compatible with browser/WebGL streaming and HTTP/1.1.
        /// </summary>
        GrpcWebText = 2
    }

    /// <summary>
    /// Modern <see cref="HttpMessageHandler"/> for Spellsource gRPC clients across standalone .NET,
    /// Unity Editor/Standalone, and Unity WebGL.
    /// Supports Bearer token authorization, HTTP/2 h2c prior-knowledge, gRPC-Web / gRPC-Web-Text compatibility,
    /// and HTTP/2 keepalive pings for long-lived streaming RPCs such as Matchmaking.Enqueue and SubscribeGame.
    /// </summary>
    public sealed class UnityHttpMessageHandler : HttpMessageHandler
    {
        private static readonly PropertyInfo VersionPolicyProperty =
            typeof(HttpRequestMessage).GetProperty("VersionPolicy", BindingFlags.Public | BindingFlags.Instance);

        private static readonly object RequestVersionExactValue =
            VersionPolicyProperty != null && VersionPolicyProperty.PropertyType.IsEnum
                ? Enum.ToObject(VersionPolicyProperty.PropertyType, 2) // HttpVersionPolicy.RequestVersionExact = 2
                : null;

        private static readonly bool IsMonoOrUnityRuntime =
            Type.GetType("Mono.Runtime") != null || VersionPolicyProperty == null;

        static UnityHttpMessageHandler()
        {
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
        }

        private readonly Func<string> _accessTokenProvider;
        private readonly UnityGrpcTransportMode _mode;
        private readonly Version _httpVersion;
        private readonly HttpMessageHandler _pipeline;
        private readonly bool _disposePipeline;

        /// <summary>
        /// Optional factory hook that Unity runtimes (e.g. WebGL via UnityWebRequest) can register
        /// to provide a platform-specific inner <see cref="HttpMessageHandler"/> when <c>innerHandler</c> is not passed.
        /// Parameters are: (keepAlivePingDelay, keepAlivePingTimeout, enableMultipleHttp2Connections).
        /// </summary>
        public static Func<TimeSpan, TimeSpan, bool, HttpMessageHandler> PlatformTransportFactory { get; set; }

        /// <summary>
        /// Optional static/mutable Bearer token if no <c>accessTokenProvider</c> delegate was supplied.
        /// </summary>
        public string AccessToken { get; set; }

        /// <summary>
        /// Configured transport mode (<see cref="UnityGrpcTransportMode.Http2"/>,
        /// <see cref="UnityGrpcTransportMode.GrpcWeb"/>, or <see cref="UnityGrpcTransportMode.GrpcWebText"/>).
        /// </summary>
        public UnityGrpcTransportMode Mode => _mode;

        public UnityHttpMessageHandler(
            Func<string> accessTokenProvider = null,
            UnityGrpcTransportMode mode = UnityGrpcTransportMode.Http2,
            Version httpVersion = null,
            TimeSpan? keepAlivePingDelay = null,
            TimeSpan? keepAlivePingTimeout = null,
            bool enableMultipleHttp2Connections = true,
            HttpMessageHandler innerHandler = null)
        {
            _accessTokenProvider = accessTokenProvider;
            _mode = mode;
            _httpVersion = httpVersion ?? (mode == UnityGrpcTransportMode.Http2 ? HttpVersion.Version20 : HttpVersion.Version11);

            var transport = innerHandler ?? CreatePlatformHandler(
                _httpVersion,
                keepAlivePingDelay ?? TimeSpan.FromSeconds(15),
                keepAlivePingTimeout ?? TimeSpan.FromSeconds(10),
                enableMultipleHttp2Connections);
            _disposePipeline = innerHandler == null;

            var directTransport = new DirectPipelineInvoker(transport, _httpVersion);
            if (mode == UnityGrpcTransportMode.GrpcWeb)
            {
                _pipeline = new GrpcWebPipelineInvoker(new GrpcWebHandler(GrpcWebMode.GrpcWeb, directTransport));
            }
            else if (mode == UnityGrpcTransportMode.GrpcWebText)
            {
                _pipeline = new GrpcWebPipelineInvoker(new GrpcWebHandler(GrpcWebMode.GrpcWebText, directTransport));
            }
            else
            {
                _pipeline = directTransport;
            }
        }

        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            if (request == null)
            {
                throw new ArgumentNullException(nameof(request));
            }

            // Inject Authorization: Bearer <token> header if not already set on the request
            if (request.Headers.Authorization == null)
            {
                var token = _accessTokenProvider != null ? _accessTokenProvider() : AccessToken;
                if (!string.IsNullOrEmpty(token))
                {
                    if (token.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
                    {
                        var raw = token.Substring("Bearer ".Length).Trim();
                        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", raw);
                    }
                    else
                    {
                        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
                    }
                }
            }

            if (_mode == UnityGrpcTransportMode.Http2)
            {
                request.Version = HttpVersion.Version20;
                if (VersionPolicyProperty != null && RequestVersionExactValue != null)
                {
                    VersionPolicyProperty.SetValue(request, RequestVersionExactValue);
                }
            }
            else if (_httpVersion != null)
            {
                request.Version = _httpVersion;
                if (_httpVersion.Major == 2 && VersionPolicyProperty != null && RequestVersionExactValue != null)
                {
                    VersionPolicyProperty.SetValue(request, RequestVersionExactValue);
                }
            }

            return ((IPipelineInvoker)_pipeline).InvokeSendAsync(request, cancellationToken);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing && _disposePipeline)
            {
                _pipeline.Dispose();
            }
            base.Dispose(disposing);
        }

        /// <summary>
        /// Creates a <see cref="GrpcChannel"/> backed by <see cref="UnityHttpMessageHandler"/>.
        /// </summary>
        public static GrpcChannel CreateChannel(
            string address,
            Func<string> accessTokenProvider = null,
            UnityGrpcTransportMode mode = UnityGrpcTransportMode.Http2,
            Version httpVersion = null,
            TimeSpan? keepAlivePingDelay = null,
            TimeSpan? keepAlivePingTimeout = null,
            CallCredentials callCredentials = null,
            HttpMessageHandler innerHandler = null)
        {
            var handler = new UnityHttpMessageHandler(
                accessTokenProvider: accessTokenProvider,
                mode: mode,
                httpVersion: httpVersion,
                keepAlivePingDelay: keepAlivePingDelay,
                keepAlivePingTimeout: keepAlivePingTimeout,
                innerHandler: innerHandler);

            var options = new GrpcChannelOptions
            {
                HttpHandler = handler,
                DisposeHttpClient = true,
                MaxReceiveMessageSize = 64 * 1024 * 1024,
                MaxSendMessageSize = 64 * 1024 * 1024,
                UnsafeUseInsecureChannelCallCredentials = true
            };

            if (callCredentials != null)
            {
                options.Credentials = ChannelCredentials.Create(ChannelCredentials.Insecure, callCredentials);
            }

            return GrpcChannel.ForAddress(address, options);
        }

        /// <summary>
        /// Creates a <see cref="GrpcChannel"/> using an existing <see cref="UnityHttpMessageHandler"/>.
        /// </summary>
        public GrpcChannel CreateChannel(string address, CallCredentials callCredentials = null, bool disposeHandler = false)
        {
            var options = new GrpcChannelOptions
            {
                HttpHandler = this,
                DisposeHttpClient = disposeHandler,
                MaxReceiveMessageSize = 64 * 1024 * 1024,
                MaxSendMessageSize = 64 * 1024 * 1024,
                UnsafeUseInsecureChannelCallCredentials = true
            };

            if (callCredentials != null)
            {
                options.Credentials = ChannelCredentials.Create(ChannelCredentials.Insecure, callCredentials);
            }

            return GrpcChannel.ForAddress(address, options);
        }

        /// <summary>
        /// Creates a self-contained managed RFC 7540 HTTP/2 transport handler suitable for runtimes
        /// whose <c>System.Net.Http</c> stack only implements HTTP/1.1 (such as Unity Mono / IL2CPP).
        /// </summary>
        public static HttpMessageHandler CreateManagedHttp2Handler(
            TimeSpan? keepAlivePingDelay = null,
            TimeSpan? keepAlivePingTimeout = null)
        {
            return new ManagedHttp2Handler(
                keepAlivePingDelay ?? TimeSpan.FromSeconds(15),
                keepAlivePingTimeout ?? TimeSpan.FromSeconds(10));
        }

        private static HttpMessageHandler CreatePlatformHandler(
            Version httpVersion,
            TimeSpan keepAlivePingDelay,
            TimeSpan keepAlivePingTimeout,
            bool enableMultipleHttp2Connections)
        {
            var customFactory = PlatformTransportFactory;
            if (customFactory != null)
            {
                var customHandler = customFactory(keepAlivePingDelay, keepAlivePingTimeout, enableMultipleHttp2Connections);
                if (customHandler != null)
                {
                    return customHandler;
                }
            }

            if (httpVersion != null && httpVersion.Major >= 2 && IsMonoOrUnityRuntime)
            {
                return new ManagedHttp2Handler(keepAlivePingDelay, keepAlivePingTimeout);
            }

            var socketsHandlerType = Type.GetType("System.Net.Http.SocketsHttpHandler, System.Net.Http");
            if (socketsHandlerType != null && !IsMonoOrUnityRuntime)
            {
                try
                {
                    var handler = (HttpMessageHandler)Activator.CreateInstance(socketsHandlerType);
                    socketsHandlerType.GetProperty("EnableMultipleHttp2Connections")?.SetValue(handler, enableMultipleHttp2Connections);
                    socketsHandlerType.GetProperty("KeepAlivePingDelay")?.SetValue(handler, keepAlivePingDelay);
                    socketsHandlerType.GetProperty("KeepAlivePingTimeout")?.SetValue(handler, keepAlivePingTimeout);

                    var policyProp = socketsHandlerType.GetProperty("KeepAlivePingPolicy");
                    if (policyProp != null && policyProp.PropertyType.IsEnum)
                    {
                        // HttpKeepAlivePingPolicy.Always = 1
                        policyProp.SetValue(handler, Enum.ToObject(policyProp.PropertyType, 1));
                    }

                    return handler;
                }
                catch
                {
                    // Fall back to ManagedHttp2Handler or HttpClientHandler if reflection fails
                }
            }

            if (httpVersion != null && httpVersion.Major >= 2)
            {
                return new ManagedHttp2Handler(keepAlivePingDelay, keepAlivePingTimeout);
            }

            return new HttpClientHandler();
        }

        private interface IPipelineInvoker
        {
            Task<HttpResponseMessage> InvokeSendAsync(HttpRequestMessage request, CancellationToken cancellationToken);
        }

        private sealed class GrpcWebPipelineInvoker : DelegatingHandler, IPipelineInvoker
        {
            public GrpcWebPipelineInvoker(GrpcWebHandler inner) : base(inner)
            {
            }

            public Task<HttpResponseMessage> InvokeSendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
            {
                return base.SendAsync(request, cancellationToken);
            }
        }

        private sealed class DirectPipelineInvoker : DelegatingHandler, IPipelineInvoker
        {
            private static readonly PropertyInfo VersionPolicyProp = VersionPolicyProperty;
            private static readonly object RequestVersionExact = RequestVersionExactValue;
            private readonly Version _targetVersion;

            public DirectPipelineInvoker(HttpMessageHandler inner, Version targetVersion) : base(inner)
            {
                _targetVersion = targetVersion;
            }

            protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
            {
                if (_targetVersion != null)
                {
                    request.Version = _targetVersion;
                }
                if (VersionPolicyProp != null && RequestVersionExact != null)
                {
                    VersionPolicyProp.SetValue(request, RequestVersionExact);
                }
                return base.SendAsync(request, cancellationToken);
            }

            public Task<HttpResponseMessage> InvokeSendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
            {
                return SendAsync(request, cancellationToken);
            }
        }
    }
}
