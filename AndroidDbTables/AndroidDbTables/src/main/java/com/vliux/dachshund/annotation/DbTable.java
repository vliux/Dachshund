package com.vliux.dachshund.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by vliux on 12/8/13.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DbTable {
    String tableName();

    /**
     * If a table is added at db versoin N, should assign minVersion to N here.
     * Otherwise Dachshund won't be able to detect the newly-added table.
     * @return
     */
    int minVersion();
}
