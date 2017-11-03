package net.demilich.metastone.game.cards;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionCard extends Card {
	private static Logger logger = LoggerFactory.getLogger(MinionCard.class);
	private static final Set<Attribute> ignoredAttributes = new HashSet<Attribute>(
			Arrays.asList(Attribute.PASSIVE_TRIGGERS, Attribute.DECK_TRIGGER, Attribute.MANA_COST_MODIFIER, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO));

	@SerializedName("minionDesc")
	private final MinionCardDesc desc;

	public MinionCard(MinionCardDesc desc) {
		super(desc);
		setAttribute(Attribute.BASE_ATTACK, desc.baseAttack);
		setAttribute(Attribute.ATTACK, desc.baseAttack);
		setAttribute(Attribute.BASE_HP, desc.baseHp);
		setAttribute(Attribute.HP, desc.baseHp);
		setAttribute(Attribute.MAX_HP, desc.baseHp);
		if (desc.race != null) {
			setRace(desc.race);
		}
		this.desc = desc;
	}

	protected Minion createMinion(Attribute... tags) {
		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		minion.setBaseAttack(getBaseAttack());
		minion.setAttack(getAttack());
		minion.setHp(getHp());
		minion.setMaxHp(getHp());
		minion.setBaseHp(getBaseHp());
		minion.setBattlecry(desc.getBattlecryAction());

		if (desc.deathrattle != null) {
			minion.getAttributes().remove(Attribute.DEATHRATTLES);
			minion.addDeathrattle(desc.deathrattle);
		}
		if (desc.trigger != null) {
			minion.addEnchantment(desc.trigger.create());
		}
		if (desc.triggers != null) {
			for (TriggerDesc trigger : desc.triggers) {
				minion.addEnchantment(trigger.create());
			}
		}
		if (desc.aura != null) {
			final Aura enchantment = desc.aura.create();
			if (enchantment == null) {
				logger.error("Failed to create an aura for minion!. cardId {}", minion.getSourceCard().getCardId());
			} else {
				minion.addEnchantment(enchantment);
			}
		}
		if (desc.cardCostModifier != null) {
			minion.setCardCostModifier(desc.cardCostModifier.create());
		}
		minion.setHp(minion.getMaxHp());
		return minion;
	}

	public int getAttack() {
		return getAttributeValue(Attribute.ATTACK);
	}

	public int getBonusAttack() {
		return getAttributeValue(Attribute.ATTACK_BONUS);
	}

	public int getHp() {
		return getAttributeValue(Attribute.HP);
	}

	public int getBonusHp() {
		return getAttributeValue(Attribute.HP_BONUS);
	}

	public int getBaseAttack() {
		return getAttributeValue(Attribute.BASE_ATTACK);
	}

	public int getBaseHp() {
		return getAttributeValue(Attribute.BASE_HP);
	}

	@Override
	public PlayCardAction play() {
		return new PlayMinionCardAction(getCardReference());
	}

	public void setRace(Race race) {
		setAttribute(Attribute.RACE, race);
	}

	public Minion summon() {
		return createMinion();
	}

	public boolean hasTrigger() {
		return desc.trigger != null || (desc.triggers != null && desc.triggers.length > 0);
	}

	public boolean hasAura() {
		return desc.aura != null;
	}

	public boolean hasCardCostModifier() {
		return desc.cardCostModifier != null;
	}

	public boolean hasBattlecry() {
		return desc.battlecry != null;
	}

	public boolean hasDeathrattle() {
		return desc.deathrattle != null;
	}
}
