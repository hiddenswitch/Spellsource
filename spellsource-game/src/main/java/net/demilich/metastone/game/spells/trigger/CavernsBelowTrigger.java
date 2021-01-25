package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.events.AfterSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public final class CavernsBelowTrigger extends AfterMinionPlayedTrigger {

	public CavernsBelowTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		boolean minionPlayed = super.innerQueues(event, enchantment, host);
		if (!minionPlayed) {
			return false;
		}

		AfterSummonEvent summonEvent = (AfterSummonEvent) event;
		GameContext context = event.getGameContext();
		EnvironmentEntityList list = EnvironmentEntityList.getList(context);
		list.add(host, summonEvent.getSource());
		// We're going to store the most recent max in the conditional attack bonus attribute
		// on our enchantment
		int max = (int) host.getAttributes().getOrDefault(Attribute.RESERVED_INTEGER_1, 0);
		Map<EntityReference, Entity> entities = context.getEntities()
				.filter(entity -> !GameLogic.isCardType(entity.getSourceCard().getCardType(), CardType.CHOOSE_ONE)
						&& entity.getEntityType() != EntityType.ENCHANTMENT)
				.collect(toMap(Entity::getReference, Function.identity()));
		Map<String, Long> counts = list.getReferences(host)
				.stream()
				.map(entities::get)
				.map(Entity::getSourceCard)
				.collect(groupingBy(Card::getName, Collectors.counting()));
		int newMax = counts.entrySet().stream()
				.max(Comparator.comparingLong(Map.Entry::getValue))
				.orElseThrow(RuntimeException::new).getValue().intValue();
		if (newMax > max) {
			host.getAttributes().put(Attribute.RESERVED_INTEGER_1, newMax);
			return true;
		} else {
			return false;
		}
	}
}
