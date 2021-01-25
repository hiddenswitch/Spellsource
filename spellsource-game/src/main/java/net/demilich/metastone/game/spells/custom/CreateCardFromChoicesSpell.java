package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.cards.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates a card from a sequence of discovers.
 */
public final class CreateCardFromChoicesSpell extends Spell {
	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(CreateCardFromChoicesSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter[] filters = (EntityFilter[]) desc.get(SpellArg.CARD_FILTERS);
		int howMany = filters.length;
		Multimap<Integer, Card> sourceCards = ArrayListMultimap.create();

		CardSource cardSource = desc.getCardSource();
		for (int i = 0; i < howMany; i++) {
			EntityFilter filter = filters[i];
			sourceCards.putAll(i, cardSource.getCards(context, source, player).filtered(filter.matcher(context, player, source)));
		}

		CardArrayList[] options = new CardArrayList[howMany];
		int discoverHowMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		for (int i = 0; i < howMany; i++) {
			options[i] = new CardArrayList();
			List<Card> cards = new ArrayList<>(sourceCards.get(i));
			for (int j = 0; j < discoverHowMany && !cards.isEmpty(); j++) {
				options[i].add(context.getLogic().removeRandom(cards));
			}
		}

		SpellDesc nullSpell = NullSpell.create();
		// Eyeroll emoji.
		nullSpell.put(SpellArg.SPELL, NullSpell.create());

		DiscoverAction[] chosen = new DiscoverAction[howMany];
		for (int i = 0; i < howMany; i++) {
			chosen[i] = SpellUtils.discoverCard(context, player, source, nullSpell, options[i]);
		}

		// According to https://us.battle.net/forums/en/hearthstone/topic/20759196602
		// Retrieve the first beast chosen, then apply the stats from the second beast as a buff
		// Except for the attack and health, that seems to just be baked in.
		Card card = chosen[0].getCard().getCopy();
		for (int j = 1; j < howMany; j++) {
			Card other = chosen[j].getCard().getCopy();
			card.getAttributes().put(Attribute.NAME, desc.getString(SpellArg.NAME));
			card.getAttributes().put(Attribute.DESCRIPTION, card.getDescription() + "\n" + other.getDescription());
			card.getAttributes().put(Attribute.ATTACK, card.getAttack() + other.getAttack());
			card.getAttributes().put(Attribute.BASE_ATTACK, card.getBaseAttack() + other.getBaseAttack());
			card.getAttributes().put(Attribute.HP, card.getHp() + other.getHp());
			card.getAttributes().put(Attribute.BASE_HP, card.getBaseHp() + other.getBaseHp());
			card.getAttributes().put(Attribute.BASE_MANA_COST, card.getBaseManaCost() + other.getBaseManaCost());
			for (Attribute attribute : other.getAttributes().unsafeKeySet()) {
				if (attribute == Attribute.ATTACK
						|| attribute == Attribute.HP
						|| attribute == Attribute.BASE_ATTACK
						|| attribute == Attribute.BASE_HP
						|| attribute == Attribute.BASE_MANA_COST) {
					continue;
				}

				card.getAttributes().put(attribute, other.getAttributes().get(attribute));
			}
		}


		// Then receive the card
		context.getLogic().receiveCard(player.getId(), card);
	}

}
