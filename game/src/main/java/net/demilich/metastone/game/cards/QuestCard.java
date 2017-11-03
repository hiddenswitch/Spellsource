package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.QuestCardDesc;
import net.demilich.metastone.game.spells.AddQuestSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.targeting.TargetSelection;

public class QuestCard extends SpellCard {

	public QuestCard(QuestCardDesc desc) {
		super(desc);
		EventTrigger trigger = desc.quest.create();
		setQuest(new Quest(trigger, desc.spell, this, desc.countUntilCast));
		setAttribute(Attribute.QUEST);
	}

	public boolean canBeCast(GameContext context, Player player) {
		return context.getLogic().canPlayQuest(player, this);
	}

	public void setQuest(Quest quest) {
		SpellDesc spell = AddQuestSpell.create(quest);
		setTargetRequirement(TargetSelection.NONE);
		setSpell(spell);
	}

}
