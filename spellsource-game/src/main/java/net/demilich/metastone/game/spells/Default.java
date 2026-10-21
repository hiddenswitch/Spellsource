package net.demilich.metastone.game.spells;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the default integer or string value for a typed {@code onCast}, {@code test}, {@code provideValue},
 * or {@code isFulfilled} parameter when the corresponding {@link net.demilich.metastone.game.cards.desc.Desc}
 * argument is omitted from the card JSON.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {
	int value() default 0;

	String string() default "";

	boolean bool() default false;
}
