package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.ChooseBattlecryCardDesc;
import net.demilich.metastone.game.spells.TransformMinionSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class ChooseBattlecryMinionCard extends MinionCard implements HasChooseOneActions {
	private final BattlecryDesc[] battlecryOptions;
	private final BattlecryDesc battlecryBothOptions;

	public ChooseBattlecryMinionCard(ChooseBattlecryCardDesc desc) {
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

	public String getTransformMinionCardId(int index) {
		BattlecryDesc battlecryOption = battlecryOptions[index];
		if (battlecryOption == null) {
			return null;
		}

		SpellDesc spell = battlecryOption.spell;
		if (spell == null) {
			return null;
		}

		if (TransformMinionSpell.class.isAssignableFrom(spell.getSpellClass())) {
			return spell.getString(SpellArg.CARD);
		}

		return null;
	}

	public boolean hasBothOptions() {
		return battlecryBothOptions != null;
	}

	@Override
	public PlayCardAction[] playOptions() {
		PlayCardAction[] actions = new PlayCardAction[battlecryOptions.length];
		for (int i = 0; i < battlecryOptions.length; i++) {
			BattlecryAction battlecry = battlecryOptions[i].toBattlecryAction();
			PlayCardAction option = new PlayMinionCardAction(getReference(), battlecry);
			option.setChooseOneOptionIndex(i);
			actions[i] = option;
		}
		return actions;
	}

	@Override
	public PlayCardAction playBothOptions() {
		BattlecryDesc battlecryOption = battlecryBothOptions;
		BattlecryAction battlecry = BattlecryAction.createBattlecry(battlecryOption.spell, battlecryOption.getTargetSelection());
		PlayCardAction option = new PlayMinionCardAction(getReference(), battlecry);
		return option;
	}
}
