package com.vliux.dachshundsample;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbManager;
import com.vliux.dachshund.annotation.DbField;

/**
 * Created by vliux on 12/6/13.
 */
public class SampleTable extends BaseDbTable {
    @DbField(columnType = "TEXT", defaultValue = "liuxin", minVersion = 1)
    private static String columnName = "theName";

    @DbField(columnType = "INTEGER")
    private static String columnAge = "theAge";

    @DbField(columnType = "TEXT", minVersion = 2)
    private static String columnDes = "theDesc";

    @DbField(columnType = "TEXT", minVersion = 3)
    private static String columnExtra = "theExtra";

    public SampleTable(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public void onTableCreated(SQLiteDatabase db) {
        Log.d(DbManager.TAG, "SampleTable.onTableCreated()");
        ContentValues cv = new ContentValues();
        cv.put(columnName, "sampleee");
        cv.put(columnAge, 1123);
        //cv.put(columnDes, "...");
        insert(db, cv);
    }

    public void insertSomething(){
        ContentValues cv = new ContentValues();
        cv.put(columnName, "sampleee2");
        cv.put(columnAge, 1124);
        //cv.put(columnDes, "some description here");
        insert(cv);
    }
}