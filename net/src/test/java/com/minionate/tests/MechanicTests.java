package com.Spellsource.tests;

/*
public class MechanicTests extends IntegrationTestBase {
	@Test
	@Ignore
	public void testOneUpper(TestContext testContext) {
		IntegrationTestContext context = new IntegrationTestContext(testContext, vertx);

		wrapSync(testContext, () -> {
			// Start a game
			context.startGame()
					// Play from hand One Upper for player 1
					.playFromHand(GameContext.PLAYER_1, "minion_one_upper")
					// Summon a 1/1 for player 2
					.summonToBattlefield(GameContext.PLAYER_2, context.testMinion(1, 1, 1))
					// Destroy One Upper
					.destroy("minion_one_upper")
					// End the game
					.endGame()
					// Start a game
					.startGame()
					// Play from hand One Upper for player 1
					.playFromHand(GameContext.PLAYER_1, "minion_one_upper")
					// Assert that the attack has not increased (2)
					.assertThat(c -> getContext()
							.assertEquals(2, c.getMinion("minion_one_upper").getAttack()))
					// Summon a 3/3 for player 2
					.summonToBattlefield(GameContext.PLAYER_2, context.testMinion(3, 3, 3))
					// Destroy One Upper
					.destroy("minion_one_upper")
					// End the game
					.endGame()
					// Start a game
					.startGame()
					// Play from hand One Upper for player 1
					.playFromHand(GameContext.PLAYER_1, "minion_one_upper")
					// Assert that the attack has increased by 1 (3)
					.assertThat(c -> getContext()
							.assertEquals(3, c.getMinion("minion_one_upper").getAttack()))
					// Summon a 3/3 for player 2
					.summonToBattlefield(GameContext.PLAYER_2, context.testMinion(3, 3, 3))
					// Destroy One Upper
					.destroy("minion_one_upper")
					// End the game
					.endGame()
					// Start the game
					.startGame()
					// Play from hand One Upper for player 1
					.playFromHand(GameContext.PLAYER_1, "minion_one_upper")
					// Assert that the attack has not increased, but is still increased by 1 (3)
					.assertThat(c -> getContext()
							.assertEquals(3, c.getMinion("minion_one_upper").getAttack()));

		});

	}
}
*/