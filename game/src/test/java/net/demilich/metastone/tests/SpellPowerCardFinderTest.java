package net.demilich.metastone.tests;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.util.Logging;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.ChooseLastBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.ReflectionComparatorFactory;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.reflectionassert.difference.ObjectDifference;

public class SpellPowerCardFinderTest extends TestBase {
	@Test
	@Ignore
	public void testShowAllCardsAffected() {
		Logging.setLoggingLevel(Level.ERROR);
		/*
		Approach 1: for detecting spell damage
		 - Build a game context with valid targets for a given spell card
		 - Try casting the spell on a valid target
		 - Observe if its damage changes due to spell damage being applied

		  Nothing
		  attribute spell amplify multiplier 2 on the hero
		  attribute spell damage + 1 on the hero
		 */
		CardCatalogue.loadCardsFromPackage();
		for (Card card : CardCatalogue.getAll()) {
			if (!card.isSpell()) {
				continue;
			}
			GameContext nothingContext = createGameContext();
			int activePlayer = nothingContext.getActivePlayerId();

			GameContext spellDamageContext = createGameContext();
			GameContext spellAmplifyContext = createGameContext();
			spellDamageContext.getActivePlayer().getHero().setAttribute(Attribute.SPELL_DAMAGE, 1);
			spellAmplifyContext.getActivePlayer().getHero().setAttribute(Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER, 2);

			for (GameContext context : new GameContext[]{nothingContext, spellAmplifyContext, spellDamageContext}) {
				context.getLogic().receiveCard(activePlayer, card.getCopy());
				Card cardInHand = context.getActivePlayer().getHand().get(0);
				SpellUtils.playCardRandomly(context, context.getActivePlayer(), cardInHand, cardInHand, true, false, false, true, true);
				context.getPlayers().stream().forEach(p -> {
					p.getAttributes().remove(Attribute.GAME_START_TIME_MILLIS);
					p.getAttributes().remove(Attribute.TURN_START_TIME_MILLIS);
				});
			}

			// All the contexts have been mutated
			for (GameContext rhs : new GameContext[]{spellAmplifyContext, spellDamageContext}) {
				GameContext lhs = nothingContext;

				rhs.getActivePlayer().getHero().getAttributes().remove(Attribute.SPELL_DAMAGE);
				rhs.getActivePlayer().getHero().getAttributes().remove(Attribute.SPELL_DAMAGE_AMPLIFY_MULTIPLIER);

				ReflectionComparator reflectionComparator = ReflectionComparatorFactory.createRefectionComparator(ReflectionComparatorMode.IGNORE_DEFAULTS);
				GameState leftGameState = lhs.getGameStateCopy();
				GameState rightGameState = rhs.getGameStateCopy();
				ObjectDifference difference = (ObjectDifference)reflectionComparator.getDifference(leftGameState, rightGameState);

				if (difference != null && difference.getFieldDifferences().size() > 1) {
					System.err.println(card.getCardId()/*String.format("Difference found for %s:\n%s\n",)*/);
				}
			}

		}
	}

	private static GameContext createGameContext() {
		GameContext context = GameContext.uninitialized(HeroClass.BLACK, HeroClass.BLACK);
		context.setLogic(new GameLogic(1010101L));
		context.setBehaviour(0, new ChooseLastBehaviour());
		context.setBehaviour(1, new ChooseLastBehaviour());

		context.init();
		// Put targets on the board
		for (int i = 0; i < 2; i++) {
			for (int playerId = 0; playerId < 2; playerId++) {
				Card bloodfenCard = CardCatalogue.getCardById("minion_bloodfen_raptor");
				Minion bloodfen = bloodfenCard.summon();
				context.getLogic().summon(playerId, bloodfen, bloodfenCard, -1, false);
				bloodfen.setMaxHp(999);
				bloodfen.setHp(999);
			}
		}

		return context;
	}
}
