package com.vliux.dachshundsample;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbManager;

/**
 * Created by vliux on 12/6/13.
 */
public class SampleTable extends BaseDbTable {
    private static final String DB_COL_SNAME_TEXT_1 = "sName";
    private static final String DB_COL_NUM_INT_1 = "num";

    public SampleTable(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public void onTableCreated(SQLiteDatabase db) {
        Log.d(DbManager.TAG, "SampleTable.onTableCreated()");
        ContentValues cv = new ContentValues();
        cv.put(DB_COL_SNAME_TEXT_1, "sampleee");
        cv.put(DB_COL_NUM_INT_1, 1123);
        insert(cv);
    }

    public void insertSomething(){
        ContentValues cv = new ContentValues();
        cv.put(DB_COL_SNAME_TEXT_1, "sampleee2");
        cv.put(DB_COL_NUM_INT_1, 1124);
        insert(cv);
    }
}