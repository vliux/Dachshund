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
import com.vliux.dachshund.bean.DbColumnDef;
import com.vliux.dachshund.bean.DbTableDef;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(DbManager.TAG, "onCreate()");
        for (Class tableClass : DbManager.getsInstance().getTableClasses()) {
            String sql = getCreateSql(tableClass);
            if (null != sql && sql.length() > 0) {
                sqLiteDatabase.execSQL(sql);
            }
        }
    }

    private String getCreateSql(Class tableClass) {
        DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(tableClass);
        HashMap<Field, DbColumnDef> columnDefs = DbManager.getsInstance().getColumnDefinitions(tableClass);

        StringBuilder sb = new StringBuilder("CREATE TABLE " + tableDef.getTableName() +
                String.format(" (%s INTEGER DEFAULT '0' NOT NULL PRIMARY KEY AUTOINCREMENT", BaseDbTable.PRIMARY_COLUMN_NAME));
        for (Field field : columnDefs.keySet()) {
            DbColumnDef colDef = columnDefs.get(field);
            sb.append(String.format(Locale.US,
                    ",%s %s DEFAULT '%s'", colDef.getColumn(), colDef.getType().name(), colDef.getDefaultValue()));
        }
        sb.append(")");
        Log.i(DbManager.TAG, sb.toString());
        return sb.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        Log.d(DbManager.TAG, "onUpgrade()");
        for (Class tableClass : DbManager.getsInstance().getTableClasses()) {
            DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(tableClass);
            if (tableDef.getMinVersion() >= newVer) {
                Log.d(DbManager.TAG,
                        String.format("table %s has annotation with minVersion >= %d", tableDef.getTableName(), newVer));
                String createSql = getCreateSql(tableClass);
                if (null == createSql || createSql.length() <= 0) {
                    throw new IllegalStateException(String.format("getCreateSql() of table %s is NULL",
                            tableDef.getTableName()));
                }
                Log.d(DbManager.TAG,
                        "use CREATE to upgrade: " + createSql);
                sqLiteDatabase.execSQL(createSql);
                continue;
            }

            /* otherwise use ALTER to upgrade the table */
            String[] sqls = getUpdateSql(tableClass, oldVer, newVer);
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

    private String[] getUpdateSql(Class tableClass, int oldVer, int newVer) {
        List<String> sqlStrList = new ArrayList<String>();
        DbTableDef tableDef = DbManager.getsInstance().getTableDefinition(tableClass);
        HashMap<Field, DbColumnDef> columnDefs = DbManager.getsInstance().getColumnDefinitions(tableClass);

        for(Field field : columnDefs.keySet()){
            DbColumnDef dbColumnDef = columnDefs.get(field);
            int columnVer = dbColumnDef.getIntroducedVersion();
            if(columnVer > oldVer && columnVer <= newVer){
                String sqlStr = String.format("ALTER TABLE %s ADD %s %s DEFAULT '%s'",
                        tableDef.getTableName(), dbColumnDef.getColumn(), dbColumnDef.getType().name(), dbColumnDef.getDefaultValue());
                sqlStrList.add(sqlStr);
                Log.d(DbManager.TAG, "getUpdateSql(): " + sqlStr);
            }
        }
        return sqlStrList.toArray(new String[0]);
    }


}

