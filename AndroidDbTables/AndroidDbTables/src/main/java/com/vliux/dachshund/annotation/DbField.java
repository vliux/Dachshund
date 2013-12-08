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

    /**
     * If you don't value minVersion for a field, Dachshund will try to obtain it from DbTable annotation.
     * Db version should start at 1.
     * @return
     */
    int minVersion() default -1;
}
