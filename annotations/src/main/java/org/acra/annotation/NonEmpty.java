package org.acra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author F43nd1r
 * @since 03.06.2017
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface NonEmpty {
}
