package com.vliux.dachshund;

/**
 * Created by vliux on 9/21/13.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
            String[] sqls = table.getUpdateSql(oldVer, newVer);
            for(String sql : sqls){
                if (null != sql && sql.length() > 0) {
                    sqLiteDatabase.execSQL(sql);
                }
            }
        }
    }

}

