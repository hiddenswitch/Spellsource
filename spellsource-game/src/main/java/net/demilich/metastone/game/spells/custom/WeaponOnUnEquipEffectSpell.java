package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.List;

public class WeaponOnUnEquipEffectSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Entity> targets = new ArrayList<>();
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, 99);
		if (cards != null) {
			targets.addAll(cards);
		}
		if (target != null) {
			targets.add(target);
		}

		for (Entity entity : targets) {
			if (entity instanceof Weapon) {
				Weapon weapon = (Weapon) entity;
				Card card = weapon.getSourceCard();
				if (card.getDesc().getOnUnequip() != null) {
					SpellUtils.castChildSpell(context, player, card.getDesc().getOnUnequip(), source, source);
				}
			} else if (entity instanceof Card) {
				Card card = (Card) entity;
				if (GameLogic.isCardType(card.getCardType(), CardType.WEAPON)) {
					if (card.getDesc().getOnUnequip() != null) {
						SpellUtils.castChildSpell(context, player, card.getDesc().getOnUnequip(), source, source);
					}
				}

			}
		}
	}
}
