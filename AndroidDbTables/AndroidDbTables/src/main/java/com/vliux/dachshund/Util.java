package com.vliux.dachshund;

import java.lang.reflect.Field;

/**
 * Created by vliux on 1/18/14.
 */
public class Util {
    public static String getColumnName(Field field){
        String fieldValue = null;
        try {
            field.setAccessible(true);
            fieldValue = (String) field.get(null);
            return fieldValue;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
