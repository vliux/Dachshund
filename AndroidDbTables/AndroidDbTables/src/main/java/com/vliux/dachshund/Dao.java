package com.vliux.dachshund;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.vliux.dachshund.bean.DbColumnDef;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vliux on 12/10/13.
 */
public class Dao {

    public List<ContentValues> query(Class tableClass, ContentValues whereArgs, int[] limits){
        List<ContentValues> retValues = new ArrayList<ContentValues>();

        HashMap<Field, DbColumnDef> columnDefMap = DbManager.getsInstance().getColumnDefinitions(tableClass);
        if(null == columnDefMap){
            throw new IllegalArgumentException(String.format("class %s is not a db table class", tableClass.getSimpleName()));
        }

        String[] columns = new String[columnDefMap.values().size() + 1];
        columns[0] = BaseDbTable.PRIMARY_COLUMN_NAME;
        for(int i = 0; i < columnDefs.length; i++){
            columns[i + 1] = columnDefs[i].getColumn();
        }

        Cursor cursor = __select(columns, whereArgs, limits);
        if(null != cursor && cursor.getCount() > 0){
            cursor.moveToFirst();
            for(; !cursor.isAfterLast(); cursor.moveToNext()){
                ContentValues cv = new ContentValues();
                cv.put(PRIMARY_COLUMN_NAME, cursor.getInt(0));
                for(int i = 0; i < columnDefs.length; i++){
                    DbColumnDef cd = columnDefs[i];
                    if(DbType.TEXT == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getString(i + 1));
                    }else if(DbType.INTEGER == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getInt(i + 1));
                    }else if(DbType.REAL == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getDouble(i + 1));
                    }else if(DbType.BLOB == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getBlob(i + 1));
                    }
                }
                retValues.add(cv);
            }
        }
        return retValues;
    }

    /**
     *
     * @param tableClass
     * @param columns
     * @param whereArgs columnName --> columnValue
     * @param limits
     * @return
     * @throws IllegalArgumentException
     */
    private Cursor __select(Class tableClass, String[] columns, ContentValues whereArgs, int[] limits) throws IllegalArgumentException {
        // generate SQL WHERE clause and selectArgs
        String whereClause = "";
        ArrayList<String> selectArgs = new ArrayList<String>();
        if (null == whereArgs || whereArgs.size() <= 0) {
            whereClause = null;
        } else {
            for (Map.Entry<String, Object> entry : whereArgs.valueSet()) {
                String columnName = entry.getKey();
                if (null != DbManager.getsInstance().findField(tableClass, columnName)) {
                    String clause = String.format("%s=?", columnName);
                    if (whereClause.length() > 0) {
                        whereClause += String.format(" AND %s", clause);
                    } else {
                        whereClause += clause;
                    }
                    String value = "";
                    Object object = entry.getValue();
                    value = String.valueOf(object);
                    selectArgs.add(value);
                } else {
                    throw new IllegalArgumentException(String.format("'%s' is not a db column", columnName));
                }
            }
        }
        // generate LIMIT clause
        String limitClause;
        if (null == limits || limits.length <= 0) {
            limitClause = null;
        } else if (limits.length == 1) {
            limitClause = String.format("%d", limits[0]);
        } else {
            limitClause = String.format("%d, %d", limits[0], limits[1]);
        }

        SQLiteDatabase db = null;
        try {
            db = DbManager.getsInstance().getHelper().getReadableDatabase();
            return db.query(DbManager.getsInstance().getTableDefinition(tableClass).getTableName(),
                    columns,
                    whereClause,
                    (null != whereClause ? selectArgs.toArray(new String[selectArgs.size()]) : null),
                    null,
                    null,
                    null,
                    limitClause);
        } catch (SQLiteException exp) {
            Log.e(DbManager.TAG, exp.toString());
            throw exp;
        } finally {
            // no need to close SQLiteDatabase as it is cached by Helper
            // if close it here, later use of the SQLiteDatabase obj will throw NullPointerException
            //if(null != db){
            //    db.close();
            //}
        }
    }
}
