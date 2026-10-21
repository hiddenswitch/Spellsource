package net.demilich.metastone.game.cards;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the storage tier for an {@link Attribute} in {@link AttributeMap}.
 * <ul>
 *   <li>{@link Type#INLINE_INT}: Stored in an inline primitive {@code int} slot on {@link AttributeMap}.</li>
 *   <li>{@link Type#INLINE_BOOL}: Stored in an inline {@code long} bitset slot on {@link AttributeMap}.</li>
 *   <li>{@link Type#OVERFLOW}: Stored in the lazy popcount-compressed overflow map when accessed.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tier {
	enum Type {
		INLINE_INT,
		INLINE_BOOL,
		INLINE_OBJECT,
		OVERFLOW
	}

	Type value() default Type.OVERFLOW;
}
