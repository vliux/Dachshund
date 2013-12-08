package com.vliux.dachshundsample;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbColumnDef;
import com.vliux.dachshund.DbManager;
import com.vliux.dachshund.annotation.DbField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        addRandomRow();
    }

    public void addRandomRow(){
        Date current = new Date();
        ContentValues cv = new ContentValues();
        cv.put(columnName, String.format("name-%s-%s-%s", current.getHours(), current.getMinutes(), current.getSeconds()));
        cv.put(columnAge, current.getSeconds());
        //cv.put(columnDes, "some description here");
        insert(cv);
    }

    public List<ContentValues> queryAll(){
        List<ContentValues> retValues = new ArrayList<ContentValues>();

        DbColumnDef[] columnDefs = getColumnDefinitions();
        String[] columns = new String[columnDefs.length + 1];
        columns[0] = PRIMARY_COLUMN_NAME;
        for(int i = 0; i < columnDefs.length; i++){
            columns[i + 1] = columnDefs[i].getColumn();
        }

        Cursor cursor = __select(columns, null, null);
        if(null != cursor && cursor.getCount() > 0){
            cursor.moveToFirst();
            for(; !cursor.isAfterLast(); cursor.moveToNext()){
                ContentValues cv = new ContentValues();
                cv.put(PRIMARY_COLUMN_NAME, cursor.getInt(0));
                for(int i = 0; i < columnDefs.length; i++){
                    DbColumnDef cd = columnDefs[i];
                    if(cd.getType().equalsIgnoreCase("text")){
                        cv.put(cd.getColumn(), cursor.getString(i+1));
                    }else if(cd.getType().equalsIgnoreCase("integer")){
                        cv.put(cd.getColumn(), cursor.getInt(i+1));
                    }
                }
                retValues.add(cv);
            }
        }
        return retValues;
    }
}