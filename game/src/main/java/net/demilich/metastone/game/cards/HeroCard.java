package net.demilich.metastone.game.cards;

import java.util.*;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayHeroCardAction;
import net.demilich.metastone.game.cards.desc.HeroCardDesc;
import net.demilich.metastone.game.cards.desc.WeaponCardDesc;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.spells.EquipWeaponSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;

public class HeroCard extends Card {
	private static final long serialVersionUID = 1L;

	protected static final Set<Attribute> inheritedAttributes = new HashSet<>(
			Arrays.asList(Attribute.HP, Attribute.MAX_HP, Attribute.BASE_HP, Attribute.ARMOR));

	protected HeroCard() {
		super();
	}

	public HeroCard(HeroCardDesc desc) {
		super(desc);
		setAttribute(Attribute.BASE_HP, getAttributeValue(Attribute.MAX_HP));
	}

	public Hero createHero() {
		final HeroCardDesc desc = (HeroCardDesc) getDesc();
		HeroPowerCard heroPower = (HeroPowerCard) CardCatalogue.getCardById(desc.heroPower);
		Hero hero = new Hero(this, heroPower);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (inheritedAttributes.contains(gameTag)) {
				hero.setAttribute(gameTag, getAttribute(gameTag));
			}
		}
		hero.setBattlecry(desc.getBattlecryAction());
		hero.setRace(desc.race);

		if (desc.deathrattle != null) {
			hero.getAttributes().remove(Attribute.DEATHRATTLES);
			hero.addDeathrattle(desc.deathrattle);
		}
		if (desc.trigger != null) {
			hero.addEnchantment(desc.trigger.create());
		}
		if (desc.triggers != null) {
			for (TriggerDesc trigger : desc.triggers) {
				hero.addEnchantment(trigger.create());
			}
		}

		return hero;
	}

	@Override
	public PlayCardAction play() {
		return new PlayHeroCardAction(getCardReference());
	}

	/**
	 * Gets the weapon equipped by a {@link EquipWeaponSpell} in this hero's battlecry.
	 *
	 * @return A weapon card, or {@code null} if none was found.
	 */
	public WeaponCard getWeapon() {
		HeroCardDesc heroDesc = (HeroCardDesc) getDesc();
		if (heroDesc.battlecry == null) {
			return null;
		}

		if (heroDesc.battlecry.spell == null) {
			return null;
		}

		// Return the first weapon we find equipped by the battlecry
		SpellDesc spell = heroDesc.battlecry.spell;
		return getWeaponCard(spell);
	}

	protected WeaponCard getWeaponCard(SpellDesc battlecry) {
		SpellDesc equipWeaponSpell = battlecry.subSpells()
				.filter(p -> p.getSpellClass().equals(EquipWeaponSpell.class))
				.findFirst().orElse(null);

		if (equipWeaponSpell == null) {
			return null;
		}

		String cardId = equipWeaponSpell.getString(SpellArg.CARD);
		if (cardId == null) {
			return null;
		}

		return (WeaponCard) CardCatalogue.getCardById(cardId);
	}

	@Override
	public CardType getCardType() {
		return CardType.HERO;
	}

	public int getArmor() {
		return getAttributeValue(Attribute.ARMOR);
	}
}

