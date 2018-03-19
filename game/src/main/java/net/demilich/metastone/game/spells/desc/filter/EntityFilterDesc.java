package net.demilich.metastone.game.spells.desc.filter;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.EntityFilterDescDeserializer;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = EntityFilterDescDeserializer.class)
public class EntityFilterDesc extends Desc<FilterArg, EntityFilter> {

	public EntityFilterDesc() {
		super();
	}

	public EntityFilterDesc(Class<? extends EntityFilter> filterClass) {
		super(filterClass);
	}

	public EntityFilterDesc(Map<FilterArg, Object> arguments) {
		super(arguments);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return EntityFilterDesc.class;
	}

	@Override
	public FilterArg getClassArg() {
		return FilterArg.CLASS;
	}

	@Override
	public EntityFilterDesc clone() {
		return (EntityFilterDesc)copyTo(new EntityFilterDesc(getDescClass()));
	}
}
