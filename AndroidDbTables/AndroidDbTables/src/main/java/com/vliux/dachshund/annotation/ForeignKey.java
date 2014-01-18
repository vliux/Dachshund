package com.vliux.dachshund.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by vliux on 1/18/14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey {
    Class referTo();

    /**
     * If you don't value minVersion for a field, Dachshund will try to obtain it from DbTable annotation.
     * Db version should start at 1.
     * @return
     */
    int minVersion() default -1;
}
