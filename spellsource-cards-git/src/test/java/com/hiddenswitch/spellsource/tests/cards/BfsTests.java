package com.hiddenswitch.spellsource.tests.cards;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDescArg;
import net.demilich.metastone.game.cards.desc.HasEntrySet;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.BattlecryDescArg;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.OrCondition;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDescArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.spells.trigger.AfterSpellCastedTrigger;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class BfsTests {

	@BeforeAll
	public static void loadCards() {
		CardCatalogue.loadCardsFromPackage();
	}

	@Test
	public void testAccurateSummoningBattlecry() {
		Card card = CardCatalogue.getCardById("minion_test_opener_summon");
		Stream.Builder<HasEntrySet.BfsNode<Enum, Object>> bfs = card.getDesc().bfs();

		boolean shouldMatch = bfs.build()
				.anyMatch(node -> node.getKey().equals(SpellArg.CLASS)
						&& SummonSpell.class.isAssignableFrom((Class) node.getValue())
						&& node.predecessors().anyMatch(pred -> pred.getKey().equals(BattlecryDescArg.SPELL)));

		assertTrue(shouldMatch, "Minion should match");
	}

	@Test
	public void testAccurateBfs() {
		Card card = CardCatalogue.getCardById("minion_test_carddesc");

		List<HasEntrySet.BfsNode<Enum, Object>> nodes = card.getDesc().bfs().build().collect(Collectors.toList());

		assertContains(nodes, CardDescArg.NAME, "Name");
		assertContains(nodes, CardDescArg.BASE_MANA_COST, 3);
		assertContains(nodes, CardDescArg.TYPE, CardType.MINION);
		assertContains(nodes, CardDescArg.HERO_CLASS, "GOLD");
		assertContains(nodes, CardDescArg.BASE_ATTACK, 2);
		assertContains(nodes, CardDescArg.BASE_HP, 1);
		assertContains(nodes, CardDescArg.RARITY, Rarity.COMMON);
		assertContains(nodes, CardDescArg.DESCRIPTION, "Test");
		assertContains(nodes, EventTriggerArg.CLASS, AfterSpellCastedTrigger.class);
		assertContains(nodes, EventTriggerArg.CLASS, TurnEndTrigger.class);
		assertContains(nodes, EventTriggerArg.CLASS, TurnStartTrigger.class);
		assertContains(nodes, SpellArg.CLASS, MissilesSpell.class);
		assertContains(nodes, SpellArg.CLASS, ConditionalSpell.class);
		assertContains(nodes, SpellArg.CLASS, SummonSpell.class);
		assertContains(nodes, SpellArg.CLASS, DrawCardSpell.class);
		assertContains(nodes, SpellArg.CLASS, HealSpell.class);
		assertContains(nodes, ConditionArg.CLASS, OrCondition.class);
		assertContains(nodes, ValueProviderArg.CLASS, AttributeValueProvider.class);
		assertContains(nodes, SpellArg.VALUE, 5);
		assertContains(nodes, ValueProviderArg.ATTRIBUTE, Attribute.STEALTH);
		assertContains(nodes, Attribute.TAUNT, true);
		assertContains(nodes, Attribute.BATTLECRY, true);
		assertContains(nodes, Attribute.SPELL_DAMAGE, 2);
		assertContains(nodes, CardDescArg.COLLECTIBLE, true);
		assertContains(nodes, CardDescArg.SETS, "TEST");
		assertContains(nodes, BattlecryDescArg.TARGET_SELECTION, TargetSelection.MINIONS);
		assertContains(nodes, CardDescArg.ATTRIBUTES, card.getDesc().getAttributes());
		assertContains(nodes, CardDescArg.TRIGGERS, card.getDesc().getTrigger());
		assertContains(nodes, EnchantmentDescArg.EVENT_TRIGGER, card.getDesc().getTrigger().getEventTrigger());

	}

	public static void assertContains(List<HasEntrySet.BfsNode<Enum, Object>> nodes, Enum key, Object value) {
		for (HasEntrySet.BfsNode<Enum, Object> node : nodes) {
			if (node.getKey().equals(key) && node.getValue().equals(value)) {
				return;
			}
		}

		fail(String.format("Nodelist does not contain %s %s", key.toString(), value.toString()));
	}
}
