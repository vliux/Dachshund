package com.vliux.dachshund.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by vliux on 12/7/13.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbField {
    String columnType() default "TEXT";
    String defaultValue() default "";
    int minVersion() default 1;
}
