package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.spellsource.rpc.Spellsource.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import com.hiddenswitch.spellsource.testutils.RandomDeck;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class RuleCardsAndModelConversionsTest extends TestBase {

	private static final List<String> EXPECTED_RULE_CARDS = List.of(
			"enchantment_constructed_rules",
			"enchantment_rule_mana",
			"enchantment_rule_draw",
			"enchantment_rule_hero_power",
			"enchantment_rule_deck_size"
	);

	@Test
	public void testSpellsourceFormatsAndRunGymUseRuleCards() {
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
		for (String formatName : List.of("Spellsource", "Pauper", "Rogue", "All")) {
			var format = ClasspathCardCatalogue.INSTANCE.getFormat(formatName);
			assertNotNull(format, "Format " + formatName + " should exist");
			assertNotNull(format.getRuleCards(), "Format " + formatName + " should have ruleCards");
			var ruleCards = Set.of(format.getRuleCards());
			for (String expected : EXPECTED_RULE_CARDS) {
				assertTrue(ruleCards.contains(expected),
						"Format " + formatName + " missing rule card " + expected);
			}
		}

		runGym((context, player, opponent) -> {
			for (var p : List.of(player, opponent)) {
				var activeRuleIds = context.getTriggers().stream()
						.filter(t -> t instanceof Enchantment e
								&& !e.isExpired()
								&& e.getZone() == Zones.ENCHANTMENT
								&& p.getReference().equals(e.getHostReference())
								&& e.getSourceCard() != null)
						.map(t -> ((Enchantment) t).getSourceCard().getCardId())
						.collect(Collectors.toSet());
				for (String expected : EXPECTED_RULE_CARDS) {
					assertTrue(activeRuleIds.contains(expected),
							"Spellsource runGym player " + p.getId() + " missing active rule enchantment " + expected);
				}
			}
		});
	}

	@Test
	public void testMultiplayerFormatResolutionAndGrpcModelDataPopulatedCorrectly() {
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
		var spellsourceFormat = ClasspathCardCatalogue.INSTANCE.spellsource();
		var deck1 = RandomDeck.randomDeck(HeroClass.BROWN, spellsourceFormat);
		var deck2 = RandomDeck.randomDeck(HeroClass.NAVY, spellsourceFormat);

		// Verify ServerGameContext's smallest-superset format resolution preserves ruleCards
		var resolvedFormat = ClasspathCardCatalogue.INSTANCE.getSmallestSupersetFormat(deck1, deck2);
		assertNotNull(resolvedFormat.getRuleCards());
		assertEquals(Set.copyOf(EXPECTED_RULE_CARDS), Set.of(resolvedFormat.getRuleCards()));

		var context = createContext(HeroClass.BROWN, HeroClass.NAVY, true, resolvedFormat);
		var activePlayer = context.getActivePlayer();
		Minion minion = playMinionCard(context, activePlayer, "minion_neutral_test");

		// Apply an inline buff spell (+3/+4) and an attribute enchantment (Taunt)
		var buffSpell = BuffSpell.create(minion.getReference(), 3, 4);
		SpellUtils.castChildSpell(context, activePlayer, buffSpell, activePlayer.getHero(), minion);
		var tauntSpell = AddAttributeSpell.create(minion.getReference(), Attribute.TAUNT);
		SpellUtils.castChildSpell(context, activePlayer, tauntSpell, activePlayer.getHero(), minion);
		assertEquals(minion.getBaseAttack() + 3, minion.getAttack());
		assertEquals(minion.getBaseHp() + 4, minion.getHp());
		assertTrue(minion.hasAttribute(Attribute.TAUNT));

		var gameState = ModelConversions.getGameState(context, activePlayer, context.getOpponent(activePlayer)).build();
		var entities = gameState.getEntitiesList();

		// 1. Verify zero duplicate entity IDs (stand-ins must never duplicate Enchantments, Secrets, or Quests)
		Set<Integer> seenIds = new HashSet<>();
		for (Entity entity : entities) {
			assertTrue(seenIds.add(entity.getId()),
					"Duplicate Entity ID in gRPC GameState: id=" + entity.getId()
							+ ", type=" + entity.getEntityType()
							+ ", name=" + entity.getName());
		}

		// 2. Verify all 5 rule card enchantments for both players are present in gRPC GameState
		for (int playerId : List.of(0, 1)) {
			var playerRuleEntities = entities.stream()
					.filter(e -> e.getEntityType() == EntityType.ENCHANTMENT
							&& e.getLocation().getZone() == Zones.ENCHANTMENT
							&& e.getHost() == playerId)
					.toList();
			var cardIds = playerRuleEntities.stream().map(Entity::getCardId).collect(Collectors.toSet());
			for (String expected : EXPECTED_RULE_CARDS) {
				assertTrue(cardIds.contains(expected),
						"gRPC GameState missing rule card " + expected + " for player " + playerId);
			}
			for (Entity ruleEntity : playerRuleEntities) {
				if (EXPECTED_RULE_CARDS.contains(ruleEntity.getCardId())) {
					assertFalse(ruleEntity.getUncensored(),
							"Hidden rule card " + ruleEntity.getCardId() + " should have uncensored=false");
					assertFalse(ruleEntity.getName().isEmpty(), "Rule card name must be populated");
				}
			}
		}

		// 3. Verify the minion's buff & attribute enchantments are populated in gRPC GameState with runtime stats & description
		var minionEnchantments = entities.stream()
				.filter(e -> e.getEntityType() == EntityType.ENCHANTMENT
						&& e.getLocation().getZone() == Zones.ENCHANTMENT
						&& e.getHost() == minion.getId())
				.toList();
		assertTrue(minionEnchantments.size() >= 2, "Minion should have both buff and attribute enchantments in gRPC GameState");
		var buffEntity = minionEnchantments.stream()
				.filter(e -> e.getAttack() == 3 && e.getHp() == 4)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected +3/+4 buff enchantment on minion, found: " + minionEnchantments));
		assertEquals("+3/+4.", buffEntity.getDescription());
		assertTrue(buffEntity.getUncensored(), "Visible minion buff enchantment should have uncensored=true");
		assertFalse(buffEntity.getCardId().contains("__ench_"),
				"Synthesized enchantment suffix __ench_ should be stripped from gRPC cardId");

		var tauntEntity = minionEnchantments.stream()
				.filter(e -> "Taunt.".equals(e.getDescription()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected Taunt. enchantment on minion, found: " + minionEnchantments));
		assertTrue(tauntEntity.getUncensored());

		// 4. Verify stylized rule card names and inline enchantment synthesis
		assertEquals("Standard Combat", ClasspathCardCatalogue.INSTANCE.getCardById("enchantment_constructed_rules").getName());
		assertEquals("Mana Growth", ClasspathCardCatalogue.INSTANCE.getCardById("enchantment_rule_mana").getName());
		assertEquals("Turn Draw", ClasspathCardCatalogue.INSTANCE.getCardById("enchantment_rule_draw").getName());
		assertEquals("Skill Refresh", ClasspathCardCatalogue.INSTANCE.getCardById("enchantment_rule_hero_power").getName());
		assertEquals("Deck & Hand Limits", ClasspathCardCatalogue.INSTANCE.getCardById("enchantment_rule_deck_size").getName());

		var synthCards = ClasspathCardCatalogue.INSTANCE.getSynthesizedEnchantmentCards();
		assertFalse(synthCards.isEmpty(), "Expected synthesized inline enchantment cards in catalogue");
		var broodguardEnch = synthCards.get("minion_broodguard__ench_0");
		assertNotNull(broodguardEnch, "Expected synthesized enchantment for minion_broodguard");
		assertEquals("Broodguard's Ward", broodguardEnch.getName());
		assertEquals("+1/+1.", broodguardEnch.getDescription());
	}
}
