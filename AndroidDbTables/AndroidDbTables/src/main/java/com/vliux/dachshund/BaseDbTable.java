package com.vliux.dachshund;

/**
 * Created by vliux on 9/21/13.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.vliux.dachshund.bean.DbColumnDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class BaseDbTable{
    protected final String PRIMARY_COLUMN_NAME = "_id";
    private DbManager mDbManger;
    private SQLiteOpenHelper mDbHelper;
    private String mTableName;

    protected BaseDbTable(SQLiteOpenHelper dbHelper, DbManager dbManager) {
        mDbHelper = dbHelper;
        mDbManger = dbManager;
    }

    /**
     * Get all column definitions except the primary key "_id".
     * @return an array of DbColumnDef objects.
     */
    public final DbColumnDef[] getColumnDefinitions(){
        return mDbManger.getColumnDefs(getClass()).values().toArray(new DbColumnDef[0]);
    }

    /**
     * @return the SQL string for creating table.
     */
    public String getCreateSql() {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + getTableName() +
                String.format(" (%s INTEGER DEFAULT '0' NOT NULL PRIMARY KEY AUTOINCREMENT", PRIMARY_COLUMN_NAME));
        for (String colName : mDbManger.getColumnDefs(getClass()).keySet()) {
            DbColumnDef colDef = mDbManger.getColumnDefs(getClass()).get(colName);
            if(colDef.getType() == DbColumnType.FOREIGN_KEY){
                sb.append(String.format(Locale.US,
                        ",%s INTEGER DEFAULT -1", colDef.getColumn()));
                sb.append(String.format(Locale.US,
                        ",FOREIGN KEY(%s) REFERENCES %s(%s)",
                        colDef.getColumn(),
                        mDbManger.getTableName(colDef.getForeignReferTo()),
                        PRIMARY_COLUMN_NAME));
            }else {
                sb.append(String.format(Locale.US,
                        ",%s %s DEFAULT '%s'", colDef.getColumn(), colDef.getType().name(), colDef.getDefaultValue()));
            }
        }
        sb.append(")");
        Log.i(DbManager.TAG, sb.toString());
        return sb.toString();
    }

    /**
     * TODO: if add foreign key in new ver, need to re-create the table, then copy the old table data.
     * @param oldVer
     * @param newVer
     * @return a list of SQL strings of altering table from version oldVer to newVer.
     */
    public String[] getUpdateSql(int oldVer, int newVer) {
        List<String> sqlStrList = new ArrayList<String>();

        for(String colName : mDbManger.getColumnDefs(getClass()).keySet()){
            DbColumnDef dbColumnDef = mDbManger.getColumnDefs(getClass()).get(colName);
            int columnVer = dbColumnDef.getIntroducedVersion();
            if(columnVer > oldVer && columnVer <= newVer){
                String sqlStr = null;
                if(dbColumnDef.getType() == DbColumnType.FOREIGN_KEY){
                    sqlStr = String.format("ALTER TABLE %s ADD %s INTEGER DEFAULT -1",
                            getTableName(),
                            dbColumnDef.getColumn());
                }else{
                    sqlStr = String.format("ALTER TABLE %s ADD %s %s DEFAULT '%s'",
                            getTableName(),
                            dbColumnDef.getColumn(),
                            dbColumnDef.getType().name(),
                            dbColumnDef.getDefaultValue());
                }
                sqlStrList.add(sqlStr);
                Log.d(DbManager.TAG, "getUpdateSql(): " + sqlStr);
            }
        }
        return sqlStrList.toArray(new String[0]);
    }

    /**
     * Called right after the table is created in the database.
     * Derived classes can override their own logic for table initialization.
     * @param db
     */
    public abstract void onTableCreated(SQLiteDatabase db);

    /**
     * Called right after the table is upgraded from oldVersion to newVersion.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public abstract void onTableUpdated(SQLiteDatabase db, int oldVersion, int newVersion);

    public final String getTableName(){
        if(TextUtils.isEmpty(mTableName)){
            mTableName = mDbManger.getTableName(getClass());
        }
        return mTableName;
    }

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
    public List<ContentValues> query(ContentValues whereArgs, int[] limits){
        List<ContentValues> retValues = new ArrayList<ContentValues>();

        DbColumnDef[] columnDefs = getColumnDefinitions();
        String[] columns = new String[columnDefs.length + 1];
        columns[0] = PRIMARY_COLUMN_NAME;
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
                    if(DbColumnType.TEXT == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getString(i + 1));
                    }else if(DbColumnType.INTEGER == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getInt(i + 1));
                    }else if(DbColumnType.REAL == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getDouble(i + 1));
                    }else if(DbColumnType.BLOB == cd.getType()){
                        cv.put(cd.getColumn(), cursor.getBlob(i + 1));
                    }
                }
                retValues.add(cv);
            }
        }
        return retValues;
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
                if (mDbManger.getColumnDefs(getClass()).keySet().contains(key)) {
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

