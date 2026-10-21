package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Package-level metadata declaring common/envelope arguments and parameter mixin bindings for
 * {@link Spell}, {@link net.demilich.metastone.game.spells.desc.filter.EntityFilter},
 * {@link net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider},
 * {@link net.demilich.metastone.game.spells.desc.condition.Condition}, and
 * {@link net.demilich.metastone.game.spells.trigger.EventTrigger} components.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface ComponentPackage {
	SpellArg[] commonSpellArgs() default {};

	EntityFilterArg[] commonFilterArgs() default {};

	ValueProviderArg[] commonValueProviderArgs() default {};

	ConditionArg[] commonConditionArgs() default {};

	EventTriggerArg[] commonTriggerArgs() default {};

	Mixin[] mixins() default {};

	@Retention(RetentionPolicy.RUNTIME)
	@Target({})
	@interface Mixin {
		String parameterName();

		Class<?> parameterType();

		Class<?> helperClass();

		String helperMethod();

		String[] consumedArgs();
	}
}
