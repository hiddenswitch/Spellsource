package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.InviteId;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static co.paralleluniverse.strands.Strand.sleep;
import static io.vertx.ext.sync.Sync.invoke;
import static io.vertx.ext.sync.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.*;

public class InvitesTest extends SpellsourceTestBase {

	@Test
	@Timeout(36000)
	public void testFriendInvite(Vertx vertx, VertxTestContext testContext) throws InterruptedException, SuspendExecution {
		runOnFiberContext(() -> {
			// Regular friending.
			var friended = new CountDownLatch(1);
			var didReceiveFriend = new CountDownLatch(1);
			var inviteChecks = new AtomicInteger(2);
			var recipientId = new AtomicReference<String>();
			try (
					var sender = new UnityClient(testContext) {
						@Override
						@Suspendable
						protected void handleMessage(Envelope message) {
							verify(testContext, () -> {
								if (message.getAdded() != null && message.getAdded().getFriend() != null) {
									assertNotNull(message.getAdded().getFriend().getFriendId());
									assertEquals(message.getAdded().getFriend().getFriendId(), recipientId.get());
									didReceiveFriend.countDown();
								}

								if (message.getAdded() != null && message.getAdded().getInvite() != null) {
									var invite = message.getAdded().getInvite();
									if (invite.getStatus() == Invite.StatusEnum.ACCEPTED) {
										inviteChecks.decrementAndGet();
										inviteChecks.decrementAndGet();
									} else {
										assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(invite.getStatus()));
										inviteChecks.decrementAndGet();
									}
								}

								if (message.getChanged() != null && message.getChanged().getInvite() != null) {
									var invite = message.getChanged().getInvite();
									if (inviteChecks.get() == 1) {
										assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
										inviteChecks.decrementAndGet();
									}
								}
							});

						}
					}) {
				try (var recipient = new UnityClient(testContext) {
					@Override
					@Suspendable
					protected void handleMessage(Envelope message) {
						verify(testContext, () -> {
							if (message.getAdded() != null && message.getAdded().getInvite() != null) {
								var invite = message.getAdded().getInvite();
								try {
									if (invite.getFriendId() != null) {
										assertEquals(Invite.StatusEnum.PENDING, invite.getStatus());
										this.getApi().acceptInvite(invite.getId(), new AcceptInviteRequest());
										friended.countDown();
									}
								} catch (Exception ex) {
									fail(ex.getMessage());
								}
							}

							if (message.getChanged() != null && message.getChanged().getInvite() != null) {
								var invite = message.getChanged().getInvite();
								assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
							}
						});
					}
				}) {
					invoke0(sender::createUserAccount);
					invoke0(recipient::createUserAccount);

					invoke0(recipient::ensureConnected);
					invoke0(sender::ensureConnected);

					recipientId.set(recipient.getUserId().toString());
					assertTrue(recipient.getAccount().getName().contains("#"));

					var inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.friend(true)
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
					friended.await();
					didReceiveFriend.await();
					sleep(1000L);
					assertEquals(0, inviteChecks.get());

					var updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
					var updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
					assertEquals(1, updatedRecipient.getAccounts().get(0).getFriends().size());
					assertEquals(1, updatedSender.getAccounts().get(0).getFriends().size());
					assertEquals(sender.getUserId().toString(), updatedRecipient.getAccounts().get(0).getFriends().get(0).getFriendId());
					assertEquals(recipient.getUserId().toString(), updatedSender.getAccounts().get(0).getFriends().get(0).getFriendId());
				}
			}
		}, testContext, vertx);
	}

	@Test
	@Timeout(18000)
	public void testRecipientRejectsFriend(Vertx vertx, VertxTestContext testContext) throws InterruptedException, SuspendExecution {
		runOnFiberContext(() -> {
			var rejected = new CountDownLatch(1);
			var inviteChecks = new AtomicInteger(2);
			var recipientId = new AtomicReference<String>();
			try (var sender = new UnityClient(testContext) {
				@Override
				@Suspendable
				protected void handleMessage(Envelope message) {
					verify(testContext, () -> {
						if (message.getAdded() != null && message.getAdded().getFriend() != null) {
							fail("Should not have friended");
						}

						if (message.getAdded() != null && message.getAdded().getInvite() != null) {
							var invite = message.getAdded().getInvite();
							// Confirm the invite statuses are updated correctly
							assertTrue(
									Sets.newHashSet(Invite.StatusEnum.UNDELIVERED,
											Invite.StatusEnum.PENDING)
											.contains(invite.getStatus()));
							inviteChecks.decrementAndGet();
						}

						if (message.getChanged() != null && message.getChanged().getInvite() != null) {
							var invite = message.getChanged().getInvite();
							assertEquals(Invite.StatusEnum.REJECTED, invite.getStatus());
							inviteChecks.decrementAndGet();
						}
					});
				}
			}) {
				try (var recipient = new UnityClient(testContext) {
					@Override
					protected void handleMessage(Envelope message) {
						verify(testContext, () -> {
							if (message.getAdded() != null && message.getAdded().getInvite() != null) {
								var invite = message.getAdded().getInvite();
								try {
									if (invite.getFriendId() != null) {
										assertEquals(Invite.StatusEnum.PENDING, invite.getStatus());
										this.getApi().deleteInvite(invite.getId());
										rejected.countDown();
									}
								} catch (Exception ex) {
									fail(ex.getMessage());
								}
							}
						});
					}
				}) {
					invoke0(sender::createUserAccount);
					invoke0(recipient::createUserAccount);

					invoke0(recipient::ensureConnected);
					invoke0(sender::ensureConnected);

					recipientId.set(recipient.getUserId().toString());
					assertTrue(recipient.getAccount().getName().contains("#"));

					var inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.friend(true)
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
					rejected.await();
					sleep(1000L);
					assertEquals(0, inviteChecks.get());

					var updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
					var updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
					assertEquals(0, updatedRecipient.getAccounts().get(0).getFriends().size());
					assertEquals(0, updatedSender.getAccounts().get(0).getFriends().size());
				}
			}
		}, testContext, vertx);
	}

	@Test
	@Timeout(18000)
	public void testSenderCancelsFriend(Vertx vertx, VertxTestContext testContext) throws InterruptedException, SuspendExecution {
		runOnFiberContext(() -> {
			var receivedInvite = new CountDownLatch(1);
			var inviteChecks = new CountDownLatch(2);
			var recipientId = new AtomicReference<String>();
			try (var sender = new UnityClient(testContext) {
				@Override
				@Suspendable
				protected void handleMessage(Envelope message) {
					verify(testContext, () -> {
						if (message.getAdded() != null && message.getAdded().getFriend() != null) {
							fail("Should not have friended");
						}
					});
				}
			}) {
				try (var recipient = new UnityClient(testContext) {
					@Override
					@Suspendable
					protected void handleMessage(Envelope message) {
						verify(testContext, () -> {
							if (message.getChanged() != null && message.getChanged().getInvite() != null) {
								var invite = message.getChanged().getInvite();
								assertEquals(Invite.StatusEnum.CANCELLED, invite.getStatus());
								try {
									// Accepting the invite should fail
									this.getApi().acceptInvite(invite.getId(), new AcceptInviteRequest());
									fail("Should not be able to accept cancelled invite");
								} catch (ApiException didFail) {
									assertEquals(didFail.getCode(), 500);
									inviteChecks.countDown();
								}
							}
							if (message.getAdded() != null && message.getAdded().getInvite() != null) {
								var invite = message.getAdded().getInvite();
								if (invite.getFriendId() != null) {
									if (invite.getStatus() == Invite.StatusEnum.PENDING) {
										inviteChecks.countDown();
										receivedInvite.countDown();
									}
								}
							}
						});
					}
				}) {
					invoke0(sender::createUserAccount);
					invoke0(recipient::createUserAccount);
					invoke0(recipient::ensureConnected);
					invoke0(sender::ensureConnected);

					recipientId.set(recipient.getUserId().toString());
					assertTrue(recipient.getAccount().getName().contains("#"));

					var inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.friend(true)
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
					receivedInvite.await();
					var cancelResponse = invoke(sender.getApi()::deleteInvite, inviteResponse.getInvite().getId());
					assertEquals(Invite.StatusEnum.CANCELLED, cancelResponse.getInvite().getStatus());
					inviteChecks.await();
					var updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
					var updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
					assertEquals(0, updatedRecipient.getAccounts().get(0).getFriends().size());
					assertEquals(0, updatedSender.getAccounts().get(0).getFriends().size());
				}
			}
		}, testContext, vertx);
	}

	@Test
	@Timeout(18000)
	public void testPrivateGameInvite(Vertx vertx, VertxTestContext testContext) throws InterruptedException, SuspendExecution {
		runOnFiberContext(() -> {
			var receivedInvite = new CountDownLatch(1);
			try (var sender = new UnityClient(testContext) {
				@Override
				protected int getActionIndex(ServerToClientMessage message) {
					var endTurn = message.getActions().getAll().stream().filter(ga -> ga.getActionType().equals(ActionType.END_TURN)).findFirst();
					if (endTurn.isPresent()) {
						return endTurn.get().getAction();
					}
					return super.getActionIndex(message);
				}

				@Override
				protected void handleMessage(Envelope env) {
					handleGameMessages(env);
				}
			}) {
				try (var recipient = new UnityClient(testContext) {
					@Override
					protected int getActionIndex(ServerToClientMessage message) {
						var endTurn = message.getActions().getAll().stream().filter(ga -> ga.getActionType().equals(ActionType.END_TURN)).findFirst();
						if (endTurn.isPresent()) {
							return endTurn.get().getAction();
						}
						return super.getActionIndex(message);
					}

					@Override
					protected void handleMessage(Envelope env) {
						if (env.getAdded() != null && env.getAdded().getInvite() != null) {
							if (env.getAdded().getInvite().getStatus() == Invite.StatusEnum.PENDING) {
								receivedInvite.countDown();
							}
						}

						handleGameMessages(env);
					}
				}) {

					invoke0(sender::createUserAccount);
					invoke0(recipient::createUserAccount);

					invoke0(recipient::ensureConnected);
					invoke0(sender::ensureConnected);

					Friends.putFriend(Accounts.get(sender.getUserId().toString()), new FriendPutRequest().friendId(recipient.getUserId().toString()).usernameWithToken(recipient.getAccount().getName()));
					// Friend them coercively

					// Send a 1v1 invite
					var inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.queueId("customQueueId")
							// Start the queue automatically
							.deckId(sender.getAccount().getDecks().get(0).getId())
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					assertEquals(inviteResponse.getInvite().getQueueId(), Matchmaking.getUsersInQueues().get(sender.getUserId().toString()), "The sender was queued automatically because the sender specified a deckId");
					receivedInvite.await();

					// Accept the invite with a deck ID, which should enqueue automatically
					Invites.accept(new InviteId(inviteResponse.getInvite().getId()), new AcceptInviteRequest()
							.match(new MatchmakingQueuePutRequest()
									.queueId(inviteResponse.getInvite().getQueueId())
									.deckId(recipient.getAccount().getDecks().get(0).getId())), Accounts.get(recipient.getUserId().toString()));


					assertTrue(Games.isInGame(recipient.getUserId()), "Both players should be in a game now");
					assertTrue(Games.isInGame(sender.getUserId()), "Both players should be in a game now");
					sender.play();
					recipient.play();


					// The players should be in a game, and play it.
					invoke0(recipient::waitUntilDone);
					invoke0(sender::waitUntilDone);
					assertTrue(recipient.getTurnsPlayed() > 0);
					assertTrue(sender.getTurnsPlayed() > 0);

					// Check that the queue has been destroyed.
					assertFalse(Games.isInGame(recipient.getUserId()), "Neither players should be in a game now");
					assertFalse(Games.isInGame(sender.getUserId()), "Neither players should be in a game now");
				}
			}
		}, testContext, vertx);
	}
}
