package com.hiddenswitch.spellsource.cards.hearthstone;

import com.hiddenswitch.spellsource.core.AbstractCardResources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HearthstoneCardResources extends AbstractCardResources<HearthstoneCardResources> {
	private static final List<String> BANNED_DRAFT_CARD_IDS = Collections.unmodifiableList(Arrays.asList("spell_forgotten_torch",
			"minion_snowchugger",
			"minion_faceless_summoner",
			"minion_goblin_auto-barber",
			"minion_undercity_valiant",
			"minion_vitality_totem",
			"minion_dust_devil",
			"spell_totemic_might",
			"spell_ancestral_healing",
			"minion_dunemaul_shaman",
			"minion_windspeaker",
			"minion_anima_golem",
			"spell_sacrificial_pact",
			"spell_curse_of_rafaam",
			"spell_sense_demons",
			"minion_void_crusher",
			"minion_reliquary_seeker",
			"minion_succubus",
			"spell_savagery",
			"spell_poison_seeds",
			"spell_soul_of_the_forest",
			"spell_mark_of_nature",
			"spell_tree_of_life",
			"spell_astral_communion",
			"minion_warsong_commander",
			"spell_bolster",
			"spell_charge",
			"spell_bouncing_blade",
			"minion_axe_flinger",
			"spell_rampage",
			"minion_ogre_warmaul",
			"minion_starving_buzzard",
			"spell_call_pet",
			"minion_timber_wolf",
			"spell_cobra_shot",
			"spell_lock_and_load",
			"secret_dart_trap",
			"secret_snipe",
			"spell_mind_blast",
			"minion_shadowbomber",
			"minion_lightwell",
			"spell_power_word_glory",
			"spell_confuse",
			"spell_convert",
			"spell_inner_fire"
	));

	private static final List<String> HARD_REMOVAL_CARD_IDS = Collections.unmodifiableList(Arrays.asList(
			"spell_polymorph",
			"spell_execute",
			"spell_crush",
			"spell_assassinate",
			"spell_siphon_soul",
			"spell_shadow_word_death",
			"spell_naturalize",
			"spell_hex",
			"spell_humility",
			"spell_equality",
			"spell_deadly_shot",
			"spell_sap",
			"minion_doomsayer",
			"minion_big_game_hunter"));

	public HearthstoneCardResources() {
		super(HearthstoneCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "hearthstone";
	}

	@Override
	public List<String> getDraftBannedCardIds() {
		return BANNED_DRAFT_CARD_IDS;
	}

	@Override
	public List<String> getHardRemovalCardIds() {
		return HARD_REMOVAL_CARD_IDS;
	}
}
