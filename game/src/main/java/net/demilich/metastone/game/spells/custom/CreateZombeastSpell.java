package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class CreateZombeastSpell extends Spell {
	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(CreateZombeastSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Map<Integer, List<MinionCard>> cards = CardCatalogue.stream()
				.filter(c -> c.getRace() == Race.BEAST)
				.filter(c -> c.getCardType() == CardType.MINION)
				.filter(c -> c.getHeroClass() == HeroClass.ANY || c.getHeroClass() == HeroClass.HUNTER)
				.filter(c -> c.getBaseManaCost() <= 5)
				.filter(c -> context.getDeckFormat().isInFormat(c))
				.map(c -> (MinionCard) c)
				.collect(groupingBy(c -> {
					if (c.hasAura()
							|| c.hasBattlecry()
							|| c.hasCardCostModifier()
							|| c.hasDeathrattle()
							|| c.hasTrigger()) {
						return 0;
					} else {
						return 1;
					}
				}));

		CardArrayList[] options = new CardArrayList[]{
				new CardArrayList(), new CardArrayList()
		};
		for (int i = 0; i < 2; i++) {
			List<MinionCard> minionCards = cards.get(i);
			minionCards.stream().filter(new Selector(minionCards.size(), 3, context.getLogic().getRandom()))
					.forEach(options[i]::addCard);
		}

		SpellDesc nullSpell = new SpellDesc(SpellDesc.build(NullSpell.class));
		// Eyeroll emoji.
		nullSpell.put(SpellArg.SPELL, new SpellDesc(SpellDesc.build(NullSpell.class)));

		List<DiscoverAction> chosen =
				IntStream.range(0, 2).mapToObj(i -> SpellUtils.discoverCard(context, player, nullSpell, options[i]))
						.collect(toList());


		// According to https://us.battle.net/forums/en/hearthstone/topic/20759196602
		// Retrieve the first beast chosen, then apply the stats from the second beast as a buff
		// Except for the attack and health, that seems to just be baked in.
		MinionCard card = (MinionCard) chosen.get(0).getCard().getCopy();
		MinionCard other = (MinionCard) chosen.get(1).getCard().getCopy();
		card.setName("Zombeast");
		card.setDescription(card.getDescription() + "\n" + other.getDescription());
		card.getAttributes().put(Attribute.BASE_ATTACK, card.getAttack() + other.getAttack());
		card.getAttributes().put(Attribute.BASE_HP, card.getHp() + other.getHp());
		card.getAttributes().put(Attribute.BASE_MANA_COST, card.getBaseManaCost() + other.getBaseManaCost());
		for (Attribute attribute : other.getAttributes().keySet()) {
			if (attribute == Attribute.BASE_ATTACK
					|| attribute == Attribute.BASE_HP
					|| attribute == Attribute.BASE_MANA_COST) {
				continue;
			}

			card.getAttributes().put(attribute, other.getAttributes().get(attribute));
		}

		// Then receive the card
		context.getLogic().receiveCard(player.getId(), card);
	}

	/**
	 * A stateful predicate that, given a total number of items and the number to choose, will return 'true' the chosen
	 * number of times distributed randomly across the total number of calls to its test() method.
	 */
	static class Selector implements Predicate<Object> {
		int total;  // total number items remaining
		int remain; // number of items remaining to select
		Random random;

		Selector(int total, int remain, Random random) {

			this.total = total;
			this.remain = remain;
			this.random = random;
		}

		@Override
		public synchronized boolean test(Object o) {
			assert total > 0;
			if (random.nextInt(total--) < remain) {
				remain--;
				return true;
			} else {
				return false;
			}
		}
	}
}
