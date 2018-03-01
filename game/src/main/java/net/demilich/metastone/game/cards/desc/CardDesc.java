package net.demilich.metastone.game.cards.desc;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;
import net.demilich.metastone.game.utils.AttributeMap;

public abstract class CardDesc implements Serializable, Cloneable {
	public String id;
	public String name;
	public String description;
	public Boolean legacy;
	public CardType type;
	public HeroClass heroClass;
	public HeroClass[] heroClasses;
	public Rarity rarity;
	public CardSet set;
	public int baseManaCost;
	public boolean collectible = true;
	public AttributeMap attributes;
	public int fileFormatVersion = 1;
	public ValueProviderDesc manaCostModifier;
	public TriggerDesc passiveTrigger;
	public TriggerDesc[] passiveTriggers;
	public TriggerDesc deckTrigger;
	public TriggerDesc[] gameTriggers;

	public abstract Card createInstance();

	public boolean getCollectible() {
		return collectible;
	}
}
