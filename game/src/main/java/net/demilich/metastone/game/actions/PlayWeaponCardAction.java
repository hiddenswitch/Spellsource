package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.desc.OpenerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * An action that corresponds to playing a weapon card from the hand.
 */
public final class PlayWeaponCardAction extends PlayCardAction implements OpenerOverridable {
	private OpenerDesc opener;
	private boolean resolveOpener = true;

	public PlayWeaponCardAction(EntityReference EntityReference) {
		super(EntityReference);
		setActionType(ActionType.EQUIP_WEAPON);
	}

	public PlayWeaponCardAction(EntityReference reference, OpenerDesc opener) {
		super(reference);
		setActionType(ActionType.EQUIP_WEAPON);
		this.opener = opener;
	}

	@Override
	public PlayWeaponCardAction clone() {
		PlayWeaponCardAction clone = (PlayWeaponCardAction) super.clone();
		clone.opener = opener != null ? opener.clone() : null;
		return clone;
	}

	@Override
	@Suspendable
	public void innerExecute(GameContext context, int playerId) {
		Card weaponCard = (Card) context.resolveSingleTarget(getSourceReference());

		Weapon weapon = weaponCard.weapon();
		context.getLogic().equipWeapon(playerId, weapon, weaponCard, getResolveOpener());
	}

	@Override
	public OpenerDesc getOpener() {
		return opener;
	}

	@Override
	public void setOpener(OpenerDesc action) {
		opener = action;
	}

	@Override
	public void setResolveOpener(boolean resolveOpener) {
		this.resolveOpener = resolveOpener;
	}

	@Override
	public boolean getResolveOpener() {
		return resolveOpener;
	}
}
