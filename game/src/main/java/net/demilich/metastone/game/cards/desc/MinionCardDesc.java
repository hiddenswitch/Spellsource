package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;

/**
 * The additional fields specific to {@code "cardType": "MINION"} when creating JSON files corresponding to minions.
 *
 * @see ActorCardDesc for more fields.
 */
public class MinionCardDesc extends ActorCardDesc {
	/**
	 * The base attack of the minion. This will be the {@link Actor#getBaseAttack()} value.
	 */
	public int baseAttack;
	/**
	 * The base HP of the minion. This will be the {@link Actor#getBaseHp()} value.
	 */
	public int baseHp;

	@Override
	public Card createInstance() {
		return new MinionCard(this);
	}
}
