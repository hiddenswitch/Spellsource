package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.targeting.EntityReference;

public class PlayWeaponCardAction extends PlayCardAction {
	private PlayWeaponCardAction() {
		setTargetReference(EntityReference.NONE);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	public PlayWeaponCardAction(EntityReference EntityReference) {
		super(EntityReference);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	@Override
	@Suspendable
	public void play(GameContext context, int playerId) {
		WeaponCard weaponCard = (WeaponCard) context.getPendingCard();
		context.getLogic().equipWeapon(playerId, weaponCard.getWeapon(), weaponCard, true);
	}

}
