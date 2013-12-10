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
import com.vliux.dachshund.bean.DbColumnDef;
import com.vliux.dachshund.bean.DbTableDef;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class BaseDbTable{
    public static final String PRIMARY_COLUMN_NAME = "_id";

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

    /**
     *
     * @param whereArgs
     * @param limits
     * @return list of ContentValues, a ContentValues represents one row.
     */




}

