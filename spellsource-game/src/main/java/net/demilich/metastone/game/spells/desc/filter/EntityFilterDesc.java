package net.demilich.metastone.game.spells.desc.filter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.EntityFilterDescDeserializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = EntityFilterDescDeserializer.class)
public class EntityFilterDesc extends Desc<EntityFilterArg, EntityFilter> {

	public EntityFilterDesc() {
		super(EntityFilterArg.class);
	}

	public EntityFilterDesc(Class<? extends EntityFilter> filterClass) {
		super(filterClass, EntityFilterArg.class);
	}

	public EntityFilterDesc(Map<EntityFilterArg, Object> arguments) {
		super(arguments, EntityFilterArg.class);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return EntityFilterDesc.class;
	}

	@Override
	public EntityFilterArg getClassArg() {
		return EntityFilterArg.CLASS;
	}

	@Override
	public EntityFilterDesc clone() {
		return (EntityFilterDesc) copyTo(new EntityFilterDesc(getDescClass()));
	}

	public List<String> getCardOrCards() {
		if (getDesc().containsKey(EntityFilterArg.CARD) && ! getDesc().containsKey(EntityFilterArg.CARDS)) {
			return Collections.singletonList((String)get(EntityFilterArg.CARD));
		}
		if (!getDesc().containsKey(EntityFilterArg.CARD) && getDesc().containsKey(EntityFilterArg.CARDS)) {
			return Arrays.asList((String[])get(EntityFilterArg.CARDS));
		}
		if (getDesc().containsKey(EntityFilterArg.CARD) && getDesc().containsKey(EntityFilterArg.CARDS)) {
			String[] cards = (String[])get(EntityFilterArg.CARDS);
			String[] newCards = Arrays.copyOf(cards,cards.length+1);
			newCards[cards.length] = (String)get(EntityFilterArg.CARD);
			return Arrays.asList(newCards);
		}
		return Collections.emptyList();
	}
}
