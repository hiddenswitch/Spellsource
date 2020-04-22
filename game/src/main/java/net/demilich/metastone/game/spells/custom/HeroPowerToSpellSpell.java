package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.HeroPowerSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.cards.Attribute;

import java.util.stream.Stream;

/**
 * Turns a hero power into a spell card. Finds the {@link HeroPowerSpell} specified on the card, regardless if it is a
 * passive hero power or not.
 * <p>
 * Implements Little Helper.
 */
public final class HeroPowerToSpellSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// TODO: Wrap the hero power effects in HeroPowerSpell
		// Retrieve the actual hero power effect
		Card heroPower = player.getHeroPowerZone().get(0);

		TargetSelection selection = heroPower.getDesc().getTargetSelection();
		// Case 1: Check the spell
		Stream<SpellDesc> spells = Stream.empty();
		if (heroPower.getSpell() != null) {
			spells = heroPower.getSpell().spellStream(true);
		}

		// Case 2: Check the passiveTrigger's spell
		EnchantmentDesc passiveTrigger = heroPower.getDesc().getPassiveTrigger();
		if (passiveTrigger != null) {
			spells = Stream.concat(spells, passiveTrigger.getSpell().spellStream(true));
		}

		// Find the HeroPowerSpell effects!
		SpellDesc[] heroPowerSpellEffect = spells
				.filter(s -> s.getDescClass().equals(HeroPowerSpell.class))
				.toArray(SpellDesc[]::new);

		String descriptionOverride = "";
		String cardOverride = "";
		for (SpellDesc spellDesc : heroPowerSpellEffect) {
			if (spellDesc.get(SpellArg.DESCRIPTION) != null) {
				descriptionOverride = spellDesc.getString(SpellArg.DESCRIPTION);
			}
			if (spellDesc.get(SpellArg.CARD) != null) {
				cardOverride = spellDesc.getString(SpellArg.CARD);
			}
		}

		SpellDesc allEffects = MetaSpell.create(heroPowerSpellEffect);
		Card spellCard;

		if (!cardOverride.isEmpty()) {
			spellCard = context.getCardById(cardOverride);
		} else {
			CardDesc spellCardDesc = new CardDesc();
			if (heroPower.isChooseOne()) {
				spellCardDesc.setType(CardType.CHOOSE_ONE);
				spellCardDesc.setChooseOneCardIds(heroPower.getChooseOneCardIds());
				spellCardDesc.setChooseBothCardId(heroPower.getChooseBothCardId());
			} else {
				spellCardDesc.setType(CardType.SPELL);
				spellCardDesc.setSpell(allEffects);
			}
			spellCardDesc.setId(context.getLogic().generateCardId());
			spellCardDesc.setName(heroPower.getName());
			spellCardDesc.setDescription(descriptionOverride.isEmpty() ? heroPower.getDescription() : descriptionOverride);
			spellCardDesc.setRarity(heroPower.getRarity());
			spellCardDesc.setBaseManaCost(heroPower.getBaseManaCost());
			spellCardDesc.setTargetSelection(heroPower.getTargetSelection());
			spellCardDesc.setHeroClass(heroPower.getHeroClass());
			spellCardDesc.setSet(heroPower.getCardSet());
			spellCardDesc.setCollectible(false);
			spellCard = spellCardDesc.create();
			context.addTempCard(spellCard);
			if (heroPower.hasAttribute(Attribute.LIFESTEAL)) {
				spellCard.setAttribute(Attribute.LIFESTEAL);
			}

		}
		context.getLogic().receiveCard(player.getId(), spellCard);
	}
}


