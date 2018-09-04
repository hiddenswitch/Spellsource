package net.demilich.metastone.game.actions;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.targeting.EntityReference;

public class PlayWeaponCardAction extends PlayCardAction implements HasBattlecry {
	private BattlecryAction battlecry;

	private PlayWeaponCardAction() {
		setTargetReference(EntityReference.NONE);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	public PlayWeaponCardAction(EntityReference EntityReference) {
		super(EntityReference);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	public PlayWeaponCardAction(EntityReference reference, BattlecryAction battlecry) {
		super(reference);
		setActionType(ActionType.EQUIP_WEAPON);
		this.battlecry = battlecry;
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		Card weaponCard = (Card) context.resolveSingleTarget(getSourceReference());

		Weapon weapon = weaponCard.createWeapon();
		if (battlecry != null) {
			weapon.setBattlecry(battlecry);
		}
		context.getLogic().equipWeapon(playerId, weapon, weaponCard, true);
	}

	@Override
	public BattlecryAction getBattlecryAction() {
		return battlecry;
	}

	@Override
	public void setBattlecryAction(BattlecryAction action) {
		battlecry = action;
	}
}
