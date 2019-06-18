package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.cards.CardCatalogue;

import java.util.HashSet;

public class NeverUseOnOwnMinion {
	HashSet<String> classicAndBasicSets;

	NeverUseOnOwnMinion() {
		classicAndBasicSets = new HashSet<>();
		classicAndBasicSets.addAll(generateHeroPowers());
		classicAndBasicSets.addAll(generateBasicSet());
		classicAndBasicSets.addAll(generateClassicSet());
	}

	public HashSet<String> generateHeroPowers() {
		HashSet<String> hashSet = new HashSet<>();
		testAdd(hashSet, "hero_power_mind_spike");
		testAdd(hashSet, "hero_power_mind_shatter");
		testAdd(hashSet, "hero_power_berserker_throw");
		testAdd(hashSet, "hero_power_fireblast");
		testAdd(hashSet, "hero_power_fireblast_rank_2");
		testAdd(hashSet, "hero_power_icy_touch");
		testAdd(hashSet, "hero_power_voidform");
		testAdd(hashSet, "hero_power_siphon_life");
		testAdd(hashSet, "hero_power_lightning_jolt");
		testAdd(hashSet, "hero_power_zap_cannon");
		return hashSet;
	}

	public HashSet<String> generateClassicSet() {
		HashSet<String> hashSet = new HashSet<>();
		testAdd(hashSet, "minion_ironbeak_owl");
		testAdd(hashSet, "minion_spellbreaker");
		testAdd(hashSet, "minion_frost_elemental");
		testAdd(hashSet, "minion_big_game_hunter");
		testAdd(hashSet, "spell_dream");
		testAdd(hashSet, "spell_wrath");
		testAdd(hashSet, "spell_wrath_1");
		testAdd(hashSet, "spell_wrath_2");
		testAdd(hashSet, "spell_wrath_3");
		testAdd(hashSet, "spell_savagery");
		testAdd(hashSet, "minion_keeper_of_the_grove");
		testAdd(hashSet, "spell_starfall");
		testAdd(hashSet, "spell_starfall_1");
		testAdd(hashSet, "spell_starfall_3");
		testAdd(hashSet, "spell_explosive_shot");
		testAdd(hashSet, "spell_cone_of_cold");
		testAdd(hashSet, "spell_icicle");
		testAdd(hashSet, "spell_pyroblast");
		testAdd(hashSet, "spell_fireball");
		testAdd(hashSet, "minion_aldor_peacekeeper");
		testAdd(hashSet, "spell_holy_wrath");
		testAdd(hashSet, "spell_icicle");
		testAdd(hashSet, "spell_silence");
		testAdd(hashSet, "spell_holy_fire");
		testAdd(hashSet, "spell_eviscerate");
		testAdd(hashSet, "weapon_perditions_blade");
		testAdd(hashSet, "spell_eviscerate");
		testAdd(hashSet, "minion_si7_agent");
		testAdd(hashSet, "minion_kidnapper");
		testAdd(hashSet, "spell_earth_shock");
		testAdd(hashSet, "spell_lightning_bolt");
		testAdd(hashSet, "spell_lava_burst");
		testAdd(hashSet, "spell_siphon_soul");
		testAdd(hashSet, "spell_bane_of_doom");
		testAdd(hashSet, "spell_slam");
		testAdd(hashSet, "spell_mortal_strike");
		testAdd(hashSet, "spell_shield_slam");
		return hashSet;
	}

	public HashSet<String> generateBasicSet() {
		HashSet<String> hashSet = new HashSet<>();
		testAdd(hashSet, "minion_elven_archer");
		testAdd(hashSet, "minion_stormpike_commando");
		testAdd(hashSet, "spell_moonfire");
		testAdd(hashSet, "spell_starfire");
		testAdd(hashSet, "spell_arcane_shot");
		testAdd(hashSet, "spell_hunters_mark");
		testAdd(hashSet, "spell_kill_command");
		testAdd(hashSet, "spell_frostbolt");
		testAdd(hashSet, "spell_fireball");
		testAdd(hashSet, "spell_polymorph");
		testAdd(hashSet, "spell_humility");
		testAdd(hashSet, "spell_hammer_of_wrath");
		testAdd(hashSet, "spell_holy_smite");
		testAdd(hashSet, "spell_shadow_word_pain");
		testAdd(hashSet, "spell_shadow_word_death");
		testAdd(hashSet, "spell_backstab");
		testAdd(hashSet, "spell_shiv");
		testAdd(hashSet, "spell_hex");
		testAdd(hashSet, "minion_fire_elemental");
		testAdd(hashSet, "spell_sacrificial_pact");
		testAdd(hashSet, "spell_mortal_coil");
		testAdd(hashSet, "spell_soulfire");
		testAdd(hashSet, "spell_shadow_bolt");
		return hashSet;
	}

	private void testAdd(HashSet<String> hashSet, String cardIdToAdd) {
		CardCatalogue.getCardById(cardIdToAdd);
		hashSet.add(cardIdToAdd);
	}
}
