package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.GameLogic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

@Execution(ExecutionMode.CONCURRENT)
public class SoulseekerTests extends TestBase {

	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.DARKBLUE;
	}

	private static final List<String> SOULBIND_TOKENS = Arrays.asList(
			"token_wandering_soul",
			"token_wicked_soul",
			"token_woeful_soul",
			"token_wrathful_soul"
	);

	@Test
	public void testSoulbind() {
		for (int i = 0; i < 40; i++) {
			runGym((context, player, opponent) -> {
				playCard(context, player, "minion_soultender");
				assertTrue(SOULBIND_TOKENS.contains(player.getMinions().get(1).getSourceCard().getCardId()));
			});
		}
	}

	@Test
	public void testDeathwalk() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 9; i++) {
				receiveCard(context, player, "minion_neutral_test");
				shuffleToDeck(context, player, "minion_neutral_test");
			}
			Minion hi = playMinionCard(context, player, "minion_test_deathrattle");
			playCard(context, player, "spell_deathwalk", hi);
			assertEquals(player.getHand().get(9).getCardId(), "minion_neutral_test");
		});
	}

	// Delve into Memory - 3 Mana Spell Free - "Look at the top three cards of your deck. Discard one and draw the others."
	@Test
	public void testDelveIntoMemory() {
		for (int i = 0; i <= 3; i++) {
			final int i1 = i;
			runGym((context, player, opponent) -> {
				Collections.nCopies(i1, "minion_test_3_2")
						.forEach(cid -> context.getLogic().shuffleToDeck(player, CardCatalogue.getCardById(cid)));

				playCard(context, player, "spell_delve_into_memory");
				assertEquals(player.getDeck().size(), 0);
				if (i1 == 3) {
					assertEquals(player.getHand().size(), 2);
				}
				if (i1 > 1) {
					assertEquals(player.getHand().get(0).getCardId(), "minion_test_3_2");
				} else {
					assertEquals(player.getHand().size(), 0);
				}
			});
		}
	}

	//Essence Scatter - 3 Mana Spell Free - "Destroy a minion. Restore #8 health to its owner."
	@Test
	public void testEssenceScatter() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(player.getHero().getMaxHp() - 8);
			opponent.getHero().setHp(player.getHero().getMaxHp() - 8);
			Minion own = playMinionCard(context, player, "minion_neutral_test_1");
			Minion theirs = playMinionCard(context, opponent, "minion_neutral_test_1");
			playCard(context, player, "spell_essence_scatter", own);
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
			playCard(context, player, "spell_essence_scatter", theirs);
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp());
		});
	}

	//Otherworldy Bond - 1 Mana Secret Free - "Secret: After two friendly minions die in a turn, Soulbind twice."
	@Test
	public void testOtherworldlyBond() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_otherworldly_bond");
			Minion one = playMinionCard(context, player, "minion_neutral_test_1");
			Minion two = playMinionCard(context, player, "minion_neutral_test_1");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", one);
			assertEquals(player.getMinions().size(), 1);
			playCard(context, opponent, "spell_test_deal_6", two);
			context.getLogic().endOfSequence();
			assertEquals(player.getMinions().size(), 2);

		});
	}

	//Bend Will - 1 Mana Rare Spell - "Secret: When a spell is cast on a friendly minion, it targets a random friendly minion instead."
	@Test
	public void testBendwill() {
		AtomicBoolean survivesAtLeastOnce = new AtomicBoolean(false);
		for (int i = 0; i < 100; i++) {
			if (!survivesAtLeastOnce.get()) {
				runGym((context, player, opponent) -> {
					for (int j = 0; j < 6; j++) {
						playCard(context, player, "minion_neutral_test");
					}
					Minion you = playMinionCard(context, player, "minion_neutral_test_1");
					playCard(context, player, "spell_bend_will");
					context.endTurn();
					playCard(context, opponent, "spell_test_deal_6", you);
					if (!you.isDestroyed()) {
						survivesAtLeastOnce.set(true);
					}
				});
			}
		}
		assertTrue(survivesAtLeastOnce.get());
	}

	//In Fate's Hands - 1 Mana Epic Spell - "Secret: When a friendly minion dies, give the ones next to it Immune until your next turn."
	@Test
	@Disabled("Old Implementation")
	public void testInFatesHands() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_in_fates_hands");
			Minion left = playMinionCard(context, player, "minion_neutral_test");
			Minion center = playMinionCard(context, player, "minion_neutral_test");
			Minion right = playMinionCard(context, player, "minion_neutral_test");
			context.endTurn();
			playCard(context, opponent, "spell_test_deal_6", center);
			context.getLogic().endOfSequence();
			assertTrue(left.hasAttribute(Attribute.IMMUNE));
			assertTrue(right.hasAttribute(Attribute.IMMUNE));
			for (int i = 0; i < 10; i++) {
				playCard(context, opponent, "spell_2_missiles");
			} //what? too much?
			assertFalse(left.isDestroyed());
			assertFalse(right.isDestroyed());
		});
	}

	@Test
	public void testFatherKiliar() {
		//Ritual of Proliferation
		for (int i = 0; i < 5; i++) {
			int finalI = i;
			runGym((context, player, opponent) -> {
				overrideDiscover(context, player, "spell_ritual_of_proliferation");
				for (int j = 0; j < finalI; j++) {
					playCard(context, player, "minion_neutral_test");
				}
				Minion daddy = playMinionCard(context, player, "minion_father_kiliar");
				assertEquals(player.getMinions().size(), 1);
				assertEquals(player.getHand().size(), 1);
				Card ritual = player.getHand().get(0);
				playCard(context, player, ritual, daddy);
				assertEquals(player.getMinions().size(), 1 + finalI);
			});
		}

		//Ritual of Annihilation
		for (int i = 0; i < 5; i++) {
			int finalI = i;
			runGym((context, player, opponent) -> {
				overrideDiscover(context, player, "spell_ritual_of_annihilation");
				for (int j = 0; j < finalI; j++) {
					playCard(context, player, "minion_neutral_test");
				}
				playCard(context, player, "minion_father_kiliar");
				assertEquals(player.getMinions().size(), 1);
				assertEquals(player.getHand().size(), 1);
				Card ritual = player.getHand().get(0);
				for (int j = 0; j < 7; j++) {
					playCard(context, opponent, "minion_neutral_test");
				}
				playCard(context, player, ritual, opponent.getMinions().get(0));
				assertEquals(opponent.getMinions().size(), 7 - (1 + finalI));
			});
		}

		//Ritual of Exultation
		for (int i = 0; i < 5; i++) {
			int finalI = i;
			runGym((context, player, opponent) -> {
				overrideDiscover(context, player, "spell_ritual_of_exultation");
				for (int j = 0; j < finalI; j++) {
					playCard(context, player, "minion_neutral_test");
				}
				playCard(context, player, "minion_father_kiliar");
				assertEquals(player.getMinions().size(), 1);
				assertEquals(player.getHand().size(), 1);
				Card ritual = player.getHand().get(0);
				for (int j = 0; j < 10; j++) {
					shuffleToDeck(context, player, "minion_neutral_test");
				}
				playCard(context, player, ritual);
				assertEquals(player.getHand().size(), finalI);
			});
		}
	}

	//Haunting Vision - 2 mana 3/2 Minion - "Opener: If you've played another Haunting Vision this game, deal 3 Damage."
	@Test
	public void testHauntingVision() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_haunting_vision", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp());
			playMinionCard(context, player, "minion_haunting_vision", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 3);
			playMinionCard(context, player, "minion_haunting_vision", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), opponent.getHero().getMaxHp() - 6);
		});
	}

	//Dreamstep - 1 Mana Spell - "Return a friendly minion to your hand. If it's a Spirit, it costs (3) less."
	@Test
	public void testDreamstep() {
		runGym((context, player, opponent) -> {
			Minion not = playMinionCard(context, player, "minion_neutral_test");
			Minion spirit = playMinionCard(context, player, "token_spirit");
			playCard(context, player, "spell_dreamstep", not);
			playCard(context, player, "spell_dreamstep", spirit);
			assertEquals(costOf(context, player, player.getHand().get(0)), 2);
			assertEquals(costOf(context, player, player.getHand().get(1)), 0);
		});
	}

	//Olivia the Successor - 4 Mana 3/2 Minion - "Opener: Your Spirits cost (2) less this turn."
	@Test
	public void testOliviaTheSuccessor() {
		runGym((context, player, opponent) -> {
			Card not = receiveCard(context, player, "minion_neutral_test");
			Card spirit = receiveCard(context, player, "token_spirit");
			assertEquals(costOf(context, player, not), 2);
			assertEquals(costOf(context, player, spirit), 1);
			playCard(context, player, "minion_olivia_the_successor");
			assertEquals(costOf(context, player, not), 2);
			assertEquals(costOf(context, player, spirit), 0);
			context.endTurn();
			context.endTurn();
			assertEquals(costOf(context, player, not), 2);
			assertEquals(costOf(context, player, spirit), 1);
		});
	}

	//Osiris - 6 Mana 4/4 Legendary Minion - "Opener: Return your other minions to your hand. Summon a 4/4 Awoken for each."
	@Test
	public void testOsiris() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 6; i++) {
				playCard(context, player, "token_spirit");
			}
			playCard(context, player, "minion_osiris");
			assertEquals(player.getMinions().size(), 7);
		});
	}

	//Dormant Spirits - 1 Mana Spell - "Secret: If your opponent has unspent mana when your turn starts, summon three 1/1 Spirits."
	@Test
	public void testDormantSpirits() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_dormant_spirits");
			assertEquals(player.getMinions().size(), 0);
			context.endTurn();
			assertEquals(player.getMinions().size(), 0);
			context.endTurn();
			assertEquals(player.getMinions().size(), 3);
			assertFalse(player.getMinions().get(0).canAttackThisTurn(context));
		});
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_dormant_spirits");
			assertEquals(player.getMinions().size(), 0);
			context.endTurn();
			playCard(context, opponent, "minion_neutral_test_1");
			assertEquals(player.getMinions().size(), 0);
			context.endTurn();
			assertEquals(player.getMinions().size(), 0);
		});
	}

	@Test
	public void testAncestralEffigy() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_ancestral_effigy");
			Minion target = playMinionCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			context.endTurn();
			Minion charger = playMinionCard(context, opponent, "minion_charge_test");
			// Make sure we destroy the target. We could make it attack zero but that has all sorts of weird arcane rules.
			charger.setAttack(999);
			attack(context, opponent, charger, target);
			assertFalse(target.isDestroyed());
			assertTrue(player.getGraveyard().stream().anyMatch(e -> SOULBIND_TOKENS.contains(e.getSourceCard().getCardId())));
			assertEquals(player.getSecrets().size(), 0);
		});
	}

	@Test
	public void testStreamOfConsciousness() {
		// Also verifies that soulbinding occurs with replacement
		runGym((context, player, opponent) -> {
			GameLogic spyLogic = spy(context.getLogic());
			doAnswer(invocation -> {
				List<? extends Entity> options = invocation.getArgument(0);
				assertEquals(options.size(), SOULBIND_TOKENS.size());
				return invocation.callRealMethod();
			}).when(spyLogic).getRandom(anyList());
			context.setLogic(spyLogic);
			playCard(context, player, "spell_stream_of_consciousness");
			assertEquals(player.getMinions().size(), 5);
		});
	}

	@Test
	public void testProphetElenthris() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_prophet_elenthris");
			assertEquals(player.getHand().size(), 5);
			for (int i = 0; i < 4; i++) {
				playCard(context, player, "token_spirit");
			}
			for (int i = 0; i < 5; i++) {
				playCard(context, player, player.getHand().get(0), player.getMinions().get(0));
			}
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getSourceCard().getCardId(), "token_magoria");
		});
	}
}
