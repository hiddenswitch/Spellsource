package net.demilich.metastone.game.spells.desc.valueprovider;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.ValueProviderDescDeserializer;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.EnumMap;
import java.util.Map;

/**
 * This instance stores the definition of a {@link ValueProvider}.
 */
@JsonDeserialize(using = ValueProviderDescDeserializer.class)
public class ValueProviderDesc extends Desc<ValueProviderArg, ValueProvider> {

	public ValueProviderDesc() {
		super(ValueProviderArg.class);
	}

	public static Map<ValueProviderArg, Object> build(Class<? extends ValueProvider> providerClass) {
		final Map<ValueProviderArg, Object> arguments = new EnumMap<>(ValueProviderArg.class);
		arguments.put(ValueProviderArg.CLASS, providerClass);
		return arguments;
	}

	public ValueProviderDesc(Map<ValueProviderArg, Object> arguments) {
		super(arguments, ValueProviderArg.class);
	}

	public ValueProviderDesc(Class<? extends ValueProvider> vpClass) {
		super(vpClass, ValueProviderArg.class);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return ValueProviderDesc.class;
	}

	@Override
	public ValueProviderArg getClassArg() {
		return ValueProviderArg.CLASS;
	}

	@Override
	public ValueProviderDesc clone() {
		return (ValueProviderDesc) copyTo(new ValueProviderDesc(getDescClass()));
	}

	public EntityReference getTarget() {
		return (EntityReference) get(ValueProviderArg.TARGET);
	}
}
