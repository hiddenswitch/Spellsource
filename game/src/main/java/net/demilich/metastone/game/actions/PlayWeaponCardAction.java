package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * An action that corresponds to playing a weapon card from the hand.
 */
public class PlayWeaponCardAction extends PlayCardAction implements HasBattlecry {
	private BattlecryDesc battlecry;

	private PlayWeaponCardAction() {
		setTargetReference(EntityReference.NONE);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	public PlayWeaponCardAction(EntityReference EntityReference) {
		super(EntityReference);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	public PlayWeaponCardAction(EntityReference reference, BattlecryDesc battlecry) {
		super(reference);
		setActionType(ActionType.EQUIP_WEAPON);
		this.battlecry = battlecry;
	}

	@Override
	public PlayWeaponCardAction clone() {
		PlayWeaponCardAction clone = (PlayWeaponCardAction) super.clone();
		clone.battlecry = battlecry != null ? battlecry.clone() : null;
		return clone;
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
	public BattlecryDesc getBattlecry() {
		return battlecry;
	}

	@Override
	public void setBattlecry(BattlecryDesc action) {
		battlecry = action;
	}
}
