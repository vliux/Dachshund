package com.vliux.dachshund;

/**
 * Created by vliux on 9/21/13.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.DropBoxManager;
import android.util.Log;

import com.vliux.dachshund.annotation.DbTable;

class DbHelper extends SQLiteOpenHelper {
    private BaseDbTable[] mTables;

    public DbHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    public void setDbTables(BaseDbTable[] dbTables) {
        mTables = dbTables;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(DbManager.TAG, "onCreate()");
        for (BaseDbTable table : mTables) {
            String sql = table.getCreateSql();
            if (null != sql && sql.length() > 0) {
                sqLiteDatabase.execSQL(sql);
                table.onTableCreated(sqLiteDatabase);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        Log.d(DbManager.TAG, "onUpgrade()");
        for (BaseDbTable table : mTables) {
            /* if current table is newly added in 'newVer', use CREATE TABLE instead of ALTER TABLE */
            if(null != DbManager.getsInstance().getTableDef(table.getClass())){
                if(DbManager.getsInstance().getTableDef(table.getClass()).getMinVersion() >= newVer){
                    Log.d(DbManager.TAG,
                            String.format("table %s has annotation with minVersion >= %d", table.getTableName(), newVer));
                    String createSql = table.getCreateSql();
                    if(null == createSql || createSql.length() <= 0){
                        throw new IllegalStateException(String.format("getCreateSql() of table %s is NULL",
                                table.getTableName()));
                    }
                    Log.d(DbManager.TAG,
                            "use CREATE to upgrade: " + createSql);
                    sqLiteDatabase.execSQL(createSql);
                    continue;
                }
            }
            /* otherwise use ALTER to upgrade the table */
            String[] sqls = table.getUpdateSql(oldVer, newVer);
            if(null != sqls && sqls.length > 0){
                for(String sql : sqls){
                    if (null != sql && sql.length() > 0) {
                        Log.d(DbManager.TAG, "upgrade: " + sql);
                        sqLiteDatabase.execSQL(sql);
                    }
                }
            }
        }
    }

}

