package net.demilich.metastone.game.spells;

import java.util.*;
import java.util.stream.Collectors;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardDescType;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

public class CreateCardSpell extends Spell {

	Logger logger = LoggerFactory.getLogger(CreateCardSpell.class);

	@Suspendable
	private SpellDesc[] discoverCardParts(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<SpellDesc> spells = new ArrayList<SpellDesc>();
		SpellDesc[] spellArray = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		for (SpellDesc spell : spellArray) {
			spells.add(spell);
		}

		Map<SpellDesc, Integer> spellOrder = new HashMap<SpellDesc, Integer>();
		for (int i = 0; i < spells.size(); i++) {
			SpellDesc spell = spells.get(i);
			spellOrder.put(spell, i);
		}

		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		int value = desc.getValue(SpellArg.SECONDARY_VALUE, context, player, target, source, 2);
		boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);
		List<Integer> chosenSpellInts = new Vector<>();
		List<DiscoverAction> discoveries = new Vector<>();
		List<SpellDesc> shuffledSpells = new Vector<>(spells);
		for (int i = 0; i < value; i++) {
			Collections.shuffle(shuffledSpells);
			List<SpellDesc> spellChoices = shuffledSpells.stream().limit(count).collect(Collectors.toList());
			if (spellChoices.isEmpty()) {
				continue;
			}

			final DiscoverAction spellDiscover = SpellUtils.getSpellDiscover(context, player, desc, spellChoices, source);
			SpellDesc chosenSpell = spellDiscover.getSpell();
			chosenSpellInts.add(spellOrder.get(chosenSpell));
			discoveries.add(spellDiscover);

			if (exclusive) {
				shuffledSpells.removeIf(f -> f.equals(chosenSpell));
			}
		}
		Collections.sort(chosenSpellInts);
		SpellDesc[] chosenSpells = new SpellDesc[chosenSpellInts.size()];
		for (int i = 0; i < chosenSpellInts.size(); i++) {
			chosenSpells[i] = spellArray[chosenSpellInts.get(i)];
		}
		return chosenSpells;
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		HeroClass heroClass = HeroClass.ANY;
		Rarity rarity = Rarity.FREE;
		CardSet cardSet = CardSet.BASIC;
		SpellDesc[] spells = discoverCardParts(context, player, desc, source, target);
		switch (source.getEntityType()) {
			case ANY:
				break;
			case CARD:
				break;
			case HERO:
				break;
			case MINION:
				Minion sourceMinion = (Minion) source;
				heroClass = sourceMinion.getSourceCard().getHeroClass();
				rarity = Rarity.FREE;
				cardSet = sourceMinion.getSourceCard().getCardSet();
				break;
			case PLAYER:
				break;
			case WEAPON:
				break;
			default:
				break;
		}
		Card newCard = null;
		switch ((CardType) desc.get(SpellArg.CARD_TYPE)) {
			case SPELL:
				List<SpellDesc> spellList = new ArrayList<SpellDesc>();
				String description = "";
				TargetSelection targetSelection = TargetSelection.NONE;
				for (SpellDesc spell : spells) {
					CardDescType cardDescType = (CardDescType) spell.get(SpellArg.CARD_DESC_TYPE);
					if (cardDescType == CardDescType.SPELL) {
						description += spell.getString(SpellArg.DESCRIPTION) + " ";
						spellList.add(spell);
						TargetSelection checkTS = (TargetSelection) spell.get(SpellArg.TARGET_SELECTION);
						if (checkTS != null && checkTS.compareTo(targetSelection) > 0) {
							targetSelection = checkTS;
						}
					}
				}
				SpellDesc[] spellArray = new SpellDesc[spellList.size()];
				spellList.toArray(spellArray);
				SpellDesc spell = MetaSpell.create(target != null ? target.getReference() : null, false, spellArray);
				SpellCardDesc spellCardDesc = new SpellCardDesc();
				spellCardDesc.id = context.getLogic().generateCardId();
				spellCardDesc.name = desc.getString(SpellArg.SECONDARY_NAME);
				spellCardDesc.heroClass = heroClass;
				spellCardDesc.type = CardType.SPELL;
				spellCardDesc.rarity = rarity;
				spellCardDesc.description = description;
				spellCardDesc.targetSelection = targetSelection;
				spellCardDesc.spell = spell;
				//spellCardDesc.attributes.put(key, value);
				spellCardDesc.set = cardSet;
				spellCardDesc.collectible = false;
				spellCardDesc.baseManaCost = desc.getValue(SpellArg.MANA, context, player, target, source, 0);
				newCard = spellCardDesc.createInstance();
				break;
			case CHOOSE_ONE:
			case HERO_POWER:
			case MINION:
			case WEAPON:
			default:
				return;
		}
		if (newCard != null) {
			context.addTempCard(newCard);
			context.getLogic().receiveCard(player.getId(), newCard.clone());
		}
	}
}
