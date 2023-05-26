package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class VampireLordTests extends TestBase {
	@NotNull
	@Override
	public String getDefaultHeroClass() {
		return HeroClass.TWILIGHT;
	}

	@Test
	public void testGatekeeperSha() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_gatekeeper_sha_rework");
			// "Take [3] damage. Summon [1] [3]/[2] Shadow."
			playCard(context, player, "spell_hemoshade");
			assertEquals(3, player.getMinions().size(), "Summoned 2 shadows + Gatekeeper Sha");
			for (var minion : new Minion[]{player.getMinions().get(1), player.getMinions().get(2)}) {
				assertEquals(4, minion.getAttack(), "Buffed attack by Sha");
				assertEquals(3, minion.getHp(), "Buffed hp by Sha");
			}
		});

		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_gatekeeper_sha_rework");
			context.endTurn();
			playCard(context, opponent, "spell_hemoshade");
			assertEquals(1, opponent.getMinions().size(), "Summoned 1 shadow not affected by player's Sha");
			for (var minion : new Minion[]{opponent.getMinions().get(0)}) {
				assertEquals(3, minion.getAttack(), "No effect");
				assertEquals(2, minion.getHp(), "No effect");
			}
		});
	}

	@Test
	public void testSiphon() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(28);
			Minion target = playMinionCard(context, player, 1, 1);
			playCard(context, player, "spell_siphon", target);
			assertEquals(30, player.getHero().getHp(), "less health than opponent + 2 lifedrain");
		});

		runGym((context, player, opponent) -> {
			opponent.getHero().setHp(28);
			player.getHero().setHp(28);
			Minion target = playMinionCard(context, player, 1, 1);
			playCard(context, player, "spell_siphon", target);
			assertEquals(28, player.getHero().getHp(), "not less health than opponent, no lifedrain");
		});

		runGym((context, player, opponent) -> {
			opponent.getHero().setHp(29);
			playCard(context, player, "spell_siphon", player.getHero());
			assertEquals(28, player.getHero().getHp(), "not less health than opponent, evaluated at time of playing the card, no lifedrain");
		});

		runGym((context, player, opponent) -> {
			player.getHero().setHp(28);
			playCard(context, player, "spell_siphon", opponent.getHero());
			assertEquals(30, player.getHero().getHp(), "at the start of the card, less health than opponent, but after damage, not anymore, should lifedrain");
		});
	}

	@Test
	public void testGatekeeperAcolyte() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_gatekeeper_acolyte");
			player.getHero().setHp(28);
			playCard(context, player, "spell_test_heal_1", player.getHero());
			assertEquals(30, player.getHero().getHp(), "should buff healing by 1");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_gatekeeper_acolyte");
			opponent.getHero().setHp(28);
			context.endTurn();
			playCard(context, opponent, "spell_test_heal_1", opponent.getHero());
			assertEquals(30, opponent.getHero().getHp(), "should buff opponent healing by 1");
		});
	}

	@Test
	public void testFleshshaper() {
		runGym((context, player, opponent) -> {
			player.getHero().setHp(28);
			Minion target = playMinionCard(context, player, 1, 1);
			playCard(context, player, "minion_fleshshaper");
			playCard(context, player, "spell_siphon", target);
			assertEquals(28, player.getHero().getHp(), "less health than opponent + 2 lifedrain, BUT fleshshaper turns it into armor");
			assertEquals(2, player.getHero().getArmor(), "less health than opponent + 2 lifedrain, BUT fleshshaper turns it into armor");
		});
	}

	@Test
	public void testPsychoticServant() {
		runGym((context, player, opponent) -> {
			var target = playMinionCard(context, player, 1, 3);
			var servant = playMinionCard(context, player, "minion_psychotic_servant", target);
			assertEquals(servant.getBaseHp() + 3, servant.getHp(), "stole 3 hp");
			assertTrue(target.isDestroyed());
		});
		runGym((context, player, opponent) -> {
			var target = playMinionCard(context, player, 1, 1);
			var servant = playMinionCard(context, player, "minion_psychotic_servant", target);
			assertEquals(servant.getBaseHp() + 1, servant.getHp(), "stole 1 hp");
			assertTrue(target.isDestroyed());
		});
		runGym((context, player, opponent) -> {
			var target = playMinionCard(context, player, 1, 4);
			var servant = playMinionCard(context, player, "minion_psychotic_servant", target);
			assertEquals(servant.getBaseHp() + 3, servant.getHp(), "stole 3 hp");
			assertFalse(target.isDestroyed());
		});
	}

	@Test
	public void testCalamityBeckonsSpell() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			shuffleToDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			shuffleToDeck(context, player, "spell_lunstone");
			playCard(context, player, "spell_calamity_beckons");
			assertEquals(2, player.getMinions().size(), "summoned both destroyed things");
			assertEquals(0, player.getDeck().size(), "deck is destroyed");
		});
	}

	@Test
	public void testSeekerAshi() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_seeker_ashi");
			var card = shuffleToDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			player.getHero().setAttack(1);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(Zones.HAND, card.getZone(), "drawn");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_seeker_ashi");
			var card = shuffleToDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			card.getAttributes().put(Attribute.BASE_MANA_COST, 2);
			player.getHero().setAttack(1);
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(Zones.DECK, card.getZone(), "not drawn, wrong cost");
		});
	}

	@Test
	public void testStemTheFlow() {
		runGym((context, player, opponent) -> {
			var target = playMinionCard(context, player, 1, 2);
			target.setHp(1);
			playCard(context, player, "spell_stem_the_flow", target);
			assertEquals(11, target.getMaxHp(), "9 extra healing, base hp 2, total is 11");
		});
	}

	@Test
	public void testXueTheEternal() {
		runGym((context, player, opponent) -> {
			var xue = playMinionCard(context, player, "minion_xue_the_eternal");
			var target = playMinionCard(context, player, 1, 2);
			target.setHp(1);
			playCard(context, player, "spell_stem_the_flow", target);
			assertEquals("permanent_xue_the_eternal", xue.transformResolved(context).getSourceCard().getCardId(), "still needs 1 more excess healing to convert");
			target = playMinionCard(context, player, 1, 2);
			target.setHp(1);
			playCard(context, player, "spell_stem_the_flow", target);
			assertEquals("minion_xue_the_eternal", xue.transformResolved(context).getSourceCard().getCardId(), "revived");
		});
	}

	@Test
	public void testGravelordsGambit() {
		runGym((context, player, opponent) -> {
			for (var i = 0; i < 4; i++) {
				destroy(context, playMinionCard(context, player, "minion_test_deathrattle"));
			}

			for (var i = 0; i < 4; i++) {
				putOnTopOfDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			}

			playCard(context, player, "spell_gravelords_gambit");
			assertEquals(player.getHand().size(), 3, "only repeated last 3 aftermaths");
		});
	}

	@Test
	public void testSoulscream() {
		runGym((context, player, opponent) -> {
			Card drawn = putOnTopOfDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			putOnTopOfDeck(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			Minion target = playMinionCard(context, player, "minion_test_deathrattle");
			destroy(context, target);
			assertEquals(Zones.GRAVEYARD, target.getZone());
			assertEquals(1, player.getHand().size());
			overrideDiscover(context, player, discoverActions -> {
				assertEquals("minion_test_deathrattle", discoverActions.get(0).getCard().getCardId());
				return discoverActions.get(0);
			});
			playCard(context, player, "spell_soulscream");
			assertEquals(3, player.getHand().size(), "receive Soulscream AND the aftermath result");
			assertEquals(Zones.HAND, drawn.getZone());
		});
	}

	@Test
	public void testSoulscreamRework() {
		runGym((context, player, opponent) -> {
			var target = playMinionCard(context, player, "minion_test_deathrattle");
			destroy(context, target);
			var deathrattlesTriggered = 1;
			var hp = opponent.getHero().getHp();
			playCard(context, player, "spell_soulscream_rework", opponent.getHero());
			assertEquals(hp - deathrattlesTriggered, opponent.getHero().getHp());
			var card = receiveCard(context, player, "spell_soulscream_rework");
			assertEquals("Deal 1 damage. (Increases by 1 for each Aftermath you've triggered this game)", card.getDescription(context, player));
			playCard(context, player, "minion_gatekeeper_sha_rework");
			assertEquals("Deal 3 damage. (Increases by 2 for each Aftermath you've triggered this game)", card.getDescription(context, player));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"minion_gravelord_goa", "minion_bloodlord_goa"})
	public void testBloodlordGoa(String cardId) {
		runGym((context, player, opponent) -> {
			Minion drawsCard = playMinionCard(context, player, "minion_test_deathrattle");
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "spell_lunstone");
			putOnTopOfDeck(context, player, "spell_lunstone");
			destroy(context, drawsCard);
			assertEquals(Zones.DECK, shouldBeDrawn.getZone(), "should not yet be drawn");
			Minion goa = playMinionCard(context, player, cardId);
			destroy(context, goa);
			assertEquals(Zones.HAND, shouldBeDrawn.getZone(), "goa repeats draw card deathrattle");
		});

		runGym((context, player, opponent) -> {
			Minion drawsCard = playMinionCard(context, player, "minion_test_deathrattle");
			Card shouldNotBeDrawn = putOnTopOfDeck(context, player, "spell_lunstone");
			putOnTopOfDeck(context, player, "spell_lunstone");
			assertEquals(Zones.DECK, shouldNotBeDrawn.getZone(), "should not yet be drawn");
			Minion goa = playMinionCard(context, player, cardId);
			destroy(context, goa);
			assertEquals(Zones.DECK, shouldNotBeDrawn.getZone(), "goa does NOT repeat draw card deathrattle because it hasn't triggered yet");
		});

		runGym((context, player, opponent) -> {
			Minion drawsCard = playMinionCard(context, player, "minion_test_deathrattle");
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "spell_lunstone");
			putOnTopOfDeck(context, player, "spell_lunstone");
			playMinionCard(context, player, "minion_vein_burster", drawsCard);
			assertEquals(Zones.DECK, shouldBeDrawn.getZone(), "should not yet be drawn");
			Minion goa = playMinionCard(context, player, cardId);
			destroy(context, goa);
			assertEquals(Zones.HAND, shouldBeDrawn.getZone(), "goa repeats draw card deathrattle");
		});

		runGym((context, player, opponent) -> {
			Minion drawsCard = playMinionCard(context, player, "minion_test_deathrattle");
			destroy(context, drawsCard);
			Card shouldBeDrawn = putOnTopOfDeck(context, player, "spell_lunstone");
			Card shouldBeDrawnToo = putOnTopOfDeck(context, player, "spell_lunstone");
			putOnTopOfDeck(context, player, "spell_lunstone");
			playCard(context, player, "spell_soulscream");
			assertEquals(Zones.DECK, shouldBeDrawn.getZone(), "should not yet be drawn");
			Minion goa = playMinionCard(context, player, cardId);
			destroy(context, goa);
			assertEquals(Zones.HAND, shouldBeDrawn.getZone(), "goa repeats draw card deathrattle");
			assertEquals(Zones.HAND, shouldBeDrawnToo.getZone(), "goa repeats draw card deathrattle again");
		});
	}

	@Test
	public void testUnendingNightmare() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			playCard(context, player, "spell_unending_nightmare", target);
			context.endTurn();
			assertEquals(opponent.getId(), context.getActivePlayerId(), "should be opponent's turn");
			assertTrue(target.isDestroyed(), "should be destroyed");
			assertEquals(1, player.getMinions().size(), "should have summoned replacement");
			target = player.getMinions().get(0);
			assertEquals(context.getCardCatalogue().getOneOneNeutralMinionCardId(), target.getSourceCard().getCardId(), "should have resummoned");
			assertEquals(1L, player.getGraveyard().stream().filter(c -> c.getEntityType() == EntityType.MINION && c.getSourceCard().getCardId().equals(context.getCardCatalogue().getOneOneNeutralMinionCardId())).count(), "should only have been destroyed once so far");
			context.endTurn();
			assertEquals(player.getId(), context.getActivePlayerId(), "should be player's turn");
			context.endTurn();
			target = player.getMinions().get(0);
			assertEquals(2L, player.getGraveyard().stream().filter(c -> c.getEntityType() == EntityType.MINION && c.getSourceCard().getCardId().equals(context.getCardCatalogue().getOneOneNeutralMinionCardId())).count(), "should be two destroyed minions now");
			assertEquals(1, player.getMinions().size(), "should still just be one unending nightmare minion");
			assertEquals(context.getCardCatalogue().getOneOneNeutralMinionCardId(), target.getSourceCard().getCardId(), "should have resummoned");
			assertEquals(opponent.getId(), context.getActivePlayerId(), "should be opponent's turn");
		});
	}

	@Test
	public void testSanguineShiv() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_sanguine_shiv");
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getBaseDurability() - 1, player.getWeaponZone().get(0).getDurability(), "no drain this turn");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_sanguine_shiv");
			playCard(context, player, "spell_test_drain", opponent.getHero());
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getBaseDurability(), player.getWeaponZone().get(0).getDurability(), "drained this turn");
		});

		runGym((context, player, opponent) -> {
			playCard(context, player, "weapon_sanguine_shiv");
			playCard(context, player, "spell_test_drain", opponent.getHero());
			context.endTurn();
			context.endTurn();
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getWeaponZone().get(0).getBaseDurability() - 1, player.getWeaponZone().get(0).getDurability(), "drained last turn should spend durability normally");
		});
	}

	@Test
	public void testMaliciousMirage() {
		runGym((context, player, opponent) -> {
			Card shouldNotDraw = putOnTopOfDeck(context, player, "spell_lunstone");
			Minion malicious = playMinionCard(context, player, "minion_malicious_mirage");
			destroy(context, malicious);
			assertEquals(Zones.DECK, shouldNotDraw.getZone());
		});

		// Increase health via regular buff
		runGym((context, player, opponent) -> {
			Card shouldNotDraw = putOnTopOfDeck(context, player, "spell_lunstone");
			Minion malicious = playMinionCard(context, player, "minion_malicious_mirage");
			playMinionCard(context, player, "minion_test_buff", malicious);
			destroy(context, malicious);
			assertEquals(Zones.HAND, shouldNotDraw.getZone());
		});
	}

	@Test
	public void testYaganLifetaker() {
		runGym((context, player, opponent) -> {
			Minion otherTarget = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			int heroHp = player.getHero().getHp();
			Minion lifetaker = playMinionCard(context, player, "minion_yagan_lifetaker");
			assertEquals(player.getHero().getHp(), heroHp / 2);
			assertEquals(otherTarget.getHp() + lifetaker.getHp(), lifetaker.getBaseHp() + otherTarget.getBaseHp() + (heroHp - heroHp / 2));
		});
	}

	@Test
	public void testLividZealotBadSwarmInteraction() {
		runGym((context, player, opponent) -> {
			Minion batSwarm = playMinionCard(context, player, "minion_vampiric_savage");
			Minion shouldBeBuffed = playMinionCard(context, player, "minion_livid_zealot");
			context.endTurn();
			Minion target = playMinionCard(context, player, "minion_gorthal_the_ravager");
			context.endTurn();
			attack(context, player, batSwarm, target);
			assertTrue(batSwarm.isDestroyed());
			useHeroPower(context, player);
			assertEquals(shouldBeBuffed.getMaxHp(), shouldBeBuffed.getBaseHp() + 2);
		}, HeroClass.BLOOD, HeroClass.NAVY);
	}

	@Test
	public void testBatSwarm() {
		runGym((context, player, opponent) -> {
			// i.e. bat swarm
			Minion shouldBeBuffed = playMinionCard(context, player, "minion_vampiric_savage");
			useHeroPower(context, player);
			assertEquals(shouldBeBuffed.getMaxHp(), shouldBeBuffed.getBaseHp() + 2);
		}, HeroClass.BLOOD, HeroClass.BLOOD);
	}

	@Test
	public void testRendingCurseEternalSteedInteraction() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			destroy(context, target);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_eternal_steed");
			Minion other = playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			playCard(context, player, "spell_rending_curse", other);
			destroy(context, target);
		});

		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_eternal_steed");
			Minion other = playMinionCard(context, player, "minion_eternal_steed");
			playMinionCard(context, player, "minion_eternal_steed");
			playMinionCard(context, player, "minion_eternal_steed");
			playCard(context, player, "spell_rending_curse", target);
			playCard(context, player, "spell_rending_curse", other);
			destroy(context, target);
		});
	}

	@Test
	public void testLadyUki() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_lady_uki");
			// Opener
			int hp = opponent.getHero().getHp();
			playCard(context, player, "weapon_test_opener_deal_1", opponent.getHero());
			assertEquals(opponent.getHero().getHp(), hp - 2, "Doubled damage");

			// Aftermath
			hp = player.getHero().getHp();
			hp = player.getHero().getHp();
			playCard(context, player, "weapon_test_aftermath_deal_1_friendly_hero");
			playCard(context, player, "weapon_test_1_1");
			assertEquals(player.getHero().getHp(), hp - 2);
			// Aura should not "double"
			playCard(context, player, "weapon_test_aura");
			Minion test = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			assertEquals(test.getAttack(), test.getBaseAttack() + 1);
			// Trigger
			playCard(context, player, "weapon_test_trigger");
			hp = player.getHero().getHp();
			attack(context, player, player.getHero(), opponent.getHero());
			assertEquals(player.getHero().getHp(), hp - 2);
		});
	}

	@Test
	public void testSuddenConversion() {
		runGym((context, player, opponent) -> {
			Minion target1 = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			Minion target2 = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			Minion target3 = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			playCard(context, player, "spell_sudden_conversion", target2);
			assertEquals(target2.getHp(), target1.getBaseHp() + target3.getBaseHp() + target2.getBaseHp());
			assertTrue(target1.isDestroyed());
			assertTrue(target3.isDestroyed());
		});
	}

	@Test
	public void testTiramashi() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_tiramashi");
			context.endTurn();
			Minion target = playMinionCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			context.endTurn();
			playCard(context, player, "spell_test_drain", target);
			assertEquals(player.getHand().size(), 1);
			assertEquals(player.getHand().get(0).getCardId(), context.getCardCatalogue().getOneOneNeutralMinionCardId());
		});
	}

	@Test
	public void testFinalFeast() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 5; i++) {
				playCard(context, player, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			}
			context.endTurn();
			for (int i = 0; i < 2; i++) {
				playCard(context, opponent, context.getCardCatalogue().getOneOneNeutralMinionCardId());
			}
			context.endTurn();
			// Minion arrangement
			//    X X     opposing
			// X X X X X  friendly
			playCard(context, player, "spell_final_feast");
			assertEquals(player.getMinions().get(0).getMaxHp(), 5, "drains from champion");
			assertEquals(player.getMinions().get(1).getMaxHp(), 5, "drains from 1 minion");
			assertEquals(player.getMinions().get(2).getMaxHp(), 9, "drains from 2 minions");
			assertEquals(player.getMinions().get(3).getMaxHp(), 5, "drains from 1 minion");
			assertEquals(player.getMinions().get(4).getMaxHp(), 5, "drains from champion");
		});
	}

	@Test
	public void testDoombringerVisha() {
		runGym((context, player, opponent) -> {
			Minion doombringer = playMinionCard(context, player, "minion_doombringer_visha");
			Minion target = playMinionCard(context, player, 10, 10);
			playCard(context, player, "spell_test_drain", target);
			assertEquals(doombringer.getMaxHp(), doombringer.getBaseHp(), "no buff because drain damage");
			playCard(context, player, "spell_test_deal_1", target);
			assertEquals(doombringer.getMaxHp(), doombringer.getBaseHp() + 2, "drain additional 2 from target");
		});
	}

	@Test
	public void testYakhaReiri() {
		runGym((context, player, opponent) -> {
			Card yakhaReiri = receiveCard(context, player, "minion_yakha_reiri");
			Minion target = playMinionCard(context, player, 0, 51);
			castDamageSpell(context, player, 49, target);
			player.setMana(10);
			assertFalse(context.getLogic().conditionMet(player.getId(), yakhaReiri));
			castDamageSpell(context, player, 1, target);
			assertTrue(context.getLogic().conditionMet(player.getId(), yakhaReiri));
		});
	}


	@Test
	public void testLifetaker() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 3; i++) {
				playMinionCard(context, player, "minion_neutral_test");
			}
			Minion lifetaker = playMinionCard(context, player, "minion_marrow_render");
			assertEquals(lifetaker.getMaxHp(), lifetaker.getBaseHp() + 3);
		});
	}

	@Test
	public void testBloodElfChampion() {
		runGym((context, player, opponent) -> {
			// No opposing minions, no swap
			Minion elf = playMinionCard(context, player, "minion_blood_elf_champion");
			assertEquals(elf.getHp(), 2);
		});

		runGym((context, player, opponent) -> {
			// One opposing minion, swap
			context.endTurn();
			Minion swapped = playMinionCard(context, opponent, "minion_neutral_test");
			swapped.setHp(10);
			context.endTurn();
			Minion elf = playMinionCard(context, player, "minion_blood_elf_champion");
			assertEquals(elf.getHp(), 10);
			assertEquals(swapped.getHp(), 2);
		});

		runGym((context, player, opponent) -> {
			// Two opposing minions, split
			context.endTurn();
			Minion swapped1 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped1.setHp(10);
			Minion swapped2 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped2.setHp(10);
			context.endTurn();
			Minion elf = playMinionCard(context, player, "minion_blood_elf_champion");
			assertEquals(elf.getHp(), 20);
			assertEquals(swapped1.getHp(), 1);
			assertEquals(swapped2.getHp(), 1);
		});

		runGym((context, player, opponent) -> {
			// Two opposing minions, handbuffed, split remainder to first minion
			context.endTurn();
			Minion swapped1 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped1.setHp(10);
			Minion swapped2 = playMinionCard(context, opponent, "minion_neutral_test");
			swapped2.setHp(10);
			context.endTurn();
			Card elfCard = receiveCard(context, player, "minion_blood_elf_champion");
			elfCard.setAttribute(Attribute.HP_BONUS, 1);
			Minion elf = playMinionCard(context, player, elfCard);
			assertEquals(elf.getHp(), 20);
			assertEquals(swapped1.getHp(), 2);
			assertEquals(swapped2.getHp(), 1);
		});
	}


	@Test
	public void testSkullsplitterTroll() {
		runGym((context, player, opponent) -> {
			Minion troll = playMinionCard(context, player, "minion_skullsplitter_troll");
			for (int i = 0; i < 2; i++) {
				playMinionCard(context, player, "minion_neutral_test");
			}
			Minion lifetaker = playMinionCard(context, player, "minion_marrow_render");
			assertEquals(lifetaker.getMaxHp(), lifetaker.getBaseHp() + 3);
			assertEquals(troll.getAttack(), troll.getBaseAttack() + 1, "Draining only occurred once");
		});
	}

	@Test
	public void testDreamOfDeath() {
		runGym((context, player, opponent) -> {
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");
			playMinionCard(context, player, "minion_neutral_test");

			playCard(context, player, "spell_dream_of_death");

			assertEquals(player.getHero().getHp(), 11);
		});
	}

	@Test
	public void testSpiritSaber() {
		runGym((context, player, opponent) -> {
			for (int i = 0; i < 30; i++) {
				shuffleToDeck(context, player, "spell_lunstone");
			}
			Minion testD = playMinionCard(context, player, "minion_test_deathrattle");
			playCard(context, player, "spell_hate_spike", testD);
			playCard(context, player, "spell_hex_behemoth", testD);
			AtomicBoolean b = new AtomicBoolean(false);
			overrideDiscover(context, player, discoverActions -> {
				assertEquals(1, discoverActions.size());
				assertEquals("minion_test_deathrattle", discoverActions.get(0).getCard().getCardId());
				b.set(true);
				return discoverActions.get(0);
			});
			playCard(context, player, "weapon_spirit_saber");
			if (!b.get()) {
				fail();
			}
		});
	}
}


