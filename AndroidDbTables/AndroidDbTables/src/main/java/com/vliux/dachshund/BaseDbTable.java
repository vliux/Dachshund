package com.vliux.dachshund;

/**
 * Created by vliux on 9/21/13.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.annotation.DbField;
import com.vliux.dachshund.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class BaseDbTable{
    protected final String PRIMARY_COLUMN_NAME = "_id";
    protected DbTableDef mDbTableDef;
    protected HashMap<String, DbColumnDef> mColumnDefinitions = new HashMap<String, DbColumnDef>();
    private SQLiteOpenHelper mDbHelper;

    protected BaseDbTable(SQLiteOpenHelper dbHelper) {
        mDbHelper = dbHelper;
        detectAnnotations();
    }

    protected void detectAnnotations(){
        Class theClass = this.getClass();
        /* check DbTable annotation */
        if(theClass.isAnnotationPresent(DbTable.class)){
            mDbTableDef = new DbTableDef();
            DbTable dbTableAnnotation = (DbTable) theClass.getAnnotation(DbTable.class);
            mDbTableDef.setMinVersion(dbTableAnnotation.minVersion());
            Log.d(DbManager.TAG, String.format("table %s has DbTable annotation, minVersion = %d", getTableName(), mDbTableDef.getMinVersion()));
        }

        /* check DbField annotations */
        while(true){
            if(null == theClass || theClass.equals(BaseDbTable.class)){
                break;
            }

            Field[] fields = theClass.getDeclaredFields();
            for (Field field : fields) {
                Log.d(DbManager.TAG, "check field " + field.getName());
                if (field.isAnnotationPresent(DbField.class)) {
                    String fieldName = field.getName();
                    Log.d(DbManager.TAG, String.format("field %s has DbField annotation", fieldName));
                    DbField dbField = field.getAnnotation(DbField.class);
                    String columnType = dbField.columnType();
                    String defaultValue = dbField.defaultValue();
                    int minVersion = dbField.minVersion();
                    if(minVersion <= 0 && null != mDbTableDef){
                        minVersion = mDbTableDef.getMinVersion();
                        Log.d(DbManager.TAG, "minVersion invalid from DbField, try to obtain from DbTable, which is " + minVersion);
                    }
                    /* obtain field value as column name*/
                    String columnName = null;
                    try {
                        field.setAccessible(true);
                        columnName = (String) field.get(null);
                    } catch (IllegalAccessException e) {
                        try {
                            columnName = (String) field.get(this);
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                            IllegalArgumentException thrownExp =
                                    new IllegalArgumentException("unable to get value of field " + fieldName);
                            thrownExp.initCause(e);
                            throw thrownExp;
                        }
                    }

                    if (null == columnType || columnType.length() <= 0) {
                        throw new IllegalArgumentException(String.format("invalid db date type %s annotated at field %s",
                                String.valueOf(columnType), fieldName));
                    }

                    if (null == columnName || columnName.length() <= 0) {
                        throw new IllegalArgumentException(String.format("invalid column name %s annotated at field %s",
                                String.valueOf(columnName), fieldName));
                    }
                    Log.d(DbManager.TAG, "db columnName = " + columnName);

                    if (minVersion <= 0) {
                        throw new IllegalArgumentException(String.format("invalid minVersion %d annotated at field %s",
                                minVersion, fieldName));
                    }

                    DbColumnDef dbColumnDef = new DbColumnDef();
                    dbColumnDef.setColumn(columnName);
                    dbColumnDef.setDefaultValue(defaultValue);
                    dbColumnDef.setIntroducedVersion(minVersion);
                    dbColumnDef.setType(columnType);
                    mColumnDefinitions.put(columnName, dbColumnDef);

                }
            }
            theClass = theClass.getSuperclass();
        }
    }

    /**
     * Get all column definitions except the primary key "_id".
     * @return an array of DbColumnDef objects
     */
    public final DbColumnDef[] getColumnDefinitions(){
        return mColumnDefinitions.values().toArray(new DbColumnDef[0]);
    }

    public final DbTableDef getTableDefinition(){
        return mDbTableDef;
    }

    public String getTableName() {
        return getClass().getSimpleName();
    }

    public String getCreateSql() {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + getTableName() +
                String.format(" (%s INTEGER DEFAULT '0' NOT NULL PRIMARY KEY AUTOINCREMENT", PRIMARY_COLUMN_NAME));
        for (String colName : mColumnDefinitions.keySet()) {
            DbColumnDef colDef = mColumnDefinitions.get(colName);
            sb.append(String.format(Locale.US,
                    ",%s %s DEFAULT '%s'", colDef.getColumn(), colDef.getType(), colDef.getDefaultValue()));
        }
        sb.append(")");
        Log.i(DbManager.TAG, sb.toString());
        return sb.toString();
    }

    public String[] getUpdateSql(int oldVer, int newVer) {
        List<String> sqlStrList = new ArrayList<String>();

        for(String colName : mColumnDefinitions.keySet()){
            DbColumnDef dbColumnDef = mColumnDefinitions.get(colName);
            int columnVer = dbColumnDef.getIntroducedVersion();
            if(columnVer > oldVer && columnVer <= newVer){
                String sqlStr = String.format("ALTER TABLE %s ADD %s %s DEFAULT '%s'",
                        getTableName(), dbColumnDef.getColumn(), dbColumnDef.getType(), dbColumnDef.getDefaultValue());
                sqlStrList.add(sqlStr);
                Log.d(DbManager.TAG, "getUpdateSql(): " + sqlStr);
            }
        }
        return sqlStrList.toArray(new String[0]);
    }

    public  abstract void onTableCreated(SQLiteDatabase db);

    protected boolean insert(SQLiteDatabase sqliteDb, ContentValues cv){
        try{
            sqliteDb.beginTransaction();
            long ret = sqliteDb.insert(getTableName(), null, cv);
            if (ret != -1L) {
                sqliteDb.setTransactionSuccessful();
            }
            return (-1L != ret ? true : false);
        }catch(SQLiteException e){
            e.printStackTrace();
            throw e;
        }finally {
            if(null != sqliteDb){
                sqliteDb.endTransaction();
            }
        }
    }

    protected boolean insert(ContentValues cv) {
        SQLiteDatabase sqliteDb = null;
        try {
            sqliteDb = mDbHelper.getWritableDatabase();
            sqliteDb.beginTransaction();
            long ret = sqliteDb.insert(getTableName(), null, cv);
            if (ret != -1L) {
                sqliteDb.setTransactionSuccessful();
            }
            return (-1L != ret ? true : false);
        } catch (SQLiteException ex) {
            Log.e(getTableName(), ex.toString());
            throw ex;
        } finally {
            if (null != sqliteDb) {
                sqliteDb.endTransaction();
            }
        }
    }

    protected boolean update(ContentValues cv, String whereClause, String[] whereArgs) {
        SQLiteDatabase sqliteDb = null;
        try {
            sqliteDb = mDbHelper.getWritableDatabase();
            sqliteDb.beginTransaction();
            int ret = sqliteDb.update(getTableName(), cv, whereClause, whereArgs);
            if (ret > 0) {
                sqliteDb.setTransactionSuccessful();
            }
            return (0 < ret ? true : false);
        } catch (SQLiteException ex) {
            Log.e(getTableName(), ex.toString());
            throw ex;
        } finally {
            if (null != sqliteDb) {
                sqliteDb.endTransaction();
            }
        }
    }

    protected boolean delete(String whereClause, String[] whereArgs) {
        SQLiteDatabase sqliteDb = null;
        try {
            sqliteDb = mDbHelper.getWritableDatabase();
            sqliteDb.beginTransaction();
            int ret = sqliteDb.delete(getTableName(), whereClause, whereArgs);
            if (ret > 0) {
                sqliteDb.setTransactionSuccessful();
            }
            return (0 < ret ? true : false);
        } catch (SQLiteException ex) {
            Log.e(getTableName(), ex.toString());
            throw ex;
        } finally {
            if (null != sqliteDb) {
                sqliteDb.endTransaction();
            }
        }
    }

    protected Cursor __select(String[] columns, ContentValues cv, int[] limits) throws IllegalArgumentException {
        // generate SQL WHERE clause and selectArgs
        String whereClause = "";
        ArrayList<String> selectArgs = new ArrayList<String>();
        if (null == cv || cv.size() <= 0) {
            whereClause = null;
        } else {
            for (Map.Entry<String, Object> entry : cv.valueSet()) {
                String key = entry.getKey();
                if (mColumnDefinitions.keySet().contains(key)) {
                    String clause = String.format("%s=?", key);
                    if (whereClause.length() > 0) {
                        whereClause += String.format(" AND %s", clause);
                    } else {
                        whereClause += clause;
                    }
                    String value = "";
                    Object object = entry.getValue();
                    try {
                        value = (String) object;
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = String.valueOf(object);
                    }
                    selectArgs.add(value);
                } else {
                    throw new IllegalArgumentException(String.format("'%s' is not a db column", key));
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
            db = mDbHelper.getReadableDatabase();
            return db.query(getTableName(),
                    columns,
                    whereClause,
                    (null != whereClause ? selectArgs.toArray(new String[selectArgs.size()]) : null),
                    null,
                    null,
                    null,
                    limitClause);
        } catch (SQLiteException exp) {
            Log.e(getTableName(), exp.toString());
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

