package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.cards.CardCatalogue;

import java.util.HashSet;

public class NeverUseOnEnemyMinions {
	HashSet<String> classicAndBasicSets;

	NeverUseOnEnemyMinions() {
		classicAndBasicSets = new HashSet<>();
		classicAndBasicSets.addAll(generateHeroPowers());
		classicAndBasicSets.addAll(generateBasicSet());
		classicAndBasicSets.addAll(generateClassicSet());
	}

	public HashSet<String> generateHeroPowers() {
		HashSet<String> hashSet = new HashSet<>();
		testAdd(hashSet, "hero_power_dinomancy");
		testAdd(hashSet, "hero_power_heal");
		testAdd(hashSet, "hero_power_lesser_heal");
		return hashSet;
	}

	public HashSet<String> generateClassicSet() {
		HashSet<String> hashSet = new HashSet<>();
		testAdd(hashSet, "minion_abusive_sergeant");
		testAdd(hashSet, "minion_earthen_ring_farseer");
		testAdd(hashSet, "minion_dark_iron_dwarf");
		testAdd(hashSet, "spell_bananas");
		testAdd(hashSet, "spell_nightmare");
		testAdd(hashSet, "spell_mark_of_nature");
		testAdd(hashSet, "spell_mark_of_nature_1");
		testAdd(hashSet, "spell_mark_of_nature_2");
		testAdd(hashSet, "spell_mark_of_nature_3");
		testAdd(hashSet, "minion_argent_protector");
		testAdd(hashSet, "spell_blessed_champion");
		testAdd(hashSet, "spell_blessing_of_kings");
		testAdd(hashSet, "spell_lay_on_hands");
		testAdd(hashSet, "spell_cold_blood");
		testAdd(hashSet, "spell_ancestral_spirit");
		testAdd(hashSet, "spell_inner_rage");
		testAdd(hashSet, "minion_cruel_taskmaster");
		return hashSet;
	}

	public HashSet<String> generateBasicSet() {
		HashSet<String> hashSet = new HashSet<>();
		testAdd(hashSet, "minion_voodoo_doctor");
		testAdd(hashSet, "spell_mark_of_the_wild");
		testAdd(hashSet, "spell_healing_touch");
		testAdd(hashSet, "spell_hand_of_protection");
		testAdd(hashSet, "spell_holy_light");
		testAdd(hashSet, "spell_hammer_of_wrath");
		testAdd(hashSet, "spell_power_word_shield");
		testAdd(hashSet, "spell_divine_spirit");
		testAdd(hashSet, "spell_ancestral_healing");
		testAdd(hashSet, "spell_windfury");
		return hashSet;
	}

	private void testAdd(HashSet<String> hashSet, String cardIdToAdd) {
		CardCatalogue.getCardById(cardIdToAdd);
		hashSet.add(cardIdToAdd);
	}
}
