package net.demilich.metastone.tests;


import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TheOldGodsTests extends TestBase {

	@Test
	public void testCallInTheFinishers() {
		runGym((context, player, opponent) -> {
			playCard(context, player, CardCatalogue.getCardById("spell_call_in_the_finishers"));

			for (Minion minion : player.getMinions()) {
				Assert.assertEquals(minion.getSourceCard().getCardId(), "token_murloc_razorgill");
			}
		});
	}

	@Test
	public void testDarkshireCoucilman() {
		runGym((context, player, opponent) -> {
			Minion darkshireCouncilman = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_darkshire_councilman"));
			Assert.assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack());

			Minion darkshireCouncilman2 = playMinionCard(context, player, (MinionCard) CardCatalogue.getCardById("minion_darkshire_councilman"));
			Assert.assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack() + 1);
			Assert.assertEquals(darkshireCouncilman2.getAttack(), darkshireCouncilman2.getBaseAttack());

			context.getLogic().endTurn(player.getId());
			Minion opponentMinion = playMinionCard(context, opponent, (MinionCard) CardCatalogue.getCardById("minion_darkshire_councilman"));

			Assert.assertEquals(darkshireCouncilman.getAttack(), darkshireCouncilman.getBaseAttack() + 1);
			Assert.assertEquals(darkshireCouncilman2.getAttack(), darkshireCouncilman2.getBaseAttack());
			Assert.assertEquals(opponentMinion.getAttack(), opponentMinion.getBaseAttack());
		});
	}

	@Test
	public void testALightInTheDarkness() {
		GameContext context = createContext(HeroClass.SHAMAN, HeroClass.WARRIOR);
		Player player = context.getActivePlayer();
		final DiscoverAction[] action = {null};
		final Minion[] originalMinion = new Minion[1];
		final int[] handSize = new int[1];
		player.setBehaviour(new TestBehaviour() {
			boolean first = true;

			@Override
			public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
				if (first) {
					Assert.assertTrue(validActions.stream().allMatch(ga -> ga.getActionType() == ActionType.DISCOVER));
					action[0] = (DiscoverAction) validActions.get(0);
					MinionCard original = (MinionCard) action[0].getCard();
					originalMinion[0] = original.summon();
					handSize[0] = player.getHand().size();
				}
				first = false;
				return super.requestAction(context, player, validActions);
			}
		});
		SpellCard light = (SpellCard) CardCatalogue.getCardById("spell_a_light_in_the_darkness");
		playCard(context, player, light);
		Assert.assertEquals(player.getHand().size(), handSize[0] + 1);
		Card cardInHand = player.getHand().get(player.getHand().size() - 1);
		Assert.assertEquals(cardInHand.getCardId(), originalMinion[0].getSourceCard().getCardId());
		context.getLogic().performGameAction(player.getId(), cardInHand.play());
		int buff = light.getSpell().subSpells().filter(sd -> sd.getSpellClass().equals(BuffSpell.class)).findFirst().orElseThrow(AssertionError::new).getInt(SpellArg.VALUE, -999);
		Assert.assertEquals(player.getMinions().get(0).getAttack(), originalMinion[0].getAttack() + buff);
		Assert.assertEquals(player.getMinions().get(0).getHp(), originalMinion[0].getHp() + buff);
	}
}

