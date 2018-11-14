package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.concurrent.CountDownLatch;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.concurrent.SuspendableQueue;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.fromage.quasi.strands.Strand.sleep;
import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static org.junit.Assert.*;

public class InvitesTest extends SpellsourceTestBase {

	@Test
	@Suspendable
	public void testFriendInvite(TestContext testContext) {
		sync(() -> {
			// Regular friending.
			CountDownLatch friended = new CountDownLatch(1);
			CountDownLatch didReceiveFriend = new CountDownLatch(1);
			AtomicInteger inviteChecks = new AtomicInteger(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			UnityClient sender = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getFriend() != null) {
						assertNotNull(message.getAdded().getFriend().getFriendId());
						assertEquals(message.getAdded().getFriend().getFriendId(), recipientId.get());
						didReceiveFriend.countDown();
					}

					if (message.getAdded() != null && message.getAdded().getInvite() != null) {
						Invite invite = message.getAdded().getInvite();
						assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(invite.getStatus()));
						inviteChecks.decrementAndGet();
					}

					if (message.getChanged() != null && message.getChanged().getInvite() != null) {
						Invite invite = message.getChanged().getInvite();
						assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
						inviteChecks.decrementAndGet();
					}
				}
			};

			UnityClient recipient = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getInvite() != null) {
						Invite invite = message.getAdded().getInvite();
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
						Invite invite = message.getChanged().getInvite();
						assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
					}
				}
			};

			sender.createUserAccount();
			recipient.createUserAccount();

			recipient.ensureConnected();
			sender.ensureConnected();

			recipientId.set(recipient.getUserId().toString());
			assertTrue(recipient.getAccount().getName().contains("#"));

			InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
					.friend(true)
					// Get name contains the privacy token
					.toUserNameWithToken(recipient.getAccount().getName())
					.message("Would you be my friend?"));

			assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
			friended.await();
			didReceiveFriend.await();
			sleep(1000L);
			assertEquals(0, inviteChecks.get());

			GetAccountsResponse updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
			GetAccountsResponse updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
			assertEquals(1, updatedRecipient.getAccounts().get(0).getFriends().size());
			assertEquals(1, updatedSender.getAccounts().get(0).getFriends().size());
			assertEquals(sender.getUserId().toString(), updatedRecipient.getAccounts().get(0).getFriends().get(0).getFriendId());
			assertEquals(recipient.getUserId().toString(), updatedSender.getAccounts().get(0).getFriends().get(0).getFriendId());
		});
	}

	@Test
	@Suspendable
	public void testRecipientRejectsFriend(TestContext testContext) {
		sync(() -> {
			CountDownLatch rejected = new CountDownLatch(1);
			AtomicInteger inviteChecks = new AtomicInteger(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			UnityClient sender = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getFriend() != null) {
						fail("Should not have friended");
					}

					if (message.getAdded() != null && message.getAdded().getInvite() != null) {
						Invite invite = message.getAdded().getInvite();
						// Confirm the invite statuses are updated correctly
						assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(invite.getStatus()));
						inviteChecks.decrementAndGet();
					}

					if (message.getChanged() != null && message.getChanged().getInvite() != null) {
						Invite invite = message.getChanged().getInvite();
						assertEquals(Invite.StatusEnum.REJECTED, invite.getStatus());
						inviteChecks.decrementAndGet();
					}
				}
			};

			UnityClient recipient = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getInvite() != null) {
						Invite invite = message.getAdded().getInvite();
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
				}
			};

			sender.createUserAccount();
			recipient.createUserAccount();

			sender.ensureConnected();
			recipient.ensureConnected();

			recipientId.set(recipient.getUserId().toString());
			assertTrue(recipient.getAccount().getName().contains("#"));

			InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
					.friend(true)
					// Get name contains the privacy token
					.toUserNameWithToken(recipient.getAccount().getName())
					.message("Would you be my friend?"));

			assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
			rejected.await();
			sleep(1000L);
			assertEquals(0, inviteChecks.get());

			GetAccountsResponse updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
			GetAccountsResponse updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
			assertEquals(0, updatedRecipient.getAccounts().get(0).getFriends().size());
			assertEquals(0, updatedSender.getAccounts().get(0).getFriends().size());
		});
	}

	@Test
	@Suspendable
	public void testSenderCancelsFriend(TestContext testContext) {
		sync(() -> {
			CountDownLatch receivedInvite = new CountDownLatch(1);
			CountDownLatch inviteChecks = new CountDownLatch(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			UnityClient sender = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getFriend() != null) {
						fail("Should not have friended");
					}
				}
			};

			UnityClient recipient = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getInvite() != null) {
						Invite invite = message.getAdded().getInvite();
						if (invite.getFriendId() != null) {
							if (invite.getStatus() == Invite.StatusEnum.PENDING) {
								inviteChecks.countDown();
								receivedInvite.countDown();
							} else if (invite.getStatus() == Invite.StatusEnum.CANCELLED) {
								// Should not be able to cancel
								try {
									// Accepting the invite should fail
									this.getApi().acceptInvite(invite.getId(), new AcceptInviteRequest());
									fail("Should not be able to accept cancelled invite");
								} catch (ApiException didFail) {
									assertEquals(didFail.getCode(), 418);
									inviteChecks.countDown();
								}
							}
						}
					}
				}
			};


			sender.createUserAccount();
			recipient.createUserAccount();

			sender.ensureConnected();
			recipient.ensureConnected();

			recipientId.set(recipient.getUserId().toString());
			assertTrue(recipient.getAccount().getName().contains("#"));

			InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
					.friend(true)
					// Get name contains the privacy token
					.toUserNameWithToken(recipient.getAccount().getName())
					.message("Would you be my friend?"));

			assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
			receivedInvite.await();
			InviteResponse cancelResponse = invoke(sender.getApi()::deleteInvite, inviteResponse.getInvite().getId());
			assertEquals(Invite.StatusEnum.CANCELLED, cancelResponse.getInvite().getStatus());
			inviteChecks.await();
			GetAccountsResponse updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
			GetAccountsResponse updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
			assertEquals(0, updatedRecipient.getAccounts().get(0).getFriends().size());
			assertEquals(0, updatedSender.getAccounts().get(0).getFriends().size());
		});
	}

	@Test
	public void testPrivateGameInvite(TestContext context) {
		sync(() -> {
			CountDownLatch receivedInvite = new CountDownLatch(1);
			// Create the users
			UnityClient sender = new UnityClient(context) {
				@Override
				protected void handleMessage(Envelope env) {
					handleGameMessages(env);
				}
			};
			UnityClient recipient = new UnityClient(context) {
				@Override
				protected void handleMessage(Envelope env) {
					if (env.getAdded() != null && env.getAdded().getInvite() != null) {
						receivedInvite.countDown();
					}

					handleGameMessages(env);
				}
			};

			sender.createUserAccount();
			recipient.createUserAccount();

			sender.ensureConnected();
			recipient.ensureConnected();

			// Friend them coercively
			Friends.putFriend(Accounts.get(sender.getUserId()), new FriendPutRequest().friendId(recipient.getUserId().toString()).usernameWithToken(recipient.getAccount().getName()));

			// Send a 1v1 invite
			InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
					.queueId("customQueueId")
					// Start the queue automatically
					.deckId(sender.getAccount().getDecks().get(0).getId())
					// Get name contains the privacy token
					.toUserNameWithToken(recipient.getAccount().getName())
					.message("Would you be my friend?"));

			assertEquals("The sender was queued automatically because the sender specified a deckId", inviteResponse.getInvite().getQueueId(), Matchmaking.getUsersInQueues().get(sender.getUserId()));
			receivedInvite.await();

			// Accept the invite with a deck ID, which should enqueue automatically
			AcceptInviteResponse acceptInviteResponse = invoke(recipient.getApi()::acceptInvite, inviteResponse.getInvite().getId(), new AcceptInviteRequest()
					.match(new MatchmakingQueuePutRequest()
							.queueId(inviteResponse.getInvite().getQueueId())
							.deckId(recipient.getAccount().getDecks().get(0).getId())));

			sleep(2000L);
			assertTrue("Both players should be in a game now", Games.getUsersInGames().containsKey(recipient.getUserId()));
			assertTrue("Both players should be in a game now", Games.getUsersInGames().containsKey(sender.getUserId()));

			sender.play();
			recipient.play();

			// The players should be in a game, and play it.
			recipient.waitUntilDone();
			sender.waitUntilDone();

			// Check that the queue has been destroyed.
			assertFalse("The queue should be destroyed", SuspendableQueue.exists(inviteResponse.getInvite().getQueueId()));
			assertFalse("Neither players should be in a game now", Games.getUsersInGames().containsKey(recipient.getUserId()));
			assertFalse("Neither players should be in a game now", Games.getUsersInGames().containsKey(sender.getUserId()));
		});
	}
}
