package com.vliux.dachshund;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.vliux.dachshund.annotation.DbTable;
import com.vliux.dachshund.bean.DbColumnDef;
import com.vliux.dachshund.bean.DbTableDef;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vliux on 12/10/13.
 */
public class Dao {

    public List<BaseDbTable> query(BaseDbTable dbTable, String[] whereFieldNames, Object[] whereValues, int[] limits){
        List<BaseDbTable> retValues = new ArrayList<BaseDbTable>();

        HashMap<Field, DbColumnDef> columnDefMap = DbManager.getsInstance().getColumnDefinitions(dbTable.getClass());
        if(null == columnDefMap){
            throw new IllegalArgumentException(String.format("class %s is not a db table class", dbTable.getClass().getSimpleName()));
        }

        DbColumnDef[] columnDefs = columnDefMap.values().toArray(new DbColumnDef[0]);
        String[] columns = new String[columnDefs.length + 1];
        columns[0] = BaseDbTable.PRIMARY_COLUMN_NAME;
        for(int i = 0; i < columnDefs.length; i++){
            columns[i + 1] = columnDefs[i].getColumn();
        }

        Cursor cursor = __select(dbTable, whereFieldNames, whereValues, limits);
        if(null != cursor && cursor.getCount() > 0){
            cursor.moveToFirst();
            for(; !cursor.isAfterLast(); cursor.moveToNext()){
                BaseDbTable dbTableInstance = null;
                try {
                    dbTableInstance = (BaseDbTable)dbTable.getClass().newInstance();
                    dbTableInstance.setId(cursor.getInt(0));
                    for(int i = 1; i < columnDefs.length; i++){
                        DbColumnDef cd = columnDefs[i];
                        Field field = cd.getField();
                        if(null == field){
                            throw new IllegalStateException(String.format("'field' of DbColumnDef %s is null", cd.getColumn()));
                        }
                        field.setAccessible(true);

                        if(DbType.TEXT == cd.getType()){
                            field.set(dbTableInstance, cursor.getString(i));
                        }else if(DbType.INTEGER == cd.getType()){
                            field.set(dbTableInstance, cursor.getInt(i));
                        }else if(DbType.REAL == cd.getType()){
                            field.set(dbTableInstance, cursor.getDouble(i));
                        }else if(DbType.BLOB == cd.getType()){
                            field.set(dbTableInstance, cursor.getBlob(i));
                        }
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

                retValues.add(dbTableInstance);
            }
        }
        return retValues;
    }

    private Cursor __select(BaseDbTable dbTable, String[] whereFieldNames, Object[] whereValues, int[] limits) throws IllegalArgumentException {
        HashMap<Field, DbColumnDef> columnDefMap = DbManager.getsInstance().getColumnDefinitions(dbTable.getClass());
        // generate columnNames, SQL WHERE clause and selectArgs
        List<String> columnNames = new ArrayList<String>();
        String whereClause = "";
        ArrayList<String> selectArgs = new ArrayList<String>();
        if (null == whereFieldNames || whereFieldNames.length <= 0) {
            whereClause = null;
            for(DbColumnDef columnDef : columnDefMap.values()){
                columnNames.add(columnDef.getColumn());
            }
        } else {
            for(int i = 0; i < whereFieldNames.length; i++){
                String fieldName = whereFieldNames[i];
                if(null == fieldName || fieldName.length() <= 0){
                    throw new IllegalArgumentException("invalid empty field in whereFieldNames");
                }

                Field field = DbManager.getsInstance().getField(dbTable.getClass(), fieldName);
                if(null == field){
                    throw new IllegalArgumentException(String.format("field %s is not a db table annotated field", fieldName));
                }else{
                    String columnName = columnDefMap.get(field).getColumn();
                    columnNames.add(columnName);
                    String clause = String.format("%s=?", columnName);
                    if (whereClause.length() > 0) {
                        whereClause += String.format(" AND %s", clause);
                    } else {
                        whereClause += clause;
                    }
                    String value = String.valueOf(whereValues[i]);
                    selectArgs.add(value);
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
            return db.query(DbManager.getsInstance().getTableDefinition(dbTable.getClass()).getTableName(),
                    columnNames.toArray(new String[0]),
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
            /* no need to close SQLiteDatabase as it is cached by Helper
             * if close it here, later use of the SQLiteDatabase obj will throw NullPointerException
             * if(null != db){
             *   db.close();
             * } */
        }
    }

    public boolean insert(SQLiteDatabase sqliteDb, BaseDbTable dbTable){
        DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(dbTable.getClass());
        if(null == tableDef){
            throw new IllegalStateException(String.format("class %s not registered as db table", dbTable.getClass().getSimpleName()));
        }

        ContentValues cv = DbUtil.getColumnValues(dbTable);

        try{
            sqliteDb.beginTransaction();
            long ret = sqliteDb.insert(tableDef.getTableName(), null, cv);
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

    public boolean insert(BaseDbTable dbTable) {
        SQLiteDatabase sqliteDb = null;
        DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(dbTable.getClass());
        if(null == tableDef){
            throw new IllegalStateException(String.format("class %s not registered as db table", dbTable.getClass().getSimpleName()));
        }

        ContentValues cv = DbUtil.getColumnValues(dbTable);

        try {
            sqliteDb = DbManager.getsInstance().getHelper().getWritableDatabase();
            sqliteDb.beginTransaction();
            long ret = sqliteDb.insert(tableDef.getTableName(), null, cv);
            if (ret != -1L) {
                sqliteDb.setTransactionSuccessful();
            }
            return (-1L != ret ? true : false);
        } catch (SQLiteException ex) {
            Log.e(DbManager.TAG, ex.toString());
            throw ex;
        } finally {
            if (null != sqliteDb) {
                sqliteDb.endTransaction();
            }
        }
    }

    public boolean update(BaseDbTable dbTable, String[] fieldsToUpdate) {
        DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(dbTable.getClass());
        if(null == tableDef){
            throw new IllegalStateException(String.format("class %s is not registered as db table", dbTable.getClass().getSimpleName()));
        }

        SQLiteDatabase sqliteDb = null;
        /* generate ContentValues */
        ContentValues cv = null;
        if(null == fieldsToUpdate || fieldsToUpdate.length <= 0){
            cv = DbUtil.getColumnValues(dbTable);
        }else{
            cv = new ContentValues();
            for(String fieldName : fieldsToUpdate){
                Field field = DbManager.getsInstance().getField(dbTable.getClass(), fieldName);
                HashMap<Field, DbColumnDef> columnDefMap = DbManager.getsInstance().getColumnDefinitions(dbTable.getClass());
                if(null == field || null == columnDefMap){
                    throw new IllegalArgumentException(String.format("field %s is not a registered db table class field", fieldName));
                }
                DbColumnDef columnDef = columnDefMap.get(field);
                try {
                    DbUtil.setColumnValue(cv, columnDef, field.get(dbTable));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            sqliteDb = DbManager.getsInstance().getHelper().getWritableDatabase();
            sqliteDb.beginTransaction();
            int ret = sqliteDb.update(tableDef.getTableName(),
                    cv,
                    BaseDbTable.PRIMARY_COLUMN_NAME + "=?",
                    new String[]{String.valueOf(dbTable.getId())});
            if (ret > 0) {
                sqliteDb.setTransactionSuccessful();
            }
            return (0 < ret ? true : false);
        } catch (SQLiteException ex) {
            Log.e(DbManager.TAG, ex.toString());
            throw ex;
        } finally {
            if (null != sqliteDb) {
                sqliteDb.endTransaction();
            }
        }
    }

    public boolean delete(BaseDbTable dbTable) {
        SQLiteDatabase sqliteDb = null;
        DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(dbTable.getClass());

        try {
            sqliteDb = DbManager.getsInstance().getHelper().getWritableDatabase();
            sqliteDb.beginTransaction();
            int ret = sqliteDb.delete(tableDef.getTableName(),
                    BaseDbTable.PRIMARY_COLUMN_NAME + "=?",
                    new String[]{String.valueOf(dbTable.getId())});
            if (ret > 0) {
                sqliteDb.setTransactionSuccessful();
            }
            return (0 < ret ? true : false);
        } catch (SQLiteException ex) {
            Log.e(DbManager.TAG, ex.toString());
            throw ex;
        } finally {
            if (null != sqliteDb) {
                sqliteDb.endTransaction();
            }
        }
    }

}
