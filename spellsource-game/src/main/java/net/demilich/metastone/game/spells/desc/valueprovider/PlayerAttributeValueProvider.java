package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.PlayerAttribute;
import net.demilich.metastone.game.statistics.Statistic;
import net.demilich.metastone.game.cards.Attribute;

public class PlayerAttributeValueProvider extends ValueProvider {

	public PlayerAttributeValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		PlayerAttribute attribute = (PlayerAttribute) getDesc().get(ValueProviderArg.PLAYER_ATTRIBUTE);
		switch (attribute) {
			case DECK_COUNT:
				return player.getDeck().getCount();
			case HAND_COUNT:
				return player.getHand().getCount();
			case HERO_POWER_USED:
				return (int) player.getStatistics().getLong(Statistic.HERO_POWER_USED);
			case LAST_MANA_COST:
				return (int) context.getEnvironment().get(Environment.LAST_MANA_COST);
			case MANA:
				return player.getMana();
			case MAX_MANA:
				return player.getMaxMana();
			case SECRET_COUNT:
				return player.getSecrets().size();
			case CARDS_DRAWN:
				return (int) player.getStatistics().getLong(Statistic.CARDS_DRAWN);
			case SPELLS_CAST:
				return (int) player.getStatistics().getLong(Statistic.SPELLS_CAST);
			case CARDS_DISCARDED:
				return (int) player.getStatistics().getLong(Statistic.CARDS_DISCARDED);
			case INVOKED_CARDS:
				return (int) player.getAttributes().getOrDefault(Attribute.INVOKED, 0);
			case OVERLOADED_THIS_GAME:
				return (int) player.getAttributes().getOrDefault(Attribute.OVERLOADED_THIS_GAME, 0);
			case DAMAGE_THIS_TURN:
				return (int) player.getAttributeValue(Attribute.DAMAGE_THIS_TURN);
			case SUPREMACIES_THIS_GAME:
				return (int) player.getAttributeValue(Attribute.SUPREMACIES_THIS_GAME);
			case LOCKED_MANA:
				return player.getLockedMana();
			case HERO_POWER_DAMAGE_DEALT:
				return (int) player.getStatistics().getLong(Statistic.HERO_POWER_DAMAGE_DEALT);
			case HEALING_DONE:
				return (int) player.getStatistics().getLong(Statistic.HEALING_DONE);
			case ARMOR_LOST:
				return (int) player.getStatistics().getLong(Statistic.ARMOR_LOST);
			default:
				break;
		}
		return 0;
	}

	public static ValueProvider create(PlayerAttribute attribute) {
		ValueProviderDesc desc = new ValueProviderDesc(PlayerAttributeValueProvider.class);
		desc.put(ValueProviderArg.PLAYER_ATTRIBUTE, attribute);
		return desc.create();
	}
}
