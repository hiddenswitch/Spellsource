package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayHeroCardAction;
import net.demilich.metastone.game.cards.desc.ChooseBattlecryHeroCardDesc;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class ChooseBattlecryHeroCard extends HeroCard implements HasChooseOneActions {
	private final BattlecryDesc[] battlecryOptions;
	private final BattlecryDesc battlecryBothOptions;

	public ChooseBattlecryHeroCard(ChooseBattlecryHeroCardDesc desc) {
		super(desc);
		this.battlecryOptions = desc.options;
		this.battlecryBothOptions = desc.bothOptions;
		setAttribute(Attribute.CHOOSE_ONE);
	}

	public String getBattlecryDescription(int index) {
		if (index < 0 || index >= battlecryOptions.length) {
			return null;
		}
		if (battlecryOptions[index] == null) {
			return null;
		}
		return battlecryOptions[index].description;
	}

	@Override
	public PlayCardAction[] playOptions() {
		PlayCardAction[] actions = new PlayCardAction[battlecryOptions.length];
		for (int i = 0; i < battlecryOptions.length; i++) {
			PlayCardAction option = new PlayHeroCardAction(getCardReference(), battlecryOptions[i].toBattlecryAction());
			option.setChooseOneOptionIndex(i);
			actions[i] = option;
		}
		return actions;
	}

	@Override
	public PlayCardAction playBothOptions() {
		return new PlayHeroCardAction(getCardReference(), battlecryBothOptions.toBattlecryAction());
	}

	@Override
	public boolean hasBothOptions() {
		return battlecryBothOptions != null;
	}

	@Override
	protected WeaponCard getWeaponCard(SpellDesc battlecry) {
		if (hasBothOptions()) {
			return super.getWeaponCard(battlecryBothOptions.spell);
		} else {
			for (BattlecryDesc battlecryOption : battlecryOptions) {
				WeaponCard card = super.getWeaponCard(battlecryOption.spell);
				if (card != null) {
					return card;
				}
			}
		}
		return null;
	}
}
