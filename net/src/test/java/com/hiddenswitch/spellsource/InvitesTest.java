package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
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

import static co.paralleluniverse.strands.Strand.sleep;
import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static org.junit.Assert.*;

public class InvitesTest extends SpellsourceTestBase {

	@Test(timeout = 18000L)
	@Suspendable
	public void testFriendInvite(TestContext testContext) throws InterruptedException, SuspendExecution {
		Strand.sleep(1000L);
		sync(() -> {
			// Regular friending.
			CountDownLatch friended = new CountDownLatch(1);
			CountDownLatch didReceiveFriend = new CountDownLatch(1);
			AtomicInteger inviteChecks = new AtomicInteger(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			try (
					UnityClient sender = new UnityClient(testContext) {
						@Override
						@Suspendable
						protected void handleMessage(Envelope message) {
							if (message.getAdded() != null && message.getAdded().getFriend() != null) {
								testContext.assertNotNull(message.getAdded().getFriend().getFriendId());
								testContext.assertEquals(message.getAdded().getFriend().getFriendId(), recipientId.get());
								didReceiveFriend.countDown();
							}

							if (message.getAdded() != null && message.getAdded().getInvite() != null) {
								Invite invite = message.getAdded().getInvite();
								if (invite.getStatus() == Invite.StatusEnum.ACCEPTED) {
									inviteChecks.decrementAndGet();
									inviteChecks.decrementAndGet();
								} else {
									testContext.assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(invite.getStatus()));
									inviteChecks.decrementAndGet();
								}
							}

							if (message.getChanged() != null && message.getChanged().getInvite() != null) {
								Invite invite = message.getChanged().getInvite();
								if (inviteChecks.get() == 1) {
									testContext.assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
									inviteChecks.decrementAndGet();
								}
							}
						}
					}) {
				try (UnityClient recipient = new UnityClient(testContext) {
					@Override
					@Suspendable
					protected void handleMessage(Envelope message) {
						if (message.getAdded() != null && message.getAdded().getInvite() != null) {
							Invite invite = message.getAdded().getInvite();
							try {
								if (invite.getFriendId() != null) {
									testContext.assertEquals(Invite.StatusEnum.PENDING, invite.getStatus());
									this.getApi().acceptInvite(invite.getId(), new AcceptInviteRequest());
									friended.countDown();
								}
							} catch (Exception ex) {
								testContext.fail(ex.getMessage());
							}
						}

						if (message.getChanged() != null && message.getChanged().getInvite() != null) {
							Invite invite = message.getChanged().getInvite();
							testContext.assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
						}
					}
				}) {
					sender.createUserAccount();
					recipient.createUserAccount();

					recipient.ensureConnected();
					sender.ensureConnected();

					recipientId.set(recipient.getUserId().toString());
					testContext.assertTrue(recipient.getAccount().getName().contains("#"));

					InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.friend(true)
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					testContext.assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
					friended.await();
					didReceiveFriend.await();
					sleep(1000L);
					testContext.assertEquals(0, inviteChecks.get());

					GetAccountsResponse updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
					GetAccountsResponse updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
					testContext.assertEquals(1, updatedRecipient.getAccounts().get(0).getFriends().size());
					testContext.assertEquals(1, updatedSender.getAccounts().get(0).getFriends().size());
					testContext.assertEquals(sender.getUserId().toString(), updatedRecipient.getAccounts().get(0).getFriends().get(0).getFriendId());
					testContext.assertEquals(recipient.getUserId().toString(), updatedSender.getAccounts().get(0).getFriends().get(0).getFriendId());
				}
			}
		}, 18, testContext);
	}

	@Test(timeout = 18000L)
	@Suspendable
	public void testRecipientRejectsFriend(TestContext testContext) throws InterruptedException, SuspendExecution {
		Strand.sleep(1000L);
		sync(() -> {
			CountDownLatch rejected = new CountDownLatch(1);
			AtomicInteger inviteChecks = new AtomicInteger(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			try (UnityClient sender = new UnityClient(testContext) {
				@Override
				@Suspendable
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getFriend() != null) {
						testContext.fail("Should not have friended");
					}

					if (message.getAdded() != null && message.getAdded().getInvite() != null) {
						Invite invite = message.getAdded().getInvite();
						// Confirm the invite statuses are updated correctly
						testContext.assertTrue(
								Sets.newHashSet(Invite.StatusEnum.UNDELIVERED,
										Invite.StatusEnum.PENDING)
										.contains(invite.getStatus()));
						inviteChecks.decrementAndGet();
					}

					if (message.getChanged() != null && message.getChanged().getInvite() != null) {
						Invite invite = message.getChanged().getInvite();
						testContext.assertEquals(Invite.StatusEnum.REJECTED, invite.getStatus());
						inviteChecks.decrementAndGet();
					}
				}
			}) {
				try (UnityClient recipient = new UnityClient(testContext) {
					@Override
					protected void handleMessage(Envelope message) {
						if (message.getAdded() != null && message.getAdded().getInvite() != null) {
							Invite invite = message.getAdded().getInvite();
							try {
								if (invite.getFriendId() != null) {
									testContext.assertEquals(Invite.StatusEnum.PENDING, invite.getStatus());
									this.getApi().deleteInvite(invite.getId());
									rejected.countDown();
								}
							} catch (Exception ex) {
								testContext.fail(ex.getMessage());
							}
						}
					}
				}) {
					sender.createUserAccount();
					recipient.createUserAccount();

					sender.ensureConnected();
					recipient.ensureConnected();

					recipientId.set(recipient.getUserId().toString());
					testContext.assertTrue(recipient.getAccount().getName().contains("#"));

					InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.friend(true)
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					testContext.assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
					rejected.await();
					sleep(1000L);
					testContext.assertEquals(0, inviteChecks.get());

					GetAccountsResponse updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
					GetAccountsResponse updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
					testContext.assertEquals(0, updatedRecipient.getAccounts().get(0).getFriends().size());
					testContext.assertEquals(0, updatedSender.getAccounts().get(0).getFriends().size());
				}
			}
		}, 18, testContext);
	}

	@Test(timeout = 18000L)
	@Suspendable
	public void testSenderCancelsFriend(TestContext testContext) throws InterruptedException, SuspendExecution {
		Strand.sleep(1000L);
		sync(() -> {
			CountDownLatch receivedInvite = new CountDownLatch(1);
			CountDownLatch inviteChecks = new CountDownLatch(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			try (UnityClient sender = new UnityClient(testContext) {
				@Override
				@Suspendable
				protected void handleMessage(Envelope message) {
					if (message.getAdded() != null && message.getAdded().getFriend() != null) {
						testContext.fail("Should not have friended");
					}
				}
			}) {
				try (UnityClient recipient = new UnityClient(testContext) {
					@Override
					@Suspendable
					protected void handleMessage(Envelope message) {
						if (message.getChanged() != null && message.getChanged().getInvite() != null) {
							Invite invite = message.getChanged().getInvite();
							testContext.assertEquals(Invite.StatusEnum.CANCELLED, invite.getStatus());
							try {
								// Accepting the invite should fail
								this.getApi().acceptInvite(invite.getId(), new AcceptInviteRequest());
								testContext.fail("Should not be able to accept cancelled invite");
							} catch (ApiException didFail) {
								testContext.assertEquals(didFail.getCode(), 418);
								inviteChecks.countDown();
							}
						}
						if (message.getAdded() != null && message.getAdded().getInvite() != null) {
							Invite invite = message.getAdded().getInvite();
							if (invite.getFriendId() != null) {
								if (invite.getStatus() == Invite.StatusEnum.PENDING) {
									inviteChecks.countDown();
									receivedInvite.countDown();
								}
							}
						}
					}
				}) {
					sender.createUserAccount();
					recipient.createUserAccount();

					sender.ensureConnected();
					recipient.ensureConnected();

					recipientId.set(recipient.getUserId().toString());
					testContext.assertTrue(recipient.getAccount().getName().contains("#"));

					InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.friend(true)
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					testContext.assertTrue(Sets.newHashSet(Invite.StatusEnum.UNDELIVERED, Invite.StatusEnum.PENDING).contains(inviteResponse.getInvite().getStatus()));
					receivedInvite.await();
					InviteResponse cancelResponse = invoke(sender.getApi()::deleteInvite, inviteResponse.getInvite().getId());
					testContext.assertEquals(Invite.StatusEnum.CANCELLED, cancelResponse.getInvite().getStatus());
					inviteChecks.await();
					GetAccountsResponse updatedRecipient = invoke(recipient.getApi()::getAccount, recipient.getUserId().toString());
					GetAccountsResponse updatedSender = invoke(sender.getApi()::getAccount, sender.getUserId().toString());
					testContext.assertEquals(0, updatedRecipient.getAccounts().get(0).getFriends().size());
					testContext.assertEquals(0, updatedSender.getAccounts().get(0).getFriends().size());
				}
			}
		}, 18, testContext);
	}

	@Test(timeout = 18000L)
	public void testPrivateGameInvite(TestContext testContext) throws InterruptedException, SuspendExecution {
		Strand.sleep(1000L);
		sync(() -> {
			CountDownLatch receivedInvite = new CountDownLatch(1);
			try (UnityClient sender = new UnityClient(testContext) {
				@Override
				protected int getActionIndex(ServerToClientMessage message) {
					if (message.getActions().getEndTurn() != null) {
						return message.getActions().getEndTurn();
					} else {
						return super.getActionIndex(message);
					}
				}

				@Override
				protected void handleMessage(Envelope env) {
					handleGameMessages(env);
				}
			}) {
				try (UnityClient recipient = new UnityClient(testContext) {
					@Override
					protected int getActionIndex(ServerToClientMessage message) {
						if (message.getActions().getEndTurn() != null) {
							return message.getActions().getEndTurn();
						} else {
							return super.getActionIndex(message);
						}
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
					sender.createUserAccount();
					recipient.createUserAccount();

					sender.ensureConnected();
					recipient.ensureConnected();

					// Friend them coercively
					Friends.putFriend(Accounts.get(sender.getUserId().toString()), new FriendPutRequest().friendId(recipient.getUserId().toString()).usernameWithToken(recipient.getAccount().getName()));

					// Send a 1v1 invite
					InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
							.queueId("customQueueId")
							// Start the queue automatically
							.deckId(sender.getAccount().getDecks().get(0).getId())
							// Get name contains the privacy token
							.toUserNameWithToken(recipient.getAccount().getName())
							.message("Would you be my friend?"));

					testContext.assertEquals(inviteResponse.getInvite().getQueueId(), Matchmaking.getUsersInQueues().get(sender.getUserId()), "The sender was queued automatically because the sender specified a deckId");
					receivedInvite.await();

					// Accept the invite with a deck ID, which should enqueue automatically
					AcceptInviteResponse acceptInviteResponse = invoke(recipient.getApi()::acceptInvite, inviteResponse.getInvite().getId(), new AcceptInviteRequest()
							.match(new MatchmakingQueuePutRequest()
									.queueId(inviteResponse.getInvite().getQueueId())
									.deckId(recipient.getAccount().getDecks().get(0).getId())));

					sleep(2000L);
					testContext.assertTrue(Games.getUsersInGames().containsKey(recipient.getUserId()), "Both players should be in a game now");
					testContext.assertTrue(Games.getUsersInGames().containsKey(sender.getUserId()), "Both players should be in a game now");

					sender.play();
					recipient.play();

					// The players should be in a game, and play it.
					recipient.waitUntilDone();
					sender.waitUntilDone();
					testContext.assertTrue(recipient.getTurnsPlayed() > 0);
					testContext.assertTrue(sender.getTurnsPlayed() > 0);

					// Check that the queue has been destroyed.
					testContext.assertFalse(SuspendableQueue.exists(inviteResponse.getInvite().getQueueId()), "The queue should be destroyed");
					testContext.assertFalse(Games.getUsersInGames().containsKey(recipient.getUserId()), "Neither players should be in a game now");
					testContext.assertFalse(Games.getUsersInGames().containsKey(sender.getUserId()), "Neither players should be in a game now");
				}
			}
		}, 18, testContext);
	}
}
