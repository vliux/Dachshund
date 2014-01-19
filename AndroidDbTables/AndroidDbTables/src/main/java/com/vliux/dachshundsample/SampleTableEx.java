package com.vliux.dachshundsample;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbColumnType;
import com.vliux.dachshund.DbManager;
import com.vliux.dachshund.annotation.DbField;
import com.vliux.dachshund.annotation.DbTable;
import com.vliux.dachshund.annotation.ForeignKey;

import java.util.Date;

/**
 * Created by vliux on 12/8/13.
 */
@DbTable(minVersion = 5)
public class SampleTableEx extends BaseDbTable{
    @DbField(columnType = DbColumnType.TEXT)
    private static final String exName = "exName";

    @DbField(columnType = DbColumnType.INTEGER)
    private static final String exGrade = "exGrade";

    @ForeignKey(referTo = SampleTable.class)
    private static final String exForeignSampleTable = "exForeignSample";

    public SampleTableEx(SQLiteOpenHelper dbHelper, DbManager dbManager) {
        super(dbHelper, dbManager);
    }

    @Override
    public void onTableCreated(SQLiteDatabase db) {
        addRandomRow(db);
    }

    @Override
    public void onTableUpdated(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void addRandomRow(){
        Date current = new Date();
        ContentValues cv = new ContentValues();
        cv.put(exName, String.format("name-%s-%s-%s", current.getHours(), current.getMinutes(), current.getSeconds()));
        cv.put(exGrade, current.getSeconds());
        //cv.put(columnDes, "some description here");
        insert(cv);
    }

    private void addRandomRow(SQLiteDatabase db){
        Date current = new Date();
        ContentValues cv = new ContentValues();
        cv.put(exName, String.format("name-%s-%s-%s", current.getHours(), current.getMinutes(), current.getSeconds()));
        cv.put(exGrade, current.getSeconds());
        //cv.put(columnDes, "some description here");
        insert(db, cv);
    }
}
