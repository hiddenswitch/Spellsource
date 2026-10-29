using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Threading;
using System.Threading.Tasks;
using Google.Protobuf.WellKnownTypes;
using Grpc.Core;
using Grpc.Net.Client;
using HiddenSwitch.Proto;
using NUnit.Framework;
using Spellsource.Proto;
using HsCreateAccountRequest = HiddenSwitch.Proto.CreateAccountRequest;
using HsGetAccountsRequest = HiddenSwitch.Proto.GetAccountsRequest;
using HsLoginRequest = HiddenSwitch.Proto.LoginRequest;

namespace Framework.Tests
{
    [TestFixture]
    public class GrpcIntegrationTests
    {
        private static Process _serverProcess;
        private static string _serverAddress;
        private static int _serverPort;

        [OneTimeSetUp]
        public async Task OneTimeSetUp()
        {
            var portStr = Environment.GetEnvironmentVariable("SPELLSOURCE_GRPC_PORT");
            _serverPort = int.TryParse(portStr, out var parsedPort) ? parsedPort : 8081;
            _serverAddress = Environment.GetEnvironmentVariable("SPELLSOURCE_GRPC_URL") ?? $"http://127.0.0.1:{_serverPort}";

            if (!await IsGrpcServerReadyAsync(_serverAddress))
            {
                var repoRoot = FindRepoRoot();
                var gradlew = Path.Combine(repoRoot, "gradlew");
                var psi = new ProcessStartInfo
                {
                    FileName = gradlew,
                    Arguments = ":spellsource-server:run --no-daemon",
                    WorkingDirectory = repoRoot,
                    UseShellExecute = false,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true
                };
                _serverProcess = Process.Start(psi);
                Assert.That(_serverProcess, Is.Not.Null, "Failed to start Java Spellsource server process.");
                _serverProcess.OutputDataReceived += (_, e) =>
                {
                    if (!string.IsNullOrEmpty(e.Data))
                    {
                        TestContext.Progress.WriteLine("[server] " + e.Data);
                    }
                };
                _serverProcess.ErrorDataReceived += (_, e) =>
                {
                    if (!string.IsNullOrEmpty(e.Data))
                    {
                        TestContext.Progress.WriteLine("[server-err] " + e.Data);
                    }
                };
                _serverProcess.BeginOutputReadLine();
                _serverProcess.BeginErrorReadLine();

                var deadline = DateTime.UtcNow.AddMinutes(4);
                var ready = false;
                while (DateTime.UtcNow < deadline)
                {
                    if (_serverProcess.HasExited)
                    {
                        Assert.Fail($"Java Spellsource server process exited prematurely with code {_serverProcess.ExitCode}.");
                    }
                    if (await IsGrpcServerReadyAsync(_serverAddress))
                    {
                        ready = true;
                        break;
                    }
                    await Task.Delay(1000);
                }

                Assert.That(ready, Is.True, $"Timed out waiting for Java Spellsource gRPC server at {_serverAddress}");
            }
        }

        [OneTimeTearDown]
        public void OneTimeTearDown()
        {
            if (_serverProcess != null && !_serverProcess.HasExited)
            {
                try
                {
                    _serverProcess.Kill(entireProcessTree: true);
                    _serverProcess.WaitForExit(10000);
                }
                catch
                {
                    // Ignore teardown exceptions
                }
                finally
                {
                    _serverProcess.Dispose();
                    _serverProcess = null;
                }
            }
        }

        private static string FindRepoRoot()
        {
            var dir = new DirectoryInfo(TestContext.CurrentContext.TestDirectory);
            while (dir != null)
            {
                if (File.Exists(Path.Combine(dir.FullName, "settings.gradle")))
                {
                    return dir.FullName;
                }
                dir = dir.Parent;
            }
            throw new DirectoryNotFoundException("Could not locate repository root containing settings.gradle");
        }

        private static async Task<bool> IsGrpcServerReadyAsync(string address)
        {
            try
            {
                using var channel = UnityHttpMessageHandler.CreateChannel(address);
                var client = new Unauthenticated.UnauthenticatedClient(channel);
                var res = await client.VerifyTokenAsync(
                    new AccessTokenResponse { Token = "probe" },
                    deadline: DateTime.UtcNow.AddSeconds(3));
                return res != null;
            }
            catch
            {
                return false;
            }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // 1. Error Statuses & Detailed Error Messages
        // ─────────────────────────────────────────────────────────────────────────

        [Test]
        public void ErrorStatuses_UnauthenticatedServiceRejectsMissingAndInvalidToken()
        {
            // 1. Missing Authorization header
            using (var channel = UnityHttpMessageHandler.CreateChannel(_serverAddress))
            {
                var cardsClient = new AuthenticatedCards.AuthenticatedCardsClient(channel);
                var ex1 = Assert.ThrowsAsync<RpcException>(async () =>
                    await cardsClient.GetCardsByUserAsync(new GetCardsRequest()));
                Assert.That(ex1.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));
                Assert.That(ex1.Status.Detail, Is.EqualTo("Unauthenticated"));

                var legacyClient = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(channel);
                var ex2 = Assert.ThrowsAsync<RpcException>(async () =>
                    await legacyClient.DecksGetAllAsync(new Empty()));
                Assert.That(ex2.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));
                Assert.That(ex2.Status.Detail, Is.EqualTo("Unauthenticated"));

                var accountsClient = new Accounts.AccountsClient(channel);
                var ex3 = Assert.ThrowsAsync<RpcException>(async () =>
                    await accountsClient.GetAccountsAsync(new HsGetAccountsRequest()));
                Assert.That(ex3.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));
                Assert.That(ex3.Status.Detail, Is.EqualTo("Unauthenticated"));

                var gamesClient = new Games.GamesClient(channel);
                var ex4 = Assert.ThrowsAsync<RpcException>(async () =>
                    await gamesClient.IsInMatchAsync(new Empty()));
                Assert.That(ex4.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));
                Assert.That(ex4.Status.Detail, Is.EqualTo("Unauthenticated"));
            }

            // 2. Invalid / malformed Bearer token
            using (var badTokenChannel = UnityHttpMessageHandler.CreateChannel(
                       _serverAddress,
                       accessTokenProvider: () => "invalid.jwt.token"))
            {
                var cardsClient = new AuthenticatedCards.AuthenticatedCardsClient(badTokenChannel);
                var ex = Assert.ThrowsAsync<RpcException>(async () =>
                    await cardsClient.GetCardsByUserAsync(new GetCardsRequest()));
                Assert.That(ex.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));
                Assert.That(ex.Status.Detail, Is.EqualTo("Unauthenticated"));
            }
        }

        [Test]
        public async Task ErrorStatuses_InvalidArgumentAndDetailedMessages()
        {
            using var channel = UnityHttpMessageHandler.CreateChannel(_serverAddress);
            var unauth = new Unauthenticated.UnauthenticatedClient(channel);

            // 1. Username too short (< 4 chars)
            var shortUserEx = Assert.ThrowsAsync<RpcException>(async () =>
                await unauth.CreateAccountAsync(new HsCreateAccountRequest
                {
                    Username = "ab",
                    Email = $"{Guid.NewGuid():N}@hiddenswitch.com",
                    Password = "validpassword"
                }));
            Assert.That(shortUserEx.StatusCode, Is.EqualTo(StatusCode.InvalidArgument));
            Assert.That(shortUserEx.Status.Detail, Does.Contain("The username must be at least 4 characters."));

            // 2. Password too short (< 4 chars)
            var shortPassEx = Assert.ThrowsAsync<RpcException>(async () =>
                await unauth.CreateAccountAsync(new HsCreateAccountRequest
                {
                    Username = $"user_{Guid.NewGuid():N}".Substring(0, 16),
                    Email = $"{Guid.NewGuid():N}@hiddenswitch.com",
                    Password = "12"
                }));
            Assert.That(shortPassEx.StatusCode, Is.EqualTo(StatusCode.InvalidArgument));
            Assert.That(shortPassEx.Status.Detail, Does.Contain("The password must be at least 4 characters."));

            // 3. Duplicate username
            var username = $"dup_{Guid.NewGuid():N}".Substring(0, 16);
            var email = $"{username}@hiddenswitch.com";
            var created = await unauth.CreateAccountAsync(new HsCreateAccountRequest
            {
                Username = username,
                Email = email,
                Password = "validpassword"
            });
            Assert.That(created.AccessTokenResponse.Token, Is.Not.Empty);

            var dupEx = Assert.ThrowsAsync<RpcException>(async () =>
                await unauth.CreateAccountAsync(new HsCreateAccountRequest
                {
                    Username = username,
                    Email = $"other_{email}",
                    Password = "validpassword"
                }));
            Assert.That(dupEx.StatusCode, Is.EqualTo(StatusCode.InvalidArgument));
            Assert.That(dupEx.Status.Detail, Does.Contain("invalid"));

            // 4. Invalid login password
            var badLoginEx = Assert.ThrowsAsync<RpcException>(async () =>
                await unauth.LoginAsync(new HsLoginRequest
                {
                    UsernameOrEmail = username,
                    Password = "wrongpassword"
                }));
            Assert.That(badLoginEx.StatusCode, Is.EqualTo(StatusCode.InvalidArgument));
            Assert.That(badLoginEx.Status.Detail, Does.Contain("The username, email or password are invalid."));
        }

        [Test]
        public async Task ErrorStatuses_UnimplementedAndInternalErrors()
        {
            using var unauthChannel = UnityHttpMessageHandler.CreateChannel(_serverAddress);
            var unauth = new Unauthenticated.UnauthenticatedClient(unauthChannel);
            var guest = await unauth.CreateAccountAsync(new HsCreateAccountRequest { Guest = true });
            var token = guest.AccessTokenResponse.Token;

            using var authChannel = UnityHttpMessageHandler.CreateChannel(
                _serverAddress,
                accessTokenProvider: () => token);

            // 1. Unimplemented method on a registered service path
            var fakeMethod = new Method<Empty, Empty>(
                MethodType.Unary,
                "hiddenswitch.Unauthenticated",
                "NonExistentMethod",
                Marshallers.Create(m => new byte[0], _ => new Empty()),
                Marshallers.Create(m => new byte[0], _ => new Empty()));

            var invoker = unauthChannel.CreateCallInvoker();
            var unimpEx = Assert.ThrowsAsync<RpcException>(async () =>
                await invoker.AsyncUnaryCall(fakeMethod, null, new CallOptions(), new Empty()).ResponseAsync);
            Assert.That(unimpEx.StatusCode, Is.EqualTo(StatusCode.Unimplemented));
            Assert.That(unimpEx.Status.Detail, Does.Contain("Method not found: hiddenswitch.Unauthenticated/NonExistentMethod"));

            // 2. Deleting a non-existent deck returns error with "deck not found"
            var legacy = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(authChannel);
            var deleteEx = Assert.ThrowsAsync<RpcException>(async () =>
                await legacy.DecksDeleteAsync(new DecksDeleteRequest { DeckId = "nonexistent_deck_id_12345" }));
            Assert.That(deleteEx.StatusCode, Is.Not.EqualTo(StatusCode.OK));
            Assert.That(deleteEx.Status.Detail, Does.Contain("deck not found"));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // 2. Authorization Header & Token Propagation
        // ─────────────────────────────────────────────────────────────────────────

        [Test]
        public async Task AuthorizationHeader_ProviderAndDynamicTokenRotationAndIsolation()
        {
            using var baseChannel = UnityHttpMessageHandler.CreateChannel(_serverAddress);
            var unauth = new Unauthenticated.UnauthenticatedClient(baseChannel);

            // Create two distinct users
            var user1Name = $"u1_{Guid.NewGuid():N}".Substring(0, 16);
            var user1Email = $"{user1Name}@hiddenswitch.com";
            var user1Reply = await unauth.CreateAccountAsync(new HsCreateAccountRequest
            {
                Username = user1Name,
                Email = user1Email,
                Password = "password123",
                Decks = true
            });

            var user2Name = $"u2_{Guid.NewGuid():N}".Substring(0, 16);
            var user2Email = $"{user2Name}@hiddenswitch.com";
            var user2Reply = await unauth.CreateAccountAsync(new HsCreateAccountRequest
            {
                Username = user2Name,
                Email = user2Email,
                Password = "password123",
                Decks = false
            });

            var token1 = user1Reply.AccessTokenResponse.Token;
            var token2 = user2Reply.AccessTokenResponse.Token;

            // VerifyToken confirms valid and invalid tokens
            var verifyValid = await unauth.VerifyTokenAsync(user1Reply.AccessTokenResponse);
            Assert.That(verifyValid.Value, Is.True);
            var verifyInvalid = await unauth.VerifyTokenAsync(new AccessTokenResponse { Token = "not-a-valid-token" });
            Assert.That(verifyInvalid.Value, Is.False);

            // Test dynamic token rotation on a single UnityHttpMessageHandler
            using var handler = new UnityHttpMessageHandler();
            using var dynamicChannel = handler.CreateChannel(_serverAddress);
            var accountsClient = new Accounts.AccountsClient(dynamicChannel);
            var legacyClient = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(dynamicChannel);

            // Initially no token -> Unauthenticated
            var unauthEx = Assert.ThrowsAsync<RpcException>(async () =>
                await accountsClient.GetAccountsAsync(new HsGetAccountsRequest { Ids = { user1Reply.UserEntity.Id } }));
            Assert.That(unauthEx.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));

            // Set token1 -> Authenticated as user1
            handler.AccessToken = token1;
            var accountsRes = await accountsClient.GetAccountsAsync(new HsGetAccountsRequest
            {
                Ids = { user1Reply.UserEntity.Id, user2Reply.UserEntity.Id }
            });
            var byId = accountsRes.UserEntities.ToDictionary(u => u.Id);
            Assert.That(byId[user1Reply.UserEntity.Id].Email, Is.EqualTo(user1Email), "Own email should be visible");
            Assert.That(byId[user2Reply.UserEntity.Id].Email, Is.Empty, "Other user's email must be redacted");

            var user1Decks = await legacyClient.DecksGetAllAsync(new Empty());
            Assert.That(user1Decks.Decks.Count, Is.GreaterThan(0), "User1 with premade decks should have decks");

            // Rotate token to token2 on the exact same handler/channel -> Authenticated as user2
            handler.AccessToken = token2;
            var user2Decks = await legacyClient.DecksGetAllAsync(new Empty());
            Assert.That(user2Decks.Decks.Count, Is.EqualTo(0), "User2 without premade decks should have 0 decks");

            // Also verify per-call Metadata header and CallCredentials on an unauthenticated channel
            var metadata = new Metadata { { "authorization", $"Bearer {token1}" } };
            var viaMetadata = await new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(baseChannel)
                .DecksGetAllAsync(new Empty(), metadata);
            Assert.That(viaMetadata.Decks.Count, Is.EqualTo(user1Decks.Decks.Count));

            var callCreds = CallCredentials.FromInterceptor((_, meta) =>
            {
                meta.Add("authorization", $"Bearer {token1}");
                return Task.CompletedTask;
            });
            using var credsChannel = UnityHttpMessageHandler.CreateChannel(_serverAddress, callCredentials: callCreds);
            var viaCreds = await new AuthenticatedCards.AuthenticatedCardsClient(credsChannel)
                .GetCardsByUserAsync(new GetCardsRequest());
            Assert.That(viaCreds.Content, Is.Not.Null);
            var viaCredsDecks = await new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(credsChannel)
                .DecksGetAllAsync(new Empty());
            Assert.That(viaCredsDecks.Decks.Count, Is.EqualTo(user1Decks.Decks.Count));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // 3. Web Compatibility Layer (GrpcWeb & GrpcWebText over HTTP/1.1 & HTTP/2)
        // ─────────────────────────────────────────────────────────────────────────

        [TestCase(UnityGrpcTransportMode.GrpcWeb, "1.1")]
        [TestCase(UnityGrpcTransportMode.GrpcWebText, "1.1")]
        [TestCase(UnityGrpcTransportMode.GrpcWeb, "2.0")]
        [TestCase(UnityGrpcTransportMode.GrpcWebText, "2.0")]
        public async Task WebCompatibility_UnaryAuthCardsAndErrorsWorkAcrossModes(
            UnityGrpcTransportMode mode,
            string httpVersionString)
        {
            var httpVersion = Version.Parse(httpVersionString);
            string currentToken = null;

            using var channel = UnityHttpMessageHandler.CreateChannel(
                _serverAddress,
                accessTokenProvider: () => currentToken,
                mode: mode,
                httpVersion: httpVersion);

            var unauth = new Unauthenticated.UnauthenticatedClient(channel);
            var unauthCards = new UnauthenticatedCards.UnauthenticatedCardsClient(channel);
            var authCards = new AuthenticatedCards.AuthenticatedCardsClient(channel);
            var legacy = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(channel);

            // 1. Unauthenticated call to protected service over gRPC-Web returns StatusCode.Unauthenticated
            var unauthEx = Assert.ThrowsAsync<RpcException>(async () =>
                await authCards.GetCardsByUserAsync(new GetCardsRequest()));
            Assert.That(unauthEx.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));
            Assert.That(unauthEx.Status.Detail, Is.EqualTo("Unauthenticated"));

            // 2. InvalidArgument error status and message over gRPC-Web
            var invalidEx = Assert.ThrowsAsync<RpcException>(async () =>
                await unauth.CreateAccountAsync(new HsCreateAccountRequest
                {
                    Username = "x",
                    Email = "x@hiddenswitch.com",
                    Password = "password"
                }));
            Assert.That(invalidEx.StatusCode, Is.EqualTo(StatusCode.InvalidArgument));
            Assert.That(invalidEx.Status.Detail, Does.Contain("The username must be at least 4 characters."));

            // 3. Create guest account over gRPC-Web
            var accountReply = await unauth.CreateAccountAsync(new HsCreateAccountRequest { Guest = true, Decks = true });
            Assert.That(accountReply.AccessTokenResponse.Token, Is.Not.Empty);
            currentToken = accountReply.AccessTokenResponse.Token;

            // 4. Authenticated calls over gRPC-Web (DecksGetAll + AuthenticatedCards.GetCardsByUser)
            var decksReply = await legacy.DecksGetAllAsync(new Empty());
            Assert.That(decksReply.Decks.Count, Is.GreaterThan(0));

            var cardsReply = await authCards.GetCardsByUserAsync(new GetCardsRequest());
            Assert.That(cardsReply.Content, Is.Not.Null);

            // 5. Unauthenticated large payload over gRPC-Web (~3300+ cards)
            var publicCards = await unauthCards.GetCardsAsync(new GetCardsRequest());
            Assert.That(publicCards.Content.Cards.Count, Is.GreaterThan(1000));
        }

        // ─────────────────────────────────────────────────────────────────────────
        // 4. Keepalive & Streaming Semantics (Matchmaking.Enqueue & SubscribeGame)
        // ─────────────────────────────────────────────────────────────────────────

        [Test]
        public async Task StreamingAndKeepalive_MatchmakingEnqueueCancelAndReenqueue()
        {
            using var baseChannel = UnityHttpMessageHandler.CreateChannel(_serverAddress);
            var unauth = new Unauthenticated.UnauthenticatedClient(baseChannel);
            var user = await unauth.CreateAccountAsync(new HsCreateAccountRequest { Guest = true, Decks = true });
            var token = user.AccessTokenResponse.Token;

            // Use aggressive 1-second keepalive ping delay to exercise HTTP/2 PING frames during streaming
            using var authChannel = UnityHttpMessageHandler.CreateChannel(
                _serverAddress,
                accessTokenProvider: () => token,
                keepAlivePingDelay: TimeSpan.FromSeconds(1),
                keepAlivePingTimeout: TimeSpan.FromSeconds(5));

            var legacy = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(authChannel);
            var decks = await legacy.DecksGetAllAsync(new Empty());
            Assert.That(decks.Decks.Count, Is.GreaterThan(0));
            var deckId = decks.Decks[0].Collection.Id;

            var matchmaking = new Matchmaking.MatchmakingClient(authChannel);

            // 1. Non-existent queue closes the stream cleanly without messages
            using (var badQueueCall = matchmaking.Enqueue())
            {
                await badQueueCall.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
                {
                    QueueId = "nonexistent_queue_id",
                    DeckId = deckId
                });
                using var badCts = new CancellationTokenSource(TimeSpan.FromSeconds(10));
                Assert.That(await badQueueCall.ResponseStream.MoveNext(badCts.Token), Is.False);
            }

            // 2. Open stream on "constructed" (2-player queue), hold stream open across multiple keepalive intervals, then send Cancel = true
            using (var call = matchmaking.Enqueue())
            {
                await call.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
                {
                    QueueId = "constructed",
                    DeckId = deckId
                });

                // Hold stream open for 2.5s so both client and server keepalive mechanisms are exercised
                await Task.Delay(2500);

                // Send graceful cancel message
                await call.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
                {
                    Cancel = true
                });

                using var cts = new CancellationTokenSource(TimeSpan.FromSeconds(10));
                var hasMessage = await call.ResponseStream.MoveNext(cts.Token);
                Assert.That(hasMessage, Is.False, "Server should cleanly complete the response stream after Cancel = true");
            }

            // 3. Open stream on "constructed", enqueue ticket, and abort via CancellationTokenSource
            using (var cts = new CancellationTokenSource(TimeSpan.FromMilliseconds(800)))
            using (var call = matchmaking.Enqueue(cancellationToken: cts.Token))
            {
                await call.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
                {
                    QueueId = "constructed",
                    DeckId = deckId
                });

                var cancelEx = Assert.ThrowsAsync<RpcException>(async () =>
                    await call.ResponseStream.MoveNext(cts.Token));
                Assert.That(cancelEx.StatusCode, Is.EqualTo(StatusCode.Cancelled));
            }
        }

        [Test]
        public async Task StreamingAndKeepalive_TwoPlayersMatchmakeAndSubscribeGame()
        {
            using var baseChannel = UnityHttpMessageHandler.CreateChannel(_serverAddress);
            var unauth = new Unauthenticated.UnauthenticatedClient(baseChannel);

            var p1 = await unauth.CreateAccountAsync(new HsCreateAccountRequest { Guest = true, Decks = true });
            var p2 = await unauth.CreateAccountAsync(new HsCreateAccountRequest { Guest = true, Decks = true });

            using var p1Channel = UnityHttpMessageHandler.CreateChannel(
                _serverAddress,
                accessTokenProvider: () => p1.AccessTokenResponse.Token,
                keepAlivePingDelay: TimeSpan.FromSeconds(2),
                keepAlivePingTimeout: TimeSpan.FromSeconds(5));

            using var p2Channel = UnityHttpMessageHandler.CreateChannel(
                _serverAddress,
                accessTokenProvider: () => p2.AccessTokenResponse.Token,
                keepAlivePingDelay: TimeSpan.FromSeconds(2),
                keepAlivePingTimeout: TimeSpan.FromSeconds(5));

            var p1Legacy = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(p1Channel);
            var p2Legacy = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(p2Channel);

            var p1Decks = await p1Legacy.DecksGetAllAsync(new Empty());
            var p2Decks = await p2Legacy.DecksGetAllAsync(new Empty());
            var p1DeckId = p1Decks.Decks[0].Collection.Id;
            var p2DeckId = p2Decks.Decks[0].Collection.Id;

            var p1Matchmaking = new Matchmaking.MatchmakingClient(p1Channel);
            var p2Matchmaking = new Matchmaking.MatchmakingClient(p2Channel);

            const string sharedQueueId = "constructed";
            using var matchCts = new CancellationTokenSource(TimeSpan.FromSeconds(30));

            using var call1 = p1Matchmaking.Enqueue(cancellationToken: matchCts.Token);
            using var call2 = p2Matchmaking.Enqueue(cancellationToken: matchCts.Token);

            await call1.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
            {
                QueueId = sharedQueueId,
                DeckId = p1DeckId
            });
            await call2.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
            {
                QueueId = sharedQueueId,
                DeckId = p2DeckId
            });

            var move1Task = call1.ResponseStream.MoveNext(matchCts.Token);
            var move2Task = call2.ResponseStream.MoveNext(matchCts.Token);

            await Task.WhenAll(move1Task, move2Task);
            Assert.That(await move1Task, Is.True, "Player 1 should receive a matchmaking response");
            Assert.That(await move2Task, Is.True, "Player 2 should receive a matchmaking response");

            var gameId1 = call1.ResponseStream.Current.UnityConnection.GameId;
            var gameId2 = call2.ResponseStream.Current.UnityConnection.GameId;
            Assert.That(gameId1, Is.Not.Empty);
            Assert.That(gameId1, Is.EqualTo(gameId2), "Both players must be matched into the same gameId");

            // Verify Games.IsInMatch returns the active match
            var p1Games = new Games.GamesClient(p1Channel);
            var inProgress = await p1Games.IsInMatchAsync(new Empty());
            Assert.That(inProgress.Value, Is.EqualTo(gameId1));

            // Subscribe both players to the live game via SubscribeGame streaming RPC and send FirstMessage
            using var gameCts = new CancellationTokenSource(TimeSpan.FromSeconds(30));
            using var sub1 = p1Legacy.SubscribeGame(cancellationToken: gameCts.Token);
            using var sub2 = p2Legacy.SubscribeGame(cancellationToken: gameCts.Token);

            var firstMsg = new ClientToServerMessage
            {
                MessageType = MessageTypeMessage.Types.MessageType.FirstMessage
            };
            await sub1.RequestStream.WriteAsync(firstMsg);
            await sub2.RequestStream.WriteAsync(firstMsg);

            var sub1FirstMove = sub1.ResponseStream.MoveNext(gameCts.Token);
            var sub2FirstMove = sub2.ResponseStream.MoveNext(gameCts.Token);

            await Task.WhenAll(sub1FirstMove, sub2FirstMove);
            Assert.That(await sub1FirstMove, Is.True, "Player 1 should receive initial game state message on SubscribeGame stream");
            Assert.That(await sub2FirstMove, Is.True, "Player 2 should receive initial game state message on SubscribeGame stream");

            Assert.That(sub1.ResponseStream.Current, Is.Not.Null);
            Assert.That(sub2.ResponseStream.Current, Is.Not.Null);
            Assert.That(sub1.ResponseStream.Current.MessageType, Is.Not.EqualTo(MessageTypeMessage.Types.MessageType.OnGameEnd));
            Assert.That(sub2.ResponseStream.Current.MessageType, Is.Not.EqualTo(MessageTypeMessage.Types.MessageType.OnGameEnd));
        }

        [Test]
        public async Task ManagedHttp2Handler_UnaryErrorsAuthAndDuplexStreaming()
        {
            string currentToken = null;
            using var channel = UnityHttpMessageHandler.CreateChannel(
                _serverAddress,
                accessTokenProvider: () => currentToken,
                innerHandler: UnityHttpMessageHandler.CreateManagedHttp2Handler(
                    keepAlivePingDelay: TimeSpan.FromSeconds(1),
                    keepAlivePingTimeout: TimeSpan.FromSeconds(5)));

            var unauth = new Unauthenticated.UnauthenticatedClient(channel);
            var authCards = new AuthenticatedCards.AuthenticatedCardsClient(channel);
            var legacy = new HiddenSwitchSpellsourceAPIService.HiddenSwitchSpellsourceAPIServiceClient(channel);
            var matchmaking = new Matchmaking.MatchmakingClient(channel);

            // 1. Unauthenticated error
            var unauthEx = Assert.ThrowsAsync<RpcException>(async () =>
                await authCards.GetCardsByUserAsync(new GetCardsRequest()));
            Assert.That(unauthEx.StatusCode, Is.EqualTo(StatusCode.Unauthenticated));

            // 2. InvalidArgument error with detailed message (trailers-only HTTP/2 response)
            var invalidEx = Assert.ThrowsAsync<RpcException>(async () =>
                await unauth.CreateAccountAsync(new HsCreateAccountRequest
                {
                    Username = "ab",
                    Email = $"{Guid.NewGuid():N}@hiddenswitch.com",
                    Password = "validpassword"
                }));
            Assert.That(invalidEx.StatusCode, Is.EqualTo(StatusCode.InvalidArgument));
            Assert.That(invalidEx.Status.Detail, Does.Contain("The username must be at least 4 characters."));

            // 3. Create account & authenticated unary RPC
            var guest = await unauth.CreateAccountAsync(new HsCreateAccountRequest { Guest = true, Decks = true });
            currentToken = guest.AccessTokenResponse.Token;

            var decks = await legacy.DecksGetAllAsync(new Empty());
            Assert.That(decks.Decks.Count, Is.GreaterThan(0));
            var deckId = decks.Decks[0].Collection.Id;

            // 4. Bidirectional streaming + keepalive + graceful cancel over ManagedHttp2Handler
            using (var call = matchmaking.Enqueue())
            {
                await call.RequestStream.WriteAsync(new MatchmakingQueuePutRequest
                {
                    QueueId = "constructed",
                    DeckId = deckId
                });
                await Task.Delay(1200);
                await call.RequestStream.WriteAsync(new MatchmakingQueuePutRequest { Cancel = true });

                using var cts = new CancellationTokenSource(TimeSpan.FromSeconds(10));
                Assert.That(await call.ResponseStream.MoveNext(cts.Token), Is.False);
            }
        }
    }
}
