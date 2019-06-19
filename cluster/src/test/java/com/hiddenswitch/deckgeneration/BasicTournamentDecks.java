package com.hiddenswitch.deckgeneration;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.ArrayList;
import java.util.List;

public class BasicTournamentDecks {

	public List<GameDeck> getAllTrumpBasicDecks() {
		List<GameDeck> trumpBasicDecks = new ArrayList<>();
		trumpBasicDecks.add(getTrumpBasicDruidDeck());
		trumpBasicDecks.add(getTrumpBasicHunterDeck());
		trumpBasicDecks.add(getTrumpBasicMageDeck());
		trumpBasicDecks.add(getTrumpBasicPaladinDeck());
		trumpBasicDecks.add(getTrumpBasicPriestDeck());
		trumpBasicDecks.add(getTrumpBasicRogueDeck());
		trumpBasicDecks.add(getTrumpBasicShamanDeck());
		trumpBasicDecks.add(getTrumpBasicWarlockDeck());
		trumpBasicDecks.add(getTrumpBasicWarriorDeck());
		return trumpBasicDecks;
	}

	public GameDeck getTrumpBasicDruidDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicDruidDeck = new GameDeck(HeroClass.BROWN);
		for (int i = 0; i < 2; i++) {
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("spell_innervate"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("spell_claw"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("spell_mark_of_the_wild"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("spell_wild_growth"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_razorfen_hunter"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("spell_swipe"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("spell_starfire"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
			trumpBasicDruidDeck.getCards().add(CardCatalogue.getCardById("minion_ironbark_protector"));
		}
		return trumpBasicDruidDeck;
	}

	public GameDeck getTrumpBasicHunterDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicHunterDeck = new GameDeck(HeroClass.GREEN);
		for (int i = 0; i < 2; i++) {
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("spell_arcane_shot"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_timber_wolf"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_bluegill_warrior"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_river_crocolisk"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_starving_buzzard"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("spell_animal_companion"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("spell_kill_command"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_razorfen_hunter"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_wolfrider"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("spell_multi-shot"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_houndmaster"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_tundra_rhino"));
			trumpBasicHunterDeck.getCards().add(CardCatalogue.getCardById("minion_reckless_rocketeer"));
		}
		return trumpBasicHunterDeck;
	}

	public GameDeck getTrumpBasicMageDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicMageDeck = new GameDeck(HeroClass.BLUE);
		for (int i = 0; i < 2; i++) {
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("spell_arcane_missiles"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("spell_frostbolt"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("spell_arcane_intellect"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("spell_fireball"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("spell_polymorph"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_water_elemental"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_gurubashi_berserker"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
			trumpBasicMageDeck.getCards().add(CardCatalogue.getCardById("spell_flamestrike"));
		}
		return trumpBasicMageDeck;
	}


	public GameDeck getTrumpBasicPaladinDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicPaladinDeck = new GameDeck(HeroClass.GOLD);
		for (int i = 0; i < 2; i++) {
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("spell_hand_of_protection"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_razorfen_hunter"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("weapon_truesilver_champion"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("spell_blessing_of_kings"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("spell_consecration"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("spell_hammer_of_wrath"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_frostwolf_warlord"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_guardian_of_kings"));
			trumpBasicPaladinDeck.getCards().add(CardCatalogue.getCardById("minion_stormwind_champion"));
		}
		return trumpBasicPaladinDeck;
	}

	public GameDeck getTrumpBasicPriestDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicPriestDeck = new GameDeck(HeroClass.WHITE);
		for (int i = 0; i < 2; i++) {
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("spell_holy_smite"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("spell_power_word_shield"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_northshire_cleric"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("spell_shadow_word_pain"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("spell_shadow_word_death"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("spell_holy_nova"));
			trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
		}
		trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("minion_stormwind_champion"));
		trumpBasicPriestDeck.getCards().add(CardCatalogue.getCardById("spell_mind_control"));
		return trumpBasicPriestDeck;
	}

	public GameDeck getTrumpBasicRogueDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicRogueDeck = new GameDeck(HeroClass.BLACK);
		for (int i = 0; i < 2; i++) {
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("spell_backstab"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("spell_deadly_poison"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("spell_fan_of_knives"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_razorfen_hunter"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("weapon_assassins_blade"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("spell_assassinate"));
			trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
		}
		trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("spell_sprint"));
		trumpBasicRogueDeck.getCards().add(CardCatalogue.getCardById("minion_stormwind_champion"));
		return trumpBasicRogueDeck;
	}

	public GameDeck getTrumpBasicShamanDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicShamanDeck = new GameDeck(HeroClass.SILVER);
		for (int i = 0; i < 2; i++) {
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("spell_rockbiter_weapon"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_flametongue_totem"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("spell_hex"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_windspeaker"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_frostwolf_warlord"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_fire_elemental"));
			trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_stormwind_champion"));
		}
		trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
		trumpBasicShamanDeck.getCards().add(CardCatalogue.getCardById("spell_bloodlust"));

		return trumpBasicShamanDeck;
	}

	public GameDeck getTrumpBasicWarlockDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicWarlockDeck = new GameDeck(HeroClass.VIOLET);
		for (int i = 0; i < 2; i++) {
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("spell_soulfire"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("spell_mortal_coil"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_elven_archer"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_bluegill_warrior"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("spell_shadow_bolt"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("spell_hellfire"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_dread_infernal"));
			trumpBasicWarlockDeck.getCards().add(CardCatalogue.getCardById("minion_stormwind_champion"));
		}
		return trumpBasicWarlockDeck;
	}

	public GameDeck getTrumpBasicWarriorDeck() {
		CardCatalogue.loadCardsFromPackage();

		GameDeck trumpBasicWarriorDeck = new GameDeck(HeroClass.RED);
		for (int i = 0; i < 2; i++) {
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("spell_execute"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("weapon_fiery_war_axe"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("spell_cleave"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("spell_heroic_strike"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_acidic_swamp_ooze"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_bloodfen_raptor"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_novice_engineer"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("spell_shield_block"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_shattered_sun_cleric"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_warsong_commander"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_chillwind_yeti"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_gnomish_inventor"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_korkron_elite"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_senjin_shieldmasta"));
			trumpBasicWarriorDeck.getCards().add(CardCatalogue.getCardById("minion_boulderfist_ogre"));
		}
		return trumpBasicWarriorDeck;
	}

}
