package com.vliux.dachshundsample;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbManager;

/**
 * Created by vliux on 12/6/13.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            DbManager.init(getApplicationContext(),new Class[]{SampleTable.class});
            SampleTable sampleTable = (SampleTable)DbManager.getsInstance().getTable(SampleTable.class);
            sampleTable.insertSomething();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

}
