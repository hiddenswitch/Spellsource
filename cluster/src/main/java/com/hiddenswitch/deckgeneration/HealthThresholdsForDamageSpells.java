package com.hiddenswitch.deckgeneration;

import java.util.HashMap;

public class HealthThresholdsForDamageSpells {
	HashMap<String, Integer> allThresholds = new HashMap<>();
	HashMap<String, Integer> basicAndClassicThresholds;

	public HealthThresholdsForDamageSpells() {
		basicAndClassicThresholds = generateBasicAndClassicthresholds();
		allThresholds.putAll(basicAndClassicThresholds);
	}

	public HashMap<String, Integer> generateBasicAndClassicthresholds() {
		HashMap<String, Integer> hashMap = new HashMap<>();
		// Basic Set
		hashMap.put("spell_starfire", 3);
		hashMap.put("spell_swipe", 3);
		hashMap.put("spell_kill_command", 2);
		hashMap.put("spell_frostbolt", 2);
		hashMap.put("spell_fireball", 4);
		hashMap.put("spell_hammer_of_wrath", 2);
		hashMap.put("minion_fire_elemental", 2);
		hashMap.put("spell_soulfire", 3);
		hashMap.put("spell_shadow_bolt", 3);

		// Classic Set
		hashMap.put("spell_starfall", 3);
		hashMap.put("spell_starfall_1", 3);
		hashMap.put("spell_starfall_3", 3);
		hashMap.put("spell_explosive_shot", 3);
		hashMap.put("spell_holy_fire", 3);
		hashMap.put("spell_lightning_bolt", 2);
		hashMap.put("spell_lava_burst", 3);
		hashMap.put("spell_mortal_strike", 3);

		return hashMap;
	}
}
