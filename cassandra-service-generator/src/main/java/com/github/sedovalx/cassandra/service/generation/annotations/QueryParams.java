package com.github.sedovalx.cassandra.service.generation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Alexander
 * on 02.01.2016.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface QueryParams {
    String consistency() default "";
    int fetchSize() default -1;
    boolean tracing() default false;
}
