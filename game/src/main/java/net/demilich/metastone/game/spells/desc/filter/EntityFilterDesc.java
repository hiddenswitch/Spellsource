package net.demilich.metastone.game.spells.desc.filter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.EntityFilterDescDeserializer;

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
}
