package com.vliux.dachshund;

import android.content.ContentValues;
import android.util.Log;

import com.vliux.dachshund.bean.DbColumnDef;

import java.lang.reflect.Field;

/**
 * Created by vliux on 12/10/13.
 */
public class DbUtil {

    /**
     * Set the ContentValues according to values in BaseDbTable object.
     * @param table
     * @return
     */
    public static ContentValues getColumnValues(BaseDbTable table){
        ContentValues cv = new ContentValues();
        for (Field field : table.getColumnDefinitions().keySet()) {
            DbColumnDef columnDef = table.getColumnDefinitions().get(field);
            try {
                DbUtil.setColumnValue(cv, columnDef, field.get(table));
            } catch (IllegalAccessException e) {
                Log.e(DbManager.TAG,
                        String.format("failed to get value of field %s from %s, is the field private?", field.getName(), table.getTableName()));
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
        return cv;
    }

    /**
     * Add column value into ContentValues accroding to column type.
     * @param cv the ContentValues to add into.
     * @param columnDef column definition.
     * @param object the value of the column from/to db.
     */
    public static void setColumnValue(ContentValues cv, DbColumnDef columnDef, Object object){
        String key = columnDef.getColumn();
        switch (columnDef.getType()){
            case TEXT:
                cv.put(key, String.valueOf(object));
                break;
            case INTEGER:
                cv.put(key, (Integer)object);
                break;
            case REAL:
                cv.put(key, (Double)object);
                break;
            case BLOB:
                cv.put(key, (byte[])object);
                break;
        }
    }
}
