using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Net.Security;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace HiddenSwitch.Proto
{
    /// <summary>
    /// Self-contained RFC 7540 HTTP/2 (<c>h2c</c> prior-knowledge and <c>h2</c> over TLS) <see cref="HttpMessageHandler"/>
    /// for runtimes whose <c>System.Net.Http</c> implementation only supports HTTP/1.1 (such as Unity Mono / IL2CPP).
    /// Supports full-duplex bidirectional streaming, response trailers (<see cref="HttpResponseMessage.TrailingHeaders"/>),
    /// flow-control window management (<c>WINDOW_UPDATE</c>), stream cancellation (<c>RST_STREAM</c>), and HTTP/2 <c>PING</c> keepalives.
    /// </summary>
    internal sealed class ManagedHttp2Handler : HttpMessageHandler
    {
        private readonly TimeSpan _keepAlivePingDelay;
        private readonly TimeSpan _keepAlivePingTimeout;
        private readonly object _connectionsLock = new object();
        private readonly Dictionary<string, List<Http2Connection>> _connections = new Dictionary<string, List<Http2Connection>>(StringComparer.OrdinalIgnoreCase);
        private bool _disposed;

        public ManagedHttp2Handler(TimeSpan keepAlivePingDelay, TimeSpan keepAlivePingTimeout)
        {
            _keepAlivePingDelay = keepAlivePingDelay;
            _keepAlivePingTimeout = keepAlivePingTimeout;
        }

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            if (_disposed)
            {
                throw new ObjectDisposedException(nameof(ManagedHttp2Handler));
            }

            var uri = request.RequestUri ?? throw new InvalidOperationException("RequestUri must be set.");
            var conn = await GetOrCreateConnectionAsync(uri, cancellationToken).ConfigureAwait(false);
            return await conn.SendAsync(request, cancellationToken).ConfigureAwait(false);
        }

        private async Task<Http2Connection> GetOrCreateConnectionAsync(Uri uri, CancellationToken cancellationToken)
        {
            var key = $"{uri.Scheme}://{uri.Host}:{uri.Port}";
            lock (_connectionsLock)
            {
                if (_connections.TryGetValue(key, out var list))
                {
                    for (int i = list.Count - 1; i >= 0; i--)
                    {
                        var existing = list[i];
                        if (!existing.IsActive)
                        {
                            list.RemoveAt(i);
                            continue;
                        }
                        if (existing.CanAcceptNewStream)
                        {
                            return existing;
                        }
                    }
                }
            }

            var created = new Http2Connection(uri.Host, uri.Port, string.Equals(uri.Scheme, "https", StringComparison.OrdinalIgnoreCase), _keepAlivePingDelay, _keepAlivePingTimeout);
            await created.ConnectAsync(cancellationToken).ConfigureAwait(false);

            lock (_connectionsLock)
            {
                if (_disposed)
                {
                    created.Dispose();
                    throw new ObjectDisposedException(nameof(ManagedHttp2Handler));
                }
                if (!_connections.TryGetValue(key, out var list))
                {
                    list = new List<Http2Connection>();
                    _connections[key] = list;
                }
                list.Add(created);
            }

            return created;
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing && !_disposed)
            {
                _disposed = true;
                lock (_connectionsLock)
                {
                    foreach (var kvp in _connections)
                    {
                        foreach (var conn in kvp.Value)
                        {
                            conn.Dispose();
                        }
                    }
                    _connections.Clear();
                }
            }
            base.Dispose(disposing);
        }

        private sealed class Http2Connection : IDisposable
        {
            private const byte FrameData = 0x00;
            private const byte FrameHeaders = 0x01;
            private const byte FramePriority = 0x02;
            private const byte FrameRstStream = 0x03;
            private const byte FrameSettings = 0x04;
            private const byte FramePushPromise = 0x05;
            private const byte FramePing = 0x06;
            private const byte FrameGoAway = 0x07;
            private const byte FrameWindowUpdate = 0x08;
            private const byte FrameContinuation = 0x09;

            private const byte FlagEndStream = 0x01;
            private const byte FlagAck = 0x01;
            private const byte FlagEndHeaders = 0x04;
            private const byte FlagPadded = 0x08;
            private const byte FlagPriority = 0x20;

            private static readonly byte[] ClientPreface = Encoding.ASCII.GetBytes("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n");

            private readonly string _host;
            private readonly int _port;
            private readonly bool _useTls;
            private readonly TimeSpan _keepAlivePingDelay;
            private readonly TimeSpan _keepAlivePingTimeout;
            private readonly SemaphoreSlim _writeLock = new SemaphoreSlim(1, 1);
            private readonly ConcurrentDictionary<int, StreamState> _streams = new ConcurrentDictionary<int, StreamState>();
            private readonly HpackCodec.Decoder _hpackDecoder = new HpackCodec.Decoder();
            private readonly CancellationTokenSource _connCts = new CancellationTokenSource();

            private TcpClient _tcpClient;
            private Stream _stream;
            private int _nextStreamId = 1;
            private volatile bool _closed;
            private volatile bool _goAwayReceived;
            private long _pingCounter;

            public bool IsActive => !_closed && !_goAwayReceived && _tcpClient != null && _tcpClient.Connected;
            public bool CanAcceptNewStream => IsActive && _nextStreamId < 0x3fffffff && _streams.Count < 64;

            public Http2Connection(string host, int port, bool useTls, TimeSpan keepAlivePingDelay, TimeSpan keepAlivePingTimeout)
            {
                _host = host;
                _port = port;
                _useTls = useTls;
                _keepAlivePingDelay = keepAlivePingDelay;
                _keepAlivePingTimeout = keepAlivePingTimeout;
            }

            public async Task ConnectAsync(CancellationToken cancellationToken)
            {
                _tcpClient = new TcpClient { NoDelay = true };
                using (cancellationToken.Register(() => { try { _tcpClient.Dispose(); } catch { } }))
                {
                    await _tcpClient.ConnectAsync(_host, _port).ConfigureAwait(false);
                }

                Stream netStream = _tcpClient.GetStream();
                if (_useTls)
                {
                    var ssl = new SslStream(netStream, leaveInnerStreamOpen: false);
                    var sslOptions = new SslClientAuthenticationOptions
                    {
                        TargetHost = _host,
                        ApplicationProtocols = new List<SslApplicationProtocol> { SslApplicationProtocol.Http2 }
                    };
                    await ssl.AuthenticateAsClientAsync(sslOptions, cancellationToken).ConfigureAwait(false);
                    _stream = ssl;
                }
                else
                {
                    _stream = netStream;
                }

                // Send HTTP/2 client preface + initial SETTINGS + connection-level WINDOW_UPDATE
                using var initBuf = new MemoryStream();
                initBuf.Write(ClientPreface, 0, ClientPreface.Length);

                // SETTINGS: ENABLE_PUSH (2) = 0, INITIAL_WINDOW_SIZE (4) = 64MB, MAX_FRAME_SIZE (5) = 1MB
                byte[] settingsPayload = new byte[18];
                WriteUInt16BE(settingsPayload, 0, 0x02);
                WriteUInt32BE(settingsPayload, 2, 0);
                WriteUInt16BE(settingsPayload, 6, 0x04);
                WriteUInt32BE(settingsPayload, 8, 64 * 1024 * 1024);
                WriteUInt16BE(settingsPayload, 12, 0x05);
                WriteUInt32BE(settingsPayload, 14, 1024 * 1024);
                WriteFrameToBuffer(initBuf, FrameSettings, 0, 0, settingsPayload, 0, settingsPayload.Length);

                // Connection-level WINDOW_UPDATE (streamId = 0)
                byte[] winPayload = new byte[4];
                WriteUInt32BE(winPayload, 0, (64 * 1024 * 1024) - 65535);
                WriteFrameToBuffer(initBuf, FrameWindowUpdate, 0, 0, winPayload, 0, winPayload.Length);

                var initBytes = initBuf.ToArray();
                await _stream.WriteAsync(initBytes, 0, initBytes.Length, cancellationToken).ConfigureAwait(false);
                await _stream.FlushAsync(cancellationToken).ConfigureAwait(false);

                _ = Task.Run(ReadLoopAsync);
                if (_keepAlivePingDelay > TimeSpan.Zero && _keepAlivePingDelay != Timeout.InfiniteTimeSpan)
                {
                    _ = Task.Run(KeepAliveLoopAsync);
                }
            }

            public async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
            {
                if (_closed)
                {
                    throw new IOException("HTTP/2 connection is closed.");
                }

                bool hasBody = request.Content != null;
                var headerBlock = HpackCodec.EncodeRequestHeaders(request);

                int streamId;
                StreamState state;

                await _writeLock.WaitAsync(cancellationToken).ConfigureAwait(false);
                try
                {
                    streamId = _nextStreamId;
                    _nextStreamId += 2;
                    state = new StreamState(streamId, request);
                    _streams[streamId] = state;

                    byte flags = (byte)(FlagEndHeaders | (hasBody ? 0 : FlagEndStream));
                    await WriteFrameRawAsync(FrameHeaders, flags, streamId, headerBlock, 0, headerBlock.Length, cancellationToken).ConfigureAwait(false);
                }
                finally
                {
                    _writeLock.Release();
                }

                var ctr = cancellationToken.Register(() =>
                {
                    state.Cancel(cancellationToken);
                    _ = SendRstStreamAsync(streamId, 0x08); // CANCEL = 0x08
                });
                state.SetCancellationRegistration(ctr);

                if (hasBody)
                {
                    _ = Task.Run(() => PumpRequestBodyAsync(state, request.Content, cancellationToken));
                }

                return await state.HeadersTcs.Task.ConfigureAwait(false);
            }

            private async Task PumpRequestBodyAsync(StreamState state, HttpContent content, CancellationToken cancellationToken)
            {
                try
                {
                    var reqStream = new Http2RequestStream(this, state.StreamId, cancellationToken);
                    await content.CopyToAsync(reqStream).ConfigureAwait(false);
                    if (!cancellationToken.IsCancellationRequested && !_closed && !state.IsCompleted)
                    {
                        await SendDataFrameAsync(state.StreamId, Array.Empty<byte>(), 0, 0, endStream: true, cancellationToken).ConfigureAwait(false);
                    }
                }
                catch (Exception ex)
                {
                    if (!cancellationToken.IsCancellationRequested && !state.IsCompleted)
                    {
                        state.Fault(ex);
                        await SendRstStreamAsync(state.StreamId, 0x08).ConfigureAwait(false);
                    }
                }
            }

            internal async Task SendDataFrameAsync(int streamId, byte[] buffer, int offset, int count, bool endStream, CancellationToken cancellationToken)
            {
                const int maxChunk = 16384;
                if (count == 0)
                {
                    await _writeLock.WaitAsync(cancellationToken).ConfigureAwait(false);
                    try
                    {
                        await WriteFrameRawAsync(FrameData, endStream ? FlagEndStream : (byte)0, streamId, buffer, offset, 0, cancellationToken).ConfigureAwait(false);
                    }
                    finally
                    {
                        _writeLock.Release();
                    }
                    return;
                }

                int remaining = count;
                int currentOffset = offset;
                while (remaining > 0)
                {
                    int chunk = Math.Min(remaining, maxChunk);
                    bool isLast = (remaining == chunk);
                    byte flags = (isLast && endStream) ? FlagEndStream : (byte)0;

                    await _writeLock.WaitAsync(cancellationToken).ConfigureAwait(false);
                    try
                    {
                        await WriteFrameRawAsync(FrameData, flags, streamId, buffer, currentOffset, chunk, cancellationToken).ConfigureAwait(false);
                    }
                    finally
                    {
                        _writeLock.Release();
                    }

                    currentOffset += chunk;
                    remaining -= chunk;
                }
            }

            private async Task SendRstStreamAsync(int streamId, uint errorCode)
            {
                if (_closed) return;
                byte[] payload = new byte[4];
                WriteUInt32BE(payload, 0, errorCode);
                try
                {
                    await _writeLock.WaitAsync(_connCts.Token).ConfigureAwait(false);
                    try
                    {
                        await WriteFrameRawAsync(FrameRstStream, 0, streamId, payload, 0, 4, _connCts.Token).ConfigureAwait(false);
                    }
                    finally
                    {
                        _writeLock.Release();
                    }
                }
                catch
                {
                    // Ignore write errors during cancellation
                }
            }

            private async Task SendWindowUpdateAsync(int streamId, int increment)
            {
                if (_closed || increment <= 0) return;
                byte[] payload = new byte[4];
                WriteUInt32BE(payload, 0, (uint)increment);
                try
                {
                    await _writeLock.WaitAsync(_connCts.Token).ConfigureAwait(false);
                    try
                    {
                        await WriteFrameRawAsync(FrameWindowUpdate, 0, streamId, payload, 0, 4, _connCts.Token).ConfigureAwait(false);
                    }
                    finally
                    {
                        _writeLock.Release();
                    }
                }
                catch
                {
                }
            }

            private async Task KeepAliveLoopAsync()
            {
                try
                {
                    while (!_closed && !_connCts.IsCancellationRequested)
                    {
                        await Task.Delay(_keepAlivePingDelay, _connCts.Token).ConfigureAwait(false);
                        if (_closed || _connCts.IsCancellationRequested) break;

                        long seq = Interlocked.Increment(ref _pingCounter);
                        byte[] pingData = BitConverter.GetBytes(seq);
                        await _writeLock.WaitAsync(_connCts.Token).ConfigureAwait(false);
                        try
                        {
                            await WriteFrameRawAsync(FramePing, 0, 0, pingData, 0, 8, _connCts.Token).ConfigureAwait(false);
                        }
                        finally
                        {
                            _writeLock.Release();
                        }
                    }
                }
                catch
                {
                    // Keepalive loop exits when connection closes
                }
            }

            private async Task ReadLoopAsync()
            {
                byte[] headerBuf = new byte[9];
                try
                {
                    while (!_closed && !_connCts.IsCancellationRequested)
                    {
                        if (!await ReadExactAsync(_stream, headerBuf, 0, 9, _connCts.Token).ConfigureAwait(false))
                        {
                            break;
                        }

                        int length = (headerBuf[0] << 16) | (headerBuf[1] << 8) | headerBuf[2];
                        byte type = headerBuf[3];
                        byte flags = headerBuf[4];
                        int streamId = ((headerBuf[5] & 0x7F) << 24) | (headerBuf[6] << 16) | (headerBuf[7] << 8) | headerBuf[8];

                        byte[] payload = length > 0 ? new byte[length] : Array.Empty<byte>();
                        if (length > 0)
                        {
                            if (!await ReadExactAsync(_stream, payload, 0, length, _connCts.Token).ConfigureAwait(false))
                            {
                                break;
                            }
                        }

                        await ProcessFrameAsync(type, flags, streamId, payload, headerBuf).ConfigureAwait(false);
                    }
                }
                catch (Exception ex)
                {
                    FaultAllStreams(ex);
                }
                finally
                {
                    _closed = true;
                    FaultAllStreams(new IOException("HTTP/2 connection closed."));
                }
            }

            private async Task ProcessFrameAsync(byte type, byte flags, int streamId, byte[] payload, byte[] headerBuf)
            {
                switch (type)
                {
                    case FrameSettings:
                        if ((flags & FlagAck) == 0)
                        {
                            await _writeLock.WaitAsync(_connCts.Token).ConfigureAwait(false);
                            try
                            {
                                await WriteFrameRawAsync(FrameSettings, FlagAck, 0, Array.Empty<byte>(), 0, 0, _connCts.Token).ConfigureAwait(false);
                            }
                            finally
                            {
                                _writeLock.Release();
                            }
                        }
                        break;

                    case FramePing:
                        if ((flags & FlagAck) == 0 && payload.Length == 8)
                        {
                            await _writeLock.WaitAsync(_connCts.Token).ConfigureAwait(false);
                            try
                            {
                                await WriteFrameRawAsync(FramePing, FlagAck, 0, payload, 0, 8, _connCts.Token).ConfigureAwait(false);
                            }
                            finally
                            {
                                _writeLock.Release();
                            }
                        }
                        break;

                    case FrameHeaders:
                        await HandleHeadersFrameAsync(flags, streamId, payload, headerBuf).ConfigureAwait(false);
                        break;

                    case FrameData:
                        await HandleDataFrameAsync(flags, streamId, payload).ConfigureAwait(false);
                        break;

                    case FrameRstStream:
                        if (payload.Length >= 4 && _streams.TryRemove(streamId, out var rstStream))
                        {
                            uint errorCode = ReadUInt32BE(payload, 0);
                            if (errorCode == 0)
                            {
                                rstStream.CompleteResponse();
                            }
                            else if (errorCode == 0x08)
                            {
                                rstStream.Fault(new OperationCanceledException("HTTP/2 stream cancelled."));
                            }
                            else
                            {
                                rstStream.Fault(new IOException($"HTTP/2 stream {streamId} reset with error code 0x{errorCode:x}."));
                            }
                        }
                        break;

                    case FrameGoAway:
                        _goAwayReceived = true;
                        if (payload.Length >= 8)
                        {
                            int lastStreamId = ((payload[0] & 0x7F) << 24) | (payload[1] << 16) | (payload[2] << 8) | payload[3];
                            uint errorCode = ReadUInt32BE(payload, 4);
                            foreach (var kvp in _streams)
                            {
                                if (kvp.Key > lastStreamId && _streams.TryRemove(kvp.Key, out var st))
                                {
                                    st.Fault(new IOException($"HTTP/2 GOAWAY received (error 0x{errorCode:x})."));
                                }
                            }
                        }
                        break;

                    case FrameWindowUpdate:
                    case FramePriority:
                    case FramePushPromise:
                    default:
                        break;
                }
            }

            private async Task HandleHeadersFrameAsync(byte flags, int streamId, byte[] payload, byte[] headerBuf)
            {
                int offset = 0;
                int length = payload.Length;

                if ((flags & FlagPadded) != 0)
                {
                    if (length < 1) return;
                    int padLen = payload[0];
                    offset += 1;
                    length -= (1 + padLen);
                }

                if ((flags & FlagPriority) != 0)
                {
                    offset += 5;
                    length -= 5;
                }

                if (length < 0) return;

                byte[] headerBlock;
                int blockOffset;
                int blockLength;

                if ((flags & FlagEndHeaders) != 0)
                {
                    headerBlock = payload;
                    blockOffset = offset;
                    blockLength = length;
                }
                else
                {
                    using var ms = new MemoryStream();
                    ms.Write(payload, offset, length);
                    while (true)
                    {
                        if (!await ReadExactAsync(_stream, headerBuf, 0, 9, _connCts.Token).ConfigureAwait(false))
                        {
                            throw new EndOfStreamException("Unexpected EOF while reading CONTINUATION frame.");
                        }
                        int contLen = (headerBuf[0] << 16) | (headerBuf[1] << 8) | headerBuf[2];
                        byte contType = headerBuf[3];
                        byte contFlags = headerBuf[4];
                        if (contType != FrameContinuation)
                        {
                            throw new IOException($"Expected CONTINUATION frame but got 0x{contType:x}.");
                        }
                        if (contLen > 0)
                        {
                            byte[] contPayload = new byte[contLen];
                            if (!await ReadExactAsync(_stream, contPayload, 0, contLen, _connCts.Token).ConfigureAwait(false))
                            {
                                throw new EndOfStreamException("Unexpected EOF while reading CONTINUATION payload.");
                            }
                            ms.Write(contPayload, 0, contLen);
                        }
                        if ((contFlags & FlagEndHeaders) != 0)
                        {
                            break;
                        }
                    }
                    headerBlock = ms.ToArray();
                    blockOffset = 0;
                    blockLength = headerBlock.Length;
                }

                // Always decode HPACK headers in order so dynamic table stays synchronized
                var headers = _hpackDecoder.Decode(headerBlock, blockOffset, blockLength);

                if (!_streams.TryGetValue(streamId, out var state))
                {
                    return;
                }

                bool endStream = (flags & FlagEndStream) != 0;
                state.OnHeaders(headers, endStream);
                if (endStream)
                {
                    _streams.TryRemove(streamId, out _);
                }
            }

            private async Task HandleDataFrameAsync(byte flags, int streamId, byte[] payload)
            {
                int offset = 0;
                int length = payload.Length;

                if ((flags & FlagPadded) != 0)
                {
                    if (length < 1) return;
                    int padLen = payload[0];
                    offset += 1;
                    length -= (1 + padLen);
                }

                if (payload.Length > 0)
                {
                    await SendWindowUpdateAsync(0, payload.Length).ConfigureAwait(false);
                    await SendWindowUpdateAsync(streamId, payload.Length).ConfigureAwait(false);
                }

                if (_streams.TryGetValue(streamId, out var state))
                {
                    if (length > 0)
                    {
                        state.OnData(payload, offset, length);
                    }
                    if ((flags & FlagEndStream) != 0)
                    {
                        _streams.TryRemove(streamId, out _);
                        state.CompleteResponse();
                    }
                }
            }

            private void FaultAllStreams(Exception ex)
            {
                foreach (var kvp in _streams)
                {
                    if (_streams.TryRemove(kvp.Key, out var state))
                    {
                        state.Fault(ex);
                    }
                }
            }

            private async Task WriteFrameRawAsync(byte type, byte flags, int streamId, byte[] payload, int offset, int count, CancellationToken cancellationToken)
            {
                byte[] header = new byte[9];
                header[0] = (byte)((count >> 16) & 0xFF);
                header[1] = (byte)((count >> 8) & 0xFF);
                header[2] = (byte)(count & 0xFF);
                header[3] = type;
                header[4] = flags;
                header[5] = (byte)((streamId >> 24) & 0x7F);
                header[6] = (byte)((streamId >> 16) & 0xFF);
                header[7] = (byte)((streamId >> 8) & 0xFF);
                header[8] = (byte)(streamId & 0xFF);

                await _stream.WriteAsync(header, 0, 9, cancellationToken).ConfigureAwait(false);
                if (count > 0)
                {
                    await _stream.WriteAsync(payload, offset, count, cancellationToken).ConfigureAwait(false);
                }
                await _stream.FlushAsync(cancellationToken).ConfigureAwait(false);
            }

            private static void WriteFrameToBuffer(Stream target, byte type, byte flags, int streamId, byte[] payload, int offset, int count)
            {
                byte[] header = new byte[9];
                header[0] = (byte)((count >> 16) & 0xFF);
                header[1] = (byte)((count >> 8) & 0xFF);
                header[2] = (byte)(count & 0xFF);
                header[3] = type;
                header[4] = flags;
                header[5] = (byte)((streamId >> 24) & 0x7F);
                header[6] = (byte)((streamId >> 16) & 0xFF);
                header[7] = (byte)((streamId >> 8) & 0xFF);
                header[8] = (byte)(streamId & 0xFF);
                target.Write(header, 0, 9);
                if (count > 0)
                {
                    target.Write(payload, offset, count);
                }
            }

            private static async Task<bool> ReadExactAsync(Stream stream, byte[] buffer, int offset, int count, CancellationToken cancellationToken)
            {
                int totalRead = 0;
                while (totalRead < count)
                {
                    int read = await stream.ReadAsync(buffer, offset + totalRead, count - totalRead, cancellationToken).ConfigureAwait(false);
                    if (read == 0)
                    {
                        return false;
                    }
                    totalRead += read;
                }
                return true;
            }

            private static void WriteUInt16BE(byte[] buf, int offset, ushort value)
            {
                buf[offset] = (byte)((value >> 8) & 0xFF);
                buf[offset + 1] = (byte)(value & 0xFF);
            }

            private static void WriteUInt32BE(byte[] buf, int offset, uint value)
            {
                buf[offset] = (byte)((value >> 24) & 0xFF);
                buf[offset + 1] = (byte)((value >> 16) & 0xFF);
                buf[offset + 2] = (byte)((value >> 8) & 0xFF);
                buf[offset + 3] = (byte)(value & 0xFF);
            }

            private static uint ReadUInt32BE(byte[] buf, int offset)
            {
                return ((uint)buf[offset] << 24)
                     | ((uint)buf[offset + 1] << 16)
                     | ((uint)buf[offset + 2] << 8)
                     | buf[offset + 3];
            }

            public void Dispose()
            {
                if (_closed) return;
                _closed = true;
                try { _connCts.Cancel(); } catch { }
                try { _stream?.Dispose(); } catch { }
                try { _tcpClient?.Dispose(); } catch { }
                FaultAllStreams(new ObjectDisposedException(nameof(Http2Connection)));
            }
        }

        private sealed class StreamState
        {
            public int StreamId { get; }
            public HttpRequestMessage Request { get; }
            public TaskCompletionSource<HttpResponseMessage> HeadersTcs { get; } =
                new TaskCompletionSource<HttpResponseMessage>(TaskCreationOptions.RunContinuationsAsynchronously);
            public ResponsePipeStream ResponsePipe { get; } = new ResponsePipeStream();

            private HttpResponseMessage _response;
            private CancellationTokenRegistration _ctr;
            private volatile bool _completed;

            public bool IsCompleted => _completed;

            public StreamState(int streamId, HttpRequestMessage request)
            {
                StreamId = streamId;
                Request = request;
            }

            public void SetCancellationRegistration(CancellationTokenRegistration ctr)
            {
                _ctr = ctr;
            }

            public void OnHeaders(List<(string Name, string Value)> headers, bool endStream)
            {
                if (_response == null)
                {
                    int statusCode = 200;
                    foreach (var (name, val) in headers)
                    {
                        if (name == ":status" && int.TryParse(val, out int parsed))
                        {
                            statusCode = parsed;
                            break;
                        }
                    }

                    _response = new HttpResponseMessage((HttpStatusCode)statusCode)
                    {
                        Version = HttpVersion.Version20,
                        RequestMessage = Request,
                        Content = new StreamContent(ResponsePipe)
                    };

                    foreach (var (name, val) in headers)
                    {
                        if (name.StartsWith(":", StringComparison.Ordinal))
                        {
                            continue;
                        }
                        if (name.StartsWith("content-", StringComparison.OrdinalIgnoreCase))
                        {
                            _response.Content.Headers.TryAddWithoutValidation(name, val);
                        }
                        else
                        {
                            _response.Headers.TryAddWithoutValidation(name, val);
                            if (endStream)
                            {
                                _response.TrailingHeaders.TryAddWithoutValidation(name, val);
                            }
                        }
                    }

                    if (endStream)
                    {
                        CompleteResponse();
                    }

                    HeadersTcs.TrySetResult(_response);
                }
                else
                {
                    foreach (var (name, val) in headers)
                    {
                        if (!name.StartsWith(":", StringComparison.Ordinal))
                        {
                            _response.TrailingHeaders.TryAddWithoutValidation(name, val);
                        }
                    }

                    if (endStream)
                    {
                        CompleteResponse();
                    }
                }
            }

            public void OnData(byte[] data, int offset, int count)
            {
                ResponsePipe.Append(data, offset, count);
            }

            public void CompleteResponse()
            {
                _completed = true;
                try { _ctr.Dispose(); } catch { }
                ResponsePipe.Complete();
            }

            public void Cancel(CancellationToken ct)
            {
                _completed = true;
                try { _ctr.Dispose(); } catch { }
                HeadersTcs.TrySetCanceled(ct);
                ResponsePipe.Fail(new OperationCanceledException(ct));
            }

            public void Fault(Exception ex)
            {
                _completed = true;
                try { _ctr.Dispose(); } catch { }
                HeadersTcs.TrySetException(ex);
                ResponsePipe.Fail(ex);
            }
        }

        private sealed class Http2RequestStream : Stream
        {
            private readonly Http2Connection _conn;
            private readonly int _streamId;
            private readonly CancellationToken _ct;

            public Http2RequestStream(Http2Connection conn, int streamId, CancellationToken ct)
            {
                _conn = conn;
                _streamId = streamId;
                _ct = ct;
            }

            public override bool CanRead => false;
            public override bool CanSeek => false;
            public override bool CanWrite => true;
            public override long Length => throw new NotSupportedException();
            public override long Position { get => throw new NotSupportedException(); set => throw new NotSupportedException(); }

            public override void Flush() { }
            public override Task FlushAsync(CancellationToken cancellationToken) => Task.CompletedTask;

            public override void Write(byte[] buffer, int offset, int count)
            {
                WriteAsync(buffer, offset, count, _ct).GetAwaiter().GetResult();
            }

            public override Task WriteAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken)
            {
                if (count == 0) return Task.CompletedTask;
                var token = cancellationToken.CanBeCanceled ? cancellationToken : _ct;
                return _conn.SendDataFrameAsync(_streamId, buffer, offset, count, endStream: false, token);
            }

            public override int Read(byte[] buffer, int offset, int count) => throw new NotSupportedException();
            public override long Seek(long offset, SeekOrigin origin) => throw new NotSupportedException();
            public override void SetLength(long value) => throw new NotSupportedException();
        }

        private sealed class ResponsePipeStream : Stream
        {
            private readonly object _lock = new object();
            private readonly Queue<ArraySegment<byte>> _chunks = new Queue<ArraySegment<byte>>();
            private TaskCompletionSource<bool> _waiter;
            private bool _completed;
            private Exception _error;

            public override bool CanRead => true;
            public override bool CanSeek => false;
            public override bool CanWrite => false;
            public override long Length => throw new NotSupportedException();
            public override long Position { get => throw new NotSupportedException(); set => throw new NotSupportedException(); }

            public void Append(byte[] buffer, int offset, int count)
            {
                if (count <= 0) return;
                byte[] copy = new byte[count];
                Buffer.BlockCopy(buffer, offset, copy, 0, count);
                TaskCompletionSource<bool> waiterToWake = null;
                lock (_lock)
                {
                    if (_completed) return;
                    _chunks.Enqueue(new ArraySegment<byte>(copy, 0, count));
                    waiterToWake = _waiter;
                    _waiter = null;
                }
                waiterToWake?.TrySetResult(true);
            }

            public void Complete()
            {
                TaskCompletionSource<bool> waiterToWake = null;
                lock (_lock)
                {
                    _completed = true;
                    waiterToWake = _waiter;
                    _waiter = null;
                }
                waiterToWake?.TrySetResult(true);
            }

            public void Fail(Exception ex)
            {
                TaskCompletionSource<bool> waiterToWake = null;
                lock (_lock)
                {
                    _error = ex;
                    _completed = true;
                    waiterToWake = _waiter;
                    _waiter = null;
                }
                waiterToWake?.TrySetResult(true);
            }

            public override async Task<int> ReadAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken)
            {
                while (true)
                {
                    Task waitTask;
                    lock (_lock)
                    {
                        if (_error != null)
                        {
                            throw _error;
                        }
                        if (_chunks.Count > 0)
                        {
                            var seg = _chunks.Peek();
                            int toCopy = Math.Min(count, seg.Count);
                            Buffer.BlockCopy(seg.Array, seg.Offset, buffer, offset, toCopy);
                            if (toCopy == seg.Count)
                            {
                                _chunks.Dequeue();
                            }
                            else
                            {
                                // Replace head segment
                                var remaining = new ArraySegment<byte>(seg.Array, seg.Offset + toCopy, seg.Count - toCopy);
                                var temp = _chunks.ToArray();
                                _chunks.Clear();
                                _chunks.Enqueue(remaining);
                                for (int i = 1; i < temp.Length; i++)
                                {
                                    _chunks.Enqueue(temp[i]);
                                }
                            }
                            return toCopy;
                        }
                        if (_completed)
                        {
                            return 0;
                        }
                        if (_waiter == null)
                        {
                            _waiter = new TaskCompletionSource<bool>(TaskCreationOptions.RunContinuationsAsynchronously);
                        }
                        waitTask = _waiter.Task;
                    }

                    using (cancellationToken.Register(() =>
                    {
                        TaskCompletionSource<bool> w = null;
                        lock (_lock)
                        {
                            w = _waiter;
                            _waiter = null;
                        }
                        w?.TrySetCanceled(cancellationToken);
                    }))
                    {
                        await waitTask.ConfigureAwait(false);
                    }
                }
            }

            public override int Read(byte[] buffer, int offset, int count)
            {
                return ReadAsync(buffer, offset, count, CancellationToken.None).GetAwaiter().GetResult();
            }

            public override void Flush() { }
            public override long Seek(long offset, SeekOrigin origin) => throw new NotSupportedException();
            public override void SetLength(long value) => throw new NotSupportedException();
            public override void Write(byte[] buffer, int offset, int count) => throw new NotSupportedException();
        }

        private static class HpackCodec
        {
            private static readonly (string Name, string Value)[] StaticTable = new (string, string)[]
            {
                (":authority", ""),
                (":method", "GET"),
                (":method", "POST"),
                (":path", "/"),
                (":path", "/index.html"),
                (":scheme", "http"),
                (":scheme", "https"),
                (":status", "200"),
                (":status", "204"),
                (":status", "206"),
                (":status", "304"),
                (":status", "400"),
                (":status", "404"),
                (":status", "500"),
                ("accept-charset", ""),
                ("accept-encoding", "gzip, deflate"),
                ("accept-language", ""),
                ("accept-ranges", ""),
                ("accept", ""),
                ("access-control-allow-origin", ""),
                ("age", ""),
                ("allow", ""),
                ("authorization", ""),
                ("cache-control", ""),
                ("content-disposition", ""),
                ("content-encoding", ""),
                ("content-language", ""),
                ("content-length", ""),
                ("content-location", ""),
                ("content-range", ""),
                ("content-type", ""),
                ("cookie", ""),
                ("date", ""),
                ("etag", ""),
                ("expect", ""),
                ("expires", ""),
                ("from", ""),
                ("host", ""),
                ("if-match", ""),
                ("if-modified-since", ""),
                ("if-none-match", ""),
                ("if-range", ""),
                ("if-unmodified-since", ""),
                ("last-modified", ""),
                ("link", ""),
                ("location", ""),
                ("max-forwards", ""),
                ("proxy-authenticate", ""),
                ("proxy-authorization", ""),
                ("range", ""),
                ("referer", ""),
                ("refresh", ""),
                ("retry-after", ""),
                ("server", ""),
                ("set-cookie", ""),
                ("strict-transport-security", ""),
                ("transfer-encoding", ""),
                ("user-agent", ""),
                ("vary", ""),
                ("via", ""),
                ("www-authenticate", "")
            };

            private static readonly int[] HuffmanCodes = new int[]
            {
                0x1ff8,0x7fffd8,0xfffffe2,0xfffffe3,0xfffffe4,0xfffffe5,0xfffffe6,0xfffffe7,0xfffffe8,0xffffea,0x3ffffffc,0xfffffe9,0xfffffea,0x3ffffffd,0xfffffeb,0xfffffec,0xfffffed,0xfffffee,0xfffffef,0xffffff0,0xffffff1,0xffffff2,0x3ffffffe,0xffffff3,0xffffff4,0xffffff5,0xffffff6,0xffffff7,0xffffff8,0xffffff9,0xffffffa,0xffffffb,0x14,0x3f8,0x3f9,0xffa,0x1ff9,0x15,0xf8,0x7fa,0x3fa,0x3fb,0xf9,0x7fb,0xfa,0x16,0x17,0x18,0x0,0x1,0x2,0x19,0x1a,0x1b,0x1c,0x1d,0x1e,0x1f,0x5c,0xfb,0x7ffc,0x20,0xffb,0x3fc,0x1ffa,0x21,0x5d,0x5e,0x5f,0x60,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x6a,0x6b,0x6c,0x6d,0x6e,0x6f,0x70,0x71,0x72,0xfc,0x73,0xfd,0x1ffb,0x7fff0,0x1ffc,0x3ffc,0x22,0x7ffd,0x3,0x23,0x4,0x24,0x5,0x25,0x26,0x27,0x6,0x74,0x75,0x28,0x29,0x2a,0x7,0x2b,0x76,0x2c,0x8,0x9,0x2d,0x77,0x78,0x79,0x7a,0x7b,0x7ffe,0x7fc,0x3ffd,0x1ffd,0xffffffc,0xfffe6,0x3fffd2,0xfffe7,0xfffe8,0x3fffd3,0x3fffd4,0x3fffd5,0x7fffd9,0x3fffd6,0x7fffda,0x7fffdb,0x7fffdc,0x7fffdd,0x7fffde,0xffffeb,0x7fffdf,0xffffec,0xffffed,0x3fffd7,0x7fffe0,0xffffee,0x7fffe1,0x7fffe2,0x7fffe3,0x7fffe4,0x1fffdc,0x3fffd8,0x7fffe5,0x3fffd9,0x7fffe6,0x7fffe7,0xffffef,0x3fffda,0x1fffdd,0xfffe9,0x3fffdb,0x3fffdc,0x7fffe8,0x7fffe9,0x1fffde,0x7fffea,0x3fffdd,0x3fffde,0xfffff0,0x1fffdf,0x3fffdf,0x7fffeb,0x7fffec,0x1fffe0,0x1fffe1,0x3fffe0,0x1fffe2,0x7fffed,0x3fffe1,0x7fffee,0x7fffef,0xfffea,0x3fffe2,0x3fffe3,0x3fffe4,0x7ffff0,0x3fffe5,0x3fffe6,0x7ffff1,0x3ffffe0,0x3ffffe1,0xfffeb,0x7fff1,0x3fffe7,0x7ffff2,0x3fffe8,0x1ffffec,0x3ffffe2,0x3ffffe3,0x3ffffe4,0x7ffffde,0x7ffffdf,0x3ffffe5,0xfffff1,0x1ffffed,0x7fff2,0x1fffe3,0x3ffffe6,0x7ffffe0,0x7ffffe1,0x3ffffe7,0x7ffffe2,0xfffff2,0x1fffe4,0x1fffe5,0x3ffffe8,0x3ffffe9,0xffffffd,0x7ffffe3,0x7ffffe4,0x7ffffe5,0xfffec,0xfffff3,0xfffed,0x1fffe6,0x3fffe9,0x1fffe7,0x1fffe8,0x7ffff3,0x3fffea,0x3fffeb,0x1ffffee,0x1ffffef,0xfffff4,0xfffff5,0x3ffffea,0x7ffff4,0x3ffffeb,0x7ffffe6,0x3ffffec,0x3ffffed,0x7ffffe7,0x7ffffe8,0x7ffffe9,0x7ffffea,0x7ffffeb,0xffffffe,0x7ffffec,0x7ffffed,0x7ffffee,0x7ffffef,0x7fffff0,0x3ffffee,0x3fffffff
            };

            private static readonly byte[] HuffmanCodeLengths = new byte[]
            {
                13,23,28,28,28,28,28,28,28,24,30,28,28,30,28,28,28,28,28,28,28,28,30,28,28,28,28,28,28,28,28,28,6,10,10,12,13,6,8,11,10,10,8,11,8,6,6,6,5,5,5,6,6,6,6,6,6,6,7,8,15,6,12,10,13,6,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,8,7,8,13,19,13,14,6,15,5,6,5,6,5,6,6,6,5,7,7,6,6,6,5,6,7,6,5,5,6,7,7,7,7,7,15,11,14,13,28,20,22,20,20,22,22,22,23,22,23,23,23,23,23,24,23,24,24,22,23,24,23,23,23,23,21,22,23,22,23,23,24,22,21,20,22,22,23,23,21,23,22,22,24,21,22,23,23,21,21,22,21,23,22,23,23,20,22,22,22,23,22,22,23,26,26,20,19,22,23,22,25,26,26,26,27,27,26,24,25,19,21,26,27,27,26,27,24,21,21,26,26,28,27,27,27,20,24,20,21,22,21,21,23,22,22,25,25,24,24,26,23,26,27,26,26,27,27,27,27,27,28,27,27,27,27,27,26,30
            };

            private sealed class HuffmanNode
            {
                public HuffmanNode Left;
                public HuffmanNode Right;
                public int Symbol = -1;
            }

            private static readonly HuffmanNode HuffmanRoot = BuildHuffmanTree();

            private static HuffmanNode BuildHuffmanTree()
            {
                var root = new HuffmanNode();
                for (int sym = 0; sym < 256; sym++)
                {
                    int code = HuffmanCodes[sym];
                    int len = HuffmanCodeLengths[sym];
                    var cur = root;
                    for (int bit = len - 1; bit >= 0; bit--)
                    {
                        int b = (code >> bit) & 1;
                        if (b == 0)
                        {
                            cur.Left ??= new HuffmanNode();
                            cur = cur.Left;
                        }
                        else
                        {
                            cur.Right ??= new HuffmanNode();
                            cur = cur.Right;
                        }
                    }
                    cur.Symbol = sym;
                }
                return root;
            }

            public static byte[] EncodeRequestHeaders(HttpRequestMessage request)
            {
                using var ms = new MemoryStream();
                var uri = request.RequestUri;

                WriteLiteralHeaderWithoutIndexing(ms, ":method", request.Method.Method);
                WriteLiteralHeaderWithoutIndexing(ms, ":scheme", uri.Scheme);
                WriteLiteralHeaderWithoutIndexing(ms, ":authority", request.Headers.Host ?? uri.Authority);
                WriteLiteralHeaderWithoutIndexing(ms, ":path", uri.PathAndQuery);

                bool hasTe = false;
                foreach (var h in request.Headers)
                {
                    var lower = h.Key.ToLowerInvariant();
                    if (lower == "host" || lower == "connection" || lower == "transfer-encoding" || lower == "upgrade")
                    {
                        continue;
                    }
                    if (lower == "te")
                    {
                        hasTe = true;
                    }
                    WriteLiteralHeaderWithoutIndexing(ms, lower, string.Join(", ", h.Value));
                }

                if (!hasTe)
                {
                    WriteLiteralHeaderWithoutIndexing(ms, "te", "trailers");
                }

                if (request.Content != null)
                {
                    bool hasContentType = false;
                    foreach (var h in request.Content.Headers)
                    {
                        var lower = h.Key.ToLowerInvariant();
                        if (lower == "content-length")
                        {
                            continue;
                        }
                        if (lower == "content-type")
                        {
                            hasContentType = true;
                        }
                        WriteLiteralHeaderWithoutIndexing(ms, lower, string.Join(", ", h.Value));
                    }
                    if (!hasContentType)
                    {
                        WriteLiteralHeaderWithoutIndexing(ms, "content-type", "application/grpc");
                    }
                }

                return ms.ToArray();
            }

            private static void WriteLiteralHeaderWithoutIndexing(Stream s, string name, string value)
            {
                // 0000 0000 = Literal Header Field without Indexing — New Name
                s.WriteByte(0x00);
                WriteString(s, name);
                WriteString(s, value);
            }

            private static void WriteString(Stream s, string value)
            {
                byte[] bytes = Encoding.ASCII.GetBytes(value ?? string.Empty);
                WriteInteger(s, 0x00, 7, bytes.Length);
                s.Write(bytes, 0, bytes.Length);
            }

            private static void WriteInteger(Stream s, byte mask, int prefixBits, int value)
            {
                int maxPrefix = (1 << prefixBits) - 1;
                if (value < maxPrefix)
                {
                    s.WriteByte((byte)(mask | value));
                }
                else
                {
                    s.WriteByte((byte)(mask | maxPrefix));
                    value -= maxPrefix;
                    while (value >= 128)
                    {
                        s.WriteByte((byte)((value & 0x7F) | 0x80));
                        value >>= 7;
                    }
                    s.WriteByte((byte)value);
                }
            }

            public sealed class Decoder
            {
                private readonly List<(string Name, string Value)> _dynamicTable = new List<(string, string)>();
                private int _maxDynamicTableSize = 4096;
                private int _currentDynamicTableSize;

                public List<(string Name, string Value)> Decode(byte[] buf, int offset, int length)
                {
                    var result = new List<(string Name, string Value)>();
                    int end = offset + length;
                    while (offset < end)
                    {
                        byte b = buf[offset];
                        if ((b & 0x80) != 0)
                        {
                            int index = DecodeInteger(buf, ref offset, 7);
                            result.Add(GetEntry(index));
                        }
                        else if ((b & 0x40) != 0)
                        {
                            int nameIdx = DecodeInteger(buf, ref offset, 6);
                            string name = nameIdx > 0 ? GetEntry(nameIdx).Name : DecodeString(buf, ref offset);
                            string val = DecodeString(buf, ref offset);
                            AddDynamicEntry(name, val);
                            result.Add((name, val));
                        }
                        else if ((b & 0x20) != 0)
                        {
                            int newMax = DecodeInteger(buf, ref offset, 5);
                            _maxDynamicTableSize = newMax;
                            EvictDynamicTable(0);
                        }
                        else
                        {
                            int nameIdx = DecodeInteger(buf, ref offset, 4);
                            string name = nameIdx > 0 ? GetEntry(nameIdx).Name : DecodeString(buf, ref offset);
                            string val = DecodeString(buf, ref offset);
                            result.Add((name, val));
                        }
                    }
                    return result;
                }

                private (string Name, string Value) GetEntry(int index)
                {
                    if (index >= 1 && index <= StaticTable.Length)
                    {
                        return StaticTable[index - 1];
                    }
                    int dynIdx = index - StaticTable.Length - 1;
                    if (dynIdx >= 0 && dynIdx < _dynamicTable.Count)
                    {
                        return _dynamicTable[dynIdx];
                    }
                    return (string.Empty, string.Empty);
                }

                private void AddDynamicEntry(string name, string value)
                {
                    int entrySize = (name?.Length ?? 0) + (value?.Length ?? 0) + 32;
                    EvictDynamicTable(entrySize);
                    if (entrySize <= _maxDynamicTableSize)
                    {
                        _dynamicTable.Insert(0, (name, value));
                        _currentDynamicTableSize += entrySize;
                    }
                }

                private void EvictDynamicTable(int incomingSize)
                {
                    while (_currentDynamicTableSize + incomingSize > _maxDynamicTableSize && _dynamicTable.Count > 0)
                    {
                        var last = _dynamicTable[_dynamicTable.Count - 1];
                        _dynamicTable.RemoveAt(_dynamicTable.Count - 1);
                        _currentDynamicTableSize -= (last.Name?.Length ?? 0) + (last.Value?.Length ?? 0) + 32;
                    }
                }

                private static int DecodeInteger(byte[] buf, ref int offset, int prefixBits)
                {
                    int maxPrefix = (1 << prefixBits) - 1;
                    int value = buf[offset++] & maxPrefix;
                    if (value < maxPrefix)
                    {
                        return value;
                    }
                    int shift = 0;
                    while (true)
                    {
                        byte b = buf[offset++];
                        value += (b & 0x7F) << shift;
                        if ((b & 0x80) == 0)
                        {
                            break;
                        }
                        shift += 7;
                    }
                    return value;
                }

                private static string DecodeString(byte[] buf, ref int offset)
                {
                    bool huffman = (buf[offset] & 0x80) != 0;
                    int len = DecodeInteger(buf, ref offset, 7);
                    if (len == 0)
                    {
                        return string.Empty;
                    }
                    string s = huffman
                        ? DecodeHuffman(buf, offset, len)
                        : Encoding.UTF8.GetString(buf, offset, len);
                    offset += len;
                    return s;
                }

                private static string DecodeHuffman(byte[] buf, int offset, int len)
                {
                    using var ms = new MemoryStream(len * 2);
                    var node = HuffmanRoot;
                    int end = offset + len;
                    for (int i = offset; i < end; i++)
                    {
                        byte b = buf[i];
                        for (int bit = 7; bit >= 0; bit--)
                        {
                            int bitVal = (b >> bit) & 1;
                            node = bitVal == 0 ? node.Left : node.Right;
                            if (node == null)
                            {
                                node = HuffmanRoot;
                            }
                            else if (node.Symbol >= 0)
                            {
                                ms.WriteByte((byte)node.Symbol);
                                node = HuffmanRoot;
                            }
                        }
                    }
                    return Encoding.UTF8.GetString(ms.ToArray());
                }
            }
        }
    }
}
