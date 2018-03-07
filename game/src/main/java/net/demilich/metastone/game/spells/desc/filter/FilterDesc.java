package net.demilich.metastone.game.spells.desc.filter;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.demilich.metastone.game.cards.desc.Desc;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class FilterDesc extends Desc<FilterArg, EntityFilter> {

	public FilterDesc() {
		super();
	}

	public FilterDesc(Class<? extends EntityFilter> filterClass) {
		super(filterClass);
	}

	public FilterDesc(Map<FilterArg, Object> arguments) {
		super(arguments);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return FilterDesc.class;
	}

	@Override
	public FilterArg getClassArg() {
		return FilterArg.CLASS;
	}

	@Override
	public FilterDesc clone() {
		return (FilterDesc)copyTo(new FilterDesc(getDescClass()));
	}
}
