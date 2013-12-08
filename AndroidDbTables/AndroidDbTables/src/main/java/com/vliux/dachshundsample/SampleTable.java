package com.vliux.dachshundsample;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbManager;
import com.vliux.dachshund.DbType;
import com.vliux.dachshund.annotation.DbField;

import java.util.Date;

/**
 * Created by vliux on 12/6/13.
 */
public class SampleTable extends BaseDbTable {
    @DbField(columnType = DbType.TEXT, defaultValue = "liuxin", minVersion = 1)
    private static String columnName = "theName";

    @DbField(columnType = DbType.INTEGER, minVersion = 1)
    private static String columnAge = "theAge";

    @DbField(columnType = DbType.TEXT, minVersion = 2)
    private static String columnDes = "theDesc";

    @DbField(columnType = DbType.TEXT, minVersion = 3)
    private static String columnExtra = "theExtra";

    public SampleTable(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public void onTableCreated(SQLiteDatabase db) {
        Log.d(DbManager.TAG, "SampleTable.onTableCreated()");
        addRandomRow(db);
    }

    @Override
    public void onTableUpdated(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }

    private void addRandomRow(SQLiteDatabase db){
        Date current = new Date();
        ContentValues cv = new ContentValues();
        cv.put(columnName, String.format("name-%s-%s-%s", current.getHours(), current.getMinutes(), current.getSeconds()));
        cv.put(columnAge, current.getSeconds());
        //cv.put(columnDes, "some description here");
        insert(db, cv);
    }

    public void addRandomRow(){
        Date current = new Date();
        ContentValues cv = new ContentValues();
        cv.put(columnName, String.format("name-%s-%s-%s", current.getHours(), current.getMinutes(), current.getSeconds()));
        cv.put(columnAge, current.getSeconds());
        insert(cv);
    }

}