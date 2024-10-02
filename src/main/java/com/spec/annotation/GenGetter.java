package com.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
public @interface GenGetter {
    /**
     * Will generate a for all fields in class if placed on class level.
     * To generate only for a field place the annotation on it.
     */
}
