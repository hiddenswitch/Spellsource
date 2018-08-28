package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.HeroPowerSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.stream.Stream;

public class HeroPowerToSpellSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// TODO: Wrap the hero power effects in HeroPowerSpell
		// Retrieve the actual hero power effect
		Card heroPower = player.getHero().getHeroPower();

		TargetSelection selection = heroPower.getDesc().getTargetSelection();
		// Case 1: Check the spell
		Stream<SpellDesc> spells = heroPower.getSpell().spellStream(true);

		// Case 2: Check the passiveTrigger's spell
		EnchantmentDesc passiveTrigger = heroPower.getDesc().getPassiveTrigger();
		if (passiveTrigger != null) {
			spells = Stream.concat(spells, passiveTrigger.spell.spellStream(true));
		}

		// Find the HeroPowerSpell effects!
		SpellDesc[] heroPowerSpellEffect = spells
				.filter(s -> s.getDescClass().equals(HeroPowerSpell.class))
				.toArray(SpellDesc[]::new);

		SpellDesc allEffects = MetaSpell.create(heroPowerSpellEffect);
		Card spellCard;

		if (heroPower.getCardId().equals("hero_power_deaths_shadow")) {
			spellCard = context.getCardById("spell_shadow_reflection");
		} else {
			CardDesc spellCardDesc = new CardDesc();
			spellCardDesc.setId(context.getLogic().generateCardId());
			spellCardDesc.setName(heroPower.getName());
			spellCardDesc.setType(CardType.SPELL);
			spellCardDesc.setDescription(heroPower.getDescription());
			spellCardDesc.setRarity(heroPower.getRarity());
			spellCardDesc.setBaseManaCost(heroPower.getBaseManaCost());
			spellCardDesc.setTargetSelection(heroPower.getTargetSelection());
			spellCardDesc.setSpell(allEffects);
			spellCardDesc.setHeroClass(heroPower.getHeroClass());
			spellCardDesc.setSet(heroPower.getCardSet());
			spellCardDesc.setCollectible(false);
			spellCard = spellCardDesc.create();
		}
		context.getLogic().receiveCard(player.getId(), spellCard);
	}
}

